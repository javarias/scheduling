/* ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2010
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 */
package alma.scheduling.array.guis;

import static alma.lifecycle.config.SpringConstants.STATE_SYSTEM_SPRING_CONFIG;

import java.util.List;
import java.util.Observable;
import java.util.logging.Logger;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import alma.ACSErrTypeCommon.wrappers.AcsJIllegalArgumentEx;
import alma.archive.database.helpers.wrappers.StateArchiveDbConfig;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.lifecycle.config.StateSystemContextFactory;
import alma.lifecycle.persistence.StateArchive;
import alma.scheduling.array.util.LoggerFactory;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnit;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.ObsProjectDao;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;
import alma.statearchiveexceptions.wrappers.AcsJInappropriateEntityTypeEx;

/**
 * A class to be used by GUI models to get access to data held in the database. Currently
 * this includes the Scheduling (internal) data model and the State Archive.
 * 
 * This class gets all the DAOs neededto get data from the database, encapsulating the
 * Spring interaction. It also provides methods to facilitate retrieving the data.
 * 
 * It notifies Observers about changes in the data model.
 * 
 * @author rhiriart
 *
 */
public class ModelAccessor extends Observable {
	
	public static final String SCHEDULING_COMMON_SPRING_CONFIG =  "alma/scheduling/CommonContext.xml";
	public static final String SCHEDULING_OBS_PROJECT_DAO_BEAN = "obsProjectDao";
	public static final String SCHEDULING_SCHED_BLOCK_BEAN = "schedBlockDao";
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	private StateArchive stateArchive;
	private ObsProjectDao projectDao;
	private SchedBlockDao schedBlockDao;
		
	public ModelAccessor() throws Exception {
	    // There should be only one instance of the stateArchive in the process.
	    // An implication of this is that this component cannot coexist with
	    // the OBOPS StateArchive in the same container.
		if (!StateSystemContextFactory.INSTANCE.isInitialized()) {
			StateSystemContextFactory.INSTANCE.init(STATE_SYSTEM_SPRING_CONFIG,
					new StateArchiveDbConfig(logger));
			stateArchive = StateSystemContextFactory.INSTANCE.getStateArchive();
			stateArchive.initStateArchive(logger);
		} else {
            stateArchive = StateSystemContextFactory.INSTANCE.getStateArchive();
		}
		
        ApplicationContext ctx =
            new ClassPathXmlApplicationContext(SCHEDULING_COMMON_SPRING_CONFIG);
        projectDao = (ObsProjectDao) ctx.getBean(SCHEDULING_OBS_PROJECT_DAO_BEAN);
        schedBlockDao = (SchedBlockDao) ctx.getBean(SCHEDULING_SCHED_BLOCK_BEAN);
	}
	
	public StateArchive getStateArchive() {
		return stateArchive;
	}

	public ObsProjectDao getObsProjectDao() {
		return projectDao;
	}
	
	public SchedBlockDao getSchedBlockDao() {
		return schedBlockDao;
	}

	public ProjectStatus[] getAllProjectstatuses()
		throws AcsJIllegalArgumentEx, AcsJInappropriateEntityTypeEx {
		String[] states = { StatusTStateType.ANYSTATE.toString() };
		ProjectStatus[] prjstatuses =
			stateArchive.findProjectStatusByState(states);
		return prjstatuses;
	}
	
	public List<ObsProject> getAllProjects() {
		return getObsProjectDao().findAll(ObsProject.class);
	}
	
	public ObsUnit getObsUnitForProject(ObsProject project) {
		return projectDao.getObsUnitForProject(project);
	}
	
	public void hydrateSchedBlock(SchedBlock sb) {
		schedBlockDao.hydrateSchedBlockObsParams(sb);
	}
	
	public List<ObsProject> getRelevantProjects() {
	    return null;
	}
	
	public List<SchedBlock> getSchedBlocksForProject(ObsProject project) {
	    return null;
	}
	
	public SchedBlock getSchedBlockFromEntityId(String entityId) {
	    return schedBlockDao.findByEntityId(entityId);
	}
	
}
