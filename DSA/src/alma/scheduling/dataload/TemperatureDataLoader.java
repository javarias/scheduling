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
 * "@(#) $Id: TemperatureDataLoader.java,v 1.4 2010/03/13 02:56:15 rhiriart Exp $"
 */
package alma.scheduling.dataload;

import java.util.ArrayList;
import java.util.List;

import alma.scheduling.dataload.WeatherDataReader.WeatherData;
import alma.scheduling.datamodel.weather.TemperatureHistRecord;

public class TemperatureDataLoader extends WeatherParameterDataLoader {

    public TemperatureDataLoader() {
        super();
    }

    @Override
    public void load() {
        try {
            List<TemperatureHistRecord> records = new ArrayList<TemperatureHistRecord>();
            WeatherData wd;
            while ((wd = getNextWeatherDatum()) != null) {
                TemperatureHistRecord record = new TemperatureHistRecord(wd.getTime(),
                        wd.getValue(), wd.getRms(), wd.getSlope());
                records.add(record);
            }
            dao.loadTemperatureHistory(records);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub
        
    }
}