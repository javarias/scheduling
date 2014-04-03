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
 * "@(#) $Id: ObservationProject.java,v 1.8 2011/02/15 12:42:43 ahoffsta Exp $"
 */
package alma.scheduling.datamodel.output;

import java.util.Set;

/**
 * @author Arturo Hoffstadt Urrutia
 * @version 1.0
 * @created 15-Abr-2010 14:54:52
 */
public class ObservationProject {

	private double executionTime;
	private long id;
	private String originalId;
	private long scienceRank;
	private float scienceScore;
	private String grade;
	private ExecutionStatus status;
	public Set<Affiliation> affiliation;
	public Set<SchedBlockResult> schedBlock;
	private String archiveUid;
	private String code;
	
	public String getOriginalId() {
		return originalId;
	}

	public void setOriginalId(String originalId) {
		this.originalId = originalId;
	}
	
	public ObservationProject(){

	}

    public double getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(double executionTime) {
        this.executionTime = executionTime;
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
    
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public long getScienceRank() {
		return scienceRank;
	}

	public void setScienceRank(long scienceRank) {
		this.scienceRank = scienceRank;
	}

	public float getScienceScore() {
		return scienceScore;
	}

	public void setScienceScore(float scienceScore) {
		this.scienceScore = scienceScore;
	}

	public String getGrade(){
		return this.grade;
	}

	public void setGrade(String grade){
		this.grade = grade;
	}

	public String getArchiveUid() {
		return archiveUid;
	}

	public void setArchiveUid(String archiveUid) {
		this.archiveUid = archiveUid;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
