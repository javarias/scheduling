package alma.scheduling.datamodel.output;

/**
 * @author Arturo Hoffstadt Urrutia
 * @version 1.0
 * @created 02-Mar-2010 11:49:25 AM
 */
public class ObservationProject {

	/**
	 * Sum across ScheBlock_i.executionTime.
	 */
	private double executionTime;
	private int scienceRating;
	private ExecutionStatus status;
	public Affiliation m_Affiliation;
	public SchedBlock m_SchedBlock;

	public ObservationProject(){

	}

    public double getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(double executionTime) {
        this.executionTime = executionTime;
    }

    public int getScienceRating() {
        return scienceRating;
    }

    public void setScienceRating(int scienceRating) {
        this.scienceRating = scienceRating;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public Affiliation getM_Affiliation() {
        return m_Affiliation;
    }

    public void setM_Affiliation(Affiliation mAffiliation) {
        m_Affiliation = mAffiliation;
    }

    public SchedBlock getM_SchedBlock() {
        return m_SchedBlock;
    }

    public void setM_SchedBlock(SchedBlock mSchedBlock) {
        m_SchedBlock = mSchedBlock;
    }
}