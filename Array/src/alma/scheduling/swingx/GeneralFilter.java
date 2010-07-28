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

package alma.scheduling.swingx;

import javax.swing.RowFilter;

/**
 *
 * @author dclarke
 * $Id: GeneralFilter.java,v 1.1 2010/07/28 21:28:39 dclarke Exp $
 */
public abstract class GeneralFilter extends RowFilter<Object, Object> {
    private int[] columns;


    /**
     * Throws an IllegalArgumentException if any of the values in
     * columns are < 0.
     */
    private static void checkIndices(int[] columns) {
        for (int i = columns.length - 1; i >= 0; i--) {
            if (columns[i] < 0) {
                throw new IllegalArgumentException("Index must be >= 0");
            }
        }
    }

    
    GeneralFilter(int[] columns) {
        checkIndices(columns);
        this.columns = columns;
    }

    public boolean include(Entry<? extends Object,? extends Object> value){
        int count = value.getValueCount();
        if (columns.length > 0) {
            for (int i = columns.length - 1; i >= 0; i--) {
                int index = columns[i];
                if (index < count) {
                    if (include(value, index)) {
                        return true;
                    }
                }
            }
        }
        else {
            while (--count >= 0) {
                if (include(value, count)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected abstract boolean include(
          Entry<? extends Object,? extends Object> value, int index);
}
