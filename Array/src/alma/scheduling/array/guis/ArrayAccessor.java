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
package alma.scheduling.array.guis;

import java.beans.PropertyChangeListener;

import org.omg.CORBA.Context;
import org.omg.CORBA.ContextList;
import org.omg.CORBA.DomainManager;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.InterfaceDef;
import org.omg.CORBA.NVList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.Object;
import org.omg.CORBA.Policy;
import org.omg.CORBA.Request;
import org.omg.CORBA.SetOverrideType;

import alma.ACS.ComponentStates;
import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.SchedulingArrayExceptions.NoRunningSchedBlockEx;
import alma.SchedulingExceptions.InvalidOperationEx;
import alma.asdmIDLTypes.IDLEntityRef;
import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.Array;
import alma.scheduling.ArrayDescriptor;
import alma.scheduling.ArrayGUICallback;
import alma.scheduling.ArrayHelper;
import alma.scheduling.ArraySchedulerLifecycleType;
import alma.scheduling.ArraySchedulerMode;
import alma.scheduling.SchedBlockExecutionCallback;
import alma.scheduling.SchedBlockExecutionItem;
import alma.scheduling.SchedBlockQueueCallback;
import alma.scheduling.SchedBlockQueueItem;
import alma.scheduling.array.compimpl.ArrayGUICallbackImpl;
import alma.scheduling.array.compimpl.SchedBlockExecutionCallbackImpl;
import alma.scheduling.array.compimpl.SchedBlockQueueCallbackImpl;
import alma.scheduling.array.util.NameTranslator;
import alma.scheduling.array.util.NameTranslator.TranslationException;

/**
 * A proxy for the Scheduling array component.
 * 
 * @author rhiriart
 *
 */
public class ArrayAccessor {

	private PluginContainerServices services;
    private Array array;
    private String arrayName;
    private String queueCallbackName;
    private SchedBlockQueueCallbackImpl queueCallback;
    private String executionCallbackName;
    private SchedBlockExecutionCallbackImpl executionCallback;
    private String guiCallbackName;
    private ArrayGUICallbackImpl guiCallback;
    private final static long MAX_WAIT_TIME_MILLIS = 300000;
    private boolean dynamic;
    
    
    public ArrayAccessor(PluginContainerServices services, String arrayName) throws AcsJContainerServicesEx, TranslationException {
        this.services = services;
        this.arrayName = arrayName;
        Object obj = this.services.getComponentNonSticky(NameTranslator.arrayToComponentName(arrayName));
        array = ArrayHelper.narrow(obj);
        determineArrayCharacteristics();
    }
    
    /**
     * Constructor used for dummies children
     * @param arrayName
     */
    protected ArrayAccessor(String arrayName) {
        this.arrayName = arrayName;
        this.array = new DummyArrayImpl();
    }

	private boolean determineDynamic() {
		ArraySchedulerMode[] modes = array.getModes();
		for (ArraySchedulerMode mode : modes) {
			if (mode == ArraySchedulerMode.DYNAMIC_I) {
				return true;
			}
		}
		return false;
	}

	private void determineArrayCharacteristics() {
		dynamic = determineDynamic();
	}

	public void delete(SchedBlockQueueItem arg0) {
		array.delete(arg0);
	}

	public String getArrayName() {
		return arrayName;
	}

	public SchedBlockQueueItem[] getExecutedQueue() {
		return array.getExecutedQueue();
	}

	public SchedBlockQueueItem[] getQueue() {
		return array.getQueue();
	}

	public SchedBlockExecutionItem[] getExecutions() {
		return array.getExecutions();
	}

	public int getQueueCapacity() {
		return array.getQueueCapacity();
	}

	public boolean hasRunningSchedBlock() {
		return array.hasRunningSchedBlock();
	}

	public String getRunningSchedBlock() throws NoRunningSchedBlockEx {
		return array.getRunningSchedBlock();
	}

	public void moveDown(SchedBlockQueueItem arg0) {
		array.moveDown(arg0);
	}

