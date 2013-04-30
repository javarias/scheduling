/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 */
package alma.scheduling.datamodel.obsproject;

import java.util.HashSet;
import java.util.Set;

import alma.entity.xmlbinding.ousstatus.OUSStatusEntityT;

public class ObsUnitSet extends ObsUnit {

	private String entityPartId;
	
    private Set<ObsUnit> obsUnits = new HashSet<ObsUnit>();
    
    private OUSStatusEntityT statusEntity;
    
    public Set<ObsUnit> getObsUnits() {
        return obsUnits;
    }

    public void setObsUnits(Set<ObsUnit> obsUnits) {
        this.obsUnits = obsUnits;
    }
    
    public void addObsUnit(ObsUnit obsUnit) {
        obsUnit.setParent(this);
        obsUnits.add(obsUnit);
    }

    public OUSStatusEntityT getStatusEntity() {
        return statusEntity;
    }

    public void setStatusEntity(OUSStatusEntityT statusEntity) {
        this.statusEntity = statusEntity;
    }

	public String getEntityPartId() {
		return entityPartId;
	}

	public void setEntityPartId(String entityPartId) {
		this.entityPartId = entityPartId;
	}

	@Override
	public int hashCode() {
		if (this.getUid() == null || this.getEntityPartId() == null)
			return super.hashCode();
		String concatId = this.getUid() + this.getEntityPartId();
		return concatId.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ObsUnitSet))
			return false;
		ObsUnitSet ous = (ObsUnitSet) obj;
		if (this.getUid() == null || this.getEntityPartId() == null)
			return super.equals(obj);
		if (this.getProjectUid().compareTo(ous.getProjectUid()) == 0 &&
				this.getEntityPartId().compareTo(ous.getEntityPartId()) == 0)
			return true;
		return false;
	}
	
}
