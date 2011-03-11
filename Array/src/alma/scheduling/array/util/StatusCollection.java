/* ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2010
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 */

package alma.scheduling.array.util;

import java.util.HashMap;
import java.util.Map;

import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.projectstatus.StatusBaseT;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.scheduling.SchedulingException;

/**
 * A collating object which holds all the status entities for an
 * ObsProject, its contained ObsUnitSets and its associated SchedBlocks
 *  
 * @author dclarke
 * $Id: StatusCollection.java,v 1.1 2011/03/11 00:06:34 dclarke Exp $
 */
public class StatusCollection {
	/*
	 * ================================================================
	 * Fields
	 * ================================================================
	 */
	private ProjectStatus projectStatus;
	private Map<String, OUSStatus> ousStatuses;
	private Map<String, SBStatus>  sbStatuses;
	/* End Fields
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	private StatusCollection() {
		super();
		this.projectStatus = null;
		this.ousStatuses   = new HashMap<String, OUSStatus>();
		this.sbStatuses    = new HashMap<String, SBStatus>();
	}
	
	public StatusCollection(StatusBaseT[] statuses) throws SchedulingException {
		this();
		for (final StatusBaseT status : statuses) {
			if (status.getClass().equals(ProjectStatus.class)) {
				addProjectStatus((ProjectStatus) status);
			} else if (status.getClass().equals(OUSStatus.class)) {
				addOUSStatus((OUSStatus) status);
			} else if (status.getClass().equals(SBStatus.class)) {
				addSBStatus((SBStatus) status);
			} else {
				throw new SchedulingException(String.format(
						"Unknown type of status entity: %s",
						status.getClass().getSimpleName()));
			}
		}
		
		if (projectStatus == null) {
			throw new SchedulingException(
					"No ProjectStatus entity in collection");
		}
		if (ousStatuses.size() == 0) {
			throw new SchedulingException(
					"No OUSStatus entities in collection");
		}
		if (sbStatuses.size() == 0) {
			throw new SchedulingException(
					"No SBStatus entities in collection");
		}
	}
	/* End Construction
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Adding stuff
	 * ================================================================
	 */
	private void addProjectStatus(ProjectStatus ps) throws SchedulingException {
		if (projectStatus != null) {
			throw new SchedulingException(String.format(
					"Duplicate ProjectStatus in collection (%s and %s)",
					projectStatus.getProjectStatusEntity().getEntityId(),
					ps.getProjectStatusEntity().getEntityId()));
		} else {
			projectStatus = ps;
		}
	}
	
	private void addOUSStatus(OUSStatus ouss) throws SchedulingException {
		final String key = ouss.getOUSStatusEntity().getEntityId();
		if (ousStatuses.containsKey(key)) {
			throw new SchedulingException(String.format(
					"Duplicate OUSStatus %s in collection",
					key));
		} else {
			ousStatuses.put(key, ouss);
		}
	}
	
	private void addSBStatus(SBStatus sbs) throws SchedulingException {
		final String key = sbs.getSBStatusEntity().getEntityId();
		if (sbStatuses.containsKey(key)) {
			throw new SchedulingException(String.format(
					"Duplicate SBStatus %s in collection",
					key));
		} else {
			sbStatuses.put(key, sbs);
		}
	}
	/* End Adding Stuff
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Accessing stuff
	 * ================================================================
	 */
	public ProjectStatus getProjectStatus() {
		return projectStatus;
	}
	
	public OUSStatus getOUSStatus(String entityId) {
		return ousStatuses.get(entityId);
	}
	
	public SBStatus getSBStatus(String entityId) {
		return sbStatuses.get(entityId);
	}
	/* End Accessing Stuff
	 * ============================================================= */
}
