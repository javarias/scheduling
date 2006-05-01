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
 * File SchedulerConfiguration.java
 */
 
package alma.scheduling.Scheduler;

import java.util.logging.Logger;
import java.util.Vector;

import alma.scheduling.Define.TaskControl;
import alma.scheduling.Define.NothingCanBeScheduled;
import alma.scheduling.Define.SBQueue;
import alma.scheduling.Define.SB;
import alma.scheduling.Define.Clock;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.Control;
import alma.scheduling.Define.Operator;
import alma.scheduling.Define.Telescope;
import alma.scheduling.Define.ProjectManager;
import alma.scheduling.Define.Policy;
import alma.scheduling.Event.Publishers.PublishEvent;

/**
 * The SchedulerConfiguration is an extension of TaskControlInfo and
 * is used by the MasterScheduler to control a Scheduler.
 * Communications between the MasterScheduler and a Scheduler occurs via 
 * this object, which is shared between the two.  There is one such object 
 * for each Scheduler object.  
 * <p>
 * To the TaskControlInfo class this class adds basic information about work 
 * done on scheduling blocks and actions taken by the scheduler.  It also 
 * includes components needed by the scheduler.
 * <p>
 * The items that are added to TaskControlInfo by the SchedulerConfiguration
 * include:
 * <ul>
 * <li> The mode of the scheduler (dynamic or interactive)
 * <li> The queue of scheduling units
 * <li> The clock
 * <li> The control system component
 * <li> The operator component
 * <li> The telescope component
 * <li> The project manager component
 * <li> The scheduling policy
 * <li> The number of units in the "best" list
 * <li> The time, in seconds, for the scheduler to sleep between intervals
 * <li> The name of the array to which this scheduler is assigned
 * <li> The logger
 * </ul>
 * There are a number of variables used to give an indication of work done
 * by the Scheduler.  These include:
 * <ul>
 * <li> The number of completed scheduling units
 * <li> The number of scheduling units not started
 * <li> The number of scheduling units that failed
 * <li> The accumulated list of messages used to capture information
 * 		when a scheduler cannot schedule anything.
 * </ul>
 * The two threads operate in an asynchronous mode.  In addition 
 * to examining accumulated information, there are methods for interrupting 
 * either task and for stopping the Scheduler task.
 * <p>
 * There are a number of state variables that are used by the Scheduler 
 * task to indicate its state.  These include:
 * <ul>
 * <li> operational -- The Scheduler is operational.
 * <li> sleeping -- The Scheduler is sleeping.
 * <li> nothingToSchedule -- The Scheduler is in the nothing-to-schedule 
 *      state and is waiting for a resoponse.
 * <li> failure -- An uncorrectable error has occurred.
 * <li> synchronous -- The mode in which the scheduler operates.  In the 
 *      synchronous mode
 * 		the scheduler waits for the control system to complete the execution
 *      of a scheduling 
 * 		block before continuing its activity.  In the asynchronous mode it 
 *      continues its
 * 		activity after initiating the start of an execution of a scheduling block. 
 * </ul> 
 * When a Scheduler encounters a situation in which nothing can be scheduled,
 * it enters the nothing-to-schedule state and waits to be told what to do.
 * The Master Scheduler may respond with the following types of actions: 
 * <ul>
 * <li> Continue processing.
 * <li> Stop.
 * <li> Execute a specified filler scheduling unit.
 * <li> Execute a specified scheduling unit.
 * </ul>
 * 
 * @version $Id: SchedulerConfiguration.java,v 1.10 2006/05/01 18:59:17 sslucero Exp $
 * @author Allen Farris
 */
public class SchedulerConfiguration extends TaskControl {

	// TODO Add a method to add a number of scheduling units to the queue.
	// TODO Should there also be a method to delete scheduling units?
	
