/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
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
