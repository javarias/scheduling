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

import java.util.logging.Logger;
import java.util.Properties;
import java.util.Vector;
import java.io.StringReader;
import java.sql.Timestamp;
import alma.scheduling.Define.*;



import alma.xmlentity.XmlEntityStruct;


import alma.acs.container.ContainerServices;
import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.entityutil.EntityDeserializer;
import alma.acs.entityutil.EntitySerializer;
import alma.acs.entityutil.EntityException;

import alma.alarmsystem.source.ACSAlarmSystemInterfaceFactory;
import alma.alarmsystem.source.ACSAlarmSystemInterface;
import alma.alarmsystem.source.ACSFaultState;
import cern.cmw.mom.pubsub.impl.ACSJMSTopicConnectionImpl;

import alma.xmlstore.Operational;
import alma.xmlstore.Identifier;
import alma.xmlstore.ArchiveConnection;
import alma.xmlstore.ArchiveConnectionPackage.*;
import alma.xmlstore.OperationalPackage.*;
import alma.xmlstore.ArchiveInternalError;
import alma.xmlstore.Cursor;
import alma.xmlstore.CursorPackage.QueryResult;

import alma.entity.xmlbinding.schedblock.*;
import alma.entity.xmlbinding.projectstatus.*;
import alma.entity.xmlbinding.obsproject.*;
import alma.entity.xmlbinding.obsproject.types.*;            
import alma.entity.xmlbinding.specialsb.*;
import alma.entities.commonentity.*;

import alma.hla.runtime.DatamodelInstanceChecker;

/**
 * This class provides all the functionalitiy from the archvie which 
 * is specific to the Scheduling Subsystem. It implements the Archive 
 * interface from the scheduling's define package and it connects via
 * the container services to the real archive used by all of alma.
 *
 * @version $Id: ALMAArchive.java,v 1.77 2007/08/02 15:14:42 sslucero Exp $
 * @author Sohaila Lucero
 */
public class ALMAArchive implements Archive {
    //The container services
    private ContainerServices containerServices;
    // The archive's components
    private ArchiveConnection archConnectionComp;
    private Identifier archIdentifierComp;
    private Operational archOperationComp;
    //TODO should check project queue.. if project exists don't map a new one.
    //The logger
    private Logger logger;
    //Entity deserializer - makes entities from the archive human readable
    private EntityDeserializer entityDeserializer;
    //Entity Serializer - prepares entites for the archive
    private EntitySerializer entitySerializer;
    //The DateTime of the last query for SBs
    private DateTime lastSpecialSBQuery;
    private DateTime lastSBQuery;
    private DateTime lastProjectQuery;
    private DateTime lastProjectStatusQuery;
    
    //ALMA Clock
    private ALMAClock clock;

    private String schemaVersion="";
    private DatamodelInstanceChecker dic=null;

    /**
      *
      */
    public ALMAArchive(ContainerServices cs, ALMAClock c){
        this.containerServices = cs;
        this.logger = cs.getLogger();
        this.clock = c;
        ACSJMSTopicConnectionImpl.containerServices=containerServices;
        getArchiveComponents();
        getSchemaVersion();
        getDatamodelInstanceChecker();
    }

