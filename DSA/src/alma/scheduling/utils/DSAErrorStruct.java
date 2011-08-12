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
