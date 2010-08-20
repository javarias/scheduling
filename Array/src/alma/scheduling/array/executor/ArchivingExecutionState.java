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

import alma.offline.ASDMArchivedEvent;

/**
 * @author rhiriart
 *
 */
public class ArchivingExecutionState extends ExecutionState {

    public ArchivingExecutionState(ExecutionContext context) {
        super(context);
    }
    
    @Override
    public void waitArchival() {
        ASDMArchivedEvent event = context.waitForASDMArchivedEvent(3600000);
        if (event == null) {
            context.setState(new FailedExecutionState(context));
        }
        context.setState(new CompleteExecutionState(context));
    }

    @Override
    public String toString() {
        return "ArchivingExecutionState";
    }
}
