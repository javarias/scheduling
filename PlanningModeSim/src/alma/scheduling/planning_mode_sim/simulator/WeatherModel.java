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
 * File WeatherModel.java
 */
 
package alma.scheduling.planning_mode_sim.simulator;

import alma.scheduling.planning_mode_sim.define.BasicComponent;
import alma.scheduling.planning_mode_sim.define.ComponentState;
import alma.scheduling.planning_mode_sim.master_scheduler.SchedulingException;
import alma.scheduling.planning_mode_sim.define.acs.component.ComponentLifecycleException;

import java.util.logging.Level;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.util.ArrayList;

/**
 * Description 
 * 
 * @version 1.00  May 21, 2003
 * @author Allen Farris
 */
public class WeatherModel extends BasicComponent {
	DiurnalModel[] model;

	/**
	 * Construct a weather model from the simulation input.
	 */
	public WeatherModel(SimulationInput data) {
		super(data);
	}
	
	public void initialize() throws ComponentLifecycleException {
		super.initialize();
		try {
			// Get the functions to model from the simulation input.
			// Syntax:
			//   Weather.numberFunctions = N
			//   Weather.i = functionName; units; parm0; parm1; parm2; shift0; shift1; period0; period1
			//   i <= 0 < N
			int n = data.getInt(Tag.numberWeatherFunctions);
			String[] value = new String [n];
			for (int i = 0; i < n; ++i) {
				value[i] = data.getString(Tag.weather + "." + i);
			}

			// OK, we now have the string values for each of the functions.
			// Now, create DiurnalModels for each of the weather functions, and
			// set the simulation clock in each of the DiurnalModels.
			StringTokenizer token = null;
			String name = "";
			String units = "";
			String s = "";
			double parm0 = 0.0;
			double parm1 = 0.0;
			double parm2 = 0.0;
			double shift0 = 0.0;
			double shift1 = 0.0;
			double period0 = 0.0;
			double period1 = 0.0;
			DiurnalModel m = null;
			ArrayList models = new ArrayList ();
			for (int i = 0; i < value.length; ++i) {
				token = new StringTokenizer(value[i],";");
				try {
					name = token.nextToken().trim();
					units = token.nextToken().trim();
					s = token.nextToken().trim();
					parm0 = Double.parseDouble(s);
					s = token.nextToken().trim();
					parm1 = Double.parseDouble(s);
					s = token.nextToken().trim();
					parm2 = Double.parseDouble(s);
					s = token.nextToken().trim();
					shift0 = Double.parseDouble(s);
					s = token.nextToken().trim();
					shift1 = Double.parseDouble(s);
					s = token.nextToken().trim();
					period0 = Double.parseDouble(s);
					s = token.nextToken().trim();
					period1 = Double.parseDouble(s);
				} catch (NumberFormatException err) {
					error("Invalid number format in " + s);
				} catch (NoSuchElementException err) {
					error("Missing element in eather function string: " + value[i]);
				}
				m = new DiurnalModel();
				m.setName(name);
				m.setUnits(units);
				m.setParm0(parm0);
				m.setParm1(parm1);
				m.setParm2(parm2);
				m.setShift0(shift0);
				m.setShift1(shift1);
				m.setPeriod0(period0);
				m.setPeriod1(period1);
				m.setClock((ClockSimulator)m_containerServices.getComponent(Container.CLOCK));
				models.add(m);
			}
			// Save the DiurnalModels as a array.
			model = new DiurnalModel [models.size()];
			model = (DiurnalModel[])models.toArray(model);
			
		} catch (Exception err) {
			m_state.setState(ComponentState.ERROR);
			m_logger.severe("WeatherModel.error " + err.toString());
			throw new ComponentLifecycleException("WeatherModel " + Level.SEVERE + " " + err.toString());
		}
	}

	/**
	 * An internal method used in the event an error is found in the simulation.
	 * A severe message is entered into the log and an exception is thrown.
	 * @param message The text of the error message.
	 * @throws SimulationException
	 */
	private void error(String message) throws SchedulingException {
		m_state.setState(ComponentState.ERROR);
		m_logger.severe("WeatherModel.error " + message);
		throw new SchedulingException("WeatherModel " + Level.SEVERE + " " + message);
	}

	/**
	 * Get the list of function names.
	 */
	public String[] getFunctionNames() {
		String[] name = new String [model.length];
		for (int i = 0; i < name.length; ++i)
			name[i] = model[i].getName();
		return name;
	}
	
	/**
	 * Get the objects that compute the functions.
	 */
	public Object[] getObjects() {
		return model;
	}
	
}
