package alma.scheduling.psm.reports.domain;

public class SchedBlockReportBean {
	
	private String band;
	private String executive;
	private String lstRange;
	private double executionTime;
	
	public static double totalExecutionTime;

	public String getBand() {
		return band;
	}

	public void setBand(String band) {
		this.band = band;
	}

	public String getExecutive() {
		return executive;
	}

	public void setExecutive(String executive) {
		this.executive = executive;
	}

	public double getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(double executionTime) {
		this.executionTime = executionTime;
	}
	
	public String getLstRange() {
		return lstRange;
	}

	public void setLstRange(String lstRange) {
		this.lstRange = lstRange;
	}

	public void setTotalExecutionTime(double totalExecutionTime) {
		SchedBlockReportBean.totalExecutionTime = totalExecutionTime;
	}

	public double getTotalExecutionTime() {
		return totalExecutionTime;
	}
}
