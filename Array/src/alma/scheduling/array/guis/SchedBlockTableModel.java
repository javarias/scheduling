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

import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.obsproject.SchedBlock;

/**
 * A model for a table showing a collection of
 * alma.scheduling.datamodel.obsproject.SchedBlocks.
 * 
 * @author dclarke
 * $Id: SchedBlockTableModel.java,v 1.1 2010/07/26 16:36:19 dclarke Exp $
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
	private List<SchedBlock> data;
	
	/**
	 * Initialise our internal storage
	 */
	private void initialiseData() {
		this.data = new ArrayList<SchedBlock>();
	}
	
	/**
	 * Set the data of the TableModel
	 */
	public void setData(Collection<SchedBlock> schedBlocks) {
		this.data.clear();
		this.data.addAll(schedBlocks);
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
	 * TableModel implementation
	 * ================================================================
	 */
	private static final int           Column_EntityId = 0;
	private static final int                 Column_PI = 1;
	private static final int          Column_Executive = 2;
	private static final int               Column_Name = 3;
	private static final int              Column_State = 4;
	private static final int               NUM_COLUMNS = 5;
	

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
		
		switch (columnIndex) {
		case Column_EntityId:
			return schedBlock.getUid();
		case Column_PI:
			return schedBlock.getPiName();
		case Column_Executive:
			return schedBlock.getExecutive().getName();
		case Column_Name:
			return "Not yet implemented"; // TODO: SchedBlock name
		case Column_State:
			return "Not yet implemented"; // TODO: SchedBlock state
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
		case Column_EntityId:
			return String.class;
		case Column_PI:
			return String.class;
		case Column_Executive:
			return Executive.class;
		case Column_Name:
			return String.class;
		case Column_State:
			return String.class;
		default:
			logger.severe(String.format(
					"column out of bounds in %s.getColumnClass(%d)",
					this.getClass().getSimpleName(),
					columnIndex));
			return Object.class;
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
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
		default:
			logger.severe(String.format(
					"column out of bounds in %s.getColumnName(%d)",
					this.getClass().getSimpleName(),
					columnIndex));
			return "";
		}
	}
	/* End TableModel implementation
	 * ============================================================= */
}
