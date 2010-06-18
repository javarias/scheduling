/*
 * Gets a list of projects for the sbs ids that are passed in.
 * This function is for the start queue scheduling method./*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 * 
 * File ALMAArchive.java
 * 
 */
package alma.scheduling.AlmaScheduling;

import java.io.StringReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.omg.CORBA.UserException;

import alma.ACSErrTypeCommon.IllegalArgumentEx;
import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.SchedulingExceptions.wrappers.AcsJObsProjectRejectedEx;
import alma.acs.container.ContainerServices;
import alma.acs.entityutil.EntityDeserializer;
import alma.acs.entityutil.EntityException;
import alma.acs.entityutil.EntitySerializer;
import alma.acs.logging.AcsLogger;
import alma.alarmsystem.source.ACSAlarmSystemInterface;
import alma.alarmsystem.source.ACSAlarmSystemInterfaceFactory;
import alma.alarmsystem.source.ACSFaultState;
import alma.asdmIDLTypes.IDLArrayTime;
import alma.entity.xmlbinding.obsproject.ObsProject;
import alma.entity.xmlbinding.obsproject.ObsProjectEntityT;
import alma.entity.xmlbinding.obsproject.ObsUnitControlT;
import alma.entity.xmlbinding.obsproject.ObsUnitSetT;
import alma.entity.xmlbinding.obsproject.ObsUnitSetTChoice;
import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.projectstatus.StatusBaseT;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.entity.xmlbinding.schedblock.SchedBlock;
import alma.entity.xmlbinding.schedblock.SchedBlockControlT;
import alma.entity.xmlbinding.schedblock.SchedBlockRefT;
import alma.entity.xmlbinding.specialsb.SpecialSB;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.hla.runtime.DatamodelInstanceChecker;
import alma.lifecycle.persistence.domain.StateEntityType;
import alma.lifecycle.stateengine.constants.Role;
import alma.lifecycle.stateengine.constants.Subsystem;
import alma.projectlifecycle.StateChangeData;
import alma.projectlifecycle.StateSystemHelper;
import alma.projectlifecycle.StateSystemOperations;
import alma.scheduling.AlmaScheduling.facades.BarfingStateSystem;
import alma.scheduling.AlmaScheduling.facades.LoggingStateSystem;
import alma.scheduling.AlmaScheduling.statusIF.AbstractStatusFactory;
import alma.scheduling.AlmaScheduling.statusIF.OUSStatusI;
import alma.scheduling.AlmaScheduling.statusIF.ProjectStatusI;
import alma.scheduling.AlmaScheduling.statusIF.SBStatusI;
import alma.scheduling.AlmaScheduling.statusImpl.CachedOUSStatus;
import alma.scheduling.AlmaScheduling.statusImpl.CachedProjectStatus;
import alma.scheduling.AlmaScheduling.statusImpl.CachedSBStatus;
import alma.scheduling.AlmaScheduling.statusImpl.CachedStatusFactory;
import alma.scheduling.AlmaScheduling.statusImpl.RemoteStatusFactory;
import alma.scheduling.Define.Archive;
import alma.scheduling.Define.ControlEvent;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.ExecBlock;
import alma.scheduling.Define.Policy;
import alma.scheduling.Define.Program;
import alma.scheduling.Define.Project;
import alma.scheduling.Define.SB;
import alma.scheduling.Define.SchedulingException;
import alma.scheduling.Define.SciPipelineRequest;
import alma.scheduling.Scheduler.DSA.SchedulerStats;
import alma.scheduling.utils.Profiler;
import alma.statearchiveexceptions.NoSuchEntityEx;
import alma.statearchiveexceptions.StateIOFailedEx;
import alma.stateengineexceptions.NoSuchTransitionEx;
import alma.stateengineexceptions.NotAuthorizedEx;
import alma.stateengineexceptions.PostconditionFailedEx;
import alma.stateengineexceptions.PreconditionFailedEx;
import alma.xmlentity.XmlEntityStruct;
import alma.xmlstore.ArchiveConnection;
import alma.xmlstore.ArchiveInternalError;
import alma.xmlstore.Cursor;
import alma.xmlstore.Identifier;
import alma.xmlstore.Operational;
import alma.xmlstore.ArchiveConnectionPackage.ArchiveException;
import alma.xmlstore.ArchiveConnectionPackage.PermissionException;
import alma.xmlstore.ArchiveConnectionPackage.UserDoesNotExistException;
import alma.xmlstore.CursorPackage.QueryResult;
import alma.xmlstore.OperationalPackage.DirtyEntity;
import alma.xmlstore.OperationalPackage.MalformedURI;
import alma.xmlstore.OperationalPackage.NotFound;
import alma.xmlstore.OperationalPackage.StatusStruct;

/**
 * This class provides all the functionalitiy from the archvie which 
 * is specific to the Scheduling Subsystem. It implements the Archive 
 * interface from the scheduling's define package and it connects via
 * the container services to the real archive used by all of alma.
 *
 * @version $Id: ALMAArchive.java,v 1.101 2010/06/18 15:09:45 dclarke Exp $
 * @author Sohaila Lucero
 */
public class ALMAArchive implements Archive {
    //The container services
    private ContainerServices containerServices;
    // The archive's components
    private ArchiveConnection archConnectionComp;
    private Identifier archIdentifierComp;
    private Operational archOperationComp;
    // The state system's component
    private StateSystemOperations stateSystemComp;
    private AbstractStatusFactory statusFactory;
    //The logger
    private AcsLogger logger;
    //Entity deserializer - makes entities from the archive human readable
    private EntityDeserializer entityDeserializer;
    //Entity Serializer - prepares entites for the archive
    private EntitySerializer entitySerializer;
    //The DateTime of the last query for SBs
    private DateTime lastSpecialSBQuery;
    private DateTime lastSBQuery;
    private DateTime lastProjectQuery;
    private long lastProjectStatusQuery = -1;
    
    //ALMA Clock
    private ALMAClock clock;

    private String schemaVersion="";
    private DatamodelInstanceChecker dic=null;
	private final ProjectUtil projectUtil;

    /**
      *
      */
    public ALMAArchive(ContainerServices cs, ALMAClock c){
        this.containerServices = cs;
        this.logger = cs.getLogger();
        this.clock = c;
        // By default, we make status entities which keep in synch with
        // the remotely stored versions. These are (obviously) slower
        // than ones stored locally, but do stay nicely up to date. We
        // will use locally cached ones during phases of more intense
        // activity such as ArchivePoller.pollArchive().
        this.statusFactory = CachedStatusFactory.getInstance();
        this.projectUtil = new ProjectUtil(logger, statusFactory);
        getArchiveComponents();
        getStateSystemComponent();
        getSchemaVersion();
        getDatamodelInstanceChecker();
        
        CachedStatusFactory.getInstance().setStatusSystem(
        		stateSystemComp,
        		entitySerializer,
                entityDeserializer,
                clock,
                logger);
        RemoteStatusFactory.getInstance().setStatusSystem(
        		stateSystemComp,
                entitySerializer,
                entityDeserializer,
                clock,
                logger);
    }

    protected void getSchemaVersion() {
        ObsProjectEntityT p = new ObsProjectEntityT();
        schemaVersion = p.getDatamodelVersion(); 
    }
    protected void getDatamodelInstanceChecker(){
        dic = new DatamodelInstanceChecker();
    }

    public void setLastProjectQuery(DateTime lastProjectQuery) {
        this.lastProjectQuery = lastProjectQuery;
    }
    
    public void sendAlarm(String ff, String fm, int fc, String fs) {
        try {
            ACSAlarmSystemInterface alarmSource = ACSAlarmSystemInterfaceFactory.createSource("ALMAArchive");
            ACSFaultState state = ACSAlarmSystemInterfaceFactory.createFaultState(ff, fm, fc);
            state.setDescriptor(fs);
            state.setUserTimestamp(new Timestamp(clock.getDateTime().getMillisec()));
            Properties prop = new Properties();
            prop.setProperty(ACSFaultState.ASI_PREFIX_PROPERTY, "prefix");
			prop.setProperty(ACSFaultState.ASI_SUFFIX_PROPERTY, "suffix");
			prop.setProperty("ALMAMasterScheduling_PROPERTY", "ConnArchiveException");
			state.setUserProperties(prop);
            alarmSource.push(state);
        } catch(Exception e) {
        	e.printStackTrace();
        }
        
    }
    //
    public String getIdForScheduler() throws SchedulingException {
    	 String[] s=null;
    	try {
            s = archIdentifierComp.getUIDs((short)1);
            //return s[0];
        }catch(Exception e) {
        	logger.severe("SCHEDULING: "+e.toString());
            //throw new SchedulingException (e);
        }
        return s[0];
    }
    
