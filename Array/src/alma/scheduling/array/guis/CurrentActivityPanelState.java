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
 * $Id: CurrentActivityPanelState.java,v 1.1 2012/02/06 22:44:19 dclarke Exp $
 */
public class CurrentActivityPanelState implements Serializable {

	/**  */
	private static final long serialVersionUID = -6501061530929735196L;
	/*
	 * ================================================================
	 * State of this panel
	 * ================================================================
	 */
	private double topSplitDividerLocation;
	private double bottomSplitDividerLocation;

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
}
