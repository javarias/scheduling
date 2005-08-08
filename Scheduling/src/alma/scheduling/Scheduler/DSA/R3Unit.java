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
 * File R3Unit.java
 */
package alma.scheduling.Scheduler.DSA;

import alma.scheduling.Define.Equatorial;
import alma.scheduling.Define.SB;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.SiteCharacteristics;

import java.text.NumberFormat;

/**
 * The R3Unit class is used by the R3Policy class.
 * 
 * @version $Id: R3Unit.java,v 1.2 2005/08/08 21:53:41 sslucero Exp $
 * @author Allen Farris
 */
class R3Unit extends SchedulingUnit {

    /**
      * SB associated with this unit.
      */
	private SB sb;
    /**
      * The Score
      */
	private double score;
    /**
      * The Success
      */
	private double success;
    /**
      * The Ranking
      */
	private double rank;
    /**
      * Elevation position weight
      */
	private double positionEl;
    /**
      * Maximum position weight
      */
	private double positionMax;
    /**
      * weather weight
      */
	private double weather;
    /**
      * The priority
      */
	private int priority;
    /**
      * Does it have same project & same band?
      */
	private boolean sameProjectSameBand;
    /**
      * Does it have same project & different band?
      */
	private boolean sameProjectDifferentBand;
    /**
      * Does it have different project & same band?
      */
	private boolean differentProjectSameBand;
    /**
      * Does it have different project & different band?
      */
	private boolean differentProjectDifferentBand;
    /**
      * Is it a new Project?
      */
	private boolean newProject;
    /**
      * Is there one sb remaining?
      */
	private boolean oneSBRemaining;
	/**
      * Sin Dec
      */
	private double sinDec;
    /**
      * cos dec
      */
	private double cosDec;
	/**
      * Visibility
      */
	private int visible;
    /**
      *max elevation in radians
      */
	private double elMax;	
    /**
      *LST at maximum elevation
      */
	private double lstMax;	
    /**
      *LST at setting minimum elevation, adjusted by totalTime.
      */
	private double lstSet;	
    /**
      * LST at rising minimum elevation
      */
	private double lstRise;	
    /**
      * ??
      */
	private NumberFormat dform;
	
    /**
      * Calls the scoreToString method
      */
	public String toString() {
		return scoreToString();
	}		

    /**
      * prints the format to string form
      */
	public String formatToString() {
		return "sunitId (projectId) [ score, rank (priorityFactor, sameProjectSameBandFactor, sameProjectDifferentBandFactor, differentProjectSameBandFactor, differentProjectDifferentBandFactor, newProjectFactor, oneSBRemainingFactor), success (positionEl, positionMax, weather) ]";
	}

    /**
      * prints the visibility to string form
      */
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

