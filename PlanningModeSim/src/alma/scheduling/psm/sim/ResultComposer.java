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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.observation.ExecBlock;
import alma.scheduling.datamodel.observation.dao.ObservationDao;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnit;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.ObservingParameters;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.SchedBlockState;
import alma.scheduling.datamodel.obsproject.ScienceGrade;
import alma.scheduling.datamodel.obsproject.ScienceParameters;
import alma.scheduling.datamodel.obsproject.dao.ObsProjectDao;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;
import alma.scheduling.datamodel.output.Affiliation;
import alma.scheduling.datamodel.output.Array;
import alma.scheduling.datamodel.output.ExecutionStatus;
import alma.scheduling.datamodel.output.ObservationProject;
import alma.scheduling.datamodel.output.Results;
import alma.scheduling.datamodel.output.SchedBlockResult;
import alma.scheduling.utils.DSAContextFactory;

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
	private Map<String, ArrayConfiguration> arraysUsed;
	
	public ResultComposer(){
		results = new Results();
		results.setArray( new HashSet<Array>() );
		results.setObservationProject( new HashSet<ObservationProject>() );
		arraysUsed = new HashMap<String, ArrayConfiguration>();
	}
	
	public void notifyExecutiveData(ApplicationContext ctx, Date obsSeasonStart, Date obsSeasonEnd, Date simStart, Date simStop){
		this.context = ctx;
		results.setObsSeasonStart(obsSeasonStart);
		results.setObsSeasonEnd(obsSeasonEnd);
		results.setStartSimDate(obsSeasonStart);
		results.setStopSimDate(obsSeasonEnd);
		results.setAvailableTime( (results.getObsSeasonEnd().getTime() - results.getObsSeasonStart().getTime())/3600/1000);
		results.setStartRealDate(new Date());
	}
	
	public void notifyArrayCreation(ArrayConfiguration arrcfg){
		Array arr = new Array();
		arr.setCreationDate( arrcfg.getStartTime() );
		arr.setDeletionDate( arrcfg.getEndTime() );
		arr.setAvailableTime( (arr.getDeletionDate().getTime() - arr.getCreationDate().getTime())/3600/1000);
		arr.setOriginalId(arrcfg.getId());
		if (arrcfg.getResolution() == null)
			arr.setResolution(0.0);
		else
			arr.setResolution( arrcfg.getResolution());
		if (arrcfg.getUvCoverage() == null)
			arr.setUvCoverage(0.0);
		else
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
//    @Transactional(readOnly=true)
//	public void notifySchedBlockStart(SchedBlock sb, long arrayId, Date currSimTime){
//    	
//		//Saving schedblock id and its end date
//		LinkedHashMap<Date, Long> entry = startDates.get( sb.getId() );
//		if( entry == null ){
//			entry = new LinkedHashMap<Date, Long>();
//			startDates.put(sb.getId(), entry );
//		}		
//		entry.put(currSimTime, arrayId);		
//	}
    
	public void notifySchedBlockStop(SchedBlock sb, ExecBlock eb, ArrayConfiguration ac) {
		arraysUsed.put(eb.getExecBlockUid(), ac);
		System.out.println("SB EXEC END #: " + sb.getUid() + " Sen: " + eb.getSensitivityAchieved() + " ExecTime: " + 
				(eb.getEndTime().getTime() - eb.getStartTime().getTime())/1000.0 + "seconds");
		System.out.println("SB Id: " + sb.getUid() + " took " + eb.getTimeOnSource() + " seconds to execute.");
	}
	
//	public void notifySchedBlockStop(SchedBlock sb, double executionTime, Date currSimTime){
//
//		//Saving schedblock id and their end dates
//		LinkedHashMap<Date, ArrayList<Double>> entry = endDates.get( sb.getId() );
//		if( entry == null ){
//			entry = new LinkedHashMap<Date, ArrayList<Double>>();
//			endDates.put(sb.getId(), entry );
//		}
//		ArrayList<Double> tmpAl = new ArrayList<Double>();
//		System.out.println("SB EXEC END #: " + sb.getId() + " Sen: " +sb.getSchedBlockControl().getAchievedSensitivity() + " ExecTime: " + executionTime);
//		tmpAl.add(sb.getSchedBlockControl().getAchievedSensitivity() );
//		tmpAl.add(executionTime );
//		entry.put(currSimTime, tmpAl );
//		System.out.println("SB Id: " + sb.getId() + " took " + executionTime + " seconds to execute.");
//	}
	
	private long numberOfSchedBlocks( ObsUnit ouRef ){
    	if( ouRef instanceof ObsUnitSet ){
 		
			long numberSbs = 0;
			ObsUnitSet ouSet = (ObsUnitSet)ouRef;
					
			for( ObsUnit ouTmp : ouSet.getObsUnits() ){
				if( ouTmp instanceof SchedBlock ){
					numberSbs = numberSbs + 1;
				}else if( ouTmp instanceof ObsUnitSet ){
					numberSbs = numberSbs + numberOfSchedBlocks( ouTmp );
				}
			}
			return numberSbs;
		}
		return 0;
	}
    
	private long numberOfCompletedSchedBlocks( ObsUnit ouRef ){
    	if( ouRef instanceof ObsUnitSet ){
    		
			long numberSbs = 0;
			ObsUnitSet ouSet = (ObsUnitSet)ouRef;
					
			for( ObsUnit ouTmp : ouSet.getObsUnits() ){
				if( ouTmp instanceof SchedBlock ){
		    		if( ((SchedBlock)ouTmp).getSchedBlockControl().getState() == SchedBlockState.FULLY_OBSERVED )
		    			numberSbs = numberSbs + 1;
				}else if( ouTmp instanceof ObsUnitSet ){
					numberSbs = numberSbs + numberOfCompletedSchedBlocks( ouTmp );
				}
			}
			return numberSbs;
		}
		return 0;
	}
		
	/**
	 * Gathers data at the end of simulation to complete the output data.
	 */
	public void completeResults(){
		System.out.println("Completing results");
		results.setStopRealDate( new Date() );
		
		ObsProjectDao obsProjectDao = (ObsProjectDao) context.getBean("obsProjectDao");
		SchedBlockDao schedBlockDao = (SchedBlockDao) context.getBean("sbDao");
		ExecutiveDAO execDao = (ExecutiveDAO) context.getBean("execDao");
		ObservationDao obsDao = context.getBean(DSAContextFactory.SCHEDULING_OBSERVATION_DAO_BEAN, ObservationDao.class);
		
		// Bring one by one observation project and create the output object for them
		for( ObsProject op : obsProjectDao.getObsProjectsOrderBySciRank() ){
			
			//If the project was Cancelled, or Grade is D we do not consider it for results. 
			if( op.getStatus().compareTo("CANCELLED") == 0 || op.getLetterGrade() == ScienceGrade.D )
				continue;
			
			System.out.println("\\-Completing observation project #" + op.getId());
			ObservationProject outputOp = new ObservationProject();
			// Filling what can be filled before calculations
			outputOp.setOriginalId( op.getId() );
			outputOp.setScienceRank( op.getScienceRank());
			outputOp.setScienceScore( op.getScienceScore() );
			outputOp.setGrade( op.getLetterGrade().toString() );

			HashSet<SchedBlockResult> sbrSet = new HashSet<SchedBlockResult>();
			obsProjectDao.hydrateSchedBlocks(op);			
			prepareSbrSet( op.getObsUnit(), sbrSet, obsDao ); 
			outputOp.setSchedBlock( sbrSet );
			
			outputOp.setAffiliation(new HashSet<Affiliation>());
			Affiliation newAffiliation = new Affiliation();
			newAffiliation.setExecutive( execDao.getExecutive( op.getPrincipalInvestigator() ).getName() );
			//TODO: Fix affiliation percentage.
			newAffiliation.setPercentage(100.0f);
			outputOp.getAffiliation().add( newAffiliation );

			// TODO: Calculate completion of obsproject
			long completedSbs = numberOfCompletedSchedBlocks( op.getObsUnit() );			
			long totalSbs = numberOfSchedBlocks( op.getObsUnit() );
			System.out.print("   * Number of SBs: " +  totalSbs + "\n");
			if( completedSbs == totalSbs )
				outputOp.setStatus( ExecutionStatus.COMPLETE );
			else if( completedSbs > 0 )
				outputOp.setStatus( ExecutionStatus.INCOMPLETE );
			else{
				outputOp.setStatus( ExecutionStatus.NOT_STARTED );
			}
			results.getObservationProject().add(outputOp);
		}
		
		for( ObservationProject op : results.getObservationProject()){
			double execTime = 0;
			for( SchedBlockResult sbr : op.getSchedBlock()){
				execTime += sbr.getExecutionTime();
			}
			op.setExecutionTime(execTime);

			if( (op.getStatus() != ExecutionStatus.COMPLETE) && (execTime > 0) ){
				op.setStatus( ExecutionStatus.INCOMPLETE );
			}
		}
		
		for( Array arr : results.getArray()){
			arr.setScientificTime( 0.0 );
			arr.setMaintenanceTime( 0.0 );
		}
		
		//TODO: According to actual structure, SBs need to belongs to a ObsProject. What about maintenance SBs?
		for( ObservationProject op : results.getObservationProject()){
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
    
    void prepareSbrSet( ObsUnit ptrOu, HashSet<SchedBlockResult> sbrSet, ObservationDao obsDao ){
    	if( ptrOu instanceof SchedBlock ){
    		SchedBlock sb = (SchedBlock) ptrOu;
    		String sbId = sb.getUid();
    		
			for (ExecBlock eb: obsDao.getAllExecBlocksForSB(sb.getUid())) {
			
//			for( Date d : lhmStart.keySet() ){
    			SchedBlockResult sbr = new SchedBlockResult();
    			
    			// From Start notification
    			sbr.setStartDate(eb.getStartTime());
        		//TODO: Create arrays
    			Array arrayRef = null;
    			for( Array tmpArr : results.getArray() ){
    				if( tmpArr.getOriginalId() == arraysUsed.get(eb.getExecBlockUid()).getId()){
    					arrayRef = tmpArr;
    					break;
    				}
    			}
    			
    			if( arrayRef == null ){
    				System.out.println("Output: Array not found. Critical Error");
    				//System.exit(8); //Exit code 8, no correnponding Array object found in output.
    			}
    			sbr.setArrayRef( arrayRef );
    			
        		
        		// Obtaining Goal Sensitivity
				Set<ObservingParameters> ops = ((SchedBlock)ptrOu).getObservingParameters();
		        for (Iterator<ObservingParameters> iter = ops.iterator(); iter.hasNext();) {
		            ObservingParameters params = iter.next();
		            if (params instanceof ScienceParameters) {
		                sbr.setGoalSensitivity(((ScienceParameters) params).getSensitivityGoal());
				System.out.println("SensitivityGoal: " + ((ScienceParameters) params).getSensitivityGoal() );
		            }
		        }
		        sbr.setOriginalId( sbId );
		        sbr.setMode( "N/A" );
		        sbr.setRepresentativeFrequency( ((SchedBlock)ptrOu).getSchedulingConstraints().getRepresentativeFrequency() );
		        //TODO: Add frequency band
		        sbr.setType( "SCIENTIFIC");
		        sbr.setStatus( ExecutionStatus.INCOMPLETE);
        		sbrSet.add( sbr );
        		
        		// From Stop notification.
        		sbr.setEndDate(eb.getEndTime());
    			sbr.setAchievedSensitivity(eb.getSensitivityAchieved());
    			System.out.println("Sensitivity Goal in Results: " + eb.getSensitivityAchieved());
    			sbr.setExecutionTime(eb.getTimeOnSource() / 3600.0); //in hours
    			if( ((SchedBlock)ptrOu).getSchedBlockControl().getState() == SchedBlockState.FULLY_OBSERVED ){
    				sbr.setStatus( ExecutionStatus.COMPLETE);
    			}
			}
    	}else if( ptrOu instanceof ObsUnitSet ){
    		for( ObsUnit forOu : ((ObsUnitSet)ptrOu).getObsUnits() ){
    			prepareSbrSet( forOu, sbrSet, obsDao );
    		}
    	}    	
    }
	
	/**
	 * Saves results into the database.
	 */
	public Results getResults(){
		return this.results;		
	}
	
	private static void printAncestor(Class<?> c, List<Class> l) {
		Class<?> ancestor = c.getSuperclass();
	 	if (ancestor != null) {
		    l.add(ancestor);
		    printAncestor(ancestor, l);
	 	}
	}
	
	private String getInheritance(Object o){
    	Class<?> c = o.getClass();
	    List<Class> l = new ArrayList<Class>();
	    printAncestor(c, l);
	    if (l.size() != 0) {
	    	for (Class<?> cl : l)
	    	return l.get(0).getCanonicalName();
	    }
	    return null;
	}
}
