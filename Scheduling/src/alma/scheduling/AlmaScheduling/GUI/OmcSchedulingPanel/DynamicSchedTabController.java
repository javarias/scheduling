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
 * File DynamicSchedTabController.java
 */
package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.util.logging.Logger;
import java.util.Vector; 

import alma.scheduling.SBLite;
import alma.scheduling.ProjectLite;
import alma.scheduling.MasterSchedulerIF;
import alma.scheduling.InvalidOperation;
import alma.SchedulingExceptions.InvalidOperationEx;
import alma.scheduling.Define.*;
import alma.scheduling.Dynamic_Operator_to_Scheduling;
import alma.SchedulingExceptions.wrappers.AcsJInvalidOperationEx;

import alma.xmlentity.XmlEntityStruct;
import alma.acs.callbacks.RequesterUtil;
import alma.acs.callbacks.ResponseReceiver;
import alma.acs.nc.Consumer;
import alma.acs.container.ContainerServices;
import alma.Control.ExecBlockStartedEvent;
import alma.Control.ExecBlockEndedEvent;
import alma.Control.DestroyedAutomaticArrayEvent;
import alma.offline.ASDMArchivedEvent;
import alma.xmlstore.XmlStoreNotificationEvent;
import alma.exec.extension.subsystemplugin.PluginContainerServices;

public class DynamicSchedTabController extends SchedulingPanelController {
    private String schedulername;
    private Consumer consumer = null;
    private Consumer ctrl_consumer = null;
    private String currentSBId;
    private String currentExecBlockId;
    private String arrayName;
    private String arrayStatus;
    private DynamicSchedTab parent;
    private Dynamic_Operator_to_Scheduling dsComp;
    
    public DynamicSchedTabController(PluginContainerServices cs, String a, DynamicSchedTab p){
        super(cs);
        parent = p;
        arrayName = a;
        arrayStatus = "Active";
        try{
            consumer = new Consumer(alma.xmlstore.CHANNELNAME.value,cs);
            consumer.addSubscription(XmlStoreNotificationEvent.class, this);
            consumer.consumerReady();
            ctrl_consumer = new Consumer(alma.Control.CHANNELNAME_CONTROLSYSTEM.value, container);
            ctrl_consumer.addSubscription(alma.Control.DestroyedAutomaticArrayEvent.class, this);
            ctrl_consumer.consumerReady();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public String getSchedulerName(){
        return schedulername;
    }

    public void setSchedulerName(String n){
        schedulername = n;
    }
    
    protected String getArrayStatus() {
        return arrayStatus;
    }
    
    private void setArrayStatus(String stat){
        arrayStatus = stat;
        parent.updateArrayStatus();
    }

    public void startDynamicScheduling() throws InvalidOperationEx {
        //tell MS to startScheduling
        try {
            getMSRef();
            logger.info("Creating DS Component!!!");
            schedulername = masterScheduler.createDynamicSchedulingComponent(arrayName);
            getDynamicSchedComponent();
            setCallbackInfo();
            masterScheduler.startScheduling1(new XmlEntityStruct(), arrayName);

            releaseMSRef();
        }catch(Exception e) {
            InvalidOperation e1 = new InvalidOperation("startDynamicScheduling", e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
    }

    private void setCallbackInfo() throws Exception {
       ResponseReceiver allSbResponse = new ResponseReceiver() {
            public void incomingResponse (Object x) {
                logger.info("DS allSbResponse class: "+x.getClass().getName());
                updateSBTableWithAllSBs((String[])x);
            }
            public void incomingException (Exception x) {
                logger.severe("Responding failed: "+x);
            }
        };
       ResponseReceiver topSbResponse = new ResponseReceiver() {
            public void incomingResponse (Object x) {
                logger.info("***************************");
                logger.info("DS topSbResponse class: "+x.getClass().getName());
                logger.info("***************************");
                updateSBTableWithTopSBs((String[])x);
            }
            public void incomingException (Exception x) {
                logger.severe("Responding failed: "+x);
            }
        };
        dsComp.setCbForAllSBs(RequesterUtil.giveCBStringSequence((
                              ContainerServices)container,allSbResponse),
                              RequesterUtil.giveDescIn()); 
        dsComp.setCbForTopSBs(RequesterUtil.giveCBStringSequence((
                              ContainerServices)container,topSbResponse),
                              RequesterUtil.giveDescIn()); 
    }

    protected void stopDynamicScheduling() {
        try {
            releaseDynamicSchedComponent();
            getMSRef();
            masterScheduler.stopDynamicScheduler(schedulername);
            consumer.disconnect();
            releaseMSRef();
            //container.releaseComponent(schedulername);
        } catch(Exception e) {
            logger.warning("SCHEDULING_PANEL: DS problem destroying array");
            logger.warning("SCHEDULING_PANEL: "+e.toString());
        }
    }
    /**
      * Destroy the array and release the scheduler (remove its reference 
      * in the Master Scheduler
      */
    protected void destroyArray(){
        destroyArray(arrayName);
        StopDS foo = new StopDS();
        Thread t = new Thread(foo);
        t.start();
    }

    private void getDynamicSchedComponent() throws Exception {
        dsComp = alma.scheduling.Dynamic_Operator_to_SchedulingHelper.narrow(
                        container.getComponent(schedulername));
    }

    private void releaseDynamicSchedComponent() throws Exception {
        container.releaseComponent(schedulername);
    }

    private void updateSBTableWithAllSBs(String[] s) {
        logger.info("Responded with all SBs");
        try{
           setCallbackInfo();
        } catch(Exception e) {
            logger.severe("SCHEDULING_PANEL: error setting callback for DS "+e.toString());
            e.printStackTrace();
        }
    }
    
    private void updateSBTableWithTopSBs(String[] s) {
        logger.info("Responded with list of top SBs");
        try{
            setCallbackInfo();
        } catch(Exception e) {
            logger.severe("SCHEDULING_PANEL: error setting callback for DS "+e.toString());
            e.printStackTrace();
        }
        getMSRef();
        SBLite[] topSBs = masterScheduler.getSBLite(s);
        logger.info("number of top sbs = "+topSBs.length);
        parent.updateTableWithSBList(topSBs);
        releaseMSRef();
    }

    public void respondToDS(String sbid) {
        try {
            logger.info("DSComp is given sb = "+sbid);
            dsComp.selectSB(sbid);
        } catch(Exception e){
            logger.severe("SP Couldn't respond to DS: "+e.toString());
            e.printStackTrace();
        }
    }
    
    public void receive(DestroyedAutomaticArrayEvent e){
        System.out.println("Automatic array destroyed event received for "+e.arrayName);
        if(e.arrayName.equals(arrayName)){
            setArrayStatus("Destroyed");
        }
    }
    
    public void receive(XmlStoreNotificationEvent e) {
    }


    public void processXmlStoreNotificationEvent(XmlStoreNotificationEvent e) {
    //    logger.info("SCHEDULING_PANEL: not doing anything with xml store notification event for now");
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

    class StopDS implements Runnable {
        public StopDS(){}
        public void run(){
            stopDynamicScheduling();
        }
    }
}
