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
 * File ProjectUtil.java
 */
package alma.scheduling.AlmaScheduling;

import alma.entity.xmlbinding.projectstatus.*;
import alma.entity.xmlbinding.projectstatus.types.*;
import alma.entity.xmlbinding.session.*;
import alma.entity.xmlbinding.obsproject.*;
import alma.entity.xmlbinding.schedblock.*;
import alma.entities.commonentity.EntityT;
import alma.entities.commonentity.EntityRefT;

import alma.scheduling.Define.SchedulingException;
import alma.scheduling.Define.Project;
import alma.scheduling.Define.Program;
import alma.scheduling.Define.ProgramMember;
import alma.scheduling.Define.ObservedSession;
import alma.scheduling.Define.SB;
import alma.scheduling.Define.ExecBlock;
import alma.scheduling.Define.BestSB;
import alma.scheduling.Define.Status;
import alma.scheduling.Define.DateTime;

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
 * <li>Public Methods
 * 		<ul>
 * 		<li> <em>Project map(ObsProject p, SchedBlock[] b, ProjectStaus s) throws SchedulingException</em> --
 * 			 Create a Project from an ObsProject, the scheduling blocks that belong 
 * 			 to it and its current status.  This method divides into two cases 
 * 			 depending on whether or not the ProjectStatus is a "dummy".
 * 		<li> <em>ProjectStatus map(Project p) throws SchedulingException</em> -- 
 * 			 Create a ProjectStructure object that accurately reflects the current status of a project.
 * 		<li> <em>void validate(Project p) throws SchedulingException</em> -- Check a project for 
 * 			 internal consistency.
 * 		<li> <em>Session createSession(ObservedSession source)</em> -- Create a Session object 
 * 			 from an ObservedSession object.
 * 		</ul>  
 * </ul> 
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
	 * @param project The Project to be checked.
	 * @throws SchedulingException If any error is found in the specified project.
	 */
	static public void validate(Project project) throws SchedulingException {
		// General validations
		// 1. Certain strings must have values: 
		//		s != null && s.length > 0
		// 2. Times with values must pass a reasonableness test:
		//		2000:01:01 < t < 2050:01:01
		// 3. EntityPartIds must be of the form:
		//		<EntityPartId>X00000001</EntityPartId>
		// 4. EntityIds must be of the form:
		//		entityId="uid://X0000000000000079/X00000000"
		
		// Specific validation steps.
		// 1. Project: check for null values of fields and make sure times are appropriate.
		// 2. Get the obsProgram that belongs to the Project.
		// 3. Foreach Program:
		//		4. Check for null values of fields and make sure times are appropriate.
		//		5. Make sure links to project and parent are valid.
		//		6. Foreach session:
		//			7. Check for null values of fields and make sure times are appropriate.
		//			8. Make sure links to project and parent are valid.200
		//			9. There must be at least one member.
		//			10 Make sure that members are of the same type.
		//		11. Foreach member:
		//			12. Check for null values of fields and make sure times are appropriate.
		//			13. Make sure links to project and parent are valid.
		//			14. Make sure totals are consistent with those in the parent.
		//			15. This is recursive: 
		//				16. If member is Program check<EntityPartId>X00000001</EntityPartId> each member (3).
		//				17. If member if SB check each member (16).
		// 18. Foreach SB:
		//			19. Check for null values of fields and make sure times are appropriate.
		//			20. Make sure links to project and parent are valid.
		//			21. Make sure totals are consistent with those in the parent.
		//			22. Foreach ExecBlock:
		//				23. Check for null values of fields and make sure times are appropriate.
		//				24. Make sure links to project and parent are valid.
		// End validation.
		
		checkEntityId(project.getObsProjectId());
		checkEntityId(project.getProjectStatusId());
		checkEntityId(project.getProposalId());
		checkString("projectName",project.getProjectName());
		checkString("PI",project.getPI());
		checkTime(project.getTimeOfCreation());
		checkTime(project.getTimeOfUpdate());
		checkTime(project.getBreakpointTime());
		validate(project.getStatus());
		validate(project.getProgram());
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
	
	static private void validate(Status status) throws SchedulingException {
		// TODO
	}
	
	static private void validate(Program program) throws SchedulingException {
		// TODO
	}
	
	static private void validate(SB sb) throws SchedulingException {
		// TODO
	}
	
	static private void validate(ExecBlock exec) throws SchedulingException {
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
	static public Project map(ObsProject p, SchedBlock[] b, ProjectStatus s) throws SchedulingException {
		// We are also going to need all sessions and pipeline processing requests that belong to this project.
		// Session[] session;
		// PipelineProcessingRequest[] ppr;
		
		// Fitst, we get the project data as if it were a dummy.
		Project project = initialize(p,b,s.getProjectStatusEntity().getEntityId());
		
		// How do we detect if the ProjectStatus is a "dummy".
		// Answer: If the timeOfUpdate is null or of length 0.
		String timeOfUpdate = s.getTimeOfUpdate();
		if (timeOfUpdate == null || timeOfUpdate.length() == 0) {
			validate(project);
			return project;
		}
		// Now, we apply the ProjectStatus data.
		update(project,s);
		validate(project);
		return project;
	}

	//////////////////////////////////////////////////////////////////////
	// The following private methods are used to support the 			//
	// "map(ObsProject p, SchedBlock[] b, ProjectStatus s)" method.		//
	//////////////////////////////////////////////////////////////////////
	
	// Get the project data and initilize the project status.
	static private Project initialize(ObsProject p, SchedBlock[] b, String projectStatusEntityId) {
		//TODO
		return null;
	}
	
	// Update the project status data.
	static private void update(Project project, ProjectStatus status) {
		// TODO
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
