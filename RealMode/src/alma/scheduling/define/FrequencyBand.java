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
 * File FrequencyBand.java
 */
 
package alma.scheduling.define;

/**
 * The FrequencyBand class specifies the name and the range of frequencies
 * in a particular frequency band.  
 * 
 * @version 1.30 May 10, 2004
 * @author Allen Farris
 */
public class FrequencyBand {
	private String name;
	private double lowFrequency;
	private double highFrequency;

	/**
	 * Create a Frequency Band.
	 * @param name The name of this frequency band.
	 * @param lowFrequency The lowest frequency in this band in GHz.
	 * @param highFrequency The highest frequency in this band in GHz.
	 */
	public FrequencyBand(String name, double lowFrequency, double highFrequency) {
		this.name = name;
		this.lowFrequency = lowFrequency;
		this.highFrequency = highFrequency;
	}

	/**
	 * Return true if and only if the specified frequency band is equal to this band.
	 * @param band
	 * @return
	 */
	public boolean equals(FrequencyBand band) {
		return name == band.name;
	}

	/**
	 * Get the highest frequency in this band.
	 * @return The highest frequency in this band in GHZ.
	 */
	public double getHighFrequency() {
		return highFrequency;
	}

	/**
	 * Get the lowest frequency in this band.
	 * @return The lowest frequency in this band in GHz.
	 */
	public double getLowFrequency() {
		return lowFrequency;
	}

	/**
	 * Get the name of this frequency band.
	 * @return The name of this frequency band.
	 */
	public String getName() {
		return name;
	}

}
