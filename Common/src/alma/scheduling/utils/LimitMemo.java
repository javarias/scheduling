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

package alma.scheduling.utils;

/**
 * Remembers the best (or worst, or something) value associated with
 * a selection of Categories. The categories to use are determined by
 * a given Enum - there's one Category per element in the Enum. A best
 * is kept independently for each Category.
 *  
 * @author dclarke
 */
public interface LimitMemo<Category extends Enum<Category>, Value> {
	public void addValue(Category c, Value v);
	public Value getValue(Category c);
}
