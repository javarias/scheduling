package alma.scheduling.datamodel.observation;

import java.io.Serializable;
import java.util.List;

public class Session implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3327830924265296482L;
	private String entityUid;
	private String partUid;
	private String entityType;
	private List<ExecBlock> execBlocks;
	private CreatedArray array;
	
	public List<ExecBlock> getExecBlocks() {
		return execBlocks;
	}
	
	public void setExecBlocks(List<ExecBlock> execBlocks) {
		this.execBlocks = execBlocks;
	}
	
	public CreatedArray getArray() {
		return array;
	}
	
	public void setArray(CreatedArray array) {
		this.array = array;
	}

	public String getEntityUid() {
		return entityUid;
	}

	public void setEntityUid(String entityUid) {
		this.entityUid = entityUid;
	}

	public String getPartUid() {
		return partUid;
	}

	public void setPartUid(String partUid) {
		this.partUid = partUid;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((entityUid == null) ? 0 : entityUid.hashCode());
		result = prime * result + ((partUid == null) ? 0 : partUid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Session other = (Session) obj;
		if (entityUid == null) {
			if (other.entityUid != null)
				return false;
		} else if (!entityUid.equals(other.entityUid))
			return false;
		if (partUid == null) {
			if (other.partUid != null)
				return false;
		} else if (!partUid.equals(other.partUid))
			return false;
		return true;
	}
	
}
