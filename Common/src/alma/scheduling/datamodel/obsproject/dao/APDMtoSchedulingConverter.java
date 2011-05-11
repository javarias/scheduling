/**
 * 
 */
package alma.scheduling.datamodel.obsproject.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Logger;

import org.omg.CORBA.UserException;

import alma.acs.entityutil.EntityException;
import alma.entity.xmlbinding.obsproject.types.ObsUnitTStatusType;
import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.sbstatus.ExecStatusT;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.entity.xmlbinding.schedblock.TargetT;
import alma.entity.xmlbinding.valuetypes.types.AngleTUnitType;
import alma.entity.xmlbinding.valuetypes.types.AngularVelocityTUnitType;
import alma.entity.xmlbinding.valuetypes.types.FrequencyTUnitType;
import alma.entity.xmlbinding.valuetypes.types.SensitivityTUnitType;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.entity.xmlbinding.valuetypes.types.TimeTUnitType;
import alma.entity.xmlbinding.valuetypes.types.UserAngleTUserUnitType;
import alma.scheduling.datamodel.helpers.AngleConverter;
import alma.scheduling.datamodel.helpers.AngularVelocityConverter;
import alma.scheduling.datamodel.helpers.ConversionException;
import alma.scheduling.datamodel.helpers.FrequencyConverter;
import alma.scheduling.datamodel.helpers.SensitivityConverter;
import alma.scheduling.datamodel.helpers.TimeConverter;
import alma.scheduling.datamodel.obsproject.FieldSource;
import alma.scheduling.datamodel.obsproject.GenericObservingParameters;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnitControl;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.ObservingParameters;
import alma.scheduling.datamodel.obsproject.Preconditions;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.SchedBlockControl;
import alma.scheduling.datamodel.obsproject.SchedBlockState;
import alma.scheduling.datamodel.obsproject.SchedulingConstraints;
import alma.scheduling.datamodel.obsproject.ScienceGrade;
import alma.scheduling.datamodel.obsproject.ScienceParameters;
import alma.scheduling.datamodel.obsproject.SkyCoordinates;
import alma.scheduling.datamodel.obsproject.Target;
import alma.scheduling.datamodel.obsproject.dao.ProjectImportEvent.ImportStatus;
import alma.scheduling.utils.ErrorHandling;

/**
 * Conversion of APDM ObsProjects to the equivalents in the Scheduling
 * Data Model form.
 * 
 * @author dclarke
 *
 */
public class APDMtoSchedulingConverter {

	/*
	 * ================================================================
	 * Constants
	 * ================================================================
	 */
	private static final double defaultSBMaximumHours = 0.5;
	private static final double defaultSBEstimatedExecutionHours = 0.5;
	/* End Constants
	 * ============================================================= */

	/*
	 * ================================================================
	 * Enums
	 * ================================================================
	 */
	/** Used to control which phase of the APDM to look in. */
	public enum Phase {PHASE1, PHASE2}
	/* End Enums
	 * ============================================================= */

	/*
	 * ================================================================
	 * Fields
	 * ================================================================
	 */
	private ArchiveInterface archive;
	private Phase            phase;
	private Logger           logger;
	
	private final AbstractXMLStoreProjectDao.XMLStoreImportNotifier notifier;
	/* End Fields
	 * ============================================================= */

	
	
	
	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	/** Hide the default constructor from the outside world */
	@SuppressWarnings("unused")
	private APDMtoSchedulingConverter() {
		notifier = null;
	};
	
	/**
	 * Construct a new instance.
	 */
	public APDMtoSchedulingConverter(
			ArchiveInterface archive,
			Phase            phase,
			Logger           logger,
			AbstractXMLStoreProjectDao.XMLStoreImportNotifier notifier) {
		this.archive = archive;
		this.phase   = phase;
		this.logger  = logger;
		this.notifier = notifier;
	}
	/* End Construction
	 * ============================================================= */


	
	/*
	 * ================================================================
	 * External interface
	 * ================================================================
	 */
	/**
	 * Convert the known APDM phase 1 objects to a list of their
	 * scheduling data model versions.
	 * 
	 * @return a <code>List</code> of the data model projects
	 */
	public List<ObsProject> convertAPDMProjectsToDataModel() {
		List<ObsProject> result = new Vector<ObsProject>();
		final int numProjects = archive.numObsProjects();
		int processed = 0;
		
		for (alma.entity.xmlbinding.obsproject.ObsProject 
				apdmProject : archive.obsProjects()) {
//			final LogBuffer lb = new LogBuffer(logger);
			try {
				result.add(
						convertAPDMProjectToDataModel(
								apdmProject));
//				lb.successInfo(String.format(
//						"ObsProject %s successfully converted to Scheduling Data Model",
//						apdmProject.getObsProjectEntity().getEntityId()));
				logger.info(String.format(
						"ObsProject %s successfully converted to Scheduling Data Model",
						apdmProject.getObsProjectEntity().getEntityId()));
				ProjectImportEvent event = new ProjectImportEvent();
				event.setTimestamp(new Date());
				event.setEntityId(apdmProject.getObsProjectEntity().getEntityId());
				event.setEntityType("ObsProject");
				event.setStatus(ImportStatus.STATUS_OK);
				event.setDetails("successfully converted to Scheduling Data Model");
				notifier.notifyEvent(event);
			} catch (ConversionException e) {
//				lb.failureWarning(String.format(
//						"cannot convert APDM ObsProject %s to Scheduling Data Model - %s",
//						apdmProject.getObsProjectEntity().getEntityId(),
//						e.getMessage()));
				logger.warning(String.format(
						"cannot convert APDM ObsProject %s to Scheduling Data Model - %s",
						apdmProject.getObsProjectEntity().getEntityId(),
						e.getMessage()));
				ProjectImportEvent event = new ProjectImportEvent();
				event.setTimestamp(new Date());
				event.setEntityId(apdmProject.getObsProjectEntity().getEntityId());
				event.setEntityType("ObsProject");
				event.setStatus(ImportStatus.STATUS_ERROR);
				event.setDetails("cannot convert APDM ObsProject - " + e.getMessage());
				notifier.notifyEvent(event);
			} catch (Exception e) {
				// unexpected error, bung out the stack trace too.
//				lb.failureWarning(String.format(
//						"error converting APDM ObsProject %s to Scheduling Data Model - %s",
//						apdmProject.getObsProjectEntity().getEntityId(),
//						e.getMessage()));
				ErrorHandling.severe(logger, String.format(
						"error converting APDM ObsProject %s to Scheduling Data Model - %s",
						apdmProject.getObsProjectEntity().getEntityId(),
						e.getMessage()), e);
				ProjectImportEvent event = new ProjectImportEvent();
				event.setTimestamp(new Date());
				event.setEntityId(apdmProject.getObsProjectEntity().getEntityId());
				event.setEntityType("ObsProject");
				event.setStatus(ImportStatus.STATUS_ERROR);
				event.setDetails("cannot convert APDM ObsProject - " + e.getMessage());
				notifier.notifyEvent(event);
			}
			processed ++;
			if ((numProjects > 100) && (processed % 100 == 0)) {
				logger.info(String.format(
						"Processed %d of %d ObsProjects",
						processed, numProjects));
			}
		}
		return result;
	}
	
