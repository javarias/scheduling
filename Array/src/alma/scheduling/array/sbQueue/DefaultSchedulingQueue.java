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

import alma.scheduling.ArrayGUIOperation;
import alma.scheduling.ArrayOperations;
import alma.scheduling.QueueOperation;
import alma.scheduling.array.guis.ArrayGUINotification;

/**
 * Subclass to allow us to insert scheduling specific behaviour to an
 * ObservableReorderingBlockingQueue<E>.
 * 
 * @author dclarke
 * $Id: DefaultSchedulingQueue.java,v 1.3 2011/03/18 00:20:23 dclarke Exp $
 */
public class DefaultSchedulingQueue<E> extends
		ObservableReorderingBlockingQueue<E> {
    
    public DefaultSchedulingQueue(ReorderingBlockingQueue<E> queue) {
        super(queue);
    }

	@Override
	public E take() throws InterruptedException {
		if (isEmpty()) {
    		final QueueNotification qn = new QueueNotification(
    				QueueOperation.WAITING, null);
    		setChanged();
    		notifyObservers(qn);
		}
		return super.take();
	}
}
