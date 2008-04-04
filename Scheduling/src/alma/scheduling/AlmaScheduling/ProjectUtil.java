/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied waraanty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 * File ProjectUtil.java
 */
package alma.scheduling.AlmaScheduling;

import alma.entity.xmlbinding.projectstatus.*;
import alma.entity.xmlbinding.projectstatus.types.*;
import alma.entity.xmlbinding.obsproject.*;
import alma.entity.xmlbinding.obsproposal.*;
import alma.entity.xmlbinding.schedblock.*;
import alma.entity.xmlbinding.valuetypes.StatusT;
import alma.entity.xmlbinding.valuetypes.TimeT;
import alma.entity.xmlbinding.valuetypes.SkyCoordinatesT;
import alma.entity.xmlbinding.valuetypes.LongitudeT;
import alma.entity.xmlbinding.valuetypes.LatitudeT;
import alma.entity.xmlbinding.valuetypes.VelocityT;
import alma.entity.xmlbinding.valuetypes.FrequencyT;
import alma.entity.xmlbinding.valuetypes.DoubleWithUnitT;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.entity.xmlbinding.schedblock.FrequencySetupT;
import alma.entities.commonentity.*;

import alma.scheduling.Define.*;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.io.StringWriter;

/**
 * The ProjectUtil class is a collection of static methods that
 * handle the following mappings.
 * <ul>
 * <li>Mappings
 * 		<ul>
 * 		<li> A: ObsProject|SchedBlock[]|ProjectStatus=="dummy" to Project
 * 		<li> B: ObsProject|SchedBlock[]|ProjectStatus!="dummy" to Project
 * 		<li> C: Project to  ProjectStatus
 * 		</ul>  
 * <li>External Dependencies
 * 		<ul>
 * 		<li> Mappings A and B depend on the definition of ObsProject, SchedBlock, and ProjectStatus.
 * 		<li> Mapping C only depends on the definition of ObsProject.
 * 		</ul>  
 * <li>Major Public Methods
 * 		<ul>
 * 		<li> <em>Project map(ObsProject p, SchedBlock[] b, ProjectStaus s, DateTime now) throws SchedulingException</em> --
 * 			 Create a Project from an ObsProject, the scheduling blocks that belong 
 * 			 to it and its current status.  This method divides into two cases 
 * 			 depending on whether or not the ProjectStatus is a "dummy".
 * 		<li> <em>ProjectStatus map(Project p, DateTime now) throws SchedulingException</em> -- 
 * 			 Create a ProjectStructure object that accurately reflects the current status of a project.
 * 		<li> <em>void validate(Project p) throws SchedulingException</em> -- Check a project for 
 * 			 internal consistency.
 * 		<li> <em>Session createSession(ObservedSession source)</em> -- Create a Session object 
 * 			 from an ObservedSession object.
 * 		</ul>  
 * </ul> 
 * 
 * @version 2.2 Oct 15, 2004
 * @version $Id: ProjectUtil.java,v 1.66 2008/04/04 19:50:05 wlin Exp $
 * @author Allen Farris
 */
public class ProjectUtil {
	
	static private final String nullPartId = "X00000000";
    static private SchedLogger logger = new SchedLogger(Logger.getLogger("Scheduling's ProjectUtil Logger"));

	/**
	 * Check the specified project for internal consistency.
	 * <p>
	 * The following general validations are used throughout the validation process
	 * <ul>
	 * <li>Certain strings must have values: s != null && s.length > 0
	 * <li>Times with values must pass a reasonableness test: 2000:01:01 < t < 2050:01:01
	 * <li>EntityPartIds must be of the form: <EntityPartId>X00000001</EntityPartId>
	 * <li>EntityIds must be of the form: entityId="uid://X0000000000000079/X00000000"
	 * </ul>
	 * <p>
	 * The validation procession proceeds in steps by walking down the project tree.
	 * <ul>
	 * <li>The project is checked.
	 * <li>The obsProgram in the project is checked.
	 * <li>Checking a Program includes:
	 * 		<ul>
	 * 		<li>Checking each session that belongs to the program
	 * 		<li>Checking the science pipeline processing request that belongs to the program
	 * 		<li>Checking each member of the Program, whether that member is a Program or an SB
	 * 		</ul>
	 * <li>Checking an SB includes:
	 * 		<ul>
	 * 		<li>Checking each ExecBlock that belongs to the SB
	 * 		</ul>
	 * </ul>
	 * 
	 * @param project The Project to be checked.
	 * @throws SchedulingException If any error is found in the specified project.
	 */
	static public void validate(Project project) throws SchedulingException {
		
		//checkEntityId(project.getObsProjectId());
		//checkEntityId(project.getProjectStatusId());
		//checkEntityId(project.getProposalId());
		checkString("projectName",project.getProjectName());
		checkString("PI",project.getPI());
		checkTime(project.getTimeOfCreation());
		checkTime(project.getTimeOfUpdate());
		checkTime(project.getBreakpointTime());
		validate(project.getStatus());
		//sohaila: why validate it to a null??
        //validate(project.getProgram(),project,null);
	}
	
	/**
	 * Check the the specified string has a value.
	 * @param name The name of the string to be checked.
	 * @param value The value of the string.
	 * @throws SchedulingException If the value is null or blank.
	 */
	static public void checkString(String name, String value) throws SchedulingException {
		if (value == null || value.length() == 0 || value.trim().length() == 0)
			throw new SchedulingException("The value for string " + name + " must have a value.");
	}
	
	static private final DateTime minTime = new DateTime("2000-01-01T00:00:00");
	static private final DateTime maxTime = new DateTime("2050-01-01T00:00:00");
	/**
	 * Check that a time string is valid and convert it to a DateTime.
	 * @param t The time string to be checked.
	 * @return The value converted to a DateTime.  If the value is null, null is returned.
	 * @throws SchedulingException If the time format is invalid or out of range.  (The 
	 * range of reasonable values is from 2000-01-01 to 2050-01-01.
	 */ 
	static public void checkTime(DateTime t) throws SchedulingException {
		if (t == null || t.isNull())
			return;
		if (t.lt(minTime) || t.gt(maxTime)) {
			throw new SchedulingException ("Illegal value for time (" + t + ")");
		}
	}
	
	/**
	 * Check an EntityPartId: format "X00000001"
	 * @param s The string to be checked.
	 * @throws SchedulingException If the string has an invalid format.
	 */ 
	static public void checkEntityPartId(String s) throws SchedulingException {
		if (s == null || s.length() == 0)
			return;
		if (s.length() != 9 || s.charAt(0) != 'X')
			throw new SchedulingException ("Invalid format for EntityPartId (" + s + ")");
		for (int i = 1; i < 9; ++i) {
			if (!((s.charAt(i) >= '0' && s.charAt(i) <= '9') ||
				  (s.charAt(i) >= 'a' && s.charAt(i) <= 'f')))
				throw new SchedulingException ("Invalid format for EntityPartId (" + s + ")");
		}
	}
	
	//////////////////////////////////////////////////////////////
	// The following private methods are used to support the 	//
	// "validate(Project project)" method.						//
	//////////////////////////////////////////////////////////////
	
	/**
	 * Validate a Status object.
	 * @param status
	 * @throws SchedulingException
	 */
	static private void validate(Status status) throws SchedulingException {
		checkTime(status.getReadyTime());
		checkTime(status.getStartTime());
		checkTime(status.getEndTime());
	}
	
	/**
	 * valildate a Program object.
	 * @param program
	 * @param project
	 * @param parent
	 * @throws SchedulingException
	 */
	static private void validate( Program program, Project project, Program parent) throws SchedulingException {
		checkEntityPartId(program.getProgramId());
		checkEntityPartId(program.getObsUnitSetStatusId());
		if (program.getProject() != project)
			throw new SchedulingException("Invalid value for project in program " + 
					program.getProgramId() + " in project " + project);
		checkTime(program.getTimeOfCreation());
		checkTime(program.getTimeOfUpdate());
		validate(program.getStatus());
		if (program.getParent() != parent)
			throw new SchedulingException("Invalid value for parent in program " + 
					program.getProgramId() + " in project " + project);

		// validate session members
		ObservedSession[] session = program.getAllSession();
		for (int i = 0; i < session.length; ++i)
			validate(session[i],project,program);
		
		// validate sciPipelineRequest
		if (program.getSciPipelineRequest() != null)
			validate(program.getSciPipelineRequest(),project,program);
		
		// Get the totals
		// The total time required to execute this Program.
		int totalRequiredTimeInSeconds = program.getTotalRequiredTimeInSeconds();
		// The total amount of time used by this Program so far.
		int totalUsedTimeInSeconds = program.getTotalUsedTimeInSeconds();
		// The total number of Programs that belong to this Program.
		int totalPrograms = program.getTotalPrograms();
		// The total number of Programs belonging to this set that have been successfully completed. 
		int numberProgramsCompleted = program.getNumberProgramsCompleted();
		// The total number of Programs belonging to this set that have failed.
		int numberProgramsFailed = program.getNumberProgramsFailed();
		// The total number of SBs that belong to this set.
		int totalSBs = program.getTotalSBs();
		// The total number of SBs belonging to this set that have been successfully completed. 
		int numberSBsCompleted = program.getNumberSBsCompleted();
		// The total number of SBs belonging to this set that have failed.
		int numberSBsFailed = program.getNumberSBsFailed();
		
		// validate members
		ProgramMember[] member = program.getMember();
		for (int i = 0; i < member.length; ++i) {
			if (member[i] instanceof Program)
				validate((Program)member[i],project,program);
			else
				validate((SB)member[i],project,program);
		}	
	}
	
	/**
	 * Validate an ObservedSession.
	 * @param session
	 * @param project
	 * @param parent
	 * @throws SchedulingException
	 */
	static private void validate(ObservedSession session, Project project, Program parent) throws SchedulingException {
		// TODO
	}
	
	/**
	 * validate a SciPipelineRequest.
	 * @param req
	 * @param project
	 * @param parent
	 * @throws SchedulingException
	 */
	static private void validate(SciPipelineRequest req, Project project, Program parent) throws SchedulingException {
		// TODO
        System.out.println("Validating SciPipelineRequest..");
	}

