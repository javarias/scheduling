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
 * "@(#) $Id: SchedBlockControl.java,v 1.5 2010/09/09 17:54:48 javarias Exp $"
 */
package alma.scheduling.datamodel.obsproject;

import java.util.Date;

import alma.scheduling.datamodel.Updateable;

/**
 * Provides parameters to control the execution of SchedBlocks.
 *
 */
public class SchedBlockControl implements Updateable {

    /** When was the last update performed */
    private Date lastUpdate;
    
    /** Until when the last update is valid */
    private Date validUntil;
        
    /**
     * Whether this SB can executed an unlimited number of times. This is mostly used
     * for maintainance of comissioning SchedBlocks. On the other hand, SchedBlocks targeted
     * to perform scientific observations are usually executed until its sensitivity goal
     * has been reached.
     */
    private Boolean indefiniteRepeat;
    
    /**
     * The number of times to execute this SB.
     */
    private Integer executionCount;

    /**
     * SchedBlock state. All runnable SchedBlocks are in READY state. The SchedBlock
     * remains in this state until the sensitivity goal is achieved, or the maximum
     * execution time is reached. When either of these two conditions happen, the state
     * is FULLY_OBSERVED and the SchedBlock can't be executed again.
     */
    private SchedBlockState state;
    
    /** Total accumulated execution time (hours) */
    private Double accumulatedExecutionTime;
    
    /** Maximum time for a single execution of this SB (hours)*/
    private Double sbMaximumTime;
    
    /** Sensitivity achieved so far (Jy) */
    private Double achievedSensitivity;
    
    /**
     * How many times a SchedBlock has been executed so far. This field needs to be
     * updated every time the SchedBlock is executed. 
     */
    private Integer numberOfExecutions = 0;
    
    public SchedBlockControl() {
        // nothing here yet
    }

    @Override
    public Date getLastUpdate() {
        return lastUpdate;
    }

    @Override
    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public Date getValidUntil() {
        return validUntil;
    }

    @Override
    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public Boolean getIndefiniteRepeat() {
        return indefiniteRepeat;
    }

    public void setIndefiniteRepeat(Boolean indefiniteRepeat) {
        this.indefiniteRepeat = indefiniteRepeat;
    }

    public Integer getExecutionCount() {
        return executionCount;
    }

    public void setExecutionCount(Integer executionCount) {
        this.executionCount = executionCount;
    }

    public SchedBlockState getState() {
        return state;
    }

    public void setState(SchedBlockState state) {
        this.state = state;
    }

    public Double getAchievedSensitivity() {
        return achievedSensitivity;
    }

    public void setAchievedSensitivity(Double achievedSensitivity) {
        this.achievedSensitivity = achievedSensitivity;
    }

    public Double getAccumulatedExecutionTime() {
        return accumulatedExecutionTime;
    }

    public void setAccumulatedExecutionTime(Double accumulatedExecutionTime) {
        this.accumulatedExecutionTime = accumulatedExecutionTime;
    }

    /**
     * 
     * @see #sbMaximumTime
     * @return
     */
    public Double getSbMaximumTime() {
        return sbMaximumTime;
    }

    public void setSbMaximumTime(Double sbMaximumTime) {
        this.sbMaximumTime = sbMaximumTime;
    }

    public Integer getNumberOfExecutions() {
        return numberOfExecutions;
    }

    public void setNumberOfExecutions(Integer numberOfExecutions) {
        this.numberOfExecutions = numberOfExecutions;
    }
    
    
}
