package alma.scheduling.planning_mode_sim.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

import org.springframework.context.ApplicationContext;

import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.PI;
import alma.scheduling.datamodel.executive.PIMembership;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.executive.dao.ExecutiveDaoImpl;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnit;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.SchedBlockState;
import alma.scheduling.datamodel.output.Affiliation;
import alma.scheduling.datamodel.output.Array;
import alma.scheduling.datamodel.output.ExecutionStatus;
import alma.scheduling.datamodel.output.ObservationProject;
import alma.scheduling.datamodel.output.Results;
import alma.scheduling.datamodel.output.SchedBlockResult;

/** 
 * Gathers notifications from the Simulator, and generates an output that <br>
 * includes the results from simulations, and can be further studied.<br> 
 * This class should be created only once per simulation.<br>
 * Used it to report relevant events, using the notifyXXXX family of methods.<br>
 * TODO: Implement notifications for ranker results. (Not considerate in original design)
 * 
 * @author ahoffsta
 *
 */
public class ResultComposer {
	
	private Results results;
	private ApplicationContext context = null;
	
	
	public ResultComposer(ApplicationContext ctx){
		results = new Results();
		results.setObsSeasonEnd(new Date());
		results.setObsSeasonStart(new Date());
		results.setStartSimDate(new Date());
		results.setStopSimDate(new Date());
		results.setArray( new HashSet<Array>() );
		results.setObservationProject( new HashSet<ObservationProject>() );
		
		context = ctx;
	}
	
	public void notifyArrayCreation(ArrayConfiguration arrcfg){
		Array arr = new Array();
		arr.setCreationDate( arrcfg.getStartTime() );
		arr.setDeletionDate( arrcfg.getEndTime() );
		// TODO Implement available time (deletionDate - CreationDate)
		arr.setId(arrcfg.getId());
		arr.setResolution( arrcfg.getResolution());
		arr.setUvCoverage(arrcfg.getUvCoverage());
		results.getArray().add(arr);
	}
	
//	public void notifyArrayDestruction(ArrayConfiguration arrcfg){
//		//TODO: Complete when observatory characteristics models is ready
//		Iterator<Array> it = results.getArray().iterator();
//		it.next().setDeletionDate( TimeUtil.now() );
//	}
	
