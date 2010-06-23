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

package alma.scheduling.planning_mode_sim.cli;

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
import alma.scheduling.planning_mode_sim.EventType;
import alma.scheduling.planning_mode_sim.Ph1mSynchronizer;
import alma.scheduling.planning_mode_sim.TimeEvent;
import alma.scheduling.planning_mode_sim.controller.ResultComposer;
import alma.scheduling.planning_mode_sim.controller.TimeHandler;
import alma.scheduling.planning_mode_sim.util.XsltTransformer;


public class AprcTool {
    
    private static Logger logger = LoggerFactory.getLogger(AprcTool.class);
    private String workDir;
    private ConfigurationDao xmlConfigDao = new XmlConfigurationDaoImpl();
    private VerboseLevel verboseLvl = null;
    private String DSAName = null;
    boolean toBeInterrupted = false;
      
    private void help(){
        System.out.println("APRC Tool Command Line Interface");
        System.out.print("Usage: ");
        System.out.println("AprcTool <command> [options]");
        System.out.println("\nList of Commands:\n");
        System.out.println("createWorkDir:\t Creates a template for the work directory.");
        System.out.println("load:\t\t loads the database with the data stored in the XML files.");
        // TODO: What does this command do?
        System.out.println("unload:\t\t ???");
        System.out.println("clean:\t\t unload from database obsproject, executive, results, and observatory data.");
        System.out.println("step:\t\t step through each cycle of simulation, returning to command prompt.");
        System.out.println("run:\t\t runs a simulation, generating an output file.");
        System.out.println("go:\t\t loads and run a simulation.");
        System.out.println("help:\t\t Display this helpful message.");
        System.out.println("report:\t\t Generate HTML reports from the results of the simulation.");
        System.out.println("\n\noptions:");
        System.out.println("--working-dir=[path]\t set working path, override the APRC_WORK_DIR " +
        		"environment variable. By default is APRC_WORK_DIR environment variable if it is " +
        		"available, otherwise .");
    }
    
    private void reportHelp(){
        System.out.println("Reports help");
        System.out.println("Usage: ");
        System.out.println("aprc report <command>");
        System.out.println("\nList of Commands:\n");
        System.out.println("1:\tGenerate statistics report containing crowding by LST ranges and ALMA Receiver Bands");
        System.out.println("2:\tGenerate result report (after run the simulation)");
        System.out.println("help:\tShow this help message");
    }
    
    public String[] getOpt(String[] argv, String param){
        for(int i = 0; i<argv.length; i++){
            if (argv[i].contains(param)){
                if(argv[i].contains("="))
                    return argv[i].split("=");
                String retval[] = new String[2];
                retval[0]=argv[i];
                try{
                    retval[1]=argv[i+1];
                } catch(ArrayIndexOutOfBoundsException ex){
                    retval[1] = null;
                }
                return retval;
            }
        }
        return null;
    }
    
    
    private void createWorkDir(String path) throws IOException{
        File entries[] = new File[7];
        entries[0] = new File(path + "/db");
        entries[1] = new File(path + "/projects");
        entries[2] = new File(path + "/weather");
        entries[3] = new File(path + "/observatory");
        entries[4] = new File(path + "/executives");
        entries[5] = new File(path + "/output");
        entries[6] = new File(path + "/reports");
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
        File configFile = new File(path + "/aprc-config.xml");
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
            e.printStackTrace();
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }
    
    private void fullLoad(String ctxPath) throws Exception {
        ApplicationContext ctx = new FileSystemXmlApplicationContext("file://"+ctxPath);
        String[] loadersNames = ctx.getBeanNamesForType(CompositeDataLoader.class);
        String [] cfgBeans = ctx.getBeanNamesForType(ConfigurationDaoImpl.class);
        if(cfgBeans.length == 0){
            System.out.println(ctxPath + " file doesn't contain a bean of the type alma.scheduling.datamodel.config.dao.ConfigurationDaoImpl");
            System.exit(1);
        }
        for(int i = 0; i < loadersNames.length; i++){
            DataLoader loader = (DataLoader) ctx.getBean(loadersNames[i]);
            loader.load();
        }
        ConfigurationDao configDao = (ConfigurationDao) ctx.getBean(cfgBeans[0]);
        configDao.updateConfig();
    }

