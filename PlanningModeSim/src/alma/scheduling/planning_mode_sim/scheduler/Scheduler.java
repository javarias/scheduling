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
 * File Scheduler.java
 * 
 */
package alma.scheduling.planning_mode_sim.scheduler;

import alma.scheduling.planning_mode_sim.define.SUnit;
import alma.scheduling.planning_mode_sim.define.SProject;
import alma.scheduling.planning_mode_sim.define.MemberOf;
import alma.scheduling.planning_mode_sim.define.SPolicy;
import alma.scheduling.planning_mode_sim.define.DateTime;
import alma.scheduling.planning_mode_sim.define.Clock;
import alma.scheduling.planning_mode_sim.define.Status;
import alma.scheduling.planning_mode_sim.define.SExec;
import alma.scheduling.planning_mode_sim.define.SiteCharacteristics;
import alma.scheduling.planning_mode_sim.master_scheduler.MasterScheduler;
import alma.scheduling.planning_mode_sim.master_scheduler.ControlProxy;
import alma.scheduling.planning_mode_sim.master_scheduler.SchedulingException;
import alma.scheduling.planning_mode_sim.master_scheduler.ArchiveProxy;

import alma.scheduling.planning_mode_sim.scheduler.r1policy.DynamicSchedulingAlgorithm;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.io.PrintStream;

/**
 * Scheduler.java
 * 
 * The Scheduler class is the major controlling class in the 
 * scheduler package.  See Scheduling Subsystem Design document, 
 * section 3.2.3.
 * 
 * @version 1.00 Feb 27, 2003
 * @author Allen Farris
 *
 */
public class Scheduler implements Runnable {
	private short subarrayId;
	private SUnit[] sb;
	private SPolicy policy;
	private DateTime endTime;
	private DateTime beginTime;
	private ControlProxy control;
	private ArchiveProxy archive;
	private Clock clock;
	private MasterScheduler master;
	private Logger log;
	private PrintStream out;
		
	private String currentProjectId;
	private int currentFrequencyBand;
	private ArrayList projectId;
	
	private DynamicSchedulingAlgorithm algorithm;
	private SiteCharacteristics site;
    
    public Scheduler(short subarrayId, SUnit[] sb, SPolicy policy, DateTime endTime, MasterScheduler master) {
    	this.subarrayId = subarrayId;
    	this.sb = sb;
    	this.policy = policy;
    	this.endTime = endTime;
    	this.master = master;
    	this.control = master.getControl();
    	this.archive = master.getArchive();
    	this.clock = master.getClock();
    	this.log = master.getLogger();
    	this.site = master.getSite();
    	this.beginTime = clock.getDateTime();
    	// Eventually, this will be a dynamically loaded class.
    	algorithm = new alma.scheduling.planning_mode_sim.scheduler.r1policy.DynamicSchedulingAlgorithm (this);
    	// Initialiize the current project list.
    	projectId = new ArrayList ();
    	currentProjectId = " ";
    	currentFrequencyBand = -1;
    }
    
    public SUnit[] getSB() {
    	return sb;
    }
    public String getCurrentProjectId() {
    	return currentProjectId;
    }
	public int getCurrentFrequencyBand() {
    	return currentFrequencyBand;
    }
    public SPolicy getSchedulingPolicy() {
    	return policy;
    }
	public boolean isNewProject(SUnit sb) {
		for (int i = 0; i < projectId.size(); ++i) {
			if (((String)projectId.get(i)).equals(sb.getId())) {
				return false;
			}
		}
		return true;
    }
    private void checkProjectId(String id) {
		for (int i = 0; i < projectId.size(); ++i) {
			if (((String)projectId.get(i)).equals(id)) {
				return;
			}
		}
		projectId.add(id);
    }
    
	public int getRemainingSB(String id) {
		// get the project from the archive.
		SProject p = null;
		try {
			p = archive.getProject(id);
		} catch (SchedulingException err) {
			err.printStackTrace();
		}
   		return p.getTotalUnits() - p.getNumberUnitsCompleted()  - p.getNumberUnitsFailed();
    }

	public Logger getLogger() {
		return log;
	}
	public Clock getClock() {
		return clock;
	}
    
