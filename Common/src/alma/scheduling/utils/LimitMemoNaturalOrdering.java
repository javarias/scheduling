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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of LimitMemo which defines the best value as that
 * with the greatest value according to the Value type's natural
 * ordering (as defined by Value implementing Comparable<Value>).
 *  
 * @author dclarke
 */
public class LimitMemoNaturalOrdering<Category extends Enum<Category>,
                                      Value extends Comparable<Value>>
	implements LimitMemo <Category, Value> {
	
	/*
	 * ================================================================
	 * Implementation
	 * ================================================================
	 */
	private Map<Category, Value> map;
	
	public LimitMemoNaturalOrdering() {
		this.map = new HashMap<Category, Value>();
	}
	
	public void addValue(Category c, Value v) {
		if (map.containsKey(c)) {
			final Value old = map.get(c);
			if (v.compareTo(old) > 0) {
				map.put(c, v);
			}
		} else {
			// new entry, just store it
			map.put(c, v);
		}
	}
	
	public Value getValue(Category c) {
		return map.get(c);
	}
	/* End of Implementation
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Test/demonstration
	 * ================================================================
	 */
	public static void main(String args[]) {
		final LimitMemo<Archive, Date> memo;
		
		memo = new LimitMemoNaturalOrdering<Archive, Date>();
		
		for (int i = 0; i < 3; i ++) {
			if (i != 0) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			for (final Archive a : Archive.values()) {
				final Date now = new Date();
				
				System.out.format("Adding timestamp %s for archive %s%n",
						now, a);
				memo.addValue(a, now);
			}
		}

		for (final Archive a : Archive.values()) {
			System.out.format("Memo: %s -> %s%n",
					a, memo.getValue(a));
		}
	}
	/* End of Test/demonstration
	 * ============================================================= */

}
