/*
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
import java.util.Vector;

import alma.scheduling.Define.*;
import alma.scheduling.ProjectStatusUtil;

import alma.xmlentity.XmlEntityStruct;

import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;
import alma.acs.entityutil.EntityDeserializer;
import alma.acs.entityutil.EntitySerializer;
import alma.acs.entityutil.EntityException;

import alma.xmlstore.Operational;
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
import alma.entities.generalincludes.*;
import alma.entities.commonentity.*;


/**
 * This class provides all the functionalitiy from the archvie which 
 * is specific to the Scheduling Subsystem. It implements the Archive 
 * interface from the scheduling's define package and it connects via
 * the container services to the real archive used by all of alma.
 *
 * @version 1.0
 * @author Sohaila Lucero
 */
public class ALMAArchive implements Archive {
    //The container services
    private ContainerServices containerServices;
    // The archive's components
    private ArchiveConnection archConnectionComp;
    private Operational archOperationComp;
    //The logger
    private Logger logger;
    //Entity deserializer - makes entities from the archive human readable
    private EntityDeserializer entityDeserializer;
    //Entity Serializer - prepares entites for the archive
    private EntitySerializer entitySerializer;
    //The DateTime of the last query for SBs
    private DateTime lastSBquery;
    

    public ALMAArchive(ContainerServices cs){
        this.containerServices = cs;
        this.logger = cs.getLogger();
        getArchiveComponents();
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
        Vector tmp_projects = new Vector();
        
        ObsProject[] obsProj = getAllObsProjects();
        for(int i=0; i < obsProj.length; i++) {
            ProjectStatus ps = getProjectStatusForObsProject(obsProj[i]);
            if(ps == null) { //no project status for this project. so create one
                 System.out.println("no ps for this project");
            }
            SchedBlock[] sbs = getSBsFromObsProject(obsProj[i]);
            tmp_projects.add(ProjectUtil.map(obsProj[i], sbs, ps, 
                        new DateTime(System.currentTimeMillis())));
        }
        projects = new Project[tmp_projects.size()];
        for(int i=0; i < tmp_projects.size();i++) {
            projects[i] = (Project)tmp_projects.elementAt(i);
        }
        return projects;
        /*
        String query = new String("/prj:ObsProject");
        String schema = new String("ObsProject");
        String className = new String(
            "alma.entity.xmlbinding.obsproject.ObsProject");
        try {
            Cursor cursor = archOperationComp.queryDirty(query,schema);
            if(cursor == null) {
                logger.severe("SCHEDULING: cursor was null when querying ObsProjects");
                return null;
            } else {
                logger.info("SCHEDULING: cursor not null!");
            }
            while(cursor.hasNext()) {
                //logger.info("SCHED: NEXT!");
                QueryResult res = cursor.next();
                try {
                    //logger.info("SCHED: convert to proj");
                    tmp_projects.add(convertToProject1(res));
                }catch(Exception e) {
                    logger.severe("SCHEDULING: "+e.toString());
                    e.printStackTrace();
                    throw new SchedulingException (e);
                }
            }
            projects = new Project[tmp_projects.size()];
            for(int i=0; i < tmp_projects.size();i++) {
                projects[i] = (Project)tmp_projects.elementAt(i);
            }
        return projects;
            //logger.info("SCHEDULING: Projects available = "+tmp_projects.size());
        } catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new SchedulingException(e);
        }*/
    }

