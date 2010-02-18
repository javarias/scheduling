package alma.scheduling.planning_mode_sim.cli;

import java.io.File;
import java.util.ArrayList;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import alma.scheduling.algorithm.DynamicSchedulingAlgorithm;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.datamodel.obsproject.SchedBlock;


public class AprcTool {

    
    private static void help(){
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
        System.out.println("--working-dir=[path]\t set working path");
    }
    
    public static String[] getOpt(String[] argv, String param){
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
    
    
    private static void createWorkDir(String path){
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
    }
    
    private static void run(String ctxPath){
        run(ctxPath, "dsa");
    }
    
    private static void run(String ctxPath, String DSABeanName){
        ApplicationContext ctx = new ClassPathXmlApplicationContext(ctxPath);
        DynamicSchedulingAlgorithm dsa = (DynamicSchedulingAlgorithm) ctx.getBean(DSABeanName);
        ArrayList<SchedBlock> sbs = new ArrayList<SchedBlock>();
        while(true)
            try {
                dsa.selectCandidateSB();
                dsa.rankSchedBlocks();
                sbs.add(dsa.getSelectedSchedBlock());
            } catch (NoSbSelectedException e) {
                System.out.println("DSA finished -- No more suitable SBs to be scheduled");
                return;
            }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length==0){
            help();
            System.exit(1);
        }
        if(args[0].compareTo("createWorkDir")==0){
            try{
                System.out.println("Creating Working directory structure");
                String workDir[] = getOpt(args, "--working-dir");
                if(workDir[1] == null)
                    createWorkDir(".");
                createWorkDir(workDir[1]);
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
        else if (args[0].compareTo("load")==0){
            System.out.println("I'm doing something useful 2");
        }
        else if (args[0].compareTo("run")==0){
            System.out.println("I'm doing something useful 3");
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
        System.exit(0);
    }

}
