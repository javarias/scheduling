/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
/**
 * 
 */
package alma.scheduling.datamodel.obsproject.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.logging.Logger;

import org.omg.CORBA.UserException;

import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.component.client.ComponentClient;
import alma.acs.container.ContainerServices;
import alma.acs.entityutil.EntityDeserializer;
import alma.acs.entityutil.EntityException;
import alma.acs.entityutil.EntitySerializer;
import alma.asdmIDLTypes.IDLArrayTime;
import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.entity.xmlbinding.ousstatus.OUSStatusRefT;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.entity.xmlbinding.sbstatus.SBStatusRefT;
import alma.entity.xmlbinding.schedblock.SchedBlock;
import alma.lifecycle.persistence.domain.StateEntityType;
import alma.projectlifecycle.StateChangeData;
import alma.projectlifecycle.StateSystemOperations;
import alma.scheduling.acsFacades.ACSComponentFactory;
import alma.scheduling.acsFacades.ComponentFactory;
import alma.scheduling.acsFacades.ComponentFactory.ComponentDiagnosticTypes;
import alma.scheduling.datamodel.DAOException;
import alma.scheduling.datamodel.bookkeeping.Bookkeeper;
import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.utils.ErrorHandling;
import alma.statearchiveexceptions.StateIOFailedEx;
import alma.xmlstore.OperationalOperations;

/**
 * @author dclarke
 *
 */
