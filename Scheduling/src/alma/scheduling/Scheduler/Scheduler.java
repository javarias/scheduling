/*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * All rights reserved
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 * 
 * File Scheduler.java
 * 
 */
package alma.scheduling.Scheduler;

import java.util.logging.Logger;

import alma.acs.logging.AcsLogger;
import alma.acs.logging.domainspecific.ArrayContextLogger;
import alma.scheduling.Define.Clock;
import alma.scheduling.Define.SchedulingException;
import alma.scheduling.Scheduler.DSA.DynamicSchedulingAlgorithm;

/**
 * The Scheduler class is an abstract class that forms the basis for
 * the DynamicScheduler and InteractiveScheduler classes.
 * 
 * @version $Id: Scheduler.java,v 1.19 2008/06/19 20:40:59 wlin Exp $
 * @author Allen Farris
 *
 */
abstract public class Scheduler {
	
    protected SchedulerConfiguration config;// = null;
    protected String arrayName = null;
    protected AcsLogger logger;
    protected ArrayContextLogger arrayLogger;
    protected Clock clock = null;
    protected DynamicSchedulingAlgorithm dsa = null;
    protected String type=""; //will be either queued, interactive or dynamic
    protected String id=""; //for uniqueness using archive's UID

    public Scheduler(){}
    
    public Scheduler(SchedulerConfiguration config) {
        setConfiguration(config);
    }
    public void setConfiguration(SchedulerConfiguration c) {
    	this.config = c;
        if(config == null){
            System.out.println("SchedulerConfiguration is null ");
        }
    	this.arrayName = config.getArrayName();
    	this.clock = config.getClock();
    	this.logger = config.getLog();
        logger.fine("Scheduler logger set");
        arrayLogger = new ArrayContextLogger(logger);
    	// At a minimum, the configuration, clock, and log objects
    	// cannot be null and the arrayName cannot be null.
    	if (config == null)
    		throw new IllegalArgumentException(name() + ": There is no configuration object!");
    	if (arrayName == null)
    		throw new IllegalArgumentException(name() + ": Invalid array-name!");
    	if (logger == null)
    		throw new IllegalArgumentException(name() + ": There is no logger!");
    	if (clock == null)
    		throw new IllegalArgumentException(name() + ": There is no clock!");
    }
    
    /**
     * Form a name of this scheduler, which includes its thread name and the id
     * of the array on which it operates.  This name has the following form:
     * 		Scheduler [task1] (array 1)
     * @return A string identifying this scheduler.
     */
    protected String name() {
    	return "Scheduler [" + Thread.currentThread().getName() + 
		"] (array " + arrayName + ")";
    }

    // get methods
    public String getType(){
        return type;
    }
    public String getId(){
        return id;
    }
    public String getArrayName(){
        return arrayName;
    }

        
    // Set methods
    public void setType(String t) throws SchedulingException {
        t = t.toLowerCase();
        if(t.equals("interactive") || t.equals("queued") || t.equals("dynamic") ){
            type =t;
        } else {
            throw new SchedulingException("Invaild Scheduler type");
        }
    }
        
    public void setId(String i) {
        id = i;
    }

    
}
