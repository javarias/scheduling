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
import alma.scheduling.SchedulerInfo;
import alma.scheduling.MasterSchedulerIF;
import alma.scheduling.SchedulingState;
import alma.scheduling.SchedulingStateEvent;
import alma.exec.extension.subsystemplugin.*;
import alma.acs.nc.Consumer;

import alma.ACS.MasterComponent;
import alma.ACS.ROstringSeq;
import alma.ACSErr.CompletionHolder;

public class MainSchedTabPaneController extends SchedulingPanelController{
    private ControlMaster control=null;
    private Consumer consumer;
    //private boolean connected = false;
    private MainSchedTabPane parent;

    public MainSchedTabPaneController(MainSchedTabPane p){
        super();
        //connected = false;
        parent = p;
    }
    public void setup(PluginContainerServices cs){
        super.onlineSetup(cs);
    }

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
    }

    protected SchedulerTab createSchedulerTab(String mode, String array){ //String title) {
        SchedulerTab tab=null;
        if(mode.equals("interactive")){
            //tab = new InteractiveSchedTab();//container, array);
            tab = new InteractiveSchedTab(container, array);
        } else if (mode.equals("queued")){
            tab = new QueuedSchedTab(container, array);
        } else if (mode.equals("dynamic")){
            tab = new DynamicSchedTab(container, array);
        } else if (mode.equals("manual")){
            tab = new ManualArrayTab(container, array);
        }
        return tab;
    }

    protected void openScheduler(SchedulerTab tab) throws Exception{
        container.startChildPlugin(tab.getTitle(), (SubsystemPlugin)tab);
    }

////////////////////////////////////    
    private void getControlRef(){
        try {
            control = alma.Control.ControlMasterHelper.narrow(
                getCS().getComponent("CONTROL/MASTER"));
            logger.fine("SCHEDULING_PANEL: Got Control in MainTab");
        } catch(Exception e){
            control = null;
        }
    }

    private void releaseControlRef(){
        if(control != null) {
            getCS().releaseComponent(masterScheduler.name());
            logger.fine("SCHEDULING_PANEL: Released Control in MainTab");
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
        checkOperationalState();
        return super.connected;
    }

    /**
      * Need receive here to set start state with defaults
      */
    public void receive(SchedulingStateEvent e){
        if(e.state == SchedulingState.ONLINE_PASS2){
            setOperationalStartState();
        }else if(e.state == SchedulingState.OFFLINE){
            super.connected = false;
            parent.connectedToALMA(super.connected);
        } else {
            return;
        }
    }
    private void setOperationalStartState() {
        super.connected = true;
        parent.setDefaults();
        parent.connectedToALMA(super.connected);
    }
    private void setOfflineState() {
        parent.setOfflineDisplay();
        connected = false;
        parent.connectedToALMA(connected);
    }

    private void displayExistingArrays(String[] automatic, String[] manual){
        parent.setExistingArrays(automatic, manual);
    }


    class DisplayExistingSchedulers implements Runnable {
        private SchedulerInfo[] existing;
        
        public DisplayExistingSchedulers(SchedulerInfo[] scheds){
            existing = scheds;
        }

        public void run() {
            String type, comp, id, array;
            SchedulerTab tab;
            for(int i=0; i < existing.length; i++){
                type = existing[i].schedulerType;
                comp = existing[i].schedulerCompName;
                array = existing[i].schedulerArray;
                id = existing[i].schedulerId;
                try {
                    if(type.equals("interactive")){
                        tab = new InteractiveSchedTab(container, array);   
                        //parent.addSchedulerTab(tab);
                        openScheduler(tab);
                    } else if(type.equals("queued")){
                        tab = new QueuedSchedTab(container, array);
                        //parent.addSchedulerTab(tab);
                        openScheduler(tab);
                    } else if(type.equals("dynamic")){
                        tab = new DynamicSchedTab(container,array);
                        //parent.addSchedulerTab(tab);
                        openScheduler(tab);
                    } else if(type.equals("manual")){
                        tab = new ManualArrayTab(container,array);
                        openScheduler(tab);
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    class DisplayExistingArrays implements Runnable {
        public DisplayExistingArrays (){
        }
        public void run(){
            getMSRef();
            String[] autoA = masterScheduler.getActiveAutomaticArrays();
            String[] manA = masterScheduler.getActiveManualArrays();
            displayExistingArrays(autoA, manA);
        }
    }
}
