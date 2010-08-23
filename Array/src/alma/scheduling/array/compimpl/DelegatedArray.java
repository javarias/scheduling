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

package alma.scheduling.array.compimpl;

import java.util.logging.Logger;

import alma.ACS.ComponentStates;
import alma.SchedulingArrayExceptions.NoRunningSchedBlockEx;
import alma.acs.component.ComponentLifecycle;
import alma.acs.container.ContainerServices;
import alma.scheduling.ArrayOperations;
import alma.scheduling.ArraySchedulerLifecycleType;
import alma.scheduling.ArraySchedulerMode;
import alma.scheduling.DSAOperations;
import alma.scheduling.SchedBlockExecutionCallback;
import alma.scheduling.SchedBlockExecutionManagerOperations;
import alma.scheduling.SchedBlockQueueCallback;
import alma.scheduling.SchedBlockQueueItem;
import alma.scheduling.SchedBlockQueueManagerOperations;
import alma.scheduling.SchedBlockScore;

/**
 * Implementation of the ArrayOperations interface which is an
 * aggregation of delegates for the component parts of that
 * interface.
 * 
 * @author dclarke
 * $Id: DelegatedArray.java,v 1.4 2010/08/23 23:07:36 dclarke Exp $
 */
public class DelegatedArray implements ComponentLifecycle,
        ArrayOperations {

	private ContainerServices m_containerServices;
    private Logger m_logger;
    private DSAOperations dsaDelegate;
	private SchedBlockExecutionManagerOperations sbExecDelegate;
    private SchedBlockQueueManagerOperations sbQueueDelegate;

    /////////////////////////////////////////////////////////////
    // Implementation of ComponentLifecycle
    /////////////////////////////////////////////////////////////

    public void initialize(ContainerServices containerServices) {
        m_containerServices = containerServices;
        m_logger = m_containerServices.getLogger();

        m_logger.finest("initialize() called...");
    }

    public void execute() {
        m_logger.finest("execute() called...");
    }

    public void cleanUp() {
        m_logger.finest("cleanUp() called");
    }

    public void aboutToAbort() {
        cleanUp();
        m_logger.finest("managed to abort...");
        System.out.println("DummyComponent component managed to abort...");
    }

    /////////////////////////////////////////////////////////////
    // Implementation of ACSComponent
    /////////////////////////////////////////////////////////////

    public ComponentStates componentState() {
        return m_containerServices.getComponentStateManager().getCurrentState();
    }

    public String name() {
        return m_containerServices.getName();
    }

    
    
    /*
     * ================================================================
     * Construction
     * ================================================================
     */
    public DelegatedArray(
    		DSAOperations                        dsaDelegate,
    		SchedBlockExecutionManagerOperations sbExecDelegate,
    		SchedBlockQueueManagerOperations     sbQueueDelegate) {
    	setDSA(dsaDelegate);
    	setSchedBlockExecutionManager(sbExecDelegate);
    	setSchedBlockQueueManager(sbQueueDelegate);
    }
    /* End Construction
     * ============================================================= */
	

        
    /*
     * ================================================================
     * Setting of delegates
     * ================================================================
     */
    /**
	 * @param dsaDelegate the dsaDelegate to set
	 */
	public void setDSA(DSAOperations dsaDelegate) {
		this.dsaDelegate = dsaDelegate;
	}

	/**
	 * @param sbExecDelegate the sbExecDelegate to set
	 */
	public void setSchedBlockExecutionManager(
			SchedBlockExecutionManagerOperations sbExecDelegate) {
		this.sbExecDelegate = sbExecDelegate;
	}

	/**
	 * @param sbQueueDelegate the sbQueueDelegate to set
	 */
	public void setSchedBlockQueueManager(
			SchedBlockQueueManagerOperations sbQueueDelegate) {
		this.sbQueueDelegate = sbQueueDelegate;
	}
    /* End Setting of delegates
     * ============================================================= */

    
    
    /*
     * ================================================================
     * ArrayOperations
     * ================================================================
     */
	private String name;
	private ArraySchedulerLifecycleType type;
	private ArraySchedulerMode[] modes;
	
	@Override
	public void configure(String name,
						  ArraySchedulerMode[] modes,
						  ArraySchedulerLifecycleType type) {
		this.name = name;
		this.type = type;
		this.modes = modes;
	}

	@Override
	public ArraySchedulerLifecycleType getLifecycleType() {
		return type;
	}

	@Override
	public ArraySchedulerMode[] getModes() {
		return modes;
	}
    /* End ArrayOperations
     * ============================================================= */
	

    
    /*
     * ================================================================
     * Delegation of DSAOperations
     * ================================================================
     */
	/**
	 * @return
	 * @see alma.scheduling.DSAOperations#run()
	 */
	public SchedBlockScore[] run() {
		return dsaDelegate.run();
	}
    /* End Delegation of DSAOperations
     * ============================================================= */
	

    
    /*
     * ================================================================
     * Delegation of SchedBlockExecutionManagerOperations
     * ================================================================
     */
	/**
	 * 
	 * @see alma.scheduling.SchedBlockExecutionManagerOperations#abortRunningtSchedBlock()
	 */
	public void abortRunningtSchedBlock() {
		sbExecDelegate.abortRunningSchedBlock();
	}

	/**
	 * @return
	 * @see alma.scheduling.SchedBlockExecutionManagerOperations#getRunningSchedBlock()
	 */
	public String getRunningSchedBlock() throws NoRunningSchedBlockEx {
		return sbExecDelegate.getRunningSchedBlock();
	}

	/**
	 * @param callback
	 * @see alma.scheduling.SchedBlockExecutionManagerOperations#start(alma.scheduling.SchedBlockExecutionCallback)
	 */
	public void start() {
		sbExecDelegate.start();
	}

	/**
	 * 
	 * @see alma.scheduling.SchedBlockExecutionManagerOperations#stop()
	 */
	public void stop() {
		sbExecDelegate.stop();
	}

	/**
	 * 
	 * @see alma.scheduling.SchedBlockExecutionManagerOperations#stopRunningSchedBlock()
	 */
	public void stopRunningSchedBlock() {
		sbExecDelegate.stopRunningSchedBlock();
	}

	public void abortRunningSchedBlock() {
		sbExecDelegate.abortRunningSchedBlock();
	}

	public void monitorExecution(String arg0, SchedBlockExecutionCallback arg1) {
		sbExecDelegate.monitorExecution(arg0, arg1);
	}
    /* End Delegation of SchedBlockExecutionManagerOperations
     * ============================================================= */
	

    
    /*
     * ================================================================
     * Delegation of SchedBlockQueueManagerOperations
     * ================================================================
     */
	public void delete(SchedBlockQueueItem qItem) {
		sbQueueDelegate.delete(qItem);
	}

	public SchedBlockQueueItem[] getQueue() {
		return sbQueueDelegate.getQueue();
	}

	public void moveDown(SchedBlockQueueItem qItem) {
		sbQueueDelegate.moveDown(qItem);
	}

	public void moveUp(SchedBlockQueueItem qItem) {
		sbQueueDelegate.moveUp(qItem);
	}

	public SchedBlockQueueItem pull() {
		return sbQueueDelegate.pull();
	}

	public void push(SchedBlockQueueItem qItem) {
		sbQueueDelegate.push(qItem);
	}

    public SchedBlockQueueItem[] getExecutedQueue() {
		return sbQueueDelegate.getExecutedQueue();
	}

	public int getQueueCapacity() {
		return sbQueueDelegate.getQueueCapacity();
	}

	public void monitorQueue(String arg0, SchedBlockQueueCallback arg1) {
		sbQueueDelegate.monitorQueue(arg0, arg1);
	}
    /* End Delegation of SchedBlockQueueManagerOperations
     * ============================================================= */
}
