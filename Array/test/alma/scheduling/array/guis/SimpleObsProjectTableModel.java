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
package alma.scheduling.array.guis;

import javax.swing.table.AbstractTableModel;

import alma.scheduling.datamodel.obsproject.ObsProject;

public class SimpleObsProjectTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	private String[] headings = {"ID", "Principal Investigator"};
	private ObsProjectModel model;

	public SimpleObsProjectTableModel(ObsProjectModel model) {
		this.model = model;
	}
	
	@Override
	public int getRowCount() {
		return model.getProjects().size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		ObsProject prj = model.getProjects().get(rowIndex);
		if (columnIndex == 0)
			return prj.getId();
		else if (columnIndex == 1)
			return prj.getPrincipalInvestigator();
		return null;
	}
	
	@Override
	public String getColumnName(int columnIndex) {
		return headings[columnIndex];
	}

}
