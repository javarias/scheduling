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


import java.util.Vector;
import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;
import alma.acs.util.UTCUtility;

import alma.scheduling.StartSessionEvent;
import alma.scheduling.EndSessionEvent;
import alma.scheduling.NothingCanBeScheduledEvent;
import alma.scheduling.NothingCanBeScheduledEnum;
import alma.scheduling.Event.Publishers.PublishEvent;
import alma.scheduling.Define.SB;
import alma.scheduling.Define.SBQueue;
import alma.scheduling.Define.Status;
import alma.scheduling.Define.ExecBlock;
import alma.scheduling.Define.ControlEvent;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.Project;
import alma.scheduling.Define.Program;
import alma.scheduling.Define.ProjectQueue;
import alma.scheduling.Define.ObservedSession;
import alma.scheduling.Define.SciPipelineRequest;
import alma.scheduling.Define.SchedulingException;
import alma.scheduling.ObsProjectManager.ProjectManager;
import alma.scheduling.ObsProjectManager.ProjectManagerTaskControl;

import alma.entities.commonentity.EntityRefT;
//import alma.entity.xmlbinding.execblock.*;
import alma.entity.xmlbinding.projectstatus.*;
import alma.entity.xmlbinding.projectstatus.types.*;
/**
 *
 * @author Sohaila Lucero
 * @version $Id: ALMAProjectManager.java,v 1.32 2005/03/29 18:11:55 sslucero Exp $
 */
public class ALMAProjectManager extends ProjectManager {
    //The container services
    private ContainerServices containerServices;
    private ALMAArchive archive;
    private SBQueue sbQueue;
    private ProjectQueue pQueue;
    private ProjectStatusQueue psQueue;
    private ALMAPublishEvent publisher;
    private ALMAPipeline pipeline;

    public ALMAProjectManager(ContainerServices cs, ALMAArchive a, SBQueue q, PublishEvent p) {
        super();
        this.containerServices = cs;
        this.logger = cs.getLogger();
        this.publisher =(ALMAPublishEvent)p;
        this.archive = a;
        this.sbQueue = q;
        this.psQueue = new ProjectStatusQueue();
        this.pQueue = new ProjectQueue();
        this.pipeline = new ALMAPipeline(cs);
        pQueue.add(pollArchive());
    }

