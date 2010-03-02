package alma.scheduling.datamodel.output;

import java.util.Date;

/**
 * @author Arturo Hoffstadt Urrutia
 * @version 1.0
 * @created 02-Mar-2010 11:49:24 AM
 */
public class Array {

	/**
	 * deletionTime - creationTime
	 */
	private double availableTime;
	private double baseline;
	private Date creationDate;
	private Date deletionDate;
	/**
	 * Sum of the SB_i.executionTime, given that SB_i.type == maintenance.
	 */
	private double maintenanceTime;
	/**
	 * Sum of the SB_i.executionTime, given that SB_i.type == scientific.
	 */
	private double scientificTime;

	public Array(){

	}

    public double getAvailableTime() {
        return availableTime;
    }

    public void setAvailableTime(double availableTime) {
        this.availableTime = availableTime;
    }

    public double getBaseline() {
        return baseline;
    }

    public void setBaseline(double baseline) {
        this.baseline = baseline;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getDeletionDate() {
        return deletionDate;
    }

    public void setDeletionDate(Date deletionDate) {
        this.deletionDate = deletionDate;
    }

    public double getMaintenanceTime() {
        return maintenanceTime;
    }

    public void setMaintenanceTime(double maintenanceTime) {
        this.maintenanceTime = maintenanceTime;
    }

    public double getScientificTime() {
        return scientificTime;
    }

    public void setScientificTime(double scientificTime) {
        this.scientificTime = scientificTime;
    }
}