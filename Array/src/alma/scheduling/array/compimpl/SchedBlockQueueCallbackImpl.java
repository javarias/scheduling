package alma.scheduling.array.compimpl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import alma.acs.logging.AcsLogger;
import alma.scheduling.QueueOperation;
import alma.scheduling.SchedBlockQueueCallbackPOA;
import alma.scheduling.array.guis.SBExecutionTableModel;

public class SchedBlockQueueCallbackImpl extends SchedBlockQueueCallbackPOA {

	AcsLogger logger;
	private PropertyChangeListener pcl;
	
	public SchedBlockQueueCallbackImpl(AcsLogger acsLogger, PropertyChangeListener listener){
		logger = acsLogger;
		this.pcl = listener;
	}
	
	@Override
	public void report(long timestamp, QueueOperation operation, 
			String[] uids, String newState) {
		logger.info("received report SBQC: " + timestamp + ", " + operation
				+ ", " + uids.length + ", " + newState);
		PropertyChangeEvent event = new PropertyChangeEvent(this, newState, null, operation);
		pcl.propertyChange(event);
	}

}
