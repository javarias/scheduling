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
 * File Target.java
 */
 
package alma.Scheduling.Define;

/**
 * The Target class defines a target area of the sky, including equatorial
 * coordinates of its center.  The area itself may be circular, rectangular,
 * or it may be defined by a list of coordinate positions.
 * 
 * @version 1.30 May 10, 2004
 * @author Allen Farris
 */
public class Target {

	public static String CIRCLE = "circle";
	public static String RECTANGLE = "rectangle";
	public static String IRREGULAR = "irregular"; 
	
	private static final double arcsecondsToRadians = Math.PI / (3600.0 * 180.0);
		
	private Equatorial min;
	private Equatorial max;
	private Equatorial center;
	private String shape;

	// These variables apply to the center of the target.
	private int visible;
	private double elMax;	// max elevation in radians
	private double lstMax;	// LST at maximum elevation
	private double lstSet;	// LST at setting minimum elevation, adjusted by totalTime.
	private double lstRise;	// LST at rising minimum elevation
	
	/**
	 * 
	 * @param center
	 * @param radius in arcseconds
	 */
	public Target(Equatorial center, double radius) {
		shape = CIRCLE;
		this.center = center;
		radius = radius * arcsecondsToRadians;
		min = new Equatorial ((center.getRa() - radius), (center.getDec() - radius), center.getEquinox());
		max = new Equatorial ((center.getRa() + radius), (center.getDec() + radius), center.getEquinox());
	}

	/**
	 * 
	 * @param center
	 * @param width
	 * @param height in arcseconds
	 */
	public Target(Equatorial center, double width, double height) {
		shape = RECTANGLE;
		this.center = center;
		width = width * arcsecondsToRadians;
		height = height * arcsecondsToRadians;
		min = new Equatorial ((center.getRa() - width/2.0), (center.getDec() - height/2.0), center.getEquinox());
		max = new Equatorial ((center.getRa() + width/2.0), (center.getDec() + height/2.0), center.getEquinox());
	}

	public Target(Equatorial[] list) {
		shape = IRREGULAR;
		max = new Equatorial(getRaMax(list), getDecMax(list), list[0].getEquinox());
		min = new Equatorial(getRaMin(list), getDecMin(list), list[0].getEquinox());
		center = new Equatorial ( min.getRa() + ((max.getRa() - min.getRa()) / 2.0), 
					min.getDec() + ((max.getDec() - min.getDec()) / 2.0), list[0].getEquinox());
	}
	
	public Equatorial getMin() {
		return min;
	}

	private double getRaMax(Equatorial[] list) {
		double max = list[0].getRa();
		for (int i = 1; i < list.length; ++i) {
			if (list[i].getRa() > max)
				max = list[i].getRa();
		}
		return max;
	}
	private double getRaMin(Equatorial[] list) {
		double min = list[0].getRa();
		for (int i = 1; i < list.length; ++i) {
			if (list[i].getRa() < min)
				min = list[i].getRa();
		}
		return min;
	}
	private double getDecMax(Equatorial[] list) {
		double max = list[0].getDec();
		for (int i = 1; i < list.length; ++i) {
			if (list[i].getDec() > max)
				max = list[i].getDec();
		}
		return max;
	}
	private double getDecMin(Equatorial[] list) {
		double min = list[0].getDec();
		for (int i = 1; i < list.length; ++i) {
			if (list[i].getDec() < min)
				min = list[i].getDec();
		}
		return min;
	}

	public Equatorial getMax() {
		return max;
	}

	public Equatorial getCenter() {
		return center;
	}
	
	public String getShape() {
		return shape;
	}

	/**
	 * @return Returns the elMax.
	 */
	public double getElMax() {
		return elMax;
	}

	/**
	 * @param elMax The elMax to set.
	 */
	public void setElMax(double elMax) {
		this.elMax = elMax;
	}

	/**
	 * @return Returns the lstMax.
	 */
	public double getLstMax() {
		return lstMax;
	}

	/**
	 * @param lstMax The lstMax to set.
	 */
	public void setLstMax(double lstMax) {
		this.lstMax = lstMax;
	}

	/**
	 * @return Returns the lstRise.
	 */
	public double getLstRise() {
		return lstRise;
	}

	/**
	 * @param lstRise The lstRise to set.
	 */
	public void setLstRise(double lstRise) {
		this.lstRise = lstRise;
	}

	/**
	 * @return Returns the lstSet.
	 */
	public double getLstSet() {
		return lstSet;
	}
	
	/**
	 * @param lstSet The lstSet to set.
	 */
	public void setLstSet(double lstSet) {
		this.lstSet = lstSet;
	}

	/**
	 * @return Returns the visible.
	 */
	public int getVisible() {
		return visible;
	}

	/**
	 * @param visible The visible to set.
	 */
	public void setVisible(int visible) {
		this.visible = visible;
	}

	static private final double hourToRad =  Math.PI / 12.0;
	public boolean isVisible(DateTime lst) {
		if (visible == -1)
			return false;
		if (visible == 1)
			return true;
		double lstTime = lst.getTimeOfDay() * hourToRad;
		if (lstRise < lstTime && (lstTime < lstSet))
			return true;
		if (lstRise < 0.0 && ((2.0 * Math.PI + lstRise) < lstTime))
			return true;
		if (lstSet > 2 * Math.PI && (lstTime < (lstSet - 2.0 * Math.PI)))
			return true;
		return false;
	}
	
	public String toString() {
		double deltaRa = (max.getRaInDegrees() - min.getRaInDegrees()) * 3600.0;
		double deltaDec = (max.getDecInDegrees() - min.getDecInDegrees()) * 3600.0;
		return "(" + center.getRaInDegrees() + ", " + center.getDecInDegrees() + ", " +
			   deltaRa + ", " + deltaDec + ")";
	}
	
	static public void main (String[] arg) {
		// Use the VLA as the default clock.
		DateTime.setClockCoordinates(107.6177275,34.0787491666667,-6);
		
		Equatorial center = new Equatorial (9, 53, 0.0, 54, 54, 0.0);
		Target x1 = new Target (center, 120.0, 120.0);
		System.out.println("x1.max    = " + x1.getMax());
		System.out.println("x1.min    = " + x1.getMin());
		System.out.println("x1.center = " + x1.getCenter());
		Target x2 = new Target (center, 120.0);
		System.out.println("x2.max    = " + x2.getMax());
		System.out.println("x3.min    = " + x2.getMin());
		System.out.println("x3.center = " + x2.getCenter());
		Equatorial[] e = new Equatorial [5];
		e[0] = new Equatorial (9, 54, 0.0, 54, 56, 0.0);
		e[1] = new Equatorial (9, 54, 0.0, 54, 51, 0.0);
		e[2] = new Equatorial (9, 52, 0.0, 54, 56, 0.0);
		e[3] = new Equatorial (9, 52, 0.0, 54, 51, 0.0);
		e[4] = new Equatorial (9, 53, 0.0, 54, 54, 0.0);
		Target x3 = new Target (e);
		System.out.println("x3.max    = " + x3.getMax());
		System.out.println("x3.min    = " + x3.getMin());
		System.out.println("x3.center = " + x3.getCenter());
	}
}
