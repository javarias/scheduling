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
package alma.scheduling.array.executor;

import alma.Control.ExecBlockEndedEvent;
import alma.Control.ExecBlockStartedEvent;
import alma.acs.util.UTCUtility;
import alma.asdmIDLTypes.IDLEntityRef;
import alma.scheduling.array.sessions.SessionManager;
import alma.scheduling.datamodel.obsproject.SchedBlock;

/**
 * @author rhiriart
 *
 */
public class RunningExecutionState extends ExecutionState {
    /**
     * @param context
     */
    public RunningExecutionState(ExecutionContext context) {
        super(context);
    }

    @Override
    public long observe() {
    	long execTime = super.observe();

    	final SessionManager sessions = context.getSessions();
    	SchedBlock sb = context.getSchedBlock();

    	sessions.observeSB(sb);

    	final IDLEntityRef sessionRef = sessions.getCurrentSession();
    	final IDLEntityRef sbRef      = sessions.getCurrentSB();
    	context.setSessionRef(sessionRef);
    	context.setSchedBlockRef(sbRef);

    	try {
    		context.getControlArray().observe(sbRef, sessionRef);
    	} catch (org.omg.CORBA.TIMEOUT e) {
    		//Do Nothing
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
        
    	// blocks waiting for the ExecBlockStartedEvent
    	ExecBlockStartedEvent sev = context.waitForExecBlockStartedEvent(
    			context.getStartEventTimeoutMS());

    	if (sev == null) {
    		logger.info(String.format(
    				"%s: No ExecBlockStartedEvent",
    				this.getClass().getSimpleName()));
    		context.setState(new FailedExecutionState(context));
    		return execTime;
    	}

    	sessions.addExecution(sev.execId.entityId);
    	ExecBlockEndedEvent eev = context.waitForExecBlockEndedEvent();

    	if (eev.status.value() == alma.Control.Completion.SUCCESS.value()) {
    		// Full success
    		execTime = (long) ((UTCUtility.utcOmgToJava(eev.endTime) - UTCUtility.utcOmgToJava(sev.startTime)) / 1000.0);
    		context.setState(new ArchivingExecutionState(context, true));
    	} else if (eev.status.value() != alma.Control.Completion.FAIL.value()) {
    		// Partial success
    		execTime = (long) ((UTCUtility.utcOmgToJava(eev.endTime) - UTCUtility.utcOmgToJava(sev.startTime)) / 1000.0);
    		context.setState(new ArchivingExecutionState(context, false));
    	} else {
    		// Fail
    		context.setState(new FailedExecutionState(context));
    	}

    	return execTime;
    }
    
    @Override
    public void stopObservation() {
	try {
	    context.getControlArray().stopSB();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
 /*   
	@Override
	protected boolean hasLifeCycleEquivalent() {
		return true;
	}

	@Override
	protected StatusTStateType lifeCycleEquivalent() {
		if (context.isCSV()) {
			return StatusTStateType.CSVRUNNING;
		}
		return StatusTStateType.RUNNING;
	}
*/
}