    protected void getSchemaVersion() {
        ObsProjectEntityT p = new ObsProjectEntityT();
        schemaVersion = p.getDatamodelVersion(); 
    }
    protected void getDatamodelInstanceChecker(){
        dic = new DatamodelInstanceChecker();
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
        try {
            String[] s = archIdentifierComp.getUIDs((short)1);
            return s[0];
        }catch(Exception e) {
            throw new SchedulingException (e);
        }
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
            throw new SchedulingException(e);
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
        try {
            Project[] projects = null;
            Vector tmp_projects = new Vector();
        
            ObsProject[] obsProj = getAllObsProjects();
            //SchedBlock[] sbs1 = 
            logger.fine("obsproject length = "+obsProj.length);
            for(int i=0; i < obsProj.length; i++) {
                ProjectStatus ps = getProjectStatusForObsProject(obsProj[i]);
                if(ps == null) { //no project status for this project. so create one
                     logger.warning("SCHEDULING: no ps for this project");
                } else {
            //TODO should check project queue.. if project exists don't map a new one.
                    SchedBlock[] sbs = getSBsFromObsProject(obsProj[i]);
                    Project p = ProjectUtil.map(obsProj[i], sbs, ps, 
                            new DateTime(System.currentTimeMillis()));
                    tmp_projects.add(p);
                }
            }
            projects = new Project[tmp_projects.size()];
            for(int i=0; i < tmp_projects.size();i++) {
                projects[i] = (Project)tmp_projects.elementAt(i);
            }
	        logger.fine("SCHEDULING: Scheduling converted "+projects.length+
                    " Projects from ObsProject found archived.");
            return projects;
        } catch (Exception e) {
            sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
            try {
            	Thread.sleep(1000);
            } catch (InterruptedException e1) {
            	e1.printStackTrace(System.out);
            }
            throw new SchedulingException (e);
        }
    }

    public Project checkProjectForUpdates(String id) throws SchedulingException {
        return null;
    }

    public synchronized ProjectStatus getProjectStatus(Project p) throws SchedulingException {
        ProjectStatus ps = null;
        String ps_id = p.getProjectStatusId();
        //logger.info("Getting project status from archive, id = "+ ps_id);
        try {
            XmlEntityStruct xml = archOperationComp.retrieveDirty(ps_id);
            //logger.info("Getting PS for Project");
            //logger.info("PS xml: "+xml.xmlString);
            ps = (ProjectStatus)entityDeserializer.deserializeEntity(xml, ProjectStatus.class); 
            //check time of update, if one exists DON'T do new mapping!
            String timeOfUpdate = ps.getTimeOfUpdate();
	    	if (timeOfUpdate == null || timeOfUpdate.length() == 0) {
                //Same as calling 'ProjectStatus ProjectUtil.map'
                ps = ProjectUtil.updateProjectStatus(p);
                updateProjectStatus(ps);
		    }
        } catch(Exception e) {
            e.printStackTrace(System.out);
            sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
            //send to alarm system take some time, throw any Exception immediately will result in 
            //send Alarm to alarm system failure. so we do a delay for one second to wait alarm is send.
            try {
            	Thread.sleep(1000);
            } catch (InterruptedException e1) {
            	e1.printStackTrace(System.out);
            }
            throw new SchedulingException(e);
        }
        return ps;
    }

    public synchronized ProjectStatus getProjectStatusForObsProject(ObsProject p) throws SchedulingException {
        ProjectStatus ps = null;
        try {
            String ps_id = p.getProjectStatusRef().getEntityId();
	    //logger.info("Retrieving project status "+ps_id);
            XmlEntityStruct xml = archOperationComp.retrieveDirty(ps_id);
            if(!xml.entityTypeName.equals("ProjectStatus")){
                logger.warning("SCHEDULING: Retrieved a "+xml.entityTypeName+" when we wanted a ProjectStatus");
                logger.warning("SCHEDULING: uid was "+ps_id+" and xml was:");
                logger.warning(xml.xmlString);
            } //else {
                //logger.fine(xml.xmlString);
            //}
            ps = (ProjectStatus)entityDeserializer.deserializeEntity(xml, ProjectStatus.class); 
        } catch(NullPointerException npe){
            logger.warning("SCHEDULING: Project had no Project Status. Creating one.");
            ps = createDummyProjectStatus(p);
        } catch(Exception e) {
            e.printStackTrace(System.out);
            sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
            try {
            	Thread.sleep(1000);
            } catch (InterruptedException e1) {
            	e1.printStackTrace(System.out);
            }
            throw new SchedulingException(e);
        }
        return ps;
    }

