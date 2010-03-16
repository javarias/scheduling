package alma.scheduling.planning_mode_sim.cli;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import alma.scheduling.algorithm.modelupd.ModelUpdater;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.dataload.CompositeDataLoader;
import alma.scheduling.dataload.DataLoader;
import alma.scheduling.datamodel.config.Configuration;
import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.config.dao.ConfigurationDaoImpl;
import alma.scheduling.datamodel.config.dao.XmlConfigurationDaoImpl;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.output.dao.OutputDao;
import alma.scheduling.datamodel.output.dao.OutputDaoImpl;
import alma.scheduling.datamodel.weather.dao.WeatherHistoryDAO;
import alma.scheduling.output.MasterReporter;
import alma.scheduling.output.Reporter;
import alma.scheduling.planning_mode_sim.controller.ResultComposer;


public class AprcTool {
    
    private static Logger logger = LoggerFactory.getLogger(AprcTool.class);
    
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
            + " file doesn't contain a bean of the type lma.scheduling.datamodel.config.dao.ConfigurationDaoImpl");
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
        ApplicationContext ctx = new FileSystemXmlApplicationContext("file://"+ctxPath);
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UT"));
        Date time = calendar.getTime();
        setPreconditions(ctx, new Date());
        String[] dsaNames = ctx.getBeanNamesForType(DynamicSchedulingAlgorithmImpl.class);
        if(dsaNames.length == 0)
            throw new IllegalArgumentException("There is not a Dynamic Scheduling Algorithm bean defined in the context.xml file");
        if(dsaNames.length > 1)
            throw new IllegalArgumentException("There are more than 1 Dynamic Scheduling Algorithm Beans defined in the context.xml file");
        DynamicSchedulingAlgorithm dsa = (DynamicSchedulingAlgorithm) ctx.getBean(dsaNames[0]);
        String[] masterReporterNames = ctx.getBeanNamesForType(MasterReporter.class);
        ResultComposer rc = new ResultComposer();
        // ArrayList<SchedBlock> sbs = new ArrayList<SchedBlock>();
        SchedBlockExecutor sbExecutor =
            (SchedBlockExecutor) ctx.getBean("schedBlockExecutor");
        while(true)
            try {
                logger.info("updating DB");
                update(ctx, time);
                logger.info("selecting candidate SBs");
                dsa.selectCandidateSB();
                dsa.rankSchedBlocks();
                SchedBlock sb = dsa.getSelectedSchedBlock();
                // sbs.add(sb);
                time = sbExecutor.execute(sb, time);
                for(int i = 0; i < masterReporterNames.length; i++){
                    Reporter rep = (Reporter) ctx.getBean(masterReporterNames[i]);
                    rep.report(sb);
                }
                rc.notifySchedBlockStart(sb);
            } catch (NoSbSelectedException e) {
            	OutputDao outDao = new OutputDaoImpl();
            	outDao.saveResults( rc.getResults() );
                System.out.println("DSA finished -- No more suitable SBs to be scheduled");
                return;
            }
       
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
