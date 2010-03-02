package alma.scheduling.datamodel.output;

import java.util.Set;

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
	public Set<Array> array;
	public Set<ObservationProject> observationProject;

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

    public Set<Array> getArray() {
        return array;
    }

    public void setArray(Set<Array> mArray) {
        array = mArray;
    }

    public Set<ObservationProject> getObservationProject() {
        return observationProject;
    }

    public void setObservationProject(Set<ObservationProject> mObservationProject) {
        observationProject = mObservationProject;
    }
}