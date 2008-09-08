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


import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import alma.acs.container.ContainerServices;
import alma.acs.util.UTCUtility;
import alma.asdmIDLTypes.IDLEntityRef;
import alma.entity.xmlbinding.projectstatus.ExecBlockRefT;
import alma.entity.xmlbinding.projectstatus.ExecStatusT;
import alma.entity.xmlbinding.projectstatus.ObsUnitSetStatusT;
import alma.entity.xmlbinding.projectstatus.ObsUnitSetStatusTChoice;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.projectstatus.SBStatusT;
import alma.entity.xmlbinding.projectstatus.SessionT;
import alma.entity.xmlbinding.schedblock.SchedBlock;
import alma.entity.xmlbinding.specialsb.SpecialSB;
import alma.entity.xmlbinding.valuetypes.StatusT;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.log_audience.OPERATOR;
import alma.scheduling.EndSessionEvent;
import alma.scheduling.NothingCanBeScheduledEnum;
import alma.scheduling.NothingCanBeScheduledEvent;
import alma.scheduling.ProjectLite;
import alma.scheduling.SBLite;
import alma.scheduling.StartSessionEvent;
import alma.scheduling.Define.ControlEvent;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.ExecBlock;
import alma.scheduling.Define.ObservedSession;
import alma.scheduling.Define.Program;
import alma.scheduling.Define.Project;
import alma.scheduling.Define.ProjectQueue;
import alma.scheduling.Define.SB;
import alma.scheduling.Define.SBQueue;
import alma.scheduling.Define.SchedulingException;
import alma.scheduling.Define.SciPipelineRequest;
import alma.scheduling.Define.Status;
import alma.scheduling.Event.Publishers.PublishEvent;
import alma.scheduling.ObsProjectManager.ProjectManager;

/**
 *
 * @author Sohaila Lucero
 * @version $Id: ALMAProjectManager.java,v 1.112 2008/09/08 22:45:06 wlin Exp $
 */
public class ALMAProjectManager extends ProjectManager {
    //The container services
//    private ContainerServices containerServices;
    private ALMAArchive archive;
    private final SBQueue sbQueue;
    private final ProjectQueue pQueue;
    private final ProjectStatusQueue psQueue;
    private ALMAPublishEvent publisher;
    private ALMAPipeline pipeline;
//    private ALMAOperator oper;
    //TODO temporary
    private Vector specialSBs;  // never read!
    private ALMAClock clock;

//    private EntityDeserializer entityDeserializer;
//    private EntitySerializer entitySerializer;
    
    private final ArchivePoller archivePoller;
 
