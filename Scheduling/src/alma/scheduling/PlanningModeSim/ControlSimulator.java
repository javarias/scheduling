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
 
package alma.scheduling.PlanningModeSim;

import alma.scheduling.PlanningModeSim.Define.BasicComponent;
import alma.scheduling.PlanningModeSim.Define.SimulationException;

import alma.scheduling.Define.Date;
import alma.scheduling.Define.Time;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.BestSB;
import alma.scheduling.Define.LiteSB;
import alma.scheduling.Define.SB;
import alma.scheduling.Define.ExecBlock;
import alma.scheduling.Define.Control;
import alma.scheduling.Define.Status;
import alma.scheduling.Define.SchedulingException;

import alma.scheduling.Define.Antenna;
import alma.scheduling.Define.Subarray;

import java.util.logging.Level;

/**
 * Description 
 * 
 * @version 1.10  Dec. 12, 2003
 * @author Allen Farris
 */
public class ControlSimulator extends BasicComponent implements Control {

	static int subarrayIdGenerator = 0; 

	private ClockSimulator clock;
	private ProjectManagerSimulator project;
	private TelescopeSimulator telescope;
	private ArchiveSimulator archive;
    private SimulationInput input;
    private WeatherModel weather;
	
	private int setUpTimeInSec;
	private int changeProjectTimeInSec;
	
	
	public ControlSimulator() {
	}
	
	/**
	 * An internal method used in the event an error is found in the simulation.
	 * A severe message is entered into the log and an exception is thrown.
	 * @param message The text of the error message.
	 * @throws SimulationException
	 */
	private void error(String message) throws SchedulingException {
		logger.severe("Control.error " + message);
		throw new SchedulingException("ControlSimulator " + Level.SEVERE + " " + message);
	}

	public void initialize() throws SimulationException {
		clock = (ClockSimulator)containerServices.getComponent(Container.CLOCK);
		project = (ProjectManagerSimulator)containerServices.getComponent(Container.PROJECT_MANAGER);
		telescope = (TelescopeSimulator)containerServices.getComponent(Container.TELESCOPE);
        weather = (WeatherModel)containerServices.getComponent(Container.WEATHER_MODEL);
		input = (SimulationInput)containerServices.getComponent(Container.SIMULATION_INPUT);
		archive = (ArchiveSimulator)containerServices.getComponent(Container.ARCHIVE);
		setUpTimeInSec = input.getSetUpTimeInSec();
		changeProjectTimeInSec = input.getChangeProjectTimeInSec();			
		logger.info(instanceName + ".initialized");
	}
	
	public void execute() throws SimulationException {
		// Get all antennas, place them on-line and make them unallocated.
		Antenna[] antenna = telescope.getAntenna();
		for (int i = 0; i < antenna.length; ++i) {
			antenna[i].setOnline();
			antenna[i].unAllocated();
		}
		logger.info(instanceName + ".execute complete");
	}

	private Antenna getAntenna(String antennaId) {
		Antenna[] x = telescope.getAntenna();
		for (int i = 0; i < x.length; ++i) {
			if (x[i].getAntennaId().equals( antennaId))
				return x[i];
		}
		return null;
	}

	private Subarray getArray(String name) {
		Subarray[] x = telescope.getSubarray();
		for (int i = 0; i < x.length; ++i) {
			if (x[i].getArrayName() == name)
				return x[i];
		}
		return null;
	}

	public String createArray(String[] antenna, String mode) throws SchedulingException {
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
		Subarray s = new Subarray (arrayNameGenerator(), a);
		telescope.addSubarray(s);
		return s.getArrayName();
	}
    private String arrayNameGenerator() {
        //return "Array"+(++subarrayIdGenerator);
        return ""+(++subarrayIdGenerator);
        
    }

	public void destroyArray(String name) throws SchedulingException {
		Subarray s = getArray(name);
		if (s == null)
			error("Subarray " + name + " was not found.");
		else {
			Antenna[] a = s.getAntenna();
			for (int i = 0; i < a.length; ++i)
				a[i].unAllocated();
			telescope.deleteSubarray(s);
		}
	}

