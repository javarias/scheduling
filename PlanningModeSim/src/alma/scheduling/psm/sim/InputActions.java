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
 */

package alma.scheduling.psm.sim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import alma.scheduling.dataload.CompositeDataLoader;
import alma.scheduling.dataload.DataLoader;
import alma.scheduling.dataload.DataUnloader;
import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.config.dao.ConfigurationDaoImpl;
import alma.scheduling.psm.util.PsmContext;

public class InputActions extends PsmContext {
	
	private static Logger logger = LoggerFactory.getLogger(InputActions.class);
	
    public InputActions(String workDir) {
		super(workDir);
	}
	
    public void fullLoad() {
        ApplicationContext ctx = new FileSystemXmlApplicationContext( contextFile );
        String[] loadersNames = ctx.getBeanNamesForType(CompositeDataLoader.class);
        String [] cfgBeans = ctx.getBeanNamesForType(ConfigurationDaoImpl.class);
        if(cfgBeans.length == 0){
            logger.error(contextFile + 
            		" file doesn't contain a bean of the type " +
            		"alma.scheduling.datamodel.config.dao.ConfigurationDaoImpl");
            System.exit(3); // Exit code 3: No necessary bean in scheduling context file.
        }
        for(int i = 0; i < loadersNames.length; i++){
            DataLoader loader = (DataLoader) ctx.getBean(loadersNames[i]);
            try {
				loader.load();
			} catch (Exception e) {
				logger.error("Data loading error: fullload()");
				e.printStackTrace();
			}
        }
        ConfigurationDao configDao = (ConfigurationDao) ctx.getBean(cfgBeans[0]);
        configDao.updateConfig();
    }

    public void load(){
        ApplicationContext ctx = new FileSystemXmlApplicationContext(contextFile);
        String [] cfgBeans = ctx.getBeanNamesForType(ConfigurationDaoImpl.class);
        DataLoader loader = (DataLoader) ctx.getBean("fullDataLoader");
        try {
			loader.load();
		} catch (Exception e) {
			logger.error("Data loading error: load()");
			e.printStackTrace();
		}
        ConfigurationDao configDao = (ConfigurationDao) ctx.getBean(cfgBeans[0]);
        configDao.updateConfig();
    }

    public void unload() {
        ApplicationContext ctx = new FileSystemXmlApplicationContext(contextFile);
        DataUnloader loader = (DataUnloader) ctx.getBean("fullDataUnloader");
        loader.unload();
    }

    public void clean() {
        ApplicationContext ctx = new FileSystemXmlApplicationContext(contextFile);
        String[] loadersNames = ctx.getBeanNamesForType(CompositeDataLoader.class);
        String[] cfgBeans = ctx.getBeanNamesForType(ConfigurationDaoImpl.class);
        if (cfgBeans.length == 0) {
        	logger.error(contextFile
            + " file doesn't contain a bean of the type alma.scheduling.datamodel.config.dao.ConfigurationDaoImpl");
            System.exit(3); // Exit code 3: No necessary bean in scheduling context file.
        }
        for(int i = 0; i < loadersNames.length; i++) {
        	DataLoader loader = (DataLoader) ctx.getBean(loadersNames[i]);
            loader.clear();
        }
        ConfigurationDao configDao = (ConfigurationDao) ctx.getBean("configDao");
        configDao.deleteAll();
    }

}