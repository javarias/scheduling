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
 * "@(#) $Id: ExecutivePercentage.java,v 1.12 2010/04/09 15:20:02 rhiriart Exp $"
 */
package alma.scheduling.datamodel.executive;

import java.io.Serializable;

/**
 * This class represents a many-to-many relationship between ObservingSeason
 * and Executive. During an ObservingSeason, several Executives will have the
 * right of a certain amount of observation time, which is represented either by a percentage
 * over the total observation time during the ObservingSeason, or directly by the total
 * observation time in hours. These are parameters of the link between ObservingSeason and
 * Executive.
 * Reciprocally, one Executive can participate in several ObservingSeasons.
 */
public class ExecutivePercentage {

    public static class Id implements Serializable {
        
        private static final long serialVersionUID = 7509403722076407203L;
        private Long executiveId;
        private Long seasonId;
        
        public Id() {}
        
        public Id(Long executiveId, Long seasonId) {
            this.executiveId = executiveId;
            this.seasonId = seasonId;
        }
        
        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (!(o instanceof Id))
                return false;
            Id that = (Id) o;
            return (this.executiveId == null ? that.executiveId == null : this.executiveId.equals(that.executiveId)) &&
                   (this.seasonId == null ? that.seasonId == null : this.seasonId.equals(that.seasonId));
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + ((executiveId == null) ? 0 : executiveId.hashCode());
            result = 31 * result + ((seasonId == null) ? 0 : seasonId.hashCode());
            return result;
        }
    }

    private Id id = new Id();
    
    /** The Executive end of the many-to-many association */
    private Executive executive;
    
    /** The ObservingSeason end of the many-to-many association */
    private ObservingSeason season;
    
    /**
     * Percentage of the total observation time in a ObservingSeason that the Executive
     * has the right to use (0-100).
     */
	private Float percentage;
	
	/**
	 * Total observation time (in hours) that the Executive has the right to use
	 * for its observations (provided the other conditions are met, of course).
	 * This parameter is calculated from the percentage and the total observation time
	 * available in the ObservingSeason. It is included here to avoid having to calculate
	 * it all the time.
	 */
	private Double totalObsTimeForSeason;

	/**
	 * Zero-args constructor, required by Hibernate.
	 */
	public ExecutivePercentage() { }

	public ExecutivePercentage(ObservingSeason season, Executive executive, Float percentage) {
	    this(season, executive, percentage, null);
	}
	
	public ExecutivePercentage(ObservingSeason season, Executive executive, Float percentage,
	        Double totalObsTimeForSeason) {
	    this.season = season;
	    this.executive = executive;
	    this.percentage = percentage;
	    if (totalObsTimeForSeason == null || totalObsTimeForSeason == 0) {
            long start = season.getStartDate().getTime();
            long end = season.getEndDate().getTime();
            double diffHours = (end - start) / (60 * 60 * 1000);
            double obsTime = diffHours * percentage / 100;
            this.totalObsTimeForSeason = obsTime;
	    } else {
	        this.totalObsTimeForSeason = totalObsTimeForSeason;	        
	    }
	    
	    this.id.executiveId = executive.getId();
	    this.id.seasonId = season.getId();
	    
	    executive.getExecutivePercentage().add(this);
	    season.getExecutivePercentage().add(this);
	}
	
    // --- Getters and Setters ---
	
	public Id getId() {
	    return id;
	}
	
	public void setId(Id id) {
	    this.id = id;
	}
	
    public Float getPercentage() {
        return percentage;
    }

    public void setPercentage(Float percentage) {
        this.percentage = percentage;
    }

    public Double getTotalObsTimeForSeason() {
        return totalObsTimeForSeason;
    }

    public void setTotalObsTimeForSeason(Double totalObsTimeForSeason) {
        this.totalObsTimeForSeason = totalObsTimeForSeason;
    }

    public Executive getExecutive() {
        return executive;
    }

    public void setExecutive(Executive executive) {
        this.executive = executive;
    }

    public ObservingSeason getSeason() {
        return season;
    }

    public void setSeason(ObservingSeason season) {
        this.season = season;
    }
}