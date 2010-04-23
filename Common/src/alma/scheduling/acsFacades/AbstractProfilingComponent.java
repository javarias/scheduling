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

import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.container.ContainerServices;
import alma.scheduling.utils.Profiler;


/**
 * Superclass with common functions for profiling versions of components.
 *
 * @version $Id: AbstractProfilingComponent.java,v 1.1 2010/04/23 23:35:23 dclarke Exp $
 * @author David Clarke
 */
public abstract class AbstractProfilingComponent {

	/** The profiling object. */
	protected Profiler profiler;
	
	/**
	 * Construct this object
	 * 
	 * @throws AcsJContainerServicesEx 
	 */
	public AbstractProfilingComponent(ContainerServices containerServices) {
		this.profiler = new Profiler(containerServices.getLogger());
	}
	/* Formatting utils
	 * ------------------------------------------------------------- */
}
