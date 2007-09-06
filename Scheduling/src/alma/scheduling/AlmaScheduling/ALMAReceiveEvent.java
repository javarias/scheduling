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
import java.util.logging.Level;

import alma.xmlentity.XmlEntityStruct;
import alma.entities.commonentity.EntityT;
import alma.entities.commonentity.EntityRefT;
import alma.entity.xmlbinding.projectstatus.*;

import alma.acs.container.ContainerServices;
import alma.acs.util.UTCUtility;
//import alma.acs.util.UTCUtility;

import alma.Control.ExecBlockStartedEvent;
import alma.Control.ExecBlockEndedEvent;
//import alma.Control.ControlSystemStatusEvent;
import alma.TelCalPublisher.*;
import alma.pipelinescience.ScienceProcessingDoneEvent;
import alma.offline.ASDMArchivedEvent;

import alma.scheduling.StartSessionEvent;
import alma.scheduling.EndSessionEvent;
import alma.scheduling.Event.Receivers.*;
import alma.scheduling.Define.SB;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.ArrayTime;
import alma.scheduling.Define.Status;
import alma.scheduling.Define.ExecBlock;
import alma.scheduling.Define.ControlEvent;
import alma.scheduling.Define.SciPipelineRequest;
import alma.scheduling.Define.SchedulingException;

import alma.log_audience.OPERATOR;
import alma.acs.logging.AcsLogger;
import alma.acs.logging.domainspecific.ArrayContextLogger;
import alma.alarmsystem.source.ACSAlarmSystemInterfaceFactory;
import alma.alarmsystem.source.ACSAlarmSystemInterface;
import alma.alarmsystem.source.ACSFaultState;
import java.sql.Timestamp;


/**
 * This Class receives the events sent out by other alma subsystems. 
 * @author Sohaila Lucero
 * @version $Id: ALMAReceiveEvent.java,v 1.50 2007/09/06 17:59:03 sslucero Exp $
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
    
    private ALMAClock clock;
    private ArrayContextLogger arraylogger;

    
    /**
      *
      */
    public ALMAReceiveEvent(ContainerServices cs, 
                            ALMAProjectManager m,
                            ALMAPublishEvent pub) {
        
        this.containerServices = cs;    
        this.logger = cs.getLogger();
        this.arraylogger = new ArrayContextLogger((AcsLogger)logger);
        this.manager = m;
        this.publisher = pub;
        this.currentEB = new Vector();
        this.clock = new ALMAClock();
    }
    
    public void sendAlarm(String ff, String fm, int fc, String fs) {
        try {
            ACSAlarmSystemInterface alarmSource = ACSAlarmSystemInterfaceFactory.createSource("ALMASBAbort");
            ACSFaultState state = ACSAlarmSystemInterfaceFactory.createFaultState(ff, fm, fc);
            state.setDescriptor(fs);
            state.setUserTimestamp(new Timestamp(clock.getDateTime().getMillisec()));
            alarmSource.push(state);
        } catch(Exception e) {
            e.printStackTrace();
        }    
    }

    
///////////////////////////////////////////////////////////////////////////
//  Receive functions for each event type
///////////////////////////////////////////////////////////////////////////

////////////////////////
// Control
////////////////////////
    /**
     * When an ExecBlockStartedEvent is received by the scheduling subsystem it is
     * received here and the type is determined. Once the type is determined 
     * the appropriate action is taken.
     * 
     *
     * @param ExecBlockStartedEvent The event sent out by the control subsystem.
     */ 
    public void receive(ExecBlockStartedEvent e) {
        ProcessExecBlockStartedEvent p = new ProcessExecBlockStartedEvent(e);
        Thread t = new Thread(p);
        t.start();
    }

    public void receive(ExecBlockEndedEvent e) {
        ProcessExecBlockEndedEvent p = new ProcessExecBlockEndedEvent(e);
        if(e.status.toString()!="SUCCESS")
        	sendAlarm("Scheduling","SchedSBAbortedConnAlarm",1,ACSFaultState.ACTIVE);
        	
        Thread t = new Thread(p);
        t.start();
    }


////////////////////////
// DataCapture
////////////////////////

    public void receive(ASDMArchivedEvent e ) {
        ProcessASDMArchivedEvent p = new ProcessASDMArchivedEvent(e);
        Thread t = new Thread(p);
        t.start();
    }