	// The mode of this scheduler: true if and only if dynamic mode.
	private boolean dynamic;
	// The queue of scheduling units.
	private SBQueue queue;
    //Vector to hold the SpecialSBs
    private Vector specialSBs;
	// The number of units in the "best" list.
	private int bestNumber;
	// The time, in seconds, for the scheduler to sleep between intervals.
	private int sleepTime;
	// The name of the array to which this scheduler is assigned.
	private String arrayName;
	// The clock.
	private Clock clock;
	// The control system component.
	private Control control;
	// The operator component.
	private Operator operator;
	// The telescope component.
	private Telescope telescope;
	// The project manager componenet.
	private ProjectManager projectManager;
	// The scheduling policy.
	private Policy policy;
	// The logger.
	private Logger log;
    //The scheduling notification channel publisher
    //private PublishEvent publisher;

	// The message when nothing can be scheduled.
	private NothingCanBeScheduled nothing;

	// Counter: The number of completed scheduling units.
	private int sbsCompleted;
	// Counter: The number of scheduling units not started.
	private int sbsNotStarted;
	// Counter: The number of scheduling units that failed.
	private int sbsFailed;
	
	// State variables
	private boolean sleeping;
	private boolean nothingToSchedule;
	private boolean failure;
	private boolean synchronous;
	

	// Possible response actions by the MasterScheduler
	
	/**
	 * A response action to indicate there is no response.
	 */
	static public final int NOTHING = 0;
	/**
	 * A response action to indicate the scheduler should continue.
	 */
	static public final int CONTINUE = 1;
	/**
	 * A response action to indicate the scheduler should stop.
	 */
	static public final int STOP = 2;
	/**
	 * A response action to indicate the scheduler should execute a specified 
     * filler program.
	 */
	static public final int FILLER = 3;
	/**
	 * A response action to indicate the scheduler should execute a specified 
     * scheduling unit.
	 */
	static public final int SB = 4;
	
	// Response action
	private int action;
	private SB sbToDo;
	
	// The currently executiong SB.
	private String currentSB;
    // The previously executed SB.
    private String previousSB;

	
	public synchronized void startExecSB(String sbId) {
        System.out.println("**********************");
        System.out.println("CurrentSB set to "+sbId);
		currentSB = sbId;
	}
	public synchronized void endExecSB(String sbId) {
        if (sbId != currentSB) {
            throw new IllegalArgumentException("Ending SB-id (" + sbId + 
                    ") does not match currently executing SB (" + currentSB + ")!");
        }
        SB sb = queue.get(sbId);
        if (sb == null)
            throw new IllegalArgumentException ("The SB with id " + sbId +
                    "is not in the configuration queue.");
        if (sb.getStatus().isComplete()) {
            incrementSbsCompleted();
        } else if (sb.getStatus().isAborted()) {
            incrementSbsFailed();
        }
        previousSB = currentSB;
        currentSB = null;
        /*
		if (sbId != currentSB) {
			throw new IllegalArgumentException("Ending SB-id (" + sbId + 
				") does not match currently executing SB (" + currentSB + ")!");
		}
		currentSB = "";
        */
	}
    ////////////////////////	
    // TEMPORARY 
    ////////////////////////	
    public SchedulerConfiguration(Thread masterScheduler) {
        super(masterScheduler);
    }
    ////////////////////////
	
	/**
	 * Create a Scheduler Configuration object for the regular SBs.
	 */
	public SchedulerConfiguration(Thread masterScheduler,
		                          boolean dynamic, 
                                  boolean synchronous, 
                                  SBQueue queue, 
                                  int bestNumber,
                                  int sleepTime, 
                                  String name, 
                                  Clock clock, 
                                  Control control,
                                  Operator operator, 
                                  Telescope telescope, 
                                  ProjectManager projectManager,
                                  Policy policy, 
                                  Logger log) {
           

		super(masterScheduler);
		this.dynamic = dynamic;
		this.synchronous = synchronous;
		if (queue == null){
			this.queue = new SBQueue ();
		}else{
			this.queue = queue;
        }
		this.bestNumber = bestNumber;
		this.sleepTime = sleepTime;
		this.arrayName = name;
		this.clock = clock;
		this.control = control;
		this.operator = operator; 
		this.telescope = telescope;
		this.projectManager = projectManager; 
		this.policy = policy;
		this.log = log;
		this.nothing = null;
		this.sbsNotStarted = queue.size();
		this.sbsCompleted = 0;
		this.sbsFailed = 0;
		this.sleeping = false;
		this.nothingToSchedule = false;
		this.failure = false;
		this.action = NOTHING;
		this.sbToDo = null;
		this.currentSB = "";
	}

