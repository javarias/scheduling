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
 * File MasterSchedTabPanController.java
 */
package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.util.logging.Logger;
import alma.Control.ControlMaster;
import alma.scheduling.MasterSchedulerIF;
import alma.scheduling.SchedulingState;
import alma.scheduling.SchedulingStateEvent;
import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.acs.nc.Consumer;

public class MainSchedTabPaneController extends SchedulingPanelController{
    private ControlMaster control=null;
    private Consumer consumer;
    private boolean connected = false;
    private MainSchedTabPane parent;

    public MainSchedTabPaneController(MainSchedTabPane p){
        super();
        connected = false;
        parent = p;
    }
    public void setup(PluginContainerServices cs){
        super.onlineSetup(cs);
        try {
            consumer = new Consumer(alma.scheduling.CHANNELNAME_SCHEDULING.value, cs);
            consumer.addSubscription(SchedulingStateEvent.class, this);
            consumer.consumerReady();
        }catch(Exception e){
            logger.severe("SCHEDULING_PANEL: problem getting NC to get state event");
            logger.severe("SCHEDULING_PANEL: setting state to connected anyways: COULD CAUSE PROBLEMS");
            //todo send alarm
            connected = true;
            e.printStackTrace();
        }
    }

////////////////////////////////////    
    private void getControlRef(){
        try {
            control = alma.Control.ControlMasterHelper.narrow(
                container.getComponent("CONTROL/MASTER"));
            logger.info("SCHEDULING_PANEL: Got Control in MainTab");
        } catch(Exception e){
            control = null;
        }
    }

    private void releaseControlRef(){
        if(control != null) {
            container.releaseComponent(masterScheduler.name());
            logger.info("SCHEDULING_PANEL: Released Control in MainTab");
        }
    }
////////////////////////////////////    

    public String[] getAvailableAntennas(){
        String[] res = new String[1];
        try {
            res = control.getAvailableAntennas();
        } catch(Exception e){
            res[0] = new String("Problem getting antennas from control: "+e.toString());
            logger.severe("SCHEDULING_PANEL: Problem getting antennas from control - "+e.toString());
            e.printStackTrace();
        }
        return res;
    }

    public boolean areWeConnected(){
        return connected;
    }

    public void receive(SchedulingStateEvent e){
        logger.info("GOT SchedulingStateEvent: "+e.state);
        if(e.state == SchedulingState.ONLINE_PASS2){
            connected = true;
            parent.setDefaults();
            parent.connectedToALMA(connected);
        }else if(e.state == SchedulingState.OFFLINE){
            connected = false;
            parent.connectedToALMA(connected);
        } else {
            return;
        }
    }
}
