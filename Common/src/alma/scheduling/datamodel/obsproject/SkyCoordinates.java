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
 * "@(#) $Id: SkyCoordinates.java,v 1.3 2011/07/29 21:55:25 dclarke Exp $"
 */
package alma.scheduling.datamodel.obsproject;

import java.util.Date;

import alma.scheduling.algorithm.astro.Constants;
import alma.scheduling.algorithm.astro.CoordinatesUtil;

public class SkyCoordinates {

    /** Rigth ascension (degrees) */
    private Double RA;
    
    /** Declination (degrees) */
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
    
    /*
     * ================================================================
     * Utilities
     * ================================================================
     */
    /**
     * Get our hour angle at Chajnantor for the given UTC time.
     * 
     * @param when (Date) - the UTC time for which we want the hour angle
     * @return hour angle (decimal hours, [0:24))
     */
    public double getHourAngle(Date when) {
        return CoordinatesUtil.getHourAngle(
        		when,
        		getRA() * 24.0 / 360.0,
        		Constants.CHAJNANTOR_LONGITUDE);
    }
    
    /**
     * Get our current hour angle at Chajnantor.
     * 
     * @return hour angle (decimal hours)
     */
    public double getHourAngle() {
        return getHourAngle(new Date()); // TODO: Make this UTC!
    }
    
    /**
     * Get our elevation at Chajnantor for the given UTC time.
     * 
     * @param when (Date) - the UTC time for which we want the elevation
     * @return elevation (in degrees, [0:360))
     */
    public double getElevation(Date when) {
    	final HorizonCoordinates h = CoordinatesUtil.equatorialToHorizon(
    			this,
    			when,
    			Constants.CHAJNANTOR_LATITUDE,
    			Constants.CHAJNANTOR_LONGITUDE);

        return h.getAltitude();
    }
    
    /**
     * Get our current elevation at Chajnantor.
     * 
     * @return elevation (in degrees, [0:360))
     */
    public double getElevation() {
        return getElevation(new Date()); // TODO: Make this UTC!
    }
    
    /**
     * Get our azimuth at Chajnantor for the given UTC time.
     * 
     * @param when (Date) - the UTC time for which we want the azimuth
     * @return azimuth (in degrees, [0:360))
     */
    public double getAzimuth(Date when) {
    	final HorizonCoordinates h = CoordinatesUtil.equatorialToHorizon(
    			this,
    			when,
    			Constants.CHAJNANTOR_LATITUDE,
    			Constants.CHAJNANTOR_LONGITUDE);

        return h.getAzimuth();
    }
    
    /**
     * Get our current azimuth at Chajnantor.
     * 
      * @return azimuth (in degrees, [0:360))
     */
    public double getAzimuth() {
        return getAzimuth(new Date()); // TODO: Make this UTC!
    }
    /* End Utilities
     * ============================================================= */
    
}
