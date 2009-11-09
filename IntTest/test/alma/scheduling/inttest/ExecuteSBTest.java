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
 * $Id: ExecuteSBTest.java,v 1.3 2009/11/09 23:40:10 rhiriart Exp $
 */

package alma.scheduling.inttest;

import java.util.Collection;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.omg.CORBA.UserException;

import alma.ACS.MasterComponent;
import alma.ACS.MasterComponentHelper;
import alma.ACS.ROstringSeq;
import alma.ACS.MasterComponentPackage.SubsystemStateEvent;
import alma.ACSErr.Completion;
import alma.ACSErr.CompletionHolder;
import alma.ACSSim.Simulator;
import alma.ACSSim.SimulatorHelper;
import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.component.client.ComponentClientTestCase;
import alma.acs.container.ContainerServices;
import alma.acs.entityutil.EntityDeserializer;
import alma.acs.entityutil.EntitySerializer;
import alma.alarmsystem.source.ACSFaultState;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.lifecycle.stateengine.constants.Role;
import alma.lifecycle.stateengine.constants.Subsystem;
import alma.projectlifecycle.StateSystem;
import alma.projectlifecycle.StateSystemHelper;
import alma.scheduling.ArrayModeEnum;
import alma.scheduling.Interactive_PI_to_Scheduling;
import alma.scheduling.MasterSchedulerIF;
import alma.scheduling.SBLite;
import alma.scheduling.AlmaScheduling.ProjectStatusQueue;
import alma.scheduling.AlmaScheduling.statusImpl.CachedSBStatus;
import alma.scheduling.Define.SchedulingException;
import alma.xmlentity.XmlEntityStruct;
import alma.xmlstore.ArchiveConnection;
import alma.xmlstore.Identifier;
import alma.xmlstore.Operational;

/**
 * Template for Scheduling tests.
 * 
 */
public class ExecuteSBTest extends ComponentClientTestCase {

	private ContainerServices container;
    private Logger logger;
    private Simulator simulator;
    private Utils utils;

    private ArchiveConnection archConnectionComp;
    private Operational archOperational;
    private Identifier archIdentifierComp;
    private StateSystem stateSystemComp;
    
    private MasterComponent schedulingMC;
    private MasterSchedulerIF masterScheduler;

    private EntityDeserializer entityDeserializer;

    public ExecuteSBTest() throws Exception {
        super(ExecuteSBTest.class.getName());
    }

    private void getStateSystemComponent() {
        try {
            logger.fine("SCHEDULING: Getting state system component");
            org.omg.CORBA.Object obj = container.getDefaultComponent("IDL:alma/projectlifecycle/StateSystem:1.0");
            this.stateSystemComp = StateSystemHelper.narrow(obj);
        } catch (AcsJContainerServicesEx e) {
            logger.severe("SCHEDULING: AcsJContainerServicesEx: "+e.toString());
            stateSystemComp =null;
        }
        if (stateSystemComp != null) {
            logger.fine("SCHEDULING: The ALMA State Engine has been constructed.");
        } else {
            logger.warning("SCHEDULING: The ALMA State Engine has NOT been constructed.");
        }
    }

    /**
     * Test case fixture setup.
     */
    protected void setUp() throws Exception {
        super.setUp();

        container = getContainerServices();
        logger = container.getLogger();
        utils = new Utils(container, logger);

        archConnectionComp = alma.xmlstore.ArchiveConnectionHelper.narrow(
                container.getComponent("ARCHIVE_CONNECTION"));
        
        archIdentifierComp = alma.xmlstore.IdentifierHelper.narrow(
                container.getComponent("ARCHIVE_IDENTIFIER"));

        archOperational = archConnectionComp
                .getOperational("ObservationTest");
        assertNotNull(archOperational);
        
        getStateSystemComponent();
        assertNotNull(stateSystemComp);

        entityDeserializer = EntityDeserializer.getEntityDeserializer(
                container.getLogger());

        simulator = 
            SimulatorHelper.narrow(container.getDefaultComponent("IDL:alma/ACSSim/Simulator:1.0"));

        logger.info("Initializing SCHEDULING...");
        schedulingMC = MasterComponentHelper.narrow(container.getComponent("SCHEDULING_MASTER_COMP"));
        schedulingMC.doTransition(SubsystemStateEvent.SUBSYSEVENT_INITPASS1);
        if (!waitForSubsystemState(schedulingMC.currentStateHierarchy(), "AVAILABLE.OFFLINE.PREINITIALIZED", 300)) fail();
        schedulingMC.doTransition(SubsystemStateEvent.SUBSYSEVENT_INITPASS2);
        if (!waitForSubsystemState(schedulingMC.currentStateHierarchy(), "AVAILABLE.ONLINE", 300)) fail();
        
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
        container.releaseComponent(archConnectionComp.name());
        container.releaseComponent(archIdentifierComp.name());
        container.releaseComponent(simulator.name());
        super.tearDown();
    }

    
    final private static int seconds = 1000;
    final private static int minutes = seconds * 60;

