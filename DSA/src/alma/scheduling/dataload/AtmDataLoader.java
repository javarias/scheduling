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
 * "@(#) $Id: AtmDataLoader.java,v 1.2 2010/03/02 23:19:15 javarias Exp $"
 */
package alma.scheduling.dataload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import alma.scheduling.dataload.AtmTableReader.AtmData;
import alma.scheduling.datamodel.weather.AtmParameters;
import alma.scheduling.datamodel.weather.dao.AtmParametersDao;

public class AtmDataLoader implements DataLoader {

    protected String file;
    protected AtmParametersDao dao;
    protected int maxNumRecords;
    private AtmTableReader reader;
    private int count = 0;
    private double pwc;

    public AtmDataLoader() {
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setDao(AtmParametersDao dao) {
        this.dao = dao;
    }

    public void setMaxNumRecords(int maxNumRecords) {
        this.maxNumRecords = maxNumRecords;
    }

    public void setPwc(double pwc) {
        this.pwc = pwc;
    }
    
    private void createDataReader() throws FileNotFoundException {
        File file = new File(this.file);
        FileReader fr = new FileReader(file);
        reader = new AtmTableReader(fr);                
    }

    protected AtmData getNextAtmDatum() throws NumberFormatException,
            IOException {
        if (reader == null)
            createDataReader();
        if ((maxNumRecords >= 0) && (count++ > maxNumRecords)) {
            return null;
        }
        return reader.getAtmData();
    }

    @Override
    public void load() {
        try {
            AtmData ad;
            while ((ad = getNextAtmDatum()) != null) {
                AtmParameters params = new AtmParameters();
                params.setPWV(pwc);
                params.setFreq(ad.getFreq());
                params.setOpacity(ad.getOpacity());
                params.setAtmBrightnessTemp(ad.getTemperature());
                dao.loadAtmParameter(params);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}