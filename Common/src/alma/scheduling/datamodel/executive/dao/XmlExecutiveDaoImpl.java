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
 * "@(#) $Id: XmlExecutiveDaoImpl.java,v 1.9 2010/04/28 19:57:48 javarias Exp $"
 */
package alma.scheduling.datamodel.executive.dao;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.datamodel.CannotParseDataException;
import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.ExecutivePercentage;
import alma.scheduling.datamodel.executive.ExecutiveTimeSpent;
import alma.scheduling.datamodel.executive.ObservingSeason;
import alma.scheduling.datamodel.executive.PI;
import alma.scheduling.datamodel.executive.PIMembership;
import alma.scheduling.input.executive.generated.ExecutiveData;

public class XmlExecutiveDaoImpl implements XmlExecutiveDAO {

    private static Logger logger = LoggerFactory.getLogger(XmlExecutiveDaoImpl.class);
    
    private boolean xmlDataHasBeenLoaded = false;
    private ArrayList<Executive> exec;
    private ArrayList<PI> pi;
    private ArrayList<ExecutivePercentage> ep;
    private ArrayList<ObservingSeason> os;
    
    /**
     * Path to the XML fine containing the Executive data. If this is not set,
     * then all the files in the executives directory from the ConfigurationDao are
     * loaded.
     */
    private String pathToExecDataXML = null;
    
    // --- Spring properties ---
    
    private ConfigurationDao configDao;
    public void setConfigDao(ConfigurationDao configDao) {
        this.configDao = configDao;
    }

    // --- Constructors ---
    
    public XmlExecutiveDaoImpl() {
        exec = new ArrayList<Executive>();
        pi = new ArrayList<PI>();
        ep = new ArrayList<ExecutivePercentage>();
        os = new ArrayList<ObservingSeason>();
    }


    public XmlExecutiveDaoImpl(String pathToExecDataXML){
        this();
        logger.debug("Setting to read XML data from: " + pathToExecDataXML);
        this.pathToExecDataXML = pathToExecDataXML;
    }
    
    // --- XmlExecutiveDAO impl ---
    
    @Override
    public List<Executive> getAllExecutive() {
        if(!xmlDataHasBeenLoaded){
            try {
                processXMLProjectFiles();
                xmlDataHasBeenLoaded = true;
            } catch (CannotParseDataException e) {
                e.printStackTrace();
            }
        }
        return exec;
    }

    @Override
    public List<ObservingSeason> getAllObservingSeason() {
        if(!xmlDataHasBeenLoaded){
            try {
                processXMLProjectFiles();
                xmlDataHasBeenLoaded = true;
            } catch (CannotParseDataException e) {
                e.printStackTrace();
            }
        }        
        return os;
    }
    
    @Override
    public List<PI> getAllPi() {
        if(!xmlDataHasBeenLoaded){
            try {
                processXMLProjectFiles();
                xmlDataHasBeenLoaded = true;
            } catch (CannotParseDataException e) {
                e.printStackTrace();
            }
        }        
        return pi;
    }

    @Override
    public ObservingSeason getCurrentSeason() {
        if(!xmlDataHasBeenLoaded){
            try {
                processXMLProjectFiles();
                xmlDataHasBeenLoaded = true;
            } catch (CannotParseDataException e) {
                e.printStackTrace();
            }
        }
        ObservingSeason[] osArray = new ObservingSeason[os.size()];
        os.toArray(osArray);
        Arrays.sort(osArray);
        return osArray[0];
    }

    @Override
    public Executive getExecutive(String piName) {
        return null;
    }

    @Override
    public ExecutivePercentage getExecutivePercentage(Executive ex,
            ObservingSeason os) {
        return null;
    }

    @Override
    public List<ExecutiveTimeSpent> getExecutiveTimeSpent(Executive ex,
            ObservingSeason os) {
        return null;
    }

    // --- Private functions ---
    
