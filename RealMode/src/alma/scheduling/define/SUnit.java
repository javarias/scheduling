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
 * File SUnit.java
 */
 
package ALMA.scheduling.define;

// Only to get this to compile.
//class SchedBlock { String dummy; }
//import alma.bo.SchedBlock;
import alma.entity.xmlbinding.schedblock.*;
import alma.entity.xmlbinding.obsproject.*;

import alma.acs.entityutil.EntityException;
import alma.acs.entityutil.EntitySerializer;
import alma.acs.entityutil.EntityDeserializer;

import alma.xmlentity.XmlEntityStruct;
import alma.entities.commonentity.EntityT;

import java.util.logging.Logger;
/**
 * An SUnit is the lowest-level, atomic scheduling unit. 
 * 
 * @version 1.00  Jun 4, 2003
 * @author Allen Farris
 */
public class SUnit {

    private alma.entity.xmlbinding.schedblock.SchedBlock schedBlock;
	private String schedBlockId;
	private int name;
		
	private Priority scientificPriority;
	private Priority userPriority;
	private Expression scienceGoal;
	private WeatherCondition weatherConstraint;
	private SystemSetup requiredInitialSetup;
	private int maximumTimeInSeconds;

	private SkyCoordinates coordinates;
	private boolean isStandardScript;
	private Status unitStatus;

    private EntitySerializer serializer;
    private EntityDeserializer deserializer;
    //////////////////////// Constructors ////////////////////////////////
    
	/**
	 * Create an SUnit object from a SchedBlock object.
	 */
	public SUnit(SchedBlock sb) {
        this.schedBlock = sb;
        this.serializer = EntitySerializer.getEntitySerializer(
            Logger.getAnonymousLogger());
        this.deserializer = EntityDeserializer.getEntityDeserializer(
            Logger.getAnonymousLogger());
	}


    //////////////////////// Get Methods ////////////////////////////////
    
	/**
	 * @return
	 */
	public SkyCoordinates getCoordinates() {
		return coordinates;
	}

	/**
	 * @return
	 */
	public boolean isStandardScript() {
		return isStandardScript;
	}

	/**
	 * @return
	 */
	public int getMaximumTimeInSeconds() {
		return maximumTimeInSeconds;
	}

	/**
	 * @return
	 */
	public int getName() {
		return name;
	}

	/**
	 * @return
	 */
	public SystemSetup getRequiredInitialSetup() {
		return requiredInitialSetup;
	}

	/**
	 * @return
	 */
	public Expression getScienceGoal() {
		return scienceGoal;
	}

	/**
	 * @return
	 */
	public Priority getScientificPriority() {
		return scientificPriority;
	}

	/**
	 * @return
	 */
	public Status getUnitStatus() {
		return unitStatus;
	}

	/**
	 * @return
	 */
	public Priority getUserPriority() {
		return userPriority;
	}

	/**
	 * @return
	 */
	public WeatherCondition getWeatherConstraint() {
		return weatherConstraint;
	}

    public String getSchedBlockId() {
        return schedBlockId;
    }

    public SchedBlock getSBEntityObject() {
        return schedBlock;
    }
    //////////////////////// Set Methods /////////////////////////////////
    
	/**
	 * @param coordinates
	 */
	public void setCoordinates(SkyCoordinates coordinates) {
		this.coordinates = coordinates;
	}

	/**
	 * @param b
	 */
	public void setStandardScript(boolean b) {
		isStandardScript = b;
	}

	/**
	 * @param i
	 */
	public void setMaximumTimeInSeconds(int i) {
		maximumTimeInSeconds = i;
	}

	/**
	 * @param i
	 */
	public void setName(int i) {
		name = i;
	}

	/**
	 * @param setup
	 */
	public void setRequiredInitialSetup(SystemSetup setup) {
		requiredInitialSetup = setup;
	}

	/**
	 * @param expression
	 */
	public void setScienceGoal(Expression expression) {
		scienceGoal = expression;
	}

	/**
	 * @param priority
	 */
	public void setScientificPriority(Priority priority) {
		scientificPriority = priority;
	}

	/**
	 * @param status
	 */
	public void setUnitStatus(Status status) {
		unitStatus = status;
	}

	/**
	 * @param priority
	 */
	public void setUserPriority(Priority priority) {
		userPriority = priority;
	}

	/**
	 * @param condition
	 */
	public void setWeatherConstraint(WeatherCondition condition) {
		weatherConstraint = condition;
	}

    public void setSchedBlockId(String id) {
        this.schedBlockId = id;
    }

    public void setSBEntityObject(SchedBlock sb) {
        this.schedBlock = sb;
        updateFields();
    }

    //////////////////////// Other Public Methods ////////////////////////////////

    public XmlEntityStruct serializeSUnit() throws EntityException {
        return serializer.serializeEntity(schedBlock);
    }
    
    public void updateSUnit(XmlEntityStruct xml) 
      throws EntityException, ClassNotFoundException {
        SchedBlock sb = (SchedBlock)deserializer.deserializeEntity(xml.xmlString, 
            Class.forName("alma.entity.xmlbinding.schedblock.SchedBlock"));
        this.setSBEntityObject(sb);
        updateFields();
    }
    //////////////////////// Other Private Methods ////////////////////////////////

    /*
     *  Updates all the fields when the entity has been updated or set.
     */
    private void updateFields() {
        setSchedBlockId(((EntityT)schedBlock.getSchedBlockEntity()).getEntityId());
        setUnitStatus(new Status(schedBlock.getObsUnitControl().getSchedStatus()));
    }
}
