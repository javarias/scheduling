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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.obsproject.SchedBlockState;

/**
 * A model for a table showing a collection of executions of
 * alma.scheduling.datamodel.obsproject.SchedBlocks.
 * 
 * @author dclarke
 * $Id: SBExecutionTableModel.java,v 1.2 2011/01/28 00:35:31 javarias Exp $
 */
@SuppressWarnings("serial") // We are unlikely to need to serialise
public class SBExecutionTableModel extends AbstractTableModel {
	/*
	 * ================================================================
	 * Time offsets
	 * ================================================================
	 */
	private static long TimeZeroMilli;
	private static long TimeZeroNano;
	private static int NanosPerMilli = 1000000;
	
	static {
		TimeZeroNano = System.nanoTime();
		TimeZeroMilli = System.currentTimeMillis();
	}
	/* End Time offsets
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Types
	 * ================================================================
	 */
	/** Is this for pending, current or past executions? */
	public enum When {
		Pending, Current, Past;
	}
	/* End Types
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Fields
	 * ================================================================
	 */
	/** Of course, a Logger */
//	private Logger logger;
	
	/** Map the displayed columns to the internal logical columns */
	private int viewToModelColumnMap[];
	
	/** Map the displayed columns to the internal logical columns */
	private DateFormat dateFormat;
	/* End Fields
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	/**
	 * Hide the default constructor.
	 */
	@SuppressWarnings("unused")
	private SBExecutionTableModel() {
	}
	
