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
 * File MemberOf.java
 */
 
package alma.scheduling.planning_mode_sim.define;

/**
 * Description 
 * 
 * @version 1.00  Sep 22, 2003
 * @author Allen Farris
 */
public interface MemberOf extends ArchiveEntity {
	
	/**
	 * Get the index of this member;
	 * @return The index of the member as an int.
	 */
	public int getMemberIndex();
	
	/**
	 * Get the id of the project to which this member belongs.
	 * @return The id of the project to which this member belongs.
	 */
	public String getProjectId();
	
	/**
	 * Get the id of the parent to which this member belongs.
	 * @return The id of the parent to which this member belongs.
	 */
	public String getParentId();
	
	/**
	 * Set the index of this member;
	 * @parm index The index of this member.
	 */
	public void setMemberIndex(int index);
	
	/**
	 * Set the id of this member's project.
	 * @param id The id of this member's project.
	 */
	public void setProjectId(String id);
	
	/**
	 * Set the id of this member's parent.
	 * @param id The id of this member's parent.
	 */
	public void setParentId(String id);

}
