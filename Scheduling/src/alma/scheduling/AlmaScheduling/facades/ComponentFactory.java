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

import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.projectlifecycle.StateSystem;
import alma.xmlstore.ArchiveConnection;
import alma.xmlstore.Identifier;

/**
 * A factory for the various components that are used in the Scheduler.
 * Done as an interface so that we can insert facades for them and do
 * useful things like tracing.
 *
 * @version $Id: ComponentFactory.java,v 1.2 2010/03/13 00:34:21 dclarke Exp $
 * @author David Clarke
 */
public interface ComponentFactory {
	public static String StateSystemIFName = "IDL:alma/projectlifecycle/StateSystem:1.0";
	
	public StateSystem getDefaultStateSystem() throws AcsJContainerServicesEx;
//	public ArchiveConnection getDefaultArchiveConnection();
//	public Identifier getDefaultIdentifier();
}