	/**
	 * Get all the Entity Ids for SB statuses that are in the state
	 * archive in a given set of states.
	 * 
	 * @param states - we are interested in SBStatuses in any of
	 *                these states.
	 *                
	 * @return a Collection<String> containing the Entity Ids of the
	 *         entities that were found.
	 */
	public Collection<String> getSBStatusesByState(String[] states) {
        final Collection<String> result = new TreeSet<String>();
        
		XmlEntityStruct xml[] = null;
		try {
			xml = stateSystemComp.findSBStatusByState(states);
	    	logger.finest(String.format(
	    			"Scheduling has pulled %d SBStatus%s from State System",
	    			xml.length, (xml.length==1)? "": "es"));
		} catch (Exception e) {
        	logger.finest("Scheduling can not pull SBStatuses from State System");
            e.printStackTrace(System.out);
		}
		
		for (final XmlEntityStruct xes : xml) {
			try {
				final SBStatus sbs = (SBStatus) entityDeserializer.
										deserializeEntity(xes, SBStatus.class);
		    	logger.finest(String.format(
		    			"Scheduling has deserialised SBStatus %s from State System",
		    			sbs.getSBStatusEntity().getEntityId()));
				result.add(sbs.getSBStatusEntity().getEntityId());
			} catch (Exception e) {
	        	logger.finest("Scheduling can not deserialise SBStatus from State System");
	            e.printStackTrace(System.out);
			}
		}
		
		return result;
	}

    public void testSomething() throws Exception {
    	masterScheduler = alma.scheduling.MasterSchedulerIFHelper.narrow(
                container.getComponent("SCHEDULING_MASTERSCHEDULER"));
        String arrayName = masterScheduler.createArray(new String[] {"DV01"},
                new String[] {"PhotonicReference1"}, ArrayModeEnum.INTERACTIVE);
        logger.info("Array name: "+arrayName);
        
        logger.info("Creating Scheduler");
        String schedulerName = masterScheduler.startInteractiveScheduling1(arrayName);
        Interactive_PI_to_Scheduling scheduler =
            alma.scheduling.Interactive_PI_to_SchedulingHelper.narrow(
                container.getComponent(schedulerName));
        
        scheduler.setRunMode(true);
        
        logger.info("Asking the master scheduler for the SBs");
        final SBLite[] sbs = masterScheduler.getSBLites();
        logger.info(String.format("Got %d SB%s from the master scheduler",
        		sbs.length, (sbs.length==1)? "": "s"));
        
        if (sbs.length > 0) {
        	int which = 1;
        	for (final SBLite sb : sbs) {
            	logger.info(String.format(
            			"Executing scheduling block %d of %d (scheduler.startSession)",
            			which++, sbs.length));
            	scheduler.startSession(sb.PI, sb.projectRef);
            	logger.info(String.format("ExecuteSB: %s by %s", sb.schedBlockRef, sb.PI));
            	scheduler.executeSB(sb.schedBlockRef);
            	logger.info("scheduler.endSession");
            	scheduler.endSession();
        	}
        	Thread.sleep(10*seconds); // 10 seconds to let the last SB finish before closing down.
        	
        } else {
        	logger.warning("No SBs found!");
        }
        container.releaseComponent(scheduler.name());
    	masterScheduler.destroyArray(arrayName);
        container.releaseComponent(masterScheduler.name());
//        deleteProject(pinfo);

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

    /**
     * A little class to hold the UIDs of the different entities that
     * are related with an AOT project.
     */
    private class ProjectInfo {
        private String schedBlockID;
        private String projectID;
        private String proposalID;
        private String PI;
        public ProjectInfo(String sblID, String prjID, String propID, String pi) {
            schedBlockID = sblID;
            projectID = prjID;
            proposalID = propID;
            PI = pi;
        }
        public String getSchedBlockID() {
            return schedBlockID;
        }
        public String getProjectID() {
            return projectID;
        }
        public String getProposalID() {
            return proposalID;
        }
        public String getPI() {
            return PI;
        }
    }
    

    /**
     * Deletes a project in the ARCHIVE.
     * @param pinfo Project information
     */
    private void deleteProject(ProjectInfo pinfo) throws Exception {
        archOperational.delete(pinfo.getSchedBlockID());
        archOperational.delete(pinfo.getProjectID());
        archOperational.delete(pinfo.getProposalID());
    }
    
}