////////////////////////
// SciPipeline
////////////////////////
    /**
     * When the scheduling subsystem receives the ScienceProcessingRequestEnd event
     * from the pipeline subsystem it is received here and processed accordingly
     *
     * @param ScienceProcessingRequestEnd The event sent by the Pipeline subsystem
     */
    public void receive(ScienceProcessingDoneEvent e) {
        logger.fine("SCHEDULING: Starting to process the pipeline event");
        logger.fine("SCHEDULING: Nothing to do yet.");
    }
////////////////////////
// TELCAL
////////////////////////
    public void receive(AmpliCalReducedEvent e) {
        logger.fine("SCHEDULING: Starting to process the AmpliCalReducedEvent");
        logger.fine("SCHEDULING: Nothing to do yet.");
    }
    
    public void receive(AmpCurveReducedEvent e) {
        logger.fine("SCHEDULING: Starting to process the AmpCurveReducedEvent");
        logger.fine("SCHEDULING: Nothing to do yet.");
    }
    
    public void receive(AntennaPositionsReducedEvent e) {
        logger.fine("SCHEDULING: Starting to process the AntennaPositionsReducedEvent");
        logger.fine("SCHEDULING: Nothing to do yet.");
    }
    
    public void receive(AtmosphereReducedEvent e) {
        logger.fine("SCHEDULING: Starting to process the AtmosphereReducedEvent");
        logger.fine("SCHEDULING: Nothing to do yet.");
    }
    
    public void receive(DelayReducedEvent e) {
        logger.fine("SCHEDULING: Starting to process the DelayReducedEvent");
        logger.fine("SCHEDULING: Nothing to do yet.");
    }

    /**
     * When the scheduling subsystem receives the FocusReducedEvent event
     * from the telcal subsystem it is received here and processed accordingly.
     *
     * @param FocusReducedEvent The event sent by the Telcal subsystem
     */
    public void receive(FocusReducedEvent e) {
        logger.fine("SCHEDULING: Starting to process the focus reduced event");
        logger.fine("SCHEDULING: Nothing to do yet.");
    }
    
    public void receive(PhaseCalReducedEvent e) {
        logger.fine("SCHEDULING: Starting to process the PhaseCalReducedEvent");
        logger.fine("SCHEDULING: Nothing to do yet.");
    }
    
    public void receive(PhaseCurveReducedEvent e) {
        logger.fine("SCHEDULING: Starting to process the PhaseCurveReducedEvent");
        logger.fine("SCHEDULING: Nothing to do yet.");
    }

    /**
     * When the scheduling subsystem receives the PointingReducedEvent event
     * from the telcal subsystem it is received here and processed accordingly.
     *
     * @param PointingReducedEvent The event sent by the Telcal subsystem
     */
    public void receive(PointingReducedEvent e) {
        logger.fine("SCHEDULING: Starting to process the pointing reduced event");
        logger.fine("SCHEDULING: Nothing to do yet.");
    }
    
    public void receive(PointingModelReducedEvent e) {
        logger.fine("SCHEDULING: Starting to process the PointingModelReducedEvent");
        logger.fine("SCHEDULING: Nothing to do yet.");
    }
    
    public void receive(SkydipReducedEvent e) {
        logger.fine("SCHEDULING: Starting to process the SkydipReducedEvent");
        logger.fine("SCHEDULING: Nothing to do yet.");
    }

