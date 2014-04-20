package alma.scheduling.algorithm.obsproject;

import java.util.Calendar;
import java.util.HashSet;
import java.util.TimeZone;

import alma.scheduling.algorithm.BaseAlgorithmTestCase;
import alma.scheduling.algorithm.DynamicSchedulingAlgorithm;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.TemporalConstraint;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;
import alma.scheduling.utils.DSAContextFactory;

public class TestFieldSourceObservableSelector extends BaseAlgorithmTestCase {

	private static final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	
	
	public void testTimeConstrainedSelection() throws Exception {
		final String xmlToLoad = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<Policies><SchedulingPolicy name=\"TestPolicy\">\n<SelectionCriteria>\n" +
				"</SelectionCriteria>\n" +
				"<Scorers></Scorers></SchedulingPolicy></Policies>";
		DynamicSchedulingAlgorithm alg = super.convertAndRetrieveAlgorithmBean(xmlToLoad);
		
		SchedBlockDao sbDao = (SchedBlockDao) DSAContextFactory.getContext().getBean("sbDao");
		SchedBlock tmp = createBasicSB();
		tmp.setExecutive(initializeDummyExecutive((ExecutiveDAO) DSAContextFactory.getContext().getBean("execDao")));
		HashSet<TemporalConstraint> tcs = new HashSet<TemporalConstraint>();
		TemporalConstraint tc = new TemporalConstraint();
		cal.setTimeInMillis(0);
		cal.set(2013, 0, 1, 0, 0);
		tc.setStartTime(cal.getTime());
		cal.set(2013, 1, 1, 0, 0);
		tc.setEndTime(cal.getTime());
		tcs.add(tc);
		tmp.setTemporalConstraints(tcs);
		sbDao.saveOrUpdate(tmp);
		
		System.out.println("Number of SBs: " + sbDao.countAll());
		
		ArrayConfiguration arrConf = new ArrayConfiguration();
		arrConf.setMaxBaseline(442.0D);
		
		cal.set(2013, 0, 15, 0, 0);
		alg.setArray(arrConf);
		alg.initialize(cal.getTime());
		alg.selectCandidateSB();
		alg.updateCandidateSB(cal.getTime());
		alg.rankSchedBlocks();
		SchedBlock sb = alg.getSelectedSchedBlock();
		assertEquals("uid://A000/X0/X0", sb.getUid());
	}
	
	public void testTimeConstrainedSelectionFailure() throws Exception {
		final String xmlToLoad = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<Policies><SchedulingPolicy name=\"TestPolicy\">\n<SelectionCriteria>\n" +
				"</SelectionCriteria>\n" +
				"<Scorers></Scorers></SchedulingPolicy></Policies>";
		DynamicSchedulingAlgorithm alg = super.convertAndRetrieveAlgorithmBean(xmlToLoad);
		
		SchedBlockDao sbDao = (SchedBlockDao) DSAContextFactory.getContext().getBean("sbDao");
		SchedBlock tmp = createBasicSB();
		tmp.setExecutive(initializeDummyExecutive((ExecutiveDAO) DSAContextFactory.getContext().getBean("execDao")));
		HashSet<TemporalConstraint> tcs = new HashSet<TemporalConstraint>();
		TemporalConstraint tc = new TemporalConstraint();
		cal.setTimeInMillis(0);
		cal.set(2013, 0, 1, 0, 0);
		tc.setStartTime(cal.getTime());
		cal.set(2013, 1, 1, 0, 0);
		tc.setEndTime(cal.getTime());
		tcs.add(tc);
		tmp.setTemporalConstraints(tcs);
		sbDao.saveOrUpdate(tmp);
		
		System.out.println("Number of SBs: " + sbDao.countAll());
		
		ArrayConfiguration arrConf = new ArrayConfiguration();
		arrConf.setMaxBaseline(442.0D);
		
		cal.set(2013, 4, 15, 0, 0);
		alg.setArray(arrConf);
		alg.initialize(cal.getTime());
		alg.selectCandidateSB();
		try {
			alg.updateCandidateSB(cal.getTime());
		} catch (NoSbSelectedException ex) {
			return;
		}
		assertEquals(true, false);
	}
	