    private void load(String ctxPath) throws Exception {
        ApplicationContext ctx = new FileSystemXmlApplicationContext("file://"+ctxPath);
        String [] cfgBeans = ctx.getBeanNamesForType(ConfigurationDaoImpl.class);
        DataLoader loader = (DataLoader) ctx.getBean("fullDataLoader");
        loader.load();
        ConfigurationDao configDao = (ConfigurationDao) ctx.getBean(cfgBeans[0]);
        configDao.updateConfig();
    }

    private void unload(String ctxPath) {
        ApplicationContext ctx = new FileSystemXmlApplicationContext("file://"+ctxPath);
        DataUnloader loader = (DataUnloader) ctx.getBean("fullDataUnloader");
        loader.unload();
    }

    private void clean(String ctxPath) {
        ApplicationContext ctx = new FileSystemXmlApplicationContext("file://"
                + ctxPath);
        String[] loadersNames = ctx.getBeanNamesForType(CompositeDataLoader.class);
        String[] cfgBeans = ctx.getBeanNamesForType(ConfigurationDaoImpl.class);
        if (cfgBeans.length == 0) {
            System.out.println(ctxPath
            + " file doesn't contain a bean of the type alma.scheduling.datamodel.config.dao.ConfigurationDaoImpl");
            System.exit(1);
        }
        for(int i = 0; i < loadersNames.length; i++) {
            DataLoader loader = (DataLoader) ctx.getBean(loadersNames[i]);
            loader.clear();
        }
        ConfigurationDao configDao = (ConfigurationDao) ctx.getBean("configDao");
        configDao.deleteAll();
    }
    
    @Transactional
    private void run(String ctxPath) throws IllegalArgumentException{
    	ApplicationContext ctx = new FileSystemXmlApplicationContext("file://"+ctxPath);
        // Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UT"));
        ExecutiveDAO execDao = (ExecutiveDAO) ctx.getBean("execDao");
        ConfigurationDao configDao = (ConfigurationDao) ctx.getBean("configDao");
        //Set the total numbers of projects (this can be useful for the rankers)
        DynamicSchedulingAlgorithmImpl.setnProjects(configDao.getConfiguration().getScienceGradeConfig().getTotalPrj());
        
        // Time Handling section
        TimeHandler.initialize(TimeHandler.Type.SIMULATED);
        Date time = execDao.getCurrentSeason().getStartDate(); //The start time is the start Time of the current Season
        TimeHandler.getHandler().setStartingDate( execDao.getCurrentSeason().getStartDate() );
        System.out.println(TimeUtil.getUTString(time) + "Starting Simulation");
        Date stopTime = execDao.getCurrentSeason().getEndDate();
        configDao.getConfiguration().setSimulationStartTime(new Date());
        
        
        setPreconditions(ctx, new Date());
        SchedBlockExecutor sbExecutor =
            (SchedBlockExecutor) ctx.getBean("schedBlockExecutor");
        ObservatoryDao observatoryDao = (ObservatoryDao) ctx.getBean("observatoryDao");
        
        // Initialization of Result Composer
        ResultComposer rc = new ResultComposer(ctx);
        rc.notifyExecutiveData(
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
        	step(timesToCheck, stopTime, rc, ctx, arraysCreated, freeArrays, sbExecutor);
        	if(toBeInterrupted)
        	    Console.getConsole(this).activate();
        }   
        
        rc.completeResults();
        //Saving results to DB and XML output file
        OutputDao outDao = (OutputDao) ctx.getBean("outDao");
        outDao.saveResults( rc.getResults() );
        XmlOutputDaoImpl xmlOutDao = new XmlOutputDaoImpl();
        xmlOutDao.setConfigDao(xmlConfigDao);
        xmlOutDao.saveResults( rc.getResults() );        
    }

