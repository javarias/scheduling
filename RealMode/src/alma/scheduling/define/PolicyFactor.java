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
 * File PolicyFactor.java
 */
 
package alma.scheduling.define;

/**
 * An PolicyFactor defines a factor used in a dynamic scheduling
 * algorithm to make its decisions.  It includes the name, a
 * weighting factor, and a definition.
 * 
 * @version 1.30 May 10, 2004
 * @author Allen Farris
 */
public class PolicyFactor {
	private String name;
	private double weight;
	private String definition;
	

	public PolicyFactor() {
	}

	/**
	 * @return
	 */
	public String getDefinition() {
		return definition;
	}

	/**
	 * @return
	 */
	public double getWeight() {
		return weight;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param string
	 */
	public void setDefinition(String string) {
		definition = string;
	}

	/**
	 * @param d
	 */
	public void setWeight(double d) {
		weight = d;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

}
