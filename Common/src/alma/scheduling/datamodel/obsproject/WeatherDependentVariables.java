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
 * "@(#) $Id: WeatherDependentVariables.java,v 1.4 2011/08/30 23:05:00 javarias Exp $"
 */
package alma.scheduling.datamodel.obsproject;

import java.util.Date;

import alma.scheduling.datamodel.Updateable;

public class WeatherDependentVariables implements Updateable {

    /** Time of last update */
    Date lastUpdate;
    
    /** Time when the values herein stop being valid */
    Date validUntil;
    
    /** System temperature (K) */
    Double tsys;

    /** Projected system temperature (K) */
    Double projectedTsys;
    
    /** Projection time increment (hours) */
    Double projectionTimeIncr;
    
    /** zenith System Temperature (K) */
    Double zenithTsys;
    
    /** Opacity (nepers)*/
    Double opacity;
    
    /** Opacity at Zenith (nepers)*/
    Double zenithOpacity;

	public WeatherDependentVariables() { }
    
    @Override
    public Date getLastUpdate() {
        return lastUpdate;
    }

    @Override
    public Date getValidUntil() {
        return validUntil;
    }

    @Override
    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public Double getTsys() {
        return tsys;
    }

    public void setTsys(Double tsys) {
        this.tsys = tsys;
    }

    public Double getProjectedTsys() {
        return projectedTsys;
    }

    public void setProjectedTsys(Double projectedTsys) {
        this.projectedTsys = projectedTsys;
    }

    public Double getProjectionTimeIncr() {
        return projectionTimeIncr;
    }

    public void setProjectionTimeIncr(Double projectionTimeIncr) {
        this.projectionTimeIncr = projectionTimeIncr;
    }

	public Double getZenithTsys() {
		return zenithTsys;
	}

	public void setZenithTsys(Double currTsys) {
		this.zenithTsys = currTsys;
	}

	public Double getOpacity() {
		return opacity;
	}

	public void setOpacity(Double opacity) {
		this.opacity = opacity;
	}
    
    public Double getZenithOpacity() {
		return zenithOpacity;
	}

	public void setZenithOpacity(Double zenithOpacity) {
		this.zenithOpacity = zenithOpacity;
	}
}
