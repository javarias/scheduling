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
 * "@(#) $Id: WeatherHistoryDAO.java,v 1.5 2012/05/02 23:22:01 javarias Exp $"
 */
package alma.scheduling.datamodel.weather.dao;

import java.util.Date;
import java.util.List;

import alma.scheduling.datamodel.weather.HumidityHistRecord;
import alma.scheduling.datamodel.weather.OpacityHistRecord;
import alma.scheduling.datamodel.weather.PathFluctHistRecord;
import alma.scheduling.datamodel.weather.TemperatureHistRecord;
import alma.scheduling.datamodel.weather.WindSpeedHistRecord;

public interface WeatherHistoryDAO {

    public void loadTemperatureHistory(List<TemperatureHistRecord> records);
    
    public void loadHumidityHistory(List<HumidityHistRecord> records);
    
    public void loadOpacityHistory(List<OpacityHistRecord> records);
    
    public void loadWindSpeedHistory(List<WindSpeedHistRecord> records);
    
    public void loadPathFluctHistory(List<PathFluctHistRecord> records);
    
    public HumidityHistRecord getHumidityForTime(Date ut);
    
    public TemperatureHistRecord getTemperatureForTime(Date ut);
    
    /**
     * Each record in the historic weather data files is identified by a time column,
     * which is relative with respect to the point in time when the simulation started.
     * This point is set with this function.
     * 
     * @param ut Start time
     */
    public void setSimulationStartTime(Date ut);
}
