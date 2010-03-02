package alma.scheduling.datamodel.output;

import java.util.Set;

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
	public Set<Affiliation> affiliation;
	public Set<SchedBlockResult> schedBlock;

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

    public Set<Affiliation> getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(Set<Affiliation> mAffiliation) {
        affiliation = mAffiliation;
    }

    public Set<SchedBlockResult> getSchedBlock() {
        return schedBlock;
    }

    public void setSchedBlock(Set<SchedBlockResult> mSchedBlock) {
        schedBlock = mSchedBlock;
    }
}