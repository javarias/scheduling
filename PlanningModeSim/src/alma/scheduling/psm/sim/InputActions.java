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

import java.rmi.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import alma.scheduling.dataload.DataLoader;
import alma.scheduling.dataload.DataUnloader;
import alma.scheduling.dataload.obsproject.ObsProjectDataLoader;
import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.obsproject.dao.Phase1XMLStoreProjectDao;
import alma.scheduling.datamodel.obsproject.dao.ProjectDao;
import alma.scheduling.psm.cli.RemoteConsole;
import alma.scheduling.psm.util.PsmContext;

public class InputActions extends PsmContext {
	
	public static final String WEATHER_PARAMS_LOADER_BEAN = "weatherSimDataLoader";
	public static final String FULL_DATA_LOADER_BEAN = "fullDataLoader";
	public static final String OBSPROJECT_DATA_LOADER_BEAN = "obsProjectDataLoader";
	public static final String ARCHIVE_PROJECT_DAO_BEAN = "archProjectDao";
	public static final String CONFIGURATION_DAO_BEAN = "configDao";
	
	private static Logger logger = LoggerFactory.getLogger(InputActions.class);
	
	private static InputActions instance = null;
		
    private InputActions(String workDir) {
		super(workDir);
	}
	
    public void fullLoad() throws Exception {
        ApplicationContext ctx = getApplicationContext();
        DataLoader weatherLoader = 
        		(DataLoader) ctx.getBean(WEATHER_PARAMS_LOADER_BEAN);
        DataLoader fullDataLoader = 
        		(DataLoader) ctx.getBean(FULL_DATA_LOADER_BEAN);
        weatherLoader.load();
        fullDataLoader.load();
        
        ConfigurationDao configDao = (ConfigurationDao) ctx.getBean(CONFIGURATION_DAO_BEAN);
        configDao.updateConfig();
    }

    public void load(){
    	ApplicationContext ctx = getApplicationContext();
        DataLoader loader = getDataLoader(ctx);
        try {
			loader.load();
		} catch (Exception e) {
			logger.error("Data loading error: load()");
			e.printStackTrace();
		}
        ConfigurationDao configDao = (ConfigurationDao) ctx.getBean(CONFIGURATION_DAO_BEAN);
        configDao.updateConfig();
    }

    public void unload() {
    	ApplicationContext ctx = getApplicationContext();
        DataUnloader loader = (DataUnloader) ctx.getBean("fullDataUnloader");
        loader.unload();
    }

    public void clean() {
    	ApplicationContext ctx = getApplicationContext();
    	DataLoader loader = getDataLoader(ctx);;
    	loader.clear();
        ConfigurationDao configDao = (ConfigurationDao) ctx.getBean(CONFIGURATION_DAO_BEAN);
        configDao.deleteAll();
    }
    
    public boolean isWeatherLoaded(){
    	return false;
    }
    
    public boolean isDataLoaded(){
    	return false;
    }
    
    public boolean hasSimulationRan(){
    	return false;
    }

    @Deprecated
	public void remoteFullLoad() {
		ApplicationContext ctx = getApplicationContext();
		RemoteConsole console = (RemoteConsole) ctx.getBean("remoteConsoleService");
		String[] args= new String[1];
		args[0]="fullload";
		try {
			console.runTask(args);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Deprecated
	public void remoteLoad() {
		ApplicationContext ctx = getApplicationContext();
		RemoteConsole console = (RemoteConsole) ctx.getBean("remoteConsoleService");
		String[] args= new String[1];
		args[0]="load";
		try {
			console.runTask(args);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Deprecated
	public void remoteClean() {
		ApplicationContext ctx = getApplicationContext();
		RemoteConsole console = (RemoteConsole) ctx.getBean("remoteConsoleService");
		String[] args= new String[1];
		args[0]="clean";
		try {
			console.runTask(args);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}    

	@Deprecated
	public void remoteRun() {
		ApplicationContext ctx = getApplicationContext();
		RemoteConsole console = (RemoteConsole) ctx.getBean("remoteConsoleService");
		String[] args= new String[1];
		args[0]="run";
		try {
			console.runTask(args);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
    
	public static InputActions getInstance(String workDir){
		if (InputActions.instance == null)
			InputActions.instance = new InputActions(workDir);
		return InputActions.instance;
	}
	
	private DataLoader getDataLoader(ApplicationContext ctx) {
		String prop = System.getProperty("ACS.manager");
		ObsProjectDataLoader loader = null;
		loader = (ObsProjectDataLoader) ctx.getBean(OBSPROJECT_DATA_LOADER_BEAN);
		if (prop == null) {
			loader.setArchProjectDao(null);
		} else {
			try {
				loader.setArchProjectDao(new Phase1XMLStoreProjectDao());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return loader;
	}
}
