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
 * File SearchArchiveOnlyController.java
 */
package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import alma.scheduling.SchedulerInfo;
import alma.scheduling.SchedulingState;
import alma.scheduling.SchedulingStateEvent;
import alma.exec.extension.subsystemplugin.*;
import alma.acs.nc.Consumer;
import alma.ACS.MasterComponent;
import alma.ACS.ROstringSeq;
import alma.ACSErr.CompletionHolder;

public class SearchArchiveOnlyController extends SchedulingPanelController{
    //private Consumer consumer;
    //private boolean connected = false;
    private SearchArchiveOnlyPlugin parent;

    public SearchArchiveOnlyController(SearchArchiveOnlyPlugin p) {
        super();
       // connected = false;
        parent = p;
    }

    public void setup(PluginContainerServices cs){
        super.onlineSetup(cs);
        logger.fine("SP: setup in SearchArchiveCtrl");
    }

    private void setOperationalStartState() {
        super.connected = true;
        parent.connectToALMA(super.connected);
    }
    
//    public boolean areWeConnected() {
//        checkOperationalState();
//        return super.connected;
//    }
    
    /**
      * Need receive to set start state
      */
    public void receive(SchedulingStateEvent e){
        logger.fine("GOT SchedulingStateEvent in SAO_Ctrl: "+e.state);
        if(e.state == SchedulingState.ONLINE_PASS2){
            setOperationalStartState();
        }else if(e.state == SchedulingState.OFFLINE){
            super.connected = false;
            parent.connectToALMA(super.connected);
        } else {
            return;
        }
    }
   
    /*
    public void checkOperationalState() {
        try {
            MasterComponent sched_mc= alma.ACS.MasterComponentHelper.
                narrow(getCS().getComponentNonSticky("SCHEDULING_MASTER_COMP"));
            ROstringSeq csh = sched_mc.currentStateHierarchy();
            CompletionHolder ch = new CompletionHolder();
            String[] states = csh.get_sync(ch);
            //check if we're already operational!
            if(states.length ==2) {
                if(states[1].equals("OPERATIONAL")){
                    setOperationalStartState();
                }
            }
        } catch(Exception e){
            logger.warning("SP: Problem checking master component state, check that SCHEDULING system is connected");
            //e.printStackTrace();
        }
    }*/
}
