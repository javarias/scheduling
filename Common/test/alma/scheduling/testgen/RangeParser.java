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

package alma.scheduling.testgen;

import java.util.ArrayList;
import java.util.Collection;

import alma.scheduling.SchedulingException;

/**
 *
 * @author dclarke
 * $Id: RangeParser.java,v 1.1 2011/07/29 15:52:46 dclarke Exp $
 */
public class RangeParser {
	
	/*
	 * ============================
	 * Parsing range specifications
	 * ============================
	 */
	private final static String NumMinStepMaxSeparator = ": *";
	private final static String ListSeparator = ", *";

	public Collection<Integer> expandedInts(String rangeSpec)
			throws SchedulingException {
		final Collection<Integer> result = new ArrayList<Integer>();
		final String[] parts = rangeSpec.split(ListSeparator);
		
		try {
			for (final String part : parts) {
				final String[] subParts = part.split(NumMinStepMaxSeparator);
				int min;
				int max;
				int step = 1;
				if (subParts.length == 1) {
					// single value
					min = Integer.parseInt(subParts[0]);
					max = min;
				} else if (subParts.length == 2) {
						// min:max
						min = Integer.parseInt(subParts[0]);
						max = Integer.parseInt(subParts[1]);
				} else if (subParts.length == 3) {
					// min:step:max
					min  = Integer.parseInt(subParts[0]);
					max  = Integer.parseInt(subParts[1]);
					step = Integer.parseInt(subParts[2]);
				} else {
					throw new SchedulingException (
							String.format("Expecting either a single value, min:max or min:max:step, not '%s'",
									part));
				}

				if (min <= max) {
					if (step <= 0) {
						throw new SchedulingException (
								String.format("Invalid range specification '%s' - will never hit the end value",
										part));
					}
					for (int i = min; i <= max; i += step) {
						result.add(i);
					}
				} else {
					if (step >= 0) {
						throw new SchedulingException (
								String.format("Invalid range specification '%s' - will never hit the end value",
										part));
					}
					for (int i = min; i >= max; i += step) {
						result.add(i);
					}
				}
			}
		} catch (NumberFormatException any) {
			throw new SchedulingException(
					String.format("Error parsing range specification %s",
							rangeSpec),
							any);
		}

		if (result.isEmpty()) {
			throw new SchedulingException(
					String.format("Empty range specification %s",
							rangeSpec));
		}

		return result;
	}

	public Collection<Double> expandedDoubles(String rangeSpec)
			throws SchedulingException {
		final Collection<Double> result = new ArrayList<Double>();
		final String[] parts = rangeSpec.split(ListSeparator);
		
		try {
			for (final String part : parts) {
				final String[] subParts = part.split(NumMinStepMaxSeparator);
				double min;
				double max;
				double step = 1;
				if (subParts.length == 1) {
					// single value
					min = Double.parseDouble(subParts[0]);
					max = min;
				} else if (subParts.length == 2) {
						// min:max
						min = Double.parseDouble(subParts[0]);
						max = Double.parseDouble(subParts[1]);
				} else if (subParts.length == 3) {
					// min:step:max
					min  = Double.parseDouble(subParts[0]);
					max  = Double.parseDouble(subParts[1]);
					step = Double.parseDouble(subParts[2]);
				} else {
					throw new SchedulingException (
							String.format("Expecting either a single value, min:max or min:max:step, not '%s'",
									part));
				}

				if (min <= max) {
					if (step <= 0) {
						throw new SchedulingException (
								String.format("Invalid range specification '%s' - will never hit the end value",
										part));
					}
					for (double i = min; i <= max; i += step) {
						result.add(i);
					}
				} else {
					if (step >= 0) {
						throw new SchedulingException (
								String.format("Invalid range specification '%s' - will never hit the end value",
										part));
					}
					for (double i = min; i >= max; i += step) {
						result.add(i);
					}
				}
			}
		} catch (NumberFormatException any) {
			throw new SchedulingException(
					String.format("Error parsing range specification %s",
							rangeSpec),
							any);
		}

		if (result.isEmpty()) {
			throw new SchedulingException(
					String.format("Empty range specification %s",
							rangeSpec));
		}

		return result;
	}
	
