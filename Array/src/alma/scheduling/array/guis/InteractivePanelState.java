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

/**
 *
 * @author dclarke
 * $Id: InteractivePanelState.java,v 1.2 2012/02/13 23:11:38 dclarke Exp $
 */
public class InteractivePanelState implements Serializable {

	
	/**  */
	private static final long serialVersionUID = -2646904187473846890L;

	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	public InteractivePanelState() {
		opState = null;
		sbState = null;
	}
	/* End of Construction
	 * ============================================================= */



	/*
	 * ================================================================
	 * State of this panel
	 * ================================================================
	 */
	private double splitDividerLocation;
	private double subsplitDividerLocation;
	private double detailssplitDividerLocation;
	private TableState opState;
	private TableState sbState;

	/**
	 * @param splitPaneDividerLocation the splitPaneDividerLocation to set
	 */
	public void setSplitDividerLocation(double splitDividerLocation) {
		this.splitDividerLocation = splitDividerLocation;
	}
	/**
	 * @return the splitPaneDividerLocation
	 */
	public double getSplitDividerLocation() {
		return splitDividerLocation;
	}

	/**
	 * @param subsplitDividerLocation the subsplitDividerLocation to set
	 */
	public void setSubsplitDividerLocation(double subsplitDividerLocation) {
		this.subsplitDividerLocation = subsplitDividerLocation;
	}
	
	/**
	 * @return the subsplitDividerLocation
	 */
	public double getSubsplitDividerLocation() {
		return subsplitDividerLocation;
	}

	/**
	 * @param detailssplitDividerLocation the detailssplitDividerLocation to set
	 */
	public void setDetailssplitDividerLocation(double detailssplitDividerLocation) {
		this.detailssplitDividerLocation = detailssplitDividerLocation;
	}
	
	/**
	 * @return the detailssplitDividerLocation
	 */
	public double getDetailssplitDividerLocation() {
		return detailssplitDividerLocation;
	}

	/**
	 * @param opState the opState to set
	 */
	public void setOPState(TableState opState) {
		this.opState = opState;
	}
	/**
	 * @return the opState
	 */
	public TableState getOPState() {
		return opState;
	}
	
	/**
	 * @param sbState the sbState to set
	 */
	public void setSBState(TableState sbState) {
		this.sbState = sbState;
	}
	/**
	 * @return the sbState
	 */
	public TableState getSBState() {
		return sbState;
	}
	/* End of State of this panel
	 * ============================================================= */
}
