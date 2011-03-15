package alma.scheduling.algorithm.results.dao;

import java.util.HashMap;
import java.util.Map;

import alma.scheduling.algorithm.results.Result;
import alma.scheduling.datamodel.GenericDaoImpl;

/**
 * 
 * Default implementation of the interface for fetching results of a
 * DSA run.
 * 
 * @since ALMA 8.1.0
 * @author javarias
 * $Id: ResultsDaoImpl.java,v 1.2 2011/03/15 22:42:27 dclarke Exp $
 */
public class ResultsDaoImpl extends GenericDaoImpl implements ResultsDao {

	@Override
	public Map<String, Result> getCurrentResults(String arrayName) {
		final Map<String, Result> result = new HashMap<String, Result>();
		// TODO Auto-generated method stub
		return result;
	}

	@Override
	public Map<String, Result> getPreviousResults(String arrayName) {
		final Map<String, Result> result = new HashMap<String, Result>();
		// TODO Auto-generated method stub
		return result;
	}

}
