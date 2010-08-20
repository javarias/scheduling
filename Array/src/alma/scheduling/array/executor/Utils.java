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
package alma.scheduling.array.executor;

/**
 * @author rhiriart
 *
 */
public class Utils {

    static private int partIdCount = 0;
    
    static private char[] digit = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
    
    public static String genPartId() {
        char[] s = new char [9];
        s[0] = 'X';
        for (int i = 1; i < s.length; ++i)
            s[i] = '0';
        int n = ++partIdCount;
        for (int i = s.length -1; i > 1; --i) {
            s[i] = digit[n % 16];
            n /= 16;
            if (n == 0)
                break;
        }
        return new String(s);
    }
}
