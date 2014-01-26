package alma.scheduling;

public class SchedulingPolicyFile {

	public String uuid;
	public String hostname;
	public String path;
	public boolean locked;
	public String[] schedulingPolicies;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SchedulingPolicyFile)) {
			return false;
		}
		SchedulingPolicyFile other = (SchedulingPolicyFile) obj;
		if (uuid == null) {
			if (other.uuid != null) {
				return false;
			}
		} else if (!uuid.equals(other.uuid)) {
			return false;
		}
		return true;
	}
	
}