    /**
     * Calls the run from ProjectManager and then does 
     * some other stuff.
     */
    public void run() {
        super.run();
        while(!stopCommand) {
            try {
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
                if(pQueue.isExists(projs[i].getId())){
                    //don't do anything coz the project already exists in the queue! 
                    logger.info("SCHEDULING: Project already in queue");
                } else {

                    //get the projectStatus of each project. if its in the queue don't map!
                    String psId = projs[i].getProjectStatusId();
                    if(psQueue.isExists(psId)) {
                        logger.info("SCHEDULING: PS already exists in queue. Not Mapping");
                    } else {
                        ProjectStatus ps;
                        try {
                            ps = ProjectUtil.updateProjectStatus(projs[i]);
                            psQueue.add(ps);
                            archive.updateProjectStatus(ps);
                        } catch(Exception e) {
                        //    ps = archive.queryProjectStatus(projs[i].getId());
                        }
                    }
                }
            }
            logger.info("SCHEDULING: Number of PS in queue = "+psQueue.size());
            logger.info("SCHEDULING: Number of projects = "+pQueue.size());
            logger.info("SCHEDULING: Number of projects returned = "+projs.length);
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
      * Log that the session has ended
      */
    public void sessionEnd(String sessionId, String sb_id) {
        logger.info("sb id = "+sb_id);
        String proj_id = (sbQueue.get(sb_id)).getProject().getId();
        logger.info("Proj id= "+proj_id);
        logger.info("SCHEDULING:(session info) Session ("+sessionId+") has ended.");
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
      * 
      * 
      * 
      */
    public void updateProjectStatus(ExecBlock eb) {
        //String[] ids=new String[2]; //ProjectStatus ID and ObsUnitSet partID: temporary for R2
        logger.info ("in PM, ps update");
        SB sb = eb.getParent();
        sb = sbQueue.get(sb.getId());
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
        //ids[0] = ps.getProjectStatusEntity().getEntityId(); //for pipeline back in ALMAReceiveEvent
        logger.info("SCHEDULING: about to update PS::"+ps.getProjectStatusEntity().getEntityId());

        /**
          * For R2 there is the assumption that there is only one SB inside one ObsUnitSet
          * which is inside one ObsProgram.
          * Need something recursive here!
          */
        
        //top level obs unit set which is actually the ObsProgram.
        ObsUnitSetStatusTChoice choice = ps.getObsProgramStatus().getObsUnitSetStatusTChoice(); 
        //ObsUnitSet
        ObsUnitSetStatusT[] sets = choice.getObsUnitSetStatus();
        for(int i=0; i < sets.length; i++) {
            SBStatusT[] sbs = parseObsUnitSetStatus(sets[i]);
        }
        try {
            psQueue.updateProjectStatus(ps);
            archive.updateProjectStatus(ps);
        } catch(Exception e) {
            logger.severe("SCHEDULING: Could not update project status in archive!");
            e.printStackTrace();
        }
        //return ids;
    }


    /*
    public SBStatusT[] parseObsUnitStatusT(ObsUnitSetStatusT unit) {
        ObsUnitSetStatusTChoice choice = unit.getObsUnitSetStatusTChoice();
        ObsUnitSetStatusT[] sets = choice.getObsUnitSetStatus();
        for(int i=0; i<sets.length; i++){
            if(sets[i] instanceof SBStatusT[]) {
            }
        }
    }
    */


     //TODO: Rename this method.
    public SBStatusT[] parseObsUnitSetStatus(ObsUnitSetStatusT set) {
        logger.info("SCHEDULING: Set PartID = "+set.getEntityPartId());
        SBStatusT[] sbs = null;
        ObsUnitSetStatusT[] obs = null;
        if(set.getObsUnitSetStatusTChoice().getObsUnitSetStatusCount() > 0) {
            logger.info("SCHEDULING: more than one obs unit set status in PS");
            obs = set.getObsUnitSetStatusTChoice().getObsUnitSetStatus();
            for(int i=0; i< obs.length; i++) {
                sbs = parseObsUnitSetStatus(obs[i]);
            }
        }
        if(set.getObsUnitSetStatusTChoice().getSBStatusCount() > 0) {
            sbs = set.getObsUnitSetStatusTChoice().getSBStatus();
        }
        return sbs;
    }

    /**
      *
      */
    public void publishNothingCanBeScheduled(NothingCanBeScheduledEnum reason){
        NothingCanBeScheduledEvent event = new NothingCanBeScheduledEvent(
                reason, (new DateTime(System.currentTimeMillis())).toString(), "");
        publisher.publish(event);
    }


    /**
     * Updates the scheduling block with the info gotten from the control
     * event. If the SB is complete
     */
    public void updateSB(ControlEvent e) {
        try {
            archive.updateSB(e);
        }catch(SchedulingException ex) {
            logger.severe("SCHEDULING: error updating sb");
            ex.printStackTrace();
        }
    }

    /*private Project addProgram(Program p) {
        // get program's parent. If it doesnt have a parent its the project then add program 
        // and return project but if its another program add it to the program and 
        // call this function again with the new program
        Project proj=null;
        Object parent = p.getParent();
        if(parent instanceof Program) {
            //its parent is another program, add to program and try again
            System.out.println("Its not the project. Another program.");
            ((Program)parent).updateMember(p);
            addProgram((Program)parent);
        } 
        //its parent is the top level project. add it to the project and 
        //return the project.
        System.out.println("Its the top project. Setting program and returning project.");
        proj = p.getProject();
        proj.setProgram(p);
        return proj;
    }*/

    
    private Program addProgram(Program p) {
        Program parent = p.getParent();
        if(parent != null) {
            parent.updateMember(addProgram(parent));
            //addProgram(parent);
        }
        return p;
        
    }

    /**
      * Creates an Observed session and maps it to the ProjectStatus. The ProjectStatus then 
      * gets updated in the archive. 
      */
    public ObservedSession createObservedSession(Program p, ExecBlock eb) {

        ObservedSession session = new ObservedSession();
        session.setSessionId(ProjectUtil.genPartId());
        session.setProgram(p);
        session.setStartTime(new DateTime(System.currentTimeMillis()));
        //System.out.println("EXEC BLOCK: eb id ="+eb.getId());
        session.addExec(eb);
        p.addObservedSession(session);
        
        //Project proj = pQueue.get(p.getProject().getId());
        Program prog =  addProgram(p);
        Project proj = prog.getProject();
        if(proj == null) {
            System.out.println("program was null!!!");
        }
        ProjectStatus ps = psQueue.getStatusFromProjectId(proj.getId());
        try {
            //ps = ProjectUtil.map(proj, new DateTime(System.currentTimeMillis()));
            ps = ProjectUtil.updateProjectStatus(proj);
            psQueue.updateProjectStatus(ps);
            archive.updateProjectStatus(ps);
        } catch(SchedulingException e) {
            logger.severe("SCHEDULING: error mapping PS with Session");
            e.printStackTrace();
        }
        return session;
    }

    /**
      * Updates the observng session information.
      */
    public void updateObservedSession(Project p, ProjectStatus ps, String sessionId, String endTime){
        logger.info("SCHEDULING: updating session with end time.");
        try {
            ObservedSession[] allSes = p.getProgram().getAllSession();
            ObservedSession ses=null;
            for(int i=0; i < allSes.length; i++){
                if(allSes[i].getSessionId() == sessionId){
                    ses = allSes[i];
                    ses.setEndTime(new DateTime(endTime));
                }
            }
           // ps = ProjectUtil.map(p, new DateTime(System.currentTimeMillis()));
            ps = ProjectUtil.updateProjectStatus(p);
            psQueue.updateProjectStatus(ps);
            archive.updateProjectStatus(ps);
        } catch(Exception e){
            logger.severe("SCHEDULING: error updating PS with session");
            e.printStackTrace();
        }
    }
    /* Will be this way in future
    public void sendStartSessionEvent(ObservedSession session) {
    }
    */
    public void sendStartSessionEvent(ExecBlock eb) {
        String sbid = ((SB)eb.getParent()).getId();
        SB sb = sbQueue.get(sbid);
        //in future will be done in scheduler.
        ObservedSession session = createObservedSession(sb.getParent(),eb);
        //session.addExec(eb);
        sessionStart(session.getSessionId(), sbid);
        logger.info("SCHEDULING: Session id == "+session.getSessionId());
        try {
            long time = UTCUtility.utcJavaToOmg(System.currentTimeMillis());
            StartSessionEvent start_event = new StartSessionEvent(
                    UTCUtility.utcJavaToOmg(System.currentTimeMillis()),
                    session.getSessionId(),
                    session.getProgram().getId(),
                    sbid,
                    eb.getId());
                    
            publisher.publish(start_event);
        } catch(Exception e) {
            logger.severe("SCHEDULING: Failed to send start session event!");
            e.printStackTrace();
        }
    }


    /* will change eventually to this
    public void sendEndSessionEvent(ObservedSession session) {
    }
    */
    public void sendEndSessionEvent(ExecBlock eb) {
        
        String endTime = (new DateTime(System.currentTimeMillis())).toString();
        String execid = eb.getExecId();
        String sbid = ((SB)eb.getParent()).getId();
        SB sb = sbQueue.get(sbid);
        Project proj = (Project)sb.getProject();
        String projectid = proj.getId();
        ProjectStatus ps = psQueue.getStatusFromProjectId(projectid);
        ObsUnitSetStatusT obsProgram = ps.getObsProgramStatus();
        
        SessionT session = getSession(eb);
        logger.info("SCHEDULING: session found!");
        session.setEndTime(endTime);
        logger.info("SCHEDULING: sbid = " +sbid);
        logger.info("SCHEDULING: session part id = "+session.getEntityPartId());
        sessionEnd(session.getEntityPartId(), sbid);
        updateObservedSession(proj, ps, session.getEntityPartId(), endTime);
        try {
            EndSessionEvent end_event = new EndSessionEvent(
                    UTCUtility.utcJavaToOmg(System.currentTimeMillis()),
                    session.getEntityPartId(),
                    obsProgram.getEntityPartId(),
                    execid);
            publisher.publish(end_event);
        } catch(Exception e) {
            logger.severe("SCHEDULING: Failed to send end session event!");
            e.printStackTrace();
        }
    }

    private SessionT getSession(ExecBlock eb) {
        boolean gotSession = false;
        String endTime = (new DateTime(System.currentTimeMillis())).toString();
        String execid = eb.getExecId();
        String sbid = ((SB)eb.getParent()).getId();
        SB sb = sbQueue.get(sbid);
        Project proj = (Project)sb.getProject();
        String projectid = proj.getId();
        ProjectStatus ps = psQueue.getStatusFromProjectId(projectid);
        ObsUnitSetStatusT obsProgram = ps.getObsProgramStatus();
        ObsUnitSetStatusT set = searchSets(obsProgram.getObsUnitSetStatusTChoice().getObsUnitSetStatus(), execid);
        SessionT[] sessions = set.getSession();
        logger.info("SCHEDULING: in PM getSession, length = "+sessions.length);
        if(sessions.length != 0) {//if this is the wrong set of sessions i screwed up..
            gotSession = sessionExists(eb, sessions);
            if(gotSession) {
                return retrieveSession(eb, sessions);
            }
        }
        return null;
        
    }
    
    /**
      * Recursive search of the program to find the obs unit set that 
      * contains the session we want.
      */
    private ObsUnitSetStatusT searchSets(ObsUnitSetStatusT[] sets, String ebId) {
        ObsUnitSetStatusT set=null;
        SessionT[] sessions;
        for(int i=0; i < sets.length; i++){
            sessions = sets[i].getSession();
            for(int j=0; j < sessions.length;j++){
                if( isSession(sessions[j], ebId) ) {
                    return sets[i];
                }
            }
            //session wasn't in those sets so lets get some more
            set = searchSets(sets[i].getObsUnitSetStatusTChoice().getObsUnitSetStatus(), ebId);
        }
        return set;
    }
    
    private boolean isSession(SessionT ses, String ebid) {
        boolean result=false;
        ExecBlockRefT[] execblocks = ses.getExecBlockRef();
        for(int i=0; i < execblocks.length; i++){
            if (execblocks[i].getExecBlockId().equals(ebid)){
                System.out.println("Session found! returning true");
                return true;
            }
        }
        return result;
    }


   /**
      * Returns true if a session in this group is the one associated with
      * this exec block!
      */
    private boolean sessionExists(ExecBlock eb, SessionT[] all) {
        boolean result=false;
        String execid = eb.getExecId();
        for(int i=0; i < all.length; i++) {
            ExecBlockRefT[] execblocks = all[i].getExecBlockRef();
            for(int j=0; j < execblocks.length; j++){
                if (execblocks[j].getExecBlockId().equals(execid)){
                    System.out.println("Session found! returning true");
                    return true;
                }
            }
            
        }
        return result;
    }

    /**
      * This function should NEVER return null...
      */
    private SessionT retrieveSession(ExecBlock eb, SessionT[] all){
        SessionT session=null;
        String execid = eb.getExecId();
        for(int i=0; i < all.length; i++) {
            session = all[i];
            ExecBlockRefT[] execblocks = session.getExecBlockRef();
            for(int j=0; j < execblocks.length; j++){
                if (execblocks[j].getExecBlockId().equals(execid)){
                    System.out.println("Session found! returning session");
                    return session;
                }
            }
            
        }
        return session;
    }
    
    /**
      * Creates a SciPipelineRequest with the given program and comment string.
      * @param Program The program that the science pipeline request belongs to.
      * @param s A comment about the science pipeline request
      * @return SciPipelineRequest
      */
    //public SciPipelineRequest createSciPipelineRequest(Program p, String s)
    public SciPipelineRequest createSciPipelineRequest(String sbid, String s)
        throws SchedulingException {

        //use sbid to get the program 
        System.out.println("Creating PPR in PM");
        SB sb = sbQueue.get(sbid);
        Program prog = sb.getParent();
        System.out.println("sb parent's part id = "+prog.getObsUnitSetStatusId());
        SciPipelineRequest ppr = new SciPipelineRequest(prog, s);
 		ppr.setReady(ProjectUtil.genPartId(), new DateTime(System.currentTimeMillis()));
        ppr.setStarted(new DateTime(System.currentTimeMillis()));
        prog.setSciPipelineRequest(ppr);
        Program prog2 = addProgram(prog);
        Project proj = prog2.getProject();
        //Project proj = pQueue.get(prog.getProject().getId());
        //proj.setProgram(prog2);
        ProjectStatus ps = psQueue.getStatusFromProjectId(proj.getId());
        try {
            //ps = ProjectUtil.map(proj, new DateTime(System.currentTimeMillis()));
            ps = ProjectUtil.updateProjectStatus(proj);
            psQueue.updateProjectStatus(ps);
            archive.updateProjectStatus(ps);
        } catch(SchedulingException e) {
            logger.severe("SCHEDULING: error mapping PS with PPR");
            e.printStackTrace();
        }
        return ppr;
    }

    public void startPipeline(SciPipelineRequest ppr) throws SchedulingException {
        //get PS that contains this ppr.
        Program prog = ppr.getProgram();
        Project proj = prog.getProject();
        ProjectStatus ps = psQueue.getStatusFromProjectId(proj.getId());
        //archive.updateProjectStatus(ps);

        logger.info("SCHEDULING: Starting Pipeline");
        String pprString = archive.getPPRString(ps, ppr.getId());
        logger.info("SCHEDULING: (in PM) PPR string =  "+pprString);
        String pipelineResult = pipeline.processRequest(pprString);
    }

    public void managerStopped() {
        try {
            pipeline.releasePipelineComp();
        } catch(Exception e){
            logger.severe("SCHEDULING: error releasing pipeline comp from PM");
            e.printStackTrace();
        }
    }

}
