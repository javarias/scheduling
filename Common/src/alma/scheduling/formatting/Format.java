/*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2010
 * (c) Associated Universities Inc., 2010
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 * 
 * File FormattingUtils.java
 */
package alma.scheduling.formatting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;


/**
 * Utility class to do various common formatting things.
 * 
 * @author dclarke
 * <br>
 * $Id: Format.java,v 1.1 2011/09/29 20:57:20 dclarke Exp $
 */
public final class Format {

	/*
	 * ================================================================
	 * Construction (actually prevention of this)
	 * ================================================================
	 */
    /**
     * Don't let anyone instantiate this class.
     */
    private Format() {}
	/* End Construction (actually prevention of this)
	 * ============================================================= */

    
    
    /*
	 * ================================================================
	 * Collections
	 * ================================================================
	 */
	public static <E> String format(Collection<E> collection,
			String open, String separator, String close) {
		final StringBuffer sb = new StringBuffer();
		String sep = "";
		
		sb.append(open);
		for (final E o : collection) {
			sb.append(sep);
			sb.append(o.toString());
			sep = separator;
		}
		sb.append(close);
		return sb.toString();
	}

	public static <E> String format(Collection<E> collection) {
		return format(collection, "", ", ", "");
	}
	/* End Collections
	 * ============================================================= */

    
    
    /*
	 * ================================================================
	 * Arrays
	 * ================================================================
	 */
	public static String formatArray(Object[] array, int perLine,
			String indent, String open, String separator, String close) {
		final StringBuffer sb = new StringBuffer();
		String sep = "";
		String pad = spaces(open.length());
		int onLine = 0;
		
		sb.append(indent);
		sb.append(open);
		for (final Object o : array) {
			sb.append(sep);
			if (onLine == perLine) {
				sb.append('\n');
				sb.append(indent);
				sb.append(pad);
				onLine = 0;
			}
			sb.append(o);
			onLine ++;
			sep = separator;
		}
		sb.append(close);
		return sb.toString();
	}

	public static String formatArray(Object[] array, int perLine) {
		return formatArray(array, perLine, "   ", "", ", ", "");
	}
	
	public static String formatArray(Object[] array,
			String open, String separator, String close) {
		final StringBuffer sb = new StringBuffer();
		String sep = "";
		
		sb.append(open);
		for (final Object o : array) {
			sb.append(sep);
			sb.append(o);
			sep = separator;
		}
		sb.append(close);
		return sb.toString();
	}

	public static String formatArray(Object[] array) {
		return formatArray(array, "", ", ", "");
	}
	
	public static String formatArray(long[] array,
			String open, String separator, String close) {
		final StringBuffer sb = new StringBuffer();
		String sep = "";
		
		sb.append(open);
		for (final long o : array) {
			sb.append(sep);
			sb.append(o);
			sep = separator;
		}
		sb.append(close);
		return sb.toString();
	}

	public static String formatArray(long[] array) {
		return formatArray(array, "", ", ", "");
	}
	
	public static String formatArray(int[] array,
			String open, String separator, String close) {
		final StringBuffer sb = new StringBuffer();
		String sep = "";
		
		sb.append(open);
		for (final int o : array) {
			sb.append(sep);
			sb.append(o);
			sep = separator;
		}
		sb.append(close);
		return sb.toString();
	}

	public static String formatArray(int[] array) {
		return formatArray(array, "", ", ", "");
	}
	
	public static String formatArray(double[] array,
			String open, String separator, String close) {
		final StringBuffer sb = new StringBuffer();
		String sep = "";
		
		sb.append(open);
		for (final double o : array) {
			sb.append(sep);
			sb.append(o);
			sep = separator;
		}
		sb.append(close);
		return sb.toString();
	}

	public static String formatArray(double[] array) {
		return formatArray(array, "", ", ", "");
	}
	
	public static String formatArray(float[] array,
			String open, String separator, String close) {
		final StringBuffer sb = new StringBuffer();
		String sep = "";
		
		sb.append(open);
		for (final float o : array) {
			sb.append(sep);
			sb.append(o);
			sep = separator;
		}
		sb.append(close);
		return sb.toString();
	}

