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
 * File R2aUnit.java
 */
package alma.scheduling.Scheduler.DSA;

import alma.scheduling.Define.Equatorial;
import alma.scheduling.Define.SB;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.SiteCharacteristics;

import java.text.NumberFormat;

/**
 * The R2aUnit class is used by the R2aPolicy class.
 * 
 * @version $Id: R2aUnit.java,v 1.4 2004/11/23 21:22:07 sslucero Exp $
 * @author Allen Farris
 */
class R2aUnit extends SchedulingUnit {

	private SB sb;
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

	private NumberFormat dform;
	
	public String toString() {
		return scoreToString();
	}		
	public String formatToString() {
		return "sunitId (projectId) [ score, rank (priorityFactor, sameProjectSameBandFactor, sameProjectDifferentBandFactor, differentProjectSameBandFactor, differentProjectDifferentBandFactor, newProjectFactor, oneSBRemainingFactor), success (positionEl, positionMax, weather) ]";
	}
	public String visibleToString() {
		if (visible == -1)
			return "Source is never visible.";
		String s =
			"ra: " + dform.format(sb.getTarget().getCenter().getRaInHours()) +
			" dec: " + dform.format(sb.getTarget().getCenter().getDecInDegrees()) + 
			" Max LST " + dform.format(lstMax);
		if (visible == 1)
			return s + " Source is always visible.";
		return s + " rise " + dform.format(lstRise) + " set " + dform.format(lstSet);
	}
	public String scoreToString() {
		return sb.getId() +// " (" + sb.getProject().getId() + ") [ " + 
		dform.format(score) + ", " + 
		dform.format(rank) + " (" + 
		priority + ", " + 
		sameProjectSameBand + ", " + 
		sameProjectDifferentBand + ", " + 
		differentProjectSameBand + ", " + 
		differentProjectDifferentBand + ", " + 
		newProject + ", " + 
		oneSBRemaining + "),  " +
		dform.format(success) + " (" + 
		dform.format(positionEl) + ", " + 
		dform.format(positionMax) + ", " +		
		dform.format(weather) + ") ]";
	}

	/**
	 * 
	 * @param sb The SUnit to be scheduled.
	 * @param lst The starting LST of the simulation.
	 */
	public R2aUnit(SB sb, SiteCharacteristics site) {
		super();
		this.sb = sb;
		this.score = 0.0;
		this.success = 0.0;
		this.rank = 0.0;
		this.positionEl = 0.0;
		this.positionMax = 0.0;
		this.weather = 0.0;
		//this.priority = sb.getScientificPriority().getPriorityAsInt();
		this.priority = 1;
		this.sameProjectSameBand = false;
		this.sameProjectDifferentBand = false;
		this.differentProjectSameBand = false;
		this.differentProjectDifferentBand = true;
		this.newProject = true;
		this.oneSBRemaining = false;
        sinDec=1.0;
        cosDec=1.0;
		//sinDec = Math.sin(sb.getTarget().getCenter().getDec());
		//cosDec = Math.cos(sb.getTarget().getCenter().getDec());
		//computeTimes(sb,site);
		// Set thenumber format.
		dform = NumberFormat.getInstance();
		dform.setMaximumFractionDigits(2);
		
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
	private void computeTimes(SB sb, SiteCharacteristics site) {
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
		// Set these quantities in the SB.
		sb.getTarget().setVisible(visible);
		sb.getTarget().setElMax(elMax);
		sb.getTarget().setLstMax(lstMax);
		sb.getTarget().setLstRise(lstRise);
		sb.getTarget().setLstSet(lstSet);
	}
	
	public boolean isVisible(DateTime lst) {
		return sb.getTarget().isVisible(lst);
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

	public SB getSB() {
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
	public boolean isReady() {
		return sb.getStatus().isReady();
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

	public static void main (String[] arg) {
        /*
		alma.scheduling.Define.Project p = new alma.scheduling.Define.Project ("AF1","","","","");
		SB sb = new SB ("af04");
		SiteCharacteristics site = new SiteCharacteristics (0.0, 0.0, 0, 0.0, 0.0, 1, null);
		sb.setProject(p);
		sb.setScientificPriority(alma.scheduling.Define.Priority.MEDIUM);
		alma.scheduling.Define.Equatorial coord = new Equatorial(3.2,47.0);
		alma.scheduling.Define.Target t = new alma.scheduling.Define.Target (coord,2.0);
		sb.setTarget(t);
		sb.setFrequency(68.0);
		R2aUnit unit = new R2aUnit(sb,site);

		unit.setScore(85.011111111111);
		unit.setRank(95.077777777777);
		unit.setSuccess(0.944444444444);
		unit.setDifferentProjectDifferentBand(true);
		unit.setDifferentProjectSameBand(false);
		unit.setNewProject(true);
		unit.setOneSBRemaining(false);
		unit.setPositionEl(0.8533333333);
		unit.setPositionMax(0.95222222222);
		unit.setSameProjectDifferentBand(false);
		unit.setSameProjectSameBand(false);
		unit.setWeather(0.96666666666666666);
		
		System.out.println(unit.formatToString());
		System.out.println(unit.visibleToString());
		System.out.println(unit.scoreToString());
        */
	}
	
}
