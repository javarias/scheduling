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
 * File ALMASchedPublisher.java
 */
package alma.scheduling.AlmaScheduling;

import java.util.logging.Logger;

import alma.scheduling.NothingCanBeScheduledEvent;
import alma.scheduling.NothingCanBeScheduledEnum;
import alma.scheduling.Event.Publishers.PublishEvent;

import alma.acs.container.ContainerServices;
import alma.acs.nc.AbstractNotificationChannel;
//import alma.acs.nc.SimpleSupplier;

/**
 * This class will publish a nothing can be scheduled event
 * over the acs notification channel when there is nothing
 * that can be scheduled.
 *
 * @version 1.00 July 29, 2004
 * @author Sohaila Lucero
 */
public class ALMASchedPublisher extends PublishEvent {
    private ContainerServices container;
    //private SimpleSupplier sup;

    public ALMASchedPublisher(ContainerServices cs) {
        super();
        this.container = cs;
        this.logger = cs.getLogger();
        this.sched_nc = AbstractNotificationChannel.createNotificationChannel(
            AbstractNotificationChannel.CORBA, 
                alma.scheduling.CHANNELNAME.value, 
                    container);
        /*
        try {
        sup = new SimpleSupplier(alma.scheduling.CHANNELNAME.value, cs);
        } catch(Exception e) {}
        */
    }

    public void publish(NothingCanBeScheduledEnum reason, int time, String comment){
        try {
            NothingCanBeScheduledEvent event = 
                new NothingCanBeScheduledEvent(reason, time, comment);
            logger.info("SCHEDULING: Event created!");
            sched_nc.publish(event);
            logger.info("SCHEDULING: Event Sent!");
        } catch(Exception e) {
            logger.severe("SCHEDULING: Error publishing event.");
            e.printStackTrace();
        }
    }

    public void publish(String reason) {
        publish(NothingCanBeScheduledEnum.OTHER,
            (int)System.currentTimeMillis(), "");
    }
}
