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
 * $Id: FakeQA0.java,v 1.3 2011/03/02 16:43:25 javarias Exp $
 */

package alma.scheduling.inttest;

import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;

import org.omg.CORBA.UserException;

import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.component.client.ComponentClientTestCase;
import alma.acs.container.ContainerServices;
import alma.acs.entityutil.EntityDeserializer;
import alma.entity.xmlbinding.sbstatus.ExecStatusT;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.entity.xmlbinding.schedblock.SchedBlock;
import alma.entity.xmlbinding.schedblock.SchedBlockControlT;
import alma.entity.xmlbinding.schedblock.SchedBlockRefT;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.lifecycle.stateengine.constants.Role;
import alma.lifecycle.stateengine.constants.Subsystem;
import alma.projectlifecycle.StateSystem;
import alma.projectlifecycle.StateSystemHelper;
import alma.scheduling.SchedulingException;
import alma.xmlentity.XmlEntityStruct;
import alma.xmlstore.ArchiveConnection;
import alma.xmlstore.Identifier;
import alma.xmlstore.Operational;

/**
 * Template for Scheduling tests.
 * 
 */
public class FakeQA0 extends ComponentClientTestCase {

	final static StatusTStateType readyState     = StatusTStateType.READY;
	final static StatusTStateType suspendedState = StatusTStateType.SUSPENDED;
	final static StatusTStateType completeState  = StatusTStateType.FULLYOBSERVED;
	final static String readyStateString     = readyState.toString();
	final static String suspendedStateString = suspendedState.toString();
	final static String completeStateString  = completeState.toString();

	private ContainerServices container;
    private Logger logger;

    private ArchiveConnection archConnectionComp;
    private Operational archOperational;
    @SuppressWarnings("unused")
	private Identifier archIdentifierComp;
    private StateSystem stateSystemComp;

    private EntityDeserializer entityDeserializer;
    
    private boolean moveToFullyObserved = (System.getenv("FULLYOBSERVED") != null) ||
    									  (System.getenv("FULLY_OBSERVED") != null);

    public FakeQA0() throws Exception {
        super(FakeQA0.class.getName());
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

        archConnectionComp = alma.xmlstore.ArchiveConnectionHelper.narrow(
                container.getComponent("ARCHIVE_CONNECTION"));
        
        archIdentifierComp = alma.xmlstore.IdentifierHelper.narrow(
                container.getComponent("ARCHIVE_IDENTIFIER"));

        archOperational = archConnectionComp.getOperational("ObservationTest");
        assertNotNull(archOperational);

        getStateSystemComponent();
        assertNotNull(stateSystemComp);

        entityDeserializer = EntityDeserializer.getEntityDeserializer(
                container.getLogger());
        
        logger.fine(String.format("will %smove suitable projects to %s",
        		moveToFullyObserved? "": "NOT ",
        				completeStateString));
        unsuspendSBs();

    }

    
    private void unsuspendSBs() throws SchedulingException {
    	final String[] fromStates = { suspendedStateString };
    	final Collection<SBStatus> fromSBs = getSBStatusesByState(fromStates);
    	changeStates(fromSBs);
	}


	/**
	 * Get all the SB statuses that are in the state archive in a given
	 * set of states.
	 * 
	 * @param states - we are interested in SBStatuses in any of
	 *                these states.
	 *                
	 * @return a Collection<SBStatus> containing the entities that were
	 *         found.
	 */
	private Collection<SBStatus> getSBStatusesByState(String[] states) {
        final Collection<SBStatus> result = new HashSet<SBStatus>();
        
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
				result.add(sbs);
			} catch (Exception e) {
	        	logger.finest("Scheduling can not deserialise SBStatus from State System");
	            e.printStackTrace(System.out);
			}
		}
		
		return result;
	}

    private boolean moreExecutionsRequired(SBStatus sbStatus) {
    	final SchedBlock sb = getSB(sbStatus.getSchedBlockRef());
    	final SchedBlockControlT sbc = sb.getSchedBlockControl();
    	
        if (sbc.getIndefiniteRepeat()) {
        	return true;
        }
    	
        final int max = sbc.getExecutionCount();
        
    	int successfulRuns = 0;  // story of my life, mate... :-<
    	for (final ExecStatusT eStatus : sbStatus.getExecStatus()) {
    		if (eStatus.getStatus().getState() == completeState) {
    			successfulRuns ++;
    			if (successfulRuns == max) {
    				return false;
    			}
    		}
    	}
		return true;
	}
    
	private SchedBlock getSB(SchedBlockRefT schedBlockRef) {
		final String sbID = schedBlockRef.getEntityId();
		SchedBlock sb = null;
		try {
			XmlEntityStruct xml = archOperational.retrieve(sbID);
			sb = (SchedBlock) entityDeserializer.deserializeEntity(xml, SchedBlock.class);
		} catch (Exception e) {
			logger.severe(String.format(
					"Error deserialising SchedBlock %s - %s.",
					sbID, e.getLocalizedMessage()));
			return null;
		}
		return sb;
	}

	private void changeStates(Collection<SBStatus> fromSBs)
						throws SchedulingException {
		int ready    = 0;
		int complete = 0;
    	int failed   = 0;
    	int skipped  = 0;

    	for (final SBStatus sb : fromSBs) {
			final String statusID = sb.getSBStatusEntity().getEntityId();
			final String domainID = sb.getSchedBlockRef().getEntityId();

    		if (moreExecutionsRequired(sb)) {
    			try {
    				stateSystemComp.changeSBStatus(
    						statusID,
    						readyStateString,
    						Subsystem.SCHEDULING,
    						Role.AOD);
    				logger.info(String.format(
    						"Converted SBStatus %s (for SchedBlock %s) to %s.",
    						statusID, domainID, readyStateString));
    				ready ++;
    			} catch (UserException e) {
    				logger.warning(String.format(
    						"cannot convert SBStatus %s (for SchedBlock %s) to %s - %s.",
    						statusID, domainID, readyStateString,
    						e.getLocalizedMessage()));
    				failed ++;
    			}
    		} else if (moveToFullyObserved) {
    			try {
    				stateSystemComp.changeSBStatus(
    						statusID,
    						completeStateString,
    						Subsystem.SCHEDULING,
    						Role.AOD);
    				logger.info(String.format(
    						"Converted SBStatus %s (for SchedBlock %s) to %s.",
    						statusID, domainID, completeStateString));
    				complete ++;
    			} catch (UserException e) {
    				logger.warning(String.format(
    						"cannot convert SBStatus %s (for SchedBlock %s) to %s - %s.",
    						statusID, domainID, readyStateString,
    						e.getLocalizedMessage()));
    				failed ++;
    			}
    		} else {
				logger.info(String.format(
						"Skipping conversion of SBStatus %s (for SchedBlock %s) to %s - no more executions are required.",
						statusID, domainID, readyStateString));
    			skipped ++;
    		}
    	}

    	logger.info(String.format(
    			"conversion of SB statuses: %s candidate SchedBlock%s found, %d converted to %s, %d converted to %s, %d failed, %d skipped.",
    			fromSBs.size(), (fromSBs.size()==1)? "": "s",
    					ready,    readyStateString,
    					complete, completeStateString,
    					failed, skipped));
	}

	public void testSomething() throws Exception {
    }    

}

