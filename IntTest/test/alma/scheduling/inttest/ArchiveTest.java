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
 * $Id: ArchiveTest.java,v 1.3 2009/11/09 23:13:27 rhiriart Exp $
 */

package alma.scheduling.inttest;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import alma.ACS.MasterComponent;
import alma.ACS.MasterComponentHelper;
import alma.ACS.ROstringSeq;
import alma.ACS.MasterComponentPackage.SubsystemStateEvent;
import alma.ACSErr.Completion;
import alma.ACSErr.CompletionHolder;
import alma.ACSErrTypeCommon.IllegalArgumentEx;
import alma.ACSSim.Simulator;
import alma.ACSSim.SimulatorHelper;
import alma.acs.component.client.ComponentClientTestCase;
import alma.acs.container.ContainerServices;
import alma.acs.entityutil.EntityDeserializer;
import alma.acs.entityutil.EntitySerializer;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.entity.xmlbinding.schedblock.SchedBlock;
import alma.entity.xmlbinding.valuetypes.StatusT;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.lifecycle.stateengine.constants.Location;
import alma.lifecycle.stateengine.constants.Role;
import alma.lifecycle.stateengine.constants.Subsystem;
import alma.projectlifecycle.StateSystem;
import alma.scheduling.MasterSchedulerIF;
import alma.statearchiveexceptions.NoSuchEntityEx;
import alma.stateengineexceptions.NoSuchTransitionEx;
import alma.stateengineexceptions.NotAuthorizedEx;
import alma.stateengineexceptions.PostconditionFailedEx;
import alma.stateengineexceptions.PreconditionFailedEx;
import alma.xmlentity.XmlEntityStruct;
import alma.xmlstore.ArchiveConnection;
import alma.xmlstore.ArchiveInternalError;
import alma.xmlstore.Identifier;
import alma.xmlstore.Operational;

/**
 * Template for Scheduling tests.
 * 
 */
public class ArchiveTest extends ComponentClientTestCase {

	final private static String[] OPRunnableStates = {
		StatusTStateType.READY.toString(),				
		StatusTStateType.PARTIALLYOBSERVED.toString()				
	};
	final private static String[] SBRunnableStates = {
		StatusTStateType.READY.toString()				
	};

	
	private ContainerServices container;
    private Logger logger;
    private Simulator simulator;
    private Utils utils;

    private EntityDeserializer entityDeserializer;
    private EntitySerializer entitySerializer;

    private ArchiveConnection archConnectionComp;
    private Operational archOperational;
    private Identifier archIdentifierComp;

    private StateSystem stateSystemComp;

    private MasterComponent schedulingMC;
    private MasterSchedulerIF masterScheduler;

	private SortedMap<String, ProjectStatus> runnablePSs;
	private SortedMap<String, SBStatus> runnableSBSs;

    public ArchiveTest() throws Exception {
        super(ArchiveTest.class.getName());
    }

