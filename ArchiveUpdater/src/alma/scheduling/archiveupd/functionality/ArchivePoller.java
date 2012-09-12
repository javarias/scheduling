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
 * $Id: ArchivePoller.java,v 1.17 2012/09/12 16:46:16 javarias Exp $
 */

package alma.scheduling.archiveupd.functionality;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.logging.Logger;

import alma.acs.container.ContainerServices;
import alma.entity.xmlbinding.obsproposal.types.InvestigatorTAssociatedExecType;
import alma.scheduling.ArchiveImportEvent;
import alma.scheduling.ArchiveUpdaterCallback;
import alma.scheduling.SchedulingException;
import alma.scheduling.dataload.AtmDataLoader;
import alma.scheduling.datamodel.DAOException;
import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.ExecutivePercentage;
import alma.scheduling.datamodel.executive.ObservingSeason;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnit;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.ModelAccessor;
import alma.scheduling.datamodel.obsproject.dao.ObsProjectDao;
import alma.scheduling.datamodel.obsproject.dao.Phase2XMLStoreProjectDao;
import alma.scheduling.datamodel.obsproject.dao.ProjectImportEvent;
import alma.scheduling.datamodel.obsproject.dao.ProjectImportEvent.ImportStatus;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;
import alma.scheduling.datamodel.weather.dao.AtmParametersDao;
import alma.scheduling.utils.CommonContextFactory;
import alma.scheduling.utils.DSAContextFactory;
import alma.scheduling.utils.ErrorHandling;

/**
 *
 * @author dclarke
 * $Id: ArchivePoller.java,v 1.17 2012/09/12 16:46:16 javarias Exp $
 */
public class ArchivePoller implements Observer{

	/*
	 * ================================================================
	 * Fields
	 * ================================================================
	 */
	private SchedBlockDao schedBlockDao;
//	private ObsUnitDao    obsUnitDao;
	private ExecutiveDAO execDao;
	private ObsProjectDao obsProjectDao;
	private AtmParametersDao atmDao;
	
	private Logger logger;
	private ErrorHandling handler;
	
	private ContainerServices containerServices;
	
	private boolean pollerBusy = false;
	
	private ConfigurationDao configDao;
	
