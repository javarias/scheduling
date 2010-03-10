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
 * "@(#) $Id: AssemblyContainerState.java,v 1.1 2010/03/10 22:31:18 rhiriart Exp $"
 */
package alma.scheduling.datamodel.observatory;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import alma.scheduling.datamodel.Updateable;

public class AssemblyContainerState implements Updateable {

    private Date lastUpdate;
    
    private Date validUntil;
    
    /** Current installed assemblies */
    private Set<AssemblyType> assemblies = new HashSet<AssemblyType>();
    
    public AssemblyContainerState() {
    }
    
    @Override
    public Date getLastUpdate() {
        return lastUpdate;
    }

    @Override
    public Date getValidUntil() {
        return validUntil;
    }

    @Override
    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public Set<AssemblyType> getAssemblies() {
        return assemblies;
    }

    public void setAssemblies(Set<AssemblyType> assemblies) {
        this.assemblies = assemblies;
    }

    public boolean supportsFrequency(double frequency) {
        return true;
    }
    
}
