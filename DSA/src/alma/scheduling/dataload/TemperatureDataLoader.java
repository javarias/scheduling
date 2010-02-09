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
 * "@(#) $Id: TemperatureDataLoader.java,v 1.1 2010/02/09 00:50:04 rhiriart Exp $"
 */
package alma.scheduling.dataload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import alma.scheduling.datamodel.weather.TemperatureHistRecord;
import alma.scheduling.datamodel.weather.dao.WeatherHistoryDAO;

public class TemperatureDataLoader implements DataLoader {

    private String file;
    
    private WeatherHistoryDAO dao;

    private int maxNumRecords;
    
    public void setFile(String file) {
        this.file = file;
    }

    public void setDao(WeatherHistoryDAO dao) {
        this.dao = dao;
    }
    
    public void setMaxNumRecords(int maxNumRecords) {
        this.maxNumRecords = maxNumRecords;
    }
    
    @Override
    public void load() {
        System.out.println("loading file " + file);
        try {
            List<TemperatureHistRecord> records = new ArrayList<TemperatureHistRecord>();
            File file = new File(this.file);
            FileReader fr = new FileReader(file);
            BufferedReader in = new BufferedReader(fr);
            String commentToken = "#";
            String line;
            int count = 0;
            while ((line = in.readLine()) != null && count < maxNumRecords) {
                if (line.startsWith(commentToken)) {
                    continue;
                }
                line = line.trim();
                String[] tokens = line.split("\\s+");
                if (tokens.length != 4) {
                    // fail("invalid source format");
                }
                Double time = Double.valueOf(tokens[0]);
                Double temp = Double.valueOf(tokens[1]);
                Double rms = Double.valueOf(tokens[2]);
                Double slope = Double.valueOf(tokens[3]);            
                TemperatureHistRecord record = new TemperatureHistRecord(time, temp, rms, slope);
                records.add(record);
                count++;
            }
            dao.loadTemperatureHistory(records);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
