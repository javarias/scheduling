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
 * "@(#) $Id: WeatherConstraints.java,v 1.1 2010/01/29 21:50:49 rhiriart Exp $"
 */
package alma.scheduling.datamodel.obsproject;

public class WeatherConstraints {

    /** Maximum wind velocity (m/s) */
    Double maxWindVelocity;
    
    /** Maximum opacity (nepers) */
    Double maxOpacity;
    
    /** Minimum phase stability (m, measured as delta path length) */
    Double minPhaseStability;
    
    /** Maximum seeing (arcseconds) */
    Double maxSeeing;

    public WeatherConstraints() { }
    
    public WeatherConstraints(Double maxWindVelocity, Double maxOpacity,
            Double minPhaseStability, Double minSeeing) {
        this.maxWindVelocity = maxWindVelocity;
        this.maxOpacity = maxOpacity;
        this.minPhaseStability = minPhaseStability;
        this.maxSeeing = minSeeing;
    }

    public Double getMaxWindVelocity() {
        return maxWindVelocity;
    }

    public void setMaxWindVelocity(Double maxWindVelocity) {
        this.maxWindVelocity = maxWindVelocity;
    }

    public Double getMaxOpacity() {
        return maxOpacity;
    }

    public void setMaxOpacity(Double maxOpacity) {
        this.maxOpacity = maxOpacity;
    }

    public Double getMinPhaseStability() {
        return minPhaseStability;
    }

    public void setMinPhaseStability(Double minPhaseStability) {
        this.minPhaseStability = minPhaseStability;
    }

    public Double getMaxSeeing() {
        return maxSeeing;
    }

    public void setMaxSeeing(Double maxSeeing) {
        this.maxSeeing = maxSeeing;
    }
    
}
