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

import alma.scheduling.Scheduler.DSA.DynamicSchedulingAlgorithm;
import alma.scheduling.Define.Clock;

import java.util.logging.Logger;

/**
 * The Scheduler class is an abstract class that forms the basis for
 * the DynamicScheduler and InteractiveScheduler classes.
 * 
 * @version Version 1.40 Jun 9, 2004
 * @author Allen Farris
 *
 */
abstract public class Scheduler {
	
    protected SchedulerConfiguration config = null;
    protected short subarrayId = -1;
    protected Logger logger = null;
    protected Clock clock = null;
    protected DynamicSchedulingAlgorithm dsa = null;
    
    public Scheduler(SchedulerConfiguration config) {
    	this.config = config;
    	this.subarrayId = config.getSubarrayId();
    	this.clock = config.getClock();
    	this.logger = config.getLog();
    	// At a minimum, the configuration, clock, and log objects
    	// cannot be null and the subarrayId cannot be negative.
    	if (config == null)
    		throw new IllegalArgumentException(name() + ": There is no configuration object!");
    	if (subarrayId < 0)
    		throw new IllegalArgumentException(name() + ": Invalid subarray-id!");
    	if (logger == null)
    		throw new IllegalArgumentException(name() + ": There is no logger!");
    	if (clock == null)
    		throw new IllegalArgumentException(name() + ": There is no clock!");
    }
    
    /**
     * Form a name of this scheduler, which includes its thread name and the id
     * of the subarray on which it operates.  This name has the following form:
     * 		Scheduler [task1] (subarray 1)
     * @return A string identifying this scheduler.
     */
    protected String name() {
    	return "Scheduler [" + Thread.currentThread().getName() + 
		"] (subarray " + subarrayId + ")";
    }
    
    
}
