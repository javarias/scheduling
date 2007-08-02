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
 * File InteractiveSchedTabController.java
 */
package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.util.logging.Logger;
import java.util.ArrayList;
import alma.scheduling.SBLite;
import alma.scheduling.ProjectLite;
import alma.scheduling.MasterSchedulerIF;
import alma.scheduling.Interactive_PI_to_Scheduling;
import alma.scheduling.Define.*;
import alma.SchedulingExceptions.CannotRunCompleteSBEx;
import alma.acs.nc.Consumer;
import alma.Control.ExecBlockStartedEvent;
import alma.Control.ExecBlockEndedEvent;
import alma.Control.DestroyedAutomaticArrayEvent;
import alma.offline.ASDMArchivedEvent;
import alma.xmlstore.XmlStoreNotificationEvent;
import alma.exec.extension.subsystemplugin.PluginContainerServices;

public class InteractiveSchedTabController extends SchedulingPanelController {
    private Interactive_PI_to_Scheduling scheduler;
    private String schedulername;
    private Consumer consumer;
    private String currentSBId;
    private ArrayList<String> waitingForArchivedSB;
    //private String currentExecBlockId;
    private String arrayName;
    private String arrayStatus;
    private InteractiveSchedTab parent;
    
    //private PluginContainerServices foo;

    public InteractiveSchedTabController(PluginContainerServices cs, String a, InteractiveSchedTab p){
        super(cs);
        parent = p;
        //foo=cs;
        arrayName = a;
        arrayStatus = "Active";
        currentSBId = "";
        waitingForArchivedSB = new ArrayList<String>();
        try{
            //consumer = new Consumer(alma.xmlstore.CHANNELNAME.value,cs);
            //consumer.addSubscription(XmlStoreNotificationEvent.class, this);
            //consumer.consumerReady();
            consumer = new Consumer(alma.Control.CHANNELNAME_CONTROLSYSTEM.value, container);
            consumer.addSubscription(alma.Control.DestroyedAutomaticArrayEvent.class, this);
            consumer.addSubscription(alma.Control.ExecBlockStartedEvent.class, this);
            consumer.addSubscription(alma.Control.ExecBlockEndedEvent.class, this);
            consumer.addSubscription(alma.offline.ASDMArchivedEvent.class, this);
            consumer.consumerReady();
        }catch(Exception e){
            e.printStackTrace();
            logger.severe("SP: Error getting consumers for IS");
        }
        startInteractiveScheduler();
    }

    public String getSchedulerName(){
        return schedulername;
    }

