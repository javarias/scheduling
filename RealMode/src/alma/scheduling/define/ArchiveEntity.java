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
 * File ArchiveEntity.java
 */
 
package alma.scheduling.define;

/**
 * The ArchiveEntity interface defines common attributes that all entries 
 * into the archive must have. 
 * 
 * @version 1.00  Sep 22, 2003
 * @author Allen Farris
 */
public interface ArchiveEntity {

	/**
	 * Get the archive identifier.
	 * @return The archive identifier as a String.
	 */
	public String getId();
	
	/**
	 * Get the time this archive entry was created.
	 * @return The time this archive entry was created as an STime.
	 */
	public STime getTimeCreated();
	
	/**
	 * Get the time this archive entry was last updated.
	 * @return The time this archive entry was last updated as an STime.
	 */
	public STime getTimeUpdated();
	
	/**
	 * Set the archive identifier.
	 * @param id The id of this archive entity.
	 */
	public void setId(String id);
	
	/**
	 * Set the time this archive entry was created.
	 * @param t The time this archive entry was created.
	 */
	public void setTimeCreated(STime t);
	
	/**
	 * Set the time this archive entry was last updated.
	 * @param t The time this archive entry was last updated.
	 */
	public void setTimeUpdated(STime t);

}
