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
import alma.scheduling.Define.DateTime;
/**
 * Description 
 * 
 * @version 1.10  Dec. 12, 2003
 * @author Allen Farris
 */
public class WeatherModel extends BasicComponent {
    String type;
	DiurnalModel[] model;
    RealWeatherModel[] realModel;

	/**
	 * Construct a weather model from the simulation input.
	 */
	public WeatherModel() {
	}
	
	public void initialize() {
		SimulationInput data = (SimulationInput)containerServices.getComponent(Container.SIMULATION_INPUT);
        //get type of weather model
        type ="";
        try {
            type = data.getString(Tag.weatherModelType);
        }catch(Exception e) {
	        logger.severe("WeatherModel.error " + e.toString());
        }
        try {
            //Syntax 
			//   Weather.numberFunctions = N
  			int n = data.getInt(Tag.numberWeatherFunctions);
    		String[] value = new String [n];
	    	for (int i = 0; i < n; ++i) {
		    	value[i] = data.getString(Tag.weather + "." + i);
		    }
		    ArrayList models = new ArrayList ();
            if(type.equals("real")) {
                try {
                    //format for real weather string is
                    //Weather.i= name; filename
                    String name="";
                    String filename="";
                    String[] s=null;
                    for(int i=0; i < value.length; i++){
                        s = value[i].split(";", -1);
                        s[0] = s[0].toLowerCase();
                        if(s[0].equals("wind")) {
                            models.add( new WindSpeedModel(s[1].trim()));
                        }else if(s[0].equals("rms")){ 
                            models.add(new RmsModel(s[1].trim()));
                        }else if(s[0].equals("opacity")){ 
                            models.add(new OpacityModel(s[1].trim()));
                        }else {
                            //this probably will throw a runtime error
                            models.add(new RealWeatherModel(s[1].trim(), s[0]));
                        }
                                    
        	        	((RealWeatherModel)models.get(i)).setClock((ClockSimulator)containerServices.getComponent(Container.CLOCK));
        	        //	((RealWeatherModel)models.get(i)).initialize();
                    }
	    	    	realModel = new RealWeatherModel [models.size()];
    	    		realModel = (RealWeatherModel[])models.toArray(realModel);
                }catch(Exception e){
	        		logger.severe("WeatherModel.error " + e.toString());
                    e.printStackTrace();
                }
            } else if(type.equals("diurnal")) {
        		try {
		    	    // Syntax:
        			//   Weather.i = functionName; units; parm0; parm1; parm2; shift0; shift1; period0; period1
	        		//   i <= 0 < N

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

        } catch(Exception e){
        }
   	}

    public void execute(){
        try {
            if(type.equals("real")) {
                for(int i=0; i < realModel.length; i++) {
            	    ((RealWeatherModel)realModel[i]).initialize();
                }
            } else if(type.equals("dirunal")){
                //for(int i=0; i < model.length; i++) {
                //}
            }
        } catch (Exception e){
            e.printStackTrace();
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
		String[] name =null;
        if(type.equals("real")){
            name = new String[realModel.length];
            for(int i=0; i < name.length; i++){
                name[i] = realModel[i].getName();
            }
        } else if(type.equals("diurnal")) {
            name = new String [model.length];
    		for (int i = 0; i < name.length; ++i){
	    		name[i] = model[i].getName();
            }
        }
		return name;
	}
	
	/**
	 * Get the objects that compute the functions.
	 */
	public Object[] getObjects() {
        if(type.equals("real")){
            return realModel;
        } else if(type.equals("diurnal")) {
		    return model;
        } else {
            return null;
        }
	}

    public double getCurrentOpacity(DateTime t, double freq, double el) {
        OpacityModel op=null;
        double val=0.0;
        for(int i=0; i < realModel.length; i++){
            if(realModel[i].getName().equals("opacity")){
                op = (OpacityModel)realModel[i];
                break;
            }
        }
        if(op != null){
            val = op.compute(t, freq, el);
        }
        return val;
    }

    public double getCurrentRMS(DateTime t, double freq, double el, double bl) {
        RmsModel rms =null;
        double val=0.0;
        for(int i=0; i < realModel.length; i++){
            if(realModel[i].getName().equals("rms")){
                rms = (RmsModel)realModel[i];
                break;
            }
        }
        if(rms != null){
            val = rms.compute(t, freq, el, bl);
        }
        return val;
    }
    
    public double getCurrentWindSpeed(DateTime t) {
        WindSpeedModel w=null;
        double val=0.0;
        for(int i=0; i<realModel.length; i++){
            if(realModel[i].getName().equals("wind")){
                w = (WindSpeedModel)realModel[i];
                break;
            }
        }
        if(w != null) {
            val = w.compute(t);
        }
        return val;
    }
        
	
}
