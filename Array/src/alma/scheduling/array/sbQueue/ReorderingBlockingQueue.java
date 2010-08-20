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

import java.util.concurrent.BlockingQueue;

/**
 * @author rhiriart
 *
 */
public interface ReorderingBlockingQueue<E> extends BlockingQueue<E> {

    /**
     * Moves a single instance of the specified element one step closer to
     * the head, if it is present.
     * 
     * @param o element to be moved up in the queue, if present
     * @return <tt>true</tt> if this queue changed as a result of the call
     */
    public boolean moveDown(Object o);
    
    /**
     * Moves a single instance of the specified element one step away from
     * the head, if it is present.
     * 
     * @param o element to be moved down in the queue, if present
     * @return <tt>true</tt> if this queue changed as a result of the call
     */
    public boolean moveUp(Object o);
}
