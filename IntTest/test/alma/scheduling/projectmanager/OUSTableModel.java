/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
/**
 * 
 */
package alma.scheduling.projectmanager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;

/**
 * @author dclarke
 *
 */
@SuppressWarnings("serial")
public class OUSTableModel extends EntityTableModel {
	
	private List<OUSStatus> statuses;
	private Map<String, ProjectStatus> projects;
	
	public OUSTableModel(StateArchiveDAO sa) {
		super(sa);
	}
	
	public void refresh() {
		statuses = new Vector<OUSStatus>(sa.getAllOUSStatuses());

		projects = new HashMap<String, ProjectStatus>();
		for (final ProjectStatus ps : sa.getAllProjectStatuses()) {
			projects.put(ps.getProjectStatusEntity().getEntityId(), ps);
		}
	}
	
	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public int getRowCount() {
		return statuses.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		final OUSStatus ouss = statuses.get(rowIndex);
		switch(columnIndex) {
			case 0:
				return ouss.getObsUnitSetRef().getEntityId();
			case 1:
				return ouss.getObsUnitSetRef().getPartId();
			case 2:
				return ouss.getOUSStatusEntity().getEntityId();
			case 3:
				return ouss.getStatus().getState().toString();
			case 4:
				return ouss.getTimeOfUpdate();
			default:
				return "UNKNOWN";
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public String getColumnName(int column) {
		// TODO Auto-generated method stub
		switch(column) {
			case 0:
				return "Project Id";
			case 1:
				return "OUS Id";
			case 2:
				return "Status Id";
			case 3:
				return "State";
			case 4:
				return "Updated";
			default:
				return "UNKNOWN";
		}
	}

	@Override
	public void setStatus(int row, String to) {
		final OUSStatus ps = statuses.get(row);
		sa.setStatus(ps, to);
		refresh();
	}

	@Override
	public boolean canTransition(int row, String to) {
		final OUSStatus status = statuses.get(row);
		final String from = status.getStatus().getState().toString();
		return sa.subsystemForObsUnitSet(from, to) != null;
	}

	@Override
	public String getEntityType() {
		return "ObsUnitSet Status";
	}
}