    /**
     * Test case fixture setup.
     */
    protected void setUp() throws Exception {
        super.setUp();

        container = getContainerServices();
        logger = container.getLogger();
        utils = new Utils(container, logger);

        entitySerializer = EntitySerializer.getEntitySerializer(
        		container.getLogger());
        entityDeserializer = EntityDeserializer.getEntityDeserializer(
        		container.getLogger());

        archConnectionComp = alma.xmlstore.ArchiveConnectionHelper.narrow(
                container.getComponent("ARCHIVE_CONNECTION"));
        
        archIdentifierComp = alma.xmlstore.IdentifierHelper.narrow(
                container.getComponent("ARCHIVE_IDENTIFIER"));

        archOperational = archConnectionComp
                .getOperational("ObservationTest");
        assertNotNull(archOperational);
        
        final Object comp = container.getDefaultComponent("IDL:alma/ACSSim/Simulator:1.0");
        simulator = SimulatorHelper.narrow(comp);

        logger.info("SCHEDULING: Getting state system component");
        stateSystemComp = alma.projectlifecycle.StateSystemHelper.narrow(
                container.getComponent("OBOPS_LIFECYCLE_STATE_SYSTEM"));
//        stateSystemComp.setRunLocation(Location.TEST);
        
        
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
    
    /**
     * Work out which SBs and ObsProjects can be observed based on
     * their statuses and the statuses of their containing projects.
     * Put the results into <code>runnableSBSs</code> and
     * <code>runnablePSs</code>.
     * 
     * Does this by:
     * <ol>
     *    <li>getting all the SBStatuses with a runnable state;</li>
     *    <li>getting all the ProjectStatuses with a runnable state;</li>
     *    <li>dropping all the otherwise runnable SBStatuses which
     *        are not part of a runnable project.</li>
     *    <li>dropping all the otherwise runnable ProjectStatuses which
     *        have no SBStatuses left in out collection.</li>
     * </ol>
     * 
     * @param initialise - if <code>true</code>, then initialise the
     *                     SBStatuses' execution count.
     *                     
     * @throws ArchiveInternalError 
     */
    public void getThingsToRun(boolean initialise) throws ArchiveInternalError {
    	// Start with the ProjectStatuses and SBStatuses which are
    	// in runnable states
    	runnablePSs  = getProjectStatusesByState(OPRunnableStates);
    	runnableSBSs = getSBStatusesByState(SBRunnableStates, initialise);
    	
    	// Find SBStatuses which are not part of a runnable project.
    	final Set<String> sbsIdsToRemove = new HashSet<String>();
    	final Set<String> psIdsToKeep    = new HashSet<String>();
    	for (final String sbsId : runnableSBSs.keySet()) {
    		final SBStatus sbs  = runnableSBSs.get(sbsId);
    		final String   psId = sbs.getProjectStatusRef().getEntityId();
    		
    		if (runnablePSs.containsKey(psId)) {
    			// The SBStatus is in a ProjectStatus we know about, so keep both
    			psIdsToKeep.add(psId);
    		} else {
    			// The SBStatus is not in a ProjectStatus we know about, so dump it
    			sbsIdsToRemove.add(sbsId);
    		}
    	}
    	
    	// Now remove the SBStatuses that we have just determined are not part
    	// of a runnable ProjectStatus.
    	for (final String sbsId : sbsIdsToRemove) {
    		runnableSBSs.remove(sbsId);
    	}
    	
    	// Also remove the ProjectStatuses that we have just determined do not
    	// any runnable SBStatuses. As we've remembered the ones to keep rather
    	// than to remove, then we need to do a bit of Set complementing first.
    	final Set<String> psIdsToRemove = new HashSet<String>(runnablePSs.keySet());
    	psIdsToRemove.removeAll(psIdsToKeep);
    	for (final String sbsId : sbsIdsToRemove) {
    		runnableSBSs.remove(sbsId);
    	}
    }
    
    /**
     * Do one simulated scheduling cycle which is basically:
     * <ol>
     *    <li>work out which SBs can be observed;</li>
     *    <li>select one to observe;</li>
     *    <li>observe it;</li>
     *    <li>update the statuses accordingly.</li>
     * </ol>
     * 
     * @param initialise - if <code>true</code>, then initialise the
     *                     SBStatuses' execution count.
     * @return <code>true</code> if an SB was observed and
     *        <code>false</code> if no SB was available
     * @throws ArchiveInternalError 
     */
    public boolean oneExecution(boolean initialise) throws ArchiveInternalError {
    	// Work out which SBs can be observed
    	getThingsToRun(initialise);
    	
    	// Check we have some SBs to observe, if not then jump ship.
    	if (runnableSBSs.isEmpty()) {
    		logger.warning("No SBs found to observe");
    		return false;
    	}
    	
    	// Select one to observe
    	final SBStatus sbs = runnableSBSs.get(runnableSBSs.firstKey());
    	
    	// Get the SchedBlock and ObsProject to match.
    	final SchedBlock sb = getSchedBlock(sbs);
    	final int runs = sbs.getExecutionsRemaining();

    	return true;
    }
    
    
    private SchedBlock getSchedBlock(SBStatus sbs, boolean dirty) {
    	SchedBlock result = null;
		try {
	    	XmlEntityStruct xml;
	    	if (dirty) {
	    		xml = archOperational.retrieveDirty(sbs.getSchedBlockRef().getEntityId());
	    	} else {
	    		xml = archOperational.retrieve(sbs.getSchedBlockRef().getEntityId());
	    	}
			result = (SchedBlock) entityDeserializer.deserializeEntity(xml, SchedBlock.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
    
    
    private SchedBlock getSchedBlock(SBStatus sbs) {
		return getSchedBlock(sbs, false);
	}

	public void testSomething() throws Exception {
    	
//    	while (oneExecution()) {
//    		// do nothing
//    	}


		getThingsToRun(true);
    	logger.fine("Initial query");
        logger.fine(showRunnables());
//        makeRunnable(getProjectStatusesByState(OPPreambleStates).keySet());
        
        for (int i = 1; i < 10; i++) {
        	Thread.sleep(2*60*1000); // Two minutes
        	getThingsToRun(false);
        	logger.fine(String.format("Query %d", i));
        	logger.fine(showRunnables());
        }
		getThingsToRun(false);
    	logger.fine("Final query");
        logger.fine(showRunnables());

//    	calculateIntersection(runPS, runSBS);
    	
    	
        
//    	masterScheduler = alma.scheduling.MasterSchedulerIFHelper.narrow(
//                container.getComponent("SCHEDULING_MASTERSCHEDULER"));
//        String arrayName = masterScheduler.createArray(new String[] {"DV01"},
//        		ArrayModeEnum.INTERACTIVE);
//        logger.info("Array name: "+arrayName);
//        
//        logger.info("Creating Scheduler");
//        String schedulerName = masterScheduler.startInteractiveScheduling1(arrayName);
//        Interactive_PI_to_Scheduling scheduler =
//            alma.scheduling.Interactive_PI_to_SchedulingHelper.narrow(
//                container.getComponent(schedulerName));
//        
//        container.releaseComponent(scheduler.name());
//    	masterScheduler.destroyArray(arrayName);
//        container.releaseComponent(masterScheduler.name());
    }    

//    private void makeRunnable(Set<String> psIDs) {
//    	for (final String psID : psIDs) {
//    		try {
//				stateSystemComp.changeProjectStatus(
//						psID,
//						StatusTStateType.READY.toString(),
//						Subsystem.SCHEDULING,
//						Role.AOD);
//			} catch (PreconditionFailedEx e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (NoSuchEntityEx e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IllegalArgumentEx e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (NoSuchTransitionEx e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (PostconditionFailedEx e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (NotAuthorizedEx e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//    	}
//	}

    private String showProjectStatus(ProjectStatus ps) {
    	String result;
    	final StatusT status = ps.getStatus();
    	
    	result = String.format("<ProjectStatus status=%s psid=%16s opref=%16s/>",
    			(status != null)? status.getState(): "null",
    					ps.getProjectStatusEntity().getEntityId(),
    					ps.getObsProjectRef().getEntityId());

			return result;
    }

    private String showSBStatus(SBStatus sbs) {
    	String result;
    	final StatusT status = sbs.getStatus();
    	
    	result = String.format("<SBStatus status=%s sbsid=%16s sbref=%16s remaining=%d/>",
    			(status != null)? status.getState(): "null",
    					sbs.getSBStatusEntity().getEntityId(),
    					sbs.getSchedBlockRef().getEntityId(),
    					sbs.getExecutionsRemaining());

		return result;
    }
    
    private String showRunnables() {
    	final StringBuilder b = new StringBuilder();
    	
    	b.append(String.format("\nAll %d Runnable%s\n",
    			runnablePSs.size(),
    			(runnablePSs.size() == 1)? "": "s"));

    	for (final String psID : runnablePSs.keySet()) {
    		final ProjectStatus ps = runnablePSs.get(psID);
    		b.append(showProjectStatus(ps));
    		b.append('\n');
    		for (final String sbsID : runnableSBSs.keySet()) {
        		final SBStatus sbs = runnableSBSs.get(sbsID);
    			if (sbs.getProjectStatusRef().getEntityId().equals(psID)) {
    	    		b.append('\t');
    	    		b.append(showSBStatus(sbs));
    	    		b.append('\n');
    			}
    		}
    	}
    	
    	return b.toString();
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
	 * Get all the project statuses that are in the state archive in a
	 * given set of states.
	 * 
	 * @param states - we are interested in ProjectStatuses in any of
	 *                these states.
	 * @return a map from EntityId to ProjectStatus containing all the
	 *         ProjectStatus entities found
	 * 
	 * @throws ArchiveInternalError
	 */
	private SortedMap<String, ProjectStatus> getProjectStatusesByState(String[] states)
				throws ArchiveInternalError {
        final SortedMap<String, ProjectStatus> result =
        	new TreeMap<String, ProjectStatus>();
        
		XmlEntityStruct xml[] = null;
		try {
			xml = stateSystemComp.findProjectStatusByState(states);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for (final XmlEntityStruct xes : xml) {
			try {
				final ProjectStatus ps = (ProjectStatus) entityDeserializer.
				deserializeEntity(xes, ProjectStatus.class);
				result.put(ps.getProjectStatusEntity().getEntityId(), ps);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	/**
	 * Get all the SB statuses that are in the state archive in a
	 * given set of states.
	 * 
	 * @param states - we are interested in SBStatuses in any of
	 *                these states.
     * @param initialise - if <code>true</code>, then initialise the
     *                     SBStatuses' execution count.
	 * @return a map from EntityId to SBStatus containing all the
	 *         ProjectStatus entities found
	 * 
	 * @throws ArchiveInternalError
	 */
	private SortedMap<String, SBStatus> getSBStatusesByState(
			String[] states,
			boolean  initialise)
				throws ArchiveInternalError {
        final SortedMap<String, SBStatus> result =
        	new TreeMap<String, SBStatus>();
        
		XmlEntityStruct xml[] = null;
		try {
			xml = stateSystemComp.findSBStatusByState(states);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for (final XmlEntityStruct xes : xml) {
			try {
				final SBStatus sb = (SBStatus) entityDeserializer.
				deserializeEntity(xes, SBStatus.class);
				result.put(sb.getSBStatusEntity().getEntityId(), sb);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (initialise) {
			initialiseExecutionsRemaining(runnableSBSs);
		}
		return result;
	}
	
    private void initialiseExecutionsRemaining(SortedMap<String, SBStatus> runnableSBSs) {
		// TODO Auto-generated method stub
		
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

