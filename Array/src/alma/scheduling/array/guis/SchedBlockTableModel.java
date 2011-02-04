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
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.SchedBlockState;
import alma.scheduling.datamodel.obsproject.SkyCoordinates;
import alma.scheduling.utils.Format;

/**
 * A model for a table showing a collection of
 * alma.scheduling.datamodel.obsproject.SchedBlocks.
 * 
 * @author dclarke
 * $Id: SchedBlockTableModel.java,v 1.7 2011/02/04 17:19:36 javarias Exp $
 */
@SuppressWarnings("serial") // We are unlikely to need to serialise
public class SchedBlockTableModel extends AbstractTableModel {
	/*
	 * ================================================================
	 * Fields
	 * ================================================================
	 */
	/** Of course, a Logger */
	private Logger logger;
	
	/** Map the displayed columns to the internal logical columns */
	private int viewToModelColumnMap[];
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
	public SchedBlockTableModel() {
		super();
		viewToModelColumnMap = defaultMap();
		initialiseData();
	}
	/* End Construction
	 * ============================================================= */


	
	/*
	 * ================================================================
	 * Managing models with different sets of columns
	 * ================================================================
	 */
	/**
	 * Create the map from view columns to logical columns for the
	 * default model. At current this is the only mapping, but this
	 * gives us a framework for changing this later (including
	 * supporting different views on the same data for different users,
	 * and even allowing the user to customise their view).
	 * 
	 * @return
	 */
	private int[] defaultMap() {
		final int[] result = {
				Column_EntityId,
				Column_PI,
				Column_Executive,
				Column_Name,
				Column_State,
				Column_CSV,
				Column_Project,
				Column_Note,
				Column_RA,
				Column_Dec,
				Column_minHA,
				Column_maxHA
		};
		
		assert result[Column_Project] == Column_Project;
			// Needed to locate the ProjectId for the filtering of SBs
			// according to which projects are selected.
		
		return result;
	}
	
	private int map(int viewColumn) {
		return viewToModelColumnMap[viewColumn];
	}
	/* End Managing models with different sets of columns
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Underlying data
	 * ================================================================
	 */
	/** The underlying data for which we are providing a TableModel */
	private List<SchedBlock> data;
	
	/**
	 * Initialise our internal storage
	 */
	private void initialiseData() {
		this.data = new ArrayList<SchedBlock>();
		this.fireTableDataChanged();
	}
	
	/**
	 * Set the data of the TableModel
	 */
	public void setData(Collection<SchedBlock> schedBlocks) {
		this.data.clear();
		this.data.addAll(schedBlocks);
		this.fireTableDataChanged();
	}
	
	/**
	 * Get the data of the TableModel
	 */
	private List<SchedBlock> getData() {
		return this.data;
	}
	/* End Underlying data
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Support methods
	 * ================================================================
	 */
	private SkyCoordinates getCoordinates(SchedBlock schedBlock) {
		try {
			return schedBlock.
				getSchedulingConstraints().
				getRepresentativeTarget().
				getSource().
				getCoordinates();
		} catch (NullPointerException npe) {
			return null;
		}
	}
	/* End Support methods
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * TableModel implementation
	 * ================================================================
	 */
	private static final int  Column_EntityId =  0;
	private static final int        Column_PI =  1;
	private static final int Column_Executive =  2;
	private static final int      Column_Name =  3;
	private static final int     Column_State =  4;
	private static final int       Column_CSV =  5;
	private static final int   Column_Project =  6;
	private static final int      Column_Note =  7;
	private static final int     Column_minHA =  8;
	private static final int     Column_maxHA =  9;
	private static final int        Column_RA = 10;
	private static final int       Column_Dec = 11;
	

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return viewToModelColumnMap.length;
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
		final SchedBlock schedBlock;

		try {
			schedBlock = getData().get(rowIndex);
		} catch (IndexOutOfBoundsException e) {
			logger.severe(String.format(
					"row out of bounds in %s.getValueAt(%d, %d)",
					this.getClass().getSimpleName(),
					rowIndex, columnIndex));
			return null;
		}
		
		switch (map(columnIndex)) {
		case Column_EntityId:
			return schedBlock.getUid();
		case Column_PI:
			return schedBlock.getPiName();
		case Column_Executive:
			return schedBlock.getExecutive().getName();
		case Column_Name:
			return schedBlock.getName();
		case Column_State:
			return schedBlock.getSchedBlockControl().getState();
		case Column_CSV:
			return schedBlock.getCsv();
		case Column_Project:
			return schedBlock.getProjectUid();
		case Column_Note:
			return schedBlock.getNote();
		case Column_minHA:
			return schedBlock.getPreConditions().getMinAllowedHourAngle();
		case Column_maxHA:
			return schedBlock.getPreConditions().getMaxAllowedHourAngle();
		case Column_RA:
			try {
				return Format.formatRA(getCoordinates(schedBlock).getRA());
			} catch (NullPointerException npe) {
				return -1.0;
			}
		case Column_Dec:
			try {
				return Format.formatDec(getCoordinates(schedBlock).getDec());
			} catch (NullPointerException npe) {
				return -1.0;
			}
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
		switch (map(columnIndex)) {
		case Column_EntityId:
			return String.class;
		case Column_PI:
			return String.class;
		case Column_Executive:
			return Executive.class;
		case Column_Name:
			return String.class;
		case Column_State:
			return SchedBlockState.class;
		case Column_CSV:
			return Boolean.class;
		case Column_Project:
			return String.class;
		case Column_Note:
			return String.class;
		case Column_minHA:
			return Double.class;
		case Column_maxHA:
			return Double.class;
		case Column_RA:
			return String.class;
		case Column_Dec:
			return String.class;
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
		switch (map(columnIndex)) {
		case Column_EntityId:
			return "Entity ID";
		case Column_PI:
			return "P. I.";
		case Column_Executive:
			return "Executive";
		case Column_Name:
			return "Name";
		case Column_State:
			return "State";
		case Column_CSV:
			return "CSV?";
		case Column_Project:
			return "Project";
		case Column_Note:
			return "Note";
		case Column_minHA:
			return "Min HA";
		case Column_maxHA:
			return "Max HA";
		case Column_RA:
			return "RA";
		case Column_Dec:
			return "Dec";
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
	public static int projectIdColumn() {
		return Column_Project;
	}

	public String getSchedBlockId(int row) {
		return (String) getValueAt(row, Column_EntityId);
	}
	/* End External interface specific to this class
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Comparators for sorting certain columns
	 * ================================================================
	 */
	public void addSpecificComparators(TableRowSorter<SchedBlockTableModel> sorter) {
		for (int i = 0; i < viewToModelColumnMap.length; i++) {
			switch (map(i)) {
			case Column_Dec:
				sorter.setComparator(i, decComparator());
				break;
			default:
				break;
			}
		}
	}
	
	private Comparator<String> decComparator() {
		final Comparator<String> result = new Comparator<String>(){

			@Override
			public int compare(String dec1, String dec2) {
				// First, determine which, if any, are negative decs
				final boolean neg1 = dec1.startsWith("-");
				final boolean neg2 = dec2.startsWith("-");
				if (neg1) {
					if (neg2) {
						// Both negative
						return dec2.compareTo(dec1);
					} else {
						// Dec1 negative, dec2 positive
						return -1;
					}
				} else {
					if (neg2) {
						// Dec1 positive, dec2 negative
						return 1;
					} else {
						// Both positive
						return dec1.compareTo(dec2);
					}
				}
			}};
		return result;
	}
	/* End External interface specific to this class
	 * ============================================================= */
}
