package alma.scheduling.datamodel.executive;

/**
 * @author rhiriart
 * @version 1.0
 * @created 26-Jan-2010 9:26:42 AM
 */
public class ExecutivePercentage {

	private float percentage;
	private float totalObsTimeForSeason;
	public Executive m_Executive;
	public ObservingSeason m_ObservingSeason;

	public ExecutivePercentage(){

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