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

package alma.scheduling.datamodel.bookkeeping;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.omg.CORBA.UserException;

import alma.acs.entityutil.EntityException;
import alma.entity.xmlbinding.obsproject.ObsProject;
import alma.entity.xmlbinding.obsproject.ObsUnitControlT;
import alma.entity.xmlbinding.obsproject.ObsUnitSetT;
import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.entity.xmlbinding.ousstatus.OUSStatusChoice;
import alma.entity.xmlbinding.ousstatus.OUSStatusRefT;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.projectstatus.StatusBaseT;
import alma.entity.xmlbinding.sbstatus.ExecStatusT;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.entity.xmlbinding.sbstatus.SBStatusRefT;
import alma.entity.xmlbinding.schedblock.SchedBlock;
import alma.entity.xmlbinding.schedblock.SchedBlockChoice2;
import alma.entity.xmlbinding.schedblock.SchedBlockChoice2Item;
import alma.entity.xmlbinding.schedblock.SchedBlockControlT;
import alma.entity.xmlbinding.schedblock.SchedBlockRefT;
import alma.entity.xmlbinding.schedblock.SchedulingConstraintsT;
import alma.entity.xmlbinding.schedblock.ScienceParametersT;
import alma.entity.xmlbinding.schedblock.TargetT;
import alma.entity.xmlbinding.valuetypes.StatusT;
import alma.entity.xmlbinding.valuetypes.types.SensitivityTUnitType;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.entity.xmlbinding.valuetypes.types.TimeTUnitType;
import alma.scheduling.datamodel.helpers.ConversionException;
import alma.scheduling.datamodel.helpers.SensitivityConverter;
import alma.scheduling.datamodel.helpers.TimeConverter;
import alma.scheduling.datamodel.obsproject.dao.ArchiveInterface;
import alma.scheduling.utils.ErrorHandling;

/**
 * Class to handle the bookkeeping associated with SBStatuses,
 * OUSStatuses and ProjectStatuses.
 *  
 * @author dclarke
 * $Id: Bookkeeper.java,v 1.1 2011/09/14 22:13:09 dclarke Exp $
 */
public final class Bookkeeper {

	// Made final simply because I am not sure I could cope with some
	// lexicographically minded person extending this class and having
	// a Subbookkeeper.

	/*
	 * ================================================================
	 * Exceptions - because you never know
	 * ================================================================
	 */
	@SuppressWarnings("serial")
	public class BookkeepingException extends Exception {

		public BookkeepingException() {
			super();
		}

		public BookkeepingException(String message, Throwable cause) {
			super(message, cause);
		}

		public BookkeepingException(String message) {
			super(message);
		}

		public BookkeepingException(Throwable cause) {
			super(cause);
		}
	}
	/* End Exceptions
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Fields & Constants
	 * ================================================================
	 */
	/**
	 *  ExecStatuses have one of three flags:<dl>
	 *     <dt>FULLY_OBSERVED</dt><dd>it worked nicely;</dd>
	 *     <dt>BROKEN</dt><dd>it didn't work;</dd>
	 *     <dt>PARTIALLY_OBSERVED</dt><dd>it worked a bit, e.g. was
	 *     interrupted.</dd>
	 *  </dl>
	 *  
	 *  FULLY_OBSERVED executions are tallied in the "successful"
	 *  totals and BROKEN observations are tallied in the "failed"
	 *  totals.
	 *  
	 *   The following flag controls whether PARTIALLY_OBSERVED
	 *   executions are tallied as "successful" (when
	 *   <code>true</code>) or "failed" (when <code>false</code>).
	 */
	private final static boolean PARTIAL_COUNTS_AS_SUCCESS = true;
	
	// Best sensitivity of ALMA ~0.05 mJy
	private final static double zeroSensitivityJy = 0.000001;
	
	private DateFormat dateFormat = null;
	
	private ArchiveInterface archive = null;
	
	private Logger logger = null;
	/* End Fields & Constants
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	/**
	 * Hidden default constructor.
	 */
	private Bookkeeper() {
    	this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	}
	
