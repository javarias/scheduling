/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
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
import alma.acs.component.client.ComponentClient;

import org.omg.CosNotification.*;
import alma.acs.nc.*;

import alma.scheduling.NothingCanBeScheduledEnum;
import alma.scheduling.NothingCanBeScheduledEvent;

/**
 * 
 * @author Sohaila Roberts
 */
public class SchedulingPublisher {
    private Logger logger;
    private AbstractNotificationChannel sched_nc;
    //private SimpleSupplier supplier;

	/**
	 * Create a Publisher object.
	 * @param isSimulation	True, if this a simulation.
	 * @param container The object that implements the container services.
	 */
	public SchedulingPublisher (boolean isSimulation, ContainerServices container) {
        /*
        this.supplier = new SimpleSupplier(alma.scheduling.CHANNELNAME.value,
                alma.scheduling.NothingCanBeScheduledEvent.class);
        */
        this.logger = container.getLogger();
		logger.info("SCHEDULING: Scheduling Publisher has been created");
        /* Scheduling NC stuff */
        sched_nc = AbstractNotificationChannel.createNotificationChannel(
                AbstractNotificationChannel.CORBA,
                    alma.scheduling.CHANNELNAME.value);
                
	}

    /** 
     * Use if using SimpleSupplier
    public void publish() {
        try {
            NothingCanBeScheduledEvent e = new NothingCanBeScheduledEvent(
                NothingCanBeScheduledEnum.OTHER, 0, "nothing to schedule");
            supplier.publishEvent(e);
            logger.info("SCHEDULING: Event sent.");
        } catch(Exception ex) {
            logger.severe("SCHEDULING: Event not published!");
        }
    }
     */

    public void publishEvent(String reason) {
        try {
            NothingCanBeScheduledEvent event = 
                        new NothingCanBeScheduledEvent(
                            NothingCanBeScheduledEnum.OTHER, 0, reason);
            //sched_nc.publish(event);
            logger.info("SCHEDULING: Event sent.");
        } catch(Exception e) {
            logger.severe("SCHEDULING: Could not publish event. "+e.toString());
        }
    }

    public void publishEvent(NothingCanBeScheduledEnum sched_enum, String comment) {
        try {
            NothingCanBeScheduledEvent event = 
                        new NothingCanBeScheduledEvent(sched_enum, 0 , comment);
            sched_nc.publish(event);
            //supplier.publishEvent(event);
            logger.info("SCHEDULING: Event sent.");
        } catch(Exception e) {
            logger.severe("SCHEDULING: Could not publish event. "+e.toString());
        }
    }
    /**
     * A generic publish event method which sends out the event with 
     * the reason "Nothing could be scheduled."
     */
    public void publishEvent() {
        publishEvent("Nothing could be scheduled.");
    }

    /**
     * Shuts down the scheduling notification channel.
     */
    public void shutdown() {
        logger.info("SCHEDULING: Shutting down SCHEDULING notification channel.");
        //supplier.disconnect();
        sched_nc.deactivate();
    }
	
	public static void main(String[] args) {
        /*
        try {
            ComponentClient client = new ComponentClient(null,
                System.getProperty("ACS.manager"), 
                    "Scheduling publisher test");
            ContainerServices cs = client.getContainerServices();
            SchedulingPublisher sp = new SchedulingPublisher(false, cs);
            sp.publishEvent(NothingCanBeScheduledEnum.NOTHING_VISIBLE,
                "nothing to see!");
            Thread.sleep(2000);
            sp.publishEvent(NothingCanBeScheduledEnum.BAD_WEATHER,
                "Thunder Storms!");
            Thread.sleep(2000);
            sp.publishEvent(NothingCanBeScheduledEnum.INSUFFICIENT_RESOURCES,
                "all antennas in maintence!");
            Thread.sleep(2000);
            sp.publishEvent(NothingCanBeScheduledEnum.NOT_OPTIMAL,
                "wait 2 hours and it'll be better!");
            Thread.sleep(2000);
            sp.publishEvent("Unknown Reason!");
            
            sp.shutdown();
            alma.acs.nc.Helper.disconnect();
        } catch(Exception e){}
        */
	}
}

