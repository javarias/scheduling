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
 * "@(#) $Id: ObsProjectDataLoader.java,v 1.2 2010/03/02 23:19:15 javarias Exp $"
 */
package alma.scheduling.dataload.obsproject;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.dataload.DataLoader;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.dao.ObsProjectDao;
import alma.scheduling.datamodel.obsproject.dao.XmlObsProjectDao;

@Transactional
public class ObsProjectDataLoader implements DataLoader {

    XmlObsProjectDao xmlDao;
    
    ObsProjectDao dao;
    
    public void setXmlDao(XmlObsProjectDao xmlDao) {
        this.xmlDao = xmlDao;
    }

    public void setDao(ObsProjectDao dao) {
        this.dao = dao;
    }

    @Override
    public void load() {
        List<ObsProject> projects = xmlDao.getAllObsProjects();
        dao.saveOrUpdate(projects);
    }

}
