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

package ALMA.scheduling.project_manager;

import java.util.Vector;
import java.util.logging.Logger;
import java.util.logging.Level;

import alma.entity.xmlbinding.obsproject.*;
import alma.entity.xmlbinding.pipelineprocessingrequest.*;
import alma.entities.commonentity.EntityRefT;

import ALMA.scheduling.NothingCanBeScheduledEvent;
import ALMA.scheduling.master_scheduler.*;
import ALMA.scheduling.define.nc.*;

import alma.acs.component.ComponentLifecycle;
import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;

import alma.entity.xmlbinding.pipelineprocessingrequest.*;
import alma.entity.xmlbinding.pipelineprocessingrequest.types.*;

import org.omg.CosNotification.*;
import alma.acs.nc.*;

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
    private Vector projects;
    private ALMAPipeline pipeline;
    private MasterSBQueue sbQueue;
    private ProjectManagerTaskControl pmTaskControl;
    private PipelineEventReceiver p_event;
    private ControlEventReceiver c_event;
    private Receiver pipelineReceiver;
    private Receiver controlReceiver;
    private boolean pmFlag=true;
    private int pmSleepTime = 300000; //5 minute sleep
    private SimpleSupplier supplier;
    
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
        projects = convertToVector(archive.getProject());
        pipeline = new ALMAPipeline(isSimulation, containerServices);
        c_event = new ControlEventReceiver(pipeline, archive);
        p_event = new PipelineEventReceiver(pipeline, archive);
        if(isSimulation) {
            //its a simulation, so create local channels
            
            /*
            controlReceiver = AbstractNotificationChannel.getReceiver(
                AbstractNotificationChannel.LOCAL, 
                    ALMA.Control.CHANNELNAME.value, 
                        "scheduling");
            controlReceiver.attach("ALMA.Control.ExecBlockEndEvent",c_event);
            */
            /*
            pipelineReceiver = AbstractNotificationChannel.getReceiver(
                AbstractNotificationChannel.LOCAL, 
                    ALMA.pipelinescience.CHANNELNAME.value, 
                        "scheduling");
            pipelineReceiver.attach("alma.piplinescience.ScienceProcessingRequestEnd",p_event);
            */
        } else {
            //its the real thing! create corba channels.
            logger.info("Trying to get nc");
            try {
                c_event.addSubscription(ALMA.acsnc.DEFAULTTYPE.value);
                c_event.consumerReady();
            } catch(Exception e) {
                logger.severe("Could not get control channel");
                logger.severe(e.toString());
            }
            try {
                p_event.addSubscription(ALMA.acsnc.DEFAULTTYPE.value);
                p_event.consumerReady();
            } catch(Exception e) {
                logger.severe("Could not get pipeline channel");
                logger.severe(e.toString());
            }
            /*
            controlReceiver = AbstractNotificationChannel.getReceiver(
                AbstractNotificationChannel.CORBA, 
                    ALMA.Control.CHANNELNAME.value, 
                        "CONTROL", ALMA.Control.CHANNELNAME.value);
            controlReceiver.attach("ALMA.Control.ExecBlockEndEvent",c_event);
            pipelineReceiver = AbstractNotificationChannel.getReceiver(
                AbstractNotificationChannel.CORBA, 
                    ALMA.pipelinescience.CHANNELNAME.value, 
                        "PIPELINE", ALMA.pipelinescience.CHANNELNAME.value);
            pipelineReceiver.attach("alma.piplinescience.ScienceProcessingRequestEnd",p_event);
            */
        }
    }

    public void createSchedulingNC() {
        String[] eventType = new String[1];
        eventType[0] = "ALMA.scheduling.NothingCanBeScheduledEvent";
        if(isSimulation) { //create local channel
            LocalNotificationChannel sched = 
                new LocalNotificationChannel(
                    ALMA.scheduling.CHANNELNAME.value);
        } else { //create corba channel
            CorbaNotificationChannel sched = 
                new CorbaNotificationChannel(
                   ALMA.scheduling.CHANNELNAME.value);
        }
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
        c_event.setProjectManagerTaskControl(pmTaskControl);
        p_event.setProjectManagerTaskControl(pmTaskControl);
        while(pmFlag) {
            try {
                Thread.sleep(pmSleepTime); //5 minute sleep.
                logger.info("PM woken up");
            }catch(InterruptedException e) {
                logger.info("PM interrupted");
            }
        }
    }
    /**
     *  Stops the project manager thread 
     */
    public void stop() {
        pmFlag = false;
        p_event.disconnect();
        c_event.disconnect();
        logger.info("PM Stopped!");
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

    public void sendNothingCanBeScheduledEvent(NothingCanBeScheduledEvent e) {
        //action.sendNothingCanBeScheduledEvent(e);
    }

    public static void main(String[] args) {
	}
}
