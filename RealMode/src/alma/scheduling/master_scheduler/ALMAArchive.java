/*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
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
package ALMA.scheduling.master_scheduler;

import java.io.IOException;
import java.io.StringWriter;
import java.io.StringReader;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ArrayList;
import java.util.Vector;
import java.lang.Class;

import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;
import alma.xmlstore.Operational;
import alma.xmlentity.XmlEntityStruct;
import alma.entities.commonentity.EntityT;
import alma.entity.xmlbinding.schedblock.SchedBlock;
//import alma.bo.SchedBlock;
import alma.entity.xmlbinding.schedblock.SchedBlockEntityT;
import alma.entity.xmlbinding.obsproject.ObsUnitControl;
//import alma.bo.ObsUnitControl;
import alma.entity.xmlbinding.obsproject.ObsProject;
import alma.entity.xmlbinding.pipelineprocessingrequest.PipelineProcessingRequest;
import alma.entity.xmlbinding.obsproject.types.SchedStatusT;
import alma.entity.xmlbinding.schedulingpolicy.SchedulingPolicy;

import alma.acs.entityutil.EntitySerializer;
import alma.acs.entityutil.EntityDeserializer;
import alma.acs.entityutil.EntityException;

import alma.xmlstore.OperationalPackage.MalformedURI;
import alma.xmlstore.OperationalPackage.NotFound;
import alma.xmlstore.ArchiveInternalError;
import alma.xmlstore.Cursor;
import alma.xmlstore.CursorPackage.QueryResult;

import ALMA.scheduling.define.STime;
/**
 * The ALMAArchive class is the interface to the real or simulated archive.
 * 
 * @version 1.00 May 5, 2003
 * @author Allen Farris
 */
public class ALMAArchive implements ArchiveProxy {
    private ContainerServices containerServices;
    private Operational operArchiveComp;
    private boolean isSimulation;
    private EntitySerializer entitySerializer;
    private EntityDeserializer entityDeserializer;
    private Logger logger;

    private int sbCount;
    private int pprCount;
    private int projectCount;
    private int policyCount;
    
    public ALMAArchive (boolean isSimulation, ContainerServices container) {
        this.containerServices = container;
        this.isSimulation = isSimulation;
        this.logger = containerServices.getLogger();
        try {
            this.operArchiveComp = alma.xmlstore.OperationalHelper.narrow(
                container.getComponent("OPERATIONAL_ARCHIVE"));
        } catch(ContainerException e) {
            logger.log(Level.SEVERE, "SCHEDULING: Could not get archive!");
        }
        entitySerializer = EntitySerializer.getEntitySerializer(container.getLogger());
        //entityDeserializer = new EntityDeserializer(container.getLogger());
        entityDeserializer = EntityDeserializer.getEntityDeserializer(container.getLogger());
        logger.log(Level.FINE, "SCHEDULING: The ALMAArchive has been constructed.");
    }

    /**
     *  Release the archive's component.
     */
    public void release() {
        containerServices.releaseComponent("OPERATIONAL_ARCHIVE");
    }

    /**
     *  Get all the scheduling blocks out of the archive.
     *  @return SchedBlock[] The array of scheduling blocks.
     */
    public SchedBlock[] getSchedBlock() {
        //String query = "/SchedBlock";
        String query = "/*";
        String schema = "SchedBlock";
        String className = "alma.entity.xmlbinding.schedblock.SchedBlock";
        //String className = "alma.bo.SchedBlock";
        try {
            Cursor cursor = operArchiveComp.query(query, schema);
            sbCount = cursor.count();
            if(cursor == null) {
                logger.severe("SCHEDULING: cursor is null..");
            }
            SchedBlock[] sbs = new SchedBlock[cursor.count()];
            //logger.log(Level.FINE, "SCHEDULING: Cursor's count = "+ cursor.count());
            int i=0;
            while(cursor.hasNext()) {
                QueryResult res = cursor.next();
                //logger.log(Level.FINE, "XML::::"+ res.xml);
                //sbs[i++] = (SchedBlock)convertToObject(res.xml, schema);
                sbs[i++] = (SchedBlock)convertToObject(res, className);
            }
            return sbs;
        } catch(ArchiveInternalError e){
            //System.out.println(e.toString());
            logger.log(Level.SEVERE, "SCHEDULING:"+ e.toString());
        }
        return null;
    }