	/**
	 * Public constructor.
	 */
	public Bookkeeper(ArchiveInterface archive, Logger logger) {
		this();
		this.archive = archive;
		this.logger  = logger;
	}
	/* End Construction
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Utility methods
	 * ================================================================
	 */
	/**
	 * Find the representative target in the given SchedBlock. Returns
	 * null if there isn't one.
	 * 
	 * @param schedBlock
	 * @return
	 */
	private TargetT getRepresentativeTarget(SchedBlock schedBlock) {
		try {
			final SchedulingConstraintsT constraints = schedBlock.getSchedulingConstraints();
			final SchedBlockRefT ref = constraints.getRepresentativeTargetRef();
			final String partId = ref.getPartId();

			for (final TargetT target : schedBlock.getTarget()) {
				if (target.getEntityPartId().equals(partId)) {
					return target;
				}
			}
		} catch (Exception e) {
		}
		return null;
	}
	
	/**
	 * Guddle around in the given SchedBlock to find the
	 * ScienceParameters used by the given Target (which should be a
	 * target in the SchedBlock). Returns the set of ScienceParameters
	 * found (which could be empty).
	 * 
	 * @param schedBlock
	 * @param target
	 * @return
	 */
	private Set<ScienceParametersT> getScienceParameters(
								SchedBlock schedBlock,
								TargetT    target) {

		// 1. Find the ObservingParametersT partids in use in this TargetT
		final Set<String> partids = new HashSet<String>();
		
		for (final SchedBlockRefT ref : target.getObservingParametersRef()) {
			partids.add(ref.getPartId());
		}
		
		// 2. Find the ObservingParameters corresponding to the partids
		//    and remember those which are ScienceParameters.
		final Set<ScienceParametersT> result = new HashSet<ScienceParametersT>();
		for (SchedBlockChoice2 outer : schedBlock.getSchedBlockChoice2()) {
			for (SchedBlockChoice2Item inner : outer.getSchedBlockChoice2Item()) {
				ScienceParametersT science = inner.getScienceParameters();
				if (science != null) {
					result.add(science);
				}
			}
		}
		return result;
	}
	
	/**
	 * Guddle around in the given SchedBlock to find the sensitivity
	 * goal for the given Target (which should be a target in the
	 * SchedBlock). Does this by looking through the ScienceParameters
	 * of the Target, and taking the most stringent (i.e. lowest) goal
	 * of them. Returns either the value found in janskys, or -1.0 if
	 * no value is found.
	 *
	 * @param schedBlock
	 * @param target
	 * @return
	 */
	private double findSensitivityGoalJy(SchedBlock schedBlock,
									     TargetT    target) {
		boolean doneOne = false;
		double result = -1.0;

		if (target != null) {
			final Set<ScienceParametersT> params = getScienceParameters(schedBlock, target);

			for (final ScienceParametersT param : params) {
				try {
					double sensitivity =  SensitivityConverter.convertedValue(
							param.getSensitivityGoal(),
							SensitivityTUnitType.JY);
					if (doneOne) {
						if (sensitivity < result) {
							result = sensitivity;
						}
					} else {
						result = sensitivity;
						doneOne = true;
					}
				} catch (ConversionException e) {
					ErrorHandling.warning(logger,
							String.format("problem interpreting sensitivity in SchedBlock %s - %s",
									schedBlock.getSchedBlockEntity().getEntityId(),
									e.getMessage()),
									e);
				}
			}
		}

		if (!doneOne) {
			return -1.0;
		}
		return result;
		
	}
	
	/**
	 * Find the ObsUnitSet with the given partId in the given
	 * ObsUnitSet hierarchy, or <code>null</code>, if there isn't one.
	 * 
	 * @param parent
	 * @param partId
	 * @return
	 */
	private ObsUnitSetT findOUS(ObsUnitSetT parent, String partId) {
		// Is "parent" the one?
		if (parent.getEntityPartId().equals(partId)) {
			return parent;
		}
		
		// Nope, so check the children.
		for (final ObsUnitSetT child : parent.getObsUnitSetTChoice().getObsUnitSet()) {
			final ObsUnitSetT ous = findOUS(child, partId);
			if (ous != null) {
				return ous;
			}
		}
		
		// Not found it.
		return null;
	}
	
