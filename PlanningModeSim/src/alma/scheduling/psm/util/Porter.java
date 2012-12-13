 package alma.scheduling.psm.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.dao.ObsProjectDao;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;
import alma.scheduling.datamodel.obsproject.dao.XmlObsProjectDao;
import alma.scheduling.psm.sim.InputActions;

/** Porter utility applications. Acts as an IMporter, and EXporter
 * for scheduling data sources.
 * Useful when the online data needs to be use in offline simulations.
 */
public class Porter extends PsmContext {

	static Porter instance = null;
	private static final Logger logger = LoggerFactory.getLogger(Porter.class);
	
	private Porter(String workDir) {
		super(workDir);
	}

	public void obsprojectExport(){
		ApplicationContext ctx = getApplicationContext();
		ObsProjectDao obsProjectDao = (ObsProjectDao)ctx.getBean("obsProjectDao");
		SchedBlockDao sbDao = (SchedBlockDao)ctx.getBean("sbDao");
		XmlObsProjectDao xmlObsProjectDao = (XmlObsProjectDao)ctx.getBean("xmlObsProjectDao");
		
		List<ObsProject> obsProjects = obsProjectDao.getObsProjectsOrderBySciRank();
		
		for( ObsProject tmpObsProject : obsProjects ){
			logger.info("Exporting ObsProject: " + tmpObsProject.getCode() + " (" + tmpObsProject.getUid() + ")");
			obsProjectDao.hydrateSchedBlocks( tmpObsProject );
			xmlObsProjectDao.saveObsProject( tmpObsProject );
		}
		
	}
	
    
	public static Porter getInstance(String workDir){
		if (Porter.instance == null)
			Porter.instance = new Porter(workDir);
		return Porter.instance;
	}

}
