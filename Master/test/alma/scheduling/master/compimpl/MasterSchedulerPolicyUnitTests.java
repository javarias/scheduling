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

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.jmock.lib.legacy.ClassImposteriser;

import alma.Control.ControlMaster;
import alma.SchedulingMasterExceptions.SchedulingInternalExceptionEx;
import alma.acs.container.ContainerServices;
import alma.acs.logging.AcsLogger;
import alma.scheduling.Array;
import alma.scheduling.ArrayStatusCallback;

public class MasterSchedulerPolicyUnitTests extends MockObjectTestCase {
	private ControlMaster controlMaster = mock(ControlMaster.class);
	private ContainerServices contServices = mock(ContainerServices.class);
	final Array retArray = mock(Array.class);
	MasterImpl schedMaster;
	AcsLogger logger;
	
	protected void setUp() throws Exception {
		super.setUp();
		setImposteriser(ClassImposteriser.INSTANCE);
		schedMaster = new MasterImpl();
		logger = mock(AcsLogger.class);
		final ArrayStatusCallback callback = mock(ArrayStatusCallback.class);
		checking(new Expectations() { {
			ignoring(contServices);
			ignoring(logger);
		} });
		schedMaster.initialize(contServices);
		schedMaster.setLogger(logger);
		schedMaster.setControlMaster(controlMaster);
	}

	public void testAddNewSchedulingPolicies() throws SchedulingInternalExceptionEx {
		final String xmlToLoad = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<Policies><SchedulingPolicy name=\"TestPolicy\">\n<SelectionCriteria></SelectionCriteria>" +
				"<Scorers></Scorers></SchedulingPolicy></Policies>";
		checking(new Expectations() { {
		} });
		schedMaster.addSchedulingPolicies("localhost", "lala.xml", xmlToLoad);
	}
	
	public void testAddNewSchedulingPoliciesFailure() {
		final String xmlToLoad = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<Policies><SchedulingPolicy name=\"TestPolicy\">\n<SelectionCriteria></SelectionCriteria>" +
				"<Scorers></Scorers><SchedulingPolicy></Policies>";
		checking(new Expectations() { {
		} });
		try {
			schedMaster.addSchedulingPolicies("localhost", "lala.xml", xmlToLoad);
		} catch (SchedulingInternalExceptionEx ex) {
			//System.out.println("This failure is expected: " + ex.errorTrace.previousError[0].shortDescription);
		}
	}
}
