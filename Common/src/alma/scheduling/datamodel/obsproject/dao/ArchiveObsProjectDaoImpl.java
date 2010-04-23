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
 * "@(#) $Id: ArchiveObsProjectDaoImpl.java,v 1.2 2010/04/23 23:35:37 dclarke Exp $"
 */
package alma.scheduling.datamodel.obsproject.dao;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.omg.CORBA.UserException;

import alma.acs.component.client.ComponentClient;
import alma.acs.entityutil.EntityDeserializer;
import alma.acs.entityutil.EntityException;
import alma.acs.entityutil.EntitySerializer;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.projectlifecycle.StateSystemOperations;
import alma.scheduling.Define.SchedulingException;
import alma.scheduling.acsFacades.ACSComponentFactory;
import alma.scheduling.acsFacades.ComponentFactory;
import alma.scheduling.acsFacades.ComponentFactory.ComponentDiagnosticTypes;
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
import alma.scheduling.datamodel.obsproject.SchedulingConstraints;
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
import alma.xmlentity.XmlEntityStruct;
import alma.xmlstore.OperationalOperations;

/**
 * A DAO for ObsProjects in the XML store.
 *
 */
public class ArchiveObsProjectDaoImpl extends ComponentClient
									  implements ArchiveObsProjectDao {

    
    
    /*
     * ================================================================
     * Auld stuff no tidied up yet
     * ================================================================
     */
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
	/* End auld stuff no tidied up yet
	 * ============================================================= */

    
    
    /*
     * ================================================================
     * Fields and Constants
     * ================================================================
     */
//    private static Logger logger = LoggerFactory.getLogger(ArchiveObsProjectDaoImpl.class);
    
    private ConfigurationDao configurationDao;
    
    public void setConfigurationDao(ConfigurationDao configurationDao) {
        this.configurationDao = configurationDao;
    }

    // ACS Components
    private ComponentFactory componentFactory;
    private OperationalOperations xmlStore;
    private StateSystemOperations stateSystem;

    private EntityDeserializer entityDeserializer;
    private EntitySerializer entitySerializer;
    
    private Logger logger;
    
    // ACS Diagnostics
    private final static ComponentDiagnosticTypes[] xmlStoreDiags = {
    	ComponentDiagnosticTypes.LOGGING
    };
    
    private final static ComponentDiagnosticTypes[] stateSystemDiags = {
    	ComponentDiagnosticTypes.LOGGING,
    	ComponentDiagnosticTypes.PROFILING
    };
    
    // APDM
    final public static String[] OPPhase1RunnableStates = {
    	StatusTStateType.PHASE1SUBMITTED.toString(),
        StatusTStateType.READY.toString(),              
        StatusTStateType.PARTIALLYOBSERVED.toString()               
    };
    final public static String[] OPPhase2RunnableStates = {
    	StatusTStateType.PHASE2SUBMITTED.toString(),
        StatusTStateType.READY.toString(),              
        StatusTStateType.PARTIALLYOBSERVED.toString()               
    };
    final public static String[] SBPhase1RunnableStates = {
    	StatusTStateType.PHASE1SUBMITTED.toString(),
        StatusTStateType.READY.toString(),              
        StatusTStateType.RUNNING.toString()             
    };
    final public static String[] SBPhase2RunnableStates = {
    	StatusTStateType.PHASE2SUBMITTED.toString(),
        StatusTStateType.READY.toString(),              
        StatusTStateType.RUNNING.toString()             
    };
	/* End Fields and constants
	 * ============================================================= */

    
    
    /*
     * ================================================================
     * Construction
     * ================================================================
     */
	/**
	 * @throws Exception
	 */
	public ArchiveObsProjectDaoImpl()
			throws Exception {
		this(null, // This must be null, argument retained in ACS for compatibility
				getManagerLocation(),
				getClientName());
	}

	/**
	 * @param logger
	 * @param managerLoc
	 * @param clientName
	 * @throws Exception
	 */
	public ArchiveObsProjectDaoImpl(java.util.logging.Logger logger,
			                        String managerLoc,
			                        String clientName)
			throws Exception {
		// TODO: Logger type has been hacked, must be resolved properly.
		super(logger, managerLoc, clientName);
		this.componentFactory = new ACSComponentFactory(getContainerServices());
		this.xmlStore = componentFactory.getDefaultArchive(xmlStoreDiags);
		this.stateSystem = componentFactory.getDefaultStateSystem(stateSystemDiags);
		this.entitySerializer = EntitySerializer.getEntitySerializer(
                getContainerServices().getLogger());
		this.entityDeserializer = EntityDeserializer.getEntityDeserializer(
        		getContainerServices().getLogger());
		this.logger = getContainerServices().getLogger();
	}
	/* End Construction
	 * ============================================================= */

    
    
    /*
     * ================================================================
     * Atomic fetching and deserialisation of Entities
     * ================================================================
     */
	/**
	 * Gets the specified APDM ObsProject from the xmlstore. In the
	 * event of any problems, swallow the exception but log what
	 * happened and then return null.
	 * 
	 * @param uid
	 * @return an APDM ObsProject (or null, if there are problems).
	 */
	private alma.entity.xmlbinding.obsproject.ObsProject fetchAPDMObsProject(final String uid) {
		alma.entity.xmlbinding.obsproject.ObsProject result = null;

		try {
			final XmlEntityStruct xml = xmlStore.retrieveDirty(uid);
			result = (alma.entity.xmlbinding.obsproject.ObsProject)
			entityDeserializer.deserializeEntity(
					xml,
					alma.entity.xmlbinding.obsproject.ObsProject.class);
		} catch (EntityException deserialiseEx) {
        	logger.warning(String.format(
        			"can not deserialise APDM ObsProject %s from XML Store - %s, (skipping it)",
        			uid,
        			deserialiseEx.getMessage()));
        	deserialiseEx.printStackTrace(System.out);
		} catch (UserException retrieveEx) {
        	logger.warning(String.format(
        			"can not fetch APDM ObsProject %s from XML Store - %s, (skipping it)",
        			uid,
        			retrieveEx.getMessage()));
        	retrieveEx.printStackTrace(System.out);
		}

        return result;
	}
	
	/**
	 * Gets the specified APDM SchedBlock from the xmlstore. In the
	 * event of any problems, swallow the exception but log what
	 * happened and then return null.
	 * 
	 * @param uid
	 * @return an APDM SchedBlock (or null, if there are problems).
	 */
	private alma.entity.xmlbinding.schedblock.SchedBlock fetchAPDMSchedBlock(final String uid) {
		alma.entity.xmlbinding.schedblock.SchedBlock result = null;

		try {
			final XmlEntityStruct xml = xmlStore.retrieveDirty(uid);
			result = (alma.entity.xmlbinding.schedblock.SchedBlock)
			entityDeserializer.deserializeEntity(
					xml,
					alma.entity.xmlbinding.schedblock.SchedBlock.class);
		} catch (EntityException deserialiseEx) {
        	logger.warning(String.format(
        			"can not deserialise APDM SchedBlock %s from XML Store - %s, (skipping it)",
        			uid,
        			deserialiseEx.getMessage()));
        	deserialiseEx.printStackTrace(System.out);
		} catch (UserException retrieveEx) {
        	logger.warning(String.format(
        			"can not fetch APDM SchedBlock %s from XML Store - %s, (skipping it)",
        			uid,
        			retrieveEx.getMessage()));
        	retrieveEx.printStackTrace(System.out);
		}

        return result;
	}
	
	/**
	 * Gets the specified APDM ObsProposal from the xmlstore. In the
	 * event of any problems, swallow the exception but log what
	 * happened and then return null.
	 * 
	 * @param uid
	 * @return an APDM ObsProposal (or null, if there are problems).
	 */
	private alma.entity.xmlbinding.obsproposal.ObsProposal fetchAPDMObsProposal(final String uid) {
		alma.entity.xmlbinding.obsproposal.ObsProposal result = null;

		try {
			final XmlEntityStruct xml = xmlStore.retrieveDirty(uid);
			result = (alma.entity.xmlbinding.obsproposal.ObsProposal)
			entityDeserializer.deserializeEntity(
					xml,
					alma.entity.xmlbinding.obsproposal.ObsProposal.class);
		} catch (EntityException deserialiseEx) {
        	logger.warning(String.format(
        			"can not deserialise APDM ObsProposal %s from XML Store - %s, (skipping it)",
        			uid,
        			deserialiseEx.getMessage()));
        	deserialiseEx.printStackTrace(System.out);
		} catch (UserException retrieveEx) {
        	logger.warning(String.format(
        			"can not fetch APDM ObsProposal %s from XML Store - %s, (skipping it)",
        			uid,
        			retrieveEx.getMessage()));
        	retrieveEx.printStackTrace(System.out);
		}

        return result;
	}
	/* End Atomic fetching and deserialisation of Entities
	 * ============================================================= */

    
    
    /*
     * ================================================================
     * Steps from which the main operations are made
     * ================================================================
     */
	/**
	 * Get all the project statuses that are in the state archive in a
	 * given set of states.
	 * 
	 * @param states - we are interested in ProjectStatuses in any of
	 *                these states.
	 * @return a map from ProjectStatusId (note, NOT ObsProjectId) to
	 *         ProjectStatus containing all the ProjectStatus entities
	 *         found
	 */
    public Map<String, ProjectStatus> getProjectStatusesByState(String[] states) {
    	
        final Map<String, ProjectStatus> result =
        	new TreeMap<String, ProjectStatus>();
        
		XmlEntityStruct xml[] = null;
		try {
			xml = stateSystem.findProjectStatusByState(states);
		} catch (Exception e) {
        	logger.warning("can not pull ProjectStatuses from State System");
            e.printStackTrace(System.out);
		}
		
		for (final XmlEntityStruct xes : xml) {
			try {
				final ProjectStatus ps = (ProjectStatus) entityDeserializer.
				deserializeEntity(xes, ProjectStatus.class);
				result.put(ps.getProjectStatusEntity().getEntityId(), ps);
			} catch (Exception e) {
	        	logger.warning("can not deserialise ProjectStatus from State System (skipping)");
	            e.printStackTrace(System.out);
			}
		}
		
		return result;
	}

    /**
	 * Get all the APDM ObsProjects that correspond to the given
	 * project statuses.
	 * 
     * @param projectStatuses
     * @return a map from ObsProjectId to APDM ObsProject containing
     *         all the APDM ObsProject entities found.
     */
    private Map<String, alma.entity.xmlbinding.obsproject.ObsProject> getAPDMProjectsFor(
			Map<String, ProjectStatus> projectStatuses) {
    	
        final Map<String, alma.entity.xmlbinding.obsproject.ObsProject> result =
        	new TreeMap<String, alma.entity.xmlbinding.obsproject.ObsProject>();
        
        for (final ProjectStatus ps : projectStatuses.values()) {
        	final String projectId = ps.getObsProjectRef().getEntityId();
        	final alma.entity.xmlbinding.obsproject.ObsProject proj = fetchAPDMObsProject(projectId);
        	if (proj != null) {
                result.put(projectId, proj);
        	}
        }
        return result;
    }

    /**
	 * Get all the APDM ScheBlocks that correspond to the given APDM
	 * ObsUnitSet and put them in the given map. Recurses down the
	 * ObsUnitSet hierarchy.
	 * 
     * @param apdmObsUnitSet
     * @param into
     * @return a map from SchedBlockId to APDM SchedBlock containing
     *         all the APDM SchedBlock entities found.
     */
    private void getAPDMSchedBlocksFor(
			alma.entity.xmlbinding.obsproject.ObsUnitSetT apdmObsUnitSet,
			Map<String, alma.entity.xmlbinding.schedblock.SchedBlock> into) {
    	
    	// Get the choice object for convenience
    	final alma.entity.xmlbinding.obsproject.ObsUnitSetTChoice choice =
    		apdmObsUnitSet.getObsUnitSetTChoice();
    	
    	// Recurse down child ObsUnitSetTs
    	for (final alma.entity.xmlbinding.obsproject.ObsUnitSetT childOUS : choice.getObsUnitSet()) {
    		getAPDMSchedBlocksFor(childOUS, into);
    	}
    	
    	// Get any referred SchedBlocks
    	for (final alma.entity.xmlbinding.schedblock.SchedBlockRefT childSBRef : choice.getSchedBlockRef()) {
    		final String sbId = childSBRef.getEntityId();
        	final alma.entity.xmlbinding.schedblock.SchedBlock sb = fetchAPDMSchedBlock(sbId);
        	if (sb != null) {
                into.put(sbId, sb);
        	}
    	}
    }

    /**
	 * Get all the APDM ScheBlocks that correspond to the given APDM
	 * ObsProjects.
	 * 
     * @param apdmProjects
     * @return a map from SchedBlockId to APDM SchedBlock containing
     *         all the APDM SchedBlock entities found.
     */
    private Map<String, alma.entity.xmlbinding.schedblock.SchedBlock> getPhase1APDMSchedBlocksFor(
			Map<String, alma.entity.xmlbinding.obsproject.ObsProject> apdmProjects) {
    	
        final Map<String, alma.entity.xmlbinding.schedblock.SchedBlock> result =
        	new TreeMap<String, alma.entity.xmlbinding.schedblock.SchedBlock>();
        
    	for (final alma.entity.xmlbinding.obsproject.ObsProject op : apdmProjects.values()) {
            final alma.entity.xmlbinding.obsproposal.ObsProposal proposal =
            	fetchAPDMObsProposal(op.getObsProposalRef().getEntityId());
            if (proposal != null) {
            	getAPDMSchedBlocksFor(proposal.getObsPlan(), result);
            }
    	}
        return result;
    }

    /**
	 * Get all the APDM ScheBlocks that correspond to the given APDM
	 * ObsProjects.
	 * 
     * @param apdmProjects
     * @return a map from SchedBlockId to APDM SchedBlock containing
     *         all the APDM SchedBlock entities found.
     */
    private Map<String, alma.entity.xmlbinding.schedblock.SchedBlock> getPhase2APDMSchedBlocksFor(
			Map<String, alma.entity.xmlbinding.obsproject.ObsProject> apdmProjects) {
    	
        final Map<String, alma.entity.xmlbinding.schedblock.SchedBlock> result =
        	new TreeMap<String, alma.entity.xmlbinding.schedblock.SchedBlock>();
        
    	for (final alma.entity.xmlbinding.obsproject.ObsProject op : apdmProjects.values()) {
    		getAPDMSchedBlocksFor(op.getObsProgram().getObsPlan(), result);
    	}
        return result;
    }
	/* End Steps from which the main operations are made
	 * ============================================================= */

    
    
    /*
     * ================================================================
     * Implementation of ArchiveObsProjectDao
     * ================================================================
     */
    /**
     * Get all Phase 1 ObsProjects.
     */
    @Override
    public List<ObsProject> getAllPhase1ObsProjects() {
        
        logger.info(String.format("%s.getAllPhase1ObsProjects()",
        		this.getClass().getSimpleName()));
        
        // Get all the ProjectStatuses for runnable projects
        final Map<String, ProjectStatus> projectStatuses =
        	getProjectStatusesByState(OPPhase1RunnableStates);
       
        logger.info("Got the project statuses");
        logProjectStatuses(projectStatuses);

        // Get all the corresponding APDM ObsProjects
        final Map<String, alma.entity.xmlbinding.obsproject.ObsProject> apdmProjects =
        	getAPDMProjectsFor(projectStatuses);
        
        logger.info("Got the projects");
        logAPDMObsProjects(apdmProjects);

        // Get all the corresponding APDM SchedBlocks
        final Map<String, alma.entity.xmlbinding.schedblock.SchedBlock> apdmSchedBlocks =
        	getPhase1APDMSchedBlocksFor(apdmProjects);
        
        logger.info("Got the schedblocks");
        logAPDMSchedBlocks(apdmProjects, apdmSchedBlocks);

        // Convert them to Scheduling stylee ObsProjects
        final List<ObsProject> result = null;
        logObsProjects(result);

        return result;
    }

    /**
     * Get all Phase 2 ObsProjects.
     */
    @Override
    public List<ObsProject> getAllPhase2ObsProjects() {
        
        logger.info(String.format("%s.getAllPhase2ObsProjects()",
        		this.getClass().getSimpleName()));
        
        // Get all the ProjectStatuses for runnable projects
        final Map<String, ProjectStatus> projectStatuses =
        	getProjectStatusesByState(OPPhase2RunnableStates);
       
        logger.info("Got the project statuses");
        logProjectStatuses(projectStatuses);

        // Get all the corresponding APDM ObsProjects
        final Map<String, alma.entity.xmlbinding.obsproject.ObsProject> apdmProjects =
        	getAPDMProjectsFor(projectStatuses);
        
        logger.info("Got the projects");
        logAPDMObsProjects(apdmProjects);

        // Get all the corresponding APDM SchedBlocks
        final Map<String, alma.entity.xmlbinding.schedblock.SchedBlock> apdmSchedBlocks =
        	getPhase2APDMSchedBlocksFor(apdmProjects);
        
        logger.info("Got the schedblocks");
        logAPDMSchedBlocks(apdmProjects, apdmSchedBlocks);

        // Convert them to Scheduling stylee ObsProjects
        final List<ObsProject> result = null;
        logObsProjects(result);

        return result;
    }
	/* End Implementation of ArchiveObsProjectDao
	 * ============================================================= */

    
    
    /*
     * ================================================================
     * Mair auld stuff no tidied up yet
     * ================================================================
     */
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
                    schedBlock.addObservingParameters(scip);
                    for (Target t : targets.values()) { // TODO fix this
                        t.setObservingParameters(scip);
                    }
                }
            }
            obsUnitSet.addObsUnit(schedBlock);
            SchedBlockControlT sbControl = xmlSchedBlock.getSchedBlockControl();
            if(sbControl!=null){
                ObsUnitControl ou = new ObsUnitControl();
                ou.setArrayRequested(ArrayType.valueOf(sbControl.getArrayRequested().toString()));
                ou.setEstimatedExecutionTime(sbControl.getEstimatedExecutionTime());
                ou.setMaximumTime(sbControl.getMaximumTime());
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
    
    public void saveObsProject(ObsProject prj) {
        String prjDir = configurationDao.getConfiguration().getProjectDirectory();
        String absPrjDir = System.getenv("APRC_WORK_DIR") + "/" + prjDir;
        alma.scheduling.input.obsproject.generated.ObsProject xmlPrj =
            new alma.scheduling.input.obsproject.generated.ObsProject();
        
        xmlPrj.setScientificScore(prj.getScienceScore());
        xmlPrj.setScientificRank(prj.getScienceRank());
        xmlPrj.setPrincipalInvestigator(prj.getPrincipalInvestigator());
        ObsUnit obsUnit = prj.getObsUnit();
        xmlPrj.setObsUnitSet((alma.scheduling.input.obsproject.generated.ObsUnitSetT) getXmlObsUnit(obsUnit));
        
        String fileName = String.format("%s/ObsProject%05d.xml", absPrjDir, prj.getId()); 
        File newFile = new File(fileName);
        if (newFile.exists()) {
            logger.warning(newFile + " already exists");
            String backupFile = fileName + ".bak";
            if (newFile.renameTo(new File(backupFile))) {
                logger.info(newFile + " was moved to " + backupFile);
            } else {
                logger.severe("failed to rename file " + newFile);
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
            WeatherConstraintsT wc = new WeatherConstraintsT();
            wc.setMaxOpacity(sb.getWeatherConstraints().getMaxOpacity());
            wc.setMaxSeeing(sb.getWeatherConstraints().getMaxSeeing());
            wc.setMaxWindVelocity(sb.getWeatherConstraints().getMaxWindVelocity());
            wc.setMinPhaseStability(sb.getWeatherConstraints().getMinPhaseStability());
            xmlSchedBlock.setWeatherConstraints(wc);
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
                xmlTarget.setObsParametersIdRef(getXmlRefId(ObsParametersT.class, t.getObservingParameters().getId()));
                xmlTarget.setSourceIdRef(getXmlRefId(FieldSourceT.class, t.getSource().getId()));
                xmlTargets.put(xref, xmlTarget);
                FieldSource src = t.getSource();
                FieldSourceT xmlSrc = new FieldSourceT();
                xmlSrc.setId(getXmlRefId(FieldSourceT.class, src.getId()));
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
            xmlSbCtrl.setArrayRequested(ArrayTypeT.TWELVE_M);
            xmlSbCtrl.setEstimatedExecutionTime(ouCtrl.getEstimatedExecutionTime());
            xmlSbCtrl.setIndefiniteRepeat(sbCtrl.getIndefiniteRepeat());
            xmlSbCtrl.setMaximumTime(ouCtrl.getMaximumTime());
            xmlSchedBlock.setSchedBlockControl(xmlSbCtrl);
            return xmlSchedBlock;
        } else {
            return null;
        }
    }
	/* End Mair auld stuff no tidied up yet
	 * ============================================================= */

    
    
    /*
     * ================================================================
     * Logging
     * ================================================================
     */
	private void logProjectStatuses(
			final Map<String, ProjectStatus> projectStatuses) {
		final StringBuilder sb = new StringBuilder();
		final Formatter f = new Formatter(sb);
		f.format("Found the following %d ProjectStatus%s:%n",
				projectStatuses.size(),
				(projectStatuses.size()==1)? "": "es");
		for (final ProjectStatus ps : projectStatuses.values()) {
			f.format("\tPS uid: %s, OP uid: %s, status is %s%n",
					ps.getProjectStatusEntity().getEntityId(),
					ps.getObsProjectRef().getEntityId(),
					ps.getStatus().getState());
		}
		logger.info(sb.toString());
	}

	private void logAPDMObsProjects(
			final Map<String, alma.entity.xmlbinding.obsproject.ObsProject> apdmProjects) {
		final StringBuilder sb = new StringBuilder();
		final Formatter f = new Formatter(sb);
		f.format("Found the following %d APDM ObsProject%s:%n",
				apdmProjects.size(),
				(apdmProjects.size()==1)? "": "s");
		for (final alma.entity.xmlbinding.obsproject.ObsProject op : apdmProjects.values()) {
			f.format("\tOP uid: %s, PS uid: %s, name is %s%n",
					op.getObsProjectEntity().getEntityId(),
					op.getProjectStatusRef().getEntityId(),
					op.getProjectName());
		}
		logger.info(sb.toString());
	}

	private void logAPDMSchedBlocks(
			final Map<String, alma.entity.xmlbinding.obsproject.ObsProject> apdmProjects,
			final Map<String, alma.entity.xmlbinding.schedblock.SchedBlock> apdmSchedBlocks) {
		final StringBuilder sb = new StringBuilder();
		final Formatter f = new Formatter(sb);
		f.format("Found the following %d APDM SchedBlock%s:%n",
				apdmSchedBlocks.size(),
				(apdmSchedBlocks.size()==1)? "": "s");
		for (final alma.entity.xmlbinding.schedblock.SchedBlock schedBlock : apdmSchedBlocks.values()) {
			final alma.entity.xmlbinding.obsproject.ObsProject op =
				apdmProjects.get(schedBlock.getObsProjectRef().getEntityId());
			f.format("\tSB uid: %s, SBS uid: %s, part of %s (%s)%n",
					schedBlock.getSchedBlockEntity().getEntityId(),
					schedBlock.getSBStatusRef().getEntityId(),
					op.getProjectName(),
					op.getObsProjectEntity().getEntityId());
		}
		logger.info(sb.toString());
	}

	private void logObsProjects(List<ObsProject> result) {
	}
	/* End Logging
	 * ============================================================= */

    
    
    /*
     * ================================================================
     * ACS Bookkeeping
     * ================================================================
     */
	private static String getManagerLocation() throws SchedulingException {
		final String result = System.getProperty("ACS.manager");
		if (result == null) {
			throw new SchedulingException("Java property 'ACS.manager' is not set. It must be set to the corbaloc of the ACS manager!");
		}
		return result;
	}

	private static String getClientName() {
		return ArchiveObsProjectDaoImpl.class.getSimpleName();
	}
	/* End ACS Bookkeeping
	 * ============================================================= */
}