    /**
     *  Gets all the scheduling blocks entered into the archive since the
     *  given time.
     *  @param time The time from which to check if there have been any new 
     *              scheduling blocks entered into the archive.
     *  @return SchedBlock[] Array of all the new scheduling blocks.
     */
    public SchedBlock[] getNewSchedBlock(STime time) {
        SchedBlock[] sbs = getSchedBlock();
        return sbs;
    }

    /**
     *  Get the scheduling block from the archive that has the given id 
     *  @param id The unique identifier to search the archive for.
     *  @return SchedBlock  The scheduling block with the given id. 
     */
    public SchedBlock getSchedBlock(String id) {
        SchedBlock sb = null;
        //This is a temporary fix coz i can't do namespace queries yet!!
        SchedBlock[] all_sb = getSchedBlock();
        for(int i=0; i < all_sb.length; i++) {
            if(((String)all_sb[i].getSchedBlockEntity().getEntityId()).equals(id)){
                sb = (SchedBlock)all_sb[i];
                break;
            }
        }
        /*
        String query = "/SchedBlock/SchedBlockEntityT[@entityId=\""+id+"\"]";
        String schema = "SchedBlock";
        try { 
            Cursor cursor = operArchiveComp.query(query, schema);
            if(cursor.count() > 1) { //error coz there should only be one!! 
                return null;
            }
            QueryResult res = cursor.next();
            sb = (SchedBlock) convertToObject(res, schema);
            //sb = (SchedBlock) convertToObject(res.xml, schema);
        } catch (ArchiveInternalError e){
            logger.log(Level.SEVERE, e.toString());
        }
        */
        return sb;
    }

    public void updateSchedBlock(String id) {
        XmlEntityStruct sb_xml = retrieve(id);
        //convert to SchedBlock
        SchedBlock sb_obj = (SchedBlock)convertToObject(sb_xml.xmlString, 
            "alma.entity.xmlbinding.schedblock.SchedBlock");
        //update status - for now it just does it to complete! will change!!! :)
        ObsUnitControl ouc = sb_obj.getObsUnitControl(); 
        if(ouc == null) {
            ouc = new ObsUnitControl();
        }
        ouc.setSchedStatus(SchedStatusT.COMPLETED);
        sb_obj.setObsUnitControl(ouc);
        //stores it back in archive
        updateSchedBlock(sb_obj);
    }

    /**
     *  Updates the provided scheduling block in the archive.
     *  @param sb Updated copy of the scheduling block to be updated in the archive
     */
    public void updateSchedBlock(SchedBlock sb) {
        try {
            XmlEntityStruct xmlEntity = entitySerializer.serializeEntity(sb);
            operArchiveComp.update(xmlEntity);
        } catch(EntityException e){
            logger.log(Level.SEVERE,"SCHEDULING: "+ e.toString());
        } catch(ArchiveInternalError e) {
            logger.log(Level.SEVERE,"SCHEDULING: "+ e.toString());
        } catch(MalformedURI e) {
            logger.log(Level.SEVERE,"SCHEDULING: "+ e.toString());
        } catch(InternalError e) {
            logger.log(Level.SEVERE,"SCHEDULING: "+ e.toString());
        }
    }


    public ObsProject[] getProject() {
        //String query = "/ObsProject";
        String query = "/*";
        String schema = "ObsProject";
        String className = "alma.entity.xmlbinding.obsproject.ObsProject";
        try {
            Cursor cursor = operArchiveComp.query(query, schema);
            projectCount = cursor.count();
            ObsProject[] obsProject = new ObsProject[cursor.count()];
            int i=0;
            while(cursor.hasNext()) {
                QueryResult res = cursor.next();
                obsProject[i++] = (ObsProject)convertToObject(res, className);
            }
            return obsProject;
        } catch (ArchiveInternalError e) {
            logger.log(Level.SEVERE, "SCHEDULING: "+e.toString());
        }
        return null;
    }

    public ObsProject getProject(String id) {
        ObsProject obsProject = null;
        /*
        String query = "/ObsProject/ObsProjectEntity[@entityId=\""+id+"\"]";
        String schema = "ObsProject";
        
        try {
          Cursor cursor = operArchiveComp.query(query, schema);
            if(cursor.count() > 1) { //error coz there should only be one!! 
                return null;
            }
            QueryResult res = cursor.next();
            obsProject = (ObsProject) convertToObject(res, schema);
            //obsProject = (ObsProject) convertToObject(res.xml, schema);
            
            obsProject
        } catch (ArchiveInternalError e){
            logger.log(Level.SEVERE, e.toString());
        }
        */
        return obsProject;
    }

