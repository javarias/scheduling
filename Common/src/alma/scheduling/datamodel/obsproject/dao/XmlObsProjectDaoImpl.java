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
package alma.scheduling.datamodel.obsproject.dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;


import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.obsproject.ArrayType;
import alma.scheduling.datamodel.obsproject.FieldSource;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnit;
import alma.scheduling.datamodel.obsproject.ObsUnitControl;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.ObservingParameters;
import alma.scheduling.datamodel.obsproject.Preconditions;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.SchedBlockControl;
import alma.scheduling.datamodel.obsproject.SchedBlockState;
import alma.scheduling.datamodel.obsproject.SchedulingConstraints;
import alma.scheduling.datamodel.obsproject.ScienceGrade;
import alma.scheduling.datamodel.obsproject.ScienceParameters;
import alma.scheduling.datamodel.obsproject.SkyCoordinates;
import alma.scheduling.datamodel.obsproject.Target;
import alma.scheduling.datamodel.obsproject.WeatherConstraints;
import alma.scheduling.input.obsproject.generated.FieldSourceT;
import alma.scheduling.input.obsproject.generated.ObsParametersT;
import alma.scheduling.input.obsproject.generated.ObsUnitSetT;
import alma.scheduling.input.obsproject.generated.PreconditionsT;
import alma.scheduling.input.obsproject.generated.SchedBlockControlT;
import alma.scheduling.input.obsproject.generated.SchedBlockT;
import alma.scheduling.input.obsproject.generated.SchedulingConstraintsT;
import alma.scheduling.input.obsproject.generated.ScienceParametersT;
import alma.scheduling.input.obsproject.generated.TargetT;
import alma.scheduling.input.obsproject.generated.WeatherConstraintsT;
import alma.scheduling.input.obsproject.generated.types.ArrayTypeT;
import alma.scheduling.input.obsproject.generated.types.GradeT;

/**
 * A DAO for ObsProject XML files.
 *
 */
public class XmlObsProjectDaoImpl implements XmlObsProjectDao {

    private String getXmlRefId(Class cls, Long id) {
        return String.format("_%s_%05d", cls.getSimpleName(), id);
    }