	/**
	 * Convert the known APDM phase 1 objects to a list of their
	 * scheduling data model versions.
	 * 
	 * @param ids - the entity ids of the projects to convert
	 * @return a <code>List</code> of the data model projects
	 */
	public List<ObsProject> convertAPDMProjectsToDataModel(String... ids) {
		List<ObsProject> result = new Vector<ObsProject>();
		for (String id : ids) {
			if (archive.hasObsProject(id)) {
				final alma.entity.xmlbinding.obsproject.ObsProject
					apdmProject = archive.cachedObsProject(id);
				try {
					result.add(
							convertAPDMProjectToDataModel(
									apdmProject));
				} catch (ConversionException e) {
					logger.warning(String.format(
						"cannot convert APDM ObsProject %s to Scheduling Data Model - %s",
						apdmProject.getObsProjectEntity().getEntityId(),
						e.getMessage()));
				}
			} else {
				logger.warning(String.format(
						"cannot convert APDM ObsProject %s to Scheduling Data Model - %s",
						id,
						"unknown project"));
			}
		}
		return result;
	}
		
	/**
	 * Convert the known APDM phase 1 objects to a list of their
	 * scheduling data model versions.
	 * 
	 * @param ids - the entity ids of the projects to convert
	 * @return a <code>List</code> of the data model projects
	 */
	public List<ObsProject> convertAPDMProjectsToDataModel(Collection<String> ids) {
		return convertAPDMProjectsToDataModel(ids.toArray(new String[0]));
	}
	/* End External interface
	 * ============================================================= */


	
	/*
	 * ================================================================
	 * Conversion of projects
	 * ================================================================
	 */
	private ObsProject convertAPDMProjectToDataModel(
			alma.entity.xmlbinding.obsproject.ObsProject apdmProject)
		throws ConversionException {
		
		// Create the result
		ObsProject obsProject = new ObsProject();
		
		{
			// Everything in this block is about logging.
			String projectId = "<<didn't get far enough to set>>";
			String statusId  = "<<didn't get far enough to set>>";
			String programId = "<<didn't get far enough to set>>";
			
			alma.entity.xmlbinding.obsproject.ObsProjectEntityT
					opEnt = apdmProject.getObsProjectEntity();
			if (opEnt != null) {
				projectId = opEnt.getEntityId();
				if (projectId == null) {
					projectId = "<<APDM ObsProject has null entity id>>";
				}
			} else {
				projectId = "<<APDM ObsProject has no entity object>>";
			}

			logger.info(String.format(
					"Processing project %s, is %sin dictionary",
					projectId,
					archive.hasObsProject(projectId)? "": "NOT "));

			alma.entity.xmlbinding.projectstatus.ProjectStatusRefT
					psRef = apdmProject.getProjectStatusRef();
			if (psRef != null) {
				statusId = psRef.getEntityId();
				if (statusId == null) {
					statusId = "<<APDM ObsProject has null project status id>>";
				}
			} else {
				statusId = "<<APDM ObsProject has no project status ref object>>";
			}
			logger.info(String.format(
					"... project status id %s, is %sin dictionary",
					statusId,
					archive.hasProjectStatus(statusId)? "": "NOT "));
			
			if (archive.hasProjectStatus(statusId)) {
				ProjectStatus
						projectStatus = archive.cachedProjectStatus(statusId);
				alma.entity.xmlbinding.ousstatus.OUSStatusRefT
						sRef = projectStatus.getObsProgramStatusRef();
				if (sRef != null) {
					programId = sRef.getEntityId();
					if (programId == null) {
						programId = "<<APDM ObsProject' program status ref has null entity id>>";
					}
				} else {
					programId = "<<APDM ObsProject has no program status ref object>>";
				}

				logger.info(String.format(
					"... program status %s is %sin dictionary",
					programId,
					archive.hasOUSStatus(programId)? "": "NOT "));
			} else {
				logger.info("... therefore cannot get program status");
			}
		}
		
		
		// Fill in the top level object
		
		/* obsProject.setId() - No need, it is handled by Hibernate */
		if (phase == Phase.PHASE1) {
			obsProject.setUid(apdmProject.getObsProposalRef().getEntityId());
		} else { 
			obsProject.setUid(apdmProject.getObsProjectEntity().getEntityId());
		}
		obsProject.setCode(apdmProject.getCode());
		obsProject.setName(apdmProject.getProjectName());
		obsProject.setVersion(apdmProject.getVersion());
		obsProject.setPrincipalInvestigator(apdmProject.getPI());
		obsProject.setScienceRank(apdmProject.getScientificRank());
		obsProject.setScienceScore((float)apdmProject.getScientificScore());
		obsProject.setLetterGrade(ScienceGrade.valueOf(apdmProject.getLetterGrade()));
		obsProject.setCsv(apdmProject.getIsCommissioning());
		obsProject.setManual(apdmProject.getManualMode());
		
		if (phase == Phase.PHASE1) {
			obsProject.setStatus(
					alma.entity.xmlbinding.valuetypes.types.StatusTStateType.READY.toString());
			obsProject.setTotalExecutionTime(0.0);
		} else {
			ProjectStatus projectStatus = archive.cachedProjectStatus(
					apdmProject.getProjectStatusRef().getEntityId());
			OUSStatus programStatus = archive.cachedOUSStatus(
					projectStatus.getObsProgramStatusRef().getEntityId());

			obsProject.setStatus(
					projectStatus.getStatus().getState().toString());
			obsProject.setTotalExecutionTime(
					programStatus.getTotalUsedTimeInSec()*1.0);
			obsProject.setStatusEntity(projectStatus.getProjectStatusEntity());
		}
		
		// Shuffle down the structure creating it
		alma.entity.xmlbinding.obsproject.ObsUnitSetT apdmOUS;
		
		alma.entity.xmlbinding.obsproposal.ObsProposalRefT proposalRef = apdmProject
				.getObsProposalRef();
		alma.entity.xmlbinding.obsproposal.ObsProposal apdmProposal = null;
		try {
			apdmProposal = archive.getObsProposal(proposalRef.getEntityId());
		} catch (Exception e1) {
			throw new ConversionException(String.format(
					"Error getting APDM ObsProposal %s for APDM Obs Project %s - %s",
					proposalRef.getEntityId(),
					obsProject.getUid(),
					e1.getMessage()));
		}
		if (phase == Phase.PHASE1) {
			apdmOUS = apdmProposal.getObsPlan();
			logger.info("Proposal with uid: " + obsProject.getId() + "  had a status in ObsPlan of :" + apdmProposal.getObsPlan().getStatus().toString() );
		} else {
			apdmOUS = apdmProject.getObsProgram().getObsPlan();
		}
		
		try {
			obsProject.setAffiliation(apdmProposal.getPrincipalInvestigator().getAssociatedArc().toString());
		} catch (java.lang.NullPointerException e) {
			throw new ConversionException(String.format(
					"No PI information in APDM ObsProposal %s for APDM Obs Project %s",
					proposalRef.getEntityId(),
					obsProject.getUid()));
		}
		
		ObsUnitSet obsProgram = createObsUnitSet(
				apdmOUS,
				apdmProject,
				obsProject);
		obsProject.setObsUnit(obsProgram);
		obsProgram.setProject(obsProject);
		
		// Return the result
		return obsProject;
	}

