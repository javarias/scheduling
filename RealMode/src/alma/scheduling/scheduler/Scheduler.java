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
 * File Scheduler.java
 * 
 */
package ALMA.scheduling.scheduler;

import java.util.Vector;
import java.util.logging.Logger;
import java.util.logging.Level;

import ALMA.scheduling.define.STime;
import ALMA.scheduling.master_scheduler.State;
import ALMA.scheduling.master_scheduler.MasterSBQueue;
import ALMA.scheduling.master_scheduler.ALMATelescopeOperator;
import ALMA.scheduling.master_scheduler.ALMADispatcher;
import ALMA.scheduling.master_scheduler.Message;
import ALMA.scheduling.master_scheduler.MessageQueue;

import ALMA.scheduling.project_manager.ProjectManager;

import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;

import alma.entity.xmlbinding.schedblock.SchedBlock;
//import alma.bo.SchedBlock;

/**
 * Scheduler.java
 * 
 * The Scheduler class is the major controlling class in the 
 * scheduler package.  See Scheduling Subsystem Design document, 
 * section 3.2.3.
 * 
 * @version 1.00 Feb 27, 2003
 * @author Allen Farris
 *
 */
public class Scheduler implements Runnable {
    
    /**
     * The operational mode of the scheduling subsystem.  It is either
	 * in simulation mode or in real mode.
     */
    private boolean isSimulation;
    /**
     * The state of the scheduler
     */
    private State schedulerState;
    /**
     *  The mode of the scheduler.
     */
    private String mode;
    private MasterSBQueue queue;
    private ContainerServices container;
    private ALMATelescopeOperator operator;
    private ALMADispatcher dispatcher;
    private ProjectManager projectManager;
    private Logger logger;
    private MessageQueue messageQueue;
    
    public Scheduler(boolean isSimulation, 
                      ContainerServices c, 
                       ALMATelescopeOperator o, 
                        ALMADispatcher d,
                         MasterSBQueue q, 
                          MessageQueue mq, 
                           ProjectManager pm,
                            String m) {
        this.isSimulation = isSimulation;
        this.container = c;
        this.operator = o;
        this.dispatcher = d;
        this.queue = q;
        this.messageQueue = mq;
        this.schedulerState = new State(State.NEW);
        this.mode = m;
        this.projectManager = pm;

        logger = container.getLogger();
        
        // Subscribe to Control's NC and Pipeline's NC
        
        /* put this after connecting to the control component
        c_consumer = new ControlConsumer();
        c_consumer.addSubscription(ALMA.acsnc.DEFAULTTYPE.value);
        c_consumer.consumerReady();
        */
        
        /* put this after connecting to the pipeline component
        p_consumer = new PipelineConsumer();
        p_consumer.addSubscription(ALMA.acsnc.DEFAULTTYPE.value);
        p_consumer.consumerReady();
        */
    }

    public void initialize() {
        schedulerState = State.INITIALIZED;
    }
    

    public void run() {
        schedulerState = State.EXECUTING;
        System.out.println("Scheduler is running in "+mode+" mode!");
        Vector uidList = queue.getAllUid();
        String[] ids = new String[uidList.size()];
        for(int i=0; i < uidList.size(); i++) {
            ids[i] = (String)uidList.elementAt(i);
        }
        Message m = new Message();
        try {
            container.assignUniqueEntityId(m.getMessageEntity());
        } catch(ContainerException e) {}
        String m_id = m.getMessageId();
        //String selectedSB = operator.selectSB(ids, m_id);
        String selectedSB = getSB(ids, m_id);
        logger.log(Level.INFO, "in MS. selectedSB = "+selectedSB);
        messageQueue.removeMessage(m_id);
        logger.log(Level.INFO, "Message "+m_id+" removed from queue.");

        if(selectedSB != null) {
            dispatchSB(selectedSB);
        } else {
            logger.log(Level.INFO, "selectedSB was null. Nothing sent to control.");
        }
        //this will go whereever the control event comes back saying sb is done
        //startPipeline(selectedSB);
    }
    
    public void stop() {
        schedulerState = State.STOPPED;
        System.out.println("Scheduler is stopped");
    }

    /**
     *  Gets a single scheduling block out of the MasterSBQueue
     *  @param ids An array of strings which contain all the SB uids
     *  @param m_id A string of the uid of the Message
     *  @return String The uid of the selected sb
     */
    public String getSB(String[] ids, String m_id) {

        String sb = operator.selectSB(ids, m_id);
        return sb;
    }
    public SchedBlock getSB() {
        SchedBlock sb = queue.getSB();
        return sb;
    }

    public void dispatchSB(String id) {
        //no STime available yet so ignoring.
        dispatcher.sendToControl(id, new STime() );
    }

    /*
    public void startPipeline(String id) {
        projectManager.startPipeline(id);
    }
    */
    
	public static void main(String[] args) {
	}
}
