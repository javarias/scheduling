/*
 * ALMA - Atacama Large Millimetre Array
 * (c) European Southern Observatory, 2010
 * (c) Associated Universities Inc., 2010
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 * 
 */
package alma.scheduling.acsFacades;

import alma.ACS.ComponentStates;
import alma.ACSErrTypeCommon.IllegalArgumentEx;
import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.container.ContainerServices;
import alma.asdmIDLTypes.IDLArrayTime;
import alma.lifecycle.stateengine.constants.Subsystem;
import alma.projectlifecycle.StateChangeData;
import alma.projectlifecycle.StateSystemOperations;
import alma.scheduling.utils.Profiler;
import alma.statearchiveexceptions.EntitySerializationFailedEx;
import alma.statearchiveexceptions.InappropriateEntityTypeEx;
import alma.statearchiveexceptions.NoSuchEntityEx;
import alma.statearchiveexceptions.NullEntityIdEx;
import alma.statearchiveexceptions.StateIOFailedEx;
import alma.stateengineexceptions.NoSuchTransitionEx;
import alma.stateengineexceptions.NotAuthorizedEx;
import alma.stateengineexceptions.PostconditionFailedEx;
import alma.stateengineexceptions.PreconditionFailedEx;
import alma.xmlentity.XmlEntityStruct;


/**
 * A facade for the StateSystem which logs calls made to it.
 *
 * @version $Id: ProfilingStateSystem.java,v 1.2 2012/08/14 16:22:53 javarias Exp $
 * @author David Clarke
 */