	private ObsUnitSet createObsUnitSet(
			alma.entity.xmlbinding.obsproject.ObsUnitSetT apdmOUS,
			alma.entity.xmlbinding.obsproject.ObsProject  apdmProject,
			ObsProject                                    obsProject)
		throws ConversionException {
		
		// Create the result
		ObsUnitControl obsUnitControl = new ObsUnitControl();
		ObsUnitSet     obsUnitSet     = new ObsUnitSet();
		{
			// Everything in this block is about logging.
			String ousPartId = apdmOUS.getEntityPartId();
			String statusId  = "<<didn't get far enough to set>>";
			
			if (ousPartId == null) {
				ousPartId = "<<APDM ObsUnitSet has null entity id>>";
			}

			logger.info(String.format(
					"Processing OUS %s",
					ousPartId));

			alma.entity.xmlbinding.ousstatus.OUSStatusRefT
					oussRef = apdmOUS.getOUSStatusRef();
			if (oussRef != null) {
				statusId = oussRef.getEntityId();
				if (statusId == null) {
					statusId = "<<APDM ObsUnitSet has null ous status id>>";
				}
			} else {
				statusId = "<<APDM ObsUnitSet has no ous status ref object>>";
			}
			logger.info(String.format(
					"... OUSStatus id %s, is %sin dictionary",
					statusId,
					archive.hasOUSStatus(statusId)? "": "NOT "));
		}

		
		// Fill in the top level objects
		/* obsUnitSet.setId() - No need, it is handled by Hibernate */
		obsUnitSet.setObsUnitControl(obsUnitControl);
		/* obsUnitSet.setparent() - Handled by the calling code */
		obsUnitSet.setProject(obsProject);

		
		// Shuffle down the structure creating it
		// Firstly, any child ObsUnitSets.
		alma.entity.xmlbinding.obsproject.ObsUnitSetTChoice
				choice = apdmOUS.getObsUnitSetTChoice();
		
		if (choice == null) {
			ProjectImportEvent event = new ProjectImportEvent();
			event.setTimestamp(new Date());
			event.setEntityId(apdmOUS.getEntityPartId());
			event.setEntityType("ObsUnitSet");
			event.setStatus(ImportStatus.STATUS_ERROR);
			event.setDetails("ObsUnitSet has no children");
			notifier.notifyEvent(event);
			
			throw new ConversionException(String.format(
					"APDM ObsUnitSet %s of APDM ObsProject %s has no children",
					apdmOUS.getEntityPartId(),
					apdmProject.getObsProjectEntity().getEntityId()));
		}
		for (alma.entity.xmlbinding.obsproject.ObsUnitSetT
					childOUS : choice.getObsUnitSet()) {
			ObsUnitSet child = createObsUnitSet(
					childOUS,
					apdmProject,
					obsProject);
			obsUnitSet.addObsUnit(child);
		}
		
		// Secondly, any child SchedBlocks.
		for (alma.entity.xmlbinding.schedblock.SchedBlockRefT
					childSBref : choice.getSchedBlockRef()) {
			String childDomainUID = childSBref.getEntityId();
			if (archive.hasSchedBlock(childDomainUID)) {
				final alma.entity.xmlbinding.schedblock.SchedBlock
						apdmSB = archive.cachedSchedBlock(childDomainUID);
				try {
					SchedBlock schedBlock = createSchedBlock(
							apdmSB,
							apdmOUS,
							apdmProject,
							obsProject);
					obsUnitSet.addObsUnit(schedBlock);
					ProjectImportEvent event = new ProjectImportEvent();
					event.setTimestamp(new Date());
					event.setEntityId(apdmSB.getSchedBlockEntity().getEntityId());
					event.setEntityType("SchedBlock");
					event.setStatus(ImportStatus.STATUS_OK);
					event.setDetails("successfully converted APDM SchedBlock" );
					notifier.notifyEvent(event);
					
				} catch (ConversionException e) {
					logger.info(String.format(
							"Skipping SchedBlock %s in Project %s - %s",
							apdmSB.getSchedBlockEntity().getEntityId(),
							apdmProject.getObsProjectEntity().getEntityId(),
							e.getMessage()));
					ProjectImportEvent event = new ProjectImportEvent();
					event.setTimestamp(new Date());
					event.setEntityId(apdmSB.getSchedBlockEntity().getEntityId());
					event.setEntityType("SchedBlock");
					event.setStatus(ImportStatus.STATUS_ERROR);
					event.setDetails("cannot convert APDM SchedBlock - " + e.getMessage());
					notifier.notifyEvent(event);
				}
			} else {
				// No SchedBlock for the child, skip gently over it.
				logger.info(String.format(
					"No APDM SchedBlock %s for ObsUnitSet %s of APDM ObsProject %s - skipping",
					childDomainUID,
					apdmOUS.getEntityPartId(),
					apdmProject.getObsProjectEntity().getEntityId()));
			}
		}
		
		alma.entity.xmlbinding.ousstatus.OUSStatusRefT oussRef = apdmOUS
				.getOUSStatusRef();
		try {
			if (phase == Phase.PHASE2) {
				OUSStatus ousStatus = archive.getOUSStatus(oussRef
						.getEntityId());
				obsUnitSet.setStatusEntity(ousStatus.getOUSStatusEntity());
			}
		} catch (EntityException e) {
			throw new ConversionException(e);
		} catch (UserException e) {
			throw new ConversionException(e);
		}
		
		// Return the result
		return obsUnitSet;
	}