    public void run() {
    	String unitId = null;
    	SUnit unit = null;
    	
    	DateTime begNothing = null;
    	DateTime endNothing = null;
    	boolean[] reason = new boolean [3];
    	

    	while (true) {
			// Are we at the end of this scheduling period?
    		if (clock.getDateTime().gt(endTime)) {
    			out.println();
   				out.println("End of scheduling period.");
    			break;
    		}
    		// Do we have anything left to schedule?
    		if (algorithm.remaining() == 0) {
				out.println();
				out.println("Nothing left to schedule.");
    			break;
    		}
    		// Rank the sceduling blocks.
    		algorithm.score();
    		// Get the best one.
    		unitId = algorithm.getBest();
    		// Can anything be scheduled?
    		if (unitId != null) { 
    			if (begNothing != null) {
					out.println();
					out.println("Nothing could be scheduled from " + begNothing.timeOfDayToString() + 
						" to " + endNothing.timeOfDayToString());
					if (reason[0]) out.println(DynamicSchedulingAlgorithm.REASON[0]); 
					if (reason[1]) out.println(DynamicSchedulingAlgorithm.REASON[1]); 
					if (reason[2]) out.println(DynamicSchedulingAlgorithm.REASON[2]); 
					out.println();
    				begNothing = null;
    			}
 				try {
					// Execute the SB.
					control.execSB(subarrayId,unitId);
					// Get the unit.
					unit = archive.getSUnit(unitId);
				} catch (SchedulingException err) {
					System.out.println(err.toString());
				}
				// check the unit status.
				updateUnitStatus(unit);
				// Check the project list.
				checkProjectId(unit.getProjectId());
				// If the unit is complete, delete the SB from the algorithm's list.
				if (unit.getUnitStatus().equals(Status.COMPLETE))
					algorithm.deleteSB(unitId);
				// Reset project and band.
				currentProjectId = unit.getProjectId();
				currentFrequencyBand = unit.getFrequencyBand();
    		} else {
				if (begNothing == null) {
					begNothing = clock.getDateTime();
					reason[0] = false;
					reason[1] = false;
					reason[2] = false;
				}
				reason[algorithm.getReason()] = true;
    			master.cannotScheduleAnything();
				endNothing = clock.getDateTime();   				
    		}
    	}
		algorithm.showSummary(out);
    }
    
    private void updateUnitStatus(SUnit unit) {
    	// If we've executed it the maximum number of times, mark it complete.
    	if (unit.getNumberMembers() > unit.getMaximumNumberOfRepeats()) {
    		unit.setUnitStatus(Status.COMPLETE);   		
    	}
    	// Get this unit's project.
    	SProject prj = unit.getProject();
    	SExec[] x = unit.getSExec();
    	// And, the last execution record.
    	SExec ex = x[x.length - 1];
    	// If this is a new project, record tbe start time and change the status.
    	if (prj.getNumberUnitsCompleted() == 0) {
    		prj.setStartTime(ex.getStartTime());
    		prj.setProjectStatus(Status.RUNNING);
    	}
    	// If this is complete,
    	if (unit.getUnitStatus().equals(Status.COMPLETE)) {
			// increment this project's numberUnitsCompleted.
			int n = prj.getNumberUnitsCompleted() + 1;
			prj.setNumberUnitsCompleted(n);
			n = prj.getTotalUsedTimeInSeconds() + unit.getMaximumTimeInSeconds();
			prj.setTotalUsedTimeInSeconds(n);
			//check if all SBs in this project are complete; if so, record the end time.
			MemberOf[] m = prj.getProgram().getMember();
			int i = 0;
			for (; i < m.length; ++i) {
				if (!((SUnit)(m[i])).getUnitStatus().equals(Status.COMPLETE)) {
					break;
				}
			}
			if (i == m.length) {
				prj.setEndTime(ex.getEndTime());
				prj.setProjectStatus(Status.COMPLETE);
				
			}
    	}
    }

	public void setOut(PrintStream out) {
		this.out = out;
	}

	public void generateOutputRecord(short subarrayId, SUnit sb, SExec ex) {
		algorithm.generateOutputRecord(out, subarrayId, sb, ex);
	}


	/**
	 * @return
	 */
	public SiteCharacteristics getSite() {
		return site;
	}

	/**
	 * @return
	 */
	public ArchiveProxy getArchive() {
		return archive;
	}

	/**
	 * @return
	 */
	public DateTime getBeginTime() {
		return beginTime;
	}

	/**
	 * @return
	 */
	public DateTime getEndTime() {
		return endTime;
	}

}
