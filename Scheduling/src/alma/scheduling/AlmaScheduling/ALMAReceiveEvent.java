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
 * File ALMAReceiveEvent.java
 */
package alma.scheduling.AlmaScheduling;

import java.util.Vector;

import alma.xmlentity.XmlEntityStruct;
import alma.entities.commonentity.EntityT;
import alma.entities.commonentity.EntityRefT;
import alma.entity.xmlbinding.projectstatus.*;

import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;
import alma.acs.util.UTCUtility;
import alma.acs.util.UTCUtility;

import alma.Control.ExecBlockEvent;
import alma.Control.ControlSystemStatusEvent;
import alma.TelCalPublisher.FocusReducedEvent;
import alma.TelCalPublisher.PointingReducedEvent;
import alma.pipelinescience.ScienceProcessingDoneEvent;

import alma.scheduling.StartSessionEvent;
import alma.scheduling.EndSessionEvent;
import alma.scheduling.Event.Receivers.*;
import alma.scheduling.Define.SB;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.Status;
import alma.scheduling.Define.ExecBlock;
import alma.scheduling.Define.ControlEvent;
import alma.scheduling.Define.SciPipelineRequest;
import alma.scheduling.Define.SchedulingException;

/**
 * This Class receives the events sent out by other alma subsystems. 
 * @author Sohaila Lucero
 * @version $Id: ALMAReceiveEvent.java,v 1.24 2005/03/30 18:39:58 sslucero Exp $
 */
public class ALMAReceiveEvent extends ReceiveEvent {
    // container services
    private ContainerServices containerServices;
    // the alma project manager
    private ALMAProjectManager manager;
    //publish event class
    private ALMAPublishEvent publisher;
    //a list of the ExecBlock that are currently started but not finished.
    private Vector currentEB; 

    /**
      *
      */
    public ALMAReceiveEvent(ContainerServices cs, 
                            ALMAProjectManager m,
                            ALMAPublishEvent pub) {
        
        this.containerServices = cs;    
        this.logger = cs.getLogger();
        this.manager = m;
        this.publisher = pub;
        this.currentEB = new Vector();
    }

    ///////////////////////////////////////////////////////////////////////////
    //  Receive functions for each event type
    ///////////////////////////////////////////////////////////////////////////
    /**
     * When an ExecBlockEvent is received by the scheduling subsystem it is
     * received here and the type is determined. Once the type is determined 
     * the appropriate action is taken.
     * 
     *
     * @param ExecBlockEvent The event sent out by the control subsystem.
     */ 
    public void receive(ExecBlockEvent e) {
        //the processes below are still being thought out and may not be the
        //best way to do what needs to be done.
        try {
        logger.info("SCHEDULING: Starting to process the control event");
        ExecBlock eb = null;
        switch(e.type.value()) {
            case 0:
                logger.info("SCHEDULING: Event reason = started");
                logger.info("SCHEDULING: Received sb start event from control.");
                //create an execblock internal to scheduling. 
                eb = createExecBlock(e);
                DateTime startEb = new DateTime(UTCUtility.utcOmgToJava(e.startTime));
                eb.setStartTime(startEb);
                eb.setTimeOfCreation(startEb);
                eb.setTimeOfUpdate(startEb);
                currentEB.add(eb);
                //send out a start session event.
                startSession(eb);
                break;
            case 1:
                logger.info("SCHEDULING: Event reason = end");
                logger.info("SCHEDULING: Received sb end event from control.");
                //create a control event internal to scheduling.
                //this object contains the info from controls event which sched wants
                ControlEvent ce = new ControlEvent(e.execID, e.sbId, e.saId, 
                    e.type.value(), e.status.value(), new DateTime(
                        UTCUtility.utcOmgToJava(e.startTime)));
                //update the sb with the new info from the event
                updateSB(ce);
                //eb = createExecBlock(e);
                eb = retrieveExecBlock(e.execID);
                DateTime endEb = new DateTime(UTCUtility.utcOmgToJava(e.startTime));
                eb.setEndTime(endEb, Status.COMPLETE);
                eb.setTimeOfUpdate(endEb);
                //send out an end session event
                endSession(eb);
                sbCompleted(eb);
                startPipeline(ce);
                deleteFinishedEB(eb);
                break;
            default: 
                logger.severe("SCHEDULING: Event reason = error");
                break;
        }
        } catch(Exception ex) {
            logger.severe("SCHEDULING: Error receiving and processing ExecBlockEvent.");
            ex.printStackTrace();
        }

    }
    
    /**
     * Event sent by the control system indication what the status of the control
     * system is.
     * @param ControlSystemStatusEvent
     */
    public void receive(ControlSystemStatusEvent e) {
        logger.info("SCHEDULING: Received Control System's status event.");
    }

