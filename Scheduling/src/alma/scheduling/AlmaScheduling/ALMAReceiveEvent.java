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

import alma.xmlentity.XmlEntityStruct;
import alma.entities.commonentity.EntityT;
//import alma.entity.xmlbinding.session.*;

import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;

import alma.Control.ExecBlockEvent;
import alma.Control.ControlSystemStatusEvent;
import alma.TelCalPublisher.FocusReducedEvent;
import alma.TelCalPublisher.PointingReducedEvent;
import alma.pipelinescience.ScienceProcessingRequestEnd;

import alma.scheduling.StartSession;
import alma.scheduling.EndSession;
import alma.scheduling.Event.Receivers.*;
//import alma.scheduling.Define.ArrayTime;
import alma.scheduling.Define.Session;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.ControlEvent;
import alma.scheduling.Define.SchedulingException;

/**
 * This Class receives the events sent out by other alma subsystems. 
 * @author Sohaila Lucero
 */
public class ALMAReceiveEvent extends ReceiveEvent {
    private ContainerServices containerServices;
    private ALMAProjectManager manager;
    private ALMAArchive archive;
    private ALMAPipeline pipeline;
    private ALMAPublishEvent publisher;

    public ALMAReceiveEvent(ContainerServices cs, ALMAProjectManager m, ALMAArchive a, 
        ALMAPipeline p, ALMAPublishEvent pub) {
        
        this.containerServices = cs;    
        this.logger = cs.getLogger();
        this.manager = m;
        this.archive = a;
        this.pipeline = p;
        this.publisher = pub;
    }

    ///////////////////////////////////////////////////////////////////////////
    //  Receive functions for each event type
    ///////////////////////////////////////////////////////////////////////////
    /**
     * When an ExecBlockEvent is received by the scheduling subsystem it is
     * received here and the type is determined. Once the type is determined 
     * the appropriate action is taken.
     *
     * @param ExecBlockEvent The event sent out by the control subsystem.
     */ 
    public void receive(ExecBlockEvent e) {
        logger.info("SCHEDULING: Starting to process the control event");
        switch(e.type.value()) {
            case 0:
                logger.info("SCHEDULING: Event reason = started");
                logger.info("SCHEDULING: Received sb start event from control.");
                startSession(e);
                break;
            case 1:
                logger.info("SCHEDULING: Event reason = end");
                logger.info("SCHEDULING: Received sb end event from control.");
                //ArrayTime time = new ArrayTime(e.startTime);
                ControlEvent ce = new ControlEvent(e.execID, e.sbId, e.saId, 
                    e.type.value(), e.status.value(), new DateTime(e.startTime));
                endSession(e);
                updateSB(ce);
                startPipeline(ce);
                break;
            default: 
                logger.severe("SCHEDULING: Event reason = error");
                break;
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
    public void receive(ScienceProcessingRequestEnd e) {
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
     * Updates the scheduling block with the info gotten from the control
     * event. If the SB is complete
     */
    private void updateSB(ControlEvent e) {
        try {
            logger.info("SCHEDULING: updating the SB after event from control received");
            archive.updateSB(e);
        } catch(SchedulingException ex) {
            logger.severe("SCHEDULING: error updating sb");
            ex.printStackTrace();
        }
    }


    /**
     * Creates a Session object for the start of this SB execution session. 
     * Stores the session object in the archive and sends out a start session
     * event.
     * @param ExecBlockEvent The event which tells us that the SB has started
     *                       its execution.
     */
    private void startSession(ExecBlockEvent e) {
      try {
        logger.info("SCHEDULING: Start of a Session!");
        //create a session object 
        ALMASession s = new ALMASession();
        s.addExecBlockId(e.execID);
        //s.setStartTime(e.startTime);
        //ouc & sb == same thing right now!
        s.setObsUnitSetId(e.sbId);
        s.setSbId(e.sbId);
        //store session obj in the archive
        String sessionID = archive.storeSession(s);
        //send out the session start event
        StartSession start_event = new StartSession (e.startTime, 
            sessionID, e.sbId, e.sbId, e.execID); //NOTE: for now second last 2 are the same..
        publisher.publish(start_event);       
        manager.sessionStart(sessionID,e.sbId);
      }catch(Exception ex) {
        logger.severe("SCHEDULING: error! ");
        ex.printStackTrace();
      }
    }
    
    /**
     * Updates an existing session object in the archive to say that the SB has 
     * finished its execution. Then sends out an event saying that the session 
     * has ended.
     * @param ExecBlockEvent The event to tell us that the event has ended.
     */
    private void endSession(ExecBlockEvent e) {
        //query session object from the archive
        Session s = archive.querySession(e.sbId);
        if(s != null) {
            logger.info("non-null session! ");
        }
    //update session object and update it in the archive
    //archive.updateSession
    //send out the session end event
        manager.sessionEnd(e.sbId);
    }


    /** 
     * Starts the Science Pipeline given the SB which completed with this control
     * event.
     * @param ControlEvent The event from control
     */
    private void startPipeline(ControlEvent e) {
        String result = null;
        try {
            XmlEntityStruct ppr = pipeline.createPipelineProcessingRequest(e.getSBId());
            archive.storePipelineProcessingRequest(new ALMAPipelineProcessingRequest(ppr));
            ppr = archive.retrievePPR(ppr.entityId);
            result = pipeline.processRequest(ppr);
        } catch(Exception ex) {
            logger.severe("SCHEDULING: error starting the science pipeline");
            ex.printStackTrace();
        }
    }
}
