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
 * File PointingReducedEventReceiver.java
 */
package ALMA.scheduling.receivers;

import java.util.logging.Logger;

import alma.acs.container.ContainerServices;

import ALMA.scheduling.project_manager.ProjectManagerTaskControl;
import ALMA.scheduling.define.nc.*;

import ALMA.TelCalPublisher.PointingReducedEvent;
import ALMA.TelCalPublisher.PointingReducedEventHelper;

import org.omg.CosNotification.*;
import alma.acs.nc.*;

//Take consumer acs.nc stuff when using scheduling's nc
public class PointingReducedEventReceiver extends Consumer {
    private ProjectManagerTaskControl pmTaskControl;
    private Logger logger;
    private ContainerServices containerServices;
    
    public PointingReducedEventReceiver(ContainerServices cs){
        super(ALMA.TelCalPublisher.CHANNELNAME.value);
        this.containerServices = cs;
        this.logger = cs.getLogger();
    }

    public void receive(PointingReducedEvent e) {
        System.out.println("SCHEDULING: Starting to process the PointingReduced event");
    }
    
    public void push_structured_event(StructuredEvent structuredEvent) 
        throws org.omg.CosEventComm.Disconnected {
    
        try {
            PointingReducedEvent e = 
                PointingReducedEventHelper.extract(
                    structuredEvent.filterable_data[0].value);
            System.out.println("SCHEDULING: Got PointingReduced event");
            receive(e);
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
