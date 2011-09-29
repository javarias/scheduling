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
 * $Id: SchedulingPluginsTest.java,v 1.10 2011/09/29 20:53:55 dclarke Exp $
 */

package alma.scheduling.inttest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import alma.Control.ControlMaster;
import alma.Control.ControlMasterHelper;
import alma.Control.CorrelatorType;
import alma.TMCDB.Access;
import alma.TMCDB.AccessHelper;
import alma.acs.component.client.ComponentClientTestCase;
import alma.acs.container.ContainerServices;
import alma.scheduling.ArchiveUpdater;
import alma.scheduling.Array;
import alma.scheduling.ArrayCreationInfo;
import alma.scheduling.ArrayDescriptor;
import alma.scheduling.ArrayModeEnum;
import alma.scheduling.ArraySchedulerLifecycleType;
import alma.scheduling.Master;
import alma.scheduling.SchedBlockQueueItem;
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
    
    private Access tmcdb;
    private MasterComponent schedulingMC;
    private Master masterScheduler;
    private ArchiveUpdater archiveUpdater;
    private ControlMaster control;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

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
                } else if (testSuite.equals("Automated")) {
                    suite.addTest(new SchedulingPluginsTest("testAutomated"));
                }else {
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

        
        tmcdb = AccessHelper.narrow(container.getComponent("TMCDB"));
        
        control = ControlMasterHelper.narrow(container.getComponent("CONTROL/MASTER"));
        
        archiveUpdater = alma.scheduling.ArchiveUpdaterHelper.narrow(container.getComponent("SCHEDULING_UPDATER"));
        
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
        container.releaseComponent(archiveUpdater.name());
        container.releaseComponent(tmcdb.name());
        container.releaseComponent(control.name());
        container.releaseComponent(archConnectionComp.name());
        container.releaseComponent(archIdentifierComp.name());
        container.releaseComponent(simulator.name());
        super.tearDown();
    }

    public void notestControlNotOperational() throws Exception {
    	
    	masterScheduler = alma.scheduling.MasterHelper.narrow(
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
    
    public void testAutomated() throws Exception {
    	String timestamp = dateFormat.format(new Date());
    	container.getLogger().info("Populating Archive with Projects");
    	Process pop = Runtime.getRuntime().exec("./scripts/populate.sh");
    	assertEquals(0, pop.waitFor());
    	internalTestAutomaticArray(timestamp);
    	internalTestManualArray(timestamp);
    }
    
    private void internalTestAutomaticArray(String timestamp) throws Exception {
    	masterScheduler = alma.scheduling.MasterHelper.narrow(
                container.getComponent("SCHEDULING_MASTERSCHEDULER"));
    	container.getLogger().info("Testing Automatic Array");
    	final String[] antennaList =  {"DV01", "DA41"};
    	final String[] photonicsList = {};
    	String [] sbs = archOperational.queryRecent("SchedBlock", timestamp);
    	final ArrayDescriptor desc = new ArrayDescriptor(antennaList, photonicsList, CorrelatorType.BL, ArrayModeEnum.INTERACTIVE, ArraySchedulerLifecycleType.NORMAL, "");
    	final ArrayCreationInfo arrayName = masterScheduler.createArray(desc);
    	Array array = alma.scheduling.ArrayHelper.narrow(container.getComponent(
    			arrayName.arrayComponentName)); 
    	for(String sb: sbs) {
    		array.push(new SchedBlockQueueItem(System.currentTimeMillis(), sb));
    	}
    	array.start("SchedulingTest", "Test");
    	Thread.sleep(5 * 60 * 1000);
    	array.stop("SchedulingTest", "Test");
    	Thread.sleep(30 * 1000);
   		masterScheduler.destroyArray(arrayName.arrayId, "SchedulingTest", "Test");
    	container.releaseComponent(masterScheduler.name(), null);
    }
    
    private void internalTestManualArray(String timestamp) throws Exception {
    	masterScheduler = alma.scheduling.MasterHelper.narrow(
                container.getComponent("SCHEDULING_MASTERSCHEDULER"));
    	container.getLogger().info("Testing Manual Array");
    	final String[] antennaList =  {"DV01", "DA41"};
    	final String[] photonicsList = {};
    	String [] sbs = archOperational.queryRecent("SchedBlock", timestamp);
    	final ArrayDescriptor desc = new ArrayDescriptor(antennaList, photonicsList, CorrelatorType.BL, ArrayModeEnum.MANUAL, ArraySchedulerLifecycleType.NORMAL, "");
    	final ArrayCreationInfo arrayName = masterScheduler.createArray(desc);
    	Array array = alma.scheduling.ArrayHelper.narrow(container.getComponent(
    			arrayName.arrayComponentName));
    	array.push(new SchedBlockQueueItem(System.currentTimeMillis(), sbs[0]));
    	array.start("SchedulingTest", "Test");
    	Process proc = Runtime.getRuntime().exec("python testManualScript.py");
    	proc.waitFor();
    	proc = Runtime.getRuntime().exec("python testManualScript.py");
    	proc.waitFor();
    	masterScheduler.destroyArray(arrayName.arrayId, "SchedulingTest", "Test");
    	container.releaseComponent(masterScheduler.name(), null);
    }
    
    public void testInteractiveSchedulingPlugin() throws Exception {
    	masterScheduler = alma.scheduling.MasterHelper.narrow(
                container.getComponent("SCHEDULING_MASTERSCHEDULER"));
    	final ArrayDescriptor desc = new ArrayDescriptor(
        		new String[] {"DV01"},
                new String[] {"PhotonicReference1"},
                CorrelatorType.BL,
                ArrayModeEnum.INTERACTIVE, ArraySchedulerLifecycleType.NORMAL, "");
        ArrayCreationInfo arrayName = masterScheduler.createArray(desc);
        logger.info("Array name: "+arrayName);
        
        logger.info("Creating Scheduler");
        Array scheduler =
            alma.scheduling.ArrayHelper.narrow(
                container.getComponent(arrayName.arrayComponentName));
        
        // Wait here until a key is entered, so the OMC Plugins can be tested.
        System.out.print("Press a key to continue...");
        System.in.read();
        
        container.releaseComponent(scheduler.name());
    	masterScheduler.destroyArray(arrayName.arrayId, "SchedulingTest", "Test");
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

    public void testManualSchedulingPlugin() throws Exception {
    	masterScheduler = alma.scheduling.MasterHelper.narrow(
                container.getComponent("SCHEDULING_MASTERSCHEDULER"));
        ArrayDescriptor desc = new ArrayDescriptor (
        		new String[] {"DV01"},
                new String[] {"PhotonicReference1"},
                CorrelatorType.BL,
                ArrayModeEnum.MANUAL, ArraySchedulerLifecycleType.NORMAL, "");
        ArrayCreationInfo arrayName = masterScheduler.createArray(desc);
        logger.info("Array name: "+arrayName);
    	
        System.out.print("Press a key to continue...");
        System.in.read();
        
        masterScheduler.destroyArray(arrayName.arrayId, "SchedulingTest", "Test");
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

