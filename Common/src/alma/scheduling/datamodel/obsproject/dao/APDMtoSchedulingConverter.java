/**
 * 
 */
package alma.scheduling.datamodel.obsproject.dao;

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
import alma.scheduling.Define.SchedulingException;
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
	public enum Phase{PHASE1, PHASE2};
	/* End Enums
	 * ============================================================= */

	/*
	 * ================================================================
	 * Fields
	 * ================================================================
	 */
	private Map<String, alma.entity.xmlbinding.obsproject.ObsProject> apdmProjects;
	private Map<String, alma.entity.xmlbinding.schedblock.SchedBlock> apdmSchedBlocks;
	private Map<String, ProjectStatus> projectStatuses;
	private Map<String, OUSStatus> ousStatuses;
	private Map<String, SBStatus> sbStatuses;
	private Phase phase;
	private Logger logger;
	
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
	 * Get the one and only instance.
	 * 
	 * @return - the one and only instance
	 */
	public APDMtoSchedulingConverter(
			final Map<String, alma.entity.xmlbinding.obsproject.ObsProject> apdmProjects,
			final Map<String, alma.entity.xmlbinding.schedblock.SchedBlock> apdmSchedBlocks,
			final Map<String, ProjectStatus> projectStatuses,
			final Map<String, OUSStatus> ousStatuses,
			final Map<String, SBStatus> sbStatuses,
			final Phase phase,
			final Logger logger) {
		this.apdmProjects    = apdmProjects;
		this.apdmSchedBlocks = apdmSchedBlocks;
		this.projectStatuses = projectStatuses;
		this.ousStatuses     = ousStatuses;
		this.sbStatuses      = sbStatuses;
		this.phase           = phase;
		this.logger          = logger;
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
	 * @return
	 */
	public List<ObsProject> convertAPDMPhase1ProjectsToDataModel() {
		final List<ObsProject> result = new Vector<ObsProject>();
		for (final alma.entity.xmlbinding.obsproject.ObsProject apdmProject : apdmProjects.values()) {
			try {
				result.add(convertAPDMPhase1ProjectToDataModel(apdmProject));
			} catch (SchedulingException e) {
				logger.warning(String.format(
						"cannot convert APDM ObsProject %s to Scheduling Data Model - %s",
						apdmProject.getObsProjectEntity().getEntityId(),
						e.getMessage()));
			}
		}
		return result;
	}
	
	/**
	 * Convert the known APDM phase 2 objects to a list of their
	 * scheduling data model versions.
	 * 
	 * @return
	 */
	public List<ObsProject> convertAPDMPhase2ProjectsToDataModel() {
		final List<ObsProject> result = new Vector<ObsProject>();
		for (final alma.entity.xmlbinding.obsproject.ObsProject apdmProject : apdmProjects.values()) {
			try {
				result.add(convertAPDMPhase2ProjectToDataModel(apdmProject));
			} catch (SchedulingException e) {
				logger.warning(String.format(
						"cannot convert APDM ObsProject %s to Scheduling Data Model - %s",
						apdmProject.getObsProjectEntity().getEntityId(),
						e.getMessage()));
			}
		}
		return result;
	}
	/* End External interface
	 * ============================================================= */



	
	/*
	 * ================================================================
	 * Conversion of Phase 1 projects
	 * ================================================================
	 */
	private ObsProject convertAPDMPhase1ProjectToDataModel(
			alma.entity.xmlbinding.obsproject.ObsProject apdmProject)
		throws SchedulingException {
		throw new SchedulingException("Not yet implemented");
	}
	/* End Conversion of Phase 1 projects
	 * ============================================================= */


	
	/*
	 * ================================================================
	 * Conversion of Phase 2 projects
	 * ================================================================
	 */
	private ObsProject convertAPDMPhase2ProjectToDataModel(
			alma.entity.xmlbinding.obsproject.ObsProject apdmProject)
		throws SchedulingException {
		
		// Create the result
		final ObsProject obsProject = new ObsProject();
		
		// Fill in the top level object
		final ProjectStatus projectStatus = projectStatuses.get(apdmProject.getObsProjectEntity().getEntityId());
		final OUSStatus programStatus = ousStatuses.get(
				projectStatus.getObsProgramStatusRef().getEntityId());
		
		/* obsProject.setId() - No need, it is handled by Hibernate */
		obsProject.setPrincipalInvestigator(apdmProject.getPI());
		obsProject.setScienceRank(apdmProject.getScientificRank());
		obsProject.setScienceScore(0.0f); // TODO: model conversion
		obsProject.setStatus(
				projectStatus.getStatus().getState().toString());
		obsProject.setTotalExecutionTime(
				programStatus.getTotalUsedTimeInSec()*1.0);
		
		// Shuffle down the structure creating it
		final ObsUnitSet obsProgram = createPhase2ObsUnitSet(
				apdmProject.getObsProgram().getObsPlan(),
				apdmProject,
				programStatus,
				projectStatus,
				obsProject);
		obsProject.setObsUnit(obsProgram);
		obsProgram.setProject(obsProject);
		
		// Return the result
		return obsProject;
	}

	private ObsUnitSet createPhase2ObsUnitSet(
			alma.entity.xmlbinding.obsproject.ObsUnitSetT apdmOUS,
			alma.entity.xmlbinding.obsproject.ObsProject  apdmProject,
			OUSStatus                                     ousStatus,
			ProjectStatus                                 projectStatus,
			ObsProject                                    obsProject) {
		
		// Create the result
		final ObsUnitControl obsUnitControl = new ObsUnitControl();
		final ObsUnitSet     obsUnitSet     = new ObsUnitSet();
		
		// Fill in the top level objects
		/* obsUnitSet.setId() - No need, it is handled by Hibernate */
		obsUnitSet.setObsUnitControl(obsUnitControl);
		/* obsUnitSet.setparent() - Handled by the calling code */
		obsUnitSet.setProject(obsProject);
		
		// Shuffle down the structure creating it
		// Firstly, any child ObsUnitSets.
		final alma.entity.xmlbinding.obsproject.ObsUnitSetTChoice choice = apdmOUS.getObsUnitSetTChoice();
		for (final alma.entity.xmlbinding.obsproject.ObsUnitSetT childOUS : choice.getObsUnitSet()) {
			final String childStatusUID = childOUS.getOUSStatusRef().getEntityId();
			
			if (ousStatuses.containsKey(childStatusUID)) {
				// We have an OUSStatus for the child, so go ahead.
				final OUSStatus childOUSStatus = ousStatuses.get(
						childOUS.getOUSStatusRef().getEntityId());
				final ObsUnitSet child = createPhase2ObsUnitSet(
						childOUS,
						apdmProject,
						childOUSStatus,
						projectStatus,
						obsProject);
				obsUnitSet.addObsUnit(child);
			} else {
				// No OUSStatus for the child, skip gently over it.
				logger.info(String.format(
						"No status entity for ObsUnitSet %s of APDM ObsProject %s (%s) - skipping",
						childOUS.getEntityPartId(),
						apdmProject.getProjectName(),
						apdmProject.getObsProjectEntity().getEntityId()));
			}
		}
		
		// Secondly, any child SchedBlocks.
		for (final alma.entity.xmlbinding.schedblock.SchedBlockRefT childSBref : choice.getSchedBlockRef()) {
			final String childDomainUID = childSBref.getEntityId();
			if (apdmSchedBlocks.containsKey(childDomainUID)) {
				final alma.entity.xmlbinding.schedblock.SchedBlock apdmSB = apdmSchedBlocks.get(childDomainUID);
				final String childStatusUID = apdmSB.getSBStatusRef().getEntityId();
				if (sbStatuses.containsKey(childStatusUID)) {
					final SBStatus sbStatus = sbStatuses.get(childDomainUID);
					try {
						final SchedBlock schedBlock = createPhase2SchedBlock(
								apdmSB,
								apdmOUS,
								apdmProject,
								sbStatus,
								ousStatus,
								projectStatus,
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
							"No APDM SBStatus %s for ObsUnitSet %s of APDM ObsProject %s - skipping",
							childStatusUID,
							apdmOUS.getEntityPartId(),
							apdmProject.getObsProjectEntity().getEntityId()));
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

	private SchedBlock createPhase2SchedBlock(
			alma.entity.xmlbinding.schedblock.SchedBlock  apdmSB,
			alma.entity.xmlbinding.obsproject.ObsUnitSetT apdmOUS,
			alma.entity.xmlbinding.obsproject.ObsProject  apdmProject,
			SBStatus                                      sbStatus,
			OUSStatus                                     ousStatus,
			ProjectStatus                                 projectStatus,
			ObsProject                                    obsProject) throws ConversionException {
		
		// Create the result
		final SchedBlock schedBlock = new SchedBlock();
		
		// Fill in the top level object
		
		// Create objects which hang off the top level SchedBlock, and hang them off it
		Preconditions preconditions;
		SchedulingConstraints schedulingConstraints;
		SchedBlockControl schedBlockControl;
		
		try {
			preconditions = createPhase2Preconditions(apdmSB.getPreconditions());
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
			schedulingConstraints = createPhase2SchedulingConstraints(apdmSB.getSchedulingConstraints());
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
			schedBlockControl = createPhase2SchedBlockControl(apdmSB.getSchedBlockControl(), sbStatus);
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
		final Map<String, ObservingParameters> observingParameters =
			createAllObservingParameters(apdmSB);
		final Map<String, FieldSource> fieldSources =
			createAllFieldSources(apdmSB);
		final Map<String, Target> targets =
			createAllTargets(apdmSB, observingParameters, fieldSources);
		
		for (final ObservingParameters op : observingParameters.values()) {
			schedBlock.addObservingParameters(op);
		}
		
		for (final Target t : targets.values()) {
			schedBlock.addTarget(t);
		}
		
		// Dig around for the representative target.
		final String representativeTargetId =
			apdmSB.getSchedulingConstraints().getRepresentativeTargetRef().getPartId();
		final Target representativeTarget = targets.get(representativeTargetId);
		if (representativeTarget != null) {
			schedBlock.getSchedulingConstraints().setRepresentativeTarget(representativeTarget);
		} else {
			throw new ConversionException("Cannot find representative target");
		}
		
		// Return the result
		return schedBlock;
	}

	private Preconditions createPhase2Preconditions(
			alma.entity.xmlbinding.obsproject.PreconditionsT preconditions) throws ConversionException {
		final Preconditions result = new Preconditions();
		
		result.setMinAllowedHourAngle(AngleConverter.convertedValue(
				preconditions.getMinAllowedHA(),
				UserAngleTUserUnitType.H));
		result.setMaxAllowedHourAngle(AngleConverter.convertedValue(
				preconditions.getMaxAllowedHA(),
				UserAngleTUserUnitType.H));
		return result;
	}

	private SchedulingConstraints createPhase2SchedulingConstraints(
			alma.entity.xmlbinding.schedblock.SchedulingConstraintsT constraints) throws ConversionException {
		final SchedulingConstraints result = new SchedulingConstraints();
		
		result.setMaxAngularResolution(AngleConverter.convertedValue(
				constraints.getMaxAcceptableAngResolution(),
				AngleTUnitType.ARCSEC));
		result.setRepresentativeFrequency(FrequencyConverter.convertedValue(
				constraints.getRepresentativeFrequency(),
				FrequencyTUnitType.GHZ));
		return result;
	}

	private SchedBlockControl createPhase2SchedBlockControl(
			alma.entity.xmlbinding.schedblock.SchedBlockControlT control,
			SBStatus                                             sbStatus) throws ConversionException {
		final SchedBlockControl result = new SchedBlockControl();
		
		result.setAccumulatedExecutionTime(sbStatus.getTotalUsedTimeInSec()/3600.0);
		result.setAchievedSensitivity(0.0); // TODO: model conversion
		result.setExecutionCount(sbExecutionCount(sbStatus));
		result.setIndefiniteRepeat(control.getIndefiniteRepeat());
		result.setState(sbState(sbStatus));

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
		
		final Map<String, ObservingParameters> result = new TreeMap<String, ObservingParameters>();
		
		for (final alma.entity.xmlbinding.schedblock.SchedBlockChoice2 outer : apdmSB.getSchedBlockChoice2()) {
			for (final alma.entity.xmlbinding.schedblock.SchedBlockChoice2Item inner : outer.getSchedBlockChoice2Item()) {
				ObservingParameters op;
				String partId;
				if (inner.getScienceParameters() != null) {
					partId = inner.getScienceParameters().getEntityPartId();
					try {
						op = createScienceParameters(inner.getScienceParameters());
						result.put(partId, op);
					} catch (ConversionException e) {
						logger.info(String.format(
								"Cannot convert APDM ObservingParameters %s in SchedBlock %s - %s, skipping",
								partId,
								apdmSB.getSchedBlockEntity().getEntityId(),
								e.getMessage()));
					}
				} else {
					logger.info(String.format(
							"Cannot convert APDM ObservingParameters in SchedBlock %s - unsupported type, skipping",
							apdmSB.getSchedBlockEntity().getEntityId()));
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
		
		final Map<String, FieldSource> result = new TreeMap<String, FieldSource>();
		
		for (final alma.entity.xmlbinding.schedblock.FieldSourceT apdmFS : apdmSB.getFieldSource()) {
			final String partId = apdmFS.getEntityPartId();
			try {
				final FieldSource fieldSource = createFieldSource(apdmFS);
				result.put(partId, fieldSource);
			} catch (ConversionException e) {
				logger.info(String.format(
						"Cannot convert APDM FieldSoruce %s in SchedBlock %s - %s, skipping",
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
		
		final Map<String, Target> result = new TreeMap<String, Target>();
		
		for (final alma.entity.xmlbinding.schedblock.TargetT apdmTarget : apdmSB.getTarget()) {
			final String partId = apdmTarget.getEntityPartId();
			try {
				final Target target = createTarget(apdmTarget, observingParameters, fieldSources);
				result.put(partId, target);
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
			alma.entity.xmlbinding.schedblock.ScienceParametersT parameters) throws ConversionException {
		final ScienceParameters result = new ScienceParameters();
		
		result.setRepresentativeBandwidth(FrequencyConverter.convertedValue(
				parameters.getRepresentativeBandwidth(),
				FrequencyTUnitType.GHZ));
		result.setRepresentativeFrequency(FrequencyConverter.convertedValue(
				parameters.getRepresentativeFrequency(),
				FrequencyTUnitType.GHZ));
		result.setSensitivityGoal(SensitivityConverter.convertedValue(
				parameters.getSensitivityGoal(),
				SensitivityTUnitType.JY));

		return result;
	}

	private FieldSource createFieldSource(
			alma.entity.xmlbinding.schedblock.FieldSourceT fieldSource) throws ConversionException {
		final FieldSource result = new FieldSource();
		
		result.setCoordinates(createSkyCoordinates(fieldSource.getSourceCoordinates()));
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
			alma.entity.xmlbinding.valuetypes.SkyCoordinatesT skyCoordinates) throws ConversionException {
		final SkyCoordinates result = new SkyCoordinates();
		
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
		final Target result = new Target();
		
		// Find the field source. If it's not there (due to not having
		// been created) then skip the whole Target.
		final FieldSource fs = fieldSources.get(apdmTarget.getFieldSourceRef().getPartId());
		
		if (fs == null) {
			throw new ConversionException(String.format(
					"field source %s missing (look in earlier logs for reasons)",
					apdmTarget.getFieldSourceRef().getPartId()));
		}
		
		result.setSource(fs);
		
		for (final alma.entity.xmlbinding.schedblock.SchedBlockRefT ref : apdmTarget.getObservingParametersRef()) {
			final String partId = ref.getPartId();
			final ObservingParameters op = observingParameters.get(partId);
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
	/* End Conversion of Phase 2 projects
	 * ============================================================= */


	
	/*
	 * ================================================================
	 * Utils
	 * ================================================================
	 */
	private int sbExecutionCount(SBStatus sbStatus) {
		int result = 0;
		for (final ExecStatusT exec : sbStatus.getExecStatus()) {
			if (exec.getStatus().getState().getType() == StatusTStateType.FULLYOBSERVED_TYPE) {
				result ++;
			}
		}
		return result;
	}
	
	private SchedBlockState sbState(SBStatus sbStatus) throws ConversionException {
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
	/* End Utils
	 * ============================================================= */
}