	public static String formatArray(float[] array) {
		return formatArray(array, "", ", ", "");
	}
	
	public static String formatArray(char[] array,
			String open, String separator, String close) {
		final StringBuffer sb = new StringBuffer();
		String sep = "";
		
		sb.append(open);
		for (final char o : array) {
			sb.append(sep);
			sb.append(o);
			sep = separator;
		}
		sb.append(close);
		return sb.toString();
	}

	public static String formatArray(char[] array) {
		return formatArray(array, "", ", ", "");
	}
	
	public static String formatArray(boolean[] array,
			String open, String separator, String close) {
		final StringBuffer sb = new StringBuffer();
		String sep = "";
		
		sb.append(open);
		for (final boolean o : array) {
			sb.append(sep);
			sb.append(o);
			sep = separator;
		}
		sb.append(close);
		return sb.toString();
	}

	public static String formatArray(boolean[] array) {
		return formatArray(array, "", ", ", "");
	}
	/* End Arrays
	 * ============================================================= */

    
    
    /*
	 * ================================================================
	 * RA & Dec, HA
	 * ================================================================
	 */
	public static String formatRA(double degrees) {
		// Map degrees to hours, remaining in [0.0, 24.0)
		double remaining = degrees * 24.0/360.0;
		
		// Extract the whole hours, hh  in [0, 23]
		final long hh = Math.round(Math.floor(remaining)); 
		// Subtract the hours, remaining in [0.0, 60.0)
		remaining = (remaining - hh) * 60.0;
		
		// Extract the whole minutes, mm  in [0, 59]
		final long mm = Math.round(Math.floor(remaining)); 
		// Subtract the minutes, remaining in [0.0, 60.0)
		remaining = (remaining - mm) * 60.0;

		return String.format("%02d:%02d:%06.3f",
				hh, mm, remaining);
	}
	
	public static String formatDec(double degrees) {
		
		int sign = 1;
		if (degrees < 0.0) {
			sign = -1;
			degrees = Math.abs(degrees);
		}
		
		// Remaining in [-90.0, 90.0]
		double remaining = degrees;
		
		// Extract the whole degrees, dd  in [-90, 90]
		final long dd = Math.round(Math.floor(remaining)); 
		// Subtract the hours, remaining in [0.0, 60.0)
		remaining = (remaining - dd) * 60.0;
		
		// Extract the whole minutes, mm  in [0, 59]
		final long mm = Math.round(Math.floor(remaining)); 
		// Subtract the minutes, remaining in [0.0, 60.0)
		remaining = (remaining - mm) * 60.0;

		return String.format("% 03d:%02d:%06.3f",
				sign * dd, mm, remaining);
	}
	
	public static String formatHA(double hours) {
		double remaining = hours;
		
		// Extract the whole hours, hh  in [0, 23]
		final long hh = Math.round(Math.floor(remaining)); 
		// Subtract the hours, remaining in [0.0, 60.0)
		remaining = (remaining - hh) * 60.0;
		
		// Extract the whole minutes, mm  in [0, 59]
		final long mm = Math.round(remaining); 

		return String.format("% 03d:%02d",
				hh, mm, remaining);
	}
	
	/* End RA & Dec
	 * ============================================================= */

    
    
    /*
	 * ================================================================
	 * Main
	 * ================================================================
	 */
	private static String spaces(int n) {
		StringBuffer result = new StringBuffer();
		
		for (int i = 0; i < n; i++) {
			result.append(' ');
		}
		
		return result.toString();
	}
	
	private static String centre(String s, int width) {
		final int spaces = width - s.length();
		if (spaces <= 0) {
			return s;
		}
		final int before = spaces / 2;
		final int after  = spaces - before;
		return spaces(before) + '\'' + s + '\'' + spaces(after);
	}
	
