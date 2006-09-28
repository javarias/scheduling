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
import alma.entity.xmlbinding.valuetypes.DoubleWithUnitT;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.entity.xmlbinding.schedblock.FrequencySetupT;
import alma.entities.commonentity.*;

import alma.scheduling.Define.SchedulingException;
import alma.scheduling.Define.Project;
import alma.scheduling.Define.Program;
import alma.scheduling.Define.ProgramMember;
import alma.scheduling.Define.ObservedSession;
import alma.scheduling.Define.SciPipelineRequest;
import alma.scheduling.Define.SB;
import alma.scheduling.Define.ExecBlock;
import alma.scheduling.Define.BestSB;
import alma.scheduling.Define.Status;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.Priority;
import alma.scheduling.Define.FrequencyBand;
import alma.scheduling.Define.Equatorial;
import alma.scheduling.Define.Target;
import alma.scheduling.Define.Source;

import java.util.ArrayList;

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
 * @version $Id: ProjectUtil.java,v 1.43 2006/09/28 21:38:28 sslucero Exp $
 * @author Allen Farris
 */
public class ProjectUtil {
	
	static private final String nullPartId = "X00000000";

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
		
		checkEntityId(project.getObsProjectId());
		checkEntityId(project.getProjectStatusId());
		checkEntityId(project.getProposalId());
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
	
