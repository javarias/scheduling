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
 * "@(#) $Id: AtmTableReader.java,v 1.2 2010/03/02 23:19:15 javarias Exp $"
 */
package alma.scheduling.dataload;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class AtmTableReader extends BufferedReader {

    private static final String COMMENT_CHAR = "#";
    
    public final class AtmData {
        private final double freq;
        private final double opacity;
        private final double temperature;

        public AtmData(double freq, double opacity, double temperature) {
            super();
            this.freq = freq;
            this.opacity = opacity;
            this.temperature = temperature;
        }

        public double getFreq() {
            return freq;
        }

        public double getOpacity() {
            return opacity;
        }

        public double getTemperature() {
            return temperature;
        }
    }
        
    public AtmTableReader(Reader reader) {
        super(reader);
    }
    
    public AtmTableReader(Reader reader, int sz) {
        super(reader, sz);
    }
    
    public AtmData getAtmData() throws NumberFormatException, IOException {
        String line;
        while ((line = readLine()) != null) {
            if (line.startsWith(COMMENT_CHAR)) {
                continue;
            }
            line = line.trim();
            String[] tokens = line.split("\\s+");
            if (tokens.length != 3) {
                // fail("invalid source format");
            }
            double freq = Double.valueOf(tokens[0]);
            double op = Double.valueOf(tokens[1]);
            double temp = Double.valueOf(tokens[2]);
            return new AtmData(freq, op, temp);
        }
        return null;
    }
}
