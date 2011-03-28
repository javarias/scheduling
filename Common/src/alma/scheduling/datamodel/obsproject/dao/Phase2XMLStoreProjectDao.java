/**
 * 
 */
package alma.scheduling.datamodel.obsproject.dao;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import org.omg.CORBA.UserException;

import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.container.ContainerServices;
import alma.acs.entityutil.EntityException;
import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.lifecycle.stateengine.constants.Role;
import alma.lifecycle.stateengine.constants.Subsystem;
import alma.scheduling.datamodel.DAOException;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.dao.ProjectImportEvent.ImportStatus;
import alma.scheduling.utils.ErrorHandling;
import alma.scheduling.utils.SchedulingProperties;
import alma.xmlentity.XmlEntityStruct;

/**
 * @author dclarke
 *
 */
public class Phase2XMLStoreProjectDao extends AbstractXMLStoreProjectDao {
	
    final private static String[] OPPhase2RunnableStates = {
    	alma.entity.xmlbinding.valuetypes.types.StatusTStateType.READY.toString(),              
    	alma.entity.xmlbinding.valuetypes.types.StatusTStateType.PARTIALLYOBSERVED.toString(),
    	alma.entity.xmlbinding.valuetypes.types.StatusTStateType.CSVREADY.toString()
    };
	
    final private static String[] SBPhase2RunnableStates = {
    	alma.entity.xmlbinding.valuetypes.types.StatusTStateType.READY.toString(),              
    	alma.entity.xmlbinding.valuetypes.types.StatusTStateType.RUNNING.toString(),
    	alma.entity.xmlbinding.valuetypes.types.StatusTStateType.CSVREADY.toString(),
    	alma.entity.xmlbinding.valuetypes.types.StatusTStateType.CSVRUNNING.toString()
    };

    private static Set<String> opPhase2RunnableStates = null;
    private static Set<String> sbPhase2RunnableStates = null;
    
    static {
    	opPhase2RunnableStates = new HashSet<String>();
    	for (final String state : OPPhase2RunnableStates) {
    		opPhase2RunnableStates.add(state);
    	}
    	sbPhase2RunnableStates = new HashSet<String>();
    	for (final String state : SBPhase2RunnableStates) {
    		sbPhase2RunnableStates.add(state);
    	}
    }
    
	public Phase2XMLStoreProjectDao() throws Exception {
		super(Phase2XMLStoreProjectDao.class.getSimpleName());
	}
	
	public Phase2XMLStoreProjectDao(ContainerServices containerServices) throws AcsJContainerServicesEx, UserException {
		super(containerServices);
	}

	@Override
	protected List<ObsProject> convertAPDMProjectsToDataModel(
			ArchiveInterface archive,
			Logger           logger) {
		final APDMtoSchedulingConverter converter =
			new APDMtoSchedulingConverter(archive,
                                          APDMtoSchedulingConverter.Phase.PHASE2,
                                          logger, notifier);
		return converter.convertAPDMProjectsToDataModel();
	}

	/**
	 * Ensure that the OUSStatuses and SBStatues corresponding to the
	 * given ProjectStatus are cached in the given ArchiveInterface.
	 * 
	 * @param projectStatus
	 * @param archive
	 */
	private void getOUSandSBStatuses(ProjectStatus    projectStatus,
			                         ArchiveInterface archive) {
		
		XmlEntityStruct xml[] = null;
		try {
			xml = stateSystem.getProjectStatusList(projectStatus.getProjectStatusEntity().getEntityId());
		} catch (Exception e) {
        	logger.warning(String.format(
        			"can not pull Status objects for ProjectStatus %s from State System",
        			projectStatus.getProjectStatusEntity().getEntityId()));
            e.printStackTrace(System.out);
		}
		
		for (final XmlEntityStruct xes : xml) {
			if (xes.entityTypeName.equals(OUSStatus.class.getSimpleName())) {
				try {
					final OUSStatus status = (OUSStatus) entityDeserializer.
						deserializeEntity(xes, OUSStatus.class);
					archive.cache(status);
					logger.info(String.format(
							"Got APDM OUSStatus %s",						
							status.getOUSStatusEntity().getEntityId()));
				} catch (Exception e) {
		        	logger.warning("can not deserialise OUSStatus from State System (skipping)");
		            e.printStackTrace(System.out);
				}
			} else if (xes.entityTypeName.equals(SBStatus.class.getSimpleName())) {
				try {
					final SBStatus status = (SBStatus) entityDeserializer.
						deserializeEntity(xes, SBStatus.class);
					archive.cache(status);
					logger.info(String.format(
							"Got APDM SBStatus %s",						
							status.getSBStatusEntity().getEntityId()));
				} catch (Exception e) {
		        	logger.warning("can not deserialise SBStatus from State System (skipping)");
		            e.printStackTrace(System.out);
				}
			} else if (xes.entityTypeName.equals(ProjectStatus.class.getSimpleName())) {
				// Skip, we've already got the ProjectStatus.
			} else {
	        	logger.warning(String.format(
	        			"Unexpected entity type (%s) as child of ProjectStatus %s (skipping)",
	        			xes.entityTypeName,
	        			projectStatus.getProjectStatusEntity().getEntityId()));
			}
		}
	}