public class ProfilingStateSystem
	extends AbstractProfilingComponent
	implements StateSystemOperations {

	/** The object for which we are a facade. */
	private StateSystemOperations delegate;

	/**
	 * Construct this object
	 * 
	 * @throws AcsJContainerServicesEx 
	 */
	public ProfilingStateSystem(ContainerServices     containerServices,
			                  StateSystemOperations delegate)
		throws AcsJContainerServicesEx {
		super(containerServices);
        this.delegate = delegate;
	}


	
	/*
	 * ================================================================
	 * Delegation
	 * ================================================================
	 */
	/**
	 * @param target
	 * @param destinationState
	 * @param subsys
	 * @param userID
	 * @return
	 * @throws PreconditionFailedEx
	 * @throws NoSuchEntityEx
	 * @throws IllegalArgumentEx
	 * @throws NoSuchTransitionEx
	 * @throws PostconditionFailedEx
	 * @throws NotAuthorizedEx
	 * @see alma.projectlifecycle.StateSystemOperations#changeOUSStatus(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean changeOUSStatus(String target, String destinationState,
			String subsys, String userID) throws PreconditionFailedEx,
			NoSuchEntityEx, IllegalArgumentEx, NoSuchTransitionEx,
			PostconditionFailedEx, NotAuthorizedEx {
		profiler.start("changeOUSStatus()");
		boolean result = delegate.changeOUSStatus(target, destinationState, subsys, userID);
		profiler.end();
		return result;
	}

	/**
	 * @param target
	 * @param destinationState
	 * @param subsys
	 * @param userID
	 * @return
	 * @throws PreconditionFailedEx
	 * @throws NoSuchEntityEx
	 * @throws IllegalArgumentEx
	 * @throws NoSuchTransitionEx
	 * @throws PostconditionFailedEx
	 * @throws NotAuthorizedEx
	 * @see alma.projectlifecycle.StateSystemOperations#changeProjectStatus(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean changeProjectStatus(String target, String destinationState,
			String subsys, String userID) throws PreconditionFailedEx,
			NoSuchEntityEx, IllegalArgumentEx, NoSuchTransitionEx,
			PostconditionFailedEx, NotAuthorizedEx {
		profiler.start("changeProjectStatus()");
		boolean result = delegate.changeProjectStatus(target, destinationState, subsys, userID);
		profiler.end();
		return result;
	}

	/**
	 * @param target
	 * @param destinationState
	 * @param subsys
	 * @param userID
	 * @return
	 * @throws PreconditionFailedEx
	 * @throws NoSuchEntityEx
	 * @throws IllegalArgumentEx
	 * @throws NoSuchTransitionEx
	 * @throws PostconditionFailedEx
	 * @throws NotAuthorizedEx
	 * @see alma.projectlifecycle.StateSystemOperations#changeSBStatus(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean changeSBStatus(String target, String destinationState,
			String subsys, String userID) throws PreconditionFailedEx,
			NoSuchEntityEx, IllegalArgumentEx, NoSuchTransitionEx,
			PostconditionFailedEx, NotAuthorizedEx {
		profiler.start("changeSBStatus()");
		boolean result = delegate.changeSBStatus(target, destinationState, subsys, userID);
		profiler.end();
		return result;
	}

	/**
	 * @return
	 * @see alma.ACS.ACSComponentOperations#componentState()
	 */
	public ComponentStates componentState() {
		profiler.start("componentState()");
		ComponentStates result = delegate.componentState();
		profiler.end();
		return result;
	}

	/**
	 * @param states
	 * @return
	 * @throws InappropriateEntityTypeEx
	 * @throws IllegalArgumentEx
	 * @see alma.projectlifecycle.StateSystemOperations#findProjectStatusByState(java.lang.String[])
	 */
	public XmlEntityStruct[] findProjectStatusByState(String[] states)
			throws InappropriateEntityTypeEx, IllegalArgumentEx {
		profiler.start("findProjectStatusByState()");
		XmlEntityStruct[] result = delegate.findProjectStatusByState(states);
		profiler.end();
		return result;
	}

	/**
	 * @param states
	 * @return
	 * @throws InappropriateEntityTypeEx
	 * @throws IllegalArgumentEx
	 * @see alma.projectlifecycle.StateSystemOperations#findSBStatusByState(java.lang.String[])
	 */
	public XmlEntityStruct[] findSBStatusByState(String[] states)
			throws InappropriateEntityTypeEx, IllegalArgumentEx {
		profiler.start("findSBStatusByState()");
		XmlEntityStruct[] result = delegate.findSBStatusByState(states);
		profiler.end();
		return result;
	}

	/**
	 * @param start
	 * @param end
	 * @param domainEntityId
	 * @param state
	 * @param userId
	 * @param type
	 * @return
	 * @throws StateIOFailedEx
	 * @see alma.projectlifecycle.StateSystemOperations#findStateChangeRecords(alma.asdmIDLTypes.IDLArrayTime, alma.asdmIDLTypes.IDLArrayTime, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public StateChangeData[] findStateChangeRecords(IDLArrayTime start,
			IDLArrayTime end, String domainEntityId, String state,
			String userId, String type) throws StateIOFailedEx {
		profiler.start("findStateChangeRecords()");
		StateChangeData[] result = delegate.findStateChangeRecords(start, end, domainEntityId, state, userId, type);
		profiler.end();
		return result;
	}

	/**
	 * @param subsystem
	 * @return
	 * @see alma.projectlifecycle.StateSystemOperations#getObsProjectStates(java.lang.String)
	 */
	public String getObsProjectStates(String subsystem) {
		profiler.start("getObsProjectStates()");
		String result = delegate.getObsProjectStates(subsystem);
		profiler.end();
		return result;
	}

	/**
	 * @param subsystem
	 * @return
	 * @see alma.projectlifecycle.StateSystemOperations#getObsUnitSetStates(java.lang.String)
	 */
	public String getObsUnitSetStates(String subsystem) {
		profiler.start("getObsUnitSetStates()");
		String result = delegate.getObsUnitSetStates(subsystem);
		profiler.end();
		return result;
	}

	/**
	 * @param id
	 * @return
	 * @throws InappropriateEntityTypeEx
	 * @throws NullEntityIdEx
	 * @throws NoSuchEntityEx
	 * @see alma.projectlifecycle.StateSystemOperations#getOUSStatus(java.lang.String)
	 */
	public XmlEntityStruct getOUSStatus(String id)
			throws InappropriateEntityTypeEx, NullEntityIdEx, NoSuchEntityEx {
		profiler.start("getOUSStatus()");
		XmlEntityStruct result = delegate.getOUSStatus(id);
		profiler.end();
		return result;
	}

	/**
	 * @param id
	 * @return
	 * @throws InappropriateEntityTypeEx
	 * @throws NullEntityIdEx
	 * @throws NoSuchEntityEx
	 * @see alma.projectlifecycle.StateSystemOperations#getOUSStatusXml(java.lang.String)
	 */
	public String getOUSStatusXml(String id) throws InappropriateEntityTypeEx,
			NullEntityIdEx, NoSuchEntityEx {
		profiler.start("getOUSStatusXml()");
		String result = delegate.getOUSStatusXml(id);
		profiler.end();
		return result;
	}

	/**
	 * @param id
	 * @return
	 * @throws InappropriateEntityTypeEx
	 * @throws NullEntityIdEx
	 * @throws NoSuchEntityEx
	 * @see alma.projectlifecycle.StateSystemOperations#getProjectStatus(java.lang.String)
	 */
	public XmlEntityStruct getProjectStatus(String id)
			throws InappropriateEntityTypeEx, NullEntityIdEx, NoSuchEntityEx {
		profiler.start("getProjectStatus()");
		XmlEntityStruct result = delegate.getProjectStatus(id);
		profiler.end();
		return result;
	}

	/**
	 * @param id
	 * @return
	 * @throws EntitySerializationFailedEx
	 * @throws InappropriateEntityTypeEx
	 * @throws NullEntityIdEx
	 * @throws NoSuchEntityEx
	 * @see alma.projectlifecycle.StateSystemOperations#getProjectStatusList(java.lang.String)
	 */
	public XmlEntityStruct[] getProjectStatusList(String id)
			throws EntitySerializationFailedEx, InappropriateEntityTypeEx,
			NullEntityIdEx, NoSuchEntityEx {
		profiler.start("getProjectStatusList()");
		XmlEntityStruct[] result = delegate.getProjectStatusList(id);
		profiler.end();
		return result;
	}

	/**
	 * @param id
	 * @return
	 * @throws InappropriateEntityTypeEx
	 * @throws NullEntityIdEx
	 * @throws NoSuchEntityEx
	 * @see alma.projectlifecycle.StateSystemOperations#getProjectStatusXml(java.lang.String)
	 */
	public String getProjectStatusXml(String id)
			throws InappropriateEntityTypeEx, NullEntityIdEx, NoSuchEntityEx {
		profiler.start("getProjectStatusXml()");
		String result = delegate.getProjectStatusXml(id);
		profiler.end();
		return result;
	}

	/**
	 * @param id
	 * @return
	 * @throws InappropriateEntityTypeEx
	 * @throws NullEntityIdEx
	 * @throws NoSuchEntityEx
	 * @see alma.projectlifecycle.StateSystemOperations#getProjectStatusXmlList(java.lang.String)
	 */
	public String[] getProjectStatusXmlList(String id)
			throws InappropriateEntityTypeEx, NullEntityIdEx, NoSuchEntityEx {
		profiler.start("getProjectStatusXmlList()");
		String[] result = delegate.getProjectStatusXmlList(id);
		profiler.end();
		return result;
	}

	/**
	 * @param id
	 * @return
	 * @throws InappropriateEntityTypeEx
	 * @throws NullEntityIdEx
	 * @throws NoSuchEntityEx
	 * @see alma.projectlifecycle.StateSystemOperations#getSBStatus(java.lang.String)
	 */
	public XmlEntityStruct getSBStatus(String id)
			throws InappropriateEntityTypeEx, NullEntityIdEx, NoSuchEntityEx {
		profiler.start("getSBStatus()");
		XmlEntityStruct result = delegate.getSBStatus(id);
		profiler.end();
		return result;
	}

	/**
	 * @param id
	 * @return
	 * @throws InappropriateEntityTypeEx
	 * @throws NullEntityIdEx
	 * @throws NoSuchEntityEx
	 * @see alma.projectlifecycle.StateSystemOperations#getSBStatusListForOUSStatus(java.lang.String)
	 */
	public XmlEntityStruct[] getSBStatusListForOUSStatus(String id)
			throws InappropriateEntityTypeEx, NullEntityIdEx, NoSuchEntityEx {
		profiler.start("getSBStatusListForOUSStatus()");
		XmlEntityStruct[] result = delegate.getSBStatusListForOUSStatus(id);
		profiler.end();
		return result;
	}

	/**
	 * @param id
	 * @return
	 * @throws InappropriateEntityTypeEx
	 * @throws NullEntityIdEx
	 * @throws NoSuchEntityEx
	 * @see alma.projectlifecycle.StateSystemOperations#getSBStatusListForProjectStatus(java.lang.String)
	 */
	public XmlEntityStruct[] getSBStatusListForProjectStatus(String id)
			throws InappropriateEntityTypeEx, NullEntityIdEx, NoSuchEntityEx {
		profiler.start("getSBStatusListForProjectStatus()");
		XmlEntityStruct[] result = delegate.getSBStatusListForProjectStatus(id);
		profiler.end();
		return result;
	}

	/**
	 * @param id
	 * @return
	 * @throws InappropriateEntityTypeEx
	 * @throws NullEntityIdEx
	 * @throws NoSuchEntityEx
	 * @see alma.projectlifecycle.StateSystemOperations#getSBStatusXml(java.lang.String)
	 */
	public String getSBStatusXml(String id) throws InappropriateEntityTypeEx,
			NullEntityIdEx, NoSuchEntityEx {
		profiler.start("getSBStatusXml()");
		String result = delegate.getSBStatusXml(id);
		profiler.end();
		return result;
	}

	/**
	 * @param id
	 * @return
	 * @throws InappropriateEntityTypeEx
	 * @throws NullEntityIdEx
	 * @throws NoSuchEntityEx
	 * @see alma.projectlifecycle.StateSystemOperations#getSBStatusXmlListForOUSStatus(java.lang.String)
	 */
	public String[] getSBStatusXmlListForOUSStatus(String id)
			throws InappropriateEntityTypeEx, NullEntityIdEx, NoSuchEntityEx {
		profiler.start("getSBStatusXmlListForOUSStatus()");
		String[] result = delegate.getSBStatusXmlListForOUSStatus(id);
		profiler.end();
		return result;
	}

	/**
	 * @param id
	 * @return
	 * @throws InappropriateEntityTypeEx
	 * @throws NullEntityIdEx
	 * @throws NoSuchEntityEx
	 * @see alma.projectlifecycle.StateSystemOperations#getSBStatusXmlListForProjectStatus(java.lang.String)
	 */
	public String[] getSBStatusXmlListForProjectStatus(String id)
			throws InappropriateEntityTypeEx, NullEntityIdEx, NoSuchEntityEx {
		profiler.start("getSBStatusXmlListForProjectStatus()");
		String[] result = delegate.getSBStatusXmlListForProjectStatus(id);
		profiler.end();
		return result;
	}

	/**
	 * @param subsystem
	 * @return
	 * @see alma.projectlifecycle.StateSystemOperations#getSchedBlockStates(java.lang.String)
	 */
	public String getSchedBlockStates(String subsystem) {
		profiler.start("getSchedBlockStates()");
		String result = delegate.getSchedBlockStates(subsystem);
		profiler.end();
		return result;
	}

	/**
	 * @param opStatus
	 * @param ousStatus
	 * @param sbStatus
	 * @throws NoSuchEntityEx
	 * @throws StateIOFailedEx
	 * @see alma.projectlifecycle.StateSystemOperations#insert(alma.xmlentity.XmlEntityStruct, alma.xmlentity.XmlEntityStruct[], alma.xmlentity.XmlEntityStruct[])
	 */
	public void insert(XmlEntityStruct opStatus, XmlEntityStruct[] ousStatus,
			XmlEntityStruct[] sbStatus) throws NoSuchEntityEx, StateIOFailedEx {
		profiler.start("insert()");
		delegate.insert(opStatus, ousStatus, sbStatus);
		profiler.end();
	}

	/**
	 * @return
	 * @see alma.ACS.ACSComponentOperations#name()
	 */
	public String name() {
		profiler.start("name()");
		String result = delegate.name();
		profiler.end();
		return result;
	}

	/**
	 * @param entity
	 * @throws NoSuchEntityEx
	 * @throws StateIOFailedEx
	 * @see alma.projectlifecycle.StateSystemOperations#updateOUSStatus(alma.xmlentity.XmlEntityStruct)
	 */
	public void updateOUSStatus(XmlEntityStruct entity) throws NoSuchEntityEx,
			StateIOFailedEx {
		profiler.start("updateOUSStatus()");
		delegate.insertOrUpdateOUSStatus(entity, Subsystem.SCHEDULING);
		profiler.end();
	}

	/**
	 * @param entity
	 * @throws NoSuchEntityEx
	 * @throws StateIOFailedEx
	 * @see alma.projectlifecycle.StateSystemOperations#updateProjectStatus(alma.xmlentity.XmlEntityStruct)
	 */
	public void updateProjectStatus(XmlEntityStruct entity)
			throws NoSuchEntityEx, StateIOFailedEx {
		profiler.start("updateProjectStatus()");
		delegate.insertOrUpdateProjectStatus(entity, Subsystem.SCHEDULING);
		profiler.end();
	}

	/**
	 * @param entity
	 * @throws NoSuchEntityEx
	 * @throws StateIOFailedEx
	 * @see alma.projectlifecycle.StateSystemOperations#updateSBStatus(alma.xmlentity.XmlEntityStruct)
	 */
	public void updateSBStatus(XmlEntityStruct entity) throws NoSuchEntityEx,
			StateIOFailedEx {
		profiler.start("updateSBStatus()");
		delegate.insertOrUpdateSBStatus(entity, Subsystem.SCHEDULING);
		profiler.end();
	}
	
    @Override
    public XmlEntityStruct[] getOUSStatusList(String[] states) {
		profiler.start("getOUSStatusList()");
		XmlEntityStruct[] result = delegate.getOUSStatusList(states);
		profiler.end();
		return result;
    }
	/* Delegation
	 * ------------------------------------------------------------- */



	@Override
	public void insertOrUpdateProjectStatus(XmlEntityStruct entity,
			String userId) throws NoSuchEntityEx, StateIOFailedEx {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void insertOrUpdateOUSStatus(XmlEntityStruct entity, String userId)
			throws NoSuchEntityEx, StateIOFailedEx {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void insertOrUpdateSBStatus(XmlEntityStruct entity, String userId)
			throws NoSuchEntityEx, StateIOFailedEx {
		// TODO Auto-generated method stub
		
	}
}
