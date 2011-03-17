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
 * $Id: ResultsDaoImpl.java,v 1.3 2011/03/17 22:58:35 javarias Exp $
 */
public class ResultsDaoImpl extends GenericDaoImpl implements ResultsDao {

	@Override
	public void saveOrUpdate(Result result) {
		saveOrUpdate(result);
	}

	@Override
	public Result getCurrentResult(String arrayName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result getPreviousResult(String arrayName) {
		// TODO Auto-generated method stub
		return null;
	}

}
