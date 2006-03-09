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
import java.util.Vector;

import alma.scheduling.Define.*;
import alma.scheduling.SBLite;

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
import alma.entity.xmlbinding.specialsb.*;
import alma.entities.commonentity.*;


/**
 * This class provides all the functionalitiy from the archvie which 
 * is specific to the Scheduling Subsystem. It implements the Archive 
 * interface from the scheduling's define package and it connects via
 * the container services to the real archive used by all of alma.
 *
 * @version $Id: ALMAArchive.java,v 1.51 2006/03/09 17:16:34 sslucero Exp $
 * @author Sohaila Lucero
 */
public class ALMAArchive implements Archive {
    //The container services
    private ContainerServices containerServices;
    // The archive's components
    private ArchiveConnection archConnectionComp;
    private Operational archOperationComp;
    //TODO should check project queue.. if project exists don't map a new one.
    //The logger
    private Logger logger;
    //Entity deserializer - makes entities from the archive human readable
    private EntityDeserializer entityDeserializer;
    //Entity Serializer - prepares entites for the archive
    private EntitySerializer entitySerializer;
    //The DateTime of the last query for SBs
    private DateTime lastSBquery;
    
    //ALMA Clock
    private ALMAClock clock;

    /**
      *
      */
    public ALMAArchive(ContainerServices cs, ALMAClock c){
        this.containerServices = cs;
        this.logger = cs.getLogger();
        this.clock = c;
        getArchiveComponents();
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
                logger.info("SCHEDULING: cursor not null when querying Special SB!");
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
                    e.printStackTrace();
                    throw new SchedulingException (e);
                }
            }
            sbs = new SpecialSB[tmpSpecialSBs.size()];
            for(int i=0; i < tmpSpecialSBs.size();i++) {
                sbs[i] = (SpecialSB)tmpSpecialSBs.elementAt(i);
            }
        } catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: "+e.toString());
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
        Project[] projects = null;
        Vector tmp_projects = new Vector();
        
        ObsProject[] obsProj = getAllObsProjects();
        for(int i=0; i < obsProj.length; i++) {
            ProjectStatus ps = getProjectStatusForObsProject(obsProj[i]);
            if(ps == null) { //no project status for this project. so create one
                 logger.info("SCHEDULING: no ps for this project");
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
	//logger.info("returning "+projects.length+" from getAllProject");
        return projects;
    }

    public Project checkProjectForUpdates(String id) throws SchedulingException {
        return null;
    }

    public ProjectStatus getProjectStatus(Project p) throws SchedulingException {
        ProjectStatus ps = null;
        String ps_id = p.getProjectStatusId();
        //logger.info("Getting project status from archive, id = "+ ps_id);
        try {
            XmlEntityStruct xml = archOperationComp.retrieveDirty(ps_id);
            //logger.info("PS xml: "+xml.xmlString);
            ps = (ProjectStatus)entityDeserializer.deserializeEntity(xml, ProjectStatus.class); 
            ps = ProjectUtil.updateProjectStatus(p);
            updateProjectStatus(ps);
        } catch(Exception e) {
            e.printStackTrace();
            throw new SchedulingException(e);
        }
        return ps;
    }

    public ProjectStatus getProjectStatusForObsProject(ObsProject p) throws SchedulingException {
        ProjectStatus ps = null;
        String ps_id = p.getProjectStatusRef().getEntityId();
        try {
	    //logger.info("Retrieving project status "+ps_id);
            XmlEntityStruct xml = archOperationComp.retrieveDirty(ps_id);
            //logger.info("PROJECT STATUS: "+ xml.xmlString);
            ps = (ProjectStatus)entityDeserializer.deserializeEntity(xml, ProjectStatus.class); 
        } catch(Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
            throw new SchedulingException(e);
        }
        return ps;
    }
    

    /**
      * retruns all the ObsProjects in the archive.
      */
    private ObsProject[] getAllObsProjects() throws SchedulingException {
        Vector tmpObsProject = new Vector();
        ObsProject[] projects=null;
        String query = new String("/prj:ObsProject");
        String schema = new String("ObsProject");
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
                    //logger.info("PROJECT : "+ xml.xmlString);
                    //System.out.println("PROJECT taken out of archive: "+ xml.xmlString);
                    ObsProject obsProj= (ObsProject)
                        entityDeserializer.deserializeEntity(xml, ObsProject.class);
                            //"alma.entity.xmlbinding.obsproject.ObsProject"));
                    //logger.info("SCHEDULING: xml struct timestamp = "+xml.timeStamp);
                    //logger.info("SCHEDULING: entityT timestamp = "+obsProj.getObsProjectEntity().getTimestamp());
		    //logger.info("Project deserialized.. time stamp = "+obsProj.getObsProjectEntity().getTimestamp());
                    tmpObsProject.add(obsProj);
                    //obsProj = ProjectUtil.addPartIds(obsProj);
                    //XmlEntityStruct p_xml = entitySerializer.serializeEntity(
                     //       obsProj, obsProj.getObsProjectEntity());
                    //xml.xmlString = p_xml.xmlString;
                    //System.out.println("project with generated part ids"+xml.xmlString);
                    //archOperationComp.update(xml);
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
        /*
        if(projects == null){
            projects = new ObsProject[1];
            projects[0]= new ObsProject();
        }*/
        return projects;

    }
    
    /**
      * Creates a 'dummy' ProjectStatus and stores it in the archive.
      */
    private ProjectStatus createDummyProjectStatus(ObsProject p) throws SchedulingException {
        logger.info("SCHEDULING: creating a project status for a project");
        //project doesn't have a ProjectStatus, so create one for it now and archive it
        String proj_id = p.getObsProjectEntity().getEntityId();
        ProjectStatus newPS = new ProjectStatus();
        ProjectStatusEntityT ps_entity = new ProjectStatusEntityT();
        newPS.setProjectStatusEntity(ps_entity);

        // Create the entity reference object for the ObsProject
        ObsProjectRefT obsProjRef = new ObsProjectRefT();
        obsProjRef.setEntityId(proj_id);
        newPS.setObsProjectRef(obsProjRef);

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
            logger.info("SCHEDULING: no sbs stuff available in project");
            throw new SchedulingException("No SB info in ObsProject");
        } else {
            SchedBlockRefT[] sbs_refs = getSBRefs(p.getObsProgram().getObsPlan().getObsUnitSetTChoice());
            SchedBlock[] sbs = new SchedBlock[sbs_refs.length];
            for(int i=0; i < sbs_refs.length; i++){
                //get the sb
                sbs[i] = getSchedBlock(sbs_refs[i].getEntityId());
                
            }
            logger.info("SCHEDULING: (in archive) project has "+sbs.length+" sbs");
            return sbs;
        }

    }

    /**
      *
      */
    private ObsProject getObsProject(String id) throws SchedulingException {
        ObsProject proj;
        try {
            XmlEntityStruct xml = archOperationComp.retrieveDirty(id);
            proj = (ObsProject) entityDeserializer.deserializeEntity(xml, ObsProject.class);
        }catch(Exception e){
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
    private SchedBlockRefT[] getSBRefs(ObsUnitSetTChoice choice) {
        if(choice == null) {
            logger.info("SCHEDULING: choice is null..");
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
    public SBLite[] getSBLites() {
	logger.info("SCHEDULING: Called getSBLites");
        SBLite[] sbliteArray=null;
        SBLite sblite;
        Vector<SBLite> sbliteVector = new Vector<SBLite>();
        try {
            Project[] projects = getAllProject();
//System.out.println("# of projects retrieved in getSBLite = "+projects.length);
            String sid,pid,sname,pname,pi,pri;
            double ra,dec,freq,score,success,rank;
            long maxT;
            for(int i=0; i < projects.length; i++){
                //get all the sbs of this project
                SB[] sbs = projects[i].getAllSBs ();
//System.out.println("# of sbs  retrieved in getSBLite for project "+i+" = "+sbs.length);
                for(int j=0; j < sbs.length; j++) {
                    sblite = new SBLite();
                    sid = sbs[j].getId();
                    if(sid == null || sid =="") {
                        sid = "WARNING: Problem with SB id";
                    }
                    sblite.schedBlockRef =sid;
                    //sblite.schedBlockRef = sbs[j].getId();
                    pid = sbs[j].getProject().getId();
                    if(pid ==null||pid=="") {
                        pid = "WARNING: problem with project id";   
                    }
                    sblite.projectRef = pid;
                    //sblite.projectRef = sbs[j].getProject().getId();
                    sblite.obsUnitsetRef = "";
                    sname =sbs[j].getSBName();
                    if(sname == null || sname ==""){
                        sname = "WARNING: problem with SB name";
                    }
                    sblite.sbName =sname;
                    //sblite.sbName = sbs[j].getSBName();
                    pname = sbs[j].getProject().getProjectName();
                    if(pname == null ||pname =="") {
                        pname = "WARNING: problem with project name";
                    }
                    sblite.projectName = pname;
                    //sblite.projectName = sbs[j].getProject().getProjectName();
                    pi = sbs[j].getProject().getPI();sbs[j].getProject().getPI();
                    if(pi == null || pi == ""){
                        pi = "WARNING: problem with pi";
                    }
                    sblite.PI = pi;
                    //sblite.PI = sbs[j].getProject().getPI();
                    pri = sbs[j].getProject().getScientificPriority().getPriority();
                    if(pri == null || pri =="") {
                        pri = "WARNING: problem with scientific priority";
                    }
                    sblite.priority = pri;
                    //sblite.priority = sbs[j].getProject().getScientificPriority().getPriority();
		            try {
	                    ra = sbs[j].getTarget().getCenter().getRa();
        	 	    } catch(NullPointerException npe) {
		            	logger.info("SCHEDULING: RA object == null in SB, setting to 0.0");
            			ra = 0.0;
		            }
                    sblite.ra = ra;
                    //sblite.ra = sbs[j].getTarget().getCenter().getRa();
        		    try {
	                    dec = sbs[j].getTarget().getCenter().getDec();
	 	            } catch(NullPointerException npe) {
            			logger.info("SCHEDULING: DEC object == null in SB, setting to 0.0");
			            dec = 0.0;
        		    }
                    sblite.dec = dec;
                    //sblite.dec = sbs[j].getTarget().getCenter().getDec();
                    sblite.freq = 0;//sbs[j].getFrequencyBand().getHighFrequency();
                    sblite.maxTime = 0;
                    sblite.score = 0;
                    sblite.success = 0; 
                    sblite.rank = 0 ;

                    sbliteVector.add(sblite);
                }
            }
            sbliteArray = new SBLite[sbliteVector.size()];
            sbliteArray = sbliteVector.toArray(sbliteArray);
            
        } catch(Exception e) {
	    logger.info(e.toString());
            e.printStackTrace();
        }
        return sbliteArray;
    }


    /**
      *
      */
    public synchronized void updateProjectStatus(ProjectStatus ps) throws SchedulingException {
        try {
            XmlEntityStruct xml = entitySerializer.serializeEntity(ps, ps.getProjectStatusEntity());
            //logger.info("SCHEDULING: updated PS: "+xml.xmlString);
            XmlEntityStruct xml2 = archOperationComp.retrieveDirty(ps.getProjectStatusEntity().getEntityId());
            xml2.xmlString = xml.xmlString;
            archOperationComp.update(xml2);
        } catch(Exception e){
            e.printStackTrace();
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

    public ObsProject getProjectForSB(String sbid) throws SchedulingException {
        try {
            String query = new String("/prj:ObsProject//sbl:SchedBlockRef[entityId=\""+sbid+"\"]");
            String schema = new String("ObsProject");
            Cursor cursor = archOperationComp.query(query, schema);
            if(cursor == null) {
                logger.severe("SCHEDULING: cursor was null when querying obsproject");
                throw new SchedulingException("SCHEDULING: Cursor was null when querying obsproject");
            }
            ObsProject proj=null;
            QueryResult res = cursor.next();
            //deprecated method; will have to retrieve objsproject with res.identifier thing
            proj = (ObsProject)entityDeserializer.deserializeEntity(res.xml,ObsProject.class); 
            if(cursor.hasNext()){
                throw new SchedulingException("SCHEDULING: getting proj with sb ref, should only be one proj!");
            }
            return proj;

        } catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new SchedulingException(e);
        //} catch(NotFound e) {
           // logger.severe("SCHEDULING: "+e.toString());
        //} catch(MalformedURI e) {
         //   logger.severe("SCHEDULING: "+e.toString());
        } catch(Exception e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new SchedulingException(e);
        }
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
    public Cursor querySBs() throws SchedulingException {
        try {
            String query = new String("/sbl:SchedBlock");
            String schema = new String("SchedBlock");
            Cursor cursor = archOperationComp.query(query, schema);
            if(cursor == null) {
                logger.severe("SCHEDULING: cursor was null when querying SchedBlocks");
                throw new SchedulingException("SCHEDULING: Cursor was null when querying SBs");
            }
            return cursor;
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
        //String query = new String("/sbl:SchedBlock");
        //String schema = new String("SchedBlock");
        //String className = new String(
        //    "alma.entity.xmlbinding.schedblock.SchedBlock");
        try {
            /*
            Cursor cursor = archOperationComp.query(query, schema);
            if(cursor == null) {
                logger.severe("SCHEDULING: cursor was null when querying SchedBlocks");
                return null;
            }*/
            Cursor cursor = querySBs();
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
        /*
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

    ///////////////////////////////////////////////////////////////////////////
    
    /**
     *
     */
    private void getArchiveComponents() {
        try {
            org.omg.CORBA.Object obj = containerServices.getDefaultComponent("IDL:alma/xmlstore/ArchiveConnection:1.0");
            logger.info("got ARCHIVE CONNECTION stub of type " + obj.getClass().getName());
            this.archConnectionComp = alma.xmlstore.ArchiveConnectionHelper.narrow(obj);
                //containerServices.getComponent("ARCHIVE_CONNECTION"));
            //TODO take this out eventually
            this.archConnectionComp.getAdministrative("SCHEDULING").init();
            this.archOperationComp = archConnectionComp.getOperational("SCHEDULING");
        } catch(ContainerException e) {
            logger.severe("SCHEDULING: ContainerException: "+e.toString());
        } catch (ArchiveException e) {
            logger.severe("SCHEDULING: Archive error: "+e.toString());
        } catch(UserDoesNotExistException e) {
            logger.severe("SCHEDULING: Archive error: "+e.toString());
        } catch (PermissionException e) {
            logger.severe("SCHEDULING: Archive error: "+e.toString());
        } catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: Archive error: "+e.toString());
        }
        entitySerializer = EntitySerializer.getEntitySerializer(
            containerServices.getLogger());
        entityDeserializer = EntityDeserializer.getEntityDeserializer(
            containerServices.getLogger());
        logger.fine("SCHEDULING: The ALMAArchive has been constructed.");
    }

    /**
      * Releases all the archive components.
      * TODO: find out how to release operational one..
      */
    public void releaseArchiveComponents() {
        try {
            containerServices.releaseComponent("ARCHIVE_CONNECTION");
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
        SB sb = null;
        /*
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
        */
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
                entityDeserializer.deserializeEntity(xml, ObsProject.class);
                    //"alma.entity.xmlbinding.obsproject.ObsProject"));
          //  proj = new ALMAProject(obsProj); 
        } catch(EntityException e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new Exception (e);
        }
        return proj;
    }



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
            e.printStackTrace();
        }
    }

    public String getPPRString(ProjectStatus ps, String pprId) {
        String result = "";
        try {
            updateProjectStatus(ps);

            String query = new String("/ps:ProjectStatus//ps:PipelineProcessingRequest[@entityPartId='"+pprId+"']");
            String schema = new String("ProjectStatus");
            
            Cursor cursor = archOperationComp.queryDirty(query,schema);
            if(cursor == null) {
                throw new SchedulingException ("SCHEDULING: Error querying archive for PPR");
            }
            while(cursor.hasNext()){
                QueryResult res = cursor.next();
                //logger.info("PPR xml (in archive)= "+res.xml);
                result = res.xml;
            }

            //query the archive for the pipelineprocessing requestthe pipelineprocessing request..
        } catch(Exception e) {
            logger.severe("SCHEDULING: Error getting the PPR String.");
            e.printStackTrace();
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
