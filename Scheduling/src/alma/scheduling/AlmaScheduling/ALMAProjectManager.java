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


import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import alma.SchedulingExceptions.wrappers.AcsJObsProjectRejectedEx;
import alma.SchedulingExceptions.wrappers.AcsJSchedBlockRejectedEx;
import alma.acs.container.ContainerServices;
import alma.acs.entityutil.EntitySerializer;
import alma.acs.logging.AcsLogger;
import alma.acs.util.UTCUtility;
import alma.asdmIDLTypes.IDLEntityRef;
import alma.entities.commonentity.EntityRefT;
import alma.entity.xmlbinding.ousstatus.OUSStatusEntityT;
import alma.entity.xmlbinding.ousstatus.SessionT;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.sbstatus.ExecStatusT;
import alma.entity.xmlbinding.schedblock.SchedBlock;
import alma.entity.xmlbinding.specialsb.SpecialSB;
import alma.entity.xmlbinding.valuetypes.ExecBlockRefT;
import alma.entity.xmlbinding.valuetypes.StatusT;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.log_audience.OPERATOR;
import alma.scheduling.EndSessionEvent;
import alma.scheduling.NothingCanBeScheduledEnum;
import alma.scheduling.NothingCanBeScheduledEvent;
import alma.scheduling.ProjectAndSBLites;
import alma.scheduling.ProjectLite;
import alma.scheduling.SBLite;
import alma.scheduling.StartSessionEvent;
import alma.scheduling.AlmaScheduling.statusIF.OUSStatusI;
import alma.scheduling.AlmaScheduling.statusIF.ProjectStatusI;
import alma.scheduling.AlmaScheduling.statusIF.SBStatusI;
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
import alma.scheduling.Scheduler.Scheduler;

/**
 *
 * @author Sohaila Lucero
 * @version $Id: ALMAProjectManager.java,v 1.132 2010/03/30 17:52:08 dclarke Exp $
 */
public class ALMAProjectManager extends ProjectManager {
	
	private static boolean TEST = false;
	final private static long ArchivePollInterval = TEST?
														 1 * 60 * 1000:
														15 * 60 * 1000;
	
	final public static String[] OPRunnableStates = {
		StatusTStateType.READY.toString(),				
		StatusTStateType.PARTIALLYOBSERVED.toString()				
	};
	final public static String[] SBRunnableStates = {
		StatusTStateType.READY.toString(),				
		StatusTStateType.RUNNING.toString()				
	};
	
    //The container services
//    private ContainerServices containerServices;
    private ALMAArchive archive;
    private final SBQueue sbQueue;
    private final ProjectQueue projectQueue;
    private final StatusEntityQueueBundle statusQs;
    private ALMAPublishEvent publisher;
    private ALMAPipeline pipeline;
    
    private final ProjectUtil projectUtil;
    private LinkedHashMap<String, Scheduler> arrayName2Scheduler;

    //    private ALMAOperator oper;
    //TODO temporary
    private Vector specialSBs;  // never read!
    private ALMAClock clock;

//    private EntityDeserializer entityDeserializer;
//    private EntitySerializer entitySerializer;
    
//    private final ArchivePoller archivePoller;
    private final ALMAArchivePoller archivePoller;
 
