/*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by AUI (in the framework of the ALMA collaboration),
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
 * File MasterScheduler.java
 *
 */

package alma.scheduling.MasterScheduler;

import java.util.logging.Logger;

import alma.scheduling.Define.Policy;
import alma.scheduling.Event.Publishers.PublishEvent;
import alma.scheduling.ObsProjectManager.ProjectManager;

/** 
 * @author Sohaila Roberts
 */
public class MasterScheduler implements Runnable {

    // Is the master scheduler stopped?
    private boolean stopCommand;
    // The obs project manager
    protected ProjectManager manager;
    //The logger
    protected Logger logger;
    //The scheduling notification channel
    protected PublishEvent publisher;

    /**
     * Constructor
     */
    public MasterScheduler() {
        this.stopCommand = false;
    }
    
    /**
     * Initialize the Master Scheduler
     */
    public void initialize() {
        // TODO create the ControlReceiver
    }
    
    /** 
     * Sets reference objects to be used to null.
     */
    private void setNullReferences() {
    }
    
    /**
     * The Master Scheduler's run method.
     */
    public void run() {
        while(!stopCommand) {
        }
    }

    /**
     *
     */
    public void startScheduling(Policy schedulingPolicy) {
    }

    /**
     *
     */
    public void stopScheduling() throws Exception {
    }

    /////////////////////////////////////////////////////////////////////
    // GET METHODS
    /////////////////////////////////////////////////////////////////////

    /**
     * Tells you whether the scheduling subsystem is stopped or not.
     * @return boolean True if stopped
     */
    public boolean getStopCommand() {
        return stopCommand;
    }
    /////////////////////////////////////////////////////////////////////
    // GET METHODS
    /////////////////////////////////////////////////////////////////////

    /**
     * Sets the master scheduler to stopped or not.
     * @param value 
     */
    public void setStopCommand(boolean value) {
        this.stopCommand = value;
    }
}
