package alma.scheduling.master.gui;

import alma.scheduling.ArrayModeEnum;

public interface ArrayStatusListener {

	public void notifyArrayCreation(String name, ArrayModeEnum arrayMode);
	
	public void notifyArrayDestruction(String name);
}
