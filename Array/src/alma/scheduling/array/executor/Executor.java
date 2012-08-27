/* ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2010
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 */
package alma.scheduling.array.executor;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import alma.Control.ExecBlockEndedEvent;
import alma.Control.ExecBlockStartedEvent;
import alma.SchedulingExceptions.InvalidOperationEx;
import alma.asdmIDLTypes.IDLEntityRef;
import alma.offline.ASDMArchivedEvent;
import alma.offline.SubScanProcessedEvent;
import alma.offline.SubScanSequenceEndedEvent;
import alma.scheduling.ArrayGUIOperation;
import alma.scheduling.SchedBlockExecutionItem;
import alma.scheduling.array.executor.services.Services;
import alma.scheduling.array.guis.ArrayGUINotification;
import alma.scheduling.array.sbQueue.SchedBlockItem;
import alma.scheduling.array.sessions.SessionManager;
import alma.scheduling.utils.ErrorHandling;
import alma.scheduling.utils.LoggerFactory;

/**
 * 
 * An instance of this class observes the EventReceiver, and is observed by the
 * ExecutorNotifier.
 * 
 * @author rhiriart
 *
 */
public class Executor extends Observable {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final BlockingQueue<SchedBlockItem> queue;
    private final String arrayName;
    private ExecutionContext currentExecution;
    private final List<ArchivalWaitThread> pastExecutions = new ArrayList<ArchivalWaitThread>();
    private Lock pastExecutionsLock = new ReentrantLock();
    private ExecutorConsumer executionThread;
    private boolean fullAuto;
    private boolean activeDynamic;
    private boolean manual;
    private boolean running;
    
    private Services services;
    private SessionManager sessions;
    
    private final static long ms      = 1000;         // milliseconds per second
    private final static long minutes = 60 * ms;      // milliseconds per minute
    private final static long hours   = 60 * minutes; // milliseconds per hour
    private long startEventTimeoutMS  =  5 * minutes;
    private long endedEventTimeoutMS  = 10 * ms;
    private long asdmArchiveTimeoutMS =  1 * hours;
    
    private class ExecutorConsumer extends Thread {
        
        private Executor executor;
        private boolean interruptRun = false;
        private AtomicBoolean inCriticalSection;
        private AtomicBoolean isRunning;
	
        public ExecutorConsumer(Executor e) {
            this.executor = e;
            inCriticalSection = new AtomicBoolean(false);
            isRunning = new AtomicBoolean(false);
        }
        
        @Override
        public void run() {
        	inCriticalSection.set(true);
        	while(!interruptRun) {
        		synchronized (this) {
        			// Synchronize so that this Executor will only have
        			// one current execution at once.
        			SchedBlockItem item = null;
        			try {
        				// Blocks waiting for an item.
        				item = queue.take();
        				isRunning.set(true);
        			} catch (InterruptedException e) {
        				logger.warning("executor consumer has been interrupted");
        				continue;
        			} finally {
        				inCriticalSection.set(false);
        			}
        			logger.info("executor consumer took item " + item);

        			try {
        				currentExecution = new ExecutionContext(item, executor, manual);
         				// Blocks until the execution has finished.
        				logger.finer(String.format("(%s) %s for %s just before %s.startObservation()",
        						this.getName(),
        						this.getClass().getSimpleName(),
        						item.getUid(),
        						currentExecution.getClass().getSimpleName()));
        				currentExecution.startObservation();
        				logger.finer(String.format("(%s) %s for %s just after %s.startObservation()",
        						this.getName(),
        						this.getClass().getSimpleName(),
        						item.getUid(),
        						currentExecution.getClass().getSimpleName()));

        				currentExecution.observe();
        				logger.finer(String.format("(%s) %s for %s just after %s.observe()",
        						this.getName(),
        						this.getClass().getSimpleName(),
        						item.getUid(),
        						currentExecution.getClass().getSimpleName()));
           			} catch (Exception e) {
           				currentExecution = null;
           				ErrorHandling.warning(logger,
           						String.format("Problem creating execution for SchedBlock %s",
           								item.getUid()), e);
        			} finally {
        				isRunning.set(false);
        			}
        			// The observation finished or failed.

        			if (currentExecution != null) {
        				// Pass on the execution to the pastExecutions list, where it will
        				// wait for the ASDMArchivedEvent.
        				ArchivalWaitThread wthr = new ArchivalWaitThread(currentExecution);
        				try {
        					pastExecutionsLock.lock();
        					pastExecutions.add(wthr);
        					logger.finer(String.format("(%s) %s for %s just before %s.start()",
        							this.getName(),
        							this.getClass().getSimpleName(),
        							item.getUid(),
        							wthr.getClass().getSimpleName()));
        					wthr.start();
        					logger.finer(String.format("(%s) %s for %s just after %s.start()",
        							this.getName(),
        							this.getClass().getSimpleName(),
        							item.getUid(),
        							wthr.getClass().getSimpleName()));
        					currentExecution = null;
        				} finally {
        					logger.finer(String.format("(%s) %s for %s in finally block after %s.start()",
        							this.getName(),
        							this.getClass().getSimpleName(),
        							item.getUid(),
        							wthr.getClass().getSimpleName()));
        					pastExecutionsLock.unlock();
        				}
        			}
        		}
        		inCriticalSection.set(true);
        	}
        	inCriticalSection.set(false);
        }

