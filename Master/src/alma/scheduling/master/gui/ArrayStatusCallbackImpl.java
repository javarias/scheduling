package alma.scheduling.master.gui;

import alma.scheduling.ArrayEvent;
import alma.scheduling.ArrayModeEnum;
import alma.scheduling.ArrayStatusCallbackPOA;

public class ArrayStatusCallbackImpl extends ArrayStatusCallbackPOA {

	private ArrayStatusListener listener;
	
	public ArrayStatusCallbackImpl (ArrayStatusListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("ArrayStatusListener cannot be null");
		this.listener = listener;
	}

	@Override
	public void report(ArrayEvent operation, ArrayModeEnum arrayMode,
			String arrayName) {
		if (operation.value() == ArrayEvent._CREATION) 
			listener.notifyArrayCreation(arrayName, arrayMode);
		else if (operation.value() == ArrayEvent._DESTRUCTION)
			listener.notifyArrayDestruction(arrayName);
	}
	
}
