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
 * File ProjectManager.java
 * 
 */

package alma.scheduling.project_manager;

import java.util.Vector;
import java.util.logging.Logger;
import java.util.logging.Level;

import alma.entity.xmlbinding.obsproject.*;
import alma.entity.xmlbinding.pipelineprocessingrequest.*;
import alma.entity.xmlbinding.pipelineprocessingrequest.types.*;

import alma.entities.commonentity.EntityRefT;

import alma.scheduling.NothingCanBeScheduledEvent;
import alma.scheduling.master_scheduler.ALMAArchive;
import alma.scheduling.master_scheduler.ALMADispatcher;
import alma.scheduling.master_scheduler.MasterSBQueue;
import alma.scheduling.master_scheduler.SchedulingPublisher;

import alma.scheduling.receivers.*;

import alma.acs.nc.*;
import alma.acs.component.ComponentLifecycle;
import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;

import org.omg.CosNotification.*;

/**
 * The ProjectManager class is the major controlling class in the 
 * project_manager package.  See Scheduling Subsystem Design document, 
 * section 3.2.2.
 * 
 * @version 1.00 Feb 27, 2003
 * @author Allen Farris
 *
 */
public class ProjectManager implements Runnable {
    private boolean isSimulation;
    private ContainerServices containerServices;
    private Logger logger;
    private ALMAArchive archive;
    private ALMADispatcher dispatcher;
    private ProjectQueue projects;
    private ALMAPipeline pipeline;
    private MasterSBQueue sbQueue;
    private ProjectManagerTaskControl pmTaskControl;
    private PipelineEventReceiver pipeline_event;
    private ControlEventReceiver control_event;
    private PointingReducedEventReceiver pointing_event;
    private FocusReducedEventReceiver focus_event;
    private boolean pmFlag=true;
    private int pmSleepTime = 300000; //5 minute sleep
    private SchedulingPublisher sp;

    public ProjectManager(boolean isSimulation, ContainerServices cs, 
                    ALMAArchive a, MasterSBQueue q, ALMADispatcher d ) {
        this.isSimulation = isSimulation;
        this.archive = a;
        this.containerServices = cs;
        this.sbQueue = q;
        this.dispatcher = d;
        this.logger = cs.getLogger();
        // create the scheduling notification channel.
        createSchedulingNC();
        projects = new ProjectQueue();
        // TOOK OUT FOR TESTING!!!
        //projects.addProject(archive.getProject());
        
        // TEMPORARY!!
        //Link sbs to their projects!
        /*
        for(int i=0; i < projects.getQueueSize(); i++) {
            for(int j=0; j < sbQueue.getSUnitSize(); j++) {
                if(sbQueue.getSUnit(j).getProjectId().equals(projects.getProject(i).getId())){
                    projects.getProject(i).linkSUnitToProject(sbQueue.getSUnit(j));
                    System.out.println("SCHEDULING: sunit project id = "+sbQueue.getSUnit(j).getProjectId());
                    System.out.println("SCHEDULING: project id = "+projects.getProject(i).getId());
                }
            }
        }
        */
        pipeline = new ALMAPipeline(isSimulation, containerServices);
        //Receiver objects
        control_event = new ControlEventReceiver(cs, pipeline, archive, sbQueue);
        pipeline_event = new PipelineEventReceiver(cs, pipeline, archive);
        //pointing_event = new PointingReducedEventReceiver(cs);
        //focus_event = new FocusReducedEventReceiver(cs);
        if(isSimulation) {
            //its a simulation, so create local channels
            
            /*
            controlReceiver = AbstractNotificationChannel.getReceiver(
                AbstractNotificationChannel.LOCAL, 
                    alma.Control.CHANNELNAME.value, 
                        "scheduling");
            controlReceiver.attach("alma.Control.ExecBlockEndEvent",control_event);
            */
            /*
            pipelineReceiver = AbstractNotificationChannel.getReceiver(
                AbstractNotificationChannel.LOCAL, 
                    alma.pipelinescience.CHANNELNAME.value, 
                        "scheduling");
            pipelineReceiver.attach("alma.piplinescience.ScienceProcessingRequestEnd",pipeline_event);
            */
        } else {
            //its the real thing! create corba channels.
            logger.info("SCHEDULING: Trying to get NCs");
            
            try {
                control_event.addSubscription(alma.Control.EXECEVENTS.class);
                control_event.consumerReady();
                logger.info("SCHEDULING: Subscribed to CONTROL");
            } catch(Exception e) {
                logger.severe("SCHEDULING: Could not get control channel");
                logger.severe(e.toString());
            }
           
            try {
                //pipeline_event.addSubscription(alma.acsnc.DEFAULTTYPE.value);
                pipeline_event.addSubscription(
                    alma.pipelinescience.ScienceProcessingRequestEnd.class);
                pipeline_event.consumerReady();
                logger.info("SCHEDULING: Subscribed to PIPELINE");
            } catch(Exception e) {
                logger.severe("SCHEDULING: Could not get pipeline channel");
                logger.severe("SCHEDULING: "+ e.toString());
            }
            //Listen for TELCAL events
            try {
                //pointing_event.addSubscription("PointingReducedEvent");
                pointing_event.addSubscription(alma.TelCalPublisher.PointingReducedEvent.class);
                pointing_event.consumerReady();
                logger.info("SCHEDULING: Subscribed to TELCAL PointingReducedEvent");
            } catch(Exception e) {
                logger.severe("SCHEDULING: Could not get PointingReduced channel");
                logger.severe("SCHEDULING: "+ e.toString());
            }
            try {
                focus_event.addSubscription(alma.TelCalPublisher.FocusReducedEvent.class);
                focus_event.consumerReady();
                logger.info("SCHEDULING: Subscribed to TELCAL FocusReducedEvent");
            } catch(Exception e) {
                logger.severe("SCHEDULING: Could not get FocusReduced channel");
                logger.severe("SCHEDULING: "+ e.toString());
            }
            /*
            try {
            } catch(Exception e) {
            }
            try {
            } catch(Exception e) {
            }
            /*
            controlReceiver = AbstractNotificationChannel.getReceiver(
                        AbstractNotificationChannel.CORBA,
                            alma.Control.CHANNELNAME.value);
            //controlReceiver.attach(alma.Control.EXECEVENTS.value,control_event);
            controlReceiver.attach("alma.Control.ExecBlockEvent",control_event);
            controlReceiver.begin();
            pipelineReceiver = AbstractNotificationChannel.getReceiver(
                        AbstractNotificationChannel.CORBA,
                            alma.pipelinescience.CHANNELNAME.value);
            pipelineReceiver.attach("alma.pipelinescience.ScienceProcessingRequestEnd",pipeline_event);
            pipelineReceiver.begin();
            */
            logger.info("SCHEDULING: Got NCs");
        }
    }

