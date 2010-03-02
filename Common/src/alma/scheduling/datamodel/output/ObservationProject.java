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
 * "@(#) $Id: ObservationProject.java,v 1.3 2010/03/02 23:35:41 javarias Exp $"
 */
package alma.scheduling.datamodel.output;

import java.util.Set;

/**
 * @author Arturo Hoffstadt Urrutia
 * @version 1.0
 * @created 02-Mar-2010 11:49:25 AM
 */
public class ObservationProject {

	/**
	 * Sum across ScheBlock_i.executionTime.
	 */
	private double executionTime;
	private int scienceRating;
	private ExecutionStatus status;
	public Set<Affiliation> affiliation;
	public Set<SchedBlockResult> schedBlock;

	public ObservationProject(){

	}

    public double getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(double executionTime) {
        this.executionTime = executionTime;
    }

    public int getScienceRating() {
        return scienceRating;
    }

    public void setScienceRating(int scienceRating) {
        this.scienceRating = scienceRating;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public Set<Affiliation> getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(Set<Affiliation> mAffiliation) {
        affiliation = mAffiliation;
    }

    public Set<SchedBlockResult> getSchedBlock() {
        return schedBlock;
    }

    public void setSchedBlock(Set<SchedBlockResult> mSchedBlock) {
        schedBlock = mSchedBlock;
    }
}