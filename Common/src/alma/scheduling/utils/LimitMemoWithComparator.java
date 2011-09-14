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

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of LimitMemo which defines the best value as that
 * with the greatest value according to a supplied Comparator<Value>.
 *  
 * @author dclarke
 * $Id: LimitMemoWithComparator.java,v 1.1 2011/09/14 22:14:19 dclarke Exp $
 */
public class LimitMemoWithComparator<E extends Enum<E>, Value>
	implements LimitMemo <E, Value> {
	
	/*
	 * ================================================================
	 * Implementation
	 * ================================================================
	 */
	private Map<E, Value> map;
	private Comparator<Value> comp;
	
	public LimitMemoWithComparator(Comparator<Value> comp) {
		this.map = new HashMap<E, Value>();
		this.comp = comp;
	}
	
	public void addValue(E e, Value d) {
		if (map.containsKey(e)) {
			final Value old = map.get(e);
			if (comp.compare(old, d) < 0) {
				map.put(e, d);
			}
		} else {
			// new entry, just store it
			map.put(e, d);
		}
	}
	
	public Value getValue(E e) {
		return map.get(e);
	}
	/* End of Implementation
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Test/demonstration
	 * ================================================================
	 */
	public static void main(String args[]) {
		final LimitMemoWithComparator<Archive, Date> memo1;
		final LimitMemoWithComparator<Archive, Date> memo2;
		
		final Comparator<Date> comp1 = new Comparator<Date>() {

			@Override
			public int compare(Date o1, Date o2) {
				int result = 0;
				if (o1.before(o2)) {
					result = -1;
				} else if (o1.after(o2)) {
					result = 1;
				}
				return result;
			}
		};
		final Comparator<Date> comp2 = new Comparator<Date>() {

			@Override
			public int compare(Date o1, Date o2) {
				return comp1.compare(o2, o1);
			}
		};

		memo1 = new LimitMemoWithComparator<Archive, Date>(comp1);
		memo2 = new LimitMemoWithComparator<Archive, Date>(comp2);
		
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
				memo1.addValue(a, now);
				memo2.addValue(a, now);
			}
		}

		for (final Archive a : Archive.values()) {
			System.out.format("Memo1: %s -> %s%n",
					a, memo1.getValue(a));
			System.out.format("Memo2: %s -> %s%n",
					a, memo2.getValue(a));
		}
	}
	/* End of Test/demonstration
	 * ============================================================= */

}
