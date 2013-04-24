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

import java.util.logging.Logger;

import alma.scheduling.utils.LoggerFactory;

/**
 * The ReadyExecutionState represents a SchedBlock that is ready for execution.
 * When the startObservation() method is called, the execution will be triggered in
 * Control, and the execution will remain in this state until the
 * processExecBlockStartedEvent() method is called. At this point the state will
 * transition to the RunningExecutionState state.
 * 
 * @author Rafael Hiriart (rhiriart@nrao.edu)
 *
 */
public class ReadyExecutionState extends ExecutionState {

    private Logger logger = LoggerFactory.getLogger(getClass());
        
    public ReadyExecutionState(ExecutionContext context) {
        super(context);
    }

    @Override
    public void startObservation() {
        
        context.setState(new RunningExecutionState(context));
    }
/*       
	@Override
	protected boolean hasLifeCycleEquivalent() {
		return true;
	}

	@Override
	protected StatusTStateType lifeCycleEquivalent() {
		if (context.isCSV()) {
			return StatusTStateType.CSVREADY;
		}
		return StatusTStateType.READY;
	}
*/
}
