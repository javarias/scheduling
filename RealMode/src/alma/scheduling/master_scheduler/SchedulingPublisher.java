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
 * File SchedulingPublisher.java
 */
 
package alma.scheduling.master_scheduler;

import java.util.logging.Logger;

import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;

import org.omg.CosNotification.*;
import alma.acs.nc.*;

import alma.scheduling.define.nc.*;
import alma.scheduling.NothingCanBeScheduledEvent;

/**
 * 
 * @author Sohaila Roberts
 */
public class SchedulingPublisher {
    private Logger logger;
    //private sched_channel
	/**
	 * Create a clock object.
	 * @param isSimulation	True, if this a simulation.
	 * @param container The object that implements the container services.
	 */
	public SchedulingPublisher (boolean isSimulation, ContainerServices container) {
        this.logger = container.getLogger();
		logger.info("SCHEDULING: Scheduling Publisher has been created");
        /* Scheduling NC stuff */
        String[] eventType = new String[1];
        eventType[0] = "alma.scheduling.NothingCanBeScheduledEvent";
        if(isSimulation) { //create local channel
            LocalNotificationChannel sched = 
                new LocalNotificationChannel(
                    alma.scheduling.CHANNELNAME.value);
        } else { //create corba channel
            CorbaNotificationChannel sched = 
                new CorbaNotificationChannel(
                   alma.scheduling.CHANNELNAME.value);
        }
        
	}
	
	public static void main(String[] args) {
	}
}

