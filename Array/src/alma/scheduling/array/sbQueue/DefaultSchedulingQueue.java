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
 * Subclass to allow us to insert scheduling specific behaviour to an
 * ObservableReorderingBlockingQueue<E>.
 * 
 * @author dclarke
 * $Id: DefaultSchedulingQueue.java,v 1.4 2011/05/05 21:46:20 javarias Exp $
 */
public class DefaultSchedulingQueue<E> extends
		ObservableReorderingBlockingQueue<E> {
    
    public DefaultSchedulingQueue(ReorderingBlockingQueue<E> queue) {
        super(queue);
    }

	@Override
	public E take() throws InterruptedException {
		if (isEmpty()) {
			//Empty item to avoid crash in the Queue callback notifier
			final SchedBlockItem item = new SchedBlockItem("", System.currentTimeMillis());
    		final QueueNotification qn = new QueueNotification(
    				QueueOperation.WAITING, item);
    		setChanged();
    		notifyObservers(qn);
		}
		return super.take();
	}
}
