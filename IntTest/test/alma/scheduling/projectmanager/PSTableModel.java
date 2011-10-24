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

import java.util.List;
import java.util.Vector;

import alma.entity.xmlbinding.projectstatus.ProjectStatus;

/**
 * @author dclarke
 *
 */
@SuppressWarnings("serial")
public class PSTableModel extends EntityTableModel {
	
	private List<ProjectStatus> statuses;
	
	public PSTableModel(StateArchiveDAO sa) {
		super(sa);
	}

	public void refresh() {
		this.statuses = new Vector<ProjectStatus>(sa.getAllProjectStatuses());
	}
	
	@Override
	public int getColumnCount() {
		return 6;
	}

	@Override
	public int getRowCount() {
		return statuses.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		final ProjectStatus ps = statuses.get(rowIndex);
		switch(columnIndex) {
			case 0:
				return ps.getName();
			case 1:
				return ps.getPI();
			case 2:
				return ps.getObsProjectRef().getEntityId();
			case 3:
				return ps.getProjectStatusEntity().getEntityId();
			case 4:
				return ps.getStatus().getState().toString();
			case 5:
				return ps.getTimeOfUpdate();
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
				return "Name";
			case 1:
				return "P.I.";
			case 2:
				return "Project Id";
			case 3:
				return "Status Id";
			case 4:
				return "State";
			case 5:
				return "Updated";
			default:
				return "UNKNOWN";
		}
	}

	@Override
	public void setStatus(int row, String to) {
		final ProjectStatus ps = statuses.get(row);
		sa.setStatus(ps, to);
		refresh();
	}

	@Override
	public boolean canTransition(int row, String to) {
		final ProjectStatus status = statuses.get(row);
		final String from = status.getStatus().getState().toString();
		return sa.subsystemForProject(from, to) != null;
	}

	@Override
	public String getEntityType() {
		return "Project Status";
	}
}
