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
 * File ControlSimulator.java
 */
 
package alma.scheduling.planning_mode_sim.simulator;

import alma.scheduling.planning_mode_sim.define.BasicComponent;

import alma.scheduling.planning_mode_sim.define.DateTime;
import alma.scheduling.planning_mode_sim.define.SUnit;
import alma.scheduling.planning_mode_sim.define.SExec;
import alma.scheduling.planning_mode_sim.define.ComponentState;
import alma.scheduling.planning_mode_sim.master_scheduler.ControlProxy;
import alma.scheduling.planning_mode_sim.master_scheduler.SchedulingException;
import alma.scheduling.planning_mode_sim.scheduler.Scheduler;
import alma.scheduling.planning_mode_sim.define.acs.component.ComponentLifecycleException;

import java.util.logging.Level;
import java.util.ArrayList;

/**
 * Description 
 * 
 * @version 1.00  Jul 18, 2003
 * @author Allen Farris
 */
public class ControlSimulator extends BasicComponent implements ControlProxy {

	static int subarrayIdGenerator = 0; 

	private ClockSimulator clock;
	private WeatherModel weather;
	
	private Antenna[] antennaList;
	private ArrayList subarray;
	private ArchiveSimulator archive;
	private Scheduler scheduler;
	
	private int setUpTimeInSec;
	private int changeProjectTimeInSec;
	private String currentProject;
	private int currentBand;
	
	
	public ControlSimulator(SimulationInput data) {
		super(data);
		currentProject = " ";
		currentBand = -1;
	}
	
	/**
	 * An internal method used in the event an error is found in the simulation.
	 * A severe message is entered into the log and an exception is thrown.
	 * @param message The text of the error message.
	 * @throws SimulationException
	 */
	private void error(String message) throws SchedulingException {
		m_state.setState(ComponentState.ERROR);
		m_logger.severe("Control.error " + message);
		throw new SchedulingException("Control " + Level.SEVERE + " " + message);
	}

	public void initialize() throws ComponentLifecycleException {
		super.initialize();
		try {
			clock = (ClockSimulator)m_containerServices.getComponent(Container.CLOCK);
			//weather = new WeatherModel ();
			// There are no subarrays and all antennas are idle.
			subarray = new ArrayList ();
			int numberAntennas = data.getInt(Tag.numberAntennas);
			antennaList = new Antenna [numberAntennas];
			for (int i = 0; i < numberAntennas; ++i) {
				antennaList[i] = new Antenna (i);
			}
			archive = (ArchiveSimulator)m_containerServices.getComponent(Container.ARCHIVE);
			setUpTimeInSec = data.getSetUpTimeInSec();
			changeProjectTimeInSec = data.getChangeProjectTimeInSec();
		} catch (Exception err) {
			m_state.setState(ComponentState.ERROR);
			m_logger.severe("Control.error " + err.toString());
			throw new ComponentLifecycleException("Control " + Level.SEVERE + " " + err.toString());
		}
	}
	
	private Antenna getAntenna(int antennaId) {
		for (int i = 0; i < antennaList.length; ++i) {
			if (antennaList[i].getAntennaId() == antennaId)
				return antennaList[i];
		}
		return null;
	}

