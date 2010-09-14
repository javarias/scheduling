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
import java.net.URL;
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
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.algorithm.DynamicSchedulingAlgorithm;
import alma.scheduling.algorithm.DynamicSchedulingAlgorithmImpl;
import alma.scheduling.algorithm.SchedBlockExecutor;
import alma.scheduling.algorithm.VerboseLevel;
import alma.scheduling.algorithm.astro.TimeUtil;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.dataload.CompositeDataLoader;
import alma.scheduling.dataload.DataLoader;
import alma.scheduling.dataload.DataUnloader;
import alma.scheduling.datamodel.config.Configuration;
import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.config.dao.ConfigurationDaoImpl;
import alma.scheduling.datamodel.config.dao.XmlConfigurationDaoImpl;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.observatory.dao.ObservatoryDao;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;
import alma.scheduling.datamodel.output.Results;
import alma.scheduling.datamodel.output.dao.OutputDao;
import alma.scheduling.datamodel.output.dao.XmlOutputDaoImpl;
import alma.scheduling.datamodel.weather.dao.WeatherHistoryDAO;
import alma.scheduling.psm.cli.Console;
import alma.scheduling.psm.sim.EventType;
import alma.scheduling.psm.sim.TimeEvent;
import alma.scheduling.psm.util.Ph1mSynchronizer;
import alma.scheduling.psm.util.PsmContext;
import alma.scheduling.psm.util.XsltTransformer;
import alma.scheduling.psm.sim.ResultComposer;
import alma.scheduling.psm.sim.TimeHandler;

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
    

    
    @Transactional
    public void run() throws IllegalArgumentException{
    	ApplicationContext ctx = getApplicationContext();
        // Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UT"));
        ExecutiveDAO execDao = (ExecutiveDAO) ctx.getBean("execDao");
        ConfigurationDao configDao = (ConfigurationDao) ctx.getBean("configDao");
        //Set the total numbers of projects (this can be useful for the rankers)
        DynamicSchedulingAlgorithmImpl.setnProjects(configDao.getConfiguration().getScienceGradeConfig().getTotalPrj());
        
        // Time Handling section
        TimeHandler.initialize(TimeHandler.Type.SIMULATED);
        Date time = execDao.getCurrentSeason().getStartDate(); //The start time is the start Time of the current Season
        TimeHandler.getHandler().setStartingDate( execDao.getCurrentSeason().getStartDate() );
        logger.info( TimeUtil.getUTString(time) + "Starting Simulation" );
        Date stopTime = execDao.getCurrentSeason().getEndDate();
        configDao.getConfiguration().setSimulationStartTime(new Date());
        if( simThread != null ){
        	simThread.setStartDate( time );
        	simThread.setStopDate( stopTime );
        	simThread.setCurrentDate( time );
        }
                
        setPreconditions(ctx, new Date());
        SchedBlockExecutor sbExecutor =
            (SchedBlockExecutor) ctx.getBean("schedBlockExecutor");
        ObservatoryDao observatoryDao = (ObservatoryDao) ctx.getBean("observatoryDao");
        
        // Initialization of Result Composer
        //ResultComposer rc = (ResultComposer) ctx.getBean("resultComposer");
        ResultComposer rc = new ResultComposer();
        rc.notifyExecutiveData(
        		ctx,
        		execDao.getCurrentSeason().getStartDate(), 
        		execDao.getCurrentSeason().getEndDate(), 
        		// TODO: Fix Configuration datamodel to include start and stop dates.
        		execDao.getCurrentSeason().getStartDate(), 
        		execDao.getCurrentSeason().getEndDate() );
      
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
            logger.debug(ev.toString());
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
        while( time.before(stopTime) && !timesToCheck.isEmpty() ){
            Date t1 = new Date();
        	step(timesToCheck, stopTime, rc, 
        			ctx, 
        			arraysCreated, freeArrays, sbExecutor);
        	Date t2 = new Date();
        	System.out.println("Step takes: "+ (t2.getTime() - t1.getTime()));
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
    }

//    @Transactional
    public void step(
            LinkedList<TimeEvent> timesToCheck,
            Date time,
            ResultComposer rc,
            ApplicationContext ctx,
            Hashtable<ArrayConfiguration, DynamicSchedulingAlgorithm> arraysCreated,
            ArrayList<ArrayConfiguration> freeArrays,
            SchedBlockExecutor sbExecutor) throws IllegalArgumentException {

		TimeEvent ev = timesToCheck.remove();
        // Change the current simulation time to event time
        time = ev.getTime();
        if( simThread != null ){
        	simThread.setCurrentDate( time );
        }
        switch (ev.getType()) {
        case ARRAY_CREATION:
            TimeHandler.getHandler().step(ev.getTime());
            DynamicSchedulingAlgorithm dsa;
            System.out.println(TimeUtil.getUTString(time) + "Array "
                    + ev.getArray().getId() + " created");
            rc.notifyArrayCreation(ev.getArray());
            dsa = getDSA(ctx);
            dsa.setVerboseLevel(verboseLvl);
            dsa.setArray(ev.getArray());
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
                logger.info("Ranking available scheduling blocks for Arrai Id: " + ev.getArray().getId());
                dsa.rankSchedBlocks(time);
                logger.info("Obtaining scheduling block for array: " + ev.getArray().getId());
                SchedBlock sb = dsa.getSelectedSchedBlock();
                logger.info("Executing scheduling block for array: " + ev.getArray().getId());
                TimeHandler.getHandler().step( ev.getTime() );
                // Replace second argument with the proper execution time when it gets simulated.
                Date d = sbExecutor.execute(sb, ev.getArray(), time);
                System.out.println("ARRAY_CREATION End Date given by executor: " + d);
                rc.notifySchedBlockStart(sb, ev.getArray().getId() );
                logger.info("Notification: " + ev.getArray().getId());
                // Create a new EventTime to check the SB execution termination
                // in the future
                logger.info("Simulating passed time for array: " + ev.getArray().getId());
                TimeEvent sbEndEv = new TimeEvent();
                sbEndEv.setType(EventType.SCHEDBLOCK_EXECUTION_FINISH);
                sbEndEv.setSb(sb);
                sbEndEv.setArray(ev.getArray());
                sbEndEv.setTime(d);
                TimeHandler.getHandler().step( d );
                rc.notifySchedBlockStop(sb, sb.getObsUnitControl().getEstimatedExecutionTime());
                timesToCheck.add(sbEndEv);                
            } catch (NoSbSelectedException ex) {
                System.out.println("After selectors " + new Date());
                System.out.println(TimeUtil.getUTString(time)
                        + " DSA for array " + ev.getArray().getId().toString()
                        + " -- No suitable SBs to be scheduled");
                freeArrays.add(ev.getArray());
                break;
            }
            break;
        case ARRAY_DESTRUCTION:
            // notify the destruction?? (RESPONSE: No, at this moment.
            // Everything is gathered at creation)
            System.out.println(TimeUtil.getUTString(time) + "Array Id: "
                    + ev.getArray().getId() + " destroyed");
            TimeHandler.getHandler().step(ev.getTime());
            arraysCreated.remove(ev.getArray());
            freeArrays.remove(ev.getArray());
            break;
        case SCHEDBLOCK_EXECUTION_FINISH:
            sbExecutor.finishSbExecution(ev.getSb(), ev.getArray(), time);
            TimeHandler.getHandler().step(ev.getTime());
            System.out.println(TimeUtil.getUTString(time)
                    + "Finishing Execution of SchedBlock Id: "
                    + ev.getSb().getId());
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
                TimeHandler.getHandler().step( ev.getTime() );
                // Replace second argument with the proper execution time when it gets simulated.
                Date d = sbExecutor.execute(sb, ev.getArray(), time);
                System.out.println("SCHEDBLOCK_EXECUTION_FINISH End Date given by executor: " + d);
                rc.notifySchedBlockStart(sb, ev.getArray().getId());
                // Create a new EventTime to check the SB execution termination
                // in the future
                TimeEvent sbEndEv = new TimeEvent();
                sbEndEv.setType(EventType.SCHEDBLOCK_EXECUTION_FINISH);
                sbEndEv.setSb(sb);
                sbEndEv.setArray(ev.getArray());
                sbEndEv.setTime(d);
                timesToCheck.add(sbEndEv);
                TimeHandler.getHandler().step( d );
                rc.notifySchedBlockStop(sb, sb.getObsUnitControl().getEstimatedExecutionTime());
            } catch (NoSbSelectedException ex) {
                System.out.println("After selectors " + new Date());
                System.out.println("DSA for array "
                        + ev.getArray().getId().toString()
                        + " No suitable SBs to be scheduled");
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
            TimeHandler.getHandler().step(ev.getTime());
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
                
                TimeHandler.getHandler().step( ev.getTime() );
                // Replace second argument with the proper execution time when it gets simulated.
                Date d = sbExecutor.execute(sb, ev.getArray(), time);
                System.out.println("FREE_ARRAY End Date given by executor: " + d);
                rc.notifySchedBlockStart(sb, ev.getArray().getId() );
                // Create a new EventTime to check the SB execution termination
                // in the future
                TimeEvent sbEndEv = new TimeEvent();
                sbEndEv.setType(EventType.SCHEDBLOCK_EXECUTION_FINISH);
                sbEndEv.setSb(sb);
                sbEndEv.setArray(ev.getArray());
                sbEndEv.setTime(d);
                timesToCheck.add(sbEndEv);
                TimeHandler.getHandler().step( d );
                rc.notifySchedBlockStop(sb, sb.getObsUnitControl().getEstimatedExecutionTime());           
                
            } catch (NoSbSelectedException ex) {
                System.out.println("DSA for array "
                        + ev.getArray().getId().toString()
                        + " No suitable SBs to be scheduled");
                freeArrays.add(ev.getArray());
                break;
            }
            break;
        }
        if (freeArrays.size() > 0) {
            // Check in 30 mins more of the simulated time
            Date next = new Date(time.getTime() + (30 * 60 * 1000));
            for (ArrayConfiguration a : freeArrays) {
                TimeEvent freeEv = new TimeEvent();
                freeEv.setArray(a);
                freeEv.setTime(next);
                freeEv.setType(EventType.FREE_ARRAY);
                timesToCheck.add(freeEv);
            }
        }
        // Sort in ascending order the timeline
        Collections.sort(timesToCheck);
    }
    
    private DynamicSchedulingAlgorithm getDSA(ApplicationContext ctx) throws IllegalArgumentException {
        if (DSAName == null) {
            String[] dsaNames = ctx
                    .getBeanNamesForType(DynamicSchedulingAlgorithmImpl.class);
            if (dsaNames.length == 0)
                throw new IllegalArgumentException(
                        "There is not a Dynamic Scheduling Algorithm bean defined in the context.xml file");
            if (dsaNames.length > 1)
                throw new IllegalArgumentException(
                        "There are more than 1 Dynamic Scheduling Algorithm Beans defined in the context.xml file");
            DSAName = dsaNames[0];
        }
        DynamicSchedulingAlgorithm dsa = (DynamicSchedulingAlgorithm) ctx
                .getBean(DSAName);
        return dsa;
    }
    
    private void setPreconditions(ApplicationContext ctx, Date time) throws IllegalArgumentException {
        String[] whDaos = ctx.getBeanNamesForType(WeatherHistoryDAO.class);
        for(int i = 0; i < whDaos.length; i++) {
            WeatherHistoryDAO whDao = (WeatherHistoryDAO) ctx.getBean(whDaos[i]);
            whDao.setSimulationStartTime(time);
        }
        DynamicSchedulingAlgorithm dsa = getDSA(ctx);
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
