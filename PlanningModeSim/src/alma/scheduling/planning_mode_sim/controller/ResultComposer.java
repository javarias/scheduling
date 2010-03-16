package alma.scheduling.planning_mode_sim.controller;

import java.util.HashSet;
import java.util.Iterator;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;
import alma.scheduling.datamodel.output.Array;
import alma.scheduling.datamodel.output.ExecutionStatus;
import alma.scheduling.datamodel.output.ObservationProject;
import alma.scheduling.datamodel.output.Results;
import alma.scheduling.datamodel.output.SchedBlockResult;
import alma.scheduling.datamodel.output.dao.OutputDao;

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
	//TODO: Delete this dummy obsProject for SBs, as the references for obtained the correct ObsProejct is ready.
	private ObservationProject dummyObsProject;
	
	
	public ResultComposer(){
		//TODO: Create a new instances of the output datamodel, and fill it with essential and top-level data.
		results = new Results();
		results.setArray( new HashSet<Array>() );
		results.setObservationProject( new HashSet<ObservationProject>() );
		
		
		dummyObsProject = new ObservationProject();
	}
	
	public void notifyArrayCreation(ArrayConfiguration arrcfg){
		Array arr = new Array();
		arr.setBaseline( 1000.0 );
		arr.setCreationDate( arrcfg.getStartTime() );
		arr.setDeletionDate( arrcfg.getEndTime() );
		results.getArray().add(arr);
	}
	
//	public void notifyArrayDestruction(ArrayConfiguration arrcfg){
//		//TODO: Complete when observatory characteristics models is ready
//		Iterator<Array> it = results.getArray().iterator();
//		it.next().setDeletionDate( TimeUtil.now() );
//	}
	
	/**
	 * 
	 * TODO: Add param that indicates in which array was started its execution
	 * @param sb SchedulingBlock (class from datamodel) that needs to be informed of its execution.
	 */
	public void notifySchedBlockStart(SchedBlock sb){
		
		//Creation of SchedBlockResult
		SchedBlockResult sbr = new SchedBlockResult();
		Iterator<Array> it = results.getArray().iterator();
		sbr.setArrayRef( it.next() );
		sbr.setStartDate( TimeHandler.now() );
		sbr.setExecutionTime( 0.0 );
		sbr.setMode( "Single Dish" ); //TODO: Implement modes in data-model
		sbr.setStatus(ExecutionStatus.INCOMPLETE);
		sbr.setType( "Observation" ); //TODO: Implement types in data-model
		
		//TODO: Check if the containing Observation Project already exists in the results collection of obsprojects.
		//... code
		if( !results.getObservationProject().contains(dummyObsProject) )
			results.getObservationProject().add(dummyObsProject);
	
		//TODO: Assign the SB to the correct Observation Project, whence the reverse association is created.
		dummyObsProject.getSchedBlock().add(sbr);
		
		//TODO: Use the correct time spend on simulation
		TimeHandler.stepAhead();
		sbr.setEndDate( TimeHandler.now() );
		
		sbr.setExecutionTime( sbr.getStartDate().getTime() - sbr.getEndDate().getTime() );		
	}
		
	/**
	 * Gathers data at the end of simulation to complete the output data.
	 */
	public void completeResults(){
		
		
	}
	
	/**
	 * Saves results into the database.
	 */
	public Results getResults(){
		return this.results;
		
	}


}
