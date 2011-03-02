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
 * $Id: CreateArrayPluginTest.java,v 1.5 2011/03/02 17:47:21 javarias Exp $
 */

package alma.scheduling.plugintest;

import java.util.Properties;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.TabGroup;
import org.uispec4j.Table;
import org.uispec4j.Trigger;
import org.uispec4j.UISpec4J;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

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
import alma.TMCDB.Access;
import alma.TMCDB.AccessHelper;
import alma.acs.component.client.ComponentClientTestCase;
import alma.acs.container.ContainerServices;
import alma.scheduling.ArchiveUpdater;
import alma.scheduling.Master;
import alma.scheduling.master.gui.SchedulingPanelMainFrame;
import alma.xmlstore.ArchiveConnection;
import alma.xmlstore.Identifier;
import alma.xmlstore.Operational;

/**
 * Automated test for the CreateArray plugin.
 * 
 */
public class CreateArrayPluginTest extends ComponentClientTestCase {

	static {
		UISpec4J.init();
	}

	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite();
		suite.addTest(new CreateArrayPluginTest("testControlNotOperational"));
		suite.addTest(new CreateArrayPluginTest("testCreateInteractiveArray"));
		return suite;
	}
	
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
    
    public CreateArrayPluginTest() throws Exception {
        super(CreateArrayPluginTest.class.getName());
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
        container.releaseComponent(control.name());
        container.releaseComponent(tmcdb.name());
        container.releaseComponent(archConnectionComp.name());
        container.releaseComponent(archIdentifierComp.name());
        container.releaseComponent(simulator.name());
        super.tearDown();
    }

    public CreateArrayPluginTest(String name) throws Exception {
    	super(name);
    }
    
    /**
     * If the buttons in the CreateArray panel are pressed before the CONTROL
     * subsystem is operational, an error dialog should pop up.
     * 
     * @throws Exception
     */
    public void testControlNotOperational() throws Exception {
    	
    	masterScheduler = alma.scheduling.MasterHelper.narrow(
                container.getComponent("SCHEDULING_MASTERSCHEDULER"));
        
    	// Set the CONTROL simulator so this subsystem is not operational
    	logger.info("Setting CONTROL state to INACCESSIBLE");
    	String code =
    		    "import Control\n";
    	code += "Control.INACCESSIBLE";
    	simulator.setMethod("CONTROL/MASTER", "getMasterState", code, 0);
    	
    	// Start the plugin
    	Properties props = new Properties();
    	props.setProperty("array.name", "Array001");
    	PluginContainerServices pluginSvc = new PluginContainerServices(container, props);
    	SchedulingPanelMainFrame plugin = new SchedulingPanelMainFrame();
    	plugin.setServices(pluginSvc);
    	plugin.start();
    	Thread.sleep(2000);
    	
    	Panel panel = new Panel(plugin);
    	TabGroup tg = panel.getTabGroup();
    	tg.selectTab("Main");
    	Panel mainPanel = tg.getSelectedTab();
    	Button interactiveButton = mainPanel.getButton("Interactive");
    	// Click the "Interactive" button, intercept the error dialog that
    	// will popup and press its "OK" button.
    	WindowInterceptor
    	    .init(interactiveButton.triggerClick())
    	    .process(new WindowHandler() {
    	    	public Trigger process(Window window) {
    	    		assertEquals("System not operational yet.",
    	    				     window.getTextBox("OptionPane.label").getText());
    	    		return window.getButton("OK").triggerClick();
    	    	}
    	    }).run();

    	container.releaseComponent(masterScheduler.name());
    }    

    /**
     * Test the creation of an interactive array.
     * 
     * @throws Exception
     */
    public void testCreateInteractiveArray() throws Exception {
    	
    	masterScheduler = alma.scheduling.MasterHelper.narrow(
                container.getComponent("SCHEDULING_MASTERSCHEDULER"));

    	// Set the CONTROL simulator so this subsystem is operational
    	logger.info("Setting CONTROL state to OPERATIONAL");
    	String code =
    		    "import Control\n";
    	code += "Control.OPERATIONAL";
    	simulator.setMethod("CONTROL/MASTER", "getMasterState", code, 0);
    	
    	// Start the CreateArray plugin
    	Properties props = new Properties();
    	props.setProperty("array.name", "Array001");
    	PluginContainerServices pluginSvc = new PluginContainerServices(container, props);
    	SchedulingPanelMainFrame plugin = new SchedulingPanelMainFrame();
    	plugin.setServices(pluginSvc);
    	plugin.start();
    	Thread.sleep(2000); // allow some time for the plugin to start (the plugin
    	                    // changes the GUI in a thread)
    	
    	// Test the user interface
    	Panel panel = new Panel(plugin);
    	TabGroup tg = panel.getTabGroup();
    	tg.selectTab("Main");
    	Panel mainPanel = tg.getSelectedTab();
    	Panel twelveChessboardPanel = mainPanel.getPanel("TwelveMeterAntennas");
    	Table twelveChessboard = twelveChessboardPanel.getTable();
    	Button interactiveButton = mainPanel.getButton("Interactive");
    	Button createButton = mainPanel.getButton("Create");
    	// Click in the Interactive button
        interactiveButton.click();
        Thread.sleep(1000);
        // Select the first antenna in the chessboard
    	twelveChessboard.click(0, 0);
        Thread.sleep(1000);
        // Click the "Create" button
        createButton.click();
        Thread.sleep(5000); // allow some time for the array to be created
        tg.selectTab("Existing Arrays");
        Panel existArrPanel = tg.getSelectedTab();
        Table existArr = existArrPanel.getTable();
        // Check that the new array appears in the "Existing Arrays" tab
        assertEquals(1, existArr.getRowCount());

        masterScheduler.destroyArray("Array001");
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