		public void stopRun() {
			interruptRun = true;
			if (this.inCriticalSection.get()) {
            	while (this.inCriticalSection.get()
            			&& this.getState() != Thread.State.WAITING) {
            		//Wait for the thread until it is waiting to take an item from the queue
    				try {
    					Thread.sleep(5);
    				} catch (InterruptedException e) {
    				}
            	}
            	if (this.inCriticalSection.get())
            		this.interrupt();
            }
		}
    }
    
    private class ArchivalWaitThread extends Thread {

    	private final ExecutionContext execution;

    	public ArchivalWaitThread(ExecutionContext execution) {
    		this.execution = execution;
    	}

    	@Override
    	public void run() {
    		// blocks waiting for archival or times out
    		logger.finer(String.format(
    				"(%s) %s for %s just before %s.waitForArchival()",
    				this.getName(),
    				this.getClass().getSimpleName(),
    				execution.getSbUid(),
    				execution.getClass().getSimpleName()));
    		execution.waitForArchival();
    		logger.finer(String.format(
    				"(%s) %s for %s just after %s.waitForArchival()",
    				this.getName(),
    				this.getClass().getSimpleName(),
    				execution.getSbUid(),
    				execution.getClass().getSimpleName()));
    	}

    	public ExecutionContext getContext() {
    		return execution;
    	}
    }
    
    public Executor(String arrayName, BlockingQueue<SchedBlockItem> queue) {
        this.arrayName = arrayName;
        this.queue = queue;
        fullAuto = false;
        manual   = false;
        running  = false;
//        activeObservations = new TreeMap<String, ExecStatusT>();
//        activeObsGroupBySbUid = new TreeMap<String, TreeMap<String,ExecStatusT>>();
    }

	public void start(String name, String role) {
		if (executionThread != null && executionThread.isRunning.get()) {
			executionThread.interruptRun = false;
		} else {
			executionThread = new ExecutorConsumer(this);
			executionThread.setPriority(executionThread.getPriority() - 1);
			executionThread.setDaemon(true);
			executionThread.start();
		}
		setRunning(true, name, role);
	}

    /**
     * Stops the execution thread.
     * <P>
     * This function will not stop the currently running SchedBlock.
     * To stop the currently executing SchedBlock, use stopCurrentExecution() 
     * @param role 
     * @param name 
     */
    public void stop(String name, String role) {
        if (executionThread != null) {
            executionThread.stopRun();
        }
        setRunning(false, name, role);
    }

    public void stopCurrentExecution(String name, String role) {
        if (currentExecution != null) {
            currentExecution.stopObservation();
        }
    }
    
    public void abortCurrentExecution(String name, String role) {
        if (currentExecution != null) {
            currentExecution.abortObservation();
        }        
    }

