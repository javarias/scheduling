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
 * File FocusReducedEventReceiver.java
 */
package ALMA.scheduling.receivers;

import java.util.logging.Logger;

import alma.acs.container.ContainerServices; 

import ALMA.TelCalPublisher.FocusReducedEvent;
import ALMA.TelCalPublisher.FocusReducedEventHelper;

import ALMA.scheduling.project_manager.ProjectManagerTaskControl;
import ALMA.scheduling.define.nc.*;

import org.omg.CosNotification.*;
import alma.acs.nc.*;

//Take consumer acs.nc stuff when using scheduling's nc
public class FocusReducedEventReceiver extends Consumer {
    private ProjectManagerTaskControl pmTaskControl;
    private Logger logger;
    private ContainerServices containerServices;
    
    public FocusReducedEventReceiver(ContainerServices cs){
        super(ALMA.TelCalPublisher.CHANNELNAME.value);
        this.containerServices = cs;
        this.logger = cs.getLogger();
    }

    public void receive(FocusReducedEvent e) {
        logger.info("SCHEDULING: Starting to process the FocusReduced event");
    }
    
    public void push_structured_event(StructuredEvent structuredEvent) 
        throws org.omg.CosEventComm.Disconnected {
    
        try {
            FocusReducedEvent e = 
                FocusReducedEventHelper.extract(
                    structuredEvent.filterable_data[0].value);
            logger.info("SCHEDULING: Got FocusReduced event");
            receive(e);
        } catch(Exception e) {
            logger.severe("SCHEDULING: got something else "+e.toString());
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
