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
 * File DiurnalModel.java
 */
 
package alma.scheduling.PlanningModeSim;

import java.io.PrintStream;
import alma.scheduling.Define.Clock;

/**
 * DiurnalModel is a class that allows one to craft a function that
 * computes values of physical quantities based on a 24-hour clock.  It
 * uses a simple sinusoidal function to compute its values.  One can specify
 * several parameters in conjunction with the function in order to model,
 * fairly realisticly, physical quantities.  One specifies the name and
 * units associated with the physical quantity.  The formula used is:
 * 
 * 		f(t) = a*sin(x*t + n) + b*cos(y*t + m) + c;		where  0.0 <= t < 24.0;
 * 
 * where the "parms" are specified in the given physical units and "shifts" are
 * times, in units of hours and fractions thereof, that are added to the
 * current time of day, and the periods are dimensionless numbers.
 * 
 * As an example, the following produces a model of temperature.
 * 
 * 		DiurnalModel temp = new DiurnalModel();
 *		temp.setName("termperature");
 *		temp.setUnits("deg C");
 *		temp.setParm0(10.0);
 *		temp.setParm1(2.0);
 *		temp.setParm2(-4.0);
 *		temp.setShift0(-10.0);
 *		temp.setShift1(-9.0);
 *
 * The maximum temperature is approximately 5.6 deg C at a time between
 * 15 and 15:30 and the minimum is approximately -13.7 deg C at a time
 * between 3.0 and 3.5, with smooth and reasonable variations throughout 
 * the 24-hour period.
 * 
 * ----------------------------------------------------------------------
 * To Do:
 * 		(1) add a Clocksimulator to this class.
 * 		(2) add a method that sets the clock.
 *  	(3) add a method "double compute()" that uses the current time
 * 			of the clock to compute the desired function.
 * Implementation:
 * 		(a) The simulator's run method creates objects using this class
 * 			with parameters specified in the simulator input.
 * 		(b) The DiurnalModel object's name is used as the function names
 * 			in expressions and the object itself is used in the method 
 * 			interface. (Its "compute()" method is called.)
 * Issues:
 * 		How can other parameters, such as frequency and angle of elevation
 * 		be added to the computational function?
 * 		-- E.g., we might need to compute opacity at time t for frequency f
 * 		   at elevation angle theta.
 * 		-- One way to do this might be to provide the expression object, which
 * 		   is embedded within a scheduling block, with a refererence to that
 * 		   scheduling block, from which it can get the frequency and the
 * 		   angle of elevation.
 * ----------------------------------------------------------------------
 * 
 * @version 1.10  Dec. 12, 2003
 * @author Allen Farris
 */
public class DiurnalModel {
	// Name of this physical quantity.
	private String name;
	// Units of this physical quantity.
	private String units;
	// Parameters
	private double parm0;
	private double parm1;
	private double parm2;
	// Time shift in hours.
	private double shift0;
	private double shift1;
	// The periodicity.
	private double period0;
	private double period1;
	// The clock.
	private Clock clock;
	

	/**
	 * Create a Diurnal function.
	 * Such a function computes a value based on the time of day.
	 * 
	 */
	public DiurnalModel() {
		name = "noname";
		units = " ";
		parm0 = 1.0;
		parm1 = 1.0;
		parm2 = 0.0;
		shift0 = 0.0;
		shift1 = 0.0;
		period0 = 1.0;
		period1 = 1.0;
	}
	
	/**
	 * Formula:  parm0 * sin(period0 * time + shift) + parm1 * cos(period1 * time + shift) + parm2
	 * 
	 * @param time is the time  of day in hours and fractions thereof.
	 * @return
	 */
	private double sinusoidalFormula(double time) {
		double angle0 = (period0 * time + shift0) * Math.PI / 12.0;
		double angle1 = (period1 * time + shift1) * Math.PI / 12.0;
		return parm0 * Math.sin(angle0) + parm1 * Math.cos(angle1) + parm2;
	}

