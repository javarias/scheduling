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
 * "@(#) $Id: PI.java,v 1.13 2010/09/03 16:47:05 javarias Exp $"
 */
package alma.scheduling.datamodel.executive;

import java.util.Set;

/**
 * The Principal Investigator (PI).
 */
public class PI {

    /** The PI's email */
    private String email;
    
    /** The PI's name */
	private String name;
	
	/** The PI Executive memberships */
	private Set<PIMembership> pIMembership;

	private Long id;
	/**
	 * Zero-arg constructor.
	 */
	public PI() { }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
	
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