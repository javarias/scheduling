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
 *
 * @author dclarke
 * $Id: StringUtil.java,v 1.1 2011/08/11 22:48:35 dclarke Exp $
 */
public class StringUtil {
	
	public final static int MAX_STRING_LENGTH = 1000;
	
	public static String trimmed(String s, int maxLength) {
		try {
			if (s.length() > maxLength) {
				return s.substring(0, maxLength) + "...";
			} else {
				return s;
			}
		} catch (NullPointerException e) {
			return s;
		}
	}
	
	public static String trimmed(String s) {
		return trimmed(s, MAX_STRING_LENGTH);
	}

}
