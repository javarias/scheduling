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
package alma.scheduling.acsFacades;

import org.omg.CORBA.UserException;

import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.projectlifecycle.StateSystemOperations;
import alma.xmlstore.IdentifierOperations;
import alma.xmlstore.OperationalOperations;

/**
 * A factory for the various components that are used in the Scheduler.
 * Done as an interface so that we can insert facades for them and do
 * useful things like tracing.
 *
 * @version $Id: ComponentFactory.java,v 1.3 2012/05/29 17:46:36 dclarke Exp $
 * @author David Clarke
 */
public interface ComponentFactory {
	
    public enum ComponentDiagnosticTypes {
    	LOGGING, PROFILING, BARFING
    }

	// State System
	// ============
	final public static String StateSystemIFName =
		"IDL:alma/projectlifecycle/StateSystem:1.0";

	public StateSystemOperations getDefaultStateSystem(
			ComponentDiagnosticTypes... diags)
		throws AcsJContainerServicesEx;
	
	public StateSystemOperations getStateSystem(
			String name,
			ComponentDiagnosticTypes... diags)
		throws AcsJContainerServicesEx;
	
	
	// Archive XMLStore
	// ================
	final public static String ArchiveIFName =
		"IDL:alma/xmlstore/ArchiveConnection:1.0";
	public OperationalOperations getDefaultArchive(
			ComponentDiagnosticTypes... diags)
		throws AcsJContainerServicesEx, UserException;
	
	public OperationalOperations getArchive(
			String name,
			ComponentDiagnosticTypes... diags)
		throws AcsJContainerServicesEx, UserException;
	
	// Archive UIDs
	// ============
	final public static String IdentifierIFName =
		"IDL:alma/xmlstore/Identifier:1.0";
	
	public IdentifierOperations  getDefaultIdentifier(
			ComponentDiagnosticTypes... diags)
		throws AcsJContainerServicesEx;
	
	public IdentifierOperations  getIdentifier(
			String name,
			ComponentDiagnosticTypes... diags)
		throws AcsJContainerServicesEx;

	// Tidying up
	// ==========
	public void tidyUp();

}
