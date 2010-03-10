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
 * "@(#) $Id: Antenna.java,v 1.1 2010/03/10 22:31:18 rhiriart Exp $"
 */
package alma.scheduling.datamodel.observatory;

import java.util.Date;

public class Antenna extends TelescopeEquipment {

    private Double diameter;
    
    private String antennaType;
    
    private Double effectiveCollectingArea;
    
    private Double systemTemperature;
    
    public Antenna() {
        super();
    }
    
    public Antenna(String name, Date commissionDate) {
        super(name, AssemblyGroupType.ANTENNA, commissionDate);
    }

    public Double getDiameter() {
        return diameter;
    }

    public void setDiameter(Double diameter) {
        this.diameter = diameter;
    }

    public String getAntennaType() {
        return antennaType;
    }

    public void setAntennaType(String antennaType) {
        this.antennaType = antennaType;
    }

    public Double getEffectiveCollectingArea() {
        return effectiveCollectingArea;
    }

    public void setEffectiveCollectingArea(Double effectiveCollectingArea) {
        this.effectiveCollectingArea = effectiveCollectingArea;
    }

    public Double getSystemTemperature() {
        return systemTemperature;
    }

    public void setSystemTemperature(Double systemTemperature) {
        this.systemTemperature = systemTemperature;
    }
}
