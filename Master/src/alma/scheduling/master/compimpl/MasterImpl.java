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
import java.util.logging.Logger;

import org.omg.CORBA.Object;

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
import alma.scheduling.Array;
import alma.scheduling.ArrayCreationInfo;
import alma.scheduling.ArrayEvent;
import alma.scheduling.ArrayHelper;
import alma.scheduling.ArrayModeEnum;
import alma.scheduling.ArraySchedulerLifecycleType;
import alma.scheduling.ArraySchedulerMode;
import alma.scheduling.ArrayStatusCallback;
import alma.scheduling.MasterOperations;
import alma.scheduling.array.util.NameTranslator;
import alma.scheduling.array.util.NameTranslator.TranslationException;
import alma.scheduling.datamodel.weather.dao.WeatherStationDao;
import alma.scheduling.utils.DSAContextFactory;
import alma.scheduling.utils.DynamicSchedulingPolicyFactory;

public class MasterImpl implements ComponentLifecycle,
        MasterOperations {

    private ContainerServices m_containerServices;
    private Logger m_logger;
    private ControlMaster controlMaster;
    private HashMap<String, ArrayModeEnum> activeArrays;
    private HashMap<String, ArrayStatusCallback> callbacks;
    private CurrentWeather weatherComp;
	
    /////////////////////////////////////////////////////////////
    // Implementation of ComponentLifecycle
    /////////////////////////////////////////////////////////////

    public MasterImpl(){
    	activeArrays = new HashMap<String, ArrayModeEnum>();
    	callbacks =  new HashMap<String, ArrayStatusCallback>();
    }
    
    public void initialize(ContainerServices containerServices) {
        m_containerServices = containerServices;
        if (m_logger == null)
        	m_logger = m_containerServices.getLogger();
        
        //Get Weather Station reference and pass it to Weather Station Dao.
        //Change this if Master Scheduler is not longer deployed in the same container
        //than the SCHEDULING Arrays
        try {
			weatherComp = CurrentWeatherHelper.narrow(containerServices.getDefaultComponent("IDL:alma/Control/CurrentWeather:1.0"));
			String nameComp = weatherComp.name();
			containerServices.releaseComponent(nameComp);
			weatherComp = null;
			weatherComp = CurrentWeatherHelper.narrow(containerServices.getComponentNonSticky(nameComp));
			WeatherStationDao.setWeatherStation(weatherComp);
		} catch (AcsJContainerServicesEx e) {
			m_logger.warning("Unable to retrieve Weather Station Controller Component: IDL:alma/Control/CurrentWeather:1.0");
		} catch (org.omg.CORBA.SystemException e) {
			m_logger.warning("Unable to retrieve Weather Station Controller Component: IDL:alma/Control/CurrentWeather:1.0");
		}
		m_logger.finest("initialize() called...");
    }

    public void execute() {
        m_logger.finest("execute() called...");
    }

    public void cleanUp() {
    	for(String arrayName: activeArrays.keySet()){
    		m_logger.warning("Triying to destroy Array: " + arrayName + " at cleanUp. " +
    				"You should destroy the arrays before component cleanUp");
    		try {
				destroyArray(arrayName, "Clean-up", "");
				m_logger.fine("Array: " + arrayName + " Destroyed successfully");
			} catch (ACSInternalExceptionEx e) {
				m_logger.severe("Array: " + arrayName + " Cannot be destroyed");
				AcsJSchedulingInternalExceptionEx ex = new AcsJSchedulingInternalExceptionEx(e); 
				ex.log(m_logger);
			} catch (ControlInternalExceptionEx e) {
				m_logger.severe("Array: " + arrayName + " Cannot be destroyed");
				AcsJSchedulingInternalExceptionEx ex = new AcsJSchedulingInternalExceptionEx(e); 
				ex.log(m_logger);
			} catch (SchedulingInternalExceptionEx e) {
				m_logger.severe("Array: " + arrayName + " Cannot be destroyed");
				AcsJSchedulingInternalExceptionEx ex = new AcsJSchedulingInternalExceptionEx(e); 
				ex.log(m_logger);
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
				AcsJControlInternalExceptionEx ex = 
					new AcsJControlInternalExceptionEx();
				ex.setProperty("reason", Constants.CONTROL_MASTER_URL + " component is not OPERATIONAL");
				ex.log(m_logger);
				throw ex.toControlInternalExceptionEx();
			}
		} catch (InaccessibleException e) {
			AcsJControlInternalExceptionEx ex = new AcsJControlInternalExceptionEx(e);
			ex.log(m_logger);
			throw ex.toControlInternalExceptionEx();
		} catch (InvalidRequest e) {
			AcsJControlInternalExceptionEx ex = new AcsJControlInternalExceptionEx(e);
			ex.log(m_logger);
			throw ex.toControlInternalExceptionEx();
		} catch(org.omg.CORBA.SystemException e) {
			AcsJControlInternalExceptionEx ex = new AcsJControlInternalExceptionEx(e);
			ex.log(m_logger);
			throw ex.toControlInternalExceptionEx();
		}
		
		try {
			createNewSchedulingArray(arrayName,
					                 schedulingMode,
					                 lifecycleType,
					                 policyName);
		} catch (AcsJContainerServicesEx e) {
			AcsJACSInternalExceptionEx ex = new AcsJACSInternalExceptionEx(e);
			ex.log(m_logger);
			throw ex.toACSInternalExceptionEx();
		} catch (AcsJSchedulingInternalExceptionEx e) {
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
		
		return arrayInfo;
	}

	@Override
	public void destroyArray(String arrayName, String name, String role) throws ACSInternalExceptionEx,
	ControlInternalExceptionEx, SchedulingInternalExceptionEx{
		Object obj = null;
		String schedArrayName = null;
		m_logger.info("About to destroy array: " + arrayName);
		Array array = null;
		try {
			schedArrayName = NameTranslator.arrayToComponentName(arrayName);
			obj = m_containerServices.getComponent(schedArrayName);
		} catch (AcsJContainerServicesEx e) {
			AcsJACSInternalExceptionEx ex = new AcsJACSInternalExceptionEx(e);
			ex.log(m_logger);
			throw ex.toACSInternalExceptionEx();
		} catch (TranslationException e) {
			AcsJSchedulingInternalExceptionEx ex = new AcsJSchedulingInternalExceptionEx(e);
			ex.log(m_logger);
			throw ex.toSchedulingInternalExceptionEx();
		}
		
		array = ArrayHelper.narrow(obj);
		m_logger.info("Stopping SchedBlock in " + schedArrayName);
		array.stop("Master Panel", "Master Panel");
		array.stopRunningSchedBlock("Master Panel", "Master Panel");
		array.destroyArray(name, role);
		
		obj = null;
		array = null;
		m_logger.info ("Releasing Scheduling Array " + schedArrayName);
		m_containerServices.releaseComponent(schedArrayName);
		
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
		} catch (InaccessibleException e) {
			AcsJControlInternalExceptionEx ex = new AcsJControlInternalExceptionEx(e);
			ex.log(m_logger);
			throw ex.toControlInternalExceptionEx();
		} catch (InvalidRequest e) {
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
	
	void setLogger(Logger logger) {
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
	
}