	/**
	 * Check an EntityId: format "uid://X0000000000000079/X00000000"
	 * @param s The string to be checked.
	 * @throws SchedulingException If the string has an invalid format.
	 */
	static public void checkEntityId(String s) throws SchedulingException {
		if (s == null || s.length() == 0)
			return;
		if ((s.length() != 33) || (!s.startsWith("uid://X")) ||
				(s.charAt(23) != '/' || s.charAt(24) != 'X'))
			throw new SchedulingException ("Invalid format for EntityId (" + s + ")");
		for (int i = 7; i < 23; ++i) {
			if (!((s.charAt(i) >= '0' && s.charAt(i) <= '9') ||
				  (s.charAt(i) >= 'a' && s.charAt(i) <= 'f')))
				throw new SchedulingException ("Invalid format for EntityId (" + s + ")");
		}
		for (int i = 25; i < 33; ++i) {
			if (!((s.charAt(i) >= '0' && s.charAt(i) <= '9') ||
					(s.charAt(i) >= 'a' && s.charAt(i) <= 'f')))
				throw new SchedulingException ("Invalid format for EntityId (" + s + ")");
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
		Project project = initialize(p,b,s.getProjectStatusEntity().getEntityId(),now);
		
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
			                          String projectStatusEntityId, DateTime now) 
                                      throws SchedulingException {

		Project project = null;
        try {
        project = new Project (obs.getObsProjectEntity().getEntityId(),
									   obs.getObsProposalRef().getEntityId(),
									   obs.getProjectName(),
									   obs.getVersion(),
									   obs.getPI());
		project.setProjectStatusId(projectStatusEntityId);
        if(project.getTimeOfCreation() == null) {
    		project.setTimeOfCreation(now);
        }
		project.setTimeOfUpdate(now);
		//project.setBreakpoint(null); Sohaila: Took out coz in Define/Project this throws an error!
		
		// To check if all SchedBlocks are used, we will create an array
		// of booleans and check them off as we use them.
		boolean[] schedUsed = new boolean [sched.length];
		for (int i = 0; i < schedUsed.length; ++i)
			schedUsed[i] = false;
		
		// Initialize the obsProgram.
		//ObsUnitSetT obsProgram = obs.getObsProgram().getObsPlan();
		Program program = initialize(obs.getObsProgram().getObsPlan(),sched,schedUsed,project,null,now);
		project.setProgram(program);
		
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
		// Mark the project as ready.  (This also initializes the totals.)
		project.setReady(now);
		
		// Now, validate the project
		validate(project);
		} catch(Exception e) {
            e.printStackTrace(System.out);
        }
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
			DateTime now) throws SchedulingException {
		
	  Program program = null;
      try {
        if(set.getEntityPartId() == null) {
            set.setEntityPartId(genPartId());
        }
        program = new Program (set.getEntityPartId());
		program.setProject(project);
		//program.setObsUnitSetStatusId(null); // We get this from the ProjectStatus.
		program.setTimeOfCreation(now);
		program.setTimeOfUpdate(now);
		program.setParent(parent);
		// We get SciPipelineRequest and Sessions from the ProjectStatus. 
		program.setScientificPriority(Priority.MEDIUM); // Where is this in ObsPrep?
		program.setUserPriority(Priority.MEDIUM);
		program.setDataReductionProcedureName(set.getScienceProcessingScript());
        //System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
       // System.out.println("About to set data processing paramters");
        try {
            //System.out.println("Assigning data processing parameters to program");
            Object[] params = new Object[5];
            params[0] = set.getDataProcessingParameters().getAngularResolution();
            //System.out.println(params[0].getClass().getName());
            params[1] = set.getDataProcessingParameters().getVelocityResolution();
            //System.out.println(params[1].getClass().getName());
            params[2] = set.getDataProcessingParameters().getTBSensitivityGoal();
            //System.out.println(params[2].getClass().getName());
            params[3] = set.getDataProcessingParameters().getRMSGoal();
            //System.out.println(params[3].getClass().getName());
            params[4] = set.getDataProcessingParameters().getProjectType();
            //System.out.println(params[4].getClass().getName());
    		program.setDataReductionParameters(params);
        } catch(Exception e) {
            //System.out.println("Problem, setting data processing params to null");
            //e.printStackTrace();
		    program.setDataReductionParameters(null);
        }
		program.setFlowControl(null);
		program.setNotify(null);
		program.setScienceGoal(null);
		program.setWeatherConstraint(null);
		program.setCenterFrequency(0.0);
		program.setFrequencyBand(null);
		program.setRequiredInitialSetup(null);
	
        //System.out.println( set.getObsUnitSetTChoice().getSchedBlockRefCount());

		// Assign the members of this set: either Program or SB objects.
		if (set.getObsUnitSetTChoice().getObsUnitSetCount() > 0) {
            //System.out.println("More than one obs unit sets");
			ObsUnitSetT[] setMember = set.getObsUnitSetTChoice().getObsUnitSet();
			Program memberProgram = null;
			for (int i = 0; i < setMember.length; ++i) {
				memberProgram = initialize(setMember[i],sched,schedUsed,project,program,now);
				program.addMember(memberProgram);
			}
		}
		if (set.getObsUnitSetTChoice().getSchedBlockRefCount() > 0) {
			SchedBlockRefT[] setMember = set.getObsUnitSetTChoice().getSchedBlockRef();
			SB memberSB = null;
			for (int i = 0; i < setMember.length; ++i) {
				memberSB = initialize(setMember[i],sched,schedUsed,project,program,now);
				program.addMember(memberSB);
                }
			}
		}
		
		// Set the MaximumTimeInSeconds from the members.
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
          e.printStackTrace(System.out);
      }
      // Return the newly created program.
	  return program;
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
	static private SB initialize(SchedBlockRefT schedRef, SchedBlock[] schedArray, boolean[] schedUsed, Project project, Program parent, DateTime now) 
		throws SchedulingException {
            
            //TODO Need to split all this stuff up...
            
    	SB sb = new SB (schedRef.getEntityId());
        try {
    
    		sb.setSbStatusId(null); // This comes from ProjectStatus.
	    	sb.setProject(project);
		    sb.setTimeOfCreation(now);
    		sb.setTimeOfUpdate(now);
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
		    sb.setScientificPriority(Priority.MEDIUM); // Can't find this in ObsPrep << Problem.
    		sb.setUserPriority(Priority.MEDIUM);
	    	sb.setScienceGoal(null);
		    sb.setWeatherConstraint(null);
    		sb.setRequiredInitialSetup(null);
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
	    		sb.setMaximumNumberOfRepeats(0);
    		} else {
		    	TimeT tt = ctrl.getSBMaximumTime();
	    		if (tt == null) {
    				sb.setMaximumTimeInSeconds(1800); 
			    } else {
		    		double maxTime = tt.getContent();
	    			sb.setMaximumTimeInSeconds((int)(maxTime * 60 + 0.05));
    			}
			    int repeatcount = ctrl.getRepeatCount();
		    	if (repeatcount < 1){
	    			throw new SchedulingException("Invalid repeat count (" + repeatcount + ").");
                }
            //set to -1 because the first run is the original one. Not considered a 'repeat'
			//sb.setMaximumNumberOfRepeats(repeatcount - 1); 
            //TODO Changed this to not have -1, coz scheduler doesn't think this way yet.
    			sb.setMaximumNumberOfRepeats(repeatcount); 
                sb.setIndefiniteRepeat(ctrl.getIndefiniteRepeat());

    		}
            
            FieldSourceT[] fieldSources = sched.getFieldSource();
            FieldSourceT fs;
            TargetT[] targets = sched.getTarget();
            Source source;
            SkyCoordinatesT coord;
            String fs_id;
            Equatorial[] eq = new Equatorial[targets.length];
            //OpticalPointingParametersT op_params = sched.getOpticalPointingParameters();
            String[] op_params_list;
            for(int i=0; i < targets.length; i++){
                fs_id = targets[i].getFieldSourceId();
                fs = getFieldSourceFromList(fieldSources, fs_id);
                if(fs == null){
	    	        throw new SchedulingException(
                            "There is no FieldSourceT object in the scheduling block");
                }
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
    		if (eq.length == 1) {
		    	Target target = new Target (eq[0],3600.0,3600.0);
	    		sb.setTarget(target);
    		} else {
		    	Target target = new Target (eq);
	    		sb.setTarget(target);
    		}

            //SpectralSpecT[] setup = sched.getSchedBlockChoice().getSpectralSpec();
		    //alma.entity.xmlbinding.schedblock.FrequencySetupT freqSetup = setup[0].getFrequencySetup();
	    	//if (freqSetup == null) {
            
            //TODO fix this temporary hack!!
    			sb.setCenterFrequency(0.0);
			    sb.setFrequencyBand(new FrequencyBand("TempBand",100, 200));
		    
                //} else {
	    	//	sb.setCenterFrequency(freqSetup.getRestFrequency().getContent());
    		//	String band = freqSetup.getReceiverBand().toString();
			  //  FrequencyBand freq = new FrequencyBand(band,50.0,150.0); // These reanges are merely place-holders.
		    //	sb.setFrequencyBand(freq);
	    //	}
        }catch(Exception e){
            e.printStackTrace();    
        }
		return sb;
	}
    private static FieldSourceT getFieldSourceFromList(FieldSourceT[] fs, String id){
        for(int i=0; i < fs.length; i++){
            if(fs[i].getEntityPartId().equals(id)){
                return fs[i];
            }
        }
        return null;
    }

//////////////                    
 /*   public static Source createOpticalSBSource(OpticalCameraControlT oc_ctrl, 
            OpticalCameraTargetT target, ArrayList allEqs) 
                throws SchedulingException { //target == opticalTargetList[i]

        Source sbSource = new Source();
        FieldSourceT fieldSource = target.getFieldSource();
    	if (fieldSource == null){
	    	throw new SchedulingException("There is no FieldSourceT object in the scheduling block");// with id " + 
        }
	    SkyCoordinatesT coord = fieldSource.getSourceCoordinates();
    	if (coord == null){
	       	throw new SchedulingException("There is no SkyCoordinatesT object in the scheduling block"); //with id " + 
        }
	    LongitudeT lng = coord.getLongitude(); 	// in degrees
    	double ra = lng.getContent();
		LatitudeT lat = coord.getLatitude();	// in degrees
	    double dec = lat.getContent();
    	String coordType = coord.getSystem().toString(); // must be J2000
        if (!coordType.equals("J2000")){
            throw new SchedulingException(coordType + " is not supported.  Must be J2000");
        }
        Equatorial eq = new Equatorial((ra /24.0),dec);
        allEqs.add(eq);
        try {
            sbSource.setSourceName(fieldSource.getSourceName());
        } catch(Exception e) {
            sbSource.setSourceName("Source was not named.");
        }
        try {
            sbSource.setSolarSystemObj(fieldSource.getSolarSystemObject().toString());
        } catch(Exception e) {
            sbSource.setSolarSystemObj("Not a solar system object.");
        }
        SourcePropertyT[] sourceProperties = fieldSource.getSourceProperty();
        try {
            sbSource.setNumberSourceProperties(fieldSource.getSourcePropertyCount());
            //only really care about the first one!
            sbSource.setVisibleMagnitude(sourceProperties[0].getVisibleMagnitude().getValue());
        } catch(Exception e){}
        try {
            sbSource.setMinMagnitude(oc_ctrl.getMinMagnitude().getValue());
            sbSource.setMaxMagnitude(oc_ctrl.getMaxMagnitude().getValue());
        } catch(Exception e){}
        return sbSource;
    }    

    
    public static Source createObsTargetSource(ObsTargetT target, ArrayList eqList, FrequencySetupT freq)
        throws SchedulingException {
    	FieldSourceT fieldSource = target.getFieldSource();
        if (fieldSource == null){
            throw new SchedulingException("There is no FieldSourceT object in the scheduling block");// with id " + 
//                    sb.getSchedBlockId());
        }
        SkyCoordinatesT coord = fieldSource.getSourceCoordinates();
        if (coord == null){
            throw new SchedulingException("There is no SkyCoordinatesT object in the scheduling block ");//with id " + 
    //                sb.getSchedBlockId());
        }
        LongitudeT lng = coord.getLongitude(); 	// in degrees
        try {
            double ra = lng.getContent();
            LatitudeT lat = coord.getLatitude();	// in degrees
            double dec = lat.getContent();
            Equatorial eq = new Equatorial((ra /24.0),dec);
            eqList.add(eq);
        } catch(Exception e) {
            System.out.println("Equatorial not created");
        }
        String coordType = coord.getSystem().toString(); // must be J2000
        if (!coordType.equals("J2000"))
            throw new SchedulingException(coordType + " is not supported.  Must be J2000");
        Source sbSource = new Source();
        if(fieldSource.getSourceName() != null){
            sbSource.setSourceName(fieldSource.getSourceName());
        } else {
            sbSource.setSourceName("Source was not named.");
        }
        try {
            sbSource.setSolarSystemObj(fieldSource.getSolarSystemObject().toString());
        } catch(Exception e) {
            sbSource.setSolarSystemObj("Not a solar system object.");
        }
        SourcePropertyT[] sourceProperties = fieldSource.getSourceProperty();
        if(sourceProperties != null) {
            sbSource.setNumberSourceProperties(fieldSource.getSourcePropertyCount());
            //only really care about the first one!
            try {
                sbSource.setVisibleMagnitude(sourceProperties[0].getVisibleMagnitude().getValue());
            } catch(NullPointerException npe) {
                sbSource.setVisibleMagnitude(0.0);
            }
        }
        if(freq != null) {
            //don't think units are set for frequency...
            sbSource.setRestFrequency(freq.getRestFrequency().getContent(), "");
            sbSource.setTransition(freq.getTransitionName());
        }
        return sbSource;
    }*/
///////////////                    
	
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
    
    
    /*
    static public Project updateProject(ObsProject obs, Project project, SchedBlock[] sched, DateTime now) 
        throws SchedulingException {
        
            project.setTimeOfUpdate(now);
            //project.setBreakpoint(null); Sohaila: Took out coz in Define/Project this throws an error!
		
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
            program.setScientificPriority(Priority.MEDIUM); // Where is this in ObsPrep?
            program.setUserPriority(Priority.MEDIUM);
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
                    //System.out.println("updating program");
				    program.updateMember(memberProgram);
                } else {
                    //System.out.println("adding program");
				    program.addMember(memberProgram);
                }
			}
		}
		if (set.getObsUnitSetTChoice().getSchedBlockRefCount() > 0) {
			SchedBlockRefT[] setMember = set.getObsUnitSetTChoice().getSchedBlockRef();
			SB memberSB = null;
			for (int i = 0; i < setMember.length; ++i) {
                if(program.memberExists(setMember[i].getEntityId())){
                    //System.out.println("updating sb");
                    SB currentSB = (SB)program.getMember(setMember[i].getEntityId());
				    memberSB = updateSchedBlock(setMember[i],sched,schedUsed,project,program,now,currentSB);
				    program.updateMember(memberSB);
                } else {
                    //System.out.println("adding sb");
				    memberSB = initialize(setMember[i],sched,schedUsed,project,program,now);
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
            //System.out.println("Setting up new sb in update");
    		sb = new SB (schedRef.getEntityId());

            if(existingSB.getSbStatusId() == null) {
    		    sb.setSbStatusId(null); // This comes from ProjectStatus.
            } else { 
    		    sb.setSbStatusId(existingSB.getSbStatusId()); // This comes from ProjectStatus.
            }
	    	sb.setProject(project);
		    sb.setTimeOfCreation(now);
    		sb.setTimeOfUpdate(now);
        //System.out.println("SB parent part id (in sb initialize PU)"+parent.getObsUnitSetStatusId());
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
			sb.setMaximumNumberOfRepeats(0);
		} else {
			TimeT tt = ctrl.getSBMaximumTime();
			if (tt == null) {
				sb.setMaximumTimeInSeconds(1800); 
			} else {
				double maxTime = tt.getContent();
				sb.setMaximumTimeInSeconds((int)(maxTime * 60 + 0.05));
			}
			int repeatcount = ctrl.getRepeatCount();
			if (repeatcount < 1)
				throw new SchedulingException("Invalid repeat count (" + repeatcount + ").");
            //set to -1 because the first run is the original one. Not considered a 'repeat'
			//sb.setMaximumNumberOfRepeats(repeatcount - 1); 
            //TODO Changed this to not have -1, coz scheduler doesn't think this way yet.
			sb.setMaximumNumberOfRepeats(repeatcount); 

		}
		
        //////////////////////////////////////////////////////////
        //
        // Optical Camera Target stuff
        // IMPORTANT NOTE: see important notes for ObsTarget stuff below
        //
        //////////////////////////////////////////////////////////
        OpticalCameraTargetT[] opticalTargetList = sched.getOpticalCameraTarget();
        if(opticalTargetList.length > 0) {
            System.out.println("SCHEDULING: there are "+ opticalTargetList.length+" optical camera targets");
            SpectralSpecT setup = opticalTargetList[0].getTargetTChoice().getSpectralSpec();

            ArrayList eqList = new ArrayList ();
		    for (int i = 0; i < opticalTargetList.length; ++i) {
	    		FieldSourceT fieldSource = opticalTargetList[i].getFieldSource();
    			if (fieldSource == null)
			    	throw new SchedulingException("There is no FieldSourceT object in the scheduling block with id " + 
		    				sb.getSchedBlockId());
	    		SkyCoordinatesT coord = fieldSource.getSourceCoordinates();
    			if (coord == null)
			    	throw new SchedulingException("There is no SkyCoordinatesT object in the scheduling block with id " + 
		    				sb.getSchedBlockId());
	    		LongitudeT lng = coord.getLongitude(); 	// in degrees
    			double ra = lng.getContent();
			    LatitudeT lat = coord.getLatitude();	// in degrees
		    	double dec = lat.getContent();
	    		String coordType = coord.getSystem().toString(); // must be J2000
    			if (!coordType.equals("J2000"))
			    	throw new SchedulingException(coordType + " is not supported.  Must be J2000");
		    	Equatorial eq = new Equatorial((ra /24.0),dec);
	    		eqList.add(eq);
    		}
		    Equatorial[] eqArray = new Equatorial [eqList.size()];
	    	eqArray = (Equatorial[])eqList.toArray(eqArray);
    		if (eqArray.length == 1) {
		    	Target target = new Target (eqArray[0],3600.0,3600.0);
	    		sb.setTarget(target);
    		} else {
		    	Target target = new Target (eqArray);
	    		sb.setTarget(target);
    		}

        } else {
            System.out.println("SCHEDULING: No optical camera targets");
        }
        
        //////////////////////////////////////////////////////////
        //
        // ObsTarget stuff
        //
        //////////////////////////////////////////////////////////
        
		// Set the frequency and frequency band.
		// IMPORTANT NOTE!
		// We are using the rest frequency and receiver band from the first member of
		// the frequency setup that is in the Obstarget list.  This probably isn't the
		// right way to do it.
		ObsTargetT[] targetList = sched.getObsTarget();
        if(targetList.length > 0) {
            SpectralSpecT setup = targetList[0].getTargetTChoice().getSpectralSpec();
		    alma.entity.xmlbinding.schedblock.FrequencySetupT freqSetup = setup.getFrequencySetup();
	    	if (freqSetup == null) {
    			sb.setCenterFrequency(0.0);
			    sb.setFrequencyBand(null);
		    } else {
	    		sb.setCenterFrequency(freqSetup.getRestFrequency().getContent());
    			String band = freqSetup.getReceiverBand().toString();
			    FrequencyBand freq = new FrequencyBand(band,50.0,150.0); // These reanges are merely place-holders.
		    	sb.setFrequencyBand(freq);
	    	}
		
		// Set the target
		// IMPORTANT NOTE!
		// Targets are a problem.
		// We are going to take the list of ObsTargets and construct an IRREGULAR shape,
		// which is really a rectangular area that includes all targets.  If there is only
		// one target, we will add a one-degree rectangle around it.
    		ArrayList eqList = new ArrayList ();
		    for (int i = 0; i < targetList.length; ++i) {
	    		FieldSourceT fieldSource = targetList[i].getFieldSource();
    			if (fieldSource == null)
			    	throw new SchedulingException("There is no FieldSourceT object in the scheduling block with id " + 
		    				sb.getSchedBlockId());
	    		SkyCoordinatesT coord = fieldSource.getSourceCoordinates();
    			if (coord == null)
			    	throw new SchedulingException("There is no SkyCoordinatesT object in the scheduling block with id " + 
		    				sb.getSchedBlockId());
	    		LongitudeT lng = coord.getLongitude(); 	// in degrees
    			double ra = lng.getContent();
			    LatitudeT lat = coord.getLatitude();	// in degrees
		    	double dec = lat.getContent();
	    		String coordType = coord.getSystem().toString(); // must be J2000
    			if (!coordType.equals("J2000"))
			    	throw new SchedulingException(coordType + " is not supported.  Must be J2000");
		    	Equatorial eq = new Equatorial((ra /24.0),dec);
	    		eqList.add(eq);
    		}
		    Equatorial[] eqArray = new Equatorial [eqList.size()];
	    	eqArray = (Equatorial[])eqList.toArray(eqArray);
    		if (eqArray.length == 1) {
		    	Target target = new Target (eqArray[0],3600.0,3600.0);
	    		sb.setTarget(target);
    		} else {
		    	Target target = new Target (eqArray);
	    		sb.setTarget(target);
    		}
        } else {
            System.out.println("SCHEDULING: No obs targets");
        }
				
		// Return the newly create SB.
		return sb;
	}
   */ 
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
		entity.setDocumentVersion("1");
		entity.setSchemaVersion("1");
        //create a part Id ?
		pstatus.setProjectStatusEntity(entity);
		
		// Create the project reference and add it to the status.
		ObsProjectRefT obsProjectRef = new ObsProjectRefT ();
		obsProjectRef.setEntityId(project.getObsProjectId());
        //check for a part id in ObsProject
		obsProjectRef.setPartId(nullPartId);
		obsProjectRef.setDocumentVersion("1");
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
		pstatus.setStatus(assignState(project.getStatus()));
		pstatus.setBreakpointTime(project.getBreakpointTime() == null ? "" : project.getBreakpointTime().toString());
		// The obsProgram status.
		pstatus.setObsProgramStatus(assignObsProgramStatus(project.getProgram(),now));
		
		// Return the newly created project status.
		return pstatus;
		
	}