	/**
	 * Find the ObsUnitSet with the given partid in the ObsProject.
	 * 
	 * @param obsProject
	 * @param partId
	 * @return
	 */
	private ObsUnitSetT findOUS(ObsProject obsProject, String partId) {
		return findOUS(obsProject.getObsProgram().getObsPlan(), partId);
	}
	/* End Utility methods
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Testing for initialisation - private stuff
	 * ================================================================
	 */
	/**
	 * Is the given StatusBaseT's ExecutionCount initialised?
	 * 
	 * @return boolean
	 */
	private boolean isInitialisedExecutionCount(StatusBaseT status) {
		if (!status.hasHasExecutionCount()) {
			// "hasExecutionCount" isn't set, so cannot be initialised
			return false;
		}
		
		if (status.getHasExecutionCount()) {
			// There is an execution count, so must be initialised
			return true;
		}

		return false;
	}
	
	/**
	 * Is the given StatusBaseT's Time initialised?
	 * 
	 * @return boolean
	 */
	private boolean isInitialisedTime(StatusBaseT status) {
		if (!status.hasHasTimeLimit()) {
			// "hasTimeLimit" isn't set, so cannot be initialised
			return false;
		}
		
		if (status.getHasTimeLimit()) {
			// There is a time limit, so must be initialised
			return true;
		}

		return false;
	}
	
	/**
	 * Is the given SBStatus' Sensitivity initialised?
	 * @param target 
	 * 
	 * @return boolean
	 */
	private boolean isInitialisedSensitivity(SBStatus status) {
		if (!status.hasHasSensitivityGoal()) {
			// "hasSensitivityGoal" isn't set, so cannot be initialised
			return false;
		}
		
		if (status.getHasSensitivityGoal()) {
			// There is a sensitivity goal, so must be initialised
			return true;
		}
		
		final double statusGoal = status.getSensitivityGoalJy();
		if (statusGoal > zeroSensitivityJy) {
			// There is an achievable sensitivity in the sbStatus
			return true;
		}

		return false;
	}
	
	/**
	 * Is the given SBStatus initialised?
	 * 
	 * @return boolean
	 */
	public boolean isInitialised(SBStatus status) {
		return isInitialisedExecutionCount(status) &&
			   isInitialisedTime(status) &&
			   isInitialisedSensitivity(status);
	}
	