    private void startInteractiveScheduler() {
        try {
            getMSRef();
            schedulername = masterScheduler.startInteractiveScheduling1(arrayName);
            logger.fine("SCHEDULINGPANEL: Interactive scheduling ("+schedulername+") started on array "+arrayName+".");
            releaseMSRef();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    protected void destroyArray(){
        destroyArray(arrayName);
        StopIS stopIs = new StopIS();
        Thread t = container.getThreadFactory().newThread(stopIs);
        t.start();
    }

    protected void stopInteractiveScheduling() {
        try {
    //        consumer.disconnect();
            getMSRef();
            masterScheduler.stopInteractiveScheduler(schedulername);
            releaseMSRef();
            if(consumer != null) {
                consumer.disconnect();
                consumer = null;
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void getISRef() {
        try {
            scheduler = alma.scheduling.Interactive_PI_to_SchedulingHelper.narrow(
                    container.getComponent(schedulername));
        }catch(Exception e){
            e.printStackTrace();
        }
        logger.fine("Got interactive scheduler reference");

    }
    public void releaseISRef(){
        try{
            container.releaseComponent(schedulername);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void setArrayInUse(String arrayName){
        try {
            getMSRef();
            masterScheduler.setArrayInUse(arrayName);
            releaseMSRef();
        }catch(Exception e){
        }
    }
    protected String getArrayName(){
        return arrayName;
    }

    protected String getArrayStatus() {
        return arrayStatus;
    }
    private void setArrayStatus(String stat){
        arrayStatus = stat;
        parent.updateArrayStatus();
    }

    public void startInteractiveSession(){
        try {
            scheduler.startSession("","");
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public synchronized void executeSB(String id) 
        throws SchedulingException, CannotRunCompleteSBEx {
        try{
            logger.fine("IS: Sending sb ("+id+") to be executed");
            currentSBId = id;
            getMSRef();
            ProjectLite project = masterScheduler.getProjectLiteForSB(id);
            scheduler.startSession(project.piName, project.uid);
            scheduler.executeSB(id);
            scheduler.setCurrentSB(id);
        }catch(CannotRunCompleteSBEx e){
            throw e;
        }catch( Exception e){
            throw new SchedulingException (e);
        }
    }

    public synchronized void stopSB()throws SchedulingException{
        try{
            logger.fine("IS: Requesting sb to stop");
            scheduler.stopSB();
            //scheduler.endSession();
        }catch( Exception e){
            throw new SchedulingException (e);
        }
    }

    public synchronized void stopNowSB()throws SchedulingException{
        try{
            logger.fine("IS: Requesting sb to abort");
            scheduler.stopNowSB();
        }catch( Exception e){
            throw new SchedulingException (e);
        }
    }

    public void receive(XmlStoreNotificationEvent event) {
        //logger.fine("IS: got xml update event");
        CheckArchiveEvent processor = new CheckArchiveEvent(event);
        Thread t = container.getThreadFactory().newThread(processor);
        t.start();
    }
    int startC =0;
    int stopC =0;
    public void receive(ExecBlockStartedEvent e) {
        String sbid = e.sbId.entityId;
        String exec_id = e.execId.entityId;
        logger.fine("got start event in IS for sb "+sbid);
        if(!sbid.equals(currentSBId)){
            return;
        }
        if(scheduler.getCurrentSB().equals(sbid) ){
            scheduler.setCurrentEB(exec_id);
            //currentExecBlockId = exec_id;
            logger.finest("Got start event for "+sbid+", ctr = "+startC);
            startC++;
            
        }
        //startInteractiveSession();
        waitingForArchivedSB.add(sbid);
        parent.setSBStatus(sbid, "RUNNING");
        parent.closeExecutionWaitingThing();
       // parent.setEnabled(false);
    }

    public void receive(ExecBlockEndedEvent e){
        parent.closeExecutionWaitingThing();
        logger.fine("got eb ended in IS");
        String exec_id = e.execId.entityId;
        String sbid = e.sbId.entityId;
        logger.fine("got ended event in IS for sb "+sbid);
        if(!sbid.equals(currentSBId)){
            return;
        }
        logger.fine("SCHEDULING_PANEL: SB("+sbid+")'s exec block("+exec_id+") ended");
        if(!scheduler.getCurrentSB().equals(sbid) && 
                !scheduler.getCurrentEB().equals(exec_id) ){
            logger.warning("Problem! SB id and exec block id are not current.. this shouldnt happen!");
           // currentExecBlockId = exec_id;
        } else {
            logger.finest("got stop for sb "+sbid+", ctr = "+stopC);
            stopC++;
        }
        String completion;
        logger.fine("Completion value from control: "+e.status.value()+" : "+e.status.toString());
        completion = e.status.toString();//completions[e.status.value()];
        parent.setSBStatus(sbid, completion);
        if(scheduler.getCurrentEB().equals(exec_id)){
            //ok to re-enable the search area now..
           // parent.setEnable(true);
        }
        try {
            scheduler.endSession();
            logger.fine("SP: Stopped IS session");
        } catch(Exception ex){
            logger.severe("SP: Error ending interactive session");
            ex.printStackTrace();
        }
        currentSBId = "";
    }

    public void receive(DestroyedAutomaticArrayEvent event) {
        String name = event.arrayName;
        logger.fine("SP: Received destroy array event for "+name+" in IS");
        if(name.equals(arrayName)){
            setArrayStatus("Destroyed");
        }
    }
    
    public void receive(ASDMArchivedEvent e){
        String sbid = e.workingDCId.schedBlock.entityId;
        logger.fine("SCHEDULING_PANEL: Got asdm archived event for SB("+sbid+")'s ASDM("+e.asdmId.entityId+").");
        String asdmId = e.asdmId.entityId;
        String completion = e.status;
        logger.fine("Current SB = "+currentSBId);
        //if(sbid.equals(currentSBId)){
        if(waitingForArchivedSB.contains(sbid)){
            logger.fine("in list");
            if(completion.equals("complete")){
                parent.setSBStatus(sbid, "ARCHIVED");
            }
        }else{
            logger.fine("not in list");
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

    class StopIS implements Runnable {
        public StopIS(){ }
        public void run(){
            stopInteractiveScheduling();
        }
    }
}
