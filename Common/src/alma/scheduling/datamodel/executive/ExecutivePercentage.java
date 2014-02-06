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
 */
package alma.scheduling.datamodel.executive;

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
	 * Remaining time for Observing in hours. Initially this has the same value
	 * than totalObsTimeForSeason.
	 * TODO When populating from the XML Store this field should be calculated from
	 * the corresponding ObsUnitStatus.
	 */
    private Double remainingObsTime;

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
            double diffHrs = (end - start) / 1000 / 3600;
            double obsTime = diffHrs * percentage / 100;
            this.totalObsTimeForSeason = obsTime;
            this.remainingObsTime = obsTime;
	    } else {
	        this.totalObsTimeForSeason = totalObsTimeForSeason;
	        this.remainingObsTime = totalObsTimeForSeason;
	    }
	    
	    executive.getExecutivePercentage().add(this);
	    season.getExecutivePercentage().add(this);
	}
	
    // --- Getters and Setters ---
	
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

    /**
     * @see #remainingObsTime
     * @return
     */
    public Double getRemainingObsTime() {
        return remainingObsTime;
    }

    public void setRemainingObsTime(Double ramainingObsTime) {
        this.remainingObsTime = ramainingObsTime;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((executive == null) ? 0 : executive.hashCode());
		result = prime * result + ((season == null) ? 0 : season.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ExecutivePercentage)) {
			return false;
		}
		ExecutivePercentage other = (ExecutivePercentage) obj;
		if (executive == null) {
			if (other.executive != null) {
				return false;
			}
		} else if (!executive.equals(other.executive)) {
			return false;
		}
		if (season == null) {
			if (other.season != null) {
				return false;
			}
		} else if (!season.equals(other.season)) {
			return false;
		}
		return true;
	}
    
}
