package alma.scheduling.planning_mode_sim.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

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
		results.setArray( new HashSet<Array>() );
		results.setObservationProject( new HashSet<ObservationProject>() );
		
		context = ctx;
	}
	
	public void notifyExecutiveData(Date obsSeasonStart, Date obsSeasonEnd, Date simStart, Date simStop){
		results.setObsSeasonStart(obsSeasonStart);
		results.setObsSeasonEnd(obsSeasonEnd);
		results.setStartSimDate(obsSeasonStart);
		results.setStopSimDate(obsSeasonEnd);
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
	
	/**
	 * Notify to the ResultsComposer that a new schedblock has been send to
	 * CONTROL for execution. It store every data necesary for results about
	 * the SB, and its main characteristics.
	 * 
	 * TODO: Add param that indicates in which array was started its execution
	 * @param sb SchedulingBlock (class from datamodel) that needs to be informed of its execution.
	 */
	@Transactional
	public void notifySchedBlockStart(SchedBlock sb){
		
		// TODO Implement distinction of types of SB. If maintenance, account to a ghost ObsProject.
		
		// Creation of SchedBlockResult
		SchedBlockResult sbr = new SchedBlockResult();
		Iterator<Array> it = results.getArray().iterator();
		sbr.setArrayRef( it.next() );
		sbr.setStartDate( TimeHandler.now() );
		sbr.setExecutionTime( sb.getObsUnitControl().getEstimatedExecutionTime() );
		sbr.setStatus( ExecutionStatus.INCOMPLETE );
		//TODO: Implement modes in data-model
		// In the XSD, there is a spec: InstrumentSpecT, and ObservingMode
		sbr.setMode( "Single Dish" ); 
		//TODO: Waiting for SB type implementation on data-model.
		sbr.setType( "SCIENTIFIC" ); 
		
		// Obtaining reference from the SchedBlock to the Observation Project.
		ObsProject inputObsProjectRef = sb.getProject();
				
		// Check if the Observation Project already exists in the results collection of obsprojects.
		// TODO: add to the datamodel an attribute that saves the original obsproject ID
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
			newObsProject.setScienceRank( inputObsProjectRef.getScienceRank() );
			newObsProject.setScienceScore( inputObsProjectRef.getScienceScore() );
			newObsProject.setId( inputObsProjectRef.getId() );
			newObsProject.setStatus( ExecutionStatus.INCOMPLETE );
			
			// Create the Affiliations Set and their Affiliations			
			newObsProject.setAffiliation(new HashSet<Affiliation>());
			Affiliation newAffiliation = new Affiliation();
			ExecutiveDAO execDao = (ExecutiveDAO) context.getBean("execDao");
			newAffiliation.setExecutive( execDao.getExecutive( inputObsProjectRef.getPrincipalInvestigator() ).getName() );
			System.out.println("OUTPUT: This project belongs to: " + execDao.getExecutive( inputObsProjectRef.getPrincipalInvestigator() ).getName() );
			newAffiliation.setPercentage( 0.00f );

			newObsProject.getAffiliation().add( newAffiliation );
			
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
	
	public void notifySchedBlockStop(SchedBlock sb){
		// TODO: To be able to implement SBR retrieval, the datamodel must include the ORIGINAL ID.
		
		//if( sb.getSchedBlockControl().getState() == SchedBlockState.FULLY_OBSERVED ){
		//	sbr.setStatus( ExecutionStatus.COMPLETE );
		//}
	}
		
	/**
	 * Gathers data at the end of simulation to complete the output data.
	 */
	@Transactional
	public void completeResults(){
		System.out.println("Completing results");
		
		for( ObservationProject op : results.getObservationProject()){
			double execTime = 0;
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
			System.out.println("\\-Completing observation project #" + op.getId() + ": " + op.getExecutionTime() );
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
