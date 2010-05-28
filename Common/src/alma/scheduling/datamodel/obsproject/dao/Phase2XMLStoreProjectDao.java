/**
 * 
 */
package alma.scheduling.datamodel.obsproject.dao;

import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.omg.CORBA.UserException;

import alma.acs.entityutil.EntityException;
import alma.entity.xmlbinding.obsproject.ObsUnitSetT;
import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.xmlentity.XmlEntityStruct;

/**
 * @author dclarke
 *
 */
public class Phase2XMLStoreProjectDao extends AbstractXMLStoreProjectDao {
	
    final private static String[] OPPhase2RunnableStates = {
    	alma.entity.xmlbinding.valuetypes.types.StatusTStateType.PHASE2SUBMITTED.toString(),
    	alma.entity.xmlbinding.valuetypes.types.StatusTStateType.READY.toString(),              
    	alma.entity.xmlbinding.valuetypes.types.StatusTStateType.PARTIALLYOBSERVED.toString()               
    };

	public Phase2XMLStoreProjectDao() throws Exception {
		super(Phase2XMLStoreProjectDao.class.getSimpleName());
	}

	@Override
	protected List<ObsProject> convertAPDMProjectsToDataModel(
			ArchiveInterface archive,
			Logger           logger) {
		final APDMtoSchedulingConverter converter =
			new APDMtoSchedulingConverter(archive,
                                          APDMtoSchedulingConverter.Phase.PHASE2,
                                          logger);
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

	@Override
	protected void getInterestingProjects(ArchiveInterface archive) {
		List<ProjectStatus> apdmProjectStatuses;
		
		apdmProjectStatuses = fetchAppropriateAPDMProjectStatuses(archive);
		getAPDMProjectsFor(archive, apdmProjectStatuses);
	}

	@Override
	protected ObsUnitSetT getTopLevelOUSForProject(
			ArchiveInterface                             archive,
			alma.entity.xmlbinding.obsproject.ObsProject apdmProject) {
		return apdmProject.getObsProgram().getObsPlan();
	}
	
}
