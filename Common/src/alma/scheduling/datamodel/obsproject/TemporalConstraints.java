package alma.scheduling.datamodel.obsproject;

import java.util.Date;

public class TemporalConstraints {
	
	private Date startTime;
	private Date endTime;
	/**
	 * TODO: Unit?
	 */
	private double margin;
	
	
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
	
	/**
	 * @see #margin
	 * @return
	 */
	public double getMargin() {
		return margin;
	}
	
	/**
	 * @see #margin
	 * @param margin
	 */
	public void setMargin(double margin) {
		this.margin = margin;
	}
}