    /**
      * Querys the ProjectStatus for the project with the given proj_id.
      */
    public ProjectStatus queryProjectStatus(String proj_id) throws SchedulingException {
        ProjectStatus ps = null;
        String query = new String("/ps:ProjectStatus/ps:ObsProjectRef[@entityId='"+proj_id+"']");
        String schema = new String("ProjectStatus");
        try {
            Cursor cursor = archOperationComp.queryDirty(query,schema);
            if(cursor == null){
            } 
            boolean one = true;
            while(cursor.hasNext()) { //should be only one.
                if(one) {
                    QueryResult res = cursor.next();
                    XmlEntityStruct xml = archOperationComp.retrieveDirty(res.identifier);
                    //System.out.println("PROJECT STATUS: "+xml.xmlString);
                    ps = (ProjectStatus)entityDeserializer.deserializeEntity(xml, ProjectStatus.class);
                } else {
                    throw new SchedulingException("More than one PS for project.");
                }
                one = false;
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
            try {
            	Thread.sleep(1000);
            } catch (InterruptedException e1) {
            	e1.printStackTrace(System.out);
            }
            throw new SchedulingException(e);
        }
        return ps;
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
    private ObsProject[] getAllObsProjects() throws SchedulingException {
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
                    for(int i=0; i <  newArchUpdates.length; i++){
                        //get status for this entitiy and see if it matches the schema we like
                        //logger.fine("SCHEDULING: getting status object for "+newArchUpdates[i]);
                        //status = archOperationComp.status(newArchUpdates[i]);
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
                        logger.fine("SCHEDULING: About to retrieve project with uid "+res.identifier+", gotten from Query");
                        xml = archOperationComp.retrieveDirty(res.identifier);
                        //System.out.println("SchemaVersion in xmlEntityStruct: "+xml.schemaVersion);
                        //logger.info("PROJECT : "+ xml.xmlString);
                        //System.out.println("PROJECT taken out of archive: "+ xml.xmlString);
                        //if( matchSchemaVersion(xml.xmlString) ){ } else {
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
                        throw new SchedulingException (e);
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
            throw new SchedulingException(e);
        }
        return projects;

    }
    
    /**
      * Creates a 'dummy' ProjectStatus and stores it in the archive.
      */
    private ProjectStatus createDummyProjectStatus(ObsProject p) throws SchedulingException {
        logger.finest("SCHEDULING: creating a project status for a project");
        //project doesn't have a ProjectStatus, so create one for it now and archive it
        String proj_id = p.getObsProjectEntity().getEntityId();
        ProjectStatus newPS = new ProjectStatus();
        ProjectStatusEntityT ps_entity = new ProjectStatusEntityT();
        newPS.setProjectStatusEntity(ps_entity);

        // Create the entity reference object for the ObsProject
        ObsProjectRefT obsProjRef = new ObsProjectRefT();
        obsProjRef.setEntityId(proj_id);
        newPS.setObsProjectRef(obsProjRef);

        logger.finest("ps-sched: creating ps for project "+ p.getObsProjectEntity().getEntityId() );

        try {
            containerServices.assignUniqueEntityId(newPS.getProjectStatusEntity());
        } catch(AcsJContainerServicesEx ce) {
            throw new SchedulingException(
                    "SCHEDULING: error assinging UID to new ProjectStatus entity");
        }
        try {
            XmlEntityStruct ps_xml = entitySerializer.serializeEntity(
                newPS, newPS.getProjectStatusEntity());
            //System.out.println("dummy PROJECT STATUS: "+ps_xml.xmlString);
            archOperationComp.store(ps_xml);
        } catch(EntityException ee) {
            throw new SchedulingException(
                    "SCHEDULING: error serializing ProjectStatus entity.");
        } catch(IllegalEntity e) {
            logger.severe("SCHEDULING: illegal entity error");
            e.printStackTrace(System.out);
        } catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: ArchiveInternalError");
            e.printStackTrace(System.out);
        }
        return newPS;
    }

    /**
      *
      */
    private SchedBlock[] getSBsFromObsProject(ObsProject p) throws SchedulingException {
        if(p.getObsProgram().getObsPlan().getObsUnitSetTChoice() == null) {
            logger.severe("SCHEDULING: no sbs stuff available in project");
            throw new SchedulingException("No SB info in ObsProject");
        } else {
            SchedBlockRefT[] sbs_refs = getSBRefs(p.getObsProgram().getObsPlan().getObsUnitSetTChoice());
            SchedBlock[] sbs = new SchedBlock[sbs_refs.length];
            for(int i=0; i < sbs_refs.length; i++){
                //get the sb
                sbs[i] = getSchedBlock(sbs_refs[i].getEntityId());
                
            }
            /*
            logger.info("SCHEDULING: Scheduling found that project "+
                    p.getObsProjectEntity().getEntityId() +" has "+
                    sbs.length+" sbs");
                    */
            return sbs;
        }

    }

    /**
      *
      */
    private ObsProject getObsProject(String id) throws SchedulingException {
        ObsProject proj;
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
            throw new SchedulingException (e);
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
            throw new SchedulingException("ObsUnitSetTChoice == null");
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
            logger.fine("SCHEDULING: About to retrieve SB with uid "+id);
            XmlEntityStruct xml = archOperationComp.retrieveDirty(id);
            //System.out.println("SCHEDBLOCK: "+ xml.xmlString);
            sb = (SchedBlock) entityDeserializer.deserializeEntity(xml, SchedBlock.class);
                    //"alma.entity.xmlbinding.schedblock.SchedBlock"));
            
        }catch(Exception e){
            throw new SchedulingException (e);
        }
        return sb;
    }



