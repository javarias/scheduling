package alma.scheduling.algorithm.results;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
	 * {@link SBRank}
	 * 
	 *  @see SBRank
	 */
	private Set<SBRank> scores = new HashSet<SBRank>();
	
	public Date getTime() {
		return time;
	}
	
	public void setTime(Date time) {
		this.time = time;
	}
	
	public Set<SBRank> getScore() {
		return scores;
	}
	
	public void setScore(Set<SBRank> scores) {
		this.scores = scores;
	}
	
	public String getArrayName() {
		return arrayName;
	}
	
	public void setArrayName(String arrayName) {
		this.arrayName = arrayName;
	}
	
}
