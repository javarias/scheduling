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
 * File TestALMAPublishEvent.java
 */
package alma.scheduling.test;

import java.util.logging.Logger;

import alma.acs.nc.*;
import alma.acs.util.UTCUtility;
import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;
import alma.acs.component.client.ComponentClient;

import alma.scheduling.EndSessionEvent;
import alma.scheduling.StartSessionEvent;
import alma.scheduling.NothingCanBeScheduledEvent;
import alma.scheduling.NothingCanBeScheduledEnum;
import alma.scheduling.Define.DateTime;
import alma.scheduling.AlmaScheduling.ALMAPublishEvent;
import alma.Control.ControlSystemChangeOfStateEvent;

import alma.asdmIDLTypes.IDLEntityRef;

public class TestALMAPublishEvent {
    private ContainerServices container;
    private ALMAPublishEvent publisher;
    private Logger logger;
    private Receiver r1;
    private Receiver r2;
    private Receiver r3;
    private Receiver r4;
    private SimpleSupplier sup;
    
    public TestALMAPublishEvent(ContainerServices cs){
        this.container = cs;
        this.logger = cs.getLogger();
        
        r1 = AbstractNotificationChannel.getReceiver(
            AbstractNotificationChannel.CORBA, alma.scheduling.CHANNELNAME_SCHEDULING.value,
                container);
        r1.attach("alma.scheduling.NothingCanBeScheduledEvent", this);
        r1.begin();

        r2 = AbstractNotificationChannel.getReceiver(
            AbstractNotificationChannel.CORBA, alma.scheduling.CHANNELNAME_SCHEDULING.value,
                container);
        r2.attach("alma.scheduling.StartSessionEvent", this);
        r2.begin();
       
        r3 = AbstractNotificationChannel.getReceiver(
            AbstractNotificationChannel.CORBA, alma.scheduling.CHANNELNAME_SCHEDULING.value,
                container);
        r3.attach("alma.scheduling.EndSessionEvent", this);
        r3.begin();
        try {
            sup = new SimpleSupplier(alma.scheduling.CHANNELNAME_SCHEDULING.value, cs);
        } catch(Exception e) {
            e.printStackTrace();
        }
        r4 = AbstractNotificationChannel.getReceiver(
            AbstractNotificationChannel.CORBA, alma.scheduling.CHANNELNAME_SCHEDULING.value,
                container);
        r4.attach("alma.Control.ControlSystemChangeOfStateEvent", this);
        r4.begin();
    }

    public void receive(NothingCanBeScheduledEvent event) {
        logger.info("SCHED_TEST: Received NothingCanBeScheduledEvent");
    }
    public void receive(StartSessionEvent event) {
        logger.info("SCHED_TEST: received StartSessionEvent Event");
    }
    public void receive(EndSessionEvent event) {
        logger.info("SCHED_TEST: received EndSessionEvent Event");
    }
    public void receive(ControlSystemChangeOfStateEvent event) {
        logger.info("SCHED_TEST: received ControlSystemChangeOfStateEvent Event");
    }
 
    
    public void publish(ControlSystemChangeOfStateEvent e) {
        try {
            sup.publishEvent(e);
        }catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    

    public static void main(String[] args) {
        String name = new String("Scheduling Publisher Test");
        String managerLocation = System.getProperty("ACS.manager");
        try {
            ComponentClient client = new ComponentClient(null, managerLocation, name);
            
            TestALMAPublishEvent test = new TestALMAPublishEvent(client.getContainerServices());
            
            ALMAPublishEvent publisher = new ALMAPublishEvent(client.getContainerServices());
            Thread.sleep(5000);
            
            long startTime = UTCUtility.utcJavaToOmg(System.currentTimeMillis());
            IDLEntityRef sessRef = new IDLEntityRef();
            sessRef.entityId = "project status id";
            sessRef.partId = "session part id";
            sessRef.entityTypeName = "ProjectStatus";
            sessRef.instanceVersion = "1.0";
            IDLEntityRef sbRef = new IDLEntityRef();
            sbRef.entityId = "sb id";
            sbRef.partId = "sb part id";
            sbRef.entityTypeName = "SchedBlock";
            sbRef.instanceVersion = "1.0";
            //startTime, "session id", "ous part id", "sb id");
            StartSessionEvent event1 = new StartSessionEvent(
                    startTime, sessRef, sbRef);
            
            publisher.publish(event1);
            
            NothingCanBeScheduledEvent event2 = new NothingCanBeScheduledEvent(
                    NothingCanBeScheduledEnum.OTHER, (
                        new DateTime(System.currentTimeMillis())).toString(), "");
            
            publisher.publish(event2);       


            
            long endTime = UTCUtility.utcJavaToOmg(System.currentTimeMillis()); 
            String[] exec_ids = new String[2];
            exec_ids[0] = "exec id 1";
            exec_ids[1] = "exec id 2";
            EndSessionEvent event3 = new EndSessionEvent(
                    endTime, sessRef, sbRef, exec_ids);

            publisher.publish(event3);

            
            /*
            ControlSystemChangeOfStateEvent event4 = new ControlSystemChangeOfStateEvent();
                    event4.currentState = SystemState.OPERATIONAL;
                    event4.currentSubstate = SystemSubstate.STARTING_UP_PASS2;
                    event4.previousState = SystemState.OPERATIONAL;
                    event4.previousSubstate = SystemSubstate.STARTING_UP_PASS1;
                    event4.error = false; 
                    event4.time =0L;
            test.publish(event4);
*/
            
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
