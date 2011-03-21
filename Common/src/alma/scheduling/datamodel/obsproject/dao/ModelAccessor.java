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
package alma.scheduling.datamodel.obsproject.dao;

import static alma.lifecycle.config.SpringConstants.STATE_SYSTEM_SPRING_CONFIG;
import static alma.scheduling.utils.CommonContextFactory.SCHEDULING_EXECUTIVE_DAO_BEAN;
import static alma.scheduling.utils.CommonContextFactory.SCHEDULING_OBSPROJECT_DAO_BEAN;
import static alma.scheduling.utils.CommonContextFactory.SCHEDULING_SCHEDBLOCK_DAO_BEAN;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Vector;
import java.util.logging.Logger;

import org.springframework.context.support.AbstractApplicationContext;

import alma.ACSErrTypeCommon.wrappers.AcsJIllegalArgumentEx;
import alma.archive.database.helpers.wrappers.StateArchiveDbConfig;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.lifecycle.config.StateSystemContextFactory;
import alma.lifecycle.persistence.StateArchive;
import alma.lifecycle.stateengine.RoleProvider;
import alma.lifecycle.stateengine.RoleProviderMock;
import alma.lifecycle.stateengine.StateEngine;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnit;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.utils.CommonContextFactory;
import alma.scheduling.utils.LoggerFactory;
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
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	private StateArchive stateArchive;
	private StateEngine stateEngine;
	private ObsProjectDao projectDao;
	private SchedBlockDao schedBlockDao;
	private ExecutiveDAO execDao;
		
	public ModelAccessor() throws Exception {
	    // There should be only one instance of the stateArchive in the process.
	    // An implication of this is that this component cannot coexist with
	    // the OBOPS StateArchive in the same container.
		if (!StateSystemContextFactory.INSTANCE.isInitialized()) {
			final RoleProvider roleProvider = new RoleProviderMock();
			StateSystemContextFactory.INSTANCE.init(STATE_SYSTEM_SPRING_CONFIG,
					new StateArchiveDbConfig(logger));
			stateArchive = StateSystemContextFactory.INSTANCE.getStateArchive();
			stateArchive.initStateArchive(logger);
			stateEngine = StateSystemContextFactory.INSTANCE.getStateEngine();
			stateEngine.initStateEngine(logger, stateArchive, roleProvider);
		} else {
            stateArchive = StateSystemContextFactory.INSTANCE.getStateArchive();
			stateEngine = StateSystemContextFactory.INSTANCE.getStateEngine();
		}
		
		logger.info("Loading Context from factory");
		//TODO: Remove this reflection call
		Class<?> factoryClass = Class.forName("alma.scheduling.utils.DSAContextFactory");
		Method m = factoryClass.getMethod("getContext");
        AbstractApplicationContext ctx = (AbstractApplicationContext) m.invoke(factoryClass, (Object [])null);
        projectDao = (ObsProjectDao) ctx.getBean(SCHEDULING_OBSPROJECT_DAO_BEAN);
        schedBlockDao = (SchedBlockDao) ctx.getBean(SCHEDULING_SCHEDBLOCK_DAO_BEAN);
        execDao = (ExecutiveDAO) ctx.getBean(SCHEDULING_EXECUTIVE_DAO_BEAN);
	}
	
	public StateArchive getStateArchive() {
		return stateArchive;
	}
	
	public StateEngine getStateEngine() {
		return stateEngine;
	}

	public ObsProjectDao getObsProjectDao() {
		return projectDao;
	}
	
	public SchedBlockDao getSchedBlockDao() {
		return schedBlockDao;
	}
	
	public ExecutiveDAO getExecutiveDao() {
		return execDao;
	}

	public ProjectStatus[] getAllProjectStatuses()
		throws AcsJIllegalArgumentEx, AcsJInappropriateEntityTypeEx {
		String[] states = { StatusTStateType.ANYSTATE.toString() };
		ProjectStatus[] prjstatuses =
			stateArchive.findProjectStatusByState(states);
		return prjstatuses;
	}
	
	public List<ObsProject> getAllProjects() {
		return getObsProjectDao().findAll(ObsProject.class);
	}
	
	public List<ObsProject> getAllProjects(boolean manual) {
		final List<ObsProject> all = getAllProjects();
		final List<ObsProject> result = new ArrayList<ObsProject>();
		for (final ObsProject op : all) {
			if (op.getManual() == manual) {
				result.add(op);
			}
		}
		return result;
	}
	
	public List<ObsProject> getProjects(String... ids) {
		final ObsProjectDao dao = getObsProjectDao();
		
		final List<ObsProject> result = new Vector<ObsProject>();
		for (final String id : ids) {
			final ObsProject project = dao.findById(ObsProject.class, id);
			result.add(project);
		}
		return result;
	}
	
	public ObsProject getObsProjectFromEntityId(String entityId) {
		final ObsProject op = getObsProjectDao().findByEntityId(entityId);

		return op;
	}
	
	public ObsUnit getObsUnitForProject(ObsProject project) {
		return projectDao.getObsUnitForProject(project);
	}
	
	public void hydrateSchedBlock(SchedBlock sb) {
		schedBlockDao.hydrateSchedBlockObsParams(sb);
//		final ObsProject p = projectDao.getObsProject(sb);
//		sb.setProject(p);
	}

	public List<SchedBlock> getAllSchedBlocks() {
		final List<SchedBlock> sbs = getSchedBlockDao().findAll(SchedBlock.class);
		
		for (final SchedBlock sb : sbs) {
			hydrateSchedBlock(sb);
		}
		return sbs;
	}

	
	public List<SchedBlock> getAllSchedBlocks(boolean manual) {
		final List<SchedBlock> all = getSchedBlockDao().findAll(SchedBlock.class);
		final List<SchedBlock> result = new ArrayList<SchedBlock>();
		for (final SchedBlock sb : all) {
			if (sb.getManual() == manual) {
				hydrateSchedBlock(sb);
				result.add(sb);
			}
		}
		return result;
	}

	public List<SchedBlock> getSchedBlocks(String... ids) {
		final SchedBlockDao dao = getSchedBlockDao();
		
		final List<SchedBlock> result = new Vector<SchedBlock>();
		for (final String id : ids) {
			final SchedBlock schedBlock = dao.findByEntityId(id);
			hydrateSchedBlock(schedBlock);
			result.add(schedBlock);
		}
		return result;
	}
	
	public List<ObsProject> getRelevantProjects() {
	    return null;
	}
	
	public List<SchedBlock> getSchedBlocksForProject(ObsProject project) {
	    return null;
	}
	
	public SchedBlock getSchedBlockFromEntityId(String entityId) {
		final SchedBlock sb = schedBlockDao.findByEntityId(entityId);
		hydrateSchedBlock(sb);
		return sb;
	}
	
}
