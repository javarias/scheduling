package alma.scheduling.dataload.obsproject;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.dataload.DataLoader;
import alma.scheduling.datamodel.DAOException;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.dao.ObsProjectDao;
import alma.scheduling.datamodel.obsproject.dao.ProjectDao;

public class AlmaArchiveObsProjectLoaderImpl implements DataLoader {
	private static Logger logger = LoggerFactory.getLogger(AlmaArchiveObsProjectLoaderImpl.class);
	private ProjectDao archiveProjectDao;
	private ObsProjectDao dao;

	
	public ProjectDao getArchiveProjectDao() {
		return archiveProjectDao;
	}

	public void setArchiveProjectDao(ProjectDao archiveProjectDao) {
		this.archiveProjectDao = archiveProjectDao;
	}

	public ObsProjectDao getDao() {
		return dao;
	}

	public void setDao(ObsProjectDao dao) {
		this.dao = dao;
	}

	@Override
	@Transactional(readOnly=false)
	public void load() throws Exception {
		List<ObsProject> projects = null;
		try {
			Date start = new Date();
			projects = archiveProjectDao.getAllObsProjects();
			Date end = new Date();
			logger.info("To convert projects from archive took " + (end.getTime() - start.getTime()) + " ms");
			logger.info("Total projects: " + projects.size());
		} catch (DAOException ex) {
			logger.error("error getting projects from XML Store", ex);
			ex.printStackTrace();
		}
		try{
    		dao.saveOrUpdate(projects);
    	}catch(NullPointerException e){
    		logger.error("List of project to save or update has " + projects.size() + " elements.");
    		if( projects.size() == 0 )
    			logger.error("No obs projects retrived. Please check your sources.");
    		else {
    			logger.error("Unexpected Exception", e);
    			e.printStackTrace();
    		}
    	}
	}

	@Override
	@Transactional(readOnly=false)
	public void clear() {
		logger.info("Deleting projects from local DB...");
		dao.deleteAll();
		logger.info("Deleting complete.");
	}

}
