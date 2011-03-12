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



/**
 * The state into which we move if there is a problem with the
 * archiving of an execution's data (including the interruption
 * of the wait for the ASDMArchivedEvent.
 * 
 * @author dclarke
 * $Id: FailedArchivingExecutionState.java,v 1.1 2011/03/12 00:10:28 dclarke Exp $
 */
public class FailedArchivingExecutionState extends ExecutionState {

	private boolean interrupted;
	
    /**
     * @param context
     */
    public FailedArchivingExecutionState(ExecutionContext context,
    		                             boolean interrupted) {
        super(context);
        this.interrupted = interrupted;
    }
    
    public boolean wasInterrupted() {
    	return interrupted;
    }

}