    public ALMAProjectManager(ContainerServices cs, 
    		ALMAOperator o, 
    		ALMAArchive a, 
    		SBQueue q, 
    		PublishEvent p, 
    		ALMAClock c) {
    	super(cs.getLogger());
//  	this.containerServices = cs;
    	this.publisher =(ALMAPublishEvent)p;
//  	this.oper = o;
    	this.archive = a;
    	this.sbQueue = q;
    	statusQs = new StatusEntityQueueBundle(logger);
    	projectUtil = a.getProjectUtil();
    	this.projectQueue = new ProjectQueue();
    	this.pipeline = new ALMAPipeline(cs);
    	this.clock = c;
//    	this.archivePoller = new ArchivePoller(archive, sbQueue, projectQueue,
//    			statusQs, projectUtil, logger);
        this.archivePoller = new ALMAArchivePoller(archive, sbQueue,
                projectQueue, statusQs, projectUtil, clock, logger);
        this.arrayName2Scheduler = new LinkedHashMap<String, Scheduler>();
//  	sbQueue = new SBQueue();
    	specialSBs = new Vector<SpecialSB>();
    	try  {
    		archivePoller.pollArchive();
    		querySpecialSBs();
    	} catch(Exception e) {
    	}
    	try {
//  		entitySerializer = EntitySerializer.getEntitySerializer(logger);
//  		entityDeserializer = EntityDeserializer.getEntityDeserializer(logger);
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
                Thread.sleep(ArchivePollInterval);
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

    public ProjectAndSBLites getProjectAndSBLitesFromSearchCriteria(
            String projectName, String PI, boolean manualMode,
            String projectType, String arrayType, String sbModeName,
            String sbModeType) {
        try {
            archivePoller.pollArchive();
        } catch (SchedulingException ex) {
            ex.printStackTrace();
        }
        ProjectAndSBLites retVal = new ProjectAndSBLites();
        Project[] projects = searchProjects(projectName, PI, manualMode, projectType,
                arrayType);
        logger.info("# of projects after search: " + projects.length);
        List<String> projectIds = new ArrayList<String>();
        for (Project p : projects) {
            projectIds.add(p.getId());
        }
        SB[] sbs = searchSBs(sbModeName, sbModeType);
        logger.info("# of sbs after search: " + sbs.length); 
        SB[] filteredSBs =
            filterSBsFromProjectIds(sbs, projectIds);
        List<String> sbIds = new ArrayList<String>();
        for (SB sb : filteredSBs) {
            sbIds.add(sb.getId());
        }
        Project[] filteredProjects = filterProjectsFromSBIds(projects, sbIds);
        SBLite[] sbLites = new SBLite[filteredSBs.length];
        for (int i = 0; i < filteredSBs.length; i++) {
            sbLites[i] = filteredSBs[i].getSBLite();
        }
        retVal.sbLites = sbLites;
        ProjectLite[] prjLites = new ProjectLite[filteredProjects.length];
        for (int i = 0; i < filteredProjects.length; i++) {
            prjLites[i] = filteredProjects[i].getProjectLite();
        }
        retVal.projectLites = prjLites;
        return retVal;
    }

    public SB[] filterSBsFromProjectIds(SB[] sbs, List<String> projectIds) {
        List<SB> retVal = new ArrayList<SB>();
        for (SB sb : sbs) {
            Project p = sb.getProject();
            if (projectIds.contains(p.getId())) {
                retVal.add(sb);
            }
        }
        return retVal.toArray(new SB[0]);
    }
    
    public Project[] filterProjectsFromSBIds(Project[] projects, List<String> sbIds) {
        List<Project> retVal = new ArrayList<Project>();
        for (Project p : projects) {
            SB[] projectSBs = p.getAllSBs();
            for (SB sb : projectSBs){
                if(sbIds.contains(sb.getId())){
                    retVal.add(p);
                    break;
                }
            }
        }
        return retVal.toArray(new Project[0]);
    }
    
    public Project[] searchProjects(String projectName, String PI, boolean manualMode,
            String projectType, String arrayType) {
        List<Project> selectedProjects = new ArrayList<Project>();
        for (Project prj : projectQueue.getAll()) {
            String prjProjectName = prj.getProjectName().toUpperCase();
            String prjPI = prj.getPI().toUpperCase();
            if (projectName.equals("*") || prjProjectName.contains(projectName.toUpperCase())) {
                if (PI.equals("*") || prjPI.contains(PI.toUpperCase())) {
                    if (prj.getManualMode() == manualMode) {
                        if (projectType.equals("All") ||
                                projectType.equals(prj.getProjectType())) {
                            if (arrayType.equals("All") ||
                                    arrayType.equals(prj.getArrayType())) {
                                selectedProjects.add(prj);
                            }
                        }
                    }
                }
            }
        }        
        return selectedProjects.toArray(new Project[0]);
    }
    
    public SB[] searchSBs(String modeName, String modeType) {
        List<SB> selectedSBs = new ArrayList<SB>();
        for (SB sb : sbQueue.getAll()) {
            if (modeName.equals("All") || modeName.equals(sb.getModeName())) {
                if (modeType.equals("All") || modeType.equals(sb.getModeType())) {
                    selectedSBs.add(sb);
                }
            }
        }
        return selectedSBs.toArray(new SB[0]);
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
        return projectQueue;
    }

    public Project getProject(String id) throws SchedulingException {
        return getProjectQueue().get(id);
    }
    public Project getProjectFromQueue(String id) throws SchedulingException {
        return getProjectQueue().get(id);
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

    private ExecStatusT makeExecStatus(ExecBlock eb) {
    	final ExecStatusT result = new ExecStatusT();
    	result.setTimeOfCreation(DateTime.currentSystemTime().toString());
    	result.setTimeOfUpdate(result.getTimeOfCreation().toString());
    	result.setArrayName(eb.getArrayName());
    	
    	final ExecBlockRefT ref = new ExecBlockRefT();
    	ref.setExecBlockId(eb.getExecId());
    	
    	final StatusT status = new StatusT();
    	status.setState(StatusTStateType.BROKEN);
    	// TODO: Other fields in status.
    	
    	result.setStatus(status);
    	result.setExecBlockRef(ref);
    	
    	return result;
    }
    
    /**
     * Work out if the SB is in some way special - intended to allow
     * AIV peeps to crack on without their projects going through the
     * SUSPENDED state.
     * 
     * @param sb - the SB to check
     * @return <code><b>true</b></code> if the SB should never end up
     *         in the supended state, <code><b>false</b></code>
     *         otherwise.
     */
    private boolean sbCannotBeStoppedMwahhHaHaaaaa(SB sb) {
    	// Currently somewhat simplistic.
    	return sb.getIndefiniteRepeat();
    }
    
    public void endExecutionBlock(ExecBlock eb) {
    	final Status    status    = eb.getStatus();
        final Scheduler scheduler = getSchedulerForArray(eb.getArrayName());
        final String    sbId      = eb.getParent().getId();
        final SB        completed = sbQueue.get(sbId);
        final SBStatusI sbStatus  = statusQs.getSBStatusQueue().getStatusFromSBId(sbId);
        ProjectStatusI  prStatus;
        
        try {
        	prStatus = sbStatus.getProjectStatus();
        } catch (SchedulingException e) {
			logger.severe(String.format(
					"Cannot process end execution event for ExecBlock %s. Failed to get ProjectStatus: %s",
					eb.getId(),
					e.getLocalizedMessage()));
			return;
		}
    	
        addExecStatusAndDoBookkeeping(eb, sbStatus);
        {   // All that happens in this block is logging
        	final StringBuilder b = new StringBuilder();
        	final Formatter     f = new Formatter(b);
        	f.format("%n%n%nALMAProjectManager.endExecutionBlock(eb)%n");
        	f.format("\teb status   = %s%n", status.getStatus());
        	f.format("\tsbId        = %s%n", sbId);
        	f.format("\tsbStatus Id = %s, type = %s%n",
        			sbStatus.getUID(), sbStatus.getClass().getSimpleName());
        	f.format("\tprStatus Id = %s, type = %s%n",
        			prStatus.getUID(), prStatus.getClass().getSimpleName());
        	if (completed == null) {
        		f.format("\tcompleted is null%n");
        	} else {
        		f.format("\tcompleted   = %s%n", completed.getId());
        	}
        	if (scheduler == null) {
        		f.format("\tscheduler is null%n");
        	} else {
        		f.format("\tscheduler   = %s %s %s%n",
        				scheduler.getId(),
        				scheduler.getType(),
        				scheduler.isFullAuto()? "Full Auto":
        					scheduler.isSemiAuto()? "Semi Auto":
        						"** unknown mode **");
        	}
        	int full = 0;
        	int part = 0;
        	int brok = 0;
        	int unkn = 0;
        	for (final ExecStatusT eStatus : sbStatus.getExecStatus()) {
        		f.format("\t\tEB status = %s%n", eStatus.getStatus().getState());
        		if (eStatus.getStatus().getState() == StatusTStateType.FULLYOBSERVED) {
        			full ++;
        		} else if (eStatus.getStatus().getState() == StatusTStateType.PARTIALLYOBSERVED) {
        			part ++;
        		} else if (eStatus.getStatus().getState() == StatusTStateType.BROKEN) {
        			brok ++;
        		} else {
        			unkn ++;
        		}
        	}
        	f.format("\tSuccessful: %d (including new one if applicable)%n", full);
        	if (completed.getIndefiniteRepeat()) {
        		f.format("\tIndefinite repeat%n");
        	} else {
        		f.format("\tMaximum:    %d%n", completed.getMaximumNumberOfExecutions());
        	}
    		f.format("\tMore executions required = %s%n", moreExecutionsRequired(completed, sbStatus));
            final DateTime startTime = eb.getStatus().getStartTime();
            final DateTime endTime   = eb.getStatus().getEndTime();
            final int elapsedTime = (int)((endTime.getMillisec() - startTime.getMillisec()) / 1000);
    		f.format("\tEnd     Time = %s%n", endTime);
    		f.format("\tStart   Time = %s%n", startTime);
    		f.format("\tElapsed Time = %ds%n", elapsedTime);
        	f.format("%n%n%n");
        	
        	logger.info(b.toString());
        } // End of logging block.
		
        if (sbCannotBeStoppedMwahhHaHaaaaa(completed)) {
//    		prStatus = updateObsUnitSetStatusStats(sbId, eb, prStatus);
			logger.info(String.format(
					"SchedBlock %s execution %s %s, special case, marking SchedBlock as %s",
					sbId, eb.getId(), status, StatusTStateType.READY));
			setSBReady(sbId);
        } else if (status.isComplete()) {
//    		prStatus = updateObsUnitSetStatusStats(sbId, eb, prStatus);
    		if (scheduler.isSemiAuto()) {
    			logger.info(String.format(
    					"SchedBlock %s execution %s %s, scheduler mode is SemiAuto, marking SchedBlock as %s",
    					sbId, eb.getId(), status, StatusTStateType.SUSPENDED));
    			setSBSuspended(sbId);
    		} else if (moreExecutionsRequired(completed, sbStatus)) {
    			logger.info(String.format(
    					"SchedBlock %s execution %s %s, scheduler mode is FullAuto and more executions are required, marking SchedBlock as %s",
    					sbId, eb.getId(), status, StatusTStateType.READY));
    			setSBReady(sbId);
    		} else {
    			logger.info(String.format(
    					"SchedBlock %s execution %s %s, scheduler mode is FullAuto and no more executions are required, marking SchedBlock as %s",
    					sbId, eb.getId(), status, StatusTStateType.SUSPENDED));
    			setSBSuspended(sbId);
    		}
    	} else if (status.isAborted() || status.isFailed()) {
//    		prStatus = updateObsUnitSetStatusStats(sbId, eb, prStatus);
    		logger.info(String.format(
    				"SchedBlock %s execution %s %s, marking SchedBlock as %s",
    				sbId, eb.getId(), status, StatusTStateType.SUSPENDED));
    		setSBSuspended(sbId);
    	} else {
			logger.severe(String.format(
					"Unexpected status (%s) in completed ExecBlock",
					status.getStatus()));
    		setSBSuspended(sbId);
    	}
    }
    
    private boolean moreExecutionsRequired(SB completed, SBStatusI sbStatus) {
        if (completed.getIndefiniteRepeat()) {
        	return true;
        }
    	
        final int max = completed.getMaximumNumberOfExecutions();
        
    	int successfulRuns = 0;  // story of my life, mate... :-<
    	for (final ExecStatusT eStatus : sbStatus.getExecStatus()) {
    		if (eStatus.getStatus().getState() == StatusTStateType.FULLYOBSERVED) {
    			successfulRuns ++;
    			if (successfulRuns == max) {
    				return false;
    			}
    		}
    	}
		return true;
	}

	/**
      * An SB has completed an execution. Check that its repeat count is met and if so 
      * its status to complete. If not set it back to ready.
      */
    public void logSBComplete(ExecBlock eb) {
        
      //  try {
        ProjectStatusI ps;
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
//            archive.printProjectStatusFromObject(ps);
//            archive.printProjectStatusFromArchive(ps.getProjectStatusEntity().getEntityId());
            ps = updateSBStatusInProjectStatus(eb, completed.getStatus());
            //hack test need check more
            sbQueue.replace(completed);
            try {
            	statusQs.getProjectStatusQueue().updateProjectStatus(ps);
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
//            ps = updateObsUnitSetStatusStats(completed.getId(),eb, ps);
        }
        try {
        	statusQs.getProjectStatusQueue().updateProjectStatus(ps);
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
    private synchronized ProjectStatusI updateObsUnitSetStatusStats(String sb_id, 
                                                                    ExecBlock eb,
                                                                    ProjectStatusI ps) {
    	try {
    		logger.fine("SCHEDULING: about to update ObsUnitSetStatus for sb "+sb_id);
    		//top level obs unit set which is actually the ObsProgram.
    		//check if sb belongs here
    		final SB         sb     = sbQueue.get(sb_id);
    		final Program    p      = sb.getParent();
    		final OUSStatusI set    = ps.getObsProgramStatus();
    		final Status     status = eb.getStatus();

    		if (isSbInThisSet(sb_id, set)) {
    			if (status.isAborted() || status.isFailed()) {
    				int x = set.getNumberSBsFailed();
    				set.setNumberSBsFailed(x + 1);
    				p.setNumberSBsFailed(x +1);
    				logger.fine("aborted; x = "+ (x+1));
    			} else if(status.isObserved()) {
    				int x = set.getNumberSBsCompleted();
    				set.setNumberSBsCompleted(x + 1);
    				p.setNumberSBsCompleted(x +1);
    				logger.fine("completed; x = "+ (x+1));
    				//add for testing not very sure I can do this    
    			} else if(status.isComplete()) {
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

    			OUSStatusI[] sets = set.getOUSStatus();
    			findSet(sets, sb_id, eb, set, p);
    		}
    	} catch (Exception e) {
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
    private synchronized void findSet(OUSStatusI[] sets, String id, 
                                      ExecBlock eb, OUSStatusI parent, Program p){
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
                        sets[i].getStatus().setState(StatusTStateType.FULLYOBSERVED);
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
//            OUSStatusChoice choice = sets[i].getOUSStatusChoice(); 
            OUSStatusI[] sets2 = sets[i].getOUSStatus();
            findSet(sets2, id, eb, sets[i], p);
        }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
    /**
      * A method which checks if the given sb_id is in the given obs unit set status.
      */
    private synchronized boolean isSbInThisSet(String sb_id, OUSStatusI set) {
        if (set.getSBStatusCount() == 0){
            return false;
        } else {
            SBStatusI[] sbs;
			try {
				sbs = set.getSBStatus();
			} catch (SchedulingException e) {
				logger.warning(String.format(
						"Cannot get SBStatuses for OUSStatus %s: %s",
						set.getUID(),
						e.getLocalizedMessage()));
				return false;
			}
            for(int i=0; i < sbs.length; i++){
                if(sbs[i].getDomainEntityId().equals(sb_id)){
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
    
    private ProjectStatusI getProjectStatusForSB(SB sb){
        String proj_id = sb.getProject().getId();
        logger.fine ("SCHEDULING: getting project status for project ("+proj_id+")");
        ProjectStatusI ps = statusQs.getProjectStatusQueue().getStatusFromProjectId(proj_id);
        return ps;
    }
    
    /**
      */
    public synchronized ProjectStatusI updateSBStatusInProjectStatus(ExecBlock eb, Status sbStatus) {
    	SB sb = eb.getParent();
    	sb = sbQueue.get(sb.getId());
    	ProjectStatusI ps = getProjectStatusForSB(sb);
    	logger.finest("SCHEDULING: about to update sbStatus for "+sb.getId());
    	logger.finest("SCHEDULING: about to update PS::"+ps.getProjectStatusEntity().getEntityId());

    	//top level obs unit set which is actually the ObsProgram.
//    	OUSStatusChoice choice = statusQs.get(ps.getObsProgramStatusRef()).getOUSStatusChoice(); 
    	//ObsUnitSet
//    	OUSStatusChoice choice = statusQs.get(ps.getObsProgramStatusRef()).getOUSStatusChoice();
    	OUSStatusI   programStatus;
    	OUSStatusI[] sets;
    	SBStatusI[]  sbs;

    	
    	try {
			programStatus = ps.getObsProgramStatus();
	    	sets = programStatus.getOUSStatus();
	    	sbs  = programStatus.getSBStatus();
		} catch (SchedulingException e) {
			logger.warning(String.format(
					"Cannot update ProjectStatus for SchedBlock %s - %s",
					sb.getSchedBlockId(),
					e.getLocalizedMessage()));
			return ps;
		}

    	Vector<SBStatusI> foo = new Vector<SBStatusI>();
    	for(int i=0; i < sbs.length; i++){
    		foo.add(sbs[i]);
    	}
    	for(int i=0; i < sets.length; i++) {
    		foo = parseObsUnitSetStatus(sets[i], foo);
    	}
    	sbs = new SBStatusI[foo.size()];
    	sbs = foo.toArray(sbs);
    	SBStatusI status = getSBStatusMatch(sb, sbs);
    	addExecStatusAndDoBookkeeping(eb, status);
    	try {
    		logger.fine("SCHEDULING: SB's status, for SB "+status.getSchedBlockRef().getEntityId()+
    				" is "+sb.getStatus().getStatus());
    		StatusT     stat  = status.getStatus();
    		Enumeration e     = StatusTStateType.enumerate();
    		boolean     found = false;
    		
    		// Loop over the known statuses to find the one which
    		// matches the one in sbStatus. This would be much neater
    		// if StatusTStateType used the generics framework. 
    		while (e.hasMoreElements()) {
    			StatusTStateType stst = (StatusTStateType) e.nextElement();
    			if (sbStatus.getStatus().equals(stst.toString())) {
    				stat.setState(stst);
    				found = true;
    			}
    		}
    		if (!found) {
    			final String s = String.format(
    					"unrecognised status for SB: trying to set it to %s",
    					sb.getStatus());
    			logger.warning(s);
    			throw new Exception(s);
    		}

    		/*
    		 * TODO David, Lifecycle: commented out, replaced by the
    		 * above loop and check on "found".
    		 *
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
    		} else {
    			final String s = String.format(
    					"unrecognised status for SB: trying to set it to %s",
    					sb.getStatus());
    			logger.warning(s);
    			throw new Exception(s);
    		}
    		 *
    		 * end of comment out
    		 */
    		status.setStatus(stat);
    		logger.fine("SCHEDULING: SBStatus's status is "+status.getStatus().getState().toString());
    		logger.fine("SCHEDULING: got "+sbs.length+" sb status' in this PS");
    	} catch(Exception e){
    		e.printStackTrace();
    	}
    	return ps;
    }

    private SBStatusI getSBStatusMatch(SB sb, SBStatusI[] allSBs) {
        SBStatusI match=null;
        for(int i=0; i < allSBs.length; i++){
            if(allSBs[i].getSchedBlockRef().getEntityId().equals(sb.getId())){
                match = allSBs[i];
                break;
            }
        }
        return match;
    }

    
    
    /*
     * ================================================================
     * Status Entity updating
     * ================================================================
     */
    /**
     * Update the given ousStatus after a successful execution of an SB
     * within it. Percolate the relevant info up the OUSStatus
     * hierarchy.
     * 
     * @param ousStatus - the status object to update;
     * @param endTime - when the exec block finished;
     * @param timeInSec - the amount of observing time taken.
     * @param updateSBCounts - do we update the SB counts (for when its
     *                         the lowest level OUSStatus) or not?
     * 
     * @throws SchedulingException
     */
    private void updateForSuccess(OUSStatusI ousStatus,
    		                      DateTime   endTime,
    		                      int        timeInSec,
    		                      boolean    updateSBCounts)
				throws SchedulingException {
    	if (ousStatus != null) {
    		final int time   = ousStatus.getTotalUsedTimeInSec();

    		if (updateSBCounts) {
    			final int worked = ousStatus.getNumberSBsCompleted();
    			ousStatus.setNumberSBsCompleted(worked + 1);
    		}
    		ousStatus.setTotalUsedTimeInSec(time + timeInSec);
    		ousStatus.setTimeOfUpdate(endTime.toString());
    		updateForSuccess(ousStatus.getContainingObsUnitSet(),
    				         endTime,
    				         timeInSec,
    				         false);
    	}
    }
    
    /**
     * Update the given sbStatus after a successful execution of its
     * SB. Percolate the relevant info up the OUSStatus hierarchy.
     * 
     * @param sbStatus - the status object to update;
     * @param endTime - when the exec block finished;
     * @param timeInSec - the amount of observing time taken.
     * 
     * @throws SchedulingException
     */
    private void updateForSuccess(SBStatusI sbStatus,
    		                      DateTime  endTime,
    		                      int       timeInSec) {
    	final int execs = sbStatus.getExecutionsRemaining();
    	final int time  = sbStatus.getTotalUsedTimeInSec();
    	
    	if (execs > 0) {
    		// 0 used for indefinite repeat, so execs == 0 could happen
    		sbStatus.setExecutionsRemaining(execs-1);
    	}
        sbStatus.setTotalUsedTimeInSec(time + timeInSec);
        sbStatus.setTimeOfUpdate(endTime.toString());
    	try {
            updateForSuccess(sbStatus.getContainingObsUnitSet(),
            		         endTime,
            		         timeInSec,
            		         true);
    	} catch (SchedulingException e) {
    		logger.warning(String.format(
    				"Error updating SBStatus %s for SchedBlock %s: %s",
    				sbStatus.getUID(), sbStatus.getDomainEntityId(),
    				e.getLocalizedMessage()));
    	}
    }
    
    /**
     * Update the given ousStatus after an unsuccessful execution of an
     * SB within it. Percolate the relevant info up the OUSStatus
     * hierarchy.
     * 
     * @param ousStatus - the status object to update;
     * @param endTime - when the exec block finished;
     * @param updateSBCounts - do we update the SB counts (for when its
     *                         the lowest level OUSStatus) or not?
     * 
     * @throws SchedulingException
     */
    private void updateForFailure(OUSStatusI ousStatus,
    		                      DateTime   endTime,
    		                      boolean    updateSBCounts)
    			throws SchedulingException {
    	if (ousStatus != null) {
    		if (updateSBCounts) {
    			final int failed = ousStatus.getNumberSBsFailed();
    			ousStatus.setNumberSBsFailed(failed + 1);
    		}
    		ousStatus.setTimeOfUpdate(endTime.toString());
    		updateForFailure(ousStatus.getContainingObsUnitSet(),
    				         endTime,
    				         false);
    	}
    }
        
    /**
     * Update the given sbStatus after an unsuccessful execution of its
     * SB. Percolate the relevant info up the OUSStatus hierarchy.
     * 
     * @param sbStatus - the status object to update;
     * @param endTime - when the exec block finished;
     * 
     * @throws SchedulingException
     */
    private void updateForFailure(SBStatusI sbStatus, DateTime endTime) {
    	try {
    		updateForFailure(sbStatus.getContainingObsUnitSet(),
    				         endTime,
    				         true);
    	} catch (SchedulingException e) {
    		logger.warning(String.format(
    				"Error updating SBStatus %s for SchedBlock %s: %s",
    				sbStatus.getUID(), sbStatus.getDomainEntityId(),
    				e.getLocalizedMessage()));
    	}
    }
    /* End Status Entity updating
     * ============================================================= */
    
    
    
    private void addExecStatusAndDoBookkeeping(ExecBlock eb,
    		                                   SBStatusI sbStatus) {

        ExecStatusT es = new ExecStatusT();
        StatusT execStatus = new StatusT();
        ExecBlockRefT ref = new ExecBlockRefT();
        final DateTime startTime = eb.getStatus().getStartTime();
        final DateTime endTime   = eb.getStatus().getEndTime();
        
        ref.setExecBlockId(eb.getExecId());
        es.setExecBlockRef(ref);
        es.setArrayName(eb.getArrayName());
        es.setTimeOfCreation(startTime.toString());
        execStatus.setStartTime(startTime.toString());
        execStatus.setEndTime(endTime.toString());
        
        StatusTStateType state;
    	if(eb.getStatus().isComplete()){
    		state = StatusTStateType.FULLYOBSERVED;
            final int elapsedTime = (int)((endTime.getMillisec() - startTime.getMillisec()) / 1000);
            updateForSuccess(sbStatus, endTime, elapsedTime);
    	} else if( eb.getStatus().isAborted() ) {
    		state = StatusTStateType.PARTIALLYOBSERVED;
            updateForFailure(sbStatus, endTime);
    	} else if( eb.getStatus().isFailed() ) {
    		state = StatusTStateType.BROKEN;
            updateForFailure(sbStatus, endTime);
    	} else {
    		state = StatusTStateType.BROKEN;
    		final String s = String.format(
    				"unrecognised status for SB: trying to set it to %s, will set it to %s",
    				eb.getStatus().getStatus());
    		logger.warning(s);
            updateForFailure(sbStatus, endTime);
    	}
        execStatus.setState(state);
        es.setStatus(execStatus);
        sbStatus.addExecStatus(es);
    }

 
     //TODO: Rename this method.
    public Vector<SBStatusI> parseObsUnitSetStatus(OUSStatusI set, Vector<SBStatusI> v) {
        logger.finest("SCHEDULING: Set PartID = "+set.getOUSStatusEntity().getEntityId());
        if(set.getOUSStatusCount() > 0) {
        	logger.finest("SCHEDULING: more than one obs unit set status in PS");
        	try {
				for (final OUSStatusI child : set.getOUSStatus()) {
					v = parseObsUnitSetStatus(child, v);
				}
			} catch (SchedulingException e) {
    			logger.warning(String.format(
    					"Cannot get OUSStatuses for OUSStatus %s - %s",
    					set.getOUSStatusEntity().getEntityId(),
    					e.getLocalizedMessage()));
			}
        }
        if(set.getOUSStatusCount() > 0) {
        	try {
        		for (final SBStatusI child : set.getSBStatus()) {
        			v.add(child);
        		}
        	} catch (SchedulingException e) {
        		logger.warning(String.format(
        				"Cannot get SBStatuses for OUSStatus %s - %s",
        				set.getOUSStatusEntity().getEntityId(),
        				e.getLocalizedMessage()));
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

        final Program p = ((SB)sbQueue.get(sbid)).getParent();
        final OUSStatusI ouss = statusQs.getOUSStatusQueue().get(p.getOUSStatusId());
        
        ObservedSession session = new ObservedSession();
        session.setSessionId(eb.getSessionId());
        session.setStartTime(new DateTime(System.currentTimeMillis()));
        session.addExec(eb);
        p.addObservedSession(session);
        logger.fine("the program(SB) now has "+p.getAllSession().length + " sessions");
        
        final SessionT sesh = createNewSessionStatus(session);
        sesh.addExecBlockRef(createRefTo(eb));
        logger.fine(String.format(
        		"SchedBlock %s, OUS %s, OUSStatus %s, ouss is %s",
        		sbid, p.getProgramId(), p.getOUSStatusId(),
        		ouss == null? "null": ouss.getUID()));
        ouss.addSession(sesh);
        
        //Project proj = pQueue.get(p.getProject().getId());
        Program prog =  addProgram(p);
        session.setProgram(p);
        logger.fine("This program now has "+prog.getAllSession().length+" sessions");
        Project proj = prog.getProject();
        if(proj == null) {
            logger.severe("SCHEDULING: project was null!!!"); //should never happen.
            //throw new Exception("SCHEDULING: Error with project structure!"); TODO Add this eventually
        }
    }

    private SessionT createNewSessionStatus(ObservedSession session) {
		final SessionT result = new SessionT();
		result.setEntityPartId(session.getSessionId());
		result.setStartTime(session.getStartTime().toString());
		result.setEndTime("End time not known yet.");
		result.clearExecBlockRef();
		
		return result;
	}

    private ExecBlockRefT createRefTo(ExecBlock eb) {
		final ExecBlockRefT result = new ExecBlockRefT();
		result.setExecBlockId(eb.getExecId());
		
		return result;
	}

	/**
      * Updates the observng session information.
      */
    public synchronized void updateObservedSession(Project p, ProjectStatusI ps, String sessionId, String endTime){
        logger.finest("SCHEDULING: updating session with end time.");
        try {
        	Program program= searchPrograms(p.getProgram(), sessionId);
        	
        	if(program!=null) {
        		ObservedSession[] allSes = program.getAllSession();
        		ObservedSession ses=null;
                for(int i=0; i < allSes.length; i++){
                    if(allSes[i].getSessionId().equals(sessionId)){
                        ses = allSes[i];
                        ses.setEndTime(new DateTime(endTime));
                    }
                }
                ps = projectUtil.updateProjectStatus(p);
                statusQs.getProjectStatusQueue().updateProjectStatus(ps);
                archive.updateProjectStatus(ps);
        	}
        	else {
        		throw new Exception();
        	}
            
            
        } catch(Exception e){
            logger.severe("SCHEDULING: error updating PS with session");
            logger.severe("session "+sessionId + "can not find relative Obsunitset, check ProjectStatus");
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
            if(prog!=null)
            	return prog;
        }
        return prog;
    }
    
    /**
     * Return a new status proxy for the supplied SBStatusI which is a
     * synched to the remote storage. Also go up the OUSStatusI
     * hierarchy replacing them all in the supplied status queue bundle
     * with synched equivalents. Finally, do the same for the
     * ProjectStatusI in which all these statuses reside. Whilst we're
     * doing all that, we also make sure that the status entities we
     * traverse are initialised.
     * 
     * @param sbs - the SBStatusI from which we start out
     * @param statusQs - the status queues in which we operate.
     * 
     * @return a proxy for the same object for which sbs is a proxy,
     *         but one which is synched with the state archive.
     */
    private SBStatusI linkHierarchyToStateArchive(
    		SBStatusI               sbs,
    		StatusEntityQueueBundle statusQs) {
    	
    	// Replace sbs with a synched version
    	final SBStatusI result = sbs.asRemote();
    	statusQs.getSBStatusQueue().replace(result);
    	
    	try {
			archive.ensureStatusIsInitialised(result);
		} catch (SchedulingException e) {
			logger.warning(String.format(
					"Internal logic failure with SBStatus %s - %s",
					sbs.getSBStatusEntity().getEntityId(),
					e.getLocalizedMessage()));
		}
    	
    	// Climb the OUSStatusI hierarchy replacing them with synched
    	OUSStatusI ouss = null;
		try {
			ouss = result.getContainingObsUnitSet();
		} catch (SchedulingException e) {
			logger.warning(String.format(
					"Cannot get containing OUSStatus for SBStatus %s - %s",
					sbs.getSBStatusEntity().getEntityId(),
					e.getLocalizedMessage()));
		}

		// As we climb the OUSStatus hierarchy, we leave a trail of
		// breadcrumbs to follow back down later whilst initialising
		// them. The bottom-most OUSStatus will be at the start of
		// the trail (i.e. index 0).
		final List<OUSStatusI> breadcrumbs = new ArrayList<OUSStatusI>();
		
		while (ouss != null) {
			ouss = ouss.asRemote();
			statusQs.getOUSStatusQueue().replace(ouss);
			breadcrumbs.add(ouss);
			try {
				ouss = ouss.getContainingObsUnitSet();
			} catch (SchedulingException e) {
				logger.warning(String.format(
						"Cannot get containing OUSStatus for OUSStatus %s - %s",
						ouss.getOUSStatusEntity().getEntityId(),
						e.getLocalizedMessage()));
				ouss = null;
			}
		}
		
    	// Replace the ProjectStatusI in which this all resides
    	ProjectStatusI ps = null;
		try {
			ps = result.getProjectStatus().asRemote();
	    	statusQs.getProjectStatusQueue().replace(ps);
	    } catch (SchedulingException e) {
			logger.warning(String.format(
					"Cannot get ProjectStatus for SBStatus %s - %s",
					sbs.getSBStatusEntity().getEntityId(),
					e.getLocalizedMessage()));
		}
	    if (ps != null) {
	    	try {
	    		archive.ensureStatusIsInitialised(ps, breadcrumbs);
	    	} catch (SchedulingException e) {
	    		logger.warning(String.format(
	    				"Internal logic failure with ProjectStatus %s - %s",
	    				sbs.getSBStatusEntity().getEntityId(),
	    				e.getLocalizedMessage()));
	    	}
	    }
    	
    	// Return the replacement for sbs
    	return result;
    }

	/* Will be this way in future
    public void sendStartSessionEvent(ObservedSession session,String arrayName) {
    }
    */
    public IDLEntityRef sendStartSessionEvent(String sbid,String arrayName) {
        logger.fine(String.format(
        		"start of ALMAProjectManager.sendStartSessionEvent(%s, %s)",
        		sbid,
        		arrayName));
        
        final SB      sb    = sbQueue.get(sbid);
        SBStatusI     sbs;
        final String  sbsId = sb.getSbStatusId();
        
        if (sbsId == null) {
        	sbs = statusQs.getSBStatusQueue().getStatusFromSBId(sbid);
        	sb.setSbStatusId(sbs.getUID());
        } else {
        	sbs = statusQs.getSBStatusQueue().get(sb.getSbStatusId());
        }
        sbs = linkHierarchyToStateArchive(sbs, statusQs);
        
        OUSStatusI ouss;
		try {
			ouss = sbs.getContainingObsUnitSet();
		} catch (SchedulingException e) {
			logger.warning(String.format(
					"Cannot send StartSessionEvent for SchedBlock %s on array %s - error getting containing OUSStatus for SBStatus %s - %s",
					sbid, arrayName,
					sbs.getSBStatusEntity().getEntityId(),
					e.getLocalizedMessage()));
			return null;
		}
        final OUSStatusEntityT oussEntity = ouss.getOUSStatusEntity();
        
        logger.fine(String.format(
        		"SBStatusI  %s for SB  %s at %h: %s",
        		sbs.getUID(),
        		sbs.getSchedBlockRef().getEntityId(),
        		sbs.hashCode(),
        		sbs.getClass().getSimpleName()));
        logger.fine(String.format(
        		"OUSStatusI %s for OUS %s at %h; %s",
        		ouss.getUID(),
        		ouss.getOUSStatusEntity().getEntityId(),
        		ouss.hashCode(),
        		ouss.getClass().getSimpleName()));

        String ArrayName = arrayName;
        //in future will be done in scheduler.
        //ObservedSession session = createObservedSession(sb.getParent(),eb);
        //session.addExec(eb);
        //the entity which contains the session is the OUS status
        String sessionId = new String(projectUtil.genPartId());
        sessionStart(sessionId, sbid);
        
        IDLEntityRef sessionRef = new IDLEntityRef();

        sessionRef.entityId = oussEntity.getEntityId();
        sessionRef.partId = sessionId;
        sessionRef.entityTypeName = oussEntity.getEntityTypeName();
        sessionRef.instanceVersion ="1.0";
        logger.fine("OUSStatus for SB ("+sb.getId()+") is "+sessionRef.entityId);
          
        IDLEntityRef sbRef = new IDLEntityRef();
        sbRef.entityId = sbid;
        sbRef.partId ="";
        sbRef.entityTypeName  = "SchedBlock";
        sbRef.instanceVersion = "1.0";
        
        //try and tell quicklook pipeline a session is about to start
        String title="";
        if(!sb.getProject().getProjectName().equals("")){
            title = sb.getProject().getProjectName();
        }else {
            title = "undefined_project_name";
        }
        if(!sb.getSBName().equals("")){
            title = title + " " + sb.getSBName();
        } else {
            title = title + " undefined_sb_name";
        }
        logger.fine("SCHEDULING: title for quicklook = "+title);
        if(sb.isRunQuicklook()){
        	try {
        		pipeline.startQuickLookSession(sessionRef, sbRef, ArrayName,title);
        	} catch (Exception e){
        		if(pipeline.getQlSessionState(sessionRef, sbRef)!=null) {
        	    pipeline.shutdownQlSession(sessionRef, sbRef);
        		logger.warning("SCHEDULING: Quick look not available.");
        		}
        	}
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
        final ProjectStatusI ps   = statusQs.getProjectStatusQueue().getStatusFromProjectId(projectid);
        final SBStatusI      sbs  = statusQs.getSBStatusQueue().getStatusFromSBId(sbid);
        OUSStatusI ouss;
		try {
			ouss = sbs.getContainingObsUnitSet();
		} catch (SchedulingException e) {
			logger.warning(String.format(
					"Cannot send EndSessionEvent for ExecBlock %s - error getting containing OUSStatuses for SBStatus %s (for SchedBlock %s) - %s",
					eb.getId(),
					sbs.getSBStatusEntity().getEntityId(),
					sbid,
					e.getLocalizedMessage()));
			return;
		}
        
        logger.fine(String.format(
        		"SBStatusI  %s for SB  %s at %h: %s",
        		sbs.getUID(),
        		sbs.getSchedBlockRef().getEntityId(),
        		sbs.hashCode(),
        		sbs.getClass().getSimpleName()));
        logger.fine(String.format(
        		"OUSStatusI %s for OUS %s at %h; %s",
        		ouss.getUID(),
        		ouss.getOUSStatusEntity().getEntityId(),
        		ouss.hashCode(),
        		ouss.getClass().getSimpleName()));

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
        if(sb.isRunQuicklook() && (pipeline.getQlSessionState(sessionRef, sbRef)!=null)){
        	pipeline.endQuickLookSession(sessionRef, sbRef);
        }
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
        final String sbid = ((SB)eb.getParent()).getId();
        final SB               sb         = sbQueue.get(sbid);
        final SBStatusI        sbs        = statusQs.getSBStatusQueue().get(sb.getSbStatusId());
        OUSStatusI ouss;
		try {
			ouss = sbs.getContainingObsUnitSet();
		} catch (SchedulingException e) {
			logger.warning(String.format(
					"Cannot find session for ExecBlock %s - error getting containing OUSStatuses for SBStatus %s (for SchedBlock %s) - %s",
					eb.getId(),
					sbs.getSBStatusEntity().getEntityId(),
					sbid,
					e.getLocalizedMessage()));
			return null;
		}

        if(ouss == null) {
            logger.severe("SCHEDULING: PM: returned set is null! (looking for session)");
        }
        SessionT[] sessions = ouss.getSession();
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
      * Returns true if a session in this group is the one associated with
      * this exec block!
      */
    private boolean sessionExists(ExecBlock eb, SessionT[] all) {
        boolean result=false;
        String execid = eb.getExecId();
        logger.finer(String.format("Looking for ExecBlock %s in Sessions", execid));
        for(int i=0; i < all.length; i++) {
            ExecBlockRefT[] execblocks = all[i].getExecBlockRef();
            logger.finer(String.format(
            		"\tLooking in SessionT[%d] started %s, found %d ExecBlockRefT%s",
            		i,
            		all[i].getStartTime(),
            		execblocks.length,
            		execblocks.length==1? "": "s"));
            for(int j=0; j < execblocks.length; j++){
                logger.finer(String.format(
                		"\t\tchecking ExecBlock[%d] %s",
                		j,
                		execblocks[j].getExecBlockId()));
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
        ProjectStatusI ps = statusQs.getProjectStatusQueue().getStatusFromProjectId(proj.getId());
        try {
            ps = projectUtil.updateProjectStatus(proj);
            statusQs.getProjectStatusQueue().updateProjectStatus(ps);
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
        ProjectStatusI ps = statusQs.getProjectStatusQueue().getStatusFromProjectId(proj.getId());

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
    	if(schema.equals("SchedBlock")) {
    		archivePoller.pollArchive();
    	}
    	
        String[] tmp = archive.query(query, schema);
        Vector v_uids = new Vector();
        if(schema.equals("ObsProject")) {
            for(int i=0;i< tmp.length; i++) {
                logger.finest("proj. id = "+tmp[i]);
                if(getProjectQueue().isExists(tmp[i])){
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
            p= getProjectQueue().get(projIds[i]);
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
            SB[] projectSBs = ((Project)getProjectQueue().get(projectIds[i])).getAllSBs();

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
    	final List<SB> sbs = new ArrayList<SB>();
    	for (final SB sb : sbQueue.getAll()) {
    		if (sb.getProject().getId().equals(projId)) {
    			sbs.add(sb);
    		}
    	}
        SB[] result = new SB[sbs.size()];
        return sbs.toArray(result);
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
        // getUpdates();
        ProjectLite p = createProjectLite(sb.getProject().getId());
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
    public class ArchivePoller {

    	private final AcsLogger               logger;
        private final ALMAArchive             archive;
        private final SBQueue                 sbQueue;
        private final ProjectQueue            projectQueue;
        private final StatusEntityQueueBundle statusQs;
		private final ProjectUtil             projectUtil;
    	
    	public ArchivePoller(ALMAArchive             archive,
    			             SBQueue                 sbQueue,
    			             ProjectQueue            projectQueue,
    			             StatusEntityQueueBundle statusQs,
    			             ProjectUtil             projectUtil,
    			             AcsLogger               logger) {
    		this.logger       = logger;
    		this.projectUtil  = projectUtil;
    		this.archive      = archive;
    		this.sbQueue      = sbQueue;
    		this.projectQueue = projectQueue;
    		this.statusQs     = statusQs;
    	}
		
	    /**
	     * Update the <code>sbQueue</code> & <code>projectQueue</code>
	     * so that they match the given status objects in the supplied
	     * StatusEntityQueueBundle
	     * 
	     * Does this by looping over the SBStatuses and the
	     * ProjectStatuses checking to see if their domain equivalents
	     * are in <code>sbQueue</code> or <code>ProjectQueue</code>
	     * and, if they're not, fetching them from the archive,
	     * wrapping them and putting them in the queues. In the mean-
	     * time we remember the IDs of all the SchedBlocks and
	     * ObsProjects that are in use so we can chuck out the ones
	     * we don't
	     * 
	     * TODO: Check what happens about the currently running SB. 
	     * 
	     * @param newQs - the status entities corresponding to the SBs
	     *                and projects we care about.
	     * @throws SchedulingException 
	     */
//	    public void updateProjectsAndSBsToMatch(StatusEntityQueueBundle statusQs)
//	    		throws SchedulingException {
//	    	// Start with the ProjectStatuses and SBStatuses which are
//	    	// in runnable states
//	    	final ProjectStatusQueue runnablePSs  =
//	    		archive.getProjectStatusesByState(OPRunnableStates);
//	    	final SBStatusQueue runnableSBSs =
//	    		archive.getSBStatusesByState(SBRunnableStates);
//	    	
//	    	// Find SBStatuses which are not part of a runnable project
//	    	// and ProjectStatuses which have at least one SBStatus
//	    	// associated with them.
//	    	final Set<String> sbsIdsToRemove = new HashSet<String>();
//	    	final Set<String> psIdsToKeep    = new HashSet<String>();
//	    	for (final String sbsId : runnableSBSs.getAllIds()) {
//	    		final SBStatus sbs  = runnableSBSs.get(sbsId);
//	    		final String   psId = sbs.getProjectStatusRef().getEntityId();
//	    		
//	    		if (runnablePSs.isExists(psId)) {
//	    			// The SBStatus is in a ProjectStatus we know about, so keep both
//	    			psIdsToKeep.add(psId);
//	    		} else {
//	    			// The SBStatus is not in a ProjectStatus we know about, so dump it
//	    			sbsIdsToRemove.add(sbsId);
//	    		}
//	    	}
//	    	
//	    	// Now remove the SBStatuses that we have just determined are not part
//	    	// of a runnable ProjectStatus.
//	    	for (final String sbsId : sbsIdsToRemove) {
//	    		runnableSBSs.remove(sbsId);
//	    	}
//	    	
//	    	// Also remove the ProjectStatuses that we have just determined do not
//	    	// any runnable SBStatuses. As we've remembered the ones to keep rather
//	    	// than to remove, then we need to do a bit of Set complementing first.
//	    	final Set<String> psIdsToRemove = new HashSet<String>(runnablePSs.getAllIds());
//	    	psIdsToRemove.removeAll(psIdsToKeep);
//	    	for (final String sbsId : sbsIdsToRemove) {
//	    		runnableSBSs.remove(sbsId);
//	    	}
//	    	
//	    	// Finally, get the OUSStatuses corresponding to the
//	    	// ProjectStatuses we're left with.
//	    	final OUSStatusQueue runnableOUSs =
//	    		archive.getOUSStatusesFor(runnablePSs);
//	    	return new StatusEntityQueueBundle(runnablePSs, runnableOUSs, runnableSBSs);
//	    }
		
	    
	    /**
		  * polls the archive for new/updated projects
		  * then updates the queues (project queue, sb queue & project
		  * status queue). As a stop-gap for the Lifecycle II FBT this
		  * uses a slightly tweaked version of the old pollArchive to
		  * do all the jiggering about with the project and status
		  * block queues.
		  */
		void pollArchive() throws SchedulingException {
//			logNumbers("at start of pollArchive");

			logger.fine("SCHEDULING: polling archive for runnable projects");
			archive.convertProjects(StatusTStateType.PHASE2SUBMITTED,
			                        StatusTStateType.READY);
			final StatusEntityQueueBundle newQs =
				archive.determineRunnablesByStatus(OPRunnableStates,
						                           SBRunnableStates);
			statusQs.updateWith(newQs);
			
//			updateProjectsAndSBsToMatch(statusQs);
			OLD_PollArchive();
			logger
			.logToAudience(Level.INFO,
					"The Scheduling Subsystem is currently managing "
							+ projectQueue.size() + " projects, "
							+ sbQueue.size() + " SBs, "
							+ statusQs.getProjectStatusQueue().size() + " project statuses and "
							+ statusQs.getSBStatusQueue().size() + " SB statuses",
					OPERATOR.value);
			logNumbers("at end of pollArchive");
			logDetails("at end of pollArchive");
		}

		/**
		 * Make sure that all the SBs in the given collection point to
		 * their SBStatus.
		 * 
		 * @param schedBlocks
		 */
		private void pointSBsAtTheirStatuses(List<SB> schedBlocks) {
			final SBStatusQueue q = statusQs.getSBStatusQueue();
			
			for (final SB sb : schedBlocks) {
				final SBStatusI sbs = q.getStatusFromSBId(sb.getId());
				if (sbs != null) {
					logger.fine(String.format("pointing SB %s at SBStatus %s (was pointing at %s)",
							sb.getSchedBlockId(), sbs.getUID(), sb.getSbStatusId()));
					sb.setSbStatusId(sbs.getUID());
				} else {
					logger.fine(String.format("No SBStatus for SB %s so not pointing it at anything (was pointing at %s)",
							sb.getSchedBlockId(), sb.getSbStatusId()));
				}
			}
		}

		/**
		 * Make sure that all the Programs associated with the Projects in
		 * in the given collection point to their OUSStatus.
		 * 
		 * @param schedBlocks
		 */
		private void pointOUSsAtTheirStatuses(List<Project> projects) {
			final OUSStatusQueue q = statusQs.getOUSStatusQueue();
			
			for (final Project proj : projects) {
				final String projectId = proj.getId();
				for (final Program p : proj.getAllPrograms()) {
					final String programId = p.getId();
					final OUSStatusI ouss = q.getStatusFromOUSId(projectId, programId);
					if (ouss != null) {
						logger.fine(String.format("pointing Program %s in %s at OUSStatus %s (was pointing at %s)",
								p.getId(), p.getProjectId(), ouss.getUID(), p.getOUSStatusId()));
						p.setOUSStatusId(ouss.getUID());
					} else {
						logger.fine(String.format("No OUSStatus for Program %s in %s so not pointing it at anything (was pointing at %s)",
								p.getId(), p.getProjectId(), p.getOUSStatusId()));
					}
				}
			}
		}


		/**
	      * polls the archive for new/updated projects
	      * then updates the queues (project queue, sb queue & project status queue)
	      */
        void OLD_PollArchive() throws SchedulingException {
            Project[] projectList = new Project[0];
            ProjectStatus ps;
            Vector<SB> tmpSBs = new Vector<SB>();
            final ProjectStatusQueue psQ  = statusQs.getProjectStatusQueue();
            final SBStatusQueue      sbsQ = statusQs.getSBStatusQueue();

            try {
                projectList = archive.getAllProject();
                logger.finest("ProjectList size =  " + projectList.length);
                ArrayList<Project> projects = new ArrayList<Project>(
                        projectList.length);
                for (final Project project : projectList) {
                    if (psQ.isExists(project.getProjectStatusId())) {
                        projects.add(project);
                        logger.fine(String.format(
                                "Including project %s (status is %s)",
                                project.getId(),
                                project.getStatus().getState()));
                    } else {
                        logger.fine(String.format(
                                "Rejecting project %s (not in status queue, status = %s)",
                                project.getId(),
                                project.getStatus().getState()));
                        AcsJObsProjectRejectedEx ex = new AcsJObsProjectRejectedEx();
                        ex.setProperty("UID", project.getId());
                        ex.setProperty("Reason", "Not in status queue");
                        ex.log(logger);
                    }
                }

                logger.finest("Projects size =  " + projects.size());
                for (final Project p : projects) {
                    SB[] sbs = archive.getSBsForProject(p.getId());
                    for (final SB sb : sbs) {
                        if (sbsQ.isExists(sb.getSbStatusId())) {
                            // Only worry about SBs for which there is
                            // a status (which means the SB is in a
                            // runnable state.
                            tmpSBs.add(sb);
                        } else {
                            logger.fine(String.format(
                                    "Rejecting SchedBlock %s (not in status queue, status = %s)",
                                    sb.getId(),
                                    sb.getStatus().getState()));
                            AcsJSchedBlockRejectedEx ex = new AcsJSchedBlockRejectedEx();
                            ex.setProperty("UID", sb.getId());
                            ex.setProperty("Reason", "Not in status queue");
                            ex.log(logger);
                        }
                    }
                }

                logger.finest("projects = " + projects.size());
                logger.finest("tmp sbs " + tmpSBs.size());
//              pointOUSsAtTheirStatuses(projects);
//              pointSBsAtTheirStatuses(tmpSBs);

                // For all the stuff gotten above from the archive, determine if
                // they are new (then add them), if they are updated (then
                // updated) or the same (then do nothing)
                for (final Project newProject : projects) {

                    // does project exist in queue?
                    if (projectQueue.isExists(newProject.getId())) {
                        final Project oldProject = projectQueue.get(newProject.getId());
                        // logger.finest("(old project)number of program in
                        // pollarchive:"+oldProject.getAllSBs().length);
                        // yes it is so check if project needs to be updated,
                        // check if
                        if (newProject.getTimeOfUpdate().compareTo(
                                oldProject.getTimeOfUpdate()) == 1) {
                            // needs updating
                            projectQueue.replace(newProject);
                        } else if (newProject.getTimeOfUpdate().compareTo(
                                oldProject.getTimeOfUpdate()) == 0) {
                            // DO NOTHING hasn't been updated
                        } else if (newProject.getTimeOfUpdate().compareTo(
                                oldProject.getTimeOfUpdate()) == -1) {
                            // TODO should throw an error coz the old project
                            // has been updated and the new one hasnt
                        } else {
                            // TODO Throw an error here
                        }

                        // TODO if the sbs need updating and if there are new
                        // ones to add
                        SB[] currSBs = getSBs(tmpSBs, newProject.getId());
                        SB newSB, oldSB;
                        for (int j = 0; j < currSBs.length; j++) {
                            newSB = currSBs[j];
                            if (sbQueue.isExists(newSB.getId())) {
                                logger.finest("Sb not new");
                                oldSB = sbQueue.get(newSB.getId());

                                // check if it needs to be updated, if yes then
                                // update
                                if (newSB.getTimeOfUpdate().compareTo(
                                        oldSB.getTimeOfUpdate()) == 1) {
                                    logger.finest("Sb needs updating");
                                    sbQueue.replace(newSB);
                                    projectQueue.replace(newProject);
                                } else if (newSB.getTimeOfUpdate().compareTo(
                                        oldSB.getTimeOfUpdate()) == 0) {
                                    // DO NOTHING, hasn't been updated
                                } else if (newSB.getTimeOfUpdate().compareTo(
                                        oldSB.getTimeOfUpdate()) == -1) {
                                    // TODO should throw an error coz the old sb
                                    // has been updated and the new one hasnt
                                } else {
                                    // TODO Throw an error
                                }
                            } else {
                                // not in queue, so add it.
                                logger.finest("SB new, adding");
                                sbQueue.add(newSB);
                                projectQueue.replace(newProject);
                            }
                        }
                    } else {
                        logger.finest("Project new, adding");
                        // no it isn't so add project to queue,
                        projectQueue.add(newProject);
                        // and sbs to sbqueue
                        SB[] schedBlocks = getSBs(tmpSBs, newProject.getId());
                        if (schedBlocks.length > 0) {
                            sbQueue.add(schedBlocks);
                            Program p = (schedBlocks[0]).getParent();
                            logger.finest("Program's session " + p.getId()
                                    + "has " + p.getNumberSession()
                                    + " session");
                        } else {
                            logger
                                    .info("HSO hotfix 2008-06-07: new project "
                                            + newProject.getId()
                                            + " does not have any schedblocks. Not sure if this is OK.");
                        }
                    }
                }

                checkSBUpdates();
            } catch (Exception e) {
                e.printStackTrace();
                throw new SchedulingException(e);
            }
            logger
                    .logToAudience(Level.INFO,
                            "The Scheduling Subsystem is currently managing "
                                    + projectQueue.size() + " projects, "
                                    + sbQueue.size() + " SBs, "
                                    + statusQs.getProjectStatusQueue().size() + " project statuses and "
                                    + statusQs.getSBStatusQueue().size() + " SB statuses",
                            OPERATOR.value);
        }

        private void logNumbers(String when) {
            logger.fine(String.format(
                    "ObsProject    queue size %s = %d",
                    when,
                    projectQueue.size()));
            logger.fine(String.format(
                    "SchedBlock    queue size %s = %d",
                    when,
                    sbQueue.size()));
            logger.fine(String.format(
                    "ProjectStatus queue size %s = %d",
                    when,
                    statusQs.getProjectStatusQueue().size()));
            logger.fine(String.format(
                    "OUSStatus     queue size %s = %d",
                    when,
                    statusQs.getOUSStatusQueue().size()));
            logger.fine(String.format(
                    "SBStatus      queue size %s = %d",
                    when,
                    statusQs.getSBStatusQueue().size()));
        }
        
        private void logDetails(String when) {
            logProjectsAndStatuses(projectQueue, statusQs.getProjectStatusQueue());
            logOUSsAndStatuses(projectQueue, statusQs.getOUSStatusQueue());
            logSBsAndStatuses(sbQueue, statusQs.getSBStatusQueue());
        }

        private void logProjectsAndStatuses(ProjectQueue       domainQueue,
                ProjectStatusQueue statusQueue) {
            final StringBuilder b = new StringBuilder();
            final Formatter     f = new Formatter(b);

            try {
                f.format("%nProjects and Statuses in queues%n");

                final SortedSet<String> haveLogged = new TreeSet<String>();

                f.format("Domain to Status%n");
                for (final Project p : domainQueue.getAll()) {
                    final String domainId = p.getObsProjectId();
                    final String statusId = p.getProjectStatusId();
                    f.format("\tProject ID = %s, Status ID = %s", domainId, statusId);
                    final ProjectStatusI status = statusQueue.get(statusId);
                    if (status != null) {
                        final EntityRefT ref = status.getObsProjectRef();
                        if (ref != null) {
                            final String id = ref.getEntityId();
                            if (id.equals(domainId)) {
                                f.format(", status and domain ids match");
                            } else {
                                f.format(", loopback and domain id MISMATCH %s vs %s",
                                        id, domainId);
                            }
                        } else {
                            f.format(", status has missing domain reference");
                        }
                    } else {
                        f.format(", status object NOT IN QUEUE!");
                    }
                    f.format("%n");
                    if (statusId != null) {
                        haveLogged.add(statusId);
                    }
                }

                f.format("Status to Domain (skipping statuses already logged)%n");
                for (final ProjectStatusI ps : statusQueue.getAll()) {
                    final String statusId = ps.getUID();
                    if (!haveLogged.contains(statusId)) {
                        f.format("\tStatus ID = %s", statusId);
                        final EntityRefT ref = ps.getObsProjectRef();

                        if (ref != null) {
                            f.format(", ObsProject ID = %s", ref.getEntityId());
                        } else {
                            f.format(", status has missing domain reference");
                        }
                        f.format("%n");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                logger.info(b.toString());
            }
        }

        private void logOUSsAndStatuses(ProjectQueue   domainQueue,
                OUSStatusQueue statusQueue) {
            final StringBuilder b = new StringBuilder();
            final Formatter     f = new Formatter(b);
            try {
                f.format("%nOUSs and Statuses in queues%n");

                final SortedSet<String> haveLogged = new TreeSet<String>();

                f.format("Domain to Status%n");
                for (final Project proj : domainQueue.getAll()) {
                    final String projectId = proj.getObsProjectId();
                    for (final Program p : proj.getAllPrograms()) {
                        final String domainId = p.getProgramId();
                        final String statusId = p.getOUSStatusId();
                        f.format("\tOUS ID = %s in %s, Status ID = %s", domainId, projectId, statusId);
                        final OUSStatusI status = statusQueue.get(statusId);
                        if (status != null) {
                            final EntityRefT ref = status.getObsUnitSetRef();
                            if (ref != null) {
                                final String id = ref.getEntityId();
                                final String part = ref.getPartId();
                                if (id.equals(projectId) && part.equals(domainId)) {
                                    f.format(", status and domain ids match");
                                } else {
                                    f.format(", loopback and domain id MISMATCH %s in %s vs %s in %s",
                                            part, id, domainId, projectId);
                                }
                            } else {
                                f.format(", status has missing domain reference");
                            }
                        } else {
                            f.format(", status object NOT IN QUEUE!");
                        }
                        f.format("%n");
                        if (statusId != null) {
                            haveLogged.add(statusId);
                        }
                    }
                }

                f.format("Status to Domain (skipping statuses already logged)%n");
                for (final OUSStatusI ps : statusQueue.getAll()) {
                    final String statusId = ps.getUID();
                    if (!haveLogged.contains(statusId)) {
                        f.format("\tStatus ID = %s", statusId);
                        final EntityRefT ref = ps.getObsUnitSetRef();

                        if (ref != null) {
                            f.format(", OUS ID = %s in %s", ref.getPartId(), ref.getEntityId());
                        } else {
                            f.format(", status has missing domain reference");
                        }
                        f.format("%n");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                logger.info(b.toString());
            }
        }


        private void logSBsAndStatuses(SBQueue       domainQueue,
                SBStatusQueue statusQueue) {
            final StringBuilder b = new StringBuilder();
            final Formatter     f = new Formatter(b);

            try {
                f.format("%nSBs and Statuses in queues%n");

                final SortedSet<String> haveLogged = new TreeSet<String>();

                f.format("Domain to Status%n");
                for (final SB p : domainQueue.getAll()) {
                    final String domainId = p.getSchedBlockId();
                    final String statusId = p.getSbStatusId();
                    f.format("\tSchedBlock ID = %s, Status ID = %s", domainId, statusId);
                    final SBStatusI status = statusQueue.get(statusId);
                    if (status != null) {
                        final EntityRefT ref = status.getSchedBlockRef();
                        if (ref != null) {
                            final String id = ref.getEntityId();
                            if (id.equals(domainId)) {
                                f.format(", status and domain ids match");
                            } else {
                                f.format(", loopback and domain id MISMATCH %s vs %s",
                                        id, domainId);
                            }
                        } else {
                            f.format(", status has missing domain reference");
                        }
                    } else {
                        f.format(", status object NOT IN QUEUE!");
                    }
                    f.format("%n");
                    if (statusId != null) {
                        haveLogged.add(statusId);
                    }
                }

                f.format("Status to Domain (skipping statuses already logged)%n");
                for (final SBStatusI ps : statusQueue.getAll()) {
                    final String statusId = ps.getUID();
                    if (!haveLogged.contains(statusId)) {
                        f.format("\tStatus ID = %s", statusId);
                        final EntityRefT ref = ps.getSchedBlockRef();

                        if (ref != null) {
                            f.format(", SchedBlock ID = %s", ref.getEntityId());
                        } else {
                            f.format(", status has missing domain reference");
                        }
                        f.format("%n");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally{
                logger.info(b.toString());
            }
        }
    }

    /**
      * Ask the archive for any updated SBs since the last query time
      * update any new ones in the queue.
      */
    public  void checkSBUpdates() throws SchedulingException {
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
      * Removes the project status element with the given id from the vector 
      * and returns the new vector.
      * To be used only with pollArchive and the vector holding the ProjectStatus'.
      * @param v The Vector holind all the projectStatus gotten during a pollArchive
      * @param s The id of the project status to be removed.
      * @return Vector REturn the vector minus one element
      */
    private static Vector removePSElement(Vector v, String s) {
        for(int i=0; i < v.size(); i++){
            if(((ProjectStatusI)v.elementAt(i)).getProjectStatusEntity().
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
      * @return ProjectStatusI The project status with the given id.
      */
    private static ProjectStatusI getPS(Vector v, String s) {
        ProjectStatusI ps=null;
        for(int i=0; i < v.size(); i++) {
            ps = (ProjectStatusI)v.elementAt(i);
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

    protected SBLite createSBLite(String id) {
        String sid,pid,sname,pname,pi,pri;
        double ra,dec,freq,score,success,rank;
        long maxT;
        SB sb = sbQueue.get(id);
        if (sb == null) {
        	return null;
        }
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
        SBStatusI sbs = statusQs.getSBStatusQueue().getStatusFromSBId(id);
        if (sbs == null) {
        	// For some reason the sbs is no longer in the queue, so we
        	// pretend that it's complete.
        	sblite.isComplete = true;
        } else {
        	// and SB is deemed complete if it's SUSPENDED as that's
        	// the state we put an SB into after an execution (unless
        	// we're operating in Full Auto mode in which case it might
        	// be READY, but deeming that complete seems a bad idea).
        	// We also deem FULLYOBSERVED as complete just in case the
        	// SB has been moved to that state after (a quick) QA0.
        	final StatusTStateType sbState = sbs.getStatus().getState();
        	sblite.isComplete = sbState.equals(StatusTStateType.SUSPENDED) ||
        	                    sbState.equals(StatusTStateType.FULLYOBSERVED);
        }
        
        // We may pass the all the ExecStatus to the Scheduling Plugin
        // Right now just displaying the XML for the SB Status
        if (sbs != null) {        	
        	StringWriter writer = new StringWriter();
		    try {
		    	sbs.marshal(writer);
//				logger.info("SB Status XML:\n" + writer.toString());
				sblite.statusXML = writer.toString();
			} catch (MarshalException ex) {
				ex.printStackTrace();
			} catch (ValidationException ex) {
				ex.printStackTrace();
			}
        } else {
        	sblite.statusXML = "";
        }
        
        if (sbs != null)
        	sblite.status = sbs.getStatus().getState().toString();
        else
        	sblite.status = "Not in Scheduling Queue";
        return sblite;
    }

    /*
     * TODO: David, Lifecycle: Observant readers will notice that while
     * they are similar, there is a crucial difference between
     * getProjectLites() and getSBLites(). This is that getSBLites()
     * polls the archive while getProjectLites() does not. We assume
     * that getSBLites() is called first.
     * 
     * Some refactoring required...
     */
    public ProjectLite[] getProjectLites(String projectName,
                                         String piName, 
                                         String projectType,
                                         String arrayType)
    			throws SchedulingException {
        logger.fine("SCHEDULING: Called getProjectLites()");
        
        // Compile the search strings
        final Pattern projectNamePattern = makePattern(projectName);
        final Pattern piNamePattern      = makePattern(piName);

        ProjectLite[] result = null;
        try {
        	Vector<ProjectLite> lites = new Vector<ProjectLite>();
        	
        	for (final Project project : getProjectQueue().getAll()) {
        		if (matches(project,
        				    projectNamePattern,
        				    piNamePattern,
        				    projectType,
        				    arrayType)) {
        			ProjectLite projectlite = createProjectLite(project.getId());
        			if (projectlite != null) {
        				lites.add(projectlite);
        			}
        		}
        	}
        	result = new ProjectLite[lites.size()];
        	result = lites.toArray(result);
        } catch(Exception e) {
	        logger.severe(e.toString());
            e.printStackTrace();
        }
        return result;
    }

    private boolean matches(Project project,
    		                Pattern projectNamePattern,
    		                Pattern piNamePattern,
    		                String  projectType,
    		                String  arrayType) {
    	if (!projectNamePattern.matcher(project.getProjectName()).matches()) {
    		return false;
    	}
    	if (!piNamePattern.matcher(project.getPI()).matches()) {
    		return false;
    	}
        if(!projectType.equals("All")){
        	if (!projectType.equals(project.getProjectType())) {
        		return false;
        	}
        }
        if(!arrayType.equals("All")){
        	if (!arrayType.equals(project.getArrayType())) {
        		return false;
        	}
        }
		return true;
	}

	private Pattern makePattern(String userPattern) throws SchedulingException {
    	String pattern;
    	
    	if (userPattern.equals("")) {
    		pattern = ".*";
    	} else {
    		// TODO David, lifecycle, do this properly if necessary by
    		// telling the users to use Java regexps.
    		pattern = userPattern.replaceAll("\\*", ".*");
    	}

    	Pattern result;
    	try {
    		result = Pattern.compile(pattern);
    	} catch (PatternSyntaxException e) {
    		throw new SchedulingException(String.format(
    				"Cannot parse regular expression %s", pattern), e);
    	}
		return result;
	}

	public SBLite[] getSBLites() {
        logger.fine("SCHEDULING: Called getSBLites()");
        SBLite[] sbliteArray=null;
        SBLite sblite;
        Vector<SBLite> sbliteVector = new Vector<SBLite>();
        try {
            archivePoller.pollArchive();
            Project[] projects = getProjectQueue().getAll();
            for(int i=0; i < projects.length; i++){
                //get all the sbs of this project
                SB[] sbs = projects[i].getAllSBs ();
                for(int j=0; j < sbs.length; j++) {
                    sblite = createSBLite(sbs[j].getId());
                    if (sblite != null) {
                    	sbliteVector.add(sblite);
                    }
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

	public SBLite[] getSBLites(String sbModeName,
                               String sbModeType) {
        logger.fine("SCHEDULING: Called getSBLites()");
        SBLite[] sbliteArray=null;
        SBLite sblite;
        Vector<SBLite> sbliteVector = new Vector<SBLite>();
        try {
            archivePoller.pollArchive();
            Project[] projects = getProjectQueue().getAll();
            for(int i=0; i < projects.length; i++){
                //get all the sbs of this project
                SB[] sbs = projects[i].getAllSBs ();
                for(int j=0; j < sbs.length; j++) {
                    if (matches(sbs[j], sbModeName, sbModeType)) {
                    	sblite = createSBLite(sbs[j].getId());
                    	if (sblite != null) {
                    		sbliteVector.add(sblite);
                    	}
                    }
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

    private boolean matches(SB sb, String sbModeName, String sbModeType) {
    	if(!sbModeName.equals("All")){
        	if (!sbModeName.equals(sb.getModeName())) {
        		return false;
        	}
        }
        if(!sbModeType.equals("All")){
        	if (!sbModeType.equals(sb.getModeType())) {
        		return false;
        	}
        }
		return true;
	}

	public SBLite[] getSBLite(String[] ids) {
//        try {
//            archivePoller.pollArchive();
//        } catch(Exception e) {
//            return null;
//        }
        List<SBLite> sblites = new ArrayList<SBLite>();
        for(int i=0; i < ids.length; i++){
            SBLite sblite;
            sblite = createSBLite(ids[i]);
            if (sblite != null)
            	sblites.add(sblite);
        }
        return sblites.toArray(new SBLite[0]);
    }

    public SBLite[] getExistingSBLite(String[] ids) {
        return archivePoller.getExistingSBLites(ids);
//        SBLite[] sblites = new SBLite[ids.length];
//        SBLite sblite;
//        for(int i=0; i < ids.length; i++){
//            sblite = createSBLite(ids[i]);
//            sblites[i] = sblite;
//        }
//        return sblites;
    }
    
    public ProjectLite[] getProjectLites(String[] ids) {
        return archivePoller.getProjectLites(ids);
//        //getUpdates();
//        logger.fine("SCHEDULING: Called getProjectLites(ids)");
//        ProjectLite[] projectliteArray=new ProjectLite[ids.length];
//        for(int i=0; i < ids.length; i++){
//            projectliteArray[i] = createProjectLite(ids[i]);
//
//        }
//        return projectliteArray;
    }

    protected ProjectLite createProjectLite(String id) {
        Project p = projectQueue.get(id);;
        ProjectLite projectlite= new ProjectLite();
        projectlite.uid = p.getId();
        projectlite.projectName = p.getProjectName();
        projectlite.piName = p.getPI();
        projectlite.version = p.getProjectVersion();
//        projectlite.status = p.getStatus().getStatus();
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
        ProjectStatusI ps = getPSForProject(p);
        projectlite.isComplete = isProjectComplete(ps);

        int sbcompl =
            statusQs.getOUSStatusQueue().get(ps.getObsProgramStatusRef()).getNumberSBsCompleted();
        projectlite.completeSBs = String.valueOf(sbcompl);
        
        int numsbfail = statusQs.getOUSStatusQueue().get(ps.getObsProgramStatusRef()).getNumberSBsFailed();
        projectlite.failedSBs = String.valueOf(numsbfail);
        
		if (ps != null ){
        	StringWriter writer = new StringWriter();
        	try{
        		ps.marshal(writer);
        		// logger.info("PS XML: " + writer.toString());
        		projectlite.statusXML = writer.toString();
        	}catch(MarshalException ex){
        		ex.printStackTrace();
        	}
        	catch(ValidationException ex){
        		ex.printStackTrace();
        	}
        } else {
        	projectlite.statusXML = "";
        }
		
        projectlite.status = ps.getStatus().getState().toString();
        logger.fine("Project Status: " + projectlite.status);
        
        return projectlite;
    }

    public SBQueue getSBQueue(){
        return sbQueue;
    }

    public void createProjectWebpage(String uid) {
        //start a new webpage for project with uid
        //email PI with webpage address
    }

    public ProjectStatusI getPSForProject(String p_id) {
        Project p = getProjectQueue().get(p_id);
        return getPSForProject(p);
    }

    public ProjectStatusI getPSForProject(Project p) {
        String ps_id = p.getProjectStatusId();
        ProjectStatusI ps = statusQs.getProjectStatusQueue().get(ps_id);
        
    	if (ps == null) {
    		try {
				ps = archive.getProjectStatus(p);
    			statusQs.getProjectStatusQueue().add(ps);
			} catch (SchedulingException e) {
	    		logger.warning(e.getLocalizedMessage());
	    		ps = null;
			}
    	}
        return ps;
    }
    
    public ProjectStatusI getPSForSB(String s_id){
        SB sb = sbQueue.get(s_id);
        return getPSForProject(sb.getProject().getId());
    }


    private boolean isProjectComplete(ProjectStatusI ps){
    	try {
    		return ps.getStatus().getState().toString().equals("complete");
    	} catch (NullPointerException e) {
    		return false;
    	}
    }
    

    protected IDLEntityRef startManualModeSession(String arrayName,String sbId) throws SchedulingException {
        IDLEntityRef refs = new IDLEntityRef();
        //query for uid of manual mode sb
        //String manualArrayName = arrayName;
        //String p_id = archive.queryForManualModeProject();
        //Project p = pQueue.get(p_id);
        //SB[] sbs = p.getAllSBs();
        //if(sbs.length < 1 ){
        //    throw new SchedulingException("SCHEDULING: Manual Mode project had not SB!");
        //}
        //String sbid = sbs[0].getId();
        //refs[0] = new IDLEntityRef();
        //refs[0].entityId = sbid;
        //refs[0].partId = "";
        //refs[0].entityTypeName="SchedBlock";
        //refs[0].instanceVersion="1.0";
        //send start session event
	String sbid = sbId;
	String manualArrayName = arrayName;
        refs = sendStartSessionEvent(sbid,manualArrayName);
        return refs;
    }


    
    
    /*
     * ================================================================
     * Changing the state of SBs via the archive
     * ================================================================
     */
	private void setSBState(String sbsId, StatusTStateType status) {
		archive.setSBState(sbsId, status);
	}

	public void setSBRunning(SB sb) {
		final SBStatusI sbs = statusQs.getSBStatusQueue().get(sb.getSbStatusId());
		if (!sbs.getStatus().getState().equals(StatusTStateType.RUNNING)) {
			setSBState(sb.getSbStatusId(), StatusTStateType.RUNNING);
		}
	}

	public void setSBReady(SB sb) {
		setSBState(sb.getSbStatusId(), StatusTStateType.READY);
		sb.getStatus().setReady();
	}

	public void setSBFullyObserved(SB sb) {
		final SBStatusI sbs = statusQs.getSBStatusQueue().get(sb.getSbStatusId());
		if (sbs.getStatus().getState().equals(StatusTStateType.RUNNING)) {
			setSBState(sb.getSbStatusId(), StatusTStateType.SUSPENDED);
			sb.getStatus().setEnded(DateTime.currentSystemTime(), Status.COMPLETE);
		}
		setSBState(sb.getSbStatusId(), StatusTStateType.FULLYOBSERVED);
	}

	public void setSBSuspended(SB sb) {
		setSBState(sb.getSbStatusId(), StatusTStateType.SUSPENDED);
		sb.getStatus().setEnded(DateTime.currentSystemTime(), Status.COMPLETE);
	}	
	
	public void setSBRunning(String sbId) {
		final SB sb = sbQueue.get(sbId);
		setSBRunning(sb);
		if (!sb.getStatus().isStarted()) {
			sb.setStartTime(DateTime.currentSystemTime());
		}
		sb.setRunning();
	}

	public void setSBReady(String sbId) {
		final SB sb = sbQueue.get(sbId);
		setSBReady(sb);
	}

	public void setSBFullyObserved(String sbId) {
		final SB sb = sbQueue.get(sbId);
		setSBFullyObserved(sb);
	}

	public void setSBSuspended(String sbId) {
		final SB sb = sbQueue.get(sbId);
		setSBSuspended(sb);
	}
	/* End of Changing the state of SBs via the archive
	 * ============================================================= */
	
	
	/*
	 * ================================================================
	 * Associations between arrays and schedulers
	 * ================================================================
	 */
	// Really this should be handled by the MasterScheduler but this is
	// an expedient hack until we sort this entire mess out - by which
	// I mean persuading the MasterScheduler to let go of some control
	// and allow the arrays and schedulers to handle their own stuff.
	// - David, 20th October 2009
	final private static String CONTROL_PREFIX_HACK = "CONTROL/";
	
	/**
	 * Somehow or another, the array name seems to change between the
	 * creation of an array and scheduler pair, and the completion of
	 * an execution block on the array. It acquires a prefix - go
	 * figure.
	 * 
	 * @param arrayName - one possible array name (either with or
	 *                    without the prefix).
	 * @return a String containing the other variant (with the prefix
	 *         stripped if it was there or added if it wasn't).
	 */
	private String alternativeArrayName(String arrayName) {
		String alternative = null;
		if (arrayName.startsWith(CONTROL_PREFIX_HACK)) {
			alternative = arrayName.substring(CONTROL_PREFIX_HACK.length());
		} else {
			alternative = String.format("%s%s",
					CONTROL_PREFIX_HACK,
					arrayName);
		}
		return alternative;
	}
	
	
	/**
	 * Remember that the given scheduler is running on the given array.
	 * Store it under both the supplied name and the variant of it.
	 * 
	 * @param arrayName
	 * @param scheduler
	 */
	public void rememberSchedulerForArray(String    arrayName,
			                              Scheduler scheduler) {
		arrayName2Scheduler.put(arrayName, scheduler);
		arrayName2Scheduler.put(alternativeArrayName(arrayName), scheduler);
	}

	/**
	 * Forget whatever scheduler is running on the given array.
	 * Remove it under both the supplied name and the variant of it.
	 * 
	 * @param arrayName
	 */
	public void forgetSchedulerForArray(String arrayName) {
		arrayName2Scheduler.remove(arrayName);
		arrayName2Scheduler.remove(alternativeArrayName(arrayName));
	}

	/**
	 * Get whatever scheduler is running on the given array. Should be
	 * in under both variants, so no need to faff about with
	 * alternativeName() here.
	 * 
	 * @param arrayName
	 */
	public Scheduler getSchedulerForArray(String arrayName) {
		return arrayName2Scheduler.get(arrayName);
	}
	/* End of Associations between arrays and schedulers
	 * ============================================================= */
}
