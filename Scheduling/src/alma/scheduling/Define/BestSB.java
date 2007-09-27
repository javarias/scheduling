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
 * File BestSB.java
 */
 
package alma.scheduling.Define;


/**
 * The BestSB class is information that is returned by a dynamic scheduling
 * algorithm when requested to select the best units to execute at any point
 * in time.  It contains an small array of SB identifiers in order 
 * of highest to lowest score, together with information about their ranking
 * factors.  The number of items in the array is also returned.  If nothing 
 * can be scheduled, the number of items returned is zero; otherwise, the
 * number of items returned is the length of the array of SB-Ids.
 * If nothing can be scheduled, the BestSB object contains a NothingCanBeScheduled 
 * object that designates why nothing could be scheduled.
 * 
 * @version $Id: BestSB.java,v 1.10 2007/09/27 19:43:04 sslucero Exp $
 * @author Allen Farris
 */
public class BestSB {
	
	/**
	 * The number of units returned.  This may be 0, in which case there were
	 * no units that could be selected at this time.
	 */
	private int numberReturned;

    private LiteSB[] liteSBs;
	/**
	 * The array of SB-ds that are to be considered, in rank order highest to lowest. 
	 */
	private String[] sbId;
	/**
	 * An array of strings representing the computed ranking and factors in the same
	 * order as the array of unitIds.
	 */
	private String[] scoreString;
	
    /**
      *
      */
	private double[] score;
    /**
      *
      */
	private double[] success;
    /**
      *
      */
	private double[] rank;
    /**
      *
      */
    private int[] priority;
    /**
      *
      */
	private int selection;

	/**
	 * The time this selection was made.
	 */
	private DateTime time;
	
	/**
	 * An object designating why nothing could be scheduled.
	 */
	private NothingCanBeScheduled nothing;

	/**
	 * Constrruct a BestSB object when nothing can be scheduled.
	 * @param nothing The NothingCanBeScheduled object designating why nothing
	 * 		can be scheduled at this time.
	 */
	public BestSB(NothingCanBeScheduled nothing) {
		this.nothing = nothing;
		numberReturned = 0;
		sbId = new String [0];
		scoreString = new String [0];
		score = new double [0];
		success = new double [0];
		rank = new double [0];
		selection = 0;		//if this is not modifed elsewhere then the first sb is always taken...
		time = nothing.getTime();
	}
	
	/**
	 * Construct a BestSB object when something can be scheduled.
	 * @param unitId The array of SUnit-ids that are to be considered, in rank 
	 * 		order highest to lowest.
	 * @param rank An array of strings representing the computed ranking and 
	 * 		factors in the same order as the array of unitIds.
	 * @param time The time this selection was made.
	 */
	public BestSB(String[] sbId, String[] scoreString, double[] score, double[] success, 
			double[] rank, int[] pri, DateTime time) {
		if (scoreString.length != sbId.length) {
			throw new IllegalArgumentException (
               "The sbId and scoreString arrays do not have the same number of dimensions.");
        }
		if (score.length != sbId.length) {
			throw new IllegalArgumentException (
               "The sbId and score arrays do not have the same number of dimensions.");
        }
		if (success.length != sbId.length) {
			throw new IllegalArgumentException (
               "The sbId and success arrays do not have the same number of dimensions.");
        }
		if (rank.length != sbId.length) {
			throw new IllegalArgumentException (
               "The sbId and rank arrays do not have the same number of dimensions.");
        }
		numberReturned = sbId.length;		
		this.sbId = sbId;
		this.scoreString = scoreString;
		this.score = score;
		this.success = success;
		this.rank = rank;
		this.time = time;
        this.priority = pri;
		selection = 0;
		nothing = null;
	}
    
    /**
      * Constructor which includes sblites
      */
	public BestSB(String[] sbId, LiteSB[] lites, String[] scoreString, double[] score, double[] success, 
			double[] rank, int[] pri, DateTime time) {
        this(sbId, scoreString, score, success, rank, pri, time);
        this.liteSBs = lites;
    }

    

    
	/**
	 * Get the number of units returned.  This may be 0, in which case there were
	 * no units that could be selected at this time.
	 * @return The number of units returned.
	 */
	public int getNumberReturned() {
		return numberReturned;
	}

	/**
	 * Get the time this selection was made.
	 * @return The time this selection was made.
	 */
	public DateTime getTime() {
		return time;
	}

	/**
	 * Get the array of SUnit-ids that are to be considered, in rank 
	 * order highest to lowest.
	 * @return The array of SUnit-ids that are to be considered, in rank 
	 * order highest to lowest.
	 */
	public String[] getSbId() {
		return sbId;
	}
	
    public LiteSB[] getLiteSBs() {
        return liteSBs;
    }
	/**
	 * Get the NothingCanBeScheduled object, if there is one.  If there is no
	 * NothingCanBeScheduled cobject, null is returned.
	 * @return The NothingCanBeScheduled object
	 */
	public NothingCanBeScheduled getNothingCanBeScheduled() {
		return nothing;
	}
	
    /**
      * Returns the SB that is the best one to schedule now!
      */
	public String getBestSelection() {
		if (numberReturned == 0) {
            //System.out.println("BEST SB: num returned == 0");
			return null;
        }
        //numberReturned--;
		return sbId[selection];
	}
	
    /**
      *
      */
	public String toString() {
		if (numberReturned == 0) {
			return nothing.toString();
		}
		String tmp = "best list:";
		for (int i = 0; i < numberReturned; ++i) {
			tmp += "\n\t[" +sbId[i] + "|" + scoreString[i] + "]";
		}
		return tmp;
	}

	/**
	 * @return Returns the selection.
	 */
	public int getSelection() {
		return selection;
	}

	/**
	 * @param selection The selection to set.
	 */
	public void setSelection(int selection) {
		this.selection = selection;
	}

	/**
	 * @return Returns the rank.
	 */
	public double[] getRank() {
		return rank;
	}

	/**
	 * @return Returns the score.
	 */
	public double[] getScore() {
		return score;
	}

	/**
	 * @return Returns the scoreString.
	 */
	public String[] getScoreString() {
		return scoreString;
	}

	/**
	 * @return Returns the success.
	 */
	public double[] getSuccess() {
		return success;
	}

    public int[] getPriority(){
        return priority;
    }
}
