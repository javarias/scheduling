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
 * $Id: InteractivePanelState.java,v 1.1 2012/02/06 22:44:19 dclarke Exp $
 */
public class InteractivePanelState implements Serializable {

	/**  */
	private static final long serialVersionUID = -7022390644408915777L;
	
	/*
	 * ================================================================
	 * State of this panel
	 * ================================================================
	 */
	private double splitDividerLocation;
	private double subsplitDividerLocation;

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
	/* End of State of this panel
	 * ============================================================= */
}
