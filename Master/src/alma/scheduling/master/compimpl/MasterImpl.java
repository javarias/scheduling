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

import java.util.logging.Logger;

import org.omg.CORBA.Object;

import alma.ACS.ComponentStates;
import alma.Control.ArrayIdentifier;
import alma.Control.ControlMaster;
import alma.Control.ControlMasterHelper;
import alma.Control.CorrelatorType;
import alma.Control.InaccessibleException;
import alma.Control.InvalidRequest;
import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.SchedulingMasterExceptions.ACSInternalExceptionEx;
import alma.SchedulingMasterExceptions.ControlInternalExceptionEx;
import alma.SchedulingMasterExceptions.SchedulingInternalExceptionEx;
import alma.SchedulingMasterExceptions.wrappers.AcsJACSInternalExceptionEx;
import alma.SchedulingMasterExceptions.wrappers.AcsJControlInternalExceptionEx;
import alma.SchedulingMasterExceptions.wrappers.AcsJSchedulingInternalExceptionEx;
import alma.acs.component.ComponentDescriptor;
import alma.acs.component.ComponentLifecycle;
import alma.acs.component.ComponentQueryDescriptor;
import alma.acs.container.ContainerServices;
import alma.scheduling.ArrayHelper;
import alma.scheduling.ArrayCreationInfo;
import alma.scheduling.ArrayModeEnum;
import alma.scheduling.ArraySchedulerLifecycleType;
import alma.scheduling.ArraySchedulerMode;
import alma.scheduling.MasterOperations;
import alma.scheduling.array.util.NameTranslator;
import alma.scheduling.array.util.NameTranslator.TranslationException;
import alma.scheduling.Array;