	/**
	 * Is the given StatusBaseT initialised?
	 * 
	 * @return boolean
	 */
	public boolean isInitialised(StatusBaseT status) {
		return isInitialisedExecutionCount(status) &&
			   isInitialisedTime(status);
	}
	/* End Testing for initialisation - private stuff
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Initialisation from domain objects - private stuff
	 * ================================================================
	 */
	/**
	 * Initialise the time and execution limits and the sensitivity
	 * goal in the supplied SBStatus by copying them from the APDM
	 * SchedBlock.
	 * 
	 * @param status
	 * @throws EntityException
	 * @throws UserException
	 */
	private void initialiseFromAPDM(SBStatus status)
			throws EntityException, UserException {
		final String             sbId       = status.getSchedBlockRef().getEntityId();
		final SchedBlock         schedBlock = archive.getSchedBlock(sbId);
		final SchedBlockControlT sbControl  = schedBlock.getSchedBlockControl();
		final ObsUnitControlT    ouControl  = schedBlock.getObsUnitControl();
		final TargetT            target     = getRepresentativeTarget(schedBlock);
		final double             goal       = findSensitivityGoalJy(schedBlock, target);
		
		// Execution Counts
		status.setHasExecutionCount(!sbControl.getIndefiniteRepeat());
		status.setSuccessfulExecutions(0);
		status.setFailedExecutions(0);
		status.setExecutionsRemaining(ouControl.getAggregatedExecutionCount());
		
		// Time Limits
		status.setHasTimeLimit(!sbControl.getIndefiniteRepeat());
		status.setSuccessfulSeconds(0);
		status.setFailedSeconds(0);
		try {
			int limit = (int) Math.round(TimeConverter.convertedValue(
					ouControl.getMaximumTime(),
					TimeTUnitType.S));
			status.setSecondsRemaining(limit);
		} catch (ConversionException e) {
			if (status.getHasTimeLimit()) {
				// Only report an error if there should be a time limit
				ErrorHandling.warning(logger,
						String.format("problem interpreting ObsUnitControl.maximumTime in SchedBlock %s - %s",
								schedBlock.getSchedBlockEntity().getEntityId(),
								e.getMessage()),
								e);
				status.setSecondsRemaining(0);
			}
		}

		// Sensitivity
		status.setHasSensitivityGoal(goal > zeroSensitivityJy);
		status.setSensitivityGoalJy(goal);
		status.setSensitivityAchievedJy(-1.0); // marker for nothing.
		
		logger.fine(String.format(
				"initialiseFromAPDM(SBS  %s for SB %s):%n\tisInitialised: %s%n\tHEC: %s, SE: %d, FE: %d, ER: %d%n\tHTL: %s, SS: %d, FS: %d, SR: %d%n\tHSG: %s, SG: %f, SA: %f",
				status.getSBStatusEntity().getEntityId(),
				schedBlock.getName(),
				isInitialised(status),
				status.getHasExecutionCount(),
				status.getSuccessfulExecutions(),
				status.getFailedExecutions(),
				status.getExecutionsRemaining(),
				status.getHasTimeLimit(),
				status.getSuccessfulSeconds(),
				status.getFailedSeconds(),
				status.getSecondsRemaining(),
				status.getHasSensitivityGoal(),
				status.getSensitivityGoalJy(),
				status.getSensitivityAchievedJy()));
	}

