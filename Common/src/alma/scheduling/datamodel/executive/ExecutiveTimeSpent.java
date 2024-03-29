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
 * "@(#) $Id: ExecutiveTimeSpent.java,v 1.12 2010/04/07 22:43:51 rhiriart Exp $"
 */
package alma.scheduling.datamodel.executive;

import java.util.Date;

/**
 * The amount of time that is charged to an Executive, during an ObservingSeason,
 * as a consequence of the execution of an SchedBlock.
 * The SchedBlock's PI is used to resolve the percentages that should be charged
 * to each Executive, depending on the PI memberships to each Executive.
 */
public class ExecutiveTimeSpent {

    /** Amount of time charged to the Executive (hours) */
	private float timeSpent;
	
	/** Identifier of the SchedBlock that was executed */
	private String sbUid;
	
	/** Executive to whom time is charged */
	private Executive executive;
	
	/** Observing season during which the SchedBlock was executed */
	private ObservingSeason observingSeason;

	/** Timestamp (UT) when the SchedBlock was executed */
	private Date executionTime;
	
	/**
	 * Zero-args constructor.
	 */
	public ExecutiveTimeSpent() { }

    // --- Getters and Setters ---	
	
    public float getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(float timeSpent) {
        this.timeSpent = timeSpent;
    }
    
    public String getSbUid() {
        return sbUid;
    }

    public void setSbUid(String sbUid) {
        this.sbUid = sbUid;
    }

    public Executive getExecutive() {
        return executive;
    }

    public void setExecutive(Executive mExecutive) {
        executive = mExecutive;
    }

    public ObservingSeason getObservingSeason() {
        return observingSeason;
    }

    public void setObservingSeason(ObservingSeason mObservingSeason) {
        observingSeason = mObservingSeason;
    }

    public Date getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Date executionTime) {
        this.executionTime = executionTime;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((executionTime == null) ? 0 : executionTime.hashCode());
		result = prime * result
				+ ((observingSeason == null) ? 0 : observingSeason.hashCode());
		result = prime * result + ((sbUid == null) ? 0 : sbUid.hashCode());
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
		if (!(obj instanceof ExecutiveTimeSpent)) {
			return false;
		}
		ExecutiveTimeSpent other = (ExecutiveTimeSpent) obj;
		if (executionTime == null) {
			if (other.executionTime != null) {
				return false;
			}
		} else if (!executionTime.equals(other.executionTime)) {
			return false;
		}
		if (observingSeason == null) {
			if (other.observingSeason != null) {
				return false;
			}
		} else if (!observingSeason.equals(other.observingSeason)) {
			return false;
		}
		if (sbUid == null) {
			if (other.sbUid != null) {
				return false;
			}
		} else if (!sbUid.equals(other.sbUid)) {
			return false;
		}
		return true;
	}
    
}