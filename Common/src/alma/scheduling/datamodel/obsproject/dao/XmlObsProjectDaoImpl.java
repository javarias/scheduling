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
 * "@(#) $Id: XmlObsProjectDaoImpl.java,v 1.3 2010/02/18 19:37:30 rhiriart Exp $"
 */
package alma.scheduling.datamodel.obsproject.dao;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.config.dao.ConfigurationDaoTest;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.WeatherConstraints;

/**
 * A DAO for ObsProject XML files.
 *
 */
public class XmlObsProjectDaoImpl implements XmlObsProjectDao {

    private static Logger logger = LoggerFactory.getLogger(ConfigurationDaoTest.class);
    
    private ConfigurationDao configurationDao;
    
    public XmlObsProjectDaoImpl() { }
    
    public void setConfigurationDao(ConfigurationDao configurationDao) {
        this.configurationDao = configurationDao;
    }

    /**
     * Get all ObsProjects.
     */
    @Override
    public List<ObsProject> getAllObsProjects() {
        List<ObsProject> retVal = new ArrayList<ObsProject>();
        List<String> prjFiles = configurationDao.getConfiguration().getProjectFiles();
        for (Iterator<String> iter = prjFiles.iterator(); iter.hasNext();) {
            String prjFile = iter.next();
            try {
                alma.scheduling.input.obsproject.generated.ObsProject xmlPrj =
                    alma.scheduling.input.obsproject.generated.ObsProject.unmarshalObsProject(new FileReader(prjFile));
                ObsProject prj = new ObsProject();
                prj.setAssignedPriority(xmlPrj.getAssignedPriority());
                prj.setPrincipalInvestigator(xmlPrj.getPrincipalInvestigator());
                prj.setStatus("ready");
                alma.scheduling.input.obsproject.generated.ObsUnitSetT xmlObsUnitSet =
                    xmlPrj.getObsUnitSet();
                ObsUnitSet obsUnitSet = createObsUnitSet(xmlObsUnitSet);
                prj.setObsUnit(obsUnitSet);
                retVal.add(prj);
            } catch (MarshalException e) {
                e.printStackTrace();
            } catch (ValidationException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return retVal;
    }

    /**
     * Transforms the XML Castor representation of the ObsUnitSet in the Hibernate
     * POJO.
     * This function is recursive.
     * @param xmlObsUnitSet ObsUnitSet Castor generated class
     * @return ObsUnitSet data model object
     */
    private ObsUnitSet createObsUnitSet(alma.scheduling.input.obsproject.generated.ObsUnitSetT xmlObsUnitSet) {
        ObsUnitSet obsUnitSet = new ObsUnitSet();
        alma.scheduling.input.obsproject.generated.SchedBlockT[] xmlSchedBlocks = 
            xmlObsUnitSet.getSchedBlock();
        for (alma.scheduling.input.obsproject.generated.SchedBlockT xmlSchedBlock : xmlSchedBlocks) {
            SchedBlock schedBlock = new SchedBlock();
            schedBlock.setPiName("");
            WeatherConstraints wc = new WeatherConstraints(xmlSchedBlock.getWeatherConstraints().getMaxWindVelocity(),
                    xmlSchedBlock.getWeatherConstraints().getMaxOpacity(),
                    xmlSchedBlock.getWeatherConstraints().getMinPhaseStability(),
                    xmlSchedBlock.getWeatherConstraints().getMaxSeeing());
            schedBlock.setWeatherConstraints(wc);
            // ... complete this ...
            obsUnitSet.addObsUnit(schedBlock);
        }
        alma.scheduling.input.obsproject.generated.ObsUnitSetT[] xmlObsUnitSets =
            xmlObsUnitSet.getObsUnitSet();
        for (alma.scheduling.input.obsproject.generated.ObsUnitSetT xmlOUS : xmlObsUnitSets) {
            ObsUnitSet ous = createObsUnitSet(xmlOUS);
            obsUnitSet.addObsUnit(ous);
        }
        return obsUnitSet;
    }

}