	/**
	 * Validate an SB.
	 * @param sb
	 * @param project
	 * @param parent
	 * @throws SchedulingException
	 */
	static private void validate(SB sb, Project project, Program parent) throws SchedulingException {
		// TODO
	}

	/**
	 * Validate an ExecBlock.
	 * @param exec
	 * @param project
	 * @param parent
	 * @throws SchedulingException
	 */
	static private void validate(ExecBlock exec, Project project, SB parent) throws SchedulingException {
		//TODO
	}
	
	//////////////////////////////////////////////////////////////
	// End of private methods are used to support the		 	//
	// "validate(Project project)" method.						//
	//////////////////////////////////////////////////////////////
	
	/**
	 * Create a Project from an ObsProject, the scheduling blocks that belong 
	 * to it and its current status.  This method divides into two cases 
	 * depending on whether or not the ProjectStatus is a "dummy".
	 * @param p The ObsProject that is to be mapped.
	 * @param b The SchedBlocks that belong to the ObsProject.
	 * @param s The current ProjectStatus of the ObsProject.
	 * @return A Project object, held in memory, that corresponds to the ObsProject.
	 * @throws SchedulingException If any error is found in the process of mapping.
	 */
	static public Project map(ObsProject p, SchedBlock[] b, ProjectStatus s, DateTime now) 
		throws SchedulingException {
		// First, we get the project data as if the status is a dummy.
		Project project = initialize(p,b,s,now);
		
		// How do we detect if the ProjectStatus is a "dummy".
		// Answer: If the timeOfUpdate is null or of length 0.
        /*
        String timeOfUpdate = s.getTimeOfUpdate();
		if (timeOfUpdate == null || timeOfUpdate.length() == 0) {
			// If the project status is a dummy, then we're done.
		//	validate(project);
			return project;
		}
		
		// Now, we apply the ProjectStatus data.
		update(project,s,now);
        */
		//validate(project);
		return project;
	}

	//////////////////////////////////////////////////////////////////////
	// The following private methods are used to support the 			//
	// "map(ObsProject p, SchedBlock[] b, ProjectStatus s)" method.		//
	//////////////////////////////////////////////////////////////////////
	
	/**
	 * Create a Project from an ObsProject and its scheduling blocks, while regarding
	 * its project status as a "dummy" object.
	 * @param obs
	 * @param sched
	 * @param projectStatusEntityId
	 * @param now
	 * @return
	 * @throws SchedulingException
	 */
	static private Project initialize(ObsProject obs, SchedBlock[] sched, 
			                          ProjectStatus ps, DateTime now) 
                                      throws SchedulingException {

		Project project = null;
        try {
        project = new Project (obs.getObsProjectEntity().getEntityId(),
									   obs.getObsProposalRef().getEntityId(),
									   obs.getProjectName(),
									   obs.getVersion(),
									   obs.getPI());
		project.setProjectStatusId(ps.getProjectStatusEntity().getEntityId());
        //TODO time of Creation needs to get put into ProjectStatus so we can get it there
        if(project.getTimeOfCreation() == null) {
    		project.setTimeOfCreation(now);
        }
        try {
    		project.setTimeOfUpdate(new DateTime(ps.getTimeOfUpdate()));
        } catch(java.lang.NullPointerException npe) {
            //new project, has no time of update
            project.setTimeOfUpdate(now);
        }
		//project.setBreakpoint(null); Sohaila: Took out coz in Define/Project this throws an error!
		
		// To check if all SchedBlocks are used, we will create an array
		// of booleans and check them off as we use them.
		boolean[] schedUsed = new boolean [sched.length];
		for (int i = 0; i < schedUsed.length; ++i)
			schedUsed[i] = false;
		
		// Initialize the obsProgram.
		//ObsUnitSetT obsProgram = obs.getObsProgram().getObsPlan();
        ObsUnitSetStatusT ous=null;
        try {
            ous = ps.getObsProgramStatus();
        }catch (Exception e){
            e.printStackTrace();
        }

		Program program = initialize(obs.getObsProgram().getObsPlan(), sched, 
		//Program program = initialize(obs.getObsProgram(), sched, 
                                     schedUsed, project, null, ous, now);
		if(program==null){
			logger.info("return null value from initialize program");
			return null;
		}
		
		logger.info("after return null program info:"+program.getId());
		
		project.setProgram(program);
		// Mark the project as ready.  (This also initializes the totals.)
        try {
        	project.setReady(new DateTime(ps.getStatus().getReadyTime()));
        }catch (java.lang.IllegalArgumentException iae){
        	project.setReady(now);
        }catch (java.lang.NullPointerException npe){
        	project.setReady(now);
        } catch(Exception e){
            e.printStackTrace();
        }

        try {
            project.getStatus().setStarted(new DateTime(ps.getStatus().getStartTime()));
        } catch(Exception e) {}
        try {
            project.getStatus().setEnded(new DateTime(ps.getStatus().getStartTime()), 
                getStatusMatch(ps.getStatus()));
        } catch(Exception e) {}
        //now set all the started/ended times in the program and SBs
		program = setStatusInformation(program, obs.getObsProgram().getObsPlan(), ous, now); 
		// Make sure that all the scheduling blocks in the sched array have been accounted for.
		for (int i = 0; i < schedUsed.length; ++i) {
			if (!schedUsed[i])
				throw new SchedulingException("SchedBlock with name " + 
						sched[i].getName() + " and id " + 
						sched[i].getSchedBlockEntity().getEntityId() +
						" was not used in the initialization process.");
		}
		
		// Now, set all the partIds in the project status.
		Program p = project.getProgram();
		setProgramMember(p);
		project.setProgram(program);
		
		// Now, validate the project
		validate(project);
		} catch(Exception e) {
            e.printStackTrace();
        }

        //System.out.println("project: ready =" + 
         //       ((project.getStatus().getReadyTime() == null) ? "null" :
          //          project.getStatus().getReadyTime().toString() ) +
           //     " start = "+ (( project.getStatus().getStartTime() == null) ? "null" : 
            //        project.getStatus().getStartTime().toString()) +
             //   " end = "+  ((project.getStatus().getEndTime() == null) ? "null" : 
              //      project.getStatus().getEndTime().toString()) );
		return project;
	}

	static private void setProgramMember(Program p) {
		ProgramMember[] m = p.getMember();
		for (int i = 0; i < m.length; ++i) {
			if (m[i] instanceof Program) {
                if(((Program)m[i]).getObsUnitSetStatusId() == null) {
				    ((Program)m[i]).setObsUnitSetStatusId(genPartId());
                }             
				setProgramMember((Program)m[i]);
			} else {
                if(((SB)m[i]).getSbStatusId() == null) {
				    ((SB)m[i]).setSbStatusId(genPartId());
                }
				setSBMember((SB)m[i]);
			}
		}
	}
	static private void setSBMember(SB sb) {
		ExecBlock[] x = sb.getExec();
		for (int i = 0; i < x.length; ++i) {
            if(x[i].getExecStatusId() == null) {
    			x[i].setExecStatusId(genPartId());
            }
		}
	}
	
	static private int partIdCount = 0;
	static private char[] digit = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
	static public String genPartId() {
		char[] s = new char [9];
		s[0] = 'X';
		for (int i = 1; i < s.length; ++i)
			s[i] = '0';
		int n = ++partIdCount;
		for (int i = s.length -1; i > 1; --i) {
			s[i] = digit[n % 16];
			n /= 16;
			if (n == 0)
				break;
		}
        String tmp = new String (s);
		//return new String (s);
        return tmp;
	}

