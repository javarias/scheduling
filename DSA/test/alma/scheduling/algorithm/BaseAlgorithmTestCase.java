package alma.scheduling.algorithm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.xml.transform.TransformerException;

import org.springframework.beans.BeansException;
import org.xml.sax.SAXException;

import alma.scheduling.SchedulingPolicyFile;
import alma.scheduling.dataload.DataLoader;
import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.ExecutivePercentage;
import alma.scheduling.datamodel.executive.InvestigatorTAssociatedExecType;
import alma.scheduling.datamodel.executive.ObservingSeason;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.ArrayType;
import alma.scheduling.datamodel.obsproject.FieldSource;
import alma.scheduling.datamodel.obsproject.FieldSourceObservability;
import alma.scheduling.datamodel.obsproject.ObsUnitControl;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.SchedBlockControl;
import alma.scheduling.datamodel.obsproject.SchedBlockState;
import alma.scheduling.datamodel.obsproject.SchedulingConstraints;
import alma.scheduling.datamodel.obsproject.SkyCoordinates;
import alma.scheduling.datamodel.obsproject.Target;
import alma.scheduling.datamodel.obsproject.dao.ObsProjectDao;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;
import alma.scheduling.utils.DSAContextFactory;
import alma.scheduling.utils.DynamicSchedulingPolicyFactory;
import junit.framework.TestCase;

public abstract class BaseAlgorithmTestCase extends TestCase {

	static {
		if (System.getProperty("alma.scheduling.properties") == null) {
			System.setProperty("alma.scheduling.properties", "Common/src/scheduling.properties");
		}
		try {
			((DataLoader)(DSAContextFactory.getContext().getBean("weatherDataLoader"))).load();
			((DataLoader)(DSAContextFactory.getContext().getBean("weatherSimDataLoader"))).load();
		} catch (BeansException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		SkyCoordinates coord = new SkyCoordinates();
		coord.setRA(12.5);
		coord.setDec(-60.0);
		fs.setCoordinates(coord);
		t.setSource(fs);
		c.setRepresentativeTarget(t);
		sb.setSchedulingConstraints(c);
		
		ObsUnitControl obc = new ObsUnitControl();
		obc.setArrayRequested(ArrayType.SEVEN_M);
		sb.setObsUnitControl(obc);
		return sb;
	}
	
	protected Executive initializeDummyExecutive(ExecutiveDAO execDao) {
    	//Names retrieved from ObsProposal.xsd
    	//String [] executiveNames = {"NONALMA","OTHER", "CL", "CHILE", "EA", "EU", "NA"};
    	ArrayList<String> executiveNames = new ArrayList<String>();
		for(InvestigatorTAssociatedExecType e: InvestigatorTAssociatedExecType.values() ) {
			executiveNames.add(e.toString());
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
	
	protected ArrayConfiguration initializeBasicArrayConfiguration() {
		ArrayConfiguration arrConf = new ArrayConfiguration();
		arrConf.setArrayType(ArrayType.SEVEN_M);
		return arrConf;
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
		ObsProjectDao prjDao = (ObsProjectDao) DSAContextFactory.getContext().getBean("obsProjectDao");
		SchedBlockDao sbDao = (SchedBlockDao) DSAContextFactory.getContext().getBean("sbDao");
		prjDao.deleteAll();
		sbDao.deleteAll();
		for(SchedulingPolicyFile file: PoliciesContainersDirectory.getInstance().getAllPoliciesFiles())
			PoliciesContainersDirectory.getInstance().remove(
				UUID.fromString(file.uuid));
	}
	
	
}
