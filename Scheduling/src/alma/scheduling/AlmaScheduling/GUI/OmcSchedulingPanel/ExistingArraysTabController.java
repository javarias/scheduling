/*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by AUI (in the framework of the ALMA collaboration),
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
 * File ExistingArraysTabController.java
 */

package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import alma.Control.DestroyedManualArrayEvent;
import alma.Control.CreatedManualArrayEvent;
import alma.Control.DestroyedAutomaticArrayEvent;
import alma.Control.CreatedAutomaticArrayEvent;
import alma.Control.ExecBlockStartedEvent;
import alma.Control.ExecBlockEndedEvent;
import alma.acs.nc.Consumer;
import alma.exec.extension.subsystemplugin.PluginContainerServices;

public class ExistingArraysTabController extends SchedulingPanelController {
    private ExistingArraysTab parent;
  //  private PluginContainerServices container; 
    private Consumer consumer;

    public ExistingArraysTabController(PluginContainerServices cs, ExistingArraysTab p){
        super(cs);
   //     container = cs;
        parent = p;
        try {
            consumer = new Consumer(alma.Control.CHANNELNAME_CONTROLSYSTEM.value, cs);
            
            consumer.addSubscription(alma.Control.CreatedManualArrayEvent.class, this);
            consumer.addSubscription(alma.Control.CreatedAutomaticArrayEvent.class, this);
            consumer.addSubscription(alma.Control.DestroyedAutomaticArrayEvent.class, this);
            consumer.addSubscription(alma.Control.DestroyedManualArrayEvent.class, this);
         
          //  consumer.addSubscription(alma.Control.ExecBlockStartedEvent.class, this);
            //consumer.addSubscription(alma.Control.ExecBlockEndedEvent.class, this);
            
            consumer.consumerReady();
        } catch(Exception e){
            e.printStackTrace();
            logger.warning("SCHEDULING_PANEL: Problem getting NC consumer for control system channel, won't see existing arrays");
        }
    }

    public void receive(CreatedAutomaticArrayEvent event) {
        String name = event.arrayName;
        logger.info("SP: Received created array event for "+name);
        parent.addArray(name, "automatic");
    }
    public void receive(DestroyedAutomaticArrayEvent event) {
        String name = event.arrayName;
        logger.info("SP: Received destroy array event for "+name+" in existing array tab");
        parent.removeArray(name);
    }
    public void receive(CreatedManualArrayEvent event) {
        String name = event.arrayName;
        logger.info("SP: Received created array event for "+name);
        parent.addArray(name, "manual");
    }
    public void receive(DestroyedManualArrayEvent event) {
        String name = event.arrayName;
        logger.info("SP: Received destroy array event for "+name+" in existing array tab");
        parent.removeArray(name);
    }

    /*
     public void receive(ExecBlockStartedEvent e) {
         logger.info("Existing array tab got exec block started event");
     }
     public void receive(ExecBlockEndedEvent e) {
         logger.info("Existing array tab got exec block ended event");
     }*/
        
}