	private Subarray getSubarray(int subarrayId) {
		int i = 0;
		Subarray s = null;
		for (; i < subarray.size(); ++i) {
			s = (Subarray)subarray.get(i);
			if (s.getSubarrayId() == subarrayId)
				break;
		}
		return s;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.ControlProxy#createSubarray(short[])
	 */
	public short createSubarray(short[] antenna) throws SchedulingException {
		Antenna x = null;
		for (int i = 0; i < antenna.length; ++i) {
			x = getAntenna(antenna[i]);
			if (x == null)
				error("Cannot create subarray!  No such antenna as " + antenna[i]);
			if (x.isAllocated())
				error("Cannot create subarray!  Antenna " + antenna[i] + " is already allocated.");
			if (!x.isIdle())
				error("Cannot create subarray!  Antenna " + antenna[i] + " is busy.");
			if (x.isManual())
				error("Cannot create subarray!  Antenna " + antenna[i] + " is in manual mode.");
			if (!x.isOnline())
				error("Cannot create subarray!  Antenna " + antenna[i] + " is off-line.");
		}
		Antenna[] a = new Antenna [antenna.length];
		for (int i = 0; i < antenna.length; ++i)
			a[i] = getAntenna(antenna[i]);
		Subarray s = new Subarray (a);
		subarray.add(s);
		return s.getSubarrayId();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.ControlProxy#destroySubarray(short)
	 */
	public void destroySubarray(short subarrayId) throws SchedulingException {
		Subarray s = getSubarray(subarrayId);
		if (s == null)
			error("Subarray " + subarrayId + " was not found.");
		else {
			Antenna[] a = s.getAntenna();
			for (int i = 0; i < a.length; ++i)
				a[i].unAllocated();
			subarray.remove(s);
		}
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.ControlProxy#execSB(java.lang.Short, java.lang.String, alma.scheduling.define.DateTime)
	 */
	public void execSB(short subarrayId, String id, DateTime time)
		throws SchedulingException {
			
			// If the starting time is in the future, set the clock.
			if (clock.compareTo(time) == 1)
				clock.setTime(time);

			execSB(subarrayId,id);
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.ControlProxy#execSB(java.lang.Short, java.lang.String)
	 */
	public void execSB(short subarrayId, String id)
		throws SchedulingException {
			// Get the subarray.
			Subarray s = getSubarray(subarrayId);
			if (s == null)
				error("Subarray " + subarrayId + " was not found.");
		
			// Get the SUnit.
			SUnit sb = archive.getSUnit(id);
			if (sb == null)
				error("Scheding block " + id + " was not found.");
			
			// Check for a new project.
			if (!sb.getProjectId().equals(currentProject)) {
				clock.advance(changeProjectTimeInSec);
				currentProject = sb.getProjectId();
			}
			// Check for a new frequency band.
			if (sb.getFrequencyBand() != currentBand) {
				clock.advance(setUpTimeInSec);
				currentBand = sb.getFrequencyBand();
			}
						
			// Create the execution record.
			SExec ex = new SExec ();
			DateTime beg = clock.getDateTime();
			// Set the start time.
			ex.setStartTime(beg);
			// Set the subarray-id.
			ex.setSubarrayId(subarrayId);
			// Link it to the parent.
			sb.addMember(ex);
			// Store it in the archive.
			archive.newSExec(ex);

			// Advance the clock.
			clock.advance(sb.getMaximumTimeInSeconds());
			DateTime end = clock.getDateTime();
			// Set the end time.
			ex.setEndTime(end);

			// Update it in the archive.
			archive.updateSExec(ex);

			// Record the beg and end times in the output file.
			scheduler.generateOutputRecord(subarrayId,sb,ex);			
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.ControlProxy#getActiveSubarray()
	 */
	public short[] getActiveSubarray() throws SchedulingException {
		short[] s = new short [subarray.size()];
		for (int i = 0; i < subarray.size(); ++i) {
			s[i] = getSubarray(i).getSubarrayId();
		}
		return s;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.ControlProxy#getIdleAntennas()
	 */
	public short[] getIdleAntennas() throws SchedulingException {
		int count = 0;
		for (int i = 0; i < antennaList.length; ++i) {
			if (antennaList[i].isIdle())
				++count;
		}
		short[] s = new short [count];
		int n = 0;
		for (int i = 0; i < antennaList.length; ++i) {
			if (antennaList[i].isIdle()) {
				s[n++] = (short)antennaList[i].getAntennaId();
			}
		}
		return s;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.ControlProxy#getSubarrayAntennas(short)
	 */
	public short[] getSubarrayAntennas(short subarrayId)
		throws SchedulingException {
		// Get the subarray.
		Subarray s = getSubarray(subarrayId);
		if (s == null)
			error("Subarray " + subarrayId + " was not found.");
		Antenna[] a = s.getAntenna();
		short[] x = new short [a.length];
		for (int i = 0; i < x.length; ++i)
			x[i] = (short)a[i].getAntennaId();
		return x;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.ControlProxy#stopSB(java.lang.Short, java.lang.String)
	 */
	public void stopSB(short subarrayId, String id)
		throws SchedulingException {
		// Don't need it in the simulation.
	}

	/**
	 * @param scheduler
	 */
	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

}

class Antenna {
	private int antennaId;
	private int subarrayId;
	private boolean allocated;
	private boolean idle;
	private boolean manual;
	private boolean online;
	
	public Antenna (int antennaId) {
		this.antennaId = antennaId;
		this.subarrayId = -1;
		this.allocated = false;
		this.idle = true;
		this.manual = false;
		this.online = true;
	}
	
	public boolean isAllocated() {
		return allocated;
	}
	public boolean isIdle() {
		return idle;
	}
	public boolean isManual() {
		return manual;
	}
	public boolean isOnline() {
		return online;
	}
	public int getSubarrayId() {
		return subarrayId;
	}
	public int getAntennaId() {
		return antennaId;
	}

	public void setAllocated(int subarrayId) {
		allocated = true;
		this.subarrayId = subarrayId;
	}
	public void unAllocated() {
		allocated = false;
		this.subarrayId = -1;
		setIdle();
	}

	public void setIdle() {
		idle = true;
	}
	public void setBusy() {
		idle = false;
	}

	public void setManual() {
		manual = true;
		unAllocated();
	}
	public void setAutomatic() {
		manual = false;
	}

	public void setOnline() {
		online = true;
	}
	public void setOffline() {
		online = false;
		unAllocated();
	}

}

class Subarray {
	private int subarrayId;
	boolean idle;
	private Antenna[] antenna;
	
	public Subarray (Antenna[] antenna) {
		subarrayId = ++ControlSimulator.subarrayIdGenerator;
		idle = false;
		this.antenna = antenna;
	}

	public Antenna[] getAntenna() {
		return antenna;
	}
	public boolean isIdle() {
		return idle;
	}
	public short getSubarrayId() {
		return (short)subarrayId;
	}

	public void setIdle() {
		idle = true;
		for (int i = 0; i < antenna.length; ++i)
			antenna[i].setIdle();
	}
	public void setBusy() {
		idle = false;
		for (int i = 0; i < antenna.length; ++i)
			antenna[i].setBusy();
	}

}
