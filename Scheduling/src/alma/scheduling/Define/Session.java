/*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by AUI (in the framework of the ALMA collaboration),
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
 * File Session.java
 * 
 */

package alma.scheduling.Define;

import java.util.Vector;

public class Session {
    private String sessionId;
    private String startTime;
    private String endTime;
    private String obsUnitSetId;
    private String sbId;
    private Vector execBlockIds;
    
    public Session() {
        execBlockIds = new Vector();
    }

    public Session(String id, String sTime, String eTime, String oucId, String sbId){
        this();
        this.sessionId = id;
        this.startTime= sTime;
        this.endTime = eTime;
        this.obsUnitSetId = oucId;
        this.sbId = sbId;
    }

    public void addExecBlockId(String id) {
        execBlockIds.add(id);
    }

    public void setId(String s) {
        sessionId = s;
    }
    
    public void setStartTime(String s) {
        startTime = s;
    }

    public void setEndTime(String s) {
        endTime = s;
    }

    public void setObsUnitSetId(String s) {
        obsUnitSetId =s;
    }
    
    public void setSbId(String s) {
        sbId = s;
    }
  

    public String getSessionId() {
        if(sessionId != null) {
            return sessionId;
        } else {
            return "Session ID is null";
        }
    }
    public String getStartTime(){
        if(startTime !=null) {
            return startTime;
        } else {
            return "Start time is null";
        }
    }
    public String getEndTime(){
        if(endTime != null) {
            return endTime;
        } else {
            return "End Time is null";
        }
    }
    public String getObsUnitSetId() {
        if(obsUnitSetId != null) {
            return obsUnitSetId;
        } else {
            return "ObsUnitSet id is null";
        }
    }
    public String getSbId() {
        if(sbId == null) {
            return sbId;
        } else {
            return "SB id is null";
        }
    }

    public String[] getExecBlockIds() {
        String[] ids = new String[execBlockIds.size()];
        for(int i=0; i < execBlockIds.size(); i++){
            ids[i] = (String)execBlockIds.elementAt(i);
        }
        return ids;
    }

}
