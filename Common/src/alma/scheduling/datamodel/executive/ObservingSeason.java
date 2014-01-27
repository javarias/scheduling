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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * The ObservingSeason is the unit of time that is used in the accountability of
 * observation time to the Executives (USA, Japan, Europe, etc.). It is usually
 * 6 months.
 * 
 * APRC simulations and the planning of the telescope operations are made for a time span
 * encopassing one or more ObservingSeasons.
 */
public class ObservingSeason implements Comparable<ObservingSeason>{
    
    /** Observing season start date */
    private Date startDate;
    
    /** Observing season end date */
	private Date endDate;
	
	/** Name to be used in output reports. For example "EarlyScience2011", "Spring2012", etc. */
	private String name;
	
	/** Executives that participate in the ObservingSeason and their percentages */
	private Set<ExecutivePercentage> executivePercentage = new HashSet<ExecutivePercentage>();
	
	private TimeInterval observingInterval;

	/**
	 * Zero-args constructor.
	 */
	public ObservingSeason() {}

    // --- Getters and Setters ---
    
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<ExecutivePercentage> getExecutivePercentage() {
        return executivePercentage;
    }

    public void setExecutivePercentage(Set<ExecutivePercentage> mExecutivePercentage) {
        executivePercentage = mExecutivePercentage;
    }


	public TimeInterval getObservingInterval() {
		return observingInterval;
	}

	public void setObservingInterval(TimeInterval observingInterval) {
		this.observingInterval = observingInterval;
	}
    
    @Override
    public int compareTo(ObservingSeason o) {
        return this.startDate.compareTo(o.startDate);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((startDate == null) ? 0 : startDate.hashCode());
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
		if (!(obj instanceof ObservingSeason)) {
			return false;
		}
		ObservingSeason other = (ObservingSeason) obj;
		if (startDate == null) {
			if (other.startDate != null) {
				return false;
			}
		} else if (!startDate.equals(other.startDate)) {
			return false;
		}
		return true;
	}
    
}