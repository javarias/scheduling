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
package alma.scheduling.algorithm;

import java.util.ArrayList;
import java.util.UUID;

public class PoliciesContainer {
	
	private boolean isSystemContainer;
	private UUID uuid;
	private String hostname;
	private String path;
	private int locked;
	private final ArrayList<String> policies;
	
	public PoliciesContainer(String hostname, String path) {
		this(hostname, path, false);
	}
	
	public PoliciesContainer(String hostname, String path, boolean systemPolicies) {
		uuid = UUID.randomUUID();
		this.hostname = hostname;
		this.path = path;
		this.locked = 0;
		this.policies = new ArrayList<String>();
		isSystemContainer = systemPolicies;
	}
	
	public UUID getUuid() {
		return uuid;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public String getPath() {
		return path;
	}
	
	public void lock() {
		synchronized(this) {
			locked++;
		}
	}
	
	public void unlock() {
		synchronized(this) {
			locked--;
		}
	}
	
	public boolean isLocked() {
		synchronized(this) {
			return (isSystemContainer) ? true: (locked > 0) ? true : false;
		}
	}
	
	public ArrayList<String> getPolicies() {
		return policies;
	}
	
	public String[] getPoliciesAsArray() {
		String[] retVal= new String[policies.size()];
		policies.toArray(retVal);
		return retVal;
	}
	
}
