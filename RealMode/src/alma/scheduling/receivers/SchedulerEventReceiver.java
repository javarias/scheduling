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
 * File SchedulerEventReceiver.java
 * 
 */
package alma.scheduling.receivers;

import org.omg.CosNotification.*;
import alma.acsnc.*;
import alma.acs.nc.*;
import alma.acs.container.ContainerServices;

import alma.Control.ExecBlockEvent;
import alma.Control.ExecBlockEventHelper;


import alma.scheduling.scheduler.*;
import alma.scheduling.project_manager.ProjectManagerTaskControl;

/** 
 * Listens to control for events that the scheduler needs/wants
 * to know about. 
 *
 * @author Sohaila Roberts
 */
//take out consumer stuff when using scheduling's nc
public class SchedulerEventReceiver extends Consumer {
    private ProjectManagerTaskControl pmTaskControl;
    private SchedulerTaskControl schedTaskControl;
    private Scheduler scheduler;

    public SchedulerEventReceiver(ContainerServices cs, Scheduler s) throws Exception {
        super(alma.Control.CHANNELNAME.value, cs);
        this.scheduler = s;
        System.out.println("SCHEDULING: SchedulerEventReceiver created.");
    }
    public SchedulerEventReceiver(ContainerServices cs, 
                                    SchedulerTaskControl stc,
                                        Scheduler s) throws Exception  {

        this(cs, s);
        this.schedTaskControl = stc;
    }

    public void receive(ExecBlockEvent e) {
        System.out.println("SCHEDULING: Control event received in scheduler receiver!");
        ProcessSchedEvent pse = new ProcessSchedEvent(scheduler, e);
        Thread t = new Thread(pse);
        t.start();
    }

    public void push_structured_event(StructuredEvent structuredEvent) 
        throws org.omg.CosEventComm.Disconnected {
    
        try {
            ExecBlockEvent e = 
                ExecBlockEventHelper.extract(
                    structuredEvent.filterable_data[0].value);
            receive(e);
            System.out.println("SCHEDULING: Got event from Control (scheduler receiver)");
        } catch(Exception e) {
            System.out.println("SCHEDULING: Got something else from Control (scheduler receiver)");
            System.out.println("SCHEDULING: "+e.toString());
        }
    }
    
    
    //////////////////////////////////////////
    /* Get Methods */

    /* Set Methods */
    public void setProjectManagerTaskControl(ProjectManagerTaskControl pmtc) {
        this.pmTaskControl = pmtc;
    }
    //////////////////////////////////////////
    
}
