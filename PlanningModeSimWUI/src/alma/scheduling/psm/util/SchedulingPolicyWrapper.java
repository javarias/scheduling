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
package alma.scheduling.psm.util;

import alma.scheduling.SchedulingPolicyFile;

public class SchedulingPolicyWrapper {

	private final SchedulingPolicyFile file;
	private final String policyName;
	
	/**
	 * 
	 * @param file
	 * @param policyName
	 * @throws IllegalArgumentException if policyName doesn't exist in the policy file or if policyName is null 
	 */
	public SchedulingPolicyWrapper(SchedulingPolicyFile file, String policyName) 
			throws IllegalArgumentException{
		if (file == null || policyName == null)
			throw new IllegalArgumentException("Arguments cannot be null");
		this.file = file;
		this.policyName = policyName;
		for (String policy: file.schedulingPolicies) {
			if (policy.compareTo(policyName) == 0)
				return;
		}
		throw new IllegalArgumentException(policyName + 
				"does not exist in the policy file given as argument");
	}
	
	public String getSpringBeanName() {
		if (file.path.compareTo("system") == 0)
			return policyName;
		return "uuid" + file.uuid + "-" + policyName;
	}
	
	public String getUIName() {
		return file.hostname + ":" + file.path + ":" + policyName;
	}

	@Override
	public String toString() {
		return getUIName();
	}

	public String getFileUUID() {
		return file.uuid;
	}
}
