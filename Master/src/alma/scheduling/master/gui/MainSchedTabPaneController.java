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
package alma.scheduling.master.gui;

import alma.Control.ControlMaster;
import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.exec.extension.subsystemplugin.SubsystemPlugin;
import alma.scheduling.SchedulerInfo;
import alma.scheduling.SchedulingState;
import alma.scheduling.SchedulingStateEvent;

public class MainSchedTabPaneController extends SchedulingPanelController{
    private ControlMaster control = null;
   // private Consumer consumer;
    //private boolean connected = false;
    private MainSchedTabPane parent;

    public MainSchedTabPaneController(MainSchedTabPane p){
        super();
        //connected = false;
        parent = p;
    }
    public void setup(PluginContainerServices cs){
        super.onlineSetup(cs);
        logger.fine("SP: setup called MainSchedTabController");
    }

//    protected SchedulerTab createSchedulerTab(String mode, String array){ //String title) {
//        SchedulerTab tab=null;
//        //TODO: Create new tabs
//       if(mode.equals("interactive")){
//    	   System.out.println("Esto deberia crear una Array Panel");
//       }
////        } else if (mode.equals("queued")){
////            tab = new QueuedSchedTab(container, array);
////        } else if (mode.equals("dynamic")){
////            tab = new DynamicSchedTab(container, array);
////        } else if (mode.equals("manual")){
////            tab = new ManualArrayTab(container, array);
////        }
//        return tab;
//    }

    protected void openScheduler(SchedulerTab tab) throws Exception{
        container.startChildPlugin(tab.getTitle(), (SubsystemPlugin)tab);
    }

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

//    /**
//      * Need receive here to set start state with defaults
//    */
//    public void receive(SchedulingStateEvent e){
//        logger.fine("GOT SchedulingStateEvent in Main Ctrl: "+e.state);
//        if(e.state == SchedulingState.ONLINE_PASS2){
//            setOperationalStartState();
//        }else if(e.state == SchedulingState.OFFLINE){
//            super.connected = false;
//            parent.connectedToALMA(super.connected);
//        } else {
//            return;
//        }
//    }
    
//    private void setOperationalStartState() {
//        super.connected = true;
//        parent.setDefaults();
//        parent.connectedToALMA(super.connected);
//    }
    
}
