package alma.scheduling.algorithm.observatory;

import java.util.Date;

import alma.scheduling.SchedulingPolicyFile;
import alma.scheduling.algorithm.BaseAlgorithmTestCase;
import alma.scheduling.algorithm.DynamicSchedulingAlgorithm;
import alma.scheduling.algorithm.PoliciesContainersDirectory;
import alma.scheduling.algorithm.SchedulingPolicyValidator;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;
import alma.scheduling.utils.DSAContextFactory;
import alma.scheduling.utils.DynamicSchedulingPolicyFactory;

public class TestObservatorySelectors extends BaseAlgorithmTestCase {

	static {
		if (System.getProperty("alma.scheduling.properties") == null) {
			System.setProperty("alma.scheduling.properties", "Common/src/scheduling.properties");
		}
		DSAContextFactory.getContext();
	}
	
	public void testAngularResolutionSelector() throws Exception {
		DynamicSchedulingPolicyFactory pf = DynamicSchedulingPolicyFactory.getInstance();
		final String xmlToLoad = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<Policies><SchedulingPolicy name=\"TestPolicy\">\n<SelectionCriteria>\n" +
				"<ArrayAngularResolutionSelector/></SelectionCriteria>\n" +
				"<Scorers></Scorers></SchedulingPolicy></Policies>";
		String springCtxXml = SchedulingPolicyValidator.convertPolicyString(xmlToLoad);
		pf.createDSAPolicyBeans("localhost", "nopath", springCtxXml);
		SchedulingPolicyFile spf = PoliciesContainersDirectory.getInstance().getAllPoliciesFiles()[0];
		for (String policies: spf.schedulingPolicies)
			System.out.println(policies);
		DynamicSchedulingAlgorithm alg = (DynamicSchedulingAlgorithm) 
				DSAContextFactory.getContext().getBean("uuid" + spf.uuid + "-" + spf.schedulingPolicies[0]);
		
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
	}
	
}
