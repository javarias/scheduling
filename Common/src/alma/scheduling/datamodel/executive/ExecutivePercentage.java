package alma.scheduling.datamodel.executive;

/**
 * @author rhiriart
 * @version 1.0
 * @created 26-Jan-2010 9:26:42 AM
 */
public class ExecutivePercentage {

	private float percentage;
	private float totalObsTimeForSeason;

	public ExecutivePercentage(){

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

    static ExecutivePercentage copy(alma.scheduling.input.executive.generated.ExecutivePercentage in){
        ExecutivePercentage execp = new ExecutivePercentage();
        execp.setPercentage(in.getPercentage());
        execp.setTotalObsTimeForSeason(in.getTotalObsTimeForSeason());
        return execp;
    }
    
    public void finalize() throws Throwable {

	}

}