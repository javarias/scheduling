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
import alma.SchedulingExceptions.InvalidOperationEx;
import alma.acs.component.ComponentLifecycle;
import alma.acs.container.ContainerServices;
import alma.asdmIDLTypes.IDLEntityRef;
import alma.scheduling.ArrayGUICallback;
import alma.scheduling.ArrayOperations;
import alma.scheduling.ArraySchedulerLifecycleType;
import alma.scheduling.ArraySchedulerMode;
import alma.scheduling.SchedBlockExecutionCallback;
import alma.scheduling.SchedBlockExecutionItem;
import alma.scheduling.SchedBlockExecutionManagerOperations;
import alma.scheduling.SchedBlockQueueCallback;
import alma.scheduling.SchedBlockQueueItem;
import alma.scheduling.SchedBlockQueueManagerOperations;
import alma.scheduling.SchedBlockSelector;
import alma.scheduling.SchedBlockSelectorOperations;

/**
 * Implementation of the ArrayOperations interface which is an
 * aggregation of delegates for the component parts of that
 * interface.
 * 
 * @author dclarke
 * $Id: DelegatedArray.java,v 1.7 2011/03/14 23:24:25 dclarke Exp $
 */
public class DelegatedArray implements ComponentLifecycle,
        ArrayOperations {

	private ContainerServices m_containerServices;
    private Logger m_logger;
    private SchedBlockSelectorOperations sbSelectDelegate;
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
    		SchedBlockSelectorOperations         sbSelectDelegate,
    		SchedBlockExecutionManagerOperations sbExecDelegate,
    		SchedBlockQueueManagerOperations     sbQueueDelegate) {
    	setSchedBlockSelector(sbSelectDelegate);
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
	 * @param sbSelectDelegate the sbSelectDelegate to set
	 */
	public void setSchedBlockSelector(SchedBlockSelectorOperations sbSelectDelegate) {
		this.sbSelectDelegate = sbSelectDelegate;
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
	public void configureSelector(SchedBlockSelector sbSelectDelegate) {
		this.sbSelectDelegate = sbSelectDelegate;
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
     * Delegation of SchedBlockSelectorOperations
     * ================================================================
     */
	/**
	 * @return
	 * @see alma.scheduling.SchedBlockSelectorOperations#selectNextSB()
	 */
	public void selectNextSB() {
		sbSelectDelegate.selectNextSB();
	}
    /* End Delegation of SchedBlockSelectorOperations
     * ============================================================= */
	

    
    /*
     * ================================================================
     * Delegation of SchedBlockExecutionManagerOperations
     * ================================================================
     */
	/**
	 * @return
	 * @see alma.scheduling.SchedBlockExecutionManagerOperations#hasRunningSchedBlock()
	 */
	public boolean hasRunningSchedBlock() {
		return sbExecDelegate.hasRunningSchedBlock();
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
	public void start(String name, String role) {
		sbExecDelegate.start(name, role);
	}

	/**
	 * 
	 * @see alma.scheduling.SchedBlockExecutionManagerOperations#stop()
	 */
	public void stop(String name, String role) {
		sbExecDelegate.stop(name, role);
	}

	/**
	 * 
	 * @see alma.scheduling.SchedBlockExecutionManagerOperations#stopRunningSchedBlock()
	 */
	public void stopRunningSchedBlock(String name, String role) {
		sbExecDelegate.stopRunningSchedBlock(name, role);
	}

	public void addMonitorExecution(String arg0, SchedBlockExecutionCallback arg1) {
		sbExecDelegate.addMonitorExecution(arg0, arg1);
	}
	
	public void removeMonitorExecution(String arg0){
		sbExecDelegate.removeMonitorExecution(arg0);
	}

	@Override
	public boolean isFullAuto() {
		return sbExecDelegate.isFullAuto();
	}

	@Override
	public boolean isManual() {
		return sbExecDelegate.isManual();
	}

	@Override
	public boolean isRunning() {
		return sbExecDelegate.isRunning();
	}

	@Override
	public void setFullAuto(boolean on, String name, String role) {
		sbExecDelegate.setFullAuto(on, name, role);
	}

	@Override
	public void addMonitorGUI(String name, ArrayGUICallback callback) {
		sbExecDelegate.addMonitorGUI(name, callback);
	}

	@Override
	public void removeMonitorGUI(String name) {
		sbExecDelegate.removeMonitorGUI(name);
	}
	
	@Override
	public void destroyArray() {
		sbExecDelegate.destroyArray();
	}

	@Override
	public SchedBlockExecutionItem[] getExecutions() {
		return sbExecDelegate.getExecutions();
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
	   return sbExecDelegate.startManualModeSession(sbid);
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

	public void addMonitorQueue(String arg0, SchedBlockQueueCallback arg1) {
		sbQueueDelegate.addMonitorQueue(arg0, arg1);
	}

	public void removeMonitorQueue(String arg0) {
		sbQueueDelegate.removeMonitorQueue(arg0);
		
	}
    /* End Delegation of SchedBlockQueueManagerOperations
     * ============================================================= */

}
