/**
 * 
 */
package alma.scheduling.datamodel.obsproject.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.omg.CORBA.UserException;

import alma.acs.component.client.ComponentClient;
import alma.acs.entityutil.EntityDeserializer;
import alma.acs.entityutil.EntityException;
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
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.SchedulingException;
import alma.scheduling.acsFacades.ACSComponentFactory;
import alma.scheduling.acsFacades.ComponentFactory;
import alma.scheduling.acsFacades.ComponentFactory.ComponentDiagnosticTypes;
import alma.scheduling.datamodel.DAOException;
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
	extends ComponentClient
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

    protected EntityDeserializer entityDeserializer;
    
    protected Logger logger;
    
    // ACS Diagnostics
    private final static ComponentDiagnosticTypes[] xmlStoreDiags = {
    	ComponentDiagnosticTypes.LOGGING,
    	ComponentDiagnosticTypes.PROFILING
    };
    
    private final static ComponentDiagnosticTypes[] stateSystemDiags = {
    	ComponentDiagnosticTypes.LOGGING,
    	ComponentDiagnosticTypes.PROFILING
    };
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
		super(logger, managerLoc, clientName);
		this.componentFactory = new ACSComponentFactory(getContainerServices());
		this.logger = getContainerServices().getLogger();
		this.xmlStore = componentFactory.getDefaultArchive(xmlStoreDiags);
		this.stateSystem = componentFactory.getDefaultStateSystem(stateSystemDiags);
		this.entityDeserializer = EntityDeserializer.getEntityDeserializer(
        		getContainerServices().getLogger());
		archive = new ArchiveInterface(this.xmlStore, this.stateSystem, entityDeserializer);
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
        
        logger.info(String.format("%s.getAllObsProjects()",
        		this.getClass().getSimpleName()));

        // Create somewhere for the result
        List<ObsProject> result = null;

        // Get the projects in which we're interested
        getInterestingProjects(archive);
        logger.info("Got the projects");
        logAPDMObsProposals(archive);
        logAPDMObsProjects(archive);
        logStatuses(archive);
        
        // Get all the corresponding APDM SchedBlocks
        for (alma.entity.xmlbinding.obsproject.ObsProject apdmProject : archive.obsProjects()) {
        	getAPDMSchedBlocksFor(archive, apdmProject);
        }
        logAPDMSchedBlocks(archive);
        
        // Convert them to Scheduling stylee ObsProjects
        result = convertAPDMProjectsToDataModel(archive, logger);
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
    		final DateTime     since,
    		final List<String> newOrModifiedIds,
    		final List<String> deletedIds) throws DAOException {
		
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
			final DateTime since) {
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
			final DateTime since) {
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
			final DateTime since) {
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
			final DateTime since) {
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
			final DateTime    since,
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
			final DateTime        since) {
		
		final Map<String, StateChangeData> build =
			new HashMap<String, StateChangeData>();
		final List<StateChangeData> result =
			new ArrayList<StateChangeData>();
		
		final IDLArrayTime start =
			new IDLArrayTime(since.getMillisec());
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
	private static String getManagerLocation() throws SchedulingException {
		final String result = System.getProperty("ACS.manager");
		if (result == null) {
			throw new SchedulingException("Java property 'ACS.manager' is not set. It must be set to the corbaloc of the ACS manager!");
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
	private void logAPDMObsProposals(ArchiveInterface archive) {
		final StringBuilder sb = new StringBuilder();
		final Formatter f = new Formatter(sb);
		f.format("Found the following %d APDM ObsProposal%s:%n",
				archive.numObsProposals(),
				(archive.numObsProposals()==1)? "": "s");
		for (final alma.entity.xmlbinding.obsproposal.ObsProposal op : archive.obsProposals()) {
			f.format("\tProposal uid: %s, Project uid: %s, title is %s%n",
					op.getObsProposalEntity().getEntityId(),
					op.getObsProjectRef().getEntityId(),
					op.getTitle());
		}
		logger.info(sb.toString());
	}

	private void logAPDMObsProjects(ArchiveInterface archive) {
		final StringBuilder sb = new StringBuilder();
		final Formatter f = new Formatter(sb);
		f.format("Found the following %d APDM ObsProject%s:%n",
				archive.numObsProjects(),
				(archive.numObsProjects()==1)? "": "s");
		for (final alma.entity.xmlbinding.obsproject.ObsProject op : archive.obsProjects()) {
			f.format("\tOP uid: %s, PS uid: %s, name is %s, proposal %s%n",
					op.getObsProjectEntity().getEntityId(),
					op.getProjectStatusRef().getEntityId(),
					op.getProjectName(),
					op.getObsProposalRef().getEntityId());
		}
		logger.info(sb.toString());
	}

	private void logAPDMObsProjects(ArchiveInterface archive, String... ids) {
		final StringBuilder sb = new StringBuilder();
		final Formatter f = new Formatter(sb);
		f.format("Logging %d APDM ObsProject%s:%n",
				ids.length,
				(ids.length==1)? "": "s");
		for (String id : ids) {
			if (archive.hasObsProject(id)) {
				alma.entity.xmlbinding.obsproject.ObsProject op
					= archive.cachedObsProject(id);
				f.format("\tOP uid: %s, PS uid: %s, name is %s%n",
						op.getObsProjectEntity().getEntityId(),
						op.getProjectStatusRef().getEntityId(),
						op.getProjectName());
			} else {
				f.format("\tOP uid: %s not found in cache", id);
			}
		}
		logger.info(sb.toString());
	}

	private void logAPDMSchedBlocks(ArchiveInterface archive) {
		final StringBuilder sb = new StringBuilder();
		final Formatter f = new Formatter(sb);
		f.format("Found the following %d APDM SchedBlock%s:%n",
				archive.numSchedBlocks(),
				(archive.numSchedBlocks()==1)? "": "s");
		for (final alma.entity.xmlbinding.schedblock.SchedBlock schedBlock : archive.schedBlocks()) {
			final alma.entity.xmlbinding.obsproject.ObsProject op =
				archive.cachedObsProject(schedBlock.getObsProjectRef().getEntityId());
			if (op != null) {
				final alma.entity.xmlbinding.schedblock.SchedBlockEntityT sbEnt  = schedBlock.getSchedBlockEntity();
				final                                   SBStatusRefT      sbsRef = schedBlock.getSBStatusRef();
				final alma.entity.xmlbinding.obsproject.ObsProjectEntityT prjEnt = op.getObsProjectEntity();
				f.format("\tSB uid: %s, SBS uid: %s, part of %s (%s)%n",
						(sbEnt  == null)? "<null>": sbEnt.getEntityId(),
						(sbsRef == null)? "<null>": sbsRef.getEntityId(),
						op.getProjectName(),
						(prjEnt == null)? "<null>": prjEnt.getEntityId());
			} else {
				final alma.entity.xmlbinding.schedblock.SchedBlockEntityT sbEnt  = schedBlock.getSchedBlockEntity();
				final                                   SBStatusRefT      sbsRef = schedBlock.getSBStatusRef();
				final alma.entity.xmlbinding.obsproject.ObsProjectRefT    prjRef = schedBlock.getObsProjectRef();
				f.format("\tSB uid: %s, SBS uid: %s, part of <<lost project>> (%s)%n",
						(sbEnt  == null)? "<null>": sbEnt.getEntityId(),
						(sbsRef == null)? "<null>": sbsRef.getEntityId(),
						(prjRef == null)? "<null>": prjRef.getEntityId());
			}
		}
		logger.info(sb.toString());
	}

	private void logAPDMSchedBlocks(ArchiveInterface archive,
			                        String... projectIds) {
		final StringBuilder sb = new StringBuilder();
		final Formatter f = new Formatter(sb);
		final Set<String> pids = new HashSet<String>();
		
		for (String pid : projectIds) {
			pids.add(pid);
		}
		f.format("Found the following APDM SchedBlock%s:%n",
				archive.numSchedBlocks(),
				(archive.numSchedBlocks()==1)? "": "s");
		for (final alma.entity.xmlbinding.schedblock.SchedBlock schedBlock : archive.schedBlocks()) {
			final String projectId = schedBlock.getObsProjectRef().getEntityId();
			if (pids.contains(projectId)) {
				final alma.entity.xmlbinding.obsproject.ObsProject op =
					archive.cachedObsProject(projectId);
				f.format("\tSB uid: %s, SBS uid: %s, part of %s (%s)%n",
						schedBlock.getSchedBlockEntity().getEntityId(),
						schedBlock.getSBStatusRef().getEntityId(),
						op.getProjectName(),
						projectId);
			}
		}
		logger.info(sb.toString());
	}

	private void recLogOUSStatus(
			final String indent,
			final OUSStatus ousStatus,
			final ArchiveInterface archive,
			final Formatter f) {
		f.format("%sOUSS uid: %s, OUS uid: %s in %s, status is %s%n",
				indent,
				ousStatus.getOUSStatusEntity().getEntityId(),
				ousStatus.getObsUnitSetRef().getPartId(),
				ousStatus.getObsUnitSetRef().getEntityId(),
				ousStatus.getStatus().getState());
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
				f.format("%s\tSBS uid: %s, SB uid: %s, status is %s%n",
						indent,
						child.getSBStatusEntity().getEntityId(),
						child.getSchedBlockRef().getEntityId(),
						child.getStatus().getState());
			} else {
				f.format("\t\tchild SBStatus %s not in cache", childId);
			}
		}
	}

	private void logOneProjectStatus(
			final ProjectStatus projectStatus,
			final ArchiveInterface archive,
			final Formatter f) {
		String projectId = projectStatus.getObsProjectRef().getEntityId();
		String projectName;
		if (archive.hasObsProject(projectId)) {
			projectName = archive.cachedObsProject(projectId).getProjectName();
		} else {
			projectName = "<none>";
		}
		f.format("\tProject %s, PS uid: %s, OP uid: %s, status is %s%n",
				projectName, 
				projectStatus.getProjectStatusEntity().getEntityId(),
				projectStatus.getObsProjectRef().getEntityId(),
				projectStatus.getStatus().getState());
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
		final StringBuilder sb = new StringBuilder();
		final Formatter f = new Formatter(sb);
		f.format("Found %s ProjectStatus%s, %d OUSStatus%s and %s SBStatus%s%n",
				archive.numProjectStatuses(),
				((archive.numProjectStatuses()==1)? "": "es"),
				archive.numOUSStatuses(),
				((archive.numOUSStatuses()==1)? "": "es"),
				archive.numSBStatuses(),
			    ((archive.numSBStatuses()==1)? "": "es"));
		for (final ProjectStatus ps : archive.projectStatuses()) {
			logOneProjectStatus(ps, archive, f);
		}
		logger.info(sb.toString());
	}

	private void logObsProjects(List<ObsProject> result) {
        logger.info(String.format(
        		"Converted %d Project%s",
        		result.size(),
        		(result.size() == 1)? "": "s"));
	}
	/* End Logging
	 * ============================================================= */
}
