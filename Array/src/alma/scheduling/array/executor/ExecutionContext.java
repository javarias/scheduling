/* 
 * ALMA - Atacama Large Millimiter Array
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
package alma.scheduling.array.executor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Formatter;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import alma.ACSErrTypeCommon.wrappers.AcsJIllegalArgumentEx;
import alma.Control.ExecBlockEndedEvent;
import alma.Control.ExecBlockStartedEvent;
import alma.SchedulingMasterExceptions.SchedulingInternalExceptionEx;
import alma.SchedulingMasterExceptions.wrappers.AcsJSchedulingInternalExceptionEx;
import alma.acs.time.TimeHelper;
import alma.acs.util.UTCUtility;
import alma.asdmIDLTypes.IDLEntityRef;
import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.entity.xmlbinding.ousstatus.OUSStatusEntityT;
import alma.entity.xmlbinding.ousstatus.OUSStatusRefT;
import alma.entity.xmlbinding.projectstatus.ObsUnitStatusT;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.projectstatus.ProjectStatusEntityT;
import alma.entity.xmlbinding.projectstatus.ProjectStatusRefT;
import alma.entity.xmlbinding.sbstatus.ExecStatusT;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.entity.xmlbinding.sbstatus.SBStatusEntityT;
import alma.entity.xmlbinding.valuetypes.ExecBlockRefT;
import alma.entity.xmlbinding.valuetypes.StatusT;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.lifecycle.persistence.StateArchive;
import alma.lifecycle.stateengine.constants.Role;
import alma.lifecycle.stateengine.constants.Subsystem;
import alma.offline.ASDMArchivedEvent;
import alma.offline.SubScanProcessedEvent;
import alma.offline.SubScanSequenceEndedEvent;
import alma.scheduling.SchedulingException;
import alma.scheduling.algorithm.astro.InterferometrySensitivityCalculator;
import alma.scheduling.algorithm.astro.SingleDishSensitivityCalculator;
import alma.scheduling.array.executor.services.ControlArray;
import alma.scheduling.array.executor.services.EventPublisher;
import alma.scheduling.array.executor.services.Pipeline;
import alma.scheduling.array.sbQueue.SchedBlockItem;
import alma.scheduling.array.sessions.SessionManager;
import alma.scheduling.datamodel.executive.ExecutiveTimeSpent;
import alma.scheduling.datamodel.observation.ExecBlock;
import alma.scheduling.datamodel.observation.ExecStatus;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObservingParameters;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.ScienceParameters;
import alma.scheduling.datamodel.obsproject.dao.ModelAccessor;
import alma.scheduling.datamodel.weather.HumidityHistRecord;
import alma.scheduling.datamodel.weather.TemperatureHistRecord;
import alma.scheduling.utils.Constants;
import alma.scheduling.utils.CoordinatesUtil;
import alma.scheduling.utils.ErrorHandling;
import alma.scheduling.utils.LoggerFactory;

public class ExecutionContext {

	protected ModelAccessor getModel() {
		return executor.getServices().getModel();
	}

	protected ControlArray getControlArray() {
		return executor.getServices().getControlArray();
	}

	protected Pipeline getPipeline() {
		return executor.getServices().getPipeline();
	}

	protected EventPublisher getEventPublisher() {
		return executor.getServices().getEventPublisher();
	}

	private Logger logger = LoggerFactory.getLogger(getClass());

	private ExecutionState state;

	private SchedBlockItem schedBlockItem;

	private SchedBlock schedBlock;

	private Executor executor;

	private long startTimestamp;
	private long stopTimestamp;
	private long archivedTimestamp;

	private Lock receptionLock = new ReentrantLock();
	private Condition startedCV = receptionLock.newCondition();
	private ExecBlockStartedEvent startedEvent;
	private Condition endedCV = receptionLock.newCondition();
	private ExecBlockEndedEvent endedEvent;
	private Condition archivedCV = receptionLock.newCondition();
	private ASDMArchivedEvent archivedEvent;

	private IDLEntityRef execBlockRef;
	private IDLEntityRef sessionRef;
	private IDLEntityRef schedBlockRef;

	private long execTime = 0;
	private DateFormat dateFormat;

	private int numOfAntennas;
	private double antDiameter;

	private TreeSet<SubScanProcessedEvent> SSPSet;
	private TreeSet<SubScanSequenceEndedEvent> SSSSet;

	public ExecutionContext(SchedBlockItem schedBlockItem, Executor executor,
			boolean manual, int numOfAntennas, double antDiameter) {
		this.schedBlockItem = schedBlockItem;
		this.executor = executor;
		this.schedBlock = getModel().getSchedBlockFromEntityId(
				schedBlockItem.getUid());
		this.numOfAntennas = numOfAntennas;
		this.antDiameter = antDiameter;
		this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		this.SSPSet = new TreeSet<SubScanProcessedEvent>(
				new SubScanProcessedEventComparator());
		this.SSSSet = new TreeSet<SubScanSequenceEndedEvent>(
				new SubScanSequenceEndedEventComparator());
		if (manual) {
			state = new ManualReadyExecutionState(this);
		} else {
			state = new ReadyExecutionState(this);
		}
		logger.info(String.format(
				"Creating ExecutionContext for SB %s @ %d (in state %s)",
				schedBlockItem.getUid(), schedBlockItem.getTimestamp(),
				this.state));
	}

	protected void setState(ExecutionState state) {
		// Just take care of the not manual arrays statuses
		logger.info(String.format(
				"%s.setState(): moving SB %s @ %d from %s to %s", this
						.getClass().getSimpleName(), schedBlockItem.getUid(),
				schedBlockItem.getTimestamp(), this.state, state));
		if (state instanceof CompleteExecutionState) {
			// Nothing more to do here
		} else if ((state instanceof RunningExecutionState)
				|| (state instanceof ManualRunningExecutionState)) {
			StatusTStateType fromStatus = null;
			StatusTStateType toStatus = null;
			SchedBlock sb = getSchedBlock();
			SBStatus sbs = getSBStatusOrNullFor(sb);

			if (sb.isOnCSVLifecycle(sbs)) {
				fromStatus = StatusTStateType.CSVREADY;
				toStatus = StatusTStateType.CSVRUNNING;
			} else {
				fromStatus = StatusTStateType.READY;
				toStatus = StatusTStateType.RUNNING;
			}
			try {
				doStateArchiveTransition(getSchedBlock().getStatusEntity(),
						fromStatus, toStatus);
			} catch (Exception e) {
				ErrorHandling.warning(logger, String.format(
						"Error marking SchedBlock %s @ %s as %s - %s",
						schedBlockItem.getUid(), schedBlockItem.getTimestamp(),
						toStatus.toString(), e.getMessage()), e);
				this.setState(new FailedExecutionState(this));
				return;
			}
		} else if (state instanceof ArchivingExecutionState) {
			/*
			 * We only get here when we get an ExecBlockEnded event with a
			 * successful status. Do the book-keeping here and also move the SB
			 * to the correct lifecycle state after RUNNING (which will be
			 * READY, CSVREADY or SUSPENDED).
			 */
			try {
				ArchivingExecutionState aes = (ArchivingExecutionState) state;
				final SBStatus sbStatus = getSBStatusFor(getSchedBlock());
				accountExecution(sbStatus, execTime, aes.getFinalState());
			} catch (Exception e) {
				ErrorHandling
						.warning(
								logger,
								String.format(
										"Error post-processing completed SchedBlock %s @ %s - %s",
										schedBlockItem.getUid(),
										schedBlockItem.getTimestamp(),
										e.getMessage()), e);
				this.setState(new FailedExecutionState(this));
				return;
			} // The state transition is done inside this method
		} else if (state instanceof ManualCompleteExecutionState) {
			StatusTStateType fromStatus = null;
			StatusTStateType toStatus = null;
			SchedBlock sb = getSchedBlock();
			SBStatus sbs = getSBStatusOrNullFor(sb);

			if (sb.isOnCSVLifecycle(sbs)) {
				fromStatus = StatusTStateType.CSVRUNNING;
				toStatus = StatusTStateType.CSVREADY;
			} else {
				fromStatus = StatusTStateType.RUNNING;
				toStatus = StatusTStateType.READY;
			}
			try {
				doStateArchiveTransition(getSchedBlock().getStatusEntity(),
						fromStatus, toStatus);
			} catch (Exception e) {
				ErrorHandling.warning(logger, String.format(
						"Error marking SchedBlock %s @ %s as %s - %s",
						schedBlockItem.getUid(), schedBlockItem.getTimestamp(),
						toStatus.toString(), e.getMessage()), e);
				this.setState(new FailedExecutionState(this));
				return;
			}

			// this is a manual array, so requeue the just-completed SB for
			// running again, unless the processing has been stopped.
			if (executor.isRunning()) {
				final SchedBlockItem again = new SchedBlockItem(
						schedBlockItem.getUid(), System.nanoTime());
				executor.getQueue().offer(again);
			}

		} else if (state instanceof FailedExecutionState) {
			try {
				final SBStatus sbStatus = getSBStatusFor(getSchedBlock());
				final long now = System.currentTimeMillis();
				long timeTaken = 0;
				if (startedEvent != null) {
					final long then = (long) UTCUtility
							.utcOmgToJava(startedEvent.startTime);
					timeTaken = now - then;
				}
				accountFailure(sbStatus, timeTaken);
			} catch (Exception e) {
				ErrorHandling.warning(logger, String.format(
						"Error post-processing failed SchedBlock %s @ %s - %s",
						schedBlockItem.getUid(), schedBlockItem.getTimestamp(),
						e.getMessage()), e);
				// this.setState(new FailedExecutionState(this));
			} // The state transition is done inside this method
		}

		this.executor.notify(new ExecutionStateChange(schedBlockItem, state
				.toString()));
		this.state = state;
	}

	public SchedBlock getSchedBlock() {
		return schedBlock;
	}

	private String initialBookkeeping = "";

	public void startObservation() {
		initialBookkeeping = getSchedBlock().bookkeepingString(
				getSBStatusFor(getSchedBlock()));
		state.startObservation();
	}

	public void stopObservation() {
		state.stopObservation();
	}

	public void abortObservation() {
		state.abortObservation();
	}

	public void waitForArchival() {
		state.waitArchival();
	}

	public void observe() {
		execTime = state.observe();
	}

	public void processExecBlockStartedEvent(ExecBlockStartedEvent event) {
		logger.info(String.format(
				"%s: processing ExecBlockStartedEvent for %s (execId = %s)",
				this.getClass().getSimpleName(), event.sbId.entityId,
				event.execId.entityId));
		startTimestamp = System.currentTimeMillis();
		try {
			receptionLock.lock();
			startedEvent = event;
			setExecBlockRef(event.execId);
			startedCV.signal();
		} finally {
			receptionLock.unlock();
		}
		logger.info(String.format(
				"%s: processed ExecBlockStartedEvent for %s (execRef now %s)",
				this.getClass().getSimpleName(), event.sbId.entityId,
				execBlockRef.entityId));
	}

	public void interruptWaitForExecBlockStartedEvent() {
		logger.info(String.format(
				"%s: interrupting wait for ExecBlockStartedEvent", this
						.getClass().getSimpleName()));
		startTimestamp = System.currentTimeMillis();
		try {
			receptionLock.lock();
			startedEvent = null;
			setExecBlockRef(null);
			startedCV.signal();
		} finally {
			receptionLock.unlock();
		}
	}

	public void processExecBlockEndedEvent(ExecBlockEndedEvent event) {
		logger.info(String.format("%s: processing ExecBlockEndedEvent for %s",
				this.getClass().getSimpleName(), event.sbId.entityId,
				event.execId.entityId));
		stopTimestamp = System.currentTimeMillis();
		try {
			receptionLock.lock();
			endedEvent = event;
			endedCV.signal();
		} finally {
			receptionLock.unlock();
		}
		logger.info(String.format(
				"%s: processed ExecBlockEndedEvent for %s (execRef now %s)",
				this.getClass().getSimpleName(), event.sbId.entityId,
				execBlockRef.entityId));
	}

	public void processASDMArchivedEvent(ASDMArchivedEvent event) {
		logger.info(String.format("%s: processing ASDMArchivedEvent", this
				.getClass().getSimpleName()));
		// throw something if the ExecBlockStartedEvent hasn't arrived yet.
		archivedTimestamp = System.currentTimeMillis();
		try {
			receptionLock.lock();
			archivedEvent = event;
			archivedCV.signal();
		} finally {
			receptionLock.unlock();
		}
		logger.info(String.format("%s: processed ASDMArchivedEvent", this
				.getClass().getSimpleName()));
	}

	public long getQueuedTimestamp() {
		return schedBlockItem.getTimestamp();
	}

	public long getStartTimestamp() {
		return startTimestamp;
	}

	public long getStopTimestamp() {
		return stopTimestamp;
	}

	public long getArchivedTimestamp() {
		return archivedTimestamp;
	}

	public String getSbUid() {
		return schedBlock.getUid();
	}

	protected ExecBlockStartedEvent waitForExecBlockStartedEvent(long timeout) {
		try {
			receptionLock.lock();
			while (startedEvent == null) {
				boolean received;
				try {
					received = startedCV.await(timeout, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					logger.info("Shutting down thread at waitForExecBlockStartedEvent");
					return null;
				}
				if (!received) {
					logger.severe("timeout waiting for ExecBlockStartedEvent");
					break;
				}
			}
			return startedEvent;
		} finally {
			receptionLock.unlock();
		}
	}

	protected ExecBlockStartedEvent waitForExecBlockStartedEvent() {
		try {
			receptionLock.lock();
			try {
				startedCV.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			}
			return startedEvent;
		} finally {
			receptionLock.unlock();
		}
	}

	protected ExecBlockEndedEvent waitForExecBlockEndedEvent() {
		try {
			receptionLock.lock();
			while (endedEvent == null) {
				try {
					endedCV.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
					return null;
				}
			}
			return endedEvent;
		} finally {
			receptionLock.unlock();
		}
	}

	protected ASDMArchivedEvent waitForASDMArchivedEvent(long timeout) {
		try {
			receptionLock.lock();
			while (archivedEvent == null) {
				boolean received;
				try {
					received = archivedCV.await(timeout, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
					return null;
				}
				if (!received) {
					logger.severe("timeout waiting for ASDMArchivedEvent");
					break;
				}
			}
			return archivedEvent;
		} finally {
			receptionLock.unlock();
		}
	}

	protected IDLEntityRef getExecBlockRef() {
		return execBlockRef;
	}

	protected void setExecBlockRef(IDLEntityRef ref) {
		execBlockRef = ref;
	}

	protected SessionManager getSessions() {
		return executor.getSessions();
	}

	protected IDLEntityRef getSessionRef() {
		return sessionRef;
	}

	protected void setSessionRef(IDLEntityRef ref) {
		sessionRef = ref;
	}

	protected IDLEntityRef getSchedBlockRef() {
		return schedBlockRef;
	}

	protected void setSchedBlockRef(IDLEntityRef ref) {
		schedBlockRef = ref;
		try {
			getControlArray().configure(ref);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected boolean isFullAuto() {
		return executor.isFullAuto();
	}

	public void processSubScanSequenceEndedEvent(SubScanSequenceEndedEvent event) {
		logger.info(String.format("%s: processing SubScanSequenceEndedEvent",
				this.getClass().getSimpleName()));
		if (execBlockRef != null
				&& execBlockRef.entityId
						.equals(event.processedExecBlockId.entityId)) {
			synchronized (this) {
				SSSSet.add(event);
			}
		} else {
			String msg = "Discarded SSSE event: currentExecId=";
			if (execBlockRef != null)
				msg += execBlockRef.entityId;
			else
				msg += execBlockRef;
			msg += " event.processedExecBlockId="
					+ event.processedExecBlockId.entityId;
			logger.fine(msg);
		}
	}

	public void processSubScanProcessedEvent(SubScanProcessedEvent event) {
		logger.info(String.format("%s: processing SubScanProcessedEvent", this
				.getClass().getSimpleName()));
		if (execBlockRef != null
				&& execBlockRef.entityId
						.equals(event.processedExecBlockId.entityId)) {
			synchronized (this) {
				SSPSet.add(event);
			}
		} else {
			String msg = "Discarded SSP event: currentExecId=";
			if (execBlockRef != null)
				msg += execBlockRef.entityId;
			else
				msg += execBlockRef;
			msg += " event.processedExecBlockId="
					+ event.processedExecBlockId.entityId;
			logger.fine(msg);
		}
	}

	/*
	 * ================================================================ Status
	 * Entity updating
	 * ================================================================
	 */
	/**
	 * Get the SBStatus for the given SchedBlock
	 * 
	 * @throws AcsJInappropriateEntityTypeEx
	 * @throws AcsJNoSuchEntityEx
	 * @throws AcsJNullEntityIdEx
	 */
	private SBStatus getSBStatusFor(SchedBlock schedBlock) {
		SBStatus result = null;

		final StateArchive s = getModel().getStateArchive();
		result = s.getSBStatus(schedBlock.getStatusEntity());
		return result;
	}

	/**
	 * Get the SBStatus for the given SchedBlock, or null if there's a problem
	 * 
	 * @throws AcsJNoSuchEntityEx
	 *             if there is no entity in the archive matching the supplied
	 *             statusEntityId argument
	 */
	private SBStatus getSBStatusOrNullFor(SchedBlock schedBlock) {
		SBStatus result = null;

		result = getSBStatusFor(schedBlock);
		return result;
	}

	/**
	 * Get the OUSStatus which contains the given SBStatus
	 * 
	 * @throws AcsJInappropriateEntityTypeEx
	 * @throws AcsJNoSuchEntityEx
	 * @throws AcsJNullEntityIdEx
	 */
	private OUSStatus getContainingOUSStatus(SBStatus sbStatus) {
		OUSStatus result = null;

		final OUSStatusRefT ref = sbStatus.getContainingObsUnitSetRef();
		final StateArchive s = getModel().getStateArchive();
		final OUSStatusEntityT id = createEntity(ref);
		result = s.getOUSStatus(id);
		return result;
	}

	/**
	 * Get the OUSStatus which contains the given OUSStatus
	 * 
	 * @throws AcsJInappropriateEntityTypeEx
	 * @throws AcsJNoSuchEntityEx
	 * @throws AcsJNullEntityIdEx
	 */
	private ProjectStatus getProjectStatus(ObsUnitStatusT status) {
		ProjectStatus result = null;

		final ProjectStatusRefT ref = status.getProjectStatusRef();
		if (ref != null) {
			final StateArchive s = getModel().getStateArchive();
			final ProjectStatusEntityT id = createEntity(ref);
			result = s.getProjectStatus(id);
		}
		return result;
	}

	/**
	 * Get the OUSStatus which contains the given OUSStatus
	 * 
	 * @throws AcsJInappropriateEntityTypeEx
	 * @throws AcsJNoSuchEntityEx
	 * @throws AcsJNullEntityIdEx
	 */
	private OUSStatus getContainingOUSStatus(OUSStatus ousStatus) {
		OUSStatus result = null;

		final OUSStatusRefT ref = ousStatus.getContainingObsUnitSetRef();
		if (ref != null) {
			final StateArchive s = getModel().getStateArchive();
			final OUSStatusEntityT id = createEntity(ref);
			result = s.getOUSStatus(id);
		}
		return result;
	}

	/**
	 * Create an EntityT from the given RefT.
	 * 
	 * @param ref
	 * @return
	 */
	private ProjectStatusEntityT createEntity(ProjectStatusRefT ref) {
		final ProjectStatusEntityT result = new ProjectStatusEntityT();

		result.setDocumentVersion(ref.getDocumentVersion());
		result.setEntityId(ref.getEntityId());
		result.setEntityTypeName(ref.getEntityTypeName());

		return result;
	}

	/**
	 * Create an EntityT from the given RefT.
	 * 
	 * @param ref
	 * @return
	 */
	private OUSStatusEntityT createEntity(OUSStatusRefT ref) {
		final OUSStatusEntityT result = new OUSStatusEntityT();

		result.setDocumentVersion(ref.getDocumentVersion());
		result.setEntityId(ref.getEntityId());
		result.setEntityTypeName(ref.getEntityTypeName());

		return result;
	}

	/**
	 * Update the given projectStatus after a successful execution of an SB
	 * within it.
	 * 
	 * @param projectStatus
	 *            - the status object to update;
	 * @param endTime
	 *            - when the exec block finished;
	 * @param timeInSec
	 *            - the amount of observing time taken.
	 * 
	 * @throws SchedulingException
	 */
	private void updateForSuccess(ProjectStatus projectStatus, String endTime,
			int secs) throws SchedulingException {
		if (projectStatus.getHasExecutionCount()) {
			projectStatus.setSuccessfulExecutions(projectStatus
					.getSuccessfulExecutions() + 1);
			projectStatus.setExecutionsRemaining(projectStatus
					.getExecutionsRemaining() - 1);
		}

		if (projectStatus.getHasTimeLimit()) {
			projectStatus.setSuccessfulSeconds(projectStatus
					.getSuccessfulSeconds() + secs);
			projectStatus.setSecondsRemaining(projectStatus
					.getSecondsRemaining() - secs);
		}

		projectStatus.setTimeOfUpdate(endTime);
		try {
			getModel().getStateArchive().insertOrUpdate(projectStatus,
					Subsystem.SCHEDULING);
		} catch (Exception e) {
			ErrorHandling.warning(logger, String.format(
					"Error updating ProjectStatus %s: %s", projectStatus
							.getProjectStatusEntity().getEntityId(), e
							.getMessage()), e);
		}
	}

	/**
	 * Update the given ousStatus after a successful execution of an SB within
	 * it. Percolate the relevant info up the OUSStatus hierarchy.
	 * 
	 * @param ousStatus
	 *            - the status object to update;
	 * @param endTime
	 *            - when the exec block finished;
	 * @param timeInSec
	 *            - the amount of observing time taken.
	 * @param updateSBCounts
	 *            - do we update the SB counts (for when its the lowest level
	 *            OUSStatus) or not?
	 * 
	 * @throws SchedulingException
	 */
	private void updateForSuccess(OUSStatus ousStatus, String endTime,
			int timeInSec, boolean updateSBCounts) throws SchedulingException {
		if (ousStatus != null) {
			if (ousStatus.getHasExecutionCount()) {
				ousStatus.setSuccessfulExecutions(ousStatus
						.getSuccessfulExecutions() + 1);
				ousStatus.setExecutionsRemaining(ousStatus
						.getExecutionsRemaining() - 1);
			}

			if (ousStatus.getHasTimeLimit()) {
				ousStatus.setSuccessfulSeconds(ousStatus.getSuccessfulSeconds()
						+ timeInSec);
				ousStatus.setSecondsRemaining(ousStatus.getSecondsRemaining()
						- timeInSec);
			}

			final int time = ousStatus.getTotalUsedTimeInSec();

			if (updateSBCounts) {
				final int worked = ousStatus.getNumberSBsCompleted();
				ousStatus.setNumberSBsCompleted(worked + 1);
			}
			ousStatus.setTotalUsedTimeInSec(time + timeInSec);
			ousStatus.setTimeOfUpdate(endTime);
			try {
				getModel().getStateArchive().insertOrUpdate(ousStatus,
						Subsystem.SCHEDULING);
				final OUSStatus parent = getContainingOUSStatus(ousStatus);
				if (parent != null) {
					updateForSuccess(parent, endTime, timeInSec, false);
				} else {
					final ProjectStatus projectStatus = getProjectStatus(ousStatus);
					updateForSuccess(projectStatus, endTime, timeInSec);
				}
			} catch (Exception e) {
				ErrorHandling
						.warning(
								logger,
								String.format(
										"Error updating OUSStatus %s for ObsUnitSet %s in %s: %s",
										ousStatus.getOUSStatusEntity()
												.getEntityId(),
										ousStatus.getObsUnitSetRef()
												.getPartId(), ousStatus
												.getObsUnitSetRef()
												.getEntityId(), e.getMessage()),
								e);
			}
		}
	}

	/**
	 * Update the given sbStatus after a successful execution of its SB.
	 * Percolate the relevant info up the OUSStatus hierarchy.
	 * 
	 * @param sbStatus
	 *            - the status object to update;
	 * @param endTime
	 *            - when the exec block finished;
	 * @param secs
	 *            - the amount of observing time taken.
	 * 
	 * @throws SchedulingException
	 */
	private void updateForSuccess(SBStatus sbStatus, String endTime, int secs,
			boolean csv, StatusTStateType state) {

		// Calculate sensitivity for this execution
		try {
			double sensJy = calculateSensitivity();

			// Create ExecStatus for the current Execution
			addExecStatus(sbStatus, state, sensJy, csv);

			// Add the current sensitivity achieved to the total
			// Check DSA document, SB Status subsection
			if (sbStatus.getHasExecutionCount()) {
				double totalSensJy = Math.sqrt(Math.pow(
						sbStatus.getSuccessfulExecutions(), 2)
						* Math.pow(sbStatus.getSensitivityAchievedJy(), 2)
						+ Math.pow(sensJy, 2))
						/ (sbStatus.getSuccessfulExecutions() + 1);
				sbStatus.setSensitivityAchievedJy(totalSensJy);
				logger.info("Accumulated senitivity so far for SchedBlock: "
						+ schedBlock.getUid() + "= " + totalSensJy + " [Jy]");
			}
		} catch (Exception ex) {
			AcsJSchedulingInternalExceptionEx e = new AcsJSchedulingInternalExceptionEx(
					ex);
			e.setProperty("Reason",
					"Failed calculation of the sensitivity achieved for this execution");
			e.setProperty("ExecBlockId", getExecBlockRef().entityId);
			e.log(logger);
			ex.printStackTrace();
			// TODO: Report in some way that the Sensitivity was not properly
			// calculated
			addExecStatus(sbStatus, state, 10D, csv);
		}
		if (sbStatus.getHasExecutionCount()) {
			sbStatus.setSuccessfulExecutions(sbStatus.getSuccessfulExecutions() + 1);
			sbStatus.setExecutionsRemaining(sbStatus.getExecutionsRemaining() - 1);
		}

		if (sbStatus.getHasTimeLimit()) {
			sbStatus.setSuccessfulSeconds(sbStatus.getSuccessfulSeconds()
					+ secs);
			sbStatus.setSecondsRemaining(sbStatus.getSecondsRemaining() - secs);
		}

		sbStatus.setTotalUsedTimeInSec(sbStatus.getTotalUsedTimeInSec() + secs); // old
																					// stuff

		sbStatus.setTimeOfUpdate(endTime);
		try {
			getModel().getStateArchive().insertOrUpdate(sbStatus,
					Subsystem.SCHEDULING);
			updateForSuccess(getContainingOUSStatus(sbStatus), endTime, secs,
					true);
		} catch (Exception e) {
			ErrorHandling.warning(logger, String.format(
					"Error updating SBStatus %s for SchedBlock %s: %s",
					sbStatus.getSBStatusEntity().getEntityId(), sbStatus
							.getSchedBlockRef().getEntityId(), e.getMessage()),
					e);
		}
	}

	/**
	 * Update the given ousStatus after an unsuccessful execution of an SB
	 * within it. Percolate the relevant info up the OUSStatus hierarchy.
	 * 
	 * @param ousStatus
	 *            - the status object to update;
	 * @param endTime
	 *            - when the exec block finished;
	 * @param updateSBCounts
	 *            - do we update the SB counts (for when its the lowest level
	 *            OUSStatus) or not?
	 * 
	 * @throws SchedulingException
	 */
	private void updateForFailure(ProjectStatus projectStatus, String endTime,
			int secs) {
		if (projectStatus.getHasExecutionCount()) {
			projectStatus.setFailedExecutions(projectStatus
					.getFailedExecutions() + 1);
		}

		if (projectStatus.getHasTimeLimit()) {
			projectStatus.setFailedSeconds(projectStatus.getFailedSeconds()
					+ secs);
		}

		projectStatus.setTimeOfUpdate(endTime);

		try {
			getModel().getStateArchive().insertOrUpdate(projectStatus,
					Subsystem.SCHEDULING);
		} catch (Exception e) {
			ErrorHandling.warning(logger, String.format(
					"Error updating ProjectStatus %s: %s", projectStatus
							.getProjectStatusEntity().getEntityId(), e
							.getMessage()), e);
		}
	}

	/**
	 * Update the given ousStatus after an unsuccessful execution of an SB
	 * within it. Percolate the relevant info up the OUSStatus hierarchy.
	 * 
	 * @param ousStatus
	 *            - the status object to update;
	 * @param endTime
	 *            - when the exec block finished;
	 * @param updateSBCounts
	 *            - do we update the SB counts (for when its the lowest level
	 *            OUSStatus) or not?
	 * 
	 * @throws SchedulingException
	 */
	private void updateForFailure(OUSStatus ousStatus, String endTime,
			int secs, boolean updateSBCounts) {
		if (ousStatus != null) {
			if (ousStatus.getHasExecutionCount()) {
				ousStatus
						.setFailedExecutions(ousStatus.getFailedExecutions() + 1);
			}

			if (ousStatus.getHasTimeLimit()) {
				ousStatus.setFailedSeconds(ousStatus.getFailedSeconds() + secs);
			}

			if (updateSBCounts) {
				final int failed = ousStatus.getNumberSBsFailed();
				ousStatus.setNumberSBsFailed(failed + 1);
			}
			ousStatus.setTimeOfUpdate(endTime);
			try {
				getModel().getStateArchive().insertOrUpdate(ousStatus,
						Subsystem.SCHEDULING);
				final OUSStatus parent = getContainingOUSStatus(ousStatus);
				if (parent != null) {
					updateForFailure(getContainingOUSStatus(ousStatus),
							endTime, secs, false);
				} else {
					final ProjectStatus projectStatus = getProjectStatus(ousStatus);
					updateForFailure(projectStatus, endTime, secs);
				}
			} catch (Exception e) {
				ErrorHandling
						.warning(
								logger,
								String.format(
										"Error updating OUSStatus %s for ObsUnitSet %s in %s: %s",
										ousStatus.getOUSStatusEntity()
												.getEntityId(),
										ousStatus.getObsUnitSetRef()
												.getPartId(), ousStatus
												.getObsUnitSetRef()
												.getEntityId(), e.getMessage()),
								e);
			}
		}
	}

	/**
	 * Update the given sbStatus after an unsuccessful execution of its SB.
	 * Percolate the relevant info up the OUSStatus hierarchy.
	 * 
	 * @param sbStatus
	 *            - the status object to update;
	 * @param endTime
	 *            - when the exec block finished;
	 * 
	 * @throws SchedulingException
	 */
	private void updateForFailure(SBStatus sbStatus, String endTime, int secs, boolean csv) {
		// Calculate sensitivity for this execution
		try {
			double sensJy = calculateSensitivity();

			addExecStatus(sbStatus, StatusTStateType.BROKEN, sensJy, csv);
		} catch (Exception ex) {
			AcsJSchedulingInternalExceptionEx e = new AcsJSchedulingInternalExceptionEx(
					ex);
			e.setProperty("Reason",
					"Failed calculation of the sensitivity achieved for this execution");
			e.setProperty("ExecBlockId", getExecBlockRef().entityId);
			e.log(logger);
			ex.printStackTrace();
			// TODO: Report in some way that the Sensitivity was not properly
			// calculated
			addExecStatus(sbStatus, StatusTStateType.BROKEN, 10D, csv);
		}
		if (sbStatus.getHasExecutionCount()) {
			sbStatus.setFailedExecutions(sbStatus.getFailedExecutions() + 1);
		}

		if (sbStatus.getHasTimeLimit()) {
			sbStatus.setFailedSeconds(sbStatus.getFailedSeconds() + secs);
		}

		try {
			getModel().getStateArchive().insertOrUpdate(sbStatus,
					Subsystem.SCHEDULING);
			updateForFailure(getContainingOUSStatus(sbStatus), endTime, secs,
					true);
		} catch (Exception e) {
			ErrorHandling.warning(logger, String.format(
					"Error updating SBStatus %s for SchedBlock %s: %s",
					sbStatus.getSBStatusEntity().getEntityId(), sbStatus
							.getSchedBlockRef().getEntityId(), e.getMessage()),
					e);
		}
	}

	private double calculateSensitivity() {
		double expTime = 0.0; // in hours
		double freqGHz = schedBlock.getSchedulingConstraints()
				.getRepresentativeFrequency();
		double bwGHz = 2.0;
		// double sensGoalJy = 10.0;
		// for (ObservingParameters params: schedBlock.getObservingParameters())
		// {
		// if (params instanceof ScienceParameters) {
		// bwGHz = ((ScienceParameters) params).getRepresentativeBandwidth();
		// sensGoalJy = ((ScienceParameters) params).getSensitivityGoal();
		// }
		// }
		double raDeg = schedBlock.getSchedulingConstraints()
				.getRepresentativeTarget().getSource().getCoordinates().getRA();
		double declDeg = schedBlock.getSchedulingConstraints()
				.getRepresentativeTarget().getSource().getCoordinates()
				.getDec();
		TemperatureHistRecord tr = getModel().getWeatherDao()
				.getTemperatureForTime(new Date());
		HumidityHistRecord hr = getModel().getWeatherDao().getHumidityForTime(
				new Date());

		double pwv = getModel().getOpacityInterpolator().estimatePWV(
				hr.getValue(), tr.getValue());
		double[] tmp = getModel().getOpacityInterpolator()
				.interpolateOpacityAndTemperature(pwv, freqGHz);
		double opacity = tmp[0];
		double atmBrightnessTemp = tmp[1];
		TreeMap<SubScanSequenceEndedEvent, ArrayList<SubScanProcessedEvent>> scans = null;
		synchronized (this) {
			scans = processScanEvents();
		}
		for (SubScanSequenceEndedEvent ssseEv : scans.keySet()) {
			for (SubScanProcessedEvent sspEv : scans.get(ssseEv)) {
				if (sspEv.representativeScienceSubScan) {
					double ssTime = (sspEv.subscanEndTime - sspEv.subscanStartTime) * 1e-7D / 3600.0; // to
																										// hours
					if (ssTime <= 0) {
						String msg = "Time of observation reported for execId, scan, subscan = ("
								+ sspEv.processedExecBlockId.entityId
								+ ", "
								+ sspEv.processedScanNum
								+ ", "
								+ sspEv.processedSubScanNum
								+ ") "
								+ "is not a valid time: " + ssTime + "hours";
						logger.warning(msg);
						ssTime = 0D;
					}
					expTime += ssTime;
				}
			}
		}
		double sensJy = 10D;
		switch (schedBlock.getSchedulingConstraints().getSchedBlockMode()) {
		case INTERFEROMETRY:
			sensJy = InterferometrySensitivityCalculator
					.pointSourceSensitivity(
							expTime,
							freqGHz,
							bwGHz,
							raDeg,
							declDeg,
							numOfAntennas,
							antDiameter,
							alma.scheduling.utils.Constants.CHAJNANTOR_LATITUDE,
							opacity, atmBrightnessTemp, new Date());
			break;
		case SINGLE_DISH:
			SingleDishSensitivityCalculator.pointSourceSensitivity(expTime,
					freqGHz, bwGHz, raDeg, declDeg, numOfAntennas, antDiameter,
					alma.scheduling.utils.Constants.CHAJNANTOR_LATITUDE,
					opacity, atmBrightnessTemp, new Date());
			break;
		default:
			logger.severe("No idea how to calculate the sensitivity for: "
					+ schedBlock.getSchedulingConstraints().getSchedBlockMode());
			break;
		}

		if (sensJy > 1.0) {
			String msg = new String(
					"  High Sensitivity detected in "
							+ schedBlock.getSchedulingConstraints()
									.getSchedBlockMode()
							+ "  SchedBlock ID: "
							+ schedBlock.getId()
							+ "\n"
							+ "  Number of Antennas (diameter):"
							+ this.numOfAntennas
							+ " ("
							+ this.antDiameter
							+ ")\n"
							+ "  Temp and Humi: "
							+ hr.getValue()
							+ ", "
							+ tr.getValue()
							+ "\n"
							+ "  opacityInterpolator.estimatePWV(): "
							+ pwv
							+ "\n"
							+ "  opacityInterpolator.interpolateOpacityAndTemperature().opacity: "
							+ opacity
							+ "\n"
							+ "  opacityInterpolator.interpolateOpacityAndTemperature().atmBrightnessTemp: "
							+ atmBrightnessTemp
							+ "\n"
							+ "  InterferometrySensitivityCalculator.pointSourceSensitivity(): "
							+ sensJy
							+ "\n"
							+ "  RA: "
							+ raDeg
							+ "     Dec: "
							+ declDeg
							+ "\n"
							+ "  Hour Angle at the given time: "
							+ CoordinatesUtil.getHourAngle(new Date(),
									schedBlock.getSchedulingConstraints()
											.getRepresentativeTarget()
											.getSource().getCoordinates()
											.getRA() / 15,
									Constants.CHAJNANTOR_LONGITUDE));
			logger.warning(msg);
		}
		return sensJy;
	}

	private void addExecStatus(SBStatus sbStatus, StatusTStateType state,
			double sensJy, boolean isCsv) {
		Date startDate =  new Date(UTCUtility.utcOmgToJava(startedEvent.startTime));
		Date endDate = new Date(UTCUtility.utcJavaToOmg(endedEvent.endTime));
		
		//State Archive related stuff
		ExecStatusT es = new ExecStatusT();
		StatusT execStatus = new StatusT();
		ExecBlockRefT ref = new ExecBlockRefT();
		final String startTime = dateFormat.format(startDate);
		final String endTime = dateFormat.format(endDate);

		if (getExecBlockRef() != null)
			ref.setExecBlockId(getExecBlockRef().entityId);
		if (sbStatus != null)
			es.setEntityPartId(Utils.genPartId(sbStatus));
		es.setExecBlockRef(ref);
		es.setArrayName(executor.getArrayName());
		es.setTimeOfCreation(startTime);
		es.setSensitivityAchievedJy(sensJy);
		execStatus.setStartTime(startTime);
		execStatus.setEndTime(endTime);
		execStatus.setState(state);
		es.setStatus(execStatus);
		sbStatus.addExecStatus(es);
		
		if (isCsv)
			return;
		
		//Only interested in real projects
		//SWDB related stuff
		ExecBlock eb = new ExecBlock();
		eb.setStartTime(startDate);
		eb.setEndTime(endDate);
		eb.setExecBlockUid(getExecBlockRef().entityId);
		eb.setSchedBlockUid(getSbUid());
		eb.setSensitivityAchieved(sensJy);
		eb.setExecutive(getSchedBlock().getExecutive().getName());
		if (state.getType() == StatusTStateType.FULLYOBSERVED_TYPE)
			eb.setStatus(ExecStatus.SUCCESS);
		else 
			eb.setStatus(ExecStatus.FAILURE);
		
	}

	private String format(ExecStatusT es, String prefix) {
		final StringBuilder b = new StringBuilder();
		final Formatter f = new Formatter(b);

		f.format("%sExecStatus {%n", prefix);
		f.format("%s\tEntityPartId   = %s%n", prefix, es.getEntityPartId());
		f.format("%s\tStatus {%n", prefix);
		f.format("%s\t\tState     = %s%n", prefix, es.getStatus().getState());
		f.format("%s\t\tReadyTime = %s%n", prefix, es.getStatus()
				.getReadyTime());
		f.format("%s\t\tStartTime = %s%n", prefix, es.getStatus()
				.getStartTime());
		f.format("%s\t\tEndTime   = %s%n", prefix, es.getStatus().getEndTime());
		f.format("%s\t}%n", prefix);
		f.format("%s\tTimeOfCreation = %s%n", prefix, es.getTimeOfCreation());
		f.format("%s\tTimeOfUpdate   = %s%n", prefix, es.getTimeOfUpdate());
		f.format("%s\tArrayName      = %s%n", prefix, es.getArrayName());
		f.format("%s\tExecBlockRef {%n", prefix);
		f.format("%s\t\tExecBlockId = %s%n", prefix, es.getExecBlockRef()
				.getExecBlockId());
		f.format("%s\t}%n", prefix);
		f.format("%s}", prefix);

		return b.toString();
	}

	/*
	 * End Status Entity updating
	 * =============================================================
	 */

	public void accountExecution(SBStatus sbStatus, long secs,
			StatusTStateType state) throws AcsJIllegalArgumentEx {
		double time = schedBlock.getSchedBlockControl()
				.getAccumulatedExecutionTime() + (secs / 3600.0);
		schedBlock.getSchedBlockControl().setAccumulatedExecutionTime(time);
		schedBlock.getSchedBlockControl().setExecutionCount(
				schedBlock.getSchedBlockControl().getExecutionCount() + 1);
		ObsProject prj = getModel().getObsProjectDao().findByEntityId(
				schedBlock.getProjectUid());
		prj.setTotalExecutionTime(time + prj.getTotalExecutionTime());

		if (!schedBlock.getManual() && !schedBlock.getCsv())
			saveExecBlock();
		
		// Avoid saves to the SWDB, due synch issues
		// model.getObsProjectDao().saveOrUpdate(prj);
		// model.getSchedBlockDao().saveOrUpdate(schedBlock);

		// Update State Archive Statuses
		SchedBlock sb = getSchedBlock();
		SBStatusEntityT sbId = sb.getStatusEntity();
		updateForSuccess(sbStatus, dateFormat.format(new Date()), (int) secs,
				sb.getCsv() || sb.isOnCSVLifecycle(sbStatus), state);

		logger.fine("\n\n************\n\n" + initialBookkeeping + "\n\n"
				+ getSchedBlock().bookkeepingString(sbStatus)
				+ "\n\n************");
		// Do the state transition
		StatusTStateType fromStatus = null;
		StatusTStateType toStatus = null;

		if (sb.isOnCSVLifecycle(sbStatus)) {
			fromStatus = StatusTStateType.CSVRUNNING;
			toStatus = StatusTStateType.CSVREADY;
		} else {
			fromStatus = StatusTStateType.RUNNING;
			if (executor.fullAuto()) {
				if (getSchedBlock().needsMoreExecutions(sbStatus)) {
					toStatus = StatusTStateType.READY;
				} else {
					toStatus = StatusTStateType.SUSPENDED;
				}
			} else {
				toStatus = StatusTStateType.SUSPENDED;
			}
		}
		doStateArchiveTransition(sbId, fromStatus, toStatus);
	}

	private void saveExecBlock() {
		ExecBlock eb = new ExecBlock();
		eb.setExecBlockUid(getExecBlockRef().entityId);
		eb.setSchedBlockUid(getSbUid());
		eb.setStatus(ExecStatus.SUCCESS);
		
		ExecutiveTimeSpent r = new ExecutiveTimeSpent();
		
		
	}

	public void accountFailure(SBStatus sbStatus, long secs)
			throws AcsJIllegalArgumentEx {
		// if (schedBlock.getParent().getFailuresCount() != null)
		// schedBlock.getParent().setFailuresCount(schedBlock.getParent().getFailuresCount());
		// else
		// schedBlock.getParent().setFailuresCount(1);

		// OUSStatusEntityT ousId =
		// getSchedBlock().getParent().getStatusEntity();
		// OUSStatus ousS;

		// Update State Archive Statuses
		// ousS = getModel().getStateArchive().getOUSStatus(ousId);
		// ousS.setNumberSBsFailed(ousS.getNumberSBsFailed() + 1);
		// getModel().getStateArchive().update(ousS);

		updateForFailure(sbStatus, dateFormat.format(new Date()), (int) secs, schedBlock.getCsv() || schedBlock.isOnCSVLifecycle(sbStatus));
		StatusTStateType fromStatus = null;
		StatusTStateType toStatus = null;

		if (getSchedBlock().isOnCSVLifecycle(sbStatus)) {
			fromStatus = StatusTStateType.CSVRUNNING;
			toStatus = StatusTStateType.CSVREADY;
		} else {
			fromStatus = StatusTStateType.RUNNING;
			toStatus = StatusTStateType.SUSPENDED;
		}
		doStateArchiveTransition(getSchedBlock().getStatusEntity(), fromStatus,
				toStatus);
	}

	private void doStateArchiveTransition(SBStatusEntityT sbStatusId,
			StatusTStateType fromStatus, StatusTStateType toStatus)
			throws AcsJIllegalArgumentEx {

		final String subsystem = Subsystem.SCHEDULING;
		final String role = Role.AOD;

		logger.info("Doing transition of SBStatusEntityT: "
				+ sbStatusId.getEntityId() + " from: " + fromStatus + " to: "
				+ toStatus + ", Role: " + role);
		getModel().getStateEngine().changeState(sbStatusId, toStatus,
				subsystem, role);
	}

	/**
	 * @return
	 * @see alma.scheduling.array.executor.Executor#getStartEventTimeoutMS()
	 */
	public long getStartEventTimeoutMS() {
		return executor.getStartEventTimeoutMS();
	}

	/**
	 * @return
	 * @see alma.scheduling.array.executor.Executor#getEndedEventTimeoutMS()
	 */
	public long getEndedEventTimeoutMS() {
		return executor.getEndedEventTimeoutMS();
	}

	/**
	 * @return
	 * @see alma.scheduling.array.executor.Executor#getAsdmArchiveTimeoutMS()
	 */
	public long getAsdmArchiveTimeoutMS() {
		return executor.getAsdmArchiveTimeoutMS();
	}

	/**
	 * Tidy up in preparation for the array being destroyed.
	 */
	public void tidyUp() {
		if (getSchedBlock() != null) {
			if (state instanceof RunningExecutionState) {
				logger.info(String.format(
						"Tidy-up, setting SchedBlock %s to Failed state",
						getSchedBlock().getUid()));
				this.setState(new FailedExecutionState(this));
			} else if (state instanceof ManualRunningExecutionState) {
				logger.info(String.format(
						"Tidy-up, setting SchedBlock %s to Complete state",
						getSchedBlock().getUid()));
				this.setState(new ManualCompleteExecutionState(this));
			}
		}
	}

	public String getStateName() {
		try {
			return state.toString();
		} catch (NullPointerException e) {
			return "";
		}
	}

	// very inefficient, improve if it is necessary
	private TreeMap<SubScanSequenceEndedEvent, ArrayList<SubScanProcessedEvent>> processScanEvents() {
		final TreeMap<SubScanSequenceEndedEvent, ArrayList<SubScanProcessedEvent>> retVal = new TreeMap<SubScanSequenceEndedEvent, ArrayList<SubScanProcessedEvent>>(
				new SubScanSequenceEndedEventComparator());
		for (SubScanProcessedEvent sspEv : SSPSet) {
			SubScanSequenceEndedEvent ssseEv = null;
			for (SubScanSequenceEndedEvent x : SSSSet) {
				if (sspEv.processedScanNum == x.scanNumber) {
					ssseEv = x;
					break;
				}
			}
			if (ssseEv == null) {
				String msg = "Accounting of Sensitivity will not be complete. "
						+ "Missing SubScanSequenceEndedEvent "
						+ "for execBlock: "
						+ sspEv.processedExecBlockId.entityId + " "
						+ "SubScanSequenceEndedEvent.processedScanNum="
						+ sspEv.processedScanNum + " ";
				logger.warning(msg);
				break;
			}

			if (!retVal.containsKey(ssseEv))
				retVal.put(ssseEv, new ArrayList<SubScanProcessedEvent>());
			ArrayList<SubScanProcessedEvent> subScans = retVal.get(ssseEv);
			subScans.add(sspEv);
		}

		// Remove unsuccessful subscans
		for (SubScanSequenceEndedEvent ssseEv : retVal.keySet()) {
			ArrayList<SubScanProcessedEvent> toRemove = new ArrayList<SubScanProcessedEvent>();
			for (SubScanProcessedEvent sspEv : retVal.get(ssseEv)) {
				boolean isSuccessful = false;
				for (int i : ssseEv.successfulSubscans) {
					if (sspEv.processedSubScanNum == i) {
						isSuccessful = true;
						break;
					}
				}
				if (!isSuccessful)
					toRemove.add(sspEv);
			}
			for (SubScanProcessedEvent r : toRemove)
				retVal.get(ssseEv).remove(r);
		}

		return retVal;
	}

	private class SubScanSequenceEndedEventComparator implements
			Comparator<SubScanSequenceEndedEvent> {

		@Override
		public int compare(SubScanSequenceEndedEvent o1,
				SubScanSequenceEndedEvent o2) {
			return (o1.scanNumber - o2.scanNumber);
		}
	}

	private class SubScanProcessedEventComparator implements
			Comparator<SubScanProcessedEvent> {

		@Override
		public int compare(SubScanProcessedEvent o1, SubScanProcessedEvent o2) {
			int retVal = (o1.processedScanNum - o2.processedScanNum) * 1000;
			retVal += o1.processedSubScanNum - o2.processedSubScanNum;
			return retVal;
		}
	}

	// /// For testing purposes only

	TreeSet<SubScanProcessedEvent> getSSPSet() {
		return SSPSet;
	}

	TreeSet<SubScanSequenceEndedEvent> getSSSSet() {
		return SSSSet;
	}

}
