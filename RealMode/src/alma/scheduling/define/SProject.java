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
 * File SProject.java
 */
 
package ALMA.scheduling.define;

import java.util.Vector;
import alma.entity.xmlbinding.obsproject.*;
import alma.entity.xmlbinding.schedblock.*;

/**
 * An SProject is an observing project as viewed by the
 * scheduling subsystem. 
 * 
 * @version 1.00  Jun 4, 2003
 * @author Allen Farris
 */
public class SProject implements ArchiveEntity {
    private ObsProject proj;
	private String id;
	private STime timeOfCreation;
	private STime timeOfUpdate;
	
	private String obsProjectId;
	private String proposalRef;
	private String projectName;
	private String projectVersion;
	private String PI;
	private SUnitSet program;
	
	private STime startTime;
	private STime endTime;
	private int totalRequiredTimeInSeconds;
	private int totalUsedTimeInSeconds;
	private int totalUnits;
	private int numberUnitsCompleted;
	private int numberUnitsFailed;
	private Status projectStatus;
	private boolean breakpoint;
    /********TEMPORARY!!!**********/
    private Vector sbs;

	/**
	 * Construct an SProject.
	 */
	public SProject() {
		obsProjectId = "";
		proposalRef = "";
		projectName = "";
		projectVersion = "";
		PI = "";
		program = new SUnitSet ();
		startTime = new STime ();
		endTime = new STime ();
		totalRequiredTimeInSeconds = 0;
		totalUsedTimeInSeconds = 0;
		totalUnits = 0;
		numberUnitsCompleted = 0;
		numberUnitsFailed = 0;
		projectStatus = new Status (Status.NOTDEFINED);
		breakpoint = false;
	}

    public SProject(ObsProject p) {
        this();
        this.proj = p;
        this.obsProjectId = p.getObsProjectEntity().getEntityId();
        this.id = obsProjectId;
        ObsUnitSetT[] tmp = proj.getObsProgram().getObsUnitSetTChoice().getObsUnitSet();
        /********TEMPORARY!!!**********/
        sbs = new Vector();
        
    }

    public void linkSUnitToProject(SUnit sb) {
        sbs.add(sb);
    }

	public void setMemberLink() {
		program.setMemberIndex(0);
		program.setId(getId() + ".0");
		program.setTimeCreated(getTimeCreated());
		program.setTimeUpdated(getTimeCreated());
		program.setProjectId(getId());
		program.setParentId(getId());
		program.setProject(this);
		program.setParent(null);
		// The program's parentId is set to the project's id, but the
		// program's parent is null, because it has no UnitSetMember parent.
		program.setMemberLink(program);
	}

    ////////////////////////////////////////////////////
    //  Stuff to see if project is completed or not.  //
    ////////////////////////////////////////////////////


	////////////////////////////////////////////////////
	// Implementation of the ArchiveEntity interface. //
	////////////////////////////////////////////////////

	/**
	 * Get the archive identifier.
	 * @return The archive identifier as a String.
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Get the time this archive entry was created.
	 * @return The time this archive entry was created as an STime.
	 */
	public STime getTimeCreated() {
		return timeOfCreation;
	}
	
	/**
	 * Get the time this archive entry was last updated.
	 * @return The time this archive entry was last updated as an STime.
	 */
	public STime getTimeUpdated() {
		return timeOfUpdate;
	}
	
	/**
	 * Set the archive identifier.
	 * @param id The id of this archive entity.
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Set the time this archive entry was created.
	 * @param t The time this archive entry was created.
	 */
	public void setTimeCreated(STime t) {
		this.timeOfCreation = t;
	}
	
	/**
	 * Set the time this archive entry was last updated.
	 * @param t The time this archive entry was last updated.
	 */
	public void setTimeUpdated(STime t) {
		this.timeOfUpdate = t;
	}

	////////////////////
	// Getter methods //
	////////////////////

	/**
	 * @return
	 */
	public boolean isBreakpoint() {
		return breakpoint;
	}

	/**
	 * @return
	 */
	public STime getEndTime() {
		return endTime;
	}

	/**
	 * @return
	 */
	public int getNumberUnitsCompleted() {
		return numberUnitsCompleted;
	}

	/**
	 * @return
	 */
	public int getNumberUnitsFailed() {
		return numberUnitsFailed;
	}

	/**
	 * @return
	 */
	public String getObsProjectId() {
		return obsProjectId;
	}

	/**
	 * @return
	 */
	public String getPI() {
		return PI;
	}

	/**
	 * @return
	 */
	public String getProjectName() {
		return projectName;
	}

	/**
	 * @return
	 */
	public Status getProjectStatus() {
		return projectStatus;
	}

	/**
	 * @return
	 */
	public String getProjectVersion() {
		return projectVersion;
	}

	/**
	 * @return
	 */
	public String getProposalRef() {
		return proposalRef;
	}

	/**
	 * @return
	 */
	public STime getStartTime() {
		return startTime;
	}

	/**
	 * @return
	 */
	public int getTotalRequiredTimeInSeconds() {
		return totalRequiredTimeInSeconds;
	}

	/**
	 * @return
	 */
	public int getTotalUnits() {
		return totalUnits;
	}

	/**
	 * @return
	 */
	public int getTotalUsedTimeInSeconds() {
		return totalUsedTimeInSeconds;
	}

