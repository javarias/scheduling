package alma.scheduling.array.guis;

import java.io.Serializable;

public class ArrayPanelState implements Serializable {

	/**  */
	private static final long serialVersionUID = -612334646608956878L;

	/*
	 * ================================================================
	 * Delegation to state of sub-panels
	 * ================================================================
	 */
	private InteractivePanelState interactivePanelState;
	private CurrentActivityPanelState currentActivityPanelState;

	/**
	 * @return the interactivePanelState
	 */
	public InteractivePanelState getInteractivePanelState() {
		return interactivePanelState;
	}
	/**
	 * @param interactivePanelState the interactivePanelState to set
	 */
	public void setInteractivePanelState(InteractivePanelState interactivePanelState) {
		this.interactivePanelState = interactivePanelState;
	}
	/**
	 * @return the currentActivityPanelState
	 */
	public CurrentActivityPanelState getCurrentActivityPanelState() {
		return currentActivityPanelState;
	}
	/**
	 * @param currentActivityPanelState the currentActivityPanelState to set
	 */
	public void setCurrentActivityPanelState(
			CurrentActivityPanelState currentActivityPanelState) {
		this.currentActivityPanelState = currentActivityPanelState;
	}
	/* End of Delegation to state of sub-panels
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * State of this panel
	 * ================================================================
	 */
	private double splitPaneDividerLocation;

	/**
	 * @param splitPaneDividerLocation the splitPaneDividerLocation to set
	 */
	public void setSplitPaneDividerLocation(double splitPaneDividerLocation) {
		this.splitPaneDividerLocation = splitPaneDividerLocation;
	}
	/**
	 * @return the splitPaneDividerLocation
	 */
	public double getSplitPaneDividerLocation() {
		return splitPaneDividerLocation;
	}
	/* End of State of this panel
	 * ============================================================= */
}