	/**
	 * Get all the relevant project statuses from the state archive.
	 * "Relevant" means having a state in which we're interested. While
	 * we're about all this, we also get the OUSStatuses and SBStatuses
	 * of the projects.
	 * 
	 * @param archive
	 * @return
	 */
	private List<ProjectStatus> fetchAppropriateAPDMProjectStatuses(
			ArchiveInterface archive) {
        final List<ProjectStatus> result = new Vector<ProjectStatus>();
        
		XmlEntityStruct xml[] = null;
		try {
			xml = stateSystem.findProjectStatusByState(OPPhase2RunnableStates);
		} catch (Exception e) {
			logger.severe(String.format(
					"Cannot get APDM ProjectStatuses - %s",
					e.getMessage()));
		}
		
		for (final XmlEntityStruct xes : xml) {
			try {
				final ProjectStatus ps = (ProjectStatus) entityDeserializer.
						deserializeEntity(xes, ProjectStatus.class);
				result.add(ps);
				archive.cache(ps);
				logger.info(String.format(
						"Got APDM ProjectStatus %s",						
						ps.getProjectStatusEntity().getEntityId()));
				getOUSandSBStatuses(ps, archive);
			} catch (Exception e) {
				logger.warning(String.format(
						"Cannot get APDM ProjectStatus - %s (skipping)",						
						e.getMessage()));
			}
		}
		
		return result;
	}

	/**
	 * Make sure that all the APDM ObsProjects corresponding to the
	 * given APDM ProjectStatuses are in the given ArchiveInterface's
	 * cache.
	 * 
	 * @param archive
	 * @param projectStatuses
	 */
	protected void getAPDMProjectsFor(ArchiveInterface    archive,
			                          List<ProjectStatus> projectStatuses) {
	
		for (ProjectStatus projectStatus : projectStatuses ) {
			alma.entity.xmlbinding.obsproject.ObsProjectRefT projectRef = projectStatus.getObsProjectRef();
			String                                           projectId = projectRef.getEntityId();
			try {
				archive.getObsProject(projectId);
				logger.info(String.format(
						"Succesfully got APDM ObsProject %s for APDM ProjectStatus %s",
						projectId,
						projectStatus.getProjectStatusEntity().getEntityId()));
			} catch (EntityException e) {
				logger.warning(String.format(
						"Cannot get APDM ObsProject %s for APDM ProjectStatus %s - %s",
						projectId,
						projectStatus.getProjectStatusEntity().getEntityId(),
						e.getMessage()));
			} catch (UserException e) {
				logger.warning(String.format(
						"Cannot get APDM ObsProject %s for APDM ProjectStatus %s - %s",
						projectId,
						projectStatus.getProjectStatusEntity().getEntityId(),
						e.getMessage()));
			}
		}
	}

