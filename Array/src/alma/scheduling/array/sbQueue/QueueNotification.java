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
package alma.scheduling.array.sbQueue;

import alma.scheduling.QueueOperation;

/**
 * @author rhiriart
 *
 */
public class QueueNotification {

    private QueueOperation op;
    private SchedBlockItem item;
    
    public QueueNotification(QueueOperation op, Object item) {
        this(op, (SchedBlockItem) item);
    }
    
    public QueueNotification(QueueOperation op, SchedBlockItem item) {
        this.op = op;
        this.item = item;
    }

    public QueueOperation getOperation() {
        return op;
    }
    
    public void setType(QueueOperation op) {
        this.op = op;
    }
    
    public SchedBlockItem getItem() {
        return item;
    }
    
    public void setItem(SchedBlockItem item) {
        this.item = item;
    }
}
