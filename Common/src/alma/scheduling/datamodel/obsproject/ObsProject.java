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
 * "@(#) $Id: ObsProject.java,v 1.5 2010/05/14 19:34:42 dclarke Exp $"
 */
package alma.scheduling.datamodel.obsproject;

public class ObsProject {

    /** Surrogate identifier */
    private Long Id;
    
    /**
     * The principal investigator (PI).
     * <P>
     * SchedBlocks are associated with an Executive by means of the PI
     * membership.
     */
    private String principalInvestigator;
    
    /**
     * Scientific score, assigned by the different ALMA Project Review
     * Commitees (APRC).
     * <P>
     * The APRCs are specialized on different scientific fields
     * (galactic, extra-galactic, etc.). Scores assigned by different
     * APRCs to different projects could collide. In order to construct
     * a totally ordered list of ObsProjects, these collisions needs to be
     * resolved, which is done by means of the scienceRank, assigned by a
     * central commitee.
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
    private String letterGrade;
    
    /** The ObsProject status */
    private String status;
    
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
    
    // --- constructors ---
    
    public ObsProject() { }

    // --- setters/getters ---
    
    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getPrincipalInvestigator() {
        return principalInvestigator;
    }

    public void setPrincipalInvestigator(String principalInvestigator) {
        this.principalInvestigator = principalInvestigator;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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

    public String getLetterGrade() {
        return letterGrade;
    }

    public void setLetterGrade(String letterGrade) {
        this.letterGrade = letterGrade;
    }
}
