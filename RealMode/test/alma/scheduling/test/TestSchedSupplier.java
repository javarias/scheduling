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
 * File TestSchedSupplier.java
 * 
 */
package alma.scheduling.test;

import alma.acsnc.*;
import alma.acs.nc.*;
import org.omg.CosNotification.StructuredEvent;
import alma.TelCalPublisher.PointingReducedEvent;
import alma.TelCalPublisher.PointingReducedEventHelper;
import alma.TelCalPublisher.FocusReducedEvent;
import alma.TelCalPublisher.FocusReducedEventHelper;

public class TestSchedSupplier { // extends Supplier {

    private SimpleSupplier sup1;
    private SimpleSupplier sup2;

    public TestSchedSupplier() {
        String[] names = new String[3];
        names[SimpleSupplier.CHANNELPOS] = alma.TelCalPublisher.CHANNELNAME.value;
        names[SimpleSupplier.TYPEPOS] = new String(
            "PointingReducedEvent");
        //    "alma.TelCalPublisher.PointingReducedEvent");
        names[SimpleSupplier.HELPERPOS] = new String(
            "alma.TelCalPublisher.PointingReducedEventHelper");
        //create a supplier for the PointingReducedEvent
        sup1 = new SimpleSupplier(names);
        System.out.println("SCHED_TEST: sup1 created"); 

        names[SimpleSupplier.TYPEPOS] = new String(
            "FocusReducedEvent");
            //"alma.TelCalPublisher.FocusReducedEvent");
        names[SimpleSupplier.HELPERPOS] = new String(
            "alma.TelCalPublisher.FocusReducedEventHelper");

        //create a supplier for the FocusReducedEvent
        sup2 = new SimpleSupplier(names);
        System.out.println("SCHED_TEST: sup2 created"); 
    }

    public void sendTelCalEvents() {


        //send out a PointingReducedEvent
        PointingReducedEvent p_event = new PointingReducedEvent(
            "TestingPointingReduced",1,"1","1");
        //send out a FocusReducedEvent
        FocusReducedEvent f_event = new FocusReducedEvent(
            "TestingFocusReduced",1,"1","1");

        //Publish the events
        try {
            sup1.publishEvent(p_event);
            sup2.publishEvent(f_event);
            System.out.println("SCHED_TEST: Published TelCalEvents");
        } catch(Exception e) {
            System.out.println("SCHED_TEST: Could not publish events from Telcal.");
            System.out.println(e.toString());
        }

    }

    public void disconnect() {
        sup1.disconnect();
        sup2.disconnect();
    }
}