public abstract class AbstractXMLStoreProjectDao
	implements ProjectIncrementalDao {

    
    
    /*
     * ================================================================
     * Fields and Constants
     * ================================================================
     */
    @SuppressWarnings("unused")
	private ConfigurationDao configurationDao;
    
    public void setConfigurationDao(ConfigurationDao configurationDao) {
        this.configurationDao = configurationDao;
    }

    // ACS Components
    private ComponentFactory componentFactory;
    protected OperationalOperations xmlStore;
    protected StateSystemOperations stateSystem;
    
    protected ArchiveInterface archive;
    protected Bookkeeper bookie = null;

    protected EntityDeserializer entityDeserializer;
    protected EntitySerializer   entitySerializer;
    
    protected Logger logger;
    
    // ACS Diagnostics
    private final static ComponentDiagnosticTypes[] xmlStoreDiags = {
//    	ComponentDiagnosticTypes.LOGGING,
//    	ComponentDiagnosticTypes.PROFILING
    };
    
    private final static ComponentDiagnosticTypes[] stateSystemDiags = {
//    	ComponentDiagnosticTypes.LOGGING,
//    	ComponentDiagnosticTypes.PROFILING
    };
    
    //Import Notifications
    protected final XMLStoreImportNotifier notifier;
    
    private XMLStroreProjectDaoClient client;
    /* End Fields and constants
	 * ============================================================= */

    
    
    /*
     * ================================================================
     * Construction
     * ================================================================
     */
	/**
	 * @throws Exception
	 */
	protected AbstractXMLStoreProjectDao(String clientName)
			throws Exception {
		this(null, // This must be null, argument retained in ACS for compatibility
				getManagerLocation(),
				clientName);
	}

	/**
	 * @param logger
	 * @param managerLoc
	 * @param clientName
	 * @throws Exception
	 */
	protected AbstractXMLStoreProjectDao(java.util.logging.Logger logger,
			                        String managerLoc,
			                        String clientName)
			throws Exception {
		// TODO: Logger type has been hacked, must be resolved properly.
		client = new XMLStroreProjectDaoClient(logger, managerLoc, clientName);
		notifier = new XMLStoreImportNotifier();
		this.componentFactory = new ACSComponentFactory(client.getContainerServices());
		this.logger = client.getContainerServices().getLogger();
		this.xmlStore = componentFactory.getDefaultArchive(xmlStoreDiags);
		this.stateSystem = componentFactory.getDefaultStateSystem(stateSystemDiags);
		this.entityDeserializer = EntityDeserializer.getEntityDeserializer(
        		client.getContainerServices().getLogger());
		this.entitySerializer = EntitySerializer.getEntitySerializer(
        		client.getContainerServices().getLogger());
		archive = new ArchiveInterface(this.xmlStore, this.stateSystem, entityDeserializer, entitySerializer);
		bookie = new Bookkeeper(archive, logger);
	}
	
	protected AbstractXMLStoreProjectDao(ContainerServices containerServices) throws AcsJContainerServicesEx, UserException {
		notifier = new XMLStoreImportNotifier();
		this.componentFactory = new ACSComponentFactory(containerServices);
		this.logger = containerServices.getLogger();
		this.xmlStore = componentFactory.getDefaultArchive(xmlStoreDiags);
		this.stateSystem = componentFactory.getDefaultStateSystem(stateSystemDiags);
		this.entityDeserializer = EntityDeserializer.getEntityDeserializer(
        		containerServices.getLogger());
		this.entitySerializer = EntitySerializer.getEntitySerializer(
        		containerServices.getLogger());
		archive = new ArchiveInterface(this.xmlStore, this.stateSystem, entityDeserializer, entitySerializer);
		bookie = new Bookkeeper(archive, logger);
	}
	/* End Construction
	 * ============================================================= */

    
    
    /*
     * ================================================================
     * Steps from which the main operations are made
     * ================================================================
     */
	// Abstract requirements upon subclasses.
	protected abstract void getInterestingProjects(
			ArchiveInterface archive);
	protected abstract alma.entity.xmlbinding.obsproject.ObsUnitSetT
		getTopLevelOUSForProject(
			ArchiveInterface archive,
		    alma.entity.xmlbinding.obsproject.ObsProject apdmProject);
	protected abstract List<ObsProject> convertAPDMProjectsToDataModel(
			ArchiveInterface archive,
			Logger logger);
	protected abstract boolean interestedInObsProject(String state);

	/**
	 * Ensure that all the APDM ScheBlocks that we care about and which
	 * correspond to the given APDM ObsProject are cached in the given
	 * ArchiveInterface.
	 * 
     * @param archive
     * @param apdmProject
     */
	protected abstract void getAPDMSchedBlocksFor(
			ArchiveInterface                             archive,
			alma.entity.xmlbinding.obsproject.ObsProject apdmProject);
	/* End Steps from which the main operations are made
	 * ============================================================= */

    
    
    /*
     * ================================================================
     * Implementation of ProjectDao
     * ================================================================
     */
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ProjectDao#getAllObsProjects()
	 */
	@Override
	public List<ObsProject> getAllObsProjects() throws DAOException {
        Date start, end;
        logger.info(String.format("%s.getAllObsProjects()",
        		this.getClass().getSimpleName()));
        // Use the State Archive to convert all the Phase2Submitted
        // ObsProjects and SchedBlocks to Ready
        
        // Create somewhere for the result
        List<ObsProject> result = null;

        // Get the projects in which we're interested
        getInterestingProjects(archive);
        logger.info("Got the projects");
        logAPDMObsProposals(archive);
        logAPDMObsProjects(archive);
        logStatuses(archive);
//        if (bookie != null) {
//        	Collection<ProjectStatus> projectStatuses
//        			= new ArrayList<ProjectStatus>(archive.projectStatuses());
//        	for (final ProjectStatus ps : projectStatuses) {
//        		try {
//	        		bookie.initialise(ps);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//        	}
//        	logStatuses(archive);
//        }
        start = new Date();
        // Get all the corresponding APDM SchedBlocks
        for (alma.entity.xmlbinding.obsproject.ObsProject apdmProject : archive.obsProjects()) {
        	getAPDMSchedBlocksFor(archive, apdmProject);
        }
        end = new Date();
        logger.fine("Getting apdm SchedBlocks from archive took " + (end.getTime() - start.getTime()) + " ms");
        try {
            logAPDMSchedBlocks(archive);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            logger.severe("null pointer exception when logging APDM SchedBlocks");
        }
        
        // Convert them to Scheduling stylee ObsProjects
        start = new Date();
        result = convertAPDMProjectsToDataModel(archive, logger);
        end = new Date();
        logger.fine("Conversion from APDM model to SWDB model took " + (end.getTime() - start.getTime()) + " ms");
        logObsProjects(result);

        return result;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ProjectDao#getSomeObsProjects(java.lang.String[])
	 */
	@Override
	public List<ObsProject> getSomeObsProjects(String... ids)
			throws DAOException {
        
        logger.info(String.format("%s.getSomeObsProjects()",
        		this.getClass().getSimpleName()));
        
        // For each project in which we're interested....
        for (String id : ids) {
    		try {
    			// ...get the project...
    			final alma.entity.xmlbinding.obsproject.ObsProject
    					apdmProject = archive.getObsProject(id);
    			// ... and its status...
                ProjectStatus ps = archive.getProjectStatus(apdmProject.getProjectStatusRef().getEntityId());
                archive.getOUSStatus(ps.getObsProgramStatusRef().getEntityId());
    			// ...get all the corresponding APDM SchedBlocks.
				logger.info(String.format(
						"Succesfully got APDM ObsProject %s",
						id));
            	getAPDMSchedBlocksFor(archive, apdmProject);
			} catch (EntityException e) {
				logger.warning(String.format(
						"Cannot get APDM ObsProject %s - %s (skipping)",
						id,
						e.getMessage()));
			} catch (UserException e) {
				logger.warning(String.format(
						"Cannot get APDM ObsProject %s - %s (skipping)",
						id,
						e.getMessage()));
			}
        }
        logger.info("Got the projects and sched blocks");
        logAPDMObsProjects(archive, ids);
        logAPDMSchedBlocks(archive, ids);
        
        // Convert them to Scheduling stylee ObsProjects
        final List<ObsProject> result = convertAPDMProjectsToDataModel(archive, logger);
        logObsProjects(result);

        return result;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ProjectDao#getSomeObsProjects(java.util.Collection)
	 */
	@Override
	public List<ObsProject> getSomeObsProjects(Collection<String> ids)
			throws DAOException {
        return getSomeObsProjects(ids.toArray(new String[0]));
	}
     /* End Implementation of ProjectDao
     * ============================================================= */

    
    
    /*
     * ================================================================
     * Implementation of ProjectIncrementalDao
     * ================================================================
     */
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ProjectIncrementalDao#getObsProjectChanges(alma.scheduling.Define.DateTime, java.util.List, java.util.List)
	 */
	@Override
	public void getObsProjectChanges(
    		final Date    since,
    		final List<String> newOrModifiedIds,
    		final List<String> deletedIds) throws DAOException {
        
        logger.info(String.format("%s.getObsProjectChanges(since %s)",
        		this.getClass().getSimpleName(),
        		since.toString()));
        
		// Collect the id in sets so we don't have to worry about
		// duplication
		final Set<String> changedProjects = new HashSet<String>();
		final Set<String> deletedProjects = new HashSet<String>();
		
		// A change in ProjectStatus is the only way that an ObsProject
		// can be deleted, so we differentiate these two cases in the
		// search for changed ProjectStatuses
		getProjectIdsForChangedProjectStatuses(since,
											   changedProjects,
											   deletedProjects);
		
		// Other changes - in SBStatuses, OUSStatuses, SchedBlocks and
		// ObsProjects - just result in modified projects, so no need
		// to do the same differentiation.
		changedProjects.addAll(getProjectIdsForChangedSchedBlocks(since));
		changedProjects.addAll(getProjectIdsForChangedObsProjects(since));
		changedProjects.addAll(getProjectIdsForChangedSBStatuses(since));
		changedProjects.addAll(getProjectIdsForChangedOUSStatuses(since));
		
		// Take any deleted project ids out of the changed project id
		// collection just in case there are duplications there.
		changedProjects.removeAll(deletedProjects);
		
		// Now add the ids we've found to the collections in which
		// we've been asked to put them.
		newOrModifiedIds.addAll(changedProjects);
		deletedIds.addAll(deletedProjects);
		
		// Bleargh... log
		for (final String id : deletedIds) {
			logger.finer(String.format("delete project %s", id));
		}
		for (final String id : newOrModifiedIds) {
			logger.finer(String.format("refresh or get project %s", id));
		}
	}

	/**
	 * Work out which SchedBlocks have changed in the XMLStore since
	 * the given time, then collect and return the Entity Ids of their
	 * ObsProjects.
	 * 
	 * @param since
	 * @return
	 */
	private List<String> getProjectIdsForChangedSchedBlocks(
			final Date since) {
		final List<String> result = new ArrayList<String>();
		List<String> changedSBs;
		try {
			changedSBs = archive.getIdsOfChangedSBs(since);
		} catch (UserException e) {
	    	ErrorHandling.warning(
	    			logger,
	    			String.format(
	    				"Error finding changed SchedBlocks - %s",
	    				e.getMessage()),
	    			e);
	    	return result;
		}
		
		for (final String sbId : changedSBs) {
			final SchedBlock sb;
			try {
				sb = archive.getSchedBlock(sbId);
			} catch (Exception e) {
		    	ErrorHandling.warning(
		    			logger,
		    			String.format(
		    				"Error finding changed SchedBlock %s - %s",
		    				sbId,
		    				e.getMessage()),
		    			e);
		    	continue; // move on to next SB id
			}
			final String projectId = sb.getObsProjectRef().getEntityId();
			result .add(projectId);
		}
		return result;
	}

	/**
	 * Work out which ObsProjects have changed in the XMLStore since
	 * the given time, then collect and return their Entity Ids.
	 * 
	 * @param since
	 * @return
	 */
	private List<String> getProjectIdsForChangedObsProjects(
			final Date since) {
		final List<String> result = new ArrayList<String>();
		List<String> changedOPs = null;
		try {
			changedOPs = archive.getIdsOfChangedProjects(since);
		} catch (UserException e) {
	    	ErrorHandling.warning(
	    			logger,
	    			String.format(
	    				"Error finding changed ObsProjects - %s",
	    				e.getMessage()),
	    			e);
	    	return result;
		}
		for (final String projectId : changedOPs) {
			result.add(projectId);
		}
		return result;
	}
	
	/**
	 * Work out which SBStatuses have changed in the XMLStore since
	 * the given time, then collect and return the Entity Ids of their
	 * ObsProjects.
	 * 
	 * @param since
	 * @return
	 */
	private List<String> getProjectIdsForChangedSBStatuses(
			final Date since) {
		final List<String> result = new ArrayList<String>();
		final List<StateChangeData> changes = getStatusChanges(
				StateEntityType.SBK,
				since);
		
		for (StateChangeData scd :changes) {
			String sbId = scd.domainEntityId;
			SchedBlock sb = null;
			
			try {
				sb = archive.getSchedBlock(sbId);
			} catch (Exception e) {
		    	ErrorHandling.severe(
		    			logger,
		    			String.format(
		    				"Error finding changed SchedBlock %s from StateChange %d - %s",
		    				sbId,
		    				scd.id,
		    				e.getMessage()),
		    			e);
		    	continue; // move on to next change data
			}
			try {
				String projectId = sb.getObsProjectRef().getEntityId();
				result.add(projectId);
			} catch (Exception e) {
				ErrorHandling.severe(
						logger,
						String.format(
							"Error finding ObsProject for SchedBlock %s - %s",
							sbId,
							e.getMessage()),
						e);
			}
		}
		return result;
	}
	
	/**
	 * Work out which OUSStatuses have changed in the XMLStore since
	 * the given time, then collect and return the Entity Ids of their
	 * ObsProjects.
	 * 
	 * @param since
	 * @return
	 */
	private List<String> getProjectIdsForChangedOUSStatuses(
			final Date since) {
		final List<String> result = new ArrayList<String>();
		final List<StateChangeData> changes = getStatusChanges(
				StateEntityType.OUT,
				since);
		
		for (StateChangeData scd : changes) {
			String projectId = scd.domainEntityId;
			result.add(projectId);
		}

		return result;
	}
	
	/**
	 * Work out which ProjectStatuses have changed in the XMLStore
	 * since the given time. Sort the Entity Ids of their ObsProjects
	 * into by whether they are now deleted (from a scheduling
	 * perspective) or simply changed.
	 * 
	 * @param since - the time of the last search
	 * @param changedProjects - projects which have been modified
	 * @param deletedProjects - projects which are no longer of
	 *                          interest to the Scheduler.
	 */
	private void getProjectIdsForChangedProjectStatuses(
			final Date    since,
			final Set<String> changedProjects,
			final Set<String> deletedProjects) {
		final List<StateChangeData> changes = getStatusChanges(
				StateEntityType.PRJ,
				since);
		
		for (StateChangeData scd : changes) {
			final String projectId = scd.domainEntityId;
			final String state     = scd.domainEntityState;
			if (interestedInObsProject(state)) {
				changedProjects.add(projectId);
			} else {
				deletedProjects.add(projectId);
			}
		}
	}
	
	/**
	 * Get the state changes since the last query and find the latest
	 * one for each changed Status Entity.
	 * 
	 * @return List<StateChangeData> - the last change for each changed
	 *                                 status id.
	 */
	private List<StateChangeData> getStatusChanges(
			final StateEntityType type,
			final Date       since) {
		
		final Map<String, StateChangeData> build =
			new HashMap<String, StateChangeData>();
		final List<StateChangeData> result =
			new ArrayList<StateChangeData>();
		
		final IDLArrayTime start =
			new IDLArrayTime(since.getTime());
		final IDLArrayTime end   =
			new IDLArrayTime(System.currentTimeMillis());

		try {
			final StateChangeData[] stateChanges =
				stateSystem.findStateChangeRecords(
						start,
						end, "", "", "",
						type.toString());
			
			logger.finer(String.format(
					"stateChanges(%s).length: %d (start = %d, end = %d)",
					type.toString(), stateChanges.length, start.value, end.value));
			
			for (StateChangeData sc : stateChanges) {
				if (build.containsKey(sc.statusEntityId)) {
					final StateChangeData previous = build.get(sc.statusEntityId);
					if (previous.timestamp.value < sc.timestamp.value) {
						build.put(sc.statusEntityId, sc);
					}
				} else {
					build.put(sc.statusEntityId, sc);
				}
				
			}
		} catch (StateIOFailedEx e) {
			ErrorHandling.warning(
					logger,
					String.format("Can not get changes(%s) from State Archive - %s",
							type, e.getMessage()),
					e);
		}

		result.addAll(build.values());
		return result;
	}
   /* End Implementation of ProjectIncrementalDao
     * ============================================================= */

    
    
    /*
     * ================================================================
     * ACS Bookkeeping
     * ================================================================
     */
	private static String getManagerLocation() throws Exception{
		final String result = System.getProperty("ACS.manager");
		if (result == null) {
			throw new Exception("Java property 'ACS.manager' is not set. It must be set to the corbaloc of the ACS manager!");
		}
		return result;
	}
	/* End ACS Bookkeeping
	 * ============================================================= */

    
    
    /*
     * ================================================================
     * Logging
     * ================================================================
     */
	/** Limit the size of log messages - large loads were hitting problems */
	final private static int MaxItemsPerLogMessage = 100;
	
	private void logAPDMObsProposals(ArchiveInterface archive) {
		StringBuilder sb = new StringBuilder();
		Formatter f = new Formatter(sb);
		int lines;
		
		f.format("Found the following %d APDM ObsProposal%s:%n",
				archive.numObsProposals(),
				(archive.numObsProposals()==1)? "": "s");
		lines = 1;
		
		for (final alma.entity.xmlbinding.obsproposal.ObsProposal op : archive.obsProposals()) {
			f.format("\tProposal %s, Project %s, title is %s%n",
					op.getObsProposalEntity().getEntityId(),
					op.getObsProjectRef().getEntityId(),
					op.getTitle());
			lines ++;
			if (lines == MaxItemsPerLogMessage) {
				logger.info(sb.toString());
				sb = new StringBuilder();
				f = new Formatter(sb);
				f.format("...continuing...%n");
				lines = 1;
			}
		}
		if (lines > 1) {
			logger.info(sb.toString());
		}
	}

	private void logAPDMObsProjects(ArchiveInterface archive) {
		StringBuilder sb = new StringBuilder();
		Formatter f = new Formatter(sb);
		int lines;
		
		f.format("Found the following %d APDM ObsProject%s:%n",
				archive.numObsProjects(),
				(archive.numObsProjects()==1)? "": "s");
		lines = 1;
		
		for (final alma.entity.xmlbinding.obsproject.ObsProject op : archive.obsProjects()) {
			f.format("\tOP %s, PS %s, name is %s, proposal %s%n",
					op.getObsProjectEntity().getEntityId(),
					op.getProjectStatusRef().getEntityId(),
					op.getProjectName(),
					op.getObsProposalRef().getEntityId());
			lines ++;
			if (lines == MaxItemsPerLogMessage) {
				logger.info(sb.toString());
				sb = new StringBuilder();
				f = new Formatter(sb);
				f.format("...continuing...%n");
				lines = 1;
			}
		}
		if (lines > 1) {
			logger.info(sb.toString());
		}
	}

	private void logAPDMObsProjects(ArchiveInterface archive, String... ids) {
		StringBuilder sb = new StringBuilder();
		Formatter f = new Formatter(sb);
		int lines;
		
		f.format("Logging %d APDM ObsProject%s:%n",
				ids.length,
				(ids.length==1)? "": "s");
		lines = 1;
		
		for (String id : ids) {
			if (archive.hasObsProject(id)) {
				alma.entity.xmlbinding.obsproject.ObsProject op
					= archive.cachedObsProject(id);
				f.format("\tOP %s, PS %s, name is %s%n",
						op.getObsProjectEntity().getEntityId(),
						op.getProjectStatusRef().getEntityId(),
						op.getProjectName());
			} else {
				f.format("\tOP %s not found in cache", id);
			}
			lines ++;
			if (lines == MaxItemsPerLogMessage) {
				logger.info(sb.toString());
				sb = new StringBuilder();
				f = new Formatter(sb);
				f.format("...continuing...%n");
				lines = 1;
			}
		}
		if (lines > 1) {
			logger.info(sb.toString());
		}
	}

	private void logAPDMSchedBlocks(ArchiveInterface archive) {
		StringBuilder sb = new StringBuilder();
		Formatter f = new Formatter(sb);
		int lines;
		
		f.format("Found the following %d APDM SchedBlock%s:%n",
				archive.numSchedBlocks(),
				(archive.numSchedBlocks()==1)? "": "s");
		lines = 1;
		
		for (final alma.entity.xmlbinding.schedblock.SchedBlock schedBlock : archive.schedBlocks()) {
			final alma.entity.xmlbinding.obsproject.ObsProject op =
				archive.cachedObsProject(schedBlock.getObsProjectRef().getEntityId());
			if (op != null) {
				final alma.entity.xmlbinding.schedblock.SchedBlockEntityT sbEnt  = schedBlock.getSchedBlockEntity();
				final                                   SBStatusRefT      sbsRef = schedBlock.getSBStatusRef();
				final alma.entity.xmlbinding.obsproject.ObsProjectEntityT prjEnt = op.getObsProjectEntity();
				f.format("\tSB %s, SBS %s, part of %s (%s)%n",
						(sbEnt  == null)? "<null>": sbEnt.getEntityId(),
						(sbsRef == null)? "<null>": sbsRef.getEntityId(),
						op.getProjectName(),
						(prjEnt == null)? "<null>": prjEnt.getEntityId());
			} else {
				final alma.entity.xmlbinding.schedblock.SchedBlockEntityT sbEnt  = schedBlock.getSchedBlockEntity();
				final                                   SBStatusRefT      sbsRef = schedBlock.getSBStatusRef();
				final alma.entity.xmlbinding.obsproject.ObsProjectRefT    prjRef = schedBlock.getObsProjectRef();
				f.format("\tSB %s, SBS %s, part of <<lost project>> (%s)%n",
						(sbEnt  == null)? "<null>": sbEnt.getEntityId(),
						(sbsRef == null)? "<null>": sbsRef.getEntityId(),
						(prjRef == null)? "<null>": prjRef.getEntityId());
			}
			lines ++;
			if (lines == MaxItemsPerLogMessage) {
				logger.info(sb.toString());
				sb = new StringBuilder();
				f = new Formatter(sb);
				f.format("...continuing...%n");
				lines = 1;
			}
		}
		if (lines > 1) {
			logger.info(sb.toString());
		}
	}

	private void logAPDMSchedBlocks(ArchiveInterface archive,
			                        String...        projectIds) {
		StringBuilder sb = new StringBuilder();
		Formatter     f  = new Formatter(sb);
		int lines;

		final Set<String> pids = new HashSet<String>();

		for (String pid : projectIds) {
			pids.add(pid);
		}
		f.format("Found the following %d APDM SchedBlock%s:%n",
				 archive.numSchedBlocks(),
				 (archive.numSchedBlocks()==1)? "": "s");
		lines = 1;

		for (final alma.entity.xmlbinding.schedblock.SchedBlock schedBlock : archive.schedBlocks()) {
			final String projectId = schedBlock.getObsProjectRef().getEntityId();
			if (pids.contains(projectId)) {
				final alma.entity.xmlbinding.obsproject.ObsProject        op     = archive.cachedObsProject(projectId);
				final alma.entity.xmlbinding.schedblock.SchedBlockEntityT sbEnt  = schedBlock.getSchedBlockEntity();
				final                                   SBStatusRefT      sbsRef = schedBlock.getSBStatusRef();
				final alma.entity.xmlbinding.obsproject.ObsProjectEntityT prjEnt = op.getObsProjectEntity();
				f.format("\tSB %s, SBS %s, part of %s (%s)%n",
                         (sbEnt  == null)? "<null>": sbEnt.getEntityId(),
                         (sbsRef == null)? "<null>": sbsRef.getEntityId(),
                         op.getProjectName(),
                         (prjEnt == null)? "<null>": prjEnt.getEntityId());
			}
			lines ++;
			if (lines == MaxItemsPerLogMessage) {
				logger.info(sb.toString());
				sb = new StringBuilder();
				f = new Formatter(sb);
				f.format("...continuing...%n");
				lines = 1;
			}
		}
		if (lines > 1) {
			logger.info(sb.toString());
		}
	}

	private void recLogOUSStatus(
			final String indent,
			final OUSStatus ousStatus,
			final ArchiveInterface archive,
			final Formatter f) {

		f.format("%sOUSS %s, OUS %s in %s, status is %s%n",
				indent,
				ousStatus.getOUSStatusEntity().getEntityId(),
				ousStatus.getObsUnitSetRef().getPartId(),
				ousStatus.getObsUnitSetRef().getEntityId(),
				ousStatus.getStatus().getState());
		if (bookie != null) {
			f.format("%sOUSStatus is %sinitialised%n",
					indent, bookie.isInitialised(ousStatus)? "": "NOT ");
			f.format("%s", bookie.print(indent + "\t", ousStatus));
		}

		
		for (final OUSStatusRefT childRef : ousStatus.getOUSStatusChoice().getOUSStatusRef()) {
			final String childId = childRef.getEntityId();
			if (archive.hasOUSStatus(childId)) {
				final OUSStatus child = archive.cachedOUSStatus(childId);
				recLogOUSStatus(indent + "\t", child, archive, f);
			} else {
				f.format("\t\tchild OUSStatus %s not in cache", childId);
			}
		}
		for (final SBStatusRefT childRef : ousStatus.getOUSStatusChoice().getSBStatusRef()) {
			final String childId = childRef.getEntityId();
			if (archive.hasSBStatus(childId)) {
				final SBStatus child = archive.cachedSBStatus(childId);
				final String sbId = child.getSchedBlockRef().getEntityId();
				final String sbName = archive.hasSchedBlock(sbId)?
						archive.cachedSchedBlock(sbId).getName():
							"<unknown>";			
				f.format("%s\tSBS uid: %s, SB uid: %s (%s), status is %s%n",
						indent,
						child.getSBStatusEntity().getEntityId(),
						child.getSchedBlockRef().getEntityId(),
						sbName,
						child.getStatus().getState());
				if (bookie != null) {
					f.format("%s\tSBStatus is %sinitialised%n",
							indent, bookie.isInitialised(child)? "": "NOT ");
					f.format("%s\t", bookie.print(indent + "\t", child));
				}
			} else {
				f.format("\t\tchild SBStatus %s not in cache", childId);
			}
		}
	}

	private void logOneProjectStatus(final ProjectStatus projectStatus,
			                         final ArchiveInterface archive,
			                         final Formatter f,
			                         final int count) {
		String projectId = projectStatus.getObsProjectRef().getEntityId();
		String projectName;
		if (archive.hasObsProject(projectId)) {
			projectName = archive.cachedObsProject(projectId).getProjectName();
		} else {
			projectName = "<none>";
		}
		f.format("%n%7d\tProject %s, PS %s, OP %s, status is %s%n",
				count,
				projectName, 
				projectStatus.getProjectStatusEntity().getEntityId(),
				projectStatus.getObsProjectRef().getEntityId(),
				projectStatus.getStatus().getState());
		if (bookie != null) {
			f.format("\tProjectsStatus is %sinitialised%n",
					bookie.isInitialised(projectStatus)? "": "NOT ");
			f.format("%s", bookie.print("\t", projectStatus));
		}
		
		final String programId = projectStatus.getObsProgramStatusRef().getEntityId();
		if (archive.hasOUSStatus(programId)) {
			final OUSStatus ouss = archive.cachedOUSStatus(programId);
			recLogOUSStatus("\t\t", ouss, archive, f);
		} else {
			f.format("\t\tTop level OUSStatus %s not in cache", programId);
		}
		f.format("%n");
	}

	private void logStatuses(ArchiveInterface archive) {
		logger.info(String.format(
				"Found %s ProjectStatus%s, %d OUSStatus%s and %s SBStatus%s%n",
				archive.numProjectStatuses(),
				((archive.numProjectStatuses()==1)? "": "es"),
				archive.numOUSStatuses(),
				((archive.numOUSStatuses()==1)? "": "es"),
				archive.numSBStatuses(),
			    ((archive.numSBStatuses()==1)? "": "es")));
		
		int output = 0;
		
		for (final ProjectStatus ps : archive.projectStatuses()) {
			final StringBuilder sb = new StringBuilder();
			final Formatter f = new Formatter(sb);
			
			try {
				logOneProjectStatus(ps, archive, f, ++output);
				logger.info(sb.toString());
			} catch (Exception e) {
				logger.warning(String.format(
						"Unexpected error whilst printing hierarchy for ProjectStatus %s - %s.%nHere's what we managed so far:%n%s",
						ps.getProjectStatusEntity().getEntityId(),
						e.getMessage(),
						sb.toString()));
			}
		}
	}

	private void logObsProjects(List<ObsProject> result) {
        logger.info(String.format(
        		"Converted %d Project%s",
        		result.size(),
        		(result.size() == 1)? "": "s"));
	}
	
	/* End Logging
	 * ============================================================= */
	public XMLStoreImportNotifier getNotifer(){
		return notifier;
	}
	
	public class XMLStoreImportNotifier extends Observable{
		
		public void notifyEvent(ProjectImportEvent event){
			System.out.println("Notificating Observers, Time: " + event.getTimestamp());
			setChanged();
			notifyObservers(event);
		}
	}
	
	private class XMLStroreProjectDaoClient extends ComponentClient {

		public XMLStroreProjectDaoClient(Logger logger, String managerLoc,
				String clientName) throws Exception {
			super(logger, managerLoc, clientName);
		}
		
	}
}
