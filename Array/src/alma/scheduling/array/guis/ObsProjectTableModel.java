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

package alma.scheduling.array.guis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.table.AbstractTableModel;

import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ScienceGrade;

/**
 * A model for a table showing a collection of
 * alma.scheduling.datamodel.obsproject.ObsProjects.
 * 
 * @author dclarke
 */
@SuppressWarnings("serial") // We are unlikely to need to serialise
public class ObsProjectTableModel extends AbstractTableModel {
	/*
	 * ================================================================
	 * Fields
	 * ================================================================
	 */
	/** Of course, a Logger */
	private Logger logger;
	/* End Fields
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	/**
	 * The one and only constructor.
	 */
	public ObsProjectTableModel() {
		super();
		initialiseData();
	}
	/* End Construction
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Underlying data
	 * ================================================================
	 */
	/** The underlying data for which we are providing a TableModel */
	private List<ObsProject> data;
	
	/**
	 * Initialise our internal storage
	 */
	private void initialiseData() {
		this.data = new ArrayList<ObsProject>();
		this.fireTableDataChanged();
	}
	
	/**
	 * Set the data of the TableModel
	 */
	public void setData(Collection<ObsProject> obsProjects) {
		this.data.clear();
		this.data.addAll(obsProjects);
		this.fireTableDataChanged();
	}
	
	/**
	 * Get the data of the TableModel
	 */
	private List<ObsProject> getData() {
		return this.data;
	}
	/* End Underlying data
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * TableModel implementation
	 * ================================================================
	 */
	protected static final int               Column_Name =  0;
	protected static final int               Column_Code =  1;
	protected static final int            Column_Version =  2;
	protected static final int                 Column_PI =  3;
	protected static final int              Column_State =  4;
	protected static final int       Column_ScienceScore =  5;
	protected static final int        Column_ScienceRank =  6;
	protected static final int        Column_LetterGrade =  7;
	protected static final int           Column_EntityId =  8;
	protected static final int                Column_CSV =  9;
	protected static final int Column_TotalExecutionTime = 10;
	protected static final int               NUM_COLUMNS = 11;
	

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return NUM_COLUMNS;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return getData().size();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		final ObsProject obsProject;
		try {
			obsProject = getData().get(rowIndex);
		} catch (IndexOutOfBoundsException e) {
			logger.severe(String.format(
					"row out of bounds in %s.getValueAt(%d, %d)",
					this.getClass().getSimpleName(),
					rowIndex, columnIndex));
			return null;
		}
		
		switch (columnIndex) {
		case Column_Name:
			return obsProject.getName();
		case Column_Version:
			return obsProject.getVersion();
		case Column_Code:
			return obsProject.getCode();
		case Column_PI:
			return obsProject.getPrincipalInvestigator();
		case Column_ScienceScore:
			return obsProject.getScienceScore();
		case Column_ScienceRank:
			return obsProject.getScienceRank();
		case Column_LetterGrade:
			return obsProject.getLetterGrade();
		case Column_State:
			return obsProject.getStatus();
		case Column_EntityId:
			return obsProject.getUid();
		case Column_CSV:
			return obsProject.getCsv();
		case Column_TotalExecutionTime:
			return obsProject.getTotalExecutionTime();
		default:
			logger.severe(String.format(
					"column out of bounds in %s.getValueAt(%d, %d)",
					this.getClass().getSimpleName(),
					rowIndex, columnIndex));
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case Column_Name:
			return String.class;
		case Column_Version:
			return String.class;
		case Column_Code:
			return String.class;
		case Column_PI:
			return String.class;
		case Column_ScienceScore:
			return Float.class;
		case Column_ScienceRank:
			return Integer.class;
		case Column_LetterGrade:
			return ScienceGrade.class;
		case Column_State:
			return String.class;
		case Column_EntityId:
			return String.class;
		case Column_CSV:
			return Boolean.class;
		case Column_TotalExecutionTime:
			return Double.class;
		default:
			logger.severe(String.format(
					"column out of bounds in %s.getColumnClass(%d)",
					this.getClass().getSimpleName(),
					columnIndex));
			return Object.class;
		}
	}

	/**
	 * Return the text of the column's name. Leave it up to
	 * getColumnName() to apply any HTML formatting.
	 * 
	 * @param columnIndex
	 * @return
	 */
	private String getColumnInnerName(int columnIndex) {
		switch (columnIndex) {
		case Column_Name:
			return "Name";
		case Column_Version:
			return "Version";
		case Column_Code:
			return "Code";
		case Column_PI:
			return "P. I.";
		case Column_ScienceScore:
			return "Score";
		case Column_ScienceRank:
			return "Rank";
		case Column_LetterGrade:
			return "Grade";
		case Column_State:
			return "State";
		case Column_EntityId:
			return "Entity ID";
		case Column_CSV:
			return "CSV?";
		case Column_TotalExecutionTime:
			return "Time so far";
		default:
			logger.severe(String.format(
					"column out of bounds in %s.getColumnName(%d)",
					this.getClass().getSimpleName(),
					columnIndex));
			return "";
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int columnIndex) {
		return String.format("<html><b>%s</b></html>",
				getColumnInnerName(columnIndex));
	}
	/* End TableModel implementation
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * External interface specific to this class
	 * ================================================================
	 */
	public String getProjectId(int row) {
		return (String) getValueAt(row, Column_EntityId);
	}
	/* End External interface specific to this class
	 * ============================================================= */
}