	@SuppressWarnings("unused")
	private static void testRAandDec() {
		final int cw1 = 12;
		final int cw2 = 20;
		
		System.out.print(centre("Degrees", cw1));
		System.out.print(centre("RA", cw2));
		System.out.println(centre("Dec", cw2));
		
		final double steps[] = {0.50001, 1.7, 0.49999, 0.8, 1.0};
		int index = 0;
		for (double d = -90.000; d <= 360.0; d += steps[index]) {
			System.out.print(centre(String.format("%8.5f", d), cw1));
			if (0 <= d) {
				System.out.print(centre(formatRA(d), cw2));
			} else {
				System.out.print(centre("--", cw2));
			}
			if (-90 <= d && d < 90.0) {
				System.out.print(centre(formatDec(d), cw2));
			} else {
				System.out.print(centre("--", cw2));
			}
			System.out.println();
			index++;
			if (index >= steps.length) {
				index = 0;
			}
		}
	}
	
	private static <T, C extends Collection<T>> C filledCollection(Class<C> c,
			T[] some) {
		C result = null;
		
		try {
			result = c.newInstance();
			for (final T t : some) {
				result.add(t);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	@SuppressWarnings("unused")
	private static void testCollections() {
		final Integer[] someints = {1, 2, 3, 4, 5, 6};
		final Integer[] noints = {};
		final String[] somestrings = {"alpha", "beta", "gamma",
				                      "delta", "epsilon", "zeta"};
		final String[] nostrings = {};
		
		@SuppressWarnings("unchecked")
		final Set<Integer>   someIntS = filledCollection(HashSet.class, someints);
		@SuppressWarnings("unchecked")
		final Set<Integer>   noneIntS = filledCollection(TreeSet.class, noints);
		@SuppressWarnings("unchecked")
		final List<Integer>  someIntL = filledCollection(Vector.class, someints);
		@SuppressWarnings("unchecked")
		final List<Integer>  noneIntL = filledCollection(ArrayList.class, noints);
		@SuppressWarnings("unchecked")
		final Queue<Integer> someIntQ = filledCollection(PriorityQueue.class, someints);
		@SuppressWarnings("unchecked")
		final Queue<Integer> noneIntQ = filledCollection(LinkedList.class, noints);
		
		@SuppressWarnings("unchecked")
		final Set<String>   someStrS = filledCollection(HashSet.class, somestrings);
		@SuppressWarnings("unchecked")
		final Set<String>   noneStrS = filledCollection(TreeSet.class, nostrings);
		@SuppressWarnings("unchecked")
		final List<String>  someStrL = filledCollection(Vector.class, somestrings);
		@SuppressWarnings("unchecked")
		final List<String>  noneStrL = filledCollection(ArrayList.class, nostrings);
		@SuppressWarnings("unchecked")
		final Queue<String> someStrQ = filledCollection(PriorityQueue.class, somestrings);
		@SuppressWarnings("unchecked")
		final Queue<String> noneStrQ = filledCollection(LinkedList.class, nostrings);
		
		System.out.println("Basic format");
		System.out.print("someIntS: "); System.out.println(format(someIntS));
		System.out.print("noneIntS: "); System.out.println(format(noneIntS));
		System.out.print("someIntL: "); System.out.println(format(someIntL));
		System.out.print("noneIntL: "); System.out.println(format(noneIntL));
		System.out.print("someIntQ: "); System.out.println(format(someIntQ));
		System.out.print("noneIntQ: "); System.out.println(format(noneIntQ));
		System.out.print("someStrS: "); System.out.println(format(someStrS));
		System.out.print("noneStrS: "); System.out.println(format(noneStrS));
		System.out.print("someStrL: "); System.out.println(format(someStrL));
		System.out.print("noneStrL: "); System.out.println(format(noneStrL));
		System.out.print("someStrQ: "); System.out.println(format(someStrQ));
		System.out.print("noneStrQ: "); System.out.println(format(noneStrQ));
		
		final String open  = "->";
		final String sep   = "<>";
		final String close = "<-";
		System.out.format("Fancy format (open: '%s', sep: '%s', close: '%s'%n",
				open, sep, close);
		System.out.print("someIntS: "); System.out.println(format(someIntS, open, sep, close));
		System.out.print("noneIntS: "); System.out.println(format(noneIntS, open, sep, close));
		System.out.print("someIntL: "); System.out.println(format(someIntL, open, sep, close));
		System.out.print("noneIntL: "); System.out.println(format(noneIntL, open, sep, close));
		System.out.print("someIntQ: "); System.out.println(format(someIntQ, open, sep, close));
		System.out.print("noneIntQ: "); System.out.println(format(noneIntQ, open, sep, close));
		System.out.print("someStrS: "); System.out.println(format(someStrS, open, sep, close));
		System.out.print("noneStrS: "); System.out.println(format(noneStrS, open, sep, close));
		System.out.print("someStrL: "); System.out.println(format(someStrL, open, sep, close));
		System.out.print("noneStrL: "); System.out.println(format(noneStrL, open, sep, close));
		System.out.print("someStrQ: "); System.out.println(format(someStrQ, open, sep, close));
		System.out.print("noneStrQ: "); System.out.println(format(noneStrQ, open, sep, close));
	}

	@SuppressWarnings("unused")
	private static void testArrays() {
		final int[] someints = {1, 2, 3, 4, 5, 6};
		final int[] noints = {};
		final String[] somestrings = {"alpha", "beta", "gamma",
				                      "delta", "epsilon", "zeta"};
		final String[] nostrings = {};
		
		System.out.println("Basic format");
		System.out.print("someints:    ");
		System.out.println(formatArray(someints));
		System.out.print("noints:      ");
		System.out.println(formatArray(noints));
		System.out.print("somestrings: ");
		System.out.println(formatArray(somestrings));
		System.out.print("nostrings:   ");
		System.out.println(formatArray(nostrings));
		
		final String open  = "->";
		final String sep   = "<>";
		final String close = "<-";
		System.out.format("Fancy format (open: '%s', sep: '%s', close: '%s'%n",
				open, sep, close);
		System.out.print("someints:    ");
		System.out.println(formatArray(someints, open, sep, close));
		System.out.print("noints:      ");
		System.out.println(formatArray(noints, open, sep, close));
		System.out.print("somestrings: ");
		System.out.println(formatArray(somestrings, open, sep, close));
		System.out.print("nostrings:   ");
		System.out.println(formatArray(nostrings, open, sep, close));
	}

	private static void testMultiLineArrays() {
		final String[] somestrings = {"alpha", "beta", "gamma",
				                      "delta", "epsilon", "zeta"};
		final String[] nostrings = {};
		
		System.out.println("Basic multi-line format");
		System.out.println("nostrings, 1:   ");
		System.out.println(formatArray(nostrings, 1));
		System.out.println("somestrings,  1: ");
		System.out.println(formatArray(somestrings, 1));
		System.out.println("somestrings,  3: ");
		System.out.println(formatArray(somestrings, 3));
		System.out.println("somestrings,  6: ");
		System.out.println(formatArray(somestrings, 6));
		System.out.println("somestrings,  7: ");
		System.out.println(formatArray(somestrings, 7));
		
		final String indent = "\t";
		final String open   = "{";
		final String sep    = ", ";
		final String close  = "}";
		System.out.format("Fancy format multi-line (indent: '%s', open: '%s', sep: '%s', close: '%s'%n",
				indent, open, sep, close);
		System.out.println("nostrings, 1:   ");
		System.out.println(formatArray(nostrings, 1, indent, open, sep, close));
		System.out.println("somestrings,  1: ");
		System.out.println(formatArray(somestrings, 1, indent, open, sep, close));
		System.out.println("somestrings,  3: ");
		System.out.println(formatArray(somestrings, 3, indent, open, sep, close));
		System.out.println("somestrings,  6: ");
		System.out.println(formatArray(somestrings, 6, indent, open, sep, close));
		System.out.println("somestrings,  7: ");
		System.out.println(formatArray(somestrings, 7, indent, open, sep, close));
	}
	
	@SuppressWarnings("unused")
	private static void testHAs() {
		for (double ha = 0.25; ha < 24.0; ha += .5) {
			System.out.format("%7.3f -> %s%n", ha, formatHA(ha));
		}
		final Random gen = new Random();
		for (double ha = 0.0; ha < 24.0; ha += gen.nextDouble()) {
			System.out.format("%7.3f -> %s%n", ha, formatHA(ha));
		}
	}
	
	public static void main(String[] args) {
		testMultiLineArrays();
	}
	/* End Main
	 * ============================================================= */
}