	private SchedBlock createSchedBlock(
			alma.entity.xmlbinding.schedblock.SchedBlock  apdmSB,
			alma.entity.xmlbinding.obsproject.ObsUnitSetT apdmOUS,
			alma.entity.xmlbinding.obsproject.ObsProject  apdmProject,
			ObsProject                                    obsProject)
		throws ConversionException {
		
		// Create the result
		SchedBlock schedBlock = new SchedBlock();
		
		{
			// Everything in this block is about logging.
			String sbId = "<<didn't get far enough to set>>";
			String statusId  = "<<didn't get far enough to set>>";

			alma.entity.xmlbinding.schedblock.SchedBlockEntityT
					sbEnt = apdmSB.getSchedBlockEntity();
			if (sbEnt != null) {
				sbId = sbEnt.getEntityId();
				if (sbId == null) {
					sbId = "<<APDM SchedBlock has null entity id>>";
				}
			} else {
				sbId = "<<APDM SchedBlock has no entity object>>";
			}
			logger.info(String.format(
					"Processing sched block %s, is %sin dictionary",
					sbId,
					archive.hasSchedBlock(sbId)? "": "NOT "));

			alma.entity.xmlbinding.sbstatus.SBStatusRefT
					sbsRef = apdmSB.getSBStatusRef();
			if (sbsRef != null) {
				statusId = sbsRef.getEntityId();
				if (statusId == null) {
					statusId = "<<APDM SchedBlock has null sb status id>>";
				}
			} else {
				statusId = "<<APDM SchedBlock has no sb status ref object>>";
			}
			logger.info(String.format(
					"... SBStatus id %s, is %sin dictionary",
					statusId,
					archive.hasSBStatus(statusId)? "": "NOT "));
		}

		// Fill in the top level object
		
		// schedBlock.setPiName(apdmSB.getPIName());
		schedBlock.setPiName(obsProject.getPrincipalInvestigator());
		schedBlock.setNote(apdmSB.getNote());
		schedBlock.setUid(apdmSB.getSchedBlockEntity().getEntityId());
		schedBlock.setRunQuicklook(apdmSB.getSchedBlockControl().getRunQuicklook());
		schedBlock.setProjectUid(obsProject.getUid());
		schedBlock.setCsv(obsProject.getCsv());
		schedBlock.setManual(obsProject.getManual());
		schedBlock.setName(apdmSB.getName());
		
		// Create objects which hang off the top level SchedBlock, and
		// hang them off it.
		ObsUnitControl obsUnitControl;
		Preconditions preconditions;
		SchedulingConstraints schedulingConstraints;
		SchedBlockControl schedBlockControl;
		
		try {
			obsUnitControl = createObsUnitControl(
					apdmSB.getObsUnitControl(),
					apdmSB.getSchedBlockEntity().getEntityId(),
					defaultSBMaximumHours,
					defaultSBEstimatedExecutionHours);
			schedBlock.setObsUnitControl(obsUnitControl);
		} catch (ConversionException e) {
			logger.info(String.format(
					"Cannot create ObsUnitControl object for SchedBlock %s in Project %s - %s",
					apdmSB.getSchedBlockEntity().getEntityId(),
					apdmProject.getObsProjectEntity().getEntityId(),
					e.getMessage()));
			ProjectImportEvent event = new ProjectImportEvent();
			event.setTimestamp(new Date());
			event.setEntityId(apdmSB.getSchedBlockEntity().getEntityId());
			event.setEntityType("SchedBlock");
			event.setStatus(ImportStatus.STATUS_ERROR);
			event.setDetails("Cannot create ObsUnitControl object for SchedBlock" );
			notifier.notifyEvent(event);
			throw e;
		}
		try {
			preconditions = createPreconditions(
					apdmSB.getPreconditions());
			schedBlock.setPreConditions(preconditions);
		} catch (ConversionException e) {
			logger.info(String.format(
					"Cannot create Preconditions object for SchedBlock %s in Project %s - %s",
					apdmSB.getSchedBlockEntity().getEntityId(),
					apdmProject.getObsProjectEntity().getEntityId(),
					e.getMessage()));
			ProjectImportEvent event = new ProjectImportEvent();
			event.setTimestamp(new Date());
			event.setEntityId(apdmSB.getSchedBlockEntity().getEntityId());
			event.setEntityType("SchedBlock");
			event.setStatus(ImportStatus.STATUS_ERROR);
			event.setDetails("Cannot create Preconditions object for SchedBlock" );
			notifier.notifyEvent(event);
			throw e;
		}
		try {
			schedulingConstraints = createSchedulingConstraints(
					apdmSB.getSchedulingConstraints());
			schedBlock.setSchedulingConstraints(schedulingConstraints);
		} catch (ConversionException e) {
			logger.info(String.format(
				"Cannot create SchedulingConstraints object for SchedBlock %s in Project %s - %s",
				apdmSB.getSchedBlockEntity().getEntityId(),
				apdmProject.getObsProjectEntity().getEntityId(),
				e.getMessage()));
			ProjectImportEvent event = new ProjectImportEvent();
			event.setTimestamp(new Date());
			event.setEntityId(apdmSB.getSchedBlockEntity().getEntityId());
			event.setEntityType("SchedBlock");
			event.setStatus(ImportStatus.STATUS_ERROR);
			event.setDetails("Cannot create SchedulingConstraints object for SchedBlock");
			notifier.notifyEvent(event);
			throw e;
		}
		final alma.entity.xmlbinding.sbstatus.SBStatus sbStatus;
		try {
			sbStatus = (phase == Phase.PHASE1) ? null
					: archive.cachedSBStatus(apdmSB.getSBStatusRef()
							.getEntityId());
			schedBlockControl = createSchedBlockControl(
					apdmSB.getSchedBlockControl(), sbStatus);
			schedBlock.setSchedBlockControl(schedBlockControl);
			if (sbStatus != null) {
				schedBlock.setStatusEntity(sbStatus.getSBStatusEntity());
			}
		} catch (ConversionException e) {
			logger.info(String.format(
				"Cannot create SchedBlockControl object for SchedBlock %s in Project %s - %s",
				apdmSB.getSchedBlockEntity().getEntityId(),
				apdmProject.getObsProjectEntity().getEntityId(),
				e.getMessage()));
			ProjectImportEvent event = new ProjectImportEvent();
			event.setTimestamp(new Date());
			event.setEntityId(apdmSB.getSchedBlockEntity().getEntityId());
			event.setEntityType("SchedBlock");
			event.setStatus(ImportStatus.STATUS_ERROR);
			event.setDetails("Cannot create SchedBlockControl object for SchedBlock");
			notifier.notifyEvent(event);
			throw e;
		}
		
		// Now, all the Target stuff.
		Map<String, ObservingParameters> observingParameters =
			createAllObservingParameters(apdmSB);
		Map<String, FieldSource> fieldSources =
			createAllFieldSources(apdmSB);
		Map<String, Target> targets =
			createAllTargets(apdmSB, observingParameters, fieldSources);
		
		for (ObservingParameters op : observingParameters.values()) {
			schedBlock.addObservingParameters(op);
		}
		
		for (Target t : targets.values()) {
			schedBlock.addTarget(t);
		}
		
		// Dig around for the representative target.
		try {
			alma.entity.xmlbinding.schedblock.SchedulingConstraintsT
					constraints = apdmSB.getSchedulingConstraints();
			if (constraints != null) {
				alma.entity.xmlbinding.schedblock.SchedBlockRefT
				representativeTargetRef =
					constraints.getRepresentativeTargetRef();
				if (representativeTargetRef != null) {
					String representativeTargetId =
						representativeTargetRef.getPartId();
					Target representativeTarget =
						targets.get(representativeTargetId);
					if (representativeTarget != null) {
						schedBlock.getSchedulingConstraints()
							.setRepresentativeTarget(representativeTarget);
					} else {
						throw new ConversionException(
									"Cannot find representative target");
					}
				} else {
					throw new ConversionException(
								"No representative target reference object");
				}
			} else {
				throw new ConversionException(
							"No scheduling constraints (want to look in them for the representative target)");
			}
		} catch (ConversionException e) {
			logger.warning(String.format(
					"%s in SchedBlock %s",
					e.getMessage(), apdmSB.getSchedBlockEntity().getEntityId()));
		}
		if (phase == Phase.PHASE2) {
			// Check if the SB has the right state
			if (schedBlock.getCsv()) {
				if (!(sbStatus.getStatus().getState().toString()
						.contains(ObsUnitTStatusType.CSVREADY.toString()) || 
						sbStatus.getStatus().getState().toString()
						.contains(ObsUnitTStatusType.CSVRUNNING.toString()))) {
					logger.warning("SchedBlock "
							+ apdmSB.getSchedBlockEntity().getEntityId()
							+ " is CSV, but the SB status is not CSV compatible: "
							+ apdmSB.getStatus().toString());
//					ProjectImportEvent event = new ProjectImportEvent();
//					event.setTimestamp(new Date());
//					event.setEntityId(apdmSB.getSchedBlockEntity()
//							.getEntityId());
//					event.setEntityType("SchedBlock");
//					event.setStatus(ImportStatus.STATUS_ERROR);
//					event.setDetails("SchedBlock is CSV, but SB status is not CSV compatible: "
//							+ apdmSB.getStatus().toString());
//					notifier.notifyEvent(event);
//
//					ConversionException e = new ConversionException(
//							"SchedBlock "
//									+ apdmSB.getSchedBlockEntity()
//											.getEntityId()
//									+ " is CSV, but the SB status is not CSV compatible: "
//									+ apdmSB.getStatus().toString());
//					throw e;
				}
			} else {
				if (!(sbStatus.getStatus().getState().toString()
						.contains(ObsUnitTStatusType.READY.toString()) || 
						sbStatus.getStatus().getState().toString()
						.contains(ObsUnitTStatusType.RUNNING.toString()))) {
					logger.warning("SchedBlock "
							+ apdmSB.getSchedBlockEntity().getEntityId()
							+ " has a not compatible SB status: "
							+ apdmSB.getStatus().toString());
//					ProjectImportEvent event = new ProjectImportEvent();
//					event.setTimestamp(new Date());
//					event.setEntityId(apdmSB.getSchedBlockEntity()
//							.getEntityId());
//					event.setEntityType("SchedBlock");
//					event.setStatus(ImportStatus.STATUS_ERROR);
//					event.setDetails("SB Status is not compatible: "
//							+ apdmSB.getStatus().toString());
//					notifier.notifyEvent(event);
//
//					ConversionException e = new ConversionException(
//							"SchedBlock "
//									+ apdmSB.getSchedBlockEntity()
//											.getEntityId()
//									+ " has a not compatible SB status: "
//									+ apdmSB.getStatus().toString());
//					throw e;
				}
			}
		}
		// Return the result
		return schedBlock;
	}

