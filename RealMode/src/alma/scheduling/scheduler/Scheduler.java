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
 * File Scheduler.java
 * 
 */
package alma.scheduling.scheduler;

import java.util.Vector;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.lang.Thread;
import java.lang.InterruptedException;

import alma.scheduling.NothingCanBeScheduledEnum;
import alma.scheduling.define.STime;
import alma.scheduling.master_scheduler.*;
import alma.scheduling.project_manager.PIProxy;
import alma.scheduling.receivers.SchedulerEventReceiver;
//import alma.scheduling.

import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;

import alma.entity.xmlbinding.schedblock.SchedBlock;

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
    /** The state of the scheduler */
    private State schedulerState;
    /** The mode of the scheduler.  */
    private String mode;
    /** Selected number of SBs from masterSbQueue */
    private SBSubQueue queue;
    private ContainerServices container;
    private ALMATelescopeOperator operator;
    private ALMADispatcher dispatcher;
    private Logger logger;
    private MessageQueue messageQueue;
    /** The task control object for the scheduler */
    private SchedulerTaskControl schedulerTaskControl;
    private ALMAClock clock;
    private PIProxy piproxy;
    private SchedulingPublisher s_publisher;
    private GUIController controller;
    
    private SchedulerEventReceiver schedEventReceiver; //rename this oneday
    
    public Scheduler (SchedulerConfiguration config) {
    }
    
    public Scheduler(boolean isSimulation, 
                      ContainerServices c, 
                        ALMATelescopeOperator o, 
                         ALMADispatcher d,
                          SBSubQueue q, 
                           MessageQueue mq, 
                            ALMAClock cl,
                             PIProxy pip,
                              String m,
                               SchedulingPublisher sp) {
        this.schedulerState = new State(State.NEW);
        this.isSimulation = isSimulation;
        this.container = c;
        this.operator = o;
        this.dispatcher = d;
        this.queue = q;
        this.messageQueue = mq;
        this.clock = cl;
        this.piproxy = pip;
        this.mode = m;
        this.s_publisher = sp;

        logger = container.getLogger();
        
        initialize();
    }

    public void initialize() {
        schedulerState = State.INITIALIZED;
        for(int i=0; i < queue.size(); i++){
        //    schedulerTaskControl.incrementSbsNotStarted();
        }
    }

    public void startSchedEventReceiver() {
        try {
            schedEventReceiver = new SchedulerEventReceiver(container, 
                                    schedulerTaskControl, this);
            schedEventReceiver.addSubscription(alma.Control.EXECEVENTS.class);
            schedEventReceiver.consumerReady();
        } catch(Exception e) {
        }
    }
    

    public void run() {
        schedulerState = State.EXECUTING;
        logger.info("SCHEDULING: Scheduler is running in "+mode+" mode!");
        String[] ids = queue.getIds();
        if(mode.equals("interactive")) {
            startInteractiveSession();
            return;
        }
        boolean moreSBs = true;
        while(moreSBs) { // will need to change ethis when there is more than 
                         // one project being executed.
            Message m = new Message();
            try {
                container.assignUniqueEntityId(m.getMessageEntity());
            } catch(ContainerException e) {}
            String m_id = m.getMessageId();
            String selectedSB = getSB(ids, m_id);
            if(selectedSB == null) {
            //all sbs have been processed!
            //should send out nothingCanBeScheduled but
            //for now this is the end of the project and we start the pipeline
                logger.info("SCHEDULING: No more SBs to process!");
                //s_publisher.publishEvent();
                s_publisher.publishEvent(NothingCanBeScheduledEnum.OTHER,
                    "No more SBs to process");
                moreSBs = false;
                break;
            }
            logger.info("SCHEDULING: in scheduler. selectedSB = "+selectedSB);
            messageQueue.removeMessage(m_id);
            logger.info("SCHEDULING: Message "+m_id+" removed from queue.");

            if(selectedSB != null) {
                dispatchSB(selectedSB);
            } else {
                logger.info("SCHEDULING: selectedSB was null. Nothing sent to control.");
            }
            try {
                logger.info("SCHEDULING: waiting til sb is done processing");
                schedulerTaskControl.getTask().sleep(24*60*60*1000L);
            } catch(InterruptedException e) {
                logger.info("SCHEDULING: scheduler woken up!");
            }
        }
        stop();
        
    }
    
    public void stop() {
        logger.info("SCHEDULING: About to stop scheduler.");
        schedulerTaskControl.stopTask();
        schedulerTaskControl.interruptMasterScheduler();
        schedulerState = State.STOPPED;
        logger.info("SCHEDULING: Scheduler is stopped");
    }

    public void dispatchSB(String id) {
        //no STime available yet so ignoring.
        dispatcher.sendToControl(id, new STime() );
    }

    public boolean isInQueue(String id) {
        System.out.println("SCHEDULING: in scheduler, id="+id);
        return queue.isInSubQueue(id);
    }

    ////////////////////////////////////////////////////////
    // Functions used for interactive scheduling
    ////////////////////////////////////////////////////////
    private void startInteractiveSession() {
        controller = new GUIController(this);
        Thread t = new Thread(controller);
        t.start();
    }

    /*
    public String[] getSBs() {
        return queue.getIds();
    }
    */
    public SchedBlock[] getSBs() {
        return queue.getAllSBs();
    }

    public boolean isSBComplete(String sb_id) {
        return operator.isCompletedSb(sb_id);
    }

/*
    public alma.obsprep.bo.SchedBlock getSB(String uid) {
        SchedBlock sb = queue.getSchedBlock(uid);
        ObjectFactory of = ObjectFactory.getFactory();
        return of.getSchedBlock(sb);
    }
*/

    ///////////////////////////////////////////////////////

    /* GetMethods */
    /**
     *  Gets a single scheduling block out of the MasterSBQueue
     *  @param ids An array of strings which contain all the SB uids
     *  @param m_id A string of the uid of the Message
     *  @return String The uid of the selected sb
     InterruptedException*/
    public String getSB(String[] ids, String m_id) {

        String sb = operator.selectSB(ids, m_id);
        return sb;
    }
    /*
    public SchedBlock getSB() {
        SchedBlock sb = queue.getSchedBlock();
        return sb;
    }
    */
    public SchedulerTaskControl getSchedulerTaskControl() {
        return schedulerTaskControl;
    }
    public State getSchedulerState() {
        return schedulerState;
    }
    
    /* SetMethods */

    public void setSchedulerTaskControl(SchedulerTaskControl stc) {
        this.schedulerTaskControl = stc;
    }
    public void setSchedulerState(State s) {
        schedulerState = s;
    }
    ///////////////////////////////////////////////////////
    /*
    public void setPipeline(ALMAPipeline p) {
        this.pipeline = p;
    }
    */
    ///////////////////////////////////////////////////////
    public void removeSBfromQueue(String sb_id, String reason) {
        queue.removeFromQueue(sb_id, reason);                   
    }
    ///////////////////////////////////////////////////////

	public static void main(String[] args) {
	}
}
