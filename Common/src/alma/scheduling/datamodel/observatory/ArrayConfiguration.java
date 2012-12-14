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
 * "@(#) $Id: ArrayConfiguration.java,v 1.4 2012/12/14 18:41:29 javarias Exp $"
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
    private String arrayName;
    private String configurationName;
    /**
     * Used by array configuration lite version. In the future this should be replaced by
     * a complete list of antennas saved in {@link ArrayConfiguration#antennaInstallations}
     */
    private Integer numberOfAntennas;
    private Double minBaseline;
    private Double maxBaseline;
    private Double antennaDiameter;

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

	public String getArrayName() {
		return arrayName;
	}

	public void setArrayName(String arrayName) {
		this.arrayName = arrayName;
	}

	public String getConfigurationName() {
		return configurationName;
	}

	public void setConfigurationName(String configurationName) {
		this.configurationName = configurationName;
	}

	/**
	 * @see ArrayConfiguration#numberOfAntennas
	 * @param numberOfAntennas
	 */
	public Integer getNumberOfAntennas() {
		return numberOfAntennas;
	}

	/**
	 * @see ArrayConfiguration#numberOfAntennas
	 * @param numberOfAntennas
	 */
	public void setNumberOfAntennas(Integer numberOfAntennas) {
		this.numberOfAntennas = numberOfAntennas;
	}

	public Double getMinBaseline() {
		return minBaseline;
	}

	public void setMinBaseline(Double minBaseline) {
		this.minBaseline = minBaseline;
	}

	public Double getMaxBaseline() {
		return maxBaseline;
	}

	public void setMaxBaseline(Double maxBaseline) {
		this.maxBaseline = maxBaseline;
	}

	public Double getAntennaDiameter() {
		return antennaDiameter;
	}

	public void setAntennaDiameter(Double antennaDiameter) {
		this.antennaDiameter = antennaDiameter;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ArrayConfiguration) {
			ArrayConfiguration param = (ArrayConfiguration) obj;
			if (this.id == param.getId())
				return true;
		}
		return false;
	}
    
	
}
