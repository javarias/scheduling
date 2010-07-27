package alma.scheduling.array.guis;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnit;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.ObsProjectDao;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;

public class ObsProjectModel {
	
	private List<ObsProject> projects;
	private ObsProjectDao projectDao;
	private SchedBlockDao schedBlockDao;

	public ObsProjectModel() {
        ApplicationContext ctx =
            new ClassPathXmlApplicationContext("alma/scheduling/CommonContext.xml");
        projectDao = (ObsProjectDao) ctx.getBean("obsProjectDao");
        schedBlockDao = (SchedBlockDao) ctx.getBean("schedBlockDao");
        projects = projectDao.findAll(ObsProject.class);
	}
	
	public List<ObsProject> getProjects() {
		return projects;
	}
	
	public ObsProject getObsProjectAt(int idx) {
		return projects.get(idx);
	}
	
	public ObsUnit getObsUnitForProject(ObsProject project) {
		return projectDao.getObsUnitForProject(project);
	}
	
	public void hydrateSchedBlock(SchedBlock sb) {
		schedBlockDao.hydrateSchedBlockObsParams(sb);
	}
}
