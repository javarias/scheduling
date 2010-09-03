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

package alma.scheduling.psm.cli;

import java.io.File;
import java.rmi.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import alma.scheduling.algorithm.VerboseLevel;
import alma.scheduling.psm.sim.InputActions;
import alma.scheduling.psm.sim.ReportGenerator;
import alma.scheduling.psm.sim.Simulator;
import alma.scheduling.psm.util.Ph1mSynchronizer;
import alma.scheduling.psm.util.Ph1mSynchronizerImpl;
import alma.scheduling.psm.util.PsmContext;

public class Console {

    private static Logger logger = LoggerFactory.getLogger(Console.class);
    private java.io.Console systemConsole = null;
    private static final String prompt ="> ";
    private static Console console = null;
    private static boolean requestedExit = false;
    private String workDir;
    private VerboseLevel verboseLvl = null;
    
    private Console(){
        systemConsole = System.console();
    }
    
    public static Console getConsole(){
        if(console == null)
            return new Console();
        return console;
    }
    
    public void run(String[] args){
    	workDir = parseWorkDir(args);
    	verboseLvl = parseVerboseLevel(args);
    	try {
			selectAction(args);
		} catch (IllegalArgumentException e) {
			logger.error("Specified working directory does not exist. Please specify a correct working directory.");
			e.printStackTrace();
			System.exit(4); // Exit code 4: Specified working directory does not exist
		}
    }
    
