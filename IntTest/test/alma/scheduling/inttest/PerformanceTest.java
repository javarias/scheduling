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
 * $Id: PerformanceTest.java,v 1.2 2009/11/09 23:13:27 rhiriart Exp $
 */

package alma.scheduling.inttest;

import java.util.SortedMap;
import java.util.TreeMap;
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
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.entity.xmlbinding.schedblock.SchedBlock;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.projectlifecycle.StateSystem;
import alma.scheduling.AlmaScheduling.ALMAArchive;
import alma.scheduling.AlmaScheduling.ALMAClock;
import alma.scheduling.Define.SB;
import alma.xmlentity.XmlEntityStruct;
import alma.xmlstore.ArchiveConnection;
import alma.xmlstore.ArchiveInternalError;
import alma.xmlstore.Cursor;
import alma.xmlstore.Identifier;
import alma.xmlstore.Operational;
import alma.xmlstore.CursorPackage.QueryResult;

/**
 * Template for Scheduling tests.
 * 
 */
public class PerformanceTest extends ComponentClientTestCase {


	private ContainerServices container;
    private Logger logger;
    private Simulator simulator;
//    private Utils utils;

    private EntityDeserializer entityDeserializer;
    private EntitySerializer entitySerializer;

    private ArchiveConnection archConnectionComp;
    private Operational archOperational;
    private Identifier archIdentifierComp;

    private StateSystem stateSystemComp;

    private MasterComponent schedulingMC;
//    private MasterSchedulerIF masterScheduler;

    public PerformanceTest() throws Exception {
        super(PerformanceTest.class.getName());
    }

    /**
     * Test case fixture setup.
     */
    protected void setUp() throws Exception {
        super.setUp();

        container = getContainerServices();
        logger = container.getLogger();
//        utils = new Utils(container, logger);

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
    
    public void testAndReportTimes() throws Exception {
    	final ALMAArchive a = new ALMAArchive(container, new ALMAClock());
    	final long t0 = System.currentTimeMillis();
   		final SortedMap<String, SBStatus> statuses = getAllSBStatuses();
    	final long t1 = System.currentTimeMillis();
   		final SortedMap<String, SchedBlock> domains = getAllSchedBlocks();
    	final long t2 = System.currentTimeMillis();
   		final SortedMap<String, SchedBlock> d2 = getAllSchedBlocks(statuses);
    	final long t3 = System.currentTimeMillis();
   		final SB[] sbArray = a.getAllSB();
    	final long t4 = System.currentTimeMillis();
 		
    	logger.info(String.format("got %d SBStatus%s in %d milliseconds",
    			statuses.size(),
    			(statuses.size() == 1)? "": "es",
    			t1-t0));
    	logger.info(String.format("got %d SchedBlock%s in %d milliseconds",
    			domains.size(),
    			(domains.size() == 1)? "": "s",
    			t2-t1));
    	logger.info(String.format("got %d SchedBlock%s in %d milliseconds",
    			d2.size(),
    			(d2.size() == 1)? "": "s",
    			t3-t2));
    	logger.info(String.format("got %d SchedBlock%s in %d milliseconds",
    			sbArray.length,
    			(sbArray.length == 1)? "": "s",
    			t4-t3));
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
     * Get all the SchedBlocks that are in the state archive .
     * 
     * @return a map from EntityId to SchedBlock containing all the
     *         SchedBlock entities found
     * 
     * @throws ArchiveInternalError
     */
    private SortedMap<String, SchedBlock> getAllSchedBlocks(SortedMap<String, SBStatus> statuses)
    throws ArchiveInternalError {
    	
        final SortedMap<String, SchedBlock> result =
    		new TreeMap<String, SchedBlock>();

        for (final SBStatus sbs : statuses.values()) {
        	try {
        		final String sbId = sbs.getSchedBlockRef().getEntityId();
        		final XmlEntityStruct xml = archOperational.retrieve(sbId);

        		SchedBlock sb = (SchedBlock)
        			entityDeserializer.deserializeEntity(xml, SchedBlock.class);
        		result.put(sbId, sb);
        	}catch(Exception e) {
        		logger.severe("SCHEDULING: " + e.toString());
        		e.printStackTrace(System.out);
        	}
    	}
    	return result;
    }

	/**
     * Get all the SchedBlocks that are in the archive .
     * 
     * @return a map from EntityId to SchedBlock containing all the
     *         SchedBlock entities found
     * 
     * @throws ArchiveInternalError
     */
    private SortedMap<String, SchedBlock> getAllSchedBlocks()
    throws ArchiveInternalError {
        final String query = new String("/sbl:ScheduBlock");
        final String schema = new String("SchedBlock");
        final SortedMap<String, SchedBlock> result =
    		new TreeMap<String, SchedBlock>();

        Cursor cursor = archOperational.query(query, schema);
        if(cursor == null) {
        	logger.severe("SCHEDULING: cursor was null when querying SchedBlocks");
        	return null;
        }

        while(cursor.hasNext()) {
        	QueryResult res = cursor.next();
        	try {
            	logger.info(String.format("SCHEDULING: looking for SB %s", res.identifier));
        		final XmlEntityStruct xml = archOperational.retrieve(res.identifier);

        		SchedBlock sb = (SchedBlock)
        		entityDeserializer.deserializeEntity(xml, SchedBlock.class);
        		result.put(res.identifier, sb);
        	}catch(Exception e) {
        		logger.severe("SCHEDULING: " + e.toString());
        		e.printStackTrace(System.out);
        	}
        }
        cursor.close();

    	return result;
    }

	/**
     * Get all the SB statuses that are in the state archive .
     * 
     * @return a map from EntityId to SBStatus containing all the
     *         SBStatus entities found
     * 
     * @throws ArchiveInternalError
     */
    private SortedMap<String, SBStatus> getAllSBStatuses()
    throws ArchiveInternalError {
    	final SortedMap<String, SBStatus> result =
    		new TreeMap<String, SBStatus>();

    	final String[] states = { StatusTStateType.ANYSTATE.toString() };

    	XmlEntityStruct xml[] = null;
    	try {
    		xml = stateSystemComp.findSBStatusByState(states);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

    	for (final XmlEntityStruct xes : xml) {
    		try {
    			final SBStatus sbs = (SBStatus) entityDeserializer.
    			deserializeEntity(xes, SBStatus.class);
    			result.put(sbs.getSBStatusEntity().getEntityId(), sbs);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}

    	return result;
    }
}

