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
 * "@(#) $Id: Array.java,v 1.3 2010/03/02 23:35:41 javarias Exp $"
 */
package alma.scheduling.datamodel.output;

import java.util.Date;

/**
 * @author Arturo Hoffstadt Urrutia
 * @version 1.0
 * @created 02-Mar-2010 11:49:24 AM
 */
public class Array {

	/**
	 * deletionTime - creationTime
	 */
	private double availableTime;
	private double baseline;
	private Date creationDate;
	private Date deletionDate;
	/**
	 * Sum of the SB_i.executionTime, given that SB_i.type == maintenance.
	 */
	private double maintenanceTime;
	/**
	 * Sum of the SB_i.executionTime, given that SB_i.type == scientific.
	 */
	private double scientificTime;

	public Array(){

	}

    public double getAvailableTime() {
        return availableTime;
    }

    public void setAvailableTime(double availableTime) {
        this.availableTime = availableTime;
    }

    public double getBaseline() {
        return baseline;
    }

    public void setBaseline(double baseline) {
        this.baseline = baseline;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getDeletionDate() {
        return deletionDate;
    }

    public void setDeletionDate(Date deletionDate) {
        this.deletionDate = deletionDate;
    }

    public double getMaintenanceTime() {
        return maintenanceTime;
    }

    public void setMaintenanceTime(double maintenanceTime) {
        this.maintenanceTime = maintenanceTime;
    }

    public double getScientificTime() {
        return scientificTime;
    }

    public void setScientificTime(double scientificTime) {
        this.scientificTime = scientificTime;
    }
}