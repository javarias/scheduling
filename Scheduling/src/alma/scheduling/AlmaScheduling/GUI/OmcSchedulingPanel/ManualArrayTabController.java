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
 * File ManualArrayTabController.java
 */
package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import alma.Control.DestroyedManualArrayEvent;
import alma.Control.ExecBlockEndedEvent;
import alma.Control.ExecBlockStartedEvent;
import alma.acs.logging.AcsLogger;
import alma.acs.nc.Consumer;
import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.exec.extension.subsystemplugin.SubsystemPlugin;
import alma.offline.ASDMArchivedEvent;
import alma.scheduling.ProjectLite;
import alma.xmlstore.XmlStoreNotificationEvent;

public class ManualArrayTabController extends SchedulingPanelController {
    private String arrayName="";
    private String arrayStatus="";
    private ManualArrayTab parent;
    private Consumer consumer = null;
    private String currentSBId;
    protected AcsLogger logger;
    private ArrayList<String> waitingForArchivedSB;
    private String currentExecBlockId;
    
    public ManualArrayTabController(PluginContainerServices cs, String a, ManualArrayTab p){
        super(cs);
        parent = p;
        arrayName = a;
        arrayStatus = "Active";
        logger = cs.getLogger();
        waitingForArchivedSB = new ArrayList<String>();
        try {
            consumer = new Consumer(alma.Control.CHANNELNAME_CONTROLSYSTEM.value, container);
            consumer.addSubscription(alma.Control.DestroyedManualArrayEvent.class, this);
            consumer.consumerReady();
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    protected void setArrayInUse(String arrayName){
        try {
            getMSRef();
            masterScheduler.setArrayInUse(arrayName);
            releaseMSRef();
        }catch(Exception e){
        }
    }

    protected String getArrayStatus() {
        return arrayStatus;
    }
    private void setArrayStatus(String s){
        arrayStatus = s;
        parent.updateArrayStatus();
    }
    protected boolean createConsolePlugin() {
        if(arrayName.equals("")){
            return false;
        }
        try {
            Class[] paramTypes = {String.class};
            String pluginImpl = "alma.Control.console.gui.CCLConsolePlugin";
            Class c = Class.forName(pluginImpl);
            Constructor constr = c.getConstructor(paramTypes);
            Object[] args = {arrayName};
            SubsystemPlugin ctrl = (SubsystemPlugin)constr.newInstance(args);
            //CCLConsolePlugin ctrl = new CCLConsolePlugin(arrayName);
            getCS().startChildPlugin("CCL Console", (SubsystemPlugin)ctrl);
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    protected boolean setupManualArrayConfigure(String sbId) {
        if(arrayName.equals("")){
            return false;
        }
        try {
        	currentSBId = sbId;
            getMSRef();
            ProjectLite project = masterScheduler.getProjectLiteForSB(sbId);
            Class[] paramTypes = {String.class};
            //String pluginImpl = "alma.Control.console.gui.CCLConsolePlugin";
            //Class c = Class.forName(pluginImpl);
            //Constructor constr = c.getConstructor(paramTypes);
            Object[] args = {arrayName,project.uid,sbId};
            getMSRef();
            masterScheduler.setManualArrayConfigure(arrayName,currentSBId);
                  
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public void receive(ExecBlockStartedEvent e) {
    	String sbid = e.sbId.entityId;
        String exec_id = e.execId.entityId;
        logger.fine("got start event in IS for sb "+sbid);
        if(!sbid.equals(currentSBId)){
        	// the event is not issue for this SB
            return;
        }
        
        if(!e.arrayName.equals(arrayName)) {
   	     //the event is not issue from this Control Array
               return;
        }
        
        currentExecBlockId = e.execId.entityId;
        waitingForArchivedSB.add(sbid);
        parent.setSBStatus(sbid, "RUNNING");
        parent.closeExecutionWaitingThing();
    }
    
    public void receive(ExecBlockEndedEvent e){
        parent.closeExecutionWaitingThing();
        String exec_id = e.execId.entityId;
        String sbid = e.sbId.entityId;
        if(!sbid.equals(currentSBId)){
            return;
        }
        
        if(!e.arrayName.equals(arrayName)) {
   	     //System.out.println("exit the receive event!"+e.arrayName);
               return;
   	}

        logger.fine("SCHEDULING_PANEL: SB("+sbid+")'s exec block("+exec_id+") ended");
        logger.fine("Completion value from control: "+e.status.value()+" : "+e.status.toString());
        
        String completion;
        completion = e.status.toString();//completions[e.status.value()];
        parent.setSBStatus(sbid, completion);
     
        currentSBId = "";
    }
    
    public void receive(ASDMArchivedEvent e){
        String sbid = e.workingDCId.schedBlock.entityId;
        logger.fine("SCHEDULING_PANEL: Got asdm archived event for SB("+sbid+")'s ASDM("+e.asdmId.entityId+").");
        String asdmId = e.asdmId.entityId;
        String completion = e.status;
        logger.fine("Current SB = "+currentSBId);
        //if(sbid.equals(currentSBId)){
        if(waitingForArchivedSB.contains(sbid)&& currentExecBlockId.equals(asdmId)){
            logger.fine("in list");
            if(completion.equals("complete")){
                parent.setSBStatus(sbid, "ARCHIVED");
            }
        }else{
            logger.fine("not in list");
        }
    }
    
    public void receive(XmlStoreNotificationEvent event) {
        //logger.fine("IS: got xml update event");
        CheckArchiveEvent processor = new CheckArchiveEvent(event);
        Thread t = container.getThreadFactory().newThread(processor);
        t.start();
    }
    
    
    protected void destroyArray(){
        try{
            getMSRef();
            masterScheduler.destroyArray(arrayName);
            releaseMSRef();
            if(consumer != null){
                consumer.disconnect();
                consumer = null;
            }
        } catch(Exception e){}
    }
    
    public void receive(DestroyedManualArrayEvent e){
        if(e.arrayName.equals(arrayName)){
            setArrayStatus("Destroyed");
        }
    }
    
    public void processXmlStoreNotificationEvent(XmlStoreNotificationEvent e) {
        //    logger.fine("SCHEDULING_PANEL: not doing anything with xml store notification event for now");
        }
    
    
    class CheckArchiveEvent implements Runnable {
        private XmlStoreNotificationEvent event;

        public CheckArchiveEvent(XmlStoreNotificationEvent e) {
            event = e;
        }
        public void run(){
            processXmlStoreNotificationEvent(event);
        }
    }
}
