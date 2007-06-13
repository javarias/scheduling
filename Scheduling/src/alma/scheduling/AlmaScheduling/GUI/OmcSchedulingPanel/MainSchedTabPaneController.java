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

    public void checkOperationalState() {
        try {
            MasterComponent sched_mc= alma.ACS.MasterComponentHelper.
                narrow(getCS().getComponentNonSticky("SCHEDULING_MASTER_COMP"));
            ROstringSeq csh = sched_mc.currentStateHierarchy();
            CompletionHolder ch = new CompletionHolder();
            String[] states = csh.get_sync(ch);
            for(int i=0; i < states.length; i++){
                System.out.println(states[i]);
            }
            //check if we're already operational!
            if(states.length ==2) {
                if(states[1].equals("OPERATIONAL")){
                    setOperationalStartState();
                    //check for existing schedulers
                    getMSRef();
                    SchedulerInfo[] active = masterScheduler.getAllActiveSchedulers();
                    releaseMSRef();
                    for(int i=0; i < active.length; i++){
                        logger.info("Active Scheduler "+(i+1)+": "+
                                active[i].schedulerCompName+"; "+
                                active[i].schedulerType+"; "+
                                active[i].schedulerArray+"; "+
                                active[i].schedulerId);
                    }
                    if ( active.length > 0 ) {
                        //start a new thread to create all the existing tabs.
                        DisplayExistingSchedulers existing = new DisplayExistingSchedulers(active);
                        Thread t = getCS().getThreadFactory().newThread(existing);
                        t.start();
                        //and presumably there will ne the same number of arrays! so lets get those
                        DisplayExistingArrays arrays = new DisplayExistingArrays();
                        Thread a = getCS().getThreadFactory().newThread(arrays);
                        a.start();
                    }
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
            logger.info("SCHEDULING_PANEL: Got Control in MainTab");
        } catch(Exception e){
            control = null;
        }
    }

    private void releaseControlRef(){
        if(control != null) {
            getCS().releaseComponent(masterScheduler.name());
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
            setOperationalStartState();
        }else if(e.state == SchedulingState.OFFLINE){
            connected = false;
            parent.connectedToALMA(connected);
        } else {
            return;
        }
    }
    private void setOperationalStartState() {
        connected = true;
        parent.setDefaults();
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
