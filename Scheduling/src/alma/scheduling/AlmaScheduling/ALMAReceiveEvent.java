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
import alma.pipelinescience.ScienceProcessingRequestEnd;

import alma.scheduling.StartSession;
import alma.scheduling.EndSession;
import alma.scheduling.Event.Receivers.*;
//import alma.scheduling.Define.ArrayTime;
import alma.scheduling.Define.SB;
//import alma.scheduling.Define.Session;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.Status;
import alma.scheduling.Define.ExecBlock;
import alma.scheduling.Define.ControlEvent;
import alma.scheduling.Define.SchedulingException;

/**
 * This Class receives the events sent out by other alma subsystems. 
 * @author Sohaila Lucero
 */
public class ALMAReceiveEvent extends ReceiveEvent {
    private ContainerServices containerServices;
    private ALMAProjectManager manager;
    private ALMAPipeline pipeline;
    private ALMAPublishEvent publisher;

    public ALMAReceiveEvent(ContainerServices cs, ALMAProjectManager m, ALMAArchive a, 
        ALMAPipeline p, ALMAPublishEvent pub) {
        
        this.containerServices = cs;    
        this.logger = cs.getLogger();
        this.manager = m;
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
        try {
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
                ControlEvent ce = new ControlEvent(e.execID, e.sbId, e.saId, 
                    e.type.value(), e.status.value(), new DateTime(
                        UTCUtility.utcOmgToJava(e.startTime)));
                endSession(e);
                updateSB(ce);
                ExecBlock eb = new ExecBlock(e.execID, e.saId);
                eb.setParent(new SB(e.sbId)); // do this to get SB id over to PM, will be replaced with proper SB
                eb.setStartTime(new DateTime(UTCUtility.utcOmgToJava(e.startTime)));
                eb.setEndTime(new DateTime(System.currentTimeMillis()),Status.COMPLETE);
                sbCompleted(eb);
                String[] ids = updateProjectStatus(eb);
                startPipeline(ce, ids);
                break;
            default: 
                logger.severe("SCHEDULING: Event reason = error");
                break;
        }
        } catch(Exception ex) {
            logger.severe("DAMMIT");
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
        //try {
            logger.info("SCHEDULING: updating the SB after event from control received");
            //archive.updateSB(e);
            manager.updateSB(e);
        //} catch(SchedulingException ex) {
        //    logger.severe("SCHEDULING: error updating sb");
        //    ex.printStackTrace();
        //}
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
        logger.info("SCHEDULING: Start of a Session! MUST BE FIXED");

        /*
        //create a session object 
        Session s = createSession(e);
        String sessionID = archive.storeSession(s);
        //send out the session start event
        StartSession start_event = new StartSession (e.startTime, 
            sessionID, "ous_partid", e.sbId, e.execID); 
        publisher.publish(start_event);       
        manager.sessionStart(sessionID,e.sbId);
       */
      }catch(Exception ex) {
        logger.severe("SCHEDULING: error! ");
        ex.printStackTrace();
      }
    }

    private SessionT createSession(ExecBlockEvent e) {
        /*
        Session s = new Session();
        SessionEntityT  s_entity = new SessionEntityT();
        s.setSessionEntity(s_entity);
        //s.setEndTime(""+e.startTime);
        EntityRefT execRef = new EntityRefT();
        execRef.setEntityId(e.execID);
        ExecutionT execution = new ExecutionT();
        execution.setExecBlockReference(execRef);
        SessionSequenceItem seq_item = new SessionSequenceItem();
        seq_item.setExecution(execution);
        SessionSequence seq = new SessionSequence();
        seq.setSessionSequenceItem(seq_item);
        SessionSequence[] seqArray = new SessionSequence[1];
        seqArray[0] = seq;
        s.setSessionSequence(seqArray);
        */
        return null;
    }
    
    /**
     * Updates an existing session object in the archive to say that the SB has 
     * finished its execution. Then sends out an event saying that the session 
     * has ended.
     * @param ExecBlockEvent The event to tell us that the event has ended.
     */
    private void endSession(ExecBlockEvent e) {
        //query session object from the archive
        logger.info("SCHEDULING: End of a Session! MUST BE FIXED");
        /*try {
            //Get Session from the archive.
            //ALMASession session = (ALMASession)archive.querySession(e.execID);
            //update session object and update it in the archive
            //alma.entity.xmlbinding.session.Session s = session.getSession();
            Session s = archive.querySession(e);
            updateSession(s, e);
            //archive.updateSession
            //send out the session end event
            EndSession end_event = new EndSession (
                                        e.startTime, 
                                        s.getSessionEntity().getEntityId(),
                                        "ous_partid", 
                                        e.execID);
            publisher.publish(end_event);
            manager.sessionEnd(e.sbId);
        } catch(Exception ex) {
            logger.severe("SCHEDULING: error in ALMAReceiveEvent:endSession");
            ex.printStackTrace();
        }
        */

    }

    private void updateSession(SessionT s, ExecBlockEvent e) {
        //s.setEndTime(""+e.startTime);
    }


    /** 
     * Starts the Science Pipeline given the SB which completed with this control
     * event.
     * @param ControlEvent The event from control
     */
    private void startPipeline(ControlEvent e, String[] ids) {
        String result = null;
        try {
            PipelineProcessingRequestT ppr = pipeline.createPipelineProcessingRequest(ids[0], ids[1]); //temporary for R2
            /*
            archive.storePipelineProcessingRequest(new ALMAPipelineProcessingRequest(ppr));
            ppr = archive.retrievePPR(ppr.entityId);
            result = pipeline.processRequest(ppr.xmlString);
            */
        } catch(Exception ex) {
            logger.severe("SCHEDULING: error starting the science pipeline");
            ex.printStackTrace();
        }
    }

    /**
      *
      */
    private void sbCompleted(ExecBlock eb){
        logger.info("SCHEDULING: setting SB to complete.");
        manager.setSBComplete(eb);
    }
    

    /**
      * Returns the ObsUnitSet part ID which is what the pipeline needs
      */
    private String[] updateProjectStatus(ExecBlock eb) {
        String[] id_info=null;
        logger.info("SCHEDULING: Updating the projectStatus");
        id_info = manager.updateProjectStatus(eb);
        return id_info;
    }
}
