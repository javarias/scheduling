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
import java.util.ArrayList;
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
import alma.entity.xmlbinding.specialsb.*;
import alma.entity.xmlbinding.projectstatus.*;
import alma.entity.xmlbinding.projectstatus.types.*;
//import alma.asdmIDLTypes.IDLEntityRef;
/**
 *
 * @author Sohaila Lucero
 * @version $Id: ALMAProjectManager.java,v 1.59 2006/05/01 14:13:25 sslucero Exp $
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
    private ALMAOperator oper;
    //TODO temporary
    private Vector specialSBs;
    private ALMAClock clock;

    public ALMAProjectManager(ContainerServices cs, 
                              ALMAOperator o, 
                              ALMAArchive a, 
                              SBQueue q, 
                              PublishEvent p, 
                              ALMAClock c) {
        super();
        this.containerServices = cs;
        this.logger = cs.getLogger();
        this.publisher =(ALMAPublishEvent)p;
        this.oper = o;
        this.archive = a;
        this.sbQueue = q;
        this.psQueue = new ProjectStatusQueue();
        this.pQueue = new ProjectQueue();
        this.pipeline = new ALMAPipeline(cs);
        this.clock = c;
        pQueue = new ProjectQueue();
        psQueue = new ProjectStatusQueue();
        sbQueue = new SBQueue();
        specialSBs = new Vector();
        try  {
            pollArchive();
            querySpecialSBs();
        } catch(Exception e) {
        }
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
                try {
                    pollArchive();
                    querySpecialSBs();
                } catch(Exception e) {}
            }
        }
    }

    private void querySpecialSBs(){
        boolean sbPresent = false;
        try {
            SpecialSB[] tmp = archive.querySpecialSBs();
            for(int i=0; i < tmp.length; i++){
                
                for(int j=0; j < specialSBs.size(); j++){
                    
                    if(tmp[i].getSpecialSBEntity().getEntityId().equals(
                         ((SpecialSB)specialSBs.elementAt(j)).getSpecialSBEntity().getEntityId())) {
                        sbPresent = true;
                        break;
                    } 
                }
                
                if(!sbPresent) {
                    //add it.
                    specialSBs.add(tmp[i]);
                }

            }
        } catch(Exception e) {
            e.printStackTrace(System.out);
        }
        logger.info("# of special sbs = "+specialSBs.size());
    }

    public Vector getSpecialSBs() {
        return specialSBs;
    }

    public void checkForProjectUpdates() {
        try {
            pollArchive();
        } catch(Exception e) {
        }
    }

    /** 
     * Check the archive periodically to see if there have been any additional
     * scheduling blocks added. This function will eventually poll for new 
     * projects.
     *
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
                    //actually check if the timestamps are the same, if they're different
                    //and the new project's time is greater than the one in the queue
                    //replace the one in the queue.. NOTE: will also have to tell the sbQueue
                    //to update somehow..
                    
                    logger.info("SCHEDULING: Project already in queue, checking if its been updated");
                    DateTime old_proj_time = pQueue.get(projs[i].getId()).getTimeOfUpdate();
                    DateTime new_proj_time = projs[i].getTimeOfUpdate();
                    if(old_proj_time.compareTo(new_proj_time) == 0) {
                        logger.info("times are equal!");

                    } else if(old_proj_time.compareTo(new_proj_time) == 1) {
                        logger.info("this project has been updated!");
                        //pQueue.replace(ProjectUtil.
                    } else { // the update time is less than the orig. == problem..
                        logger.severe("this should not have happened...");

                    }
                    //TODO: update project status too
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
                        logger.info("SCHEDULING: id's length == 0");
                        sb_present = false;
                    }
                    for(int k=0; k < ids.length; k++) {
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
                        logger.info("SCHEDULING: sbpresent false; adding "+tmp_id +" to queue");
                        sbQueue.add(sbs[j]);
                    }
                }
            }
        }catch(Exception e) {
            logger.severe("SCHEDULING: Error polling archive: "+e.toString());
            e.printStackTrace(System.out);
        }
        return projs;
    }
     */
    
    /**
      * For Scheduling an ordered list of sbs we still need to map the to their projects
      * so all the correct information gets into the SB objects.
      * So this function basically creates a sbqueue with only the sbs in the list
      * and maps only those sbs to projects.
      */
    public SBQueue mapQueuedSBsToProjects(String[] sbs) {
        SBQueue queuedSBs= new SBQueue();
        for (int i=0; i < sbs.length; i++){
            queuedSBs.add(sbQueue.get(sbs[i]));
        }
        return queuedSBs;
    }
    
    public ProjectQueue getProjectQueue(){
        return pQueue;
    }

    public Project getProject(String id) throws SchedulingException {
        return pQueue.get(id);
    }
    public Project getProjectFromQueue(String id) throws SchedulingException {
        return pQueue.get(id);
    }

    /**
      *
      * Log that the session has started and send a message to the Operator
      */
    public void sessionStart(String sessionId, String sb_id) {
        String proj_id = (sbQueue.get(sb_id)).getProject().getId();
        logger.info("SCHEDULING:(session info) Session ("+sessionId+") has started.");
        logger.info("SCHEDULING:(session info) Project id = "+proj_id+".");
        logger.info("SCHEDULING:(session info) SB id = "+sb_id+".");
        //send message to operator
        //oper.send("Session ("+sessionId+") has started for Sb ("+sb_id+").");
        
    }

    /**
      * Log that the session has ended and send a message to the Operator
      */
    public void sessionEnd(String sessionId, String sb_id) {
        logger.info("sb id = "+sb_id);
        String proj_id = (sbQueue.get(sb_id)).getProject().getId();
        logger.info("Proj id= "+proj_id);
        logger.info("SCHEDULING:(session info) Session ("+sessionId+") has ended.");
        logger.info("SCHEDULING:(session info) Project id = "+proj_id+".");
        logger.info("SCHEDULING:(session info) SB id = "+sb_id+".");
        //send message to operator
        //oper.send("Session ("+sessionId+") has ended for Sb ("+sb_id+").");
    }

    /**
      * An SB has completed an execution. Check that its repeat count is met and if so 
      * its status to complete. If not set it back to ready.
      */
    public void setSBComplete(ExecBlock eb) {
        SB completed = sbQueue.get(eb.getParent().getId());
        eb.setParent(completed);// replaced its sb-parent so exec block has full sb
        logger.info("##########################");
        logger.info("eb ("+eb.getId()+") has start time = "+eb.getStatus().getStartTime());
        logger.info("sb's status in PM = "+completed.getStatus().getStatus());
        logger.info("sb's starttime in PM = "+completed.getStatus().getStartTime());
        logger.info("##########################");
	    //If this SB has reached its maximum number of repeats set it to complete.
        if( (completed.getNumberExec() +1) > completed.getMaximumNumberOfRepeats()  ){
            logger.info("###########set to complete####");
            logger.info("Setting end time for "+eb.getId());
            logger.info(" # exec = "+completed.getNumberExec());
            logger.info(" # repeats = "+completed.getMaximumNumberOfRepeats());
            logger.info("#################################");
            completed.execEnd(eb,eb.getStatus().getEndTime(), Status.COMPLETE);
        } else { //set it to ready
            logger.info("##########set to ready###########");
            logger.info("Setting end time for "+eb.getId());
            logger.info(" # exec = "+completed.getNumberExec());
            logger.info(" # repeats = "+completed.getMaximumNumberOfRepeats());
            logger.info("#################################");
            completed.execEnd(eb,eb.getStatus().getEndTime(), Status.READY);
        }
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
            e.printStackTrace(System.out);
        }
        //return ids;
    }


 
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
            ex.printStackTrace(System.out);
        }
    }

    
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
      *
      * Gets called from ALMAReceiveEvent
      */
    public void createObservedSession(ExecBlock eb) {

        String sbid = eb.getParent().getId();
        logger.info("EB's parent id = "+sbid);
        Program p = ((SB)sbQueue.get(sbid)).getParent();
        ObservedSession session = new ObservedSession();
        session.setSessionId(eb.getSessionId());
        session.setProgram(p);
        session.setStartTime(new DateTime(System.currentTimeMillis()));
        //System.out.println("EXEC BLOCK: eb id ="+eb.getId());
        session.addExec(eb);
        p.addObservedSession(session);
        
        //Project proj = pQueue.get(p.getProject().getId());
        Program prog =  addProgram(p);
        Project proj = prog.getProject();
        if(proj == null) {
            logger.info("SCHEDULING: project was null!!!"); //should never happen.
            //throw new Exception("SCHEDULING: Error with project structure!"); TODO Add this eventually
        }
        ProjectStatus ps = psQueue.getStatusFromProjectId(proj.getId());
        try {
            ps = ProjectUtil.updateProjectStatus(proj);
            psQueue.updateProjectStatus(ps);
            archive.updateProjectStatus(ps);
        } catch(SchedulingException e) {
            logger.severe("SCHEDULING: error mapping PS with Session");
            e.printStackTrace(System.out);
        }
        //return session;
    }

    /**
      * Updates the observng session information.
      */
    public void updateObservedSession(Project p, ProjectStatus ps, String sessionId, String endTime){
        logger.info("SCHEDULING: updating session with end time.");
        try {
            ObservedSession[] allSes = searchPrograms(p.getProgram(), sessionId).getAllSession();

            ObservedSession ses=null;
            for(int i=0; i < allSes.length; i++){
                if(allSes[i].getSessionId().equals(sessionId)){
                    ses = allSes[i];
                    ses.setEndTime(new DateTime(endTime));
                }
            }
            ps = ProjectUtil.updateProjectStatus(p);
            psQueue.updateProjectStatus(ps);
            archive.updateProjectStatus(ps);
        } catch(Exception e){
            logger.severe("SCHEDULING: error updating PS with session");
            e.printStackTrace(System.out);
        }
    }

    private Program searchPrograms(Program p, String sessionId) {
        ObservedSession[] sessions = p.getAllSession();
        for(int i=0; i < sessions.length; i++){
            if( sessions[i].getSessionId().equals(sessionId) ){
                return p;
            }
        }
        Program[] allPrograms = p.getAllPrograms(); 
        Program prog = null;
        for(int i=0;i<allPrograms.length; i++){
            prog = searchPrograms(allPrograms[i], sessionId);
        }
        return prog;
    }
    
    
    /* Will be this way in future
    public void sendStartSessionEvent(ObservedSession session) {
    }
    */
    public String sendStartSessionEvent(String sbid) {
        SB sb = sbQueue.get(sbid);
        //in future will be done in scheduler.
        //ObservedSession session = createObservedSession(sb.getParent(),eb);
        //session.addExec(eb);
        String sessionId = new String(ProjectUtil.genPartId());
        sessionStart(sessionId, sbid);
        /*
        IDLEntityRef sessionRef = new IDLEntityRef();
        sessionRef.entityId = sb.getProject().getProjectStatusId();
        sessionRef.partId = sessionId;
        sessionRef.entityTypeName = "ProjectStatus";
        sessionRef.instanceVersion = "1.0";
        */
        logger.info("SCHEDULING: Session id == "+sessionId);
        try {
            long time = UTCUtility.utcJavaToOmg(System.currentTimeMillis());
            StartSessionEvent start_event = new StartSessionEvent(
                    UTCUtility.utcJavaToOmg(System.currentTimeMillis()),
                    sessionId,
                    sb.getParent().getId(),
                    sbid);
                    
            publisher.publish(start_event);
        } catch(Exception e) {
            logger.severe("SCHEDULING: Failed to send start session event!");
            e.printStackTrace(System.out);
        }
        return sessionId;
    }


    /* will change eventually to this
    public void sendEndSessionEvent(ObservedSession session) {
    }
    */
    public void sendEndSessionEvent(ExecBlock eb) {
        
        String endTime = (new DateTime(System.currentTimeMillis())).toString();
        //String execid = eb.getExecId();
        String sbid = ((SB)eb.getParent()).getId();
        SB sb = sbQueue.get(sbid);
        ExecBlock[] allExecs = sb.getExec();
        String[] allExecIds = new String[allExecs.length];
        for(int i=0; i < allExecs.length; i++){
            allExecIds[i] = allExecs[i].getExecId();
        }
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
                    allExecIds);
            publisher.publish(end_event);
        } catch(Exception e) {
            logger.severe("SCHEDULING: Failed to send end session event!");
            e.printStackTrace(System.out);
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
        //ObsUnitSetStatusT set = searchSets(obsProgram.getObsUnitSetStatusTChoice().getObsUnitSetStatus(), execid);
        ObsUnitSetStatusT[] tmp = new ObsUnitSetStatusT[1];
        tmp[0] = obsProgram;
        ObsUnitSetStatusT set = searchSets(tmp, execid);
        if(set == null) {
            logger.severe("SCHEDULING: PM: returned set is null! (looking for session)");
        }
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
                logger.info("SCHEDULING: Session found! returning true");
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
                    logger.info("SCHEDULING: Session found! returning true");
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
                    logger.info("SCHEDULING: Session found! returning session");
                    return session;
                }
            }
            
        }
        return session;
    }


    /**
      *
      */
    public boolean isPipelineNeeded(String sbid) {
        boolean needed = false;
        SB sb = sbQueue.get(sbid);
        Program prog = sb.getParent();
        if(prog.getDataReductionProcedureName() == null || 
                prog.getDataReductionProcedureName().equals("") ) {
            needed = false;
        } else {
            needed = true;
        }
        return needed;
    }
    
    /**
      * Creates a SciPipelineRequest with the given program and comment string.
      * @param Program The program that the science pipeline request belongs to.
      * @param s A comment about the science pipeline request
      * @return SciPipelineRequest
      */
    public SciPipelineRequest createSciPipelineRequest(String sbid, String s)
        throws SchedulingException {

        //use sbid to get the program 
        logger.info("SCHEDULING: Creating PPR in PM");
        SB sb = sbQueue.get(sbid);
        Program prog = sb.getParent();
        //System.out.println("sb parent's part id = "+prog.getObsUnitSetStatusId());
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
            ps = ProjectUtil.updateProjectStatus(proj);
            psQueue.updateProjectStatus(ps);
            archive.updateProjectStatus(ps);
        } catch(SchedulingException e) {
            logger.severe("SCHEDULING: error mapping PS with PPR");
            e.printStackTrace(System.out);
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
            e.printStackTrace(System.out);
        }
    }

    /**
      * Compares the SBs in project 1 with project 2.
      * If the sbs in either are different the result is false.
      * If the sbs are all the same the result is true.
      * @param p1 Project 1, the new project!
      * @param p2 Project 2, the existing project!
      * @return boolean True if all the same, false if different.
      */
    public boolean compareSBs(Project p1, Project p2) {
        boolean res = false;
        SB[] sb1 = p1.getAllSBs();
        SB[] sb2 = p2.getAllSBs();
        if(sb1.length != sb2.length) {
            logger.info("SCHEDULING: Comparing sb lists. Size is different so false return"
                    + sb1.length +" : "+ sb2.length);
            return false;
        }
        // Always starting with a false result... If the sb1[i] is not in
        // sb2 then lists are not the same. If sb1[i] is in there check
        // the next item in sb1 through all of sb2.
        for(int i=0; i < sb1.length; i++){ //call this 'i' loop
            res = false;
            for(int j=0; j < sb2.length; j++){ // call this'j' loop
                if( sb1[i].getId().equals(sb2[j].getId()) ) {
                    res = true;
                    break;
                }
            }
            if(!res) break; //out of 'j' loop
        }
        return res; 
    }

    /*
      * @param p1 Project 1, the new project!
      * @param p2 Project 2, the existing project!
      */
    public SB[] getNewSBs(Project p1, Project p2) {
        SB[] sb1 = p1.getAllSBs();
        logger.info("new proj has "+sb1.length+" sbs");
        SB[] sb2 = p2.getAllSBs();
        logger.info("old proj has "+sb2.length+" sbs");
        if(sb1.length <= sb2.length) {
            logger.info("SCHEDULING: There are no new sbs! The new project has size "+
                    + sb1.length +" and the old project has size "+ sb2.length);
            return null;
        }
        int size = sb1.length - sb2.length;
        int x=0;
        SB[] newSBs = new SB[size];
        boolean isThere = false;
        for(int i=0; i < sb1.length; i++){ //Call this 'i' loop
            for(int j=0; j < sb2.length; j++){ //Call this 'j' loop
                if(sb1[i].getId().equals(sb2[j].getId())){ 
                    logger.info("sb is there. not adding");
                    isThere = true;
                }
                if(isThere){
                    logger.info("break out of j loop only (hopefully)");
                    isThere = false;
                    break; //out of 'j' loop
                } else {
                    logger.info("sb is not there. adding");
                    //add to newSBs
                    logger.info("new sbs's id == "+sb1[i].getId());
                    newSBs[x] = sb1[i];
                    logger.info("new sbs's id == "+newSBs[x].getId());
                    x++;
                    
                }
            }
            logger.info(" in getNewSBs i = "+i);
        }
        
        logger.info("SCHEDULING: difference between p1 & p2 = "+size);
        logger.info("SCHEDULING: size of newSBs = "+x);
        return newSBs;
    }

    public void updateSBQueue(Project p) {
        // get SBs from the project
        SB[] sbs = p.getAllSBs();
        // replace existing ones & add new ones
    }

    public void getUpdates() {
        try {
            pollArchive();
        } catch(Exception e) {
            e.printStackTrace(System.out);
        }
    }
    ///////////////////////////////////////////////////////////////
    // Archive stuff
    ///////////////////////////////////////////////////////////////


    /**
      * Queries the archive on the given query and schema. Assumes you're looking for
      * projects. Should change this eventually...
      * Then it checks that the project exists in the queue and then returns all the 
      * project ids.
      *
      */
    public String[] archiveQuery(String query, String schema) throws SchedulingException  {
        //only return the ones which the project manager knows.
        String[] tmp = archive.query(query, schema);
        logger.info("@@@@@@@@ Archive returned "+tmp.length);
        Vector v_uids = new Vector();
        for(int i=0;i< tmp.length; i++) {
            logger.info(tmp[i]);
            if(pQueue.isExists(tmp[i])){
                v_uids.add(tmp[i]);
            }
        }
        String[] p_uids = new String[v_uids.size()];
        for(int i=0; i < v_uids.size(); i++){
            p_uids[i] =(String) v_uids.elementAt(i);
        }
        return p_uids;
    }
    public Object archiveRetrieve(String uid) throws SchedulingException {
        return archive.retrieve(uid);
    }
    public void archiveReleaseComponents() throws SchedulingException  {
        archive.releaseArchiveComponents();
    }

    public SB[] getSBsForProject(String projId) throws SchedulingException {
        SB[] sbsFromArchive = archive.getSBsForProject(projId);
        if(sbsFromArchive == null || sbsFromArchive.length == 0) {
            throw new SchedulingException("No SBs in this project");
        }
        SB[] sbsFromPM = new SB[sbsFromArchive.length];
        for(int i=0; i < sbsFromArchive.length; i++) {
            sbsFromPM[i] = sbQueue.get(sbsFromArchive[i].getId());
        }
        return sbsFromPM;
    }
    
    ///////////////////////////////////////////////////////////////
        // PollArchiveStuff
    ///////////////////////////////////////////////////////////////
    /**
      * 
      */
    private void pollArchive() throws SchedulingException {
        Project[] projectList = new Project[0];
        Vector<ProjectStatus> tmpPS = new Vector<ProjectStatus>();
        ProjectStatus ps;
        Vector<SB> tmpSBs = new Vector<SB>();
    
        try {
            // Get all Projects, SBs and PS's from the archive
            projectList = archive.getAllProject();
            logger.info("ProjectList size =  "+projectList.length);
            ArrayList<Project> projects = new ArrayList<Project>(projectList.length);
            for(int i=0; i < projectList.length; i++) {
                projects.add(projectList[i]);
            }
            logger.info("Projects size =  "+projects.size());
            for(int i=0; i < projects.size(); i++) {
                //if project status is complete don't add
                ps = archive.getProjectStatus( projects.get(i) );
                //check if project status is complete
                logger.info("PS status = "+ps.getStatus().getState().toString());
                if(!ps.getStatus().getState().toString().equals("complete")){
                    tmpPS.add(ps);
                    SB[] sbs = archive.getSBsForProject( projects.get(i).getId() );
                    for(int j=0; j< sbs.length; j++){
                        tmpSBs.add( sbs[j] );
                    }
                } else {
                    logger.info("PS status = "+ps.getStatus().getState().toString());
                    //project status says project is complete.
                    //take PS out of tmpPS
                    tmpPS = removePSElement(tmpPS, projects.get(i).getProjectStatusId());
                    //take project's sbs out of tmpSBs
                    tmpSBs = removeSBElements(tmpSBs, projects.get(i).getId());
                    //take project out of the temp Project array so it
                    //doesn't get put into the pQueue.
                    projects.remove(i);
                    //TODO: Should check if its in the queues already and remove
                }
            }

            logger.info("projects = "+projects.size());
            logger.info("tmp ps = "+tmpPS.size());
            logger.info("tmp sbs " +tmpSBs.size());
            
            // For all the stuff gotten above from the archive, determine if
            // they are new (then add them), if the are updated (then updated)
            // or the same (then do nothing)
            Project newProject;
            Project oldProject;
            ProjectStatus newPS;
            ProjectStatus oldPS;
               
            for(int i=0; i < projects.size(); i++){
                newProject = projects.get(i);
                //does project exist in queue?
                if(pQueue.isExists( newProject.getId() )){
                    oldProject = pQueue.get(newProject.getId());
                    //yes it is so check if project needs to be updated, check if 
                    if(newProject.getTimeOfUpdate().compareTo(oldProject.getTimeOfUpdate()) == 1 ){
                        //needs updating
                        pQueue.replace(newProject);
                    } else if(newProject.getTimeOfUpdate().compareTo(oldProject.getTimeOfUpdate()) == 0 ){
                        // DO NOTHING hasn't been updated
                    } else if(newProject.getTimeOfUpdate().compareTo(oldProject.getTimeOfUpdate()) == -1 ){
                        // TODO should throw an error coz the old project has been updated and the new one hasnt
                    } else {
                        //TODO Throw an error here
                    }
                    //check if PS needs to be updated 
                    newPS = getPS(tmpPS, newProject.getId());
                    oldPS = psQueue.get(newPS.getProjectStatusEntity().getEntityId());
                    if(newPS.getTimeOfUpdate().compareTo(oldPS.getTimeOfUpdate()) == 1 ){
                        //needs updating
                        psQueue.updateProjectStatus(newPS);
                    } else if(newPS.getTimeOfUpdate().compareTo(oldPS.getTimeOfUpdate()) == 0 ){
                        // DO NOTHING hasn't been updated
                    } else if(newPS.getTimeOfUpdate().compareTo(oldPS.getTimeOfUpdate()) == -1 ){
                        // TODO should throw an error coz the old project has been updated and the new one hasnt
                    } else {
                        //TODO Throw an error here
                    }

                    //TODO if the sbs need updating and if there are new ones to add
                    SB[] currSBs = getSBs( tmpSBs, newProject.getId() );
                    SB newSB, oldSB;
                    for(int j=0; j < currSBs.length; j++){
                        newSB = currSBs[j];
                        if( sbQueue.isExists(newSB.getId()) ){
                            logger.info("Sb not new");
                            oldSB = sbQueue.get(newSB.getId());
                            //check if it needs to be updated, if yes then update
                            if(newSB.getTimeOfUpdate().compareTo(oldSB.getTimeOfUpdate()) == 1) {
                                logger.info("Sb needs updating");
                                sbQueue.replace(newSB);
                            }else if(newSB.getTimeOfUpdate().compareTo(oldSB.getTimeOfUpdate()) == 0) {
                                // DO NOTHING, hasn't been updated
                            }else if(newSB.getTimeOfUpdate().compareTo(oldSB.getTimeOfUpdate()) == -1) {
                                // TODO should throw an error coz the old sb has been updated and the new one hasnt
                            } else {
                                //TODO Throw an error
                            }
                        } else {
                            //not in queue, so add it.
                            logger.info("SB new, adding");
                            sbQueue.add(newSB);
                        }
                    }
                } else {
                    logger.info("Project new, adding");
                    //no it isn't so add project to queue, 
                    pQueue.add(newProject);
                    //add its project status to project status queue
                    psQueue.add( getPS( tmpPS, newProject.getId() ) );
                    //and sbs to sbqueue
                    sbQueue.add( getSBs(tmpSBs, newProject.getId() ) );
                    
                }
            }
        } catch(Exception e) {
            throw new SchedulingException(e);
        }
        logger.info("Size of pQueue = "+pQueue.size());
        logger.info("Size of psQueue = "+psQueue.size());
        logger.info("Size of sbQueue = "+sbQueue.size());
    }

    /**
      * Removes the project status element with the given id from the vector 
      * and returns the new vector.
      * To be used only with pollArchive and the vector holding the ProjectStatus'.
      * @param v The Vector holind all the projectStatus gotten during a pollArchive
      * @param s The id of the project status to be removed.
      * @return Vector REturn the vector minus one element
      */
    private Vector removePSElement(Vector v, String s) {
        for(int i=0; i < v.size(); i++){
            if(((ProjectStatus)v.elementAt(i)).getProjectStatusEntity().
                    getEntityId().equals(s)) {
                v.remove(i);
            }
        }
        return v;
    }

    /**
      * Removes all the sbs from the vector which belong to a given project.
      * To be used only with the pollAchive and the vector holding the sbs.
      * @param v The vector holding all the SBs gotten from all the projects in a pollarchive
      * @param s The id of the project which the sbs to be removed belong to
      * @return Vector The vector with all the sbs, minus the one(s) taken out
      */
    private Vector removeSBElements(Vector v, String s) {
        for(int i=0; i < v.size(); i++) {
            if(((SB)v.elementAt(i)).getProject().getId().equals(s) ){
                v.remove(i);
            }
        }
        return v;
    }

    /**
      * Get the Project status for the given project id
      * To be used only with the pollArchive and the vector holding the projectStatus'
      * @param v The vector of project status'
      * @param s The project Id
      * @return ProjectStatus The project status with the given id.
      */
    private ProjectStatus getPS(Vector v, String s) {
        ProjectStatus ps=null;
        for(int i=0; i < v.size(); i++) {
            ps = (ProjectStatus)v.elementAt(i);
            if(ps.getObsProjectRef().getEntityId().equals(s) ){
                return ps;
            }
        }
        return null;
    
    }


    /**
      * Get all the SBs from the given vector which belong to the given project.
      * @param v The vector containing all SBs gotten from pollarchive
      * @param s The projectId
      * @return SB[] The array of all SBs for the given project
      */
    private SB[] getSBs(Vector v, String s) {
        Vector<SB> sbsV = new Vector<SB>();
        SB sb;
        for(int i=0; i < v.size(); i++) {
            sb = (SB)v.elementAt(i);
            if (sb.getProject().getId().equals(s)){
                sbsV.add(sb);
            }
        }
        SB[] sbs = new SB[sbsV.size()];
        for(int i=0; i < sbsV.size(); i++){
            sbs[i] = (SB)sbsV.elementAt(i);
        }
        return sbs;
    }
}
