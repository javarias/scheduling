/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 * File Equatorial.java
 */
package alma.Scheduling.Define;

/**
 * Equatorial coordinates -- right ascension, declination, and the equinox.
 * 
 * @version 1.30 May 10, 2004
 * @author Allen Farris
 */
public class Equatorial {
	
	static private final double cosE = Math.cos(Math.toRadians(23.4392911));
	static private final double sinE = Math.sin(Math.toRadians(23.4392911));
	
	static public Ecliptical J2000ToEcliptical(double ra, double dec) {
		double sinRa = Math.sin(ra);
		double cosRa = Math.cos(ra);
		double sinDec = Math.sin(dec);
		double cosDec = Math.cos(dec);
		double tanDec = sinDec / cosDec;
		double lamdaN = sinRa * cosE  + tanDec * sinE;
		double beta = sinDec * cosE - cosDec * sinE * sinRa;
		return new Ecliptical(Math.atan2(lamdaN,cosRa),Math.asin(beta),new DateTime(2000,1,1.0));
	}
	/**
	 * The right ascension in radians.
	 */
	private double ra;
	/**
	 * The declination in radians.
	 */
	private double dec;
	/**
	 * The equinox.
	 */
	private DateTime equinox;

	/**
	 * Create Equatorial coordinates.
	 * @param ra The right ascension in radians.
	 * @param dec The declination in radians.
	 * @param equinox The equinox.
	 */
	public Equatorial(double ra, double dec, DateTime equinox) {
		this.ra = ra;
		this.dec = dec;
		this.equinox = equinox;
	}

	/**
	 * Create J2000 Equatorial coordinates.
	 * @param ra The right ascension in hours
	 * @param dec The declination in degrees
	 */
	public Equatorial(double ra, double dec) {
		this.ra = Math.toRadians(ra * 15.0);
		this.dec = Math.toRadians(dec);
		this.equinox = new DateTime(2000,1,1.0);
	}

	/**
	 * Create J2000 Equatorial coordinates.
	 * @param hr,minT,secT The right ascension in hours, minutes, and seconds of time.
	 * @param deg,min,sec The declination in degrees, minutes, and seconds of arc.
	 * @param equinox The equinox.
	 */
	public Equatorial(int hr, int minT, double secT, int deg, int min, double sec) {
		this.ra = Math.toRadians((hr + minT/60.0 +  secT/3600.0) * 15.0);
		this.dec = Math.toRadians(deg + min/60.0 + sec/3600.0);
		this.equinox = new DateTime(2000,1,1.0);
	}

	/**
	 * Get the equinox associated with this coordinate.
	 * @return The equinox.
	 */
	public DateTime getEquinox() {
		return equinox;
	}

	/**
	 * Get the declination in radians.
	 * @return The declination in radians.
	 */
	public double getDec() {
		return dec;
	}

	/**
	 * Get the right ascension in radians.
	 * @return The right ascension in radians.
	 */
	public double getRa() {
		return ra;
	}
	/**
	 * Get the declination in degrees.
	 * @return The declination in degrees.
	 */
	public double getDecInDegrees() {
		return Math.toDegrees(dec);
	}

	/**
	 * Get the right ascension in degrees.
	 * @return The right ascension in degrees.
	 */
	public double getRaInDegrees() {
		return Math.toDegrees(ra);
	}
	
	/**
	 * Get the right ascension in hours.
	 * @return The right ascension in hours.
	 */
	public double getRaInHours() {
		return Math.toDegrees(ra) / 15.0;
	}
	
	public String toString() {
		return "(" + getRaInDegrees() + "," + getDecInDegrees() + ")";
	}

	public static void main(String[] args) {
		Ecliptical x = Equatorial.J2000ToEcliptical(Math.toRadians(116.328942),Math.toRadians(28.026183));
		System.out.println("longitude = " + x.getLongitudeInDegrees());
		System.out.println(" latitude = " + x.getLatitudeInDegrees());
		System.out.println(" equinox = " + x.getEquinox());
	}	
	
}