	/**
	 * The constructor to use.
	 */
	public SBExecutionTableModel(When when) {
		super();
		switch (when) {
			case Pending:
				viewToModelColumnMap = mapForPending();
			break;
			case Current:
				viewToModelColumnMap = mapForCurrent();
			break;
			case Past:
				viewToModelColumnMap = mapForPast();
			break;
		}
		initialiseData();
		dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
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
	 * Pending model.
	 * 
	 * @return
	 */
	private int[] mapForPending() {
		final int[] result = {
				Column_Position,
				Column_EntityId,
				Column_PI,
				Column_Timestamp,
				Column_Executive,
				Column_Name,
				Column_State,
				Column_Project,
				Column_Note
		};
		return result;
	}

	/**
	 * Create the map from view columns to logical columns for the
	 * Current model.
	 * 
	 * @return
	 */
	private int[] mapForCurrent() {
		final int[] result = {
				Column_EntityId,
				Column_PI,
				Column_Timestamp,
				Column_Executive,
				Column_Name,
				Column_State,
				Column_Project,
				Column_Note
		};
		return result;
	}

	/**
	 * Create the map from view columns to logical columns for the
	 * Past model.
	 * 
	 * @return
	 */
	private int[] mapForPast() {
		final int[] result = {
				Column_Project,
				Column_EntityId,
				Column_PI,
				Column_Timestamp,
				Column_Executive,
				Column_Name,
				Column_State,
				Column_Note,
		};
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
	private List<ManifestSchedBlockQueueItem> data;
	
	/**
	 * Initialise our internal storage
	 */
	private void initialiseData() {
		this.data = new ArrayList<ManifestSchedBlockQueueItem>();
		this.fireTableDataChanged();
	}
	
	/**
	 * Set the data of the TableModel
	 */
	public void setData(Collection<ManifestSchedBlockQueueItem> queue) {
		this.data.clear();
		this.data.addAll(queue);
		this.fireTableDataChanged();
	}
	
	/**
	 * Make sure that the given item is in this model
	 */
	public void ensureIn(ManifestSchedBlockQueueItem item) {
		final int i = findDataRow(item);
		if (i < 0) { // Can't find it, so add it in
			data.add(item);
		} else { // Existing copy - make sure it has the correct state
			data.get(i).setExecutionState(item.getExecutionState());
		}

		this.fireTableDataChanged();
	}
	
	/**
	 * Make sure that the given items are in this model
	 */
	public void ensureIn(Collection<ManifestSchedBlockQueueItem> interesting) {
		for (final ManifestSchedBlockQueueItem item : interesting) {
			final int i = findDataRow(item);
			if (i < 0) { // Can't find it, so add it in
				data.add(item);
			} else { // Existing copy - make sure it has the correct state
				data.get(i).setExecutionState(item.getExecutionState());
			}
		}

		this.fireTableDataChanged();
	}
	
	/**
	 * Make sure that the given item is not in this model
	 */
	public void ensureOut(ManifestSchedBlockQueueItem item) {
		final int i = findDataRow(item);
		if (i >= 0) {
			data.remove(i);
			this.fireTableDataChanged();
		}
	}
	
	/**
	 * Make sure that the given items are not in this model
	 */
	public void ensureOut(Collection<ManifestSchedBlockQueueItem> interesting) {
		boolean changed = false;
		for (final ManifestSchedBlockQueueItem item : interesting) {
			final int i = findDataRow(item);
			if (i >= 0) {
				data.remove(i);
				changed = true;
			}
		}
		if (changed) {
			this.fireTableDataChanged();
		}
	}
	
	/**
	 * Find the data row which corresponds to the given item.
	 * 
	 * @param item
	 * @return - the index of the row, or -1 if it's not found
	 */
	private int findDataRow(ManifestSchedBlockQueueItem item) {
		int result = -1;
		
		for (int r = 0; r < data.size(); r++) {
			final ManifestSchedBlockQueueItem datum = data.get(r);
			if (datum.getTimestamp() == item.getTimestamp() &&
					datum.getUid().equals(item.getUid())) {
				result = r;
				break;
			}
		}
			
		return result;
	}

	/**
	 * Get the data of the TableModel
	 */
	public ManifestSchedBlockQueueItem getData(int rowIndex) {
		final ManifestSchedBlockQueueItem schedBlock;
		try {
			schedBlock = getData().get(rowIndex);
		} catch (IndexOutOfBoundsException e) {
			System.out.format(
					"row out of bounds in %s.getData(%d)",
					this.getClass().getSimpleName(),
					rowIndex);
			return null;
		}
		return schedBlock;
	}
	
	/**
	 * Get the data of the TableModel
	 */
	private List<ManifestSchedBlockQueueItem> getData() {
		return this.data;
	}
	/* End Underlying data
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Support methods
	 * ================================================================
	 */
	/* End Support methods
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * TableModel implementation
	 * ================================================================
	 */
	private static final int  Column_EntityId = 0;
	private static final int        Column_PI = 1;
	private static final int Column_Executive = 2;
	private static final int      Column_Name = 3;
	private static final int     Column_State = 4;
	private static final int   Column_Project = 5;
	private static final int  Column_Position = 6;
	private static final int      Column_Note = 7;
	private static final int Column_Timestamp = 8;
	

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
		final ManifestSchedBlockQueueItem schedBlock;
		try {
			schedBlock = getData().get(rowIndex);
		} catch (IndexOutOfBoundsException e) {
			System.out.format(
					"row out of bounds in %s.getValueAt(%d, %d)",
					this.getClass().getSimpleName(),
					rowIndex, columnIndex);
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
			return schedBlock.getExecutionState();
		case Column_Project:
			return schedBlock.getProjectUid();
		case Column_Position:
			return rowIndex + 1;
		case Column_Timestamp:
			final long nsSinceT0 = schedBlock.getItem().timestamp - TimeZeroNano;
			final long msSinceT0 = nsSinceT0 / NanosPerMilli;
			final long remainderNS = nsSinceT0 % NanosPerMilli;
//			System.out.format(
//					"%n%-13s = %21d%n%-13s = %21d%n%-13s = %15d (%s) %n%-13s = %21d%n%-13s = %15d%n%-13s = %21d%n",
//					"timestamp", schedBlock.getItem().timestamp,
//					"TimeZeroNano", TimeZeroNano,
//					"TimeZeroMilli", TimeZeroMilli, new Date(TimeZeroMilli),
//					"nsSinceT0", nsSinceT0,
//					"msSinceT0", msSinceT0,
//					"remainderNS", remainderNS);
			return String.format("%s%02d",
					dateFormat.format(new Date(TimeZeroMilli + msSinceT0)),
					remainderNS / 10000);
		case Column_Note:
			return schedBlock.getNote();
		default:
			System.out.format(
					"column out of bounds in %s.getValueAt(%d, %d)",
					this.getClass().getSimpleName(),
					rowIndex, columnIndex);
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
		case Column_Project:
			return String.class;
		case Column_Position:
			return Integer.class;
		case Column_Timestamp:
			return String.class;
		case Column_Note:
			return String.class;
		default:
			System.out.format(
					"column out of bounds in %s.getColumnClass(%d)",
					this.getClass().getSimpleName(),
					columnIndex);
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
		case Column_Project:
			return "Project";
		case Column_Position:
			return "Position";
		case Column_Timestamp:
			return "Time";
		case Column_Note:
			return "Note";
		default:
			System.out.format(
					"column out of bounds in %s.getColumnName(%d)",
					this.getClass().getSimpleName(),
					columnIndex);
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
	 * External interface specific to this class (uses view columns)
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
}
