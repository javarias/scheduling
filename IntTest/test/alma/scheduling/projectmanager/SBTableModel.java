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

import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.sbstatus.SBStatus;

/**
 * @author dclarke
 *
 */
@SuppressWarnings("serial")
public class SBTableModel extends EntityTableModel {
	
	private List<SBStatus> statuses;
	private Map<String, ProjectStatus> projects;
	
	public SBTableModel(StateArchiveDAO sa) {
		super(sa);
	}
	
	public void refresh() {
		statuses = new Vector<SBStatus>(sa.getAllSBStatuses());

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
		final SBStatus sbs = statuses.get(rowIndex);
		switch(columnIndex) {
			case 0:
				return sbs.getSchedBlockRef().getEntityId();
			case 1:
				return sbs.getSBStatusEntity().getEntityId();
			case 2:
				final ProjectStatus ps = projects.get(sbs.getProjectStatusRef().getEntityId());
				if (ps == null) {
					return "NULL PS (" + sbs.getProjectStatusRef().getEntityId() + ")";
				}
				return ps.getObsProjectRef().getEntityId();
			case 3:
				return sbs.getStatus().getState().toString();
			case 4:
				return sbs.getTimeOfUpdate();
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
				return "SchedBlock Id";
			case 1:
				return "Status Id";
			case 2:
				return "Project Id";
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
		final SBStatus sbs = statuses.get(row);
		sa.setStatus(sbs, to);
		refresh();
	}

	@Override
	public boolean canTransition(int row, String to) {
		final SBStatus status = statuses.get(row);
		final String from = status.getStatus().getState().toString();
		return sa.subsystemForSchedBlock(from, to) != null;
	}

	@Override
	public String getEntityType() {
		return "SchedBlock Status";
	}
}
