package alma.scheduling.array.sbSelection;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
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
import java.util.List;

public class DSASelector extends AbstractSelector {

	private DynamicSchedulingAlgorithm dsa = null;
	private String lastPolicy = null;
	private ResultsDao resultsDao = (ResultsDao) DSAContextFactory.getContext()
			.getBean(DSAContextFactory.SCHEDULING_DSA_RESULTS_DAO_BEAN);

	private void notifyWorking(boolean start) {
		final ArrayGUINotification agn = new ArrayGUINotification(
				start? ArrayGUIOperation.CALCULATINGSCORES:
					   ArrayGUIOperation.SCORESREADY,
				array.getDescriptor().policyName,
				String.format("Dynamic Scheduler on %s",
						array.getArrayName()));
		notify(agn);
	}
	
	/*
	 * (non-Javadoc)
	 * @see alma.scheduling.array.sbSelection.Selector#selectNextSB()
	 */
	@Override
	public void selectNextSB() {
		notifyWorking(true);
		try {
			Date runDate = new Date();
			final String thisPolicy = array.getDescriptor().policyName;
			
			if (thisPolicy == null) {
				// There's no policy, give up.
				throw new SchedulingException(
						"Scheduling Policy has not been properly set for Array: "
						+ array.getArrayName());
			}
			
			if (!thisPolicy.equals(lastPolicy)) {
				// It's a new policy, so force a refresh of the dsa bean
				System.out.println(String.format(
						"Policy was %s, now %s%n", lastPolicy, thisPolicy));
				lastPolicy = thisPolicy;
				dsa = null;
			}
			
			if (dsa == null) {
				final DynamicSchedulingAlgorithm lastDSA = dsa;
				try {
					dsa = (DynamicSchedulingAlgorithm) DSAContextFactory.getContextFromPropertyFile().getBean(thisPolicy);
				} catch (NoSuchBeanDefinitionException e) {
					System.out.println("Known Policies");
					System.out.println("--------------");
					final List<String> policies = DSAContextFactory.getPolicyNames();
					for (final String policy : policies) {
						System.out.print('\t');
						System.out.println(policy);
					}
					System.out.println("==============");
					e.printStackTrace();
					dsa = lastDSA; // meaningless, but it keeps us running
				}
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
				result.setScores(results);
				resultsDao.saveOrUpdate(result);
			} catch (NoSbSelectedException e) {
				e.printStackTrace();
			}
		} finally {
			notifyWorking(false);
		}

	}

}