	private void possiblyConvertProjects() {
		final StatusTStateType from = StatusTStateType.PHASE2SUBMITTED;
		final StatusTStateType to   = StatusTStateType.READY;
		
		ProjectImportEvent event;
		
		if (SchedulingProperties.isConvertPhase2ToReady()) {
			event = new ProjectImportEvent();
			event.setEntityId("Converting project states");
			event.setTimestamp(new Date());
			event.setStatus(ImportStatus.STATUS_INFO);
			event.setEntityType("<html><i>none</i></html>");
			event.setDetails(String.format("from %s to %s", from, to));
			getNotifer().notifyEvent(event);

			convertProjects(from, to);
		} else {
    		logger.info(String.format(
    				"on-the-fly conversion of projects from %s to %s not enabled.",
    				from, to));
			event = new ProjectImportEvent();
			event.setEntityId("Not converting project states");
			event.setTimestamp(new Date());
			event.setStatus(ImportStatus.STATUS_INFO);
			event.setEntityType("<html><i>none</i></html>");
			event.setDetails("(this test option is not enabled)");
			getNotifer().notifyEvent(event);
		}
	}
	
	@Override
	protected void getInterestingProjects(ArchiveInterface archive) {
		List<ProjectStatus> apdmProjectStatuses;
		
		possiblyConvertProjects();
		apdmProjectStatuses = fetchAppropriateAPDMProjectStatuses(archive);
		getAPDMProjectsFor(archive, apdmProjectStatuses);
	}

	@Override
	protected alma.entity.xmlbinding.obsproject.ObsUnitSetT getTopLevelOUSForProject(
			ArchiveInterface                             archive,
			alma.entity.xmlbinding.obsproject.ObsProject apdmProject) {
		return apdmProject.getObsProgram().getObsPlan();
	}

	
	/**
	 * Convert all the projects in the archive which are in state
	 * "from" to stat "to". This is a hideous frig for R7.0 to help
	 * AIV staff during commissioning. As such it is probably with
	 * us forever...
	 * 
	 * @param from - the state from which we wish to convert
	 * @param to   - the state to which we wish to convert projects
	 */
	private void convertProjects(StatusTStateType from,
								 StatusTStateType to) {
		final String[] fromStates = new String[1];
		fromStates[0] = from.toString();
		
		ProjectImportEvent event = new ProjectImportEvent();
		
    	final Collection<String> fromPSIds;
    	
    	try {
    		fromPSIds = archive.getProjectStatusIdsByState(fromStates);
		} catch (Exception e) {
			ErrorHandling.warning(
					logger,
					String.format(
							"cannot get project statuses from State Archive - %s",
							e.getMessage()),
					e);
			event = new ProjectImportEvent();
			event.setEntityId("Conversion failed");
			event.setTimestamp(new Date());
			event.setStatus(ImportStatus.STATUS_WARNING);
			event.setEntityType("<html><i>none</i></html>");
			event.setDetails(e.getMessage());
			getNotifer().notifyEvent(event);
			return;
		}
    	
    	int worked = 0;
    	int failed = 0;

    	for (final String psID : fromPSIds) {
    		try {
				stateSystem.changeProjectStatus(
						psID,
						to.toString(),
						Subsystem.SCHEDULING,
						Role.AOD);
				worked ++;
			} catch (UserException e) {
				logger.warning(String.format(
						"cannot convert project status %s from %s to %s - %s",
						psID, from, to, e.getLocalizedMessage()));
				failed ++;
			}
    	}
    	
    	String description;
    	ImportStatus status;
    	
    	if (worked + failed == 0) {
    		// there were no projects to convert
    		description = "no candidate projects found";
    		status = ImportStatus.STATUS_INFO;
    		logger.info(String.format(
    				"on-the-fly conversion of projects from %s to %s: %s.",
    				from, to, description));
    	} else if (failed == 0) {
    		// Don't admit to even the possibility of failure if you
    		// don't have to.
    		description = String.format("%d converted", worked);
    		status = ImportStatus.STATUS_INFO;
    		logger.info(String.format(
    				"on-the-fly conversion of projects from %s to %s: %s.",
    				from, to, description));
    	} else {
    		description = String.format("%d converted, %d failed", worked, failed);
    		status = ImportStatus.STATUS_WARNING;
    		logger.warning(String.format(
    				"on-the-fly conversion of projects from %s to %s: %s.",
    				from, to, description));
    	}
    	
		event = new ProjectImportEvent();
		event.setEntityId("Conversion results");
		event.setTimestamp(new Date());
		event.setStatus(status);
		event.setEntityType("<html><i>none</i></html>");
		event.setDetails(description);
		getNotifer().notifyEvent(event);

	}