    public void receive(ExecBlockStartedEvent event) {
        logger.info(String.format(
        		"%s: received ExecBlockStartedEvent for SchedBlock %s, ExecBlock %s",
        		this.getClass().getSimpleName(),
        		event.sbId.entityId,
        		event.execId.entityId));
        logger.fine(String.format("event.arrayName = %s, event.sessionId = %s",
                                  event.arrayName,
                                  (event.sessionId==null)? "Null": event.sessionId.entityId));
        try {
            if (currentExecution != null) {
            	logger.fine(String.format("arrayName = %s, currentEx.sessionId = %s, currentEx.sbId = %s",
					  arrayName,
					  (currentExecution.getSessionRef()==null)? "Null": currentExecution.getSessionRef().entityId,
					  (currentExecution.getSchedBlockRef()==null)? "Null": currentExecution.getSchedBlockRef().entityId));
                if (event.arrayName.equals(arrayName) &&
		    //                        event.sessionId.entityId.equals(currentExecution.getSessionRef().entityId) &&
                        event.sbId.entityId.equals(currentExecution.getSchedBlockRef().entityId)) {
                    currentExecution.processExecBlockStartedEvent(event);
                } else {
                	logger.warning(String.format(
                			"start event(%s, %s, %s) does not match current execution(%s, %s, %s)",
                			event.arrayName,
                			event.sessionId.entityId,
                			event.sbId.entityId,
                			arrayName,
                			currentExecution.getSessionRef().entityId,
                			currentExecution.getSchedBlockRef().entityId));
                }
            } else {
            	logger.warning("No current execution");
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void receive(ExecBlockEndedEvent event) {
        logger.info(String.format(
        		"%s: received ExecBlockEndedEvent",
        		this.getClass().getSimpleName()));
        try {
            if (currentExecution != null) {
                if (event.arrayName.equals(arrayName) &&
		    //		        event.sessionId.entityId.equals(currentExecution.getSessionRef().entityId) &&
                        event.sbId.entityId.equals(currentExecution.getSchedBlockRef().entityId)) {
                    currentExecution.processExecBlockEndedEvent(event);
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void receive(ASDMArchivedEvent event) {
        logger.info(String.format(
        		"%s: received ASDMArchivedEvent (asdmId = %s)",
        		this.getClass().getSimpleName(),
        		event.asdmId.entityId));
        try {
        	boolean doneIt = false;
            if (currentExecution != null) {
                logger.info("there is a current execution");
                logger.info("event.asdmId.entityId: " + event.asdmId.entityId);
                if (currentExecution.getExecBlockRef() != null) {
                	// The current execution has an exec block ref set
                    if (event.asdmId.entityId.equals(currentExecution.getExecBlockRef().entityId)) {
                        currentExecution.processASDMArchivedEvent(event);
                        doneIt = true;
                    } else {
                    	logger.info(String.format(
                    			"The current execution's execBlockRef (%s) does not match the event's",
                    			currentExecution.getExecBlockRef().entityId));
                    }
                } else {
                	logger.info("The current execution does not have an execBlockRef");
                }
            }
            if (!doneIt) {
            	logger.info("Checking past executions for match with event");
            	List<ExecutionContext> pastContexts = getPastExecutions();
            	for (int i = pastContexts.size()-1; i >= 0; i--) {
            		// Search backwards on the assumption that the event is more likely to be to
            		// do with a relatively recent execution (the list is oldest first).
            		final ExecutionContext ctx = pastContexts.get(i);
                    if (ctx.getExecBlockRef() != null) {
                    	// The past execution has an exec block ref set
                        if (event.asdmId.entityId.equals(ctx.getExecBlockRef().entityId)) {
                        	ctx.processASDMArchivedEvent(event);
                            doneIt = true;
                            break;
                        } else {
                        	logger.info(String.format(
                        			"This past execution's execBlockRef (%s) does not match the event's",
                        			ctx.getExecBlockRef().entityId));
                        }
                    } else {
            			logger.warning(String.format(
            					"Missing execBlockRef in ExecutionContext %s @ %s",
            					ctx.getSchedBlock().getUid(),
            					ctx.getQueuedTimestamp()));
                    }
            	}
            }
            if (!doneIt) {
    			logger.warning(String.format(
    					"Could not find SchedBlock execution for ASDMArchivedEvent %s",
    					event.asdmId.entityId));
           }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void receive(SubScanProcessedEvent event) {
    	final String sep = ",\n\t";
    	StringBuilder b = new StringBuilder();

    	b.append("Received ");
    	b.append("SubScanProcessedEvent(");
    	b.append(sep); b.append("execBlockId: "); b.append(event.processedExecBlockId.entityId);
    	b.append(sep); b.append("status: ");      b.append(event.status);
    	b.append(sep); b.append("finishedAt: ");  b.append(event.finishedAt);
    	b.append(sep); b.append("scanNum: ");     b.append(event.processedScanNum);
    	b.append(sep); b.append("subScanNum: ");  b.append(event.processedSubScanNum);
    	b.append(sep); b.append("startTime: ");   b.append(event.subscanStartTime);
    	b.append(sep); b.append("endTime: ");     b.append(event.subscanEndTime);
    	b.append(sep); b.append("science?: ");    b.append(event.representativeScienceSubScan);
    	b.append(")");

    	logger.info(b.toString());
    	currentExecution.processSubScanProcessedEvent(event);
    }
    
    public void receive(SubScanSequenceEndedEvent event) {
    	final String sep = ",\n\t";
    	StringBuilder b = new StringBuilder();

    	b.append("Received ");
    	b.append("SubScanSequenceEndedEvent(");
    	b.append(sep); b.append("execBlockId: "); b.append(event.processedExecBlockId.entityId);
    	b.append(sep); b.append("status: ");      b.append(event.status);
    	b.append(sep); b.append("finishedAt: ");  b.append(event.finishedAt);
    	b.append(sep); b.append("scanNum: ");     b.append(event.scanNumber);
    	b.append(sep); b.append("successfulSubscans: ");  b.append(event.successfulSubscans);
    	b.append(")");

    	logger.info(b.toString());
    	currentExecution.processSubScanSequenceEndedEvent(event);
    }
	
    public ExecutionContext getCurrentExecution() {
        return currentExecution;
    }
    
    private void stopPastExecutions() {
    	try {
    		pastExecutionsLock.lock();
    		for (final ArchivalWaitThread waiter : pastExecutions) {
    			waiter.interrupt();
    		}
    	} finally {
    		pastExecutionsLock.unlock();
    	}
    }
    
    public List<ExecutionContext> getPastExecutions() {
        try {
            pastExecutionsLock.lock();
            List<ExecutionContext> retVal = new ArrayList<ExecutionContext>();
            for (final ArchivalWaitThread thread : pastExecutions) {
                retVal.add(thread.getContext());
            }
            return retVal;
        } finally {
            pastExecutionsLock.unlock();
        }
    }
    
    protected void notify(ExecutionStateChange stateChange) {
        setChanged();
        notifyObservers(stateChange);
    }
    
    protected void notify(ArrayGUINotification change) {
        setChanged();
        notifyObservers(change);
    }

    public boolean isFullAuto() {
    	return fullAuto;
    }

    public boolean isActiveDynamic() {
    	return activeDynamic;
    }

    public void configureManual(boolean manual) {
    	this.manual = manual;
    }

    public void configureServices(Services services) {
    	this.services = services;
    }

    public void configureSessionManager(SessionManager sessions) {
    	this.sessions = sessions;
    }

    public boolean isManual() {
    	return manual;
    }

    public boolean isRunning() {
    	return running;
    }

    public void setFullAuto(boolean on, String name, String role) {
    	if (fullAuto != on) {
    		fullAuto = on;
    		final ArrayGUINotification agn = new ArrayGUINotification(
    				on? ArrayGUIOperation.FULLAUTO: ArrayGUIOperation.SEMIAUTO,
    						name,
    						role);
    		notify(agn);
    	}
    }

    public void setActiveDynamic(boolean on, String name, String role) {
    	if (activeDynamic != on) {
    		activeDynamic = on;
    		final ArrayGUINotification agn = new ArrayGUINotification(
    				on? ArrayGUIOperation.ACTIVEDYNAMIC: ArrayGUIOperation.PASSIVEDYNAMIC,
    						name,
    						role);
    		notify(agn);
    	}
    }

    private void setRunning(boolean on, String name, String role) {
    	if (running != on) {
    		running = on;
    		final ArrayGUINotification agn = new ArrayGUINotification(
    				on? ArrayGUIOperation.RUNNING: ArrayGUIOperation.STOPPED,
    						name,
    						role);
    		notify(agn);
    	}
    }

    public void destroyArray(String user, String role) {
    	sessions.endObservingSession(); // Just to be sure
    	if (currentExecution != null) {
    		currentExecution.tidyUp();
    	}
    	stopPastExecutions();
    	final ArrayGUINotification agn = new ArrayGUINotification(
    			ArrayGUIOperation.DESTROYED,
    			user, role);
    	notify(agn);
    }

    protected boolean fullAuto(){
    	return fullAuto;
    }

    /**
     * @return the startEventTimeoutMS
     */
    public long getStartEventTimeoutMS() {
    	return startEventTimeoutMS;
    }

    /**
     * @param startEventTimeoutMS the startEventTimeoutMS to set
     */
    public void setStartEventTimeoutMS(long startEventTimeoutMS) {
    	this.startEventTimeoutMS = startEventTimeoutMS;
    }

    /**
     * @return the endedEventTimeoutMS
     */
    public long getEndedEventTimeoutMS() {
    	return endedEventTimeoutMS;
    }

    /**
     * @param endedEventTimeoutMS the endedEventTime to set
     */
    public void setEndedEventTimeoutMS(long endedEventTimeoutMS) {
    	this.endedEventTimeoutMS = endedEventTimeoutMS;
    }

    /**
     * @return the asdmArchiveTimeoutMS
     */
    public long getAsdmArchiveTimeoutMS() {
    	return asdmArchiveTimeoutMS;
    }

    /**
     * @param asdmArchiveTimeoutMS the asdmArchiveTimeoutMS to set
     */
    public void setAsdmArchiveTimeoutMS(long asdmArchiveTimeoutMS) {
    	this.asdmArchiveTimeoutMS = asdmArchiveTimeoutMS;
    }

    public Services getServices() {
    	return services;
    }
    
    void setServices(Services services) {
    	this.services = services;
    }

    public SessionManager getSessions() {
    	return sessions;
    }
    
    void setSessions(SessionManager sessions) {
    	this.sessions = sessions;
    }

    public String getArrayName() {
    	return arrayName;
    }

    public BlockingQueue<SchedBlockItem> getQueue() {
    	return queue;
    }

	public SchedBlockExecutionItem[] getExecutions() {
	    List<SchedBlockExecutionItem> retVal = new ArrayList<SchedBlockExecutionItem>();
	    if (currentExecution != null) {
	    	// Add in any current execution
	    	final SchedBlockExecutionItem sbei = new SchedBlockExecutionItem(
	    			currentExecution.getQueuedTimestamp(),
	    			currentExecution.getSbUid(),
	    			currentExecution.getStateName());
	    	retVal.add(sbei);
	    }
	    // Now add the past executions
    	for (final ExecutionContext context : getPastExecutions()) {
    		final SchedBlockExecutionItem sbei = new SchedBlockExecutionItem(
    				context.getQueuedTimestamp(),
    				context.getSbUid(),
    				context.getStateName());
	    	retVal.add(sbei);
    	}
		return retVal.toArray(new SchedBlockExecutionItem[0]);
	}

	/**
     * Method called by control when the user calls beginExecution from the CCL.
     * This method gives control the the SB id and the session id to use through
     * out the execution. It is only needed in manual mode when the user wants
     * an asdm produced. There will be a 'dummy' project with sb in the archive
     * to attach these asdms to its project status.
     */
	public IDLEntityRef startManualModeSession(String sbid)
				throws InvalidOperationEx {
		final IDLEntityRef result = getSessions().getCurrentSession();
		StringBuilder b = new StringBuilder();
		Formatter     f = new Formatter(b);

		f.format("%s.startManualModeSession(%s) - ref obtained from SessionManager is %s",
				getArrayName(),
				sbid,
				result.entityId);
		if (currentExecution != null) {
			if (currentExecution.getSessionRef() == null) {
				currentExecution.setSessionRef(result);
				b.append(" - applying to current execution");
			} else {
				f.format(" - current execution already has a session ref (%s)",
						currentExecution.getSessionRef().entityId);
			}
		} else {
			b.append(" - no current execution");
		}
		logger.info(b.toString());

		return result;
	}
}
