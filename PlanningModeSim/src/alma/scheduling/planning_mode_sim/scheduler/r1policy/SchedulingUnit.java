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
 * File SchedulingUnit.java
 */
 
package alma.scheduling.planning_mode_sim.scheduler.r1policy;

import alma.scheduling.planning_mode_sim.define.SUnit;
import alma.scheduling.planning_mode_sim.define.DateTime;
import alma.scheduling.planning_mode_sim.define.SiteCharacteristics;

/**
 * Description 
 * 
 * @version 1.00  Sep 26, 2003
 * @author Allen Farris
 */
public class SchedulingUnit {
	
	private SUnit sb;
	private double score;
	private double success;
	private double rank;
	private double positionEl;
	private double positionMax;
	private double weather;
	private int priority;
	private boolean sameProjectSameBand;
	private boolean sameProjectDifferentBand;
	private boolean differentProjectSameBand;
	private boolean differentProjectDifferentBand;
	private boolean newProject;
	private boolean oneSBRemaining;
	
	private double sinDec;
	private double cosDec;
	
	private int visible;
	private double elMax;	// max elevation in radians
	private double lstMax;	// LST at maximum elevation
	private double lstSet;	// LST at setting minimum elevation, adjusted by totalTime.
	private double lstRise;	// LST at rising minimum elevation
	
	private boolean complete;

	public String toString() {
		return "[" + score + "," + rank + "," + success + "] " + positionEl + " " + positionMax + " " +
		weather + " " +
		priority + " " + 
		sameProjectSameBand + " " + 
		sameProjectDifferentBand + " " + 
		differentProjectSameBand + " " + 
		differentProjectDifferentBand + " " + 
		newProject + " " + 
		oneSBRemaining + " " +
		lstRise + " " +
		lstMax + " " +
		lstRise;
	}

	/**
	 * 
	 * @param sb The SUnit to be scheduled.
	 * @param lst The starting LST of the simulation.
	 */
	public SchedulingUnit(SUnit sb, SiteCharacteristics site) {
		this.sb = sb;
		this.score = 0.0;
		this.success = 0.0;
		this.rank = 0.0;
		this.positionEl = 0.0;
		this.positionMax = 0.0;
		this.weather = 0.0;
		this.priority = sb.getScientificPriority().getPriorityAsInt();
		this.sameProjectSameBand = false;
		this.sameProjectDifferentBand = false;
		this.differentProjectSameBand = false;
		this.differentProjectDifferentBand = true;
		this.newProject = true;
		this.oneSBRemaining = false;
		sinDec = Math.sin(sb.getTarget().getCenter().getDec());
		cosDec = Math.cos(sb.getTarget().getCenter().getDec());
		this.complete = false;
		computeTimes(sb,site);
	}

	static private final double radToDeg = 180.0 / Math.PI;
	static private final double radToHour =  12.0 / Math.PI;
	static private final double degToRad = Math.PI / 180.0;
	static private final double hourToRad =  Math.PI / 12.0;
	
	/**
	 * This calculation is primarily based on the discussion in Meeus,
	 * chapter 13, page 93, equation 13.6.
	 * 
	 * sin(h) = sin(latitude) * sin(declination) + 
	 * 			cos(latitude) * cos(declination) * cos(localHourAngle),
	 * 
	 * where h is the altitude, positive above the horizon, negative below, and
	 * where localHourAngle is equal to the local sideral time - the right ascension.
	 * 
	 * This calculation ignores nutation.
	 * 
	 * @param sb
	 * @param site
	 * @param lstStart
	 */
	private void computeTimes(SUnit sb, SiteCharacteristics site) {
		double sinL = site.getSinLatitude();						// sin(latitude)
		double cosL = site.getCosLatitude();						// cos(latitude)
		double sinMinEl = site.getSinMinEl();						// sin of the minimum elevation
		double ra = sb.getTarget().getCenter().getRa();				// ra in radians
		elMax = Math.asin(sinL * sinDec + cosL * cosDec);			// max elevation in radians
		double cosMinH = (sinMinEl - sinL * sinDec) / (cosL * cosDec);
		double minH = 0.0; 											// hour angle at minimum elevation
		visible = 0;
		double totalTime = (sb.getMaximumTimeInSeconds() / 3600.0) * hourToRad;

		if (cosMinH > 1) {
			visible = -1;
		} else if (cosMinH < -1) {
			lstMax = ra;
			visible = 1;
		} else {
			minH = Math.acos(cosMinH);
			lstMax = ra;
	  		lstSet = minH + ra - totalTime;
	  		lstRise = -minH + ra;
		}
		if (visible == -1)
			throw new IllegalArgumentException("The source in this SB is never visible.");
		if (visible == 0 && (lstSet - lstRise) < totalTime)
			throw new IllegalArgumentException("This source is never visible for the entire time of the SB.");
	}
		
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
	
