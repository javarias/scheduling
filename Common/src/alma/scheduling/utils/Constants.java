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
 */
package alma.scheduling.utils;

/**
 * Constants used for system temperature and sensitiviy calculations.
 */
public class Constants {
    /** Boltzmann constant (Jy/K) */
	public static final double BOLTZMANN = 1.38e-16 * 1.0e23;
    /** Plank constant (J*s) */
	public static final double PLANCK    = 6.626e-34;
    /** Speed of light (m/s) */
	public static final double LIGHT_SPEED= 2.99792458e8;
    /** Chajnantor latitude */
    public static final double CHAJNANTOR_LATITUDE = -23.022778;
    /** Chajnantor longitude */
    public static final double CHAJNANTOR_LONGITUDE = -67.755;

    // Component and interface names
    public static final String CONTROL_MASTER_URL = "CONTROL/MASTER";
    public static final String SCHEDULING_MASTER_URL = "SCHEDULING_MASTERSCHEDULER";
    public static final String MASTER_SCHEDULING_COMP_URL = "SCHEDULING_MASTER_COMP";
    
    public static final String QUICKLOOK_IF = "IDL:alma/pipelineql/QlDisplayManager:1.0";
    
    public static final String WEATHER_STATION_IF = "IDL:alma/Control/WeatherStationController:1.0";
    public static final String WEATHER_STATION_SIM_IF = "IDL:alma/Control/CurrentWeather:1.0";  
}