	/**
	 * Initialise the time and execution limits in the supplied
	 * OUSStatus by copying them from the APDM ObsUnitSet.
	 * 
	 * @param status
	 * @throws EntityException
	 * @throws UserException
	 */
	private void initialiseFromAPDM(OUSStatus status)
			throws EntityException, UserException {
		final String             opId       = status.getObsUnitSetRef().getEntityId();
		final String             partId     = status.getObsUnitSetRef().getPartId();
		final ObsProject         obsProject = archive.getObsProject(opId);
		final ObsUnitSetT        ous        = findOUS(obsProject, partId);
		final ObsUnitControlT    ouControl = ous.getObsUnitControl();

		// Initial limits on time and executions
		status.setExecutionsRemaining(ouControl.getAggregatedExecutionCount());
		try {
			int limit = (int) Math.round(TimeConverter.convertedValue(
					ouControl.getMaximumTime(),
					TimeTUnitType.S));
			status.setSecondsRemaining(limit);
		} catch (ConversionException e) {
			ErrorHandling.warning(logger,
					String.format("problem interpreting ObsUnitControl.maximumTime in ObsUnitSet %s of ObsProject %s - %s",
							partId,
							opId,
							e.getMessage()),
							e);
			status.setSecondsRemaining(0);
		}
		
		logger.fine(String.format(
				"initialiseFromAPDM(OUSS %s for OUS %s): isInitialised: %s%n\tHEC: %s, SE: %d, FE: %d, ER: %d%n\tHTL: %s, SS: %d, FS: %d, SR: %d",
				status.getOUSStatusEntity().getEntityId(),
				ous.getName(),
				isInitialised(status),
				status.getHasExecutionCount(),
				status.getSuccessfulExecutions(),
				status.getFailedExecutions(),
				status.getExecutionsRemaining(),
				status.getHasTimeLimit(),
				status.getSuccessfulSeconds(),
				status.getFailedSeconds(),
				status.getSecondsRemaining()));
	}
	/* End Initialisation from domain objects - private stuff
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Initialisation from children - private stuff
	 * ================================================================
	 */
	/**
	 * Count up any executions and time that this SchedBlock has had
	 * and record them in the SBStatus. We don't have enough
	 * information to work out the sensitivity found thus far.
	 * 
	 * @param status
	 * @throws UserException - cannot get an entity from the archive
	 * @throws EntityException - cannot parse a retrieved entity
	 * @throws ParseException - cannot parse a timestamp in a status
	 */
	private void catchUp(SBStatus status)
						throws EntityException, UserException, ParseException {
		int yesX   = 0;	// Successful executions
		int noX    = 0;	// Failed executions
		int maybeX = 0;	// Partially successful executions
		int yesS   = 0;	// Successful time (in seconds)
		int noS    = 0;	// Failed time (in seconds)
		int maybeS = 0;	// Partially successful time (in seconds)

		for (final ExecStatusT execStatus : status.getExecStatus()) {
			final StatusT          s     = execStatus.getStatus();
			final StatusTStateType state = s.getState();
			final Date start = dateFormat.parse(s.getStartTime());
			final Date end   = dateFormat.parse(s.getEndTime());
			final long seconds = (end.getTime() - start.getTime())/1000;


			switch (state.getType()) {
			case StatusTStateType.FULLYOBSERVED_TYPE:
				// Successful execution
				yesX ++;
				yesS += seconds;
				break;
			case StatusTStateType.BROKEN_TYPE:
				// Failed execution
				noX ++;
				noS += seconds;
				break;
			default:
				// Assume an interrupted observation
				maybeX ++;
				maybeS += seconds;
				break;
			}
		}

		if (PARTIAL_COUNTS_AS_SUCCESS) {
			yesX += maybeX;
			yesS += maybeS;
		} else {
			noX += maybeX;
			noS += maybeS;
		}
		status.setSuccessfulExecutions(yesX);
		status.setFailedExecutions(noX);
		final int limitX = status.getExecutionsRemaining();
		status.setExecutionsRemaining(limitX - yesX);
		
		status.setSuccessfulExecutions(yesS);
		status.setFailedSeconds(noS);
		final int limitS = status.getSecondsRemaining();
		status.setSecondsRemaining(limitS - yesS);
		
		logger.fine(String.format(
				"catchUp(SBS  %s): isInitialised: %s %n\tHEC: %s, SE: %d, FE: %d, ER: %d%n\tHTL: %s, SS: %d, FS: %d, SR: %d",
				status.getSBStatusEntity().getEntityId(),
				isInitialised(status),
				status.getHasExecutionCount(),
				status.getSuccessfulExecutions(),
				status.getFailedExecutions(),
				status.getExecutionsRemaining(),
				status.getHasTimeLimit(),
				status.getSuccessfulSeconds(),
				status.getFailedSeconds(),
				status.getSecondsRemaining()));
	}
	
