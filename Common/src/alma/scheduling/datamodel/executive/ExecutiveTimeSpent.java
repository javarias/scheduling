package alma.scheduling.datamodel.executive;

/**
 * @author rhiriart
 * @version 1.0
 * @created 26-Jan-2010 9:26:43 AM
 */
public class ExecutiveTimeSpent {

	private ObservingSeason season;
	private float timeSpent;
	private Executive Executive;
	private ObservingSeason ObservingSeason;

	public ExecutiveTimeSpent(){

	}

	public ObservingSeason getSeason() {
        return season;
    }

    public void setSeason(ObservingSeason season) {
        this.season = season;
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
        return ObservingSeason;
    }

    public void setM_ObservingSeason(ObservingSeason mObservingSeason) {
        ObservingSeason = mObservingSeason;
    }

    public void finalize() throws Throwable {

	}

}