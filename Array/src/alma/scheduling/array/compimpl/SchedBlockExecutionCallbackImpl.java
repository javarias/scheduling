package alma.scheduling.array.compimpl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import alma.ACSErr.Completion;
import alma.acs.logging.AcsLogger;
import alma.scheduling.SchedBlockExecutionCallbackPOA;
import alma.scheduling.SchedBlockQueueItem;

public class SchedBlockExecutionCallbackImpl extends SchedBlockExecutionCallbackPOA {

	private AcsLogger logger;
	private PropertyChangeListener pcl;
	
	public SchedBlockExecutionCallbackImpl(AcsLogger acsLogger, PropertyChangeListener pcl){
		logger = acsLogger;
		this.pcl = pcl;
	}
	
	@Override
	public void report(SchedBlockQueueItem item, String newState, Completion completion) {
		logger.info("received report SBEC: " + item + ", " + newState
				+ ", " + completion.toString() );
		PropertyChangeEvent event = new PropertyChangeEvent(this, newState, null, item);
		pcl.propertyChange(event);
	}
	
}
