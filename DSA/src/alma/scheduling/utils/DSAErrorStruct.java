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
package alma.scheduling.utils;

public class DSAErrorStruct {

	/**
	 * Part of DSA which return Error
	 */
	private String DSAPart;
	/**
	 * ALMA Entity ID with problems
	 */
	private String EntityId;
	/**
	 * Entity Type (ex. SchedBlock, ObsProject, ObsUnitSet, etc)
	 */
	private String EntityType;
	/**
	 * Exception returned by the DSA part
	 */
	private Throwable Exception;
	
	public DSAErrorStruct(String DSAPart, String entityId, String entityType,
			Throwable exception) {
		super();
		this.DSAPart = DSAPart;
		EntityId = entityId;
		EntityType = entityType;
		Exception = exception;
	}

	public String getDSAPart() {
		return DSAPart;
	}
	public String getEntityId() {
		return EntityId;
	}
	public String getEntityType() {
		return EntityType;
	}
	public Throwable getException() {
		return Exception;
	}
	
}
