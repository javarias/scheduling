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
 * $Id: SchedulingPluginsTest.java,v 1.3 2009/11/09 23:13:27 rhiriart Exp $
 */

package alma.scheduling.inttest;

import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestSuite;

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

    public static Test suite() {
    	TestSuite suite = new TestSuite();
    	String testSuite = System.getProperty("suite");
    	System.out.println("Suite = " + testSuite);
        try {
            if (testSuite == null)
                suite.addTest(new SchedulingPluginsTest("testCreateArrayPlugin")); // Default test suite.
            else {
                if (testSuite.equals("CreateArray")) {
                    suite.addTest(new SchedulingPluginsTest("testCreateArrayPlugin"));                	
                } else if (testSuite.equals("Interactive")) {
                    suite.addTest(new SchedulingPluginsTest("testInteractiveSchedulingPlugin"));
                } else if (testSuite.equals("Queued")) {
                    suite.addTest(new SchedulingPluginsTest("testQueuedSchedulingPlugin"));                	
                } else if (testSuite.equals("Manual")) {
                    suite.addTest(new SchedulingPluginsTest("testManualSchedulingPlugin"));
                } else if (testSuite.equals("InteractiveFailed")) {
                    suite.addTest(new SchedulingPluginsTest("testInteractiveSchedulingPluginFailedExecution"));
                } else {
                	System.err.println("No test suite with this name: " + testSuite);
                }
            }
        } catch (Exception ex) {
            System.err.println("Error when creating SchedulingPluginsTest: "
                    + ex.toString());
        }
        return suite;
    }
    
    public SchedulingPluginsTest() throws Exception {
        super(SchedulingPluginsTest.class.getName());
    }

    public SchedulingPluginsTest(String testName) throws Exception {
    	super(testName);
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

    public void notestControlNotOperational() throws Exception {
    	
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

    public void testCreateArrayPlugin() throws Exception {
        // Wait here until a key is entered, so the OMC Plugins can be tested.
        System.out.print("Press a key to continue...");
        System.in.read();
    }
    
    public void testInteractiveSchedulingPlugin() throws Exception {
    	masterScheduler = alma.scheduling.MasterSchedulerIFHelper.narrow(
                container.getComponent("SCHEDULING_MASTERSCHEDULER"));
        String arrayName = masterScheduler.createArray(new String[] {"DV01"}, ArrayModeEnum.INTERACTIVE);
        logger.info("Array name: "+arrayName);
        
        logger.info("Creating Scheduler");
        String schedulerName = masterScheduler.startInteractiveScheduling1(arrayName);
        Interactive_PI_to_Scheduling scheduler =
            alma.scheduling.Interactive_PI_to_SchedulingHelper.narrow(
                container.getComponent(schedulerName));
        
        // Wait here until a key is entered, so the OMC Plugins can be tested.
        System.out.print("Press a key to continue...");
        System.in.read();
        
        container.releaseComponent(scheduler.name());
    	masterScheduler.destroyArray(arrayName);
        container.releaseComponent(masterScheduler.name());
    }

    public void testInteractiveSchedulingPluginFailedExecution() throws Exception {
    	String code =
    		    "LOGGER.logInfo('observe() called from CDB')\n";
    	code += "sbId = parameters[0]\n";
    	code += "sessionId = parameters[1]\n";
    	code += "when = parameters[2]\n";
    	code += "container = parameters[3]\n";
    	code += "import asdmIDLTypes\n";
    	code += "import time\n";
    	code += "import ACSErr\n";
    	code += "import Control\n";
    	code += "import offline\n";
    	code += "name = container._get_name()\n";
    	code += "name = name.replace('CONTROL/', '')\n";
    	code += "entityId = 'uid://X0/X0'\n";
    	code += "partId = 'X0'\n";
    	code += "entityTypeName = 'ExecBlock'\n";
    	code += "instanceVersion = '1.0'\n";
    	code += "execId = asdmIDLTypes.IDLEntityRef(entityId, partId, entityTypeName, instanceVersion)\n";
    	code += "execBlockStartedEvent = Control.ExecBlockStartedEvent(execId, sbId, sessionId, name, 0L)\n";
    	code += "completion = Control.FAIL\n";
    	code += "execBlockEndedEvent = Control.ExecBlockEndedEvent(execId, sbId, sessionId, name, 'DC000', completion, [], 0L)\n";
    	code += "dcName = name + '/DC001'\n";
    	code += "dataCapturerId = offline.DataCapturerId(dcName, name, '', '', sessionId, sbId)\n";
    	code += "asdmArchivedEvent = offline.ASDMArchivedEvent(dataCapturerId, 'complete', execId, 0L)\n";
    	code += "LOGGER.logInfo('Sending ExecBlockStartedEvent')\n";
    	code += "supplyEventByInstance(name, 'CONTROL_SYSTEM', execBlockStartedEvent)\n";
    	code += "time.sleep(10)\n";
    	code += "LOGGER.logInfo('Sending ExecBlockEndedEvent')\n";
    	code += "supplyEventByInstance(name, 'CONTROL_SYSTEM', execBlockEndedEvent)\n";
    	code += "time.sleep(10)\n";
    	code += "LOGGER.logInfo('Sending ASDMArchivedEvent')\n";
    	code += "supplyEventByInstance(name, 'CONTROL_SYSTEM', asdmArchivedEvent)\n";
    	code += "None";
    	simulator.setMethod("CONTROL/Array001", "observe", code, 0);
    	testInteractiveSchedulingPlugin();
    	simulator.removeMethod("CONTROL/Array001", "observe");

    }
    
    public void testQueuedSchedulingPlugin() throws Exception {
    	masterScheduler = alma.scheduling.MasterSchedulerIFHelper.narrow(
                container.getComponent("SCHEDULING_MASTERSCHEDULER"));
        String arrayName = masterScheduler.createArray(new String[] {"DV01"}, ArrayModeEnum.QUEUED);
        logger.info("Array name: "+arrayName);
    	
        System.out.print("Press a key to continue...");
        System.in.read();
        
    	masterScheduler.destroyArray(arrayName);
        container.releaseComponent(masterScheduler.name());
    }

    public void testManualSchedulingPlugin() throws Exception {
    	masterScheduler = alma.scheduling.MasterSchedulerIFHelper.narrow(
                container.getComponent("SCHEDULING_MASTERSCHEDULER"));
        String arrayName = masterScheduler.createArray(new String[] {"DV01"}, ArrayModeEnum.MANUAL);
        logger.info("Array name: "+arrayName);
    	
        System.out.print("Press a key to continue...");
        System.in.read();
        
    	masterScheduler.destroyArray(arrayName);
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

