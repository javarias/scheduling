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
 * $Id: CallbackFilter.java,v 1.1 2010/07/28 21:28:39 dclarke Exp $
 */
public class CallbackFilter extends GeneralFilter {
    
	public interface Callee {
		public boolean include(
	            Entry<? extends Object,? extends Object> value, int index);
	}

	private Callee callee;
	
    public CallbackFilter(Callee callee, int[] columns) {
        super(columns);
        if (callee == null) {
        	throw new NullPointerException("callback object is null");
        }
        this.callee = callee;
    }

    protected boolean include(
            Entry<? extends Object,? extends Object> value, int index) {
        return callee.include(value, index);
    }


    /**
     * Returns a <code>CallbackFilter</code> that uses an external
     * object to determine which entries to include. Only entries
     * which are approved by that external Callee are included.
     *
     * @param callee the external object to consult
     * @param indices the indices of the values to check.  If not supplied all
     *               values are evaluated
     * @return a <code>RowFilter</code> implementing the specified criteria
     * @throws NullPointerException if <code>regex</code> is
     *         <code>null</code>
     * @throws IllegalArgumentException if any of the <code>indices</code>
     *         are &lt; 0
     * @throws PatternSyntaxException if <code>regex</code> is
     *         not a valid regular expression.
     * @see java.util.regex.Pattern
     */
    public static <M,I> RowFilter<M,I> callbackFilter(Callee callee,
                                                       int... indices) {
        return (RowFilter<M,I>)new CallbackFilter(callee, indices);
    }
}
