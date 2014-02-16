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
package alma.scheduling.datamodel.config;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class provides configuration information.
 */
public class Configuration {

	/** 
	 * <h3>Changes from v1 to v2.</h3>
	 * <dl>
	 * <dt>SchedBlock field "revision"</dt>
	 * <dd>Added the <code>String</code> field <em>revision</em> to SchedBlock.
	 * This basically contains the version of the SchedBlock</dd>
	 * </dl>
	 */
	private static final long version = 2L;
	
	private String workDirectory;
    private String projectDirectory;
    private String weatherDirectory;
    private String observatoryDirectory;
    private String executiveDirectory;
    private String outputDirectory;
    private String reportDirectory;
    private Date lastLoad;
    private Date simulationStartTime;
    private String contextFilePath;
    private Long dataModelVersion;
    private String simulationStatus;
    
    /**
     * Contains the configuration of the grade of the projects.
     * 
     */
    private ScienceGradeConfig scienceGradeConfig;
    
    /** Array center latitude (degrees, N is positive, S negative) */
    private Double arrayCenterLatitude;
    
    /** Array center longitude (degrees, E is positive, W negative) */
    private Double arrayCenterLongitude;

    /** Maximum wind speed, beyond which no SchedBlock can be executed (km/hr) */
    private Double maxWindSpeed;
    
    /** Start time for the next step, used by the step function, for debugging */
    private Date nextStepTime;
    
   
    public Configuration() { 
    	this.dataModelVersion = version;
    }
    
	public String getWorkDirectory() {
        return workDirectory;
    }

    public void setWorkDirectory(String workDirectory) {
        this.workDirectory = workDirectory;
    }

    public String getProjectDirectory() {
        return projectDirectory;
    }

    public void setProjectDirectory(String projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    public String getWeatherDirectory() {
        return weatherDirectory;
    }

    public void setWeatherDirectory(String weatherDirectory) {
        this.weatherDirectory = weatherDirectory;
    }

    public String getObservatoryDirectory() {
        return observatoryDirectory;
    }

    public void setObservatoryDirectory(String observatoryDirectory) {
        this.observatoryDirectory = observatoryDirectory;
    }
    
    public ScienceGradeConfig getScienceGradeConfig() {
        return scienceGradeConfig;
    }

    public void setScienceGradeConfig(ScienceGradeConfig scienceGradeConfig) {
        this.scienceGradeConfig = scienceGradeConfig;
    }

    public String getExecutiveDirectory() {
        return executiveDirectory;
    }

    public void setExecutiveDirectory(String executiveDirectory) {
        this.executiveDirectory = executiveDirectory;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
    
    public String getReportDirectory() {
		return reportDirectory;
	}

	public void setReportDirectory(String reportDirectory) {
		this.reportDirectory = reportDirectory;
	}
    
    public Date getLastLoad() {
        return lastLoad;
    }

    public void setLastLoad(Date lastLoad) {
        this.lastLoad = lastLoad;
    }

    public String getContextFilePath() {
        return contextFilePath;
    }

    public void setContextFilePath(String contextFilePath) {
        this.contextFilePath = contextFilePath;
    }

    private List<String> getModifiedFilesFormDir(String dir){
        List<String> retval = new ArrayList<String>();
        String prjAbsPath = workDirectory + "/" + dir;
        File prjDir = new File(prjAbsPath);
        File[] prjFiles = prjDir.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
		});
        for (File f : prjFiles) {
            Date d = new Date(f.lastModified());
            //Check for modifications in the files to be loaded in the DB
            //Only the files modified after the last load of the DB will be considered
            if (lastLoad != null)
                if(lastLoad.after(d))
                    break;
            retval.add(f.getAbsolutePath());
        }
        return retval;
    }
    
    private List<String> getFilesFromDir(String dir){
        List<String> retval = new ArrayList<String>();
        String prjAbsPath = workDirectory + "/" + dir;
        File prjDir = new File(prjAbsPath);
        File[] prjFiles = prjDir.listFiles();
        for (File f : prjFiles) {
            retval.add(f.getAbsolutePath());
        }
        return retval;
    }
    
    /**
     * 
     * @return The modified executive xml files to be reloaded in the DB
     */
    public List<String> getExecutiveFiles(){
        return getModifiedFilesFormDir(executiveDirectory);
    }
    
    /**
     * 
     * @return The modified project xml files to be reloaded in the DB
     */
    public List<String> getProjectFiles() {
        return getModifiedFilesFormDir(projectDirectory);
    }

    /**
     * Get Observatory Characteristics configuration files.
     * 
     * @return the modified observatory characteristics files to be reloaded in the DB
     */
    public List<String> getObservatoryFiles() {
        return getModifiedFilesFormDir(observatoryDirectory);
    }
    
    /**
     * 
     * @return All the files in the $APRC_WORK_DIR/Observatory directory
     */
    public File[] getAllObservatoryFiles() {
    	return getObservatoryCharactericticsDir().listFiles();
    }
    
    /**
     * 
     * @return The directory containing all the observatory xml configuration files
     */
    public File getObservatoryCharactericticsDir() {
    	return new File(workDirectory + "/" + observatoryDirectory);
    }
    
    /**
     * 
     * @return All the output XML files
     */
    public List<String> getAllOutputFiles(){
        return getFilesFromDir(outputDirectory);
    }
    
    public List<String> getWeatherHistoryFiles() {
        return null;
    }
    
    public List<String> getWeatherATMFiles() {
        return null;
    }

    public Double getArrayCenterLatitude() {
        return arrayCenterLatitude;
    }

    public void setArrayCenterLatitude(Double arrayCenterLatitude) {
        this.arrayCenterLatitude = arrayCenterLatitude;
    }

    public Double getArrayCenterLongitude() {
        return arrayCenterLongitude;
    }

    public void setArrayCenterLongitude(Double arrayCenterLongitude) {
        this.arrayCenterLongitude = arrayCenterLongitude;
    }

    public Double getMaxWindSpeed() {
        return maxWindSpeed;
    }

    public void setMaxWindSpeed(Double maxWindSpeed) {
        this.maxWindSpeed = maxWindSpeed;
    }

    public Date getNextStepTime() {
        return nextStepTime;
    }

    public void setNextStepTime(Date nextStepTime) {
        this.nextStepTime = nextStepTime;
    }

    public Date getSimulationStartTime() {
        return simulationStartTime;
    }

    public void setSimulationStartTime(Date simulationStartTime) {
        this.simulationStartTime = simulationStartTime;
    }

    public Long getDataModelVersion() {
		return dataModelVersion;
	}

	public void setDataModelVersion(Long dataModelversion) {
		this.dataModelVersion = dataModelversion;
	}
	
	public void checkDataModelVersion() throws IncompatibleModelVersionException {
		if (version != dataModelVersion) {
			throw new IncompatibleModelVersionException();
		}
	}
	
	public String getSimulationStatus() {
		return simulationStatus;
	}

	public void setSimulationStatus(String simulationStatus) {
		this.simulationStatus = simulationStatus;
	}



	public class IncompatibleModelVersionException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 113605899953680840L;
		
		public IncompatibleModelVersionException() {
			super("Model version expected: " + version + ". Version get from database: " + dataModelVersion);
		}
		
	}
}
