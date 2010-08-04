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
 * "@(#) $Id: SchedBlock.java,v 1.9 2010/08/04 23:23:01 javarias Exp $"
 */
package alma.scheduling.datamodel.obsproject;

import java.util.HashSet;
import java.util.Set;

import alma.scheduling.datamodel.executive.Executive;

public class SchedBlock extends ObsUnit {

    /** Principal investigator name */
    private String piName;

    /** User friendly name */
    private String name;
    
	/** Weather constraints */
    private WeatherConstraints weatherConstraints;
    
    /** Polymorphic observing parameters, the type depends on the type of the observation */
    private Set<ObservingParameters> observingParameters = new HashSet<ObservingParameters>();
    
    /** Pre-conditions required to execute SchedBlock */
    private Preconditions preConditions;

    /** Parameters that participate in the definition of constraints to schedule this SchedBlock */
    private SchedulingConstraints schedulingConstraints;
    
    /** The targets. Each one includes a FieldSource, the ObservingParameters and an InstrumentSpec. */
    private Set<Target> targets = new HashSet<Target>();
    
    /** Weather dependent variables, an Updateable. Needs to be updated for the current conditions. */
    private WeatherDependentVariables weatherDependentVariables;
    
    /** Several parameters needed to control the execution of the SchedBlock */
    private SchedBlockControl schedBlockControl;
    
    private Executive executive;
    
    private Integer scienceRank;
    
    private ScienceGrade letterGrade;
    
    private Float scienceScore;
    
    /** Default constructor */
    public SchedBlock() { }
    
    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    public String getPiName() {
        return piName;
    }

    public void setPiName(String piName) {
        this.piName = piName;
    }

    public WeatherConstraints getWeatherConstraints() {
        return weatherConstraints;
    }

    public void setWeatherConstraints(WeatherConstraints weatherConstraints) {
        this.weatherConstraints = weatherConstraints;
    }

    public Set<ObservingParameters> getObservingParameters() {
        return observingParameters;
    }

    public void setObservingParameters(Set<ObservingParameters> observingParameters) {
        this.observingParameters = observingParameters;
    }

    public void addObservingParameters(ObservingParameters observingParameters) {
        this.observingParameters.add(observingParameters);
    }
    
    public Preconditions getPreConditions() {
        return preConditions;
    }

    public void setPreConditions(Preconditions preConditions) {
        this.preConditions = preConditions;
    }

    public SchedulingConstraints getSchedulingConstraints() {
        return schedulingConstraints;
    }

    public void setSchedulingConstraints(SchedulingConstraints schedulingConstraints) {
        this.schedulingConstraints = schedulingConstraints;
    }

    public Set<Target> getTargets() {
        return targets;
    }

    public void setTargets(Set<Target> targets) {
        this.targets = targets;
    }
    
    public void addTarget(Target target) {
        this.targets.add(target);
    }

    public WeatherDependentVariables getWeatherDependentVariables() {
        return weatherDependentVariables;
    }

    public void setWeatherDependentVariables(
            WeatherDependentVariables weatherDependentVariables) {
        this.weatherDependentVariables = weatherDependentVariables;
    }

    public SchedBlockControl getSchedBlockControl() {
        return schedBlockControl;
    }

    public void setSchedBlockControl(SchedBlockControl schedBlockControl) {
        this.schedBlockControl = schedBlockControl;
    }
    
    public Executive getExecutive() {
        return executive;
    }

    public void setExecutive(Executive executive) {
        this.executive = executive;
    }

    public Integer getScienceRank() {
        return scienceRank;
    }

    public void setScienceRank(Integer scienceRank) {
        this.scienceRank = scienceRank;
    }

    public ScienceGrade getLetterGrade() {
        return letterGrade;
    }

    public void setLetterGrade(ScienceGrade letterGrade) {
        this.letterGrade = letterGrade;
    }

    public Float getScienceScore() {
        return scienceScore;
    }

    public void setScienceScore(Float scienceScore) {
        this.scienceScore = scienceScore;
    }
}
