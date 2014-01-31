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
package alma.scheduling.datamodel.obsproject;

public class ObsProject {
    
    /** ALMA Archive unique identifier*/
    private String uid;
    
    /**
     * The project code - a user friendly, unique, identification code for
     * the project. To be assigned by the Observatory on proposal submission.
     * Intended to be read-only for the user, but at the moment is writable for
     * AIV/CSV use.
     */
    private String code;
    
	/** The user-entered name of the Project. To be used as a mnemonic for the user */
    private String name;
    
	/** The user-entered version of the Project. */
    private String version;
    
    /**
     * The principal investigator (PI).
     * <P>
     * SchedBlocks are associated with an Executive by means of the PI
     * membership.
     */
    private String principalInvestigator;
    
    /**
     * This field is equivalent to Executive Name, used to simplify the
     * conversion process from an APDMProject into an ObsProject
     */
    private String affiliation;
    
    /**
     * Scientific score, assigned by the different ALMA Project Review
     * Commitees (APRC).
     * <P>
     * The APRCs are specialized on different scientific fields
     * (galactic, extra-galactic, etc.). Scores assigned by different
     * APRCs to different projects could collide. In order to construct
     * a totally ordered list of ObsProjects, these collisions needs to be
     * resolved, which is done by means of the scienceRank, assigned by a
     * central committee.
     */
    private Float scienceScore;
    
    /**
     * Scientific Rank. The rank applies a total order over the set
     * of ObsProjects (for an Observing Season, I guess).
     */
    private Integer scienceRank;
    
    /**
     * Letter Grade
     */
    private ScienceGrade letterGrade;
    
    /**
     * The Observation Unit, which could be a SchedBlock, or a set of SchedBlocks, 
     * or Program.
     */
    private ObsUnit obsUnit;
    
    /**
     * Total execution time, accumulated from the execution times
     * of all the SchedBlock in the ObsUnitSet.
     */
    private Double totalExecutionTime;
    
    
    /**
     * Is this a commissioning project or not?
     */
    private boolean csv;
    
    /**
     * Is this a manual project or not?
     */
    private boolean manual;
    // --- constructors ---
    
    private ObservationStatus status;
    
    public ObsProject() { }

    // --- setters/getters ---

    public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
    
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
    
    public String getPrincipalInvestigator() {
        return principalInvestigator;
    }

    public void setPrincipalInvestigator(String principalInvestigator) {
        this.principalInvestigator = principalInvestigator;
    }

    public ObservationStatus getStatus() {
        return status;
    }

    public void setStatus(ObservationStatus status) {
        this.status = status;
    }

    public ObsUnit getObsUnit() {
        return obsUnit;
    }

    public void setObsUnit(ObsUnit obsUnit) {
        this.obsUnit = obsUnit;
    }

    public Double getTotalExecutionTime() {
        return totalExecutionTime;
    }

    public void setTotalExecutionTime(Double totalExecutionTime) {
        this.totalExecutionTime = totalExecutionTime;
    }

    public Float getScienceScore() {
        return scienceScore;
    }

    public void setScienceScore(Float scienceScore) {
        this.scienceScore = scienceScore;
    }

    public Integer getScienceRank() {
        return scienceRank;
    }

    public void setScienceRank(Integer scienceRank) {
        this.scienceRank = scienceRank;
    }

    public ScienceGrade getLetterGrade() {
        return letterGrade;
    }

    public void setLetterGrade(ScienceGrade letterGrade) {
        this.letterGrade = letterGrade;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
    
    public String getAffiliation() {
		return affiliation;
	}

	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}

	public boolean getCsv() {
		return csv;
	}

	public void setCsv(boolean csv) {
		this.csv = csv;
	}

	public boolean getManual() {
		return manual;
	}

	public void setManual(boolean manual) {
		this.manual = manual;
	}

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uid == null) ? 0 : uid.hashCode());
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
		if (!(obj instanceof ObsProject)) {
			return false;
		}
		ObsProject other = (ObsProject) obj;
		if (uid == null) {
			if (other.uid != null) {
				return false;
			}
		} else if (!uid.equals(other.uid)) {
			return false;
		}
		return true;
	}

	/*
     * ================================================================
     * Extensions
     * ================================================================
     */
	public void ident(StringBuilder buffer) {
		buffer.append(getCode());
		buffer.append(" (");
		buffer.append(getUid());
		buffer.append(")");
	}
	
	public String ident() {
		final StringBuilder buffer = new StringBuilder();
		ident(buffer);
		return buffer.toString();
	}
	/* End Extensions
	 * ============================================================= */
}
