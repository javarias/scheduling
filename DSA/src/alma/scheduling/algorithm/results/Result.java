package alma.scheduling.algorithm.results;

import java.util.Date;

import alma.scheduling.algorithm.sbranking.SBRank;

/**
 * Class containing the results of the DSA for a given time and given Array. 
 * 
 * @author javarias
 * @since ALMA-8.1.0
 */

public class Result {

	/**
	 * Time when the DSA returned the result.
	 */
	private Date time;
	/**
	 * Name of the Array where the DSA ran.
	 */
	private String arrayName;
	/**
	 * Scheduling Block score results, the breakdown of the score is handled by
	 * {@code SBRank}
	 * 
	 *  @see SBRank
	 */
	private SBRank score;
	
	public Date getTime() {
		return time;
	}
	
	public void setTime(Date time) {
		this.time = time;
	}
	
	public SBRank getScore() {
		return score;
	}
	
	public void setScore(SBRank score) {
		this.score = score;
	}
	
	public String getArrayName() {
		return arrayName;
	}
	
	public void setArrayName(String arrayName) {
		this.arrayName = arrayName;
	}
	
}
