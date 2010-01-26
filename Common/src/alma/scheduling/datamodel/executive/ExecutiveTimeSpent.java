package alma.scheduling.datamodel.executive;

/**
 * @author rhiriart
 * @version 1.0
 * @created 26-Jan-2010 9:26:43 AM
 */
public class ExecutiveTimeSpent {

	private ObservingSeason season;
	private float timeSpent;
	private Executive m_Executive;
	private ObservingSeason m_ObservingSeason;

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
    
    public Executive getM_Executive() {
        return m_Executive;
    }

    public void setM_Executive(Executive mExecutive) {
        m_Executive = mExecutive;
    }

    public ObservingSeason getM_ObservingSeason() {
        return m_ObservingSeason;
    }

    public void setM_ObservingSeason(ObservingSeason mObservingSeason) {
        m_ObservingSeason = mObservingSeason;
    }

    public void finalize() throws Throwable {

	}

}