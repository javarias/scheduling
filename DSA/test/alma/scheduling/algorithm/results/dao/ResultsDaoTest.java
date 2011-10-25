package alma.scheduling.algorithm.results.dao;

import java.util.ArrayList;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import alma.scheduling.algorithm.results.Result;
import alma.scheduling.algorithm.sbranking.SBRank;
import alma.scheduling.utils.DSAContextFactory;

import static org.junit.Assert.*;

public class ResultsDaoTest{

	private ResultsDao resultsDao;
	
	@Before public void setUp() throws Exception {
		resultsDao = (ResultsDao) DSAContextFactory.getContext().getBean(DSAContextFactory.SCHEDULING_DSA_RESULTS_DAO_BEAN);
	}
	
	@After public void tearDown() throws Exception {
	}

	
	@Test public void TestSaveData() {
		Result res = new Result();
		res.setArrayName("TestArray");
		res.setTime(new Date());
		
		ArrayList<SBRank> scores = new ArrayList<SBRank>();
		for (int i = 0; i < 100; i++) {
			SBRank score = new SBRank();
			score.setUid("uid://Test/UID/X"+i);
			score.setRank(i);
			for (int j = 0; j < 10; j++) {
				SBRank bScore = new SBRank();
				bScore.setUid("uid://Test/UID/X"+i);
				bScore.setRank(i);
				score.addRank(bScore);
			}
			scores.add(score);
		}
		res.setScores(scores);
		resultsDao.saveOrUpdate(res);
	}
	
	@Test public void TestRetriveCurrentResults () {
		Result res = resultsDao.getCurrentResult("TestArray");
		assertEquals(100, res.getScores().size());
		for(int i = 0; i < 100; i++) {
			assertEquals(10, res.getScores().get(i).getBreakdownScore().size());
		}
	}
	
	@Test public void TestSortScoresResults () {
		Result res = resultsDao.getCurrentResult("TestArray");
		res.sortScores();
		for (int i = 0; i < 100; i++) {
			assertEquals(100 - 1 - i, res.getScores().get(i).getRank(), 0.0);
		}
	}
	
}
