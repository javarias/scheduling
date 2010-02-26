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
 * "@(#) $Id: Configuration.java,v 1.2 2010/02/26 23:34:31 javarias Exp $"
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

    private List<String> getFileListFromDirectory(String dir){
        List<String> retval = new ArrayList<String>();
        String prjAbsPath = workDirectory + "/" + dir;
        File prjDir = new File(prjAbsPath);
        File[] prjFiles = prjDir.listFiles();
        for (File f : prjFiles) {
            retval.add(f.getAbsolutePath());
        }
        return retval;
    }
    
    public List<String> getExecutiveFiles(){
        return getFileListFromDirectory(executiveDirectory);
    }
    
    public List<String> getProjectFiles() {
        return getFileListFromDirectory(projectDirectory);
    }
    
    public List<String> getWeatherHistoryFiles() {
        return null;
    }
    
    public List<String> getWeatherATMFiles() {
        return null;
    }
}
