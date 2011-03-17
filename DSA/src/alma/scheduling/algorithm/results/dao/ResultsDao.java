package alma.scheduling.algorithm.results.dao;

import java.util.Map;

import alma.scheduling.algorithm.results.Result;

/**
 * 
 * Interface for fetching results of a DSA run.
 * 
 * @since ALMA 8.1.0
 * @author javarias
 * $Id: ResultsDao.java,v 1.3 2011/03/17 22:58:35 javarias Exp $
 */
public interface ResultsDao {

	/**
	 * Return the latest set of scores and ranks from the DSA for the
	 * given array.
	 * 
	 * @param arrayName - the array to which the results apply
	 * @return the current result
	 */
	public Result getCurrentResult(String arrayName);

	/**
	 * Return the previous set of scores and ranks from the DSA for the
	 * given array.
	 * 
	 * @param arrayName - the array to which the results applied
	 * @return the previous result
	 */
	public Result getPreviousResult(String arrayName);
	
	
	public void saveOrUpdate(Result result);
}
