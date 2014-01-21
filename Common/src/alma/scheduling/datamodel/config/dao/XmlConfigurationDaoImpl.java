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
 * "@(#) $Id: XmlConfigurationDaoImpl.java,v 1.15 2011/04/20 22:09:40 javarias Exp $"
 */
package alma.scheduling.datamodel.config.dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Date;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.datamodel.config.Configuration;
import alma.scheduling.datamodel.config.ScienceGradeConfig;

public class XmlConfigurationDaoImpl implements ConfigurationDao {

    public static final String APRC_WORK_DIR = "APRC_WORK_DIR";
    public static final String APRC_CONF_FILE = "aprc-config.xml";
    public static String workDir = "";
    
    private Configuration config = null;
    
    private static Logger logger = LoggerFactory.getLogger(XmlConfigurationDaoImpl.class);
    
	private static String getWorkDir(){
		if( workDir != "" )
			return workDir;
		else 
			return System.getenv(APRC_WORK_DIR);
	}
    
    @Override
    public Configuration getConfiguration() {
        logger.trace("entering getConfiguration");
        String confFileName = getWorkDir() + "/" + APRC_CONF_FILE;
        File confFile = new File(confFileName);
        
        if (!confFile.exists()) {
            logger.info("configuration file not found: " + confFileName);
            return null;
        }
        
        try {
            if(config != null)
                return config;
            FileReader reader = new FileReader(confFileName);
            alma.scheduling.input.config.generated.Configuration xmlConfig =
                alma.scheduling.input.config.generated.Configuration.unmarshal(reader);
            config = new Configuration();
            config.setWorkDirectory(System.getenv(APRC_WORK_DIR));
            config.setProjectDirectory(xmlConfig.getProjectDirectory());
            config.setWeatherDirectory(xmlConfig.getWeatherDirectory());
            config.setObservatoryDirectory(xmlConfig.getObservatoryDirectory());
            config.setExecutiveDirectory(xmlConfig.getExecutiveDirectory());
            config.setOutputDirectory(xmlConfig.getOutputDirectory());
            config.setReportDirectory(xmlConfig.getReportDirectory());
            config.setLastLoad(null); // for now
            config.setContextFilePath(xmlConfig.getContextFilePath());
            config.setArrayCenterLatitude(xmlConfig.getArrayCenterLatitude());
            config.setArrayCenterLongitude(xmlConfig.getArrayCenterLongitude());
            config.setMaxWindSpeed(xmlConfig.getMaxWindSpeed());
            ScienceGradeConfig sc = new ScienceGradeConfig();
            sc.setnGradeAPrj((int) xmlConfig.getGradeA());
            sc.setnGradeBPrj((int) xmlConfig.getGradeB());
            sc.setnGradeCPrj((int) xmlConfig.getGradeC()); 
            config.setScienceGradeConfig(sc);
            return config;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (MarshalException e) {
            e.printStackTrace();
        } catch (ValidationException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Not implemented 
     */
    @Override
    public void updateConfig() {}

    /**
     * Not implemented 
     */
    @Override
    public void deleteAll() {}

    /**
     * Not implemented 
     */
    @Override
    public void updateNextStep(Date nextStepTime) {}

    /**
     * Not implemented 
     */
    @Override
    public void updateSimStartTime(Date simStartTime) {}

    /**
     * Not implemented 
     */
	@Override
	public void updateConfig(Date lastUpdateTime) {
		
	}

    /**
     * Not implemented 
     */
	@Override
	public void updateConfig(String simStatus) {
		// TODO Auto-generated method stub
		
	}

    /**
     * Not implemented 
     */
	@Override
	public void deleteForSimulation() {
		// TODO Auto-generated method stub
		
	}
    
}
