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

import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.offline.ASDMArchivedEvent;

/**
 * @author rhiriart
 *
 */
public class ArchivingExecutionState extends ExecutionState {

	private boolean fullyExecuted;
	
    public ArchivingExecutionState(ExecutionContext context,
    		                       boolean fullyExecuted) {
        super(context);
        this.fullyExecuted = fullyExecuted;
    }
    
    @Override
    public void waitArchival() {
        ASDMArchivedEvent event = context.waitForASDMArchivedEvent(
        		context.getAsdmArchiveTimeoutMS());
        if (event == null) {
        	// Archiving interrupted
            context.setState(new FailedArchivingExecutionState(context, true));
        } else if (event.status.equals("complete")) {
        	// Archived successfully.
        	// TODO: change that literal to a member of alma.Control.Completion.
        	//       in line with the corresponding change in Data Capture
            context.setState(new CompleteExecutionState(context));
        } else {
        	// Archiving failed
            context.setState(new FailedArchivingExecutionState(context, false));
        }
    }

    public StatusTStateType getFinalState() {
    	return this.fullyExecuted?
    			StatusTStateType.FULLYOBSERVED:
    			StatusTStateType.PARTIALLYOBSERVED;
    }
}
