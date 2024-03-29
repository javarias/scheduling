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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import alma.scheduling.SchedulingPolicyFile;
import alma.scheduling.algorithm.DynamicSchedulingAlgorithm;
import alma.scheduling.algorithm.DynamicSchedulingAlgorithmImpl;
import alma.scheduling.algorithm.PoliciesContainersDirectory;
import alma.scheduling.algorithm.SchedBlockExecutor;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.datamodel.config.Configuration;
import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.config.dao.XmlConfigurationDaoImpl;
import alma.scheduling.datamodel.executive.TimeInterval;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.observation.ExecBlock;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.observatory.dao.ObservatoryDao;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.output.SimulationResults;
import alma.scheduling.datamodel.output.dao.OutputDao;
import alma.scheduling.datamodel.output.dao.XmlOutputDaoImpl;
import alma.scheduling.datamodel.weather.dao.WeatherHistoryDAO;
import alma.scheduling.psm.cli.Console;
import alma.scheduling.psm.sim.EventType;
import alma.scheduling.psm.sim.TimeEvent;
import alma.scheduling.psm.util.PsmContext;
import alma.scheduling.psm.sim.ResultComposer;
import alma.scheduling.utils.TimeUtil;

public class Simulator extends PsmContext {
    
	private static Logger logger = LoggerFactory.getLogger(Simulator.class);
    private ConfigurationDao xmlConfigDao = new XmlConfigurationDaoImpl();
    private static String DSAName = null;
    private boolean toBeInterrupted = false;
    private Configuration config = null;
    private SimulatorThread simThread= null;
    
    public Simulator(String workDir) {
		super(workDir);
	}
    
    public Simulator(String workDir, SimulatorThread simThread) {
		super(workDir);
		this.simThread = simThread;
		
	}
      
    public void createWorkDir(String newWorkDir){
    	
        File entries[] = new File[7];
        entries[0] = new File(newWorkDir + "/db");
        entries[1] = new File(newWorkDir + "/projects");
        entries[2] = new File(newWorkDir + "/weather");
        entries[3] = new File(newWorkDir + "/observatory");
        entries[4] = new File(newWorkDir + "/executives");
        entries[5] = new File(newWorkDir + "/output");
        entries[6] = new File(newWorkDir + "/reports");
        //aprc-config.xml    - a general configuration file for the APRC
        //context.xml        - Spring context file 
        
        for(int i = 0; i < entries.length; i++){
            if(entries[i].exists()){
                if(entries[i].isDirectory())
                    break;
                entries[i].delete();
            }
            entries[i].mkdir();
        }
        alma.scheduling.input.config.generated.Configuration config = 
            new alma.scheduling.input.config.generated.Configuration();
        File configFile = new File(newWorkDir + "/aprc-config.xml");
        config.setContextFilePath("context.xml");
        config.setProjectDirectory("projects");
        config.setWeatherDirectory("weather");
        config.setObservatoryDirectory("observatory");
        config.setExecutiveDirectory("executives");
        config.setOutputDirectory("output");
        config.setReportDirectory("reports");
        config.setContextFilePath("context.xml");
        if(configFile.exists())
            configFile.delete();
        try {
            config.marshal(new FileWriter(configFile));
        } catch (MarshalException e) {
        	logger.error("Unable to save Configuration XML.");
            e.printStackTrace();
        	System.exit(5);
        } catch (ValidationException e) {
        	logger.error("XML does not validate against schema.");
        	e.printStackTrace();
        	System.exit(6);
        } catch (IOException e) {
        	logger.error("Unable to create configuration file " + newWorkDir + "/aprc-config.xml");
			e.printStackTrace();
        	System.exit(7);
		}
    }
    
