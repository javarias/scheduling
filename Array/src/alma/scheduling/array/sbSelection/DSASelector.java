package alma.scheduling.array.sbSelection;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

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
import alma.scheduling.utils.ErrorHandling;

public class DSASelector extends AbstractSelector {
	/*
	 * ================================================================
	 * Fields
	 * ================================================================
	 */
	/** The DynamicSchedulingAlgorithm to use */
	private DynamicSchedulingAlgorithm dsa = null;
	
	/** Which SchedulingPolicy we used last */
	private String lastPolicy = null;
	
	/** DAO to get and store results */
	private ResultsDao resultsDao = (ResultsDao) DSAContextFactory.getContext()
			.getBean(DSAContextFactory.SCHEDULING_DSA_RESULTS_DAO_BEAN);
	
	/** Object to log the DSA results in a separate thread */
	private ResultsLogger resultsLogger = null;
	
	/** Value of N in the sens of logging the top N results */
	private int numToLog = 10;
	
	/** A logger, for, well, you know, logging */
	private Logger logger;
	/* End Fields
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	public DSASelector(Logger logger) {
		super();
		this.logger = logger;
	}
	/* End Construction
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Notifying
	 * ================================================================
	 */
	/**
	 * Notify our listeners about starting and finishing the scoring.
	 * 
	 * @param start - true if it's a notification that we're starting
	 */
	private void notifyWorking(boolean start) {
		final ArrayGUINotification agn = new ArrayGUINotification(
				start? ArrayGUIOperation.CALCULATINGSCORES:
					   ArrayGUIOperation.SCORESREADY,
				array.getDescriptor().policyName,
				String.format("Dynamic Scheduler on %s",
						array.getArrayName()));
		notify(agn);
	}
	/* End Notifying
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * SchedBlock scoring
	 * ================================================================
	 */
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
			
			if (lastPolicy == null) {
				// First time
				resultsLogger = new ResultsLogger(logger,
						                          resultsDao,
												  array.getArrayName(),
												  thisPolicy,
												  numToLog);
			} else {
				// Second or subsequent time
				resultsLogger = new ResultsLogger(logger,
						                          resultsDao,
												  array.getArrayName(),
												  thisPolicy,
												  lastPolicy,
												  numToLog);
			}

			if (!thisPolicy.equals(lastPolicy)) {
				// It's a new policy, so force a refresh of the dsa bean
				logger.info(String.format(
						"Policy was %s, now %s%n", lastPolicy, thisPolicy));
				lastPolicy = thisPolicy;
				dsa = null;
			}
			
			if (dsa == null) {
				final DynamicSchedulingAlgorithm lastDSA = dsa;
				try {
					dsa = (DynamicSchedulingAlgorithm) DSAContextFactory.getContextFromPropertyFile().getBean(thisPolicy);
				} catch (NoSuchBeanDefinitionException e) {
					final StringBuilder b = new StringBuilder();
					b.append('\n');
					b.append("Known Policies\n");
					b.append("--------------\n");
					final List<String> policies = DSAContextFactory.getPolicyNames();
					for (final String policy : policies) {
						b.append('\t').append(policy).append('\n');
					}
					b.append("==============\n\n");

					b.append("All Beans\n");
					b.append("---------\n");
					final String[] beans = DSAContextFactory.getContextFromPropertyFile().getBeanDefinitionNames();
					for (final String bean : beans) {
						b.append('\t').append(bean).append('\n');
					}
					b.append("=========\n");
					b.append(ErrorHandling.printedStackTrace(e));
					logger.info(b.toString());
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
			if (resultsLogger != null) {
				Thread fred = new Thread(resultsLogger,
						resultsLogger.generateThreadName());
				fred.start();
			}
		}

	}
	/* End SchedBlock scoring
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Logging of results
	 * ================================================================
	 */
	@Override
	public int getLogAmount() {
		return numToLog;
	}

	@Override
	public void setLogAmount(int n) {
		numToLog = n;
	}
	/* End Logging of results
	 * ============================================================= */
}
