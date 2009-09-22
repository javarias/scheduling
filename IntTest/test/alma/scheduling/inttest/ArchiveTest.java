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
 * $Id: ArchiveTest.java,v 1.2 2009/09/22 22:40:17 rhiriart Exp $
 */

package alma.scheduling.inttest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import alma.ACS.MasterComponent;
import alma.ACS.MasterComponentHelper;
import alma.ACS.ROstringSeq;
import alma.ACS.MasterComponentPackage.SubsystemStateEvent;
import alma.ACSErr.Completion;
import alma.ACSErr.CompletionHolder;
import alma.ACSSim.Simulator;
import alma.ACSSim.SimulatorHelper;
import alma.acs.component.client.ComponentClientTestCase;
import alma.acs.container.ContainerServices;
import alma.acs.entityutil.EntityDeserializer;
import alma.acs.entityutil.EntitySerializer;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.scheduling.MasterSchedulerIF;
import alma.xmlentity.XmlEntityStruct;
import alma.xmlstore.ArchiveConnection;
import alma.xmlstore.ArchiveInternalError;
import alma.xmlstore.Cursor;
import alma.xmlstore.Identifier;
import alma.xmlstore.Operational;
import alma.xmlstore.CursorPackage.QueryResult;
import alma.xmlstore.OperationalPackage.DirtyEntity;

/**
 * Template for Scheduling tests.
 * 
 */
public class ArchiveTest extends ComponentClientTestCase {

	private ContainerServices container;
    private Logger logger;
    private Simulator simulator;
    private Utils utils;

    private EntityDeserializer entityDeserializer;
    private EntitySerializer entitySerializer;

    private ArchiveConnection archConnectionComp;
    private Operational archOperational;
    private Identifier archIdentifierComp;
    
    private MasterComponent schedulingMC;
    private MasterSchedulerIF masterScheduler;
    
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

    private SortedSet<ProjectStatus> makePSSet() {
    	return new TreeSet<ProjectStatus>(
    			new Comparator<ProjectStatus>(){
    				public int compare(ProjectStatus p1, ProjectStatus p2) {
    					final String u1 = p1.getProjectStatusEntity().getEntityId();
    					final String u2 = p2.getProjectStatusEntity().getEntityId();
    					return u1.compareTo(u2);
    				}});
    }

    private SortedSet<SBStatus> makeSBSSet() {
    	return new TreeSet<SBStatus>(
    			new Comparator<SBStatus>(){
    				public int compare(SBStatus p1, SBStatus p2) {
    					final String u1 = p1.getSBStatusEntity().getEntityId();
    					final String u2 = p2.getSBStatusEntity().getEntityId();
    					return u1.compareTo(u2);
    				}});
    }
    