	private ObsUnitControl createObsUnitControl(
			alma.entity.xmlbinding.obsproject.ObsUnitControlT obsUnitControl,
			String                                            apdmSBId,
			double                                            defaultSBMaximumHours,
			double                                            defaultSBEstimatedExecutionHours)
		throws ConversionException {
		ObsUnitControl result = new ObsUnitControl();
		
		try {
			result.setMaximumTime(TimeConverter.convertedValue(
					obsUnitControl.getMaximumTime(),
					TimeTUnitType.H));
		} catch (NullPointerException e) {
			logger.warning(String.format(
					"Missing Maximum Time in ObsUnitControl for SchedBlock %s, using default of %f%s",
					apdmSBId, defaultSBMaximumHours,
					TimeTUnitType.H));
			result.setMaximumTime(defaultSBMaximumHours);
		}
		
		try {
			result.setEstimatedExecutionTime(TimeConverter.convertedValue(
					obsUnitControl.getEstimatedExecutionTime(),
					TimeTUnitType.H));
		} catch (NullPointerException e) {
			logger.warning(String.format(
					"Missing Estimated Execution Time in ObsUnitControl for SchedBlock %s, using default of %f%s",
					apdmSBId,
					defaultSBEstimatedExecutionHours,
					TimeTUnitType.H));
			result.setEstimatedExecutionTime(defaultSBEstimatedExecutionHours);
		}

		if (result.getEstimatedExecutionTime() <= 0.0) {
			String msg = String.format("Estimated Execution Time in ObsUnitControl for SchedBlock %s should be > 0.0, current value %f, using default of %f%s",
					apdmSBId,
					result.getEstimatedExecutionTime(),
					defaultSBEstimatedExecutionHours,
					TimeTUnitType.H);
			logger.warning(msg);
			result.setEstimatedExecutionTime(defaultSBEstimatedExecutionHours);			
		}
		if (result.getMaximumTime() < result.getEstimatedExecutionTime()) {
			String msg = String.format("Maximum Time in ObsUnitControl for SchedBlock %s should be >= Estimated Execution Time (%f%s), current value %f, setting to %f%s",
					apdmSBId,
					result.getEstimatedExecutionTime(),
					TimeTUnitType.H,
					result.getMaximumTime(),
					result.getEstimatedExecutionTime(),
					TimeTUnitType.H);
			logger.warning(msg);
			result.setMaximumTime(result.getEstimatedExecutionTime());
		}
		return result;
	}

