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
 * "@(#) $Id: ExecutivePercentage.java,v 1.10 2010/04/07 22:43:51 rhiriart Exp $"
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
	 * Zero-args constructor.
	 */
	public ExecutivePercentage() { }

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
}