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

import alma.scheduling.EndSession;
import alma.scheduling.StartSession;
import alma.scheduling.NothingCanBeScheduledEvent;
import alma.scheduling.NothingCanBeScheduledEnum;
import alma.scheduling.Define.DateTime;
import alma.scheduling.AlmaScheduling.ALMAPublishEvent;


public class TestALMAPublishEvent {
    private ContainerServices container;
    private ALMAPublishEvent publisher;
    private Logger logger;
    private Receiver r1;
    private Receiver r2;
    private Receiver r3;
    
    public TestALMAPublishEvent(ContainerServices cs){
        this.container = cs;
        this.logger = cs.getLogger();
        r1 = AbstractNotificationChannel.getReceiver(
            AbstractNotificationChannel.CORBA, alma.scheduling.CHANNELNAME.value,
                container);
        r1.attach("alma.scheduling.NothingCanBeScheduledEvent", this);
        r1.begin();

        r2 = AbstractNotificationChannel.getReceiver(
            AbstractNotificationChannel.CORBA, alma.scheduling.CHANNELNAME.value,
                container);
        r2.attach("alma.scheduling.StartSession", this);
        r2.begin();
       
        r3 = AbstractNotificationChannel.getReceiver(
            AbstractNotificationChannel.CORBA, alma.scheduling.CHANNELNAME.value,
                container);
        r3.attach("alma.scheduling.EndSession", this);
        r3.begin();
    }

    public void receive(NothingCanBeScheduledEvent event) {
        logger.info("SCHED_TEST: Received NothingCanBeScheduledEvent");
    }
    public void receive(StartSession event) {
        logger.info("SCHED_TEST: received StartSession Event");
    }
    public void receive(EndSession event) {
        logger.info("SCHED_TEST: received EndSession Event");
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
            StartSession event1 = new StartSession(startTime, "session id", "ous part id", "sb id", "eb id");
            
            publisher.publish(event1);

            NothingCanBeScheduledEvent event2 = new NothingCanBeScheduledEvent(
                    NothingCanBeScheduledEnum.OTHER, (new DateTime(System.currentTimeMillis())).toString(), "");
            
            publisher.publish(event2);       

            long endTime = UTCUtility.utcJavaToOmg(System.currentTimeMillis()); 
            EndSession event3 = new EndSession(endTime, "session id", "ous id", "eb id");

            publisher.publish(event3);

            
        } catch(Exception e) {
        }
    }
}
