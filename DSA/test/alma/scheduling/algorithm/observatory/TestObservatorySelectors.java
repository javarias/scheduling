package alma.scheduling.algorithm.observatory;

import java.util.Date;

import alma.scheduling.algorithm.BaseAlgorithmTestCase;
import alma.scheduling.algorithm.DynamicSchedulingAlgorithm;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;
import alma.scheduling.utils.DSAContextFactory;

public class TestObservatorySelectors extends BaseAlgorithmTestCase {
	
	public void testAngularResolutionSelector() throws Exception {
		final String xmlToLoad = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<Policies><SchedulingPolicy name=\"TestPolicy\">\n<SelectionCriteria>\n" +
				"<ArrayAngularResolutionSelector/></SelectionCriteria>\n" +
				"<Scorers></Scorers></SchedulingPolicy></Policies>";
		DynamicSchedulingAlgorithm alg = super.convertAndRetrieveAlgorithmBean(xmlToLoad);
		
		SchedBlockDao sbDao = (SchedBlockDao) DSAContextFactory.getContext().getBean("sbDao");
		SchedBlock tmp = createBasicSB();
		tmp.setExecutive(initializeDummyExecutive((ExecutiveDAO) DSAContextFactory.getContext().getBean("execDao")));
		sbDao.saveOrUpdate(tmp);
		
		System.out.println("Number of SBs: " + sbDao.countAll());
		
		ArrayConfiguration arrConf = new ArrayConfiguration();
		arrConf.setMaxBaseline(442.0D);
		
		alg.setArray(arrConf);
		alg.initialize(new Date());
		alg.selectCandidateSB();
		alg.rankSchedBlocks();
		SchedBlock sb = alg.getSelectedSchedBlock();
		assertEquals("uid://A000/X0/X0", sb.getUid());
	}
	
}
