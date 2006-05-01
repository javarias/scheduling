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
 * File InteractiveScheduler.java
 */
package alma.scheduling.Scheduler;

import alma.scheduling.Define.InteractiveSession;
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
import alma.scheduling.Define.Status;

import alma.scheduling.MasterScheduler.Message;
//import alma.scheduling.AlmaScheduling.ALMAProjectManager;

/**
 * The InteractiveScheduler class implements the concept of a scheduler
 * that operates in the interactive mode.  It implements the 
 * InteractiveSession interface.  The InteractiveScheduler class 
 * implements basic low-level methods for interactively defining and 
 * executing scheduling blocks.  The primary commands are the following:
 * <ul>
 * <li> login(String PI, SB interactiveSB) -- start the interactive session.
 * <li> logout() -- end the interactive session.
 * <li> SB[] getAllSB() -- get all SBs in the queue that belong to this project.
 * <li> add(SB sb) -- add the specified SB to the interactive queue.
 * <li> update(SB sb) -- modify the specified SB in the interactive queue.
 * <li> delete(String sbId) -- delete the specified SB from the interactive queue.
 * <li> execute(String sbId) -- execute the specified SB.
 * <li> stop(String sbId) -- stop the execution of the specified SB.
 * <li> startSciPipeline(SciPipelineRequest req) -- start the science pipeline 
 * (not implemented at present).
 * </ul>
 * Note that the methods getAllSB, add, update, and delete operate on
 * the interactive queue in memory and do not interact with the archive.
 * <p>
 * The InteractiveScheduler class is not implemented as a separate
 * thread.  It is merely a collection of methods that are executed
 * in the controlling thread that executes the interactive session.
 * Such a controlling thread may be a controlling GUI governing the
 * interactive session in a "real" mode, or it may be a special class
 * that implements a simulated interactive session in a "simulation"
 * mode.
 * <p>
 * The MasterScheduler (either in a "simulation" or "real" mode)
 * creates the InteractiveScheduler using the SchedulerConfiguration
 * object, which the two objects share.  The MasterScheduler then
 * creates the controlling thread and passes the InteractiveScheduler
 * object to it.  The controlling thread must then get the
 * SchedulerConfiguration object from the InteractiveScheduler
 * and must periodically monitor its status.  It is via this 
 * configuration object that the thread is told when an SB is complete
 * and when to stop, etc.  If this thread ignores a stop request,
 * the MasterScheduler will eventually kill it.
 * <P>
 * The class that implements the thread that controls the interactive session 
 * must have a constructor that takes an InteractiveScheduler as an argument, as
 * well as implementing the "run" method.  The controlling thread must then
 * get the InteractiveScheduler's configuration object, via the 
 * "getConfiguration()" method.  Using this configuration object, the controlling
 * thread must do three things:
 * <ul> 
 * <li>At the beginning of the "run" method set the subordinate task's thread 
 * using the setTask method: config.setTask(Thread.currentThread());
 * <li>Periodically check the stop flag to see if the MasterScheduler has told
 * the subordinate thread to stop: config.isStopFlag();
 * <li>Periodically check to see if the currently executing scheduling block
 * has ended.
 * </ul>
 * There are a number of methods in the configuration object dealing with
 * executing scheduling blocks.
 * <ul>
 * <li> getCurrentSBId() -- returns the ID of the currently executing scheduling 
 * block or null, if there is no SB executing.
 * <li> isSBExecuting() -- returns true if there is a SB currently executing.
 * <li> getPreviousSBId() -- returns the ID of the previously executed SB.
 * <li> startExecSB -- used internally by the InteractiveScheduler when it
 * starts the execution of an SB.
 * <li> endExecSB -- Used by the MasterScheduler when an SB has ended.
 * </ul>
 * @version $Id: InteractiveScheduler.java,v 1.10 2006/05/01 18:59:17 sslucero Exp $
 * @author Allen Farris
 */
public class InteractiveScheduler extends Scheduler implements InteractiveSession {

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
	
