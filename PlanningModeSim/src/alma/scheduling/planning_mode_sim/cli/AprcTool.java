package alma.scheduling.planning_mode_sim.cli;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import alma.scheduling.algorithm.DynamicSchedulingAlgorithm;
import alma.scheduling.algorithm.DynamicSchedulingAlgorithmImpl;
import alma.scheduling.algorithm.SchedBlockExecutor;
import alma.scheduling.algorithm.astro.TimeUtil;
import alma.scheduling.algorithm.modelupd.ModelUpdater;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.dataload.CompositeDataLoader;
import alma.scheduling.dataload.DataLoader;
import alma.scheduling.datamodel.config.Configuration;
import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.config.dao.ConfigurationDaoImpl;
import alma.scheduling.datamodel.config.dao.XmlConfigurationDaoImpl;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.observatory.dao.ObservatoryDao;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.output.dao.OutputDao;
import alma.scheduling.datamodel.output.dao.XmlOutputDaoImpl;
import alma.scheduling.datamodel.weather.dao.WeatherHistoryDAO;
import alma.scheduling.planning_mode_sim.EventType;
import alma.scheduling.planning_mode_sim.TimeEvent;
import alma.scheduling.planning_mode_sim.controller.ResultComposer;
import alma.scheduling.planning_mode_sim.controller.TimeHandler;


public class AprcTool {
    
    private static Logger logger = LoggerFactory.getLogger(AprcTool.class);
    
    private class Runner implements Runnable {

        private ApplicationContext ctx;
        private Date time;
        private DynamicSchedulingAlgorithm dsa;
        private ArrayConfiguration arrCnf;
        private SchedBlockExecutor sbExecutor;
        private ResultComposer rc;
        
        public Runner(ApplicationContext ctx, Date time,
                DynamicSchedulingAlgorithm dsa, ArrayConfiguration arrCnf,
                SchedBlockExecutor sbExecutor, ResultComposer rc) {
            this.ctx = ctx;
            this.time = time;
            this.dsa = dsa;
            this.arrCnf = arrCnf;
            this.sbExecutor = sbExecutor;
            this.rc = rc;
            rc.notifyArrayCreation(arrCnf);
        }

        @Override
        public void run() {
            while(true)
                try {
                    logger.info("updating DB");
                    update(ctx, time);
                    logger.info("selecting candidate SBs");
                    dsa.selectCandidateSB();
                    dsa.rankSchedBlocks();
                    SchedBlock sb = dsa.getSelectedSchedBlock();
                    time = sbExecutor.execute(sb, arrCnf, time);
                    rc.notifySchedBlockStart(sb);
                } catch (NoSbSelectedException e) {
                    System.out.println("DSA for array " + arrCnf.getId().toString() + " finished -- No more suitable SBs to be scheduled");
                    return;
                }            
        }
        
    }
    
    private String workDir;
    
    private ConfigurationDao xmlConfigDao = new XmlConfigurationDaoImpl();
      
    private void help(){
        System.out.println("APRC Tool Command Line Interface");
        System.out.print("Usage: ");
        System.out.println("AprcTool <command> [options]");
        System.out.println("\nList of Commands:\n");
        System.out.println("createWorkDir:\t Creates a template for the work directory");
        System.out.println("load:\t\t loads the database with the data stored in the XML files");
        System.out.println("run:\t\t runs a simulation, generating an output file");
        System.out.println("go:\t\t loads and run a simulation");
        System.out.println("help:\t\t Display this helpful message");
        System.out.println("\n\noptions:");
        System.out.println("--working-dir=[path]\t set working path, override the APRC_WORK_DIR " +
        		"environment variable. By default is APRC_WORK_DIR environment variable if it is " +
        		"available, otherwise .");
    }
    