	public void testTimeConstrainedSelectionNoEnd() throws Exception {
		final String xmlToLoad = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<Policies><SchedulingPolicy name=\"TestPolicy\">\n<SelectionCriteria>\n" +
				"</SelectionCriteria>\n" +
				"<Scorers></Scorers></SchedulingPolicy></Policies>";
		DynamicSchedulingAlgorithm alg = super.convertAndRetrieveAlgorithmBean(xmlToLoad);
		
		SchedBlockDao sbDao = (SchedBlockDao) DSAContextFactory.getContext().getBean("sbDao");
		SchedBlock tmp = createBasicSB();
		tmp.setExecutive(initializeDummyExecutive((ExecutiveDAO) DSAContextFactory.getContext().getBean("execDao")));
		HashSet<TemporalConstraint> tcs = new HashSet<TemporalConstraint>();
		TemporalConstraint tc = new TemporalConstraint();
		cal.setTimeInMillis(0);
		cal.set(2013, 3, 1, 0, 0);
		tc.setStartTime(cal.getTime());
		tcs.add(tc);
		tmp.setTemporalConstraints(tcs);
		sbDao.saveOrUpdate(tmp);
		
		System.out.println("Number of SBs: " + sbDao.countAll());
		
		ArrayConfiguration arrConf = new ArrayConfiguration();
		arrConf.setMaxBaseline(442.0D);
		
		cal.set(2013, 4, 15, 0, 0);
		alg.setArray(arrConf);
		alg.initialize(cal.getTime());
		alg.selectCandidateSB();
		alg.updateCandidateSB(cal.getTime());
		alg.rankSchedBlocks();
		SchedBlock sb = alg.getSelectedSchedBlock();
		assertEquals("uid://A000/X0/X0", sb.getUid());
	}
	
	public void testTimeConstrainedSelectionNoEndFailure() throws Exception {
		final String xmlToLoad = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<Policies><SchedulingPolicy name=\"TestPolicy\">\n<SelectionCriteria>\n" +
				"</SelectionCriteria>\n" +
				"<Scorers></Scorers></SchedulingPolicy></Policies>";
		DynamicSchedulingAlgorithm alg = super.convertAndRetrieveAlgorithmBean(xmlToLoad);
		
		SchedBlockDao sbDao = (SchedBlockDao) DSAContextFactory.getContext().getBean("sbDao");
		SchedBlock tmp = createBasicSB();
		tmp.setExecutive(initializeDummyExecutive((ExecutiveDAO) DSAContextFactory.getContext().getBean("execDao")));
		HashSet<TemporalConstraint> tcs = new HashSet<TemporalConstraint>();
		TemporalConstraint tc = new TemporalConstraint();
		cal.setTimeInMillis(0);
		cal.set(2013, 3, 1, 0, 0);
		tc.setStartTime(cal.getTime());
		tcs.add(tc);
		tmp.setTemporalConstraints(tcs);
		sbDao.saveOrUpdate(tmp);
		
		System.out.println("Number of SBs: " + sbDao.countAll());
		
		ArrayConfiguration arrConf = new ArrayConfiguration();
		arrConf.setMaxBaseline(442.0D);
		
		cal.set(2013, 1, 15, 0, 0);
		alg.setArray(arrConf);
		alg.initialize(cal.getTime());
		alg.selectCandidateSB();
		try {
			alg.updateCandidateSB(cal.getTime());
		} catch (NoSbSelectedException ex) {
			return;
		}
		assertEquals(true, false);
	}

