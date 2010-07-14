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
 */
package alma.scheduling.psm.sim;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.PI;
import alma.scheduling.datamodel.executive.PIMembership;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.executive.dao.ExecutiveDaoImpl;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.observatory.dao.ObservatoryDao;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnit;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.ObservingParameters;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.SchedBlockState;
import alma.scheduling.datamodel.obsproject.ScienceParameters;
import alma.scheduling.datamodel.obsproject.dao.ObsProjectDao;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;
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

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(ResultComposer.class);
	private Results results;
	private ApplicationContext context = null;
	private HashMap<Long,ArrayList<Date>> startDates;
	private HashMap<Long,LinkedHashMap<Date,Double>> endDates;
	
	
	public ResultComposer(){
		results = new Results();
		results.setArray( new HashSet<Array>() );
		results.setObservationProject( new HashSet<ObservationProject>() );
		startDates = new HashMap<Long,ArrayList<Date>>();
		endDates = new HashMap<Long,LinkedHashMap<Date,Double>>();
	}
	
	public void notifyExecutiveData(ApplicationContext ctx, Date obsSeasonStart, Date obsSeasonEnd, Date simStart, Date simStop){
		this.context = ctx;
		results.setObsSeasonStart(obsSeasonStart);
		results.setObsSeasonEnd(obsSeasonEnd);
		results.setStartSimDate(obsSeasonStart);
		results.setStopSimDate(obsSeasonEnd);
		results.setAvailableTime( (results.getObsSeasonEnd().getTime() - results.getObsSeasonStart().getTime())/1000);
		results.setStartRealDate(new Date());
	}
	
	public void notifyArrayCreation(ArrayConfiguration arrcfg){
		Array arr = new Array();
		arr.setCreationDate( arrcfg.getStartTime() );
		arr.setDeletionDate( arrcfg.getEndTime() );
		arr.setAvailableTime( (arr.getDeletionDate().getTime() - arr.getCreationDate().getTime())/1000);
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
    @Transactional(readOnly=true)
	public void notifySchedBlockStart(SchedBlock sb){
		// TODO Implement distinction of types of SB. If maintenance, account to a ghost ObsProject.
		
		// Creation of SchedBlockResult
		SchedBlockResult sbr = new SchedBlockResult();
		Iterator<Array> it = results.getArray().iterator();
		sbr.setArrayRef( it.next() );
		sbr.setStartDate( TimeHandler.now() );
		sbr.setExecutionTime( sb.getObsUnitControl().getEstimatedExecutionTime() );
		sbr.setStatus( ExecutionStatus.INCOMPLETE );
		sbr.setRepresentativeFrequency( sb.getSchedulingConstraints().getRepresentativeFrequency() );
		//TODO: Implement modes in data-model
		// In the XSD, there is a spec: InstrumentSpecT, and ObservingMode
		sbr.setMode( "Single Dish" ); 
		//TODO: Waiting for SB type implementation on data-model.
		sbr.setType( "SCIENTIFIC" ); 
		sbr.setOriginalId( sb.getId() );
		
		// Obtaining reference from the SchedBlock to the Observation Project.
		ObsProject inputObsProjectRef = sb.getProject();
				
		// Check if the Observation Project already exists in the results collection of obsprojects.
		// TODO: add to the datamodel an attribute that saves the original obsproject ID
		boolean isPresent = false;
		ObservationProject outputObservationProjectRef = null;
		for( ObservationProject tmpOp : results.getObservationProject() ){
			if( tmpOp.getOriginalId() == inputObsProjectRef.getId() ){
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
			newObsProject.setOriginalId( inputObsProjectRef.getId() );
			newObsProject.setStatus( ExecutionStatus.INCOMPLETE );
			
			// Create the Affiliations Set and their Affiliations			
			newObsProject.setAffiliation(new HashSet<Affiliation>());
			Affiliation newAffiliation = new Affiliation();
			ExecutiveDAO execDao = (ExecutiveDAO) context.getBean("execDao");
			// newAffiliation.setExecutive( execDao.getExecutive( inputObsProjectRef.getPrincipalInvestigator() ).getName() );
			logger.info("Passing 001");
			//newAffiliation.setExecutive(sb.getExecutive().getName());
			newAffiliation.setExecutive(execDao.getExecutive(sb.getPiName()).getName());
//			newAffiliation.setPercentage( 
//					execDao.getPIFromEmail( 
//							inputObsProjectRef.getPrincipalInvestigator() )
//							.getPIMembership()
//							.iterator()
//							.next()
//							.getMembershipPercentage() );
			//TODO: Fix affiliation percentage.
			newAffiliation.setPercentage(1.0f);
			newObsProject.getAffiliation().add( newAffiliation );
			
			// Create the SchedBlock Set and add the new ObservationProject to the Results
			newObsProject.setSchedBlock(new HashSet<SchedBlockResult>());

			results.getObservationProject().add( newObsProject );
			// Save the reference for further use
			outputObservationProjectRef = newObsProject;
		}
		
		// Add the SchedBlock to the corresponding ObservationProject
		outputObservationProjectRef.getSchedBlock().add(sbr);
		
		TimeHandler.stepAhead( sb.getObsUnitControl().getEstimatedExecutionTime() );
		sbr.setEndDate( TimeHandler.now() );
	}
	
    @Transactional(readOnly=true)
	public void notifySchedBlockStop(SchedBlock sb){
		// private HashMap<Long,LinkedHashMap<Date,Double>> endDates;
		
		//Saving schedblock id and their end dates
		LinkedHashMap<Date, Double> entry = endDates.get( sb.getId() );
		if( entry == null ){
			entry = new LinkedHashMap<Date, Double>( );
			endDates.put(sb.getId(), entry );
		}		
		entry.put( TimeHandler.now(), sb.getSchedBlockControl().getAchievedSensitivity() );
		
	}
	
    @Transactional(readOnly=true)
	public void calculateCompletion(SchedBlock sb, Date d, Double s){
		
	
		// Obtaining reference from the SchedBlock to the Observation Project.
		ObsProject inputObsProjectRef = sb.getProject();
				
		// Check if the Observation Project already exists in the results collection of obsprojects.
		// TODO: add to the datamodel an attribute that saves the original obsproject ID
		boolean isPresent = false;
		ObservationProject outputObservationProjectRef = null;
		for( ObservationProject tmpOp : results.getObservationProject() ){
			if( tmpOp.getOriginalId() == inputObsProjectRef.getId() ){
				isPresent = true;
				outputObservationProjectRef = tmpOp;
				break;
			}
		}
		
		long completedSbs = 0;
		for( SchedBlockResult sbr : outputObservationProjectRef.getSchedBlock() ){
			if( sbr.getOriginalId() == sb.getId() ){
				if( sb.getSchedBlockControl().getState() == SchedBlockState.FULLY_OBSERVED ){
					sbr.setStatus( ExecutionStatus.COMPLETE );
					sbr.setEndDate( d );
					completedSbs += 1;
					// Obtaining Sensitivities
					Set<ObservingParameters> ops = sb.getObservingParameters();
			        double sensGoalJy = 10.0;
			        for (Iterator<ObservingParameters> iter = ops.iterator(); iter.hasNext();) {
			            ObservingParameters params = iter.next();
			            if (params instanceof ScienceParameters) {
			                sbr.setGoalSensitivity(((ScienceParameters) params).getSensitivityGoal());
			            }
			        }
			        sbr.setAchievedSensitivity(s);
				}
				//TODO: See what are the other status.
			}
		}		
		
		long totalSbs = this.numberOfSchedBlocks( inputObsProjectRef.getObsUnit() );
		if( completedSbs == totalSbs )
			outputObservationProjectRef.setStatus( ExecutionStatus.COMPLETE );
		else if( completedSbs > 0 )
			outputObservationProjectRef.setStatus( ExecutionStatus.INCOMPLETE );
		else{
			// TODO: This should never happen. Illegal state reached, throw exception
			outputObservationProjectRef.setStatus( ExecutionStatus.NOT_STARTED );
		}
	}
	
    @Transactional(readOnly=true)
	private long numberOfSchedBlocks( ObsUnit ouRef ){
		if( ouRef instanceof SchedBlock ){
			return 1;
		}
		
		long numberSbs = 0;
		ObsUnitSet ouSet = (ObsUnitSet)ouRef;
				
		for( ObsUnit ouTmp : ouSet.getObsUnits() ){
			if( ouTmp instanceof SchedBlock ){
				numberSbs += 1;
			}else if( ouTmp instanceof ObsUnitSet ){
				numberSbs += numberOfSchedBlocks( ouTmp );
			}
		}
		
		return numberSbs;
	}
		
	/**
	 * Gathers data at the end of simulation to complete the output data.
	 */
    @Transactional(readOnly=true)
	public void completeResults(){
		System.out.println("Completing results");
		
		SchedBlockDao schedBlockDao = (SchedBlockDao) context.getBean("sbDao");		
		Set<Long> sbIDs = endDates.keySet();
		for( Long id : sbIDs ){
			SchedBlock sb = schedBlockDao.findById(SchedBlock.class, id);
			for( Date d : endDates.get(id).keySet()){
				calculateCompletion(sb, d, endDates.get(id).get(d));
			}
			
		}
		
		results.setStopRealDate( new Date() );
		
		for( ObservationProject op : results.getObservationProject()){
			double execTime = 0;
			for( SchedBlockResult sbr : op.getSchedBlock()){
				execTime += sbr.getExecutionTime();
			}
			op.setExecutionTime(execTime);
		}
		
		for( Array arr : results.getArray()){
			arr.setScientificTime( 0.0 );
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
