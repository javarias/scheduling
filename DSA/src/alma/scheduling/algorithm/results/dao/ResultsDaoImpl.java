package alma.scheduling.algorithm.results.dao;

import org.hibernate.Query;

import alma.scheduling.algorithm.results.Result;
import alma.scheduling.datamodel.GenericDaoImpl;

/**
 * 
 * Default implementation of the interface for fetching results of a
 * DSA run.
 * 
 * @since ALMA 8.1.0
 * @author javarias
 * $Id: ResultsDaoImpl.java,v 1.4 2011/03/21 22:07:53 javarias Exp $
 */
public class ResultsDaoImpl extends GenericDaoImpl implements ResultsDao {

	@Override
	public void saveOrUpdate(Result result) {
		Query query = getSession().createQuery("select res from Result where res.array = ? order by res.time desc");
		query.setParameter(0, result.getArrayName());
		try {
			Result old = (Result) query.list().get(1);
			delete(old);
		} catch (IndexOutOfBoundsException ex) {
			//Do nothing there are no old results
		}
		saveOrUpdate(result);
	}

	@Override
	public Result getCurrentResult(String arrayName) {
		Query query = getSession().createQuery("select res from Result where res.array = ? order by res.time desc");
		query.setParameter(0, arrayName);
		return (Result) query.list().get(0);
	}

	@Override
	public Result getPreviousResult(String arrayName) {
		Query query = getSession().createQuery("select res from Result where res.array = ? order by res.time desc");
		query.setParameter(0, arrayName);
		return (Result) query.list().get(1);
	}

}
