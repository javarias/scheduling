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
 * @version $Id: ALMAReceiveEvent.java,v 1.15 2004/11/19 16:41:38 sroberts Exp $
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
        ExecBlock eb = null;
        switch(e.type.value()) {
            case 0:
                logger.info("SCHEDULING: Event reason = started");
                logger.info("SCHEDULING: Received sb start event from control.");
                eb = createExecBlock(e);
                eb.setStartTime(new DateTime(UTCUtility.utcOmgToJava(e.startTime)));
                startSession(eb);
                break;
            case 1:
                logger.info("SCHEDULING: Event reason = end");
                logger.info("SCHEDULING: Received sb end event from control.");
                ControlEvent ce = new ControlEvent(e.execID, e.sbId, e.saId, 
                    e.type.value(), e.status.value(), new DateTime(
                        UTCUtility.utcOmgToJava(e.startTime)));
                updateSB(ce);
                eb = createExecBlock(e);
                eb.setEndTime(new DateTime(UTCUtility.utcOmgToJava(e.startTime)),Status.COMPLETE);
                endSession(eb);
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

    private ExecBlock createExecBlock(ExecBlockEvent event) {
        ExecBlock eb = new ExecBlock(event.execID, event.saId);
        // do this to get SB id over to PM, will be replaced with proper SB
        eb.setParent(new SB(event.sbId));
        return eb;
    }
    
    /**
     * Updates the scheduling block with the info gotten from the control
     * event. If the SB is complete
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