    public void execSB(String name, String id) throws SchedulingException {
    }
	/* (non-Javadoc)
	 * @see ALMA.scheduling.master_scheduler.ControlProxy#execSB(java.lang.Short, java.lang.String, ALMA.scheduling.define.DateTime)
	 */
	public void execSB(String name, BestSB best, DateTime time)
		throws SchedulingException {
			
			// If the starting time is in the future, set the clock.
			if (clock.compareTo(time) == 1)
				clock.setTime(time);

			execSB(name,best);
	}
	
	/* (non-Javadoc)
	 * @see ALMA.scheduling.master_scheduler.ControlProxy#execSB(java.lang.Short, java.lang.String)
	 */
	public void execSB(String name, BestSB best) throws SchedulingException {
			// Get the subarray.
			Subarray s = getArray(name);
			if (s == null)
				error("Subarray " + name + " was not found.");
		
			// Get the SB.
			SB sb = archive.getSB(best.getBestSelection());
			if (sb == null)
				error("Scheding block " + best.getBestSelection() + " was not found.");
			
			// Check for a new project.
			if (!sb.getProject().getId().equals(s.getCurrentProject())) {
				// Apply the penalty for changing projects.
				clock.advance(changeProjectTimeInSec);
			}
			// Check for a new frequency band.
			if (!sb.getFrequencyBand().equals(s.getCurrentFrequencyBand())) {
				// Apply the penalty for changing frequency bands.
				clock.advance(setUpTimeInSec);
			}
			
			// Set the current executing unit and frequency.
			s.setCurrentSB(sb.getProject().getId(), sb.getParent().getId(), sb.getId());
			s.setCurrentFrequency(sb.getFrequencyBand(), sb.getCenterFrequency());

			// Begin the execution.
			DateTime beg = clock.getDateTime();
			
			// Inform the project manager.
			try {
                //TODO, 1 should be name but not changing that in project yet..
                // subarray name now string not int.
				project.execStart(name,sb.getId(),beg);
			} catch (SimulationException err) {
				throw new SchedulingException(err.toString());
			}
			
			// Create the execution record.
			ExecBlock ex = new ExecBlock (containerServices.getEntityId(),name);
			// Set the start time in the exec block.
			ex.setStartTime(beg);
			// Set the best unit.
			ex.setBest(best);
			// Store it in the archive.
			archive.newExec(ex);
            //Create execution statistics 
			// Advance the clock (which simulates executing the scheduling unit).
			clock.advance(sb.getMaximumTimeInSeconds());
			DateTime end = clock.getDateTime();
			// Set the end time in the exec block.
			ex.setEndTime(end,Status.COMPLETE);

			// Update it in the archive.
			archive.updateExec(ex);

			// Inform the project manager.
			try {
                //TODO, 1 should be name but not changing that in project yet..
                // subarray name now string not int.
				project.execEnd(name,sb.getId(),ex.getId(),end);
			} catch (SimulationException err) {
				throw new SchedulingException(err.toString());
			}
            double el = sb.getTarget().getElMax();
            double f = sb.getCenterFrequency();
            double bl = s.getMaxBaseline();
            ExecutionStatistics stats = createExecutionStatistics(name, sb, best, ex, s, el, f, bl);
            archive.addExecutionStatistics(stats);
	}

