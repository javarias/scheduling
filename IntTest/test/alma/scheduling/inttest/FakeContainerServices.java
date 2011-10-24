/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
package alma.scheduling.inttest;

import java.util.logging.Logger;
import java.util.concurrent.ThreadFactory;

import org.omg.CORBA.Object;
import org.omg.PortableServer.Servant;

import si.ijs.maci.ComponentSpec;
import alma.ACS.OffShoot;
import alma.ACS.OffShootOperations;
import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.component.ComponentDescriptor;
import alma.acs.component.ComponentQueryDescriptor;
import alma.acs.component.ComponentStateManager;
import alma.acs.container.AdvancedContainerServices;
import alma.acs.container.ContainerServices;
import alma.acs.logging.AcsLogger;
import alma.entities.commonentity.EntityT;

import com.cosylab.CDB.DAL;

public class FakeContainerServices extends alma.acs.container.testsupport.DummyContainerServices {

	private static int id = 1;

	FakeContainerServices(String name, Logger logger) {
	    super(name, logger);
	}

	public void assignUniqueEntityId(EntityT entity)
			throws AcsJContainerServicesEx {
		entity.setEntityId(String.format("%s %2H ", entity.getEntityTypeName(),
				id++));
	}

}
