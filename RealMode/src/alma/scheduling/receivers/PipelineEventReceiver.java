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
 * File PipelineEventReceiver.java
 * 
 */
package alma.scheduling.receivers;

import java.util.logging.Logger;
import org.omg.CosNotification.*;

import alma.acsnc.*;
import alma.acs.nc.*;
import alma.acs.container.ContainerServices;

import alma.pipelinescience.ScienceProcessingRequestEnd;
import alma.pipelinescience.ScienceProcessingRequestEndHelper;

import alma.scheduling.master_scheduler.ALMAArchive;
import alma.scheduling.project_manager.ALMAPipeline;
import alma.scheduling.project_manager.ProjectManagerTaskControl;

/** 
 *  Listens to the pipeline for events
 *  @author Sohaila Roberts
 */
//take out consumer stuff when using scheduling's nc
//public class PipelineEventReceiver extends Receiver {
public class PipelineEventReceiver extends Consumer {
    private ALMAPipeline pipeline;
    private ALMAArchive archive;
    private ProjectManagerTaskControl pmTaskControl;
    private Logger logger;
    private ContainerServices containerServices;

    public PipelineEventReceiver(ContainerServices cs, ALMAPipeline p, 
        ALMAArchive a) {
        
        super(alma.pipelinescience.CHANNELNAME.value);
        this.pipeline = p;
        this.archive = a;
        this.containerServices = cs;
        this.logger = cs.getLogger();
        logger.info("SCHEDULING: PipelineEventListener created.");
    }
    /*
    public PipelineEventReceiver(ProjectManagerTaskControl pmtc,
                                    ALMAPipeline p, ALMAArchive a) {

        this(p, a);
        this.pmTaskControl = pmtc;
    }
    */

    public void receive(ScienceProcessingRequestEnd e) {
        logger.info("SCHEDULING: PipelineEnd event received!");
        //create a new ProcessPipelineEvent thread/class
    }

    public void push_structured_event(StructuredEvent structuredEvent) 
        throws org.omg.CosEventComm.Disconnected {
    
        try {
            ScienceProcessingRequestEnd e = 
                ScienceProcessingRequestEndHelper.extract(
                    structuredEvent.filterable_data[0].value);
            receive(e);
            logger.info("SCHEDULING: Got event from Pipeline");
        } catch(Exception e) {
            logger.severe("SCHEDULING: Got something else from Pipeline");
            logger.severe("SCHEDULING: "+e.toString());
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
