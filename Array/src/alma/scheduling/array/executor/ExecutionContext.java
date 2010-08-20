/* 
 * ALMA - Atacama Large Millimiter Array
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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import alma.Control.ExecBlockEndedEvent;
import alma.Control.ExecBlockStartedEvent;
import alma.asdmIDLTypes.IDLEntityRef;
import alma.offline.ASDMArchivedEvent;
import alma.scheduling.array.executor.services.ControlArray;
import alma.scheduling.array.executor.services.EventPublisher;
import alma.scheduling.array.executor.services.Pipeline;
import alma.scheduling.array.executor.services.Services;
import alma.scheduling.array.guis.ModelAccessor;
import alma.scheduling.array.sbQueue.SchedBlockItem;
import alma.scheduling.array.util.LoggerFactory;
import alma.scheduling.datamodel.obsproject.SchedBlock;

public class ExecutionContext {
    
    private static ModelAccessor model = Services.getModel();
    
    private static ControlArray controlArray = Services.getControlArray();
    
    private static Pipeline pipeline = Services.getPipeline();
    
    private static EventPublisher publisher = Services.getEventPublisher();
    
    protected static ModelAccessor getModel() {
        return model;
    }
    
    protected static ControlArray getControlArray() {
        return controlArray;
    }
    
    protected static Pipeline getPipeline() {
        return pipeline;
    }
    
    protected static EventPublisher getEventPublisher() {
        return publisher;
    }
    
    private Logger logger = LoggerFactory.getLogger(getClass());

    private ExecutionState state = new ReadyExecutionState(this);
    
    private SchedBlockItem schedBlockItem;
    
    private SchedBlock schedBlock;
    
    private Executor executor;
    
    private long startTimestamp;
    
    private long stopTimestamp;
    
    private long archivedTimestamp;

    private Lock receptionLock = new ReentrantLock();
    private Condition startedCV = receptionLock.newCondition();
    private ExecBlockStartedEvent startedEvent;
    private Condition endedCV = receptionLock.newCondition();
    private ExecBlockEndedEvent endedEvent;
    private Condition archivedCV = receptionLock.newCondition();
    private ASDMArchivedEvent archivedEvent;
    
    private IDLEntityRef execBlockRef;
    private IDLEntityRef sessionRef;
    private IDLEntityRef schedBlockRef;
    
    public ExecutionContext(SchedBlockItem schedBlockItem, Executor executor) {
        this.schedBlockItem = schedBlockItem;
        this.executor = executor;
        this.schedBlock = model.getSchedBlockFromEntityId(schedBlockItem.getUid());
    }
    
    protected void setState(ExecutionState state) {
        logger.info("moving from " + this.state + " to " + state);
        this.executor.notify(new ExecutionStateChange(schedBlockItem,
                state.toString()));
        this.state = state;
    }
    
    protected SchedBlock getSchedBlock() {
        return schedBlock;
    }
    
    public void startObservation() {
        state.startObservation();
    }
    
    public void stopObservation() {
        state.stopObservation();
    }
    
    public void abortObservation() {
        state.abortObservation();
    }
    
    public void waitForArchival() {
        state.waitArchival();
    }
    
    public void processExecBlockStartedEvent(ExecBlockStartedEvent event) {
        logger.info(toString() + ": received ExecBlockStartedEvent");
        startTimestamp = System.currentTimeMillis();
        try {
            receptionLock.lock();
            startedEvent = event;
            setExecBlockRef(event.execId);
            startedCV.signal();
        } finally {
            receptionLock.unlock();
        }
    }
    
    public void processExecBlockEndedEvent(ExecBlockEndedEvent event) {
        stopTimestamp = System.currentTimeMillis();
        try {
            receptionLock.lock();
            endedEvent = event;
            endedCV.signal();
        } finally {
            receptionLock.unlock();
        }
    }
    
    public void processASDMArchivedEvent(ASDMArchivedEvent event) {
        logger.info("processing ASDMArchivedEvent");
        // throw something if the ExecBlockStartedEvent hasn't arrived yet.
        archivedTimestamp = System.currentTimeMillis();
        try {
            receptionLock.lock();
            archivedEvent = event;
            archivedCV.signal();
        } finally {
            receptionLock.unlock();
        }
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public long getStopTimestamp() {
        return stopTimestamp;
    }
    
    public long getArchivedTimestamp() {
        return archivedTimestamp;
    }
  
    public String getSbUid() {
        return schedBlock.getUid();
    }
    
    protected ExecBlockStartedEvent waitForExecBlockStartedEvent(long timeout) {
        try {
            receptionLock.lock();
            while (startedEvent == null) {
                boolean received;
                try {
                    received = startedCV.await(timeout, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
                if (!received) {
                    logger.severe("timeout waiting for ExecBlockStartedEvent");
                    break;
                }
            }
            return startedEvent;
        } finally {
            receptionLock.unlock();
        }
    }
    
    protected ExecBlockEndedEvent waitForExecBlockEndedEvent() {
        try {
            receptionLock.lock();
            while (endedEvent == null) {
                try {
                    endedCV.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return endedEvent;
        } finally {
            receptionLock.unlock();
        }
    }

    protected ASDMArchivedEvent waitForASDMArchivedEvent(long timeout) {
        try {
            receptionLock.lock();
            while (archivedEvent == null) {
                boolean received;
                try {
                    received = archivedCV.await(timeout, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
                if (!received) {
                    logger.severe("timeout waiting for ASDMArchivedEvent");
                    break;
                }
            }
            return archivedEvent;
        } finally {
            receptionLock.unlock();
        }
    }

    protected IDLEntityRef getExecBlockRef() {
        return execBlockRef;
    }

    protected void setExecBlockRef(IDLEntityRef ref) {
        execBlockRef = ref;
    }
    
    protected IDLEntityRef getSessionRef() {
        return sessionRef;
    }

    protected void setSessionRef(IDLEntityRef ref) {
        sessionRef = ref;
    }

    protected IDLEntityRef getSchedBlockRef() {
        return schedBlockRef;
    }
    
    protected void setSchedBlockRef(IDLEntityRef ref) {
        schedBlockRef = ref;
    }
}