    private ExecutionStatistics createExecutionStatistics(
            String arrayname, SB sb, BestSB b, ExecBlock eb, Subarray sub,
            double el, double freq, double baseline) {
        LiteSB[] l = b.getLiteSBs();
        LiteSB sblite=null;
        for(int i=0; i<l.length; i++){
            if(l[i].getSBName().equals(sb.getSBName())){
                sblite = l[i];
                break;
            }
        }
        ExecutionStatistics e = new ExecutionStatistics();
        e.setExecId(eb.getId());
        e.setArrayName(arrayname);
        e.setStartTime(eb.getStatus().getStartTime());
        logger.info("StartTime recorded as: "+eb.getStatus().getStartTime());
        e.setEndTime(eb.getStatus().getEndTime());
        if(sblite != null){
            e.setScore(sblite.getScore());
            e.setSuccess(sblite.getSuccess());
            e.setRank(sblite.getRank());
        }else{
            e.setScore(0.0);
            e.setSuccess(0.0);
            e.setRank(0.0);
        }
        e.setProjectName(sb.getProject().getProjectName());
        e.setSBName(sb.getSBName());
        e.setPriority(sb.getScientificPriority().toString());
        e.setFrequency(sub.getCurrentFrequency());
        e.setFrequencyBandName(sb.getFrequencyBand().getName());
        e.setRA(sb.getTarget().getMax().getRa());
        e.setDEC(sb.getTarget().getMax().getDec());
        e.setWeatherConstraintName(sb.getWeatherConstraintName());
        e.setElevation(sb.getElevation(clock.getDateTime(), input.getSite()));
        e.setLSTRise(sb.getTarget().getLstRise());
        e.setLSTMax(sb.getTarget().getLstMax());
        e.setLSTSet(sb.getTarget().getLstSet());
        //gotta get currect conditions
        try {
            e.setOpacity(weather.getCurrentOpacity(clock.getDateTime(), freq, el));
            e.setRMS(weather.getCurrentRMS(clock.getDateTime(), freq, el, baseline));
            e.setWind(weather.getCurrentWindSpeed(clock.getDateTime()));
        } catch(Exception ex){}
        Date d1 = input.getBeginTime().getDate();
        Date d2 = input.getEndTime().getDate();
        Time t= new Time(0,0,0.0);
        DateTime dt1 = new DateTime(d1,t);
        DateTime dt2 = new DateTime(d2,t);
        e.setLSTatVeryStartMidnight(dt1.getLocalSiderealTime());
        e.setLSTatVeryEndMidnight(dt2.getLocalSiderealTime());
        return e;
    }
	/* (non-Javadoc)
	 * @see ALMA.scheduling.master_scheduler.ControlProxy#getActiveSubarray()
	 */
	public String[] getActiveArray() throws SchedulingException {
		Subarray[] x = telescope.getSubarray();
		String[] s = new String [x.length];
		for (int i = 0; i < x.length; ++i) {
			s[i] = x[i].getArrayName();
		}
		return s;
	}

	/* (non-Javadoc)
	 * @see ALMA.scheduling.master_scheduler.ControlProxy#getIdleAntennas()
	 */
	public String[] getIdleAntennas() throws SchedulingException {
		Antenna[] x = telescope.getAntenna();
		int count = 0;
		for (int i = 0; i < x.length; ++i) {
			if (x[i].isIdle())
				++count;
		}
		String[] s = new String [count];
		int n = 0;
		for (int i = 0; i < x.length; ++i) {
			if (x[i].isIdle()) {
				s[n++] = x[i].getAntennaId();
			}
		}
		return s;
	}

	/* (non-Javadoc)
	 * @see ALMA.scheduling.master_scheduler.ControlProxy#getSubarrayAntennas(int)
	 */
	public String[] getArrayAntennas(String name)
		throws SchedulingException {
		// Get the subarray.
		Subarray s = getArray(name);
		if (s == null)
			error("Subarray " + name + " was not found.");
		Antenna[] a = s.getAntenna();
		String[] x = new String [a.length];
		for (int i = 0; i < x.length; ++i)
			x[i] = a[i].getAntennaId();
		return x;
	}

	/* (non-Javadoc)
	 * @see ALMA.scheduling.master_scheduler.ControlProxy#stopSB(java.lang.Short, java.lang.String)
	 */
	public void stopSB(String name, String id)
		throws SchedulingException {
		// Don't need it in the simulation.
	}

        public void stopSBNow(String name, String id) throws SchedulingException {}

    public void setAntennaOfflineNow(String antennaId) 
        throws SchedulingException {
		// Don't need it in the simulation.
    }
    public void setAntennaOnlineNow(String antennaId) 
        throws SchedulingException {
		// Don't need it in the simulation.
    }
    public void stopAllScheduling() throws SchedulingException {}
}

