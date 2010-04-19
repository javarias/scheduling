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
 * "@(#) $Id: Results.java,v 1.4 2010/04/19 22:24:12 ahoffsta Exp $"
 */
package alma.scheduling.datamodel.output;

import java.util.Date;
import java.util.Set;

/**
 * @author Arturo Hoffstadt Urrutia
 * @version 1.0
 * @created 02-Mar-2010 11:49:25 AM
 */
public class Results {

	private double availableTime;
	private double maintenanceTime;
	private double operationTime;
	private double scientificTime;
	private Date obsSeasonEnd;
	private Date obsSeasonStart;
	private Date startSimDate;
	private Date stopSimDate;
	public Set<Array> array;
	public Set<ObservationProject> observationProject;

	public Results(){

	}

    public double getAvailableTime() {
        return availableTime;
    }

    public void setAvailableTime(double availableTime) {
        this.availableTime = availableTime;
    }

    public double getMaintenanceTime() {
        return maintenanceTime;
    }

    public void setMaintenanceTime(double maintenanceTime) {
        this.maintenanceTime = maintenanceTime;
    }

    public double getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(double operationTime) {
        this.operationTime = operationTime;
    }

    public double getScientificTime() {
        return scientificTime;
    }

    public void setScientificTime(double scientificTime) {
        this.scientificTime = scientificTime;
    }

    public Set<Array> getArray() {
        return array;
    }

    public void setArray(Set<Array> mArray) {
        array = mArray;
    }

    public Set<ObservationProject> getObservationProject() {
        return observationProject;
    }

    public void setObservationProject(Set<ObservationProject> mObservationProject) {
        observationProject = mObservationProject;
    }

	public Date getObsSeasonEnd() {
		return obsSeasonEnd;
	}

	public void setObsSeasonEnd(Date obsSeasonEnd) {
		this.obsSeasonEnd = obsSeasonEnd;
	}

	public Date getObsSeasonStart() {
		return obsSeasonStart;
	}

	public void setObsSeasonStart(Date obsSeasonStart) {
		this.obsSeasonStart = obsSeasonStart;
	}

	public Date getStartSimDate() {
		return startSimDate;
	}

	public void setStartSimDate(Date startSimDate) {
		this.startSimDate = startSimDate;
	}

	public Date getStopSimDate() {
		return stopSimDate;
	}

	public void setStopSimDate(Date stopSimDate) {
		this.stopSimDate = stopSimDate;
	}
}