    public String[] getOpt(String[] argv, String param){
        for(int i = 0; i<argv.length; i++){
            if (argv[i].contains(param)){
                if(argv[i].contains("="))
                    return argv[i].split("=");
                String retval[] = new String[2];
                retval[0]=argv[i];
                retval[1]=argv[i+1];
                return retval;
            }
        }
        return null;
    }
    
    
    private void createWorkDir(String path) throws IOException{
        File entries[] = new File[6];
        entries[0] = new File(path + "/db");
        entries[1] = new File(path + "/projects");
        entries[2] = new File(path +"/weather");
        entries[3] = new File(path + "/observatory");
        entries[4] = new File(path + "/executives");
        entries[5] = new File (path + "/output");
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
    
    private void fullLoad(String ctxPath) {
        ApplicationContext ctx = new FileSystemXmlApplicationContext("file://"+ctxPath);
        String[] loadersNames = ctx.getBeanNamesForType(CompositeDataLoader.class);
        String [] cfgBeans = ctx.getBeanNamesForType(ConfigurationDaoImpl.class);
        if(cfgBeans.length == 0){
            System.out.println(ctxPath + " file doesn't contain a bean of the type lma.scheduling.datamodel.config.dao.ConfigurationDaoImpl");
            System.exit(1);
        }
        for(int i = 0; i < loadersNames.length; i++){
            DataLoader loader = (DataLoader) ctx.getBean(loadersNames[i]);
            loader.load();
        }
        ConfigurationDao configDao = (ConfigurationDao) ctx.getBean(cfgBeans[0]);
        configDao.updateConfig();
    }

    private void load(String ctxPath) {
        ApplicationContext ctx = new FileSystemXmlApplicationContext("file://"+ctxPath);
        String [] cfgBeans = ctx.getBeanNamesForType(ConfigurationDaoImpl.class);
        DataLoader loader = (DataLoader) ctx.getBean("fullDataLoader");
        loader.load();
        ConfigurationDao configDao = (ConfigurationDao) ctx.getBean(cfgBeans[0]);
        configDao.updateConfig();
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
    
    private void run(String ctxPath){
    	TimeHandler.initialize(TimeHandler.Type.REAL);
        ApplicationContext ctx = new FileSystemXmlApplicationContext("file://"+ctxPath);
        //Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UT"));
        ExecutiveDAO execDao = (ExecutiveDAO) ctx.getBean("execDao");
       // Date time = calendar.getTime(); // initial time is now, it should be configurable
        Date time = execDao.getCurrentSeason().getStartDate(); //The start time is the start Time of the current Season
        Date stopTime = execDao.getCurrentSeason().getEndDate();
        setPreconditions(ctx, new Date());
        String[] dsaNames = ctx.getBeanNamesForType(DynamicSchedulingAlgorithmImpl.class);
        if(dsaNames.length == 0)
            throw new IllegalArgumentException("There is not a Dynamic Scheduling Algorithm bean defined in the context.xml file");
        if(dsaNames.length > 1)
            throw new IllegalArgumentException("There are more than 1 Dynamic Scheduling Algorithm Beans defined in the context.xml file");
        
        SchedBlockExecutor sbExecutor =
            (SchedBlockExecutor) ctx.getBean("schedBlockExecutor");
        ObservatoryDao observatoryDao = (ObservatoryDao) ctx.getBean("observatoryDao");
        
        ResultComposer rc = new ResultComposer();
      
        //This is the timeline
        LinkedList<TimeEvent> timesToCheck = new LinkedList<TimeEvent>();
        
        //This will contains all the arrays and the DSA associated to the array
        //that can be used in the current simulation time
        Hashtable<ArrayConfiguration,DynamicSchedulingAlgorithm> arraysCreated =
            new Hashtable<ArrayConfiguration, DynamicSchedulingAlgorithm>();
        
        //The arrays created which are free (not running an sb)
        //ArrayList<ArrayConfiguration> freeArrays= new ArrayList<ArrayConfiguration>(); 
        
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
        
        /* Check the current time and discard the older events*/
        while(timesToCheck.size() > 0){
            if(timesToCheck.getFirst().getTime().before(time))
                timesToCheck.remove();
            else
                break;
        }
        
        /*Stop when there is not more events to schedule
         * TODO: Execute the events until end of season*/
        while(timesToCheck.size() > 0){
            TimeEvent ev = timesToCheck.remove();
            logger.info("updating DB");
            update(ctx, time);
            //Change the current simulation time to event time
            time = ev.getTime();
            switch (ev.getType()){
            case ARRAY_CREATION:
                DynamicSchedulingAlgorithm dsa;
                logger.info("Simulated Time [" + time.toString() + 
                        "] Array " + ev.getArray().getId() + " created");
                rc.notifyArrayCreation(ev.getArray());
                dsa = (DynamicSchedulingAlgorithm) ctx.getBean(dsaNames[0]);
                dsa.setArray(ev.getArray());
                arraysCreated.put(ev.getArray(), dsa);
                logger.info("selecting candidate SBs for Array: " + ev.getArray().getId());
                try{
                    dsa.selectCandidateSB(time);
                    dsa.rankSchedBlocks();
                    SchedBlock sb = dsa.getSelectedSchedBlock();
                    Date d = sbExecutor.execute(sb, ev.getArray(), time);
                    rc.notifySchedBlockStart(sb);
                    //Create a new EventTime to check the SB execution termination in the future
                    TimeEvent sbEndEv = new TimeEvent();
                    sbEndEv.setType(EventType.SCHEDBLOCK_EXECUTION_FINISH);
                    sbEndEv.setSb(sb);
                    sbEndEv.setArray(ev.getArray());
                    sbEndEv.setTime(d);
                    timesToCheck.add(sbEndEv);
                } catch (NoSbSelectedException ex){
                    System.out.println("DSA for array " + ev.getArray().getId().toString() + " finished -- No more suitable SBs to be scheduled");
                    //freeArrays.add(ev.getArray());
                }
                break;
            case ARRAY_DESTRUCTION:
                //notify the destruction??
                logger.info("Simulated Time[" + time.toString() + 
                        "] Array " + ev.getArray().getId() + " deleted");
                arraysCreated.remove(ev.getArray());
                //freeArrays.remove(ev.getArray());
                break;
            case SCHEDBLOCK_EXECUTION_FINISH:
                dsa = arraysCreated.get(ev.getArray());
                logger.info("Simulated Time[" + time.toString() + 
                        "] selecting candidate SBs for Array: " + ev.getArray().getId());
                try{
                    //The array is free now it could be scheduled a new SB
                    dsa.selectCandidateSB(time);
                    dsa.rankSchedBlocks();
                    SchedBlock sb = dsa.getSelectedSchedBlock();
                    Date d = sbExecutor.execute(sb, ev.getArray(), time);
                    rc.notifySchedBlockStart(sb);
                    //Create a new EventTime to check the SB execution termination in the future
                    TimeEvent sbEndEv = new TimeEvent();
                    sbEndEv.setType(EventType.SCHEDBLOCK_EXECUTION_FINISH);
                    sbEndEv.setSb(sb);
                    sbEndEv.setArray(ev.getArray());
                    sbEndEv.setTime(d);
                    timesToCheck.add(sbEndEv);
                } catch (NoSbSelectedException ex){
                    System.out.println("DSA for array " + ev.getArray().getId().toString() + " No suitable SBs to be scheduled");
                    //freeArrays.add(ev.getArray());
                }
                break;
                /*TODO: To Consider the case when an array is free
            case FREE_ARRAY:
                dsa = arraysCreated.get(ev.getArray());
                //removing from free list
                freeArrays.remove(ev.getArray());
                logger.info("Simulated Time[" + time.toString() + 
                        "] selecting candidate SBs for Array: " + ev.getArray().getId());
                try{
                    dsa.selectCandidateSB(time);
                    dsa.rankSchedBlocks();
                    SchedBlock sb = dsa.getSelectedSchedBlock();
                    Date d = sbExecutor.execute(sb, ev.getArray(), time);
                    rc.notifySchedBlockStart(sb);
                    //Create a new EventTime to check the SB execution termination in the future
                    TimeEvent sbEndEv = new TimeEvent();
                    sbEndEv.setType(EventType.SCHEDBLOCK_EXECUTION_FINISH);
                    sbEndEv.setSb(sb);
                    sbEndEv.setArray(ev.getArray());
                    sbEndEv.setTime(d);
                    timesToCheck.add(sbEndEv);
                } catch (NoSbSelectedException ex){
                    System.out.println("DSA for array " + ev.getArray().getId().toString() + " No suitable SBs to be scheduled");
                    freeArrays.add(ev.getArray());
                }*/
            }
            /*
            //If there are free arrays. Check in 10 mins more for a suitable SB candidate
            if(freeArrays.size() > 0){
                //Check in 10 mins more of the simulated time
                Date next = new Date(time.getTime() + (10 * 60 * 1000 ));
                for(ArrayConfiguration a: freeArrays){
                    TimeEvent freeEv = new TimeEvent();
                    freeEv.setArray(a);
                    freeEv.setTime(next);
                    freeEv.setType(EventType.FREE_ARRAY);
                    timesToCheck.add(freeEv);
                }
            }*/
            //Sort in ascending order the timeline
            Collections.sort(timesToCheck);
        }   
        
        rc.completeResults();
        //Saving results to DB and XML output file
        XmlOutputDaoImpl xmlOutDao = new XmlOutputDaoImpl();
        xmlOutDao.setConfigDao(xmlConfigDao);
        xmlOutDao.saveResults( rc.getResults() );
        OutputDao outDao = (OutputDao) ctx.getBean("outDao");
        outDao.saveResults( rc.getResults() );
        
    }
    
    private void update(ApplicationContext ctx, Date time) {
        String[] updaters = ctx.getBeanNamesForType(ModelUpdater.class);
        for(int i = 0; i < updaters.length; i++) {
            ModelUpdater updater = (ModelUpdater) ctx.getBean(updaters[i]);
            if (updater.needsToUpdate(time)) {
                updater.update(time);
            }
        }
    }
    
    private void setPreconditions(ApplicationContext ctx, Date time) {
        String[] whDaos = ctx.getBeanNamesForType(WeatherHistoryDAO.class);
        for(int i = 0; i < whDaos.length; i++) {
            WeatherHistoryDAO whDao = (WeatherHistoryDAO) ctx.getBean(whDaos[i]);
            whDao.setSimulationStartTime(time);;
        }
    }
    
    public void selectAction(String[] args) throws IOException{
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
                throw new IllegalArgumentException("Invalid working directory, directory doesn't exist", null);
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
        else if (args[0].compareTo("clean")==0){
            clean(workDir + "/" +config.getContextFilePath());
        }
        else if (args[0].compareTo("run")==0){
            run(workDir + "/" + config.getContextFilePath());
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
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        
        AprcTool cli = new AprcTool();
        cli.selectAction(args);
        System.exit(0);
 
    }
    
    public void hello(){
        System.out.println("Hola");
        
    }

}
