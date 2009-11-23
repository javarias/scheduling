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
 * File QueuedSchedTabController.java
 */
package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.util.ArrayList;

import alma.Control.DestroyedAutomaticArrayEvent;
import alma.acs.nc.Consumer;
import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.offline.ASDMArchivedEvent;
import alma.scheduling.GUIExecBlockEndedEvent;
import alma.scheduling.GUIExecBlockStartedEvent;
import alma.scheduling.Queued_Operator_to_Scheduling;
import alma.scheduling.SBLite;

public class QueuedSchedTabController extends SchedulingPanelController {
    private QueuedSchedTab parent;
    private String arrayName;
    private String[] sbs_to_run;
    private String currentSB;
    private String currentEB;
    private Consumer ctrl_consumer = null;
    private Consumer schedChannelConsumer;
    private String schedulerName;
    private String arrayStatus;
    private Queued_Operator_to_Scheduling qsComp;
    private boolean aborted = false; 

    
    public QueuedSchedTabController(PluginContainerServices cs, QueuedSchedTab p, String a){
        super(cs);
        parent = p;
        arrayName = a;
        arrayStatus = "Active";
        currentSB = "";
        try{
            //consumer = new Consumer(alma.xmlstore.CHANNELNAME.value,cs);
            //consumer.addSubscription(XmlStoreNotificationEvent.class, this);
            //consumer.consumerReady();
            ctrl_consumer = new Consumer(alma.Control.CHANNELNAME_CONTROLSYSTEM.value, container);
            ctrl_consumer.addSubscription(alma.offline.ASDMArchivedEvent.class, this);
            ctrl_consumer.addSubscription(alma.Control.DestroyedAutomaticArrayEvent.class, this);
            ctrl_consumer.consumerReady();
            
            schedChannelConsumer = new Consumer(alma.scheduling.CHANNELNAME_SCHEDULING.value, container);
            schedChannelConsumer.addSubscription(alma.scheduling.GUIExecBlockStartedEvent.class, this);
            schedChannelConsumer.addSubscription(alma.scheduling.GUIExecBlockEndedEvent.class, this);
            schedChannelConsumer.consumerReady();
            
            getMSRef();
            schedulerName = masterScheduler.createQueuedSchedulingComponent(arrayName);
            releaseMSRef();
            qsComp = alma.scheduling.Queued_Operator_to_SchedulingHelper.narrow(
                    container.getComponent(schedulerName));
            qsComp.setArray(a);
        }catch(Exception e){
            e.printStackTrace();
        }    
    }

    protected void setSchedulerName(String name){
        schedulerName = name;
    }
    protected String getSchedulerName(){
        return schedulerName;
    }

    protected SBLite[] getSBLites(String[] ids){
        getMSRef();
        SBLite[] sbs = masterScheduler.getSBLite(ids);
        releaseMSRef();
        return sbs;
    }

    protected void addSBs(String[] ids){
        ArrayList<String> all = new ArrayList<String>();
        if(sbs_to_run != null){
            for(int i=0; i < sbs_to_run.length; i++){
                all.add(sbs_to_run[i]);
            }
        } 
        for(int i=0; i < ids.length; i++){
            qsComp.addSB(ids[i]);
            all.add(ids[i]);
        }
        sbs_to_run = new String[all.size()];
        sbs_to_run = all.toArray(sbs_to_run);
    }

