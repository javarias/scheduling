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
 * File ProjectManager.java
 * 
 */

package alma.scheduling.ObsProjectManager;

import java.util.logging.Logger; 

import alma.scheduling.Define.SB;

/**
 * @author Sohaila Lucero
 */
public class ProjectManager implements Runnable,
    alma.scheduling.Define.ProjectManager {

    //The logger
    protected Logger logger;
    // True if the scheduling subsystem has been stopped
    protected boolean stopCommand;

    public ProjectManager() {
        System.out.println("I'm created!");
    }
    
    /**
     *
     */
    public void run() {
        this.stopCommand = false;
    }

    /**
     * @return boolean
     */
    public boolean newProject(SB unit) {
        return true;
    }

    /**
     * @return int
     */
    public int numberRemaining(SB unit) {
        return 0;
    }
     

    
}
