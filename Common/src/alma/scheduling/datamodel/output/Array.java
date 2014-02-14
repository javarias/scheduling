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
 * "@(#) $Id: Array.java,v 1.5 2010/08/05 19:43:10 ahoffsta Exp $"
 */
package alma.scheduling.datamodel.output;

import java.util.Date;

/**
 * @author Arturo Hoffstadt Urrutia
 * @version 1.0
 * @created 15-Abr-2010 14:54:52
 */
public class Array {

	private double availableTime;
	private Date creationDate;
	private Date deletionDate;
	private long id;
	private long originalId;
	private double maintenanceTime;
	private double scientificTime;
	private double resolution;
	private double uvCoverage;
	private double minResolution;
	private double maxResolution;
	private String configurationName;

	public Array(){

	}

    public double getAvailableTime() {
        return availableTime;
    }

    public void setAvailableTime(double availableTime) {
        this.availableTime = availableTime;
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
    
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	public double getResolution() {
		return resolution;
	}

	public void setResolution(double resolution) {
		this.resolution = resolution;
	}

	public double getUvCoverage() {
		return uvCoverage;
	}

	public void setUvCoverage(double uvCoverage) {
		this.uvCoverage = uvCoverage;
	}

	public long getOriginalId() {
		return originalId;
	}

	public void setOriginalId(long originalId) {
		this.originalId = originalId;
	}

	public double getMinResolution() {
		return minResolution;
	}

	public void setMinResolution(double minResolution) {
		this.minResolution = minResolution;
	}

	public double getMaxResolution() {
		return maxResolution;
	}

	public void setMaxResolution(double maxResolution) {
		this.maxResolution = maxResolution;
	}

	public String getConfigurationName() {
		return configurationName;
	}

	public void setConfigurationName(String configurationName) {
		this.configurationName = configurationName;
	}
	
}