    //Special SB stuff (TODO right now its temporary)
    public SpecialSB[] querySpecialSBs() throws SchedulingException {
        Vector tmpSpecialSBs = new Vector();
        SpecialSB[] sbs=null;
        String query = new String("/ssb:SpecialSB");
        String schema = new String("SpecialSB");
        try {
            Cursor cursor = archOperationComp.queryDirty(query,schema);
            if(cursor == null) {
                logger.severe("SCHEDULING: cursor was null when querying SpecialSB");
                return null;
            } else {
                logger.finest("SCHEDULING: cursor not null when querying Special SB!");
            }
            while(cursor.hasNext()) {
                QueryResult res = cursor.next();
                try {
                    XmlEntityStruct xml = archOperationComp.retrieveDirty(res.identifier);
                    //System.out.println("SpecialSB: "+ xml.xmlString);
                    SpecialSB ssb= (SpecialSB)
                        entityDeserializer.deserializeEntity(xml, SpecialSB.class);
                    tmpSpecialSBs.add(ssb);
                }catch(Exception e) {
                    logger.severe("SCHEDULING: "+e.toString());
                    e.printStackTrace(System.out);
                    throw new SchedulingException (e);
                }
            }
            cursor.close();
            sbs = new SpecialSB[tmpSpecialSBs.size()];
            for(int i=0; i < tmpSpecialSBs.size();i++) {
                sbs[i] = (SpecialSB)tmpSpecialSBs.elementAt(i);
            }
            logger.fine("SCHEDULING: Scheduling found "+sbs.length+" special SBs archived");
            lastSpecialSBQuery = clock.getDateTime();
        } catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: "+e.toString());
            sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
            try {
            	Thread.sleep(1000);
            } catch (InterruptedException e1) {
            	e1.printStackTrace(System.out);
            }
            //throw new SchedulingException(e);
        } catch (SchedulingException e) {
        	logger.severe("get errors in query Special SBs");
        }
        return sbs;

    }

    // Project
    /**
     * Queries the archive and retrieves all the ObsProjects.
     * The entities retrieved from the archive are then converted into
     * the Project object defined in the Scheduling subsystem.
     * @return Project[]
     * @throws SchedulingException
     */
	public Project[] getAllProject() throws SchedulingException {
		Project[] projects = null;
		
        try {    
            Vector<Project> tmp_projects = new Vector<Project>();
            ObsProject[] obsProjects = getAllObsProjects(null);
 
            for (final ObsProject project : obsProjects) {
            	String projectId;
            	try {
            		projectId = project.getProjectStatusRef().getEntityId();
                	try {
                		ProjectStatusI ps = getProjectStatusForObsProject(project);
                		
                        //TODO should check project queue.. if project exists don't map a new one.
                        SchedBlock[] sbs = getSBsFromObsProject(project, null);
                        //add here to check if the sbs get without problem
                        if (sbs!=null) {	
                        	Project p = projectUtil.map(project, sbs, ps, 
                                new DateTime(System.currentTimeMillis()));
                        	if (p!=null){
                        		tmp_projects.add(p);
                        	}
                        } else {
                            logger.warning(String.format(
                            		"No SchedBlocks for project %s",
                            		projectId));
                            AcsJObsProjectRejectedEx ex = new AcsJObsProjectRejectedEx();
                            ex.setProperty("UID", projectId);
                            ex.setProperty("Reason", "No SchedBlocks for project");
                            ex.log(logger);

                        }
                	} catch (SchedulingException e) {
                		logger.warning(e.getLocalizedMessage());
//                        logger.warning(String.format(
//                        		"Cannot find status object for project %s",
//                        		projectId));
                	}
            	} catch (NullPointerException e) {
            		logger.warning(String.format(
                    		"Project from archive has no EntityId, project name is %s, PI is %s",
                    		project.getProjectName(), project.getPI()));
            		projectId = null;
            	}
            }
            
            projects = new Project[tmp_projects.size()];
            for(int i=0; i < tmp_projects.size();i++) {
                projects[i] = tmp_projects.elementAt(i);
            }
	        logger.fine("SCHEDULING: Scheduling converted "+projects.length+
                    " Projects from ObsProject found archived.");
            //return projects;
        } catch (SchedulingException e1) {
        	logger.severe("Scheduling encounter errors when get obsproject from archive");
        } catch (Exception e) {
            sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
            try {
            	Thread.sleep(1000);
            } catch (InterruptedException e1) {
            	e1.printStackTrace(System.out);
            }
            logger.severe("Scheduling encounter errors when get all projects from archive!!");
            //throw new SchedulingException (e);
        }
        return projects;
    }

    public Project checkProjectForUpdates(String id) throws SchedulingException {
        return null;
    }

    public synchronized ProjectStatusI getProjectStatus(Project p) throws SchedulingException {
        return statusFactory.createProjectStatus(p.getProjectStatusId());
    }

    public synchronized ProjectStatusI getProjectStatusForObsProject(ObsProject p) throws SchedulingException {
    	final String statusId = p.getProjectStatusRef().getEntityId();
    	ProjectStatusI result = null;
    	try {
    		result = statusFactory.createProjectStatus(statusId);
    	} catch (SchedulingException e) {
    		throw new SchedulingException(String.format(
    				"Cannot find Project Status entity %s for Project %s",
    				statusId, p.getObsProjectEntity().getEntityId()), e);
    	}
    	
    	return result;
    }

    protected boolean matchSchemaVersion(String x){
        boolean match = false;
        //get entity's version 
        try {
            StringReader  rdr = new StringReader(x);
            String entityVersion = dic.getDatamodelVersion(rdr);
            //compare to schemaVersion
            logger.fine("SCHEDULING: SchemaVersion of entity = "+entityVersion+
                        "; SchemaVersion Scheduling likes = "+schemaVersion);
            if(entityVersion.equals(schemaVersion)){
                match = true;
            }
        }catch(Exception e){
            logger.warning("SCHEDULING: Error trying to compare schema versions. Will not do a compare");
        }
        return match;

    }
    

    /**
      * retruns all the ObsProjects in the archive.
      */
    public ObsProject[] getAllObsProjects(ProjectStatusQueue prjStatusQueue) throws SchedulingException {
        Vector tmpObsProject = new Vector();
        ObsProject[] projects=null;
        String query = new String("/prj:ObsProject");
        String schema = new String("ObsProject");
        XmlEntityStruct xml =null;
        try {
            checkArchiveStillActive();
            if(lastProjectQuery != null) {
                try{
                    logger.fine("Last query time = "+lastProjectQuery.toString());
                    String[] newArchUpdates =new String[0];
                    try {
                        logger.fine("SCHEDULING: sent to archive as "+lastProjectQuery.toString()+".000");
                        newArchUpdates = archOperationComp.queryRecent(schema, lastProjectQuery.toString()+".000");
                        logger.fine("There are "+newArchUpdates.length+" new project updates!");
                    } catch(Exception e){
                        sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
                        e.printStackTrace(System.out);
                    }
                    ObsProject p=null;
                    StatusStruct status=null;
                    for(int i=0; i <  newArchUpdates.length; i++) {
                        
                        // Check that the ObsProject is in the status queue.
                        // If it is not, then it means that the ObsProject is not in a runnable
                        // state and it should be discarded.
                        if (prjStatusQueue != null) {
                            ProjectStatusI prjStatus = prjStatusQueue.getStatusFromProjectId(newArchUpdates[i]);
                            if (prjStatus == null)
                                continue;
                        }
                        
                        logger.fine("SCHEDULING: About to retrieve project with uid "+newArchUpdates[i]+", gotten from queryRecent");
                        xml = archOperationComp.retrieveDirty( newArchUpdates[i] );
                        //System.out.println("SchemaVersion in XmlEntityStruct: "+xml.schemaVersion);
                        logger.fine("timestamp = "+xml.timeStamp);
                        //if( matchSchemaVersion(xml.xmlString) ){ //} else { }
                        try {
                            p = (ObsProject)entityDeserializer.deserializeEntity(xml, ObsProject.class);
                            tmpObsProject.add(p);
                        }catch(EntityException ee) {
                            matchSchemaVersion(xml.xmlString);
                            logger.warning("SCHEDULING: ObsProject ("+newArchUpdates[i]+") doesnt match the "+
                                    "schema version scheduling was compiled against. Scheduling will not "+
                                    "recognize this ObsProject until it is recompiled against the APDM/castor "+
                                    "classes generated for this ObsProject's schema or the project is converted "+
                                    "to match this version of the APDM.");
                        }
                                    
                    } 
                }catch(Exception e){
                    logger.severe("SCHEDULING: Error "+e.toString());
                    sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
                    e.printStackTrace(System.out);
                }
            } else {
                //nothing's been queried yet 
                Cursor cursor = archOperationComp.queryDirty(query,schema);
                if(cursor == null) {
                    logger.severe("SCHEDULING: cursor was null when querying ObsProjects");
                    return null;
                } else {
                    logger.finest("SCHEDULING: cursor not null!");
                }
                while(cursor.hasNext()) {
                    QueryResult res = cursor.next();
                    try {
                        
                        // Check that the ObsProject is in the status queue.
                        // If it is not, then it means that the ObsProject is not in a runnable
                        // state and it should be discarded.
                        if (prjStatusQueue != null) {
                            ProjectStatusI prjStatus = prjStatusQueue.getStatusFromProjectId(res.identifier);
                            if (prjStatus == null)
                                continue;
                        }
                        
                        logger.fine("SCHEDULING: About to retrieve project with uid "+res.identifier+", gotten from Query");
                        xml = archOperationComp.retrieveDirty(res.identifier);
                        //System.out.println("SchemaVersion in xmlEntityStruct: "+xml.schemaVersion);
                        //logger.finest("PROJECT : "+ xml.xmlString);
                        //System.out.println("PROJECT taken out of archive: "+ xml.xmlString);

                        try {
                            ObsProject obsProj= (ObsProject)
                                entityDeserializer.deserializeEntity(xml, ObsProject.class);
                            tmpObsProject.add(obsProj);
                        }catch(EntityException ee) {
                            matchSchemaVersion(xml.xmlString);
                            logger.warning("SCHEDULING: ObsProject ("+res.identifier+") doesnt match the "+
                                    "schema version scheduling was compiled against. Scheduling will not "+
                                    "recognize this ObsProject until it is recompiled against the APDM/castor "+
                                    "classes generated for this ObsProject's schema");
                        }
                    }catch(Exception e) {
                        sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
                        try {
                        	Thread.sleep(1000);
                        } catch (InterruptedException e1) {
                        	e1.printStackTrace(System.out);
                        }
                        logger.severe("SCHEDULING: "+e.toString());
                        e.printStackTrace(System.out);
                        //throw new SchedulingException (e);
                    }
                }
                cursor.close();
            }
            projects = new ObsProject[tmpObsProject.size()];
            for(int i=0; i < tmpObsProject.size();i++) {
                projects[i] = (ObsProject)tmpObsProject.elementAt(i);
            }
            lastProjectQuery = clock.getDateTime();
            logger.fine("SCHEDULING: Scheduling found "+projects.length+" projects archived.");
        } catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: "+e.toString());
            sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
            try {
            	Thread.sleep(1000);
            } catch (InterruptedException e1) {
            	e1.printStackTrace(System.out);
            }
            //throw new SchedulingException(e);
        } //catch (SchedulingException e) {
        	//logger.severe("Errors in getAllObsProjects");
        //}
        return projects;

    }
    
	
	/**
	 * Convert all the projects in the archive which are in state
	 * "from" to stat "to". This is a hideous frig for R7.0 to help
	 * AIV staff during commissioning. As such it is probably with
	 * us forever...
	 * 
	 * @param from - the state from which we wish to convert
	 * @param to   - the state to which we wish to convert projects
	 * @throws SchedulingException 
	 */
	public void convertProjects(StatusTStateType from,
			                    StatusTStateType to) throws SchedulingException {
		final String[] fromStates = new String[1];
		fromStates[0] = from.toString();
		
    	final ProjectStatusQueue fromPSs = getProjectStatusesByState(fromStates);
    	
    	int worked = 0;
    	int failed = 0;

    	for (final String psID : fromPSs.getAllIds()) {
    		try {
				stateSystemComp.changeProjectStatus(
						psID,
						to.toString(),
						Subsystem.SCHEDULING,
						Role.AOD);
				worked ++;
			} catch (UserException e) {
				logger.warning(String.format(
						"cannot convert project status %s from %s to %s - %s",
						psID, from, to, e.getLocalizedMessage()));
				failed ++;
			}
    	}
    	
    	if (worked + failed == 0) {
    		// there were no projects to convert
    		logger.info(String.format(
    				"on-the-fly conversion of projects from %s to %s: no candidate projects found.",
    				from, to));
    	} else if (failed == 0) {
    		// Don't admit to even the possibility of failure if you
    		// don't have to.
    		logger.info(String.format(
    				"on-the-fly conversion of projects from %s to %s: %d converted.",
    				from, to, worked));
    	} else {
    		logger.warning(String.format(
    				"on-the-fly conversion of projects from %s to %s: %d converted, %d failed.",
    				from, to, worked, failed));
    	}
	}

	public void convertSchedBlocks(StatusTStateType from,
               StatusTStateType to) throws SchedulingException {
        final String[] fromStates = new String[1];
        fromStates[0] = from.toString();
        
        final SBStatusQueue fromSBSs = getSBStatusesByState(fromStates);
        
        int worked = 0;
        int failed = 0;

        for (final String sbsID : fromSBSs.getAllIds()) {
            try {
                stateSystemComp.changeSBStatus(
                        sbsID,
                        to.toString(),
                        Subsystem.SCHEDULING,
                        Role.AOD);
                worked ++;
            } catch (UserException e) {
                logger.warning(String.format(
                        "cannot convert SB status %s from %s to %s - %s",
                        sbsID, from, to, e.getLocalizedMessage()));
                failed ++;
            }
        }
        
        if (worked + failed == 0) {
            // there were no projects to convert
            logger.info(String.format(
                    "on-the-fly conversion of schedblocks from %s to %s: no candidate schedblocks found.",
                    from, to));
        } else if (failed == 0) {
            // Don't admit to even the possibility of failure if you
            // don't have to.
            logger.info(String.format(
                    "on-the-fly conversion of schedblocks from %s to %s: %d converted.",
                    from, to, worked));
        } else {
            logger.warning(String.format(
                    "on-the-fly conversion of schedblocks from %s to %s: %d converted, %d failed.",
                    from, to, worked, failed));
        }	       
	}
	
	/**
	 * Get all the project statuses that are in the state archive in a
	 * given set of states.
	 * 
	 * @param states - we are interested in ProjectStatuses in any of
	 *                these states.
	 * @return a StatusEntityQueue<ProjectStatusI, ProjectStatusRefT>
	 *         containing all the ProjectStatus entities found
	 * 
	 * @throws SchedulingException
	 */
    public ProjectStatusQueue getProjectStatusesByState(String[] states)
				throws SchedulingException {
        final ProjectStatusQueue result = new ProjectStatusQueue(logger);
        
		XmlEntityStruct xml[] = null;
		try {
			xml = stateSystemComp.findProjectStatusByState(states);
		} catch (Exception e) {
        	logger.finest("Scheduling can not pull ProjectStatuses from State System");
            e.printStackTrace(System.out);
            sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
            try {
            	Thread.sleep(1000);
            } catch (InterruptedException e1) {
            	e1.printStackTrace(System.out);
            }
		}
		
		for (final XmlEntityStruct xes : xml) {
			try {
				final ProjectStatus ps = (ProjectStatus) entityDeserializer.
				deserializeEntity(xes, ProjectStatus.class);
				result.add(new CachedProjectStatus(ps));
			} catch (Exception e) {
	        	logger.finest("Scheduling can not deserialise ProjectStatus from State System");
	            e.printStackTrace(System.out);
	            sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
	            try {
	            	Thread.sleep(1000);
	            } catch (InterruptedException e1) {
	            	e1.printStackTrace(System.out);
	            }
			}
		}
		
		return result;
	}

    
    private ProjectStatus getProjectStatus(final String id) {
        XmlEntityStruct xml = null;
        ProjectStatus result = null;
        try {
            xml = stateSystemComp.getProjectStatus(id);
        } catch (Exception e) {
            logger.finest(String.format(
            		"Scheduling can not pull ProjectStatus %s from State System",
            		id));
            e.printStackTrace(System.out);
            sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                e1.printStackTrace(System.out);
            }
        }
        try {
            result = (ProjectStatus) entityDeserializer.
                deserializeEntity(xml, ProjectStatus.class);
        } catch (Exception e) {
            logger.finest(String.format(
            		"Scheduling can not deserialise ProjectStatus %s from State System",
            		id));
            e.printStackTrace(System.out);
            sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                e1.printStackTrace(System.out);
            }
        }    
        
        return result;
    }
    
    private boolean isWanted(StatusBaseT statusEntity, String[] wantedStates) {
    	final String state = statusEntity.getStatus().getState().toString();
    	
		for (String wantedState : wantedStates) {
			if (state.equals(wantedState)) {
				return true;
			}
		}
		return false;
    }
    
    /**
     * Get the ProjectStatuses indicated by ids ant sort them into ones
     * in the desired states and the ones not in those states.
     * 
     * @param ids - the EntityIds of the ProjectStatuses we should get
     *              and sort
     * @param wanted - where to put the ProjectStatuses which are in
     *                 one of the wantedStates
     * @param unwanted - where to put the ProjectStatuses which are not
     *                   in one of the wantedStates
     * @param wantedStates - the states to use for sorting the
     *                       ProjectStatuses
     */
    private void sortProjectStatuses(Iterable<String>   ids,
    								 ProjectStatusQueue wanted,
    								 ProjectStatusQueue unwanted,
    								 String[]           wantedStates) {
        for (String id : ids) {
        	final ProjectStatus ps = getProjectStatus(id);
        	if (ps != null) {
        		if (isWanted(ps, wantedStates)) {
        			wanted.add(new CachedProjectStatus(ps));
    				logger.finer(String.format("adding ProjectStatus %s (in state %s) to wanted queue",
    						ps.getProjectStatusEntity().getEntityId(),
    						ps.getStatus().getState().toString()));
        		} else {
        			unwanted.add(new CachedProjectStatus(ps));
    				logger.finer(String.format("adding ProjectStatus %s (in state %s) to unwanted queue",
    						ps.getProjectStatusEntity().getEntityId(),
    						ps.getStatus().getState().toString()));
        		}
        	}
        }
    }
    
    public ProjectStatusQueue getProjectStatusesByIDs(Iterable<String> ids, String[] opRunnableStates)
        throws SchedulingException {
        final ProjectStatusQueue result = new ProjectStatusQueue(logger);
        
        for (String id : ids) {
        	final ProjectStatus ps = getProjectStatus(id);

        	if (ps != null) {
        		for (String runnableState : opRunnableStates) {
        			if (ps.getStatus().getState().toString().equals(runnableState)) {
        				logger.finer("adding ProjectStatus " +
        						ps.getProjectStatusEntity().getEntityId() +
        				" to queue");
        				result.add(new CachedProjectStatus(ps));
        				break;
        			}
        		}
        	}
        }
        
        return result;
    }
        
    public ProjectStatusQueue getProjectStatusesByStateAndDate() {
        
        return null;
    }
    
	/**
	 * Get all the SB statuses that are in the state archive in a
	 * given set of states.
	 * 
	 * @param states - we are interested in SBStatuses in any of
	 *                these states.
     * @param initialise - if <code>true</code>, then initialise the
     *                     SBStatuses' execution count.
	 * @return an StatusEntityQueue<SBStatusI, SBStatusRefT> containing
	 *         all the ProjectStatus entities found
	 * 
	 * @throws SchedulingException
	 */
	public SBStatusQueue getSBStatusesByState(String[] states)
				throws SchedulingException {
        final SBStatusQueue result = new SBStatusQueue(logger);
        
		XmlEntityStruct xml[] = null;
		try {
			xml = stateSystemComp.findSBStatusByState(states);
		} catch (Exception e) {
        	logger.finest("Scheduling can not pull SBStatuses from State System");
            e.printStackTrace(System.out);
            sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
            try {
            	Thread.sleep(1000);
            } catch (InterruptedException e1) {
            	e1.printStackTrace(System.out);
            }
		}
		
		for (final XmlEntityStruct xes : xml) {
			try {
				final SBStatus sbs = (SBStatus) entityDeserializer.
				deserializeEntity(xes, SBStatus.class);
				result.add(new CachedSBStatus(sbs));
			} catch (Exception e) {
	        	logger.finest("Scheduling can not deserialise SBStatus from State System");
	            e.printStackTrace(System.out);
	            sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
	            try {
	            	Thread.sleep(1000);
	            } catch (InterruptedException e1) {
	            	e1.printStackTrace(System.out);
	            }
			}
		}
		
		return result;
	}

	public SBStatusQueue getSBStatusesByProjectStatusIds(ProjectStatusQueue runnablePSs, String[] sbRunnableStates) {
	    final SBStatusQueue result = new SBStatusQueue(logger);
	    for (ProjectStatusI ps : runnablePSs.getAll()) {
	    	final String uid = ps.getUID();
	        XmlEntityStruct xml[] = null;
	        try {
	            xml = stateSystemComp.getSBStatusListForProjectStatus(uid);
	        } catch (Exception e) {
	            logger.warning(String.format(
	            		"Scheduling can not get SBStatuses for ProjectStatus %s (ObsProject %s) from State System - %s",
	            		uid, ps.getDomainEntityId(), e.getMessage()));
	            e.printStackTrace(System.out);
	            sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
	            try {
	                Thread.sleep(1000);
	            } catch (InterruptedException e1) {
	                e1.printStackTrace(System.out);
	            }
	        }
	        for (final XmlEntityStruct xes : xml) {
	            try {
	                final SBStatus sbs = (SBStatus) entityDeserializer.
	                	deserializeEntity(xes, SBStatus.class);
	                if (isWanted(sbs, sbRunnableStates)) {
	    				logger.finer(String.format("adding SBStatus %s (in state %s) to wanted queue",
	    						sbs.getSBStatusEntity().getEntityId(),
	    						sbs.getStatus().getState().toString()));
	                	result.add(new CachedSBStatus(sbs));
	                } else {
	    				logger.finer(String.format("rejecting SBStatus %s (in state %s) as unwanted",
	    						sbs.getSBStatusEntity().getEntityId(),
	    						sbs.getStatus().getState().toString()));
	                }
	            } catch (Exception e) {
		            logger.warning(String.format(
		            		"Scheduling can not deserialise SBStatus %s for ProjectStatus %s (ObsProject %s) from State System - %s",
		            		xes.entityId, uid, ps.getDomainEntityId(), e.getMessage()));
	                e.printStackTrace(System.out);
	                sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
	                try {
	                    Thread.sleep(1000);
	                } catch (InterruptedException e1) {
	                    e1.printStackTrace(System.out);
	                }
	            }
	        }	        
	    }
	    return result;
	}
	
    public SchedBlock[] getSBsFromObsProject(ObsProject p, SBStatusQueue sbStatusQueue)
        throws SchedulingException {
        List<SchedBlock> retVal = new ArrayList<SchedBlock>();
        if (p.getObsProgram().getObsPlan().getObsUnitSetTChoice() == null) {
            logger.severe("SCHEDULING: no sbs stuff available in project");
        } else {
            SchedBlockRefT[] sbs_refs = getSBRefs(p.getObsProgram().getObsPlan().getObsUnitSetTChoice());
            for(int i=0; i < sbs_refs.length; i++){
                //get the sb
                if (sbStatusQueue != null) {
                    SBStatusI sbStatus = sbStatusQueue.getStatusFromSBId(sbs_refs[i].getEntityId());
                    if (sbStatus == null)
                        continue;
                }

                SchedBlock sb = getSchedBlock(sbs_refs[i].getEntityId());
                if (sb != null)
                    retVal.add(sb);
            }
        }
        return retVal.toArray(new SchedBlock[0]);
    }
	
    /**
     * Get the SchedBlocks for the given project, but only those which
     * have a status in the supplied queue.
     * 
     * @param p - the ObsProject for which we want the SchedBlocks
     * @param sbsQ - the status queue containing the SBStatuses for
     *               SchedBlocks we care about.
     * @return an array of SchedBlocks matching the criteria given.
     * 
     * @throws SchedulingException
     */
    public SchedBlock[] getSelectedSBsFromObsProject(ObsProject     p,
    		                                         SBStatusQueue sbsQ)
    		throws SchedulingException {
    	SchedBlock[] sbs=null;
        if(p.getObsProgram().getObsPlan().getObsUnitSetTChoice() == null) {
            logger.severe("SCHEDULING: no sbs stuff available in project");
            //throw new SchedulingException("No SB info in ObsProject");
        } else {
            SchedBlockRefT[] sbs_refs = getSBRefs(p.getObsProgram().getObsPlan().getObsUnitSetTChoice());
            //SchedBlock[] sbs = new SchedBlock[sbs_refs.length];
            sbs = new SchedBlock[sbs_refs.length];
            for(int i=0; i < sbs_refs.length; i++){
                //get the sb
            	if (sbsQ.getStatusFromSBId(sbs_refs[i].getEntityId()) != null) {
            		sbs[i] = getSchedBlock(sbs_refs[i].getEntityId());
            		if(sbs[i]==null) {
            			// this means the xml deserializeEntity got
            			// problem and we should ignore this objproject
            			return null;
            		}
            	} else {
            		sbs[i] = null;
            	}
            }
            /*
            logger.info("SCHEDULING: Scheduling found that project "+
                    p.getObsProjectEntity().getEntityId() +" has "+
                    sbs.length+" sbs");
                    */
            //return sbs;
        }
        return sbs;
    }

    /**
      *
      */
    public ObsProject getObsProject(String id) throws SchedulingException {
        ObsProject proj=null;
        try {
            logger.fine("SCHEDULING: About to retrieve project with uid "+id);
            XmlEntityStruct xml = archOperationComp.retrieveDirty(id);
            proj = (ObsProject) entityDeserializer.deserializeEntity(xml, ObsProject.class);
        }catch(Exception e){
            sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
            try {
            	Thread.sleep(1000);
            } catch (InterruptedException e1) {
            	e1.printStackTrace(System.out);
            }
            //throw new SchedulingException (e);
        }
        return proj;
    }
        
    /**
      *
      */
    public SB[] getSBsForProject(String projId) throws SchedulingException {
        Project p = getProject(projId);
        return p.getAllSBs();
    }
    
    /**
      * Gets all the schedblock references from the ObsUnitSetTChoice.
      */
    private SchedBlockRefT[] getSBRefs(ObsUnitSetTChoice choice) throws SchedulingException {
        if(choice == null) {
            logger.severe("SCHEDULING: choice is null..");
            //throw new SchedulingException("ObsUnitSetTChoice == null");
        }
        if(choice.getObsUnitSetCount() == 0) {
            return choice.getSchedBlockRef();
        } else {
            Vector tmpSBRefs = new Vector();
            ObsUnitSetT[] sets = choice.getObsUnitSet();
            for(int i=0; i < sets.length; i++) {
                tmpSBRefs.add(getSBRefs(sets[i].getObsUnitSetTChoice()));
            }
            Vector tmpsbs = new Vector();
            for(int j=0; j < tmpSBRefs.size(); j++){
                SchedBlockRefT[] refs = (SchedBlockRefT[])tmpSBRefs.elementAt(j);
                for(int k=0; k < refs.length; k++) {
                    tmpsbs.add(refs[k]);
                }
            }
            SchedBlockRefT[] sbRefs = new SchedBlockRefT[tmpsbs.size()];
            for(int l=0; l < tmpsbs.size(); l++){
                sbRefs[l] = (SchedBlockRefT)tmpsbs.elementAt(l);
            }
            return sbRefs;
        }
    }
    
    /**
      * Gets the SchedBlock with the given id from the archive
      */
    private SchedBlock getSchedBlock(String id) throws SchedulingException {
        
        SchedBlock sb = null;
        try {
            Profiler prof = new Profiler(logger);
            prof.start("about to retrieve SB with uid " + id);
            XmlEntityStruct xml = archOperationComp.retrieveDirty(id);
            prof.end();
            prof.start("deserializing SB XML");
            sb = (SchedBlock) entityDeserializer.deserializeEntity(xml, SchedBlock.class);
            prof.end();
        }catch(Exception e){
        	logger.severe("SchedBlock id:"+id);
        	logger.severe("Trying to deserialize xml file to SchedBlock fail!! check if APDM update");
        	return null;
        }
        return sb;
    }



    /**
      *
      */
    public synchronized void updateProjectStatus(ProjectStatusI ps) throws SchedulingException {
    	/*
    	 * No need to do this any more as we're working via the State Archive
    	 */
//        try {
//            XmlEntityStruct xml = entitySerializer.serializeEntity(ps, ps.getProjectStatusEntity());
//            //logger.finest("SCHEDULING: updated PS: "+xml.xmlString);
//            //logger.finest("SCHEDULING: About to retrieve Project Status with uid "+ps.getProjectStatusEntity().getEntityId());
//            XmlEntityStruct xml2 = archOperationComp.retrieveDirty(ps.getProjectStatusEntity().getEntityId());
//            xml2.xmlString = xml.xmlString;
//            //logger.finest("About to save PS: "+xml2.xmlString);
//            archOperationComp.update(xml2);
//        } catch(Exception e){
//            logger.severe("SCHEDULING: error updating PS in archive, "+e.toString());
//            e.printStackTrace(System.out);
//            //throw new SchedulingException (e);
//        }
    }
    /**
     * Queries the archive for all the new projects stored after the given time,
     * 'time'. They are returned, coverted to the Scheduling's project object.
     * @param time A DateTime object.
     * @return Project[]
     * @throws SchedulingException
     */
	public Project[] getNewProject(DateTime time) throws SchedulingException{
        return null;
    }

    /**
     * Gets one ObsProject (with the given id) and the SchedBlocks
     * that it contains and that we are interested in, and maps it
     * using the projectUtil.
     * @param projectId The id of the ObsProject
     * @param sbsQ      Filter the SBs with this - we only care about
     *                  SBs which have a filter in here.
     * @return Project
     * @throws SchedulingException
     */
    public Project getFilteredProject(String        projectId,
    		                          SBStatusQueue sbsQ)
    			throws SchedulingException {
        Project project = null;
        try {
            XmlEntityStruct xml = archOperationComp.retrieve(projectId);
            ObsProject obsProj= (ObsProject)
                entityDeserializer.deserializeEntity(xml, ObsProject.class);
            project = projectUtil.map(obsProj,
            		getSelectedSBsFromObsProject(obsProj, sbsQ), 
            		getProjectStatusForObsProject(obsProj),
            		clock.getDateTime());
        } catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: "+e.toString());
        } catch(NotFound e) {
            logger.severe("SCHEDULING: "+e.toString());
        } catch(MalformedURI e) {
            logger.severe("SCHEDULING: "+e.toString());
        //} catch(DirtyEntity e) {
        //    logger.severe("SCHEDULING: "+e.toString());
        } catch(Exception e) {
            logger.severe("SCHEDULING: "+e.toString());
        }
        return project;
    }

    /**
     * Queries the archive for the project with the given id.
     * @param id The id of the project
     * @return Project
     * @throws SchedulingException
     */
    public Project getProject(String id) throws SchedulingException{
        Project project = null;
        try {
            XmlEntityStruct xml = archOperationComp.retrieveDirty(id);
            ObsProject obsProj= (ObsProject)
                entityDeserializer.deserializeEntity(xml, ObsProject.class);
            project = projectUtil.map(obsProj, getSBsFromObsProject(obsProj, null), 
                    getProjectStatusForObsProject(obsProj), clock.getDateTime());
        } catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: "+e.toString());
        } catch(NotFound e) {
            logger.severe("SCHEDULING: "+e.toString());
        } catch(MalformedURI e) {
            logger.severe("SCHEDULING: "+e.toString());
        //} catch(DirtyEntity e) {
        //    logger.severe("SCHEDULING: "+e.toString());
        } catch(Exception e) {
            logger.severe("SCHEDULING: "+e.toString());
        }
        return project;
    }

    /**
     * Given a project object, its archive entry is retrieved, updated 
     * and stored back into the archive.
     * @param p The project to update in the archive
     * @throws SchedulingException
     */
	public void updateProject(Project p) throws SchedulingException {
    }

	// Program
	public Program getProgram(String id) throws SchedulingException{
        Program program=null;
        try {
            XmlEntityStruct xml = archOperationComp.retrieveDirty(id);
            //project = convertToProgram2(xml);
            //XmlEntityStruct xml = archOperationComp.retrieve(id);
            //XmlEntityStruct xml = archOperationComp.updateRetrieve(id);
        } catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: "+e.toString());
        } catch(NotFound e) {
            logger.severe("SCHEDULING: "+e.toString());
        } catch(MalformedURI e) {
            logger.severe("SCHEDULING: "+e.toString());
        //} catch(DirtyEntity e) {
        //    logger.severe("SCHEDULING: "+e.toString());
        } catch(Exception e) {
            logger.severe("SCHEDULING: "+e.toString());
        }
        return program;
    }
    public void updateProgram(Program s) throws SchedulingException{ }
    
    //General
   /* private Cursor generalQuery(String schema, String query, DateTime lastUpdate){
        try {
            Cursor cursor = archOperationComp.queryRecent(query, schema);
            return cursor
        } catch (ArchiveInteralError e){
            logger.severe("SCHEDULING: "+e.toString());
            throw new SchedulingException (e);
        }
    }*/
    
    // ProjectStatus 
