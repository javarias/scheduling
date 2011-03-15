package alma.scheduling.algorithm.results.dao;

import java.util.Map;

import alma.scheduling.algorithm.results.Result;

/**
 * 
 * Interface for fetching results of a DSA run.
 * 
 * @since ALMA 8.1.0
 * @author javarias
 * $Id: ResultsDao.java,v 1.2 2011/03/15 22:42:27 dclarke Exp $
 */
public interface ResultsDao {

	/**
	 * Return the latest set of scores and ranks from the DSA for the
	 * given array.
	 * 
	 * @param arrayName - the array to which the results apply
	 * @return Map<String, Result> - the results in a map, keyed by
	 *                               SchedBlock UID.
	 */
	public Map<String, Result> getCurrentResults(String arrayName);

	/**
	 * Return the previous set of scores and ranks from the DSA for the
	 * given array.
	 * 
	 * @param arrayName - the array to which the results applied
	 * @return Map<String, Result> - the results in a map, keyed by
	 *                               SchedBlock UID.
	 */
	public Map<String, Result> getPreviousResults(String arrayName);
	
}