    private void step(
            LinkedList<TimeEvent> timesToCheck,
            Date time,
            ResultComposer rc,
            ApplicationContext ctx,
            Hashtable<ArrayConfiguration, DynamicSchedulingAlgorithm> arraysCreated,
            ArrayList<ArrayConfiguration> freeArrays,
            SchedBlockExecutor sbExecutor) throws IllegalArgumentException {
      //  try{
            TimeEvent ev = timesToCheck.remove();
    //        } catch (java.lang.No)
        // Change the current simulation time to event time
        time = ev.getTime();
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
                dsa.selectCandidateSB(time);
                dsa.rankSchedBlocks(time);
                SchedBlock sb = dsa.getSelectedSchedBlock();
                Date d = sbExecutor.execute(sb, ev.getArray(), time);
                TimeHandler.getHandler().step(d);
                rc.notifySchedBlockStart(sb);
                // Create a new EventTime to check the SB execution termination
                // in the future
                TimeEvent sbEndEv = new TimeEvent();
                sbEndEv.setType(EventType.SCHEDBLOCK_EXECUTION_FINISH);
                sbEndEv.setSb(sb);
                sbEndEv.setArray(ev.getArray());
                sbEndEv.setTime(d);
                rc.notifySchedBlockStop(sb);
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
                dsa.selectCandidateSB(time);
                dsa.rankSchedBlocks(time);
                SchedBlock sb = dsa.getSelectedSchedBlock();
                Date d = sbExecutor.execute(sb, ev.getArray(), time);
                TimeHandler.getHandler().step(d);
                rc.notifySchedBlockStart(sb);
                // Create a new EventTime to check the SB execution termination
                // in the future
                TimeEvent sbEndEv = new TimeEvent();
                sbEndEv.setType(EventType.SCHEDBLOCK_EXECUTION_FINISH);
                sbEndEv.setSb(sb);
                sbEndEv.setArray(ev.getArray());
                sbEndEv.setTime(d);
                timesToCheck.add(sbEndEv);
                rc.notifySchedBlockStop(sb);
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
                dsa.selectCandidateSB(time);
                // System.out.println("After selectors " + new Date());
                // System.out.println("Before rankers " + new Date());
                dsa.rankSchedBlocks(time);
                // System.out.println("After rankers " + new Date());
                SchedBlock sb = dsa.getSelectedSchedBlock();
                Date d = sbExecutor.execute(sb, ev.getArray(), time);
                TimeHandler.getHandler().step(d);
                rc.notifySchedBlockStart(sb);
                // Create a new EventTime to check the SB execution termination
                // in the future
                TimeEvent sbEndEv = new TimeEvent();
                sbEndEv.setType(EventType.SCHEDBLOCK_EXECUTION_FINISH);
                sbEndEv.setSb(sb);
                sbEndEv.setArray(ev.getArray());
                sbEndEv.setTime(d);
                rc.notifySchedBlockStop(sb);
                timesToCheck.add(sbEndEv);
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
    
    private void crowdingReport(String ctxPath){
        ApplicationContext ctx = new FileSystemXmlApplicationContext("file://"+ctxPath);
        ReportGenerator reporter = new ReportGenerator((SchedBlockDao) ctx.getBean("sbDao"));
        reporter.printALMAReceiverBandReport();
        //System.out.println("");
        //reporter.printLSTRangesReport();
    	
    }
    
    private void finalreport(String ctxPath){
        ApplicationContext ctx = new FileSystemXmlApplicationContext("file://"+ctxPath);
        OutputDao outDao = (OutputDao) ctx.getBean("outDao");
        Results lastResult = outDao.getResults().get(outDao.getResults().size()-1 );
        
        URL xslt = getClass().getClassLoader()
                                .getResource("alma/scheduling/planning_mode_sim/reports/general_report.xsl");
        String xmlIn = workDir + "/" +
                       xmlConfigDao.getConfiguration().getOutputDirectory() + "/" + 
                       "output_" +
                       lastResult.getStartRealDate().getTime() + 
                       ".xml";
        String htmlOut = workDir + "/" + 
                        xmlConfigDao.getConfiguration().getReportDirectory() + "/" +
                        "report_" +
                        lastResult.getStartRealDate().getTime() + 
                        ".html";
        System.out.println("URL ofr xslt: " + xslt.toString());
        
        XsltTransformer.transform(xslt.toString(), xmlIn, htmlOut );
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
            whDao.setSimulationStartTime(time);;
        }
        DynamicSchedulingAlgorithm dsa = getDSA(ctx);
        System.out.println("Running first update " + new Date());
        dsa.updateModel(time);
        System.out.println("Finishing first update " + new Date());
    }
    
