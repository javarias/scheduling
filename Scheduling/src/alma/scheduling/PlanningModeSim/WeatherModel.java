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
 
package alma.scheduling.PlanningModeSim;

import alma.scheduling.PlanningModeSim.Define.BasicComponent;

import java.util.ArrayList;

/**
 * Description 
 * 
 * @version 1.10  Dec. 12, 2003
 * @author Allen Farris
 */
public class WeatherModel extends BasicComponent {
	DiurnalModel[] model;
    RealWeatherModel realModel;

	/**
	 * Construct a weather model from the simulation input.
	 */
	public WeatherModel() {
	}
	
	public void initialize() {
		SimulationInput data = (SimulationInput)containerServices.getComponent(Container.SIMULATION_INPUT);
        //get type of weather model
        String type ="";
        try {
            type = data.getString(Tag.weatherModelType);
        }catch(Exception e) {
	        logger.severe("WeatherModel.error " + e.toString());
        }
        if(type.equals("real")) {
            try {
                String f1 = data.getString(Tag.windFile);
                String f2 = data.getString(Tag.rmsFile);
                String f3 = data.getString(Tag.opacityFile);
                realModel = new RealWeatherModel(f1,f2,f3);
    	    	realModel.setClock((ClockSimulator)containerServices.getComponent(Container.CLOCK));
            }catch(Exception e){
	    		logger.severe("WeatherModel.error " + e.toString());
            }
        } else if(type.equals("diurnal")) {
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
			    String name = "";
    			String units = "";
	    		double parm0 = 0.0;
		    	double parm1 = 0.0;
			    double parm2 = 0.0;
    			double shift0 = 0.0;
	    		double shift1 = 0.0;
		    	double period0 = 0.0;
			    double period1 = 0.0;
    			DiurnalModel m = null;
	    		String[] s = null;
		    	ArrayList models = new ArrayList ();
			    for (int i = 0; i < value.length; ++i) {
    				s = value[i].split(";",-1);
	    			if (s.length < 9) {
		    			logger.severe("WeatherModel.error Invalid number of weather model parameters.");
			    		System.exit(0); // Must do better than this.
				    }
				    try {
		        		name = s[0].trim();
	    				units = s[1].trim();
    					parm0 = Double.parseDouble(s[2]);
				       	parm1 = Double.parseDouble(s[3]);
			    		parm2 = Double.parseDouble(s[4]);
		    			shift0 = Double.parseDouble(s[5]);
	    				shift1 = Double.parseDouble(s[6]);
    					period0 = Double.parseDouble(s[7]);
					    period1 = Double.parseDouble(s[8]);
				    } catch (NumberFormatException err) {
			    		error("Invalid number format in " + s);
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
	    			m.setClock((ClockSimulator)containerServices.getComponent(Container.CLOCK));
    				models.add(m);
			    }
		    	// Save the DiurnalModels as a array.
	    		model = new DiurnalModel [models.size()];
    			model = (DiurnalModel[])models.toArray(model);
			    logger.info(instanceName + ".initialized");
		    } catch (Exception err) {
	    		logger.severe("WeatherModel.error " + err.toString());
    		}
        }
	}

	/**
	 * An internal method used in the event an error is found in the simulation.
	 * A severe message is entered into the log and an exception is thrown.
	 * @param message The text of the error message.
	 * @throws SimulationException
	 */
	private void error(String message) {
		logger.severe("WeatherModel.error " + message);
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
