/* ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2010
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 */

package alma.scheduling.array.guis;

import java.util.Set;

import alma.entity.xmlbinding.sbstatus.SBStatusEntityT;
import alma.scheduling.SchedBlockExecutionItem;
import alma.scheduling.SchedBlockQueueItem;
import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnitControl;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.ObservingParameters;
import alma.scheduling.datamodel.obsproject.Preconditions;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.SchedBlockControl;
import alma.scheduling.datamodel.obsproject.SchedulingConstraints;
import alma.scheduling.datamodel.obsproject.ScienceGrade;
import alma.scheduling.datamodel.obsproject.Target;
import alma.scheduling.datamodel.obsproject.WeatherConstraints;
import alma.scheduling.datamodel.obsproject.WeatherDependentVariables;

/**
 * An extension to the SchedBlockQueueItem which holds the SchedBlock
 * in question rather than just it's Entity ID.
 * 
 * @author dclarke
 * $Id: ManifestSchedBlockQueueItem.java,v 1.3 2011/02/24 22:42:50 javarias Exp $
 */
public class ManifestSchedBlockQueueItem {
	/*
	 * ================================================================
	 * Fields
	 * ================================================================
	 */
	/** The SchedBlockQueueItem of which we're a manifestation */
	private SchedBlockQueueItem item;
	/** The corresponding SchedBlock */
	private SchedBlock schedBlock;
	/** The execution state we're in */
	private String executionState;
	/* End Fields
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Constructors
	 * ================================================================
	 */
	/** Hide the default constructor. */
	@SuppressWarnings("unused")
	private ManifestSchedBlockQueueItem() {
	}

	/**
	 * Construct from field values.
	 * 
	 * @param item
	 * @param schedBlock
	 */
	public ManifestSchedBlockQueueItem(SchedBlockQueueItem item,
									   SchedBlock          schedBlock) {
		this.item = item;
		this.schedBlock = schedBlock;
		this.executionState = schedBlock.
								getSchedBlockControl().
								getState().toString();
	}

	/**
	 * Construct from field values.
	 * 
	 * @param item
	 * @param schedBlock
	 */
	public ManifestSchedBlockQueueItem(SchedBlockQueueItem item,
									   SchedBlock          schedBlock,
									   String              executionState) {
		this.item = item;
		this.schedBlock = schedBlock;
		this.executionState = executionState;
	}
	/* End Construction
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Basic getters
	 * ================================================================
	 */
	public String getUid() {
		return item.uid;
	}

	public long getTimestamp() {
		return item.timestamp;
	}

	public SchedBlockQueueItem getItem() {
		return item;
	}

	public SchedBlock getSchedBlock() {
		return schedBlock;
	}
	
	public String getExecutionState() {
		return executionState;
	}
	/* End Basic getters
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Basic setters
	 * ================================================================
	 */
	public void setExecutionState(String state) {
		final int i = state.indexOf("ExecutionState");
		if (i > 0) {
			state = state.substring(0, i);
		}
		executionState = state;
	}
	/* End Basic setters
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Delegation
	 * ================================================================
	 */
	public Executive getExecutive() {
		return schedBlock.getExecutive();
	}

	public Long getId() {
		return schedBlock.getId();
	}

	public ScienceGrade getLetterGrade() {
		return schedBlock.getLetterGrade();
	}

	public String getName() {
		return schedBlock.getName();
	}

	public Set<ObservingParameters> getObservingParameters() {
		return schedBlock.getObservingParameters();
	}

	public ObsUnitControl getObsUnitControl() {
		return schedBlock.getObsUnitControl();
	}

	public ObsUnitSet getParent() {
		return schedBlock.getParent();
	}

	public String getPiName() {
		return schedBlock.getPiName();
	}

	public Preconditions getPreConditions() {
		return schedBlock.getPreConditions();
	}

	public ObsProject getProject() {
		return schedBlock.getProject();
	}

	public String getProjectUid() {
		return schedBlock.getProjectUid();
	}

	public Boolean getRunQuicklook() {
		return schedBlock.getRunQuicklook();
	}

	public SchedBlockControl getSchedBlockControl() {
		return schedBlock.getSchedBlockControl();
	}

	public SchedulingConstraints getSchedulingConstraints() {
		return schedBlock.getSchedulingConstraints();
	}

	public Integer getScienceRank() {
		return schedBlock.getScienceRank();
	}

	public Float getScienceScore() {
		return schedBlock.getScienceScore();
	}

	public SBStatusEntityT getStatusEntity() {
		return schedBlock.getStatusEntity();
	}

	public Set<Target> getTargets() {
		return schedBlock.getTargets();
	}

	public WeatherConstraints getWeatherConstraints() {
		return schedBlock.getWeatherConstraints();
	}

	public WeatherDependentVariables getWeatherDependentVariables() {
		return schedBlock.getWeatherDependentVariables();
	}
	
	public String getNote(){
		return schedBlock.getNote();
	}
	/* End Delegation
	 * ============================================================= */
}