    public void testSomething() throws Exception {
    	SortedSet<ProjectStatus> allPS = makePSSet();
    	SortedSet<ProjectStatus> runPS = makePSSet();
    	SortedSet<SBStatus> allSBS = makeSBSSet();
    	SortedSet<SBStatus> runSBS = makeSBSSet();
    	
    	allPS.addAll(getAllProjectStatuses());
    	runPS.addAll(getRunnableProjectStatuses());
    	allSBS.addAll(getAllSBStatuses());
    	runSBS.addAll(getRunnableSBStatuses());
        
        logger.fine(showPS(allPS, runPS));
        logger.fine(showSBS(allSBS, runSBS));
    	
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

    private String showPS(SortedSet<ProjectStatus> allPS, SortedSet<ProjectStatus> runPS) {
    	final StringBuilder b = new StringBuilder();
    	final Formatter     f = new Formatter(b);
    	
    	final Iterator<ProjectStatus> runIt = runPS.iterator();
    	ProjectStatus nextRun = null;
    	String        nextId  = null;
    	
    	if (runIt.hasNext()) {
    		nextRun = runIt.next();
    		nextId  = nextRun.getProjectStatusEntity().getEntityId();
    	}
    	
    	f.format("%nAll Project Statuses%n");
    	
    	for (final ProjectStatus ps : allPS) {
    		final String thisId = ps.getProjectStatusEntity().getEntityId();
    		f.format("<ProjectStatus status=%s id=%s/>",
    				ps.getStatus().getState(),
    				thisId);
    		if (nextId == thisId) {
    			b.append(" - in runnable set");
    	    	if (runIt.hasNext()) {
    	    		nextRun = runIt.next();
    	    		nextId  = nextRun.getProjectStatusEntity().getEntityId();
    	    	} else {
    	    		nextRun = null;
    	    		nextId  = null;
    	    	}
    		}
    		f.format("%n");
    	}
    	
    	if (nextRun != null) {
    		f.format("Unbalanced :-(%n");
    		f.format("\t\t<ProjectStatus status=%s id=%s/>",
    				nextRun.getStatus().getState(),
    				nextId);
    		while (runIt.hasNext()) {
	    		nextRun = runIt.next();
	    		nextId  = nextRun.getProjectStatusEntity().getEntityId();
        		f.format("\t\t<ProjectStatus status=%s id=%s/>",
        				nextRun.getStatus().getState(),
        				nextId);
    		}
    	} else {
        	f.format("Balanced :-)%n");
    	}

    	return b.toString();
	}

    private String showSBS(SortedSet<SBStatus> allSBS, SortedSet<SBStatus> runSBS) {
    	final StringBuilder b = new StringBuilder();
    	final Formatter     f = new Formatter(b);
    	
    	final Iterator<SBStatus> runIt = runSBS.iterator();
    	SBStatus nextRun = null;
    	String        nextId  = null;
    	
    	if (runIt.hasNext()) {
    		nextRun = runIt.next();
    		nextId  = nextRun.getSBStatusEntity().getEntityId();
    	}
    	
    	f.format("%nAll SB Statuses%n");
    	
    	for (final SBStatus ps : allSBS) {
    		final String thisId = ps.getSBStatusEntity().getEntityId();
    		f.format("<SBStatus status=%s id=%s/>",
    				ps.getStatus().getState(),
    				thisId);
    		if (nextId == thisId) {
    			b.append(" - in runnable set");
    	    	if (runIt.hasNext()) {
    	    		nextRun = runIt.next();
    	    		nextId  = nextRun.getSBStatusEntity().getEntityId();
    	    	} else {
    	    		nextRun = null;
    	    		nextId  = null;
    	    	}
    		}
    		f.format("%n");
    	}
    	
    	if (nextRun != null) {
    		f.format("Unbalanced :-(%n");
    		f.format("\t\t<SBStatus status=%s id=%s/>",
    				nextRun.getStatus().getState(),
    				nextId);
    		while (runIt.hasNext()) {
	    		nextRun = runIt.next();
	    		nextId  = nextRun.getSBStatusEntity().getEntityId();
        		f.format("\t\t<SBStatus status=%s id=%s/>",
        				nextRun.getStatus().getState(),
        				nextId);
    		}
    	} else {
        	f.format("Balanced :-)%n");
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
     * Get all the project statuses that are in the archive
     * 
     * @return a List of ProjectStatus objects retrieved from the archive
     * @throws ArchiveInternalError 
     */
    private List<ProjectStatus> getAllProjectStatuses() throws ArchiveInternalError {
    	final List<ProjectStatus> result = new ArrayList<ProjectStatus>();
    	
        final String schema = new String("ProjectStatus");
        final String query  = new String("/ps:ProjectStatus");

        final Cursor cursor = archOperational.query(query, schema);
        while (cursor.hasNext()) {
            final QueryResult res = cursor.next();
            try {
				final XmlEntityStruct xml = archOperational.retrieve(res.identifier);
	            final ProjectStatus ps = (ProjectStatus)
	            	entityDeserializer.deserializeEntity(xml, ProjectStatus.class);
	            result.add(ps);
			} catch (DirtyEntity e) {
				logger.info(
						String.format(
								"Skipping ProjectStatus %s, entity is dirty",
								res.identifier));
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
        }

        return result;
    }
    
    /**
     * Get all the project statuses that are in the archive
     * 
     * @return a List of ProjectStatus objects retrieved from the archive
     * @throws ArchiveInternalError 
     */
    private List<ProjectStatus> getRunnableProjectStatuses() throws ArchiveInternalError {
    	final List<ProjectStatus> result = new ArrayList<ProjectStatus>();
    	
        final String schema = new String("ProjectStatus");
        final String query  = String.format(
        		"/ps:ProjectStatus[ps:Status/@State=\"%s\" or ps:Status/@State=\"%s\"]",
        		StatusTStateType.READY,
        		StatusTStateType.PARTIALLYOBSERVED);

        final Cursor cursor = archOperational.query(query, schema);
        while (cursor.hasNext()) {
            final QueryResult res = cursor.next();
            try {
				final XmlEntityStruct xml = archOperational.retrieve(res.identifier);
	            final ProjectStatus ps = (ProjectStatus)
	            	entityDeserializer.deserializeEntity(xml, ProjectStatus.class);
	            result.add(ps);
			} catch (DirtyEntity e) {
				logger.info(
						String.format(
								"Skipping ProjectStatus %s, entity is dirty",
								res.identifier));
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
        }

        return result;
    }
    
    /**
     * Get all the SB statuses that are in the archive
     * 
     * @return a List of SBStatus objects retrieved from the archive
     * @throws ArchiveInternalError 
     */
    private List<SBStatus> getAllSBStatuses() throws ArchiveInternalError {
    	final List<SBStatus> result = new ArrayList<SBStatus>();
    	
        final String schema = new String("SBStatus");
        final String query  = new String("/sbs:SBStatus");

        final Cursor cursor = archOperational.query(query, schema);
        while (cursor.hasNext()) {
            final QueryResult res = cursor.next();
            try {
				final XmlEntityStruct xml = archOperational.retrieve(res.identifier);
	            final SBStatus ps = (SBStatus)
	            	entityDeserializer.deserializeEntity(xml, SBStatus.class);
	            result.add(ps);
			} catch (DirtyEntity e) {
				logger.info(
						String.format(
								"Skipping SBStatus %s, entity is dirty",
								res.identifier));
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
        }

        return result;
    }
    
    /**
     * Get all the SB statuses that are in the archive
     * 
     * @return a List of SBStatus objects retrieved from the archive
     * @throws ArchiveInternalError 
     */
    private List<SBStatus> getRunnableSBStatuses() throws ArchiveInternalError {
    	final List<SBStatus> result = new ArrayList<SBStatus>();
    	
        final String schema = new String("SBStatus");
        final String query  = String.format(
        		"/sbs:SBStatus[sbs:Status/@State=\"%s\" or sbs:Status/@State=\"%s\"]",
        		StatusTStateType.READY,
        		StatusTStateType.PARTIALLYOBSERVED);

        final Cursor cursor = archOperational.query(query, schema);
        while (cursor.hasNext()) {
            final QueryResult res = cursor.next();
            try {
				final XmlEntityStruct xml = archOperational.retrieve(res.identifier);
	            final SBStatus ps = (SBStatus)
	            	entityDeserializer.deserializeEntity(xml, SBStatus.class);
	            result.add(ps);
			} catch (DirtyEntity e) {
				logger.info(
						String.format(
								"Skipping SBStatus %s, entity is dirty",
								res.identifier));
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
        }

        return result;
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

