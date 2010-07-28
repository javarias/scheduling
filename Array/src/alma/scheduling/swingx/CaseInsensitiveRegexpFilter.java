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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.RowFilter;


/**
 *
 * @author dclarke
 * $Id: CaseInsensitiveRegexpFilter.java,v 1.1 2010/07/28 21:28:39 dclarke Exp $
 */
public class CaseInsensitiveRegexpFilter extends GeneralFilter {
    private Matcher matcher;

    public CaseInsensitiveRegexpFilter(Pattern regex, int[] columns) {
        super(columns);
        if (regex == null) {
            throw new IllegalArgumentException("Pattern must be non-null");
        }
        matcher = regex.matcher("");
    }

    public CaseInsensitiveRegexpFilter(String regex, int[] columns) {
        this(Pattern.compile(regex, Pattern.CASE_INSENSITIVE), columns);
    }

    protected boolean include(
            Entry<? extends Object,? extends Object> value, int index) {
        matcher.reset(value.getStringValue(index));
        return matcher.find();
    }


    /**
     * Returns a <code>RowFilter</code> that uses a regular
     * expression to determine which entries to include.  Only entries
     * with at least one matching value are included.  For
     * example, the following creates a <code>RowFilter</code> that
     * includes entries with at least one value starting with
     * "a":
     * <pre>
     *   RowFilter.regexFilter("^a");
     * </pre>
     * <p>
     * The returned filter uses {@link java.util.regex.Matcher#find}
     * to test for inclusion.  To test for exact matches use the
     * characters '^' and '$' to match the beginning and end of the
     * string respectively.  For example, "^foo$" includes only rows whose
     * string is exactly "foo" and not, for example, "food".  See
     * {@link java.util.regex.Pattern} for a complete description of
     * the supported regular-expression constructs.
     *
     * @param regex the regular expression to filter on
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
    public static <M,I> RowFilter<M,I> regexFilter(String regex,
                                                       int... indices) {
        return (RowFilter<M,I>)new CaseInsensitiveRegexpFilter(Pattern.compile(regex, Pattern.CASE_INSENSITIVE),
                                               indices);
    }
}
