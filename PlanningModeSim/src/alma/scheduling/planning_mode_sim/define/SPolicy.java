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
 * File SPolicy.java
 */
 
package alma.scheduling.planning_mode_sim.define;

/**
 * An SPolicy is a SchedulingPolicy that is archiveable. 
 * 
 * @version 1.00  Sep 4, 2003
 * @author Allen Farris
 */
public class SPolicy implements ArchiveEntity {
	private String id;
	private DateTime timeOfCreation;
	private DateTime timeOfUpdate;
	
	private String name;
	private String version;
	private String comment;
	private String scoreCalculation;
	private String successCalculation;
	private String rankingCalculation;
	private SPolicyFactor[] factor;

	public SPolicy() {
	}

	/**
	 * Get the archive identifier.
	 * @return
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Get the time this archive entry was created.
	 * @return
	 */
	public DateTime getTimeCreated() {
		return timeOfCreation;
	}
	
	/**
	 * Get the time this archive entry was last updated.
	 * @return
	 */
	public DateTime getTimeUpdated() {
		return timeOfUpdate;
	}
	
	/**
	 * Set the archive identifier.
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Set the time this archive entry was created.
	 * @param t
	 */
	public void setTimeCreated(DateTime t) {
		this.timeOfCreation = t;
	}
	
	/**
	 * Set the time this archive entry was last updated.
	 * @param t
	 */
	public void setTimeUpdated(DateTime t) {
		this.timeOfUpdate = t;
	}

	/**
	 * @return
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @return
	 */
	public SPolicyFactor[] getFactor() {
		return factor;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public String getRankingCalculation() {
		return rankingCalculation;
	}

	/**
	 * @return
	 */
	public String getScoreCalculation() {
		return scoreCalculation;
	}

	/**
	 * @return
	 */
	public String getSuccessCalculation() {
		return successCalculation;
	}

	/**
	 * @return
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param string
	 */
	public void setComment(String string) {
		comment = string;
	}

	/**
	 * @param factors
	 */
	public void setFactor(SPolicyFactor[] factors) {
		factor = factors;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * @param string
	 */
	public void setRankingCalculation(String string) {
		rankingCalculation = string;
	}

	/**
	 * @param string
	 */
	public void setScoreCalculation(String string) {
		scoreCalculation = string;
	}

	/**
	 * @param string
	 */
	public void setSuccessCalculation(String string) {
		successCalculation = string;
	}

	/**
	 * @param string
	 */
	public void setVersion(String string) {
		version = string;
	}

}
