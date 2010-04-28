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
 * "@(#) $Id: PIMembership.java,v 1.10 2010/04/28 19:23:03 ahoffsta Exp $"
 */
package alma.scheduling.datamodel.executive;

/**
 * The Principal Investigator (PI) Executive membership.
 * <P>
 * A PI can be associated to multiple Executives. Each SchedBlock specifies its PI.
 * <P>
 * This parameter is important because the PI is used to decide how much
 * observation time should be charged to each Executive every time a SchedBlock is
 * executed.
 */
public class PIMembership {

    /** 
     * Percentages to be used to distribute the total observation time between
     * the PI's associated Executives.
     */
	private float membershipPercentage;
	
	/** Executive */
	private Executive executive;

	/**
	 * Zero-arg constructor.
	 */
	public PIMembership() { }

    // --- Getters and Setters ---

	public float getMembershipPercentage() {
        return membershipPercentage;
    }

    public void setMembershipPercentage(float membershipPercentage) {
        this.membershipPercentage = membershipPercentage;
    }

    public Executive getExecutive() {
        return executive;
    }

    public void setExecutive(Executive mExecutive) {
        executive = mExecutive;
    }

}