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
 * "@(#) $Id: XmlObsProjectDaoImpl.java,v 1.11 2010/03/08 18:31:37 rhiriart Exp $"
 */
package alma.scheduling.datamodel.obsproject.dao;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.obsproject.ArrayType;
import alma.scheduling.datamodel.obsproject.FieldSource;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnitControl;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.SchedBlockControl;
import alma.scheduling.datamodel.obsproject.SchedulingConstraints;
import alma.scheduling.datamodel.obsproject.ScienceParameters;
import alma.scheduling.datamodel.obsproject.SkyCoordinates;
import alma.scheduling.datamodel.obsproject.Target;
import alma.scheduling.datamodel.obsproject.WeatherConstraints;
import alma.scheduling.input.obsproject.generated.FieldSourceT;
import alma.scheduling.input.obsproject.generated.ObsParametersT;
import alma.scheduling.input.obsproject.generated.ObsUnitSetT;
import alma.scheduling.input.obsproject.generated.SchedBlockControlT;
import alma.scheduling.input.obsproject.generated.SchedBlockT;
import alma.scheduling.input.obsproject.generated.ScienceParametersT;
import alma.scheduling.input.obsproject.generated.TargetT;

/**
 * A DAO for ObsProject XML files.
 *
 */
public class XmlObsProjectDaoImpl implements XmlObsProjectDao {

    private static Logger logger = LoggerFactory.getLogger(XmlObsProjectDaoImpl.class);
    
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
                ObsUnitSet obsUnitSet = createObsUnitSet(xmlObsUnitSet, xmlPrj.getPrincipalInvestigator());
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
    private ObsUnitSet createObsUnitSet(alma.scheduling.input.obsproject.generated.ObsUnitSetT xmlObsUnitSet,
            String piName) {
        // TODO It is not currently clear that the piName should be in the SchedBlock.
        // The link between ObsProject and ObsUnit should be bi-directional.
        ObsUnitSet obsUnitSet = new ObsUnitSet();
        alma.scheduling.input.obsproject.generated.SchedBlockT[] xmlSchedBlocks = 
            xmlObsUnitSet.getSchedBlock();
        for (SchedBlockT xmlSchedBlock : xmlSchedBlocks) {
            SchedBlock schedBlock = new SchedBlock();
            schedBlock.setPiName(piName);
            WeatherConstraints wc = new WeatherConstraints(
                    xmlSchedBlock.getWeatherConstraints().getMaxWindVelocity(),
                    xmlSchedBlock.getWeatherConstraints().getMaxOpacity(),
                    xmlSchedBlock.getWeatherConstraints().getMinPhaseStability(),
                    xmlSchedBlock.getWeatherConstraints().getMaxSeeing());
            schedBlock.setWeatherConstraints(wc);
            Map<String, Target> targets = extractTargets(xmlSchedBlock);
            for (Iterator<String> iter = targets.keySet().iterator(); iter.hasNext();) {
                Target t = targets.get(iter.next());
                schedBlock.addTarget(t);
            }
            SchedulingConstraints sc = new SchedulingConstraints(
                    xmlSchedBlock.getSchedulingConstraints().getMaxAngularResolution(),
                    xmlSchedBlock.getSchedulingConstraints().getRepresentativeFrequency(),
                    targets.get(xmlSchedBlock.getSchedulingConstraints().getRepresentativeTargetIdRef()));
            schedBlock.setSchedulingConstraints(sc);
            ObsParametersT xmlObsParams = xmlSchedBlock.getObsParameters();
            if (xmlObsParams != null) {
                ScienceParametersT xmlSciParams =
                    xmlSchedBlock.getObsParameters().getScienceParameters();
                if (xmlSciParams != null) {
                    ScienceParameters scip = new ScienceParameters();
                    scip.setRepresentativeBandwidth(xmlSciParams.getRepresentativeBandwidth());
                    scip.setRepresentativeFrequency(xmlSciParams.getRepresentativeFrequency());
                    scip.setSensitivityGoal(xmlSciParams.getSensitivityGoal());
                    schedBlock.addObservingParameters(scip);
                }
            }
            obsUnitSet.addObsUnit(schedBlock);
            SchedBlockControlT sbControl = xmlSchedBlock.getSchedBlockControl();
            if(sbControl!=null){
                ObsUnitControl ou = new ObsUnitControl();
                ou.setArrayRequested(ArrayType.valueOf(sbControl.getArrayRequested().toString()));
                ou.setEstimatedExecutionTime(sbControl.getEstimatedExecutionTime());
                ou.setLastUpdate(sbControl.getLastUpdate());
                ou.setMaximumTime(sbControl.getMaximumTime());
                ou.setTacPriority(sbControl.getTacPriority());
                ou.setValidUntil(sbControl.getValidUntil());
                schedBlock.setObsUnitControl(ou);
                SchedBlockControl sbc = new SchedBlockControl();
                sbc.setIndefiniteRepeat(sbControl.getIndefiniteRepeat());
                schedBlock.setSchedBlockControl(sbc);
            }
        }
        ObsUnitSetT[] xmlObsUnitSets = xmlObsUnitSet.getObsUnitSet();
        for (ObsUnitSetT xmlOUS : xmlObsUnitSets) {
            ObsUnitSet ous = createObsUnitSet(xmlOUS, piName);
            obsUnitSet.addObsUnit(ous);
        }
        return obsUnitSet;
    }

    /**
     * Extracts Targets from the XML SchedBlock.
     * @param xmlSchedBlock SchedBlock XML Castor object
     * @return Targets
     */
    private Map<String, Target> extractTargets(SchedBlockT xmlSchedBlock) {
        Map<String, Target> retVal = new HashMap<String, Target>();
        TargetT[] xmlTargets = xmlSchedBlock.getTarget();
        int i = 0;
        for (TargetT xmlt : xmlTargets) {
            Target target = new Target();
            // ... TODO ...
            ScienceParameters params = new ScienceParameters();
            target.setObservingParameters(params);
            target.setSource(extractFieldSource(xmlt.getSourceIdRef(), xmlSchedBlock));
            retVal.put(xmlt.getId(), target);
        }
        return retVal;
    }
    
    /**
     * Extracts a FieldSource from the XML SchedBlock, referenced by the
     * source Id.
     * @param sourceIdRef Source Id, from the XML document
     * @param xmlSchedBlock SchedBlock XML Castor object
     * @return The referenced FieldSource
     */
    private FieldSource extractFieldSource(String sourceIdRef, SchedBlockT xmlSchedBlock) {
        FieldSourceT[] xmlFieldSources = xmlSchedBlock.getFieldSource();
        for (FieldSourceT xmlfs : xmlFieldSources) {
            if (xmlfs.getId().equals(sourceIdRef)) {
                FieldSource fs = new FieldSource(xmlfs.getName(),
                        new SkyCoordinates(xmlfs.getRA(), xmlfs.getDec()),
                        xmlfs.getPmRA(), xmlfs.getPmDec());
                return fs;
            }
        }
        // ... TODO throw an exception instead ...
        return null;
    }    
}