    /**
      *
      */
    public synchronized void updateProjectStatus(ProjectStatus ps) throws SchedulingException {
        try {
            XmlEntityStruct xml = entitySerializer.serializeEntity(ps, ps.getProjectStatusEntity());
            //logger.finest("SCHEDULING: updated PS: "+xml.xmlString);
            //logger.finest("SCHEDULING: About to retrieve Project Status with uid "+ps.getProjectStatusEntity().getEntityId());
            XmlEntityStruct xml2 = archOperationComp.retrieveDirty(ps.getProjectStatusEntity().getEntityId());
            xml2.xmlString = xml.xmlString;
            //logger.finest("About to save PS: "+xml2.xmlString);
            archOperationComp.update(xml2);
        } catch(Exception e){
            logger.severe("SCHEDULING: error updating PS in archive, "+e.toString());
            e.printStackTrace(System.out);
            throw new SchedulingException (e);
        }
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
            project = ProjectUtil.map(obsProj, getSBsFromObsProject(obsProj), 
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
    public ProjectStatus[] queryRecentProjectStatus() throws SchedulingException {
        String schema = new String("ProjectStatus");
        String query = new String("/ps:ProjectStatus");
        ProjectStatus[] ps=new ProjectStatus[0];
        XmlEntityStruct xml;
        try {
            if (lastProjectStatusQuery != null){
                String[] ids = archOperationComp.queryRecent(schema, lastProjectStatusQuery.toString()+".000");
                if(ids.length > 0){
                    ps = new ProjectStatus[ids.length];
                    for(int i=0; i < ids.length; i++){
                        xml = archOperationComp.retrieve(ids[i]);
                        ps[i] = (ProjectStatus)entityDeserializer.deserializeEntity(xml, ProjectStatus.class);
                    }
                }
            } else {
                Cursor cursor = archOperationComp.query(query, schema);
                Vector tmp = new Vector();
                while(cursor.hasNext()){
                    QueryResult res = cursor.next();
                    xml = archOperationComp.retrieve(res.identifier);
                    tmp.add((SchedBlock)entityDeserializer.deserializeEntity(xml, ProjectStatus.class));
                }
                ps = new ProjectStatus[tmp.size()];
                ps = (ProjectStatus[])tmp.toArray(ps);
            }
            lastProjectStatusQuery = clock.getDateTime();
            return ps;
        }catch(NotFound e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new SchedulingException (e);
        }catch(MalformedURI e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new SchedulingException (e);
        }catch(EntityException e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new SchedulingException (e);
        }catch(DirtyEntity e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new SchedulingException (e);
        }catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new SchedulingException (e);
        }
    }

	// SB
    public SchedBlock[] queryRecentSBs() throws SchedulingException {
        String schema = new String("SchedBlock");
        String query = new String("/sbl:SchedBlock");
        SchedBlock[] sbs=new SchedBlock[0];
        XmlEntityStruct xml;
        try {
            if (lastSBQuery != null){
                String[] ids = archOperationComp.queryRecent(schema, lastSBQuery.toString()+".000");
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
            return sbs;
        }catch(NotFound e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new SchedulingException (e);
        }catch(MalformedURI e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new SchedulingException (e);
        }catch(EntityException e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new SchedulingException (e);
        }catch(DirtyEntity e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new SchedulingException (e);
        }catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new SchedulingException (e);
        }
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
            }
            cursor.close();
        }catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new SchedulingException (e);
        } catch(SchedulingException se) {
            logger.severe("SCHEDULING: "+se.toString());
            throw new SchedulingException (se);
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

	public void updateSB(SB sb) throws SchedulingException{
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
        
    }

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
    
    /**
     *
     */
    private void getArchiveComponents() {
        try {
            logger.fine("SCHEDULING: Getting archive components");
            org.omg.CORBA.Object obj = containerServices.getDefaultComponent("IDL:alma/xmlstore/ArchiveConnection:1.0");
            this.archConnectionComp = alma.xmlstore.ArchiveConnectionHelper.narrow(obj);
            
            this.archConnectionComp.getAdministrative("SCHEDULING").init();
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
        } catch(ArchiveInternalError e) {
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
            throw new Exception (e);
        } catch (ArchiveInternalError e) {
            throw new Exception (e);
        } catch (NotFound e) {
            throw new Exception (e);
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
            //sb.execEnd(eb, ce.getStartTime(), 5);
            //sb.getStatus().setEnded(ce.getStartTime(), 5);
            //SchedBlock schedblock = getSBfromArchive(ce.getSBId());
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

    public synchronized String getPPRString(ProjectStatus ps, String pprId) {
        String result = "";
        try {
            updateProjectStatus(ps);
            logger.fine("SCHEDULING: Just updated ps, now gonna query ppr");

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
        try {
            Cursor cursor = archOperationComp.queryDirty(query, schema);
            if(cursor == null) {
                throw new SchedulingException ("SCHEDULING: Error querying archive");
            }
            while(cursor.hasNext()){
                QueryResult res = cursor.next();
                //logger.info(res.xml);
                //logger.info(res.identifier);
                res_tmp.add(res.identifier);
            }
            String[] res = new String[res_tmp.size()];
            for(int i=0; i < res_tmp.size(); i++){
                res[i] = (String)res_tmp.elementAt(i);
            }
            cursor.close();
            return res;
        } catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new SchedulingException(e);
        }
    }

    public ObsProject retrieve(String uid) throws SchedulingException {
        try {
            XmlEntityStruct xml = archOperationComp.retrieveDirty(uid);
            ObsProject obsProj= (ObsProject)
                entityDeserializer.deserializeEntity(xml, ObsProject.class);
            return obsProj;
        } catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new SchedulingException(e);
        } catch(NotFound e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new SchedulingException(e);
        } catch(MalformedURI e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new SchedulingException(e);
        } catch(EntityException e){
            logger.severe("SCHEDULING: "+e.toString());
            throw new SchedulingException(e);
        }
    }
    
}
