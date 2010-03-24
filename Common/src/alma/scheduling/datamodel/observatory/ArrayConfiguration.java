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
 * "@(#) $Id: ArrayConfiguration.java,v 1.1 2010/03/10 22:31:18 rhiriart Exp $"
 */
package alma.scheduling.datamodel.observatory;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ArrayConfiguration {

    private Long id;
    
    private Date startTime;
    
    private Date endTime;
    
    private Set<AntennaInstallation> antennaInstallations = new HashSet<AntennaInstallation>();
    
    private Double resolution;
    
    private Double uvCoverage;

    public ArrayConfiguration() {
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Set<AntennaInstallation> getAntennaInstallations() {
        return antennaInstallations;
    }

    public void setAntennaInstallations(
            Set<AntennaInstallation> antennaInstallations) {
        this.antennaInstallations = antennaInstallations;
    }

    public Double getResolution() {
        return resolution;
    }

    public void setResolution(Double resolution) {
        this.resolution = resolution;
    }

    public Double getUvCoverage() {
        return uvCoverage;
    }

    public void setUvCoverage(Double uvCoverage) {
        this.uvCoverage = uvCoverage;
    }
}