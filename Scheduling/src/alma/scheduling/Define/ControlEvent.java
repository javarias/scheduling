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
 * File ControlEvent.java
 */

package alma.scheduling.Define;


/**
 * This class contains all the information from the ExecBlockEvent which in
 * ALMA production will come from the Control Subsystem. In simulation mode
 * it will be created by the Scheduling Subsystem's ControlSimulator.
 *
 * @author Sohaila Lucero
 * @version $Id: ControlEvent.java,v 1.7 2004/12/21 21:37:01 sslucero Exp $
 */
public class ControlEvent {
    /**
      *
      */
    private String ebId;
    /**
      *
      */
    private String sbId;
    //private String saId;
    /**
      *
      */
    private short saId;
    /**
      *
      */
    private int eventType;
    /**
      *
      */
    private int eventStatus;
    /**
      *
      */
    private DateTime startTime;
    

    /**
      *
      */
    public ControlEvent(String execblockId, String schedblockId, 
                        short subarrayId, int type, int status, 
                        DateTime st) {

        this.ebId = execblockId;
        this.sbId = schedblockId;
        this.saId = subarrayId;
        this.eventType = type;
        this.eventStatus = status;
        this.startTime = new DateTime(st);
        System.out.println("SCHEDULING: time is : "+ startTime.toString());
    }


    /**
      * @return String
      */
    public String getSBId() {
        return sbId;
    }
    /**
      * @return String
      */
    public String getEBId() {
        return ebId;
    }
    /**
      * @return short
      */
    public short getSAId() {
        return saId;
    }
    /**
      * @return int
      */
    public int getEventType() {
        return eventType;
    }
    /**
      * @return int
      */
    public int getStatus() {
        return eventStatus;
    }
    /**
      * @return DateTime
      */
    public DateTime getStartTime() {
        return startTime;
    }
    
}
