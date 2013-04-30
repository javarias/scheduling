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
package alma.scheduling.algorithm.results.dao;

import java.util.ArrayList;

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