    public void updateProject(ObsProject p) {
        try {
            XmlEntityStruct xmlEntity = entitySerializer.serializeEntity(p);
            operArchiveComp.update(xmlEntity);
        } catch(EntityException e){
            logger.log(Level.SEVERE,"SCHEDULING: "+ e.toString());
        } catch(ArchiveInternalError e) {
            logger.log(Level.SEVERE,"SCHEDULING: "+ e.toString());
        } catch(MalformedURI e) {
            logger.log(Level.SEVERE,"SCHEDULING: "+ e.toString());
        } catch(InternalError e) {
            logger.log(Level.SEVERE,"SCHEDULING: "+ e.toString());
        }
    }

    public SchedulingPolicy[] getSchedulingPolicy() {
        //String query = "/SchedulingPolicy";
        String query = "/*";
        String schema = "SchedulingPolicy";
        try {
            Cursor cursor = operArchiveComp.query(query, schema);
            policyCount = cursor.count();
            SchedulingPolicy[] schedulingPolicy = new SchedulingPolicy[cursor.count()];
            int i=0;
            while(cursor.hasNext()) {
                QueryResult res = cursor.next();
                schedulingPolicy[i++] = (SchedulingPolicy)convertToObject(res, schema);
                //schedulingPolicy[i++] = (SchedulingPolicy)convertToObject(res.xml, schema);
            }
            return schedulingPolicy;
        } catch (ArchiveInternalError e) {
            logger.log(Level.SEVERE,"SCHEDULING: "+ e.toString());
        }
        return null;
    }

    public PipelineProcessingRequest[] getPipelineProcessingRequest() {
        /*
        String query = "/PipelineProcessingRequest";
        String schema = "PipelineProcessingRequest";
        try {
            Cursor cursor = operArchiveComp.query(query, schema);
            PipelineProcessingRequest[] ppr = new PipelineProcessingRequest[cursor.count()];
            int i = 0;
            while(cursor.hasNext()){
                QueryResult res = cursor.next();
                ppr[i++] = (PipelineProcessingRequest)convertToObject(res, schema);
                //ppr[i++] = (PipelineProcessingRequest)convertToObject(res.xml, schema);
            }
            return ppr;
        } catch(ArchiveInternalError e) {
            logger.log(Level.SEVERE, e.toString());
        }
        */
        return null;
    }

    public PipelineProcessingRequest getPipelineProcessingRequest(String id) {
        PipelineProcessingRequest ppr = null;
        /*
        String query = "/PipelineProcessingRequest/PipelineProcessingRequestEntityT[@entityId=\""+id+"\"]";
        String schema = "PipelineProcessingRequest";
        try {
            Cursor cursor = operArchiveComp.query(query, schema);
            if(cursor.count() > 1) { //error coz there should only be one!
                return null;
            }
            QueryResult res = cursor.next();
            ppr = (PipelineProcessingRequest)convertToObject(res, schema);
            //ppr = (PipelineProcessingRequest)convertToObject(res.xml, schema);
        } catch(ArchiveInternalError e) {
            logger.log(Level.SEVERE, e.toString());
        }
        */
        return ppr;
    }

    /**
     *  Adds the pipelineprocessingrequest to the archive.
     *  @param ppr PipelineProcessingRequest which is added to the archive.
     */
    public void addPipelineProcessingRequest(PipelineProcessingRequest ppr) {
        try {
            XmlEntityStruct xmlEntity = entitySerializer.serializeEntity(ppr);
            operArchiveComp.update(xmlEntity);
        } catch(EntityException e){
            logger.log(Level.SEVERE,"SCHEDULING: "+ e.toString());
        } catch(ArchiveInternalError e) {
            logger.log(Level.SEVERE, "SCHEDULING: "+e.toString());
        } catch(MalformedURI e) {
            logger.log(Level.SEVERE, "SCHEDULING: "+e.toString());
        } catch(InternalError e) {
            logger.log(Level.SEVERE,"SCHEDULING: "+ e.toString());
        }
    }
    