    /**
     * When the scheduling subsystem receives the ScienceProcessingRequestEnd event
     * from the pipeline subsystem it is received here and processed accordingly
     *
     * @param ScienceProcessingRequestEnd The event sent by the Pipeline subsystem
     */
    public void receive(ScienceProcessingDoneEvent e) {
        logger.info("SCHEDULING: Starting to process the pipeline event");
    }

    /**
     * When the scheduling subsystem receives the FocusReducedEvent event
     * from the telcal subsystem it is received here and processed accordingly.
     *
     * @param FocusReducedEvent The event sent by the Telcal subsystem
     */
    public void receive(FocusReducedEvent e) {
        logger.info("SCHEDULING: Starting to process the focus reduced event");
    }

    /**
     * When the scheduling subsystem receives the PointingReducedEvent event
     * from the telcal subsystem it is received here and processed accordingly.
     *
     * @param PointingReducedEvent The event sent by the Telcal subsystem
     */
    public void receive(PointingReducedEvent e) {
        logger.info("SCHEDULING: Starting to process the pointing reduced event");
    }

    ///////////////////////////////////////////////////////////////////////////
    //  Util functions 
    ///////////////////////////////////////////////////////////////////////////

    /**
      * @param ExecBlockEvent
      * @return ExecBlock Internal obj to scheduling, contains the info we want from control
      */
    private ExecBlock createExecBlock(ExecBlockEvent event) {
        // System.out.println("EXECBLOCK in event id = "+event.execID);
        ExecBlock eb = new ExecBlock(event.execID, event.saId);
        // do this to get SB id over to PM, will be replaced with proper SB
        eb.setParent(new SB(event.sbId));
        return eb;
    }
    
    /**
     * Updates the scheduling block with the info gotten from the control
     * event. If the SB is complete
     * @param ControlEvent
     */
    private void updateSB(ControlEvent e) {
        logger.info("SCHEDULING: updating the SB after event from control received");
        manager.updateSB(e);
    }


    /**
     * Creates a Session object for the start of this SB execution session. 
     * Stores the session object in the archive and sends out a start session
     * event.
     * @param ExecBlock The ExecBlock which tells us that the SB has started
     *                       its execution.
     */
    private void startSession(ExecBlock eb) {
        try {
            logger.info("SCHEDULING: Start of a Session!");
            manager.sendStartSessionEvent(eb);
        }catch(Exception ex) {
            logger.severe("SCHEDULING: error! ");
            ex.printStackTrace();
        }
    }

    /**
     * Updates an existing session object in the archive to say that the SB has 
     * finished its execution. Then sends out an event saying that the session 
     * has ended.
     * @param ExecBlock 
     */
    private void endSession(ExecBlock eb) {
        try {
            logger.info("SCHEDULING: End of a Session! ");
            manager.sendEndSessionEvent(eb);
        } catch(Exception e) {
            logger.severe("SCHEDULING: error! ");
            e.printStackTrace();
        }
    }


    /** 
     * Starts the Science Pipeline given the SB which completed with this control
     * event.
     * @param ControlEvent The event from control
     */
    private void startPipeline(ControlEvent e) {
        String result = null;
        SciPipelineRequest ppr=null;
        try {
            ppr = manager.createSciPipelineRequest(e.getSBId(), "Empty Comment");
            manager.startPipeline(ppr);
        } catch(Exception ex) {
            logger.severe("SCHEDULING: error starting the science pipeline");
            ex.printStackTrace();
        }
    }

    /**
      * @param ExecBlock
      */
    private void sbCompleted(ExecBlock eb){
        logger.info("SCHEDULING: setting SB to complete.");
        manager.setSBComplete(eb);
    }
    

    /**
      * Retrieves the exec block with the given id from the exec block queue.
      *
      * @param ebId The exec block's id
      * @return ExecBlock The exec block with the given id. Returns null if not in the queue.
      */
    private ExecBlock retrieveExecBlock(String ebId) {
        ExecBlock eb = null;
        for(int i=0; i < currentEB.size(); i++) {
            if( ((ExecBlock)currentEB.elementAt(i)).getId().equals(ebId) ){
                eb = (ExecBlock)currentEB.elementAt(i);
                break;
            }
        }
        return eb;
    }

    /**
      * Deletes the given exec block from the list of current ebs.
      * @param eb The exec block to delete.
      */
    private void deleteFinishedEB(ExecBlock eb) {
        for(int i=0; i < currentEB.size(); i++) {
            ExecBlock tmpEB = (ExecBlock)currentEB.elementAt(i);
            if( tmpEB.getId().equals(eb.getId()) ){
                currentEB.removeElementAt(i);
                break;
            }
        }
    }
}
