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
 * "@(#) $Id: Executive.java,v 1.13 2010/04/09 01:26:15 rhiriart Exp $"
 */
package alma.scheduling.datamodel.executive;

import java.util.HashSet;
import java.util.Set;

/**
 * An Executive is an entity that has the right to observe in the ALMA telescope.
 */
public class Executive implements Comparable<Executive>{
    
    /**
     * Default percentage of the total telescope observation time for a season the
     * Executive has rights over.
     */
	private Float defaultPercentage;
	
	/**
	 * Executive name.
	 */
	private String name;
	
	/**
	 * The ExecutivePercentages link an Executive with the ObservingSeasons it has
	 * or will participate on, along with the percentages of the total observation time
	 * for the ObservingSeasons it has rights on.
	 */
	private Set<ExecutivePercentage> executivePercentage = new HashSet<ExecutivePercentage>();

	/**
	 * Zero-args constructor, required by Hibernate.
	 */
    public Executive() {}
	
    // --- Getters and Setters ---
    
    public Float getDefaultPercentage() {
        return defaultPercentage;
    }

    public void setDefaultPercentage(Float defaultPercentage) {
        this.defaultPercentage = defaultPercentage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<ExecutivePercentage> getExecutivePercentage() {
        return executivePercentage;
    }

    public void setExecutivePercentage(
            Set<ExecutivePercentage> mExecutivePercentage) {
        executivePercentage = mExecutivePercentage;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Executive)) {
			return false;
		}
		Executive other = (Executive) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(Executive o) {
		return name.compareTo(o.getName());
	}
}