//    public ProjectStatus[] queryRecentProjectStatus() throws SchedulingException {
//        String schema = new String("ProjectStatus");
//        String query = new String("/ps:ProjectStatus");
//        ProjectStatus[] ps=new ProjectStatus[0];
//        XmlEntityStruct xml;
//        try {
//            if (lastProjectStatusQuery != null){
//                String[] ids = archOperationComp.queryRecent(schema, lastProjectStatusQuery.toString()+".000");
//                if(ids.length > 0){
//                    ps = new ProjectStatus[ids.length];
//                    for(int i=0; i < ids.length; i++){
//                        xml = archOperationComp.retrieve(ids[i]);
//                        ps[i] = (ProjectStatus)entityDeserializer.deserializeEntity(xml, ProjectStatus.class);
//                    }
//                }
//            } else {
//                Cursor cursor = archOperationComp.query(query, schema);
//                Vector tmp = new Vector();
//                while(cursor.hasNext()){
//                    QueryResult res = cursor.next();
//                    xml = archOperationComp.retrieve(res.identifier);
//                    tmp.add((ProjectStatus)entityDeserializer.deserializeEntity(xml, ProjectStatus.class));
//                }
//                ps = new ProjectStatus[tmp.size()];
//                ps = (ProjectStatus[])tmp.toArray(ps);
//            }
//            lastProjectStatusQuery = clock.getDateTime();
//            //return ps;
//        }catch(NotFound e) {
//            logger.severe("SCHEDULING: "+e.toString());
//            //throw new SchedulingException (e);
//        }catch(MalformedURI e) {
//            logger.severe("SCHEDULING: "+e.toString());
//            //throw new SchedulingException (e);
//        }catch(EntityException e) {
//            logger.severe("SCHEDULING: "+e.toString());
//            //throw new SchedulingException (e);
//        }catch(DirtyEntity e) {
//            logger.severe("SCHEDULING: "+e.toString());
//            //throw new SchedulingException (e);
//        }catch(ArchiveInternalError e) {
//            logger.severe("SCHEDULING: "+e.toString());
//            //throw new SchedulingException (e);
//        }
//        return ps;
//    }

	// SB
    public SchedBlock[] queryRecentSBs() throws SchedulingException {
        String schema = new String("SchedBlock");
        String query = new String("/sbl:SchedBlock");
        SchedBlock[] sbs=new SchedBlock[0];
        XmlEntityStruct xml;
        try {
            if (lastSBQuery != null){
                String[] ids = archOperationComp.queryRecent(schema, lastSBQuery.toString()+".000");
		logger.info("<queryRecentSB> amount:"+ids.length);
                if(ids.length > 0){
                    sbs = new SchedBlock[ids.length];
                    for(int i=0; i < ids.length; i++){
                        xml = archOperationComp.retrieve(ids[i]);
                        sbs[i] = (SchedBlock)entityDeserializer.deserializeEntity(xml, SchedBlock.class);
                    }
                }
            } else {
                Cursor cursor = archOperationComp.query(query, schema);
                Vector tmp = new Vector();
                while(cursor.hasNext()){
                    QueryResult res = cursor.next();
                    xml = archOperationComp.retrieve(res.identifier);
                    tmp.add((SchedBlock)entityDeserializer.deserializeEntity(xml, SchedBlock.class));
                }
                sbs = new SchedBlock[tmp.size()];
                sbs = (SchedBlock[])tmp.toArray(sbs);
            }
            lastSBQuery = clock.getDateTime();
            //return sbs;
        }catch(NotFound e) {
            logger.severe("SCHEDULING: "+e.toString());
            //throw new SchedulingException (e);
        }catch(MalformedURI e) {
            logger.severe("SCHEDULING: "+e.toString());
            //throw new SchedulingException (e);
        }catch(EntityException e) {
            logger.severe("SCHEDULING: "+e.toString());
            //throw new SchedulingException (e);
        }catch(DirtyEntity e) {
            logger.severe("SCHEDULING: "+e.toString());
            //throw new SchedulingException (e);
        }catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: "+e.toString());
            //throw new SchedulingException (e);
        }
        return sbs;
    }

    /**
     * True if there are new ObsProject in the XML Store since
     * the last time that the ObsProjects were retrieved, therefore
     * an update is necessary.
     * <P>
     * Used to decide whether to call or not the State Archive.
     *  
     * @return True if an update is necessary, false otherwise
     */
    public String[] getProjectsToUpdate() throws SchedulingException {
        if ( lastProjectQuery == null ) return null;
        String[] ids = null;
        try {
            ids = archOperationComp
                  .queryRecent("ObsProject",
                               lastProjectQuery.toString() + ".000");
        } catch (ArchiveInternalError ex) {
            ex.printStackTrace();
            throw new SchedulingException("Error querying Archive for recent ObsProjects");
        }
        return ids;
    }
    
    /** 
     *
     */
	public SB[] getAllSB() throws SchedulingException{
        SB sbs[]=null;
        Vector tmp_sbs = new Vector();
        int i = 0;// the index of the SB array, sbs
        try {
            Cursor cursor = archOperationComp.query("/sbl:ScheduBlock", "SchedBlock");
            while (cursor.hasNext()){
                QueryResult res = cursor.next();
                try {
                    tmp_sbs.add(convertToSB1(res));
                } catch (Exception e) {
                    throw new SchedulingException (e);
                }
            }  
            int size = tmp_sbs.size();
            sbs = new SB[size];
            for (int x=0; x < size; x++) {
                sbs[x] = (SB)tmp_sbs.elementAt(x);
		//logger.info("<ALL SB>SB names:"+sbs[x].getSBName());
            }
            cursor.close();
        }catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: "+e.toString());
            //throw new SchedulingException (e);
        } catch(SchedulingException se) {
        	logger.severe("Get error when add SchedBlock");
            logger.severe("SCHEDULING: "+se.toString());
            //throw new SchedulingException (se);
        }
        return sbs;
    }
    
	public SB[] getNewSB(DateTime time) throws SchedulingException {
        return null;
    }

    /**
     * Retrieves a SchedBlock from the archive given the 'id' and converts it
     * to a SB object.
     * 
     * @param String The id of the SB to be retrieved
     * @return SB The converted SchedBlock to the scheduling's SB
     * @throws SchedulingException
     */
	public SB getSB(String id) throws SchedulingException{
        SB sb = null;
       /* try {
            XmlEntityStruct xml = archOperationComp.retrieveDirty(id);
            //System.out.println("SB: "+xml.xmlString);
            sb = convertToSB2(xml);
            //XmlEntityStruct xml = archOperationComp.retrieve(id);
            //XmlEntityStruct xml = archOperationComp.updateRetrieve(id);
        } catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: "+e.toString());
        } catch(NotFound e) {
            logger.severe("SCHEDULING: "+e.toString());
        } catch(MalformedURI e) {
            logger.severe("SCHEDULING: "+e.toString());
        //} catch(DirtyEntity e) {
        //    logger.severe("SCHEDULING: "+e.toString());
        } catch(Exception e) {
            logger.severe("SCHEDULING: "+e.toString());
        }*/
        return sb;
    }

	/*public void updateSB(SB sb) throws SchedulingException{
        try {
            XmlEntityStruct retrieved_sb = archOperationComp.retrieveDirty(sb.getId());
            //retrieved_sb = modifyRetrievedSB(retrieved_sb, sb);
            archOperationComp.update(retrieved_sb);
        } catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: "+e.toString());
        } catch(IllegalEntity e) {
            logger.severe("SCHEDULING: "+e.toString());
        } catch(NotFound e) {
            logger.severe("SCHEDULING: "+e.toString());
        } catch(MalformedURI e) {
            logger.severe("SCHEDULING: "+e.toString());
        }
        
    }*/

	// SchedulingPolicy
	public Policy[] getPolicy() throws SchedulingException{
        return null;
    }

    // PipelineProcessingRequest

    public void storePipelineProcessingRequest(SciPipelineRequest p) {
        /*
        try {
            ALMAPipelineProcessingRequest ppr = (ALMAPipelineProcessingRequest)p;
            XmlEntityStruct ppr_struct = ppr.getPipelineProcessingRequestStruct();
            //System.out.println(ppr_struct.xmlString);
            archOperationComp.store(ppr_struct);
        } catch(IllegalEntity e) {
            logger.severe("SCHEDULING: illegal entity error");
            e.printStackTrace(System.out);
        } catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: ArchiveInternalError");
            e.printStackTrace(System.out);
        }
        */
    }

    /**
     * TEMPORARY Function. Retrieves the PipelineProcessingRequest and
     * returns it as an XmlEntityStruct.
     * @param String PPR's ID
     * @return XmlEntityStruct The PPR's struct
     */
    public XmlEntityStruct retrievePPR(String ppr_id) {
        XmlEntityStruct ppr=null;
        try {
            ppr = archOperationComp.retrieveDirty(ppr_id);
        } catch (MalformedURI e) { 
            logger.severe("SCHEDULING: MalformedURI ");
            e.printStackTrace(System.out);
        } catch (ArchiveInternalError e) {
            logger.severe("SCHEDULING: ArchiveInternalError");
            e.printStackTrace(System.out);
        } catch (NotFound e) {
            logger.severe("SCHEDULING: Entity not found");
            e.printStackTrace(System.out);
        } //catch(EntityException e) {
            //logger.severe("SCHEDULING: error getting entity's ID");
          //  e.printStackTrace(System.out);
       // }
        return ppr;
    }

    ///////////////////////////////////////////////////////////////////////////
    private enum StateSystemDiagnostics {
    	LOGGING, BARFING
    }
    /**
    *
    */
   private void privateGetStateSystemComponent(StateSystemDiagnostics... diags) {
	   String tag = " ";
       try {
           logger.fine("SCHEDULING: Getting state system component");
           org.omg.CORBA.Object obj = containerServices.getDefaultComponent("IDL:alma/projectlifecycle/StateSystem:1.0");
           stateSystemComp = StateSystemHelper.narrow(obj);
           for (int i = diags.length-1; i >= 0; i--) {
        	   final StateSystemDiagnostics diag = diags[i];
        	   switch(diag) {
        	   case LOGGING:
            	   stateSystemComp = new LoggingStateSystem(containerServices, stateSystemComp);
        		   break;
        	   case BARFING:
            	   stateSystemComp = new BarfingStateSystem(containerServices, stateSystemComp);
        		   break;
        	   }
        	   tag = String.format(" %s%s", diag, tag);
           }
       } catch(AcsJContainerServicesEx e) {
           logger.severe("SCHEDULING: AcsJContainerServicesEx: "+e.toString());
           sendAlarm("Scheduling","SchedStateSystemConnAlarm",1,ACSFaultState.ACTIVE);
           stateSystemComp = null;
       }
       if (stateSystemComp != null) {
           logger.fine(String.format(
        		   "SCHEDULING: The ALMA%sState Engine has been constructed.",
        		   tag));
       } else {
           logger.warning(String.format(
        		   "SCHEDULING: The ALMA%sState Engine has NOT been constructed.",
        		   tag));
       }
   }
   
   /**
    *
    */
   private void getStateSystemComponent() {
	   privateGetStateSystemComponent(
			   StateSystemDiagnostics.LOGGING
//			   ,
//			   StateSystemDiagnostics.BARFING
			   );
   }

    /**
     *
     */
    private void getArchiveComponents() {
        try {
            logger.fine("SCHEDULING: Getting archive components");
            org.omg.CORBA.Object obj = containerServices.getDefaultComponent("IDL:alma/xmlstore/ArchiveConnection:1.0");
            this.archConnectionComp = alma.xmlstore.ArchiveConnectionHelper.narrow(obj);
            this.archOperationComp = archConnectionComp.getOperational("SCHEDULING");
            this.archIdentifierComp = alma.xmlstore.IdentifierHelper.narrow(
                    containerServices.getDefaultComponent(
                        "IDL:alma/xmlstore/Identifier:1.0"));
        } catch(AcsJContainerServicesEx e) {
            logger.severe("SCHEDULING: AcsJContainerServicesEx: "+e.toString());
            sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
            archConnectionComp =null;
            archOperationComp =null;
            archIdentifierComp = null;
        } catch (ArchiveException e) {
            logger.severe("SCHEDULING: Archive error: "+e.toString());
            sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
            archConnectionComp =null;
            archOperationComp =null;
            archIdentifierComp = null;
        } catch(UserDoesNotExistException e) {
            logger.severe("SCHEDULING: Archive error: "+e.toString());
            sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
            archConnectionComp =null;
            archOperationComp =null;
            archIdentifierComp = null;
        } catch (PermissionException e) {
            logger.severe("SCHEDULING: Archive error: "+e.toString());
            sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
            archConnectionComp =null;
            archOperationComp =null;
            archIdentifierComp = null;
        }
        entitySerializer = EntitySerializer.getEntitySerializer(
            containerServices.getLogger());
        entityDeserializer = EntityDeserializer.getEntityDeserializer(
            containerServices.getLogger());
        logger.fine("SCHEDULING: The ALMAArchive has been constructed.");
    }

    public void checkArchiveStillActive() {
        logger.fine("SCHEDULING: Checking archive connection.");
        try {
            if(archConnectionComp == null) {
                logger.finest("SCHEDULING: Getting archive components");
                getArchiveComponents();
            }else {
                logger.finest("SCHEDULING: Archive connection fine.");
            }
        }catch(NullPointerException npe){
            getArchiveComponents();
        } catch(Exception e){
            logger.severe("SCHEDULING: Error with archive components: "+e.toString());
            e.printStackTrace(System.out);
            sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
        }
        
    }

    public void checkStateSystemStillActive() {
        logger.fine("SCHEDULING: Checking state system connection.");
        try {
            if (stateSystemComp == null) {
                logger.finest("SCHEDULING: Getting state system components");
                getStateSystemComponent();
            } else {
                logger.finest("SCHEDULING: State system connection fine.");
            }
        } catch (NullPointerException npe) {
        	getStateSystemComponent();
        } catch (Exception e) {
            logger.severe("SCHEDULING: Error with state system components: "+e.toString());
            e.printStackTrace(System.out);
            sendAlarm("Scheduling","SchedStateSystemConnAlarm",1,ACSFaultState.ACTIVE);
        }
    }

    /**
      * Releases all the archive components.
      * TODO: find out how to release operational one..
      */
    public void releaseArchiveComponents() {
        try {
            //containerServices.releaseComponent("ARCHIVE_CONNECTION");
            containerServices.releaseComponent(archConnectionComp.name()); 
        } catch (Exception e) {
        }
    }

    /**
     * Given a ResultQuery from the archive, an SB is created and
     * returned
     *
     * @param res The ResultQuery from the archive
     * @return SB 
     */
    private SB convertToSB1(QueryResult res) throws Exception {
        String sb_id = res.identifier;
        SB sb = null;
        try {
            //XmlEntityStruct xml_struct = archOperationComp.updateRetrieve(sb_id);
            //XmlEntityStruct xml_struct = archOperationComp.retrieve(sb_id);
            XmlEntityStruct xml_struct = archOperationComp.retrieveDirty(sb_id);
            //sb = convertToSB2(xml_struct);
            //System.out.println(xml_struct.xmlString);
            //String proj_id = schedblock.getObsProjectRef().getEntityId();
            //if(proj_id == null) {
            //    System.out.println("dammit its null");
           // }
            //System.out.println ("project's id! = "+proj_id);
            //sb.setProject(getProject(proj_id));
        } catch (MalformedURI e) { 
        	e.printStackTrace(System.out);
            //throw new Exception (e);
        } catch (ArchiveInternalError e) {
        	e.printStackTrace(System.out);
            //throw new Exception (e);
        } catch (NotFound e) {
        	e.printStackTrace(System.out);
            //throw new Exception (e);
        }
        return sb;
    }

    /*
    private Project convertToProject1(QueryResult res)throws Exception {
        String proj_id = res.identifier;
        Project proj = null;
        try {
            XmlEntityStruct xml_struct = archOperationComp.retrieveDirty(proj_id);
            proj = convertToProject2(xml_struct);
            //System.out.println("Project: "+xml_struct.xmlString);
        } catch (MalformedURI e) { 
            logger.severe("SCHEDULING: "+e.toString());
            throw new Exception (e);
        } catch (ArchiveInternalError e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new Exception (e);
        } catch (NotFound e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new Exception (e);
        //} catch (DirtyEntity e) {
        //    throw new Exception (e);
        } catch(EntityException e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new Exception (e);
        }
        return proj;
        
    }
    
    /** 
     * Given an XmlEntityStruct, converts it into a Project Object.
     *
     * @param xml The XmlEntityStruct retrieved from the archive
     * @return Project
     *
    private Project convertToProject2(XmlEntityStruct xml) throws Exception {
        Project proj=null;
        //ALMAProject proj = null;
        //System.out.println(xml.xmlString);
        try {
            ObsProject obsProj= (ObsProject)
                entityDeserializer.deserializeEntity(xml, ObsProject.class);
                    //"alma.entity.xmlbinding.obsproject.ObsProject"));
          //  proj = new ALMAProject(obsProj); 
        } catch(EntityException e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new Exception (e);
        }
        return proj;
    }*/



    /**
     * Updates the SB with the information obtained from the Event received from
     * from Control.
     *
     * @param ControlEvent The event from control.
     * @throws SchedulingException
     **/
    public void updateSB(ControlEvent ce) throws SchedulingException{
        try {
            SB sb = getSB(ce.getSBId());
            ExecBlock eb = new ExecBlock(ce.getEBId(), ce.getArrayName());
            SchedBlock schedblock = getSchedBlock(ce.getSBId());
            ObsUnitControlT ouc = schedblock.getObsUnitControl();
            if(ouc == null) {
                ouc = new ObsUnitControlT();
            }
            switch(ce.getStatus()) {
                case 0://exec block status = processing
                    //ouc.setSchedStatus(ObsUnitControlTSchedStatusType.RUNNING);
                    //sb.setRunning();
                    break;
                case 1: //exec block status = ok
                    //ouc.setSchedStatus(ObsUnitControlTSchedStatusType.COMPLETED);
                    break;
                case 2://exec block status = failed
                    //ouc.setSchedStatus(ObsUnitControlTSchedStatusType.ABORTED);
                    break;
                case 3://exec block status = timeout
                    //ouc.setSchedStatus(ObsUnitControlTSchedStatusType.ABORTED);
                    break;
                default://exec block status kooky.. 
                    break;
            }
            XmlEntityStruct newsb = entitySerializer.serializeEntity(schedblock);
            archOperationComp.update(newsb);
        } catch(Exception e) {
            logger.severe("SCHEDULING: Could not update the SB after the "+
                "exec block event was received!");
            e.printStackTrace(System.out);
        }
    }

    public synchronized String getPPRString(ProjectStatusI ps, String pprId) {
    	barf();
        String result = "";
        try {
//            updateProjectStatus(ps);
            logger.fine("SCHEDULING: Just updated ps, now gonna query ppr");

            //TODO fix query here to include project status id
            String query = new String("/ps:ProjectStatus//ps:PipelineProcessingRequest[@entityPartId=\""+pprId+"\"]");
            String schema = new String("ProjectStatus");
            
            Cursor cursor = archOperationComp.queryDirty(query,schema);
            if(cursor == null) {
                throw new SchedulingException ("SCHEDULING: Error querying archive for PPR");
            }
            while(cursor.hasNext()){
                QueryResult res = cursor.next();
                //logger.info("PPR xml (in archive)= "+res.xml);
                result = res.xml;
                logger.fine("SCHEDULING: PPR string: "+result);
            }
            cursor.close();
            //query the archive for the pipelineprocessing requestthe pipelineprocessing request..
        } catch(Exception e) {
            logger.severe("SCHEDULING: Error getting the PPR String.");
            e.printStackTrace(System.out);
        }
        return result;
    }


    /**
      * Run a given query given. The UID of the results are returned.
      * @param query
      * @param schema
      * @return String[] UIDs of the results
      *
      */
    public String[] query(String query, String schema) throws SchedulingException { 
        Vector res_tmp= new Vector();
        String[] res = null;
        try {
            Cursor cursor = archOperationComp.queryDirty(query, schema);
            if(cursor == null) {
                throw new SchedulingException ("SCHEDULING: Error querying archive");
            }
            while(cursor.hasNext()){
                QueryResult result = cursor.next();
                res_tmp.add(result.identifier);
            }
            res = new String[res_tmp.size()];
            for(int i=0; i < res_tmp.size(); i++){
                res[i] = (String)res_tmp.elementAt(i);
            }
            cursor.close();
            //return res;
        } catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: "+e.toString());
            //throw new SchedulingException(e);
        } catch (SchedulingException e) {
        	logger.severe("SCHEDULING: Error querying archive");
        }
        return res;
    }

    public ObsProject retrieve(String uid) throws SchedulingException {
    	  ObsProject obsProj = null;
        try {
            XmlEntityStruct xml = archOperationComp.retrieveDirty(uid);
            obsProj= (ObsProject)
                entityDeserializer.deserializeEntity(xml, ObsProject.class);
            return obsProj;
        } catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: "+e.toString());
            //throw new SchedulingException(e);
        } catch(NotFound e) {
            logger.severe("SCHEDULING: "+e.toString());
            //throw new SchedulingException(e);
        } catch(MalformedURI e) {
            logger.severe("SCHEDULING: "+e.toString());
            //throw new SchedulingException(e);
        } catch(EntityException e){
            logger.severe("SCHEDULING: "+e.toString());
            //throw new SchedulingException(e);
        }
        return obsProj;
    }

