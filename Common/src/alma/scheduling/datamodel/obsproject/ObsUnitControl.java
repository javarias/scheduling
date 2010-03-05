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
 * "@(#) $Id: ObsUnitControl.java,v 1.1 2010/03/05 16:42:29 rhiriart Exp $"
 */
package alma.scheduling.datamodel.obsproject;

import java.util.Date;

import alma.scheduling.datamodel.Updateable;

/**
 * Execution control parameters for any ObsUnit.
 * 
 */
public class ObsUnitControl implements Updateable {

    /** When was the last update performed */
    private Date lastUpdate;
    
    /** Until when the last update is valid */
    private Date validUntil;
    
    /** The maximum time that the ObsUnit is allowed to run (hours) */
    private Double maximumTime;

    /** Extimated execution time (hours) */
    private Double estimatedExecutionTime;
    
    /** The type of the array that has been requested */
    private ArrayType arrayRequested;
    
    /** Priority assigned by the TAC */
    private Integer tacPriority;

    public ObsUnitControl() {
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

    public Double getMaximumTime() {
        return maximumTime;
    }

    public void setMaximumTime(Double maximumTime) {
        this.maximumTime = maximumTime;
    }

    public Double getEstimatedExecutionTime() {
        return estimatedExecutionTime;
    }

    public void setEstimatedExecutionTime(Double estimatedExecutionTime) {
        this.estimatedExecutionTime = estimatedExecutionTime;
    }

    public ArrayType getArrayRequested() {
        return arrayRequested;
    }

    public void setArrayRequested(ArrayType arrayRequested) {
        this.arrayRequested = arrayRequested;
    }

    public Integer getTacPriority() {
        return tacPriority;
    }

    public void setTacPriority(Integer tacPriority) {
        this.tacPriority = tacPriority;
    }
}
