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
 * File R3Policy.java
 */
package alma.scheduling.Scheduler.DSA;

import alma.scheduling.SBLite;

import alma.scheduling.Define.SB;
import alma.scheduling.Define.SBQueue;
import alma.scheduling.Define.Clock;
import alma.scheduling.Define.Telescope;
import alma.scheduling.Define.ProjectManager;
import alma.scheduling.Define.SiteCharacteristics;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.Policy;
import alma.scheduling.Define.PolicyFactor;
import alma.scheduling.Define.Subarray;
import alma.scheduling.Define.FrequencyBand;
import alma.scheduling.Define.WeatherCondition;
import alma.scheduling.Define.SchedulingException;
import alma.scheduling.Define.BestSB;
import alma.scheduling.Define.NothingCanBeScheduled;

import java.util.logging.Logger;

/**
 * This is one of the dynamic scheduling algorithms for R3.
 * 
 * @version $Id: R3Policy.java,v 1.8 2005/11/22 23:31:00 sslucero Exp $
 * @author Sohaila Lucero
 */
class R3Policy extends PolicyType {

    /**
      * policy's name
      */
	static public final String name = "R3Policy"; 
	/**
      * queue of sbs
      */
	private SBQueue queue;
    /**
      * the clock
      */
	private Clock clock;
    /**
      * the logger
      */
	private Logger log;
    /**
      * The policy
      */
	private Policy policy;
    /**
      * Telescope's info
      */
	private Telescope telescope;
    /**
      * The project manager object
      */
	private ProjectManager projectManager;
    /**
      * array's name
      */
	private String arrayName;
	
    /**
      * elevation position weight
      */
	private double positionElW;
    /**
      * max position weight
      */
	private double positionMaxW;
    /**
      * weather weight
      */
	private double weatherW;
    /**
      * priority weight
      */
	private double priorityW;
    /**
      * same project same band weight
      */
	private double samePSameBW;
    /**
      * same project diff band weight
      */
	private double samePDiffBW;
    /**
      * diff project same band weight
      */
	private double diffPSameBW;
    /**
      * diff project diff band weight
      */
	private double diffPDiffBW;
    /**
      * new project weight
      */
	private double newPW;
    /**
      * one sb weight
      */
	private double oneSBW;
	
	/**
      *The array of scheduling units.
      */
	private R3Unit[] unit;
	
