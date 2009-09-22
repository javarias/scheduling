/*
 * ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2005 
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 * $Id: SchedulingPluginsTest.java,v 1.2 2009/09/22 22:40:17 rhiriart Exp $
 */

package alma.scheduling.inttest;

import java.util.logging.Logger;

import alma.ACS.MasterComponent;
import alma.ACS.MasterComponentHelper;
import alma.ACS.ROstringSeq;
import alma.ACS.MasterComponentPackage.SubsystemStateEvent;
import alma.ACSErr.Completion;
import alma.ACSErr.CompletionHolder;
import alma.ACSSim.Simulator;
import alma.ACSSim.SimulatorHelper;
import alma.TMCDB.TMCDBComponent;
import alma.TMCDB.TMCDBComponentHelper;
import alma.acs.component.client.ComponentClientTestCase;
import alma.acs.container.ContainerServices;
import alma.scheduling.ArrayModeEnum;
import alma.scheduling.Interactive_PI_to_Scheduling;
import alma.scheduling.MasterSchedulerIF;
import alma.xmlstore.ArchiveConnection;
import alma.xmlstore.Identifier;
import alma.xmlstore.Operational;

/**
 * Test utility for running OMC plugins test. It is used in combination
 * with alma.scheduling.plugintest.PluginStarter.
 * 
 */
public class SchedulingPluginsTest extends ComponentClientTestCase {

	private ContainerServices container;
    private Logger logger;
    private Simulator simulator;

    private ArchiveConnection archConnectionComp;
    private Operational archOperational;
    private Identifier archIdentifierComp;
    
    private TMCDBComponent tmcdb;
    private MasterComponent schedulingMC;
    private MasterSchedulerIF masterScheduler;
    
    public SchedulingPluginsTest() throws Exception {
        super(SchedulingPluginsTest.class.getName());
    }

    /**
     * Test case fixture setup.
     */
    protected void setUp() throws Exception {
        super.setUp();

        container = getContainerServices();
        logger = container.getLogger();

        archConnectionComp = alma.xmlstore.ArchiveConnectionHelper.narrow(
                container.getComponent("ARCHIVE_CONNECTION"));
        
        archIdentifierComp = alma.xmlstore.IdentifierHelper.narrow(
                container.getComponent("ARCHIVE_IDENTIFIER"));

        archOperational = archConnectionComp
                .getOperational("ObservationTest");
        assertNotNull(archOperational);
        
        simulator = 
            SimulatorHelper.narrow(container.getDefaultComponent("IDL:alma/ACSSim/Simulator:1.0"));

        tmcdb = TMCDBComponentHelper.narrow(container.getComponent("TMCDB"));
        
        logger.info("Initializing SCHEDULING...");
        schedulingMC = MasterComponentHelper.narrow(container.getComponent("SCHEDULING_MASTER_COMP"));
        schedulingMC.doTransition(SubsystemStateEvent.SUBSYSEVENT_INITPASS1);
        if (!waitForSubsystemState(schedulingMC.currentStateHierarchy(), "AVAILABLE.OFFLINE.PREINITIALIZED", 300)) fail();
        schedulingMC.doTransition(SubsystemStateEvent.SUBSYSEVENT_INITPASS2);
        if (!waitForSubsystemState(schedulingMC.currentStateHierarchy(), "AVAILABLE.ONLINE", 300)) fail();
        schedulingMC.doTransition(SubsystemStateEvent.SUBSYSEVENT_START);
        if (!waitForSubsystemState(schedulingMC.currentStateHierarchy(), "AVAILABLE.OPERATIONAL", 300)) fail();
        
    }

    /**
     * Test case fixture clean up.
     */
    protected void tearDown() throws Exception {
        logger.info("Shutting down SCHEDULING...");
        schedulingMC.doTransition(SubsystemStateEvent.SUBSYSEVENT_SHUTDOWNPASS1);
        if (waitForSubsystemState(schedulingMC.currentStateHierarchy(), "AVAILABLE.OFFLINE.PRESHUTDOWN", 300)) {
            schedulingMC.doTransition(SubsystemStateEvent.SUBSYSEVENT_SHUTDOWNPASS2);
            waitForSubsystemState(schedulingMC.currentStateHierarchy(), "AVAILABLE.OFFLINE.SHUTDOWN", 300);
        }
        container.releaseComponent(tmcdb.name());
        container.releaseComponent(archConnectionComp.name());
        container.releaseComponent(archIdentifierComp.name());
        container.releaseComponent(simulator.name());
        super.tearDown();
    }

    public void testControlNotOperational() throws Exception {
    	
    	masterScheduler = alma.scheduling.MasterSchedulerIFHelper.narrow(
                container.getComponent("SCHEDULING_MASTERSCHEDULER"));
        
    	logger.info("Setting CONTROL state to INACCESSIBLE");
    	String code =
    		    "import Control\n";
    	code += "Control.INACCESSIBLE";
    	simulator.setMethod("CONTROL/MASTER", "getMasterState", code, 0);
    	
        // Wait here until a key is entered, so the OMC Plugins can be tested.
    	
        System.out.print("Ready to start plugin. Press any key when done...");
        System.in.read();

    	logger.info("Setting CONTROL state to OPERTIONAL");
    	code =  "import Control\n";
    	code += "Control.OPERATIONAL";
    	simulator.setMethod("CONTROL/MASTER", "getMasterState", code, 0);

        System.out.print("Press any key to continue...");
        System.in.read();
        
        container.releaseComponent(masterScheduler.name());

    }    

    /**
     * Waits for the subystems property to reach a given state.
     * @param stateProp Subsystem Master state property
     * @param expected Expected state
     * @param timeout timeout in seconds
     * 
     */
    private boolean waitForSubsystemState(ROstringSeq stateProp, String expected, int timeout)
        throws Exception {
        
        String state = "";
        int sleepInterval = 1000;
        int timeoutCount = (int) 1000.0 * timeout / sleepInterval;
        int count = 0;
        logger.info("Waiting for subsystem to reach state "+expected+". Timeout is " +timeout+" (s).");
        do {
            Thread.sleep(sleepInterval);
            count++;
            Completion c = new Completion(0, 0, 0, new alma.ACSErr.ErrorTrace[] {});
            CompletionHolder ch = new CompletionHolder(c);
            String[] substates = stateProp.get_sync(ch);
            
            state = "";
            for (String s : substates)
                state += s+".";
            state = state.substring(0, state.length()-1);
            // logger.info("Current state is " + state);
            if (state.equals("AVAILABLE.ERROR")) {
                logger.severe("Subsystem went to error state");
                return false;
            }
        } while(!state.equals(expected) && count < timeoutCount);
        if (!state.equals(expected)) {
            logger.severe("Timeout waiting for state "+expected+"; real state is "+state);
            return true;
        }
        logger.info("Subsystem state is now "+state);
        return true;
    }

}

