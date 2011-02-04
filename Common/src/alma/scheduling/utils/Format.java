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
package alma.scheduling.utils;


/**
 * Utility class to do various common formatting things.
 * 
 * @author dclarke
 * <br>
 * $Id: Format.java,v 1.2 2011/02/04 19:31:58 javarias Exp $
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
	 * Arrays
	 * ================================================================
	 */
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
	 * RA & Dec
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

		return String.format(" %02d:%02d:%06.3f",
				hh, mm, remaining);
	}
	
	public static String formatDec(double degrees) {
		
		String sign = "";
		if (degrees < 0.0) {
			sign = "-";
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

		return String.format("%s%02d:%02d:%06.3f",
				sign, dd, mm, remaining);
	}
	/* End RA & Dec
	 * ============================================================= */

    
    
    /*
	 * ================================================================
	 * Main
	 * ================================================================
	 */
	private static String spaces(int n) {
		return "                                      ".substring(0, n-1);
	}
	
	private static String centre(String s, int width) {
		final int spaces = width - s.length();
		if (spaces <= 0) {
			return s;
		}
		final int before = spaces / 2;
		final int after  = spaces - before;
		return spaces(before) + s + spaces(after);
	}
	
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
	
	public static void main(String[] args) {
		testArrays();
	}
	/* End Main
	 * ============================================================= */
}
