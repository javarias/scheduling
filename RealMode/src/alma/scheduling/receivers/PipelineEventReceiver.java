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
package ALMA.scheduling.receivers;

import org.omg.CosNotification.*;
import ALMA.acsnc.*;
import alma.acs.nc.*;

import ALMA.pipelinescience.ScienceProcessingRequestEnd;
import ALMA.pipelinescience.ScienceProcessingRequestEndHelper;

import ALMA.scheduling.NothingCanBeScheduledEvent;
import ALMA.scheduling.NothingCanBeScheduledEventHelper;
import ALMA.scheduling.master_scheduler.ALMAArchive;
import ALMA.scheduling.define.nc.*;
import ALMA.scheduling.project_manager.ALMAPipeline;
import ALMA.scheduling.project_manager.ProjectManagerTaskControl;

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

    public PipelineEventReceiver(ALMAPipeline p, ALMAArchive a) {
        super(ALMA.pipelinescience.CHANNELNAME.value);
        this.pipeline = p;
        this.archive = a;
        System.out.println("SCHEDULING: PipelineEventListener created.");
    }
    public PipelineEventReceiver(ProjectManagerTaskControl pmtc,
                                    ALMAPipeline p, ALMAArchive a) {

        this(p, a);
        this.pmTaskControl = pmtc;
    }

    public void receive(ScienceProcessingRequestEnd e) {
        System.out.println("SCHEDULING: PipelineEnd event received!");
        //create a new ProcessPipelineEvent thread/class
    }

    public void push_structured_event(StructuredEvent structuredEvent) 
        throws org.omg.CosEventComm.Disconnected {
    
        try {
            ScienceProcessingRequestEnd e = 
                ScienceProcessingRequestEndHelper.extract(
                    structuredEvent.filterable_data[0].value);
            receive(e);
            System.out.println("SCHEDULING: Got event from Pipeline");
        } catch(Exception e) {
            System.out.println("SCHEDULING: Got something else from Pipeline");
            System.out.println("SCHEDULING: "+e.toString());
        }
    }
    
    
    public void sendNCBSEvent() {
        NothingCanBeScheduledEvent ncbs = new NothingCanBeScheduledEvent(
            "Pipeline is done, Shut down scheduling system for R0+!");
        //pipeline.sendNothingCanBeScheduledEvent(ncbs);
    }
    //////////////////////////////////////////
    /* Get Methods */

    /* Set Methods */
    public void setProjectManagerTaskControl(ProjectManagerTaskControl pmtc) {
        this.pmTaskControl = pmtc;
    }
    //////////////////////////////////////////
    
}
