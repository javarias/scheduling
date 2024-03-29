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
package alma.scheduling.dataload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

import alma.scheduling.dataload.AtmTableReader.AtmData;
import alma.scheduling.datamodel.config.Configuration;
import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.weather.AtmParameters;
import alma.scheduling.datamodel.weather.dao.AtmParametersDao;

public class AtmDataLoader implements DataLoader {

    private AtmTableReader reader;
    private int count = 0;

    public AtmDataLoader() {
    }

    protected String file;
    public void setFile(String file) {
        this.file = file;
    }

    protected AtmParametersDao dao;
    public void setDao(AtmParametersDao dao) {
        this.dao = dao;
    }

    protected int maxNumRecords;
    public void setMaxNumRecords(int maxNumRecords) {
        this.maxNumRecords = maxNumRecords;
    }

    private double pwc;
    public void setPwc(double pwc) {
        this.pwc = pwc;
    }

    private ConfigurationDao configurationDao;
    public void setConfigurationDao(ConfigurationDao configurationDao) {
        this.configurationDao = configurationDao;
    }
    
	private void createDataReader() throws FileNotFoundException {
		if (file.startsWith("classpath:")) {
			Reader br = new BufferedReader(new InputStreamReader(getClass()
					.getClassLoader().getResourceAsStream(file.substring(10))));
			reader = new AtmTableReader(br);
		} else {
			Configuration config = configurationDao.getConfiguration();
			String workDir = config.getWorkDirectory();
			String weatherDir = config.getWeatherDirectory();
			String weatherDirFullPath = workDir + "/" + weatherDir + "/";
			File atmfile = new File(weatherDirFullPath + file);
			FileReader fr = new FileReader(atmfile);
			reader = new AtmTableReader(fr);
		}
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
            Set<AtmParameters> params = new HashSet<AtmParameters>();
            while ((ad = getNextAtmDatum()) != null) {
                AtmParameters p = new AtmParameters();
                p.setPWV(pwc);
                p.setFreq(ad.getFreq());
                p.setOpacity(ad.getOpacity());
                p.setAtmBrightnessTemp(ad.getTemperature());
                params.add(p);
            }
            dao.loadAtmParameters(params);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void clear() {
    }
}