	public void testTimeConstrainedSelectionNoStart() throws Exception {
		final String xmlToLoad = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<Policies><SchedulingPolicy name=\"TestPolicy\">\n<SelectionCriteria>\n" +
				"</SelectionCriteria>\n" +
				"<Scorers></Scorers></SchedulingPolicy></Policies>";
		DynamicSchedulingAlgorithm alg = super.convertAndRetrieveAlgorithmBean(xmlToLoad);
		
		SchedBlockDao sbDao = (SchedBlockDao) DSAContextFactory.getContext().getBean("sbDao");
		SchedBlock tmp = createBasicSB();
		tmp.setExecutive(initializeDummyExecutive((ExecutiveDAO) DSAContextFactory.getContext().getBean("execDao")));
		HashSet<TemporalConstraint> tcs = new HashSet<TemporalConstraint>();
		TemporalConstraint tc = new TemporalConstraint();
		cal.setTimeInMillis(0);
		cal.set(2013, 1, 1, 0, 0);
		tc.setEndTime(cal.getTime());
		tcs.add(tc);
		tmp.setTemporalConstraints(tcs);
		sbDao.saveOrUpdate(tmp);
		
		System.out.println("Number of SBs: " + sbDao.countAll());
		
		ArrayConfiguration arrConf = new ArrayConfiguration();
		arrConf.setMaxBaseline(442.0D);
		
		cal.set(2013, 0, 15, 0, 0);
		alg.setArray(arrConf);
		alg.initialize(cal.getTime());
		alg.selectCandidateSB();
		alg.updateCandidateSB(cal.getTime());
		alg.rankSchedBlocks();
		SchedBlock sb = alg.getSelectedSchedBlock();
		assertEquals("uid://A000/X0/X0", sb.getUid());
	}
	
	public void testTimeConstrainedSelectionNoStartFailure() throws Exception {
		final String xmlToLoad = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<Policies><SchedulingPolicy name=\"TestPolicy\">\n<SelectionCriteria>\n" +
				"</SelectionCriteria>\n" +
				"<Scorers></Scorers></SchedulingPolicy></Policies>";
		DynamicSchedulingAlgorithm alg = super.convertAndRetrieveAlgorithmBean(xmlToLoad);
		
		SchedBlockDao sbDao = (SchedBlockDao) DSAContextFactory.getContext().getBean("sbDao");
		SchedBlock tmp = createBasicSB();
		tmp.setExecutive(initializeDummyExecutive((ExecutiveDAO) DSAContextFactory.getContext().getBean("execDao")));
		HashSet<TemporalConstraint> tcs = new HashSet<TemporalConstraint>();
		TemporalConstraint tc = new TemporalConstraint();
		cal.setTimeInMillis(0);
		cal.set(2013, 1, 1, 0, 0);
		tc.setEndTime(cal.getTime());
		tcs.add(tc);
		tmp.setTemporalConstraints(tcs);
		sbDao.saveOrUpdate(tmp);
		
		System.out.println("Number of SBs: " + sbDao.countAll());
		
		ArrayConfiguration arrConf = new ArrayConfiguration();
		arrConf.setMaxBaseline(442.0D);
		
		cal.set(2013, 1, 15, 0, 0);
		alg.setArray(arrConf);
		alg.initialize(cal.getTime());
		alg.selectCandidateSB();
		try {
			alg.updateCandidateSB(cal.getTime());
		} catch (NoSbSelectedException ex) {
			return;
		}
		assertEquals(true, false);
	}
	
	public void testNoTimeConstrainedSelection() throws Exception {
		final String xmlToLoad = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<Policies><SchedulingPolicy name=\"TestPolicy\">\n<SelectionCriteria>\n" +
				"</SelectionCriteria>\n" +
				"<Scorers></Scorers></SchedulingPolicy></Policies>";
		DynamicSchedulingAlgorithm alg = super.convertAndRetrieveAlgorithmBean(xmlToLoad);
		
		SchedBlockDao sbDao = (SchedBlockDao) DSAContextFactory.getContext().getBean("sbDao");
		SchedBlock tmp = createBasicSB();
		tmp.setExecutive(initializeDummyExecutive((ExecutiveDAO) DSAContextFactory.getContext().getBean("execDao")));
		sbDao.saveOrUpdate(tmp);
		
		System.out.println("Number of SBs: " + sbDao.countAll());
		
		ArrayConfiguration arrConf = new ArrayConfiguration();
		arrConf.setMaxBaseline(442.0D);
		
		cal.set(2013, 0, 15, 0, 0);
		alg.setArray(arrConf);
		alg.initialize(cal.getTime());
		alg.selectCandidateSB();
		alg.updateCandidateSB(cal.getTime());
		alg.rankSchedBlocks();
		SchedBlock sb = alg.getSelectedSchedBlock();
		assertEquals("uid://A000/X0/X0", sb.getUid());
	}
}
