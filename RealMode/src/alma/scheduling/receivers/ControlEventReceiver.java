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
package alma.scheduling.receivers;

import java.util.logging.Logger;

import alma.acs.container.ContainerServices;

import alma.Control.ExecBlockEvent;
import alma.Control.ExecBlockEventHelper;

import alma.scheduling.master_scheduler.ALMAArchive;
import alma.scheduling.master_scheduler.MasterSBQueue;
import alma.scheduling.master_scheduler.ProcessControlEvent;
import alma.scheduling.project_manager.ALMAPipeline;
import alma.scheduling.project_manager.ProjectManagerTaskControl;
import alma.scheduling.define.nc.*;

import org.omg.CosNotification.*;
import alma.acs.nc.*;

//Take consumer acs.nc stuff when using scheduling's nc
public class ControlEventReceiver extends Consumer {
    private ALMAPipeline pipeline;
    private ALMAArchive archive;
    private ContainerServices containerServices;
    private Logger logger;
    private ProjectManagerTaskControl pmTaskControl;
    private MasterSBQueue sbQueue;
    
    public ControlEventReceiver(ContainerServices cs, ALMAPipeline p, 
        ALMAArchive a, MasterSBQueue q){
        
        super(alma.Control.CHANNELNAME.value);
        this.containerServices = cs;
        this.logger = cs.getLogger();
        this.pipeline = p;
        this.archive = a;
        this.sbQueue = q;
        //System.out.println("#########ControlEventReceiver created######");
    }
    /*
    public ControlEventReceiver(ProjectManagerTaskControl pmtc,
                                    ALMAPipeline p, ALMAArchive a){
        this(p,a);
        this.pmTaskControl = pmtc;
    }
*/
    public void receive(ExecBlockEvent e) {
        logger.info("SCHEDULING: Starting to process the control event");
        String sb_id = e.sbId;
        ProcessControlEvent pce = new ProcessControlEvent(pmTaskControl,
                                        archive, pipeline, e, sbQueue);
        Thread t = new Thread(pce);
        t.start();
    }
    
    public void push_structured_event(StructuredEvent structuredEvent) 
        throws org.omg.CosEventComm.Disconnected {
    
        try {
            ExecBlockEvent e = 
                ExecBlockEventHelper.extract(
                    structuredEvent.filterable_data[0].value);
            logger.info("SCHEDULING: Got event from control");
            switch(e.type.value()) {
                case 0:
                    logger.info("SCHEDULING: Event reason = started");
                    logger.info("SCHEDULING: Received sb start event from control.");
                    break;
                case 1:
                    logger.info("SCHEDULING: Event reason = end");
                    logger.info("SCHEDULING: Received sb end event from control.");
                    receive(e);
                    break;
                default: 
                    logger.severe("SCHEDULING: Event reason = error");
                    break;
            }
                
        } catch(Exception e) {
            logger.severe("SCHEDULING: got something else "+e.toString());
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