    private void selectAction(String[] args) throws IllegalArgumentException{
    	//TODO Obtain context.xml location properly
    	PsmContext.setApplicationContext( new FileSystemXmlApplicationContext( "file:///" + workDir + "/context.xml") );
    	
        if(args[0].compareTo("createWorkDir") == 0){
        	Simulator simulator = new Simulator( workDir );
        	simulator.setVerboseLvl(verboseLvl);
        	simulator.createWorkDir(workDir);
        }
        else if (args[0].compareTo("fullload") == 0){
        	InputActions inputActions = InputActions.getInstance(workDir);
        	inputActions.setVerboseLvl(verboseLvl);
        	inputActions.fullLoad();
        }        	
        else if (args[0].compareTo("load") == 0){
        	InputActions inputActions = InputActions.getInstance(workDir);
        	inputActions.setVerboseLvl(verboseLvl);
        	inputActions.load();
        }
        else if (args[0].compareTo("unload") == 0){
        	InputActions inputActions = InputActions.getInstance(workDir);
        	inputActions.setVerboseLvl(verboseLvl);
        	inputActions.unload();
        }
        else if (args[0].compareTo("clean") == 0){
        	InputActions inputActions = InputActions.getInstance(workDir);
        	inputActions.setVerboseLvl(verboseLvl);
        	inputActions.clean();
        }
        else if (args[0].compareTo("run") == 0){
        	Simulator simulator = new Simulator( workDir );
        	simulator.setVerboseLvl(verboseLvl);
        	simulator.run();
        }
        else if (args[0].compareTo("step") == 0){
        	Simulator simulator = new Simulator( workDir );
        	simulator.setVerboseLvl(verboseLvl);
        	simulator.setToBeInterrupted(true);
        	simulator.run();
        }
        else if (args[0].compareTo("remoteFullLoad") == 0){
        	InputActions inputActions = InputActions.getInstance(workDir);;
        	inputActions.setVerboseLvl(verboseLvl);
        	inputActions.remoteFullLoad();
        }
        else if (args[0].compareTo("remoteLoad") == 0){
        	InputActions inputActions = InputActions.getInstance(workDir);
        	inputActions.setVerboseLvl(verboseLvl);
        	inputActions.remoteLoad();
        }
        else if (args[0].compareTo("report")==0){
        	ReportGenerator reportGenerator = new ReportGenerator( workDir );
        		if(args.length == 1)
        			reportHelp();
        		else if(args[1].compareTo("help") == 0)
                    reportHelp();
                else if(args[1].compareTo("1") == 0)
                	reportGenerator.crowdingReport();
                else if(args[1].compareTo("2") == 0)
                	reportGenerator.finalreport();
                else if(args[1].compareTo("3") == 0)
                	reportGenerator.printLSTRangesReport();
                else if(args[1].compareTo("4") == 0)
                	reportGenerator.executiveReport();
                else if(args[1].compareTo("5") == 0)
                	reportGenerator.bandUsageReport();
                else if(args[1].compareTo("6") == 0)
                	reportGenerator.lstRangeBeforeSimReport();
                else if(args[1].compareTo("7") == 0)
                	reportGenerator.lstRangeAfterSimReport();
                else
                    reportHelp();
            
        }
        else if(args[0].compareTo("ph1m") == 0){
            if(args[1].compareTo("list") == 0){
            	Ph1mSynchronizer ph1mSychronizer = new Ph1mSynchronizerImpl(workDir); 
            	try {
					ph1mSychronizer.listPh1mProposals();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }                
            else if(args[1].compareTo("sync") == 0){
            	Ph1mSynchronizer ph1mSychronizer = new Ph1mSynchronizerImpl(workDir); 
            	try {
					ph1mSychronizer.synchPh1m();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
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
	
    private void help(){
        System.out.println("APRC Tool Command Line Interface");
        System.out.println("Usage: ");
        System.out.println("AprcTool <command> [options]");
        System.out.println("\nList of Commands:");
        System.out.println("createWorkDir:\t Creates a template for the work directory.");
        System.out.println("load:\t\t loads the database with the data stored in the XML files.");
        // TODO: What does this command do?
        System.out.println("unload:\t\t ");
        System.out.println("clean:\t\t unload from database obsproject, executive, results, and observatory data.");
        System.out.println("step:\t\t step through each cycle of simulation, returning to command prompt.");
        System.out.println("run:\t\t runs a simulation, generating an output file.");
        System.out.println("go:\t\t loads and run a simulation.");
        System.out.println("help:\t\t Display this helpful message.");
        System.out.println("report <help, 1, 2>:\t\t Generate reports from the results of the simulation.");
        System.out.println("\nList of Options:");
        System.out.println("--working-dir=[path]\t set working path, override the APRC_WORK_DIR " +
        		"environment variable. By default is APRC_WORK_DIR environment variable if it is " +
        		"available, otherwise .");
        System.out.println("--vvv: \t\t High verbosity level.");
        System.out.println("--vv: \t\t Medium verbosity level.");
        System.out.println("--v: \t\t Low verbosity level.");

    }
    
    private void reportHelp(){
        System.out.println("Reports help");
        System.out.println("Usage: ");
        System.out.println("aprc report <command>");
        System.out.println("\nList of Commands:");
        System.out.println("1:\tGenerate statistics report containing SBs per ALMA Receiver Bands.");
        System.out.println("2:\tGenerate result report (after run the simulation).");
        System.out.println("3:\tGenerate statistics report containing SBs per LST ranges.");
        System.out.println("help:\tShow this help message.");
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
    
    private VerboseLevel parseVerboseLevel(String[] args){
    	VerboseLevel verboseLvl = null;
    	
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
        
        logger.info("Verbose Level: " + verboseLvl);
        
        return verboseLvl;
    }
    
    private String parseWorkDir(String[] args){
    	logger.trace("Determining Working Directory");
    	
    	String workDir = null;
    	
    	// No arguments leads to exit the program and show help screen.
        if ( args.length == 0 ){
            help();
            System.exit(1);	// Exit code 1: No arguments
        }
        
        // Parse the working directory option. If not present, use environment variable.
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
        logger.debug("Using directory: " + workDir );
        
        return workDir;
    }
    
    public void activate( Simulator simulator){
        requestedExit = false;
        while(!requestedExit){
            systemConsole.printf(prompt, new Object[0]);
            interpret(systemConsole.readLine(), simulator);
        }
    }
    
    private void interpret(String line, Simulator simulator){
        String[] lineParams = line.split(" ");
        if(lineParams[0].equals("exit"))
            System.exit(0);
        else if (lineParams[0].equals("step")){
        	simulator.setToBeInterrupted(true);
            requestedExit = true;
        }
        else if(lineParams[0].equals("run")){
        	simulator.setToBeInterrupted(false);
            requestedExit = true;
        }
    }
 
//    public static void main(String[] args){
//        Console console= Console.getConsole(null);
//        console.activate();
//    }
}