    /**
      * Creates a Scheduler Configuration object for SpecialSBs.
      */
    public SchedulerConfiguration(Thread masterScheduler,
                                  boolean dynamic, 
                                  Vector sbs, 
                                  String name, 
                                  Clock clock, 
                                  Control control, 
                                  Operator operator,
                                  Telescope telescope, 
                                  ProjectManager projectManager,
                                  Policy policy, 
                                  Logger log) {

        super (masterScheduler);
        this.dynamic = dynamic;
		if (specialSBs == null){
			this.specialSBs = new Vector();
		}else{
			this.specialSBs = sbs;
        }
		this.arrayName = name;
		this.clock = clock;
		this.control = control;
		this.operator = operator; 
		this.telescope = telescope;
		this.projectManager = projectManager; 
		this.policy = policy;
		this.log = log;
        // these below will not used but just initialized
		this.synchronous = true;
		this.bestNumber = 0;
		this.sleepTime = 0;
		this.nothing = null;
		this.sbsNotStarted = specialSBs.size();
		this.sbsCompleted = 0;
		this.sbsFailed = 0;
		this.sleeping = false;
		this.nothingToSchedule = false;
		this.failure = false;
		this.action = NOTHING;
		this.sbToDo = null;
		this.currentSB = "";

    }
	
    /**
      *
      */
	public synchronized void normalEnd(DateTime time) {
		super.normalEnd(time);
		sleeping = false;
		nothingToSchedule = false;
		failure = false;
	}
    /**
      *
      */
	public synchronized void errorEnd(String err, DateTime time) {
		super.errorEnd(err,time);
		sleeping = false;
		nothingToSchedule = false;
		failure = true;
	}
	
    /**
      *
      */
	public synchronized boolean isSleeping() {
		return sleeping;
	}
    /**
      *
      */
	public synchronized boolean isNothingToSchedule() {
		return nothingToSchedule;
	}
    /**
      *
      */
	public synchronized boolean isError() {
		return failure;
	}
    /**
      *
      */
	public synchronized void sleepingOn() {
		sleeping = true;
	}
    /**
      *
      */
	public synchronized void sleepingOff() {
		sleeping = false;
	}

    /**
      *
      */
	public synchronized int getAction() {
		return action;
	}
    
    /**
      *
      */
	public synchronized SB getSBToDo() {
		return sbToDo;
	}

    /**
      *	Enter the nothing-to-schedule state and wait for a response.
      */
	public synchronized void nothingToDo(NothingCanBeScheduled x) {
        try {
		nothing = x;
		nothingToSchedule = true;
		interruptMasterScheduler();
		while (nothingToSchedule) {
			try {
				wait();
			} catch (InterruptedException e) {
                System.out.println("interrupted in SchedConfig");
			}
		}
        }catch(Exception e) {
            System.out.println("broken in SchedConfig");
        }
	}

    /**
      *
      */
	public synchronized void respondContinue() {
		action = CONTINUE;
		sbToDo = null;
		nothingToSchedule = false;
		notify();
	}

    /**
      *
      */
	public synchronized void respondStop() {
		action = STOP;
		stopTask();
		sbToDo = null;
		nothingToSchedule = false;
		notify();
	}

