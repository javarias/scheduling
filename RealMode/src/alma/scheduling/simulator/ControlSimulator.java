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
 * File ControlSimulator.java
 */
 
package alma.scheduling.simulator;

import java.util.logging.Logger;

import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;

import alma.scheduling.define.STime;
import alma.scheduling.master_scheduler.ControlProxy;

import alma.Control.*;
import alma.Control.ExecBlockEvent;
import alma.entity.xmlbinding.execblock.*;

/**
 * Description 
 * 
 * @author Sohaila Roberts
 */
public class ControlSimulator implements ControlProxy {

    private ClockSimulator clock;
    private WeatherModel weather;
    private ContainerServices container;
    private Logger logger;
    private ExecBlock eb;
    private ExecBlockEntityT eb_entity;
    private ExecBlockEvent control_event;
    private short ant_id =1;

	/**
     * Creates an instance of the scheduling ControlSimulator
	 */
	public ControlSimulator(ContainerServices cs) {
		super();
		clock = new ClockSimulator ();
		weather = new WeatherModel ();
        this.container = cs;
        this.logger = cs.getLogger();
	}

	/**
     * When the scheduling system is in simulation mode this function is 
     * called from the ALMADispatcher.
     * It simulates the control system sending an event and creating
     * ExecBlocks.
	 */
	public void sendToControl(String id, STime time) {
        //create an ExecBlock
        eb = new ExecBlock();
        eb_entity = new ExecBlockEntityT();
        try { 
            container.assignUniqueEntityId(eb_entity);
        } catch(ContainerException e){
            logger.severe("SCHEDULING SIMULATOR: could not get id for ExecBlock");
            return;
        }
        eb.setExecBlockEntityT(eb_entity);
        eb.setSchedBlockId(id);
                            
        // create a control event to say sb has started 
        // and then an event to say its completed
        control_event = new ExecBlockEvent(EventReason.START,
                                eb.getExecBlockEntityT().getEntityId(),
                                eb.getSchedBlockId(),
                                ant_id, CompletionStatus.NOT_COMPLETE,
                                System.currentTimeMillis());
        NotificationChannelSimulator.sendControlEvent(control_event);
        logger.fine("SCHEDULING SIMULATOR: Control started....");
        try {
            logger.fine("SCHEDULING SIMULATOR: sleeping..");
            Thread.sleep(7000);
        } catch(Exception e) {
        }
        control_event = new ExecBlockEvent(EventReason.END,
                                eb.getExecBlockEntityT().getEntityId(),
                                eb.getSchedBlockId(),
                                ant_id, CompletionStatus.COMPLETED_OK,
                                System.currentTimeMillis());
        NotificationChannelSimulator.sendControlEvent(control_event);
        logger.fine("SCHEDULING SIMULATOR: ....Control ended");
	}

	public static void main(String[] args) {
		System.out.println("Unit test of control simulator.");
		//ControlSimulator control = new ControlSimulator();
		System.out.println("End unit test of control simulator.");
	}
}

