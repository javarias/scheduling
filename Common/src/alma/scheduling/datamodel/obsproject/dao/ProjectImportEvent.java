package alma.scheduling.datamodel.obsproject.dao;

import java.util.Date;

/**
 * 
 * This class represents a notification event for import status (import to the Scheduling Working DataBase),
 * for each imported object into SWDB should be fired an event containing this class. </br> 
 * 
 * This class must be used internally in Scheduling subsystem only .
 * 
 * @author javarias
 *
 */
public class ProjectImportEvent {

	private ImportStatus status;
	private Date timestamp;
	private String entityId;
	private String entityType;
	private String details;
	
	public ImportStatus getStatus() {
		return status;
	}

	public void setStatus(ImportStatus status) {
		this.status = status;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(java.util.Date date) {
		this.timestamp = date;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public enum ImportStatus{
		STATUS_INFO,
		STATUS_OK,
		STATUS_WARNING,
		STATUS_ERROR
	}
}
