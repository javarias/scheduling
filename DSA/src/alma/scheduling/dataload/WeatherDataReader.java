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
 * "@(#) $Id: WeatherDataReader.java,v 1.2 2010/03/02 23:19:15 javarias Exp $"
 */
package alma.scheduling.dataload;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class WeatherDataReader extends BufferedReader {

    private static final String COMMENT_CHAR = "#";
    
    public final class WeatherData {
        private final double time;
        private final double value;
        private final double rms;
        private final double slope;
        
        public WeatherData(double time, double value, double rms, double slope) {
            this.time = time;
            this.value = value;
            this.rms = rms;
            this.slope = slope;
        }
        
        public double getTime() {
            return time;
        }
        
        public double getValue() {
            return value;
        }
        
        public double getRms() {
            return rms;
        }
        
        public double getSlope() {
            return slope;
        }
    }
        
    public WeatherDataReader(Reader reader) {
        super(reader);
    }
    
    public WeatherDataReader(Reader reader, int sz) {
        super(reader, sz);
    }
    
    public WeatherData getWeatherData() throws NumberFormatException, IOException {
        String line;
        while ((line = readLine()) != null) {
            if (line.startsWith(COMMENT_CHAR)) {
                continue;
            }
            line = line.trim();
            String[] tokens = line.split("\\s+");
            if (tokens.length != 4) {
                // fail("invalid source format");
            }
            double time = Double.valueOf(tokens[0]);
            double val = Double.valueOf(tokens[1]);
            double rms = Double.valueOf(tokens[2]);
            double slope = Double.valueOf(tokens[3]);            
            return new WeatherData(time, val, rms, slope);
        }
        return null;
    }
}
