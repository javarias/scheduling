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

	@Override
	public String toString() {
		return "AtmParameters [PWV=" + PWV + ", freq=" + freq + ", opacity="
				+ opacity + ", atmBrightnessTemp=" + atmBrightnessTemp + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((PWV == null) ? 0 : PWV.hashCode());
		result = prime
				* result
				+ ((atmBrightnessTemp == null) ? 0 : atmBrightnessTemp
						.hashCode());
		result = prime * result + ((freq == null) ? 0 : freq.hashCode());
		result = prime * result + ((opacity == null) ? 0 : opacity.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AtmParameters other = (AtmParameters) obj;
		if (PWV == null) {
			if (other.PWV != null)
				return false;
		} else if (!PWV.equals(other.PWV))
			return false;
		if (atmBrightnessTemp == null) {
			if (other.atmBrightnessTemp != null)
				return false;
		} else if (!atmBrightnessTemp.equals(other.atmBrightnessTemp))
			return false;
		if (freq == null) {
			if (other.freq != null)
				return false;
		} else if (!freq.equals(other.freq))
			return false;
		if (opacity == null) {
			if (other.opacity != null)
				return false;
		} else if (!opacity.equals(other.opacity))
			return false;
		return true;
	}
}