    /**
     *  Updates the given pipelineprocessingrequest in the archive
     *  @param ppr The updated version of the PipelineProcessingRequest
     */
    public void updatePipelineProcessingRequest(PipelineProcessingRequest ppr) {
        addPipelineProcessingRequest(ppr);
    }
    /******************************************************************/
    /* Other stuff */
    /*
    public ExecBlock getExecBlock(String id) {
    }
    */
    
    
    
/******************************************************************************************/
    /** General Archive functions **/

    /**
     *  Serializes an object and then stores it in the archive.
     *  @param obj The object to be stored.
     *  @param entityT The entity of the object.
     */
    public void store(Object obj, EntityT entityT) {
        try {
            XmlEntityStruct xmlEntity = entitySerializer.serializeEntity(obj, entityT);
            operArchiveComp.update(xmlEntity);
        } catch(EntityException e){
            logger.log(Level.SEVERE,"SCHEDULING: "+ e.toString());
        } catch(ArchiveInternalError e) {
            logger.log(Level.SEVERE,"SCHEDULING: "+ e.toString());
        } catch(MalformedURI e) {
            logger.log(Level.SEVERE, "SCHEDULING: "+e.toString());
        } catch(InternalError e) {
            logger.log(Level.SEVERE, "SCHEDULING: "+e.toString());
        }
    }
    
    /**
     * Updates an object in the archive.
     * @param obj
     * @param entityT
     */
    public void update(Object obj, EntityT entityT) {
        try {
            XmlEntityStruct xmlEntity = entitySerializer.serializeEntity(obj, entityT);
            operArchiveComp.update(xmlEntity);
        } catch(EntityException e){
            logger.log(Level.SEVERE,"SCHEDULING: "+ e.toString());
        } catch(ArchiveInternalError e) {
            logger.log(Level.SEVERE, "SCHEDULING: "+e.toString());
        } catch(MalformedURI e) {
            logger.log(Level.SEVERE, "SCHEDULING: "+e.toString());
        } catch(InternalError e) {
            logger.log(Level.SEVERE, "SCHEDULING: "+e.toString());
        }
    }
    
    /**
     *  Retrieves the most recent version of the object from the archive.
     *  @param uri 
     *  @return XmlEntityStruct
     */
    public XmlEntityStruct retrieve(String uri) {
        XmlEntityStruct entity = null;
        try {
            entity = operArchiveComp.retrieve(uri, -1);
        } catch(ArchiveInternalError e) {
            logger.log(Level.SEVERE, "SCHEDULING: "+e.toString());
        } catch(NotFound e) {
            logger.log(Level.SEVERE, "SCHEDULING: "+e.toString());
        } catch(MalformedURI e) {
            logger.log(Level.SEVERE,"SCHEDULING: "+ e.toString());
        }
        return entity;
    }

    /**
     *  Retrieves the object with the indicated version from the archive
     *  @param uri 
     *  @param version If -1 then the most recent version is returned.
     *  @return XmlEntityStruct
     */
    public XmlEntityStruct retrieve(String uri, int version) {
        XmlEntityStruct entity = null;
        try {
            entity = operArchiveComp.retrieve(uri, version);
        } catch(ArchiveInternalError e) {
            logger.log(Level.SEVERE,"SCHEDULING: "+ e.toString());
        } catch(NotFound e) {
            logger.log(Level.SEVERE, "SCHEDULING: "+e.toString());
        } catch(MalformedURI e) {
            logger.log(Level.SEVERE,"SCHEDULING: "+ e.toString());
        }
        return entity;
    }
    
    /**
     * Removes an object from the archive
     * @param uri The unique identifier of the object to be deleted.
     */
    public void delete(String uri) {
        try {
        //set to false coz it apparantly doesn't do anything yet
            operArchiveComp.delete(uri, -1, false);
        } catch(ArchiveInternalError e) {
            logger.log(Level.SEVERE, "SCHEDULING: "+e.toString());
        } catch(NotFound e) {
            logger.log(Level.SEVERE, "SCHEDULING: "+e.toString());
        } catch(MalformedURI e) {
            logger.log(Level.SEVERE, "SCHEDULING: "+e.toString());
        }
    }

