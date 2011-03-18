package alma.scheduling.array.sbSelection;

import org.springframework.scheduling.SchedulingException;

import alma.scheduling.ArrayGUIOperation;
import alma.scheduling.algorithm.DynamicSchedulingAlgorithm;
import alma.scheduling.algorithm.results.Result;
import alma.scheduling.algorithm.results.dao.ResultsDao;
import alma.scheduling.algorithm.sbranking.SBRank;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.array.guis.ArrayGUINotification;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.utils.DSAContextFactory;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class DSASelector extends AbstractSelector {

	private DynamicSchedulingAlgorithm dsa = null;
	private ResultsDao resultsDao = (ResultsDao) DSAContextFactory.getContext()
			.getBean(DSAContextFactory.SCHEDULING_DSA_RESULTS_DAO_BEAN);

	private void notifyWorking(boolean start) {
		final ArrayGUINotification agn = new ArrayGUINotification(
				start? ArrayGUIOperation.CALCULATINGSCORES:
					   ArrayGUIOperation.SCORESREADY,
				array.getSchedulingPolicy(),
				String.format("Dynamic Scheduler on %s",
						array.getArrayName()));
		notify(agn);
	}
	
	@Override
	public void selectNextSB() {
		notifyWorking(true);
		try {
			Date runDate = new Date();
			if (dsa == null) {
				if (array.getSchedulingPolicy() == null)
					throw new SchedulingException(
							"Scheduling Policy has not been properly set for Array: "
							+ array.getArrayName());
				dsa = (DynamicSchedulingAlgorithm) DSAContextFactory.getContext().getBean(array.getSchedulingPolicy());
				ArrayConfiguration arrConf = new ArrayConfiguration();
				arrConf.setStartTime(new Date());
				arrConf.setEndTime(new Date(System.currentTimeMillis() + 365 * 24 * 3600 * 1000));
				dsa.setArray(arrConf);
			}

			dsa.initialize(runDate);
			try {
				dsa.selectCandidateSB(runDate);
				dsa.updateCandidateSB(runDate);
				List<SBRank> results = dsa.rankSchedBlocks(runDate);
				Result result = new Result();
				result.setArrayName(array.getArrayName());
				result.setTime(runDate);
				result.setScores(new HashSet<SBRank>(results));
				resultsDao.saveOrUpdate(result);
			} catch (NoSbSelectedException e) {
				e.printStackTrace();
			}
		} finally {
			notifyWorking(false);
		}

	}

}