    /** 
     * Creates the scheduling notification channel.
     */
    public void createSchedulingNC() {
        sp = new SchedulingPublisher(isSimulation, containerServices);
    }

    private Vector convertToVector(Object[] obj) {
        Vector tmp = new Vector();
        for(int i=0; i < obj.length; i++) {
            tmp.add(obj[i]);
        }
        return tmp;
    }


    /** Runnable method */
    public void run() {
        // Set pmTaskControl before it enters run loop!
        control_event.setProjectManagerTaskControl(pmTaskControl);
        pipeline_event.setProjectManagerTaskControl(pmTaskControl);
        while(pmFlag) {
            //check for completed projects/ObsUnitSets if there are
            //new ones which the pipeline are not dealing with, start
            //the pipeline and note in project/ObsUnitSets that pipeline
            //is processing it!
            if(projects.isProjectComplete()) {
                String[] completed = projects.getCompletedProjects();
                //start pipeline for each one of these projects.
            }
            try {
                Thread.sleep(pmSleepTime); //5 minute sleep.
                logger.info("SCHEDULING: PM woken up");
            }catch(InterruptedException e) {
                logger.info("SCHEDULING: PM interrupted");
            }
        }
    }
    /**
     *  Stops the project manager thread 
     */
    public void stop() {
        pmFlag = false;
        /* Disconnect from all notification channels. */
        pipeline_event.disconnect();
        control_event.disconnect();
        pointing_event.disconnect();
        focus_event.disconnect();
        logger.info("SCHEDULING: PM Stopped!");
    }

    //////////////////////////////////////////////////////////////////////////

    /* Get Methods! */

    public ProjectManagerTaskControl getProjectManagerTaskControl() {
        return pmTaskControl;
    }
    
    /* Set Methods! */
    
    public void setProjectManagerTaskControl(ProjectManagerTaskControl pmtc) {
        this.pmTaskControl = pmtc;
    }
    
    //////////////////////////////////////////////////////////////////////////

    public static void main(String[] args) {
	}
}
