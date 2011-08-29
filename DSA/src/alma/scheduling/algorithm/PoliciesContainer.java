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
