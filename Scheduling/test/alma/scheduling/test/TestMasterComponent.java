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
 * File TestMasterComponent.java
 */
 
package alma.scheduling.test;

import alma.ACS.ROstringSeq;
import alma.ACS.SUBSYSSTATE_AVAILABLE;
import alma.ACS.SUBSYSSTATE_INITIALIZING_PASS1;
import alma.ACS.SUBSYSSTATE_INITIALIZING_PASS2;
import alma.ACS.SUBSYSSTATE_OFFLINE;
import alma.ACS.SUBSYSSTATE_ONLINE;
import alma.ACS.SUBSYSSTATE_PREINITIALIZED;
import alma.ACS.SUBSYSSTATE_OPERATIONAL;
import alma.ACS.SUBSYSSTATE_PRESHUTDOWN;
import alma.ACS.SUBSYSSTATE_SHUTDOWN;
import alma.ACSErr.ACSErrTypeOK;
import alma.ACSErr.CompletionHolder;
import alma.ACSErrTypeCommon.IllegalStateEventEx;
import alma.ACSErrTypeOK.ACSErrOK;
import alma.acs.component.client.ComponentClientTestCase;
import alma.acs.exceptions.AcsJCompletion;
import alma.ACS.MasterComponentImpl.*;
import alma.ACS.MasterComponentImpl.StateChangeSemaphore;
import alma.ACS.MasterComponent;
import alma.ACS.MasterComponentPackage.SubsystemStateEvent;
import alma.scheduling.AlmaScheduling.SchedulingMasterComponentHelper;
/**
  * Tests the Scheduling Master Component. Test is written using the 
  * MasterComponentTest from acs as an example.
  * @author sslucero
  */
public class TestMasterComponent extends ComponentClientTestCase {

    private MasterComponent sched_mc=null;
    /**
      *
      */
    public TestMasterComponent() throws Exception {
        super("Test Scheduling MasterComponent");
    }

    /**
      *
      */
    protected void setUp() throws Exception {
        super.setUp();
        m_logger.info("SCHED_TEST: setup called");

        sched_mc = alma.ACS.MasterComponentHelper.narrow(getContainerServices().
                            getComponent("SCHEDULINGMASTERCOMPONENT"));
        assertNotNull(sched_mc);
    }

    /*
  	protected void initAcsLogging() {
		// empty, workaround for logging bug...
	}
    */

    /**
      *
      */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    /**
      * Test just pass1
      */  
    public void testInitPass1() throws Exception {
        m_logger.info("SCHED_MC_TEST: testing initPass1");
        ROstringSeq statesProperty = sched_mc.currentStateHierarchy();
        assertNotNull(statesProperty);
        String[] expectedHierarchy = new String[] {
            SUBSYSSTATE_AVAILABLE.value, 
            SUBSYSSTATE_OFFLINE.value, 
            SUBSYSSTATE_SHUTDOWN.value
        };
        verifyCurrentState(statesProperty, expectedHierarchy);
        StateChangeListener listener = new StateChangeListener(m_logger);
        listener.createMonitor(statesProperty, getContainerServices());
        StateChangeSemaphore sync = listener.getStateChangeSemaphore();
        sync.reset();
        //initpass1
        sched_mc.doTransition(SubsystemStateEvent.SUBSYSEVENT_INITPASS1);
        sync.waitForStateChanges(2);
        expectedHierarchy[2] = SUBSYSSTATE_PREINITIALIZED.value;
        verifyCurrentState(statesProperty, expectedHierarchy);
    }

    /**
      * Test up to pass2.
      */
    public void testInitPass2() throws Exception {
        m_logger.info("SCHED_MC_TEST: testing initPass2");
        ROstringSeq statesProperty = sched_mc.currentStateHierarchy();
        assertNotNull(statesProperty);
        String[] expectedHierarchy = new String[] {
            SUBSYSSTATE_AVAILABLE.value, 
            SUBSYSSTATE_OFFLINE.value,
            SUBSYSSTATE_SHUTDOWN.value
        };
        verifyCurrentState(statesProperty, expectedHierarchy);
        StateChangeListener listener = new StateChangeListener(m_logger);
        listener.createMonitor(statesProperty, getContainerServices());
        StateChangeSemaphore sync = listener.getStateChangeSemaphore();
        sync.reset();
        sched_mc.doTransition(SubsystemStateEvent.SUBSYSEVENT_INITPASS1);
        Thread.sleep(5000);
        
        m_logger.info("SCHED_MC_TEST: pass 1 called.. slept.. now pass2");
        sched_mc.doTransition(SubsystemStateEvent.SUBSYSEVENT_INITPASS2);
        expectedHierarchy[1] = SUBSYSSTATE_ONLINE.value;
        sync.waitForStateChanges(2);
        verifyCurrentState(statesProperty, expectedHierarchy);

    }

	/**
	 * Helper method for the repeated task of getting the current state hierarchy
	 * and comparing it against the expected hierarchy.
     * Borrowed from ACS's mastercomp tests.
	 */
	private void verifyCurrentState(ROstringSeq statesProperty, 
            String[] expectedHierarchy) { 

        m_logger.info("SCHED_MC_TEST: in verify");
		CompletionHolder ch = new CompletionHolder();
		String[] states = statesProperty.get_sync(ch);
		
		AcsJCompletion statesSyncCompletion = AcsJCompletion.fromCorbaCompletion(ch.value);
		assertFalse(statesSyncCompletion.isError());
		assertEquals(ACSErrTypeOK.value, statesSyncCompletion.getType());
		assertEquals(ACSErrOK.value, statesSyncCompletion.getCode());
		
		// verify state
		assertNotNull(states);
        
        //System.out.println("States length = "+ states.length);
        //System.out.println("ExpectedHierarchy length = "+ expectedHierarchy.length);
		//assertTrue(states.length == expectedHierarchy.length);
		for (int i = 0; i < states.length; i++) {
            //System.out.println(states[i]);
			assertEquals(expectedHierarchy[i], states[i]);
		}
        /*
        System.out.println("********************");
		for (int i = 0; i < expectedHierarchy.length; i++) {
            System.out.println(expectedHierarchy[i]);
		//	assertEquals(expectedHierarchy[i], states[i]);
		}*/
	}

    
    /**
      *
      */
    public static void main(String[] args) {
        alma.acs.testsupport.tat.TATJUnitRunner.run(TestMasterComponent.class);
    }
}