	private Preconditions createPreconditions(
			alma.entity.xmlbinding.obsproject.PreconditionsT preconditions)
		throws ConversionException {
		Preconditions result = new Preconditions();
		
		result.setMinAllowedHourAngle(AngleConverter.convertedValue(
				preconditions.getMinAllowedHA(),
				UserAngleTUserUnitType.H));
		result.setMaxAllowedHourAngle(AngleConverter.convertedValue(
				preconditions.getMaxAllowedHA(),
				UserAngleTUserUnitType.H));
		return result;
	}

	private SchedulingConstraints createSchedulingConstraints(
			alma.entity.xmlbinding.schedblock.SchedulingConstraintsT constraints)
		throws ConversionException {
		SchedulingConstraints result = new SchedulingConstraints();
		
		result.setMaxAngularResolution(AngleConverter.convertedValue(
				constraints.getMaxAcceptableAngResolution(),
				AngleTUnitType.ARCSEC));
		result.setRepresentativeFrequency(FrequencyConverter.convertedValue(
				constraints.getRepresentativeFrequency(),
				FrequencyTUnitType.GHZ));
		return result;
	}

	private SchedBlockControl createSchedBlockControl(
			alma.entity.xmlbinding.schedblock.SchedBlockControlT control,
			alma.entity.xmlbinding.sbstatus.SBStatus             sbStatus)
		throws ConversionException {
		SchedBlockControl result = new SchedBlockControl();
		
		logger.fine(String.format(
				"createSchedBlockControl(SchedBlockControlT%s, SBStatus %s)",
				(control == null)?
						"(null!)": "",
				(sbStatus == null)?
						"null!": sbStatus.getSBStatusEntity().getEntityId()));

		if ((sbStatus == null) && (phase == Phase.PHASE2)) {
			throw new ConversionException(String.format(
					"missing SBStatus (required to convert SchedBlockControl)"));
		}

		result.setAchievedSensitivity(0.0); // TODO: model conversion
		result.setIndefiniteRepeat(control.getIndefiniteRepeat());
		result.setSbMaximumTime(TimeConverter.convertedValue(
								control.getSBMaximumTime(),
								TimeTUnitType.H));

		if (sbStatus == null) {
			result.setAccumulatedExecutionTime(0.0);
			result.setExecutionCount(0);
			result.setState(SchedBlockState.READY);
		} else {
			result.setAccumulatedExecutionTime(sbStatus.getTotalUsedTimeInSec()/3600.0);
			result.setExecutionCount(sbExecutionCount(sbStatus));
			result.setState(sbState(sbStatus));
		}
		logger.fine("ended createSchedBlockControl(...)");

		return result;
	}

