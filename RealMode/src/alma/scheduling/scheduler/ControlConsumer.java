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
 * File ControlConsumer.java
 * 
 */
package alma.scheduling.scheduler;

import org.omg.CosNotification.*;
import ALMA.acsnc.*;
import alma.acs.nc.*;

import alma.entity.xmlbinding.execblock.*;
import alma.entity.xmlbinding.schedblock.*;
import alma.acs.container.ContainerServices;

import alma.scheduling.master_scheduler.ALMAArchive;

//import ALMA.Control.ExecBlockEndEvent;
//import ALMA.Control.ExecBlockEndEventHelper;

/**
 *  @author Sohaila Roberts
 */
public class ControlConsumer /*extends Consumer */{

    private ContainerServices container;

    public ControlConsumer(ContainerServices cs) {
        this.container = cs;
        //super( control's channel name here .value);
    }

    public void push_structured_event(StructuredEvent structuredEvent) 
            throws org.omg.CosEventComm.Disconnected {
       /* 
        try {
            ExecBlockEndEvent execBlockEnd = 
                ExecBlockEndEventHelper.extract(
                    structuredEvent.filterable_data[0].value);
            updateSBStatus(execBlockEnd);
        } catch(Exception e) {
        }
        */
    }

    /* This will have to be done in a different way most likely!
     * mainly done like this to make R0+ happy ;)
     */
     /*
    public void updateSBStatus(ExecBlockEndEvent event) {
        ALMAArchive archive = new ALMAArchive(false, container);
        ExecBlock execBlock = (ExecBlock)archive.getExecBlock(event.execID);
        String sb_id = execBlock.getSchedBlockId();
        SchedBlock sb = (SchedBlock) archive.getSchedBlock(sb_id);
    }
    */
    
    
}