///////////////////////////////////////////////////////////////////////////
//  Util functions 
///////////////////////////////////////////////////////////////////////////

    /**
      * @param ExecBlockEvent
      * @return ExecBlock Internal obj to scheduling, contains the info we want from control
      */
    private ExecBlock createExecBlock(ExecBlockStartedEvent event) {
        // System.out.println("EXECBLOCK in event id = "+event.execID);
        ExecBlock eb = new ExecBlock(event.execId.entityId, event.arrayName);
        // do this to get SB id over to PM, will be replaced with proper SB
        eb.setParent(new SB(event.sbId.entityId));
        return eb;
    }
    
    /**
     * Updates the scheduling block with the info gotten from the control
     * event. If the SB is complete
     * @param ControlEvent
     */
    private void updateSB(ControlEvent e) {
        logger.fine("SCHEDULING: updating the SB after event from control received");
        manager.updateSB(e);
    }


    /**
     * Creates an ObservingSession object for the start of this SB execution session. 
     * Stores the session object in the archive and sends out a start session
     * event.
     * @param ExecBlock The ExecBlock which tells us that the SB has started
     *                       its execution.
     */
    private void createObservedSession(ExecBlock eb) {
        try {
            logger.fine("SCHEDULING: Start of a Session!");
            manager.createObservedSession(eb);
        }catch(Exception ex) {
            logger.severe("SCHEDULING: error! ");
            ex.printStackTrace(System.out);
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
            logger.fine("SCHEDULING: End of a Session! ");
            manager.sendEndSessionEvent(eb);
        } catch(Exception e) {
            logger.severe("SCHEDULING: error! ");
            e.printStackTrace(System.out);
        }
    }


    /** 
     * Starts the Science Pipeline given the SB which completed with this control
     * event.
     * @param ControlEvent The event from control
     */
    private void startPipeline(String sbid) {
        //check if we want to start the pipeline.
        boolean startPipeline = manager.isPipelineNeeded(sbid);
        if(startPipeline) { //returned true and we want to start the pipeline

            //check if SB's OUS is complete, if it is start the pipeline
            if(manager.isObsUnitSetComplete(sbid)) {
                logger.fine("SCHEDULING: ObsUnitSet/Parent of SB ("+sbid+") is complete, starting SciPipeline");
                String result = null;
                SciPipelineRequest ppr=null;
                try {
                    ppr = manager.createSciPipelineRequest(sbid, "Empty Comment");
                    manager.startPipeline(ppr);
                } catch(Exception ex) {
                    logger.severe("SCHEDULING: error starting the science pipeline");
                    ex.printStackTrace(System.out);
                }
            } else {
                logger.fine("SCHEDULING: ObsUnitSet/Parent of SB ("+sbid+") is not complete, not starting SciPipeline at this time");
            }
        } //else { //returned false so not starting the pipeline.. ie do nothing..
        //}
    }

    /**
      * @param ExecBlock
      */
    private void sbCompleted(ExecBlock eb){
        logger.fine("SCHEDULING: setting SB to complete.");
        manager.setSBComplete(eb);
    }
    

    /**
      * Retrieves the exec block with the given id from the exec block queue.
      *
      * @param ebId The exec block's id
      * @return ExecBlock The exec block with the given id. Returns null if not in the queue.
      */
    private ExecBlock retrieveExecBlock(String ebId) {
        logger.fine("SCHEDULING: Retrieving EB "+ebId+" from list");
        ExecBlock eb = null;
        for(int i=0; i < currentEB.size(); i++) {
            if( ((ExecBlock)currentEB.elementAt(i)).getId().equals(ebId) ){
                eb = (ExecBlock)currentEB.elementAt(i);
                logger.fine("SCHEDULING: Found eb "+eb.getExecId()+" in list");
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
///////////////////////////////////////////////////////////////////////////
/////////////////nested classes for event processing threads///////////////
///////////////////////////////////////////////////////////////////////////
    public class ProcessExecBlockStartedEvent implements Runnable{
        private ExecBlockStartedEvent event;
        
        public ProcessExecBlockStartedEvent(ExecBlockStartedEvent e) {
            event = e;
        }
        public void run(){
            processExecBlockStartedEvent(event);
        }
    }
    private void processExecBlockStartedEvent(ExecBlockStartedEvent e) {
        //the processes below are still being thought out and may not be the
        //best way to do what needs to be done.
        try {
            logger.fine("SCHEDULING: Received exec block start event from control.");
            logger.fine("SCHEDULING: ExecBlock (ASDM) = "+  e.execId.entityId+" for SB ="+e.sbId.entityId );
            logger.fine("SCHEDULING: session id ="+ e.sessionId.entityId);
            DateTime startEb = new DateTime(UTCUtility.utcOmgToJava(e.startTime));
            logger.fine("SCHEDULING: SB ("+e.sbId.entityId+") started at "+startEb.toString()+" on "+e.arrayName);
            //create an execblock internal to scheduling. 
            ExecBlock eb = createExecBlock(e);
            //ArrayTime at = new ArrayTime(e.startTime);
            //DateTime startEb = at.arrayTimeToDateTime();
            logger.fine("********************************");
            logger.fine("SCHEDULING: Recording start time for: "+e.execId.entityId);
            logger.fine("SCHEDULING: start time is : "+startEb.toString());
            logger.fine("********************************");
            eb.setStartTime(startEb);
            eb.setTimeOfCreation(startEb);
            eb.setTimeOfUpdate(startEb);
            //eb.setSessionId(e.sessionId);
            eb.setSessionId(e.sessionId.partId);
            logger.fine("SCHEDULING: Adding current EB "+eb.getExecId()+" to current list");
            currentEB.add(eb);
            createObservedSession(eb);
            //manager.createProjectWebpage(projectUid);
            //send out a start session event.
            //startSession(eb);
        } catch(Exception ex) {
            logger.severe("SCHEDULING: Error receiving and processing ExecBlockStartedEvent.");
            ex.printStackTrace(System.out);
        }
    }


    public class ProcessExecBlockEndedEvent implements Runnable{
        private ExecBlockEndedEvent event;
        
        public ProcessExecBlockEndedEvent(ExecBlockEndedEvent e) {
            event = e;
        }
        public void run(){
            processExecBlockEndedEvent(event);
        }
    }
    private void processExecBlockEndedEvent(ExecBlockEndedEvent e) {
        try{
            logger.fine("SCHEDULING: Event reason = end");
            logger.fine("SCHEDULING: Received sb end event from control.");
            DateTime endEb = new DateTime(UTCUtility.utcOmgToJava(e.endTime));
            logger.fine("SCHEDULING: end time is "+ endEb.toString());
            //create a control event internal to scheduling.
            //this object contains the info from controls event which sched wants
            //ArrayTime at = new ArrayTime(e.endTime);
            ControlEvent ce = new ControlEvent(e.execId.entityId, e.sbId.entityId, e.arrayName, e.status.value(), endEb);//at.arrayTimeToDateTime());
            //update the sb with the new info from the event
            //took out for now coz it isn't implemented.. probably won't need it back in
            //coz sb is updated
            //updateSB(ce);
            //eb = createExecBlock(e);
            ExecBlock eb = retrieveExecBlock(e.execId.entityId);
            //DateTime endEb = at.arrayTimeToDateTime();
            logger.fine("********************************");
            logger.fine("SCHEDULING: Setting end time for: "+e.execId.entityId);
            logger.fine("SCHEDULING: end time is "+ endEb.toString());
            logger.fine("********************************");
            logger.fine("SCHEDULING: SB ("+e.sbId.entityId+") ended at "+endEb.toString()+" with ASDM/ExecBlock = "+e.execId.entityId);
            //TODO change this when we start getting exec block end events for all reasons
            if(eb == null){
                logger.severe("SCHEDULNG: EB in sched's list was null. Implies Sched did not receive EB start event. Setting SB as aborted");
                //get sb and set it to not running
                SB sb = manager.getSBQueue().get(e.sbId.entityId);
                sb.setAborted(endEb);
                return;
            }
            if(e.status == alma.Control.Completion.SUCCESS) {
                eb.setEndTime(endEb, Status.COMPLETE);
                arraylogger.log(Level.INFO, 
                        "SCHEDULING: SB("+eb.getParent().getId()+") completed successfuly on array "+
                        eb.getArrayName(), OPERATOR.value, eb.getArrayName());
            } else {
                eb.setEndTime(endEb, Status.ABORTED);// need to make a failed thing in Status
                arraylogger.log(Level.WARNING, 
                        "SCHEDULING: SB("+eb.getParent().getId()+") was aborted on array "+
                        eb.getArrayName(), OPERATOR.value, eb.getArrayName());
                //NOTE: SB Aborted Alarm sent prior to this method being run
            }
            eb.setTimeOfUpdate(endEb);
            //send out an end session event
            endSession(eb);
            sbCompleted(eb);
            //startPipeline(ce);
            deleteFinishedEB(eb);
        } catch(Exception ex) {
            logger.severe("SCHEDULING: Error receiving and processing ExecBlockEndedEvent.");
            ex.printStackTrace(System.out);
        }
    }

    class ProcessASDMArchivedEvent implements Runnable {
        private ASDMArchivedEvent event;

        public ProcessASDMArchivedEvent(ASDMArchivedEvent e){
            event =e;
        }
        public void run() {
            startPipeline(event.workingDCId.schedBlock.entityId);
        }
    }
}
