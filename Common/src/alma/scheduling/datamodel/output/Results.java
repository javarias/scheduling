package alma.scheduling.datamodel.output;

/**
 * @author Arturo Hoffstadt Urrutia
 * @version 1.0
 * @created 02-Mar-2010 11:49:25 AM
 */
public class Results {

	/**
	 * Sum across Array_i.availableTime
	 */
	private double availableTime;
	/**
	 * Sum across Array_i.maintenanceTime.
	 */
	private double maintenanceTime;
	/**
	 * Sum across ObservationProject_i.executionTime
	 */
	private double operationTime;
	/**
	 * Sum across Array_i.scientificTime
	 */
	private double scientificTime;
	public Array m_Array;
	public ObservationProject m_ObservationProject;

	public Results(){

	}

    public double getAvailableTime() {
        return availableTime;
    }

    public void setAvailableTime(double availableTime) {
        this.availableTime = availableTime;
    }

    public double getMaintenanceTime() {
        return maintenanceTime;
    }

    public void setMaintenanceTime(double maintenanceTime) {
        this.maintenanceTime = maintenanceTime;
    }

    public double getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(double operationTime) {
        this.operationTime = operationTime;
    }

    public double getScientificTime() {
        return scientificTime;
    }

    public void setScientificTime(double scientificTime) {
        this.scientificTime = scientificTime;
    }

    public Array getM_Array() {
        return m_Array;
    }

    public void setM_Array(Array mArray) {
        m_Array = mArray;
    }

    public ObservationProject getM_ObservationProject() {
        return m_ObservationProject;
    }

    public void setM_ObservationProject(ObservationProject mObservationProject) {
        m_ObservationProject = mObservationProject;
    }
}