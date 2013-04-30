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

import java.io.Serializable;

import javax.swing.table.TableColumn;

/**
 * A simple bean-like store for the properties of a table column we're
 * interested in remembering.
 *  
 * @author dclarke
 */
public class ColumnProperties implements Serializable {

	/*
	 * ================================================================
	 * Fields
	 * ================================================================
	 */
	private Object identifier;
	private int    width;
	/* End of Fields
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	public ColumnProperties(TableColumn column) {
		super();
		setIdentifier(column.getIdentifier());
		setWidth(column.getWidth());
	}
	/* End of Construction
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Restoration
	 * ================================================================
	 */
	public void restore(TableColumn column) {
		// Don't restore the identifier, please.
		column.setPreferredWidth(getWidth());
	}
	/* End of Restoration
	 * ============================================================= */
	

	
	/*
	 * ================================================================
	 * Getters and Setters
	 * ================================================================
	 */
	/**
	 * @return the identifier
	 */
	public Object getIdentifier() {
		return identifier;
	}

	/**
	 * @param width the width to set
	 */
	public void setIdentifier(Object identifier) {
		this.identifier = identifier;
	}
	
	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}
	/* End of Getters and Setters
	 * ============================================================= */
}
