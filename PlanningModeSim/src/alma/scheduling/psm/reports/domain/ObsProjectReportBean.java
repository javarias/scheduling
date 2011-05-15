package alma.scheduling.psm.reports.domain;

public class ObsProjectReportBean {
	
	private String band;
	private String executive;
	private String lstRange;
	private double executionTime;
	private String grade;
	private long scienceRank;
	private double scienceScore;
	private String status;
	
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

	public String getGrade(){
		return grade;
	}

	public void setGrade(String grade){
		this.grade = grade;
	}

	public long getScienceRank(){
		return this.scienceRank;
	}

	public void setScienceRank( long scienceRank ){
		this.scienceRank = scienceRank;
	}

	public double getScienceScore(){
		return this.scienceScore;
	}

	public void setScienceScore( double scienceScore ){
		this.scienceScore = scienceScore;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}
}
