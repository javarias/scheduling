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
 * "@(#) $Id: ObsUnit.java,v 1.5 2010/08/18 16:15:21 dclarke Exp $"
 */
package alma.scheduling.datamodel.obsproject;

public abstract class ObsUnit {

    /** Identifier, a surrogate database key */
    private Long id;
    
    /** ALMA Archive unique identifier*/
    private String uid;

    private ObsUnitSet parent;

    private ObsUnitControl obsUnitControl;
    
    /**
     * In the case of the root ObsUnitSet, this is a reference to the
     * ObsProject. Null otherwise.
     */
    private ObsProject project;
    
    /**
     * This is the Entity Id of the ObsProject
     */
    private String projectUid;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ObsUnitSet getParent() {
        return parent;
    }

    public void setParent(ObsUnitSet parent) {
        this.parent = parent;
    }

    public ObsUnitControl getObsUnitControl() {
        return obsUnitControl;
    }

    public void setObsUnitControl(ObsUnitControl obsUnitControl) {
        this.obsUnitControl = obsUnitControl;
    }

    public ObsProject getProject() {
        return project;
    }

    public void setProject(ObsProject project) {
        this.project = project;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

	public String getProjectUid() {
		return projectUid;
	}

	public void setProjectUid(String projectUid) {
		this.projectUid = projectUid;
	}
    
}