	public Collection<Character> expandedChars(String rangeSpec)
			throws SchedulingException {
		final Collection<Character> result = new ArrayList<Character>();

		final char[] up = rangeSpec.toUpperCase().toCharArray();
		for (char c : up) {
			result.add(c);
		}

		if (result.isEmpty()) {
			throw new SchedulingException(
					String.format("Empty range specification %s",
							rangeSpec));
		}

		return result;
	}
	/*
	 * End of parsing range specifications
	 * -----------------------------------
	 */

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final RangeParser rp = new RangeParser();
//		title("Parsing integer ranges with colon form");
//		testParseInt(rp, "1:5");
//		testParseInt(rp, "5:1");
//		testParseInt(rp, "1:5:3");
//		testParseInt(rp, "5:1:3");
//		testParseInt(rp, "1:b");
//		testParseInt(rp, "a:2");
//		testParseInt(rp, "a:b");
//		testParseInt(rp, "1:2:c");
//		testParseInt(rp, "1:b:3");
//		testParseInt(rp, "a:2:3");
//		testParseInt(rp, "1:b:c");
//		testParseInt(rp, "a:2:c");
//		testParseInt(rp, "a:b:3");
//		testParseInt(rp, "a:b:c");
//		testParseInt(rp, "1:3:5:7");
//		
//		title("Parsing integer ranges with comma form");
//		testParseInt(rp, "1,3");
//		testParseInt(rp, "1,4,3");
//		testParseInt(rp, "a,1");
//		testParseInt(rp, "1,b");
//		
//		title("Parsing integer ranges with singleton form");
//		testParseInt(rp, "1");
//		testParseInt(rp, "a");
//		
//		title("Parsing integer ranges with combined form");
//		testParseInt(rp, "1, 2, 7:10");
//		testParseInt(rp, "1, 2, 10:7:-2");
//		testParseInt(rp, "1:2,a:4");
//		
//		title("Parsing double ranges with colon form");
//		testParseDouble(rp, "1.0:5");
//		testParseDouble(rp, "5:1.0");
//		testParseDouble(rp, "1.0:5:1.5");
//		testParseDouble(rp, "5:1:3.2");
//		testParseDouble(rp, "1:b");
//		testParseDouble(rp, "a:2");
//		testParseDouble(rp, "a:b");
//		testParseDouble(rp, "1:2:c");
//		testParseDouble(rp, "1:b:3");
//		testParseDouble(rp, "a:2:3");
//		testParseDouble(rp, "1:b:c");
//		testParseDouble(rp, "a:2:c");
//		testParseDouble(rp, "a:b:3");
//		testParseDouble(rp, "a:b:c");
//		testParseDouble(rp, "1:3:5:7");
//		
//		title("Parsing double ranges with comma form");
//		testParseDouble(rp, "1.6,3.141593");
//		testParseDouble(rp, "1,4.2,3.3");
//		testParseDouble(rp, "a,1.9");
//		testParseDouble(rp, "1.3e02,b");
//		
//		title("Parsing double ranges with singleton form");
//		testParseDouble(rp, "1.3e02");
//		testParseDouble(rp, "1.3q02");
//		
//		title("Parsing double ranges with combined form");
//		testParseDouble(rp, "1.0, 2.0 , 7.3:10.1");
//		testParseDouble(rp, "1, 2, 15:7:-2.5");
//		testParseDouble(rp, "1:2,a:4");
		
		title("Parsing character ranges");
		testParseChars(rp, "a");
		testParseChars(rp, "abc");
		testParseChars(rp, "a:b:c,d-h,x,y,z");
	}

	private static void title(String string) {
		System.out.println(string);
		for (int i = 0; i < string.length(); i++) {
			System.out.print('~');
		}
		System.out.println();
		
	}

	@SuppressWarnings("unused")
	private static void testParseInt(RangeParser rp, String rangeSpec) {
		try {
			final Collection<Integer> r = rp.expandedInts(rangeSpec);
			System.out.print(rangeSpec);
			String sep = " -> ";
			for (final int i : r) {
				System.out.print(sep);
				System.out.print(i);
				sep = ", ";
			}
			System.out.println();
		} catch (SchedulingException e) {
			System.out.println(rangeSpec);
			e.printStackTrace(System.out);
		}
	}

	@SuppressWarnings("unused")
	private static void testParseDouble(RangeParser rp, String rangeSpec) {
		try {
			final Collection<Double> r = rp.expandedDoubles(rangeSpec);
			System.out.print(rangeSpec);
			String sep = " -> ";
			for (final double i : r) {
				System.out.print(sep);
				System.out.print(i);
				sep = ", ";
			}
			System.out.println();
		} catch (SchedulingException e) {
			System.out.println(rangeSpec);
			e.printStackTrace(System.out);
		}
	}

	private static void testParseChars(RangeParser rp, String rangeSpec) {
		try {
			final Collection<Character> r = rp.expandedChars(rangeSpec);
			System.out.print(rangeSpec);
			String sep = " -> ";
			for (final char i : r) {
				System.out.print(sep);
				System.out.print(i);
				sep = ", ";
			}
			System.out.println();
		} catch (SchedulingException e) {
			System.out.println(rangeSpec);
			e.printStackTrace(System.out);
		}
	}

}
