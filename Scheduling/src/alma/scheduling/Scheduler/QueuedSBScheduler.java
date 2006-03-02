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
 * File QueuedSBScheduler.java
 */
package alma.scheduling.Scheduler;

import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.SB;
import alma.scheduling.Define.SBQueue;
import alma.scheduling.Define.SchedulingException;
import alma.scheduling.Define.SciPipelineRequest;

import alma.scheduling.Define.Control;
import alma.scheduling.Define.Operator;
import alma.scheduling.Define.Telescope;
import alma.scheduling.Define.ProjectManager;
import alma.scheduling.Define.BestSB;
import alma.scheduling.Define.Project;

import alma.scheduling.MasterScheduler.Message;
//import alma.scheduling.AlmaScheduling.ALMAProjectManager;

import alma.scheduling.NothingCanBeScheduledEnum;
/**
 */
public class QueuedSBScheduler extends Scheduler implements Runnable {

	// The components we need from the configuration.
	private Control control;
	private Operator operator;
	private Telescope telescope;
	private ProjectManager projectManager;
	
	// State variables
	private boolean sessionStarted;
	
	// Identifying the session
	private String PI;
	private String projectId;
	
	// The interactive queue.
	private SBQueue queue;

    private int tmpCount=1;
	
	/**
	 * Create an scheduler.
	 * @param config The scheduler configuration that controls this interactive scheduler.
	 * @throws SchedulingException if any error is found in the configuration.
	 */
	public QueuedSBScheduler(SchedulerConfiguration config) throws SchedulingException {
		super(config);
		// Validate the configuration object.
		String msg = validateInteractiveConfig();
		if (msg != null) {
			config.errorEnd(msg,clock.getDateTime());
			error(msg);
		}
	}
	
	/**
	 * Get this InteractiveScheduler's SchedulerConfiguration object.
	 * @return the SchedulerConfiguration
	 */
	public SchedulerConfiguration getConfiguration() {
		return config;
	}

	/**
	 * Validate the interactive scheduler configuration.
	 * @return A message if there is an error; otherwise return null.
	 */
	private String validateInteractiveConfig() {
		DateTime t = config.getCommandedEndTime();
		//if (t == null || t.isNull())
		//	return name() + ": There is no commanded stop time.";
		if ((control = config.getControl()) == null)
			return name() + ": There is no control component.";
		if ((operator = config.getOperator()) == null)
			return name() + ": There is no operator component.";
		if ((telescope = config.getTelescope()) == null)
			return name() + ": There is no telescope model component.";
		if ((projectManager = config.getProjectManager()) == null)
			return name() + ": There is no project manager component.";
		queue = config.getQueue();
		return null;
	}
	
	/**
	 * The error method is a utility method for severe errors; It logs the 
	 * message and generates a SchedulingException.
	 * @param msg the error message
	 * @throws SchedulingException The generated SchedulingException
	 */
	private void error(String msg) throws SchedulingException {
		String s = name() + ": Severe error! " + msg;
		logger.severe(s);
		throw new SchedulingException (s);
	}
	
	// Executing SBs.

	/**
	 * Executes the list of SBs 
	 * @param sbId the ID of the SB to execute.
	 * @throws SchedulingException 
	 */
	public boolean execute() throws SchedulingException {
		/*if (config.isSBExecuting()) {
			error("Invalid operation. A scheduling block is currently executing.");
		}
        */
        if(config.getQueue().getRunning().length > 0) {

            return false;
        }
        logger.info("SCHEDULING: Queued scheduler execute # "+tmpCount);
        tmpCount++;
		SB[] sbs = queue.getReady();
        logger.info("%%%%%%%%%%%%%%%%%%%");
        logger.info("length of ready sbs = "+sbs.length);
        logger.info("%%%%%%%%%%%%%%%%%%%");
		if (sbs == null || sbs.length == 0) {
			//error("No SBs to execute");
            logger.info("SCHEDULING: No sbs to execute");
            return true;
		}
        String[] ids = queue.getAllReadyIds();
		if (ids == null) {
			//error("No ready SBs to execute");
            logger.info("SCHEDULING: No sbs ready to execute");
            return true;
		}
        String[] scores = new String[ids.length];
        double[] d = new double[ids.length];
		BestSB best = new BestSB (ids, scores, d, d, d, clock.getDateTime());
        SB sb = sbs[best.getSelection()];
        //TODO need to check that if there are more than one sbs in queue then selection 
        //     count needs to be increased.
        if(sb.getStatus().getStatus().equals("complete")){
            logger.info("SCHEDULING: SB "+sb.getId()+" is completed.");
            return true;
        }
		config.startExecSB(sb.getId());
		if (!sb.getStatus().isStarted()) {
			config.decrementSbsNotStarted();
		}
        if(sb.getStatus().getStartTime() == null) {
            sb.setStartTime(clock.getDateTime());
        }
        try {
            sb.setRunning();
        } catch(Exception e) {
            logger.info("SCHEDULING: Cannot set sb to running state.");
            logger.info(e.toString());
            return true;
        }
		control.execSB(config.getArrayName(),best);
        return false;
	}

	/**
	 * Stop the currently executing SB.
	 * @param sbId The ID of the SB to be stopped.
	 * @throws SchedulingException
	 */
	public void stop(String sbId) throws SchedulingException {
		if (!sessionStarted) {
			error("Invalid operation. There is no session underway.");
		}
		if (!config.isSBExecuting()) {
			error("Invalid operation. There is no scheduling block currently executing.");
		}
		String tmp = config.getCurrentSBId();
		if (!tmp.equals(sbId)) {
			error("The SB specified in the stop method (" + sbId +
					" does not match the currently executing SB (" + tmp + ")");
		}
		control.stopSB(config.getArrayName(),sbId);
        control.destroyArray(config.getArrayName());
	}

    public void run() {
        try {
            while(true) {
                if(execute()) {
                    break;
                }
            }
            config.getProjectManager().publishNothingCanBeScheduled(NothingCanBeScheduledEnum.OTHER);
            config.getControl().destroyArray(config.getArrayName());
        } catch(Exception e){
            e.printStackTrace();
        }
    }

}
