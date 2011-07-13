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

package alma.scheduling.master.compimpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.omg.CORBA.Context;
import org.omg.CORBA.ContextList;
import org.omg.CORBA.DomainManager;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.NVList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.Object;
import org.omg.CORBA.Policy;
import org.omg.CORBA.Request;
import org.omg.CORBA.SetOverrideType;

import alma.ACS.ComponentStates;
import alma.Control.ArrayIdentifier;
import alma.Control.ControlMaster;
import alma.Control.ControlMasterHelper;
import alma.Control.CorrelatorType;
import alma.Control.CurrentWeather;
import alma.Control.CurrentWeatherHelper;
import alma.Control.InaccessibleException;
import alma.Control.InvalidRequest;
import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.SchedulingArrayExceptions.NoRunningSchedBlockEx;
import alma.SchedulingExceptions.InvalidOperationEx;
import alma.SchedulingMasterExceptions.ACSInternalExceptionEx;
import alma.SchedulingMasterExceptions.ArrayNotFoundExceptionEx;
import alma.SchedulingMasterExceptions.ControlInternalExceptionEx;
import alma.SchedulingMasterExceptions.SchedulingInternalExceptionEx;
import alma.SchedulingMasterExceptions.wrappers.AcsJACSInternalExceptionEx;
import alma.SchedulingMasterExceptions.wrappers.AcsJArrayNotFoundExceptionEx;
import alma.SchedulingMasterExceptions.wrappers.AcsJControlInternalExceptionEx;
import alma.SchedulingMasterExceptions.wrappers.AcsJSchedulingInternalExceptionEx;
import alma.acs.component.ComponentDescriptor;
import alma.acs.component.ComponentLifecycle;
import alma.acs.component.ComponentQueryDescriptor;
import alma.acs.container.ContainerServices;
import alma.acs.logging.AcsLogger;
import alma.acs.logging.domainspecific.AudienceLogger;
import alma.acs.logging.domainspecific.AudienceLogger.Audience;
import alma.asdmIDLTypes.IDLEntityRef;
import alma.scheduling.Array;
import alma.scheduling.ArrayCreationInfo;
import alma.scheduling.ArrayEvent;
import alma.scheduling.ArrayGUICallback;
import alma.scheduling.ArrayHelper;
import alma.scheduling.ArrayModeEnum;
import alma.scheduling.ArraySchedulerLifecycleType;
import alma.scheduling.ArraySchedulerMode;
import alma.scheduling.ArrayStatusCallback;
import alma.scheduling.MasterOperations;
import alma.scheduling.SchedBlockExecutionCallback;
import alma.scheduling.SchedBlockExecutionItem;
import alma.scheduling.SchedBlockQueueCallback;
import alma.scheduling.SchedBlockQueueItem;
import alma.scheduling.array.util.NameTranslator;
import alma.scheduling.array.util.NameTranslator.TranslationException;
import alma.scheduling.datamodel.weather.dao.WeatherStationDao;
import alma.scheduling.utils.AudienceFlogger;
import alma.scheduling.utils.DSAContextFactory;
import alma.scheduling.utils.DynamicSchedulingPolicyFactory;
import alma.scheduling.utils.FakeAudienceFlogger;