    private class XmlDomainXRef {
        String xmlRefId;
        Long dbId;
        XmlDomainXRef(Class cls, Long dbId) {
            this.xmlRefId = getXmlRefId(cls, dbId);
            this.dbId = dbId;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((dbId == null) ? 0 : dbId.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            XmlDomainXRef other = (XmlDomainXRef) obj;
            if (dbId == null) {
                if (other.dbId != null)
                    return false;
            } else if (!dbId.equals(other.dbId))
                return false;
            return true;
        }
    }
    
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
                logger.info("Processing ObsProject: " + xmlPrj.getCode() + " (" + xmlPrj.getArchiveUID() + ")");
                ObsProject prj = new ObsProject();
                prj.setUid(xmlPrj.getArchiveUID());
                prj.setCode(xmlPrj.getCode());
                prj.setName(xmlPrj.getName());
                prj.setScienceScore(xmlPrj.getScientificScore());
                prj.setScienceRank(xmlPrj.getScientificRank());
                prj.setPrincipalInvestigator(xmlPrj.getPrincipalInvestigator());
                if (xmlPrj.getGrade() != null)
                	prj.setLetterGrade(ScienceGrade.valueOf(xmlPrj.getGrade().toString()));
                else
                	prj.setLetterGrade(ScienceGrade.D);
                prj.setStatus("ready");
                alma.scheduling.input.obsproject.generated.ObsUnitSetT xmlObsUnitSet =
                    xmlPrj.getObsUnitSet();
                ObsUnitSet obsUnitSet = createObsUnitSet(xmlObsUnitSet, xmlPrj.getPrincipalInvestigator());
                prj.setObsUnit(obsUnitSet);
                obsUnitSet.setProject(prj);
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
            schedBlock.setUid(xmlSchedBlock.getArchiveUID());
            schedBlock.setName(xmlSchedBlock.getName());
            schedBlock.setPiName(piName);
            schedBlock.setRunQuicklook(true);
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
            Preconditions pc =
                new Preconditions(xmlSchedBlock.getPreconditions().getMaxAllowedHA(),
                                  xmlSchedBlock.getPreconditions().getMinAllowedHA());
            schedBlock.setPreConditions(pc);
            ObsParametersT xmlObsParams = xmlSchedBlock.getObsParameters();
            if (xmlObsParams != null) {
                ScienceParametersT xmlSciParams =
                    xmlSchedBlock.getObsParameters().getScienceParameters();
                if (xmlSciParams != null) {
                    ScienceParameters scip = new ScienceParameters();
                    scip.setRepresentativeBandwidth(xmlSciParams.getRepresentativeBandwidth());
                    scip.setRepresentativeFrequency(xmlSciParams.getRepresentativeFrequency());
                    scip.setSensitivityGoal(xmlSciParams.getSensitivityGoal());
					logger.debug("Sensitivity Goal: " + scip.getSensitivityGoal() + " XXXX " + xmlSciParams.getSensitivityGoal() );
                    schedBlock.addObservingParameters(scip);
                    for (Target t : targets.values()) { // TODO fix this
                        HashSet<ObservingParameters> obsParams = new HashSet<ObservingParameters>();
                        obsParams.add(scip);
                        t.setObservingParameters(obsParams);
                    }
                }
            }
            obsUnitSet.addObsUnit(schedBlock);
            SchedBlockControlT sbControl = xmlSchedBlock.getSchedBlockControl();
            if(sbControl!=null){
                ObsUnitControl ou = new ObsUnitControl();
                ou.setArrayRequested(ArrayType.valueOf(sbControl.getArrayRequested().toString()));
                ou.setEstimatedExecutionTime(sbControl.getMaximumTime());
                ou.setMaximumTime(sbControl.getMaximumTime());
                if (sbControl.getArrayRequested() != null)
                	ou.setArrayRequested(ArrayType.valueOf(sbControl.getArrayRequested().toString()));
                schedBlock.setObsUnitControl(ou);
                SchedBlockControl sbc = new SchedBlockControl();
                sbc.setIndefiniteRepeat(sbControl.getIndefiniteRepeat());
                sbc.setState(SchedBlockState.READY);
                sbc.setSbMaximumTime(sbControl.getEstimatedExecutionTime());
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
//            ScienceParameters params = new ScienceParameters();
//            target.setObservingParameters(params);
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
    
    @Transactional(readOnly=true)
    public void saveObsProject(ObsProject prj) {
    	logger.info("Exporting ObsProject: " + prj.getCode() + " (" + prj.getUid() + ")");
        String prjDir = configurationDao.getConfiguration().getProjectDirectory();
        String absPrjDir = System.getenv("APRC_WORK_DIR") + "/" + prjDir;
        alma.scheduling.input.obsproject.generated.ObsProject xmlPrj =
            new alma.scheduling.input.obsproject.generated.ObsProject();
        
        xmlPrj.setCode(prj.getCode());
        xmlPrj.setArchiveUID(prj.getUid());
        xmlPrj.setScientificScore(prj.getScienceScore());
        xmlPrj.setScientificRank(prj.getScienceRank());
        xmlPrj.setPrincipalInvestigator(prj.getPrincipalInvestigator());
        if (prj.getLetterGrade() != null)
        	xmlPrj.setGrade(GradeT.valueOf(prj.getLetterGrade().toString()));
        ObsUnit obsUnit = prj.getObsUnit();
        xmlPrj.setObsUnitSet((alma.scheduling.input.obsproject.generated.ObsUnitSetT) getXmlObsUnit(obsUnit));
        
        String fileName = String.format("%s/ObsProject_%s.xml", absPrjDir, prj.getCode()); 
        File newFile = new File(fileName);
        if (newFile.exists()) {
            logger.warn(newFile + " already exists");
            String backupFile = fileName + ".bak";
            if (newFile.renameTo(new File(backupFile))) {
                logger.info(newFile + " was moved to " + backupFile);
            } else {
                logger.error("failed to rename file " + newFile);
            }
        }
        try {
            FileWriter writer = new FileWriter(newFile);
            xmlPrj.marshal(writer);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (MarshalException ex) {
            ex.printStackTrace();
        } catch (ValidationException ex) {
            ex.printStackTrace();
        }
    }
    
    @Transactional(readOnly=true)
    private alma.scheduling.input.obsproject.generated.ObsUnitT getXmlObsUnit(ObsUnit obsUnit) {
        
        if (obsUnit instanceof ObsUnitSet) {
            alma.scheduling.input.obsproject.generated.ObsUnitSetT xmlObsUnitSet =
                new alma.scheduling.input.obsproject.generated.ObsUnitSetT();
            Set<ObsUnit> subObsUnits = ((ObsUnitSet) obsUnit).getObsUnits();
            for (ObsUnit subObsUnit : subObsUnits) {
                alma.scheduling.input.obsproject.generated.ObsUnitT subXmlObsUnit =
                    getXmlObsUnit(subObsUnit);
                if (subXmlObsUnit instanceof alma.scheduling.input.obsproject.generated.SchedBlockT) {
                    xmlObsUnitSet.addSchedBlock((alma.scheduling.input.obsproject.generated.SchedBlockT) subXmlObsUnit);
                } else if (subXmlObsUnit instanceof alma.scheduling.input.obsproject.generated.ObsUnitSetT) {
                    xmlObsUnitSet.addObsUnitSet((alma.scheduling.input.obsproject.generated.ObsUnitSetT) subXmlObsUnit);
                }
            }
            return xmlObsUnitSet;
        } else if (obsUnit instanceof SchedBlock) {
            alma.scheduling.input.obsproject.generated.SchedBlockT xmlSchedBlock =
                new alma.scheduling.input.obsproject.generated.SchedBlockT();
            SchedBlock sb = (SchedBlock) obsUnit;
            // WeatherConstraints
            if( sb.getWeatherConstraints() != null ){
            	WeatherConstraintsT wc = new WeatherConstraintsT();
	            wc.setMaxOpacity(sb.getWeatherConstraints().getMaxOpacity());
	            wc.setMaxSeeing(sb.getWeatherConstraints().getMaxSeeing());
	            wc.setMaxWindVelocity(sb.getWeatherConstraints().getMaxWindVelocity());
	            wc.setMinPhaseStability(sb.getWeatherConstraints().getMinPhaseStability());
	            xmlSchedBlock.setWeatherConstraints(wc);
            } else {
            	WeatherConstraintsT wc = new WeatherConstraintsT();
	            wc.setMaxOpacity(0.0);
	            wc.setMaxSeeing(0.0);
	            wc.setMaxWindVelocity(0.0);
	            wc.setMinPhaseStability(0.0);
	            xmlSchedBlock.setWeatherConstraints(wc);
            }
            // ObservingParameters
            Set<ObservingParameters> obsParams = sb.getObservingParameters();
            Map<XmlDomainXRef, ObsParametersT> xmlObsParams =
                new HashMap<XmlDomainXRef, ObsParametersT>();
            ObsParametersT theOne = null;
            for (ObservingParameters op : obsParams) {
                if (op instanceof ScienceParameters) {
                    ScienceParameters scp = (ScienceParameters) op;
                    ObsParametersT xmlOP = new ObsParametersT();
                    xmlOP.setId(getXmlRefId(ObsParametersT.class, scp.getId()));
                    ScienceParametersT xmlSciParams = new ScienceParametersT();
                    xmlSciParams.setDuration(0.0);
                    xmlSciParams.setRepresentativeBandwidth(scp.getRepresentativeBandwidth());
                    xmlSciParams.setRepresentativeFrequency(scp.getRepresentativeFrequency());
                    xmlSciParams.setSensitivityGoal(scp.getSensitivityGoal());
					logger.debug("Sensitivity Goal: " + scp.getSensitivityGoal() + " XXXX " + xmlSciParams.getSensitivityGoal() );
                    xmlOP.setScienceParameters(xmlSciParams);
                    xmlObsParams.put(new XmlDomainXRef(ObsParametersT.class, scp.getId()), xmlOP);
                    theOne = xmlOP;
                }
            }
            if (theOne != null) xmlSchedBlock.setObsParameters(theOne);  // TODO fix this
            // Targets
            Set<Target> targets = sb.getTargets();
            Map<XmlDomainXRef, TargetT> xmlTargets = new HashMap<XmlDomainXRef, TargetT>();
            Map<XmlDomainXRef, FieldSourceT> xmlSources = new HashMap<XmlDomainXRef, FieldSourceT>();
            for (Target t : targets) {
                TargetT xmlTarget = new TargetT();
                XmlDomainXRef xref = new XmlDomainXRef(TargetT.class, t.getId());
                xmlTarget.setId(xref.xmlRefId);
                xmlTarget.setInstrumentSpecIdRef("");
                if( t.getObservingParameters().iterator().hasNext() ){
                	xmlTarget.setObsParametersIdRef(getXmlRefId(ObsParametersT.class, t.getObservingParameters().iterator().next().getId()));
                }
                xmlTarget.setSourceIdRef(getXmlRefId(FieldSourceT.class, t.getSource().getId()));
                xmlTargets.put(xref, xmlTarget);
                FieldSource src = t.getSource();
                FieldSourceT xmlSrc = new FieldSourceT();
                xmlSrc.setId(getXmlRefId(FieldSourceT.class, src.getId()));
				if( src.getName() != null )
	                xmlSrc.setName(src.getName());
                xmlSrc.setRA(src.getCoordinates().getRA());
                xmlSrc.setDec(src.getCoordinates().getDec());
                if (!xmlSources.containsKey(new XmlDomainXRef(FieldSourceT.class, src.getId())))
                    xmlSources.put(new XmlDomainXRef(FieldSourceT.class, src.getId()), xmlSrc);
            }
            xmlSchedBlock.setTarget(xmlTargets.values().toArray(new TargetT[0]));
            xmlSchedBlock.setFieldSource(xmlSources.values().toArray(new FieldSourceT[0]));
            // SchedulingConstraints
            SchedulingConstraintsT sc = new SchedulingConstraintsT();
            sc.setMaxAngularResolution(sb.getSchedulingConstraints().getMaxAngularResolution());
            sc.setRepresentativeFrequency(sb.getSchedulingConstraints().getRepresentativeFrequency());
            
            sc.setRepresentativeTargetIdRef(getXmlRefId(TargetT.class,
                                                        sb.getSchedulingConstraints()
                                                          .getRepresentativeTarget()
                                                          .getId()));
            xmlSchedBlock.setSchedulingConstraints(sc);
            // Preconditions
            PreconditionsT pc = new PreconditionsT();
            pc.setMinAllowedHA(sb.getPreConditions().getMinAllowedHourAngle());
            pc.setMaxAllowedHA(sb.getPreConditions().getMaxAllowedHourAngle());
            xmlSchedBlock.setPreconditions(pc);
            // SchedBlockControl
            ObsUnitControl ouCtrl = sb.getObsUnitControl();
            SchedBlockControl sbCtrl = sb.getSchedBlockControl();
            SchedBlockControlT xmlSbCtrl = new SchedBlockControlT();
            if (ouCtrl.getArrayRequested() != null)
            	xmlSbCtrl.setArrayRequested(ArrayTypeT.valueOf(ouCtrl.getArrayRequested().toString()));
            else 
            	xmlSbCtrl.setArrayRequested(ArrayTypeT.TWELVE_M);
            xmlSbCtrl.setEstimatedExecutionTime(sbCtrl.getSbMaximumTime());
            xmlSbCtrl.setIndefiniteRepeat(sbCtrl.getIndefiniteRepeat());
            xmlSbCtrl.setMaximumTime(ouCtrl.getMaximumTime());
            xmlSchedBlock.setSchedBlockControl(xmlSbCtrl);
            return xmlSchedBlock;
        } else {
            return null;
        }
    }
}