    private void processXMLProjectFiles() throws CannotParseDataException {
        if (pathToExecDataXML != null) {
            logger.info("Reading Executive XML data from: " + pathToExecDataXML);
            ExecutiveData execData = null;
            try {
                execData = ExecutiveData.unmarshalExecutiveData(
                        new FileReader(pathToExecDataXML));
                copyExecutiveFromXMLGenerated(execData);
            } catch (MarshalException e) {
                throw new CannotParseDataException(pathToExecDataXML, e);
            } catch (ValidationException e) {
                throw new CannotParseDataException(pathToExecDataXML, e);
            } catch (FileNotFoundException e) {
                throw new CannotParseDataException(pathToExecDataXML, e);
            }
            
        }
        else {
            logger.info("Reading configuration");
            List<String> files = configDao.getConfiguration().getExecutiveFiles();
            // TODO: Validate against XML schema files
            for (Iterator<String> iter = files.iterator(); iter.hasNext();) {
                String file = iter.next();
                logger.info("Reading Executive XML data from: " + file);
                ExecutiveData execData = null;
                try {
                    execData = ExecutiveData.unmarshalExecutiveData(
                            new FileReader(file));
                    copyExecutiveFromXMLGenerated(execData);
                } catch (MarshalException e) {
                    throw new CannotParseDataException(file, e);
                } catch (ValidationException e) {
                    throw new CannotParseDataException(file, e);
                } catch (FileNotFoundException e) {
                    throw new CannotParseDataException(file, e);
                }
            }
        }
    }

    private void copyExecutiveFromXMLGenerated(
            alma.scheduling.input.executive.generated.ExecutiveData data) {
        if (data == null)
            throw new NullPointerException("Executive data input cannot be null");

        HashMap<String, Executive> execm = new HashMap<String, Executive>();
        HashMap<String, ObservingSeason> osm = new HashMap<String, ObservingSeason>();

        for (int i = 0; i < data.getExecutiveCount(); i++) {
            Executive tmp = copyExecutive(data.getExecutive(i));
            execm.put(data.getExecutive(i).getName(), tmp);
        }
        for (int i = 0; i < data.getObservingSeasonCount(); i++) {
            ObservingSeason tmp = copyObservingSeason(data.getObservingSeason(i));
            osm.put(data.getObservingSeason(i).getName(), tmp);
            for (int j = 0; j < data.getObservingSeason(i).getExecutivePercentageCount(); j++) {
                alma.scheduling.input.executive.generated.ExecutivePercentage ref =
                    data.getObservingSeason(i).getExecutivePercentage(j);
                Executive e = execm.get(ref.getExecutiveRef());
                ExecutivePercentage execPercent =
                    new ExecutivePercentage(tmp, e, new Float(ref.getPercentage()),
                            new Double(ref.getTotalObsTimeForSeason()));
                ep.add(execPercent);
            }
        }
        for (int i = 0; i < data.getPICount(); i++) {
            PI tmp = copyPI(data.getPI(i), execm);
            pi.add(tmp);
        }

        exec.addAll(execm.values());
        os.addAll(osm.values());

    }

    private Executive copyExecutive(
            alma.scheduling.input.executive.generated.Executive in) {
        Executive exec = new Executive();
        exec.setName(in.getName());
        exec.setDefaultPercentage(in.getDefaultPercentage());
        if (exec.getExecutivePercentage() == null)
            exec.setExecutivePercentage(new HashSet<ExecutivePercentage>());
        return exec;
    }

    private ObservingSeason copyObservingSeason(
            alma.scheduling.input.executive.generated.ObservingSeason in) {
        ObservingSeason os = new ObservingSeason();
        os.setName(in.getName());
        os.setStartDate(in.getStartDate());
        os.setEndDate(in.getEndDate());
        return os;
    }

    private PI copyPI(alma.scheduling.input.executive.generated.PI in,
            HashMap<String, Executive> execs) {
        PI pi = new PI();
        pi.setName(in.getName());
        pi.setEmail(in.getEmail());
        if (pi.getPIMembership() == null)
            pi.setPIMembership(new HashSet<PIMembership>());
        for (int i = 0; i < in.getPIMembershipCount(); i++) {
            Executive e = execs.get(in.getPIMembership(i).getExecutiveRef());
            PIMembership pim = copyPIMembership(in.getPIMembership(i));
            pim.setExecutive(e);
            pi.getPIMembership().add(pim);
        }
        return pi;
    }

    private PIMembership copyPIMembership(
            alma.scheduling.input.executive.generated.PIMembership in) {
        PIMembership pim = new PIMembership();
        pim.setMembershipPercentage(in.getMembershipPercentage());
        return pim;
    }
}
