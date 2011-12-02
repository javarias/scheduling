/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
package alma.scheduling.algorithm.results;

import java.util.ArrayList;
import java.util.Collections;
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
	// InteractivePanel.getScoresAndRanks(), the whole ResultsLogger
	// class and possibly others assume that this method returns a
	// sorted list.
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
	
	/**
	 * Sort the scores added to this result.
	 * The order will be descending according to the score obtained from the DSA
	 */
	public synchronized void sortScores() {
		if (scores.size() == 0)
			return;
		Collections.sort(scores);
		ArrayList<SBRank> descSortedList =  new ArrayList<SBRank>(scores.size());
		for (int i = scores.size() - 1; i >= 0; i--){
			descSortedList.add(scores.get(i));
		}
		scores = descSortedList;
	}
	
}