	/**
	 * Create a Program from an ObsUnitSetT and the array of SchedBlocks that belong to the project.
	 * @param set
	 * @param sched
	 * @param project
	 * @param parent
	 * @param now
	 * @return
	 * @throws SchedulingException
	 */
	static private Program initialize(ObsUnitSetT set, SchedBlock[] sched, 
			boolean[] schedUsed, Project project, Program parent,  
			ObsUnitSetStatusT ous, DateTime now) throws SchedulingException {
		
	  Program program = null;
      try {
        if(set.getEntityPartId() == null) {
            set.setEntityPartId(genPartId());
        }
        program = new Program (set.getEntityPartId());
		program.setProject(project);
		//System.out.println("ProjectUtil:program"+ program.getTotalPrograms());
		//program.setObsUnitSetStatusId(null); // We get this from the ProjectStatus.
        try {
    		program.setTimeOfCreation(new DateTime(ous.getStatus().getReadyTime()));
            //program.setReady(new DateTime(ous.getStatus().getReadyTime()));
		    program.setTimeOfUpdate(new DateTime(ous.getTimeOfUpdate()));
        } catch(Exception e){
    		program.setTimeOfCreation(now);
            //program.setReady(now);
		    program.setTimeOfUpdate(now);
        }
		program.setParent(parent);
        try{
            program.setNumberSBsCompleted(ous.getNumberSBsCompleted());
        } catch(Exception e){}// will throw exception if not set, not a problem
        try{
            program.setNumberSBsFailed(ous.getNumberSBsFailed());
        } catch(Exception e){}// will throw exception if not set, not a problem
        
		// We get SciPipelineRequest and Sessions from the ProjectStatus. 
        //TAC priority == scientific priority???
        try {
            int tac = set.getObsUnitControl().getTacPriority();
            if(tac > 10 || tac < 1) {
                tac = 1;
            }
            program.setScientificPriority(new Priority(tac)); 
        } catch(Exception e) {
            //NOTE: TODO
            //do not need tac priority when running test or commissioning projects
            //but will need it in science projects running dynamically.
            //however should be set as something thats not NPE so setting to 0.
            program.setScientificPriority(new Priority(0)); 
        }
        try {
            int pri = set.getObsUnitControl().getUserPriority();
            if(pri > 10 || pri < 1) {
                pri = 1;
            }
            program.setUserPriority(new Priority(pri));
        } catch(Exception e) {
            program.setUserPriority(new Priority(0));
        }
		program.setDataReductionProcedureName(set.getScienceProcessingScript());
        try {
            Object[] params = new Object[5];
            params[0] = set.getDataProcessingParameters().getAngularResolution();
            params[1] = set.getDataProcessingParameters().getVelocityResolution();
            params[2] = set.getDataProcessingParameters().getTBSensitivityGoal();
            params[3] = set.getDataProcessingParameters().getRMSGoal();
            params[4] = set.getDataProcessingParameters().getProjectType();
    		program.setDataReductionParameters(params);
        } catch(Exception e) {
		    program.setDataReductionParameters(null);
        }
		program.setFlowControl(null);
		program.setNotify(null);
        //TODO 
		program.setScienceGoal(null);
        //TODO 
		program.setWeatherConstraint(null);
		program.setCenterFrequency(0.0);
		program.setFrequencyBand(null);

		program.setRequiredInitialSetup(null);

        //get choice obj from obs unit set status in PS so we can get previous
        //execs and session info.
        SessionT[] session;
        //used when checking for previous things
        boolean hasStatus = false;
		// Assign the members of this set: either Program or SB objects.
		if (set.getObsUnitSetTChoice().getObsUnitSetCount() > 0) {
			//logger.info("SCHEDULING:start to add ObsUnitSet into Program");
			Program memberProgram = null;
            ObsUnitSetT[] setMember = set.getObsUnitSetTChoice().getObsUnitSet();
            ObsUnitSetStatusTChoice choice=null;
            ObsUnitSetStatusT[] status=null;
            try {
                choice = ous.getObsUnitSetStatusTChoice();
                status= choice.getObsUnitSetStatus();
                
                if(status.length < 1){
                    hasStatus = false;
                } else {
                    hasStatus = true;
                }
            }catch(Exception e){
                hasStatus = false;
            }
            String partId;
			for (int i = 0; i < setMember.length; ++i) {
                try {              	
				    memberProgram = initialize(setMember[i],sched,schedUsed,project,program,status[i],now);
                } catch(Exception e){
				    memberProgram = initialize(setMember[i],sched,schedUsed,project,program,null,now);
                }
                
                if(memberProgram==null) {
		        	//logger.severe("SCHEDULING: can not initialize because lack of some information ");
		        	return null;
		        	}

				program.addMember(memberProgram);
			}
		} else if (set.getObsUnitSetTChoice().getSchedBlockRefCount() > 0) {
			//logger.info("SCHEDULING:start to add SB into Program");
			SchedBlockRefT[] setMember = set.getObsUnitSetTChoice().getSchedBlockRef();
            ObsUnitSetStatusT[] foo=null;
            ObsUnitSetStatusTChoice choice = null;
            SBStatusT[] sbStats = null;
            try{
                choice = ous.getObsUnitSetStatusTChoice();
                foo = choice.getObsUnitSetStatus();
                sbStats = choice.getSBStatus();
                if(sbStats.length < 1){
                    hasStatus = false;
                } else {
                    hasStatus = true;
                }
            } catch(Exception e){
                hasStatus = false;
            }
			SB memberSB = null;
            String sbrefid;
            ExecStatusT[] execs;
            SBStatusT sbStatus=null;
            for (int i = 0; i < setMember.length; ++i) {
			    if(hasStatus){
                    execs = sbStats[i].getExecStatus();
                    sbrefid = sbStats[i].getSchedBlockRef().getEntityId();
                    sbStatus = getSBStatusForSBRef(sbrefid, sbStats);
                    try {
        		        memberSB = initialize(setMember[i],sbStatus, sched,schedUsed,project,program,now);
        		        //check the return value for memberSB 
        		        if(memberSB==null) {
        		        	//logger.severe("SCHEDULING: can not initialize because lack of some information ");
        		        	return null;
        		        	}
        		        
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    memberSB = assignExecStatusToSB(memberSB, sbrefid, execs, sbStatus);
                } else {
                    try {
        		        memberSB = initialize(setMember[i], null, sched,schedUsed,project,program,now);
        		        
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
			    if(memberSB!=null){
			    	//logger.info("memberSB:"+memberSB.getId());
				    program.addMember(memberSB);
			    }
			    else {
			    	logger.info("SB not add into program");
			    	return null;
			    }
			}
		}
		
        //check coz this level (already being an ObsUnitSetStatus) might have sessions 
        try {
            session = ous.getSession();
            if(session != null || session.length > 0){
                program  = assignSessionToProgram(program,set.getEntityPartId(),  session);
            }
        }catch(Exception e){
        //if we get this exception its fine and means there were no sessions to add
        }
		int maxTime = 0;
		ProgramMember[] member = program.getMember();
		for (int i = 0; i < member.length; ++i) {
			if (member[i] instanceof Program) {
				maxTime += ((Program)member[i]).getMaximumTimeInSeconds();
			} else
				maxTime += ((SB)member[i]).getMaximumTimeInSeconds();
		}
		program.setMaximumTimeInSeconds(maxTime);
		
      } catch(Exception e) {
          e.printStackTrace();
      }
	  return program;
	}

    private static Program setStatusInformation(Program p,
            ObsUnitSetT set, ObsUnitSetStatusT ous, DateTime now) {
        boolean hasStatus=false;
        if (set.getObsUnitSetTChoice().getObsUnitSetCount() > 0) {
            //Program memberProgram = p;
            ObsUnitSetT[] setMember = set.getObsUnitSetTChoice().getObsUnitSet();
            ObsUnitSetStatusTChoice choice = null;
            ObsUnitSetStatusT[] status = null;
            try {
                choice = ous.getObsUnitSetStatusTChoice();
                status = choice.getObsUnitSetStatus();
                if(status.length < 1) {
                    hasStatus = false;
                } else{
                    hasStatus = true;
                }
            } catch(Exception e){
                hasStatus = false;
            }
            ProgramMember x=null;
            for(int i=0; i < setMember.length; i++){
                x = p.getMember(setMember[i].getEntityPartId());
                try {
                    p = setStatusInformation ((Program)x, setMember[i], status[i], now);
                } catch (Exception e) {
                    p = setStatusInformation ((Program)x, setMember[i], null, now);
                }
            }
        } else if (set.getObsUnitSetTChoice().getSchedBlockRefCount() > 0) {
            SchedBlockRefT[] setMember = set.getObsUnitSetTChoice().getSchedBlockRef();
            ObsUnitSetStatusT[] foo = null;
            ObsUnitSetStatusTChoice choice = null;
            SBStatusT[] sbStats = null;
            SB memberSB = null; 
            try {
                choice= ous.getObsUnitSetStatusTChoice();
                foo= choice.getObsUnitSetStatus();
                sbStats = choice.getSBStatus();
                if(sbStats.length < 1){
                    hasStatus =false;
                } else{
                    hasStatus = true;
                }
            } catch(Exception e){
                hasStatus = false;
            }
            String sbrefid;
            SBStatusT sbStatus = null;
            for(int i=0; i <  setMember.length; i++){
                if(hasStatus){
                    sbrefid = sbStats[i].getSchedBlockRef().getEntityId();
                    memberSB = (SB)p.getMember(sbrefid);
                    try {
                        sbStatus = getSBStatusForSBRef(sbrefid, sbStats);
                        //only one!
                        memberSB = assignCompletionStatus(memberSB, sbStatus, now);
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
        return p;
    }
    
    private static SBStatusT getSBStatusForSBRef(String id, SBStatusT[] stats)
        throws SchedulingException {
            for(int i=0; i< stats.length; i++){
                if (stats[i].getSchedBlockRef().getEntityId().equals(id)){
                    return stats[i];
                }
            }
            //got to end of loop and nothing matched so throw exception
            throw new SchedulingException("SBSatus id not found in SBStatus array");
    }

    /**
      * Given a StatusT out of the project status get its corresponding status value for the 
      * scheduling Define/Status.java class
      */
    private static int getStatusMatch(StatusT stat) {
        //int state;
        if(stat.getState().toString().equals("aborted")){
            return Status.ABORTED;
        }else if(stat.getState().toString().equals("complete")){
            return Status.COMPLETE;
        }else if(stat.getState().toString().equals("observed")){
            return Status.OBSERVED;
        }else if(stat.getState().toString().equals("processed")){
            return Status.PROCESSED;
        }else if(stat.getState().toString().equals("canceled")){
            return Status.CANCELED;
        }else if(stat.getState().toString().equals("ready")){
            return Status.READY;
        } else {
            //TODO when project lifecycle starts before scheduling this should probably 
            //be set to not defined
            return Status.READY;
        }
    }

    private static SB assignCompletionStatus(SB sb, SBStatusT status, DateTime now)
        throws SchedulingException{

        int state = getStatusMatch(status.getStatus());
        try {
            /*
            try {
                sb.setReady(new DateTime(status.getStatus().getReadyTime()));
            } catch(Exception e){
                //e.printStackTrace();
                sb.setReady(now);
            }*/
                
            try {
                sb.setStartTime(new DateTime(status.getStatus().getStartTime()));
            } catch(Exception e){
                //SB probably not been started yet
                //e.printStackTrace();
            }
            try {
                sb.getStatus().setEnded(new DateTime(status.getStatus().getEndTime()), state);
            }catch(Exception e){
                //SB probably not ended yet
            //    e.printStackTrace();
            }
            return sb;
        } catch(Exception e) {
            throw new SchedulingException(e);
        }
                    
    }
    

    /**
      * Add the sessions found in the project status to the Program object. 
      * Members is the list of potential Programs to match the one with the
      * given part id. The session list is the sessions from the ObsUnitSetStatus'
      * that has the given part id.
      */
    static private Program assignSessionToProgram(Program member, String currentId, SessionT[] sessions){
        if(memberMatches((Program)member, currentId)){
            ObservedSession s;
            for (int j=0; j < sessions.length; j++){
                s = new ObservedSession();
                s.setSessionId(sessions[j].getEntityPartId());
                s.setStartTime(new DateTime(sessions[j].getStartTime()));
                try {
                    s.setEndTime(new DateTime(sessions[j].getEndTime()));
                } catch (java.lang.IllegalArgumentException iae) {
                    //session didn't have an end time 
                    s.setEndTime(null);
                }
                int execs = sessions[j].getExecBlockRefCount();
                if(execs > 0){
                    ExecBlock eb;
                    ExecBlockRefT[] e_refs = sessions[j].getExecBlockRef();
                    for (int e=0; e < execs; e++){
                        eb = new ExecBlock(e_refs[e].getExecBlockId(), "HistoricalReference");
                        s.addExec(eb);
                    }
                    ((Program)member).addObservedSession(s);
                }
            }
        }
        return member;
    }

    static private SB assignExecStatusToSB(SB sb, String sbid, ExecStatusT[] execs, SBStatusT status) {
        if(sbMatches(sb, sbid)){
            if(execs.length < 1){
                return sb;
            }
            ExecStatusT stat;
            ExecBlock eb;
            StatusT e_status;
            int i;
            DateTime dt = null;
            for(i=0; i < execs.length; i++){
                stat = execs[i];
                eb = new ExecBlock(stat.getExecBlockRef().getExecBlockId(), stat.getArrayName());
                try {
                    eb.setStartTime(new DateTime(stat.getStatus().getStartTime()));
                } catch(Exception e) {
                    eb.setStartTime(new DateTime(stat.getTimeOfCreation()));
                }
                e_status = stat.getStatus();
                try {
                    dt = new DateTime(e_status.getEndTime());
                    eb.setEndTime(dt, e_status.getState().getType());
                } catch(Exception e) { 
                }
                try {
                    sb.addExec(eb);
                } catch(Exception e){
                    logger.warning("Error adding exec block "+eb.getExecId());
                }
            }
        }
        return sb;
    }

    static private boolean sbMatches(SB sb, String id){
        if(sb.getId().equals(id)){
            return true;
        }
        return false;
    }

    /**
      * Match program's part id to the given id. True if they match,
      */
    static private boolean memberMatches(Program m, String id){
        if(m.getProgramId().equals(id)) {
            return true;
        } else {
            return false;
        }
    }

	/**
	 * Create an SB from the SchedBlockRefT and the array of SchedBlocks that belong to the project.
	 * @param schedRef
	 * @param sched
	 * @param project
	 * @param parent
	 * @param now
	 * @return
	 * @throws SchedulingException
	 */
	static private SB initialize(SchedBlockRefT schedRef, SBStatusT sbStatus, SchedBlock[] schedArray, boolean[] schedUsed, Project project, Program parent, DateTime now) 
		throws SchedulingException {
            
    	SB sb = new SB (schedRef.getEntityId());
        try {
    
    		sb.setSbStatusId(null); // This comes from ProjectStatus.
	    	sb.setProject(project);
		    sb.setTimeOfCreation(project.getTimeOfCreation());
            try {
        		sb.setTimeOfUpdate(new DateTime(sbStatus.getTimeOfUpdate()));
            }catch(Exception e){
        		sb.setTimeOfUpdate(now);
            }
		    sb.setParent(parent);

		// We need to use the entityId to get the SchedBlock from the sched array.
    		SchedBlock sched = null;
	    	for (int i = 0; i < schedArray.length; ++i) {
		    	if (schedArray[i].getSchedBlockEntity().getEntityId().equals(sb.getSchedBlockId())) {
			    	sched = schedArray[i];
				    schedUsed[i] = true;
    				break;
	    		}
		    }
    		if (sched == null)
	    		throw new SchedulingException("The scheduling block with id " + sb.getSchedBlockId() +
		    			" is not in the specified SchedBlock array.");
		
            sb.setSBName(sched.getName());
	    	// There are some problems here that we need to fix after R2.
            //TAC priority == scientific priority???
            int tac = sched.getObsUnitControl().getTacPriority();
            if(tac > 10 || tac < 1) {
                tac = 1;
            }
		    sb.setScientificPriority(new Priority(tac)); 
            int pri = sched.getObsUnitControl().getUserPriority();
            if(pri > 10 || pri < 1) {
                pri = 1;
            }
    		sb.setUserPriority(new Priority(pri));
    		//sb.setUserPriority(Priority.MEDIUM);
            //TODO: where in ot?
	    	sb.setScienceGoal(null);
            //TODO: need to redefine weather constraint stuff i think..
		    sb.setWeatherConstraint(null);
            SBSetup initialSetup = new SBSetup();
            //TODO: where in ot?
    		sb.setRequiredInitialSetup(initialSetup);
            //TODO: where in ot?
	    	sb.setImagingScript(null);
		    
		// Set the observing script.
    		sb.setObservingScript(sched.getObsProcedure().getObsProcScript());
	    	if (sched.hasStandardMode()){
		        	sb.setStandardScript(sched.getStandardMode());
            }else {
    			sb.setStandardScript(false);
            }
		// Set the time and repeat count.
		// IMPORTANT NOTE!
		// We must have a maximum time.  If it is not present, we will arbitrarily assign 30 minutes.
		// If there is no repeat count, we assume it to be 0.
		// We are also assuming that time is in minutes and that the repeat count is acutally
		// the number of executions (and not the number of repetitions).
	    	SchedBlockControlT ctrl = sched.getSchedBlockControl();
    		if (ctrl == null) {
		    	sb.setMaximumTimeInSeconds(1800);
	    		sb.setMaximumNumberOfExecutions(1);
    		} else {
		    	TimeT tt = ctrl.getSBMaximumTime();
	    		if (tt == null) {
    				sb.setMaximumTimeInSeconds(1800); 
			    } else {
		    		double maxTime = tt.getContent();
	    			sb.setMaximumTimeInSeconds((int)(maxTime * 60 + 0.05));
    			}
			    int execcount = ctrl.getExecutionCount();
		    	if (execcount < 1){
	    			throw new SchedulingException("Invalid execution count (" + execcount + ").");
                }
            //set to -1 because the first run is the original one. Not considered a 'repeat'
			//sb.setMaximumNumberOfRepeats(repeatcount - 1); 
            //TODO Changed this to not have -1, coz scheduler doesn't think this way yet.
    			sb.setMaximumNumberOfExecutions(execcount); 
                sb.setIndefiniteRepeat(ctrl.getIndefiniteRepeat());
    		}
            
            FieldSourceT[] fieldSources = sched.getFieldSource();
            FieldSourceT fs;
            TargetT[] targets = sched.getTarget();
            Source source;
            SkyCoordinatesT coord;
            String fs_id;
            Equatorial[] eq = new Equatorial[targets.length];
            String[] op_params_list;
            for(int i=0; i < targets.length; i++){
                fs_id = targets[i].getFieldSourceId();
                fs = getFieldSourceFromList(fieldSources, fs_id);
//////////////////
// Can't do the following with out a field source..
                if(fs != null){
                    coord = fs.getSourceCoordinates();
        	        if (coord == null){
        	        	logger.severe("There is no SkyCoordinatesT object in the scheduling block");
        	        	return null;
            	       	//throw new SchedulingException(
                        //        "There is no SkyCoordinatesT object in the scheduling block");
                    }
                    LongitudeT lng = coord.getLongitude();
                    double ra = lng.getContent();
                    LatitudeT lat = coord.getLatitude();
                    double dec = lat.getContent();
        	        String coordType = coord.getSystem().toString(); // must be J2000
                    if (!coordType.equals("J2000")){
                    	logger.severe("the Shy Coordination system is not J2000! skip this project");
                    	return null;
                        //throw new SchedulingException(coordType + " is not supported.  Must be J2000");
                    }  
                    eq[i] = new Equatorial((ra /24.0),dec);
                    source = new Source();
                    //TODO add source stuff..
                    try {
                        source.setSourceName(fs.getSourceName());
                    } catch(Exception e) {
                        source.setSourceName("Source was not named.");
                    }
                    try {
                        source.setSolarSystemObj(fs.getSolarSystemObject().toString());
                    } catch(Exception e) {
                        source.setSolarSystemObj("Not a solar system object.");
                    }
                    SourcePropertyT[] sourceProperties = fs.getSourceProperty();
                    try {
                        source.setNumberSourceProperties(fs.getSourcePropertyCount());
                        //only really care about the first one!
                        source.setVisibleMagnitude(sourceProperties[0].getVisibleMagnitude().getValue());
                    } catch(Exception e){}
                    try {
                        //TODO FIX Eventaully to get REAL values from SchedBlockChoice2
                        source.setMinMagnitude(1);
                        //source.setMinMagnitude(op_params.getMinMagnitude().getValue());
                        source.setMaxMagnitude(-1);
                        //source.setMaxMagnitude(op_params.getMaxMagnitude().getValue());
                    } catch(Exception e){}

                    sb.addSource(source);
                } else {
                    eq[i] = new Equatorial(0.0,0.0);
                }
            }
            
    		if (eq.length == 1) {
		    	Target target = new Target (eq[0],3600.0,3600.0);
	    		sb.setTarget(target);
            } else if (eq.length < 1) {
                //HACK!
                eq = new Equatorial[1];
                eq[0] = new Equatorial((24.0/24.0),1);
		    	Target target = new Target (eq[0],3600.0,3600.0);
	    		sb.setTarget(target);
    		} else {
		    	Target target = new Target (eq);
	    		sb.setTarget(target);
    		}
///////////////
            //TODO fix this temporary hack!!
            SchedulingConstraintsT constraints = sched.getSchedulingConstraints();
            FrequencyT freq = constraints.getRepresentativeFrequency();
  			sb.setCenterFrequency(freq.getContent());
            FrequencyBand band = getFrequencyBand(freq);
    	    sb.setFrequencyBand(band); 
            //TargetT t = constraints.getRepresentativeTargeT();
		    
        }catch(Exception e){
            e.printStackTrace();    
            throw new SchedulingException(e.toString());
        }
		return sb;
	}
    /**
      * I don't think this can be hard coded.. but it is for now..
      * TODO double check!
      *
      */
    private static FrequencyBand getFrequencyBand(FrequencyT f) {
        double content = f.getContent();
        if( content >= 31.3 && content <=45.0) {
            return new FrequencyBand(""+1, 31.3, 45.0);
        } else if (content >=67.0 && content <=90.0){
            return new FrequencyBand(""+2, 67.0,90.0);
        } else if (content >=84.0 && content <= 116.0){
            return new FrequencyBand(""+3, 84.0, 116.0);
        } else if (content >=125.0 && content < 163.0){
            return new FrequencyBand(""+4, 125.0, 163.0);
        } else if (content >=163.0 && content <211.0){
            return new FrequencyBand(""+5, 160.0,211.0);
        } else if (content >=211.0 && content < 275.0){
            return new FrequencyBand(""+6, 211.0, 275.0);
        } else if (content >=275.0 && content <=373.0){
            return new FrequencyBand(""+7, 275.0, 373.0);
        } else if (content >=385.0 && content <= 500.0){
            return new FrequencyBand(""+8, 385.0, 500.0);
        } else if (content >=602.0 && content <= 720.0){
            return new FrequencyBand(""+9, 602.0, 720.0);
        } else if (content >=787.0 && content <=950.0 ){
            return new FrequencyBand(""+10, 787.0, 950.0);
        } else {//should never happen!!!
            return new FrequencyBand("ERROR",0.0, 0.0);
        }
        
    }
    
    private static FieldSourceT getFieldSourceFromList(FieldSourceT[] fs, String id){
        for(int i=0; i < fs.length; i++){
            if(fs[i].getEntityPartId().equals(id)){
                return fs[i];
            }
        }
        return null;
    }

	/**
	 * Update the specified Project object using the specified ProjectStatus object.
	 * @param project
	 * @param status
	 * @param now
	 * @throws SchedulingException
	 */
	static private void update(Project project, ProjectStatus status, DateTime now) 
		throws SchedulingException {
		//throw new SchedulingException("The ProjectUtil.update(Project project, ProjectStatus status, DateTime now) method is not implemented at the present time.");
            //System.out.println("UPDATING THE PROJECT BECAUSE IT HAS AN EXISTING PROJECT STATUS");
		// TODO We will implement this after R2.  For R2, all projects we execute will be created from
		//	scratch and will be initialized.  For this we won't need a previously created "real" ProjectStatus.
            //Project updatedProject = updateProject(project, now);

	}
    
    
    static public Project updateProject(ObsProject obs, Project project, SchedBlock[] sched, DateTime now) 
        throws SchedulingException {
        
            project.setTimeOfUpdate(now);
		
            // To check if all SchedBlocks are used, we will create an array
            // of booleans and check them off as we use them.
            boolean[] schedUsed = new boolean [sched.length];
    		for (int i = 0; i < schedUsed.length; ++i){
	    		schedUsed[i] = false;
            }
		
            // Initialize the obsProgram.
    		
    		Program program = updateProgram(obs.getObsProgram().getObsPlan(), project.getProgram(), 
                    sched, schedUsed, project, null, now);
	    	project.setProgram(program);
	    	/*
	    	 try {
	         	project.setReady(new DateTime(ps.getStatus().getReadyTime()));
	         }catch (java.lang.IllegalArgumentException iae){
	         	project.setReady(now);
	         }catch (java.lang.NullPointerException npe){
	         	project.setReady(now);
	         } catch(Exception e){
	             e.printStackTrace();
	         }

	         try {
	             project.getStatus().setStarted(new DateTime(ps.getStatus().getStartTime()));
	         } catch(Exception e) {}
	         try {
	             project.getStatus().setEnded(new DateTime(ps.getStatus().getStartTime()), 
	                 getStatusMatch(ps.getStatus()));
	         } catch(Exception e) {}
	         */
    		
            // Make sure that all the scheduling blocks in the sched array have been accounted for.
    		
		    for (int i = 0; i < schedUsed.length; ++i) {
			    if (!schedUsed[i])
				    throw new SchedulingException("SchedBlock with name " + 
					    	sched[i].getName() + " and id " + 
						    sched[i].getSchedBlockEntity().getEntityId() +
    						" was not used in the update Project process.");
	    	}
			
    		
		// Now, set all the partIds in the project status.
            Program p = project.getProgram();
            setProgramMember(p);
		
            project.setProgram(program);
		// update the totals
            project.getProgram().updateTotals();
		
		// Now, validate the project
            validate(project);
		
            return project;
    }
   

    
    
    static private Program updateProgram(ObsUnitSetT set, Program prog, SchedBlock[] sched, 
			boolean[] schedUsed, Project project, Program parent,  
			DateTime now) throws SchedulingException {

        if(set.getEntityPartId() == null) {
            set.setEntityPartId(genPartId());
        }

		Program program;
        if(prog == null) {
            program = new Program (set.getEntityPartId());
		    program.setTimeOfCreation(now);
		    program.setProject(project);
		    program.setParent(parent);
		//program.setObsUnitSetStatusId(null); // We get this from the ProjectStatus.
		// We get SciPipelineRequest and Sessions from the ProjectStatus. 
            int tac = set.getObsUnitControl().getTacPriority();
            if(tac > 10 || tac < 1) {
                tac = 1;
            }
            program.setScientificPriority(new Priority(tac)); 
            int pri = set.getObsUnitControl().getUserPriority();
            if(pri > 10 || pri < 1) {
                pri = 1;
            }
            program.setUserPriority(new Priority(pri));

            program.setFlowControl(null);
            program.setNotify(null);
            program.setScienceGoal(null);
            program.setWeatherConstraint(null);
            program.setCenterFrequency(0.0);
            program.setFrequencyBand(null);
            program.setRequiredInitialSetup(null);
            program.setReady(now);

        } else {
            program = prog;
		    program.setProject(project);
		    program.setTimeOfUpdate(now);
    		program.setParent(parent);
        }

		program.setDataReductionProcedureName(set.getScienceProcessingScript());
        try {
            Object[] params = new Object[5];
            params[0] = set.getDataProcessingParameters().getAngularResolution();
            params[1] = set.getDataProcessingParameters().getVelocityResolution();
            params[2] = set.getDataProcessingParameters().getTBSensitivityGoal();
            params[3] = set.getDataProcessingParameters().getRMSGoal();
            params[4] = set.getDataProcessingParameters().getProjectType();
    		program.setDataReductionParameters(params);
        } catch(Exception e) {
		    program.setDataReductionParameters(null);
        }
		// Assign the members of this set: either Program or SB objects.
		if (set.getObsUnitSetTChoice().getObsUnitSetCount() > 0) {
			ObsUnitSetT[] setMember = set.getObsUnitSetTChoice().getObsUnitSet();
            // TODO: get existing memebers, need to update them if they already exist
            // or use partId to get existing memeber inside loop below
            // for that we need partId stuff stored in project which is given
            // for update
			Program memberProgram = null;
			for (int i = 0; i < setMember.length; ++i) {
                Program p = (Program)program.getMember(setMember[i].getEntityPartId());
				memberProgram = updateProgram(setMember[i], p, sched,schedUsed,project,program,now);
                if(program.memberExists(memberProgram.getId())){
				    program.updateMember(memberProgram);
                } else {
				    program.addMember(memberProgram);
                }
			}
		}
		if (set.getObsUnitSetTChoice().getSchedBlockRefCount() > 0) {
			SchedBlockRefT[] setMember = set.getObsUnitSetTChoice().getSchedBlockRef();
			SB memberSB = null;
			for (int i = 0; i < setMember.length; ++i) {
                if(program.memberExists(setMember[i].getEntityId())){
                    SB currentSB = (SB)program.getMember(setMember[i].getEntityId());
				    memberSB = updateSchedBlock(setMember[i],sched,schedUsed,project,program,now,currentSB);
				    program.updateMember(memberSB);
                } else {
                    //todo get SBStatusT to use instead of null
				    memberSB = initialize(setMember[i], null, sched,schedUsed,project,program,now);
                    //check if its set to ready
                    if(!memberSB.getStatus().isReady()){
                        memberSB.setReady(now);
                    }
				    program.addMember(memberSB);
                }
			}
		}
		
		// Set the MaximumTimeInSeconds from the members.
        // recalculate in case there are more SBs added
		int maxTime = 0;
		ProgramMember[] member = program.getMember();
		for (int i = 0; i < member.length; ++i) {
			if (member[i] instanceof Program) {
				maxTime += ((Program)member[i]).getMaximumTimeInSeconds();
			} else
				maxTime += ((SB)member[i]).getMaximumTimeInSeconds();
		}
		program.setMaximumTimeInSeconds(maxTime);
		
		// Return the newly created program.
		program.setTimeOfUpdate(now);
        if(!program.getStatus().isReady()){
            program.setReady(now);
        }
		return program;
    }


    static private SB updateSchedBlock(SchedBlockRefT schedRef, SchedBlock[] schedArray,
            boolean[] schedUsed, Project project, Program parent, DateTime now, SB existingSB) 
        		throws SchedulingException {

        SB sb;
        if(existingSB == null) {
    		sb = new SB (schedRef.getEntityId());

            if(existingSB.getSbStatusId() == null) {
    		    sb.setSbStatusId(null); // This comes from ProjectStatus.
            } else { 
    		    sb.setSbStatusId(existingSB.getSbStatusId()); // This comes from ProjectStatus.
            }
	    	sb.setProject(project);
		    sb.setTimeOfCreation(now);
    		sb.setTimeOfUpdate(now);
	    	sb.setParent(parent);
        } else {
            sb =existingSB;
        }

		// We need to use the entityId to get the SchedBlock from the sched array.
		SchedBlock sched = null;
		for (int i = 0; i < schedArray.length; ++i) {
			if (schedArray[i].getSchedBlockEntity().getEntityId().equals(sb.getSchedBlockId())) {
				sched = schedArray[i];
				schedUsed[i] = true;
				break;
			}
		}
		if (sched == null)
			throw new SchedulingException("The scheduling block with id " + sb.getSchedBlockId() +
					" is not in the specified SchedBlock array.");
		
        sb.setSBName(sched.getName());
		// There are some problems here that we need to fix after R2.
		sb.setScientificPriority(Priority.MEDIUM); // Can't find this in ObsPrep << Problem.
		sb.setUserPriority(Priority.MEDIUM);
		sb.setScienceGoal(null);
		sb.setWeatherConstraint(null);
		sb.setRequiredInitialSetup(null);
		sb.setImagingScript(null);
		
		// Set the observing script.
		sb.setObservingScript(sched.getObsProcedure().getObsProcScript());
		if (sched.hasStandardMode())
			sb.setStandardScript(sched.getStandardMode());
		else
			sb.setStandardScript(false);

		// Set the time and repeat count.
		// IMPORTANT NOTE!
		// We must have a maximum time.  If it is not present, we will arbitrarily assign 30 minutes.
		// If there is no repeat count, we assume it to be 0.
		// We are also assuming that time is in minutes and that the repeat count is acutally
		// the number of executions (and not the number of repetitions).
		SchedBlockControlT ctrl = sched.getSchedBlockControl();
		if (ctrl == null) {
			sb.setMaximumTimeInSeconds(1800);
			sb.setMaximumNumberOfExecutions(0);
		} else {
			TimeT tt = ctrl.getSBMaximumTime();
			if (tt == null) {
				sb.setMaximumTimeInSeconds(1800); 
			} else {
				double maxTime = tt.getContent();
				sb.setMaximumTimeInSeconds((int)(maxTime * 60 + 0.05));
			}
			int execCount = ctrl.getExecutionCount();
			if (execCount < 1)
				throw new SchedulingException("Invalid execution count (" + execCount + ").");
            //set to -1 because the first run is the original one. Not considered a 'repeat'
			//sb.setMaximumNumberOfRepeats(repeatcount - 1); 
            //TODO Changed this to not have -1, coz scheduler doesn't think this way yet.
			sb.setMaximumNumberOfExecutions(execCount); 
            sb.setIndefiniteRepeat(ctrl.getIndefiniteRepeat());
  		}
            
        FieldSourceT[] fieldSources = sched.getFieldSource();
        FieldSourceT fs;
        TargetT[] targets = sched.getTarget();
        Source source;
        SkyCoordinatesT coord;
        String fs_id;
        Equatorial[] eq = new Equatorial[targets.length];
        String[] op_params_list;
        for(int i=0; i < targets.length; i++){
            fs_id = targets[i].getFieldSourceId();
            fs = getFieldSourceFromList(fieldSources, fs_id);
//////////////
// Can't do the following with out a field source..
            if(fs != null){
                coord = fs.getSourceCoordinates();
    	        if (coord == null){
        	       	throw new SchedulingException(
                            "There is no SkyCoordinatesT object in the scheduling block");
                }
                LongitudeT lng = coord.getLongitude();
                double ra = lng.getContent();
                LatitudeT lat = coord.getLatitude();
                double dec = lat.getContent();
    	        String coordType = coord.getSystem().toString(); // must be J2000
                if (!coordType.equals("J2000")){
                    throw new SchedulingException(coordType + " is not supported.  Must be J2000");
                }  
                eq[i] = new Equatorial((ra /24.0),dec);
                source = new Source();
                //TODO add source stuff..
                try {
                    source.setSourceName(fs.getSourceName());
                } catch(Exception e) {
                    source.setSourceName("Source was not named.");
                }
                try {
                    source.setSolarSystemObj(fs.getSolarSystemObject().toString());
                } catch(Exception e) {
                    source.setSolarSystemObj("Not a solar system object.");
                }
                SourcePropertyT[] sourceProperties = fs.getSourceProperty();
                try {
                    source.setNumberSourceProperties(fs.getSourcePropertyCount());
                    //only really care about the first one!
                    source.setVisibleMagnitude(sourceProperties[0].getVisibleMagnitude().getValue());
                } catch(Exception e){}
                try {
                    //TODO FIX Eventaully to get REAL values from SchedBlockChoice2
                    source.setMinMagnitude(1);
                    //source.setMinMagnitude(op_params.getMinMagnitude().getValue());
                    source.setMaxMagnitude(-1);
                    //source.setMaxMagnitude(op_params.getMaxMagnitude().getValue());
                } catch(Exception e){}

                sb.addSource(source);
            }
        }
		if (eq.length == 1) {
            Target target = new Target (eq[0],3600.0,3600.0);
            sb.setTarget(target);
		} else {
            Target target = new Target (eq);
            sb.setTarget(target);
		}
///////////////
        //TODO fix this temporary hack!!
        SchedulingConstraintsT constraints = sched.getSchedulingConstraints();
        FrequencyT freq = constraints.getRepresentativeFrequency();
    	sb.setCenterFrequency(freq.getContent());
        FrequencyBand band = getFrequencyBand(freq);
         sb.setFrequencyBand(band); 
        //TargetT t = constraints.getRepresentativeTargeT();
  
	    return sb;
	}
	//////////////////////////////////////////////////////////////////////
	// End of private methods are used to support the		 			//
	// "map(ObsProject p, SchedBlock[] b, ProjectStatus s)" method.		//
	//////////////////////////////////////////////////////////////////////
	
	/**
	 * Create a ProjectStatus object that acurately reflects the current status of the specified Project.
	 * @param project The Project used as a basis for creating the ProjectStatus
	 * @param now The current time.
	 * @return A ProjectStatus object that acurately reflects the current status of the specified Project.
	 * @throws SchedulingException If any error is found in the process of mapping.
	 */
	static public ProjectStatus map(Project project, DateTime now) throws SchedulingException {
		
		// First, we validate the project.
		validate(project);
		
		// Create a new project status.
		ProjectStatus pstatus = new ProjectStatus();
		
		// Create the entityId and add it to the new status.
		ProjectStatusEntityT entity = new ProjectStatusEntityT ();
		entity.setEntityId(project.getProjectStatusId());
		entity.setEntityIdEncrypted("none");
		entity.setDocumentVersion(project.getProjectVersion());
		entity.setSchemaVersion("1");
        //create a part Id ?
		pstatus.setProjectStatusEntity(entity);
		
		// Create the project reference and add it to the status.
		ObsProjectRefT obsProjectRef = new ObsProjectRefT ();
		obsProjectRef.setEntityId(project.getObsProjectId());
        //check for a part id in ObsProject
		obsProjectRef.setPartId(nullPartId);
		obsProjectRef.setDocumentVersion(project.getProjectVersion());
		pstatus.setObsProjectRef(obsProjectRef);
		
		// Create the proposal reference and add it to the status.
		ObsProposalRefT obsProposalRef = new ObsProposalRefT ();
		obsProposalRef.setEntityId(project.getProposalId());
        //check for a part id in ObsProposal
		obsProposalRef.setPartId(nullPartId);
		obsProposalRef.setDocumentVersion("1");
		pstatus.setObsProposalRef(obsProposalRef);
		
		// Fill in the remaining values from the project.
		pstatus.setName(project.getProjectName());
		pstatus.setPI(project.getPI());
		pstatus.setTimeOfUpdate(now.toString());
		// The state of the project.
        //System.out.println("project state = "+ project.getStatus().toString());
		pstatus.setStatus(assignState(project.getStatus()));
		pstatus.setBreakpointTime(project.getBreakpointTime() == null ? "" : project.getBreakpointTime().toString());
		// The obsProgram status.
		pstatus.setObsProgramStatus(assignObsProgramStatus(project.getProgram(),now));
		/***********************************
		 * test only need to move when bug fixed
		 */
		try {
		String xml;
        StringWriter writer = new StringWriter();
        pstatus.marshal(writer);
        xml = writer.toString();
        System.out.println("ProjectUtil=>project Status:"+xml);
		}
		catch (Exception e) {
			logger.severe("SCHEDULING: Error updating ProjectStatus.");
            e.printStackTrace(System.out);
		}
        /******************************************/
		
		// Return the newly created project status.
		return pstatus;
		
	}

    /**
      * Make sure that when we do our mapping we do not overwright a good ProjectStatus, so
      * by checking if it has a time of update we know whether to map a new ProjectStatus
      * or return the one passed in. Do not update the project status. 
      * Use updateProjectStatus to get a new project status which matches the project's 
      * current state.
      *
      */
    static public ProjectStatus map(Project p, ProjectStatus ps, DateTime t) throws SchedulingException{
        if(ps.getTimeOfUpdate() == null || ps.getTimeOfUpdate() == ""){
            return ProjectUtil.map(p, t);
        }   
        return ps;
    }

    /**
      * NOTE: DO NOT USE.
      * Updates the project status.
      *
      */
    static public ProjectStatus updateProjectStatus(Project p) throws SchedulingException{
        try {
            return ProjectUtil.map(p, new DateTime(System.currentTimeMillis()));
        } catch(Exception e){
            logger.severe("SCHEDULING: Error updating ProjectStatus.");
            e.printStackTrace(System.out);
            return null;
        }
    }

    static public ProjectStatus updateProjectStatus(Project p, ProjectStatus pstat, DateTime t) throws SchedulingException{
        if(!p.getProjectStatusId().equals(pstat.getProjectStatusEntity().getEntityId())){
            throw new SchedulingException("ProjectStatus UID doesn't match Project's project status reference UID.");
        }
        ProjectStatus ps = pstat;
        ps.setTimeOfUpdate(t.toString());
        ps.setStatus(assignState(p.getStatus()));
        ps.setObsProgramStatus(updateProgramStatus(p.getProgram() ,
                    ps.getObsProgramStatus(),  t));
        return ps;
        
    }

    static public ProjectStatus updateProjectStatus(ProjectStatus oldPS, 
                                                    ProjectStatus newPS, 
                                                    DateTime t){
        return oldPS;
    }

    static private ObsUnitSetStatusT updateProgramStatus(Program prog, ObsUnitSetStatusT obs, DateTime t) 
        throws SchedulingException {
        
        return obs;
    }
	
	//////////////////////////////////////////////////////////////
	// The following private methods are used to support the 	//
	// "map(Project project, DateTime now)" method.				//
	//////////////////////////////////////////////////////////////
	
	static private StatusT assignState(Status source) {
        //System.out.println("State="+source.getState().toString());
		StatusT target = new StatusT ();
		switch (source.getStatusAsInt()) {
			case Status.NOTDEFINED: target.setState(StatusTStateType.NOTDEFINED); break;
			case Status.WAITING: target.setState(StatusTStateType.WAITING); break;
			case Status.READY: target.setState(StatusTStateType.READY); break;
			case Status.RUNNING: target.setState(StatusTStateType.RUNNING); break;
			case Status.ABORTED: target.setState(StatusTStateType.ABORTED); break;
			case Status.COMPLETE: target.setState(StatusTStateType.COMPLETE); break;
			case Status.OBSERVED: target.setState(StatusTStateType.OBSERVED); break;
			case Status.PROCESSED: target.setState(StatusTStateType.PROCESSED); break;
			case Status.CANCELED: target.setState(StatusTStateType.CANCELED); break;
		}
		target.setReadyTime(source.getReadyTime() == null ? "" : source.getReadyTime().toString());
		target.setStartTime(source.getStartTime() == null ? "" : source.getStartTime().toString());
		target.setEndTime(source.getEndTime() == null ? "" : source.getEndTime().toString());
		return target;
	}
	
	static private ObsUnitSetStatusT assignObsProgramStatus(Program source, DateTime now) {
		ObsUnitSetStatusT target = new ObsUnitSetStatusT ();
		target.setEntityPartId(source.getId());
		ObsProjectRefT obsUnitSetRef = new ObsProjectRefT ();
		obsUnitSetRef.setEntityId(source.getProject().getObsProjectId());
		obsUnitSetRef.setPartId(source.getProgramId());
		obsUnitSetRef.setDocumentVersion("1");
		target.setObsUnitSetRef(obsUnitSetRef);
		target.setTimeOfUpdate(now.toString());

		target.setNumberSBsCompleted(source.getNumberSBsCompleted());
		// Set the state of this program.
		target.setStatus(assignState(source.getStatus()));
		
		// Set the totals.
		target.setTotalRequiredTimeInSec(source.getTotalRequiredTimeInSeconds());
		target.setTotalUsedTimeInSec(source.getTotalUsedTimeInSeconds());
		target.setTotalObsUnitSets(source.getTotalPrograms());
		target.setNumberObsUnitSetsCompleted(source.getNumberProgramsCompleted());
		target.setNumberObsUnitSetsFailed(source.getNumberProgramsFailed());
		target.setTotalSBs(source.getTotalSBs());
		target.setNumberSBsCompleted(source.getNumberSBsCompleted());
		target.setNumberSBsFailed(source.getNumberSBsFailed());
		
		// Set the session list.
        SessionT[] list = assignSession(source.getAllSession());
		target.setSession(list);
		
		// Set the PipelineProcessingRequest.
		target.setPipelineProcessingRequest(assignPPR(source.getSciPipelineRequest()));
		
		// Set the members of this program.
		ProgramMember[] member = source.getMember();
		Program pgm = null;
		SB sb = null;
		ObsUnitSetStatusTChoice set = new ObsUnitSetStatusTChoice ();
        if (member.length > 0 ){
    		if (member[0] instanceof Program) {
	    		for (int i = 0; i < member.length; ++i) {
		    		pgm = (Program)member[i];
			    	set.addObsUnitSetStatus(assignObsProgramStatus(pgm,now));
    			}			
	    	} else {
		    	for (int i = 0; i < member.length; ++i) {
			    	sb = (SB)member[i];
				    set.addSBStatus(assignSBStatus(sb,now));
			    }
            }
		}
		target.setObsUnitSetStatusTChoice(set);
		return target;
	}
	
 

	static private SessionT[] assignSession(ObservedSession[] session) {
		SessionT[] list = new SessionT [session.length];
		SessionT x = null; 
        try {
		for (int i = 0; i < session.length; ++i) {
			x = new SessionT ();
			// Set the part id.
			x.setEntityPartId(session[i].getSessionId());
			// Set the start time.
			x.setStartTime(session[i].getStartTime().toString());
			// Set the end time.
            try {
			    x.setEndTime(session[i].getEndTime().toString());
            } catch (NullPointerException npe) {
			    x.setEndTime("End time not known yet.");
            }
			// Set the reference to the ObsUnitSetStatus.
            /*
			ProjectStatusRefT pRef = new ProjectStatusRefT ();
			pRef.setEntityId(session[i].getProgram().getProject().getProjectStatusId());
			pRef.setDocumentVersion("1");
			pRef.setPartId(session[i].getSessionId());
            */
			// This isn't there but should be. 
			//x.setObsUnitSetStatusRef(pRef);
			// Set the list of exec block references.
			ExecBlock[] s = session[i].getExec();
			ExecBlockRefT[] execRef = new ExecBlockRefT [s.length];
			for (int j = 0; j < s.length; ++j) {
				execRef[j] = new ExecBlockRefT ();
				execRef[j].setExecBlockId(s[j].getExecId());
			}
			x.setExecBlockRef(execRef);
			list[i] = x;
		}
        }catch(Exception e) {
            logger.severe("SCHEDULING: error assigning session to PS");
            e.printStackTrace();
        }
		return list;
	}
    
    static private Session[] addSession(ObservedSession session){
        return null;
    }
	
	static private PipelineProcessingRequestT assignPPR(SciPipelineRequest ppr) {
		if (ppr == null){
			return null;
        }
		PipelineProcessingRequestT target = new PipelineProcessingRequestT ();
		// Set the entity part id.
		target.setEntityPartId(ppr.getId());
		// Set the time of creation.
		target.setTimeOfCreation(ppr.getStatus().getReadyTime().toString());
		// Set the time of update.
		if (ppr.getStatus().getEndTime() != null)
			target.setTimeOfUpdate(ppr.getStatus().getEndTime().toString());
		else if (ppr.getStatus().getStartTime() != null)
			target.setTimeOfUpdate(ppr.getStatus().getStartTime().toString());
		else if (ppr.getStatus().getReadyTime() != null)
			target.setTimeOfUpdate(ppr.getStatus().getReadyTime().toString());
		else
			target.setTimeOfUpdate("");
		// Set the reference to the ObsUnitSetStatus.
		ProjectStatusRefT pRef = new ProjectStatusRefT ();
		pRef.setEntityId(ppr.getProgram().getProject().getProjectStatusId());
		pRef.setDocumentVersion("1");
		//pRef.setPartId(ppr.getId());
        //pRef.setPartId(ppr.getProgram().getObsUnitSetStatusId());
        pRef.setPartId(ppr.getProgram().getProgramId());
		target.setObsUnitSetStatusRef(pRef);
		// Set the request status.
		if (ppr.getStatus().getStartTime() == null)
			target.setRequestStatus(PipelineProcessingRequestTRequestStatusType.QUEUED);
		else if (ppr.getStatus().getEndTime() == null)
			target.setRequestStatus(PipelineProcessingRequestTRequestStatusType.RUNNING);
		else
			target.setRequestStatus(PipelineProcessingRequestTRequestStatusType.COMPLETED);
		// Set the completion status.
		if (ppr.getStatus().getEndTime() == null) {
			if (ppr.getStatus().getReadyTime() != null)
				target.setCompletionStatus(PipelineProcessingRequestTCompletionStatusType.SUBMITTED);
			else 
				target.setCompletionStatus(PipelineProcessingRequestTCompletionStatusType.INCOMPLETE);
		} else if (ppr.getStatus().isAborted())
			target.setCompletionStatus(PipelineProcessingRequestTCompletionStatusType.COMPLETE_FAILED);
		else if (ppr.getStatus().isComplete())
			target.setCompletionStatus(PipelineProcessingRequestTCompletionStatusType.COMPLETE_SUCCEEDED);
		// Set the comment.
		target.setComment(ppr.getComment());
		// Set the imaging procedure name.
		target.setImagingProcedureName(ppr.getReductionProcedureName());
		// Set the processing parameters.
		Object[] parm = ppr.getParms();
        if(parm != null) {
            PipelineParameterT[] pparams = new PipelineParameterT[parm.length];
		    for (int i = 0; i < parm.length; ++i){
                pparams[i] = new PipelineParameterT();
                if(parm[i].getClass().getName().equals("alma.entity.xmlbinding.valuetypes.SmallAngleT")){
                    
                    pparams[i].setName("AngularResolution");
                    pparams[i].setValue(""+ ((DoubleWithUnitT)parm[i]).getContent() + " " + 
                            ((DoubleWithUnitT)parm[i]).getUnit()); 
                    
                } else if(parm[i].getClass().getName().equals("alma.entity.xmlbinding.valuetypes.VelocityT")){
                    
                    pparams[i].setName("VelocityResolution");
                    pparams[i].setValue(""+ ((VelocityT)parm[i]).getReferenceSystem().toString() + ": " + 
                            ((VelocityT)parm[i]).getCenterVelocity().getContent() +" "+ 
                            ((VelocityT)parm[i]).getCenterVelocity().getUnit()); 
                    
                } else if(parm[i].getClass().getName().equals("alma.entity.xmlbinding.valuetypes.TemperatureT")){
                    
                    pparams[i].setName("TBSensitivityGoal");
                    pparams[i].setValue(""+ ((DoubleWithUnitT)parm[i]).getContent() +" "+
                             ((DoubleWithUnitT)parm[i]).getUnit());
                    
                } else if(parm[i].getClass().getName().equals("alma.entity.xmlbinding.valuetypes.SensitivityT")){

                    pparams[i].setName("RMSGoal");
                    pparams[i].setValue(""+ ((DoubleWithUnitT)parm[i]).getContent() +" "+
                             ((DoubleWithUnitT)parm[i]).getUnit());
                    
                } else if(parm[i].getClass().getName().equals(
                            "alma.entity.xmlbinding.obsproject.types.DataProcessingParametersTProjectTypeType")){

                    pparams[i].setName("ProjectType");
                    pparams[i].setValue(parm[i].toString());
                } else {
                    logger.fine("SCHED: Class type for data processing param is: "+parm[i].getClass().getName());
                }
            }
    		target.setPipelineParameter(pparams);
        } else {
            logger.warning("SCHEDULING: Pipeline Params = null!");
        }
		// OK, we're done.
		return target;
	}

		
	static private SBStatusT assignSBStatus(SB sb, DateTime now) {
		SBStatusT target = new SBStatusT ();
		
		// Set the status part-id.
		target.setEntityPartId(sb.getSbStatusId());
		
		// Set the reference to the SchedBlock.
		SchedBlockRefT sbRef = null;
		sbRef = new SchedBlockRefT ();
		sbRef.setEntityId(sb.getSchedBlockId());
		sbRef.setPartId(nullPartId);
		sbRef.setDocumentVersion("?");
		target.setSchedBlockRef(sbRef);
		
		// set remaining variables.
		target.setTimeOfUpdate(now.toString());

        //System.out.println("assigning state for SB "+sbRef);
        //System.out.println("state for SB "+sb.getStatus().getState().toString());
		target.setStatus(assignState(sb.getStatus()));
		target.setTotalRequiredTimeInSec(sb.getTotalRequiredTimeInSeconds());
		target.setTotalUsedTimeInSec(sb.getTotalUsedTimeInSeconds());

		// Set the ExecBlock list.
		target.setExecStatus(assignExec(sb.getExec(),now));
		
		return target;
	}
	
	static private ExecStatusT[] assignExec(ExecBlock[] ex, DateTime now) {
		ExecStatusT[] list = new ExecStatusT [ex.length];
		ExecStatusT exStatus = null;
		ExecBlockRefT execRef = null;
		StatusT state = null;
		BestSBT bestSB = null;
		for (int i = 0; i < ex.length; ++i) {
			exStatus = new ExecStatusT ();
			// Set entity part-id.
			exStatus.setEntityPartId(ex[i].getExecStatusId());
			// Set execblock reference.
			execRef = new ExecBlockRefT ();
			execRef.setExecBlockId(ex[i].getExecId());
			exStatus.setExecBlockRef(execRef);
			// Set times.
            try {
			    exStatus.setTimeOfCreation(ex[i].getStatus().getStartTime().toString());
            } catch(Exception x){
                x.printStackTrace();
            }
			exStatus.setTimeOfUpdate(now.toString());
			// Set state.
			exStatus.setStatus(assignState(ex[i].getStatus()));
			// Set subarray-id.
            //TODO This will change!
			//exStatus.setSubarrayId(ex[i].getSubarrayId());
            exStatus.setArrayName(ex[i].getArrayName()); //will change to ArrayName thing
			// Set BestSB.
			//exStatus.setBestSB(assignBestSB(ex[i].getBest()));
			list[i] = exStatus;
		}
		return list;
	}
	
	static BestSBT assignBestSB(BestSB best) {
		BestSBT target = new BestSBT ();
		
		
		// Set the array of SB-ids.
		String[] s = best.getSbId();
        double[] success = best.getSuccess();
        double[] ranks = best.getRank();
        String[] scores = best.getScoreString();
		// The above really should be SchedBlockRefT.
        
		for (int i = 0; i < s.length; ++i) {
            BestSBItemT bestSBItem = new BestSBItemT();
            bestSBItem.setScore(scores[i]);
            bestSBItem.setSuccess(Double.toString(success[i]));
            bestSBItem.setRank(Double.toString(ranks[i]));
            SchedBlockRefT sbRef = new SchedBlockRefT();
			sbRef.setEntityId(s[i]);
			sbRef.setPartId(nullPartId);
			sbRef.setDocumentVersion("?");
            target.addBestSBItem(bestSBItem);
		}
		
		// Set selection and time of selection.
		target.setSelection(best.getSelection());
		target.setTimeOfSelection(best.getTime().toString());
		
		return target;
	}
	
	//////////////////////////////////////////////////////////////
	// End of private methods are used to support the		 	//
	// "map(Project project, DateTime now)" method.				//
	//////////////////////////////////////////////////////////////
	
    static public ObsProject addPartIds(ObsProject obs) throws SchedulingException {
        ObsUnitSetT set = obs.getObsProgram().getObsPlan();
        if(set.getEntityPartId() == null) {
            String id = genPartId();
            set.setEntityPartId(id);
        }
        doObsUnitSetIds(set);
        return obs;
        
    }
    static private void doObsUnitSetIds(ObsUnitSetT set) {
        if (set.getObsUnitSetTChoice().getObsUnitSetCount() > 0) {
			ObsUnitSetT[] setMember = set.getObsUnitSetTChoice().getObsUnitSet();
			for (int i = 0; i < setMember.length; ++i) {
                if(setMember[i].getEntityPartId() == null) {
                    String id = genPartId();
                    setMember[i].setEntityPartId(id);
                }
				doObsUnitSetIds(setMember[i]);
            }
        }

    }

    /**
      * Check if there is an ObsUnitSetStatus in the given choice with the given id.
      * @return the index of where it is in the current choice, otherwise -1 
      */
    static private int obsUnitSetStatusExists(ObsUnitSetStatusTChoice choice, String id) {
        ObsUnitSetStatusT[] ous = choice.getObsUnitSetStatus();
        for(int i=0; i < ous.length; i++){
            if(ous[i].getEntityPartId().equals(id)){
                return i;
            }
        }
        return -1;

    }
    
    static private int sbStatusExists(ObsUnitSetStatusTChoice choice, String id) {
        SBStatusT[] sbs = choice.getSBStatus();
        for(int i=0; i < sbs.length; i++){
            if(sbs[i].getSchedBlockRef().getEntityId().equals(id)){
                return i;
            }
        }
        return -1;
    }

    static public SB updateSB(SB oldSB, SchedBlock newSB, DateTime now) 
        throws SchedulingException {
            
        SchedBlock sched = newSB;
        
        SB sb = oldSB;
        try{
        	sb.setSbStatusId(null);
        	
            sb.setTimeOfUpdate(now);
            sb.setSBName(sched.getName());
            int tac = sched.getObsUnitControl().getTacPriority();
            if(tac > 10 || tac < 1) {
                tac = 1;
            }
            sb.setScientificPriority(new Priority(tac));
            int pri = sched.getObsUnitControl().getUserPriority();
            if(pri > 10 || pri < 1) {
                pri = 1;
            }
            sb.setUserPriority(new Priority(pri));
            //new add for verify*************** 
            sb.setScienceGoal(null);
            sb.setWeatherConstraint(null);
            SBSetup initialSetup = new SBSetup();
            sb.setRequiredInitialSetup(initialSetup);
            sb.setImagingScript(null);
            //***************************
            
            sb.setObservingScript(sched.getObsProcedure().getObsProcScript());
            if (sched.hasStandardMode()){
                sb.setStandardScript(sched.getStandardMode());
            }else {
                sb.setStandardScript(false);
            }
            SchedBlockControlT ctrl = sched.getSchedBlockControl();
            if (ctrl == null) {
                sb.setMaximumTimeInSeconds(1800);
                sb.setMaximumNumberOfExecutions(1);
            } else {
                TimeT tt = ctrl.getSBMaximumTime();
                if (tt == null) {
                    sb.setMaximumTimeInSeconds(1800);
                } else {
                    double maxTime = tt.getContent();
                    sb.setMaximumTimeInSeconds((int)(maxTime * 60 + 0.05));
                }
                int execcount = ctrl.getExecutionCount();
                if (execcount < 1){
                    throw new SchedulingException("Invalid execution count (" + execcount + ").");
                }
                sb.setMaximumNumberOfExecutions(execcount);
                sb.setIndefiniteRepeat(ctrl.getIndefiniteRepeat());
                FieldSourceT[] fieldSources = sched.getFieldSource();
                FieldSourceT fs;
                TargetT[] targets = sched.getTarget();
                Source source;
                SkyCoordinatesT coord;
                String fs_id;
                Equatorial[] eq = new Equatorial[targets.length];
                String[] op_params_list;
                for(int i=0; i < targets.length; i++){
                    fs_id = targets[i].getFieldSourceId();
                    fs = getFieldSourceFromList(fieldSources, fs_id);
                    if(fs != null){
                        coord = fs.getSourceCoordinates();
                        if (coord == null){
                            throw new SchedulingException(
                               "There is no SkyCoordinatesT object in the scheduling block");
                        }
                        LongitudeT lng = coord.getLongitude();
                        double ra = lng.getContent();
                        LatitudeT lat = coord.getLatitude();
                        double dec = lat.getContent();
                        String coordType = coord.getSystem().toString(); // must be J2000
                        if (!coordType.equals("J2000")){
                            throw new SchedulingException(coordType + " is not supported.  Must be J2000");
                        }
                        eq[i] = new Equatorial((ra /24.0),dec);
                        source = new Source();
                        //TODO add source stuff..
                        try {
                            source.setSourceName(fs.getSourceName());
                        } catch(Exception e) {
                            source.setSourceName("Source was not named.");
                        }
                        try {
                            source.setSolarSystemObj(fs.getSolarSystemObject().toString());
                        } catch(Exception e) {
                            source.setSolarSystemObj("Not a solar system object.");
                        }
                        SourcePropertyT[] sourceProperties = fs.getSourceProperty();
                        try {
                            source.setNumberSourceProperties(fs.getSourcePropertyCount());
                            //only really care about the first one!
                            source.setVisibleMagnitude(sourceProperties[0].getVisibleMagnitude().getValue());
                        } catch(Exception e){}
                        try {
                            //TODO FIX Eventaully to get REAL values from SchedBlockChoice2
                            source.setMinMagnitude(1);
                            //source.setMinMagnitude(op_params.getMinMagnitude().getValue());
                            source.setMaxMagnitude(-1);
                            //source.setMaxMagnitude(op_params.getMaxMagnitude().getValue());
                        } catch(Exception e){}
    
                        sb.addSource(source);
                    } else {
                        eq[i] = new Equatorial(0.0,0.0);
                    }
                    if (eq.length <= 1) {
                        Target target = new Target (eq[0],3600.0,3600.0);
                        sb.setTarget(target);
                    } else {
                        Target target = new Target (eq);
                        sb.setTarget(target);
                    }
                    SchedulingConstraintsT constraints = sched.getSchedulingConstraints();
                    FrequencyT freq = constraints.getRepresentativeFrequency();
                    sb.setCenterFrequency(freq.getContent());
                    FrequencyBand band = getFrequencyBand(freq);
                    sb.setFrequencyBand(band);
                }
            }
        } catch(Exception e){
            throw new SchedulingException (e);
        }
        return sb;
    }
}