    /**
     *  Recovers a deleted object from the archive give the version
     *  @param uri The unique identifier of the object to be recovered.
     *  @param version The version of the object which will be recovered.
     */
    public void undelete(String uri, int version) {
        try {
            operArchiveComp.undelete(uri, version, false);
        } catch(ArchiveInternalError e) {
            logger.log(Level.SEVERE, "SCHEDULING: "+e.toString());
        } catch(NotFound e) {
            logger.log(Level.SEVERE, "SCHEDULING: "+e.toString());
        } catch(MalformedURI e) {
            logger.log(Level.SEVERE, "SCHEDULING: "+e.toString());
        }
     }
    
    /** 
     * Query the Archive for @param queryString 
     */
    public Vector query(String queryString, String schema) {
        Vector objects = new Vector();
        //Class className = Class.forName(schema);
        try {
            Cursor cursor = operArchiveComp.query(queryString, schema);
            while(cursor.hasNext()) {
                QueryResult result = cursor.next();
                //logger.info("xml string: "+result.xml);
                objects.add(  convertToObject(result, schema) );
            }
        } catch (ArchiveInternalError e) {
            logger.log(Level.SEVERE, "SCHEDULING: "+e.toString());
        }
        return objects;
    }

    /** Other required functions */

    private Object convertToObject(String xml, String className) {
        Object obj = null;
        try {
            obj = entityDeserializer.deserializeEntity(xml, Class.forName(className)); 
        } catch( EntityException e) {
            logger.log(Level.SEVERE, "SCHEDULING: "+e.toString());
        } catch(ClassNotFoundException e) {
            logger.log(Level.SEVERE, "SCHEDULING: "+e.toString());
        }
        return obj;
    }
    private Object convertToObject(QueryResult res, String className) {
        Object obj = null;
        try {
            obj = entityDeserializer.deserializeEntity(res.xml, Class.forName(className)); 
        } catch( EntityException e) {
            logger.log(Level.SEVERE, "SCHEDULING: "+e.toString());
        } catch(ClassNotFoundException e) {
            logger.log(Level.SEVERE, "SCHEDULING: "+e.toString());
        }
        return obj;
    }
    
    /**
     * Return all SBs that are non-complete
     * @return SchedBlock[]
     */
    public Vector getNonCompleteSBs() {
        Vector sbs = null;
        //SchedBlock sb;
        //SchedBlockEntityT sb_entity;
        logger.info("SCHEDULING: Getting noncomplete sbs from archive");
        String query = "/SchedBlock/ObsUnitControl[@schedStatus=\"waiting\"]";
        try {
            sbs = new Vector(); 
            Cursor cursor = operArchiveComp.query(query, "SchedBlock");
            while(cursor.hasNext()) {
                QueryResult result = cursor.next();
                sbs.add( (SchedBlock) convertToObject(result, "SchedBlock" ));
            }
        }catch (ArchiveInternalError e) {
            logger.log(Level.SEVERE,"SCHEDULING: "+ e.toString());
        }
        return sbs;
    }

    /**
     * Checks the archive to see if there are any new SchedBlocks.
     * True if there are new ones, false otherwise.
     *
     * @return boolean
     */
    public boolean checkNewSB() {
        boolean result;
        //query archive for new sbs
        // true if there are new sbs, else false
        result = false;
        logger.info("SCHEDULING: checking for new sbs");
        //get all sbs from archive. if count is more than current count 
        //then there are new ones!
        int currCount = sbCount;
        SchedBlock[] tmp = getSchedBlock();
        if(sbCount > currCount) {
            result = true;
        }
        return result;
    }

    /**
     * Checks the archive to see if there are any new Project defs.
     * True if there are new ones, false otherwise.
     *
     * @return boolean
     */
    public boolean checkNewProjectDefs() {
        boolean result;
        //query archive for new projects
        // true if there are new ones, else false
        result = false;
        logger.info("SCHEDULING: checking for new project defs");
        int currCount = projectCount;
        ObsProject[] tmp = getProject();
        if(projectCount > currCount) {
            result = true;
        }
        return result;
    }

    /**
     * Gets a list of the Scheduling Policy from the archive
     * @return ArrayList
     */
    public ArrayList getSchedPolicy() {
        ArrayList list = new ArrayList();
        System.out.println("SCHEDULING: get scheduling policy");
        return list;
    }

    /**
     * Gets a list of the antennas from the archive
     * @return ArrayList
     */
    public ArrayList getAntennas() {
        ArrayList list = new ArrayList();
        System.out.println("SCHEDULING: get antenna info from archive");
        return list;
    }
    
	public static void main(String[] args) {
	}
}
 
