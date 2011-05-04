package alma.scheduling.algorithm.results.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.hibernate.Query;
import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.algorithm.results.Result;
import alma.scheduling.algorithm.sbranking.SBRank;
import alma.scheduling.datamodel.GenericDaoImpl;

/**
 * 
 * Default implementation of the interface for fetching results of a
 * DSA run.
 * 
 * @since ALMA 8.1.0
 * @author javarias
 * $Id: ResultsDaoImpl.java,v 1.7 2011/05/04 23:21:21 javarias Exp $
 */
public class ResultsDaoImpl extends GenericDaoImpl implements ResultsDao {

	@Override
	@Transactional(readOnly=false)
	public void saveOrUpdate(Result result) {
		Query query = getSession().createQuery("select res from Result as res where res.arrayName = ? order by res.time desc");
		query.setParameter(0, result.getArrayName());
		try {
			Result old = (Result) query.list().get(1);
			super.delete(old);
		} catch (IndexOutOfBoundsException ex) {
			//Do nothing there are no old results
		}
//		for (SBRank score: result.getScores()) {
//			super.saveOrUpdate(score);
//			for(SBRank breakdownScore: score.getBreakdownScore())
//				super.saveOrUpdate(breakdownScore);
//		}
		super.saveOrUpdate(result);
	}

	@Override
	public Result getCurrentResult(String arrayName) {
		Query query = getSession().createQuery("select res from Result as res where res.arrayName = ? order by res.time desc");
		query.setParameter(0, arrayName);
		try {
			return (Result) query.list().get(0);
		} catch (IndexOutOfBoundsException ex) {
			Result retVal = new Result();
			retVal.setScores(new ArrayList<SBRank>());
			return retVal;
		}
	}

	@Override
	public Result getPreviousResult(String arrayName) {
		Query query = getSession().createQuery("select res from Result as res where res.arrayName = ? order by res.time desc");
		query.setParameter(0, arrayName);
		try {
			return (Result) query.list().get(1);
		} catch (IndexOutOfBoundsException ex) {
			Result retVal = new Result();
			retVal.setScores(new ArrayList<SBRank>());
			return retVal;
		}
	}

}
