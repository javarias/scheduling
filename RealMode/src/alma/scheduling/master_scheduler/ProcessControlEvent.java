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
 * File ProcessControlEvent.java
 */

package ALMA.scheduling.master_scheduler;

import ALMA.Control.ExecBlockEvent;
import ALMA.Control.CompletionStatus;

import ALMA.scheduling.project_manager.ALMAPipeline;
import ALMA.scheduling.project_manager.ProjectManagerTaskControl;

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
    
    public ProcessControlEvent(ProjectManagerTaskControl pmtc,
                                ALMAArchive a, ALMAPipeline p,
                                    ExecBlockEvent e) {

        this.pmTaskControl = pmtc;
        this.archive = a;
        this.pipeline =p;
        this.event = e;
    }                                

    public void run() {
        System.out.println("SCHEDULING: process control event started");
        updateSB(event);
        //System.out.println("SCHEDULING: Event Status = "+event.blockStatus.toString());
        /*
        switch(event.blockStatus.value()) {
            case 0:
                //System.out.println("SCHEDULING: Event Status = processing");
                break;
            case 1:
                //System.out.println("SCHEDULING: Event Status = ok");
                updateSB(event);
                break;
            case 2:
                //System.out.println("SCHEDULING: Event Status = failed");
                break;
            case 3:
                //System.out.println("SCHEDULING: Event Status = timeout");
                break;
            default: 
                //System.out.println("SCHEDULING: Event Status error");
                break;
        }*/
//        updateSB(event);
        //updateSB(event.sbId);
        //startPipeline(event.sbId);
    }

    private void updateSB(ExecBlockEvent event) {
        try {
            archive.updateSchedBlock(event);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void startPipeline(String id) {
        PipelineProcessingRequest ppr = 
            pipeline.createPipelineProcessingRequest();
        ReductionUnitT ru = new ReductionUnitT();
        ru.setEntityId(id);
        ppr.setReductionUnit(ru);
        //Store the ppr in the archive.
        archive.addPipelineProcessingRequest(ppr);
        try {
            String res = pipeline.processRequest(ppr);
        } catch (SchedulingException se) {
            se.printStackTrace();
        }
    }
    
}
