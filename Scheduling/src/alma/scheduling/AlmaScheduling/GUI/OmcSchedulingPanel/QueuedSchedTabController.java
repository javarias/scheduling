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

import java.util.logging.Logger;
import alma.scheduling.SBLite;
import alma.scheduling.ProjectLite;
import alma.scheduling.MasterSchedulerIF;
import alma.scheduling.Interactive_PI_to_Scheduling;
import alma.scheduling.Define.*;
import alma.acs.nc.Consumer;

import alma.Control.ExecBlockStartedEvent;
import alma.Control.ExecBlockEndedEvent;
import alma.offline.ASDMArchivedEvent;
import alma.xmlstore.XmlStoreNotificationEvent;
import alma.exec.extension.subsystemplugin.PluginContainerServices;

public class QueuedSchedTabController extends SchedulingPanelController {
    private QueuedSchedTab parent;
    private String arrayName;
    private Thread thread;
    private RunQueuedScheduling run;
    private String[] sbs_to_run;
    private String currentExecId;
    private Consumer consumer = null;
    private Consumer ctrl_consumer = null;

    
    public QueuedSchedTabController(PluginContainerServices cs, QueuedSchedTab p, String a){
        super(cs);
        parent = p;
        arrayName = a;
        try{
            //consumer = new Consumer(alma.xmlstore.CHANNELNAME.value,cs);
            //consumer.addSubscription(XmlStoreNotificationEvent.class, this);
            //consumer.consumerReady();
            ctrl_consumer = new Consumer(alma.Control.CHANNELNAME_CONTROLSYSTEM.value, container);
            ctrl_consumer.addSubscription(alma.Control.ExecBlockEndedEvent.class, this);
            ctrl_consumer.addSubscription(alma.Control.ExecBlockStartedEvent.class, this);
            ctrl_consumer.addSubscription(alma.offline.ASDMArchivedEvent.class, this);
            ctrl_consumer.consumerReady();
        }catch(Exception e){
            e.printStackTrace();
        }    
    }

    public SBLite[] getSBLites(String[] ids){
        getMSRef();
        SBLite[] sbs = masterScheduler.getSBLite(ids);
        releaseMSRef();
        return sbs;
    }

    public void runQueuedScheduling(String[] sb_ids){
        try {
            sbs_to_run = new String[sb_ids.length];
            for(int i=0; i < sb_ids.length; i++){
                sbs_to_run[i] = sb_ids[i];
            }
            run = new RunQueuedScheduling(
                    container, sb_ids, arrayName);
            thread = new Thread(run);
            thread.start();
        } catch(Exception e){
            e.printStackTrace();
        }

    }

    public void stopQueuedScheduling(){
        try {
            getMSRef();
            try {
                run.stop();
            } catch(Exception e){ }
            masterScheduler.destroyArray(arrayName);
            releaseMSRef();
            ctrl_consumer.disconnect();
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

    public void receive(ExecBlockStartedEvent e){
        logger.info("EXEC started event");
        String exec_id = e.execId.entityId;
        String sbid = e.sbId.entityId;
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