    public ALMAProjectManager(ContainerServices cs, 
                              ALMAOperator o, 
                              ALMAArchive a, 
                              SBQueue q, 
                              PublishEvent p, 
                              ALMAClock c) {
        super(cs.getLogger());
//        this.containerServices = cs;
        this.publisher =(ALMAPublishEvent)p;
//        this.oper = o;
        this.archive = a;
        this.sbQueue = q;
        this.psQueue = new ProjectStatusQueue(logger);
        this.pQueue = new ProjectQueue();
        this.pipeline = new ALMAPipeline(cs);
        this.clock = c;
        this.archivePoller = new ArchivePoller(archive, sbQueue, pQueue, psQueue, projectUtil, logger);
        //sbQueue = new SBQueue();
        specialSBs = new Vector();
        try  {
        	archivePoller.pollArchive();
            querySpecialSBs();
        } catch(Exception e) {
        }
        try {
//            entitySerializer = EntitySerializer.getEntitySerializer(logger);
//            entityDeserializer = EntityDeserializer.getEntityDeserializer(logger);
        }catch(Exception e){
            e.printStackTrace();
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
                Thread.sleep(60*15000);
            }catch(InterruptedException e) {
            }
            if(!stopCommand){
                try {
                    archivePoller.pollArchive();
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
            e.printStackTrace();
        }
        logger.finest("# of special sbs = "+specialSBs.size());
    }

    public Vector getSpecialSBs() {
        return specialSBs;
    }

    /**
      * Return sbs that qualify for DS... not indefinitely repeating ones
      */
    public SBQueue getDynamicSBQueue(){
        SBQueue dynamicSBs = new SBQueue();
        for(int i=0; i < sbQueue.size();i++){
            if(!sbQueue.get(i).getIndefiniteRepeat()){
                logger.fine("doesn't repeat indefinitely, qualifies for DS");
                dynamicSBs.add(sbQueue.get(i));
            } else{
                logger.fine("doesn't qualify for DS");
            }
        }
        return dynamicSBs;
    }

    public void checkForProjectUpdates() {
        try {
            archivePoller.pollArchive();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    
    /**
      * For Scheduling an ordered list of sbs we still need to map the to their projects
      * so all the correct information gets into the SB objects.
      * So this function basically creates a sbqueue with only the sbs in the list
      * and maps only those sbs to projects.
      */
    public SBQueue mapQueuedSBsToProjects(String[] sbs) {
        SBQueue queuedSBs= new SBQueue();
        logger.fine("number of sbs to be queued = "+sbs.length);
        for (int i=0; i < sbs.length; i++){
            queuedSBs.add(sbQueue.get(sbs[i]));
        }
        logger.fine("number of sbs in queue = "+queuedSBs.size());
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
        logger.finest("SCHEDULING:(session info) Session ("+sessionId+") has started.");
        logger.finest("SCHEDULING:(session info) Project id = "+proj_id+".");
        logger.finest("SCHEDULING:(session info) SB id = "+sb_id+".");
        //send message to operator
        //oper.send("Session ("+sessionId+") has started for Sb ("+sb_id+").");
        
    }

    /**
      * Log that the session has ended and send a message to the Operator
      */
    public void sessionEnd(String sessionId, String sb_id) {
        logger.finest("sb id = "+sb_id);
        String proj_id = (sbQueue.get(sb_id)).getProject().getId();
        logger.finest("Proj id= "+proj_id);
        logger.finest("SCHEDULING:(session info) Session ("+sessionId+") has ended.");
        logger.finest("SCHEDULING:(session info) Project id = "+proj_id+".");
        logger.finest("SCHEDULING:(session info) SB id = "+sb_id+".");
        //send message to operator
        //oper.send("Session ("+sessionId+") has ended for Sb ("+sb_id+").");
    }

    /**
      * An SB has completed an execution. Check that its repeat count is met and if so 
      * its status to complete. If not set it back to ready.
      */
    public void setSBComplete(ExecBlock eb) {
      //  try {
        ProjectStatus ps;
        SB completed = sbQueue.get(eb.getParent().getId());
        eb.setParent(completed);// replaced its sb-parent so exec block has full sb
        logger.fine("##########################");
        logger.fine("SCHEDULING: eb ("+eb.getId()+") has start time = "
                +eb.getStatus().getStartTime());
        logger.fine("SCHEDULING: sb's status in PM = "+completed.getStatus().getStatus());
        logger.fine("SCHEDULING: sb's starttime in PM = "+completed.getStatus().getStartTime());
        logger.fine("##########################");
	    //If this SB has reached its maximum number of repeats set it to complete.
        if(completed.getIndefiniteRepeat()) {
            logger.fine("SCHEDULING: This sb ("+completed.getId()+") has an indefinite repeat count");
            try {
	            completed.execEnd(eb,eb.getStatus().getEndTime(), Status.READY);
                logger.fine("SCHEDULING: indefinite-repeat sb keeps status = "
                        +completed.getStatus().getStatus());
            }catch (Exception e){ 
		        logger.severe(e.toString());
    	    }
            ps = getProjectStatusForSB(completed);
            archive.printProjectStatusFromObject(ps);
            archive.printProjectStatusFromArchive(ps.getProjectStatusEntity().getEntityId());
            ps = updateSBStatusInProjectStatus(eb, completed.getStatus());
            //hack test need check more
            sbQueue.replace(completed);
            try {
                psQueue.updateProjectStatus(ps);
                archive.updateProjectStatus(ps);
            } catch(Exception e) {
                logger.severe("SCHEDULING: Could not update project status in archive!");
                e.printStackTrace();
            }
            return;
        }
        
        if( completed.getNumberExec() >= completed.getMaximumNumberOfExecutions()  ){
            logger.fine("###########set to complete####");
            logger.fine("SCHEDULING: Number of executions before next added = "
                    +completed.getNumberExec());
            completed.execEnd(eb,eb.getStatus().getEndTime(), Status.OBSERVED);
            logger.fine("SCHEDULING: Setting end time for "+eb.getId());
            logger.fine("SCHEDULING: Total# executions done = "+completed.getNumberExec());
            logger.fine("SCHEDULING: Total allowed executions = "
                    +completed.getMaximumNumberOfExecutions());
            logger.fine("#################################");
            //update ProjectStatus to say this SB is completed/observed
        } else { //set it to ready
            logger.fine("##########set to ready###########");
            logger.fine("SCHEDULING: Number of executions before next added = "
                    +completed.getNumberExec());
            completed.execEnd(eb,eb.getStatus().getEndTime(), Status.READY);
            logger.fine("SCHEDULING: Setting end time for "+eb.getId());
            logger.fine("SCHEDULING: Total # executions done = "+completed.getNumberExec());
            logger.fine("SCHEDULING: Total allowed executions = "
                    +completed.getMaximumNumberOfExecutions());
            logger.fine("#################################");
        }
        logger.fine("SCHEDULING: sb status = "+completed.getStatus().getStatus());
        ps = updateSBStatusInProjectStatus(eb, completed.getStatus());
        // hack test need check more
        sbQueue.replace(completed);
        if(completed.getStatus().getStatus().equals("observed")){
            //should be done after the SBStatus is updated.
            ps = updateObsUnitSetStatusStats(completed.getId(),eb, ps);
        }
        try {
            psQueue.updateProjectStatus(ps);
            archive.updateProjectStatus(ps);
        } catch(Exception e) {
            logger.severe("SCHEDULING: Could not update project status in archive!");
            e.printStackTrace();
        }
    }

    /*
    private void setProjectComplete(Project p){
        //get all the SBs in this project
        boolean complete = false;
        SB[] sbs = p.getAllSBs();
        //check status of them all
        for(int i=0;i <  sbs.length; i++){
            if(sb.getStatus().isComplete()){
                complete = true;
            } else {
                complete = false;
            }
        }
        //if all are set to be complete then set project as complete.
        if(complete) {
            
        }
    }*/

    /**
      * Right now just setting the number of sbs passed/failed. Eventually
      * we'll worry about whether the OUS passed...
      * This should be called AFTER the SBStatus has been updated!
      * Also updates the SB/Program/Project objects.
      */
    private synchronized ProjectStatus updateObsUnitSetStatusStats(String sb_id, 
                                                                   ExecBlock eb,
                                                                   ProjectStatus ps) {
        //get project status
        try {
            logger.fine("SCHEDULING: about to update ObsUnitSetStatus for sb "+sb_id);
            //top level obs unit set which is actually the ObsProgram.
            //check if sb belongs here
            SB sb = sbQueue.get(sb_id);
            Program p = sb.getParent();
            ObsUnitSetStatusT set = ps.getObsProgramStatus();
            if(isSbInThisSet(sb_id, set)){
                if(eb.getStatus().getStatus().equals("aborted")){
                    int x = set.getNumberSBsFailed();
                    set.setNumberSBsFailed(x + 1);
                    p.setNumberSBsFailed(x +1);
                    logger.fine("aborted; x = "+ (x+1));
                }else if(eb.getStatus().getStatus().equals("observed")){
                    int x = set.getNumberSBsCompleted();
                    set.setNumberSBsCompleted(x + 1);
                    p.setNumberSBsCompleted(x +1);
                    logger.fine("completed; x = "+ (x+1));
                //add for testing not very sure I can do this    
                } else if(eb.getStatus().getStatus().equals("complete")){
                	int x = set.getNumberSBsCompleted();
                    set.setNumberSBsCompleted(x + 1);
                    p.setNumberSBsCompleted(x +1);
                    // test only not complete.....
                    p.getParent().setNumberSBsCompleted(x+1);
                    logger.fine("completed; x = "+ (x+1));
                // end of the adding    
                } else {
                    logger.warning(
                        "SCHEDULING: ObsUnitSetSTatus not updated coz status field was invalid:"+
                        eb.getStatus().getStatus());
                }
            } else {

                ObsUnitSetStatusTChoice choice = set.getObsUnitSetStatusTChoice(); 
                //ObsUnitSet
                ObsUnitSetStatusT[] sets = choice.getObsUnitSetStatus();
                findSet(sets, sb_id, eb, set, p);
            }
            //do this after findSet coz findSet recursively will update parent OUSS.
            if(set.getTotalSBs() == set.getNumberSBsCompleted()){
                //ObsProgramStatus is complete so project is complete!
                set.getStatus().setState(StatusTStateType.COMPLETE);
                set.getStatus().setEndTime(eb.getStatus().getEndTime().toString());
                ps.getStatus().setState(StatusTStateType.COMPLETE);
                ps.getStatus().setEndTime(eb.getStatus().getEndTime().toString());
            }
            if(set.getTotalObsUnitSets() == set.getNumberObsUnitSetsCompleted()){
                set.getStatus().setState(StatusTStateType.COMPLETE);
                set.getStatus().setEndTime(eb.getStatus().getEndTime().toString());
                ps.getStatus().setState(StatusTStateType.COMPLETE);
                ps.getStatus().setEndTime(eb.getStatus().getEndTime().toString());
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return ps;
    }

    /**
      * A recursive function to go through the tree of obs unit set status' to find the
      * right obs unit set status which contains the sb status, there fore updating the
      * # sb failed/completed field. Also updates the SB/Program/Project objects.
      * TODO at some point update this info in the Program and in the recursive find
      * pass the proper program.
      *
      * @param sets An array of the ObsUnitSetStatus' at this level to check for the SB
      * @param id SB's id
      * @param eb ExecBlock 
      * @param parent The parent OUS of the given sets
      * @param p The SB's direct parent, once the set if found for the SB then p is used
      */
    private synchronized void findSet(ObsUnitSetStatusT[] sets, String id, 
                                      ExecBlock eb, ObsUnitSetStatusT parent, Program p){
        try {
        logger.fine("SCHEDULING: finding set for sb "+id);
        for(int i=0; i < sets.length; i++){
            if(isSbInThisSet(id, sets[i])){
                if(eb.getStatus().getStatus().equals("aborted")){
                    int x = sets[i].getNumberSBsFailed();
                    sets[i].setNumberSBsFailed(x + 1);
                    int y = parent.getNumberSBsFailed();
                    parent.setNumberSBsFailed(y + 1);
                    p.setNumberSBsFailed(y + 1);
                    //logger.fine("aborted; sb ct = "+ (x+1));
                    //logger.fine("aborted; parent ct = "+ (y+1));
                }else if(eb.getStatus().getStatus().equals("complete")){
                    int x = sets[i].getNumberSBsCompleted();
                    int y = parent.getNumberSBsCompleted();
                    sets[i].setNumberSBsCompleted(x+1);
                    parent.setNumberSBsCompleted(y+1);
                    p.setNumberSBsCompleted(y+1);
                    //logger.fine("completed; before update = "+ (x));
                    //logger.fine("completed; after update = "+ sets[i].getNumberSBsCompleted());
                    if(sets[i].getTotalSBs() == sets[i].getNumberSBsCompleted()){
                      //  logger.fine("completed; parent ct = "+ (y+1));
                        sets[i].getStatus().setState(StatusTStateType.COMPLETE);
                        sets[i].getStatus().setEndTime(eb.getStatus().getEndTime().toString());
                        int z = parent.getNumberObsUnitSetsCompleted();
                        parent.setNumberObsUnitSetsCompleted(z+1);
                    }
                } else {
                    logger.warning(
                        "SCHEDULING: ObsUnitSetSTatus not updated coz status field was invalid:"+
                        eb.getStatus().getStatus());
                }
                return;
            } else {
                logger.fine("SCHEDULING: sb not in set, trying next level");
            }
        }
        logger.fine("SCHEDULING: sets length = "+sets.length);
        for(int i=0;i< sets.length; i++){
            logger.fine("SCHEDULING: wasn't there trying again for sb "+id);
            ObsUnitSetStatusTChoice choice = sets[i].getObsUnitSetStatusTChoice(); 
            ObsUnitSetStatusT[] sets2 = choice.getObsUnitSetStatus();
            findSet(sets2, id, eb, sets[i], p);
        }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
    /**
      * A method which checks if the given sb_id is in the given obs unit set status.
      */
    private synchronized boolean isSbInThisSet(String sb_id, ObsUnitSetStatusT set) {
        ObsUnitSetStatusTChoice c = set.getObsUnitSetStatusTChoice();
        //logger.fine("check SB and obsunit set(sb count):"+c.getSBStatusCount());
        //logger.fine("check SB and obsunit set(sb id):"+sb_id);
        if (c.getSBStatusCount() == 0){
            return false;
        } else {
            SBStatusT[] sbs = c.getSBStatus();
            for(int i=0; i < sbs.length; i++){
		//logger.fine("Obs sb:"+sbs[i].getSchedBlockRef().getEntityId());
                if(sbs[i].getSchedBlockRef().getEntityId().equals(sb_id)){
                    return true;
                }
            }
        }
        return false;
    }
    /**
      *
      */
    public void removeCompletedProjectFromQueue(String proj_id){
    }
    
    private ProjectStatus getProjectStatusForSB(SB sb){
        String proj_id = sb.getProject().getId();
        logger.fine ("SCHEDULING: getting project status for project ("+proj_id+")");
        ProjectStatus[] allPS = psQueue.getAll();
        ProjectStatus ps = null;
        for(int i=0; i < allPS.length; i++){
            if(allPS[i].getObsProjectRef().getEntityId().equals(proj_id)) {
                ps = allPS[i];
                break;
            }
        }
        return ps;
    }
    /**
      */
    public synchronized ProjectStatus updateSBStatusInProjectStatus(ExecBlock eb, Status sbStatus) {
        SB sb = eb.getParent();
        sb = sbQueue.get(sb.getId());
        ProjectStatus ps = getProjectStatusForSB(sb);
        logger.finest("SCHEDULING: about to update sbStatus for "+sb.getId());
        logger.finest("SCHEDULING: about to update PS::"+ps.getProjectStatusEntity().getEntityId());

        //top level obs unit set which is actually the ObsProgram.
        ObsUnitSetStatusTChoice choice = ps.getObsProgramStatus().getObsUnitSetStatusTChoice(); 
        //ObsUnitSet
        ObsUnitSetStatusT[] sets = choice.getObsUnitSetStatus();
        SBStatusT[] sbs = choice.getSBStatus();
        Vector<SBStatusT> foo = new Vector<SBStatusT>();
        for(int i=0; i < sbs.length; i++){
            foo.add(sbs[i]);
        }
        for(int i=0; i < sets.length; i++) {
            foo = parseObsUnitSetStatus(sets[i], foo);
        }
        sbs = new SBStatusT[foo.size()];
        sbs = foo.toArray(sbs);
        SBStatusT status = getSBStatusMatch(sb, sbs);
        addExecStatus(eb, status);
        try {
        logger.fine("SCHEDULING: SB's status, for SB "+status.getSchedBlockRef().getEntityId()+
                " is "+sb.getStatus().getStatus());
        StatusT stat = status.getStatus();
        if(sbStatus.getStatus().equals("notdefined")){
            stat.setState(StatusTStateType.NOTDEFINED);
        } else if (sbStatus.getStatus().equals("waiting")){
            stat.setState(StatusTStateType.WAITING);
        } else if (sbStatus.getStatus().equals("ready")){
            stat.setState(StatusTStateType.READY);
        } else if (sbStatus.getStatus().equals("running")){
            stat.setState(StatusTStateType.RUNNING);
        } else if (sbStatus.getStatus().equals("aborted")){
            stat.setState(StatusTStateType.ABORTED);
        } else if (sbStatus.getStatus().equals("complete")){
            stat.setState(StatusTStateType.COMPLETE);
            stat.setEndTime(sb.getStatus().getEndTime().toString());
        } else if (sbStatus.getStatus().equals("observed")){
            stat.setState(StatusTStateType.OBSERVED);
            stat.setEndTime(sb.getStatus().getEndTime().toString());
        } else if (sbStatus.getStatus().equals("processed")){
            stat.setState(StatusTStateType.PROCESSED);
            stat.setEndTime(sb.getStatus().getEndTime().toString());
        } else if (sbStatus.getStatus().equals("canceled")){
            stat.setState(StatusTStateType.CANCELED);
            stat.setEndTime(sb.getStatus().getEndTime().toString());
        }
        status.setStatus(stat);
        logger.fine("SCHEDULING: SBStatus's status is "+status.getStatus().getState().toString());
        logger.fine("SCHEDULING: got "+sbs.length+" sb status' in this PS");
        } catch(Exception e){
            e.printStackTrace();
        }
        return ps;
    }

    private SBStatusT getSBStatusMatch(SB sb, SBStatusT[] allSBs) {
        SBStatusT match=null;
        for(int i=0; i < allSBs.length; i++){
            if(allSBs[i].getSchedBlockRef().getEntityId().equals(sb.getId())){
                match = allSBs[i];
                break;
            }
        }
        return match;
    }
    private void addExecStatus(ExecBlock eb, SBStatusT sbStatus) {

        ExecStatusT es = new ExecStatusT();
        StatusT execStatus = new StatusT();
        ExecBlockRefT ref = new ExecBlockRefT();
        ref.setExecBlockId(eb.getExecId());
        es.setExecBlockRef(ref);
        es.setArrayName(eb.getArrayName());
        es.setTimeOfCreation(eb.getStatus().getStartTime().toString());
        execStatus.setStartTime(eb.getStatus().getStartTime().toString());
        execStatus.setEndTime(eb.getStatus().getEndTime().toString());
        StatusTStateType state;
        if(eb.getStatus().getStatus().equals("complete")){
            state = StatusTStateType.COMPLETE;
            //state = StatusTStateType.OBSERVED;
        }else if( eb.getStatus().getStatus().equals("aborted") ) {
            state = StatusTStateType.ABORTED;
        } else {
            state = StatusTStateType.NOTDEFINED;
        }
        execStatus.setState(state);
        es.setStatus(execStatus);
        sbStatus.addExecStatus(es);
    }

 
     //TODO: Rename this method.
    public Vector<SBStatusT> parseObsUnitSetStatus(ObsUnitSetStatusT set, Vector v) {
        logger.finest("SCHEDULING: Set PartID = "+set.getEntityPartId());
        SBStatusT[] sbs = null;
        ObsUnitSetStatusT[] obs = null;
        if(set.getObsUnitSetStatusTChoice().getObsUnitSetStatusCount() > 0) {
            logger.finest("SCHEDULING: more than one obs unit set status in PS");
            obs = set.getObsUnitSetStatusTChoice().getObsUnitSetStatus();
            for(int i=0; i< obs.length; i++) {
                v = parseObsUnitSetStatus(obs[i], v);
            }
        }
        if(set.getObsUnitSetStatusTChoice().getSBStatusCount() > 0) {
            sbs = set.getObsUnitSetStatusTChoice().getSBStatus();
            for (int i=0; i < sbs.length; i++){
                v.add(sbs[i]);
            }
        }
        return v;
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
    public synchronized void createObservedSession(ExecBlock eb) {

        String sbid = eb.getParent().getId();
	// set sb and sb's parent status from ready to running 
    	// this is specfic for manual mode array and IS/Queue/Dynamic will double set the status
    	//will modify if scheduling receive the SessionEvent later
    	//logger.fine("sb id:"+sbid);
    	SB sb = sbQueue.get(sbid);
    	//SB sb1 = sbQueue.get(0);
    	//logger.fine("SB ready time:"+sb.getStatus().getStartTime());
    	if((sb.getStatus().getStartTime() == null)){ 
    		sb.setStartTime(clock.getDateTime());
    		sb.setRunning();
    	}

        Program p = ((SB)sbQueue.get(sbid)).getParent();
        ObservedSession session = new ObservedSession();
        session.setSessionId(eb.getSessionId());
        session.setStartTime(new DateTime(System.currentTimeMillis()));
        session.addExec(eb);
        p.addObservedSession(session);
        logger.fine("the program(SB) now has "+p.getAllSession().length + " sessions");
        
        //Project proj = pQueue.get(p.getProject().getId());
        Program prog =  addProgram(p);
        session.setProgram(p);
        logger.fine("This program now has "+prog.getAllSession().length+" sessions");
        Project proj = prog.getProject();
        if(proj == null) {
            logger.severe("SCHEDULING: project was null!!!"); //should never happen.
            //throw new Exception("SCHEDULING: Error with project structure!"); TODO Add this eventually
        }
        ProjectStatus ps = psQueue.getStatusFromProjectId(proj.getId());
        try {
            logger.fine("SCHEDULING: updating project status with session "+session.getSessionId());
            //need to remove this one, because the project info did not include any other session that had been add into
            ps = projectUtil.updateProjectStatus(proj);
            psQueue.updateProjectStatus(ps);
            archive.updateProjectStatus(ps);
        } catch(SchedulingException e) {
            logger.severe("SCHEDULING: error mapping PS with Session");
            e.printStackTrace();
        }
        //return session;
    }

    /**
      * Updates the observng session information.
      */
    public synchronized void updateObservedSession(Project p, ProjectStatus ps, String sessionId, String endTime){
        logger.finest("SCHEDULING: updating session with end time.");
        try {
            ObservedSession[] allSes = searchPrograms(p.getProgram(), sessionId).getAllSession();

            ObservedSession ses=null;
            for(int i=0; i < allSes.length; i++){
                if(allSes[i].getSessionId().equals(sessionId)){
                    ses = allSes[i];
                    ses.setEndTime(new DateTime(endTime));
                }
            }
            ps = projectUtil.updateProjectStatus(p);
            psQueue.updateProjectStatus(ps);
            archive.updateProjectStatus(ps);
        } catch(Exception e){
            logger.severe("SCHEDULING: error updating PS with session");
            e.printStackTrace();
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
    //public String sendStartSessionEvent(String sbid) {
    // this method will be replace by sendStartSessionEvent(String sbid,String arrayname)
    // will be remove later
    public IDLEntityRef sendStartSessionEvent(String sbid) {
        SB sb = sbQueue.get(sbid);
        //in future will be done in scheduler.
        //ObservedSession session = createObservedSession(sb.getParent(),eb);
        //session.addExec(eb);
        //the entity which contains the session is the project status
        String sessionId = new String(projectUtil.genPartId());
        sessionStart(sessionId, sbid);
        IDLEntityRef sessionRef = new IDLEntityRef();
        sessionRef.entityId = sb.getProject().getProjectStatusId();
        logger.fine("Project status for sb ("+sb.getId()+") is "+sessionRef.entityId);
        sessionRef.partId = sessionId;
        sessionRef.entityTypeName = "ProjectStatus";
        sessionRef.instanceVersion ="1.0";
        IDLEntityRef sbRef = new IDLEntityRef();
        sbRef.entityId = sbid;
        sbRef.partId ="";
        sbRef.entityTypeName = "SchedBlock";
        sbRef.instanceVersion ="1.0";
        //try and tell quicklook pipeline a session is about to start
        String title="";
        if(!sb.getProject().getProjectName().equals("")){
            title = sb.getProject().getProjectName();
        }else {
            title = "undefined_project_name";
        }
        if(!sb.getSBName().equals("")){
            title = title + sb.getSBName();
        } else {
            title = title +"undefined_sb_name";
        }
        logger.fine("SCHEDULING: title for quicklook = "+title);
        try {
            pipeline.startQuickLookSession(sessionRef, sbRef, title);
        } catch (Exception e){
            logger.warning("SCHEDULING: Quick look not available.");
        }
        try {
            logger.fine("SCHEDULING: Session with id == "+sessionId+" (start event sent)");
            long time = UTCUtility.utcJavaToOmg(System.currentTimeMillis());
            StartSessionEvent start_event = new StartSessionEvent(
                    UTCUtility.utcJavaToOmg(System.currentTimeMillis()),
                    sessionRef,
                    sbRef);
                    
            publisher.publish(start_event);
        } catch(Exception e) {
            logger.severe("SCHEDULING: Failed to send start session event!");
            e.printStackTrace();
        }
        //return sessionId;
        return sessionRef;
    }
    
    /* Will be this way in future
    public void sendStartSessionEvent(ObservedSession session,String arrayName) {
    }
    */
    public IDLEntityRef sendStartSessionEvent(String sbid,String arrayName) {
        SB sb = sbQueue.get(sbid);
        String ArrayName = arrayName;
        //in future will be done in scheduler.
        //ObservedSession session = createObservedSession(sb.getParent(),eb);
        //session.addExec(eb);
        //the entity which contains the session is the project status
        String sessionId = new String(projectUtil.genPartId());
        sessionStart(sessionId, sbid);
        IDLEntityRef sessionRef = new IDLEntityRef();
        sessionRef.entityId = sb.getProject().getProjectStatusId();
        logger.fine("Project status for sb ("+sb.getId()+") is "+sessionRef.entityId);
        sessionRef.partId = sessionId;
        sessionRef.entityTypeName = "ProjectStatus";
        sessionRef.instanceVersion ="1.0";
        IDLEntityRef sbRef = new IDLEntityRef();
        sbRef.entityId = sbid;
        sbRef.partId ="";
        sbRef.entityTypeName = "SchedBlock";
        sbRef.instanceVersion ="1.0";
        //try and tell quicklook pipeline a session is about to start
        String title="";
        if(!sb.getProject().getProjectName().equals("")){
            title = sb.getProject().getProjectName();
        }else {
            title = "undefined_project_name";
        }
        if(!sb.getSBName().equals("")){
            title = title + sb.getSBName();
        } else {
            title = title +"undefined_sb_name";
        }
        logger.fine("SCHEDULING: title for quicklook = "+title);
        try {
            pipeline.startQuickLookSession(sessionRef, sbRef, ArrayName,title);
        } catch (Exception e){
            logger.warning("SCHEDULING: Quick look not available.");
        }
        try {
            logger.fine("SCHEDULING: Session with id == "+sessionId+" (start event sent)");
            long time = UTCUtility.utcJavaToOmg(System.currentTimeMillis());
            StartSessionEvent start_event = new StartSessionEvent(
                    UTCUtility.utcJavaToOmg(System.currentTimeMillis()),
                    sessionRef,
                    sbRef);
                    
            publisher.publish(start_event);
        } catch(Exception e) {
            logger.severe("SCHEDULING: Failed to send start session event!");
            e.printStackTrace();
        }
        //return sessionId;
        return sessionRef;
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
        logger.finest("SCHEDULING: session found!");
        session.setEndTime(endTime);
        logger.finest("SCHEDULING: sbid = " +sbid);
        logger.finest("SCHEDULING: session part id = "+session.getEntityPartId());
        sessionEnd(session.getEntityPartId(), sbid);
        updateObservedSession(proj, ps, session.getEntityPartId(), endTime);
        IDLEntityRef sessionRef = new IDLEntityRef();
        sessionRef.entityId=sb.getProject().getProjectStatusId();
        sessionRef.partId=session.getEntityPartId();
        sessionRef.entityTypeName = "ProjectStatus";
        sessionRef.instanceVersion ="1.0";
        IDLEntityRef sbRef = new IDLEntityRef();
        sbRef.entityId = sbid;
        sbRef.partId ="";
        sbRef.entityTypeName = "SchedBlock";
        sbRef.instanceVersion ="1.0";
        //try and tell quicklook pipeline a session is about to end
        pipeline.endQuickLookSession(sessionRef, sbRef);
        try {
            EndSessionEvent end_event = new EndSessionEvent(
                    UTCUtility.utcJavaToOmg(System.currentTimeMillis()),
                    sessionRef,
                    sbRef,
                    allExecIds);
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
        //ObsUnitSetStatusT set = searchSets(obsProgram.getObsUnitSetStatusTChoice().getObsUnitSetStatus(), execid);
        ObsUnitSetStatusT[] tmp = new ObsUnitSetStatusT[1];
        tmp[0] = obsProgram;
        ObsUnitSetStatusT set = searchSets(tmp, execid);
        if(set == null) {
            logger.severe("SCHEDULING: PM: returned set is null! (looking for session)");
        }
        SessionT[] sessions = set.getSession();
        logger.finest("SCHEDULING: in PM getSession, length = "+sessions.length);
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
                logger.finest("SCHEDULING: Session found! returning true");
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
                    logger.finest("SCHEDULING: Session found! returning true");
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
                    logger.finest("SCHEDULING: Session found! returning session");
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
        try {
            Program prog = sb.getParent();
            if(prog.getDataReductionProcedureName() == null || 
                    prog.getDataReductionProcedureName().equals("") ) {
                needed = false;
            } else {
                needed = true;
            }
        } catch(Exception e){
            needed = false;
        }
        return needed;
    }
    
    /**
      * Creates a SciPipelineRequest with the given program and comment string.
      * @param Program The program that the science pipeline request belongs to.
      * @param s A comment about the science pipeline request
      * @return SciPipelineRequest
      */
    public synchronized SciPipelineRequest createSciPipelineRequest(String sbid, String s)
        throws SchedulingException {

        //use sbid to get the program 
        logger.finest("SCHEDULING: Creating PPR in PM");
        SB sb = sbQueue.get(sbid);
        Program prog = sb.getParent();
        SciPipelineRequest ppr = new SciPipelineRequest(prog, s);
 		ppr.setReady(projectUtil.genPartId(), new DateTime(System.currentTimeMillis()));
        ppr.setStarted(new DateTime(System.currentTimeMillis()));
        prog.setSciPipelineRequest(ppr);
        Program prog2 = addProgram(prog);
        Project proj = prog2.getProject();
        //Project proj = pQueue.get(prog.getProject().getId());
        //proj.setProgram(prog2);
        ProjectStatus ps = psQueue.getStatusFromProjectId(proj.getId());
        try {
            ps = projectUtil.updateProjectStatus(proj);
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

        logger.fine("SCHEDULING: Starting Pipeline");
        String pprString = archive.getPPRString(ps, ppr.getId());
        logger.fine("SCHEDULING: (in PM) PPR string =  "+pprString);
        String pipelineResult = pipeline.processRequest(pprString);
    }

    /**
      * Checks to see if the SB's parent (obsunitset) is complete
      */
    protected boolean isObsUnitSetComplete(String sbid) {
        SB sb = sbQueue.get(sbid);
        Program p = sb.getParent();
        if(p.getStatus().isComplete()){
            return true;
        } else {
            return false;
        }
    }

    public void managerStopped() {
        try {
            pipeline.releasePipelineComp();
        } catch(Exception e){
            logger.severe("SCHEDULING: error releasing pipeline comp from PM");
            e.printStackTrace();
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
            logger.finest("SCHEDULING: Comparing sb lists. Size is different so false return"
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
        logger.finest("new proj has "+sb1.length+" sbs");
        SB[] sb2 = p2.getAllSBs();
        logger.finest("old proj has "+sb2.length+" sbs");
        if(sb1.length <= sb2.length) {
            logger.finest("SCHEDULING: There are no new sbs! The new project has size "+
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
                    logger.finest("sb is there. not adding");
                    isThere = true;
                }
                if(isThere){
                    logger.finest("break out of j loop only (hopefully)");
                    isThere = false;
                    break; //out of 'j' loop
                } else {
                    logger.finest("sb is not there. adding");
                    //add to newSBs
                    logger.finest("new sbs's id == "+sb1[i].getId());
                    newSBs[x] = sb1[i];
                    logger.finest("new sbs's id == "+newSBs[x].getId());
                    x++;
                    
                }
            }
            logger.finest(" in getNewSBs i = "+i);
        }
        
        logger.finest("SCHEDULING: difference between p1 & p2 = "+size);
        logger.finest("SCHEDULING: size of newSBs = "+x);
        return newSBs;
    }

    public void updateSBQueue(Project p) {
        // get SBs from the project
        SB[] sbs = p.getAllSBs();
        // replace existing ones & add new ones
    }

    public void getUpdates() {
        try {
            archivePoller.pollArchive();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    ///////////////////////////////////////////////////////////////
    // Archive stuff
    ///////////////////////////////////////////////////////////////


    /**
      * Queries the archive on the given query and schema. Assumes you're looking for
      * projects or SBs and only does one or the other.
      * Then it checks that the project/sb exists in the queue and then returns all the 
      * ids.
      *
      */
    public String[] archiveQuery(String query, String schema) throws SchedulingException  {
        //only return the ones which the project manager knows so check for updates.
        archivePoller.pollArchive();
        String[] tmp = archive.query(query, schema);
        Vector v_uids = new Vector();
        if(schema.equals("ObsProject")) {
            for(int i=0;i< tmp.length; i++) {
                logger.finest("proj. id = "+tmp[i]);
                if(pQueue.isExists(tmp[i])){
                    v_uids.add(tmp[i]);
                }
            }
        } else if (schema.equals("SchedBlock")){
            for(int i=0;i< tmp.length; i++) {
                logger.finest("sb id = "+tmp[i]);
                if(sbQueue.isExists(tmp[i])){
                    v_uids.add(tmp[i]);
                }
            }
        }
        String[] p_uids = new String[v_uids.size()];
        for(int i=0; i < v_uids.size(); i++){
            p_uids[i] =(String) v_uids.elementAt(i);
        }
        return p_uids;
    }

    /**
      * Given the results done with a *, narrow them down because the searchStr
      * contains more than just *
      * @param projIds Results of the query done with a *
      * @param searchStr String containing *
      * @param attr Attribute used in query
      * @return String[] Narrowed down results
      */
    public String[] getWildCardResults(String[] projIds, String searchStr, String attr){
        Vector res = new Vector();
        Project p;
        String tmp="";
        int x1, x2, len;
        for(int i=0;i < projIds.length; i++){
            p= pQueue.get(projIds[i]);
            len = searchStr.length();
            if( len == 1 && searchStr.equals("*")) {
                //only contains * so we return everything
                res.add(projIds[i]);
            } else {
                if(searchStr.startsWith("*") && searchStr.endsWith("*")){
                    x2 = (searchStr.substring(1,searchStr.length()).indexOf("*")) +1; 
                    //added one above coz our substring was of len 1
                    tmp = searchStr.substring(1, x2);
                } else if(searchStr.startsWith("*") ){
                    tmp = searchStr.substring(1, searchStr.length());
                } else if( searchStr.endsWith("*")){
                    tmp = searchStr.substring(0, searchStr.length() -1);
                }
            //x = searchStr.indexOf("*");
            //System.out.println("SEARCHING: "+x);
            //tmp = searchStr.substring(0,x);
           // System.out.println("SEARCHING: "+tmp);
                if(attr.equals("pI")){
                    if(p.getPI().contains(tmp)){
                        res.add(projIds[i]);
                    }
                } else if(attr.equals("projectName")){
                    if(p.getProjectName().contains(tmp)){
                        res.add(projIds[i]);
                    }
                }
            }
        }
        String[] results = new String[res.size()];
        for(int i=0; i< res.size();i++){
            results[i] = (String)res.elementAt(i);
        }
        return results;
    }

    /**
      * Given the list of project IDs and SB IDs, return the project IDs of the
      * projects which contain the SBs represented by the given SB IDs
      * @param projectIds 
      * @param sbIds
      * @return String[] The project Ids of the projects which contain the sbs.
      */
    public String[] getProjectSBUnion(String[] projectIds, String[] sbIds){
        String[] results = new String[0];
        Vector v_res = new Vector();
        Vector sbs = new Vector();
        for(int i=0; i < sbIds.length; i++){
            sbs.add(sbIds[i]);
        }
        //for each project
        for(int i=0; i < projectIds.length; i++){
            //get all its sbs
        	//logger.info("<manager total sb>"+((Project)pQueue.get(projectIds[i])).getTotalSBs());
            SB[] projectSBs = ((Project)pQueue.get(projectIds[i])).getAllSBs();

            //for each sb in that project
            for (int j=0; j < projectSBs.length; j++){
                //if it matches one of the ones in the search we return it!
                if(sbs.contains(projectSBs[j].getId())){
                    //yup its a match! return this project now.
                    v_res.add(projectIds[i]);
                    break;
                }
            }
        }
        results = new String[v_res.size()];
        for(int i=0; i < v_res.size(); i++){
            results[i] = (String)v_res.elementAt(i);
        }
        return results;
    }

    public String[] getSBProjectUnion(String[] sbIds, String[] projectIds){
        String[] results = new String[0];
        Vector res = new Vector();
        //for each sb
        for(int i=0;i < sbIds.length; i++){
            SB sb = sbQueue.get(sbIds[i]);
            //get its project
            Project p = sb.getProject();
            //check if its project is in the list of projectIds
            for(int j=0; j < projectIds.length;j++){
                if(p.getId().equals(projectIds[j])) {
                    //add that sb as one to return!
                    res.add(sbIds[i]);
                }
            }
        }
        results = new String[res.size()];
        for(int i=0; i < res.size(); i++){
            results[i] = (String)res.elementAt(i);
        }

        return results;
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

    public SBLite[] getSBLitesForProject(String projectId) {
        SB[] sbs=null;
        SBLite[] sblites=null;
        try {
            sbs = getSBsForProject(projectId);
            sblites = new SBLite[sbs.length];
            for(int i=0; i <sbs.length; i++){
                sblites[i] =createSBLite(sbs[i].getSchedBlockId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sblites;
    }

    public ProjectLite getProjectLiteForSB(String sbId) {
        SB sb =(SB)sbQueue.get(sbId);
        ProjectLite p=createProjectLite(sb.getProject().getId());
        return p;
    }
    
    
    ///////////////////////////////////////////////////////////////
        // PollArchiveStuff
    ///////////////////////////////////////////////////////////////
    /**
     * Factored method pollArchive into this separate class to allow testing
     * without starting everything up (see ATF problems in 5.0.3, 2008-06)
     * @author hsommer
     */
    public static class ArchivePoller {
    	
    	private final Logger logger;
        private final ALMAArchive archive;
        private final SBQueue sbQueue;
        private final ProjectQueue pQueue;
        private final ProjectStatusQueue psQueue;
		private final ProjectUtil projectUtil;
    	
    	public ArchivePoller(ALMAArchive archive, SBQueue sbQueue, ProjectQueue pQueue, ProjectStatusQueue psQueue, ProjectUtil projectUtil, Logger logger) {
    		this.logger = logger;
    		this.projectUtil = projectUtil;
    		this.archive = archive;
    		this.sbQueue = sbQueue;
    		this.pQueue = pQueue;
    		this.psQueue = psQueue;
    	}
    	
	    /**
	      * polls the archive for new/updated projects
	      * then updates the queues (project queue, sb queue & project status queue)
	      */
	    void pollArchive() throws SchedulingException {
	        logger.fine("project Queue size at start of pollarchive = "+pQueue.size());
	        logger.fine("sb queue size at start of pollarchive = "+sbQueue.size());
	        logger.fine("ps queue size at start of pollarchive = "+psQueue.size());
	        logger.fine("SCHEDULING: polling archive for new/updated projects");
	        Project[] projectList = new Project[0];
	        Vector<ProjectStatus> tmpPS = new Vector<ProjectStatus>();
	        ProjectStatus ps;
	        Vector<SB> tmpSBs = new Vector<SB>();
	        Vector<Integer> indicesToRemove = new Vector<Integer>();
	    
	        try {
	            // Get all Projects, SBs and PS's from the archive
	           // checkSBUpdates();
	            //checkPSUpdates();
	            projectList = archive.getAllProject();
	            logger.finest("ProjectList size =  "+projectList.length);
	            ArrayList<Project> projects = new ArrayList<Project>(projectList.length);
	            for(int i=0; i < projectList.length; i++) {
	                logger.finest("project id = "+projectList[i].getId());
	                projects.add(projectList[i]);
	            }
	            logger.finest("Projects size =  "+projects.size());
	            int size = projects.size();
	            for(int i=0; i < size; i++) {
	                //if project status is complete don't add
	                ps = archive.getProjectStatus( projects.get(i) );
	                if(ps == null){
	                    logger.warning("Project status for project "+((Project)projects.get(i)).getId()+"did not exist");
	                }
	                //check if proj04ect status is complete
	                //logger.fine("Program session number:"+ps.getObsProgramStatus().getSessionCount());
	                logger.finest("iteration "+i+" out of "+size);
	                logger.finest("PS ("+ps.getProjectStatusEntity().getEntityId()+") "+
	                        "status = "+ps.getStatus().getState().toString());
	                if(!ps.getStatus().getState().toString().equals("complete")){
	                    logger.finest("Adding non complete ProjectStatus "+
	                            ps.getProjectStatusEntity().getEntityId());
	                    tmpPS.add(ps);
	                    SB[] sbs = archive.getSBsForProject( projects.get(i).getId() );
	                    for(int j=0; j< sbs.length; j++){
	                        tmpSBs.add( sbs[j] );
	                    }
	                } else {
	                    logger.finest("PS "+ps.getProjectStatusEntity().getEntityId()+" complete");
	                    indicesToRemove.add(new Integer(i));
	                }
	            }
	            //for(int i=0;i < indicesToRemove.size(); i++){
			for(int i=(indicesToRemove.size()-1);i >=0 ; i--){
	                //project status says project is complete.
	                //take PS out of tmpPS
	                tmpPS = removePSElement(tmpPS, projects.get(indicesToRemove.elementAt(i).intValue()).getProjectStatusId());
	                //take project's sbs out of tmpSBs
	                tmpSBs = removeSBElements(tmpSBs, projects.get(indicesToRemove.elementAt(i).intValue()).getId());
	                //take project out of the temp Project array so it
	                //doesn't get put into the pQueue.
	                logger.finest("Project "+projects.get(indicesToRemove.elementAt(i).intValue()).getId()+" is complete, take out of queue");
	                projects.remove(indicesToRemove.elementAt(i).intValue());
	                //TODO: Should check if its in the queues already and remove
	            }
	
	            logger.finest("projects = "+projects.size());
	            logger.finest("tmp ps = "+tmpPS.size());
	            logger.finest("tmp sbs " +tmpSBs.size());
	            
	            // For all the stuff gotten above from the archive, determine if
	            // they are new (then add them), if the are updated (then updated)
	            // or the same (then do nothing)
	            Project newProject;
	            Project oldProject;
	            ProjectStatus newPS;
	            ProjectStatus oldPS;
	               
	            for(int i=0; i < projects.size(); i++){
	                newProject = projects.get(i);
	                //logger.finest("iteration "+i+", project = "+newProject.getId());
	                //logger.finest("number of program in pollarchive:"+newProject.getAllSBs().length);
	                //does project exist in queue?
	                if(pQueue.isExists( newProject.getId() )){
	                    oldProject = pQueue.get(newProject.getId());
	                    //logger.finest("(old project)number of program in pollarchive:"+oldProject.getAllSBs().length);
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
	                    newPS = projectUtil.updateProjectStatus(newProject);
	                    oldPS = psQueue.get(newPS.getProjectStatusEntity().getEntityId());
	                    if(newPS.getTimeOfUpdate().compareTo(oldPS.getTimeOfUpdate()) == 1 ){
	                        //needs updating
	                    	logger.finest("Update project Status after update obsproject");
	                    	//XmlEntityStruct xml1 =entitySerializer.serializeEntity(newPS);
	                    	archive.updateProjectStatus(newPS);
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
	                            logger.finest("Sb not new");
	                            oldSB = sbQueue.get(newSB.getId());
	                            //check if it needs to be updated, if yes then update
	                            if(newSB.getTimeOfUpdate().compareTo(oldSB.getTimeOfUpdate()) == 1) {
	                                logger.finest("Sb needs updating");
	                                sbQueue.replace(newSB);
	                                pQueue.replace(newProject);
	                                logger.finest("Update project Status after update SchedBlock");
	                                archive.updateProjectStatus(newPS);
	                                psQueue.updateProjectStatus(newPS);
	                            }else if(newSB.getTimeOfUpdate().compareTo(oldSB.getTimeOfUpdate()) == 0) {
	                                // DO NOTHING, hasn't been updated
	                            }else if(newSB.getTimeOfUpdate().compareTo(oldSB.getTimeOfUpdate()) == -1) {
	                                // TODO should throw an error coz the old sb has been updated and the new one hasnt
	                            } else {
	                                //TODO Throw an error
	                            }
	                        } else {
	                            //not in queue, so add it.
	                            logger.finest("SB new, adding");
	                            sbQueue.add(newSB);
	                            logger.finest("Update project Status after update SchedBlock");
	                            archive.updateProjectStatus(newPS);
	                            psQueue.updateProjectStatus(newPS);
	                            pQueue.replace(newProject);
	                        }
	                    }
	                } else {
	                    logger.finest("Project new, adding");
	                    //no it isn't so add project to queue, 
	                    pQueue.add(newProject);
	                    //add its project status to project status queue
	                    //archive.updateProjectStatus(tmpPS);
	                    psQueue.add( getPS( tmpPS, newProject.getId() ) );
	                    //and sbs to sbqueue
	                    SB[] schedBlocks = getSBs(tmpSBs, newProject.getId());
	                    if (schedBlocks.length > 0) {
	                    	sbQueue.add( schedBlocks );
	                    	Program p = (schedBlocks[0]).getParent();
	                    	logger.finest("Program's session "+p.getId()+"has "+p.getNumberSession()+" session");
	                    }
	                    else {
	                    	logger.info("HSO hotfix 2008-06-07: new project " + newProject.getId() + " does not have any schedblocks. Not sure if this is OK." );
	                    }
	                }
	            }
	
	            //checkSBUpdates();
	            //checkPSUpdates();
	        } catch(Exception e) {
	            e.printStackTrace();
	            throw new SchedulingException(e);
	        }
	        logger.fine("Size of pQueue = "+pQueue.size());
	        logger.fine("Size of psQueue = "+psQueue.size());
	        logger.fine("Size of sbQueue = "+sbQueue.size());
	        logger.log(Level.INFO, "The Scheduling Subsystem is currently managing "+
	                pQueue.size()+" projects, "+ sbQueue.size()+" sbs and "+psQueue.size()+
	                " project status'", OPERATOR.value);
    }
    }
    /**
      * Ask the archive for any updated SBs since the last query time
      * update any new ones in the queue.
      */
    private void checkSBUpdates() throws SchedulingException {
        try {
            SchedBlock[] sbs = archive.queryRecentSBs();
            //logger.fine("<check SBUpdates:>"+sbs.length);            
            for(int i=0; i < sbs.length; i++){
            	//System.out.println("schedblock name:"+sbs[i].getName());
            	//first make sure the SB is a new SB or modify SB 
                SB sb = sbQueue.get(sbs[i].getSchedBlockEntity().getEntityId());
                //System.out.println("sb name:"+sb.getSBName());
            	if( sb == null){
            		
            		logger.fine("This is a new SB");
            		//sb=ProjectUtil.createSBfromSchedBlock(sbs[i],archive,pQueue);
            		//if(sb!=null) {
            		//	System.out.println("new sb added:"+sb.getId());
            		//	sbQueue.add(sb);
            		//}	
                }
            	else {
          
            		sb = projectUtil.updateSB(sb, sbs[i], clock.getDateTime());
        			sbQueue.replace(sb);
        			//logger.fine("<sb's name>"+sb.getSBName());
        			//logger.fine("<sb's program:>"+sb.getParent().getId());
        			//logger.fine("<sb program length>"+sb.getParent().getNumberMembers());
            	}
            }
        } catch(SchedulingException e){
            logger.warning("SCHEDULING: Problem checking for SB updates");
            throw e;
        }

    }    
    /**
      * Ask the archive for any updated ProjectStatus' since the last query time
      * update any new ones in the queue.
      * Should be done after checking for project updates and SB updates
      */
    private void checkPSUpdates() throws SchedulingException {
        try {
            ProjectStatus[] ps = archive.queryRecentProjectStatus();
            
            for(int i=0; i < ps.length; i++){
            	
                ProjectStatus p = psQueue.get(ps[i].getProjectStatusEntity().getEntityId());
                if(p == null){
                    throw new SchedulingException (
                            "SCHEDULING: Trying to update a ProjectStatus which isn't in the queue!");
                }
                psQueue.replace(p);
            }
        } catch(SchedulingException e){
            logger.warning("SCHEDULING: Problem checking for ProjectStatus updates");
            throw e;
        }
    }

    /**
      * Removes the project status element with the given id from the vector 
      * and returns the new vector.
      * To be used only with pollArchive and the vector holding the ProjectStatus'.
      * @param v The Vector holind all the projectStatus gotten during a pollArchive
      * @param s The id of the project status to be removed.
      * @return Vector REturn the vector minus one element
      */
    private static Vector removePSElement(Vector v, String s) {
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
    private static Vector removeSBElements(Vector v, String s) {
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
    private static ProjectStatus getPS(Vector v, String s) {
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
    private static SB[] getSBs(Vector v, String s) {
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

    private SBLite createSBLite(String id) {
        String sid,pid,sname,pname,pi,pri;
        double ra,dec,freq,score,success,rank;
        long maxT;
        SB sb = sbQueue.get(id);
        SBLite sblite = new SBLite();
        sid = sb.getId();
        if(id == null || id =="") {
            id = "WARNING: Problem with SB id";
        }
        sblite.schedBlockRef =id;
        pid = sb.getProject().getId();
        if(pid ==null||pid=="") {
            pid = "WARNING: problem with project id";   
        }
        sblite.projectRef = pid;
        sblite.obsUnitsetRef = "";

        sname =sb.getSBName();
        if(sname == null || sname ==""){
            sname = "WARNING: problem with SB name";
        }
        sblite.sbName =sname;
        pname = sb.getProject().getProjectName();
        if(pname == null ||pname =="") {
            pname = "WARNING: problem with project name";
        }
        sblite.projectName = pname;
        pi = sb.getProject().getPI();
        if(pi == null || pi == ""){
            pi = "WARNING: problem with pi";
        }
        sblite.PI = pi;
        pri = sb.getProject().getScientificPriority().getPriority();
        if(pri == null || pri =="") {
            pri = "WARNING: problem with scientific priority";
        }
        sblite.priority = pri;
        try {
            ra = sb.getTarget().getCenter().getRa();
        } catch(NullPointerException npe) {
            logger.warning("SCHEDULING: RA object == null in SB, setting to 0.0");
            ra = 0.0;
        }
        sblite.ra = ra;
        try {
            dec = sb.getTarget().getCenter().getDec();
        } catch(NullPointerException npe) {
            logger.warning("SCHEDULING: DEC object == null in SB, setting to 0.0");
            dec = 0.0;
        }
        if(sb.getIndefiniteRepeat()){
            sblite.maxExec = "indefinite";
        } else {
            sblite.maxExec = String.valueOf(sb.getMaximumNumberOfExecutions());
        }
        sblite.dec = dec;
        sblite.freq = 0;
        sblite.maxTime = 0;
        sblite.score = 0;
        sblite.success = 0; 
        sblite.rank = 0 ;
        //have to get PS to get this info
        //System.out.println("SBid "+id);
        ProjectStatus ps = getPSForSB(id);
        sblite.isComplete = isSBComplete(ps, id);
        return sblite;
    }

    public SBLite[] getSBLites() {
        logger.fine("SCHEDULING: Called getSBLites()");
        SBLite[] sbliteArray=null;
        SBLite sblite;
        Vector<SBLite> sbliteVector = new Vector<SBLite>();
        try {
            archivePoller.pollArchive();
            Project[] projects = pQueue.getAll();
            for(int i=0; i < projects.length; i++){
                //get all the sbs of this project
                SB[] sbs = projects[i].getAllSBs ();
                for(int j=0; j < sbs.length; j++) {
                    sblite = createSBLite(sbs[j].getId());
                    sbliteVector.add(sblite);
                }
            }
            sbliteArray = new SBLite[sbliteVector.size()];
            sbliteArray = sbliteVector.toArray(sbliteArray);
            
        } catch(Exception e) {
	        logger.severe(e.toString());
            e.printStackTrace();
        }
        return sbliteArray;
    }

    public SBLite[] getSBLite(String[] ids) {
        logger.fine("SCHEDULING: Called getSBLite(ids)");
        try {
            archivePoller.pollArchive();
        } catch(Exception e) {
            return null;
        }
        SBLite[] sblites = new SBLite[ids.length];
        SBLite sblite;
        for(int i=0; i < ids.length; i++){
            sblite = createSBLite(ids[i]);
            sblites[i] = sblite;
        }
        return sblites;
    }

    public ProjectLite[] getProjectLites(String[] ids) {
        getUpdates();
        logger.fine("SCHEDULING: Called getProjectLites(ids)");
        ProjectLite[] projectliteArray=new ProjectLite[ids.length];
        for(int i=0; i < ids.length; i++){
            projectliteArray[i] = createProjectLite(ids[i]);

        }
        return projectliteArray;
    }

    private ProjectLite createProjectLite(String id) {
        Project p = pQueue.get(id);;
        ProjectLite projectlite= new ProjectLite();
        projectlite.uid = p.getId();
        projectlite.projectName = p.getProjectName();
        projectlite.piName = p.getPI();
        projectlite.version = p.getProjectVersion();
        projectlite.status = p.getStatus().getStatus();
        projectlite.creationTime = p.getTimeOfCreation().toString();
        projectlite.totalSBs = String.valueOf(p.getTotalSBs());
        projectlite.completeSBs = String.valueOf(p.getNumberSBsCompleted());
        projectlite.failedSBs = String.valueOf(p.getNumberSBsFailed());
        SB[] sbs = p.getAllSBs();
        String[] sbids= new String[sbs.length];
        for(int j=0; j < sbs.length;j++){
            sbids[j] = sbs[j].getId();
        }
        projectlite.allSBIds = sbids;
        //have to get PS to get this info
        ProjectStatus ps = getPSForProject(id);
        projectlite.isComplete = isProjectComplete(ps);
        projectlite.completeSBs = String.valueOf(ps.getObsProgramStatus().getNumberSBsCompleted());
        projectlite.failedSBs = String.valueOf(ps.getObsProgramStatus().getNumberSBsFailed());
        //logger.fine("Total complete SBs:"+projectlite.completeSBs);
        //logger.fine("Total failed SBs:"+projectlite.failedSBs);
        return projectlite;
    }

    protected SBQueue getSBQueue(){
        return sbQueue;
    }

    public void createProjectWebpage(String uid) {
        //start a new webpage for project with uid
        //email PI with webpage address
    }

    public ProjectStatus getPSForProject(String p_id){
        Project p = pQueue.get(p_id);
        String ps_id = p.getProjectStatusId();
        //System.out.println("PSid "+ps_id);
        ProjectStatus ps = psQueue.get(ps_id);
        return ps;
    }
    public ProjectStatus getPSForSB(String s_id){
        SB sb = sbQueue.get(s_id);
        return getPSForProject(sb.getProject().getId());
    }

    private boolean isProjectComplete(ProjectStatus ps){
        if(ps.getStatus().getState().toString().equals("complete")){
            return true;
        }
        return false;
    }
    
    private boolean isSBComplete(ProjectStatus ps, String sb_id){
        ObsUnitSetStatusT prog = ps.getObsProgramStatus();
        SBStatusT sb;
        if(isSbInThisSet(sb_id, prog)){
            sb = getSBStatus(prog, sb_id);
        } else {
            sb = findSB(prog, sb_id);
        }
        try {
            if(sb.getStatus().getState().toString().equals("complete")
                    || sb.getStatus().getState().toString().equals("observed")){
    
                return true;
            } else {
                return false;
            }
        } catch(Exception e) {
            return false;
        }

    }
    
    private SBStatusT findSB(ObsUnitSetStatusT o, String id){
    	//this method had logical problem which will not always get the 
    	//SBStatus from ProjectStatus 
    	//this method need to re-writing in the future....
    	//Frank Lin
        ObsUnitSetStatusTChoice c = o.getObsUnitSetStatusTChoice();
        ObsUnitSetStatusT[] sets;
        SBStatusT[] sbs;
        if(c.getObsUnitSetStatusCount() > 0){
            sets = c.getObsUnitSetStatus();
            for(int i=0; i < sets.length; i++){
                if(isSbInThisSet(id, sets[i])){
                    //get it & return it
                    return getSBStatus(sets[i], id);
                }else {
                    //lets look for it recursively
                    //return findSB(sets[i], id);
                	findSB(sets[i], id);
                }
            }
        } else if (c.getSBStatusCount() > 0){

            if(isSbInThisSet(id, o)){
                sbs = c.getSBStatus();
                return getSBStatus(o, id);
            }
        } else {
            logger.warning("SCHEDULING: Error trying to get SBStatus for ("+id+
                    ") while checking if it is complete or not.");
            return null;
        }
        
        //logger.warning("SCHEDULING: in ALMAProjectManager SB did not match projectStatus asssigned to it.");
        return null;
    }
    
    private SBStatusT getSBStatus(ObsUnitSetStatusT o, String id){
        ObsUnitSetStatusTChoice c = o.getObsUnitSetStatusTChoice();
        if(c.getObsUnitSetStatusCount() > 0 && c.getSBStatusCount() > 0){
            logger.warning("SCHEDULING: Project contains SBs and Obs unit sets at "+
                    "same level! Scheduling Software should be updated since at the "+
                    "this was written the ALMA-OT only let one or the other at a "+
                    "given level.");
        }
        SBStatusT[] sbs = c.getSBStatus();
        for(int i=0;i <  sbs.length; i++){
            if(sbs[i].getSchedBlockRef().getEntityId().equals(id)){
                //its what we want!
                return sbs[i];
            }
        }
        //if we're at this point our isSbInThisSet method failed us!
        logger.warning("SCHEDULING: isSbInThisSet in ALMAProjectManager gave wrong status when used for getSBStatus.");
        return null;
    }

    protected IDLEntityRef[] startManualModeSession(String arrayName) throws SchedulingException {
        IDLEntityRef[] refs = new IDLEntityRef[2];
        //query for uid of manual mode sb
        String manualArrayName = arrayName;
        String p_id = archive.queryForManualModeProject();
        Project p = pQueue.get(p_id);
        SB[] sbs = p.getAllSBs();
        if(sbs.length < 1 ){
            throw new SchedulingException("SCHEDULING: Manual Mode project had not SB!");
        }
        String sbid = sbs[0].getId();
        refs[0] = new IDLEntityRef();
        refs[0].entityId = sbid;
        refs[0].partId = "";
        refs[0].entityTypeName="SchedBlock";
        refs[0].instanceVersion="1.0";
        //send start session event
        refs[1] = sendStartSessionEvent(sbid,manualArrayName);
        //
        return refs;
    }
}
