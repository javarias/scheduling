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
 * File ALMAProjectManager.java
 * 
 */
package alma.scheduling.AlmaScheduling;

//import java.util.logging.Logger;

import java.util.Vector;
import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;

import alma.scheduling.NothingCanBeScheduledEvent;
import alma.scheduling.NothingCanBeScheduledEnum;
import alma.scheduling.Event.Publishers.PublishEvent;
import alma.scheduling.Define.SB;
import alma.scheduling.Define.SBQueue;
import alma.scheduling.Define.Status;
import alma.scheduling.Define.ExecBlock;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.Project;
import alma.scheduling.Define.Program;
import alma.scheduling.Define.ProjectQueue;
import alma.scheduling.ObsProjectManager.ProjectManager;
import alma.scheduling.ObsProjectManager.ProjectManagerTaskControl;

import alma.entities.commonentity.EntityRefT;
import alma.entity.xmlbinding.projectstatus.*;
import alma.entity.xmlbinding.projectstatus.types.*;
/**
 *
 * @author Sohaila Lucero
 */
public class ALMAProjectManager extends ProjectManager {
    //The container services
    private ContainerServices containerServices;
    private ALMAArchive archive;
    private SBQueue sbQueue;
    private ProjectQueue pQueue;
    private ProjectStatusQueue psQueue;
    private ALMAPublishEvent publisher;

    public ALMAProjectManager(ContainerServices cs, ALMAArchive a, SBQueue q, PublishEvent p) {
        super();
        this.containerServices = cs;
        this.logger = cs.getLogger();
        this.publisher =(ALMAPublishEvent)p;
        this.archive = a;
        this.sbQueue = q;
        this.psQueue = new ProjectStatusQueue();
        this.pQueue = new ProjectQueue(pollArchive());
    }

    /**
     * Calls the run from ProjectManager and then does 
     * some other stuff.
     */
    public void run() {
        super.run();
        while(!stopCommand) {
            try {
                //Thread.sleep(5*60*1000);
                Thread.sleep(60*5000);
            }catch(InterruptedException e) {
                logger.info("SCHEDULING: ProjectManager Interrupted!");
            }
            if(!stopCommand){
                pollArchive();
            }
        }
    }

    /** 
     * Check the archive periodically to see if there have been any additional
     * scheduling blocks added. This function will eventually poll for new 
     * projects.
     *
     */
    private Project[] pollArchive() {
        logger.info("SCHEDULING: Polling ARCHIVE");
        boolean sb_present = false;
        Project[] projs = new Project[0];
        try {
            String[] ids;
            logger.info("SCHEDULING: getting projects");
            projs = archive.getAllProject();
            logger.info("SCHEDULING: getting projectstatus'");
            for(int i=0; i < projs.length;i++){
                ProjectStatus ps;
                try {
                    ps = ProjectUtil.map(projs[i], new DateTime(System.currentTimeMillis()));
                } catch(Exception e) {
                    ps = archive.queryProjectStatus(projs[i].getId());
                }
                //check if its in queue, if it is add it, if not don't or update.
                if(psQueue.size() == 0) { //nothing in queue so ps definitely isnt in it.
                    psQueue.add(ps);
                    archive.updateProjectStatus(ps);
                }
                for(int k=0; k < psQueue.size(); k++){
                    ProjectStatus ps_tmp = null;
                    if( (ps_tmp = psQueue.get(ps.getProjectStatusEntity().getEntityId())) == null) {
                        logger.info ("PS not in queue, adding..");
                        psQueue.add(ps);
                        archive.updateProjectStatus(ps);
                    } else {
                        logger.info ("ps already in queue not adding..");
                    }
                }
            }
            logger.info("SCHEDULING: Number of PS in queue = "+psQueue.size());
            logger.info("SCHEDULING: Number of projects = "+projs.length);
            for(int i=0; i < projs.length;i++) {
                logger.info("SCHEDULING: sched blocks in Project ( "+projs[i].getId()+" ) = "+
                        projs[i].getTotalSBs());
                SB[] sbs = projs[i].getAllSBs();    
                for(int j=0; j < sbs.length; j++){
                    ids = sbQueue.getAllIds();
                    String tmp_id = sbs[j].getId();
                    if(ids.length == 0) { //nothing in the SBQueue
                        System.out.println("SCHEDULING: id's length == 0");
                        sb_present = false;
                    }
                    for(int k=0; k < ids.length; k++) {
                        logger.info("SCHEDULING: DOES "+tmp_id+" == "+ids[k]);
                        if(tmp_id.equals(ids[k])) { 
                            //SB already in queue
                            sb_present = true;
                            break;
                        } else {
                            //SB not in queue
                            sb_present = false;
                        }
                    }
                    if(!sb_present){
                        System.out.println("SCHEDULING: sbpresent false; adding "+tmp_id +" to queue");
                        sbQueue.add(sbs[j]);
                    }
                }
            }
        }catch(Exception e) {
            logger.severe("SCHEDULING: Error polling archive: "+e.toString());
            e.printStackTrace();
        }
        return projs;
    }


