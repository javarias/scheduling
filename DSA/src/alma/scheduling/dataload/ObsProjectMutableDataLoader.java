package alma.scheduling.dataload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.datamodel.obsproject.dao.ObsProjectDao;

public class ObsProjectMutableDataLoader implements DataLoader {

	private static Logger logger = LoggerFactory.getLogger(ObsProjectMutableDataLoader.class);
	
	private ObsProjectDao obsProjectDao;
	
	public void setObsProjectDao(ObsProjectDao obsProjectDao) {
		this.obsProjectDao = obsProjectDao;
	}

	@Override
	public void load() throws Exception {
		//Nothing to load. It is assumed that all the projects have been already loaded into DB
	}

	@Override
	public void clear() {
		logger.info("Starting cleaning of ObsProjects");
		obsProjectDao.setObsProjectStatusAsReady();
		logger.info("Cleaning of ObsProjects completed.");
	}

}