	protected void getAPDMSchedBlocksFor(
			ArchiveInterface                              archive,
			alma.entity.xmlbinding.obsproject.ObsUnitSetT apdmOUS,
			Map<String, SBStatus>                         sbsByDomainId) {
		
		// Get the choice object for convenience
		final alma.entity.xmlbinding.obsproject.ObsUnitSetTChoice choice =
			apdmOUS.getObsUnitSetTChoice();

		if (choice != null) {
			// Recurse down child ObsUnitSetTs
			for (final alma.entity.xmlbinding.obsproject.ObsUnitSetT childOUS : choice.getObsUnitSet()) {
				getAPDMSchedBlocksFor(archive, childOUS, sbsByDomainId);
			}

			// Get any referred SchedBlocks which are runnable.
			for (final alma.entity.xmlbinding.schedblock.SchedBlockRefT childSBRef : choice.getSchedBlockRef()) {
				final String id = childSBRef.getEntityId();
				final alma.entity.xmlbinding.sbstatus.SBStatus sbs = sbsByDomainId.get(id);
				try {
    				final String state = sbs.getStatus().getState().toString();
    				if (sbPhase2RunnableStates.contains(state)) {
    					try {
    						archive.getSchedBlock(id);
    						logger.info(String.format(
    								"Succesfully got APDM SchedBlock %s",
    								id));
    					} catch (EntityException deserialiseEx) {
    						logger.warning(String.format(
    								"can not get APDM SchedBlock %s from XML Store - %s, (skipping it)",
    								id,
    								deserialiseEx.getMessage()));
    					} catch (UserException retrieveEx) {
    						logger.warning(String.format(
    								"can not get APDM SchedBlock %s from XML Store - %s, (skipping it)",
    								id,
    								retrieveEx.getMessage()));
    					}
    				} else {
    					logger.info(String.format(
    							"APDM SchedBlock %s is in an uninteresting state (%s) - skipping it",
    							id, state));
    				}
				} catch (NullPointerException ex) {
				    ex.printStackTrace();
				    logger.severe("null pointer exception: " + ex);
				}
			}
		} else {
			String projectLabel;
			
			if (apdmOUS.getObsProjectRef() != null) {
				projectLabel = String.format("APDM ObsProject %s", apdmOUS.getObsProjectRef().getEntityId());
			} else {
				projectLabel = "unknown APDM ObsProject";
			}

			logger.warning(String.format(
					"APDM ObsUnitSet %s in %s has no children, (skipping it)",
					apdmOUS.getEntityPartId(),
					projectLabel
			));
		}	
	}
    
    /**
	 * Ensure that all the APDM ScheBlocks that correspond to the given
	 * APDM ObsProject are cached in the given ArchiveInterface.
	 * 
     * @param archive
     * @param apdmProject
     */
	@Override
	protected void getAPDMSchedBlocksFor(
			ArchiveInterface                             archive,
			alma.entity.xmlbinding.obsproject.ObsProject apdmProject) {
		
		final alma.entity.xmlbinding.obsproject.ObsUnitSetT top =
			getTopLevelOUSForProject(archive, apdmProject);
		final String projectStatusId = apdmProject.getProjectStatusRef().getEntityId();
		final String apdmProjectId =
			apdmProject.getObsProjectEntity().getEntityId();
		final Collection<SBStatus> sbStatuses;
		
		try {
			sbStatuses = archive.getSBStatusesForProjectStatus(projectStatusId);
		} catch (Exception e) {
			ErrorHandling.warning(
					logger,
					String.format("Cannot get SBStatuses for APDM Project %s [Project Status ID: %s] - %s",
							apdmProjectId, 
							projectStatusId,
							e.getMessage()),
					e);
			return;
		}
		final Map<String, SBStatus> sbsByDomainId =
			new HashMap<String, SBStatus>();
		for (final SBStatus sbs : sbStatuses) {
			sbsByDomainId.put(sbs.getSchedBlockRef().getEntityId(), sbs);
		}
		
		getAPDMSchedBlocksFor(archive, top, sbsByDomainId);
	}

	@Override
	protected boolean interestedInObsProject(String state) {
		return opPhase2RunnableStates.contains(state);
	}

    @Override
    public void getObsProjectChanges(Date since,
            List<String> newOrModifiedIds, List<String> deletedIds)
            throws DAOException {
        possiblyConvertProjects();
        super.getObsProjectChanges(since, newOrModifiedIds, deletedIds);
    }
	
	
}
