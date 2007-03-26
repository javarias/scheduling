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
import java.util.logging.Logger;
import alma.scheduling.SBLite;
import alma.scheduling.ProjectLite;
import alma.scheduling.MasterSchedulerIF;
import alma.scheduling.Queued_Operator_to_Scheduling;
import alma.scheduling.Define.*;
import alma.acs.nc.Consumer;

import alma.Control.ExecBlockStartedEvent;
import alma.Control.ExecBlockEndedEvent;
import alma.Control.DestroyedAutomaticArrayEvent;
import alma.offline.ASDMArchivedEvent;
import alma.xmlstore.XmlStoreNotificationEvent;
import alma.exec.extension.subsystemplugin.PluginContainerServices;

public class QueuedSchedTabController extends SchedulingPanelController {
    private QueuedSchedTab parent;
    private String arrayName;
    private Thread thread;
    private String[] sbs_to_run;
    private String currentSB;
    private String currentEB;
    private Consumer consumer = null;
    private Consumer ctrl_consumer = null;
    private String schedulerName;
    private String arrayStatus;
    private Queued_Operator_to_Scheduling qsComp;

    
    public QueuedSchedTabController(PluginContainerServices cs, QueuedSchedTab p, String a){
        super(cs);
        parent = p;
        arrayName = a;
        arrayStatus = "Active";
        try{
            //consumer = new Consumer(alma.xmlstore.CHANNELNAME.value,cs);
            //consumer.addSubscription(XmlStoreNotificationEvent.class, this);
            //consumer.consumerReady();
            ctrl_consumer = new Consumer(alma.Control.CHANNELNAME_CONTROLSYSTEM.value, container);
            ctrl_consumer.addSubscription(alma.Control.ExecBlockEndedEvent.class, this);
            ctrl_consumer.addSubscription(alma.Control.ExecBlockStartedEvent.class, this);
            ctrl_consumer.addSubscription(alma.offline.ASDMArchivedEvent.class, this);
            ctrl_consumer.addSubscription(alma.Control.DestroyedAutomaticArrayEvent.class, this);
            ctrl_consumer.consumerReady();
            
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
            System.out.println("orig sbs in table: ");
            for(int i=0; i < sbs_to_run.length; i++){
                System.out.println("\t"+ sbs_to_run[i]);
                all.add(sbs_to_run[i]);
            }
        } 
        System.out.println("should be order as above: "+all.toString());
        for(int i=0; i < ids.length; i++){
            qsComp.addSB(ids[i]);
            all.add(ids[i]);
        }
        System.out.println(all.toString());
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
                    System.out.println("keeping sb "+sbs_to_run[index]);
                    modified.add(sbs_to_run[index]);
                } else {
                    System.out.println("not keeping "+sbs_to_run[index]);
                }
            }
        }
        sbs_to_run = new String[modified.size()];
        sbs_to_run = modified.toArray(sbs_to_run);
        System.out.println(modified.toString());
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
            qsComp.runQueue();
        } catch(Exception e){
            e.printStackTrace();
        }
        

    }
    protected void destroyArray() {
        stopQueuedScheduling();
        destroyArray(arrayName);
    }


    protected void stopQueuedScheduling(){
        try {
            getMSRef();
            try {
                masterScheduler.stopQueuedScheduler(arrayName);
            } catch(Exception e) {}
            releaseMSRef();
            ctrl_consumer.disconnect();
            container.releaseComponent(qsComp.name());
            //cant set to null coz tab needs them for closing!
            //arrayName = null;
            //schedulerName = null;
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

    public void receive(DestroyedAutomaticArrayEvent e){
        System.out.println("Automatic array destroyed event received for "+e.arrayName);
        if(e.arrayName.equals(arrayName)){
            setArrayStatus("Destroyed");
        }
    }
    
    public void receive(ExecBlockStartedEvent e){
        logger.info("EXEC started event");
        String exec_id = e.execId.entityId;
        String sbid = e.sbId.entityId;
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
    }
            
    public void receive(ExecBlockEndedEvent e){
        logger.info("EXEC ended event");
        String exec_id = e.execId.entityId;
        String sbid = e.sbId.entityId;
        if(!doesSbBelong(sbid)){
            //no match so return
            return;
        }
        SBLite sb = getSBLite(sbid);
        logger.info("SCHEDULING_PANEL: SB("+sbid+")'s exec block("+exec_id+") ended");
        String completion;
        switch(e.status.value()) {
            case 0:
                completion ="FAILED";
                break;
            case 1:
                completion ="SUCCESS";
                break;
            case 2:
                completion ="PARTIAL";
                break;
            case 3:
                completion ="TIMEOUT";
                break;
            default:
                completion ="ERROR";
                break;
            }
            parent.updateExecutionInfo("Execution ended for SB: "+sb.sbName+".\n");
            parent.updateExecutionInfo("Waiting for it to be Archived.\n");
            parent.setSBStatus(sbid, completion);
            //TODO: Set stop buttons to disabled if last SB in queue.
    }
    
    public void receive(ASDMArchivedEvent e){
        //logger.info("asdm archived event");
        String sbid = e.workingDCId.schedBlock.entityId;
        logger.info("SCHEDULING_PANEL: Got asdm archived event for SB("+sbid+")'s ASDM("+e.asdmId.entityId+").");
        String asdmId = e.asdmId.entityId;
        String completion = e.status;
        //System.out.println("got archived event: completion = "+completion);
        boolean belongs = doesSbBelong(sbid);
        if(belongs){
            if(completion.equals("complete")){
                parent.setSBStatus(sbid, "ARCHIVED");
            }
            //set status to ARCHIVED
            SBLite sb = getSBLite(sbid);
            parent.updateExecutionInfo("ASDM archive status for "+sb.sbName+" is "+completion+".\n");
            parent.updateExecutionRow();
        }
    }

}
