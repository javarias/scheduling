/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 * "@(#) $Id: ObsProjectDataLoaderImpl.java,v 1.3 2012/05/23 21:51:00 javarias Exp $"
 */
package alma.scheduling.dataload.obsproject;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.datamodel.DAOException;
import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.ExecutiveTimeSpent;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.dao.ObsProjectDao;
import alma.scheduling.datamodel.obsproject.dao.ProjectDao;
import alma.scheduling.datamodel.obsproject.dao.XmlObsProjectDao;
import alma.scheduling.input.executive.generated.ObservingSeason;
import alma.scheduling.utils.SchedulingProperties;

@Transactional
public class ObsProjectDataLoaderImpl implements ObsProjectDataLoader {
    
	private static Logger logger = LoggerFactory.getLogger(ObsProjectDataLoaderImpl.class);
	
	/**
	 * DAO to get Light Projects from files.
	 */
    XmlObsProjectDao xmlDao;
    public void setXmlDao(XmlObsProjectDao xmlDao) {
        this.xmlDao = xmlDao;
    }

    /**
     * DAO to get projects from ARCHIVE XML Store.
     */
    ProjectDao archProjectDao;
    @Override
    public void setArchProjectDao(ProjectDao archProjectDao) {
    	this.archProjectDao  = archProjectDao;
    }
    
    /**
     * DAO to store projects in work database.
     */
    ObsProjectDao dao;
    public void setDao(ObsProjectDao dao) {
        this.dao = dao;
    }

    @Override
    @Transactional
    public void load() {
    	List<ObsProject> projects = null;
    	if (xmlDao != null && SchedulingProperties.isPMSUsingXMLProjects()) {
    		projects = xmlDao.getAllObsProjects();
    	} else if (archProjectDao != null) {
    		try {
    			Date start = new Date();
				projects = archProjectDao.getAllObsProjects();
				Date end = new Date();
				logger.info("To convert projects from archive took " + (end.getTime() - start.getTime()) + " ms");
				logger.info("Total projects: " + projects.size());
			} catch (DAOException ex) {
				logger.error("error getting projects from XML Store");
				ex.printStackTrace();
			}
    	} else {
    		logger.error("no DAO to retrieve projects has been setup");
    		return;
    	}
//    	for(ObsProject p: projects){
//    	    dao.saveOrUpdate(p);
//    	}
    	try{
    		dao.saveOrUpdate(projects);
    	}catch(NullPointerException e){
    		logger.error("List of project to save or update has " + projects.size() + " elements.");
    		if( projects.size() == 0 )
    			logger.error("No obs projects retrived. Please check your sources.");
    		else e.printStackTrace();
    	}
    }

    @Override
    public void clear() {
    	logger.info("Deleting Projects");
    	dao.deleteAll();
    }

}
