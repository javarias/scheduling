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
 * File TestSchedConsumer.java
 * 
 */
package ALMA.scheduling.test;

import org.omg.CosNotification.*;
import alma.acs.nc.*;

import ALMA.scheduling.MS;
import ALMA.scheduling.InvalidOperation;
import ALMA.scheduling.NothingCanBeScheduledEvent;
import ALMA.scheduling.NothingCanBeScheduledEventHelper;

import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;

public class TestSchedConsumer extends Consumer {
    private ContainerServices container;
    private MS masterSchedulerComp;

    public TestSchedConsumer(ContainerServices cs) {
        super(ALMA.scheduling.CHANNELNAME.value);
        System.out.println("Got scheduling channel");
        this.container=cs;
        try {
            this.masterSchedulerComp = ALMA.scheduling.MSHelper.narrow(
                container.getComponent("MASTER_SCHEDULER"));
        } catch (ContainerException e) {
        }

    }

    public void push_structured_event(StructuredEvent structuredEvent) 
            throws org.omg.CosEventComm.Disconnected {
        
        try {
            NothingCanBeScheduledEvent event = 
                NothingCanBeScheduledEventHelper.extract(structuredEvent.filterable_data[0].value);
            System.out.println("SCHED_TEST: Got NothingCanBeScheduledEvent");
            System.out.println("SCHED_TEST: "+event.reason);
        } catch (Exception e) {
        }
    }
        
    public static void main(String[] args) {
    }
}

