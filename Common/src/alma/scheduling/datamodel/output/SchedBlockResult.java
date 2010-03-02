package alma.scheduling.datamodel.output;

import java.util.Date;

/**
 * @author Arturo Hoffstadt Urrutia
 * @version 1.0
 * @created 02-Mar-2010 11:49:25 AM
 */
public class SchedBlockResult {

	private Date endDate;
	private double executionTime;
	private String mode;
	private Date startDate;
	private ExecutionStatus status;
	private String type;
	public Array ArrayRef;

	public SchedBlockResult(){

	}

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public double getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(double executionTime) {
        this.executionTime = executionTime;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Array getArrayRef() {
        return ArrayRef;
    }

    public void setArrayRef(Array arrayRef) {
        ArrayRef = arrayRef;
    }

}