    /**
      * The index of the best sb
      */
	private int bestNumber;
	
	
	public R3Policy (String arrayName, Policy policy, SBQueue queue, 
			Clock clock, Telescope telescope, ProjectManager projectManager,
			Logger log, int bestNumber ) throws SchedulingException {
		// Save the parameters.
		this.arrayName = arrayName;
		this.policy = policy;
		this.queue = queue;
		this.clock = clock;
		this.telescope = telescope;
		this.projectManager = projectManager;
		this.log = log;
		this.bestNumber = bestNumber;
		
		// Set the weighting factors.
		setWeights();
		
		// Create the scheduling units.
		SB[] sb = queue.getAll();
        //System.out.println("sb.length= " +sb.length);
		unit = new R3Unit [sb.length];
		SiteCharacteristics site = telescope.getSite();
        
        /*
        System.out.println("In R3Policy");
        System.out.println("longitude = "+ site.getLongitude());
        System.out.println("latitude = "+ site.getLatitude());
        System.out.println("timeZone = "+ site.getTimeZone());
        System.out.println("altitude = "+ site.getAltitude());
        System.out.println("minimumElevationAngle = "+ site.getMinimumElevationAngle());
        System.out.println("numberAntennas = "+ site.getNumberAntennas());
        System.out.println("band = "+ site.getBand());
        */
		for (int i = 0; i < unit.length; ++i) {
			unit[i] = new R3Unit (sb[i],site);
		}
		
		// Sort the list of scheduling units by scientific priority.
		R3Unit tmp = null;
		int n = unit.length;
		if (n > 2) {
			int j = 0;
			int incr = n / 2;
			while (incr >= 1) {
				for (int i = incr; i < n; i++) {
					tmp = unit[i];
					j = i;
					while (j >= incr && unit[j - incr].getPriority() < tmp.getPriority()) {
						unit[j] = unit[j - incr];
						j -= incr;
					}
					unit[j] = tmp;
				}
				incr /= 2;
			}
		}
        //System.out.println("unit length = " +unit.length);
		
		// Might want to modify this.
		log.info("SCHEDULING: DynamicSchedulingAlgorithm policy " + name);
		log.info("SCHEDULING: DynamicSchedulingAlgorithm position " + positionElW);
		log.info("SCHEDULING: DynamicSchedulingAlgorithm position " + positionMaxW);
		log.info("SCHEDULING: DynamicSchedulingAlgorithm weather " + weatherW);
		log.info("SCHEDULING: DynamicSchedulingAlgorithm priority " + priorityW);
		log.info("SCHEDULING: DynamicSchedulingAlgorithm sameProjectSameBand " + samePSameBW);
		log.info("SCHEDULING: DynamicSchedulingAlgorithm sameProjectDifferentBand " + samePDiffBW);
		log.info("SCHEDULING: DynamicSchedulingAlgorithm differentProjectSameBand " + diffPSameBW);
		log.info("SCHEDULING: DynamicSchedulingAlgorithm differentProjectDifferentBand " + diffPDiffBW);
		log.info("SCHEDULING: DynamicSchedulingAlgorithm newProject " + newPW);
		log.info("SCHEDULING: DynamicSchedulingAlgorithm oneSBRemaining " + oneSBW);
		for (int i = 0; i < unit.length; ++i) {
			log.info("SCHEDULING: DSA: " + unit[i].scoreToString() + " " + unit[i].visibleToString());
		}
		
	}

	static private final double radToDeg = 180.0 / Math.PI;
	static private final double radToHour =  12.0 / Math.PI;
	static private final double degToRad = Math.PI / 180.0;
	static private final double hourToRad =  Math.PI / 12.0;
	
	/**
	 * Rules about the weighting factors.
	 * 1. Sucess factors cannot all be zero.
	 * 2. The proprity weight must be positive and greater than 0.
	 * 3. The ramaining ranking factors can be negative, 0, or positive.
	 */
	private void setWeights() {
		if (!policy.getName().equals("R3.0Policy")) {
			log.severe("Scheduling policy " + policy.getName() + " is not supported");
			//System.exit(0);
		}
		PolicyFactor[] factor = policy.getFactor();
		if (factor.length != 10) {
			log.severe("Scheduling policy has wrong number of factors");
			//System.exit(0);			
		}
		String name = null;
		for (int i= 0; i < factor.length; ++i) {
			name = factor[i].getName();
			if (name.equals("positionElevation")) 
				positionElW = factor[i].getWeight();
			else if (name.equals("positionMaximum")) 
				positionMaxW = factor[i].getWeight();
			else if (name.equals("weather"))
				weatherW = factor[i].getWeight();
			else if (name.equals("priority"))
				priorityW = factor[i].getWeight();
			else if (name.equals("sameProjectSameBand"))
				samePSameBW = factor[i].getWeight();
			else if (name.equals("sameProjectDifferentBand"))
				samePDiffBW = factor[i].getWeight();
			else if (name.equals("differentProjectSameBand"))
				diffPSameBW = factor[i].getWeight();
			else if (name.equals("differentProjectDifferentBand"))
				diffPDiffBW = factor[i].getWeight();
			else if (name.equals("newProject"))
				newPW = factor[i].getWeight();
			else if (name.equals("oneSBRemaining"))
				oneSBW = factor[i].getWeight();
			else {
				log.severe("Improper scheduling factor name " + name);
				System.exit(0);			
			}
		}
	}


