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
 * File ProcessSchedEvent.java
 */

package alma.scheduling.scheduler;

import alma.Control.ExecBlockEvent;
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
public class ProcessSchedEvent implements Runnable {
    private Scheduler scheduler;
    private ExecBlockEvent event;
    
    public ProcessSchedEvent(Scheduler s, ExecBlockEvent e) {

        this.scheduler = s;
        this.event = e;
    }                                

    public void run() {
        System.out.println("SCHEDULING: process sched event started");

        //check if sb is in this scheduler
        if(scheduler == null) {
            System.out.println("SCHEDULING: SCHEDULER IS NULL!");
        }
        if(event == null) {
            System.out.println("SCHEDULING: event IS NULL!");
        }
        if(scheduler.isInQueue(event.sbId)) {
            System.out.println("SCHEDULING: id is in queue.. do something");
            scheduler.getSchedulerTaskControl().getTask().interrupt();
        }
        // if yes check to see if its the last one in its project
                //this will change to be obsunitset
        //if not the last complete one select a new sb
        //if last one call pipeline!
    }

}
