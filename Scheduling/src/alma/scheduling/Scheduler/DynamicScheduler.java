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

import java.util.logging.Logger;

/**
 * DynamicScheduler.java
 * 
 * The Scheduler class is the major controlling class in the 
 * scheduler package.  See Scheduling Subsystem Design document, 
 * section 3.2.3.
 * 
 * @version $Id: DynamicScheduler.java,v 1.21 2007/09/12 21:22:38 sslucero Exp $
 * @author Allen Farris
 *
 */
public class DynamicScheduler extends Scheduler implements Runnable {
	private int runNum=0;
    public DynamicScheduler(SchedulerConfiguration config) {
        super(config);
        try {
            super.setType("dynamic");
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    /**
     * Form a name of this scheduler, which includes its thread name and the id
     * of the array on which it operates.  This name has the folloeing form:
     * 		Scheduler [task1] (array 1)
     * @return A string identifying this scheduler.
     */
    protected String name() {
    	return "Scheduler [" + Thread.currentThread().getName() + 
			"] (subarray " + arrayName + ")";
			//"] (array " + arrayName + ")";
    }
    
    /**
     * Once the scheduler thread is started we decide here which
     * mode to run, Dynamic or Interactive.
     */
    public void run() {
        logger.fine("SCHEDULING: Running in DYNAMIC mode");
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
    	logger.fine("SCHEDULING: "+name() + ": Configuration object validated.");
    	
    	// Create the dynamic scheduling algorithm object.
    	try {
    		dsa = new DynamicSchedulingAlgorithm(
    				arrayName, config.getPolicy(), config.getQueue(), clock,
					config.getTelescope(), config.getProjectManager(), logger, 
                    config.getBestNumber());
                    
    	} catch (SchedulingException err) {
    		config.errorEnd(err.toString(),clock.getDateTime());
    		logger.severe("SCHEDULING: "+name() + ": Error creating dynamic scheduling "+
                "algorithm ! " + err.toString());
    		return;
    	}
    	logger.fine("SCHEDULING: "+name() + ": Dynamic scheduling algorithm created.");
    	
    	// Set the start and end times.  (The ending time may be null.)
    	DateTime start = clock.getDateTime();
    	DateTime end = config.getCommandedEndTime();
    	config.start(start,end);
    	logger.fine("SCHEDULING: "+name() + ": Started " + start);
    	
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
    			logger.fine("SCHEDULING: "+name() + ": Stopping because stop flag is set.");
    			break;
    		}
    		
    		// 2. Check to see if we are at the end of this scheduling period.
    		end = config.getCommandedEndTime();
    		if (end != null && !end.isNull()) {
    			now = clock.getDateTime();
    			if (now.ge(end)) {
    				checkRunning();
    				logger.fine("SCHEDULING: "+name() + ": Stopping because we are at the end "+
                        "of the scheduling period.");
					break;
    			}
    		}

    		// 3. Check to see if there are any scheduling units in the queue.
    		if (config.getQueue().size() == 0) {
    			checkRunning();
    			logger.fine("SCHEDULING: "+name() + ": Stopping because there are no more "+
                    "scheduling units.");
                logger.fine("SCHEDULING: Nothing Can Be Scheduled Event sent out, in scheduler.");

                //NothingCanBeScheduled.NoResources
				break;
    		}
    		
    		// 4. Do the required actions, which are mode dependent.
            logger.info("*******About to go!");
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
                    logger.fine("SCHEDULING: Scheduler is sleeping..");
    			} catch (InterruptedException err) {
                    logger.fine("SCHEDULING: Scheduler's sleeping interrupted!");
    			}
    		}
    	};
    	
    	config.normalEnd(clock.getDateTime());
    	logger.fine(name() + " has ended!");
    	logger.fine(name() + " started " + config.getActualStartTime());
    	logger.fine(name() + " ended " + config.getActualEndTime());
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
            logger.fine("SCHEDULING: "+name()+" nothing left to schedule. BestSB == null");
    		return true;
    	}
    	
    	// Log the best list.
    	logger.fine("SCHEDULING: ** "+name() + ": " + best.toString());
        // create a message to correspond to this selectSB request
        // Submit the list to the operator to get the id of the best SB from 
        //the list
        //String bestSBId = config.getOperator().selectSB(best, m);
        //logger.info("SCHEUDLING: best selection will be.. "+ best.getBestSelection());
        //if(best.getBestSelection() == null) {
        if(best.getNumberReturned() == 0) {
    		// Nothing can be scheduled at this time.
    		// Call config's nothingToDo method and wait.
    		config.nothingToDo(best.getNothingCanBeScheduled());
    		// Get response.
    		switch (config.getAction()) {
     		    case SchedulerConfiguration.CONTINUE:
                    logger.fine("SCHEDULING: continue");
    			    return false;
        		case SchedulerConfiguration.STOP:
                    logger.fine("SCHEDULING: stop");
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
            logger.fine("SCHEDULING: best sb presented to operator = "+best.getBestSelection());
            //check if any sbs are currently running.
            SB[] sbs = config.getQueue().getRunning();
            if( sbs.length > 0 ){
                logger.fine("SCHEDULING: There's an SB running!");
                try {
                    Thread.sleep(config.getSleepTime() * 1000);
                }catch(Exception e) {}
                return false;
            }
            try {
                Message m = new Message();
                m.setArrayName(config.getArrayName());
                //TODO need a thread that waits for the operator's response..
                String sbid = config.getOperator().selectSB(
                        best, m, config.getArrayName(), getId());
                logger.fine("SCHEDULING: Operator picked sb = "+sbid);
        		// We've got somthing to schedule.
                //SB selectedSB = config.getQueue().get(best.getBestSelection());
                SB selectedSB = config.getQueue().get(sbid);
                logger.fine("SCHEDULING: get sb = "+sbid+" from queue");
                if(selectedSB == null) {
                    logger.severe("SCHEDULING: problem getting sb = "+sbid+" from queue");
                    return true;
                }
                if(selectedSB.getStatus().isReady() ){ //&& selectedSB.getStartTime() == null) 
                    logger.fine("SCHEDULING: About to schedule sb = "+selectedSB.getId());
                    //Check if its already running.
                    //times and status stuff done here coz Control obj doesn't have 
                    //have (or need) the whole queue.
                    
                    //Check that start time is null. If its not null but still ready
                    //then we're in another execution of this SB. So no need to set the
                    //starttime again. 
                    if(selectedSB.getStatus().getStartTime() == null) {
                        selectedSB.setStartTime(clock.getDateTime());
                    }
                    //logger.info("SB is now "+selectedSB.getStatus().getStatus());
                    //TODO When Lindsey says so ;)
                    //ObservedSession session = 
                    //    config.getProjectManager().createObservedSession(
                    //            selectedSB.getParent());
                    //config.getProjectManager().sendStartSessionEvent(session);
                    //set selection of selectedSB to be the 'best' selection in best
                    String[] ids = best.getSbId();
                    int selection = -1;
                    selectedSB.setRunning();
                    for(int i=0; i < ids.length;i++){
                        if(selectedSB.getId().equals(ids[i])){
                            //logger.info("SCHEDULING: SB one if the 'best' ones");
                            selection = i;
                            best.setSelection(i);
      		                config.getControl().execSB(config.getArrayName(), best);
                            break;
                        }
                    }
                    if(selection == -1) { //wasn't one of the ones selected to be best, 
                        //obviously in the queue (coz we just got it out) so send it to control
                        logger.fine("SCHEDULING: SB not one of the best ones, but it was selected!");
      		            config.getControl().execSB(config.getArrayName(), selectedSB.getId());
                    }
      		        //config.getControl().execSB(config.getArrayName(), best);
                } else {
                    logger.fine("SCHEDULING: SB is not ready to be executed.");
                    //do something else here eventually...
                }
            } catch (Exception e) {
                //clear queue! 
                config.getQueue().clear();
                config.getProjectManager().getProjectManagerTaskControl().interruptTask();
                logger.severe("SCHEDULING: error!");
                e.printStackTrace(System.out);
                return true;
            }
    		logger.fine("SCHEDULING: "+name() + ": executing " + best.getBestSelection());
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