public class MasterImpl implements ComponentLifecycle,
        MasterOperations {

    private ContainerServices m_containerServices;
    private AcsLogger m_logger;
    private AudienceFlogger operatorLog;
    private ControlMaster controlMaster;
    private HashMap<String, ArrayModeEnum> activeArrays;
    private HashMap<String, ArrayStatusCallback> callbacks;
    private CurrentWeather weatherComp;
	
    /////////////////////////////////////////////////////////////
    // Logging
    /////////////////////////////////////////////////////////////
 
    private void initialiseLoggers() {
    	if (m_logger == null || operatorLog == null) {
        	m_logger = m_containerServices.getLogger();
        	operatorLog = new AudienceFlogger(m_logger,
        			AudienceLogger.Audience.OPERATOR);
    	}
    }
	
    /////////////////////////////////////////////////////////////
    // Implementation of ComponentLifecycle
    /////////////////////////////////////////////////////////////

    public MasterImpl(){
    	activeArrays = new HashMap<String, ArrayModeEnum>();
    	callbacks =  new HashMap<String, ArrayStatusCallback>();
    }
    
    public void initialize(ContainerServices containerServices) {
        m_containerServices = containerServices;
        initialiseLoggers();
        
        //Get Weather Station reference and pass it to Weather Station Dao.
        //Change this if Master Scheduler is not longer deployed in the same container
        //than the SCHEDULING Arrays
        try {
			weatherComp = CurrentWeatherHelper.narrow(containerServices.getDefaultComponent(Constants.WEATHER_STATION_IF));
			String nameComp = weatherComp.name();
			containerServices.releaseComponent(nameComp);
			weatherComp = null;
			weatherComp = CurrentWeatherHelper.narrow(containerServices.getComponentNonSticky(nameComp));
			WeatherStationDao.setWeatherStation(weatherComp);
			operatorLog.info("Connected to Weather Station Controller Component");
		} catch (AcsJContainerServicesEx e) {
			m_logger.warning("Unable to retrieve Weather Station Controller Component: IDL:alma/Control/CurrentWeather:1.0");
			operatorLog.warning("Unable to retrieve Weather Station Controller Component %s", Constants.WEATHER_STATION_IF);
		} catch (org.omg.CORBA.SystemException e) {
			m_logger.warning("Unable to retrieve Weather Station Controller Component: IDL:alma/Control/CurrentWeather:1.0");
			operatorLog.warning("Unable to retrieve Weather Station Controller Component %s", Constants.WEATHER_STATION_IF);
		}
		m_logger.finest("initialize() called...");
    }

    public void execute() {
        m_logger.finest("execute() called...");
    }

    public void cleanUp() {
    	for(String arrayName: activeArrays.keySet()){
    		operatorLog.warning("Automatically destroying %s at cleanUp. " +
				"You should destroy the arrays before component cleanUp", arrayName);
    		m_logger.warning("Automatically destroying " + arrayName + " at cleanUp.");
    		try {
				destroyArray(arrayName, "Clean-up", "");
				m_logger.fine("Array: " + arrayName + " Destroyed successfully");
				operatorLog.info("%s destroyed", arrayName);
			} catch (ACSInternalExceptionEx e) {
				m_logger.severe("Array: " + arrayName + " Cannot be destroyed");
				AcsJSchedulingInternalExceptionEx ex = new AcsJSchedulingInternalExceptionEx(e); 
				ex.log(m_logger);
				operatorLog.warning("Cannot destroy %s - ACS internal error", arrayName);
			} catch (ControlInternalExceptionEx e) {
				m_logger.severe("Array: " + arrayName + " Cannot be destroyed");
				AcsJSchedulingInternalExceptionEx ex = new AcsJSchedulingInternalExceptionEx(e); 
				ex.log(m_logger);
				operatorLog.warning("Cannot destroy %s - Control subsystem internal error", arrayName);
			} catch (SchedulingInternalExceptionEx e) {
				m_logger.severe("Array: " + arrayName + " Cannot be destroyed");
				AcsJSchedulingInternalExceptionEx ex = new AcsJSchedulingInternalExceptionEx(e); 
				ex.log(m_logger);
				operatorLog.warning("Cannot destroy %s - Scheduling internal error", arrayName);
			}
    	}
    	activeArrays = null;
        m_logger.finest("cleanUp() called");
    }

    public void aboutToAbort() {
        cleanUp();
        m_logger.finest("managed to abort...");
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

    /////////////////////////////////////////////////////////////
    // Implementation of MasterOperations
    /////////////////////////////////////////////////////////////
    
	@Override
	public synchronized ArrayCreationInfo createArray(String[] antennaIdList, String[] photonicsList,
			CorrelatorType corrType, ArrayModeEnum schedulingMode, 
			ArraySchedulerLifecycleType lifecycleType,
			String policyName) throws 
			ControlInternalExceptionEx, ACSInternalExceptionEx, SchedulingInternalExceptionEx{
		String arrayName = null;
		while (!isInitialized()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				//Do nothing, try again
			}
		}
		try {
			arrayName = createNewControlArray(antennaIdList, photonicsList, 
					corrType, schedulingMode);
			if (arrayName == null){
				operatorLog.warning("Cannot create array - %s component is not OPERATIONAL",
						             Constants.CONTROL_MASTER_URL);
				AcsJControlInternalExceptionEx ex = 
					new AcsJControlInternalExceptionEx();
				ex.setProperty("reason", Constants.CONTROL_MASTER_URL + " component is not OPERATIONAL");
				ex.log(m_logger);
				throw ex.toControlInternalExceptionEx();
			}
			operatorLog.info("Created CONTROL component for %s",
						     arrayName);
		} catch (InaccessibleException e) {
			operatorLog.severe("Cannot create CONTROL component for array - CONTROL internal error");
			AcsJControlInternalExceptionEx ex = new AcsJControlInternalExceptionEx(e);
			ex.log(m_logger);
			throw ex.toControlInternalExceptionEx();
		} catch (InvalidRequest e) {
			operatorLog.severe("Cannot create CONTROL component for array - CONTROL internal error");
			AcsJControlInternalExceptionEx ex = new AcsJControlInternalExceptionEx(e);
			ex.log(m_logger);
			throw ex.toControlInternalExceptionEx();
		} catch(org.omg.CORBA.SystemException e) {
			operatorLog.severe("Cannot create CONTROL component for array - system error");
			AcsJControlInternalExceptionEx ex = new AcsJControlInternalExceptionEx(e);
			ex.log(m_logger);
			throw ex.toControlInternalExceptionEx();
		}
		
		try {
			createNewSchedulingArray(arrayName,
					                 schedulingMode,
					                 lifecycleType,
					                 policyName);
			operatorLog.info("Created SCHEDULING component for %s",
				     arrayName);
		} catch (AcsJContainerServicesEx e) {
			operatorLog.severe("Cannot create CONTROL component for array - ACS internal error");
			AcsJACSInternalExceptionEx ex = new AcsJACSInternalExceptionEx(e);
			ex.log(m_logger);
			throw ex.toACSInternalExceptionEx();
		} catch (AcsJSchedulingInternalExceptionEx e) {
			operatorLog.severe("Cannot create CONTROL component for array - SCHEDULING internal error");
			e.log(m_logger);
			throw e.toSchedulingInternalExceptionEx();
		}
		
		ArrayCreationInfo arrayInfo;
		try {
			arrayInfo = new ArrayCreationInfo(arrayName, NameTranslator.arrayToComponentName(arrayName));
		} catch (TranslationException e) {
			AcsJSchedulingInternalExceptionEx ex = new AcsJSchedulingInternalExceptionEx(e);
			throw ex.toSchedulingInternalExceptionEx();
		}
		
		m_logger.info(String.format(
				"adding (%s, %s) to active arrays",
				arrayInfo.arrayId,
				schedulingMode));
		
		activeArrays.put(arrayInfo.arrayId, schedulingMode);
		
		//Notify to the callbacks
		ArrayList<String> toBeDeleted =  new ArrayList<String>();
		for (String key: callbacks.keySet()){
			try {
				callbacks.get(key).report(ArrayEvent.CREATION, schedulingMode, arrayInfo.arrayId);
			} catch (org.omg.CORBA.SystemException e) {
				m_logger.warning("Forcing release of callback " + key + ". Callback is not responding");
				toBeDeleted.add(key);
			}
		}
		for (String key: toBeDeleted)
			callbacks.remove(key);
		
		operatorLog.info("Created %s", arrayName);

		return arrayInfo;
	}

	@Override
	public void destroyArray(String arrayName, String name, String role) throws ACSInternalExceptionEx,
			ControlInternalExceptionEx, SchedulingInternalExceptionEx{
		Object obj = null;
		String schedArrayName = null;
		operatorLog.info("Destroying %s", arrayName);
		m_logger.info("About to destroy array: " + arrayName);
		Array array = null;
		try {
			schedArrayName = NameTranslator.arrayToComponentName(arrayName);
			obj = m_containerServices.getComponent(schedArrayName);
		} catch (AcsJContainerServicesEx e) {
			operatorLog.warning("Cannot destroy %s - ACS internal error", arrayName);
			AcsJACSInternalExceptionEx ex = new AcsJACSInternalExceptionEx(e);
			ex.log(m_logger);
			throw ex.toACSInternalExceptionEx();
		} catch (TranslationException e) {
			operatorLog.warning("Cannot destroy %s - Scheduling internal error", arrayName);
			AcsJSchedulingInternalExceptionEx ex = new AcsJSchedulingInternalExceptionEx(e);
			ex.log(m_logger);
			throw ex.toSchedulingInternalExceptionEx();
		}
		
		array = ArrayHelper.narrow(obj);
		m_logger.info("Stopping SchedBlock in " + schedArrayName);
		if (array.hasRunningSchedBlock()) {
			try {
				operatorLog.info("Stopping SchedBlock %s on %s",
						array.getRunningSchedBlock(),
						arrayName);
			} catch (NoRunningSchedBlockEx e) {
				// Quietly ignore it as we've just checked for one...
			}
		}
		array.stop("Master Panel", "Master Panel");
		array.stopRunningSchedBlock("Master Panel", "Master Panel");
		array.destroyArray(name, role);
		
		obj = null;
		array = null;
		m_logger.info ("Releasing Scheduling Array " + schedArrayName);
		m_containerServices.releaseComponent(schedArrayName);
		operatorLog.info("Releasing Scheduling component %s", schedArrayName);
		
		//Notify to the callbacks
		ArrayList<String> toBeDeleted =  new ArrayList<String>();
		for (String key: callbacks.keySet()){
			try {
				try {
					callbacks.get(key).report(ArrayEvent.DESTRUCTION, getSchedulerModeForArray(arrayName), arrayName);
				} catch (ArrayNotFoundExceptionEx e) {
					//This exception should not be throw
				}
			} catch (org.omg.CORBA.SystemException e) {
				m_logger.warning("Forcing release of callback " + key + ". Callback is not responding");
				toBeDeleted.add(key);
			}
		}
		for (String key: toBeDeleted)
			callbacks.remove(key);
		
		try {
			m_logger.finest("About to release CONTROL Array");
			controlMaster.destroyArray(arrayName);
			m_logger.finest("Released CONTROL Array");
			operatorLog.info("Released Control component for %s", arrayName);
		} catch (InaccessibleException e) {
			operatorLog.warning("Cannot release CONTROL component for %s - CONTROL internal error", arrayName);
			AcsJControlInternalExceptionEx ex = new AcsJControlInternalExceptionEx(e);
			ex.log(m_logger);
			throw ex.toControlInternalExceptionEx();
		} catch (InvalidRequest e) {
			operatorLog.warning("Cannot release CONTROL component for %s - CONTROL internal error", arrayName);
			AcsJControlInternalExceptionEx ex = new AcsJControlInternalExceptionEx(e);
			ex.log(m_logger);
			ex.toControlInternalExceptionEx();
		}
		m_logger.fine(String.format(
				"removing (%s) from active arrays",
				arrayName));
		activeArrays.remove(arrayName);
		
	}
	
	@Override
	public String[] getActiveAutomaticArrays() {
		ArrayList<String> arrays = new ArrayList<String>();
		for (String arrayName: activeArrays.keySet())
			if(activeArrays.get(arrayName) != ArrayModeEnum.MANUAL)
				arrays.add(arrayName);
		String[] retval = new String[arrays.size()];
		arrays.toArray(retval);
		return retval;
	}

	@Override
	public String[] getActiveManualArrays() {
		ArrayList<String> arrays = new ArrayList<String>();
		for (String arrayName: activeArrays.keySet())
			if(activeArrays.get(arrayName) == ArrayModeEnum.MANUAL)
				arrays.add(arrayName);
		String[] retval = new String[arrays.size()];
		arrays.toArray(retval);
		return retval;
	}

	@Override
	public ArrayModeEnum getSchedulerModeForArray(String arrayName) throws ArrayNotFoundExceptionEx {
		ArrayModeEnum arrayMode = activeArrays.get(arrayName);
		if (arrayMode == null){
			AcsJArrayNotFoundExceptionEx ex = new AcsJArrayNotFoundExceptionEx();
			ex.log(m_logger);
			throw ex.toArrayNotFoundExceptionEx();
		}
		return arrayMode; 
	}

    /////////////////////////////////////////////////////////////
    // Miscellany
    /////////////////////////////////////////////////////////////

	/**
	 * 
	 * Create a new CONTROL Array
	 * 
	 * @param antennaIdList
	 * @param photonicsList
	 * @param corrType
	 * @param schedulingMode
	 * 
	 * @return The name of the array
	 * 
	 * @throws InaccessibleException
	 * @throws InvalidRequest
	 */
	private String createNewControlArray(String[] antennaIdList, String[] photonicsList,
			CorrelatorType corrType, ArrayModeEnum schedulingMode) throws InaccessibleException, InvalidRequest{
		
		if( controlMaster.getMasterState() == alma.Control.SystemState.OPERATIONAL){
			m_logger.info("Control master reference is OPERATIONAL. About to create CONTROL Array");
			ArrayIdentifier arrayId;
			if(schedulingMode == ArrayModeEnum.MANUAL){
				arrayId = controlMaster.createManualArray(antennaIdList, 
						photonicsList, corrType);
				return arrayId.arrayName;
			}
			else { //DYNAMIC, INTERACTIVE and QUEUE
				arrayId = controlMaster.createAutomaticArray(antennaIdList, 
						photonicsList, corrType);
				return arrayId.arrayName;
			}
			
		}
		else 
			m_logger.warning("Control master reference is not OPERATIONAL. Cannot create CONTROL Array");
		return null;
	}
	
	private Array createNewSchedulingArray(String arrayName,
			                               ArrayModeEnum schedulingMode, 
			                               ArraySchedulerLifecycleType lifecycleType,
			                               String policyName)
				throws AcsJContainerServicesEx, AcsJSchedulingInternalExceptionEx {
		String schedArrayURL = null;
		try {
			schedArrayURL = NameTranslator.arrayToComponentName(arrayName);
		} catch (TranslationException e) {
			AcsJSchedulingInternalExceptionEx ex = new AcsJSchedulingInternalExceptionEx(e);
			ex.log(m_logger);
			throw ex;
		}
		Object dynamicComponent = null;
		
		ComponentDescriptor info = m_containerServices.getComponentDescriptor(m_containerServices.getName());
		ComponentQueryDescriptor query = new ComponentQueryDescriptor();
		query.setComponentName(schedArrayURL);
		query.setComponentType("IDL:alma/scheduling/Array:1.0");
		dynamicComponent = m_containerServices.getCollocatedComponent(query, false, info.getName());
		if (dynamicComponent == null)
			return null;
		Array array = ArrayHelper.narrow(dynamicComponent);
		array.configure(arrayName, 
				convertToArraySchedulerMode(schedulingMode),lifecycleType);
		if (schedulingMode == ArrayModeEnum.DYNAMIC) {
			array.configureDynamicScheduler(policyName);
		}
		return array;
	}
	
	private ArraySchedulerMode[] convertToArraySchedulerMode(ArrayModeEnum mode){
		ArraySchedulerMode[] arrayModes = new ArraySchedulerMode[1];
		if (mode == ArrayModeEnum.INTERACTIVE || mode == ArrayModeEnum.QUEUED){
			arrayModes[0] = ArraySchedulerMode.INTERACTIVE_I;
		}
		else if (mode == ArrayModeEnum.DYNAMIC){
			arrayModes[0] = ArraySchedulerMode.DYNAMIC_PASSIVE_I;
		}
		else if (mode == ArrayModeEnum.MANUAL){
			arrayModes[0] = ArraySchedulerMode.MANUAL_I;
		}
		
		return arrayModes;
	}
	
	
	private boolean isInitialized(){
		if (controlMaster != null)
			return true;
		try {
			initialize();
		} catch (AcsJContainerServicesEx e) {
			e.printStackTrace();
			return false;
		}
		return true;
		
	}
	
	void setControlMaster(ControlMaster controlComp) {
		controlMaster = controlComp;
	}
	
	void setLogger(AcsLogger logger) {
		m_logger = logger;
	}
	
	private void initialize() throws AcsJContainerServicesEx {
		m_logger.finest("About to get reference of "
				+ Constants.CONTROL_MASTER_URL);
		controlMaster = ControlMasterHelper.narrow(m_containerServices
				.getComponent(Constants.CONTROL_MASTER_URL));
		m_logger.finest("Got reference of " + Constants.CONTROL_MASTER_URL
				+ " successfully");
	}

	@Override
	public void addMonitorMaster(String monitorName,
			ArrayStatusCallback callback) {
		callbacks.put(monitorName, callback);
		
	}

	@Override
	public void removeMonitorQueue(String monitorName) {
		callbacks.remove(monitorName);
		
	}

	@Override
	public String[] getSchedulingPolicies() {
		List<String> policies = DSAContextFactory.getPolicyNames();
		String[] retval = new String[policies.size()];
		return policies.toArray(retval);
	}

	@Override
	public void addSchedulingPolicies(String xmlString) {
		DynamicSchedulingPolicyFactory.getInstance().createDSAPolicyBeans(xmlString);
	}
	
	
	public static void main(String args[]) {
		final FakeAudienceFlogger operatorLog =
			new FakeAudienceFlogger("Test", Audience.OPERATOR);
		final String arrayName = "Array666";
		String schedArrayName;
		
		try {
			schedArrayName = NameTranslator.arrayToComponentName(arrayName);
		} catch (TranslationException e) {
			schedArrayName = e.getMessage();
		}
		final Array  array = new Array(){
			/**  */
			private static final long serialVersionUID = 1L;
			@Override public void configure(String arrayName, ArraySchedulerMode[] modes, ArraySchedulerLifecycleType lifecycleType) {}
			@Override public void configureDynamicScheduler(String policyName) {}
			@Override public ArraySchedulerMode[] getModes() {return null;}
			@Override public ArraySchedulerLifecycleType getLifecycleType() {return null;}
			@Override public String getArrayName() {return null;}
			@Override public String name() {return null;}
			@Override public ComponentStates componentState() {return null;}
			@Override public void addMonitorQueue(String arg0, SchedBlockQueueCallback arg1) {}
			@Override public void delete(SchedBlockQueueItem arg0) {}
			@Override public SchedBlockQueueItem[] getQueue() {return null;}
			@Override public int getQueueCapacity() {return 0;}
			@Override public SchedBlockQueueItem[] getExecutedQueue() {return null;}
			@Override public void moveDown(SchedBlockQueueItem arg0) {}
			@Override public void moveUp(SchedBlockQueueItem arg0) {}
			@Override public SchedBlockQueueItem pull() {return null;}
			@Override public void push(SchedBlockQueueItem arg0) {}
			@Override public void removeMonitorQueue(String monitorName) {}
			@Override public void start(String name, String role) {}
			@Override public void stop(String name, String role) {}
			@Override public String getRunningSchedBlock() throws NoRunningSchedBlockEx {return "uid://A666//X0/Xfake";}
			@Override public boolean hasRunningSchedBlock() {return true;}
			@Override public void stopRunningSchedBlock(String name, String role) {}
			@Override public void addMonitorExecution(String monitorName, SchedBlockExecutionCallback callback) {}
			@Override public void removeMonitorExecution(String monitorName) {}
			@Override public void addMonitorGUI(String monitorName, ArrayGUICallback callback) {}
			@Override public void removeMonitorGUI(String monitorName) {}
			@Override public boolean isRunning() {return false;}
			@Override public boolean isManual() {return false;}
			@Override public boolean isFullAuto() {return false;}
			@Override public void setFullAuto(boolean on, String name, String role) {}
			@Override public void destroyArray(String name, String role) {}
			@Override public SchedBlockExecutionItem[] getExecutions() {return null;}
			@Override public IDLEntityRef startManualModeSession(String sbid) throws InvalidOperationEx {return null;}
			@Override public boolean _is_a(String repositoryIdentifier) {return false;}
			@Override public boolean _is_equivalent(Object other) {return false;}
			@Override public boolean _non_existent() {return false;}
			@Override public int _hash(int maximum) {return 0;}
			@Override public Object _duplicate() {return null;}
			@Override public void _release() {}
			@Override public Object _get_interface_def() {return null;}
			@Override public Request _request(String operation) {return null;}
			@Override public Request _create_request(Context ctx, String operation, NVList arg_list, NamedValue result) {return null;}
			@Override public Request _create_request(Context ctx, String operation, NVList arg_list, NamedValue result, ExceptionList exclist, ContextList ctxlist) {return null;}
			@Override public Policy _get_policy(int policy_type) {return null;}
			@Override public DomainManager[] _get_domain_managers() {return null;}
			@Override public Object _set_policy_override(Policy[] policies, SetOverrideType set_add) {return null;}
		};
		
		// initialize()
		operatorLog.info("Connected to Weather Station Controller Component");
		operatorLog.warning("Unable to retrieve Weather Station Controller Component %s", Constants.WEATHER_STATION_IF);
		operatorLog.warning("Unable to retrieve Weather Station Controller Component %s", Constants.WEATHER_STATION_IF);
		
		// cleanUp()
		operatorLog.warning("Automatically destroying %s at cleanUp. " +
				"You should destroy the arrays before component cleanUp", arrayName);
		operatorLog.info("%s destroyed", arrayName);
		operatorLog.warning("Cannot destroy %s - ACS internal error", arrayName);
		operatorLog.warning("Cannot destroy %s - Control subsystem internal error", arrayName);
		operatorLog.warning("Cannot destroy %s - Scheduling internal error", arrayName);
		
		// createArray()
		operatorLog.warning("Cannot create array - %s component is not OPERATIONAL",
				Constants.CONTROL_MASTER_URL);
		operatorLog.info("Created CONTROL component for %s",
				arrayName);
		operatorLog.severe("Cannot create CONTROL component for array - CONTROL internal error");
		operatorLog.severe("Cannot create CONTROL component for array - CONTROL internal error");
		operatorLog.severe("Cannot create CONTROL component for array - system error");
		operatorLog.info("Created SCHEDULING component for %s",
				arrayName);
		operatorLog.severe("Cannot create CONTROL component for array - ACS internal error");
		operatorLog.severe("Cannot create CONTROL component for array - SCHEDULING internal error");
		operatorLog.info("Created %s", arrayName);

		// destroyArray()
		operatorLog.info("Destroying %s", arrayName);
		operatorLog.warning("Cannot destroy %s - ACS internal error", arrayName);
		operatorLog.warning("Cannot destroy %s - Scheduling internal error", arrayName);
		try {
			operatorLog.info("Stopping SchedBlock %s on %s",
					array.getRunningSchedBlock(),
					arrayName);
		} catch (NoRunningSchedBlockEx e) {
			operatorLog.info("Stopping SchedBlock %s on %s",
					">> IT WENT WRONG <<",
					arrayName);
		}
		operatorLog.info("Releasing Scheduling component %s", schedArrayName);
		operatorLog.info("Released Control component for %s", arrayName);
		operatorLog.warning("Cannot release CONTROL component for %s - CONTROL internal error", arrayName);
		operatorLog.warning("Cannot release CONTROL component for %s - CONTROL internal error", arrayName);

	}
}
