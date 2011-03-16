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

package alma.scheduling.array.sbSelection;

import java.util.Observable;
import java.util.Observer;

import alma.scheduling.ArrayOperations;

/**
 *
 * @author dclarke
 * $Id: Selector.java,v 1.2 2011/03/16 23:22:11 dclarke Exp $
 */
public interface Selector extends Observer {

	/**
	 * Tell this Selector to run on the given array and its queue
	 * @param array
	 * @param queue
	 */
	void configureArray(ArrayOperations array, Observable queue);
	
	/**
	 * Select the next SB to run and queue it if appropriate 
	 */
	void selectNextSB();
}