	/**
	 * Get the best scheduling blocks to run at the specified time.
	 * 
	 * Notes:
	 *  1. In computing the "best" list, only SBs that are "ready"
	 * 		are to be considered.  SBs are never deleted from the list.
	 *  2. The success computation is always between 0.0 and 1.0.
	 *  3. The ranking calculation can be positive, 0, or negative.
	 *  4. Generally, a final score that is 0.0 or less should not
	 * 		be considered.
	 */
	public BestSB getBest() throws SchedulingException {
		// Check if there is something left to schedule.
        
		BestSB best = null;
        int i=0;
        for(; i < unit.length; i++){
            if(unit[i].isReady() || unit[i].isRunning()){
                break;
            } 
        }
        if(i == unit.length){
            best = new BestSB(new NothingCanBeScheduled(clock.getDateTime(), 
                        NothingCanBeScheduled.Other, "Nothing is ready."));
            return best;

//            return null;
        }
        score();
        R3Unit[] list = topList();

		if (list.length == 0) {
			// Nothing can be scheduled.
			best = new BestSB(new NothingCanBeScheduled (
                clock.getDateTime(), whyNothing(), ""));
            log.info("SCHEDULING: Nothing Can Be Scheduled Event sent out, in R3Policy");
		} else {
			String[] id = new String [list.length];
			String[] scoreString = new String [list.length];
			double[] score = new double [list.length];
			double[] success = new double [list.length];
			double[] rank = new double [list.length];
			for (i = 0; i < list.length; ++i) {
                try {
    				id[i] = list[i].getSB().getId();
	    			scoreString[i] = list[i].scoreToString();
		    		score[i] = list[i].getScore();
			    	success[i] = list[i].getSuccess();
				    rank[i] = list[i].getRank();
                } catch(NullPointerException npe) {
                    npe.printStackTrace();
                }   
			}
			best = new BestSB (id, getSBLites(list), scoreString, score, success, rank, clock.getDateTime());
		}
		return best;
	}

    private SBLite[] getSBLites(R3Unit[] u) {
        SBLite[] lites = new SBLite[u.length];
        for(int i=0; i < u.length; i++){
            lites[i] = u[i].getSBLite();
        }
        return lites;
    }


	// Figure out why and set the reason code.
	private int whyNothing() {
        for (int i = 0; i < unit.length; ++i) {
			if (unit[i].isReady()){
				System.out.println(unit[i].toString());
            }// else {
			//	System.out.println("Not Ready "+unit[i].toString());
            //
		}
		// Do we have visible targets? if not, then done.
		int i = 0;
		for (; i < unit.length; ++i) {
			if (!unit[i].isReady()) {
                continue;
            }
			if (unit[i].getPositionEl() > 0.0) {
				break;
			}
		}
		if (i == unit.length){
			return NothingCanBeScheduled.NoVisibleTargets;
        }
		
		// Do we have any favorable weather conditions? if not then done.
		// But, we only consider visible targets.
		for (; i < unit.length; ++i) {
			if (!unit[i].isReady()){
                continue;
            }
			if (unit[i].getPositionEl() <= 0.0){
                continue;
            }
			if (unit[i].getWeather() > 0.0) {
				break;
			}
		}
		if (i == unit.length){
			return NothingCanBeScheduled.BadWeather;		
        }
		
		// Are the targets not in optimal position? if not, then done.
		for (; i < unit.length; ++i) {
			if (!unit[i].isReady()) {
                continue;
            }
			if (unit[i].getPositionEl() <= 0.0){
                continue;
            }
			if (unit[i].getWeather() <= 0.0){
                continue;
            }
			if (unit[i].getPositionMax() > 0.0) {
				break;
			}
		}
		if (i == unit.length){
			return NothingCanBeScheduled.BetterToWait;
        }

		// Are the scores too low?
		for (; i < unit.length; ++i) {
			if (!unit[i].isReady()) {
                continue;
            }
			if (unit[i].getPositionEl() <= 0.0) {
                continue;
            }
			if (unit[i].getWeather() <= 0.0) {
                continue;
            }
			if (unit[i].getPositionMax() <= 0.0) {
                continue;
            }
			if (unit[i].getRank() != 0 ) {
				break;
			}
		}
		if (i == unit.length){
			return NothingCanBeScheduled.LowScores;		
        }
		
		return NothingCanBeScheduled.Other;
	}
	
