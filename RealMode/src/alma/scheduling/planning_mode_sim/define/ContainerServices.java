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
package alma.scheduling.planning_mode_sim.define;

import java.util.logging.Logger;

/**
 * The ContainerServices interface defines the basic services provided
 * by a Container that can be used by a component.
 * 
 * @version 1.10  Dec. 12, 2003
 * @author Allen Farris
 */
public interface ContainerServices 
{
	/**
	 * Gets a Logger object that the component should use for logging.
	 * @return Logger
	 */
	public Logger getLogger();
		
	/**
	 * Gets the specified component as a Java Object.
	 * @ return Object
	 */
	public Object getComponent(String name);

	/** 
	 * Get a unique id that can tag an object in the archive.
	 * @return String
	 */
	public String getEntityId();
}
