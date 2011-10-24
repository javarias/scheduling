/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
package alma.scheduling.master.gui;

import java.awt.Color;

import alma.common.gui.chessboard.ChessboardStatus;
import alma.common.gui.standards.StandardColors;

public class SPAntennaStatus implements ChessboardStatus {
	private static final String ONLINE_STRING = "ANT_ONLINE";
	private static final String OFFLINE_STRING = "ANT_OFFLINE";
	
	private boolean shouldFlash;
	private boolean isSelectable;
	private String description;
	private Color bgColor;
	private Color fgColor;
	private static ChessboardStatus[] values = null;
	
	/**
	 */
	private SPAntennaStatus(String description)
	{
		this.fgColor = null;
		this.bgColor = null;
		this.description = description;
		this.shouldFlash = false;
		
        this.fgColor = Color.black;
		// configure the status with proper colors and/or flashing attributes
		if(description.equals(ONLINE_STRING)) {
			this.bgColor = StandardColors.STATUS_OKAY_BG.color;
		    this.isSelectable = true;
		} else if(description.equals(OFFLINE_STRING)) {
			this.bgColor = StandardColors.STATUS_UNAVAILABLE_BG.color;
		    this.isSelectable = false;
		} 
	}
	
	// Create the fixed set of allowed instances (these will be the enum values).
	public final static SPAntennaStatus ONLINE = 
		new SPAntennaStatus(ONLINE_STRING);
	
	public final static SPAntennaStatus OFFLINE=
		new SPAntennaStatus(OFFLINE_STRING);
	
	
	/** 
	 * Required method of <code>ChessboardStatus</code> interface.
	 * 
	 * @return the background color that should be used to render the status in the user interface.
	 */
	public Color getBgColor() {
		return this.bgColor;
	}

	/** 
	 * Required method of <code>ChessboardStatus</code> interface.
	 * 
	 * @return the foreground color that should be used to render the status in the user interface.
	 */
	public Color getFgColor() {
		return this.fgColor;
	}

	/** 
	 * Required method of <code>ChessboardStatus</code> interface.
	 * 
	 * @return the description of the status.
	 */
	public String getDescription() {
		return this.description;
	}
	
	/**
	 * Overriding generic toString method from <code>Object</code> to get
	 * something more user-friendly.
	 * 
	 * @return a string representation of this status which is the same as the status description.
	 */
	public String toString() {
		return this.description;
	}
	
	/** 
	 * Required method of <code>ChessboardStatus</code> interface.
	 * 
	 * @return boolean indicating whether the status should be rendered
	 * in the user interface as a flashing cell.
	 */
	public boolean shouldFlash() {
		return this.shouldFlash;
	}
    
    public boolean isSelectable() {
        return this.isSelectable;
    }

	/** 
	 * Required method of the <code>ChessboardStatus</code> interface. Returns
	 * all of the valid instances of this enum class. This is mostly just used 
	 * for testing. Method is synchronized because it employs a singleton
	 * for the values array. For an example of its use in a test setting, 
	 * see the <code>ChessboardTest</code> and <code>ExampleChessboardStatusProvider</code> classes.
	 * 
	 * @return the full set of statuses for this enum.
	 */
	public synchronized ChessboardStatus[] values() 
	{
		// use a singleton-style mechanism to return the values
		if(null == SPAntennaStatus.values) 
		{
			SPAntennaStatus.values = new ChessboardStatus[2];

			SPAntennaStatus.values[0] = SPAntennaStatus.ONLINE;
			SPAntennaStatus.values[1] = SPAntennaStatus.OFFLINE;
		}
		
		return SPAntennaStatus.values;
	}
    
    public String getName(){
        return "";
    }
    public Color getColor(){
        return null;
    }
}