    public SimulationResults run(String DSAPolicyName) throws IllegalArgumentException{
    	ApplicationContext ctx = getApplicationContext();
        // Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UT"));
        ExecutiveDAO execDao = (ExecutiveDAO) ctx.getBean("execDao");
        ConfigurationDao configDao = (ConfigurationDao) ctx.getBean("configDao");
        //Set the total numbers of projects (this can be useful for the rankers)
        DynamicSchedulingAlgorithmImpl.setnProjects(configDao.getConfiguration().getScienceGradeConfig().getTotalPrj());
        
        // Time Handling section
        Date time = execDao.getCurrentSeason().getStartDate(); //The start time is the start Time of the current Season
        logger.info( TimeUtil.getUTString(time) + "Starting Simulation" );
        Date stopTime = execDao.getCurrentSeason().getEndDate();
        TimeInterval ti = execDao.getCurrentSeason().getObservingInterval();
        configDao.getConfiguration().setSimulationStartTime(new Date());
        if( simThread != null ){
        	simThread.setStartDate( time );
        	simThread.setStopDate( stopTime );
        	simThread.setCurrentDate( time );
        }
                
//        setPreconditions(ctx, new Date());
        setPreconditions(ctx, time, DSAPolicyName );
        SchedBlockExecutor sbExecutor =
            (SchedBlockExecutor) ctx.getBean("schedBlockExecutor");
        ObservatoryDao observatoryDao = (ObservatoryDao) ctx.getBean("observatoryDao");
        
        // Initialization of Result Composer
        //ResultComposer rc = (ResultComposer) ctx.getBean("resultComposer");
        ResultComposer rc = new ResultComposer();
        rc.notifyExecutiveData(
        		ctx,
        		execDao.getCurrentSeason(),
        		// TODO: Fix Configuration datamodel to include start and stop dates.
        		execDao.getCurrentSeason().getStartDate(), 
        		execDao.getCurrentSeason().getEndDate());
        
        //This is the timeline
        LinkedList<TimeEvent> timesToCheck = new LinkedList<TimeEvent>();
        
        //This will contains all the arrays and the DSA associated to the array
        //that can be used in the current simulation time
        Hashtable<ArrayConfiguration,DynamicSchedulingAlgorithm> arraysCreated =
            new Hashtable<ArrayConfiguration, DynamicSchedulingAlgorithm>();
        
        //The arrays created which are free (not running an sb)
        ArrayList<ArrayConfiguration> freeArrays= new ArrayList<ArrayConfiguration>(); 
        
        //All the arrays
        ArrayList<ArrayConfiguration> arrCnfs = 
            new ArrayList<ArrayConfiguration>(observatoryDao.findArrayConfigurations());
        
        //Create the events for each array construction and destruction
        for(Iterator<ArrayConfiguration> iter = arrCnfs.iterator(); iter.hasNext();){
            ArrayConfiguration arrCnf = iter.next();
            
            TimeEvent creationEvent = new TimeEvent();
            creationEvent.setType(EventType.ARRAY_CREATION);
            creationEvent.setTime(arrCnf.getStartTime());
            creationEvent.setArray(arrCnf);
            timesToCheck.add(creationEvent);
            
            TimeEvent destructionEvent = new TimeEvent();
            destructionEvent.setType(EventType.ARRAY_DESTRUCTION);
            destructionEvent.setTime(arrCnf.getEndTime());
            destructionEvent.setArray(arrCnf);
            timesToCheck.add(destructionEvent);
        }
        //Sort the times is ascending order
        Collections.sort(timesToCheck);
        
        for (TimeEvent ev : timesToCheck) {
            logger.info(ev.toString());
        }
        
        /* Check the current time and discard the older events*/
        while(timesToCheck.size() > 0){
            if(timesToCheck.getFirst().getTime().before(time))
                timesToCheck.remove();
            else
                break;
        }

        logger.debug("timesToCheck after removing before times:");
        for (TimeEvent ev : timesToCheck) {
            logger.debug(ev.toString());
        }
        

        // Stop at end of season
        while(!timesToCheck.isEmpty() && timesToCheck.get(0).getTime().before(stopTime)){
            Date t1 = new Date();
        	setChanged();
        	notifyObservers(
        			new SimulationProgressEvent(time,
        					timesToCheck.get(0).getTime(), stopTime, timesToCheck.get(0)));
        	step(timesToCheck, stopTime, ti , rc,
        			ctx, 
        			arraysCreated, freeArrays, sbExecutor, DSAPolicyName);
        	Date t2 = new Date();
        	System.out.println("Step takes: "+ (t2.getTime() - t1.getTime()));
        	//Check if the current simulation time is already after the end of the daily interval
        	//if it so calculate the next interval
//        	if (endIntervalDate != null && time.after(endIntervalDate)) {
//            	time = ti.getStartNextInterval(time);
//            	endIntervalDate = ti.getEndInterval(time);
//        	}
        	if(isToBeInterrupted())
        	    Console.getConsole().activate(this);
        }   
        
        rc.completeResults();
        //Saving results to DB and XML output file
        OutputDao outDao = (OutputDao) ctx.getBean("outDao");
        outDao.saveResults( rc.getResults() );
        XmlOutputDaoImpl xmlOutDao = new XmlOutputDaoImpl();
        xmlOutDao.setConfigDao(xmlConfigDao);
        xmlOutDao.saveResults( rc.getResults() );      
        
        return rc.getResults();
    }

//    @Transactional
    public void step(
            LinkedList<TimeEvent> timesToCheck,
            Date time,
            TimeInterval ti,
            ResultComposer rc,
            ApplicationContext ctx,
            Hashtable<ArrayConfiguration, DynamicSchedulingAlgorithm> arraysCreated,
            ArrayList<ArrayConfiguration> freeArrays,
            SchedBlockExecutor sbExecutor,
            String DSAPolicyName) throws IllegalArgumentException {

		TimeEvent ev = timesToCheck.remove();
        // Change the current simulation time to event time
        time = ev.getTime();
        if( simThread != null ){
        	simThread.setCurrentDate( time );
        }
        switch (ev.getType()) {
        case ARRAY_CREATION:
            DynamicSchedulingAlgorithm dsa;
            System.out.println(TimeUtil.getUTString(time) + "Array "
                    + ev.getArray().getId() + " created");
            rc.notifyArrayCreation(ev.getArray(), ti);
            //TODO: Fix this
            dsa = getDSA(ctx, DSAPolicyName);
            dsa.setVerboseLevel(verboseLvl);
            dsa.setArray(ev.getArray());
            dsa.initialize(time);
            arraysCreated.put(ev.getArray(), dsa);
            System.out
                    .println(TimeUtil.getUTString(time)
                            + "Starting selection of candidate SchedBlocks for Array Id: "
                            + ev.getArray().getId());
            try {
                Date t1 = new Date();
                dsa.selectCandidateSB(time);
                Date t2 = new Date();
                System.out.println("Selection takes: "+ (t2.getTime() - t1.getTime()));
                dsa.updateCandidateSB(time);
                logger.info("Ranking available scheduling blocks for Array Id: " + ev.getArray().getId());
                dsa.rankSchedBlocks(time);
                logger.info("Obtaining scheduling block for array: " + ev.getArray().getId());
                SchedBlock sb = dsa.getSelectedSchedBlock();
                logger.info("Executing scheduling block for array: " + ev.getArray().getId());
                // Replace second argument with the proper execution time when it gets simulated.
                Date d = sbExecutor.execute(sb, ev.getArray(), time);
                System.out.println("ARRAY_CREATION End Date given by executor: " + d + " Initial time: " + time);
                //If there is a valid interval of time where the observation is carried over, use it!
                Date endIntervalDate = null;
                if (ti != null && ti.isValid()) {
                	endIntervalDate = ti.getEndInterval(time);
                }
            	if (endIntervalDate != null && 
            			(d.after(endIntervalDate) || d.equals(endIntervalDate))) {
            		Date startNextIntervalDate = ti.getStartNextInterval(d);
            		TimeEvent startObsIntervalEv = new TimeEvent();
            		startObsIntervalEv.setType(EventType.START_NEXT_OBSERVATION_TIME_INTERVAL);
            		startObsIntervalEv.setTime(startNextIntervalDate);
            		startObsIntervalEv.setArray(ev.getArray());
            		timesToCheck.add(startObsIntervalEv);
            		d = endIntervalDate; //Stops at the end of this interval
            	}
                logger.info("Notification: " + ev.getArray().getId());
                // Create a new EventTime to check the SB execution termination
                // in the future
                logger.info("Simulating passed time for array: " + ev.getArray().getId());
                TimeEvent sbEndEv = new TimeEvent();
                sbEndEv.setType(EventType.SCHEDBLOCK_EXECUTION_FINISH);
                sbEndEv.setSb(sb);
                sbEndEv.setArray(ev.getArray());
                sbEndEv.setTime(d);
                if (endIntervalDate != null && d.equals(endIntervalDate))
                	sbEndEv.setEndOfInterval(true);
               timesToCheck.add(sbEndEv);                
            } catch (NoSbSelectedException ex) {
                System.out.println("After selectors " + new Date());
                System.out.println(TimeUtil.getUTString(time)
                        + " DSA for array " + ev.getArray().getId().toString()
                        + " -- No suitable SBs to be scheduled");
                if (!freeArrays.contains(ev.getArray())) 
                	freeArrays.add(ev.getArray());
                break;
            }
            break;
        case ARRAY_DESTRUCTION:
            // notify the destruction?? (RESPONSE: No, at this moment.
            // Everything is gathered at creation)
            System.out.println(TimeUtil.getUTString(time) + "Array Id: "
                    + ev.getArray().getId() + " destroyed");
            arraysCreated.remove(ev.getArray());
            freeArrays.remove(ev.getArray());
            break;
        case SCHEDBLOCK_EXECUTION_FINISH:
            ExecBlock eb = sbExecutor.finishSbExecution(ev.getSb(), ev.getArray(), time);
            System.out.println(TimeUtil.getUTString(time)
                    + "Finishing Execution of SchedBlock Id: "
                    + ev.getSb().getId());
            rc.notifySchedBlockStop(ev.getSb(), eb, ev.getArray());
            if (ev.isEndOfInterval()) //If we finish the daily execution time just continue
            	break;
            if (arraysCreated.get(ev.getArray()) != null)
            System.out
                    .println(TimeUtil.getUTString(time)
                            + "Starting selection of candidate SchedBlocks for Array Id: "
                            + ev.getArray().getId());
            try {
                dsa = arraysCreated.get(ev.getArray());
                Date t1 = new Date();
                dsa.selectCandidateSB(time);
                Date t2 = new Date();
                System.out.println("Selection takes: "+ (t2.getTime() - t1.getTime()));
                dsa.updateCandidateSB(time);
                dsa.rankSchedBlocks(time);
                SchedBlock sb = dsa.getSelectedSchedBlock();
                // Replace second argument with the proper execution time when it gets simulated.
                Date d = sbExecutor.execute(sb, ev.getArray(), time);
                System.out.println("SCHEDBLOCK_EXECUTION_FINISH End Date given by executor: " + d + " Initial time: " + time);
              //If there is a valid interval of time where the observation is carried over, use it!
                Date endIntervalDate = null;
                if (ti != null && ti.isValid()) {
                	endIntervalDate = ti.getEndInterval(time);
                	System.out.println("Current time: " + time);
                	System.out.println("End of interval time: " + endIntervalDate);
                	System.out.println("End Execution SB: " + d);
                }
            	if (endIntervalDate != null && (d.after(endIntervalDate) || d.equals(endIntervalDate))) {
            		Date startNextIntervalDate = ti.getStartNextInterval(d);
            		TimeEvent startObsIntervalEv = new TimeEvent();
            		startObsIntervalEv.setType(EventType.START_NEXT_OBSERVATION_TIME_INTERVAL);
            		startObsIntervalEv.setTime(startNextIntervalDate);
            		startObsIntervalEv.setArray(ev.getArray());
            		timesToCheck.add(startObsIntervalEv);
            		d = endIntervalDate; //Stops at the end of this interval
            	}
                // Create a new EventTime to check the SB execution termination
                // in the future
                TimeEvent sbEndEv = new TimeEvent();
                sbEndEv.setType(EventType.SCHEDBLOCK_EXECUTION_FINISH);
                sbEndEv.setSb(sb);
                sbEndEv.setArray(ev.getArray());
                sbEndEv.setTime(d);
                if (endIntervalDate != null && d.equals(endIntervalDate))
                	sbEndEv.setEndOfInterval(true);
                timesToCheck.add(sbEndEv);
           } catch (NoSbSelectedException ex) {
                System.out.println("After selectors " + new Date());
                System.out.println("DSA for array "
                        + ev.getArray().getId().toString()
                        + " No suitable SBs to be scheduled");
                if (!freeArrays.contains(ev.getArray())) 
                	freeArrays.add(ev.getArray());
                break;
            }catch (NullPointerException ex){
                // The array was destroyed already
            }
            break;

        case FREE_ARRAY:
            dsa = null;
            try{
                dsa = arraysCreated.get(ev.getArray());
            } catch (NullPointerException ex){ //The array was destroyed, deleting the remaining events   
            }
            if (dsa == null)
                return;
            // removing from free list
            while (freeArrays.remove(ev.getArray()))
                ;
            System.out
                    .println(TimeUtil.getUTString(time)
                            + "Starting selection of candidate SchedBlocks for Array Id: "
                            + ev.getArray().getId());
            try {
                // System.out.println("Before selectors " + new Date());
                Date t1 = new Date();
                dsa.selectCandidateSB(time);
                Date t2 = new Date();
                System.out.println("Selection takes: "+ (t2.getTime() - t1.getTime()));
                dsa.updateCandidateSB(time);
                // System.out.println("After selectors " + new Date());
                // System.out.println("Before rankers " + new Date());
                dsa.rankSchedBlocks(time);
                // System.out.println("After rankers " + new Date());
                SchedBlock sb = dsa.getSelectedSchedBlock();
                
                // Replace second argument with the proper execution time when it gets simulated.
                Date d = sbExecutor.execute(sb, ev.getArray(), time);
                System.out.println("FREE_ARRAY End Date given by executor: " + d + " Initial time: " + time);
                //If there is a valid interval of time where the observation is carried over, use it!
                Date endIntervalDate = null;
                if (ti != null && ti.isValid()) {
                	endIntervalDate = ti.getEndInterval(time);
                }
            	if (endIntervalDate != null && (d.after(endIntervalDate) || d.equals(endIntervalDate))) {
            		Date startNextIntervalDate = ti.getStartNextInterval(d);
            		TimeEvent startObsIntervalEv = new TimeEvent();
            		startObsIntervalEv.setType(EventType.START_NEXT_OBSERVATION_TIME_INTERVAL);
            		startObsIntervalEv.setTime(startNextIntervalDate);
            		startObsIntervalEv.setArray(ev.getArray());
            		timesToCheck.add(startObsIntervalEv);
            		d = endIntervalDate; //Stops at the end of this interval
            	}
                // Create a new EventTime to check the SB execution termination
                // in the future
                TimeEvent sbEndEv = new TimeEvent();
                sbEndEv.setType(EventType.SCHEDBLOCK_EXECUTION_FINISH);
                sbEndEv.setSb(sb);
                sbEndEv.setArray(ev.getArray());
                sbEndEv.setTime(d);
                if (endIntervalDate != null && d.equals(endIntervalDate))
                	sbEndEv.setEndOfInterval(true);
                timesToCheck.add(sbEndEv);
            } catch (NoSbSelectedException ex) {
                System.out.println("DSA for array "
                        + ev.getArray().getId().toString()
                        + " No suitable SBs to be scheduled");
                if (!freeArrays.contains(ev.getArray())) 
                	freeArrays.add(ev.getArray());
                break;
            }
            break;
        case START_NEXT_OBSERVATION_TIME_INTERVAL:
        	//HACK in case the array has been already destroyed
        	//TODO: Rafactor this piece of garbage and remove this.
        	dsa = arraysCreated.get(ev.getArray());
        	if (dsa == null)
        		break;
            System.out
                    .println(TimeUtil.getUTString(time)
                            + "Starting selection of candidate SchedBlocks for Array Id: "
                            + ev.getArray().getId());
            try {
            	dsa = arraysCreated.get(ev.getArray());
                Date t1 = new Date();
                dsa.selectCandidateSB(time);
                Date t2 = new Date();
                System.out.println("Selection takes: "+ (t2.getTime() - t1.getTime()));
                dsa.updateCandidateSB(time);
                logger.info("Ranking available scheduling blocks for Array Id: " + ev.getArray().getId());
                dsa.rankSchedBlocks(time);
                logger.info("Obtaining scheduling block for array: " + ev.getArray().getId());
                SchedBlock sb = dsa.getSelectedSchedBlock();
                logger.info("Executing scheduling block for array: " + ev.getArray().getId());
                // Replace second argument with the proper execution time when it gets simulated.
                Date d = sbExecutor.execute(sb, ev.getArray(), time);
                System.out.println("START_NEXT_OBSERVATION_TIME_INTERVAL End Date given by executor: " + d + " Initial time: " + time);
                //If there is a valid interval of time where the observation is carried over, use it!
                Date endIntervalDate = null;
                if (ti != null && ti.isValid()) {
                	endIntervalDate = ti.getEndInterval(time);
                }
            	if (endIntervalDate != null && (d.after(endIntervalDate) || d.equals(endIntervalDate))) {
            		Date startNextIntervalDate = ti.getStartNextInterval(d);
            		TimeEvent startObsIntervalEv = new TimeEvent();
            		startObsIntervalEv.setType(EventType.START_NEXT_OBSERVATION_TIME_INTERVAL);
            		startObsIntervalEv.setTime(startNextIntervalDate);
            		startObsIntervalEv.setArray(ev.getArray());
            		timesToCheck.add(startObsIntervalEv);
            		d = endIntervalDate; //Stops at the end of this interval
            	}
                logger.info("Notification: " + ev.getArray().getId());
                // Create a new EventTime to check the SB execution termination
                // in the future
                logger.info("Simulating passed time for array: " + ev.getArray().getId());
                TimeEvent sbEndEv = new TimeEvent();
                sbEndEv.setType(EventType.SCHEDBLOCK_EXECUTION_FINISH);
                sbEndEv.setSb(sb);
                sbEndEv.setArray(ev.getArray());
                sbEndEv.setTime(d);
                if (endIntervalDate != null && d.equals(endIntervalDate))
                	sbEndEv.setEndOfInterval(true);
               timesToCheck.add(sbEndEv);                
            } catch (NoSbSelectedException ex) {
                System.out.println("After selectors " + new Date());
                System.out.println(TimeUtil.getUTString(time)
                        + " DSA for array " + ev.getArray().getId().toString()
                        + " -- No suitable SBs to be scheduled");
                if (!freeArrays.contains(ev.getArray())) 
                	freeArrays.add(ev.getArray());
                break;
            }
        	break;
        }
        if (freeArrays.size() > 0) {
            // Check in 80 mins more of the simulated time
            Date next = new Date(time.getTime() + (80 * 60 * 1000));
            //If there is a valid interval of time where the observation is carried over, use it!
            Date endIntervalDate = null;
            if (ti != null && ti.isValid()) {
            	endIntervalDate = ti.getEndInterval(time);
            }
        	if (endIntervalDate != null && next.after(endIntervalDate)) {
        		Date startNextIntervalDate = ti.getStartNextInterval(next);
        		next = new Date(startNextIntervalDate.getTime()); //Stops at the end of this interval
        	}
            for (ArrayConfiguration a : freeArrays) {
                TimeEvent freeEv = new TimeEvent();
                freeEv.setArray(a);
                freeEv.setTime(next);
                freeEv.setType(EventType.FREE_ARRAY);
                timesToCheck.add(freeEv);
            }
            freeArrays.clear();
        }
        // Sort in ascending order the timeline
        Collections.sort(timesToCheck);
    }
    
