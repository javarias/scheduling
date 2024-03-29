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

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import alma.scheduling.dataload.DataLoader;
import alma.scheduling.dataload.DataUnloader;
import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.psm.sim.status.SimulationAbstractState;
import alma.scheduling.psm.sim.status.SimulationStateContext;
import alma.scheduling.psm.util.PsmContext;

public class InputActions extends PsmContext {
	
	public static final String WEATHER_PARAMS_LOADER_BEAN = "weatherSimDataLoader";
	public static final String ALMA_ARCHIVE_FULL_DATA_LOADER = "AlmaArchiveFullDataLoader";
	public static final String OBSPROJECT_DATA_LOADER_BEAN = "obsProjectDataLoader";
	public static final String ALMA_ARCHIVE_OBSPROJECT_DATA_LOADER_BEAN = "AlmaArchiveObsProjectDataLoader";
	public static final String ARCHIVE_PROJECT_DAO_BEAN = "archProjectDao";
	public static final String CONFIGURATION_DAO_BEAN = "configDao";
	public static final String IMMUTABLE_DATA_LOADER_BEAN = "immutableDataLoader";
	public static final String DATA_CLEANER_BEAN = "dataCleaner";
	
	private static Logger logger = LoggerFactory.getLogger(InputActions.class);
	
	private static InputActions instance = null;
	private SimulationStateContext simulationStateContext;
		
    private InputActions(String workDir) {
		super(workDir);
		ApplicationContext ctx = getApplicationContext();
		ConfigurationDao configDao = (ConfigurationDao) ctx.getBean(CONFIGURATION_DAO_BEAN);
		configDao.getConfiguration();
		if (configDao.getConfiguration().getSimulationStatus() == null || 
				configDao.getConfiguration().getSimulationStatus().equals(SimulationStateEnum.START.toString()))
			simulationStateContext = new SimulationStateContext();
		else if (configDao.getConfiguration().getSimulationStatus().equals(SimulationStateEnum.DYNAMIC_DATA_LOADED.toString()))
			simulationStateContext = new SimulationStateContext(SimulationStateEnum.DYNAMIC_DATA_LOADED);
		else if (configDao.getConfiguration().getSimulationStatus().equals(SimulationStateEnum.STATIC_DATA_LOADED.toString()))
			simulationStateContext = new SimulationStateContext(SimulationStateEnum.STATIC_DATA_LOADED);
		else if (configDao.getConfiguration().getSimulationStatus().equals(SimulationStateEnum.SIMULATION_COMPLETED.toString()))
			simulationStateContext = new SimulationStateContext(SimulationStateEnum.SIMULATION_COMPLETED);
		else
			simulationStateContext = new SimulationStateContext();
	}
	
    public void fullLoad(String dataLoader) throws Exception {
    	logger.info("Using " + dataLoader + " for getting projects");
        ApplicationContext ctx = getApplicationContext();
        DataLoader weatherLoader = 
        		(DataLoader) ctx.getBean(WEATHER_PARAMS_LOADER_BEAN);
        DataLoader loader = (DataLoader) ctx.getBean(dataLoader);
        Date start = new Date();
        weatherLoader.load();
        loader.load();
        Date end = new Date();
        logger.info("data load took " + (end.getTime() - start.getTime()) + " ms");
        
        ConfigurationDao configDao = (ConfigurationDao) ctx.getBean(CONFIGURATION_DAO_BEAN);
        configDao.getConfiguration();
//        simulationStateContext.getCurrentState().fullload();
//        configDao.updateConfig(simulationStateContext.getCurrentState().getCurrentState().toString());
    }

    public void load(String dataLoader){
    	ApplicationContext ctx = getApplicationContext();
        DataLoader loader = (DataLoader) ctx.getBean(dataLoader);
        try {
			loader.load();
		} catch (Exception e) {
			logger.error("Data loading error: load()");
			e.printStackTrace();
		}
        ConfigurationDao configDao = (ConfigurationDao) ctx.getBean(CONFIGURATION_DAO_BEAN);
//        simulationStateContext.getCurrentState().load();
//        configDao.updateConfig(simulationStateContext.getCurrentState().getCurrentState().toString());
    }

    public void unload() {
    	ApplicationContext ctx = getApplicationContext();
        DataUnloader loader = (DataUnloader) ctx.getBean("fullDataUnloader");
        loader.unload();
    }

    public void clean(String dataLoader) {
    	logger.info("Using " + dataLoader + " to clean the DB");
    	ApplicationContext ctx = getApplicationContext();
    	DataLoader loader = (DataLoader) ctx.getBean(dataLoader);
    	loader.clear();
        ConfigurationDao configDao = (ConfigurationDao) ctx.getBean(CONFIGURATION_DAO_BEAN);
//        simulationStateContext.getCurrentState().clean();
//        configDao.deleteForSimulation();
    }
    
    public SimulationAbstractState getCurrentSimulationState() {
    	return simulationStateContext.getCurrentState();
    }

    public static InputActions getInstance(String workDir){
		if (InputActions.instance == null)
			InputActions.instance = new InputActions(workDir);
		return InputActions.instance;
	}
    
    public SimulationStateContext getSimulationStateContext() {
    	return simulationStateContext;
    }
	
}
