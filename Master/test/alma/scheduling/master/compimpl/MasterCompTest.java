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
package alma.scheduling.master.compimpl;

import org.omg.CORBA.Object;

import alma.ACS.MasterComponent;
import alma.ACS.MasterComponentHelper;
import alma.ACS.MasterComponentPackage.SubsystemStateEvent;
import alma.ACSErrTypeCommon.IllegalStateEventEx;
import alma.acs.component.client.ComponentClientTestCase;
import alma.acs.genfw.runtime.sm.AcsStateActionException;
import alma.scheduling.utils.Constants;

public class MasterCompTest extends ComponentClientTestCase {

	MasterComponent comp;
	
	public MasterCompTest() throws Exception {
		super("Master Component Test");
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Object obj = getContainerServices().getComponent(Constants.MASTER_SCHEDULING_COMP_URL);
		comp = MasterComponentHelper.narrow(obj);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		getContainerServices().releaseComponent(Constants.MASTER_SCHEDULING_COMP_URL);
		comp=null;
	}
	
	public void testInitialization() throws AcsStateActionException, IllegalStateEventEx, InterruptedException {
		comp.doTransition(SubsystemStateEvent.SUBSYSEVENT_INITPASS1);
		Thread.sleep(10000);
		comp.doTransition(SubsystemStateEvent.SUBSYSEVENT_INITPASS2);
		Thread.sleep(10000);
		comp.doTransition(SubsystemStateEvent.SUBSYSEVENT_START);
		Thread.sleep(10000);
	}

}