	/**
	 * Create an interactive scheduler.
	 * @param config The scheduler configuration that controls this interactive scheduler.
	 * @throws SchedulingException if any error is found in the configuration.
	 */
	public InteractiveScheduler(SchedulerConfiguration config) throws SchedulingException {
		super(config);
		// Validate the configuration object.
		String msg = validateInteractiveConfig();
		if (msg != null) {
			config.errorEnd(msg,clock.getDateTime());
			error(msg);
		}
        config.getLog().info("SCHEDULING: Interactive scheduler created");
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
	
	// Logging in and out of the interactive session.
	
	/**
	 * Begin an interactive session.
	 * @param PI The PI in charge of this ssession.
	 * @param interactiveSB The interactive scheduling block that specifies this interactive session.
	 * @throws SchedulingException if there is already a session started, the specified 
	 * SB is not an interactive schedling block, or if the specified PI does not match
	 * the PI in the interactive scheduling block. 
	 */
	public void login(String PI, String projId, SB interactiveSB) throws SchedulingException {
        logger.info("id =" +interactiveSB.getProject().getId());
        logger.info("PI =" +PI);
        logger.info("PI of sb =" +interactiveSB.getProject().getPI());
		if (sessionStarted) {
			error("Cannot login.  A session is already underway.");
            return;
		}
		if (interactiveSB.getType() != SB.INTERACTIVE) {
			error("The specified SB is not an interactive scheduling block.");
            return;
		}
        /*
		if (!PI.equals(interactiveSB.getProject().getPI())) {
			error("The specified PI does not match the PI in the interactive scheduling block.");
            return;
		}*/
        Project p = config.getProjectManager().getProject(projId); 
        String tmp = p.getPI();
        if(!PI.equals(tmp)){
			error("The specified PI does not match the PI in the project.");
            return;
        }
		sessionStarted = true;
		this.PI = PI;
		projectId = projId;//interactiveSB.getProject().getId();
		String msg = "Interactive session for project " + projectId + 
			" with PI " + PI + " started.";
		//operator.send(msg, config.getArrayName());
		logger.info(msg);
	}

	/**
	 * End an interactive session.
	 * @throws SchedulingException if there is no interactive session underway
	 * or if a schedling block is currently executing. 
	 */
	public void logout() throws SchedulingException {
		if (!sessionStarted) {
			error("Cannnot logout.  There is no session underway.");
		}
		if (config.isSBExecuting()) {
			error("Connot logout.  There is a scheduling block executing.");
		}
		queue = null;
		sessionStarted = false;
		PI = null;
		projectId = null;
		String msg = "Interactive session for project " + projectId + 
			" with PI " + PI + " ended.";
		//operator.send(msg, config.getArrayName());
		logger.info(msg);
		config.normalEnd(clock.getDateTime());
	}

	// Getting, adding, updating, and deleting SBs.
	
	/**
	 * Get all SBs in the interactive queue.
	 * @throws SchedulingException if there is no interactive session underway. 
	 */
	public SB[] getAllSB() throws SchedulingException {
		if (!sessionStarted) {
			error("Invalid operation. There is no session underway.");
		}
		return queue.getAll();
	}

	/**
	 * Add an SB to the interactive queue.
	 * @param sb The SB to be added.
	 * @throws SchedulingException if there is no interactive session underway
	 * or if the SB already exists. 
	 */
	public void add(SB sb) throws SchedulingException {
		if (!sessionStarted) {
			error("Invalid operation. There is no session underway.");
		}
		SB tmp = queue.get(sb.getId());
		if (tmp != null) {
			error("Cannot add SB " + sb.getId() + ". It already exists.");
		}
		queue.add(sb);
		config.incrementSbsNotStarted();
	}

	/**
	 * Update an SB in the interactive queue.
	 * @param sb The SB to be updated.
	 * @throws SchedulingException if there is no interactive session underway
	 * or if the SB does not exist or has already been executed.  
	 */
	public void update(SB sb) throws SchedulingException {
		if (!sessionStarted) {
			error("Invalid operation. There is no session underway.");
		}
		SB tmp = queue.get(sb.getId());
		if (tmp == null) {
			error("Cannot update SB " + sb.getId() + ". It does not exist.");
		}
		if (tmp.getStatus().isStarted()) {
			error("Cannot update SB " + sb.getId() + 
					". It has been executed and cannot be modified.");
		}
		queue.remove(sb.getId());
		queue.add(sb);
	}

	/**
	 * Delete an SB from the interactive queue.
	 * @param sbId The ID of the SB to be deleted.
	 * @throws SchedulingException if there is no interactive session underway
	 * or if the SB does not exist or has already been executed. 
	 */
	public void delete(String sbId) throws SchedulingException {
		if (!sessionStarted) {
			error("Invalid operation. There is no session underway.");
		}
		SB tmp = queue.get(sbId);
		if (tmp == null) {
			error("Cannot delete SB " + sbId + ". It does not exist.");
		}
		if (tmp.getStatus().isStarted()) {
			error("Cannot delete SB " + sbId + 
					". It has been executed and cannot be modified.");
		}
		queue.remove(sbId);
		config.decrementSbsNotStarted();
	}
	
	// Executing and stopping SBs.

	/**
	 * Execute the SB with the specified ID.
	 * @param sbId the ID of the SB to execute.
	 * @throws SchedulingException if there is no interactive session underway
	 * or if there is a scheduling block already executing, or if the specified
	 * SB does not exist. 
	 */
	public void execute(String sbId) throws SchedulingException {
		if (!sessionStarted) {
			error("Invalid operation. There is no session underway.");
		}
		if (config.isSBExecuting()) {
			error("Invalid operation. A scheduling block is currently executing.");
		}
		SB sb = queue.get(sbId);
		if (sb == null) {
			error("Cannot execute SB " + sbId + ". It does not exist.");
		}
		String[] id = new String [1];
		id[0] = sbId;
		String[] s = new String [1];
		s[0] = "";
		double[] d = new double [1];
		d[0] = 0.0;
		BestSB best = new BestSB (id,s,d,d,d,clock.getDateTime());
		config.startExecSB(sbId);
		if (!sb.getStatus().isStarted()) {
			config.decrementSbsNotStarted();
		}
        if(sb.getStatus().getStartTime() == null) {
            sb.setStartTime(clock.getDateTime());
        }
        sb.setRunning();
        logger.info("############### SB's status = "+sb.getStatus().toString());
        logger.info("############### SB's starttime = "+sb.getStatus().getStartTime());
		control.execSB(config.getArrayName(),best);
		String msg = "Scheduling block " + sbId  + 
			" in interactive session for project " + projectId + 
			" with PI " + PI + " has been started.";
		//operator.send(msg, config.getArrayName());
		logger.info(msg);
	}

	/**
	 * Stop the currently executing SB.
	 * @param sbId The ID of the SB to be stopped.
	 * @throws SchedulingException if there is no interactive session underway
	 * or if there is no scheduling block currently executing, or if the specified
	 * SB does not match the one that is currently executing. 
	 */
	public void stop(String sbId) throws SchedulingException {
		if (!sessionStarted) {
			error("Invalid operation. There is no session underway.");
		}
        System.out.println("Is sb executing? "+ config.isSBExecuting());
		if (!config.isSBExecuting()) {
			error("Invalid operation. There is no scheduling block currently executing.");
		}
		String tmp = config.getCurrentSBId();
		if (!tmp.equals(sbId)) {
			error("The SB specified in the stop method (" + sbId +
					" does not match the currently executing SB (" + tmp + ")");
		}
        SB sb = queue.get(sbId);
        logger.info("SCHEDULING: Before stop, sb status = "+sb.getStatus().toString());
		control.stopSB(config.getArrayName(),sbId);
        logger.info("SCHEDULING: after stop, sb status = "+sb.getStatus().toString());
        sb.execEnd(null, clock.getDateTime(), Status.READY);
        //setting the sb status will be handled in the project manager eventually
        //but for now we set it here and disgard the exec block
        //TODO delete next 2 lines when control sends execblockendedevent in stop.
        
        config.endExecSB(sbId);
		String msg = "Scheduling block " + sbId  + 
			" in interactive session for project " + projectId + 
			" with PI " + PI + " has been stopped.";
		//operator.send(msg, config.getArrayName());
		logger.info(msg);
	}

	// Starting the science pipeline.
	
	/**
	 * Start the science pipeline. Not implemented at present.
	 * @param req The science pipeline requesst object needed to start the science pipeline.
	 * @throws SchedulingException if any error is encountered in starting the science pipeline.
	 */
	public void startSciPipeline(SciPipelineRequest req) throws SchedulingException {
		if (!sessionStarted) {
			error("Invalid operation. There is no session underway.");
		}
		throw new SchedulingException("The method to start the science pipeline is not implemented at this time.");
	}

}
