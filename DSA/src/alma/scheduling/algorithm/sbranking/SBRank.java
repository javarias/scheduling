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
 * "@(#) $Id: SBRank.java,v 1.5 2010/04/16 20:59:49 javarias Exp $"
 */
package alma.scheduling.algorithm.sbranking;


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
    private double rank;
    /**
     * SB uid reference
     */
    private String uid;
    
    public SBRank(long id, double rank, String uid) {
        super();
        this.id = id;
        this.rank = rank;
        this.uid = uid;
    }
    
    public SBRank(){
    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public String toString() {
        return "SBRank [id=" + id + ", rank=" + rank + ", uid=" + uid + "]";
    }

    @Override
    public int compareTo(SBRank o) {
        return (int) (this.getRank() * 100000 - o.getRank() * 100000);
    }
    
}
