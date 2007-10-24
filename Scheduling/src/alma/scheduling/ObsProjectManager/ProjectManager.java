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
import alma.scheduling.NothingCanBeScheduledEnum;
import alma.scheduling.Define.SB;
import alma.scheduling.Define.SchedLogger;
import alma.scheduling.Define.TaskControl;
import alma.scheduling.Define.Program;
import alma.scheduling.Define.Project;
import alma.scheduling.Define.ProjectQueue;
import alma.scheduling.Define.ObservedSession;
import alma.scheduling.Define.SchedulingException;
import alma.scheduling.Scheduler.DSA.SchedulerStats;
/**
 * @version $Id: ProjectManager.java,v 1.14 2007/10/24 18:06:47 sslucero Exp $
 * @author Sohaila Lucero
 */
public class ProjectManager implements Runnable,
    alma.scheduling.Define.ProjectManager {

    //The logger
    protected SchedLogger logger;
    // True if the scheduling subsystem has been stopped
    protected boolean stopCommand;

    protected ProjectManagerTaskControl pmTaskControl;

    public ProjectManager() {
        //System.out.println("SCHEDULING: PM created!");
        this.stopCommand = false;
    }
    
    /**
     *
     */
    public void run() {
    }

    /**
      *
      */
    public void setStopCommand(boolean stop) {
        //System.out.println("SCHEDULING: in PM stop command set");
        this.stopCommand = stop;
    }
    public boolean getStopCommand() {
        return stopCommand;
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
     
    /** 
     * Sets the TaskControl object for the PM
     *
     * @param pmtc The ProjectManagerTaskControlObject
     */
    public void setProjectManagerTaskControl(ProjectManagerTaskControl pmtc) {
        this.pmTaskControl = pmtc;
    }

    /**
     * Returns the TaskControl object of the PM
     * 
     * @return ProjectManagerTaskControl
     */
    public TaskControl getProjectManagerTaskControl() {
        return pmTaskControl;
    }

    public ObservedSession createObservedSession(Program p) {
        ObservedSession session = new ObservedSession();
        session.setProgram(p);
        //TODO generate the part id without ProjectUtil somehow...
        return session;
    }

    ////////////////////////////////////////////////////////////
    // Methods needed by ACS/REAL mode but not for simulator
    ////////////////////////////////////////////////////////////
    public void sendStartSessionEvent(ObservedSession session){
    }

    public void sendEndSessionEvent(ObservedSession session) {
    }

    public Project getProject(String projId) throws SchedulingException{
        return null;
    }

    public String[] archiveQuery(String query, String schema) throws SchedulingException {
        return null;
    }
    public Object archiveRetrieve(String uid) throws SchedulingException {
        return null;
    }
    public void archiveReleaseComponents()  throws SchedulingException {
    }
    public SB[] getSBsForProject(String projId) throws SchedulingException {
        return null;
    }

    public ProjectQueue getProjectQueue() {
        return null;
    }

    public void getUpdates() throws SchedulingException {}
    public void publishNothingCanBeScheduled(NothingCanBeScheduledEnum reason){}
    public void addSchedulerStatsToArchive(SchedulerStats s){}    
    public SchedulerStats[] getSchedulerStatsFromArchive(){ return null;}    
}