    /**
      * Updates the project status.
      *
      */
    static public ProjectStatus updateProjectStatus(Project p) throws SchedulingException{
        try {
            return ProjectUtil.map(p, new DateTime(System.currentTimeMillis()));
        } catch(Exception e){
            System.out.println("SCHEDULING: Error updating ProjectStatus.");
            e.printStackTrace(System.out);
            return null;
        }
    }
	
	//////////////////////////////////////////////////////////////
	// The following private methods are used to support the 	//
	// "map(Project project, DateTime now)" method.				//
	//////////////////////////////////////////////////////////////
	
	static private StatusT assignState(Status source) {
		StatusT target = new StatusT ();
		switch (source.getStatusAsInt()) {
			case Status.NOTDEFINED: target.setState(StatusTStateType.NOTDEFINED); break;
			case Status.WAITING: target.setState(StatusTStateType.WAITING); break;
			case Status.READY: target.setState(StatusTStateType.READY); break;
			case Status.RUNNING: target.setState(StatusTStateType.RUNNING); break;
			case Status.ABORTED: target.setState(StatusTStateType.ABORTED); break;
			case Status.COMPLETE: target.setState(StatusTStateType.COMPLETE); break;
		}
		target.setReadyTime(source.getReadyTime() == null ? "" : source.getReadyTime().toString());
		target.setStartTime(source.getStartTime() == null ? "" : source.getStartTime().toString());
		target.setEndTime(source.getEndTime() == null ? "" : source.getEndTime().toString());
		return target;
	}
	
