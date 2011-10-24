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
package alma.scheduling.array.executor;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import alma.acs.util.UTCUtility;

import alma.asdmIDLTypes.IDLEntityRef;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.array.sessions.SessionManager;
import alma.Control.ExecBlockEndedEvent;
import alma.Control.ExecBlockStartedEvent;

public class ManualRunningExecutionState extends ExecutionState {

	private boolean waitingForStart = false;
	private boolean waitingForEnd   = false;

	public ManualRunningExecutionState(ExecutionContext context) {
		super(context);
	}

	@Override
	public long observe() {
		long execTime = super.observe();

		final SessionManager sessions = context.getSessions();
		final SchedBlock sb = context.getSchedBlock();

		sessions.observeSB(sb);

		final IDLEntityRef sessionRef = sessions.getCurrentSession();
		final IDLEntityRef sbRef      = sessions.getCurrentSB();
		context.setSessionRef(sessionRef);
		context.setSchedBlockRef(sbRef);

		logger.info(String.format(
				"%s: just before context.getControlArray().observe(%s, null)",
				this.getClass().getSimpleName(),
				sbRef.entityId));
		try {
			context.getControlArray().observe(sbRef, sessionRef);
		}
		catch (org.omg.CORBA.TIMEOUT e) {
			//Do Nothing
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		// blocks waiting for the ExecBlockStartedEvent
		logger.info(String.format(
				"%s: waiting for ExecBlockStartedEvent",
				this.getClass().getSimpleName()));

		waitingForStart = true;
		ExecBlockStartedEvent sev = context.waitForExecBlockStartedEvent();
		waitingForStart = false;

		if (sev == null) {
			logger.info(String.format(
					"%s: No ExecBlockStartedEvent",
					this.getClass().getSimpleName()));
			context.setState(new ManualCompleteExecutionState(context));
			return execTime;
		}

		sessions.addExecution(sev.execId.entityId);

		waitingForEnd = true;
		ExecBlockEndedEvent eev = context.waitForExecBlockEndedEvent();
		waitingForEnd = false;

		execTime = (long) ((UTCUtility.utcOmgToJava(eev.endTime) - UTCUtility.utcOmgToJava(sev.startTime)) / 1000.0);
		context.setState(new ManualCompleteExecutionState(context));
		return execTime;
	}

	@Override
	public void stopObservation() {
		super.stopObservation();
		if (waitingForEnd) {
			try {
				context.getControlArray().stopSB();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (waitingForStart) {
			context.interruptWaitForExecBlockStartedEvent();
		}
	}

	@Override
	public void abortObservation() {
		this.stopObservation();
	}
}
