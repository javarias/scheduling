package alma.scheduling.psm.util;

public class SynchResult {
	private String letterGrade;
	private Integer rank;
	private Double score;

	public SynchResult(String letterGrade, Integer rank, Double score) {
		super();
		this.letterGrade = letterGrade;
		this.rank = rank;
		this.score = score;
	}

	public String getLetterGrade() {
		return letterGrade;
	}

	public void setLetterGrade(String letterGrade) {
		this.letterGrade = letterGrade;
	}

	public Integer getRank() {
		return rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

}
