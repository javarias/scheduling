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
 * "@(#) $Id: ObsProjectDataUnloader.java,v 1.1 2010/04/07 16:56:23 rhiriart Exp $"
 */
package alma.scheduling.dataload.obsproject;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.dataload.DataUnloader;
import alma.scheduling.dataload.DataUnloaderTest;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.dao.ObsProjectDao;
import alma.scheduling.datamodel.obsproject.dao.XmlObsProjectDao;

@Transactional
public class ObsProjectDataUnloader implements DataUnloader {
    
    private static Logger logger = LoggerFactory.getLogger(ObsProjectDataUnloader.class);
    
    XmlObsProjectDao xmlDao;
    public void setXmlDao(XmlObsProjectDao xmlDao) {
        this.xmlDao = xmlDao;
    }

    ObsProjectDao dao;
    public void setDao(ObsProjectDao dao) {
        this.dao = dao;
    }

    @Override
    public void unload() {
        List<ObsProject> projects = dao.findAll(ObsProject.class);
        for (ObsProject prj : projects) {
            logger.debug("hydrating ObsProject");
            dao.hydrateSchedBlocks(prj);
            logger.debug("saving ObsProject");
            xmlDao.saveObsProject(prj);
        }
        
    }

}
