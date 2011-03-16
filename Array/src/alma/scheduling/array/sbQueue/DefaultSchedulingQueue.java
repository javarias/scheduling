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

import alma.scheduling.ArrayOperations;

/**
 * Subclass to allow us to insert scheduling specific behaviour to an
 * ObservableReorderingBlockingQueue<E>.
 * 
 * @author dclarke
 * $Id: DefaultSchedulingQueue.java,v 1.2 2011/03/16 00:03:31 dclarke Exp $
 */
public class DefaultSchedulingQueue<E> extends
		ObservableReorderingBlockingQueue<E> {
    
	private ArrayOperations array;
	
    private DefaultSchedulingQueue(ReorderingBlockingQueue<E> queue) {
        super(queue);
    }
	
    public DefaultSchedulingQueue(ReorderingBlockingQueue<E> queue, ArrayOperations array) {
        super(queue);
        this.array = array;
    }

	@Override
	public E take() throws InterruptedException {
		if (isEmpty()) {
//			getArray().selectNextSB();
		}
		return super.take();
	}
	
	private ArrayOperations getArray() {
		return array;
	}
}
