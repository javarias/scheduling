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
 * File HasMembers.java
 */
 
package alma.scheduling.define;

/**
 * The HasMembers interface is a collection of methods that 
 * must be implemented by all objects that belong to a project
 * that can themselves contain members.  Such objects must
 * define and implement their own methods to add members.
 * 
 * @version 1.00  Sep 22, 2003
 * @author Allen Farris
 */
public interface HasMembers extends ArchiveEntity {

	/**
	 * Get the number of members in this set.
	 * @return The number of members in this set.
	 */	
	public int getNumberMembers();
	
	/**
	 * The member with the specified index.
	 * @param index The index of the member to be returned.
	 * @return The member wiht the specified index.
	 */
	public MemberOf getMember(int index);
	
	/**
	 * The member with the specified id.
	 * @param id The id of the member to be returned.
	 * @return The member wiht the specified id.
	 */
	public MemberOf getMember(String id);

	/**
	 * Get all the members of this set.
	 * @return The members of this set as an array of Objects.
	 */
	public MemberOf[] getMember();
	
}
