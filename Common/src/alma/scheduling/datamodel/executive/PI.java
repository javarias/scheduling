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
 * "@(#) $Id: PI.java,v 1.12 2010/04/09 20:52:02 rhiriart Exp $"
 */
package alma.scheduling.datamodel.executive;

import java.util.Set;

/**
 * The Principal Investigator (PI).
 */
public class PI {

    /** The PI's email, which is also used as the identifier. */
    private String email;
    
    /** The PI's name */
	private String name;
	
	/** The PI Executive memberships */
	private Set<PIMembership> pIMembership;

	/**
	 * Zero-arg constructor.
	 */
	public PI() { }

    // --- Getters and Setters ---

	public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<PIMembership> getPIMembership() {
        return pIMembership;
    }

    public void setPIMembership(Set<PIMembership> mPIMembership) {
        pIMembership = mPIMembership;
    }
}