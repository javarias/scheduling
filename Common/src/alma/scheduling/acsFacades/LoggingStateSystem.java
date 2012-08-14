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
 * @version $Id: LoggingStateSystem.java,v 1.2 2012/08/14 16:22:53 javarias Exp $
 * @author David Clarke
 */
public class LoggingStateSystem
	extends AbstractLoggingComponent
	implements StateSystemOperations {

	/** The object for which we are a facade. */
	private StateSystemOperations delegate;
	
	/**
	 * Construct this object
	 * 
	 * @throws AcsJContainerServicesEx 
	 */
	public LoggingStateSystem(ContainerServices     containerServices,
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
		logger.fine(String.format(
				"calling StateSystem.changeOUSStatus(%s, %s, %s, %s)",
				target, destinationState, subsys, userID));
		return delegate.changeOUSStatus(target, destinationState, subsys,
				userID);
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
		logger.fine(String.format(
				"calling StateSystem.changeProjectStatus(%s, %s, %s, %s)",
				target, destinationState, subsys, userID));
		return delegate.changeProjectStatus(target, destinationState, subsys,
				userID);
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
		logger.fine(String.format(
				"calling StateSystem.changeSBStatus(%s, %s, %s, %s)",
				target, destinationState, subsys, userID));
		return delegate
				.changeSBStatus(target, destinationState, subsys, userID);
	}

	/**
	 * @return
	 * @see alma.ACS.ACSComponentOperations#componentState()
	 */
	public ComponentStates componentState() {
		return delegate.componentState();
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
		logger.fine(String.format(
				"calling StateSystem.findProjectStatusByState(%s)",
				format(states)));
		return delegate.findProjectStatusByState(states);
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
		logger.fine(String.format(
				"calling StateSystem.findSBStatusByState(%s)",
				format(states)));
		return delegate.findSBStatusByState(states);
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
		logger.fine(String.format(
				"calling StateSystem.findStateChangeRecords(%d, %d, %s, %s)",
				start.value, end.value, domainEntityId, state));
		return delegate.findStateChangeRecords(start, end, domainEntityId,
				state, userId, type);
	}

	/**
	 * @param subsystem
	 * @return
	 * @see alma.projectlifecycle.StateSystemOperations#getObsProjectStates(java.lang.String)
	 */
	public String getObsProjectStates(String subsystem) {
		logger.fine(String.format(
				"calling StateSystem.getObsProjectStates(%s)",
				subsystem));
		return delegate.getObsProjectStates(subsystem);
	}

	/**
	 * @param subsystem
	 * @return
	 * @see alma.projectlifecycle.StateSystemOperations#getObsUnitSetStates(java.lang.String)
	 */
	public String getObsUnitSetStates(String subsystem) {
		logger.fine(String.format(
				"calling StateSystem.getObsProjectStates(%s)",
				subsystem));
		return delegate.getObsUnitSetStates(subsystem);
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
		logger.fine(String.format(
				"calling StateSystem.getOUSStatus(%s)",
				id));
		return delegate.getOUSStatus(id);
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
		logger.fine(String.format(
				"calling StateSystem.getOUSStatusXml(%s)",
				id));
		return delegate.getOUSStatusXml(id);
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
		logger.fine(String.format(
				"calling StateSystem.getProjectStatus(%s)",
				id));
		return delegate.getProjectStatus(id);
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
		logger.fine(String.format(
				"calling StateSystem.subsystem(%s)",
				id));
		return delegate.getProjectStatusList(id);
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
		logger.fine(String.format(
				"calling StateSystem.getProjectStatusXml(%s)",
				id));
		return delegate.getProjectStatusXml(id);
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
		logger.fine(String.format(
				"calling StateSystem.getProjectStatusXmlList(%s)",
				id));
		return delegate.getProjectStatusXmlList(id);
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
		logger.fine(String.format(
				"calling StateSystem.getSBStatus(%s)",
				id));
		return delegate.getSBStatus(id);
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
		logger.fine(String.format(
				"calling StateSystem.getSBStatusListForOUSStatus(%s)",
				id));
		return delegate.getSBStatusListForOUSStatus(id);
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
		logger.fine(String.format(
				"calling StateSystem.getSBStatusListForProjectStatus(%s)",
				id));
		return delegate.getSBStatusListForProjectStatus(id);
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
		logger.fine(String.format(
				"calling StateSystem.getSBStatusXml(%s)",
				id));
		return delegate.getSBStatusXml(id);
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
		logger.fine(String.format(
				"calling StateSystem.getSBStatusXmlListForOUSStatus(%s)",
				id));
		return delegate.getSBStatusXmlListForOUSStatus(id);
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
		logger.fine(String.format(
				"calling StateSystem.getSBStatusXmlListForProjectStatus(%s)",
				id));
		return delegate.getSBStatusXmlListForProjectStatus(id);
	}

	/**
	 * @param subsystem
	 * @return
	 * @see alma.projectlifecycle.StateSystemOperations#getSchedBlockStates(java.lang.String)
	 */
	public String getSchedBlockStates(String subsystem) {
		logger.fine(String.format(
				"calling StateSystem.getSchedBlockStates(%s)",
				subsystem));
		return delegate.getSchedBlockStates(subsystem);
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
		logger.fine(String.format(
				"calling StateSystem.insert(%s, ousStatus[])",
				opStatus));
		delegate.insert(opStatus, ousStatus, sbStatus);
	}

	/**
	 * @return
	 * @see alma.ACS.ACSComponentOperations#name()
	 */
	public String name() {
		logger.fine(String.format(
			"calling StateSystem.name()"));
		return delegate.name();
	}

	/**
	 * @param entity
	 * @throws NoSuchEntityEx
	 * @throws StateIOFailedEx
	 * @see alma.projectlifecycle.StateSystemOperations#updateOUSStatus(alma.xmlentity.XmlEntityStruct)
	 */
	public void updateOUSStatus(XmlEntityStruct entity) throws NoSuchEntityEx,
			StateIOFailedEx {
		logger.fine(String.format(
				"calling StateSystem.updateOUSStatus(%s %s)",
				entity.entityTypeName, entity.entityId));
		delegate.insertOrUpdateOUSStatus(entity, Subsystem.SCHEDULING);
	}

	/**
	 * @param entity
	 * @throws NoSuchEntityEx
	 * @throws StateIOFailedEx
	 * @see alma.projectlifecycle.StateSystemOperations#updateProjectStatus(alma.xmlentity.XmlEntityStruct)
	 */
	public void updateProjectStatus(XmlEntityStruct entity)
			throws NoSuchEntityEx, StateIOFailedEx {
		logger.fine(String.format(
				"calling StateSystem.updateProjectStatus(%s %s)",
				entity.entityTypeName, entity.entityId));
		delegate.insertOrUpdateProjectStatus(entity, Subsystem.SCHEDULING);
	}

	/**
	 * @param entity
	 * @throws NoSuchEntityEx
	 * @throws StateIOFailedEx
	 * @see alma.projectlifecycle.StateSystemOperations#updateSBStatus(alma.xmlentity.XmlEntityStruct)
	 */
	public void updateSBStatus(XmlEntityStruct entity) throws NoSuchEntityEx,
			StateIOFailedEx {
		logger.fine(String.format(
				"calling StateSystem.updateSBStatus(%s %s)",
				entity.entityTypeName, entity.entityId));
		delegate.insertOrUpdateSBStatus(entity, Subsystem.SCHEDULING);
	}
	
    @Override
    public XmlEntityStruct[] getOUSStatusList(String[] states) {
        logger.fine("calling StateSystem.getOUSStatusList(String[])");
        return delegate.getOUSStatusList(states);
    }
	/* Delegation
	 * ------------------------------------------------------------- */



	@Override
	public void insertOrUpdateProjectStatus(XmlEntityStruct entity,
			String userId) throws NoSuchEntityEx, StateIOFailedEx {
		updateProjectStatus(entity);
		
	}



	@Override
	public void insertOrUpdateOUSStatus(XmlEntityStruct entity, String userId)
			throws NoSuchEntityEx, StateIOFailedEx {
		updateOUSStatus(entity);
		
	}



	@Override
	public void insertOrUpdateSBStatus(XmlEntityStruct entity, String userId)
			throws NoSuchEntityEx, StateIOFailedEx {
		updateSBStatus(entity);
		
	}
}
