package alma.scheduling.algorithm.results;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


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
	private List<SBRank> scores = new ArrayList<SBRank>();
	
	public Date getTime() {
		return time;
	}
	
	public void setTime(Date time) {
		this.time = time;
	}
	
	// **** NOTE
	//
	// InteractivePanel.getScoresAndRanks() goes to a bit of trouble
	// to sort the result it gets from this method. So, if you change
	// this method to return the scores already sorted, either stop
	// the InteractivePanel duplicating this effort or tell David and
	// he'll do it.
	public List<SBRank> getScores() {
		return scores;
	}
	
	public void setScores(List<SBRank> scores) {
		this.scores = scores;
	}
	
	public String getArrayName() {
		return arrayName;
	}
	
	public void setArrayName(String arrayName) {
		this.arrayName = arrayName;
	}
	
}
