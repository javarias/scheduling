package alma.scheduling.datamodel.executive;

/**
 * @author rhiriart
 * @version 1.0
 * @created 26-Jan-2010 9:26:43 AM
 */
public class SchedBlockExecutivePercentage {

	private float percentage;
	private String sbid;
	private long executionTime;

	public SchedBlockExecutivePercentage(){

	}

	public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }

    public String getSbid() {
        return sbid;
    }

    public void setSbid(String sbid) {
        this.sbid = sbid;
    }
    
    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    static SchedBlockExecutivePercentage copy(alma.scheduling.input.executive.generated.SchedBlockExecutivePercentage in){
        SchedBlockExecutivePercentage sbexecp = new SchedBlockExecutivePercentage();
        sbexecp.setExecutionTime(in.getExecutionTime());
        sbexecp.setPercentage(in.getPercentage());
        sbexecp.setSbid(in.getSbid());
        return sbexecp;
    }
    
    public void finalize() throws Throwable {

	}

}