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
package alma.scheduling.dataload;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.datamodel.executive.PI;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.executive.dao.XmlExecutiveDAO;

public class ExecutiveDataLoader implements DataLoader {

    private static Logger logger = LoggerFactory.getLogger(ExecutiveDataLoader.class);
    
    private ExecutiveDAO dbDao;
    private XmlExecutiveDAO xmlDao;
    
    public ExecutiveDAO getDbDao() {
        return dbDao;
    }

    public void setDbDao(ExecutiveDAO dbDao) {
        this.dbDao = dbDao;
    }

    public XmlExecutiveDAO getXmlDao() {
        return xmlDao;
    }

    public void setXmlDao(XmlExecutiveDAO xmlDao) {
        this.xmlDao = xmlDao;
    }

    @Override
    public void load() {
        logger.info("Populating the DB with Exec data");
        dbDao.saveObservingSeasonsAndExecutives(xmlDao.getAllObservingSeason(), xmlDao.getAllExecutive());
        List<PI> pis = xmlDao.getAllPi();
        dbDao.saveOrUpdate(pis);
    }

    @Override
    public void clear() {
    	logger.info("Deleting Executive data");
        xmlDao.deleteAll();
        dbDao.deleteAll();
    }
}
