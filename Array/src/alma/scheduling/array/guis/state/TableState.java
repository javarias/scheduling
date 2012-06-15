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

package alma.scheduling.array.guis.state;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import alma.scheduling.array.guis.ColumnProperties;
import alma.scheduling.utils.ErrorHandling;

/**
 *
 * @author dclarke
 * $Id: TableState.java,v 1.2 2012/06/15 19:27:14 dclarke Exp $
 */
public class TableState implements Serializable {

	/**  */
	private static final long serialVersionUID = -3830554449258256444L;

	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	public TableState() {
		clearColumns();
	}

	public TableState(JTable table) {
		this();
		
		TableColumnModel tableColumnModel = table.getColumnModel();
		for (int i = 0; i < tableColumnModel.getColumnCount(); i++) {
			final TableColumn tableColumn = tableColumnModel.getColumn(i);
			addColumn(tableColumn);
		}
	}
	/* End of Construction
	 * ============================================================= */



	/*
	 * ================================================================
	 * Restoration
	 * ================================================================
	 */
	public void restore(JTable table, Logger logger) {
		TableColumnModel tableColumnModel = table.getColumnModel();
		for (int i = 0; i < getColumnCount(); i++) {
			ColumnProperties props = getColumn(i);
			try {
				int currentIndex = tableColumnModel.getColumnIndex(props.getIdentifier());
				TableColumn current = tableColumnModel.getColumn(currentIndex);
				props.restore(current);
				tableColumnModel.moveColumn(currentIndex, i);
			} catch (Exception e) {
				String message = String.format("Error restoring table column %d", i);
				if (logger != null) {
					ErrorHandling.warning(logger, message, e);
				} else {
					System.err.println(message);
					e.printStackTrace(System.err);
				}
			}
		}
	}
	/* End of Restoration
	 * ============================================================= */



	/*
	 * ================================================================
	 * State of this table
	 * ================================================================
	 */
	private Map<Object, Integer> indices;
	private List<ColumnProperties> properties;

	public void clearColumns() {
		indices = new HashMap<Object, Integer>();
		properties = new ArrayList<ColumnProperties>();
	}

	public void addColumn(TableColumn column) {
		final String label = column.getIdentifier().toString();
		final ColumnProperties props = new ColumnProperties(column);
		addColumn(label, props);
	}

	private void addColumn(Object identifier, ColumnProperties props) {
		if (indices.containsKey(identifier)) {
			final int index = indices.get(identifier);
			properties.remove(index);
			properties.add(index, props);
		} else {
			indices.put(identifier, properties.size());
			properties.add(props);
		}
	}

	public int getColumnCount() {
		return properties.size();
	}

	public ColumnProperties getColumn(int index) {
		return properties.get(index);
	}

	public ColumnProperties getColumn(Object identifier) {
		if (indices.containsKey(identifier)) {
			final int index = indices.get(identifier);
			return properties.get(index);
		} else {
			return null;
		}
	}
	/* End of State of this table
	 * ============================================================= */
}
