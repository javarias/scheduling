package alma.scheduling.array.compimpl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import alma.acs.logging.AcsLogger;
import alma.scheduling.ArrayGUICallbackPOA;
import alma.scheduling.ArrayGUIOperation;

public class ArrayGUICallbackImpl extends ArrayGUICallbackPOA {

	AcsLogger logger;
	private PropertyChangeListener pcl;
	
	public ArrayGUICallbackImpl(AcsLogger acsLogger, PropertyChangeListener listener){
		logger = acsLogger;
		this.pcl = listener;
	}
	
	@Override
	public void report(ArrayGUIOperation operation, String name, String role) {
		logger.info(String.format(
				"received report AGCB: %s, %s, %s",
				operation,
				name,
				role));
		String value[] = new String[2];
		value[0] = name;
		value[1] = role;
		PropertyChangeEvent event = new PropertyChangeEvent(this,
				                                            operation.toString(),
				                                            null,
				                                            value);
		pcl.propertyChange(event);
	}

}
