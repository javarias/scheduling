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
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 * File ProjectStatusDAO.java
 */
package alma.scheduling.AlmaScheduling;

import alma.entity.xmlbinding.projectstatus.*;
import alma.entity.xmlbinding.projectstatus.types.*;
import alma.entity.xmlbinding.session.*;
import alma.entity.xmlbinding.obsproject.*;
import alma.entity.xmlbinding.schedblock.*;
import alma.entity.xmlbinding.valuetypes.*;
import alma.entities.commonentity.EntityT;
import alma.entities.commonentity.EntityRefT;

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
 * @version 1.00 Sep 13, 2004
 * @author Allen Farris
 */
public class ProjectUtil {
	
	static private final String nullPartId = "X00000000";

	/**
	 * Create a Session object from an ObservedSession object.  This method assumes that the
	 * ObservedSession object is a part of a valid Project.
	 * @param source The specified ObservedSession object from which the Session object is derived.
	 * @return The new created Session object.
	 */
	static public Session createSession(ObservedSession source) {
		Session target = new Session ();

		// Create the entityId and add it to the session.
		EntityT entity = new EntityT ();
		entity.setEntityId(source.getSessionId());
		entity.setEntityIdEncrypted("none");
		entity.setDocumentVersion("1");
		entity.setEntityTypeName("Session");
		entity.setSchemaVersion("1");
		target.setSessionEntity(entity);
		
		// Set the ObsUnitSetStatusId.
		EntityRefT obsUnitSetStatusRef = new EntityRefT ();
		obsUnitSetStatusRef.setEntityId(source.getProgram().getProject().getProjectStatusId());
		obsUnitSetStatusRef.setPartId(source.getProgram().getObsUnitSetStatusId());
		obsUnitSetStatusRef.setEntityTypeName("ProjectStatus");
		obsUnitSetStatusRef.setDocumentVersion("1");
		target.setObsUnitsetReference(obsUnitSetStatusRef);
		
		// Set the times.
		target.setStartTime(source.getStartTime().toString());
		target.setEndTime(source.getEndTime().toString());
		
		// Add the execution blocks.
		ExecBlock[] x = source.getExec();
		SessionSequence seq = null;
		SessionSequenceItem item = null;
		ExecutionT ex = null;
		EntityRefT exRef = null;
		for (int i = 0; i < x.length; ++i) {
			seq = new SessionSequence();
			item = new SessionSequenceItem ();
			ex = new ExecutionT ();
			exRef = new EntityRefT ();
			exRef.setEntityId(x[i].getExecId());
			exRef.setPartId(nullPartId);
			exRef.setEntityTypeName("ProjectStatus");
			exRef.setDocumentVersion("1");
			ex.setExecBlockReference(exRef);
			item.setExecution(ex);
			seq.setSessionSequenceItem(item);
			target.addSessionSequence(seq);
		}
		return target;
	}
	
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
		validate(project.getProgram(),project,null);
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
			if (s.charAt(i) < '0' || s.charAt(i) > '9')
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
			if (s.charAt(i) < '0' || s.charAt(i) > '9')
				throw new SchedulingException ("Invalid format for EntityId (" + s + ")");
		}
		for (int i = 25; i < 33; ++i) {
			if (s.charAt(i) < '0' || s.charAt(i) > '9')
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
		String timeOfUpdate = s.getTimeOfUpdate();
		if (timeOfUpdate == null || timeOfUpdate.length() == 0) {
			// If the project status is a dummy, then we're done.
			validate(project);
			return project;
		}
		
		// Now, we apply the ProjectStatus data.
		update(project,s,now);
		validate(project);
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
			String projectStatusEntityId, DateTime now) throws SchedulingException {
		Project project = new Project (obs.getObsProjectEntity().getEntityId(),
									   obs.getObsProposalRef().getEntityId(),
									   obs.getProjectName(),
									   obs.getVersion(),
									   obs.getPI());
		project.setProjectStatusId(projectStatusEntityId);
		project.setTimeOfCreation(now);
		project.setTimeOfUpdate(now);
		project.setBreakpoint(null);
		
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
		
		// Mark the project as ready.  (This also initializes the totals.)
		project.setReady(now);
		
		// Now, validate the project
		validate(project);
		
		return project;
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
	static private Program initialize(ObsUnitSetT set, SchedBlock[] sched, boolean[] schedUsed, Project project, Program parent,  
		DateTime now) throws SchedulingException {
		
		Program program = new Program (set.getEntityPartId());
		program.setProject(project);
		program.setObsUnitSetStatusId(null); // We get this from the ProjectStatus.
		program.setTimeOfCreation(now);
		program.setTimeOfUpdate(now);
		program.setParent(parent);
		// We get SciPipelineRequest and Sessions from the ProjectStatus. 
		program.setScientificPriority(Priority.MEDIUM); // Where is this in ObsPrep?
		program.setUserPriority(Priority.MEDIUM);
		program.setDataReductionProcedureName(set.getScienceProcessingScript());
		program.setDataReductonParameters(null);
		program.setFlowControl(null);
		program.setNotify(null);
		program.setScienceGoal(null);
		program.setWeatherConstraint(null);
		program.setCenterFrequency(0.0);
		program.setFrequencyBand(null);
		program.setRequiredInitialSetup(null);
	
		// Assign the members of this set: either Program or SB objects.
		if (set.getObsUnitSetTChoice().getObsUnitSetCount() > 0) {
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
		SB sb = new SB (schedRef.getEntityId());

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
			sb.setMaximumNumberOfRepeats(repeatcount - 1); 
		}
		
		// Set the frequency and frequency band.
		// IMPORTANT NOTE!
		// We are using the rest frequency and receiver band from the first member of
		// the frequency setup that is in the Obstarget list.  This probably isn't the
		// right way to do it.
		ObsTargetT[] targetList = sched.getObsTarget();
		if (targetList == null || targetList.length == 0)
			throw new SchedulingException("There is no ObsTargetT object in the scheduling block with id " + 
					sb.getSchedBlockId());
		if (targetList[0].getSpectralSpecCount() < 1)
			throw new SchedulingException("There is no SpectralSpec object in the scheduling block with id " + 
					sb.getSchedBlockId());
		SpectralSpecT setup = targetList[0].getSpectralSpec(0);
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
			FieldSpecT fieldSpec = targetList[i].getFieldSpec();
			if (fieldSpec == null)
				throw new SchedulingException("There is no FieldSpecT object in the scheduling block with id " + 
						sb.getSchedBlockId());
			FieldSourceT fieldSource = fieldSpec.getFieldSource();
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
				
		// Return the newly create SB.
		return sb;
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
		throw new SchedulingException("The ProjectUtil.update(Project project, ProjectStatus status, DateTime now) method is not implemented at the present time.");
		// TODO We will implement this after R2.  For R2, all projects we execute will be created from
		//	scratch and will be initialized.  For this we won't need a previously created "real" ProjectStatus.
	}
	
	//////////////////////////////////////////////////////////////////////
	// End of private methods are used to support the		 			//
	// "map(ObsProject p, SchedBlock[] b, ProjectStatus s)" method.		//
	//////////////////////////////////////////////////////////////////////
	
	/**
	 * Create a ProjectStructure object that acurately reflects the current status of the specified Project.
	 * @param project The Project used as a basis for creating the ProjectStructure
	 * @param now The current time.
	 * @return A ProjectStructure object that acurately reflects the current status of the specified Project.
	 * @throws SchedulingException If any error is found in the process of mapping.
	 */
	static public ProjectStatus map(Project project, DateTime now) throws SchedulingException {
		
		// First, we validate the project.
		validate(project);
		
		// Create a new project status.
		ProjectStatus status = new ProjectStatus();
		
		// Create the entityId and add it to the new status.
		EntityT entity = new EntityT ();
		entity.setEntityId(project.getProjectStatusId());
		entity.setEntityIdEncrypted("none");
		entity.setDocumentVersion("1");
		entity.setEntityTypeName("ProjectStatus");
		entity.setSchemaVersion("1");
		status.setProjectStatusEntity(entity);
		
		// Create the project reference and add it to the status.
		EntityRefT obsProjectRef = new EntityRefT ();
		obsProjectRef.setEntityId(project.getObsProjectId());
		obsProjectRef.setPartId(nullPartId);
		obsProjectRef.setEntityTypeName("ObsProject");
		obsProjectRef.setDocumentVersion("1");
		status.setObsProjectRef(obsProjectRef);
		
		// Create the proposal reference and add it to the status.
		EntityRefT obsProposalRef = new EntityRefT ();
		obsProposalRef.setEntityId(project.getProposalId());
		obsProposalRef.setPartId(nullPartId);
		obsProposalRef.setEntityTypeName("ObsProposal");
		obsProposalRef.setDocumentVersion("1");
		status.setObsProposalRef(obsProposalRef);
		
		// Fill in the remaining values from the project.
		status.setName(project.getProjectName());
		status.setPI(project.getPI());
		status.setTimeOfUpdate(now.toString());
		// The state of the project.
		StatusT state = new StatusT ();
		assignState(project.getStatus(),state);
		status.setStatus(state);
		status.setBreakpointTime(project.getBreakpointTime() == null ? "" : project.getBreakpointTime().toString());
		// The obsProgram status.
		ObsUnitSetStatusT obsProgramStatus = new ObsUnitSetStatusT ();
		assignObsProgramStatus(project.getProgram(),obsProgramStatus,now);
		status.setObsProgramStatus(obsProgramStatus);
		
		// Return the newly created project status.
		return status;
		
	}
	
	//////////////////////////////////////////////////////////////
	// The following private methods are used to support the 	//
	// "map(Project project, DateTime now)" method.				//
	//////////////////////////////////////////////////////////////
	
	static private void assignState(Status source, StatusT target) {
		switch (source.getStatusAsInt()) {
			case Status.NOTDEFINED: target.setState(StateType.NOTDEFINED); break;
			case Status.WAITING: target.setState(StateType.WAITING); break;
			case Status.READY: target.setState(StateType.READY); break;
			case Status.RUNNING: target.setState(StateType.RUNNING); break;
			case Status.ABORTED: target.setState(StateType.ABORTED); break;
			case Status.COMPLETE: target.setState(StateType.COMPLETE); break;
		}
		target.setReadyTime(source.getReadyTime() == null ? "" : source.getReadyTime().toString());
		target.setStartTime(source.getStartTime() == null ? "" : source.getStartTime().toString());
		target.setEndTime(source.getEndTime() == null ? "" : source.getEndTime().toString());
	}
	
	static private void assignObsProgramStatus(Program source, ObsUnitSetStatusT target, DateTime now) {
		target.setEntityPartId(source.getObsUnitSetStatusId());
		EntityRefT obsUnitSetRef = new EntityRefT ();
		obsUnitSetRef.setEntityId(source.getProject().getObsProjectId());
		obsUnitSetRef.setPartId(source.getProgramId());
		obsUnitSetRef.setEntityTypeName("ObsUnitSet");
		obsUnitSetRef.setDocumentVersion("1");
		target.setObsUnitSetRef(obsUnitSetRef);
		target.setTimeOfUpdate(now.toString());
		
		// Set the state of this program.
		StatusT state = new StatusT ();
		assignState(source.getStatus(),state);
		target.setStatus(state);
		
		// Set the totals.
		target.setTotalRequiredTimeInSec(source.getTotalRequiredTimeInSeconds());
		target.setTotalUsedTimeInSec(source.getTotalUsedTimeInSeconds());
		target.setTotalObsUnitSets(source.getTotalPrograms());
		target.setNumberObsUnitSetsCompleted(source.getNumberProgramsCompleted());
		target.setNumberObsUnitSetsFailed(source.getNumberProgramsFailed());
		target.setTotalSBs(source.getTotalSBs());
		target.setNumberSBSCompleted(source.getNumberSBsCompleted());
		target.setNumberSBsFailed(source.getNumberSBsFailed());
		
		// Set the session list.
		ObsUnitSetStatusTSequence sessionList = new ObsUnitSetStatusTSequence ();
		assignSession(source.getAllSession(),sessionList);
		target.setObsUnitSetStatusTSequence(sessionList);
		
		// Set the PipelineProcessingRequest.
		EntityRefT pipelineProcessingRequestRef = new EntityRefT ();
		if (source.getSciPipelineRequest() == null)
			pipelineProcessingRequestRef.setEntityId("");
		else
			pipelineProcessingRequestRef.setEntityId(source.getSciPipelineRequest().getId());
		pipelineProcessingRequestRef.setPartId(nullPartId);
		pipelineProcessingRequestRef.setEntityTypeName("PipelineReq");
		pipelineProcessingRequestRef.setDocumentVersion("1");
		target.setPipelineProcessingRequestRef(pipelineProcessingRequestRef);
		
		// Set the members of this program.
		ProgramMember[] member = source.getMember();
		Program pgm = null;
		SB sb = null;
		ObsUnitSetStatusT obsProgramStatus = null;
		SBStatusT sbStatus = null;
		ObsUnitSetStatusTChoice set = new ObsUnitSetStatusTChoice ();
		if (member[0] instanceof Program) {
			for (int i = 0; i < member.length; ++i) {
				pgm = (Program)member[i];
				obsProgramStatus = new ObsUnitSetStatusT ();
				assignObsProgramStatus(pgm,obsProgramStatus,now);
				set.addObsUnitSet(obsProgramStatus);
			}			
		} else {
			for (int i = 0; i < member.length; ++i) {
				sb = (SB)member[i];
				sbStatus = new SBStatusT ();
				assignSBStatus(sb,sbStatus,now);
				set.addSB(sbStatus);
			}
		}
		target.setObsUnitSetStatusTChoice(set);
	}
	
	static private void assignSession(ObservedSession[] session, ObsUnitSetStatusTSequence list) {
		EntityRefT sessionRef = null;
		for (int i = 0; i < session.length; ++i) {
			sessionRef = new EntityRefT ();
			sessionRef.setEntityId(session[i].getSessionId());
			sessionRef.setPartId(nullPartId);
			sessionRef.setEntityTypeName("Session");
			sessionRef.setDocumentVersion("1");
			list.addSessionRef(sessionRef);
		}
	}
	
	static private void assignSBStatus(SB sb, SBStatusT target, DateTime now) {
		// Set the status part-id.
		target.setEntityPartId(sb.getSbStatusId());
		
		// Set the reference to the SchedBlock.
		EntityRefT sbRef = null;
		sbRef = new EntityRefT ();
		sbRef.setEntityId(sb.getSchedBlockId());
		sbRef.setPartId(nullPartId);
		sbRef.setEntityTypeName("SchedBlock");
		sbRef.setDocumentVersion("1");
		target.setSBRef(sbRef);
		
		// set remaining variables.
		target.setTimeOfUpdate(now.toString());
		StatusT state = new StatusT ();
		assignState(sb.getStatus(),state);
		target.setStatus(state);
		target.setTotalRequiredTimeInSec(sb.getTotalRequiredTimeInSeconds());
		target.setTotalUsedTimeInSec(sb.getTotalUsedTimeInSeconds());

		// Set the ExecBlock list.
		SBStatusTSequence xList = new SBStatusTSequence ();
		assignExec(sb.getExec(),xList,now);
		target.setSBStatusTSequence(xList);
	}
	
	static private void assignExec(ExecBlock[] ex, SBStatusTSequence list, DateTime now) {
		ExecStatusT exStatus = null;
		EntityRefT execRef = null;
		StatusT state = null;
		BestSBT bestSB = null;
		for (int i = 0; i < ex.length; ++i) {
			// Set entity part-id.
			exStatus = new ExecStatusT ();
			// Set execblock reference.
			exStatus.setEntityPartId(ex[i].getExecStatusId());
			execRef = new EntityRefT ();
			execRef.setEntityId(ex[i].getExecId());
			execRef.setPartId(nullPartId);
			execRef.setEntityTypeName("ExecBlock");
			execRef.setDocumentVersion("1");
			exStatus.setExecBlockRef(execRef);
			// Set times.
			exStatus.setTimeOfCreation(ex[i].getTimeOfCreation().toString());
			exStatus.setTimeOfUpdate(now.toString());
			// Set state.
			state = new StatusT ();
			assignState(ex[i].getStatus(),state);
			exStatus.setStatus(state);
			// Set subarray-id.
			exStatus.setSubarrayId(ex[i].getSubarrayId());
			// Set BestSB.
			// There is only one BestSB, not a sequence.  Mistake in model! <---
			bestSB = new BestSBT ();
			assignBestSB(ex[i].getBest(),bestSB);
			ExecStatusTSequence seq = new ExecStatusTSequence ();
			seq.addBestSB(bestSB);
			exStatus.setExecStatusTSequence(seq);
			list.addExecStatus(exStatus);
		}
	}
	
	static private void assignBestSB(BestSB best, BestSBT target) {
		// Set the number of units.
		target.setNumberUnits(best.getNumberReturned());
		
		// Set the array of SB-ids.
		BestSBTSequence seq1 = new BestSBTSequence (); 
		String[] s = best.getSbId();
		EntityRefT sbRef = null;
		for (int i = 0; i < s.length; ++i) {
			sbRef = new EntityRefT ();
			sbRef.setEntityId(s[i]);
			sbRef.setPartId(nullPartId);
			sbRef.setEntityTypeName("SchedBlock");
			sbRef.setDocumentVersion("1");
			seq1.addSBId(sbRef);
		}
		target.setBestSBTSequence(seq1);
		
		// Set the array of score strings.
		BestSBTSequence2 seq2 = new BestSBTSequence2 ();
		s = best.getScoreString();
		for (int i = 0; i < s.length; ++i) {
			seq2.addScore(s[i]);
		}
		target.setBestSBTSequence2(seq2);
		
		// Set the array of success strings.
		BestSBTSequence3 seq3 = new BestSBTSequence3 ();
		double[] d = best.getSuccess(); 
		for (int i = 0; i < d.length; ++i) {
			seq3.addSuccess(Double.toString(d[i]));
		}
		target.setBestSBTSequence3(seq3);
		
		// Set the array of rank strings.
		BestSBTSequence4 seq4 = new BestSBTSequence4 ();
		d = best.getRank(); 
		for (int i = 0; i < d.length; ++i) {
			seq4.addRank(Double.toString(d[i]));
		}
		target.setBestSBTSequence4(seq4);
		
		// Set selection and time of selection.
		target.setSelection(best.getSelection());
		target.setTimeOfSelection(best.getTime().toString());
	}
	
	//////////////////////////////////////////////////////////////
	// End of private methods are used to support the		 	//
	// "map(Project project, DateTime now)" method.				//
	//////////////////////////////////////////////////////////////
	
}
