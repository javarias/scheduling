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
import alma.entity.xmlbinding.obsproject.*;
import alma.entities.generalincludes.*;
//

/**
 * This class provides all the functionalitiy from the archvie which 
 * is specific to the Scheduling Subsystem. It implements the Archive 
 * interface from the scheduling's define package and it connects via
 * the container services to the real archive used by all of alma.
 *
 * @version 1.0
 * @author Sohaila Roberts
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
        //this.lastSBquery = 
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
                logger.info("SCHED: NEXT!");
                QueryResult res = cursor.next();
                try {
                    logger.info("SCHED: convert to proj");
                    tmp_projects.add(convertToProject1(res));
                }catch(Exception e) {
                    logger.severe("SCHEDULING: "+e.toString());
                    throw new SchedulingException (e);
                }
            }
            logger.info("SCHEDULING: Projects available = "+tmp_projects.size());
        } catch(ArchiveInternalError e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new SchedulingException(e);
        }
        return projects;
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
        return null;
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
                    tmp_sbs.add(convertToSB(res));
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
	public SB getSB(String id) throws SchedulingException{
        return null;
    }
	public void updateSB(SB sb) throws SchedulingException{
    }

	// SchedulingPolicy
	public Policy[] getPolicy() throws SchedulingException{
        return null;
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
    private SB convertToSB(QueryResult res) throws Exception {
        String sb_id = res.identifier;
        ALMASB sb = null;
        try {
            //XmlEntityStruct xml_struct = archOperationComp.updateRetrieve(sb_id);
            //XmlEntityStruct xml_struct = archOperationComp.retrieve(sb_id);
            XmlEntityStruct xml_struct = archOperationComp.retrieveDirty(sb_id);
            //System.out.println(xml_struct.xmlString);
            SchedBlock schedblock = (SchedBlock) 
                entityDeserializer.deserializeEntity(xml_struct, Class.forName(
                    "alma.entity.xmlbinding.schedblock.SchedBlock"));
            //additionalInfo(schedblock);
            sb = new ALMASB(schedblock, sb_id);
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

    private Project convertToProject1(QueryResult res)throws Exception {
        System.out.println("First conversion!");
        String proj_id = res.identifier;
        Project proj = null;
        try {
            XmlEntityStruct xml_struct = archOperationComp.retrieveDirty(proj_id);
            //System.out.println(xml_struct.xmlString);
            proj = convertToProject2(xml_struct);
            System.out.println("First conversion DONE!");
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
        ALMAProject proj = null;
        //System.out.println("Printing!" +xml.xmlString);
        System.out.println("Second conversion!");
        try {
            ObsProject obsProj= (ObsProject)
                entityDeserializer.deserializeEntity(xml, Class.forName(
                    "alma.entity.xmlbinding.obsproject.ObsProject"));
            proj = new ALMAProject(obsProj); 
            System.out.println("Second conversion DONE!");
        } catch(EntityException e) {
            logger.severe("SCHEDULING: "+e.toString());
            throw new Exception (e);
        }
        return proj;
    }
}