	/**
	 * Count up any executions and time that this ObsUnitSet has had
	 * and record them in the OUSStatus.
	 * 
	 * @param status
	 * @throws UserException - cannot get an entity from the archive
	 * @throws EntityException - cannot parse a retrieved entity
	 * @throws ParseException - cannot parse a timestamp in a status
	 */
	private void catchUp(OUSStatus status)
						throws EntityException, UserException, ParseException {
		boolean timeLimit = true;
		int yesX   = 0;	// Successful executions
		int noX    = 0;	// Failed executions
		int leftX  = 0;	// Remaining executions
		int yesS   = 0;	// Successful time (in seconds)
		int noS    = 0;	// Failed time (in seconds)
		int leftS  = 0;	// Remaining time (in seconds)

		final OUSStatusChoice choice = status.getOUSStatusChoice();
		
		for (final OUSStatusRefT childStatusRef : choice.getOUSStatusRef()) {
			final String childId = childStatusRef.getEntityId();
			final OUSStatus childStatus = archive.getOUSStatus(childId);
			
			if (!childStatus.getHasTimeLimit()) {
				timeLimit = false;
			}
			yesX  += childStatus.getSuccessfulExecutions();
			noX   += childStatus.getFailedExecutions();
			leftX += childStatus.getExecutionsRemaining();
			yesS  += childStatus.getSuccessfulSeconds();
			noS   += childStatus.getFailedSeconds();
			leftS += childStatus.getSecondsRemaining();
		}
		
		for (final SBStatusRefT childStatusRef : choice.getSBStatusRef()) {
			final String childId = childStatusRef.getEntityId();
			final SBStatus childStatus = archive.getSBStatus(childId);
			
			if (!childStatus.getHasTimeLimit()) {
				timeLimit = false;
			}
			yesX  += childStatus.getSuccessfulExecutions();
			noX   += childStatus.getFailedExecutions();
			leftX += childStatus.getExecutionsRemaining();
			yesS  += childStatus.getSuccessfulSeconds();
			noS   += childStatus.getFailedSeconds();
			leftS += childStatus.getSecondsRemaining();
		}
		
		status.setHasExecutionCount(timeLimit);
		status.setSuccessfulExecutions(yesX);
		status.setFailedExecutions(noX);
		final int limitX = status.getExecutionsRemaining();
		status.setExecutionsRemaining(limitX - yesX);
		
		status.setHasTimeLimit(timeLimit);
		status.setSuccessfulSeconds(yesS);
		status.setFailedSeconds(noS);
		final int limitS = status.getSecondsRemaining();
		status.setSecondsRemaining(limitS - yesS);
		
		logger.fine(String.format(
				"catchUp(OUSS %s): isInitialised: %s%n\tHEC: %s, SE: %d, FE: %d, ER: %d%n\tHTL: %s, SS: %d, FS: %d, SR: %d",
				status.getOUSStatusEntity().getEntityId(),
				isInitialised(status),
				status.getHasExecutionCount(),
				status.getSuccessfulExecutions(),
				status.getFailedExecutions(),
				status.getExecutionsRemaining(),
				status.getHasTimeLimit(),
				status.getSuccessfulSeconds(),
				status.getFailedSeconds(),
				status.getSecondsRemaining()));
	}
	
	/**
	 * Count up any executions and time that this ObsProject has had
	 * and record them in the ProjectStatus.
	 * 
	 * @param status
	 * @throws UserException - cannot get an entity from the archive
	 * @throws EntityException - cannot parse a retrieved entity
	 * @throws ParseException - cannot parse a timestamp in a status
	 */
	private void catchUp(ProjectStatus status)
						throws EntityException, UserException, ParseException {

		final OUSStatusRefT programStatusRef = status.getObsProgramStatusRef();
		final String        programId        = programStatusRef.getEntityId();
		final OUSStatus     programStatus    = archive.getOUSStatus(programId);

		status.setHasExecutionCount(programStatus.getHasExecutionCount());
		status.setSuccessfulExecutions(programStatus.getSuccessfulExecutions());
		status.setFailedExecutions(programStatus.getFailedExecutions());
		status.setExecutionsRemaining(programStatus.getExecutionsRemaining());
		
		status.setHasTimeLimit(programStatus.getHasTimeLimit());
		status.setSuccessfulSeconds(programStatus.getSuccessfulSeconds());
		status.setFailedSeconds(programStatus.getFailedSeconds());
		status.setSecondsRemaining(programStatus.getSecondsRemaining());
		
		logger.fine(String.format(
				"catchUp(PS   %s): isInitialised: %s%n\tHEC: %s, SE: %d, FE: %d, ER: %d%n\tHTL: %s, SS: %d, FS: %d, SR: %d",
				status.getProjectStatusEntity().getEntityId(),
				isInitialised(status),
				status.getHasExecutionCount(),
				status.getSuccessfulExecutions(),
				status.getFailedExecutions(),
				status.getExecutionsRemaining(),
				status.getHasTimeLimit(),
				status.getSuccessfulSeconds(),
				status.getFailedSeconds(),
				status.getSecondsRemaining()));
	}
	/* End Initialisation from children - private stuff
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Recursive initialisation - private stuff
	 * ================================================================
	 */
	/**
	 * Initialise the children of the given OUSStatus. Return a boolean
	 * indicating whether or not anything has changed below.
	 * 
	 * @param status
	 * @return
	 * @throws UserException 
	 * @throws EntityException 
	 * @throws BookkeepingException 
	 */
	private boolean initialiseChildren(OUSStatus status)
			throws EntityException, UserException, BookkeepingException {
		final OUSStatusChoice choice = status.getOUSStatusChoice();
		boolean result = false;
		logger.fine("Initialising children of OUSS " + status.getOUSStatusEntity().getEntityId());
		
		for (final OUSStatusRefT childStatusRef : choice.getOUSStatusRef()) {
			final String childId = childStatusRef.getEntityId();
			final OUSStatus childStatus = archive.getOUSStatus(childId);
			
			if (initialise(childStatus)) {
				result = true;
			}
		}
		
		for (final SBStatusRefT childStatusRef : choice.getSBStatusRef()) {
			final String childId = childStatusRef.getEntityId();
			final SBStatus childStatus = archive.getSBStatus(childId);
			
			if (initialise(childStatus)) {
				result = true;
			}
		}
		
		return result;
	}
	
