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
import java.util.logging.Logger;

import alma.ACS.ComponentStates;
import alma.SchedulingArrayExceptions.NoRunningSchedBlockEx;
import alma.SchedulingArrayExceptions.wrappers.AcsJNoRunningSchedBlockEx;
import alma.acs.component.ComponentLifecycle;
import alma.acs.container.ContainerServices;
import alma.acs.exceptions.AcsJException;
import alma.scheduling.ArrayOperations;
import alma.scheduling.ArraySchedulerLifecycleType;
import alma.scheduling.ArraySchedulerMode;
import alma.scheduling.SchedBlockExecutionCallback;
import alma.scheduling.SchedBlockQueueCallback;
import alma.scheduling.SchedBlockQueueItem;
import alma.scheduling.SchedBlockScore;
import alma.scheduling.array.executor.ExecutionContext;
import alma.scheduling.array.executor.Executor;
import alma.scheduling.array.executor.ExecutorCallbackNotifier;
import alma.scheduling.array.executor.services.AcsProvider;
import alma.scheduling.array.executor.services.Services;
import alma.scheduling.array.sbQueue.LinkedReorderingBlockingQueue;
import alma.scheduling.array.sbQueue.ObservableReorderingBlockingQueue;
import alma.scheduling.array.sbQueue.SchedBlockItem;
import alma.scheduling.array.sbQueue.SchedBlockQueueCallbackNotifier;
import alma.scheduling.array.util.LoggerFactory;
import alma.scheduling.array.util.NameTranslator.TranslationException;

public class ArrayImpl implements ComponentLifecycle,
        ArrayOperations {

    private ContainerServices containerServices;

    private Logger logger;

    private ObservableReorderingBlockingQueue<SchedBlockItem> queue;
    
    private Executor executor;
    
    private String arrayName;
    
    private SchedBlockQueueCallbackNotifier queueNotifier;
    
    private ExecutorCallbackNotifier executorNotifier;
    
    private ArraySchedulerMode[] modes;
    
    private ArraySchedulerLifecycleType lifecycleType;
    
    private AcsProvider serviceProvider;
    
    /////////////////////////////////////////////////////////////
    // Implementation of ComponentLifecycle
    /////////////////////////////////////////////////////////////

    public void initialize(ContainerServices containerServices) {
        this.containerServices = containerServices;
        this.logger = this.containerServices.getLogger();
        LoggerFactory.SINGLETON.setLogger(logger);
        
        logger.finest("initialize() called...");
    }
    
    @Override
    public void configure(String arrayName, ArraySchedulerMode[] modes,
            ArraySchedulerLifecycleType lifecycleType) {
        
        this.arrayName = arrayName;
        this.modes = modes;
        this.lifecycleType = lifecycleType;
        
        try {
            serviceProvider = new AcsProvider(containerServices, arrayName);
            Services.registerProvider(serviceProvider);
        } catch (TranslationException e) {
            e.printStackTrace();
            // throw an exception...
        } catch (AcsJException e) {
            e.printStackTrace();
            // throw an exception...
        }
        
        LinkedReorderingBlockingQueue<SchedBlockItem> q =
            new LinkedReorderingBlockingQueue<SchedBlockItem>();
        queue = new ObservableReorderingBlockingQueue<SchedBlockItem>(q);
        
        executor = new Executor(arrayName, queue);
        
        queueNotifier = new SchedBlockQueueCallbackNotifier();
        executorNotifier = new ExecutorCallbackNotifier();
        
        queue.addObserver(queueNotifier);
        executor.addObserver(executorNotifier);
        
        serviceProvider.getControlEventReceiver().attach("alma.Control.ExecBlockStartedEvent", executor);
        serviceProvider.getControlEventReceiver().attach("alma.Control.ExecBlockEndedEvent", executor);
        serviceProvider.getControlEventReceiver().attach("alma.offline.ASDMArchivedEvent", executor);
        serviceProvider.getControlEventReceiver().begin();
    }

    @Override
    public ArraySchedulerLifecycleType getLifecycleType() {
        return lifecycleType;
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
	public void abortRunningSchedBlock() {
	    executor.abortCurrentExecution();
	}

	@Override
	public void start() {
	    executor.start();
	}

	@Override
	public void stop() {
	    executor.stop();
	}

	@Override
	public void stopRunningSchedBlock() {
        executor.stopCurrentExecution();
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
    public void monitorQueue(String monitorName, SchedBlockQueueCallback callback) {
        queueNotifier.registerMonitor(monitorName, callback);
    }

    @Override
    public void monitorExecution(String monitorName, SchedBlockExecutionCallback callback) {
        executorNotifier.registerMonitor(monitorName, callback);
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
    public SchedBlockScore[] run() {
        // TODO DSA...
        return null;
    }
}
