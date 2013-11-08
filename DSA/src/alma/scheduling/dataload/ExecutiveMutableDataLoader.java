package alma.scheduling.dataload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;

/**
 * Clean up the DB to be used again in simulation.
 * 
 * 
 * @author javarias
 *
 */
public class ExecutiveMutableDataLoader implements DataLoader {

	private static Logger logger = LoggerFactory.getLogger(ExecutiveMutableDataLoader.class);
	
	private ExecutiveDAO execDao;
	
	public void setExecDao(ExecutiveDAO execDao) {
		this.execDao = execDao;
	}

	@Override
	public void load() throws Exception {
		//Nothing to load. It is assumed that all executive data has been already loaded into DB
	}

	@Override
	public void clear() {
		logger.info("Starting cleaning of ExecutiveTimeSpent");
		execDao.cleanExecutiveTimeSpent();
		logger.info("Cleaning of ExecutiveTimeSpent completed.");
		
	}

}
