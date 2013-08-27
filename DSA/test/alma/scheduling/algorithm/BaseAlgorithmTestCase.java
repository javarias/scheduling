package alma.scheduling.algorithm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import alma.entity.xmlbinding.obsproposal.types.InvestigatorTAssociatedExecType;
import alma.scheduling.SchedulingPolicyFile;
import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.ExecutivePercentage;
import alma.scheduling.datamodel.executive.ObservingSeason;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.obsproject.FieldSource;
import alma.scheduling.datamodel.obsproject.FieldSourceObservability;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.SchedBlockControl;
import alma.scheduling.datamodel.obsproject.SchedBlockState;
import alma.scheduling.datamodel.obsproject.SchedulingConstraints;
import alma.scheduling.datamodel.obsproject.Target;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;
import alma.scheduling.utils.DSAContextFactory;
import alma.scheduling.utils.DynamicSchedulingPolicyFactory;
import junit.framework.TestCase;

public abstract class BaseAlgorithmTestCase extends TestCase {

	static {
		if (System.getProperty("alma.scheduling.properties") == null) {
			System.setProperty("alma.scheduling.properties", "Common/src/scheduling.properties");
		}
		DSAContextFactory.getContext();
	}
	
	protected SchedBlock createBasicSB() {
		SchedBlock sb = new SchedBlock();
		sb.setUid("uid://A000/X0/X0");
		sb.setCsv(false);
		sb.setManual(false);
		SchedBlockControl ctrl =  new SchedBlockControl();
		ctrl.setState(SchedBlockState.READY);
		sb.setSchedBlockControl(ctrl);
		SchedulingConstraints c =  new SchedulingConstraints();
		c.setMaxAngularResolution(15.0D);
		c.setMinAngularResolution(0.1D);
		c.setRepresentativeFrequency(345.70447);
		Target t = new Target();
		FieldSource fs = new FieldSource();
		FieldSourceObservability fsobs = new FieldSourceObservability();
		fsobs.setAlwaysVisible(true);
		fsobs.setAlwaysHidden(false);
		fs.setObservability(fsobs);
		t.setSource(fs);
		c.setRepresentativeTarget(t);
		sb.setSchedulingConstraints(c);
		
		return sb;
	}
	
	protected Executive initializeDummyExecutive(ExecutiveDAO execDao) {
    	//Names retrieved from ObsProposal.xsd
    	//String [] executiveNames = {"NONALMA","OTHER", "CL", "CHILE", "EA", "EU", "NA"};
    	ArrayList<String> executiveNames = new ArrayList<String>();
		@SuppressWarnings("rawtypes")
		Enumeration e = InvestigatorTAssociatedExecType.enumerate();
		while(e.hasMoreElements()) {
			InvestigatorTAssociatedExecType apdmExec = (InvestigatorTAssociatedExecType) e.nextElement();
			executiveNames.add(apdmExec.toString());
		}
    	List<Executive> execs = new ArrayList<Executive>();
    	List<ObservingSeason> seasons = new ArrayList<ObservingSeason>();
    	Set<ExecutivePercentage> eps = new HashSet<ExecutivePercentage>();
		ObservingSeason season = new ObservingSeason();
		season.setName("Current Observing Season");
		season.setStartDate(new Date());
		season.setEndDate(new Date(System.currentTimeMillis() + 315360000 ));
		seasons.add(season);
    	for (String execName: executiveNames){
    		Executive exec =  new Executive();
    		exec.setName(execName);
    		double percentage = 0.0;
    		if (executiveNames.size() > 0)
    				percentage = 1.0F/(float)executiveNames.size();
    		exec.setDefaultPercentage((float)percentage);
    		execs.add(exec);
    		ExecutivePercentage ep = new ExecutivePercentage();
    		ep.setExecutive(exec);
    		ep.setSeason(season);
    		ep.setPercentage((float)percentage);
    		ep.setTotalObsTimeForSeason(315360000 * percentage);
    		eps.add(ep);
    	}
    	season.setExecutivePercentage(eps);
    	execDao.saveObservingSeasonsAndExecutives(seasons, execs);
    	return execs.get(0);
	}
	
	protected DynamicSchedulingAlgorithm convertAndRetrieveAlgorithmBean(final String DSAPolicyFileContent) 
			throws TransformerException, SAXException, IOException {
		DynamicSchedulingPolicyFactory pf = DynamicSchedulingPolicyFactory.getInstance();
		String springCtxXml = SchedulingPolicyValidator.convertPolicyString(DSAPolicyFileContent);
		pf.createDSAPolicyBeans("localhost", "nopath", springCtxXml);
		SchedulingPolicyFile spf = PoliciesContainersDirectory.getInstance().getAllPoliciesFiles()[0];
		return (DynamicSchedulingAlgorithm) DSAContextFactory.getContext().getBean("uuid" + spf.uuid + "-" + spf.schedulingPolicies[0]);
	}

	@Override
	protected void tearDown() throws Exception {
		SchedBlockDao sbDao = (SchedBlockDao) DSAContextFactory.getContext().getBean("sbDao");
		sbDao.deleteAll();
		for(SchedulingPolicyFile file: PoliciesContainersDirectory.getInstance().getAllPoliciesFiles())
			PoliciesContainersDirectory.getInstance().remove(
				UUID.fromString(file.uuid));
	}
	
	
}