	/**
	 * @return
	 */
	public SUnitSet getProgram() {
		return program;
	}

	////////////////////
	// Setter methods //
	////////////////////

	/**
	 * @param b
	 */
	public void setBreakpoint(boolean b) {
		breakpoint = b;
	}

	/**
	 * @param time
	 */
	public void setEndTime(STime time) {
		endTime = time;
	}

	/**
	 * @param i
	 */
	public void setNumberUnitsCompleted(int i) {
		numberUnitsCompleted = i;
	}

	/**
	 * @param i
	 */
	public void setNumberUnitsFailed(int i) {
		numberUnitsFailed = i;
	}

	/**
	 * @param string
	 */
	public void setObsProjectId(String string) {
		obsProjectId = string;
	}

	/**
	 * @param string
	 */
	public void setPI(String string) {
		PI = string;
	}

	/**
	 * @param string
	 */
	public void setProjectName(String string) {
		projectName = string;
	}

	/**
	 * @param status
	 */
	public void setProjectStatus(Status status) {
		projectStatus = status;
	}

	/**
	 * @param string
	 */
	public void setProjectVersion(String string) {
		projectVersion = string;
	}

	/**
	 * @param string
	 */
	public void setProposalRef(String string) {
		proposalRef = string;
	}

	/**
	 * @param time
	 */
	public void setStartTime(STime time) {
		startTime = time;
	}

	/**
	 * @param i
	 */
	public void setTotalRequiredTimeInSeconds(int i) {
		totalRequiredTimeInSeconds = i;
	}

	/**
	 * @param i
	 */
	public void setTotalUnits(int i) {
		totalUnits = i;
	}

	/**
	 * @param i
	 */
	public void setTotalUsedTimeInSeconds(int i) {
		totalUsedTimeInSeconds = i;
	}

	/**
	 * @param set
	 */
	public void setProgram(SUnitSet set) {
		program = set;
	}

	public String toString() {
		StringBuffer s = new StringBuffer ();
		s.append("SProject (" + getId() + "," + getTimeCreated() + "," + getTimeUpdated() + ")");
		s.append("\n\t" + getProjectName() + " " + getProjectVersion() + " " + getObsProjectId() + " " +
				getProposalRef() + " " + getPI() + "\nProgram");
		s.append("\n" + program);
		return s.toString();
	}

	public static void main(String[] arg) {
        /*
		System.out.println("Test of SProject");
		java.io.PrintStream out = null;
		try {
			out = new java.io.PrintStream (new java.io.FileOutputStream (new java.io.File ("ALMA\\scheduling\\define","out.txt")));
		} catch (Exception err) {
			err.printStackTrace();
			System.exit(0);
		}

		SProject prj = new SProject ();
		prj.setObsProjectId("0001");
		prj.setProposalRef("0002");
		prj.setProjectName("TestProject");
		prj.setProjectVersion("v01");
		prj.setPI("Allen Farris");
		prj.setStartTime(new STime(2003,9,5,13,36,30));
		prj.setEndTime(new STime(2003,9,6,13,36,30));
		SUnitSet program = new SUnitSet ();
		prj.setProgram(program);
		
		program.setScientificPriority(Priority.HIGH);
		program.setUserPriority(Priority.HIGH);
		
		// Use the VLA as the default clock.
		Clock c = new alma.scheduling.simulator.ClockSimulator (107.6177275,34.0787491666667,-6);
		DateTime.setDefaultClock(c);
		
		SUnitSet[] set = new SUnitSet [3];
		for (int i = 0; i < set.length; ++i) {
			set[i] = new SUnitSet ();
			program.addMember(set[i]);
			SUnit[] unit = new SUnit [3];
			for (int j = 0; j < unit.length; ++j) {
				unit[j] = new SUnit ();
				unit[j].setImagingScript("myImagingScript");
				unit[j].setObservingScript("myObervingScript");
				unit[j].setMaximumTimeInSeconds(1800);
				unit[j].setSchedBlockId("0004" + i + "_" + j);
				Equatorial coord = new Equatorial (9 + i + j, 53, 0.0, 54 + i + j, 54, 0.0);
				Target t = new Target(coord,0.1,0.2);
				unit[j].setTarget(t);
				set[i].addMember(unit[j]);
			}
		}
		
		prj.setId("0000001");
		prj.setTimeCreated(new STime(2003,9,5,0,0,0));
		prj.setTimeUpdated(new STime(2003,9,5,1,0,0));
		prj.setMemberLink();
		
		out.println("Project " + prj.getId());
		SUnitSet prog = prj.getProgram();
		out.println("Program " + prog.getId());
		MemberOf[] mem = prog.getMember();
		SUnitSet x = null;
		MemberOf[] xmem = null;
		for (int i = 0; i < mem.length; ++i) {
			out.println("\t member: name " + mem[i].getMemberIndex() + " id " + mem[i].getId());
			if (mem[i] instanceof SUnitSet) {
				xmem = ((SUnitSet)mem[i]).getMember();
				for (int j = 0; j < xmem.length; ++j) {
					out.println("\t\t member: name " + xmem[j].getMemberIndex() + " id " + xmem[j].getId());
				}
			}
		}

		out.println("The Project.");
		out.println();
		out.println(prj.toString());

		System.out.println("End test of SProject");		
		*/
	}

}