	public void moveUp(SchedBlockQueueItem arg0) {
		array.moveUp(arg0);
	}

	public SchedBlockQueueItem pull() {
		return array.pull();
	}

	public void push(SchedBlockQueueItem arg0) {
		array.push(arg0);
	}

	public void selectNextSB() {
//		array.selectNextSB();
	}

	public void start(String name, String role) {
		array.start(name, role);
	}

	public void stop(String name, String role) {
		array.stop(name, role);
	}

	public void stopRunningSchedBlock(String name, String role) {
		array.stopRunningSchedBlock(name, role);
	}
	
	public void registerQueueCallback(PropertyChangeListener listener) throws AcsJContainerServicesEx {
		services.getLogger().info("Registering Queue Callback");
		queueCallbackName = array.name() + "_" + System.currentTimeMillis();
		queueCallback = new SchedBlockQueueCallbackImpl(services.getLogger(),
				listener);
		services.activateOffShoot(queueCallback);
		final long maxWaitTimeMillis = System.currentTimeMillis() + MAX_WAIT_TIME_MILLIS;
		boolean succeed = false;
		Exception ex = null;
		while (!succeed && (System.currentTimeMillis() <= maxWaitTimeMillis)) {
			try {
				array.addMonitorQueue(queueCallbackName, queueCallback._this());
				succeed = true;
			} catch (Exception e) {
				ex = e; // Save last Exception just in case
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		if (!succeed) {
			services.getLogger().severe(
					"Queue Callback could not be installed in Array.");
			throw new RuntimeException(
					"Queue Callback could not be installed in Array.", ex);
		}
	}
	
	public void registerExecutionCallback(PropertyChangeListener pcl)
			throws AcsJContainerServicesEx {
		services.getLogger().info("Registering Execution Callback");
		executionCallbackName = array.name() + "_" + System.currentTimeMillis();
		executionCallback = new SchedBlockExecutionCallbackImpl(
				services.getLogger(), pcl);
		services.activateOffShoot(executionCallback);
		final long maxWaitTimeMillis = System.currentTimeMillis() + MAX_WAIT_TIME_MILLIS;
		boolean succeed = false;
		Exception ex = null;
		while (!succeed && (System.currentTimeMillis() <= maxWaitTimeMillis)) {
			try {
				array.addMonitorExecution(executionCallbackName,
						executionCallback._this());
				succeed = true;
			} catch (Exception e) {
				ex = e; // Save last Exception just in case
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		if (!succeed) {
			services.getLogger().severe(
					"Execution Callback could not be installed in Array.");
			throw new RuntimeException(
					"Execution Callback could not be installed in Array.", ex);
		}
	}
	

	
	public void registerGUICallback(PropertyChangeListener pcl)
			throws AcsJContainerServicesEx {
		services.getLogger().info("Registering GUI Callback");
		guiCallbackName = array.name() + "_" + System.currentTimeMillis();
		guiCallback = new ArrayGUICallbackImpl(services.getLogger(), pcl);
		services.activateOffShoot(guiCallback);
		final long maxWaitTimeMillis = System.currentTimeMillis() + MAX_WAIT_TIME_MILLIS;
		boolean succeed = false;
		Exception ex = null;
		while (!succeed && (System.currentTimeMillis() <= maxWaitTimeMillis)) {
			try {
				array.addMonitorGUI(guiCallbackName, guiCallback._this());
				succeed = true;
			} catch (Exception e) {
				ex = e; // Save last Exception just in case
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		if (!succeed) {
			services.getLogger().severe(
					"GUI Callback could not be installed in Array.");
			throw new RuntimeException(
					"GUI Callback could not be installed in Array.", ex);
		}
	}
	
	private void unregisterQueueCallback() throws AcsJContainerServicesEx {
		if (queueCallback != null) {
			try {
				array.removeMonitorQueue(queueCallbackName);
			} catch (Exception ex){
				System.out.println("Exception catched when tried to remove the monitor of the Sched Block Queue, continuing anway");
				ex.printStackTrace();
			}
			services.deactivateOffShoot(queueCallback);
		}
	}
	
	private void unregisterGUICallback() throws AcsJContainerServicesEx {
		if (guiCallback != null) {
			try {
				array.removeMonitorGUI(guiCallbackName);
			} catch (Exception ex){
				System.out.println("Exception catched when tried to remove the monitor of the array state for GUIs, continuing anway");
				ex.printStackTrace();
			}
			services.deactivateOffShoot(guiCallback);
		}
	}
	
	private void unregisterExecutionCallback() throws AcsJContainerServicesEx {
		if (queueCallback != null) {
			try {
				array.removeMonitorExecution(executionCallbackName);
			} catch (Exception ex){
				System.out.println("Exception catched when tried to remove the monitor of Sched Block Execution, continuing anway");
				ex.printStackTrace();
			}
			services.deactivateOffShoot(executionCallback);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		try {
			unregisterQueueCallback();
		} catch (Exception ex){
			ex.printStackTrace();
		}
		try {
			unregisterExecutionCallback();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			unregisterGUICallback();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public boolean isRunning() {
		return array.isRunning();
	}
	
	public boolean isActiveDynamic() {
		final long maxWaitTimeMillis = System.currentTimeMillis()
				+ MAX_WAIT_TIME_MILLIS;
		boolean succeed = false;
		Exception ex = null;
		while (!succeed && (System.currentTimeMillis() <= maxWaitTimeMillis)) {
			try {
				return array.isActiveDynamic();
			} catch (Exception e) {
				ex = e; // Save last Exception just in case
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		services.getLogger().severe(
				"Could not get connection to the Array.");
		throw new RuntimeException(
				"Could not get connection to the Array.", ex);
	}
	
	public boolean isDynamic() {
		return dynamic;
	}
	
	public boolean isManual() {
		final long maxWaitTimeMillis = System.currentTimeMillis()
				+ MAX_WAIT_TIME_MILLIS;
		boolean succeed = false;
		Exception ex = null;
		while (!succeed && (System.currentTimeMillis() <= maxWaitTimeMillis)) {
			try {
				return array.isManual();
			} catch (Exception e) {
				ex = e; // Save last Exception just in case
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		services.getLogger().severe(
				"Could not get connection to the Array.");
		throw new RuntimeException(
				"Could not get connection to the Array.", ex);
	}
	
	public boolean isFullAuto() {
		final long maxWaitTimeMillis = System.currentTimeMillis()
				+ MAX_WAIT_TIME_MILLIS;
		boolean succeed = false;
		Exception ex = null;
		while (!succeed && (System.currentTimeMillis() <= maxWaitTimeMillis)) {
			try {
				return array.isFullAuto();
			} catch (Exception e) {
				ex = e; // Save last Exception just in case
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		services.getLogger().severe("Could not get connection to the Array.");
		throw new RuntimeException("Could not get connection to the Array.", ex);
	}
	
	public void setFullAuto(boolean on, String name, String role) {
		array.setFullAuto(on, name, role);
	}
	
	public void setActiveDynamic(boolean on, String name, String role) {
		array.setActiveDynamic(on, name, role);
	}
	
	public ArraySchedulerMode[] getModes() {
		return array.getModes();
	}
	
	public void setSchedulingPolicy(String name) {
		array.configureDynamicScheduler(name);
	}
	
	/* For test purposes only */
	private static class DummyArrayImpl implements alma.scheduling.Array{

		@Override
		public void configure(String arrayName, ArraySchedulerMode[] modes,
				ArrayDescriptor descriptor) {
			
		}

		@Override
		public ArraySchedulerMode[] getModes() {
			return null;
		}

		@Override
		public ArraySchedulerLifecycleType getLifecycleType() {
			return null;
		}

		@Override
		public String getArrayName() {
			return null;
		}

		@Override
		public ArrayDescriptor getDescriptor() {
			return null;
		}

		@Override
		public String name() {
			return null;
		}

		@Override
		public ComponentStates componentState() {
			return null;
		}

		@Override
		public void addMonitorQueue(String arg0, SchedBlockQueueCallback arg1) {
			
		}

		@Override
		public void delete(SchedBlockQueueItem arg0) {
			
		}

		@Override
		public SchedBlockQueueItem[] getQueue() {
			return null;
		}

		@Override
		public int getQueueCapacity() {
			return 0;
		}

		@Override
		public SchedBlockQueueItem[] getExecutedQueue() {
			return null;
		}

		@Override
		public void moveDown(SchedBlockQueueItem arg0) {
			
		}

		@Override
		public void moveUp(SchedBlockQueueItem arg0) {
			
		}

		@Override
		public SchedBlockQueueItem pull() {
			return null;
		}

		@Override
		public void push(SchedBlockQueueItem arg0) {
			
		}

		@Override
		public void removeMonitorQueue(String monitorName) {
			
		}

		@Override
		public void start(String name, String role) {
			
		}

		@Override
		public void stop(String name, String role) {
			
		}

		@Override
		public String getRunningSchedBlock() throws NoRunningSchedBlockEx {
			return null;
		}

		@Override
		public boolean hasRunningSchedBlock() {
			return false;
		}

		@Override
		public void stopRunningSchedBlock(String name, String role) {
			
		}

		@Override
		public void addMonitorExecution(String monitorName,
				SchedBlockExecutionCallback callback) {
			
		}

		@Override
		public void removeMonitorExecution(String monitorName) {
			
		}

		@Override
		public void addMonitorGUI(String monitorName, ArrayGUICallback callback) {
			
		}

		@Override
		public void removeMonitorGUI(String monitorName) {
			
		}

		@Override
		public boolean isRunning() {
			return false;
		}

		@Override
		public boolean isManual() {
			return false;
		}

		@Override
		public boolean isFullAuto() {
			return false;
		}

		@Override
		public void setFullAuto(boolean on, String name, String role) {
			
		}

		@Override
		public void destroyArray(String name, String role) {
			
		}

		@Override
		public SchedBlockExecutionItem[] getExecutions() {
			return null;
		}

		@Override
		public IDLEntityRef startManualModeSession(String sbid)
				throws InvalidOperationEx {
			return null;
		}

		@Override
		public void configureDynamicScheduler(String policyName) {
			
		}

		@Override
		public boolean isActiveDynamic() {
			return false;
		}

		@Override
		public void setActiveDynamic(boolean on, String name, String role) {
			
		}

		@Override
		public String getSchedulingPolicy() {
			return null;
		}

		@Override
		public int getLogAmount() {
			return 0;
		}

		@Override
		public void setLogAmount(int n) {
			
		}

		@Override
		public boolean _is_a(String repositoryIdentifier) {
			return false;
		}

		@Override
		public boolean _is_equivalent(Object other) {
			return false;
		}

		@Override
		public boolean _non_existent() {
			return false;
		}

		@Override
		public int _hash(int maximum) {
			return 0;
		}

		@Override
		public Object _duplicate() {
			return null;
		}

		@Override
		public void _release() {
			
		}

		public InterfaceDef _get_interface() {
			return null;
		}

		@Override
		public Object _get_interface_def() {
			return null;
		}

		@Override
		public Request _request(String operation) {
			return null;
		}

		@Override
		public Request _create_request(Context ctx, String operation,
				NVList arg_list, NamedValue result) {
			return null;
		}

		@Override
		public Request _create_request(Context ctx, String operation,
				NVList arg_list, NamedValue result, ExceptionList exclist,
				ContextList ctxlist) {
			return null;
		}

		@Override
		public Policy _get_policy(int policy_type) {
			return null;
		}

		@Override
		public DomainManager[] _get_domain_managers() {
			return null;
		}

		@Override
		public Object _set_policy_override(Policy[] policies,
				SetOverrideType set_add) {
			return null;
		}
		
	}
}
