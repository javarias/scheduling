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
 * File R4Policy.java
 */
package alma.scheduling.Scheduler.DSA;


import alma.scheduling.Define.SB;
import alma.scheduling.Define.SBQueue;
import alma.scheduling.Define.LiteSB;
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
import alma.scheduling.Define.SchedLogger;

import java.util.logging.Logger;

/**
 * This is one of the dynamic scheduling algorithms for R3.
 * 
 * @version $Id: R4Policy.java,v 1.5 2007/09/27 19:43:04 sslucero Exp $
 * @author Sohaila Lucero
 */
class R4Policy extends PolicyType {

    /**
      * policy's name
      */
	static public final String name = "R4Policy"; 
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
	private SchedLogger log;
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
	private R4Unit[] unit;
	
    /**
      * The index of the best sb
      */
	private int bestNumber;
	
	
	public R4Policy (String arrayName, Policy policy, SBQueue queue, 
			Clock clock, Telescope telescope, ProjectManager projectManager,
			SchedLogger log, int bestNumber ) throws SchedulingException {
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
		unit = new R4Unit [sb.length];
		SiteCharacteristics site = telescope.getSite();
        
		for (int i = 0; i < unit.length; ++i) {
			unit[i] = new R4Unit (sb[i],site);
		}
		
		// Sort the list of scheduling units by scientific priority.
		R4Unit tmp = null;
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
        /*
		log.info("SCHEDULING: DynamicSchedulingAlgorithm sameProjectSameBand " + samePSameBW);
		log.info("SCHEDULING: DynamicSchedulingAlgorithm sameProjectDifferentBand " + samePDiffBW);
		log.info("SCHEDULING: DynamicSchedulingAlgorithm differentProjectSameBand " + diffPSameBW);
		log.info("SCHEDULING: DynamicSchedulingAlgorithm differentProjectDifferentBand " + diffPDiffBW);
		log.info("SCHEDULING: DynamicSchedulingAlgorithm newProject " + newPW);
		log.info("SCHEDULING: DynamicSchedulingAlgorithm oneSBRemaining " + oneSBW);
        */
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
		if (!policy.getName().equals("R4.0Policy")) {
			log.severe("Scheduling policy " + policy.getName() + " is not supported");
			//System.exit(0);
		}
		PolicyFactor[] factor = policy.getFactor();
	/*	if (factor.length != 0) {
			log.severe("Scheduling policy has wrong number of factors");
			//System.exit(0);			
		}
        */
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
            /*
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
                */
			else {
				log.severe("Improper scheduling factor name " + name);
				//System.exit(0);			
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

        }
        score();
        R4Unit[] list = topList();

		if (list.length == 0) {
			// Nothing can be scheduled.
			best = new BestSB(new NothingCanBeScheduled (
                clock.getDateTime(), whyNothing(), ""));
            log.info("SCHEDULING: Nothing Can Be Scheduled Event sent out, in R4Policy");
		} else {
			String[] id = new String [list.length];
			String[] scoreString = new String [list.length];
			double[] score = new double [list.length];
			double[] success = new double [list.length];
			double[] rank = new double [list.length];
			int[] pri = new int [list.length];
			for (i = 0; i < list.length; ++i) {
                try {
    				id[i] = list[i].getSB().getId();
	    			scoreString[i] = list[i].scoreToString();
		    		score[i] = list[i].getScore();
			    	success[i] = list[i].getSuccess();
				    rank[i] = list[i].getRank();
                    pri[i] = list[i].getPriority();
                } catch(NullPointerException npe) {
                    npe.printStackTrace(System.out);
                }   
			}
			best = new BestSB (id, getLiteSBs(list), scoreString, score, success, rank, pri, clock.getDateTime());
		}
		return best;
	}

    private LiteSB[] getLiteSBs(R4Unit[] u) {
        LiteSB[] lites = new LiteSB[u.length];
        for(int i=0; i < u.length; i++){
            lites[i] = u[i].getLiteSB();
        }
        return lites;
    }


	// Figure out why and set the reason code.
	private int whyNothing() {
        for (int i = 0; i < unit.length; ++i) {
			if (unit[i].isReady()){
				System.out.println(unit[i].toString());
            }
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
	
	private R4Unit[] topList() {
		class Pair {
			Pair (int a, double b) {
				this.a = a; //position
				this.b = b; //score value
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
			return new R4Unit [0];
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
		R4Unit[] out = new R4Unit [bestSize];
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
		R4Unit u = null;
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
				x = priorityW * u.getPriority();
				u.setRank(x);
			}
		}
	}
	
	private void success() {
		double pEl = 0.0;
		double pMax = 0.0;
		double w = 0.0;
		SB sb = null;
		R4Unit u = null;
		for (int i = 0; i < unit.length; ++i) {
			if (!unit[i].isReady()) {
                continue;
            }
			u = unit[i];
			sb = u.getSB();
            pEl = positionEl(u);
			pMax = positionMax(u);
			w = weather(sb, u.getMaxElevation());
            u.setPositionEl(pEl);
			u.setPositionMax(pMax);
			u.setWeather(w);
			if (pEl == 0.0 || pMax == 0.0 || w == 0.0) {
				u.setSuccess(0.0);
			} else {
                double tmp =
                    (positionElW * pEl + positionMaxW * pMax + weatherW * w) / 
                        (positionElW + positionMaxW + weatherW);
				u.setSuccess(tmp);
			}
		}
	}

    private double positionEl(R4Unit u) {
		if (u.isVisible(clock.getDateTime())) {
			double tmp =Math.sin(u.getElevation(clock.getDateTime(),telescope.getSite()));
            //System.out.println(u.getSB().getSBName()+": Its visible, positionEl = "+tmp);
            return tmp;
        }
        //System.out.println(u.getSB().getSBName()+":In posEl, not vis");
		return 0.0;
	}

	private double positionMax(R4Unit u) {
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
		if (1.5 <= delta && delta> 2.0) { 
            return 0.7;
        }
		if (2.0 <= delta && delta> 2.5) { 
            return 0.6;
        }
		if (2.5 <= delta && delta> 3.0) { 
            return 0.5;
        }
		if (3.0 <= delta && delta> 3.5) { 
            return 0.4;
        }
		if (3.5 <= delta && delta> 4.0) { 
            return 0.3;
        }
		if (4.0 <= delta && delta> 4.5) { 
            return 0.2;
        }
		return 0.1;
		
	}

	private double weather(SB sb, double el) {
		double x = 1.0;
        double baseline=0.0;
		try {
			WeatherCondition w = sb.getWeatherConstraint();
            //System.out.println("SB ("+sb.getSBName()+") weather condition "+w.toString());
            baseline = telescope.getArray(arrayName).getMaxBaseline();
			if (w != null){
				x =  w.evaluate(new Double(sb.getCenterFrequency()), 
                                new Double(el), 
                                new Double(baseline));
            }
		} catch (Exception err) {
			err.printStackTrace(System.out);
		}
		return x;
	}
	
}
