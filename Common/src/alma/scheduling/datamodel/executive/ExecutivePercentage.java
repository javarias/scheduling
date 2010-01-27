package alma.scheduling.datamodel.executive;

/**
 * @author rhiriart
 * @version 1.0
 * @created 26-Jan-2010 9:26:42 AM
 */
public class ExecutivePercentage {

	private float percentage;
	private float totalObsTimeForSeason;
	public Executive Executive;
	public ObservingSeason ObservingSeason;

	public ExecutivePercentage(){

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

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }

    public float getTotalObsTimeForSeason() {
        return totalObsTimeForSeason;
    }

    public void setTotalObsTimeForSeason(float totalObsTimeForSeason) {
        this.totalObsTimeForSeason = totalObsTimeForSeason;
    }

    public void finalize() throws Throwable {

	}

}