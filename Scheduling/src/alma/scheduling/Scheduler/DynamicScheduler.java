/*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * All rights reserved
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 * 
 * File DynamicScheduler.java
 * 
 */
package alma.scheduling.Scheduler;


import alma.scheduling.Define.SB;
import alma.scheduling.Define.BestSB;
import alma.scheduling.Define.Clock;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.SBQueue;
import alma.scheduling.Define.ObservedSession;
import alma.scheduling.Define.SchedulingException;
import alma.scheduling.MasterScheduler.Message;
import alma.scheduling.MasterScheduler.Message;
import alma.scheduling.Scheduler.DSA.DynamicSchedulingAlgorithm;
//import alma.scheduling.GUI.InteractiveSchedGUI.GUIController;

import java.util.logging.Logger;

/**
 * DynamicScheduler.java
 * 
 * The Scheduler class is the major controlling class in the 
 * scheduler package.  See Scheduling Subsystem Design document, 
 * section 3.2.3.
 * 
 * @version 1.10 March 4, 2004
 * @author Allen Farris
 *
 */
public class DynamicScheduler extends Scheduler implements Runnable {
	
    public DynamicScheduler(SchedulerConfiguration config) {
        super(config);
    }
    
    /**
     * Form a name of this scheduler, which includes its thread name and the id
     * of the subarray on which it operates.  This name has the folloeing form:
     * 		Scheduler [task1] (subarray 1)
     * @return A string identifying this scheduler.
     */
    protected String name() {
    	return "Scheduler [" + Thread.currentThread().getName() + 
			"] (subarray " + subarrayId + ")";
    }
    
    /**
     * Once the scheduler thread is started we decide here which
     * mode to run, Dynamic or Interactive.
     */
    public void run() {
        logger.info("SCHEDULING: Running in DYNAMIC mode");
    	config.setTask(Thread.currentThread());
        runDynamic();
    }
        
    
    /**
     * If in dynamic mode, this method validates the configuration file
     * for an dynamic session.
     * @return String Returns null if there were no errors. If errors in 
     *                validating occured the error string would be returned.
     */
    private String validateDynamicConfig() {
    	if (config.getControl() == null)
    		return name() + ": There is no control component.";
    	if (config.getOperator() == null)
    		return name() + ": There is no operator component.";
    	if (config.getTelescope() == null)
    		return name() + ": There is no telescope model component.";
    	if (config.getProjectManager() == null)
    		return name() + ": There is no project manager component.";
    	if (config.getPolicy() == null)
    		return name() + ": There is no scheduling policy.";
    	SBQueue queue = config.getQueue();
    	if (queue.size() == 0)
    		return name() + ": There are no scheduling units in the queue";
    	int n = config.getBestNumber();
    	if (n <= 0)
    		return name() + ": Invalid value of number of units in the \"best\" list (" + n + ")";
    	n = config.getSleepTime();
    	if (n < 0)
    		return name() + ": Invalid value of sleep time (" + n + ")";
    	if (!config.isSynchronous())
    		return name() + ": The asynchronous mode is not implemented "+
                "at this time.";
    	return null;
    }
    
    /**
     * Not yet implemented.. 
     */
    private void checkRunning() {
    	// TODO Check to see if anything is running and, if so, stop it.
    }
    
    /**
     * Run the scheduler in dynamic mode.
     *
     */
    public void runDynamic() {
    	System.out.println(name() + " is running in dynamic mode!");
     	
    	// Validate the configuration object.
    	String msg = validateDynamicConfig();
    	if (msg != null) {
    		config.errorEnd(msg,clock.getDateTime());
    		logger.severe("SCHEDULING: "+name() + ": Configuraton error! " + msg);
    		return;
    	}
    	logger.info("SCHEDULING: "+name() + ": Configuration object validated.");
    	
    	// Create the dynamic scheduling algorithm object.
    	try {
    		dsa = new DynamicSchedulingAlgorithm(
    				subarrayId, config.getPolicy(), config.getQueue(), clock,
					config.getTelescope(), config.getProjectManager(), logger, 
                    config.getBestNumber());
                    
    	} catch (SchedulingException err) {
    		config.errorEnd(err.toString(),clock.getDateTime());
    		logger.severe("SCHEDULING: "+name() + ": Error creating dynamic scheduling "+
                "algorithm ! " + err.toString());
    		return;
    	}
    	logger.info("SCHEDULING: "+name() + ": Dynamic scheduling algorithm created.");
    	
    	// Set the start and end times.  (The ending time may be null.)
    	DateTime start = clock.getDateTime();
    	DateTime end = config.getCommandedEndTime();
    	config.start(start,end);
    	logger.info("SCHEDULING: "+name() + ": Started " + start);
    	
    	// Go into the major run-time loop.
    	// We'll need the following variables in the loop.
    	DateTime now = null;
    	long sleepTime = config.getSleepTime() * 1000;
    	boolean synchronous = config.isSynchronous();
    	while (true) {
    		config.sleepingOff();
    		
    		// 1. Check to see if the master scheduler told us to stop.
    		if (config.isStopFlag()) {
    			checkRunning();
    			logger.info("SCHEDULING: "+name() + ": Stopping because stop flag is set.");
    			break;
    		}
    		
    		// 2. Check to see if we are at the end of this scheduling period.
    		end = config.getCommandedEndTime();
    		if (end != null && !end.isNull()) {
    			now = clock.getDateTime();
    			if (now.ge(end)) {
    				checkRunning();
    				logger.info("SCHEDULING: "+name() + ": Stopping because we are at the end "+
                        "of the scheduling period.");
					break;
    			}
    		}

    		// 3. Check to see if there are any scheduling units in the queue.
    		if (config.getQueue().size() == 0) {
    			checkRunning();
    			logger.info("SCHEDULING: "+name() + ": Stopping because there are no more "+
                    "scheduling units.");
                logger.info("SCHEDULING: Nothing Can Be Scheduled Event sent out, in scheduler.");
                //config.getSchedulingPublisher().publish("No more SBs to schedule");

                //NothingCanBeScheduled.NoResources
				break;
    		}
    		
    		// 4. Do the required actions, which are mode dependent.
    		try {
    			if (synchronous) {
    				if (synchronousMode()) {
                        break;
                    } //else do nothing
    			} else {
    				if (asynchronousMode()) {
    					break;
                    }
    			}
    		} catch (SchedulingException err) {
    			logger.severe("SCHEDULING: "+err.toString());
    			config.errorEnd(err.toString(),clock.getDateTime());
    			return;
    		}
    		
    		// 5. Sleep for a time, if we are required to do so.
    		if (sleepTime > 0) {
    			try {
    				config.sleepingOn();
    				Thread.sleep(sleepTime);
                    System.out.println("Sleeping..");
    			} catch (InterruptedException err) {
                    System.out.println("Sleeping interrupted!");
    			}
    		}
    	};
    	
    	config.normalEnd(clock.getDateTime());
    	System.out.println(name() + " has ended!");
    	System.out.println(name() + " started " + config.getActualStartTime());
    	System.out.println(name() + " ended " + config.getActualEndTime());
    }

