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
 * File PipelineEventListener.java
 * 
 */
package alma.scheduling.project_manager;

import org.omg.CosNotification.*;
import ALMA.acsnc.*;
import alma.acs.nc.*;
import alma.pipelinescience.ScienceProcessingRequestEnd;
import alma.pipelinescience.ScienceProcessingRequestEndHelper;

import alma.scheduling.NothingCanBeScheduledEvent;
import alma.scheduling.NothingCanBeScheduledEventHelper;

/* 
 *  Listens to the pipeline for events
 *  @author Sohaila Roberts
 */
public class PipelineEventListener extends Consumer {
    private ALMAPipeline pipeline; 

    public PipelineEventListener(ALMAPipeline p){
        super(alma.pipelinescience.CHANNELNAME.value);
        this.pipeline = p;
        System.out.println("PipelineEventListener created.");
    }

    public void push_structured_event(StructuredEvent structuredEvent) 
            throws org.omg.CosEventComm.Disconnected {

        try {
            ScienceProcessingRequestEnd spre = 
                ScienceProcessingRequestEndHelper.extract(
                    structuredEvent.filterable_data[0].value);
            sendNCBSEvent();
        } catch (Exception e) {
            System.out.println("something NULL in push pipeline thing");
            System.err.println(e);
        }
    }

    public void sendNCBSEvent() {
        NothingCanBeScheduledEvent ncbs = new NothingCanBeScheduledEvent(
            "Pipeline is done, Shut down scheduling system for R0+!");
        pipeline.sendNothingCanBeScheduledEvent(ncbs);
        /*
        String[] names = new String[3];
        names[SimpleSupplier.CHANNELPOS] = alma.scheduling.CHANNELNAME.value;
        names[SimpleSupplier.TYPEPOS] = ALMA.acsnc.DEFAULTTYPE.value;
        names[SimpleSupplier.HELPERPOS] = new 
            String("alma.scheduling.NothingCanBeScheduledEventHelper");
        SimpleSupplier supplier = new SimpleSupplier(names);
        try {
            supplier.publishEvent(ncbs);
        } catch(Exception e) {
            System.out.println("error when sending ncbs: "+e.toString());
        }
        */
    }
}
