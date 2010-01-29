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
 * "@(#) $Id: FieldSource.java,v 1.1 2010/01/29 21:50:49 rhiriart Exp $"
 */
package alma.scheduling.datamodel.obsproject;

public class FieldSource {

    private Long id;
    
    private String name;
    
    private SkyCoordinates coordinates;
    
    private String ephemeris;
        
    private Double pmRA;
    
    private Double pmDec;

    private FieldSourceObservability observability;
    
    public FieldSource() { }
        
    public FieldSource(String name, SkyCoordinates coordinates, Double pmRA,
            Double pmDec) {
        this.name = name;
        this.coordinates = coordinates;
        this.pmRA = pmRA;
        this.pmDec = pmDec;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SkyCoordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(SkyCoordinates coordinates) {
        this.coordinates = coordinates;
    }

    public String getEphemeris() {
        return ephemeris;
    }

    public void setEphemeris(String ephemeris) {
        this.ephemeris = ephemeris;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPmRA() {
        return pmRA;
    }

    public void setPmRA(Double pmRA) {
        this.pmRA = pmRA;
    }

    public Double getPmDec() {
        return pmDec;
    }

    public void setPmDec(Double pmDec) {
        this.pmDec = pmDec;
    }

    public FieldSourceObservability getObservability() {
        return observability;
    }

    public void setObservability(FieldSourceObservability observability) {
        this.observability = observability;
    }
    
}
