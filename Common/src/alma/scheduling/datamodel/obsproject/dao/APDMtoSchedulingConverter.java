/**
 * 
 */
package alma.scheduling.datamodel.obsproject.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Logger;

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
import alma.entity.xmlbinding.valuetypes.types.UserAngleTUserUnitType;
import alma.scheduling.datamodel.helpers.AngleConverter;
import alma.scheduling.datamodel.helpers.AngularVelocityConverter;
import alma.scheduling.datamodel.helpers.ConversionException;
import alma.scheduling.datamodel.helpers.FrequencyConverter;
import alma.scheduling.datamodel.helpers.SensitivityConverter;
import alma.scheduling.datamodel.obsproject.FieldSource;
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
	 * Enums
	 * ================================================================
	 */
	/** Used to control which phase of the APDM to look in. */
	public enum Phase {PHASE1, PHASE2};
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
	
	/* End Fields
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	/** Hide the default constructor from the outside world */
	@SuppressWarnings("unused")
	private APDMtoSchedulingConverter() {};
	
	/**
	 * Construct a new instance.
	 */
	public APDMtoSchedulingConverter(
			ArchiveInterface archive,
			Phase            phase,
			Logger           logger) {
		this.archive = archive;
		this.phase   = phase;
		this.logger  = logger;
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
		for (alma.entity.xmlbinding.obsproject.ObsProject 
				apdmProject : archive.obsProjects()) {
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
			obsProject.setUid(projectId);
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
		obsProject.setPrincipalInvestigator(apdmProject.getPI());
		obsProject.setScienceRank(apdmProject.getScientificRank());
		obsProject.setScienceScore((float)apdmProject.getScientificScore());
		obsProject.setLetterGrade(ScienceGrade.valueOf(apdmProject.getLetterGrade()));
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
		}
		
		// Shuffle down the structure creating it
		alma.entity.xmlbinding.obsproject.ObsUnitSetT apdmOUS;
		
		if (phase == Phase.PHASE1) {
			alma.entity.xmlbinding.obsproposal.ObsProposalRefT
				proposalRef = apdmProject.getObsProposalRef();
			alma.entity.xmlbinding.obsproposal.ObsProposal
				apdmProposal = archive.cachedObsProposal(proposalRef.getEntityId());
			apdmOUS = apdmProposal.getObsPlan();
		} else {
			apdmOUS = apdmProject.getObsProgram().getObsPlan();
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
				} catch (ConversionException e) {
					logger.info(String.format(
							"Skipping SchedBlock %s in Project %s - %s",
							apdmSB.getSchedBlockEntity().getEntityId(),
							apdmProject.getObsProjectEntity().getEntityId(),
							e.getMessage()));
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
			schedBlock.setUid(sbId);
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
		
		// Create objects which hang off the top level SchedBlock, and
		// hang them off it.
		Preconditions preconditions;
		SchedulingConstraints schedulingConstraints;
		SchedBlockControl schedBlockControl;
		
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
			throw e;
		}
		try {
			final alma.entity.xmlbinding.sbstatus.SBStatus sbStatus =
				(phase == Phase.PHASE1)?
						null:
						archive.cachedSBStatus(apdmSB.getSBStatusRef().getEntityId());
			schedBlockControl = createSchedBlockControl(
					apdmSB.getSchedBlockControl(),
					sbStatus);
			schedBlock.setSchedBlockControl(schedBlockControl);
		} catch (ConversionException e) {
			logger.info(String.format(
				"Cannot create SchedBlockControl object for SchedBlock %s in Project %s - %s",
				apdmSB.getSchedBlockEntity().getEntityId(),
				apdmProject.getObsProjectEntity().getEntityId(),
				e.getMessage()));
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
		alma.entity.xmlbinding.schedblock.SchedulingConstraintsT
				constraints = apdmSB.getSchedulingConstraints();
		if (constraints != null) {
			alma.entity.xmlbinding.schedblock.SchedBlockRefT
					representativeTargetRef =
							constraints.getRepresentativeTargetRef();
			if (representativeTargetRef != null) {
				String representativeTargetId
						= representativeTargetRef.getPartId();
				Target representativeTarget
						= targets.get(representativeTargetId);
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
		
		// Return the result
		return schedBlock;
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
				ObservingParameters op;
				String name;
				String partId;
				alma.entity.xmlbinding.schedblock.ScienceParametersT
					scienceParameters = inner.getScienceParameters();
				if (scienceParameters != null) {
					name   = scienceParameters.getName();
					partId = scienceParameters.getEntityPartId();
					try {
						op = createScienceParameters(scienceParameters);
						result.put(partId, op);
						logger.info(String.format(
							"Converted APDM Science Parameters %s (%s) in SchedBlock %s",
							name, partId,
							apdmSB.getSchedBlockEntity().getEntityId()));
					} catch (ConversionException e) {
						logger.info(String.format(
							"Cannot convert APDM ObservingParameters %s (%s) in SchedBlock %s - %s, skipping",
							name, partId,
							apdmSB.getSchedBlockEntity().getEntityId(),
							e.getMessage()));
					}
				} else {
					logger.info(String.format(
						"Cannot convert APDM ObservingParameters in SchedBlock %s - unsupported type (%s), skipping",
						apdmSB.getSchedBlockEntity().getEntityId(),
						workOutType(inner)));
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
		result.setEphemeris(fieldSource.getSourceEphemeris());
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
				return SchedBlockState.READY;
			case StatusTStateType.RUNNING_TYPE:
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
			result = "Holography Cal";
		} else if (inner.getOpticalPointingParameters() != null) {
			result = "Optical Pointing Cal";
		} else if (inner.getPhaseCalParameters() != null) {
			result = "Phase Cal";
		} else if (inner.getPointingCalParameters() != null) {
			result = "Pointing Cal";
		} else if (inner.getPolarizationCalParameters() != null) {
			result = "Polarization Cal";
		} else if (inner.getRadiometricPointingParameters() != null) {
			result = "Radiometric Cal";
		} else if (inner.getReservationParameters() != null) {
			result = "Reservation Cal";
		} else if (inner.getScienceParameters() != null) {
			result = "Science Cal";
		}
		
		return result;
	}
	/* End Utils
	 * ============================================================= */
}
