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

// import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
// import alma.obsprep.bo.schedblock.SchedBlock;

/**
 * @author rhiriart
 *
 */
public class CompleteExecutionState extends ExecutionState {

    /**
     * @param context
     */
    public CompleteExecutionState(ExecutionContext context) {
        super(context);
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
		if (!context.isFullAuto()) {
			return StatusTStateType.SUSPENDED;
		}
		if (context.getSchedBlock().needsMoreExecutions()) {
			return StatusTStateType.READY;
		}
		return StatusTStateType.SUSPENDED;
	}*/
}