public class MasterImpl implements ComponentLifecycle,
        MasterOperations {

    private ContainerServices m_containerServices;
    private Logger m_logger;
    private ControlMaster controlMaster;
    
    private static final String CONTROL_MASTER_URL = "CONTROL/MASTER";
	
    /////////////////////////////////////////////////////////////
    // Implementation of ComponentLifecycle
    /////////////////////////////////////////////////////////////

    public void initialize(ContainerServices containerServices) {
        m_containerServices = containerServices;
        m_logger = m_containerServices.getLogger();

		m_logger.finest("About to get reference of " + CONTROL_MASTER_URL);
		try {
			controlMaster = ControlMasterHelper.narrow(m_containerServices
					.getComponent(CONTROL_MASTER_URL));
			m_logger.finest("Got reference of " +  CONTROL_MASTER_URL + " successfully");
		} catch (AcsJContainerServicesEx e) {
			m_logger.warning("Unable to get reference to " + CONTROL_MASTER_URL);
			throw new RuntimeException("Unable to get reference to " + CONTROL_MASTER_URL,
					e);
		}

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

    /////////////////////////////////////////////////////////////
    // Implementation of MasterOperations
    /////////////////////////////////////////////////////////////
    
	@Override
	public ArrayCreationInfo createArray(String[] antennaIdList, String[] photonicsList,
			CorrelatorType corrType, ArrayModeEnum schedulingMode, 
			ArraySchedulerLifecycleType lifecycleType) throws 
			ControlInternalExceptionEx, ACSInternalExceptionEx, SchedulingInternalExceptionEx{
		String arrayName;
		try {
			arrayName = createNewControlArray(antennaIdList, photonicsList, 
					corrType, schedulingMode);
		} catch (InaccessibleException e) {
			AcsJControlInternalExceptionEx ex = new AcsJControlInternalExceptionEx(e);
			ex.log(m_logger);
			throw ex.toControlInternalExceptionEx();
		} catch (InvalidRequest e) {
			AcsJControlInternalExceptionEx ex = new AcsJControlInternalExceptionEx(e);
			ex.log(m_logger);
			throw ex.toControlInternalExceptionEx();
		}
		
		try {
			createNewSchedulingArray(arrayName, schedulingMode, lifecycleType);
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
		
		return arrayInfo;
	}

	@Override
	public void destroyArray(String arrayName) throws ACSInternalExceptionEx,
	ControlInternalExceptionEx, SchedulingInternalExceptionEx{
		Object obj = null;
		String schedArrayName = null;
		try {
			schedArrayName = NameTranslator.arrayToComponentName(arrayName);
			m_containerServices.getComponent(schedArrayName);
		} catch (AcsJContainerServicesEx e) {
			AcsJACSInternalExceptionEx ex = new AcsJACSInternalExceptionEx(e);
			ex.log(m_logger);
			throw ex.toACSInternalExceptionEx();
		} catch (TranslationException e) {
			AcsJSchedulingInternalExceptionEx ex = new AcsJSchedulingInternalExceptionEx(e);
			ex.log(m_logger);
			throw ex.toSchedulingInternalExceptionEx();
		}
		
		//The CORBA reference should not be null, but just in case
		if (obj == null)
			return;
		//If Array is executing schedBlocks, stop it
		Array array = ArrayHelper.narrow(obj);
		m_logger.fine("Stopping SchedBlock in " + schedArrayName);
		array.stop();
		array.abortRunningSchedBlock();
		
		obj = null;
		array = null;
		m_logger.fine ("Releasing Scheduling Array " + schedArrayName);
		m_containerServices.releaseComponent(schedArrayName);
		
		try {
			m_logger.finest("About to release CONTROL Array");
			controlMaster.destroyArray(NameTranslator.arrayToControlComponentName(arrayName));
			m_logger.finest("Released CONTROL Array");
		} catch (InaccessibleException e) {
			AcsJControlInternalExceptionEx ex = new AcsJControlInternalExceptionEx(e);
			ex.log(m_logger);
			throw ex.toControlInternalExceptionEx();
		} catch (InvalidRequest e) {
			AcsJControlInternalExceptionEx ex = new AcsJControlInternalExceptionEx(e);
			ex.log(m_logger);
			ex.toControlInternalExceptionEx();
		} catch (TranslationException e) {
			AcsJSchedulingInternalExceptionEx ex = new AcsJSchedulingInternalExceptionEx(e);
			ex.log(m_logger);
			throw ex.toSchedulingInternalExceptionEx();
		}
		
	}
	
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
			m_logger.finest("Control master reference is OPERATIONAL. About to create CONTROL Array");
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
	
	private Array createNewSchedulingArray(String arrayName, ArrayModeEnum schedulingMode, 
			ArraySchedulerLifecycleType lifecycleType) throws AcsJContainerServicesEx, AcsJSchedulingInternalExceptionEx{
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
		System.out.println(info.getName());
		ComponentQueryDescriptor query = new ComponentQueryDescriptor();
		query.setComponentName(schedArrayURL);
		query.setComponentType("IDL:alma/scheduling/Array:1.0");
		dynamicComponent = m_containerServices.getCollocatedComponent(query, false, info.getName());
		if (dynamicComponent == null)
			return null;
		Array array = ArrayHelper.narrow(dynamicComponent);
		array.configure(arrayName, 
				convertToArraySchedulerMode(schedulingMode),lifecycleType);
		return array;
	}
	
	private ArraySchedulerMode[] convertToArraySchedulerMode(ArrayModeEnum mode){
		ArraySchedulerMode[] arrayModes = new ArraySchedulerMode[1];
		if (mode == ArrayModeEnum.INTERACTIVE || mode == ArrayModeEnum.QUEUED){
			arrayModes[0] = ArraySchedulerMode.INTERACTIVE_I;
		}
		else if (mode == ArrayModeEnum.DYNAMIC){
			arrayModes[0] = ArraySchedulerMode.DYNAMIC_ACTIVE_I;
		}
		else if (mode == ArrayModeEnum.MANUAL){
			arrayModes[0] = ArraySchedulerMode.MANUAL_I;
		}
		
		return arrayModes;
	}
}