    private ProjectStatus getProjectStatusForObsProject(ObsProject p) throws SchedulingException {
        ProjectStatus ps = null;
        String query = new String("/ps:ProjectStatus/ps:ObsProjectRef[@entityId='"+
                p.getObsProjectEntity().getEntityId()+"']");
        String schema = new String("ProjectStatus");
        String className = new String(
                "alma.entity.xmlbinding.projectstatus.ProjectStatus");
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
                    ps = (alma.entity.xmlbinding.projectstatus.ProjectStatus)
                        entityDeserializer.deserializeEntity(xml, Class.forName(
                            "alma.entity.xmlbinding.projectstatus.ProjectStatus"));
                } else {
                    throw new SchedulingException("More than one PS for project.");
                }
                one = false;
            }
            if(ps == null) {
                logger.info("SCHEDULING: Creating a PS for project.");
                ps = createDummyProjectStatus(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SchedulingException(e);
        }
        return ps;
    }
    /**
      * Gets all the project status objects from the archive.
      * @return An array of all the project status objects
      * @throws SchedulingException
      */
    public ProjectStatus[] getAllProjectStatus() throws SchedulingException {
        ProjectStatus[] projectStatus = null;
        Vector tmp_ps = new Vector();
        String query = new String("/ps:ProjectStatus");
        String schema = new String("ProjectStatus");
        String className = new String(
            "alma.entity.xmlbinding.projectstatus.ProjectStatus");
        try {
            Cursor cursor = archOperationComp.queryDirty(query,schema);
            if(cursor == null) {
                logger.severe("SCHEDULING: cursor was null when querying project status");
                return null;
            } else {
                logger.info("SCHEDULING: cursor was not null when querying PS.");
            }
            while(cursor.hasNext()) {
                QueryResult res = cursor.next();
                try{
                    logger.info("SCHEDULING: PS now being converted.");
                    tmp_ps.add(convertToProjectStatus1(res));
                } catch(Exception e) {
                    logger.severe("SCHEDULING: "+e.toString());
                    throw new SchedulingException (e);
                }
            }
        } catch(ArchiveInternalError e) {
             logger.severe("SCHEDULING: "+e.toString());
             throw new SchedulingException(e);
        }
        logger.info("######################################");
        logger.info("SCHEDULING: # of PS in archive = "+tmp_ps.size());
        logger.info("######################################");
        for(int i=0; i < tmp_ps.size(); i++) {
            logger.info("Current PS = "+
                ((ProjectStatus)tmp_ps.elementAt(i)).getProjectStatusEntity().getEntityId()
                    + " belongs to project "+
                        ((ProjectStatus)tmp_ps.elementAt(i)).getObsProjectRef().getEntityId());
        }
        /**
          * Temporary:
          *     For R2, check to see if there is a project status for each
          *     project. If there exists a project that doesn't have a project
          *     status a new one is created and stored in the archive. 
          */
        //Project[] tmpProjects = getAllProject();
        ObsProject[] tmpProjects = getAllObsProjects();
        if(tmpProjects.length < tmp_ps.size()) {
            logger.severe("SCHEDULING: tmp_ps greater than tmpProjects!");
            throw new SchedulingException("Cannot have more Project Status objects "+
                    "than there are Project Objects in the archive!");
        } else if(tmpProjects.length == tmp_ps.size()) { 
            logger.info("SCHEDULING: tmp_ps equal to tmpProjects!");
            //same number for each, double check they match up!
            boolean ok = true;
            for(int i=0; i < tmpProjects.length; i++){
                for(int j=0; j < tmp_ps.size(); j++){
                    System.out.println("project id = "+ tmpProjects[i].getObsProjectEntity().getEntityId());
                    System.out.println("ps project id = "+ ((ProjectStatus)tmp_ps.elementAt(j)).getObsProjectRef().getEntityId());
                    if(tmpProjects[i].getObsProjectEntity().getEntityId().equals(((ProjectStatus)tmp_ps.elementAt(j)).getObsProjectRef().getEntityId())) {
                            //WRONG! ((ProjectStatus)tmp_ps.elementAt(j)).getProjectStatusEntity().getEntityId()) 
                        //yes this project has a Project status!
                        //break this for loop and go to next project now
                        System.out.println("yes they are equal!");
                        ok = true;
                        break;
                    } else {
                        System.out.println("no not equal!");
                        //this project doesn't match this project status
                        //try again
                        ok = false;
                        //if this is the last one there's a problem! 
                        //gotta figure out a way to recover from this...
                    }
                }
                 
                if(!ok) {
                    throw new SchedulingException(
                            "SCHEDULING: Project/ProjectStatus length's match but actual objects do not match! ");
                
                }
            }
            logger.info("SCHEDULING: tmp_ps equal to tmpProjects! exiting everything ok!");
        } else if(tmpProjects.length > tmp_ps.size()) {
            logger.info("SCHEDULING: tmp_ps less than tmpProjects!");
            //more projects than project status.. 
            //create a project status & store in archive for project that doesn't have one.
            boolean ok=true;
            for(int i=0; i < tmpProjects.length; i++) {
                if(tmp_ps.size() == 0 ){
                    //No project status's at all!
                    logger.info("SCHEDULING: there were no project status objects in the archive");
                    ok = false;
                    //break; 
                } else {
                    for (int j=0; j < tmp_ps.size(); j++){
                        if(tmpProjects[i].getObsProjectEntity().getEntityId().equals(
                                    ((ProjectStatus)tmp_ps.elementAt(j)).getObsProjectRef().getEntityId())) {
                            //project has a projectStatus already
                            logger.info("PS-sched: project "+tmpProjects[i].getObsProjectEntity().getEntityId()+" has a PS");
                            ok = true;
                            break;
                        } else {
                            logger.info("PS-sched: project "+tmpProjects[i].getObsProjectEntity().getEntityId()+" doesn't have a PS");
                            ok = false;
                        }
                    }
                }
                
                if(!ok) {
                    tmp_ps.add(createDummyProjectStatus(tmpProjects[i]));
                }
            }
            
        }
        //now convert the tmp_ps vector into that ProjectStatus array to be returned.
        projectStatus = new ProjectStatus[tmp_ps.size()];
        for(int i=0; i < tmp_ps.size(); i++) {
            projectStatus[i] = (ProjectStatus)tmp_ps.elementAt(i);
        }
        if(projectStatus.length != tmpProjects.length) {
            throw new SchedulingException(
                    "SCHEDULING: ERROR in projectStatus vs project length matches.");
        }
        return projectStatus;
    }

    /**
      * retruns all the ObsProjects in the archive.
      */
    private ObsProject[] getAllObsProjects() throws SchedulingException {
        Vector tmpObsProject = new Vector();
        ObsProject[] projects = null;
        String query = new String("/prj:ObsProject");
        String schema = new String("ObsProject");
        String className = new String(
            "alma.entity.xmlbinding.obsproject.ObsProject");
        try {
            Cursor cursor = archOperationComp.queryDirty(query,schema);
            if(cursor == null) {
                logger.severe("SCHEDULING: cursor was null when querying ObsProjects");
                return null;
            } else {
                logger.info("SCHEDULING: cursor not null!");
            }
            while(cursor.hasNext()) {
                QueryResult res = cursor.next();
                try {
                    XmlEntityStruct xml = archOperationComp.retrieveDirty(res.identifier);
                    //System.out.println("PROJECT: "+ xml.xmlString);
                    ObsProject obsProj= (ObsProject)
                        entityDeserializer.deserializeEntity(xml, Class.forName(
                            "alma.entity.xmlbinding.obsproject.ObsProject"));
                    tmpObsProject.add(obsProj);
                }catch(Exception e) {
                    logger.severe("SCHEDULING: "+e.toString());
                    e.printStackTrace();
                    throw new SchedulingException (e);
                }
            }
            projects = new ObsProject[tmpObsProject.size()];
            for(int i=0; i < tmpObsProject.size();i++) {
                projects[i] = (ObsProject)tmpObsProject.elementAt(i);
            }
        } catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new SchedulingException(e);
        }
        return projects;

    }
    
    /**
      * Creates a 'dummy' ProjectStatus and stores it in the archive.
      */
    private ProjectStatus createDummyProjectStatus(ObsProject p) throws SchedulingException {
        logger.info("SCHEDULING: creating a project status for a project");
        //project doesn't have a ProjectStatus, so create one for it now and archive it
        ProjectStatus newPS = alma.scheduling.ProjectStatusUtil.createProjectStatus(p.getObsProjectEntity().getEntityId());
        logger.info("ps-sched: creating ps for project "+ p.getObsProjectEntity().getEntityId() );

        try {
            containerServices.assignUniqueEntityId(newPS.getProjectStatusEntity());
        } catch(ContainerException ce) {
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
            e.printStackTrace();
        } catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: ArchiveInternalError");
            e.printStackTrace();
        }
        return newPS;
    }

    /**
      *
      */
    private SchedBlock[] getSBsFromObsProject(ObsProject p) throws SchedulingException {
        if(p.getObsProgram().getObsPlan().getObsUnitSetTChoice() == null) {
            System.out.println("SCHEDULING: no sbs stuff available in project");
            throw new SchedulingException("Not SB info in ObsProject");
        } else {
            SchedBlockRefT[] sbs_refs = getSBRefs(p.getObsProgram().getObsPlan().getObsUnitSetTChoice());
            SchedBlock[] sbs = new SchedBlock[sbs_refs.length];
            for(int i=0; i < sbs_refs.length; i++){
                //get the sb
                sbs[i] = getSchedBlock(sbs_refs[i].getEntityId());
                
            }
            return sbs;
        }

    }
    /**
      * Gets all the schedblock references from the ObsUnitSetTChoice.
      */
    private SchedBlockRefT[] getSBRefs(ObsUnitSetTChoice choice) {
        if(choice == null) {
            System.out.println("SCHEDULING: choice is null..");
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
            XmlEntityStruct xml = archOperationComp.retrieveDirty(id);
            //System.out.println("SCHEDBLOCK: "+ xml.xmlString);
            sb = (SchedBlock) entityDeserializer.deserializeEntity(xml, Class.forName(
                    "alma.entity.xmlbinding.schedblock.SchedBlock"));
            
        }catch(Exception e){
            throw new SchedulingException (e);
        }

        return sb;
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
            project = convertToProject2(xml);
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

	// SB
    /** 
     *
     */
	public SB[] getAllSB() throws SchedulingException{
        SB sbs[]=null;
        Vector tmp_sbs = new Vector();
        int i = 0;// the index of the SB array, sbs
        String query = new String("/sbl:SchedBlock");
        String schema = new String("SchedBlock");
        String className = new String(
            "alma.entity.xmlbinding.schedblock.SchedBlock");
        try {
            Cursor cursor = archOperationComp.query(query, schema);
            if(cursor == null) {
                logger.severe("SCHEDULING: cursor was null when querying SchedBlocks");
                return null;
            }
            while (cursor.hasNext()){
                QueryResult res = cursor.next();
                try {
                    tmp_sbs.add(convertToSB1(res));
                } catch (Exception e) {
                    throw new SchedulingException (e);
                }
            }  
            int size = tmp_sbs.size();
            //System.out.println("in archive, sb length = "+size);
            
            sbs = new SB[size];
            for (int x=0; x < size; x++) {
                sbs[x] = (SB)tmp_sbs.elementAt(x);
            }
        }catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: "+e.toString());
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
        try {
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
        }
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
        try {
            ALMAPipelineProcessingRequest ppr = (ALMAPipelineProcessingRequest)p;
            XmlEntityStruct ppr_struct = ppr.getPipelineProcessingRequestStruct();
            //System.out.println(ppr_struct.xmlString);
            archOperationComp.store(ppr_struct);
        } catch(IllegalEntity e) {
            logger.severe("SCHEDULING: illegal entity error");
            e.printStackTrace();
        } catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: ArchiveInternalError");
            e.printStackTrace();
        }
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
            e.printStackTrace();
        } catch (ArchiveInternalError e) {
            logger.severe("SCHEDULING: ArchiveInternalError");
            e.printStackTrace();
        } catch (NotFound e) {
            logger.severe("SCHEDULING: Entity not found");
            e.printStackTrace();
        } //catch(EntityException e) {
            //logger.severe("SCHEDULING: error getting entity's ID");
          //  e.printStackTrace();
       // }
        return ppr;
    }

    //Session
    public String storeSession(Session s) {
        String id = null;
        try {
            ALMASession session = (ALMASession)s;
            containerServices.assignUniqueEntityId(session.getSession().getSessionEntity());
            XmlEntityStruct sessionStruct = entitySerializer.serializeEntity(
                session.getSession(), session.getSession().getSessionEntity());
            //System.out.println("Storing: "+sessionStruct.xmlString);
            archOperationComp.store(sessionStruct);
            id = session.getSession().getSessionEntity().getEntityId();
            //System.out.println(sessionStruct.xmlString);
        } catch(ContainerException e) {
            logger.severe("SCHEDULING: error getting entity's ID");
            e.printStackTrace();
        } catch(EntityException e) {
            logger.severe("SCHEDULING: error serializing session");
            e.printStackTrace();
        } catch(IllegalEntity e) {
            logger.severe("SCHEDULING: illegal entity error");
            e.printStackTrace();
        } catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: ArchiveInternalError");
            e.printStackTrace();
        }
        return id;
    }

    public Session querySession(String execid) {
        //String query = "/se:Session/ObsUnitSetReference[@entityId='"+sbid+"']";
        String query = "/se:Session/se:Execution/se:ExecBlockReference[@entityId='"+execid+"']";
        String schema = "Session"; 
        String className = new String("alma.entity.xmlbinding.session.Session");
        Session session = null;
        try {
            Cursor cursor = archOperationComp.queryDirty(query,schema);
            if(cursor == null) {
                logger.severe("SCHEDULING: cursor was null when querying Sessions!");
                return null ;
            } 
            while(cursor.hasNext()) {
                QueryResult res = cursor.next();
                try {
                    logger.info("woo getting session!");
                    session = convertToSession1(res);
                    logger.info("woo got session!");
                }catch(Exception e) {
                    logger.severe("SCHEDULING: "+e.toString());
                }
            }
        } catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: "+e.toString());
            e.printStackTrace();
        }
        return session;
    }
    
    public void updateSession(String sbid) {
        String query = "/se:Session/ObsUnitSetReference[@entityId='"+sbid+"']";
        String schema = "Session"; 
        String className = new String("alma.entity.xmlbinding.session.Session");
        try {
            Cursor cursor = archOperationComp.queryDirty(query,schema);
            if(cursor == null) {
                logger.severe("SCHEDULING: cursor was null when querying Sessions!");
                return ;
            } 
            while(cursor.hasNext()) {
                QueryResult res = cursor.next();
               /*
                try {
                //    tmp_projects.add(convertToProject1(res));
                }catch(Exception e) {
                    logger.severe("SCHEDULING: "+e.toString());
                //    throw new SchedulingException (e);
                }
                */
            }
        } catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: "+e.toString());
            e.printStackTrace();
        }
    }
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     *
     */
    private void getArchiveComponents() {
        try {
            this.archConnectionComp = alma.xmlstore.ArchiveConnectionHelper.narrow(
                containerServices.getComponent("ARCHIVE_CONNECTION"));
            this.archOperationComp = archConnectionComp.getOperational("SCHEDULING");
        } catch(ContainerException e) {
            logger.severe("SCHEDULING: ContainerException: "+e.toString());
        } catch (ArchiveException e) {
            logger.severe("SCHEDULING: Archive error: "+e.toString());
        } catch(UserDoesNotExistException e) {
            logger.severe("SCHEDULING: Archive error: "+e.toString());
        } catch (PermissionException e) {
            logger.severe("SCHEDULING: Archive error: "+e.toString());
        }
        entitySerializer = EntitySerializer.getEntitySerializer(
            containerServices.getLogger());
        entityDeserializer = EntityDeserializer.getEntityDeserializer(
            containerServices.getLogger());
        logger.fine("SCHEDULING: The ALMAArchive has been constructed.");
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
            sb = convertToSB2(xml_struct);
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
        //} catch (DirtyEntity e) {
        //    throw new Exception (e);
        } catch(EntityException e) {
            throw new Exception (e);
        }
        return sb;
    }

    /** 
     * Given the retrieved XmlEntityStruct it is converted into an SB object
     * @param XmlEntityStruct
     * @return SB
     * @throws Exception
     */
    private SB convertToSB2(XmlEntityStruct xml) throws Exception {
        ALMASB sb = null;
        //System.out.println(xml.xmlString);
        try {
            SchedBlock schedblock = (SchedBlock) 
                entityDeserializer.deserializeEntity(xml, Class.forName(
                    "alma.entity.xmlbinding.schedblock.SchedBlock"));
            sb = new ALMASB(schedblock, schedblock.getSchedBlockEntity().getEntityId());
            //sb.setCenterFrequency(schedblock.
            sb.setParent(new Program("not implemented yet"));
            sb.getParent().setReady(new DateTime(System.currentTimeMillis()));
            
        } catch(EntityException e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new Exception (e);
        }
        return sb;
    }

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
     */
    private Project convertToProject2(XmlEntityStruct xml) throws Exception {
        Project proj=null;
        //ALMAProject proj = null;
        //System.out.println(xml.xmlString);
        try {
            ObsProject obsProj= (ObsProject)
                entityDeserializer.deserializeEntity(xml, Class.forName(
                    "alma.entity.xmlbinding.obsproject.ObsProject"));
            proj = new ALMAProject(obsProj); 
        //    proj = ProjectUtil.initialize(
        } catch(EntityException e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new Exception (e);
        }
        return proj;
    }

    /*Program
    private Program convertToProgram1(QueryResult res) throws Exception{
        String prog_id = res.identifier;
        Program prog = null;
        try {
            XmlEntityStruct xml_struct = archOperationComp.retrieveDirty(prog_id);
            System.out.println(xml_struct.xmlString);
            proj = convertToProgram2(xml_struct);
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
        return prog;
    }
    private Program convertToProgram2(XmlEntityStruct xml) throws Exception{
        ALMAProgram prog = null;
        try {
            ObsProgram obsProg = (ObsProgram)
                entityDeserializer.deserializeEntity(xml.Class.forName(
                    "alma.entity.xmlbinding.
        } catch (Exception e){
        }
        return prog;
    }
    */

    private Session convertToSession1(QueryResult res) throws Exception {
        String session_id = res.identifier;
        Session session=null;
        try {
            XmlEntityStruct xml_struct = archOperationComp.retrieveDirty(session_id);
            session = convertToSession2(xml_struct);
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
        return session;
    }

    private Session convertToSession2(XmlEntityStruct xml) throws Exception {
        ALMASession session=null;
        try {
            //logger.info(xml.xmlString);
            alma.entity.xmlbinding.session.Session s = (alma.entity.xmlbinding.session.Session) 
                entityDeserializer.deserializeEntity(xml, Class.forName(
                    "alma.entity.xmlbinding.session.Session"));

            session = new ALMASession(s);
            
        } catch(EntityException e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new Exception (e);
        }
        return session;
    }

    private ProjectStatus convertToProjectStatus1(QueryResult res) throws Exception {
        String ps_id = res.identifier;
        ProjectStatus ps = null;
        try{
            XmlEntityStruct xml_struct = archOperationComp.retrieveDirty(ps_id);
            //logger.info("project status" + xml_struct.xmlString);
            ps = (alma.entity.xmlbinding.projectstatus.ProjectStatus)
                    entityDeserializer.deserializeEntity(xml_struct, Class.forName(
                        "alma.entity.xmlbinding.projectstatus.ProjectStatus"));
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
        return ps;
    }

    /*
    private ProjectStatus convertToProjectStatus2(XmlEntityStruct xml) throws Exception {
        ProjectStatus almaps = null;
        try {
            alma.entity.xmlbinding.projectstatus.ProjectStatus ps =
                (alma.entity.xmlbinding.projectstatus.ProjectStatus)
                    entityDeserializer.deserializeEntity(xml, Class.forName(
                        "alma.entity.xmlbinding.projectstatus.ProjectStatus"));
            almaps = new ALMAProjectStatus(ps);
        } catch(EntityException e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new Exception (e);
        }
        return almaps;
    }
    */

    /**
     * Temporary function for updating the sb in the archive.
     */
    /*private SchedBlock getSBfromArchive(String id) {
        SchedBlock sb =null;
        try {
            XmlEntityStruct xml = archOperationComp.retrieveDirty(id);
            //logger.info(xml.xmlString); //not poll archive
            sb = (SchedBlock) 
                entityDeserializer.deserializeEntity(xml, Class.forName(
                    "alma.entity.xmlbinding.schedblock.SchedBlock"));
        } catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: "+e.toString());
        } catch(NotFound e) {
            logger.severe("SCHEDULING: "+e.toString());
        } catch(MalformedURI e) {
            logger.severe("SCHEDULING: "+e.toString());
        } catch(Exception e) {
            logger.severe("SCHEDULING: "+e.toString());
        }
        return sb;
    }
    */
    /**
     * Updates the SB with the information obtained from the Event received from
     * from Control.
     *
     * @param ControlEvent The event from control.
     * @throws SchedulingException
     */
    public void updateSB(ControlEvent ce) throws SchedulingException{
        try {
            SB sb = getSB(ce.getSBId());
            ExecBlock eb = new ExecBlock(ce.getEBId(), ce.getSAId());
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
                    ouc.setSchedStatus(ObsUnitControlTSchedStatusType.RUNNING);
                    //sb.setRunning();
                    break;
                case 1: //exec block status = ok
                    ouc.setSchedStatus(ObsUnitControlTSchedStatusType.COMPLETED);
                    break;
                case 2://exec block status = failed
                    ouc.setSchedStatus(ObsUnitControlTSchedStatusType.ABORTED);
                    break;
                case 3://exec block status = timeout
                    ouc.setSchedStatus(ObsUnitControlTSchedStatusType.ABORTED);
                    break;
                default://exec block status kooky.. 
                    break;
            }
            XmlEntityStruct newsb = entitySerializer.serializeEntity(schedblock);
            archOperationComp.update(newsb);
        } catch(Exception e) {
            logger.severe("SCHEDULING: Could not update the SB after the "+
                "exec block event was received!");
            e.printStackTrace();
        }
    }

    
    /*
    private XmlEntityStruct modifyRetrievedSB(XmlEntityStruct oldSBstruct, SB newSB) {
        XmlEntityStruct newSBstruct = null;
        try {
            SchedBlock oldSB = (SchedBlock) 
                entityDeserializer.deserializeEntity(oldSBstruct, Class.forName(
                    "alma.entity.xmlbinding.schedblock.SchedBlock"));
            ////////////////////////
            // update the status
            ////////////////////////
            ObsUnitControl ouc = oldSB.getObsUnitControl();
            if(ouc == null) {
                ouc = new ObsUnitControl();
            }
            switch(newSB.getStatus().getStatusAsInt()) {
                case 0://exec block status = processing
                    ouc.setSchedStatus(SchedStatusT.RUNNING);
                    break;
                case 1: //exec block status = ok
                    ouc.setSchedStatus(SchedStatusT.COMPLETED);
                    break;
                case 2://exec block status = failed
                    ouc.setSchedStatus(SchedStatusT.ABORTED);
                    break;
                case 3://exec block status = timeout
                    ouc.setSchedStatus(SchedStatusT.ABORTED);
                    break;
                default://exec block status kooky.. 
                    break;
            }            
            oldSB.setObsUnitControl(ouc);
            ////////////////////////
            // Add the exec block reference
            ////////////////////////

            newSBstruct = entitySerializer.serializeEntity(oldSB);
        } catch(Exception e) {
        }
        return newSBstruct;
    }
    */
}
