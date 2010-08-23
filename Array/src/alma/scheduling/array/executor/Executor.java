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
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import alma.Control.ExecBlockEndedEvent;
import alma.Control.ExecBlockStartedEvent;
import alma.offline.ASDMArchivedEvent;
import alma.scheduling.array.sbQueue.SchedBlockItem;
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
    private Thread executionThread;
    
    private class ExecutorConsumer extends Thread {
        
        private Executor executor;

        public ExecutorConsumer(Executor e) {
            this.executor = e;
        }
        
        @Override
        public void run() {
            while(!isInterrupted()) {
                SchedBlockItem item = null;
                try {
                    // Blocks waiting for an item.
                    item = queue.take();
                } catch (InterruptedException e) {
                    logger.warning("executor consumer has been interrupted");
                    return;
                }
                logger.info("executor consumer took item " + item);
    
                currentExecution = new ExecutionContext(item, executor);
                // Blocks until the execution has finished.
                currentExecution.startObservation();
                // The observation finished or failed.
                
                // Pass on the execution to the pastExecutions list, where it will
                // wait for the ASDMArchivedEvent.
                ArchivalWaitThread wthr = new ArchivalWaitThread(currentExecution);
                try {
                    pastExecutionsLock.lock();
                    pastExecutions.add(wthr);
                    wthr.start();
                    currentExecution = null;
                } finally {
                    pastExecutionsLock.unlock();
                }
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
            execution.waitForArchival();
        }
        
        public ExecutionContext getContext() {
            return execution;
        }
    }
    
    public Executor(String arrayName, BlockingQueue<SchedBlockItem> queue) {
        this.arrayName = arrayName;
        this.queue = queue;
    }

    public void start() {
        executionThread = new ExecutorConsumer(this);
        executionThread.start();
    }

    /**
     * Stops the execution thread.
     * <P>
     * This function will not stop to currently running SchedBlock.
     * To stop the currently executing SchedBlock, use abort(). 
     */
    public void stop() {
        if (executionThread != null)
            executionThread.interrupt();
    }

    public void stopCurrentExecution() {
        if (currentExecution != null) {
            currentExecution.stopObservation();
        }
    }
    
    public void abortCurrentExecution() {
        if (currentExecution != null) {
            currentExecution.abortObservation();
        }        
    }

    public void receive(ExecBlockStartedEvent event) {
        logger.info("received ExecBlockStartedEvent event");
        try {
            if (currentExecution != null) {
                if (event.arrayName.equals(arrayName) &&
                        event.sessionId.entityId.equals(currentExecution.getSessionRef().entityId) &&
                        event.sbId.entityId.equals(currentExecution.getSchedBlockRef().entityId)) {
                    currentExecution.processExecBlockStartedEvent(event);
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void receive(ExecBlockEndedEvent event) {
        logger.info("received ExecBlockEndedEvent event");
        try {
            if (currentExecution != null) {
                if (event.arrayName.equals(arrayName) &&
                        event.sessionId.entityId.equals(currentExecution.getSessionRef().entityId) &&
                        event.sbId.entityId.equals(currentExecution.getSchedBlockRef().entityId)) {
                    currentExecution.processExecBlockEndedEvent(event);
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void receive(ASDMArchivedEvent event) {
        logger.info("received ASDMArchivedEvent event");
        try {
            if (currentExecution != null) {
                logger.info("there is a current execution");
                logger.info("event.asdmId.entityId: " + event.asdmId.entityId);
                logger.info("currentExecution.getExecBlockRef().entityId" + currentExecution.getExecBlockRef().entityId);
                if (event.asdmId.entityId.equals(currentExecution.getExecBlockRef().entityId)) {
                    currentExecution.processASDMArchivedEvent(event);
                }
            }
            for (Iterator<ExecutionContext> iter = getPastExecutions().iterator(); iter.hasNext();) {
                ExecutionContext ctx = iter.next();
                if (event.asdmId.entityId.equals(ctx.getExecBlockRef().entityId)) {
                    ctx.processASDMArchivedEvent(event);
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public ExecutionContext getCurrentExecution() {
        return currentExecution;
    }
    
    public List<ExecutionContext> getPastExecutions() {
        try {
            pastExecutionsLock.lock();
            List<ExecutionContext> retVal = new ArrayList<ExecutionContext>();
            for (Iterator<ArchivalWaitThread> iter = pastExecutions.iterator(); iter.hasNext();) {
                retVal.add(iter.next().getContext());
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
}
