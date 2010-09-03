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
 * "@(#) $Id: AssemblyContainer.java,v 1.2 2010/09/03 16:47:05 javarias Exp $"
 */
package alma.scheduling.datamodel.observatory;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class AssemblyContainer {

    private Long id;
    
    private AssemblyGroupType type;
    
    private Date commissionDate;

    private AssemblyContainer parent;
    
    private AssemblyContainerState state;
    
    private Set<AssemblyContainerOperation> operations = new HashSet<AssemblyContainerOperation>();
    
    public AssemblyContainer() {
    }
    
    public AssemblyContainer(AssemblyGroupType type, Date commissionDate) {
        this.type = type;
        this.commissionDate = commissionDate;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AssemblyGroupType getType() {
        return type;
    }

    public void setType(AssemblyGroupType type) {
        this.type = type;
    }

    public Date getCommissionDate() {
        return commissionDate;
    }

    public void setCommissionDate(Date commissionDate) {
        this.commissionDate = commissionDate;
    }

    public AssemblyContainer getParent() {
        return parent;
    }

    public void setParent(AssemblyContainer parent) {
        this.parent = parent;
    }

    public Set<AssemblyContainerOperation> getOperations() {
        return operations;
    }

    public void setOperations(Set<AssemblyContainerOperation> operations) {
        this.operations = operations;
    }

    public AssemblyContainerState getState() {
        return state;
    }

    public void setState(AssemblyContainerState state) {
        this.state = state;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (id == null) return false;
        if ( !(obj instanceof AssemblyContainer)) return false;
        final AssemblyContainer that = (AssemblyContainer) obj;
        return this.id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return id == null ? System.identityHashCode(this) : id.hashCode();
    }
}