//    protected void printProjectStatusFromObject(ProjectStatus ps){
//        try {
//            XmlEntityStruct xml = entitySerializer.serializeEntity(ps, ps.getProjectStatusEntity());
//            logger.fine("ProjectStatus XML from object: "+ xml.xmlString);
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    protected void printProjectStatusFromArchive(String id){
//        try {
//            XmlEntityStruct xml = archOperationComp.retrieve(id);
//            logger.fine("ProjectStatus XML from Archive: "+ xml.xmlString);
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
//    }
    
    /* Query to get project which has name = "manual mode" and pi = "manual mode"
     * If one is not found and exception is thrown
     * @return String The UID of the FIRST project
     */
    protected String queryForManualModeProject() throws SchedulingException {
        try {
            String query = "/prj:ObsProject[prj:pI=\"manual mode\" and "+
                           "prj:projectName=\"manual mode\"]";
            String schema = "ObsProject";
            Cursor cursor = archOperationComp.query(query, schema);
            if(cursor == null) {
                logger.severe("SCHEDULING: error querying manual mode project");
                throw new SchedulingException("No manual mode project found");
            }
            //will only do it once.
            while(cursor.hasNext()) {
                QueryResult res = cursor.next();
                try {
                    //id of manual mode project
                    return res.identifier;
                } catch(Exception e){
                    logger.severe("SCHEDULING: Could not return manual mode project id");
                    throw new SchedulingException(e);
                }
            }
        } catch(Exception e) {
            logger.severe("SCHEDULING: Could not return manual mode project id");
            //throw new SchedulingException (e);
        }
        return null;
    }
    
    public void addSchedulerStats(SchedulerStats s) { }
    public SchedulerStats[] getSchedulerStats(){ return null; }
    
    public void barf() {
    	String x = "";
    	try {
    		x.charAt(99);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

	/**
	 * @return the projectUtil
	 */
	public ProjectUtil getProjectUtil() {
		return projectUtil;
	}
	
	
	
    /*
     * ================================================================
     * New stuff for Lifecycle II FBT
     * ================================================================
     */
	/**
	 * Find the UIDs of the Statuses of the given type for which there
	 * are change records
	 * 
	 * @return Set<String> - a set of status UIDs
	 */
	private Set<String> getUIDsForChangedStatuses(final StateEntityType prj) {
		final Set<String> result = new HashSet<String>();

		final IDLArrayTime start = new IDLArrayTime(lastProjectStatusQuery);
		final IDLArrayTime end   = new IDLArrayTime(System.currentTimeMillis());

		try {
			StateChangeData[] stateChanges =
				stateSystemComp.findStateChangeRecords(
						start,
						end, "", "", "",
						prj.toString());
			logger.finer(String.format(
					"stateChanges(%s).length: %d (start = %d, end = %d)",
					prj.toString(), stateChanges.length, start.value, end.value));
			for (StateChangeData sc : stateChanges) {
				logger.finer("domainEntityId: " + sc.domainEntityId);
				logger.finer("domainEntityState: " + sc.domainEntityState);
				logger.finer("statusEntityId: " + sc.statusEntityId);
				result.add(sc.statusEntityId);
			}
		} catch (StateIOFailedEx e) {
			e.printStackTrace();
		}

		return result;
	}

	
	/**
     * Work out which SBs and ObsProjects can be observed based on
     * their statuses and the statuses of their containing projects.
     * Put the results into <code>runnableSBSs</code> and
     * <code>runnablePSs</code>.
     * 
     * Does this by:
     * <ol>
     *    <li>getting all the SBStatuses with a runnable state;</li>
     *    <li>getting all the ProjectStatuses with a runnable
     *        state;</li>
     *    <li>dropping all the otherwise runnable SBStatuses which
     *        are not part of a runnable project.</li>
     *    <li>dropping all the otherwise runnable ProjectStatuses which
     *        have no SBStatuses left in our collection.</li>
     * </ol>
     * 
     * @param opRunnableStates - we care about projects in any of these
     *                           states
     * @param sbRunnableStates - we care about sched blocks in any of
     *                           these states
     * 
     * @return a StatusEntityQueueBundle with all the statuses for
     *         ObsProjects, SchedBlocks and ObsUnitSets in which we're
     *         interested.
     * 
     * @throws SchedulingException
     */
    public StatusEntityQueueBundle determineRunnablesByStatus(
    		String[] opRunnableStates,
    		String[] sbRunnableStates) throws SchedulingException {
        logger.info("entering determineRunnablesByStatus");
        
        if (lastProjectStatusQuery < 0) {
            lastProjectStatusQuery = System.currentTimeMillis();
            logger.info("new lastProjectStatusQuery: " + (new Date(lastProjectStatusQuery)));
        }
        
        // Start with the ProjectStatuses and SBStatuses which are
        // in runnable states
    	final ProjectStatusQueue runnablePSs  = getProjectStatusesByState(opRunnableStates);
    	final SBStatusQueue      runnableSBSs = getSBStatusesByState(sbRunnableStates);
    	logger.info("# of projects retrieved from State Archive: " + runnablePSs.size());
    	
    	// Find SBStatuses which are not part of a runnable project
    	// and ProjectStatuses which have at least one SBStatus
    	// associated with them.
    	final Set<String> sbsIdsToRemove = new HashSet<String>();
    	final Set<String> psIdsToKeep    = new HashSet<String>();
    	for (final String sbsId : runnableSBSs.getAllIds()) {
    		final SBStatusI sbs  = runnableSBSs.get(sbsId);
    		final String    psId = sbs.getProjectStatusRef().getEntityId();
    		
    		if (runnablePSs.isExists(psId)) {
    			// The SBStatus is in a ProjectStatus we know about, so keep both
    			psIdsToKeep.add(psId);
    		} else {
    			// The SBStatus is not in a ProjectStatus we know about, so dump it
    			sbsIdsToRemove.add(sbsId);
    		}
    	}
    	
    	// Now remove the SBStatuses that we have just determined are not part
    	// of a runnable ProjectStatus.
    	for (final String sbsId : sbsIdsToRemove) {
    		runnableSBSs.remove(sbsId);
    	}
    	
    	// Also remove the ProjectStatuses that we have just determined do not
    	// any runnable SBStatuses. As we've remembered the ones to keep rather
    	// than to remove, then we need to do a bit of Set complementing first.
    	final Set<String> psIdsToRemove = new HashSet<String>(runnablePSs.getAllIds());
    	psIdsToRemove.removeAll(psIdsToKeep);
    	for (final String sbsId : sbsIdsToRemove) {
    		runnableSBSs.remove(sbsId);
    	}
    	
    	// Finally, get the OUSStatuses corresponding to the
    	// ProjectStatuses we're left with.
    	final OUSStatusQueue runnableOUSs = getOUSStatusesFor(runnablePSs);

    	logger.info("# of projects after checks: " + runnablePSs.size());
    	return new StatusEntityQueueBundle(runnablePSs, runnableOUSs, runnableSBSs);
    }

    
    private<E> Set<E> toSet(E[] array) {
        final Set<E> set = new HashSet<E>();
        for (final E state : array) {
        	set.add(state);
        }
        return set;
    }
    
    public StatusEntityQueueBundle determineRunnablesByStatusIncr(
            String[] opRunnableStatesArray,
            String[] sbRunnableStatesArray,
            StatusEntityQueueBundle zapQs) throws SchedulingException {
        logger.info("entering determineRunnablesByStatusIncr");
        
        Set<String> changedPSUids;
        
        if (lastProjectStatusQuery > 0) {
        	// Note the time just before the queries and use this as
        	// the time of the query. This means a slight overlap next
        	// time we do this, which in turn means that no updates
        	// made while the query results are being processed will be
        	// missed.
        	final long now = System.currentTimeMillis();
        	changedPSUids   = getUIDsForChangedStatuses(StateEntityType.PRJ);
            lastProjectStatusQuery = now;
        } else {
            lastProjectStatusQuery = System.currentTimeMillis();
            logger.info("new lastProjectStatusQuery: " + (new Date(lastProjectStatusQuery)));
            changedPSUids  = new HashSet<String>();
        }
        
        final ProjectStatusQueue runnablePSs    = new ProjectStatusQueue(logger);
        final ProjectStatusQueue unrunnablePSs  = new ProjectStatusQueue(logger);
        sortProjectStatuses(changedPSUids, runnablePSs, unrunnablePSs, opRunnableStatesArray);
        final SBStatusQueue runnableSBSs = getSBStatusesByProjectStatusIds(runnablePSs, sbRunnableStatesArray);

        // We could have just used zapQs.getProjectStatusQueue() rather
        // than bother with unrunnablePSs. Depends if we decide to do
        // any processing based upon it, I guess.
        zapQs.getProjectStatusQueue().updateWith(unrunnablePSs);
        getOUSandSBStatusesFor(zapQs);
        
        
        // Find SBStatuses which are not part of a runnable project
        // and ProjectStatuses which have at least one SBStatus
        // associated with them.
        final Set<String> sbsIdsToRemove = new HashSet<String>();
        final Set<String> psIdsToKeep    = new HashSet<String>();
        for (final String sbsId : runnableSBSs.getAllIds()) {
            final SBStatusI sbs  = runnableSBSs.get(sbsId);
            final String    psId = sbs.getProjectStatusRef().getEntityId();
            
            if (runnablePSs.isExists(psId)) {
                // The SBStatus is in a ProjectStatus we know about, so keep both
                psIdsToKeep.add(psId);
            } else {
                // The SBStatus is not in a ProjectStatus we know about, so dump it
                sbsIdsToRemove.add(sbsId);
            }
        }
        
        // Now remove the SBStatuses that we have just determined are not part
        // of a runnable ProjectStatus.
        for (final String sbsId : sbsIdsToRemove) {
            runnableSBSs.remove(sbsId);
        }
        
        // Also remove the ProjectStatuses that we have just determined do not
        // any runnable SBStatuses. As we've remembered the ones to keep rather
        // than to remove, then we need to do a bit of Set complementing first.
        final Set<String> psIdsToRemove = new HashSet<String>(runnablePSs.getAllIds());
        psIdsToRemove.removeAll(psIdsToKeep);
        for (final String sbsId : sbsIdsToRemove) {
            runnableSBSs.remove(sbsId);
        }
        
        // Finally, get the OUSStatuses corresponding to the
        // ProjectStatuses we're left with.
        final OUSStatusQueue runnableOUSs = getOUSStatusesFor(runnablePSs);
        return new StatusEntityQueueBundle(runnablePSs, runnableOUSs, runnableSBSs);        
    }
    
	/**
	 * Get all the OUSStatuses that are associated with the provided
	 * ProjectStatuses' ObsProjects' ObsUnitSets.
	 * 
	 * @param runnablePSs - the queue of ProjectStatuses to use as a
	 *                      starting point for the search for
	 *                      OUSStatuses.
	 * @return an StatusEntityQueue<SBStatusI, SBStatusRefT> containing
	 *         all the OSUStatus entities found
	 * 
	 * @throws SchedulingException
	 */
	public OUSStatusQueue getOUSStatusesFor(ProjectStatusQueue runnablePSs)
				throws SchedulingException {
        final OUSStatusQueue result = new OUSStatusQueue(logger);
        
        ProjectStatusI[] prjStatuses = runnablePSs.getAll();
        String[] ousStatusIds = new String[prjStatuses.length];
        for (int i = 0; i < prjStatuses.length; i++) {
            ousStatusIds[i] = prjStatuses[i].getObsProgramStatusRef().getEntityId();
        }
            XmlEntityStruct[] xmlOusStatuses = stateSystemComp.getOUSStatusList(ousStatusIds);
            for (XmlEntityStruct xmlOusStatus : xmlOusStatuses) {
                OUSStatus ous;
                try {
                    ous = (OUSStatus)entityDeserializer.deserializeEntity(xmlOusStatus, OUSStatus.class);
                } catch (EntityException ex) {
                    ex.printStackTrace();
                    continue;
                }
                OUSStatusI ousi = new CachedOUSStatus(ous);
                result.add(ousi);
            }
        
//        for (final ProjectStatusI ps : runnablePSs.getAll()) {
//    		try {
//    			addAllOUSStatuses(ps.getObsProgramStatus(), result);
//    		} catch (SchedulingException e) {
//    			logger.warning(String.format(
//    					"Cannot get OUSStatuses for ProjectStatus %s (for ObsProject %s) - %s",
//    					ps.getProjectStatusEntity().getEntityId(),
//    					ps.getObsProjectRef().getEntityId(),
//    					e.getLocalizedMessage()));
//    		}
//        }
		
		return result;
	}
    
	/**
	 * Get all the OUSStatuses that are associated with the provided
	 * ProjectStatuses' ObsProjects' ObsUnitSets.
	 * 
	 * @param runnablePSs - the queue of ProjectStatuses to use as a
	 *                      starting point for the search for
	 *                      OUSStatuses.
	 * @return an StatusEntityQueue<SBStatusI, SBStatusRefT> containing
	 *         all the OSUStatus entities found
	 * 
	 * @throws SchedulingException
	 */
	public void getOUSandSBStatusesFor(StatusEntityQueueBundle bundle)
				throws SchedulingException {
		
		for (final ProjectStatusI ps : bundle.getProjectStatusQueue().getAll()) {
			try {
				final XmlEntityStruct[] xml
					= stateSystemComp.getProjectStatusList(
						ps.getProjectStatusEntity().getEntityId());
				for (XmlEntityStruct xes : xml) {
					try {
						if (xes.entityTypeName.equals("OUSStatus")) {
							final OUSStatus ouss = (OUSStatus) entityDeserializer.
							deserializeEntity(xes, OUSStatus.class);
							bundle.getOUSStatusQueue().add(new CachedOUSStatus(ouss));
						} else if (xes.entityTypeName.equals("SBStatus")) {
							final SBStatus sbs = (SBStatus) entityDeserializer.
							deserializeEntity(xes, SBStatus.class);
							bundle.getSBStatusQueue().add(new CachedSBStatus(sbs));
						} else if (!xes.entityTypeName.equals("ProjectStatus")) {
							logger.warning(String.format(
									"Unrecognised entity type for entity %s from State System - type is %s",
									xes.entityId,
									xes.entityTypeName));
						}
					} catch (Exception e) {
						logger.warning(String.format(
								"Can not deserialise %s %s from State System - %s",
								xes.entityTypeName,
								xes.entityId,
								e.getMessage()));
					}
				}
			} catch (Exception e) {
				logger.warning(String.format(
						"Can not pull child statuses for ProjectStatus %s from State System - %s",
						ps.getProjectStatusEntity().getEntityId(),
						e.getMessage()));
			}
		}
        
//        for (final ProjectStatusI ps : runnablePSs.getAll()) {
//    		try {
//    			addAllOUSStatuses(ps.getObsProgramStatus(), result);
//    		} catch (SchedulingException e) {
//    			logger.warning(String.format(
//    					"Cannot get OUSStatuses for ProjectStatus %s (for ObsProject %s) - %s",
//    					ps.getProjectStatusEntity().getEntityId(),
//    					ps.getObsProjectRef().getEntityId(),
//    					e.getLocalizedMessage()));
//    		}
//        }
	}
	
//	private void addAllOUSStatuses(OUSStatusI     ouss,
//			                       OUSStatusQueue result)
//							throws SchedulingException {
//		result.add(ouss);
//			for (final OUSStatusI child : ouss.getOUSStatus()) {
//				addAllOUSStatuses(child, result);
//			}
//	}
	
	public void setSBState(String sbsId, StatusTStateType status) {
		try {
			stateSystemComp.changeSBStatus(sbsId,
					                       status.toString(),
					                       Subsystem.SCHEDULING,
					                       Role.AOD);
		} catch (PreconditionFailedEx e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchEntityEx e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentEx e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchTransitionEx e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PostconditionFailedEx e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotAuthorizedEx e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		updateProjectStatus(ps);
	}
    /* end of New stuff for Lifecycle II FBT
     * ============================================================= */
	
	
	
    /*
     * ================================================================
     * Initialisation of Status Entities
     * ================================================================
     */
    /**
     * Make sure that the given ProjectStatus and OUSStatuses are all
     * initialised.
     * 
     * @param ps - the status entity to initialise
     * @param breadcrumbs - the trail through the OUSStatus hierarchy
     *                      (with lowest level of hierarchy at the
     *                      start of the list).
     * @throws SchedulingException 
     */
    public void ensureStatusIsInitialised(ProjectStatusI ps,
    		                              List<OUSStatusI> breadcrumbs)
    						throws SchedulingException {
    	if (!ps.isSynched()) {
    		throw new SchedulingException(
    		"ProjectStatus is not synched with State Archive");
    	}

    	final String projectID = ps.getDomainEntityId();
   		final ObsProject op = getObsProject(projectID);

    	if (ps.getPI() == null || ps.getPI().equals("")) {
    		// The OT enforces a PI name in the ObsProject, so a
    		// missing one here means that the ProjectStatus has not
    		// been initialised.
    		
    		logger.info(String.format(
    				"Initialising ProjectStatus %s from ObsProject %s",
    				ps.getUID(), projectID));

    		ps.setName(op.getProjectName());
    		ps.setPI(op.getPI());
    		ps.setBreakpointTime(null);
    		ps.setTimeOfUpdate(DateTime.currentSystemTime().toString());
    	}
    	
    	ObsUnitSetT ous = null;
    	
		// Work backwards through the trail of breadcrumbs, starting at
    	// the highest level of Program hierarchy.
    	for (int i = breadcrumbs.size()-1; i >= 0; i--) {
    		
    		// Find the OUSStatus to initialise, and check that we can.
    		final OUSStatusI ouss = breadcrumbs.get(i);
    		final String     partID = ouss.getDomainPartId();

        	if (!ouss.isSynched()) {
        		throw new SchedulingException(
        		"OUSStatus is not synched with State Archive");
        	}

        	// Find the ObsUnitSetT for which ouss is the status. Both 
        	// variants of findProgram() throw an exception if the
        	// specified ObsUnitSetT is not found, so ous will never be
        	// null afterwards.
    		if (ous == null) {
    			ous = findProgram(op, partID);
    		} else {
    			ous = findProgram(ous, partID);
    		}
    		
        	if (!ouss.hasTotalUsedTimeInSec()) {
        		// Only Scheduling is supposed to write that field so
        		// we use its absence to mean we need to initialise.
        		
        		logger.info(String.format(
        				"Initialising OUSStatus %s from ObsUnitSet %s",
        				ouss.getUID(), partID));

        		final ObsUnitSetTChoice choice = ous.getObsUnitSetTChoice();
        		final ObsUnitControlT ouc = ous.getObsUnitControl();
        		
        		if (ouc != null) {
        			// There isn't an ObsUnitControl for the ObsPlan,
        			// so guard against nulls.
            		final int secs = (int)projectUtil.
            					convertToSeconds(ouc.getMaximumTime());
            		ouss.setTotalRequiredTimeInSec(secs);
        		}
        		
        		ouss.setTotalUsedTimeInSec(0);
        		ouss.setTimeOfUpdate(DateTime.currentSystemTime().toString());

        		ouss.setNumberSBsFailed(0);
        		ouss.setNumberSBsCompleted(0);
        		ouss.setNumberObsUnitSetsFailed(0);
        		ouss.setNumberObsUnitSetsCompleted(0);
        		ouss.setTotalObsUnitSets(choice.getObsUnitSetCount());
        		ouss.setTotalSBs(choice.getSchedBlockRefCount());
        	}
    	}
    }

    private ObsUnitSetT findProgram(ObsProject proj, String partID)
    						throws SchedulingException {
    	logger.fine(String.format(
    			"Looking for ObsUnitSet %s in ObsProject %s",
    			partID, proj.getObsProjectEntity().getEntityId()));
    	
    	ObsUnitSetT plan = proj.getObsProgram().getObsPlan();

    	if (plan.getEntityPartId().equals(partID)) {
    		logger.fine("\t\tGot it!");
    		return plan;
    	}
		throw new SchedulingException(String.format(
        		"Cannot find ObsUnitSet %s in ObsProject %s",
    			partID, proj.getObsProjectEntity().getEntityId()));
	}

    private ObsUnitSetT findProgram(ObsUnitSetT prog, String partID)
    						throws SchedulingException {
    	logger.fine(String.format(
    			"Looking for ObsUnitSet %s in ObsUnitSet %s",
    			partID, prog.getEntityPartId()));
    	
		for (ObsUnitSetT child : prog.getObsUnitSetTChoice().getObsUnitSet()) {
	    	logger.fine(String.format(
	    			"\tChild id is %s",
	    			child.getEntityPartId()));
			if (child.getEntityPartId().equals(partID)) {
		    	logger.fine("\t\tGot it!");
				return child;
			}
		}
		throw new SchedulingException(String.format(
        		"Cannot find ObsUnitSet %s in ObsUnitSet %s",
        		partID, prog.getEntityPartId()));
	}

	/**
     * Make sure that the given SBStatus is initialised.
     * 
     * @param sbs - the status entity to initialise
     * @throws SchedulingException 
     */
    public void ensureStatusIsInitialised(SBStatusI sbs) throws SchedulingException {
    	if (!sbs.isSynched()) {
    		throw new SchedulingException(
			"SBStatus is not synched with State Archive");
    	}
    	
    	if (!sbs.hasTotalUsedTimeInSec()) {
    		// Only Scheduling is supposed to write that field so
    		// we use its absence to mean we need to initialise.

    		final String             domainID = sbs.getDomainEntityId();
    		final SchedBlock         sb = getSchedBlock(domainID);
    		final SchedBlockControlT sbc = sb.getSchedBlockControl();
    		final ObsUnitControlT    ouc = sb.getObsUnitControl();
    		
    		logger.info(String.format(
    				"Initialising SBStatus %s from SchedBlock %s",
    				sbs.getUID(), domainID));

    		if (sbc.getIndefiniteRepeat()) {
        		sbs.setExecutionsRemaining(0);
    		} else {
        		sbs.setExecutionsRemaining(sbc.getExecutionCount());
    		}
    		sbs.setTotalRequiredTimeInSec(
    				(int)projectUtil.convertToSeconds(ouc.getMaximumTime()));
    		sbs.setTotalUsedTimeInSec(0);
    		sbs.setTimeOfUpdate(DateTime.currentSystemTime().toString());
    	}
	}
	/* End of Initialisation of Status Entities
	 * ============================================================= */
    
    public void setProjectUtilStatusQueue(StatusEntityQueueBundle queue) {
        projectUtil.setStatusQueue(queue);
    }    
}

