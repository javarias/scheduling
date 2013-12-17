package alma.scheduling.datamodel.observation;

import java.util.Date;

public class ExecBlock {

	private String execBlockUid;
	private String schedBlockUid;
	
	/**
	 * Total Time actually observing targets, in seconds
	 */
	private double timeOnSource;
	
	/**
	 * Time used in calibration, in seconds.
	 */	
	private double timeOnCalibration;

	private Date startTime;
	private Date endTime;
	/**
	 * Sensitivity Achieved during this observation block, in Jy
	 */
	private double sensitivityAchieved;
	
	/**
	 * Status of the completed ExecBlock
	 */
	private ExecStatus status;
	
	public String getExecBlockUid() {
		return execBlockUid;
	}
	public void setExecBlockUid(String execBlockUid) {
		this.execBlockUid = execBlockUid;
	}
	public String getSchedBlockUid() {
		return schedBlockUid;
	}
	public void setSchedBlockUid(String schedBlockUid) {
		this.schedBlockUid = schedBlockUid;
	}
	/**
	 * @see ExecBlock#timeOnSource
	 * @return
	 */
	public double getTimeOnSource() {
		return timeOnSource;
	}
	/**
	 * @see ExecBlock#timeOnSource
	 * @return
	 */
	public void setTimeOnSource(double timeOnSource) {
		this.timeOnSource = timeOnSource;
	}
	/**
	 * @see ExecBlock#timeOnCalibration
	 * @return
	 */
	public double getTimeOnCalibration() {
		return timeOnCalibration;
	}
	/**
	 * @see ExecBlock#timeOnCalibration
	 * @return
	 */
	public void setTimeOnCalibration(double timeOnCalibration) {
		this.timeOnCalibration = timeOnCalibration;
	}
	
	public Date getStartTime() {
		return startTime;
	}
	
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	
	public Date getEndTime() {
		return endTime;
	}
	
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	
	public double getSensitivityAchieved() {
		return sensitivityAchieved;
	}
	
	public void setSensitivityAchieved(double sensitivityAchieved) {
		this.sensitivityAchieved = sensitivityAchieved;
	}
	public ExecStatus getStatus() {
		return status;
	}
	public void setStatus(ExecStatus status) {
		this.status = status;
	}
	@Override
	public int hashCode() {
		return execBlockUid.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExecBlock other = (ExecBlock) obj;
		if (execBlockUid == null) {
			if (other.execBlockUid != null)
				return false;
		} else if (!execBlockUid.equals(other.execBlockUid))
			return false;
		return true;
	}
	
	
	
}
