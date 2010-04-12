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
 * "@(#) $Id: Configuration.java,v 1.10 2010/04/12 20:53:35 rhiriart Exp $"
 */
package alma.scheduling.datamodel.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class provides configuration information.
 */
public class Configuration {

    private String workDirectory;
    private String projectDirectory;
    private String weatherDirectory;
    private String observatoryDirectory;
    private String executiveDirectory;
    private String outputDirectory;
    private Date lastLoad;
    private String contextFilePath;
    
    /**
     * Percent of projects graded "A" (0-100).
     * 
     * Given this parameter, a project is graded "A" if its scoring number
     * situates it between the top gradeAPercent % projects, when they are ordered
     * by score.
     * 
     * gradeAPercet + gradeBPercent + gradeCPercent < 100
     */
    private Float gradeAPercent;
    
    /**
     * Percent of projects graded "B" (0-100).
     * 
     * Given this parameter, a project is graded "B" if its scoring number
     * situates it below the top gradeAPercent % projects, but between the
     * gradeBPercent % projects, when they are ordered by score. 
     * 
     * gradeAPercet + gradeBPercent + gradeCPercent < 100
     */
    private Float gradeBPercent;
    
    /**
     * Percent of projects graded "B" (0-100).
     * 
     * Given this parameter, a project is graded "C" if its scoring number
     * situates it below the gradeBPercent % projects, but between the
     * gradeCPercent % projects, when they are ordered by score.
     * 
     * gradeAPercet + gradeBPercent + gradeCPercent < 100
     * 
     * All the rest of ObsProjects are graded "D".
     */
    private Float gradeCPercent;
    
    /** Array center latitude (degrees, N is positive, S negative) */
    private Double arrayCenterLatitude;
    
    /** Array center longitude (degrees, E is positive, W negative) */
    private Double arrayCenterLongitude;

    /** Maximum wind speed, beyond which no SchedBlock can be executed (km/hr) */
    private Double maxWindSpeed;
    
    /** Start time for the next step, used by the step function, for debugging */
    private Date nextStepTime;
    
    /** Simulation start time */
    private Date simulationStartTime;
    
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

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
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

    public Float getGradeAPercent() {
        return gradeAPercent;
    }

    public void setGradeAPercent(Float gradeAPercent) {
        this.gradeAPercent = gradeAPercent;
    }

    public Float getGradeBPercent() {
        return gradeBPercent;
    }

    public void setGradeBPercent(Float gradeBPercent) {
        this.gradeBPercent = gradeBPercent;
    }

    public Float getGradeCPercent() {
        return gradeCPercent;
    }

    public void setGradeCPercent(Float gradeCPercent) {
        this.gradeCPercent = gradeCPercent;
    }

}
