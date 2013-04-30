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


/**
 *
 * @author dclarke
 */
public class CurrentActivityPanelState implements Serializable {


	/**  */
	private static final long serialVersionUID = 417112467070396624L;
	/*
	 * ================================================================
	 * State of this panel
	 * ================================================================
	 */
	private double topSplitDividerLocation;
	private double bottomSplitDividerLocation;
	private TableState pendingState;
	private TableState currentState;
	private TableState pastState;

	/**
	 * @param topSplitDividerLocation the topSplitDividerLocation to set
	 */
	public void setTopSplitDividerLocation(double topSplitDividerLocation) {
		this.topSplitDividerLocation = topSplitDividerLocation;
	}
	/**
	 * @return the topSplitDividerLocation
	 */
	public double getTopSplitDividerLocation() {
		return topSplitDividerLocation;
	}

	/**
	 * @param bottomSplitDividerLocation the bottomSplitDividerLocation to set
	 */
	public void setBottomSplitDividerLocation(double bottomSplitDividerLocation) {
		this.bottomSplitDividerLocation = bottomSplitDividerLocation;
	}
	/**
	 * @return the bottomSplitDividerLocation
	 */
	public double getBottomSplitDividerLocation() {
		return bottomSplitDividerLocation;
	}

	/**
	 * @param state the pendingState to set
	 */
	public void setPendingState(TableState state) {
		this.pendingState = state;
	}
	/**
	 * @return the pendingState
	 */
	public TableState getPendingState() {
		return pendingState;
	}

	/**
	 * @param state the currentState to set
	 */
	public void setCurrentState(TableState state) {
		this.currentState = state;
	}
	/**
	 * @return the currentState
	 */
	public TableState getCurrentState() {
		return currentState;
	}

	/**
	 * @param state the pastState to set
	 */
	public void setPastState(TableState state) {
		this.pastState = state;
	}
	/**
	 * @return the pastState
	 */
	public TableState getPastState() {
		return pastState;
	}
}