    /**
      *
      */
	public synchronized void respondFiller(SB sb) {
		action = FILLER;
		sbToDo = sb;
		nothingToSchedule = false;
		notify();
	}

    /**
      *
      */
	public synchronized void respondSB(SB sb) {
		action = SB;
		sbToDo = sb;
		nothingToSchedule = false;
		notify();
	}
	
	/**
	 * Increment the number of schedblocks completed.
	 */
	public synchronized void incrementSbsCompleted() {
		++sbsCompleted;
	}
	/**
	 * Decrement the number of schedblocks completed.
	 */
	public synchronized void decrementSbsCompleted() {
		--sbsCompleted;
	}

	/**
	 * Increment the number of schedblocks not started.
	 */
	public synchronized void incrementSbsNotStarted() {
		++sbsNotStarted;
	}
	/**
	 * Decrement the number of schedblocks not started.
	 */
	public synchronized void decrementSbsNotStarted() {
		--sbsNotStarted;
	}
	
	/**
	 * Increment the number of scheduling blocks failed.
	 */
	public synchronized void incrementSbsFailed() {
		++sbsFailed;
	}
	/**
	 * Decrement the number of scheduling blocks failed.
	 */
	public synchronized void decrementSbsFailed() {
		--sbsFailed;
	}

	/**
	 * Return the number of scheduling blocks completed.
	 * @return the number of scheduling blocks completed.
	 */
	public synchronized int getSbsCompleted() {
		return sbsCompleted;
	}

	/**
	 * Return the number of scheduling blocks not started.
	 * @return the number of scheduling blocks not started.
	 */
	public synchronized int getSbsNotStarted() {
		return sbsNotStarted;
	}

	/**
	 * Return the number of scheduling blocks failed.
	 * @return the number of scheduling blocks failed.
	 */
	public synchronized int getSbsFailed() {
		return sbsFailed;
	}

	/**
	 * Get all "nothing-to-schedule" messages.
	 * @return
	 */
	public synchronized NothingCanBeScheduled getNothingToSchedule() {
		return  nothing;
	}
	
	/**
	 * Clear all "nothing-to-schedule" message.
	 */
	public synchronized void clearMessage() {
		nothing = null;
	}
	
	// Note:  The following get methods are not synchronized because they are
	// set by the MasterScheduler when creating a Scheduler and never changed 
    //after that.

	/**
	 * @return
	 */
	public int getBestNumber() {
		return bestNumber;
	}

	/**
	 * @return
	 */
	public Clock getClock() {
		return clock;
	}

	/**
	 * @return
	 */
	public Control getControl() {
		return control;
	}

	/**
	 * @return
	 */
	public Logger getLog() {
		return log;
	}

	/**
	 * @return
	 */
	public Operator getOperator() {
		return operator;
	}

	/**
	 * @return
	 */
	public Policy getPolicy() {
		return policy;
	}

	/**
	 * @return
	 */
	public SBQueue getQueue() {
		return queue;
	}

	/**
	 * @return
	 */
	public int getSleepTime() {
		return sleepTime;
	}

	/**
	 * @return
	 */
	public String getArrayName() {
		return arrayName;
	}

	/**
	 * @return
	 */
	public Telescope getTelescope() {
		return telescope;
	}

	/**
	 * @return
	 */
	public ProjectManager getProjectManager() {
		return projectManager;
	}

	/**
	 * @return
	 */
	public boolean isDynamic() {
		return dynamic;
	}
	
	/**
	 * @return
	 */
	public boolean isSynchronous() {
		return synchronous;
	}
	
    /**
     * @return Returns the currentSB.
     */
    public String getCurrentSBId() {
        return currentSB;
    }

    /**
      *
      */
    public boolean isSBExecuting() {
        System.out.println("**************");
        System.out.println("Current SB =="+ currentSB);
        return currentSB == null;
    }

    /**
      *
      */
    public String getPreviousSBId() {
        return previousSB;
    }

}
