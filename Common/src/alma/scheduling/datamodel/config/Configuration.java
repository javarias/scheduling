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
 * "@(#) $Id: Configuration.java,v 1.4 2010/03/02 02:21:30 rhiriart Exp $"
 */
package alma.scheduling.datamodel.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class provides configuration information.
 *
 */
public class Configuration {

    private String workDirectory;
    private String projectDirectory;
    private String weatherDirectory;
    private String observatoryDirectory;
    private String executiveDirectory;
    private Date lastLoad;
    private String contextFilePath;
    
    /** Array center latitude (degrees, N is positive, S negative) */
    private Double arrayCenterLatitude;
    
    /** Array center longitude (degrees, E is positive, W negative) */
    private Double arrayCenterLongitude;

    /** Maximum wind speed, beyond which no SchedBlock can be executed (km/hr) */
    private Double maxWindSpeed;
    
    public Configuration() { }
    
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

    public String getExecutiveDirectory() {
        return executiveDirectory;
    }

    public void setExecutiveDirectory(String executiveDirectory) {
        this.executiveDirectory = executiveDirectory;
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
        File[] prjFiles = prjDir.listFiles();
        for (File f : prjFiles) {
            Date d = new Date(f.lastModified());
            //Check for modifications in the files to be loaded in the DB
            //Only the files modified after the last load of the DB will be considered
            if (lastLoad != null)
                if(lastLoad.before(d))
                    retval.add(f.getAbsolutePath());
        }
        return retval;
    }
    
    public List<String> getExecutiveFiles(){
        return getModifiedFilesFormDir(executiveDirectory);
    }
    
    public List<String> getProjectFiles() {
        return getModifiedFilesFormDir(projectDirectory);
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
}