	/**
	 * Notify to the ResultsComposer that a new schedblock has been send to
	 * CONTROL for execution. It store every data necesary for results about
	 * the SB, and its main characteristics.
	 * 
	 * TODO: Add param that indicates in which array was started its execution
	 * @param sb SchedulingBlock (class from datamodel) that needs to be informed of its execution.
	 */
	public void notifySchedBlockStart(SchedBlock sb){
		
		// TODO Implement distinction of types of SB. If maintenance, account to a ghost ObsProject.
		
		// Creation of SchedBlockResult
		SchedBlockResult sbr = new SchedBlockResult();
		Iterator<Array> it = results.getArray().iterator();
		sbr.setArrayRef( it.next() );
		sbr.setStartDate( TimeHandler.now() );
		sbr.setExecutionTime( sb.getObsUnitControl().getEstimatedExecutionTime() );
		sbr.setMode( "Single Dish" ); //TODO: Implement modes in data-model

		// State
		if( sb.getSchedBlockControl().getState() == SchedBlockState.FULLY_OBSERVED ){
			sbr.setStatus( ExecutionStatus.COMPLETE );
		}else{
			sbr.setStatus( ExecutionStatus.INCOMPLETE);
		}
		
		//TODO: Waiting for SB type implementation on data-model.
		sbr.setType( "SCIENTIFIC" ); 
		
		// Obtaining reference from the SchedBlock to the Observation Project.
		ObsProject inputObsProjectRef = null;
		ObsUnit refOu = sb;
		while ( inputObsProjectRef == null){
			// If the current obsunit has parent, asign and exit
			if( refOu.getProject() != null){
				inputObsProjectRef = refOu.getProject();
				continue;
			}
			// If the current obsunit doesn't have a project, jump to next parent 
			if( refOu.getProject() == null)
				refOu = refOu.getParent();
		}
		
		// Check if the containing Observation Project already exists in the results collection of obsprojects.
		boolean isPresent = false;
		ObservationProject outputObservationProjectRef = null;
		for( ObservationProject tmpOp : results.getObservationProject() ){
			if( tmpOp.getId() == inputObsProjectRef.getId() ){
				isPresent = true;
				outputObservationProjectRef = tmpOp;
				break;
			}
		}
		
		// If false, add the observation project.
		if( !isPresent ){
			// Create the Observation Project
			ObservationProject newObsProject = new ObservationProject();
			newObsProject.setAssignedPriority( inputObsProjectRef.getScienceRank() );
			newObsProject.setId( inputObsProjectRef.getId());
			newObsProject.setStatus( ExecutionStatus.INCOMPLETE );
			// Create the Affiliations Set and their Affiliations			
			newObsProject.setAffiliation(new HashSet<Affiliation>());
			Affiliation newAffiliation = new Affiliation();
			ExecutiveDAO execDao = (ExecutiveDAO) context.getBean("execDao");
			for( PI tmpPI : execDao.getAllPi() ){
				if( tmpPI.getName().compareTo( inputObsProjectRef.getPrincipalInvestigator() ) == 0 ){
					for( PIMembership tmpPIMembership : tmpPI.getPIMembership() ){
						newAffiliation.setPercentage( tmpPIMembership.getMembershipPercentage().intValue() );
						newAffiliation.setExecutive( tmpPIMembership.getExecutive().getName() );
						// Only the first PI Membership is considered, as currently the datamodel doesn't allow to know which PI Membership to use.
						break;
					}
					// And if we found the correct PI, we are finished searching it.
					break;
				}
			}
			// Create the SchedBlock Set.
			newObsProject.setSchedBlock(new HashSet<SchedBlockResult>());
			// Add the new ObservationProject to the Results
			results.getObservationProject().add( newObsProject );
			// Save the reference for further use
			outputObservationProjectRef = newObsProject;
		}
		
		// Add the SchedBlock to the corresponding ObservationProject
		outputObservationProjectRef.getSchedBlock().add(sbr);
		
		//TODO: Use the correct time spend on simulation
		TimeHandler.stepAhead( (int) sbr.getExecutionTime() );
		sbr.setEndDate( TimeHandler.now() );
	}
		
	/**
	 * Gathers data at the end of simulation to complete the output data.
	 */
	public void completeResults(){
		System.out.println("Completing results");
		
		for( ObservationProject op : results.getObservationProject()){
			long execTime = 0;
			for( SchedBlockResult sbr : op.getSchedBlock()){
				execTime += sbr.getExecutionTime();
			}
			op.setExecutionTime(execTime);
		}
		
		for( Array arr : results.getArray()){
			arr.setScientificTime( 0.0 );
			arr.setAvailableTime( 0.0 );
			arr.setMaintenanceTime( 0.0 );
		}
		
		//TODO: According to actual structure, SBs need to belongs to a ObsProject. What about maintenance SBs?
		for( ObservationProject op : results.getObservationProject()){
			System.out.println("\\-Completing observation project:" + op.getExecutionTime() );
			for( SchedBlockResult sbr : op.getSchedBlock()){
				if( sbr.getType() == "SCIENTIFIC")
					sbr.getArrayRef().setScientificTime( sbr.getArrayRef().getScientificTime() + sbr.getExecutionTime() );
				if( sbr.getType() == "MAINTENANCE")
					sbr.getArrayRef().setMaintenanceTime( sbr.getArrayRef().getMaintenanceTime() + sbr.getExecutionTime() );
			}
		}
		
		for( Array arr : results.getArray()){
			this.results.setScientificTime( arr.getScientificTime() +  this.results.getScientificTime() );
			this.results.setAvailableTime( arr.getAvailableTime() +  this.results.getAvailableTime() );
			this.results.setMaintenanceTime( arr.getMaintenanceTime() +  this.results.getMaintenanceTime() );
		}
				
	}
	
	/**
	 * Saves results into the database.
	 */
	public Results getResults(){
		return this.results;
		
	}


}