    /**
      *
      */
    public void sessionStart(String sessionId, String sb_id) {
        String proj_id = (sbQueue.get(sb_id)).getProject().getId();
        logger.info("SCHEDULING:(session info) Session ("+sessionId+") has started.");
        logger.info("SCHEDULING:(session info) Project id = "+proj_id+".");
        logger.info("SCHEDULING:(session info) SB id = "+sb_id+".");
    }

    /**
      *
      */
    public void sessionEnd(String sb_id) {
        logger.info("sb id = "+sb_id);
        String proj_id = (sbQueue.get(sb_id)).getProject().getId();
        logger.info("Proj id= "+proj_id);
        logger.info("SCHEDULING:(session info) Session has ended.");
        logger.info("SCHEDULING:(session info) Project id = "+proj_id+".");
        logger.info("SCHEDULING:(session info) SB id = "+sb_id+".");
    }

    /**
      *
      */
    public void setSBComplete(ExecBlock eb) {
        SB completed = sbQueue.get(eb.getParent().getId());
        eb.setParent(completed);// replaced its sb-parent so exec block has full sb
        completed.execEnd(eb,eb.getStatus().getEndTime(), Status.COMPLETE);
        logger.info("SCHEDULING: sb status = "+completed.getStatus().getStatus());
    }

    /**
      *
      */
    public void removeCompletedProjectFromQueue(String proj_id){
    }


    
    /**
      * Returns two strings. Ons is the id of the ProjectStatus and the second is
      * the id of the obs unit set that just completed.
      * NOTE: this is temporary
      */
    public String[] updateProjectStatus(ExecBlock eb) {
        String[] ids=new String[2]; //ProjectStatus ID and ObsUnitSet partID: temporary for R2
        logger.info ("in PM, ps update");
        SB sb = eb.getParent();
        logger.info("SCHEDULING: SB id = " +sb.getId());
        String proj_id = sb.getProject().getId();
        logger.info("SCHEDULING: project id = " +proj_id);
        
        ProjectStatus[] allPS = psQueue.getAll();
        ProjectStatus ps = null;
        for(int i=0; i < allPS.length; i++){
            if(allPS[i].getObsProjectRef().getEntityId().equals(proj_id)) {
                ps = allPS[i];
                break;
            }
        }
        ids[0] = ps.getProjectStatusEntity().getEntityId(); //for pipeline back in ALMAReceiveEvent
        logger.info("SCHEDULING: about to update PS::"+ps.getProjectStatusEntity().getEntityId());

        /**
          * For R2 there is the assumption that there is only one SB inside one ObsUnitSet
          * which is inside one ObsProgram.
          */
        
        //top level obs unit set which is actually the ObsProgram.
        ObsUnitSetStatusTChoice choice = ps.getObsProgramStatus().getObsUnitSetStatusTChoice(); 
        //ObsUnitSet
        ObsUnitSetStatusT[] sets = choice.getObsUnitSet();
        for(int i=0; i< sets.length; i++){
            System.out.println("ObsUnitSet part id = "+sets[i].getEntityPartId());
            SBStatusT[] sbs = sets[i].getObsUnitSetStatusTChoice().getSB();
            for(int j=0; j<sbs.length; j++) {
                if(sbs[j].getSBRef().getEntityId().equals (sb.getId())) {
                    //this is the sb which just finished!
                    sbs[j].getStatus().setState(StateType.COMPLETE);
                    //set obs unit set to complete too
                    sets[i].getStatus().setState(StateType.COMPLETE);
                    sets[i].setNumberSBSCompleted(1);
                    sets[i].setNumberObsUnitSetsCompleted(1);
                    ids[1] =  sets[i].getEntityPartId(); //for pipeline back in ALMAReceiveEvent
                    //add exec block ref
                    ExecStatusT exec = new ExecStatusT();
                    EntityRefT e_ref = new EntityRefT();
                    e_ref.setEntityId(eb.getId());
                    exec.setExecBlockRef(e_ref);
                    exec.setSubarrayId(eb.getSubarrayId());
                    /*
                    if(eb.getStatus().isComplete()) {
                        exec.setStatus.setState(StateType.COMPLETE);
                    }
                    */
                    ExecStatusT[] execs = new ExecStatusT[1];
                    execs[0] = exec;
                    SBStatusTSequence seq = new SBStatusTSequence();
                    seq.setExecStatus(execs);
                    sbs[j].setSBStatusTSequence(seq);
                    break;
                }
            }
        }
        try {
            archive.updateProjectStatus(ps);
        } catch(Exception e) {
        }
        return ids;
    }

    /**
      *
      */
    public void publishNothingCanBeScheduled(NothingCanBeScheduledEnum reason){
        NothingCanBeScheduledEvent event = new NothingCanBeScheduledEvent(
                reason, (new DateTime(System.currentTimeMillis())).toString(), "");
        publisher.publish(event);
    }

    
}
