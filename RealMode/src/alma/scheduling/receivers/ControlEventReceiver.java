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
 * File ControlEventReceiver.java
 */
package ALMA.scheduling.receivers;

import ALMA.Control.ExecBlockEvent;
import ALMA.Control.ExecBlockEventHelper;

import ALMA.scheduling.master_scheduler.ALMAArchive;
import ALMA.scheduling.master_scheduler.ProcessControlEvent;
import ALMA.scheduling.project_manager.ALMAPipeline;
import ALMA.scheduling.project_manager.ProjectManagerTaskControl;
import ALMA.scheduling.define.nc.*;

import org.omg.CosNotification.*;
import alma.acs.nc.*;

//Take consumer acs.nc stuff when using scheduling's nc
public class ControlEventReceiver extends Consumer {
    private ALMAPipeline pipeline;
    private ALMAArchive archive;
    private ProjectManagerTaskControl pmTaskControl;
    
    public ControlEventReceiver(ALMAPipeline p, ALMAArchive a){
        super(ALMA.Control.CHANNELNAME.value);
        this.pipeline = p;
        this.archive = a;
        //System.out.println("#########ControlEventReceiver created######");
    }
    public ControlEventReceiver(ProjectManagerTaskControl pmtc,
                                    ALMAPipeline p, ALMAArchive a){
        this(p,a);
        this.pmTaskControl = pmtc;
    }

    public void receive(ExecBlockEvent e) {
        System.out.println("SCHEDULING: Starting to process the control event");
        String sb_id = e.sbId;
        ProcessControlEvent pce = new ProcessControlEvent(pmTaskControl,
                                        archive, pipeline, e);
        Thread t = new Thread(pce);
        t.start();
    }
    
    public void push_structured_event(StructuredEvent structuredEvent) 
        throws org.omg.CosEventComm.Disconnected {
    
        try {
            ExecBlockEvent e = 
                ExecBlockEventHelper.extract(
                    structuredEvent.filterable_data[0].value);
            System.out.println("SCHEDULING: Got event from control");
            switch(e.reason.value()) {
                case 0:
                    System.out.println("SCHEDULING: Event reason = started");
                    break;
                case 1:
                    System.out.println("SCHEDULING: Event reason = end");
                    receive(e);
                    break;
                /*
                case 2:
                    System.out.println("SCHEDULING: Event Status = failed");
                    break;
                case 3:
                    System.out.println("SCHEDULING: Event Status = timeout");
                    break;
                    */
                default: 
                    System.out.println("SCHEDULING: Event reason = error");
                    break;
            }
                
        } catch(Exception e) {
            System.out.println("SCHEDULING: got something else "+e.toString());
            e.printStackTrace();
        }
    }
    
    //////////////////////////////////////////////////
    /* Get Methods */

    /* Set Methods */
    public void setProjectManagerTaskControl(ProjectManagerTaskControl pmtc) {
        this.pmTaskControl = pmtc;
    }
    //////////////////////////////////////////////////
}
