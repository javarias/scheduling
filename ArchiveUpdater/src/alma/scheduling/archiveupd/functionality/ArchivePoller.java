/* ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2010
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
 */

package alma.scheduling.archiveupd.functionality;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.SchedulingException;
import alma.scheduling.datamodel.DAOException;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.ModelAccessor;
import alma.scheduling.datamodel.obsproject.dao.ObsProjectDao;
import alma.scheduling.datamodel.obsproject.dao.Phase2XMLStoreProjectDao;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;
import alma.scheduling.utils.ErrorHandling;

/**
 *
 * @author dclarke
 * $Id: ArchivePoller.java,v 1.1 2010/09/03 22:09:11 dclarke Exp $
 */
public class ArchivePoller {

	/*
	 * ================================================================
	 * Fields
	 * ================================================================
	 */
	private SchedBlockDao schedBlockDao;
//	private ObsUnitDao    obsUnitDao;
	private ObsProjectDao obsProjectDao;
	
	private Logger logger;
	private ErrorHandling handler;
	
	private DateTime lastUpdate;
//    private SBQueue                     sbQueue;
//    private ProjectQueue                projectQueue;
//    private StatusEntityQueueBundle     statusQs;
	/* End Fields
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	@SuppressWarnings("unused")
	private ArchivePoller() throws Exception { }
	
	public ArchivePoller(Logger logger) throws Exception {	
		final ModelAccessor ma = new ModelAccessor();

		this.logger = logger;
		this.handler = new ErrorHandling(logger);
		this.schedBlockDao = ma.getSchedBlockDao();
		this.obsProjectDao = ma.getObsProjectDao();
	}
	/* End Construction
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Utilities
	 * ================================================================
	 */
	/**
	 * Delete the ObsProjects referred to in the given collection. Note
	 * that these are ALMA entity ids, not Scheduling database ids.
	 * 
	 * @param deletedIds
	 */
	private int deleteProjects(List<String> deletedIds) {
		final List<ObsProject> projects = new ArrayList<ObsProject>();
		
		for (final String id : deletedIds) {
			final ObsProject op = obsProjectDao.findByEntityId(id);
			if (op != null) {
				projects.add(op);
			}
		}
		
		obsProjectDao.deleteAll(projects);
		return projects.size();
	}
	
	private void logNumbers() {
		final int numOPs = obsProjectDao.countAll();
		final int numSBs = schedBlockDao.countAll();

		logger.info(String.format(
				"Scheduling is managing %d ObsProject%s and %s SchedBlock%s",
				numOPs,
				numOPs==1? "": "s",
				numSBs,
				numSBs==1? "": "s"));
	}
	/* End Utilities
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Initial Polling of the ALMA Archives 
	 * ================================================================
	 */
    /**
     * Get all projects that we're interested in, starting afresh.
     * 
     * @throws SchedulingException
     */
    private void initialPollArchive() {
    	logger.info("Starting initial poll of the archive");
    	
    	Phase2XMLStoreProjectDao inDao;
		try {
			inDao = new Phase2XMLStoreProjectDao();
		} catch (Exception e) {
			handler.severe(String.format(
					"Error creating DAO for ALMA project store - %s",
					e.getMessage()), e);
			return;
		}
    	List<ObsProject> allProjects;
		try {
			allProjects = inDao.getAllObsProjects();
		} catch (DAOException e) {
			handler.severe(String.format(
					"Error getting projects from ALMA project store - %s",
					e.getMessage()), e);
			return;
		}
    	obsProjectDao.saveOrUpdate(allProjects);
    	logger.info(String.format(
    			"%d project%s loaded",
    			allProjects.size(),
    			allProjects.size()==1? "": "s"));
    	logNumbers();
    }
	/* End Initial Polling of the ALMA Archives
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Incremental Polling of the ALMA Archives 
	 * ================================================================
	 */
    /**
     * Work out what relevant changes have happened in the XML Store
     * and the State Archive and then change the Scheduler's pool of
     * Projects and SBs to match.
     * 
     * "Relevant changes" is a strangely complicated thing. As a first
     * stab we'll update any project which:
     * <ul>
     * <li>has a SchedBlock whose XML has changed;
     * <li>has changed XML;
     * <li>has an SBStatus which has changed state;
     * <li>has an OUSStatus which has changed state;
     * <li>has changed its status.
     * </ul>
     * To "update" a project we
     * <ol>
     * <li>unload any current information we are holding on that
     * project;
     * <li>load it from the archives <code>if</code> it is active.
     * </ol>
     * 
     * We also cache SchedBlocks we've just got in order to avoid
     * fetching them multiple times, which would be a real performance
     * hit. Although it might look like we could apply the same sort of
     * caching to SBStatuses and OUSStatuses, the payback for so doing
     * would be very slight as the SBStatuses and OUSStatuses for each
     * ObsProjects are obtained with one CORBA call regardless of how
     * many of them the project has. It is the CORBA calls which are
     * the performance hit.
     * 
     * @param now - timestamp to use for any created objects.
     * @throws SchedulingException
     */
    private void incrementalPollArchive(DateTime since) {
    	logger.info("Starting incremental poll of the archive");
    	
    	Phase2XMLStoreProjectDao inDao;
		try {
			inDao = new Phase2XMLStoreProjectDao();
		} catch (Exception e) {
			handler.severe(String.format(
					"Error creating DAO for ALMA project store - %s",
					e.getMessage()), e);
			return;
		}
    	List<String> newOrModifiedIds = new ArrayList<String>();
    	List<String> deletedIds       = new ArrayList<String>();
		try {
			inDao.getObsProjectChanges(
		    		since,
		    		newOrModifiedIds,
		    		deletedIds);
		} catch (DAOException e) {
			handler.severe(String.format(
					"Error getting new projects from ALMA project store - %s",
					e.getMessage()), e);
			return;
		}
    	List<ObsProject> newProjects;
		try {
			newProjects = inDao.getSomeObsProjects(newOrModifiedIds);
		} catch (DAOException e) {
			handler.severe(String.format(
					"Error getting projects from ALMA project store - %s",
					e.getMessage()), e);
			return;
		}
		final int deleted = deleteProjects(deletedIds);
    	obsProjectDao.saveOrUpdate(newProjects);
    	logger.info(String.format(
    			"%d new or modified project%s, %d project%s removed",
    			newProjects.size(),
    			newProjects.size()==1? "": "s",
    			deleted,
    			deleted==1? "": "s"));
    	logNumbers();
    }
	/* End Incremental Polling of the ALMA Archives
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * External interface 
	 * ================================================================
	 */
    private boolean donePollArchive = false;
    
    /**
     * Reset back to the initial state
     */
    public void reset() {
    	donePollArchive = false;
    	obsProjectDao.deleteAll(
    			obsProjectDao.findAll(ObsProject.class));
    	schedBlockDao.deleteAll(
    			schedBlockDao.findAll(SchedBlock.class));
    }
    
    synchronized public void pollArchive() throws SchedulingException {

		logger.fine("Polling archive for runnable projects");
		final DateTime now = new DateTime(System.currentTimeMillis());
        
        if (!donePollArchive) {
    		initialPollArchive();
    		donePollArchive = true;
    	} else {
    		incrementalPollArchive(lastUpdate);
    	}

        lastUpdate = now;
        
//        logNumbers(String.format("at end of pollArchive(%s)", prjuid));
//        logDetails(String.format("at end of pollArchive(%s)", prjuid));
    }

	/* End External interface
	 * ============================================================= */
}
