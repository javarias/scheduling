/*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by AUI (in the framework of the ALMA collaboration),
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
 * File NotificationChannelSimulator.java
 */

package alma.scheduling.simulator;

import java.util.logging.Logger;

import alma.acs.nc.*;
import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;

import alma.Control.ExecBlockEvent;

/**
 * Creates all the notification channels that the scheduling
 * subsystem needs for simulation purposes.
 *
 * @author Sohaila Roberts
 */
public class NotificationChannelSimulator {
    private ContainerServices container;
    private static Logger logger;
    private static AbstractNotificationChannel control;
    private static AbstractNotificationChannel pipeline;
    private static AbstractNotificationChannel telcal;
    
    public NotificationChannelSimulator(ContainerServices cs) {
        this.container = cs;
        this.logger = cs.getLogger();
        control = AbstractNotificationChannel.createNotificationChannel(
            AbstractNotificationChannel.CORBA, 
                alma.Control.CHANNELNAME.value, cs);
        pipeline = AbstractNotificationChannel.createNotificationChannel(
            AbstractNotificationChannel.CORBA, 
                alma.pipelinescience.CHANNELNAME.value, cs);
        telcal = AbstractNotificationChannel.createNotificationChannel(
            AbstractNotificationChannel.CORBA, 
                alma.TelCalPublisher.CHANNELNAME.value, cs);
        logger.fine("SCHEDULING SIMULATOR: NCs created.");
    }
    
    public static AbstractNotificationChannel getControlChannel() {
        return control;
    }
    
    public static AbstractNotificationChannel getPipelineChannel() {
        return pipeline;
    }
    
    public static AbstractNotificationChannel getTelCalChannel() {
        return telcal;
    }
    
    public static void sendControlEvent(ExecBlockEvent e) {
        control.publish(e);
        logger.fine("SCHEDULING SIMULATOR: Control sent an event.");
    }

    public static void sendPipelineEvent(){
    }
    public static void sendTelCalEvent() {
    }
}
