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
package ALMA.scheduling.test;

import ALMA.acsnc.*;
import alma.acs.nc.*;
import org.omg.CosNotification.StructuredEvent;
import ALMA.TelCalPublisher.PointingScanReducedEvent_t;
import ALMA.TelCalPublisher.PointingScanReducedEvent_tHelper;
import ALMA.TelCalPublisher.FocusScanReducedEvent_t;
import ALMA.TelCalPublisher.FocusScanReducedEvent_tHelper;

public class TestSchedSupplier { // extends Supplier {

    private SimpleSupplier sup1;
    private SimpleSupplier sup2;

    public TestSchedSupplier() {
        String[] names = new String[3];
        names[SimpleSupplier.CHANNELPOS] = ALMA.TelCalPublisher.CHANNELNAME.value;
        names[SimpleSupplier.TYPEPOS] = new String(
            "PointingScanReducedEvent");
        //    "ALMA.TelCalPublisher.PointingScanReducedEvent_t");
        names[SimpleSupplier.HELPERPOS] = new String(
            "ALMA.TelCalPublisher.PointingScanReducedEvent_tHelper");
        //create a supplier for the PointingScanReducedEvent
        sup1 = new SimpleSupplier(names);
        System.out.println("SCHED_TEST: sup1 created"); 

        names[SimpleSupplier.TYPEPOS] = new String(
            "FocusScanReducedEvent");
            //"ALMA.TelCalPublisher.FocusScanReducedEvent_t");
        names[SimpleSupplier.HELPERPOS] = new String(
            "ALMA.TelCalPublisher.FocusScanReducedEvent_tHelper");

        //create a supplier for the FocusScanReducedEvent
        sup2 = new SimpleSupplier(names);
        System.out.println("SCHED_TEST: sup2 created"); 
    }

    public void sendTelCalEvents() {


        //send out a PointingScanReducedEvent
        PointingScanReducedEvent_t p_event = new PointingScanReducedEvent_t(
            "TestingPointingScanReduced",1,1,1);
        //send out a FocusScanReducedEvent
        FocusScanReducedEvent_t f_event = new FocusScanReducedEvent_t(
            "TestingFocusScanReduced",1,1,1);

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