	private R3Unit[] topList() {
		class Pair {
			Pair (int a, double b) {
				this.a = a;
				this.b = b;
			}
			int a;
			double b;
		}
		if (unit.length < 2) {
            //System.out.println("should be only one left, top list < 2");
			return unit;
        }
		
		// Get anything that can be scheduled.
		Pair[] copy = new Pair [unit.length];
		double value = 0.0;
		int size = 0;
		for (int i = 0; i < copy.length; ++i) {
			if (unit[i].isReady()) {
				value = unit[i].getScore();
                //System.out.println("Score = "+ value +" for "+unit[i].getSB().getId());
				if (value > 0.0) { // We're not going to consider scores that are less than 0.
					copy[size++] = new Pair (i,value);
				}
			}
		}

		// If we can't schedule anything, we're done.
		if (size == 0) {
			return new R3Unit [0];
		}

		// Get the top N.
		int bestSize = size < bestNumber ? size : bestNumber;
		Pair tmp = null;
		int pos = 0;
		for (int i = 0; i < bestSize; ++i) {
			tmp = copy[i];
			pos = i;
			for (int j = i + 1; j < size; ++j) {
				if (copy[j].b > tmp.b) {
					tmp = copy[j];
					pos = j;
				}
			}
			copy[pos] = copy[i];
			copy[i] = tmp;
		}

		// Return the top N scheduling units.
        System.out.println("BestSize="+bestSize);
		R3Unit[] out = new R3Unit [bestSize];
		for (int i = 0; i < bestSize; ++i) {
			out[i] = unit[copy[i].a];
        }
	
        //System.out.println("top list size: "+out.length);
		return out;
	}

	private void score() {
		success();
		rank();		
		for (int i = 0; i < unit.length; ++i) {
			if (!unit[i].isReady()) {
                continue;
            }
			unit[i].setScore(unit[i].getSuccess() * unit[i].getRank());
		}
	}

	private void rank() {
		R3Unit u = null;
		double x = 0.0;
		Subarray array = telescope.getArray(arrayName);
		if (array == null) {
			log.severe("No such array as " + arrayName);
			throw new IllegalArgumentException ("No such array as " + arrayName);
		}
		String currentProjectId = array.getCurrentProject();
		FrequencyBand currentBand = array.getCurrentFrequencyBand();
		for (int i = 0; i < unit.length; ++i) {
			if (!unit[i].isReady()) { 
                continue;
            }
			u = unit[i];
			if (u.getSuccess() == 0.0) {
				u.setRank(0.0);
            } else {
				setState(u,currentProjectId,currentBand);
				x = priorityW * u.getPriority();
				if (u.isSameProjectSameBand()) {
					x += samePSameBW;
                }
				if (u.isSameProjectDifferentBand()){
					x += samePDiffBW;
                }
				if (u.isDifferentProjectSameBand()){
					x += diffPSameBW;
                }
				if (u.isDifferentProjectDifferentBand()) {
					x += diffPDiffBW;
                }
				if (u.isNewProject()){
					x += newPW;
                }
				if (u.isOneSBRemaining()){
					x += oneSBW;
                }
				u.setRank(x);
                //System.out.println("Rank == "+x);
			}
		}
	}
	
