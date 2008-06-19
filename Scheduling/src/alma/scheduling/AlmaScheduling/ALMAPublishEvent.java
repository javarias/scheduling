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
 * File ALMAPublishEvent.java
 */
package alma.scheduling.AlmaScheduling;

import java.util.logging.Logger;

import alma.scheduling.StartSessionEvent;
import alma.scheduling.EndSessionEvent;
import alma.scheduling.NothingCanBeScheduledEvent;
import alma.scheduling.NothingCanBeScheduledEnum;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Event.Publishers.PublishEvent;
import alma.scheduling.SchedulingStateEvent;
import alma.acs.container.ContainerServices;
import alma.acs.exceptions.AcsJException;
import alma.acs.nc.AbstractNotificationChannel;

import alma.acs.nc.*;

/**
 * This class will publish a nothing can be scheduled event
 * over the acs notification channel when there is nothing
 * that can be scheduled.
 *
 * @version $Id: ALMAPublishEvent.java,v 1.15 2008/06/19 19:28:52 wlin Exp $
 * @author Sohaila Lucero
 */
public class ALMAPublishEvent extends PublishEvent {

    /**
      *
      */
    private ContainerServices container;


    /**
     * @throws AcsJException 
      *
      */
    public ALMAPublishEvent(ContainerServices cs) throws AcsJException {
        super(cs.getLogger()); 
        this.container = cs;
        this.sched_nc = AbstractNotificationChannel.createNotificationChannel(
            AbstractNotificationChannel.CORBA, 
                alma.scheduling.CHANNELNAME_SCHEDULING.value, 
                    container);
    }

    /** 
     * Publishes a NothingCanBeScheduledEvent given the reason-struct, the time it 
     * happened and if required a comment.
     * @param NothingCanBeScheduledEnum The reason!
     * @param int The time
     * @param String A comment
     */
    public void publish(NothingCanBeScheduledEnum reason, String time, String comment){
        try {
            NothingCanBeScheduledEvent event = 
                new NothingCanBeScheduledEvent(reason, time, comment);
            logger.finest("SCHEDULING: Event created!");
            sched_nc.publish(event);
            logger.finest("SCHEDULING: Event Sent!");
        } catch(Exception e) {
            logger.severe("SCHEDULING: Error publishing event.");
            e.printStackTrace(System.out);
        }
    }

    /**
     * Publishes a NothingCanBeScheduledEvent with the given string as 
     * the reason nothing can be scheduled.
     * @param String The Comment why there was nothing scheduled
     */
    public void publish(String reason) {
        publish(NothingCanBeScheduledEnum.OTHER,
            (new DateTime(System.currentTimeMillis())).toString(), reason);
    }


    /**
     * Publishes a StartSession event. This event is sent when the scheduling subsystem
     * starts to schedule the first SB in the session.
     * @param long The starttime defined in ACS time
     * @param String The session's ID
     * @param String The ObsUnitSet's ID
     * @param String The SB's ID
     */
   // public void publish(long start_time, String session_id, String ouc_id, String sb_id) {
        
     //   StartSessionEvent event = new StartSessionEvent();//start_time, session_id, ouc_id, sb_id);
       // event.startTime = start_time;
       // event.sessionId = session_id;
        //event.obsUnitSetId = ouc_id;
       // event.sbId = sb_id;
       // sched_nc.publish(event);    

    //}

    /**
     * Publishes an EndSession event.
     *
     * @param long The end time of the session. Defined in ACS time
     * @param String The session's ID
     * @param String The ObsUnitSet's ID
     */
   // public void publish(long end_time, String session_id, String ouc_id) {

     //   EndSessionEvent event = new EndSessionEvent();
       // event.endTime = end_time;
       // event.sessionId = session_id;
       // event.obsUnitSetId = ouc_id;
       // sched_nc.publish(event);
   // }

    /**
      * @param Object
      */
    public void publish(Object event) {
        logger.fine("SCHEDULING: event's class == "+
            event.getClass().getName());
        String eventClass = event.getClass().getName();
        try {
            if(event instanceof StartSessionEvent) {
                logger.fine("SCHEDULING: about to publish start session event");
                sched_nc.publish((StartSessionEvent)event);
                logger.finest("SCHEDULING: published start session event");
            
            } else if(event instanceof EndSessionEvent) {
                logger.fine("SCHEDULING: about to publish end session event");
                sched_nc.publish((EndSessionEvent)event);
                logger.finest("SCHEDULING: published end session event");
    
            } else if(event instanceof NothingCanBeScheduledEvent) {
                logger.fine("SCHEDULING: about to publish nothing can be scheduled event");
                sched_nc.publish((NothingCanBeScheduledEvent)event);
                logger.finest("SCHEDULING: published nothing can be scheduled event");
            }
        }catch(Exception e) {
            e.printStackTrace(System.out);
        }
    }

//    public void publishStateEvent(SchedulingStateEvent e){
  //      sched_nc.publish(e);
    //}
    

    /**
     * @throws AcsJException 
      *
      */
    public void disconnect() throws AcsJException {
        logger.finest("SCHEDULING: Disconnecting from Scheduling NC");
        sched_nc.deactivate();
    }
}
