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
 * File PipelineEvent.java
 */

package alma.scheduling.Define;

/**
 * This class contains all the information from the PipelineProcessingRequestEnd
 * which in ALMA production will come from the Pipeline Subsystem. In simulation
 * mode it will be created by the Scheduling Subsystem's PipelineSimulator.
 *
 * @author Sohaila Lucero
 * @version $Id: PipelineEvent.java,v 1.4 2004/11/23 20:41:21 sslucero Exp $
 */
public class PipelineEvent {
    /*
    private String ebId;
    private String sbId;
    private String saId;
    private String eventType;
    private String eventStatus;
    private DateTime startTime;
    */

    public PipelineEvent() {
    }

/*
    public String getSBId() {
        return sbId;
    }
    public String getEBId() {
        return ebId;
    }
    public String getSAId() {
        return saId;
    }
    public String getEventType() {
        return eventType;
    }
    public String getStatus() {
        return eventStatus;
    }
    public DateTime getStartTime() {
        return startTime;
    }
*/

}
