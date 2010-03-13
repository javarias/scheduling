/*
 * ALMA - Atacama Large Millimiter Array
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
 * File ComponentFactory.java
 * 
 */
package alma.scheduling.AlmaScheduling.facades;

import java.util.logging.Logger;

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

import alma.projectlifecycle.StateChangeData;
import alma.projectlifecycle.StateSystem;
import alma.projectlifecycle.StateSystemHelper;
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
import alma.ACS.ComponentStates;
import alma.ACSErrTypeCommon.IllegalArgumentEx;
import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.container.ContainerServices;
import alma.asdmIDLTypes.IDLArrayTime;


/**
 * A facade for the StateSystem which logs calls made to it.
 *
 * @version $Id: LoggingStateSystem.java,v 1.2 2010/03/13 00:34:21 dclarke Exp $
 * @author David Clarke
 */
public class LoggingStateSystem implements StateSystem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1784171564754132358L;

	/** The object for which we are a facade. */
	private StateSystem delegate;
	
	/** The logger on which to, well, log things. */
	private Logger      logger;
	
	/** Are we currently logging? */
	private boolean     logging;
	
	/**
	 * Construct this object
	 * 
	 * @throws AcsJContainerServicesEx 
	 */
	public LoggingStateSystem(ContainerServices containerServices) throws AcsJContainerServicesEx {
		this.logger = containerServices.getLogger();
        org.omg.CORBA.Object obj = containerServices.getDefaultComponent(ComponentFactory.StateSystemIFName);
        this.delegate = StateSystemHelper.narrow(obj);
        this.logging = true;
	}
	
	/**
	 * Construct this object
	 * 
	 * @throws AcsJContainerServicesEx 
	 */
	public LoggingStateSystem(ContainerServices containerServices,
			                  StateSystem       delegate)
		throws AcsJContainerServicesEx {
		this.logger = containerServices.getLogger();
        this.delegate = delegate;
        this.logging = true;
	}


	
	/*
	 * ================================================================
	 * Logging control
	 * ================================================================
	 */
	public void setLogging(boolean on) {
		logging = on;
	}

	public boolean isLogging() {
		return logging;
	}
	/* Logging control
	 * ------------------------------------------------------------- */
	
	/*
	 * ================================================================
	 * Delegation
	 * ================================================================
	 */
	/**
	 * @param ctx
	 * @param operation
	 * @param argList
	 * @param result
	 * @param exclist
	 * @param ctxlist
	 * @return
	 * @see org.omg.CORBA.Object#_create_request(org.omg.CORBA.Context, java.lang.String, org.omg.CORBA.NVList, org.omg.CORBA.NamedValue, org.omg.CORBA.ExceptionList, org.omg.CORBA.ContextList)
	 */
	public Request _create_request(Context ctx, String operation,
			NVList argList, NamedValue result, ExceptionList exclist,
			ContextList ctxlist) {
		return delegate._create_request(ctx, operation, argList, result,
				exclist, ctxlist);
	}

	/**
	 * @param ctx
	 * @param operation
	 * @param argList
	 * @param result
	 * @return
	 * @see org.omg.CORBA.Object#_create_request(org.omg.CORBA.Context, java.lang.String, org.omg.CORBA.NVList, org.omg.CORBA.NamedValue)
	 */
	public Request _create_request(Context ctx, String operation,
			NVList argList, NamedValue result) {
		return delegate._create_request(ctx, operation, argList, result);
	}

	/**
	 * @return
	 * @see org.omg.CORBA.Object#_duplicate()
	 */
	public Object _duplicate() {
		return delegate._duplicate();
	}

	/**
	 * @return
	 * @see org.omg.CORBA.Object#_get_domain_managers()
	 */
	public DomainManager[] _get_domain_managers() {
		return delegate._get_domain_managers();
	}

	/**
	 * @return
	 * @see org.omg.CORBA.Object#_get_interface_def()
	 */
	public Object _get_interface_def() {
		return delegate._get_interface_def();
	}

	/**
	 * @param policyType
	 * @return
	 * @see org.omg.CORBA.Object#_get_policy(int)
	 */
	public Policy _get_policy(int policyType) {
		return delegate._get_policy(policyType);
	}

	/**
	 * @param maximum
	 * @return
	 * @see org.omg.CORBA.Object#_hash(int)
	 */
	public int _hash(int maximum) {
		return delegate._hash(maximum);
	}

	/**
	 * @param repositoryIdentifier
	 * @return
	 * @see org.omg.CORBA.Object#_is_a(java.lang.String)
	 */
	public boolean _is_a(String repositoryIdentifier) {
		return delegate._is_a(repositoryIdentifier);
	}

	/**
	 * @param other
	 * @return
	 * @see org.omg.CORBA.Object#_is_equivalent(org.omg.CORBA.Object)
	 */
	public boolean _is_equivalent(Object other) {
		return delegate._is_equivalent(other);
	}

	/**
	 * @return
	 * @see org.omg.CORBA.Object#_non_existent()
	 */
	public boolean _non_existent() {
		return delegate._non_existent();
	}

	/**
	 * 
	 * @see org.omg.CORBA.Object#_release()
	 */
	public void _release() {
		delegate._release();
	}

	/**
	 * @param operation
	 * @return
	 * @see org.omg.CORBA.Object#_request(java.lang.String)
	 */
	public Request _request(String operation) {
		return delegate._request(operation);
	}

	/**
	 * @param policies
	 * @param setAdd
	 * @return
	 * @see org.omg.CORBA.Object#_set_policy_override(org.omg.CORBA.Policy[], org.omg.CORBA.SetOverrideType)
	 */
	public Object _set_policy_override(Policy[] policies, SetOverrideType setAdd) {
		return delegate._set_policy_override(policies, setAdd);
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
	 * @see alma.projectlifecycle.StateSystemOperations#changeOUSStatus(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean changeOUSStatus(String target, String destinationState,
			String subsys, String userID) throws PreconditionFailedEx,
			NoSuchEntityEx, IllegalArgumentEx, NoSuchTransitionEx,
			PostconditionFailedEx, NotAuthorizedEx {
		if (isLogging()) {
			logger.fine(String.format(
				"calling StateSystem.changeOUSStatus(%s, %s, %s, %s)",
				target, destinationState, subsys, userID));
		}
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
		if (isLogging()) {
			logger.fine(String.format(
					"calling StateSystem.changeProjectStatus(%s, %s, %s, %s)",
					target, destinationState, subsys, userID));
		}
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
		if (isLogging()) {
			logger.fine(String.format(
					"calling StateSystem.changeSBStatus(%s, %s, %s, %s)",
					target, destinationState, subsys, userID));
		}
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
		if (isLogging()) {
			logger.fine(String.format(
					"calling StateSystem.findProjectStatusByState(%s)",
					format(states)));
		}
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
		if (isLogging()) {
			logger.fine(String.format(
					"calling StateSystem.findSBStatusByState(%s)",
					format(states)));
		}
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
		if (isLogging()) {
			logger.fine(String.format(
					"calling StateSystem.findStateChangeRecords(%d, %d, %s, %s)",
					start.value, end.value, domainEntityId, state));
		}
		return delegate.findStateChangeRecords(start, end, domainEntityId,
				state, userId, type);
	}

	/**
	 * @param subsystem
	 * @return
	 * @see alma.projectlifecycle.StateSystemOperations#getObsProjectStates(java.lang.String)
	 */
	public String getObsProjectStates(String subsystem) {
		if (isLogging()) {
			logger.fine(String.format(
					"calling StateSystem.getObsProjectStates(%s)",
					subsystem));
		}
		return delegate.getObsProjectStates(subsystem);
	}

	/**
	 * @param subsystem
	 * @return
	 * @see alma.projectlifecycle.StateSystemOperations#getObsUnitSetStates(java.lang.String)
	 */
	public String getObsUnitSetStates(String subsystem) {
		if (isLogging()) {
			logger.fine(String.format(
					"calling StateSystem.getObsProjectStates(%s)",
					subsystem));
		}
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
		if (isLogging()) {
			logger.fine(String.format(
					"calling StateSystem.getOUSStatus(%s)",
					id));
		}
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
		if (isLogging()) {
			logger.fine(String.format(
					"calling StateSystem.getOUSStatusXml(%s)",
					id));
		}
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
		if (isLogging()) {
			logger.fine(String.format(
					"calling StateSystem.getProjectStatus(%s)",
					id));
		}
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
		if (isLogging()) {
			logger.fine(String.format(
					"calling StateSystem.subsystem(%s)",
					id));
		}
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
		if (isLogging()) {
			logger.fine(String.format(
					"calling StateSystem.getProjectStatusXml(%s)",
					id));
		}
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
		if (isLogging()) {
			logger.fine(String.format(
					"calling StateSystem.getProjectStatusXmlList(%s)",
					id));
		}
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
		if (isLogging()) {
			logger.fine(String.format(
					"calling StateSystem.getSBStatus(%s)",
					id));
		}
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
		if (isLogging()) {
			logger.fine(String.format(
					"calling StateSystem.getSBStatusListForOUSStatus(%s)",
					id));
		}
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
		if (isLogging()) {
			logger.fine(String.format(
					"calling StateSystem.getSBStatusListForProjectStatus(%s)",
					id));
		}
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
		if (isLogging()) {
			logger.fine(String.format(
					"calling StateSystem.getSBStatusXml(%s)",
					id));
		}
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
		if (isLogging()) {
			logger.fine(String.format(
					"calling StateSystem.getSBStatusXmlListForOUSStatus(%s)",
					id));
		}
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
		if (isLogging()) {
			logger.fine(String.format(
					"calling StateSystem.getSBStatusXmlListForProjectStatus(%s)",
					id));
		}
		return delegate.getSBStatusXmlListForProjectStatus(id);
	}

	/**
	 * @param subsystem
	 * @return
	 * @see alma.projectlifecycle.StateSystemOperations#getSchedBlockStates(java.lang.String)
	 */
	public String getSchedBlockStates(String subsystem) {
		if (isLogging()) {
			logger.fine(String.format(
					"calling StateSystem.getSchedBlockStates(%s)",
					subsystem));
		}
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
		if (isLogging()) {
			logger.fine(String.format(
					"calling StateSystem.insert(%s, ousStatus[])",
					opStatus));
		}
		delegate.insert(opStatus, ousStatus, sbStatus);
	}

	/**
	 * @return
	 * @see alma.ACS.ACSComponentOperations#name()
	 */
	public String name() {
		if (isLogging()) {
			logger.fine(String.format(
			"calling StateSystem.name()"));
		}
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
		if (isLogging()) {
			logger.fine(String.format(
					"calling StateSystem.updateOUSStatus(%s %s)",
					entity.entityTypeName, entity.entityId));
		}
		delegate.updateOUSStatus(entity);
	}

	/**
	 * @param entity
	 * @throws NoSuchEntityEx
	 * @throws StateIOFailedEx
	 * @see alma.projectlifecycle.StateSystemOperations#updateProjectStatus(alma.xmlentity.XmlEntityStruct)
	 */
	public void updateProjectStatus(XmlEntityStruct entity)
			throws NoSuchEntityEx, StateIOFailedEx {
		if (isLogging()) {
			logger.fine(String.format(
					"calling StateSystem.updateProjectStatus(%s %s)",
					entity.entityTypeName, entity.entityId));
		}
		delegate.updateProjectStatus(entity);
	}

	/**
	 * @param entity
	 * @throws NoSuchEntityEx
	 * @throws StateIOFailedEx
	 * @see alma.projectlifecycle.StateSystemOperations#updateSBStatus(alma.xmlentity.XmlEntityStruct)
	 */
	public void updateSBStatus(XmlEntityStruct entity) throws NoSuchEntityEx,
			StateIOFailedEx {
		if (isLogging()) {
			logger.fine(String.format(
					"calling StateSystem.updateSBStatus(%s %s)",
					entity.entityTypeName, entity.entityId));
		}
		delegate.updateSBStatus(entity);
	}
	/* Delegation
	 * ------------------------------------------------------------- */


	
	/*
	 * ================================================================
	 * Formatting utils
	 * ================================================================
	 */
	private String format(String[] array) {
		final StringBuffer b = new StringBuffer();
		String sep = "[";
		for (final String s : array) {
			b.append(sep);
			b.append(s);
			sep = ", ";
		}
		b.append(']');
		return b.toString();
	}
	/* Formatting utils
	 * ------------------------------------------------------------- */

}
