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
 * File ProcessControlEvent.java
 */

package alma.scheduling.master_scheduler;

import alma.xmlentity.XmlEntityStruct;

import alma.Control.ExecBlockEvent;
import alma.Control.CompletionStatus;

import alma.scheduling.master_scheduler.MasterSBQueue;
import alma.scheduling.project_manager.ALMAPipeline;
import alma.scheduling.project_manager.ProjectManagerTaskControl;

import alma.entity.xmlbinding.pipelineprocessingrequest.*;
import alma.entity.xmlbinding.pipelineprocessingrequest.types.*;
import alma.entities.commonentity.EntityRefT;

/**
 * This class updates the schedblock and starts the pipeline after 
 * a control execblock end event has been received.
 *
 * @author Sohaila Roberts
 */
public class ProcessControlEvent implements Runnable {
    private ProjectManagerTaskControl pmTaskControl;
    private ALMAArchive archive;
    private ALMAPipeline pipeline;
    private ExecBlockEvent event;
    private MasterSBQueue sbQueue;
    
    public ProcessControlEvent(ProjectManagerTaskControl pmtc,
                                ALMAArchive a, ALMAPipeline p,
                                    ExecBlockEvent e, MasterSBQueue q) {

        this.pmTaskControl = pmtc;
        this.archive = a;
        this.pipeline =p;
        this.event = e;
        this.sbQueue = q;
    }                                

    public void run() {
        System.out.println("SCHEDULING: process control event started");
        updateSB(event);
        startPipeline(event.sbId);
        // interrupt project manager so that it can check to see if thats 
        //the last SB in the ObsUnitSet to be processed! (actually checks 
        //project now)
        //System.out.println("SCHEDULING: about to interrupt PM");
        //pmTaskControl.getTask().interrupt();
    }

    private void updateSB(ExecBlockEvent event) {
        try {
            archive.updateSchedBlock(event);
            sbQueue.updateSUnit(event);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public void startPipeline(String id) {
        PipelineProcessingRequest ppr1 = 
            pipeline.createPipelineProcessingRequest();
        ReductionUnitT ru = new ReductionUnitT();
        ru.setEntityId(id);
        ppr1.setReductionUnit(ru);
        //Store the ppr1 in the archive.
        archive.addPipelineProcessingRequest(ppr1);
        
        XmlEntityStruct ppr_struct = archive.getPipelineProcessingRequest(
                ppr1.getPipelineProcessingRequestEntity().getEntityId());
        
        try {
            String res = pipeline.processRequest(ppr_struct);
            System.out.println("SCHEDULING: "+res);
        } catch (SchedulingException se) {
            se.printStackTrace();
        }
    }
    
}