	static private ObsUnitSetStatusT assignObsProgramStatus(Program source, DateTime now) {
        //System.out.println("Assigning obs program status");
		ObsUnitSetStatusT target = new ObsUnitSetStatusT ();
		target.setEntityPartId(source.getId());
		ObsProjectRefT obsUnitSetRef = new ObsProjectRefT ();
		obsUnitSetRef.setEntityId(source.getProject().getObsProjectId());
		obsUnitSetRef.setPartId(source.getProgramId());
		obsUnitSetRef.setDocumentVersion("1");
		target.setObsUnitSetRef(obsUnitSetRef);
		target.setTimeOfUpdate(now.toString());
		
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
		target.setSession(assignSession(source.getAllSession()));
		
		// Set the PipelineProcessingRequest.
		target.setPipelineProcessingRequest(assignPPR(source.getSciPipelineRequest()));
		
		// Set the members of this program.
		ProgramMember[] member = source.getMember();
		Program pgm = null;
		SB sb = null;
		ObsUnitSetStatusTChoice set = new ObsUnitSetStatusTChoice ();
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
		target.setObsUnitSetStatusTChoice(set);
		return target;
	}
	
    /*
	static private ObsUnitSetStatusT updateObsProgramStatus(Program source, 
            ObsUnitSetStatusT currentOUS, DateTime now) {
        
        if(!source.getProgramId().equals(currentOUS.getEntityPartId())) {
            //problem, updating wrong one.
            //TODO: throw error.. or something
            System.out.println("Program ID does not match what you're updating.");
            return null;
        }
        if(!source.getProject().getObsProjectId().equals(currentOUS.getObsUnitSetRef().getEntityId())){
            System.out.println("Project ID does not match what you're updating.");
            return null;
        }
        if(!source.getProgramId().equals(currentOUS.getObsUnitSetRef().getPartId())){
            System.out.println("Project ID does not match what you're updating.");
            return null;
        }
		currentOUS.setTimeOfUpdate(now.toString());
		
		// Set the state of this program.
		currentOUS.setStatus(assignState(source.getStatus()));
		
		// Set the totals.
		currentOUS.setTotalRequiredTimeInSec(source.getTotalRequiredTimeInSeconds());
		currentOUS.setTotalUsedTimeInSec(source.getTotalUsedTimeInSeconds());
		currentOUS.setTotalObsUnitSets(source.getTotalPrograms());
		currentOUS.setNumberObsUnitSetsCompleted(source.getNumberProgramsCompleted());
		currentOUS.setNumberObsUnitSetsFailed(source.getNumberProgramsFailed());
		currentOUS.setTotalSBs(source.getTotalSBs());
		currentOUS.setNumberSBsCompleted(source.getNumberSBsCompleted());
		currentOUS.setNumberSBsFailed(source.getNumberSBsFailed());
		
		// Set the session list.
		currentOUS.setSession(assignSession(source.getAllSession()));
		//currentOUS.setSession(updateSession(source.getAllSession()));
		
		// Set the PipelineProcessingRequest.
		currentOUS.setPipelineProcessingRequest(assignPPR(source.getSciPipelineRequest()));
		//currentOUS.setPipelineProcessingRequest(updatePPR(source.getSciPipelineRequest()));
		
		// Set the members of this program.
		ProgramMember[] member = source.getMember();
		Program pgm = null;
		SB sb = null;
		ObsUnitSetStatusTChoice currentSet = currentOUS.getObsUnitSetStatusTChoice();
        ObsUnitSetStatusT ousStatus = null;
		if (member[0] instanceof Program) {
			for (int i = 0; i < member.length; ++i) {
				pgm = (Program)member[i];
                //if the program exists then isthere is its index in the array, else its -1
                int isthere = obsUnitSetStatusExists(currentSet, pgm.getProgramId());
                if(isthere != -1 ){
                    //can't just add it again..we'll get 2, replace the old one with the new one
                    System.out.println("Replace obs unit set status in update");
				    currentSet.setObsUnitSetStatus(isthere,updateObsProgramStatus(pgm,now));
                } else {
                    System.out.println("adding obs unit set status in update");
				    currentSet.addObsUnitSetStatus(assignObsProgramStatus(pgm,now));
                }
			}			
		} else {
			for (int i = 0; i < member.length; ++i) {
				sb = (SB)member[i];
                //if the sb exists then isthere is its index in the array, else its -1
                int isthere=sbStatusExists(currentSet, sb.getId());
                if(isthere != -1) {
                    //can't just add it again..we'll get 2, replace old with new
                    System.out.println("Replace sbstatus in update");
				    //currentSet.setSBStatus(isthere, updateSbStatus(sb,now));
                } else {
                    System.out.println("adding sbstatus in update");
				    set.addSBStatus(assignSBStatus(sb,now));
                }
			}
		}
		currentOUS.setObsUnitSetStatusTChoice(set);
		return currentOUS;
    }
    */