	/**
	 * Initialise the children of the given ProjectStatus. Return a boolean
	 * indicating whether or not anything has changed below.
	 * 
	 * @param status
	 * @return
	 * @throws UserException 
	 * @throws EntityException 
	 * @throws BookkeepingException 
	 */
	private boolean initialiseChildren(ProjectStatus status)
			throws EntityException, UserException, BookkeepingException {
		final OUSStatusRefT programStatusRef = status.getObsProgramStatusRef();
		final String        programId        = programStatusRef.getEntityId();
		final OUSStatus     programStatus    = archive.getOUSStatus(programId);
		
		logger.fine("Initialising children of PS " + status.getProjectStatusEntity().getEntityId());
		return initialise(programStatus);
	}
	/* End Recursive initialisation - private stuff
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Status initialisation
	 * ================================================================
	 */
	/**
	 * Initialise the given SBStatus, taking into account executions of
	 * its SchedBlock. Returns true if anything has changed.
	 * 
	 * @param status
	 * @throws BookkeepingException
	 */
	private boolean initialise(SBStatus status) throws BookkeepingException {
		try {
			if (!isInitialised(status)) {
				initialiseFromAPDM(status);
				catchUp(status);
				archive.write(status);
				return true;
			}
		} catch (Exception e) {
			throw new BookkeepingException(e);
		}
		return false;
	}
	
	/**
	 * Initialise the given OUSStatus, taking into account executions of
	 * its descendant SchedBlocks. Returns true if anything has changed.
	 * 
	 * @param status
	 * @throws BookkeepingException
	 */
	private boolean initialise(OUSStatus status) throws BookkeepingException {
		try {
			boolean needToCatchUp = initialiseChildren(status);
			
			if (needToCatchUp || !isInitialised(status)) {
				initialiseFromAPDM(status);
				catchUp(status);
				archive.write(status);
				return true;
			}
		} catch (Exception e) {
			throw new BookkeepingException(e);
		}
		return false;
	}

	/**
	 * Initialise the given ProjectStatus, taking into account executions of
	 * its descendant SchedBlocks. Returns true if anything has changed.
	 * 
	 * @param status
	 * @throws BookkeepingException
	 */
	public boolean initialise(ProjectStatus status) throws BookkeepingException {
		try {
			boolean needToCatchUp = initialiseChildren(status);
			
			if (needToCatchUp || !isInitialised(status)) {
				catchUp(status);
				archive.write(status);
				return true;
			}
		} catch (Exception e) {
			throw new BookkeepingException(e);
		}
		return false;
	}
	/* End Status initialisation
	 * ============================================================= */

	

