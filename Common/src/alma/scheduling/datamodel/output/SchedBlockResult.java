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
 * "@(#) $Id: SchedBlockResult.java,v 1.5 2010/05/20 16:47:28 ahoffsta Exp $"
 */
package alma.scheduling.datamodel.output;

import java.util.Date;

import alma.scheduling.datamodel.obsproject.SkyCoordinates;

/**
 * @author Arturo Hoffstadt Urrutia
 * @version 1.0
 * @created 02-Mar-2010 11:49:25 AM
 */
public class SchedBlockResult {

	private Date endDate;
	private double executionTime;
	private long id;
	private long originalId;
	private String mode;
	private double representativeFrequency;
	private double goalSensitivity;
	private double achievedSensitivity;
	private Date startDate;
	private ExecutionStatus status;
	private String type;
	public Array ArrayRef;
	private SkyCoordinates representativeSource;
	private Integer representativeBand;
	
	public long getOriginalId() {
		return originalId;
	}

	public void setOriginalId(long originalId) {
		this.originalId = originalId;
	}

	public SchedBlockResult(){

	}

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public double getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(double executionTime) {
        this.executionTime = executionTime;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Array getArrayRef() {
        return ArrayRef;
    }

    public void setArrayRef(Array arrayRef) {
        ArrayRef = arrayRef;
    }

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public double getRepresentativeFrequency() {
		return representativeFrequency;
	}

	public void setRepresentativeFrequency(double representativeFrequency) {
		this.representativeFrequency = representativeFrequency;
	}
	
	public double getGoalSensitivity() {
		return goalSensitivity;
	}

	public void setGoalSensitivity(double goalSensitivity) {
		this.goalSensitivity = goalSensitivity;
	}

	public double getAchievedSensitivity() {
		return achievedSensitivity;
	}

	public void setAchievedSensitivity(double achievedSensitivity) {
		this.achievedSensitivity = achievedSensitivity;
	}

	public SkyCoordinates getRepresentativeSource() {
		return representativeSource;
	}

	public void setRepresentativeSource(SkyCoordinates representativeSource) {
		this.representativeSource = representativeSource;
	}

	public Integer getRepresentativeBand() {
		return representativeBand;
	}

	public void setRepresentativeBand(Integer representativeBand) {
		this.representativeBand = representativeBand;
	}

}