	/**
	 * Compute the quantity for the specified time.
	 * 
	 * @param time The specified time as hours and fractions thereof.
	 * @return The value of the psecified function at the specified time.
	 */
	public double compute (double time) {
		return sinusoidalFormula(time);
	}
	
	/**
	 * Compute the quantity for the current time as specified by the clock.
	 * 
	 * @return The value of the secified function at the current time.
	 */
	public double compute () {
		return sinusoidalFormula(clock.getTimeOfDay());
	}
	
	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public double getParm0() {
		return parm0;
	}

	/**
	 * @return
	 */
	public double getParm1() {
		return parm1;
	}

	/**
	 * @return
	 */
	public double getParm2() {
		return parm2;
	}

	/**
	 * @return
	 */
	public double getShift0() {
		return shift0;
	}

	/**
	 * @return
	 */
	public double getShift1() {
		return shift1;
	}

	/**
	 * @return
	 */
	public String getUnits() {
		return units;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * @param d
	 */
	public void setParm0(double d) {
		parm0 = d;
	}

	/**
	 * @param d
	 */
	public void setParm1(double d) {
		parm1 = d;
	}

	/**
	 * @param d
	 */
	public void setParm2(double d) {
		parm2 = d;
	}

	/**
	 * @param d
	 */
	public void setShift0(double d) {
		shift0 = d;
	}

	/**
	 * @param d
	 */
	public void setShift1(double d) {
		shift1 = d;
	}

	/**
	 * @param string
	 */
	public void setUnits(String string) {
		units = string;
	}

	/**
	 * @return
	 */
	public double getPeriod0() {
		return period0;
	}

	/**
	 * @return
	 */
	public double getPeriod1() {
		return period1;
	}

	/**
	 * @param d
	 */
	public void setPeriod0(double d) {
		period0 = d;
	}

	/**
	 * @param d
	 */
	public void setPeriod1(double d) {
		period1 = d;
	}
	
	/**
	 * 
	 * @param c
	 */
	public void setClock(Clock c) {
		clock = c;
	}

	public void show(PrintStream out) {
		out.println("name " + name);
		out.println("units " + units);
		out.println("parm0 " + parm0);
		out.println("parm1 " + parm1);
		out.println("parm2 " + parm2);
		out.println("shift0 " + shift0);
		out.println("shift1 " + shift1);
		out.println("period0 " + period0);
		out.println("period1 " + period1);
		out.println();
		double t = 0.0F;
		double[] hour = new double [50];
		for (int i = 0; i < 50; ++i) {
			t = i / 2.0;
			hour[i] = compute(t);
			out.println(t + "\t" + hour[i]);
		}
	}

	public static void main(String[] args) {
		System.out.println("Test of DiurnalModel");
		
		/*
		DiurnalModel temp = new DiurnalModel();
		temp.setName("termperature");
		temp.setUnits("deg C");
		temp.setParm0(10.0);
		temp.setParm1(2.0);
		temp.setParm2(-4.0);
		temp.setShift0(-10.0);
		temp.setShift1(-9.0);
		temp.setPeriod0(1.0);
		temp.setPeriod1(1.0);
		temp.show(System.out);
		*/
		
		/*
		DiurnalModel wind = new DiurnalModel();
		wind.setName("wind");
		wind.setUnits("m per sec");
		wind.setParm0(-3.0);
		wind.setParm1(2.0);
		wind.setParm2(5.0);
		wind.setShift0(13.0);
		wind.setShift1(-1.0);
		wind.setPeriod0(1.0);
		wind.setPeriod1(1.0);
		wind.show(System.out);
		*/
		
		DiurnalModel quality = new DiurnalModel();
		quality.setName("weatherQuality");
		quality.setUnits("");
		quality.setParm0(0.15);
		quality.setParm1(-0.08);
		quality.setParm2(0.75);
		quality.setShift0(0.0);
		quality.setShift1(6.0);
		quality.setPeriod0(2.0);
		quality.setPeriod1(2.0);
		quality.show(System.out);
		
		System.out.println("End test of DiurnalModel");		
	}

}
