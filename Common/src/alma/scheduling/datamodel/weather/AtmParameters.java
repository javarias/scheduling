/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
package alma.scheduling.datamodel.weather;

public class AtmParameters {

    /** Surrogate identifier */
    Long id;
    
    /** Precipitable water vapor content (mm) */
    Double PWV;

    /** Frequency (GHz) */
    Double freq;
    
    /** Opacity (nepers) */
    Double opacity;
    
    /** Atmospheric brightness temperature (K) */
    Double atmBrightnessTemp;
        
    public AtmParameters() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getPWV() {
        return PWV;
    }

    public void setPWV(Double pWV) {
        this.PWV = pWV;
    }

    public Double getFreq() {
        return freq;
    }

    public void setFreq(Double freq) {
        this.freq = freq;
    }

    public Double getOpacity() {
        return opacity;
    }

    public void setOpacity(Double opacity) {
        this.opacity = opacity;
    }

    public Double getAtmBrightnessTemp() {
        return atmBrightnessTemp;
    }

    public void setAtmBrightnessTemp(Double atmBrightnessTemp) {
        this.atmBrightnessTemp = atmBrightnessTemp;
    }
}
