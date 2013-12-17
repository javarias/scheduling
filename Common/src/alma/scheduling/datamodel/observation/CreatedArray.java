package alma.scheduling.datamodel.observation;

import java.io.Serializable;
import java.util.Date;

import alma.scheduling.datamodel.obsproject.ArrayType;

public class CreatedArray implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5721954103529766917L;
	
	private String name;
	private Date creationDate;
	private Date destructionDate;
	private ArrayType arrayType;
	
	public CreatedArray() {
	}

	public CreatedArray(String name, Date creationDate, ArrayType arrayType) {
		super();
		this.name = name;
		this.creationDate = creationDate;
		this.arrayType = arrayType;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	public ArrayType getArrayType() {
		return arrayType;
	}

	public void setArrayType(ArrayType arrayType) {
		this.arrayType = arrayType;
	}
	
	public Date getDestructionDate() {
		return destructionDate;
	}

	public void setDestructionDate(Date destructionDate) {
		this.destructionDate = destructionDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((creationDate == null) ? 0 : creationDate.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		CreatedArray other = (CreatedArray) obj;
		if (creationDate == null) {
			if (other.creationDate != null)
				return false;
		} else if (!creationDate.equals(other.creationDate))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