    private DynamicSchedulingAlgorithm getDSA(ApplicationContext ctx, String DSAPolicyName) throws IllegalArgumentException {
    	for (String n: ctx.getBeanDefinitionNames())
    		System.out.println(n);
        if (DSAName == null) {
            SchedulingPolicyFile[] dsaNames = PoliciesContainersDirectory.getInstance().getAllPoliciesFiles();
            if (dsaNames.length == 0)
                throw new IllegalArgumentException(
                        "There is not a Dynamic Scheduling Algorithm bean defined in the context.xml file");
            if (dsaNames.length > 1)
                throw new IllegalArgumentException(
                        "There are more than 1 Dynamic Scheduling Algorithm Beans defined in the context.xml file");
    //TODO: Add the policy name as parameter
            DSAName = dsaNames[0].schedulingPolicies[0];
        }
        System.out.println("Using Policy: " + DSAPolicyName);
        DynamicSchedulingAlgorithm dsa = (DynamicSchedulingAlgorithm) ctx
                .getBean(DSAPolicyName);
        return dsa;
    }
    
    private void setPreconditions(ApplicationContext ctx, Date time, String DSAPolicyName) throws IllegalArgumentException {
        WeatherHistoryDAO wheatherDao = (WeatherHistoryDAO) ctx.getBean("weatherSimDao");
        wheatherDao.setSimulationStartTime(time);
        DynamicSchedulingAlgorithm dsa = getDSA(ctx, DSAPolicyName);
        System.out.println("Running first update " + new Date());
        dsa.initialize(time);
        System.out.println("Finishing first update " + new Date());
    }
    
	public void setToBeInterrupted(boolean toBeInterrupted) {
		this.toBeInterrupted = toBeInterrupted;
	}

	public boolean isToBeInterrupted() {
		return toBeInterrupted;
	}
	
	public void setWorkDirectory(String workDir){
		if(config == null){
			logger.error("Critical error: No configuration has been loaded");
			System.exit(2); // Exit code 2: no Configuration data has been loaded, and it has been requested by program.
		}
		config.setWorkDirectory(workDir);
	}

}