	private HashMap<String, ArchiveUpdaterCallback> callbacks 
		= new HashMap<String, ArchiveUpdaterCallback>();
	/* End Fields
	 * ============================================================= */

	
	
	
	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	
	public ArchivePoller(Logger logger, ContainerServices containerServices) throws Exception {	
		final ModelAccessor ma = new ModelAccessor();

		this.containerServices = containerServices;
		this.logger = logger;
        if (!ErrorHandling.isInitialized())
        	ErrorHandling.initialize(logger);
		this.handler = ErrorHandling.getInstance();
		this.schedBlockDao = ma.getSchedBlockDao();
		this.obsProjectDao = ma.getObsProjectDao();
		this.execDao = ma.getExecutiveDao();
		this.atmDao = ma.getAtmDao();
		this.pollerBusy = false;
		configDao = (ConfigurationDao) DSAContextFactory
				.getContextFromPropertyFile()
				.getBean(CommonContextFactory.SCHEDULING_CONFIGURATION_DAO_BEAN);
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
			} else {
				logger.warning(String.format(
						"Cannot find project %s to delete it", id));
			}
		}
		
		obsProjectDao.deleteAll(projects);
		return projects.size();
	}
	
	/**
	 * Log the numbers of Projects and SBs we've got.
	 */
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
	
    /**
     * Report that we're busy.
     */
    private void reportBusy() {
    	String message = "The ArchiveUpdater is currently busy, and thus is ignoring the new request";
    	
    	Phase2XMLStoreProjectDao inDao;
		try {
			inDao = new Phase2XMLStoreProjectDao(containerServices);
			inDao.getNotifer().addObserver(this);
			ProjectImportEvent event = new ProjectImportEvent();
			event.setEntityId(message);
			event.setTimestamp(new Date());
			event.setStatus(ImportStatus.STATUS_INFO);
			event.setEntityType("<html><i>none</i></html>");
			event.setDetails("");
			inDao.getNotifer().notifyEvent(event);
		} catch (Exception e) {
			// Do nothing. We're only using this dao to report that we're
			// not doing anything. It might have sorted itself out when we
			// really need it, and if not the issue will be reported then
			// anyway.
		}
		logger.info(message);
    }

	/**
	 * Determine when the last refresh took place. We use a result of
	 * <code>null</code> to denote that there has never been a refresh
	 * (or that we don't know when it was). If <code>forceRefresh</code>
	 * is <code>true</code>, then pretend there has never been a refresh
	 * and return <code>null</code> to force a full refresh.
	 * 
	 * @param forceRefresh
	 * @return
	 */
	private Date lastRefresh(boolean forceRefresh) {
		if (forceRefresh) {
			return null;
		}

		logger.fine("Checking for last update in the Scheduling Working DB");
		Date savedTime = configDao.getConfiguration().getLastLoad();
		logger.fine("Last time saved in the Scheduling Working DB is: " + savedTime);
		
		if (savedTime != null) {
			logger.fine("Restoring saved time as last update and doing an incremental polling after that");
			return new Date(savedTime.getTime());
		} else {
			logger.fine("Ignoring saved time in Scheduling Working DB because it is null");
			return null;
		}
	}
	/* End Utilities
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Initial Polling of the ALMA Archives 
	 * ================================================================
	 */
    /**
     * Reset back to the initial state
     */
    private void reset() {
    	logger.info("Resetting scheduling working database");
    	obsProjectDao.deleteAll();
//    	schedBlockDao.deleteAll(
//    			schedBlockDao.findAll(SchedBlock.class));
//    	execDao.deleteAll(execDao.findAll(ExecutivePercentage.class));
//    	execDao.deleteAll(execDao.findAll(ExecutiveTimeSpent.class));
//    	execDao.deleteAll(execDao.findAll(ObservingSeason.class));
//    	execDao.deleteAll(execDao.findAll(Executive.class));
//    	atmDao.deleteAll(atmDao.findAll(AtmParameters.class));
    }
    
    /**
     * Get all projects that we're interested in, starting afresh.
     * 
     * @throws SchedulingException
     */
    private void initialPollArchive() {
    	logger.info("Starting initial poll of the archive");
    	
    	fillAtmData();
    	
    	createExecutives();
    	
    	Phase2XMLStoreProjectDao inDao;
		try {
			inDao = new Phase2XMLStoreProjectDao(containerServices);
			inDao.getNotifer().addObserver(this);
			ProjectImportEvent event = new ProjectImportEvent();
			event.setEntityId("Starting Initial Poll Archive");
			event.setTimestamp(new Date());
			event.setStatus(ImportStatus.STATUS_INFO);
			event.setEntityType("<html><i>none</i></html>");
			event.setDetails("");
			inDao.getNotifer().notifyEvent(event);
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
		for(ObsProject prj: allProjects)
			linkData(prj);
		if (allProjects.size() > 0) {
			ProjectImportEvent event = new ProjectImportEvent();
			event.setEntityId("Saving Converted Projects to SWDB");
			event.setTimestamp(new Date());
			event.setStatus(ImportStatus.STATUS_INFO);
			event.setEntityType("<html><i>none</i></html>");
			event.setDetails("About to save: " + allProjects.size()
					+ " Projects");
			inDao.getNotifer().notifyEvent(event);
		};
		obsProjectDao.saveOrUpdate(allProjects);
    	logger.info(String.format(
    			"%d project%s loaded",
    			allProjects.size(),
    			allProjects.size()==1? "": "s"));
    	logNumbers();
    	ProjectImportEvent event = new ProjectImportEvent();
		event.setEntityId("Completing Initial Poll Archive");
		event.setTimestamp(new Date());
		event.setStatus(ImportStatus.STATUS_INFO);
		event.setEntityType("<html><i>none</i></html>");
		event.setDetails("");
		inDao.getNotifer().notifyEvent(event);
		inDao.tidyUp();
    }
    
	private void deleteExistingProjectsInSWDB(List<ObsProject> prjs) {
		for (ObsProject prj : prjs) {
			logger.finer("Checking project with entity ID: " + prj.getUid());
			ObsProject ret = obsProjectDao.findByEntityId(prj.getUid());
			if (ret != null) {
				logger.finer("Deleting project with entity ID: " + prj.getUid());
				obsProjectDao.delete(prj.getObsUnit());
				obsProjectDao.delete(ret);
			}
		}
	}
    
    private void createExecutives(){
    	//Names retrieved from ObsProposal.xsd
    	//String [] executiveNames = {"NONALMA","OTHER", "CL", "CHILE", "EA", "EU", "NA"};
    	ArrayList<String> executiveNames = new ArrayList<String>();
		@SuppressWarnings("rawtypes")
		Enumeration e = InvestigatorTAssociatedExecType.enumerate();
		while(e.hasMoreElements()) {
			InvestigatorTAssociatedExecType apdmExec = (InvestigatorTAssociatedExecType) e.nextElement();
			executiveNames.add(apdmExec.toString());
		}
    	List<Executive> execs = new ArrayList<Executive>();
    	List<ObservingSeason> seasons = new ArrayList<ObservingSeason>();
    	Set<ExecutivePercentage> eps = new HashSet<ExecutivePercentage>();
		ObservingSeason season = new ObservingSeason();
		season.setName("Current Observing Season");
		season.setStartDate(new Date());
		season.setEndDate(new Date(System.currentTimeMillis() + 315360000 ));
		seasons.add(season);
    	for (String execName: executiveNames){
    		Executive exec =  new Executive();
    		exec.setName(execName);
    		//All executives will have the same percentage
    		//TODO: Change this: each executive has different percentages 
    		double percentage = 0.0;
    		if (executiveNames.size() > 0)
    				percentage = 1.0F/(float)executiveNames.size();
    		exec.setDefaultPercentage((float)percentage);
    		execs.add(exec);
    		ExecutivePercentage ep = new ExecutivePercentage();
    		ep.setExecutive(exec);
    		ep.setSeason(season);
    		ep.setPercentage((float)percentage);
    		ep.setTotalObsTimeForSeason(315360000 * percentage);
    		eps.add(ep);
    	}
    	season.setExecutivePercentage(eps);
    	execDao.saveObservingSeasonsAndExecutives(seasons, execs);
    }
    
    private void linkData(ObsProject proj){
    	List<Executive> executives = execDao.getAllExecutive();
    	Executive executiveSelected = null;
		logger.finer("Look for executive: " + proj.getAffiliation());
		for(Executive exec: executives)
			if(exec.getName().compareTo(proj.getAffiliation()) == 0){
				logger.finer("Executive found. Updating references into the SchedBlocks");
				executiveSelected = exec;
				break;
			}
		linkData(proj.getObsUnit(), executiveSelected, proj);
    }
    
    private void linkData(ObsUnit ou, Executive exec, ObsProject proj){
    	if (ou instanceof ObsUnitSet){
    		ObsUnitSet ous = (ObsUnitSet) ou;
    		for (ObsUnit tmp: ous.getObsUnits())
    			linkData(tmp, exec, proj);
    	}
    	else if (ou instanceof SchedBlock){
    		SchedBlock sb = (SchedBlock) ou;
    		sb.setExecutive(exec);
    		sb.setCsv(proj.getCsv());
    		sb.setManual(proj.getManual());
    		sb.setLetterGrade(proj.getLetterGrade());
    		sb.setScienceRank(proj.getScienceRank());
    	}
    }
    
    private void fillAtmData() {
    	AtmDataLoader loader1 = new AtmDataLoader();
    	loader1.setDao(atmDao);
    	loader1.setFile("classpath:config/otData/SKY.SPE0001.trim");
    	loader1.setMaxNumRecords(-1);
    	loader1.setPwc(0.4722);
    	
    	AtmDataLoader loader2 = new AtmDataLoader();
    	loader2.setDao(atmDao);
    	loader2.setFile("classpath:config/otData/SKY.SPE0002.trim");
    	loader2.setMaxNumRecords(-1);
    	loader2.setPwc(0.658);
    	
    	AtmDataLoader loader3 = new AtmDataLoader();
    	loader3.setDao(atmDao);
    	loader3.setFile("classpath:config/otData/SKY.SPE0003.trim");
    	loader3.setMaxNumRecords(-1);
    	loader3.setPwc(0.9134);
    	
    	AtmDataLoader loader4 = new AtmDataLoader();
    	loader4.setDao(atmDao);
    	loader4.setFile("classpath:config/otData/SKY.SPE0004.trim");
    	loader4.setMaxNumRecords(-1);
    	loader4.setPwc(1.262);
    	
    	AtmDataLoader loader5 = new AtmDataLoader();
    	loader5.setDao(atmDao);
    	loader5.setFile("classpath:config/otData/SKY.SPE0005.trim");
    	loader5.setMaxNumRecords(-1);
    	loader5.setPwc(1.796);
    	
    	AtmDataLoader loader6 = new AtmDataLoader();
    	loader6.setDao(atmDao);
    	loader6.setFile("classpath:config/otData/SKY.SPE0006.trim");
    	loader6.setMaxNumRecords(-1);
    	loader6.setPwc(2.748);
    	
    	AtmDataLoader loader7 = new AtmDataLoader();
    	loader7.setDao(atmDao);
    	loader7.setFile("classpath:config/otData/SKY.SPE0007.trim");
    	loader7.setMaxNumRecords(-1);
    	loader7.setPwc(5.186);
    	
    	loader1.load();
    	loader2.load();
    	loader3.load();
    	loader4.load();
    	loader5.load();
    	loader6.load();
    	loader7.load();
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
    private void incrementalPollArchive(Date since) {
    	logger.info("Starting incremental poll of the archive");
    	
    	Phase2XMLStoreProjectDao inDao;
		try {
			inDao = new Phase2XMLStoreProjectDao(containerServices);
			inDao.getNotifer().addObserver(this);
			ProjectImportEvent event = new ProjectImportEvent();
			event.setEntityId("Starting Incremental Poll Archive");
			event.setTimestamp(new Date());
			event.setStatus(ImportStatus.STATUS_INFO);
			event.setEntityType("<html><i>none</i></html>");
			event.setDetails("");
			inDao.getNotifer().notifyEvent(event);
		} catch (Exception e) {
			handler.severe(String.format(
					"Error creating DAO for ALMA project store - %s",
					e.getMessage()), e);
			return;
		}
    	List<String> newOrModifiedIds = new ArrayList<String>();
		List<String> deletedIds = new ArrayList<String>();
		try {
			inDao.getObsProjectChanges(since, newOrModifiedIds, deletedIds);
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
		for(ObsProject prj: newProjects)
			linkData(prj);
		logger.info("Checking for entities already stored in SWDB");
		deleteExistingProjectsInSWDB(newProjects);
		if (newProjects.size() > 0) {
			ProjectImportEvent event = new ProjectImportEvent();
			event.setEntityId("Saving Converted Projects to SWDB");
			event.setTimestamp(new Date());
			event.setStatus(ImportStatus.STATUS_INFO);
			event.setEntityType("<html><i>none</i></html>");
			event.setDetails("About to save: " + newProjects.size()
					+ " Projects");
			inDao.getNotifer().notifyEvent(event);
		}
    	obsProjectDao.saveOrUpdate(newProjects);
    	logger.info(String.format(
    			"%d new or modified project%s, %d project%s removed",
    			newProjects.size(),
    			newProjects.size()==1? "": "s",
    			deleted,
    			deleted==1? "": "s"));
    	logNumbers();
    	
		ProjectImportEvent event = new ProjectImportEvent();
		event.setEntityId("Incremental Poll Archive Completed");
		event.setTimestamp(new Date());
		event.setStatus(ImportStatus.STATUS_INFO);
		event.setEntityType("<html><i>none</i></html>");
		event.setDetails("");
		inDao.getNotifer().notifyEvent(event);
		inDao.tidyUp();
    }
	/* End Incremental Polling of the ALMA Archives
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * External interface 
	 * ================================================================
	 */
	/**
	 * Poll the archive (i.e. the <code>XMLStore</code> and the <code>
	 * StateArchive</code>) for ObsProjects and SchedBlocks in which we
	 * are interested. 
	 * 
	 * If this is the first time the archive has been polled, or if the
	 * <code>forceRefresh</code> parameter is <code>true</code> then we
	 * clear out the current SWDB and load everything from scratch.
	 * Otherwise we just look for changes since the last update.
	 * 
	 * @param forceRefresh - do we want to force a full refresh?
	 */
	public void pollArchive(boolean forceRefresh) {
		Date lastUpdate;
		Date startTime;
		
		synchronized (this) {
			if (pollerBusy) {	// Already doing something
				reportBusy();
				return;
			}
			pollerBusy = true;
		}
		
		startTime = new Date();
		lastUpdate = lastRefresh(forceRefresh);
		if (lastUpdate == null) {
			reset();
			initialPollArchive();
		} else {
			//WORKAROUND: Check JIRA:CSV-2149
			//Avoid projects that doesn't show at Scheduler; go back 2 minutes before the last update
			lastUpdate = new Date(lastUpdate.getTime() - 2 * 60 * 1000);
			incrementalPollArchive(lastUpdate);
		}
		
		configDao.getConfiguration().setLastLoad(startTime);

		synchronized (this) {
			pollerBusy = false;
		}
	}
    
    public void deregisterCallback(String arg0) {
    	synchronized(callbacks){
    		callbacks.remove(arg0);
    	}
    }
    
    public void registerCallback(String arg0, ArchiveUpdaterCallback arg1) {
    	synchronized(callbacks){
    		callbacks.put(arg0, arg1);
    	}
    }

	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof ProjectImportEvent){
			ProjectImportEvent evt = (ProjectImportEvent) arg;
			ArchiveImportEvent event = new ArchiveImportEvent();
			event.timestamp = alma.acs.util.UTCUtility.utcJavaToOmg(evt.getTimestamp().getTime());
			event.entityId = evt.getEntityId();
			event.entityType = evt.getEntityType();
			event.status = alma.scheduling.ImportStatus.from_int(evt.getStatus().ordinal());
			event.details = evt.getDetails();
			logger.finer("Received notification update for project: " +
					event.entityId + " Type: " + event.entityType + " status: " + event.status);
			ArrayList<String> toBeUnregister =  new ArrayList<String>();
			for (String callback: callbacks.keySet()){
				try{
					callbacks.get(callback).report(event);
				} catch (org.omg.CORBA.MARSHAL ex){
					logger.warning("Found null field in event, id: " + callback + " Reason: " + ex.getMessage());
					
				} catch (org.omg.CORBA.SystemException ex){
					logger.fine("Found dead callback, id: " + callback + " De-regestering callback. Reason: " + ex.getMessage());
					ex.printStackTrace();
					toBeUnregister.add(callback);
				}
			}
			for (String callback: toBeUnregister)
				deregisterCallback(callback);
		}
	}

	/* End External interface
	 * ============================================================= */
}
