package alma.scheduling.master.util;

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