    /**
     * Perform the required actions in synchronous mode.
     * @return true if we are stopping this thread; otherwise return false 
     *              to continue.
     * @throws SchedulingException
     */
    private boolean synchronousMode() throws SchedulingException {
    	DateTime now = clock.getDateTime();
    	
    	// Get the best list from the dsa.
    	BestSB best = dsa.getBest();
        //BestSB best = null;
    	
    	if (best == null) {
    		// There will be no best list if there is nothing left to schedule.
            logger.info("SCHEDULING: "+name()+" nothing left to schedule. BestSB == null");
    		return true;
    	}
    	
    	// Log the best list.
    	logger.info("SCHEDULING: ** "+name() + ": " + best.toString());
        // create a message to correspond to this selectSB request
        // Submit the list to the operator to get the id of the best SB from 
        //the list
        //String bestSBId = config.getOperator().selectSB(best, m);
        System.out.println("SCHEUDLING: best selection will be.. "+ best.getBestSelection());
        if(best.getBestSelection() == null) {
    		// Nothing can be scheduled at this time.
    		// Call config's nothingToDo method and wait.
    		config.nothingToDo(best.getNothingCanBeScheduled());
    		// Get response.
    		switch (config.getAction()) {
     		    case SchedulerConfiguration.CONTINUE:
                    logger.info("SCHEDULING: continue");
    			    return false;
        		case SchedulerConfiguration.STOP:
                    logger.info("SCHEDULING: stop");
        			return true;
    	    	case SchedulerConfiguration.FILLER:
    		    	logger.severe("SCHEDULING: "+name() + ": Invalid action response! Fillers "+
                        "cannot run in synchronous mode.");
        			throw new SchedulingException("Invalid action response! "+
                        "Fillers cannot run in synchronous mode.");
    	    	case SchedulerConfiguration.SB:
    		    	logger.severe("SCHEDULING: "+name() + ": Invalid action response! SBs cannot "+
                        "run at this time.");
        			throw new SchedulingException("Invalid action response! SBs "+
                        "cannot run at this time.");
    	    	default:
    		    	logger.severe("SCHEDULING: "+name() + ": Invalid action parameter! (" 
                        + config.getAction() + ")"); 
        			throw new SchedulingException("Invalid action parameter! (" 
                        + config.getAction() + ")"); 
    	    }
    	} else {
            try {
                Message m = new Message();
                config.getOperator().selectSB(best, m);
        		// We've got somthing to schedule.
                SB selectedSB = config.getQueue().get(best.getBestSelection());
                if(selectedSB.getStatus().isReady() ){ //&& selectedSB.getStartTime() == null) {
                    logger.info("SCHEDULING: About to schedule sb = "+selectedSB.getId());
                    selectedSB.setStartTime(new DateTime(System.currentTimeMillis()));
                    logger.info("SB is now "+selectedSB.getStatus().getStatus());
                    //TODO When Lindsey says so ;)
                    //ObservedSession session = 
                    //    config.getProjectManager().createObservedSession(
                    //            selectedSB.getParent());
                    //config.getProjectManager().sendStartSessionEvent(session);
      		        config.getControl().execSB(config.getSubarrayId(), best);
                } else {
                    logger.info("SCHEDULING: SB is not ready to be executed.");
                    //do something else here eventually...
                }
            } catch (Exception e) {
                //clear queue! 
                config.getQueue().clear();
                config.getProjectManager().getProjectManagerTaskControl().interruptTask();
                logger.severe("SCHEDULING: error!");
                e.printStackTrace();
                return true;
            }
    		logger.info("SCHEDULING: "+name() + ": executing " + best.getBestSelection());
    	}
   	
    	return false;
    }
    
    /**
     * Perform the required actions in asynchronous mode.
     * Not implemented yet so a SchedulingException is always thrown!
     *
     * @throws SchedulingException
     */
    private boolean asynchronousMode() throws SchedulingException {
    	throw new SchedulingException(name() + ": The asycronous mode is not "+
            "implemented at this time!");    	
    }
    
}
