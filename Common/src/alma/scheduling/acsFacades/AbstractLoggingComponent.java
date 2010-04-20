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

import java.util.logging.Logger;

import alma.ACS.ComponentStates;
import alma.ACSErrTypeCommon.IllegalArgumentEx;
import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.container.ContainerServices;
import alma.asdmIDLTypes.IDLArrayTime;
import alma.projectlifecycle.StateChangeData;
import alma.projectlifecycle.StateSystemHelper;
import alma.projectlifecycle.StateSystemOperations;
import alma.scheduling.AlmaScheduling.facades.ComponentFactory;
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
 * Superclass with common functions for Logging versions of components.
 *
 * @version $Id: AbstractLoggingComponent.java,v 1.1 2010/04/20 23:04:14 dclarke Exp $
 * @author David Clarke
 */
public abstract class AbstractLoggingComponent {

	/** The logger on which to, well, log things. */
	protected Logger logger;
	
	/**
	 * Construct this object
	 * 
	 * @throws AcsJContainerServicesEx 
	 */
	public AbstractLoggingComponent(ContainerServices containerServices) {
		this.logger = containerServices.getLogger();
	}


	
	/*
	 * ================================================================
	 * Formatting utils
	 * ================================================================
	 */
	protected String format(String[] array) {
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
	
	protected String format(XmlEntityStruct entityStruct) {
		return String.format("%s(%s)",
				entityStruct.entityTypeName,
				entityStruct.entityId);
	}
	
	protected String first(int howMany, String full) {
		String result;
		if (full.length() > howMany) {
			result = full.substring(0, howMany-1);
		} else {
			result = full;
		}
		return result;
	}
	/* Formatting utils
	 * ------------------------------------------------------------- */
}
