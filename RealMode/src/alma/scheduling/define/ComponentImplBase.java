/*
 *    ALMA - Atacama Large Millimiter Array
 *    (c) European Southern Observatory, 2002
 *    Copyright by ESO (in the framework of the ALMA collaboration),
 *    All rights reserved
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *    MA 02111-1307  USA
 */
package ALMA.scheduling.define;

import java.util.logging.Logger;

import alma.acs.component.ComponentLifecycle;
import alma.acs.component.ComponentLifecycleException;
import alma.acs.container.ContainerServices;

/**
 * @author hsommer May 6, 2003 11:50:18 AM
 * afarris added state model concepts, June 24, 2003.
 */
public class ComponentImplBase implements ComponentLifecycle {
	protected String m_instanceName;
	protected ContainerServices m_containerServices;
	protected Logger m_logger;
	protected ComponentState m_state;

	protected ComponentImplBase() {
		m_instanceName = null;
		m_containerServices = null;
		m_logger = null;
		m_state = new ComponentState(ComponentState.NEW);
	}
	/* 
	 * Set this component's name -- done by the container.
	 * 
	 * @see alma.acs.component.ComponentLifecycle#setComponentName(java.lang.String)
	 */
	public void setComponentName(String instanceName)  {
		if (instanceName == null || instanceName.length() == 0)
			throw new IllegalArgumentException (
				"Component name cannot be a null string.");
		if (m_instanceName != null)
			throw new IllegalArgumentException (
				"Cannot change the name of a component that has already been named.");
		m_instanceName = instanceName;
	}

	/* 
	 * Set the object that provides container services -- done by the container.
	 * 
	 * @see alma.acs.component.ComponentLifecycle#setContainerServices(alma.acs.container.ContainerServices)
	 */
	public void setContainerServices(ContainerServices containerServices) {
		if (containerServices == null)
			throw new IllegalArgumentException (
				"ContainerServices object cannot be a null.");
		m_containerServices = containerServices;
		m_logger = m_containerServices.getLogger();
	}


	/* 
	 * The initialize method completely initializes the component.
	 * It cannot be called if the component is in the "executing"
	 * state.  In addition the component name and container services must
	 * be provided before initialization can occur.  If any error occurs 
	 * during initialization, the component should be placed in the "error"
	 * state and an exception thrown.  
	 * 
	 * @see alma.acs.component.ComponentLifecycle#initialize()
	 */
	public void initialize() throws ComponentLifecycleException {
		if (m_instanceName == null)
			throw new ComponentLifecycleException (
			"The component must be named before this object can be initialized.");
		if (m_containerServices == null)
			throw new ComponentLifecycleException (
			"The ContainerServices must be set before this object can be initialized.");
		if (m_state.equals(ComponentState.EXECUTING))
			throw new ComponentLifecycleException (
			"Cannot initialize this object.  It is already executing!  It must be stopped first.");
	}

	/* 
	 * The execute method begins the process of executing the component.
	 * If any error occurs during execution, the component should be
	 * placed in the "error" state and an exception thrown. 
	 *  
	 * @see alma.acs.component.ComponentLifecycle#execute()
	 */
	public void execute() throws ComponentLifecycleException {
		if (!m_state.equals(ComponentState.INITIALIZED))
			throw new ComponentLifecycleException (
			"Cannot execute this component.  It is not initialized!");
	}

	/* 
	 * The cleanUp method is used to stop the component gracefully.
	 * 
	 * @see alma.acs.component.ComponentLifecycle#cleanUp()
	 */
	public void cleanUp() {
		if (m_state.equals(ComponentState.STOPPED)) {
			return;
		}
		if (m_state.equals(ComponentState.NEW)) {
			m_state.setState(ComponentState.STOPPED);
			return;
		}
		if (m_state.equals(ComponentState.INITIALIZED)) {
			m_state.setState(ComponentState.STOPPED);
			return;
		}
		// This leaves the ERROR and EXECUTING states, which the 
		// extended component must provide.
	}

	/* 
	 * The aboutToAbort method is called to allow the ocmponent to
	 * do whatever it can before it is aborted.  The default action
	 * is to merely call the cleanUp method.
	 * 
	 * @see alma.acs.component.ComponentLifecycle#aboutToAbort()
	 */
	public void aboutToAbort() {
		cleanUp();
	}

	/**
	 * Get the state of the component. 
	 * 
	 * @see alma.acs.component.ComponentLifecycle#getState()
	 * @return The component state.
	 */
	public ComponentState getState() {
		return m_state;
	}

}
