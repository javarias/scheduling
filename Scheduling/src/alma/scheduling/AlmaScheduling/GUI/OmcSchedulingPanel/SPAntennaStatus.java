package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.awt.Color;
import alma.common.gui.chessboard.*;

public class SPAntennaStatus implements ChessboardStatus {
	private static final String ONLINE_STRING = "ANT_ONLINE";
	private static final String OFFLINE_STRING = "ANT_OFFLINE";
	
	private boolean shouldFlash;
	private String name;
	private Color color;
	private static ChessboardStatus[] values = null;
	
	/**
	 */
	private SPAntennaStatus(String name)
	{
		this.color = null;
		this.name = name;
		this.shouldFlash = false;
		
		// configure the status with proper color and/or flashing attributes
		if(name.equals(ONLINE_STRING)) {
			this.color = Color.green;
		} else if(name.equals(OFFLINE_STRING)) {
			this.color = Color.lightGray;
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
	 * @return the color that should be used to render the status in the user interface.
	 */
	public Color getColor() {
		return this.color;
	}

	/** 
	 * Required method of <code>ChessboardStatus</code> interface.
	 * 
	 * @return the name of the status.
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Overriding generic toString method from <code>Object</code> to get
	 * something more user-friendly.
	 * 
	 * @return a string representation of this status which is the same as the status name.
	 */
	public String toString() {
		return this.name;
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
}


