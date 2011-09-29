/* ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2006 
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

package alma.scheduling.array.compimpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import alma.ACS.ComponentStates;
import alma.SchedulingArrayExceptions.NoRunningSchedBlockEx;
import alma.SchedulingArrayExceptions.wrappers.AcsJNoRunningSchedBlockEx;
import alma.SchedulingExceptions.InvalidOperationEx;
import alma.acs.component.ComponentLifecycle;
import alma.acs.container.ContainerServices;
import alma.acs.exceptions.AcsJException;
import alma.asdmIDLTypes.IDLEntityRef;
import alma.scheduling.ArrayDescriptor;
import alma.scheduling.ArrayGUICallback;
import alma.scheduling.ArrayOperations;
import alma.scheduling.ArraySchedulerLifecycleType;
import alma.scheduling.ArraySchedulerMode;
import alma.scheduling.SchedBlockExecutionCallback;
import alma.scheduling.SchedBlockExecutionItem;
import alma.scheduling.SchedBlockQueueCallback;
import alma.scheduling.SchedBlockQueueItem;
import alma.scheduling.algorithm.PoliciesContainersDirectory;
import alma.scheduling.array.executor.ExecutionContext;
import alma.scheduling.array.executor.Executor;
import alma.scheduling.array.executor.ExecutorCallbackNotifier;
import alma.scheduling.array.executor.services.AcsProvider;
import alma.scheduling.array.executor.services.Services;
import alma.scheduling.array.guis.ArrayGUICallbackNotifier;
import alma.scheduling.array.sbQueue.DefaultSchedulingQueue;
import alma.scheduling.array.sbQueue.LinkedReorderingBlockingQueue;
import alma.scheduling.array.sbQueue.ObservableReorderingBlockingQueue;
import alma.scheduling.array.sbQueue.SchedBlockItem;
import alma.scheduling.array.sbQueue.SchedBlockQueueCallbackNotifier;
import alma.scheduling.array.sbSelection.AbstractSelector;
import alma.scheduling.array.sbSelection.DSASelector;
import alma.scheduling.array.sbSelection.Selector;
import alma.scheduling.array.sessions.SessionManager;
import alma.scheduling.array.util.NameTranslator.TranslationException;
import alma.scheduling.utils.ErrorHandling;
import alma.scheduling.utils.LoggerFactory;

public class ArrayImpl implements ComponentLifecycle,
        ArrayOperations {

    private ContainerServices containerServices;

    private Logger logger;

    private ObservableReorderingBlockingQueue<SchedBlockItem> queue;
    
    private Executor executor;
    
    private String arrayName;
    
    private SchedBlockQueueCallbackNotifier queueNotifier;
    
    private ExecutorCallbackNotifier executorNotifier;
    
    private ArrayGUICallbackNotifier guiNotifier;
    
    private ArraySchedulerMode[] modes;
    
    private ArrayDescriptor descriptor;
    
    private AcsProvider serviceProvider;
    
    private Selector selector = null;
    
    void setLogger(Logger logger) {
    	this.logger = logger;
    	LoggerFactory.SINGLETON.setLogger(logger);
    }
    
    /////////////////////////////////////////////////////////////
    // Implementation of ComponentLifecycle
    /////////////////////////////////////////////////////////////

    public void initialize(ContainerServices containerServices) {
        this.containerServices = containerServices;
        if(this.containerServices.getLogger() != null) {
        	this.logger = this.containerServices.getLogger();
        	LoggerFactory.SINGLETON.setLogger(logger);
        }
        
        logger.finest("initialize() called...");
    }
    
    private boolean isManual(ArraySchedulerMode[] modes) {
    	ErrorHandling.logArray(logger, "Scheduler Modes" , modes);
    	for (final ArraySchedulerMode mode : modes) {
    		if (mode.equals(ArraySchedulerMode.MANUAL_I)) {
    			return true;
    		}
    	}
    	return false;
    }
    
	@Override
	public void configure(String arrayName,
			              ArraySchedulerMode[] modes,
			              ArrayDescriptor descriptor) {
		this.arrayName  = arrayName;
		this.modes      = modes;
		this.descriptor = descriptor;

		final boolean manual = isManual(modes);
		Services services = null;

		try {
			serviceProvider = new AcsProvider(containerServices, arrayName,
					manual);
			services = new Services(serviceProvider);
		} catch (TranslationException e) {
			ErrorHandling.warning(logger, String.format(
					"Error in array name %s - %s (more details in finer logs)",
					arrayName, e.getMessage()), e);
		} catch (AcsJException e) {
			ErrorHandling
					.warning(
							logger,
							String.format(
									"Error linking to services for %s - %s (more details in finer logs)",
									arrayName, e.getMessage()), e);
		}

		LinkedReorderingBlockingQueue<SchedBlockItem> q;
		if (manual) {
			q = new LinkedReorderingBlockingQueue<SchedBlockItem>(1);
		} else {
			q = new LinkedReorderingBlockingQueue<SchedBlockItem>();
		}

		queue = new DefaultSchedulingQueue<SchedBlockItem>(q);
//		queue = new ObservableReorderingBlockingQueue<SchedBlockItem>(q);

		executor = new Executor(arrayName, queue);
		executor.configureManual(manual);
		executor.configureServices(services);
		executor.configureSessionManager(new SessionManager(arrayName,
				containerServices, services, manual));

		queueNotifier = new SchedBlockQueueCallbackNotifier();
		executorNotifier = new ExecutorCallbackNotifier();
		guiNotifier = new ArrayGUICallbackNotifier();

		queue.addObserver(queueNotifier);
		executor.addObserver(executorNotifier);
		executor.addObserver(guiNotifier);

		serviceProvider.getControlEventReceiver().attach(
				"alma.Control.ExecBlockStartedEvent", executor);
		serviceProvider.getControlEventReceiver().attach(
				"alma.Control.ExecBlockEndedEvent", executor);
		serviceProvider.getControlEventReceiver().attach(
				"alma.offline.ASDMArchivedEvent", executor);
		serviceProvider.getControlEventReceiver().begin();

		if (manual) {
			executor.setFullAuto(true, "Master Scheduler",
					"array configuration");
			executor.start("Master Scheduler", "array configuration");
		}
	}

	@Override
	public void configureDynamicScheduler(String policyName) {
		logger.info(String.format(
				"Configuring %s as a dynamic array with policy %s",
				arrayName, policyName));
		
		descriptor.policyName = policyName;
		if (this.selector == null) {
			final AbstractSelector dsa = new DSASelector();
			dsa.configureArray(this, queue);
			dsa.addObserver(guiNotifier);
			this.selector = dsa;
		} else {
			logger.fine("DSA object already exists, not creating a new one (worried that this will not work as it might continue to use the old policy)");
		}
	}

    @Override
    public ArraySchedulerLifecycleType getLifecycleType() {
        return descriptor.lifecycleType;
    }

	@Override
	public ArrayDescriptor getDescriptor() {
		return descriptor;
	}

	@Override
    public String getArrayName() {
        return arrayName;
    }

    @Override
    public ArraySchedulerMode[] getModes() {
        return modes;
    }
    
    public void execute() {
        logger.finest("execute() called...");
    }

    public void cleanUp() {
    	logger.finest("cleanUp() called");
    	serviceProvider.getControlEventReceiver().end();
    	serviceProvider.cleanUp();
    	if (descriptor != null) {
    		if (descriptor.policyName != null) {
    			try {
    				//The policy file must be unlocked before the array is destroyed
    				if(descriptor.policyName.startsWith("uuid")) {
    					String fileUUID = descriptor.policyName.substring(4, 41);
    					PoliciesContainersDirectory.getInstance().unlockPolicyContainer(UUID.fromString(fileUUID));
    				}
    			} catch (Exception ex) {
    				ex.printStackTrace();
    			}
    		}
    	}
    }

    public void aboutToAbort() {
        cleanUp();
        logger.finest("managed to abort...");
        System.out.println("ArrayImpl component managed to abort...");
    }

    /////////////////////////////////////////////////////////////
    // Implementation of ACSComponent
    /////////////////////////////////////////////////////////////

    public ComponentStates componentState() {
        return containerServices.getComponentStateManager().getCurrentState();
    }

    public String name() {
        return containerServices.getName();
    }

    /////////////////////////////////////////////////////////////
    // Implementation of ArrayOperations
    /////////////////////////////////////////////////////////////    
    
	@Override
	public void moveDown(SchedBlockQueueItem item) {
	    queue.moveDown(new SchedBlockItem(item));
	}

	@Override
	public void moveUp(SchedBlockQueueItem item) {
        queue.moveUp(new SchedBlockItem(item));
	}

	@Override
	public SchedBlockQueueItem pull() {
	    try {
            SchedBlockItem item = queue.take();
            return new SchedBlockQueueItem(item.getTimestamp(), item.getUid());
        } catch (InterruptedException e) {
            e.printStackTrace();
            // This point should only be reached when the Container or Manager
            // forcibly deactivates the component. Assuming that in this case
            // the returned value is not really important.
            return new SchedBlockQueueItem(-1, "");
        }
	}

	@Override
	public void push(SchedBlockQueueItem item) {
	    SchedBlockItem newItem = new SchedBlockItem(item.uid, item.timestamp);
	    if (queue.remainingCapacity() == 0) {
	        // throw exception
	    }
	    boolean inserted = queue.offer(newItem);
	    if (!inserted) {
	        // throw exception
	    }
	}

	@Override
	public void start(String name, String role) {
	    executor.start(name, role);
	}

	@Override
	public void stop(String name, String role) {
	    executor.stop(name, role);
	}

	@Override
	public void stopRunningSchedBlock(String name, String role) {
        executor.stopCurrentExecution(name, role);
	}

	@Override
	public boolean hasRunningSchedBlock() {
        return (executor.getCurrentExecution() != null);
	}

	@Override
	public String getRunningSchedBlock() throws NoRunningSchedBlockEx {
        ExecutionContext ctx = executor.getCurrentExecution();
        if (ctx != null) {
            return ctx.getSbUid();
        }
        AcsJNoRunningSchedBlockEx ex = new AcsJNoRunningSchedBlockEx();
        throw ex.toNoRunningSchedBlockEx();
	}

	@Override
	public void delete(SchedBlockQueueItem item) {
	    queue.remove(new SchedBlockItem(item));
	}

	@Override
	public SchedBlockQueueItem[] getQueue() {
	    List<SchedBlockQueueItem> retVal = new ArrayList<SchedBlockQueueItem>();
	    for (Iterator<SchedBlockItem> iter = queue.iterator(); iter.hasNext(); ) {
	        SchedBlockItem item = iter.next();
	        retVal.add(new SchedBlockQueueItem(item.getTimestamp(), item.getUid()));
	    }
		return retVal.toArray(new SchedBlockQueueItem[0]);
	}

    @Override
    public int getQueueCapacity() {
        return queue.remainingCapacity() + queue.size();
    }

	@Override
	public void addMonitorQueue(String monitorName,
			SchedBlockQueueCallback callback) {
		logger.info("This method can throw an exception. This exception is handled by the GUI. If you see a backtrace below that, don't panic, everything is ok");
		queueNotifier.registerMonitor(monitorName, callback);
	}
    
	@Override
	public void removeMonitorQueue(String monitorName) {
		queueNotifier.unregisterMonitor(monitorName);
	}

	@Override
	public void addMonitorExecution(String monitorName,
			SchedBlockExecutionCallback callback) {
		executorNotifier.registerMonitor(monitorName, callback);
	}

	@Override
	public void removeMonitorExecution(String monitorName) {
		executorNotifier.unregisterMonitor(monitorName);
		
	}

	@Override
	public void addMonitorGUI(String monitorName, ArrayGUICallback callback) {
		guiNotifier.registerMonitor(monitorName, callback);
	}

	@Override
	public void removeMonitorGUI(String monitorName) {
		guiNotifier.unregisterMonitor(monitorName);
		
	}

    @Override
    public SchedBlockQueueItem[] getExecutedQueue() {
        List<SchedBlockQueueItem> retVal = new ArrayList<SchedBlockQueueItem>();
        for (Iterator<ExecutionContext> iter = executor.getPastExecutions().iterator();
            iter.hasNext(); ) {
            ExecutionContext ctx = iter.next();
            retVal.add(new SchedBlockQueueItem(ctx.getStopTimestamp(), ctx.getSbUid()));
        }
        return retVal.toArray(new SchedBlockQueueItem[0]);
    }

	@Override
	public boolean isFullAuto() {
		return executor.isFullAuto();
	}

	@Override
	public boolean isActiveDynamic() {
		return executor.isActiveDynamic();
	}

	@Override
	public boolean isManual() {
		return executor.isManual();
	}

	@Override
	public boolean isRunning() {
		return executor.isRunning();
	}

	@Override
	public void setFullAuto(boolean on, String name, String role) {
		executor.setFullAuto(on, name, role);
	}

	@Override
	public void setActiveDynamic(boolean on, String name, String role) {
		executor.setActiveDynamic(on, name, role);
	}
	
	@Override
	public void destroyArray(String name, String role) {
		executor.destroyArray(name, role);
	}

	@Override
	public SchedBlockExecutionItem[] getExecutions() {
		return executor.getExecutions();
	}

    /**
     * Method called by control when the user calls beginExecution from the CCL.
     * This method gives control the the SB id and the session id to use through
     * out the execution. It is only needed in manual mode when the user wants
     * an asdm produced. There will be a 'dummy' project with sb in the archive
     * to attach these asdms to its project status.
     */
	@Override
	public IDLEntityRef startManualModeSession(String sbid)
			throws InvalidOperationEx {
		return executor.startManualModeSession(sbid);
	}
	
	@Override
    public String getSchedulingPolicy() {
    	return descriptor.policyName;

    }

}
