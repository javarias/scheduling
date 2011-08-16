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
 * "@(#) $Id: SBRank.java,v 1.9 2011/08/16 16:58:29 javarias Exp $"
 */
package alma.scheduling.algorithm.sbranking;

import java.util.ArrayList;
import java.util.List;


/**
 * This class will store the score obtained by SB in the Dynamic Scheduling Algorithm Ranker
 * 
 * @author javarias
 * @see SchedBlockRanker
 */
public class SBRank implements Comparable<SBRank>{

    /**
     * ID of SBRank
     */
    private long id;
    /**
     * Score obtained
     */
    private double score;
    /**
     * SB uid reference
     */
    private String uid;
    /**
     * Details, it could be the name of the scorer for future references
     */
    private String details;
    
    private List<SBRank> breakdownScore;
    
    public SBRank(long id, double score, String uid) {
    	this();
        this.id = id;
        this.score = score;
        this.uid = uid;
        breakdownScore = new ArrayList<SBRank>();
    }
    
    public SBRank(){
    	breakdownScore = new ArrayList<SBRank>();
    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getRank() {
        return score;
    }

    public void setRank(double rank) {
        this.score = rank;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
    
    public List<SBRank> getBreakdownScore() {
    	return breakdownScore;
    }
    
    public void setBreakdownScore(List<SBRank> score) {
    	breakdownScore = score;
    }
    
    public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public void addRank(SBRank score) {
    	breakdownScore.add(score);
    }

    @Override
    public String toString() {
        return "SBRank [id=" + id + ", score=" + score + ", uid=" + uid + "]";
    }

    @Override
    public int compareTo(SBRank o) {
        return (int) (this.getRank() * 100000 - o.getRank() * 100000);
    }
    
}
