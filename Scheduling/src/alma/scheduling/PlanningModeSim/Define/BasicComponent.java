/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 * File BasicComponent.java
 */
 
package alma.scheduling.PlanningModeSim.Define;

import java.util.logging.Logger;

import alma.acs.logging.AcsLogger;

/**
 * The BasicComponent class implements the component lifecycle methods
 * in a very basic fashion.  Classes that extended from this class should
 * override the initialize and execute methods in particular.
 * 
 * @version 1.10  Dec. 12, 2003
 * @author Allen Farris
 */
public abstract class BasicComponent implements ComponentLifecycle {
	protected String instanceName;
	protected ContainerServices containerServices;
	protected AcsLogger logger;
	
	protected BasicComponent() {
		instanceName = null;
		containerServices = null;
		logger = null;
	}

	/* 
	 * Set this component's name -- done by the container.
	 */
	public void setComponentName(String instanceName)  {
		if (instanceName == null || instanceName.length() == 0)
			throw new IllegalArgumentException (
				"Component name cannot be a null string.");
		if (this.instanceName != null)
			throw new UnsupportedOperationException (
				"Cannot change the name of a component that has already been named.");
		this.instanceName = instanceName;
	}

	/* 
	 * Set the object that provides container services -- done by the container.
	 */
	public void setContainerServices(ContainerServices containerServices) {
		if (containerServices == null)
			throw new IllegalArgumentException (
				"ContainerServices object cannot be a null.");
		if (this.containerServices != null)
			throw new UnsupportedOperationException (
				"Cannot change container services that have already been set.");
		this.containerServices = containerServices;
		this.logger = this.containerServices.getLogger();
	}

	public void initialize() throws SimulationException {
		logger.info(instanceName + ".initialized");
	}

	public void execute() throws SimulationException {
		logger.info(instanceName + ".execute complete");
	}

	public void cleanUp() {
		logger.info(instanceName + ".stopped");
	}

	public void aboutToAbort() {
		cleanUp();
	}

	/**
	 * @return
	 */
	public ContainerServices getContainerServices() {
		return containerServices;
	}

	/**
	 * @return
	 */
	public String getInstanceName() {
		return instanceName;
	}

	/**
	 * @return
	 */
	public Logger getLogger() {
		return logger;
	}

}
