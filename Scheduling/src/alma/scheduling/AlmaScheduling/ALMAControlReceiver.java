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
 * File ALMAControlReceiver.java
 */
package alma.scheduling.AlmaScheduling;

import alma.acs.container.ContainerServices;

import alma.Control.ExecBlockEvent;

import alma.scheduling.Event.Receivers.ControlEventReceiver;

/**
 * This Class receives the ExecBlockEvent sent out by control. 
 * @author Sohaila Lucero
 */
public class ALMAControlReceiver extends ControlEventReceiver {

    public ALMAControlReceiver(ContainerServices cs){
        this.logger = cs.getLogger();
    }

    /**
     * When an ExecBlockEvent is received by the scheduling subsystem it is
     * received here and the type is determined. Once the type is determined 
     * the appropriate action is taken.
     *
     * @param ExecBlockEvent The event sent out by the control subsystem.
     */ 
    public void receive(ExecBlockEvent e) {
        logger.info("SCHEDULING: Starting to process the control event");
        String sb_id = e.sbId;
        switch(e.type.value()) {
            case 0:
                logger.info("SCHEDULING: Event reason = started");
                logger.info("SCHEDULING: Received sb start event from control.");
                break;
            case 1:
                logger.info("SCHEDULING: Event reason = end");
                logger.info("SCHEDULING: Received sb end event from control.");
                //ProcessControlEvent pce = new ProcessControlEvent(pmTaskControl,
                //                         archive, pipeline, e, sbQueue);
                //Thread t = new Thread(pce);
                //t.start();
                break;
            default: 
                logger.severe("SCHEDULING: Event reason = error");
                break;
        }

    }
}