	/**
	 * Create all the data model ObservingParameters for the given
	 * SchedBlock. Returns a map keyed by APDM part id for an
	 * ObservingParametersT to their Scheduling data model
	 * ObservingParameters equivalent. If there is a problem creating
	 * the data model ObservingParameters for an APDM
	 * ObservingParametersT then it is skipped (with a log message
	 *  being put out, of course)
	 *  
	 * @param apdmSB
	 * @return Map<String, ObservingParameters>
	 */
	private Map<String, ObservingParameters> createAllObservingParameters(
			alma.entity.xmlbinding.schedblock.SchedBlock apdmSB) {
		
		Map<String, ObservingParameters> result
				= new TreeMap<String, ObservingParameters>();
		
		for (alma.entity.xmlbinding.schedblock.SchedBlockChoice2
				outer : apdmSB.getSchedBlockChoice2()) {
			for (alma.entity.xmlbinding.schedblock.SchedBlockChoice2Item
					inner : outer.getSchedBlockChoice2Item()) {

				alma.entity.xmlbinding.schedblock.ScienceParametersT
					scienceParameters = inner.getScienceParameters();
				
				if (scienceParameters != null) {
					ScienceParameters sp;
					String name   = scienceParameters.getName();
					String partId = scienceParameters.getEntityPartId();

					try {
						sp = createScienceParameters(scienceParameters);
						result.put(partId, sp);
						logger.info(String.format(
							"Converted APDM Science Parameters %s (%s) in SchedBlock %s",
							name, partId,
							apdmSB.getSchedBlockEntity().getEntityId()));
					} catch (ConversionException e) {
						logger.info(String.format(
							"Cannot convert APDM Science Parameters %s (%s) in SchedBlock %s - %s, skipping",
							name, partId,
							apdmSB.getSchedBlockEntity().getEntityId(),
							e.getMessage()));
					}
				} else {
					alma.entity.xmlbinding.schedblock.ObservingParametersT
							observingParameters = getAPDMObservingParameters(inner);
					if (observingParameters != null) {
						String name   = observingParameters.getName();
						String partId = observingParameters.getEntityPartId();

						try {
							GenericObservingParameters gp = createGenericObservingParameters(workOutType(inner));
							result.put(partId, gp);
							logger.info(String.format(
									"Converted APDM %s Parameters %s (%s) in SchedBlock %s",
									gp.getType(),
									name, partId,
									apdmSB.getSchedBlockEntity().getEntityId()));
						} catch (ConversionException e) {
							logger.info(String.format(
									"Cannot convert APDM %s Parameters %s (%s) in SchedBlock %s - %s, skipping",
									workOutType(inner),
									name, partId,
									apdmSB.getSchedBlockEntity().getEntityId(),
									e.getMessage()));
						}
					} else {
						logger.info("Cannot convert APDM Observing Parameters in SchedBlock - unknown type, skipping");
					}
				}
			}
		}
		return result;
	}

	/**
	 * Create all the data model FieldSources for the given SchedBlock.
	 * Returns a map keyed by APDM part id for a FieldSourceT to their
	 * Scheduling data model FieldSource equivalent. If there is a
	 * problem creating the data model FieldSource for an APDM 
	 * FieldSourceT then it is skipped (with a log message being put
	 * out, of course)
	 *  
	 * @param apdmSB
	 * @return Map<String, FieldSource>
	 */
	private Map<String, FieldSource> createAllFieldSources(
			alma.entity.xmlbinding.schedblock.SchedBlock apdmSB) {
		
		Map<String, FieldSource> result
				= new TreeMap<String, FieldSource>();
		
		for (alma.entity.xmlbinding.schedblock.FieldSourceT
				apdmFS : apdmSB.getFieldSource()) {
			String partId = apdmFS.getEntityPartId();
			try {
				FieldSource fieldSource = createFieldSource(apdmFS);
				result.put(partId, fieldSource);
			} catch (ConversionException e) {
				logger.info(String.format(
						"Cannot convert APDM FieldSource %s in SchedBlock %s - %s, skipping",
						partId,
						apdmSB.getSchedBlockEntity().getEntityId(),
						e.getMessage()));
			}
		}
		return result;
	}

	/**
	 * Create all the data model Targets for the given SchedBlock.
	 * Returns a map keyed by APDM part id for a TargetT to their
	 * Scheduling data model Target equivalent. If there is a problem
	 * creating the data model Target for an APDM TargetT then it is
	 * skipped (with a log message being put out, of course)
	 *  
	 * @param apdmSB
	 * @return Map<String, Target>
	 */
	private Map<String, Target> createAllTargets(
		alma.entity.xmlbinding.schedblock.SchedBlock apdmSB,
		Map<String, ObservingParameters>             observingParameters,
		Map<String, FieldSource>                     fieldSources) {
		
		Map<String, Target> result = new TreeMap<String, Target>();
		
		for (alma.entity.xmlbinding.schedblock.TargetT
				apdmTarget : apdmSB.getTarget()) {
			String partId = apdmTarget.getEntityPartId();
			try {
				Target target = createTarget(apdmTarget,
						                     observingParameters,
						                     fieldSources);
				result.put(partId, target);
				logger.info(String.format(
						"Converted APDM Target %s in SchedBlock %s",
						partId,
						apdmSB.getSchedBlockEntity().getEntityId()));
			} catch (ConversionException e) {
				logger.info(String.format(
						"Cannot convert APDM Target %s in SchedBlock %s - %s, skipping",
						partId,
						apdmSB.getSchedBlockEntity().getEntityId(),
						e.getMessage()));
			}
		}
		return result;
	}

	private ScienceParameters createScienceParameters(
			alma.entity.xmlbinding.schedblock.ScienceParametersT parameters)
		throws ConversionException {
		ScienceParameters result = new ScienceParameters();
		
		result.setRepresentativeBandwidth(
			FrequencyConverter.convertedValue(
				parameters.getRepresentativeBandwidth(),
				FrequencyTUnitType.GHZ));
		result.setRepresentativeFrequency(
			FrequencyConverter.convertedValue(
				parameters.getRepresentativeFrequency(),
				FrequencyTUnitType.GHZ));
		result.setSensitivityGoal(
			SensitivityConverter.convertedValue(
				parameters.getSensitivityGoal(),
				SensitivityTUnitType.JY));

		return result;
	}

	private GenericObservingParameters createGenericObservingParameters(
			String type)
		throws ConversionException {
		
		GenericObservingParameters result = new GenericObservingParameters();
		
		result.setType(type);

		return result;
	}

	private FieldSource createFieldSource(
			alma.entity.xmlbinding.schedblock.FieldSourceT fieldSource)
		throws ConversionException {
		FieldSource result = new FieldSource();
		
		if (fieldSource.getIsQuery()) {
			// Skip over virtual targets for now
			throw new ConversionException("Query field sources are not supported yet");
		}

		result.setCoordinates(createSkyCoordinates(
				fieldSource.getSourceCoordinates()));
// Ephemeris could be a long string		
//		result.setEphemeris(fieldSource.getSourceEphemeris());
		result.setName(fieldSource.getName());
		result.setPmRA(AngularVelocityConverter.convertedValue(
				fieldSource.getPMRA(),
				AngularVelocityTUnitType.DEG_S));
		result.setPmDec(AngularVelocityConverter.convertedValue(
				fieldSource.getPMDec(),
				AngularVelocityTUnitType.DEG_S));
		return result;
	}

