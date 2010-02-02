package alma.scheduling.datamodel.executive;

/**
 * @author rhiriart
 * @version 1.0
 * @created 26-Jan-2010 9:26:43 AM
 */
public class ExecutiveTimeSpent {

	private float timeSpent;
	private Executive Executive;
	private ObservingSeason observingSeason;

	public ExecutiveTimeSpent(){

	}

    public float getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(float timeSpent) {
        this.timeSpent = timeSpent;
    }
    
    public Executive getExecutive() {
        return Executive;
    }

    public void setExecutive(Executive mExecutive) {
        Executive = mExecutive;
    }

    public ObservingSeason getObservingSeason() {
        return observingSeason;
    }

    public void setObservingSeason(ObservingSeason mObservingSeason) {
        observingSeason = mObservingSeason;
    }

    public void finalize() throws Throwable {

	}

}