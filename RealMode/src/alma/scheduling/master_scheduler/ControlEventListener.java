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
 * File ControlEventListener.java
 */
package alma.scheduling.master_scheduler;

import ALMA.acsnc.*;
import alma.acs.nc.*;
import org.omg.CosNotification.*;

import ALMA.Control.ExecBlockEndEvent;
import ALMA.Control.ExecBlockEndEventHelper;

public class ControlEventListener extends Consumer {
    private ALMADispatcher dispatcher;
    
    public ControlEventListener(ALMADispatcher d) {
        super(ALMA.Control.CHANNELNAME.value);
        this.dispatcher = d;
    }

    public void push_structured_event(StructuredEvent structuredEvent)
            throws org.omg.CosEventComm.Disconnected {


        try {
            ExecBlockEndEvent event = 
                ExecBlockEndEventHelper.extract(
                    structuredEvent.filterable_data[0].value);
            System.out.println("#####ExecBlockEndEvent gotten!####"); //+event);
            String id = event.sbId;
            System.out.println(id);
            //update sb status in the archive
            updateSB(id);
            //start the pipeline!
            startPipeline(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateSB(String id) {
        dispatcher.updateSB(id);
    }

    public void startPipeline(String sb_id) {
        dispatcher.startPipeline(sb_id);
    }

}