	static private SessionT[] assignSession(ObservedSession[] session) {
		SessionT[] list = new SessionT [session.length];
		SessionT x = null; 
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
			ProjectStatusRefT pRef = new ProjectStatusRefT ();
			pRef.setEntityId(session[i].getProgram().getProject().getProjectStatusId());
			pRef.setDocumentVersion("1");
			pRef.setPartId(session[i].getSessionId());
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
		return list;
	}
	
	static private PipelineProcessingRequestT assignPPR(SciPipelineRequest ppr) {
        //System.out.println("Assign PPR!");
		if (ppr == null){
            //System.out.println("ppr == null");
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
            System.out.println("Params = "+parm.length);
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
                    System.out.println("Class type for data processing param is: "+parm[i].getClass().getName());
                }
            }
    		target.setPipelineParameter(pparams);
        } else {
            System.out.println("Params = null!");
        }
		// OK, we're done.
		return target;
	}

		
	static private SBStatusT assignSBStatus(SB sb, DateTime now) {
		SBStatusT target = new SBStatusT ();
		
		// Set the status part-id.
        //System.out.println("SB status id being set = "+sb.getSbStatusId());
		target.setEntityPartId(sb.getSbStatusId());
		
		// Set the reference to the SchedBlock.
		SchedBlockRefT sbRef = null;
		sbRef = new SchedBlockRefT ();
		sbRef.setEntityId(sb.getSchedBlockId());
		sbRef.setPartId(nullPartId);
		sbRef.setDocumentVersion("1");
		target.setSchedBlockRef(sbRef);
		
		// set remaining variables.
		target.setTimeOfUpdate(now.toString());
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
			exStatus.setTimeOfCreation(ex[i].getTimeOfCreation().toString());
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
			sbRef.setDocumentVersion("1");
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

}