    /**
      * Prints the score to string form 
      */
	public String scoreToString() {
		return sb.getId() + " (" + sb.getProject().getId() + ") [ " + 
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
	public R3Unit(SB sb, SiteCharacteristics site) {
		super();
		this.sb = sb;
		this.score = 0.0;
		this.success = 0.0;
		this.rank = 0.0;
		this.positionEl = 0.0;
		this.positionMax = 0.0;
		this.weather = 0.0;
		this.priority = sb.getScientificPriority().getPriorityAsInt();
		//this.priority = 1;
		this.sameProjectSameBand = false;
		this.sameProjectDifferentBand = false;
		this.differentProjectSameBand = false;
		this.differentProjectDifferentBand = true;
		this.newProject = true;
		this.oneSBRemaining = false;
        sinDec=1.0;
        cosDec=1.0;
		sinDec = Math.sin(sb.getTarget().getCenter().getDec());
		cosDec = Math.cos(sb.getTarget().getCenter().getDec());
		computeTimes(sb,site); //added back in..
		// Set thenumber format.
		dform = NumberFormat.getInstance();
		dform.setMaximumFractionDigits(2);
		
	}

    /**
      * convertion of rad to deg
      */
	static private final double radToDeg = 180.0 / Math.PI;
    /**
      * conversion of rad to hour
      */
	static private final double radToHour =  12.0 / Math.PI;
    /**
      * conversion of deg to rad
      */
	static private final double degToRad = Math.PI / 180.0;
    /**
      * conversion of hour to rad
      */
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
		double minH = 0.0; 										// hour angle at minimum elevation
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
	
    /**
      * Is it visible?
      * @param DateTime LST
      * @return boolean
      */
	public boolean isVisible(DateTime lst) {
		return sb.getTarget().isVisible(lst);
	}
	
    /**
      * returns max elevation
      * @return double
      */
	public double getMaxElevation() {
		return elMax;
	}

	/**
	 * Get the elevation of the target at the specified LST.
	 * @param lst The time of interest.
	 * @param site The Site characteristics.
	 * @return double The elevation in radians of the target at the specified time.
	 */
	public double getElevation(DateTime lst, SiteCharacteristics site) {
		double sinL = site.getSinLatitude();						// sin(latitude)
		double cosL = site.getCosLatitude();						// cos(latitude)
		double sinMinEl = site.getSinMinEl();						// sin of the minimum elevation
		double x = sinL * sinDec + cosL * cosDec * Math.cos(lst.getTimeOfDay()* hourToRad - sb.getTarget().getCenter().getRa());
		return Math.asin(x);
	}

    /**
      * get sb
      * @return SB
      */
	public SB getSB() {
		return sb;
	}

    /**
      * get score
      * @return double
      */
	public double getScore() {
		return score;
	}

    /**
      * set score
      * @param double
      */
	public void setScore(double d) {
		score = d;
	}

    /**
      * get success
      * @return double
      */
	public double getSuccess() {
		return success;
	}
    /**
      * set success
      * @param double
      */
	public void setSuccess(double d) {
		success = d;
	}

    /**
      * get rank
      * @return double
      */
	public double getRank() {
		return rank;
	}
    /**
      * set rank
      * @param double
      */
	public void setRank(double d) {
		rank = d;
	}

    /**
      * @return boolean
      */
	public boolean isDifferentProjectDifferentBand() {
		return differentProjectDifferentBand;
	}
    /**
      * @return boolean
      */
	public boolean isDifferentProjectSameBand() {
		return differentProjectSameBand;
	}
    /**
      * @return boolean
      */
	public boolean isNewProject() {
		return newProject;
	}
    /**
      * @return boolean
      */
	public boolean isOneSBRemaining() {
		return oneSBRemaining;
	}
    /**
      * @return double
      */
	public double getPositionEl() {
		return positionEl;
	}
    /**
      * @return double
      */
	public double getPositionMax() {
		return positionMax;
	}
    /**
      * @return int
      */
	public int getPriority() {
		return priority;
	}
    /**
      * @return boolean
      */
	public boolean isSameProjectDifferentBand() {
		return sameProjectDifferentBand;
	}
    /**
      * @return boolean
      */
	public boolean isSameProjectSameBand() {
		return sameProjectSameBand;
	}
    /**
      * @return double
      */
	public double getWeather() {
		return weather;
	}
    
    /**
      * @param boolean
      */
	public void setDifferentProjectDifferentBand(boolean b) {
		differentProjectDifferentBand = b;
	}
    /**
      * @param boolean
      */
	public void setDifferentProjectSameBand(boolean b) {
		differentProjectSameBand = b;
	}
    /**
      * @param boolean
      */
	public void setNewProject(boolean b) {
		newProject = b;
	}
    /**
      * @param boolean
      */
	public void setOneSBRemaining(boolean b) {
		oneSBRemaining = b;
	}
    /**
      * @param double
      */
	public void setPositionEl(double d) {
		positionEl = d;
	}
    /**
      * @param double
      */
	public void setPositionMax(double d) {
		positionMax = d;
	}
    /**
      * @param boolean
      */
	public void setSameProjectDifferentBand(boolean b) {
		sameProjectDifferentBand = b;
	}
    /**
      * @param boolean
      */
	public void setSameProjectSameBand(boolean b) {
		sameProjectSameBand = b;
	}
    /**
      * @param double
      */
	public void setWeather(double d) {
		weather = d;
	}

	/**
	 * @return boolean
	 */
	public boolean isReady() {
		return sb.getStatus().isReady();
	}

	/**
	 * @return double 
	 */
	public double getLstMax() {
		return lstMax;
	}

	/**
	 * @return double 
	 */
	public double getLstRise() {
		return lstRise;
	}

	/**
	 * @return double 
	 */
	public double getLstSet() {
		return lstSet;
	}

    /**
      *
      */
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
		R3Unit unit = new R3Unit(sb,site);

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
