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
 * "@(#) $Id: SkyCoordinates.java,v 1.1 2010/01/29 21:50:49 rhiriart Exp $"
 */
package alma.scheduling.datamodel.obsproject;

public class SkyCoordinates {

    /** Rigth ascension (radians) */
    private Double RA;
    
    /** Declination (radians) */
    private Double Dec;

    public SkyCoordinates() { }
    
    public SkyCoordinates(Double rA, Double dec) {
        RA = rA;
        Dec = dec;
    }

    public Double getRA() {
        return RA;
    }

    public void setRA(Double rA) {
        RA = rA;
    }

    public Double getDec() {
        return Dec;
    }

    public void setDec(Double dec) {
        Dec = dec;
    }
    
    
}