	public double getMaxElevation() {
		return elMax;
	}

	/**
	 * Get the elevation of the target at the specified LST.
	 * @param lst The time of interest.
	 * @param site The Site characteristics.
	 * @return The elevation in radians of the target at the specified time.
	 */
	public double getElevation(DateTime lst, SiteCharacteristics site) {
		double sinL = site.getSinLatitude();						// sin(latitude)
		double cosL = site.getCosLatitude();						// cos(latitude)
		double sinMinEl = site.getSinMinEl();						// sin of the minimum elevation
		double x = sinL * sinDec + cosL * cosDec * Math.cos(lst.getTimeOfDay()* hourToRad - sb.getTarget().getCenter().getRa());
		return Math.asin(x);
	}

	public SUnit getSB() {
		return sb;
	}

	public double getScore() {
		return score;
	}
	public void setScore(double d) {
		score = d;
	}

	public double getSuccess() {
		return success;
	}
	public void setSuccess(double d) {
		success = d;
	}

	public double getRank() {
		return rank;
	}
	public void setRank(double d) {
		rank = d;
	}

	public boolean isDifferentProjectDifferentBand() {
		return differentProjectDifferentBand;
	}
	public boolean isDifferentProjectSameBand() {
		return differentProjectSameBand;
	}
	public boolean isNewProject() {
		return newProject;
	}
	public boolean isOneSBRemaining() {
		return oneSBRemaining;
	}
	public double getPositionEl() {
		return positionEl;
	}
	public double getPositionMax() {
		return positionMax;
	}
	public int getPriority() {
		return priority;
	}
	public boolean isSameProjectDifferentBand() {
		return sameProjectDifferentBand;
	}
	public boolean isSameProjectSameBand() {
		return sameProjectSameBand;
	}
	public double getWeather() {
		return weather;
	}

	public void setDifferentProjectDifferentBand(boolean b) {
		differentProjectDifferentBand = b;
	}
	public void setDifferentProjectSameBand(boolean b) {
		differentProjectSameBand = b;
	}
	public void setNewProject(boolean b) {
		newProject = b;
	}
	public void setOneSBRemaining(boolean b) {
		oneSBRemaining = b;
	}
	public void setPositionEl(double d) {
		positionEl = d;
	}
	public void setPositionMax(double d) {
		positionMax = d;
	}
	public void setSameProjectDifferentBand(boolean b) {
		sameProjectDifferentBand = b;
	}
	public void setSameProjectSameBand(boolean b) {
		sameProjectSameBand = b;
	}
	public void setWeather(double d) {
		weather = d;
	}

	/**
	 * @return
	 */
	public boolean isComplete() {
		return complete;
	}

	/**
	 * @param b
	 */
	public void setComplete(boolean b) {
		complete = b;
	}

	/**
	 * @return
	 */
	public double getLstMax() {
		return lstMax;
	}

	/**
	 * @return
	 */
	public double getLstRise() {
		return lstRise;
	}

	/**
	 * @return
	 */
	public double getLstSet() {
		return lstSet;
	}

}