	private void success() {
		double pEl = 0.0;
		double pMax = 0.0;
		double w = 0.0;
		SB sb = null;
		R3Unit u = null;
		for (int i = 0; i < unit.length; ++i) {
			if (!unit[i].isReady()) {
                continue;
            }
			u = unit[i];
			sb = u.getSB();
			pEl = positionEl(u);
			pMax = positionMax(u);
			w = weather(sb);
			u.setPositionEl(pEl);
			u.setPositionMax(pMax);
			u.setWeather(w);
            /*
            System.out.println("pEl="+pEl);
            System.out.println("pMax="+pMax);
            System.out.println("w="+w);
            */
                    
			if (pEl == 0.0 || pMax == 0.0 || w == 0.0) {
				u.setSuccess(0.0);
                //System.out.println("Bad success calculation");
			} else {
				u.setSuccess((positionElW * pEl + positionMaxW * pMax + weatherW * w) / (positionElW + positionMaxW + weatherW));
			}
		}
	}

	private void setState(R3Unit u, String currentProjectId, FrequencyBand currentBand) {
		SB sb = u.getSB();
		if (currentProjectId.equals(sb.getProject().getId())) {
			if (sb.getFrequencyBand().equals(currentBand)) {
				u.setSameProjectSameBand(true);
				u.setSameProjectDifferentBand(false);
				u.setDifferentProjectDifferentBand(false);
				u.setDifferentProjectSameBand(false);
			} else {
				u.setSameProjectSameBand(false);
				u.setSameProjectDifferentBand(true);
				u.setDifferentProjectDifferentBand(false);
				u.setDifferentProjectSameBand(false);
			}
		} else {
			if (sb.getFrequencyBand().equals(currentBand)) {
				u.setSameProjectSameBand(false);
				u.setSameProjectDifferentBand(false);
				u.setDifferentProjectDifferentBand(false);
				u.setDifferentProjectSameBand(true);
			} else {
				u.setSameProjectSameBand(false);
				u.setSameProjectDifferentBand(false);
				u.setDifferentProjectDifferentBand(true);
				u.setDifferentProjectSameBand(false);
			}
		}
		if (projectManager.newProject(sb)) {
			u.setNewProject(true);
		}
		if (projectManager.numberRemaining(sb) == 1) {
			u.setOneSBRemaining(true);
		}
	}
	
	private double positionEl(R3Unit u) {
		if (u.isVisible(clock.getDateTime())) {
			return Math.sin(u.getElevation(clock.getDateTime(),telescope.getSite()));
        }
		return 0.0;
	}

	private double positionMax(R3Unit u) {
		// First, we make sure the source is visible.
		DateTime t = clock.getDateTime();
		if (!u.isVisible(t)) {
			return 0.0;
        }
		// Second, make sure it is still visible at the end of the observing time.
		t.add(u.getSB().getMaximumTimeInSeconds());
		if (!u.isVisible(t)){
			return 0.0;
        }
		// Ok, now the idea is to give a high weight to a position if it is close 
		// to the maximum elevation of the source.
		// We compute a delta; its units are hours.  This delta is the absolute value of 
		// the difference between the current LST and the LST at the maximum elevation.  
		// However, the LST at the maximum elevation is adjusted so that its maximum elevation 
		// would occur in the middle of this scheduling unit's observing period.
		double delta = Math.abs((u.getLstMax() * radToHour) - 
				(u.getSB().getMaximumTimeInSeconds() / 7200.0) - 
				clock.getTimeOfDay());
		if (0.0 <= delta && delta < 0.5) {
            return 1.0;
        }
		if (0.5 <= delta && delta < 1.0) {
            return 0.9;
        }
		if (1.0 <= delta && delta < 1.5) {
            return 0.8;
        }
		return 0.0;
		
	}
	
	private double weather(SB sb) {
		double x = 1.0;
		try {
			WeatherCondition w = sb.getWeatherConstraint();
			if (w != null){
				x =  w.evaluate();
            }
		} catch (Exception err) {
			err.printStackTrace();
		}
		return x;
	}
	
}