    private void listPh1mProposals(String ctx){
        Ph1mSynchronizer ph1m = new Ph1mSynchronizer();
        ph1m.init(ctx);
        ph1m.printReport();
    }
    
    private void synchPh1m(String ctx){
        Ph1mSynchronizer ph1m = new Ph1mSynchronizer();
        ph1m.init(ctx);
        try {
            ph1m.synchronizeAllProjects();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
    
    public void selectAction(String[] args) throws Exception{
        if (args.length==0){
            help();
            System.exit(1);
        }
        String tmpWorkDir[] = getOpt(args, "--working-dir");
        try{
            if(tmpWorkDir == null)
                throw new java.lang.IndexOutOfBoundsException();
            workDir = tmpWorkDir[1];
            File dir = new File(workDir);
            if (!dir.exists()){
                throw new IllegalArgumentException("Invalid working directory, directory doesn't exist");
            }
        }catch(java.lang.IndexOutOfBoundsException ex){
            workDir = System.getenv("APRC_WORK_DIR");
            if (workDir == null){
                File tmp = new File(".");
                workDir = tmp.getAbsolutePath();
                logger.debug( "Working directory is: " + workDir);
            }
        }
        System.out.println("Using directory: " + workDir);
        String[] verbose = getOpt(args, "-vvv");
        if(verboseLvl == null && verbose != null)
            verboseLvl = VerboseLevel.HIGH;
        verbose = getOpt(args, "-vv");
        if(verboseLvl == null && verbose != null)
            verboseLvl = VerboseLevel.MEDIUM;
        verbose = getOpt(args, "-v");
        if(verboseLvl == null && verbose != null)
            verboseLvl = VerboseLevel.LOW;
        if(verboseLvl == null)
            verboseLvl = VerboseLevel.NONE;
        System.out.println("Verbose Level: " + verboseLvl);
        Configuration config = xmlConfigDao.getConfiguration();
        if(args[0].compareTo("createWorkDir")==0){
            try{
                System.out.println("Creating Working directory structure");
                createWorkDir(workDir);
            }
            catch(java.lang.NullPointerException ex){
                help();
                System.exit(1);
            }
            catch(java.lang.IndexOutOfBoundsException ex){
                help();
                System.exit(1);
            }
        }
        else if (args[0].compareTo("fullload")==0){
            fullLoad(workDir + "/" +config.getContextFilePath());
        }
        else if (args[0].compareTo("load")==0){
            load(workDir + "/" +config.getContextFilePath());
        }
        else if (args[0].compareTo("unload")==0){
            unload(workDir + "/" +config.getContextFilePath());
        }
        else if (args[0].compareTo("clean")==0){
            clean(workDir + "/" +config.getContextFilePath());
        }
        else if (args[0].compareTo("run")==0){
            run(workDir + "/" + config.getContextFilePath());
        }
        else if (args[0].compareTo("step")==0){
            this.toBeInterrupted = true;
            run(workDir + "/" + config.getContextFilePath());
        }
        else if (args[0].compareTo("report")==0){
                if(args[1].compareTo("help") == 0)
                    reportHelp();
                else if(args[1].compareTo("1") == 0)
                    crowdingReport(workDir + "/" +config.getContextFilePath());
                else if(args[1].compareTo("2") == 0)
                    finalreport(workDir + "/" +config.getContextFilePath());
                else
                    reportHelp();
            
        }
        else if(args[0].compareTo("ph1m") == 0){
            if(args[1].compareTo("list") == 0)
                listPh1mProposals(workDir + "/" + config.getContextFilePath());
            else if(args[1].compareTo("sync") == 0)
                synchPh1m(workDir + "/" + config.getContextFilePath());
        }
        else if (args[0].compareTo("go")==0){
            System.out.println("I'm doing something useful 4");
        }
        else if (args[0].compareTo("help")==0){
            help();
        }
        else{
            help();
            System.exit(1);
        }
    }
    
    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        
        AprcTool cli = new AprcTool();
        cli.selectAction(args);
        // Wait until HSQLDB thread is finished to do his job
        // This case applies when the simulator is using HSQLDB in File mode
        if(Thread.activeCount() > 1){
            Thread.sleep(10000);
        }
        System.exit(0);
    }
    
    public void hello(){
        System.out.println("Hola");
        
    }

}