    protected void removeSBs(String[] ids, int[] ind){
        // SOMEHOW:
        // check the sbs's running status, IE: if its already running or finished 
        // running then don't take it from component
        ArrayList<String> modified = new ArrayList<String>();
        //int index=0;
        for(int index=0; index < sbs_to_run.length; index++){
            for(int i=0; i < ind.length; i++){
                if(index != ind[i]){
                    //if the index isn't in the lest to delete
                    //add it back
                    modified.add(sbs_to_run[index]);
                } //else {
                //}
            }
        }
        sbs_to_run = new String[modified.size()];
        sbs_to_run = modified.toArray(sbs_to_run);
        try {
            qsComp.removeSBs(ids, ind);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    protected void runQueuedScheduling(String[] sb_ids){
        try {
            sbs_to_run = new String[sb_ids.length];
            for(int i=0; i < sb_ids.length; i++){
                sbs_to_run[i] = sb_ids[i];
            }
            //reset aborted flag
            aborted = false;
            qsComp.runQueue();
        } catch(Exception e){
            e.printStackTrace();
        }
        

    }
    protected void destroyArray() {
        destroyArray(arrayName);
        StopQS foo = new StopQS();
        Thread t = container.getThreadFactory().newThread(foo);
        t.start();
    }


    protected void stopQueuedScheduling(){
        try {
            getMSRef();
            try {
                masterScheduler.stopQueuedScheduler(arrayName);
            } catch(Exception e) {}
            releaseMSRef();
            container.releaseComponent(qsComp.name());
            if(ctrl_consumer != null){
                ctrl_consumer.disconnect();
                ctrl_consumer = null;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private boolean doesSbBelong(String id){
        boolean b= false;
        for(int i=0; i <  sbs_to_run.length; i++){
            if(id.equals(sbs_to_run[i])) {
                //matches
                b= true;
                break;
            }
        }
        return b;
    }
    private SBLite getSBLite(String id) {
        String[] ids = new String[1];
        ids[0]=id;
        try {
            getMSRef();
            SBLite[] lites = masterScheduler.getSBLite(ids);
            releaseMSRef();
            return lites[0];
        } catch(Exception e){
            return null;
        }
    }
    protected String getCurrentSB(){
        return currentSB;
    }

    //used to check if any of the selected sb marked fro removal is running
    protected String getCurrentEB(){
        return currentEB;
    }

    protected String getArrayStatus(){
        return arrayStatus;
    }

    private void setArrayStatus(String stat){
        arrayStatus = stat;
        parent.updateArrayStatus();
    }

    private boolean stopFlag=false;
    public void stopSB() throws Exception {
        try {
            logger.fine("stopSB in QS Ctrller called");
            if(!currentSB.equals("")){
                qsComp.stopSB(currentSB);
            } else {
                stopFlag = true;
            }
        } catch(Exception e){
            e.printStackTrace();
            throw e;
        }
        
    }
    public void stopWholeQueue() throws Exception{
        try {
            logger.fine("stopQueue in QS Ctrller called");
            qsComp.stopQueue();
        } catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    public void abortSB() throws Exception{
        try {
            logger.fine("abortSB in QS Ctrller called");
            qsComp.abortSB();
            aborted = true;
        } catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    public void abortQueue()  throws Exception{
        try {
            logger.fine("abortSB in QS Ctrller called");
            qsComp.abortQueue();
            aborted = true;
        } catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    public void receive(DestroyedAutomaticArrayEvent e){
        logger.fine("Automatic array destroyed event received for "+e.arrayName);
        if(e.arrayName.equals(arrayName)){
            setArrayStatus("Destroyed");
        }
    }
    
    public void receive(GUIExecBlockStartedEvent e){
        logger.fine("EXEC started event");
        String exec_id = e.execBlockUID;
        String sbid = e.schedBlockUID;
        currentSB = sbid;
        currentEB = exec_id;
        boolean belongs = doesSbBelong(sbid);
        //DateTime start_time = new DateTime(UTCUtility.utcOmgToJava(e.startTime));
        if(belongs) {
            SBLite sb = getSBLite(sbid);
            if(sb == null){
                parent.updateExecutionInfo
                    ("An error occured getting SB info when exec block started.\n");
            } else {
                parent.updateExecutionInfo("Execution started for SB: "+sb.sbName+"\n");
                parent.setSBStatus(sbid, "RUNNING");
            }
        }
        if(stopFlag){
            qsComp.stopSB(currentSB);
            stopFlag = false;
        }
    }
            
    public void receive(GUIExecBlockEndedEvent e){
        logger.fine("EXEC ended event");
        String exec_id = e.execBlockUID;
        String sbid = e.schedBlockUID;
        if(!doesSbBelong(sbid)){
            //no match so return
            return;
        }
        SBLite sb = getSBLite(sbid);
        logger.fine("SCHEDULING_PANEL: SB("+sbid+")'s exec block("+exec_id+") ended");
        String completion = e.status;
        parent.updateExecutionInfo("Execution ended for SB: "+sb.sbName+".\n");
        if(!aborted) {
        	parent.updateExecutionInfo("Waiting for it to be Archived.\n");
        } else {
        	parent.updateExecutionInfo("Aborted: nothing will be archived.\n");
        }
        System.out.println(completion);
        parent.setSBStatus(sbid, completion);
        //TODO: Set stop buttons to disabled if last SB in queue.
        parent.updateExecutionRow();
        if(parent.getCurrentExecutionRow()==sbs_to_run.length){
        	qsComp.setExecStarted(false);
        }
        currentSB = "";
    }
    
    public void receive(ASDMArchivedEvent e){
        //logger.fine("asdm archived event");
        String sbid = e.workingDCId.schedBlock.entityId;
        logger.fine("SCHEDULING_PANEL: Got asdm archived event for SB("+sbid+")'s ASDM("+e.asdmId.entityId+").");
        String asdmId = e.asdmId.entityId;
        String completion = e.status;
        boolean belongs = doesSbBelong(sbid);
        if(belongs){
            if(completion.equals("complete")){
                parent.setSBStatus(sbid, "ARCHIVED");
            }
            //set status to ARCHIVED
            SBLite sb = getSBLite(sbid);
            parent.updateExecutionInfo("ASDM archive status for "+sb.sbName+" is "+completion+".\n");
            parent.updateArchivingRow();
        }
    }

    class StopQS implements Runnable {
        public StopQS (){}
        public void run (){
            stopQueuedScheduling();
        }
    }
}