	private SkyCoordinates createSkyCoordinates(
			alma.entity.xmlbinding.valuetypes.SkyCoordinatesT skyCoordinates)
		throws ConversionException {
		SkyCoordinates result = new SkyCoordinates();
		
		result.setRA(AngleConverter.convertedValue(
				skyCoordinates.getLongitude(),
				AngleTUnitType.DEG));
		result.setDec(AngleConverter.convertedValue(
				skyCoordinates.getLatitude(),
				AngleTUnitType.DEG));
		return result;
	}
	
	private Target createTarget(
			TargetT                          apdmTarget,
			Map<String, ObservingParameters> observingParameters,
			Map<String, FieldSource>         fieldSources)
		throws ConversionException {
		Target result = new Target();
		
		// Find the field source. If it's not there (due to not having
		// been created) then skip the whole Target.
		FieldSource fs = fieldSources.get(
				apdmTarget.getFieldSourceRef().getPartId());
		
		if (fs == null) {
			throw new ConversionException(String.format(
				"field source %s missing (look in earlier logs for reasons)",
				apdmTarget.getFieldSourceRef().getPartId()));
		}
		
		result.setSource(fs);
		
		for (alma.entity.xmlbinding.schedblock.SchedBlockRefT
				ref : apdmTarget.getObservingParametersRef()) {
			String partId = ref.getPartId();
			ObservingParameters op = observingParameters.get(partId);
			if (op != null) {
				result.addObservingParameters(op);
			}
		}
		
		if (result.getObservingParameters().isEmpty()) {
			// No observing parameters were found
			throw new ConversionException(String.format(
					"All observing parameters missing for Target %s (look in earlier logs for reasons)",
					apdmTarget.getEntityPartId()));
		}

		return result;
	}
	/* End Conversion of projects
	 * ============================================================= */


	
	/*
	 * ================================================================
	 * Utils
	 * ================================================================
	 */
	private int sbExecutionCount(SBStatus sbStatus) {
		int result = 0;
		for (ExecStatusT exec : sbStatus.getExecStatus()) {
			if (exec.getStatus().getState().getType()
					== StatusTStateType.FULLYOBSERVED_TYPE) {
				result ++;
			}
		}
		return result;
	}
	
	private SchedBlockState sbState(SBStatus sbStatus)
		throws ConversionException {
		switch (sbStatus.getStatus().getState().getType()) {
			case StatusTStateType.READY_TYPE:
			case StatusTStateType.PHASE1SUBMITTED_TYPE:
			case StatusTStateType.PHASE2SUBMITTED_TYPE:
			case StatusTStateType.CSVREADY_TYPE:
				return SchedBlockState.READY;
			case StatusTStateType.RUNNING_TYPE:
			case StatusTStateType.CSVRUNNING_TYPE:
				return SchedBlockState.RUNNING;
			case StatusTStateType.FULLYOBSERVED_TYPE:
				return SchedBlockState.FULLY_OBSERVED;
			default:
				throw new ConversionException(String.format(
						"APDM SchedBlock %s in unexpected state (%s)",
						sbStatus.getSBStatusEntity().getEntityId(),
						sbStatus.getStatus().getState().toString()));
		}
	}


	private alma.entity.xmlbinding.schedblock.ObservingParametersT getAPDMObservingParameters(
			alma.entity.xmlbinding.schedblock.SchedBlockChoice2Item inner) {
		alma.entity.xmlbinding.schedblock.ObservingParametersT result = null;
		
		if (inner.getAmplitudeCalParameters() != null) {
			result = inner.getAmplitudeCalParameters();
		} else if (inner.getAtmosphericCalParameters() != null) {
			result = inner.getAtmosphericCalParameters();
		} else if (inner.getBandpassCalParameters() != null) {
			result = inner.getBandpassCalParameters();
		} else if (inner.getDelayCalParameters() != null) {
			result = inner.getDelayCalParameters();
		} else if (inner.getFocusCalParameters() != null) {
			result = inner.getFocusCalParameters();
		} else if (inner.getHolographyParameters() != null) {
			result = inner.getHolographyParameters();
		} else if (inner.getOpticalPointingParameters() != null) {
			result = inner.getOpticalPointingParameters();
		} else if (inner.getPhaseCalParameters() != null) {
			result = inner.getPhaseCalParameters();
		} else if (inner.getPointingCalParameters() != null) {
			result = inner.getPointingCalParameters();
		} else if (inner.getPolarizationCalParameters() != null) {
			result = inner.getPolarizationCalParameters();
		} else if (inner.getRadiometricPointingParameters() != null) {
			result = inner.getRadiometricPointingParameters();
		} else if (inner.getReservationParameters() != null) {
			result = inner.getReservationParameters();
		} else if (inner.getScienceParameters() != null) {
			result = inner.getScienceParameters();
		}
		
		return result;
	}

	private String workOutType(
			alma.entity.xmlbinding.schedblock.SchedBlockChoice2Item inner) {
		String result = "<<really, seriously unknown type>>";
		
		if (inner.getAmplitudeCalParameters() != null) {
			result = "Amplitute Cal";
		} else if (inner.getAtmosphericCalParameters() != null) {
			result = "Atmospheric Cal";
		} else if (inner.getBandpassCalParameters() != null) {
			result = "Bandpass Cal";
		} else if (inner.getDelayCalParameters() != null) {
			result = "Delay Cal";
		} else if (inner.getFocusCalParameters() != null) {
			result = "Focus Cal";
		} else if (inner.getHolographyParameters() != null) {
			result = "Holography";
		} else if (inner.getOpticalPointingParameters() != null) {
			result = "Optical Pointing";
		} else if (inner.getPhaseCalParameters() != null) {
			result = "Phase Cal";
		} else if (inner.getPointingCalParameters() != null) {
			result = "Pointing Cal";
		} else if (inner.getPolarizationCalParameters() != null) {
			result = "Polarization Cal";
		} else if (inner.getRadiometricPointingParameters() != null) {
			result = "Radiometric";
		} else if (inner.getReservationParameters() != null) {
			result = "Reservation";
		} else if (inner.getScienceParameters() != null) {
			result = "Science";
		}
		
		return result;
	}
	/* End Utils
	 * ============================================================= */
}