	/*
	 * ================================================================
	 * Status printing
	 * ================================================================
	 */
	public String print(String indent, SBStatus status) {
		final StringBuilder b = new StringBuilder();
		final Formatter     f = new Formatter(b);
		
		f.format("%s%s %s for Entity %s (%s)%n",
				indent,
				status.getClass().getSimpleName(),
				status.getSBStatusEntity().getEntityId(),
				status.getSchedBlockRef().getEntityId(),
				status.getStatus().getState());

		if (status.getHasExecutionCount()) {
			f.format("%s   Executions, remaining: %d, success: %d, failed: %d%n",
					indent,
					status.getExecutionsRemaining(),
					status.getSuccessfulExecutions(),
					status.getFailedExecutions());
		} else {
			f.format("%s   No execution counts%n", indent);
		}

		if (status.getHasTimeLimit()) {
			f.format("%s   Seconds, remaining: %d, success: %d, failed: %d%n",
					indent,
					status.getSecondsRemaining(),
					status.getSuccessfulSeconds(),
					status.getFailedSeconds());
		} else {
			f.format("%s   No time limits%n", indent);
		}

		if (status.getHasSensitivityGoal()) {
			f.format("%s   Sensitivity, goal: %6.3fJy, achieved: %6.3fJy%n",
					indent,
					status.getSensitivityGoalJy(),
					status.getSensitivityAchievedJy());
		} else {
			f.format("%s   No sensitivity goal%n", indent);
		}

		return b.toString();
	}
	
	public String print(String indent, OUSStatus status) {
		final StringBuilder b = new StringBuilder();
		final Formatter     f = new Formatter(b);
		
		f.format("%s%s %s for EntityPart %s in %s (%s)%n",
				indent,
				status.getClass().getSimpleName(),
				status.getOUSStatusEntity().getEntityId(),
				status.getObsUnitSetRef().getPartId(),
				status.getObsUnitSetRef().getEntityId(),
				status.getStatus().getState());

		if (status.getHasExecutionCount()) {
			f.format("%s   Executions, remaining: %d, success: %d, failed: %d%n",
					indent,
					status.getExecutionsRemaining(),
					status.getSuccessfulExecutions(),
					status.getFailedExecutions());
		} else {
			f.format("%s   No execution counts%n", indent);
		}

		if (status.getHasTimeLimit()) {
			f.format("%s   Seconds, remaining: %d, success: %d, failed: %d%n",
					indent,
					status.getSecondsRemaining(),
					status.getSuccessfulSeconds(),
					status.getFailedSeconds());
		} else {
			f.format("%s   No time limits%n", indent);
		}

		return b.toString();
	}
	
	public String print(String indent, ProjectStatus status) {
		final StringBuilder b = new StringBuilder();
		final Formatter     f = new Formatter(b);
		
		f.format("%s%s %s for Entity %s (%s)%n",
				indent,
				status.getClass().getSimpleName(),
				status.getProjectStatusEntity().getEntityId(),
				status.getObsProjectRef().getEntityId(),
				status.getStatus().getState());

		if (status.getHasExecutionCount()) {
			f.format("%s   Executions, remaining: %d, success: %d, failed: %d%n",
					indent,
					status.getExecutionsRemaining(),
					status.getSuccessfulExecutions(),
					status.getFailedExecutions());
		} else {
			f.format("%s   No execution counts%n", indent);
		}

		if (status.getHasTimeLimit()) {
			f.format("%s   Seconds, remaining: %d, success: %d, failed: %d%n",
					indent,
					status.getSecondsRemaining(),
					status.getSuccessfulSeconds(),
					status.getFailedSeconds());
		} else {
			f.format("%s   No time limits%n", indent);
		}

		return b.toString();
	}
	
	public String print(SBStatus status) {
		return print("", status);
	}
	
	public String print(OUSStatus status) {
		return print("", status);
	}
	
	public String print(ProjectStatus status) {
		return print("", status);
	}
	/* End Status printing
	 * ============================================================= */
}
