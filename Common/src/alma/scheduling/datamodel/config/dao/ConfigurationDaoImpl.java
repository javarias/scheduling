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
package alma.scheduling.datamodel.config.dao;

import java.util.Date;

import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.datamodel.GenericDaoImpl;
import alma.scheduling.datamodel.config.Configuration;

/* TODO Why it is necesarry to merge the XML with database? Wouldn't be cleaner to store
 * Why not to store the information from the XML in the database and then forget about the XML.
 */

@Transactional
public class ConfigurationDaoImpl extends GenericDaoImpl implements
        ConfigurationDao {

    private static Logger logger = LoggerFactory.getLogger(ConfigurationDaoImpl.class);
    
    private XmlConfigurationDaoImpl xmlDao;
    
    private Configuration config = null;
    
    public ConfigurationDaoImpl(){
        xmlDao = new XmlConfigurationDaoImpl();
    }
    
    /**
     * Get the current Configuration, merging data from the XML input file
     * that is not kept in the database with some dynamic data that is
     * updated there.
     */
    @Override
    public Configuration getConfiguration() {
        if(config != null)
            return config;
        config = xmlDao.getConfiguration(); 
        Query query = getSession().createQuery("from Configuration as config"
                + " order by config.lastLoad desc");
        query.setMaxResults(1);
        if (query.list().size() == 0) {
        	if (config == null){
        		logger.info("Ignoring configuration from XML file");
        		config = new Configuration();
        		config.setArrayCenterLatitude(-23.022894444444443);
        		config.setArrayCenterLongitude(-67.75492777777778);
        	}
        	else
        		config.setLastLoad(null);
        } else {
        	if (config == null)
        		config = new Configuration();
        	logger.info("Ignoring configuration from XML file. Using configuration stored in the DB");
            Configuration dbConf = (Configuration) query.list().get(0);
            config.setLastLoad(dbConf.getLastLoad());
            config.setNextStepTime(dbConf.getNextStepTime());
            config.setSimulationStartTime(dbConf.getSimulationStartTime());
            config.setScienceGradeConfig(dbConf.getScienceGradeConfig());
    		config.setArrayCenterLatitude(-23.022894444444443);
    		config.setArrayCenterLongitude(-67.75492777777778);
        }
        return config;
    }

    /**
     * Store in the DB a new last Load timestamp
     */
    @Override
    public void updateConfig() {
        config.setLastLoad(new Date());
        saveOrUpdate(config);
    }
    
    /**
     * Store in the DB the given update time
     */
    @Override
    public void updateConfig(Date lastUpdateTime){
    	if (config == null ) getConfiguration();
    	config.setLastLoad(lastUpdateTime);
    	saveOrUpdate(config);
    }

    @Override
    public void updateNextStep(Date nextStepTime) {
        config.setNextStepTime(nextStepTime);
        saveOrUpdate(config);
    }
    
    @Override
    public void updateSimStartTime(Date simStartTime) {
        logger.info("updating simulation start time");
        config.setSimulationStartTime(simStartTime);
        saveOrUpdate(config);
    }

    @Override
    public void deleteAll() {
        getHibernateTemplate().bulkUpdate("delete Configuration");
        config = null;
    }
}
