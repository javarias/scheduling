/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
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
 * File PipelineProcessingRequest.java
 */
 
package alma.scheduling.Define;

import org.exolab.castor.xml.ValidationException;
import java.util.logging.Logger;
import java.util.logging.Level;

import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;
import alma.acs.entityutil.*;

import alma.entities.commonentity.EntityT;
import alma.entities.commonentity.EntityRefT;
import alma.entity.xmlbinding.pipelineprocessingrequest.*;
import alma.entity.xmlbinding.pipelineprocessingrequest.types.*;

/**
 * A wrapper class for the generated PipelineProcessingRequest
 * binding class and its corresponding generated classes.
 * 
 * NOTE: Since the ContainerServices object is not given to this class 
 *       the UID must be set whereever this class is created! And then
 *       the entity object must be reset in this object. It will be
 *       null otherwise!
 *
 * A PipelineProcessingRequest consists of the following entities:
 *  <ul>
 *    <li>Comment                         (String)
 *    <li>ImagingProcedureName            (String)
 *    <li>PipelineProcessingRequestEntity (EntityT)
 *    <li>ProjectReference                (EntityRefT)
 *    <li>ResultsReference                (EntityRefT)
 *    <li>RequestStatus                   (RequestStatusType)
 *    <li>CompletionStatus                (CompletionStatusType)
 *    <li>Reduction Units                 (ReductionUnitT)
 *  </ul>
 *  Details of the reduction units:
 *    A PipelineProcessingRequest has one Reduction Unit that is a  
 *  reference to an ObsUnitSet. It is an EntityRefT object.
 * 
 * @author Sohaila Roberts
 */
public class PipelineProcessingRequest {
    
    private alma.entity.xmlbinding.pipelineprocessingrequest.PipelineProcessingRequest ppr;
    
    ///////////////////////// Constructors ////////////////////////////////
    public PipelineProcessingRequest() throws ValidationException {
        ppr = new 
            alma.entity.xmlbinding.pipelineprocessingrequest.PipelineProcessingRequest();    
        
    }
    public PipelineProcessingRequest(alma.entity.xmlbinding.pipelineprocessingrequest.PipelineProcessingRequest xml) 
      throws ValidationException {
        xml.validate();
        ppr = xml;
    }
    
    //////////////Functions to return the binding objects //////////////////

    public alma.entity.xmlbinding.pipelineprocessingrequest.PipelineProcessingRequest
      getBindingObject() {
        
        if(ppr == null) {
            //This should never be a possibility but just in case!
            return ppr = new alma.entity.xmlbinding.pipelineprocessingrequest.PipelineProcessingRequest();
        }
        return ppr;
    }


    //////////////////// Create Methods ////////////////////////

    /**
     * Creates and returns a new PipelineProcessingRequestEntityT.
     * This entity is not set into the PipelineProcessingRequest because it 
     * does not have its uid yet. The uid must be set externally and then
     * manually set in to the request.
     * @return PipelineProcessingRequestEntityT
     */
    public PipelineProcessingRequestEntityT createEntity() {
        PipelineProcessingRequestEntityT entity = 
                new PipelineProcessingRequestEntityT();
        entity.setDocumentVersion("1.0");
        entity.setSchemaVersion("1.0");
        return entity;
    }
    
    
    //////////////////// Get Methods //////////////////////////
    
    /**
     * Returns the Comment associated with this PipelineProcessingRequest.
     * @return String The Comment.
     */
    public String getComment() {
        return ppr.getComment();
    }
    /** 
     * Returns the name of the Imaging Procedure for this PipelineProcessingRequest.
     * @return String The name.
     */
    public String getImagingProcedureName() {
        return ppr.getImagingProcedureName();
    }
    /**
     * Returns the Entity object for this PipelineProcessingRequest.
     * @return EntityT The entity object.
     */
    public EntityT getEntity() {
        return ppr.getPipelineProcessingRequestEntity();
    }
    /**
     * Returns the UID of this PipelineProcessingRequest.
     * @return String The unique identifier for this object.
     */
    public String getId(){
        return getEntity().getEntityId();
    }
    /**
     * Returns the document version.
     * @return String The document version in string form
     */
    public String getDocumentVersion() {
        return getEntity().getDocumentVersion();
    }
    /**
     * Returns the schema version.
     * @return String The schema version in string form
     */
    public String getSchemaVersion() {
        return getEntity().getSchemaVersion();
    }
    /**
     * Returns the EntityRefT object for the ProjectReference of this object.
     * @return EntityRefT The entity object.
     */
    public EntityRefT getProjectReference() {
        return ppr.getProjectReference();
    }
    /**
     * Returns the Id of the project reference.
     * @return String The Id.
     */
    public String getProjectReferenceId() {
        return getProjectReference().getEntityId();
    }
    /**
     *  Returns the type name of the project reference.
     * @return String The name of the project reference type.
     */
    public String getProjectReferenceTypeName() {
        return getProjectReference().getEntityTypeName();
    }
    /**
     * Returns the document version of the project reference.
     * @return String The document version.
     */
    public String getProjectReferenceDocumentVersion() {
        return getProjectReference().getDocumentVersion();
    }
    /**
     * Returns the EntityRefT object for the Results Reference of this object.
     * @return EntityRefT The entity object.
     */
    public EntityRefT getResultsReference() {
        return ppr.getResultsReference();
    }
    /**
     * Returns the Id of the results reference.
     * @return String The Id.
     */
    public String getResultsReferenceId() {
        return getResultsReference().getEntityId();
    }
    /**
     *  Returns the type name of the results reference.
     * @return String The name of the results reference type.
     */
    public String getResultsReferenceTypeName() {
        return getResultsReference().getEntityTypeName();
    }
    /**
     * Returns the document version of the results reference.
     * @return String The document version.
     */
    public String getResultsReferenceDocumentVersion() {
        return getResultsReference().getDocumentVersion();
    }
    /**
     * Returns the completion status of the request in the string
     * representation of the CompletionStatusT object. 
     */
    public String getCompletionStatusString() {
        return ppr.getCompletionStatus().getCompletionStatus().toString();
    }
    /**
     * Returns the completion status object.
     * @return CompletionStatusT
     */
    public CompletionStatusT getCompletionStatusObject() {
        return ppr.getCompletionStatus();
    }
    /**
     * Returns the status of the request in the string representation 
     * of the RequestStatusT object.
     * @return String
     */
    public String getRequestStatusString(){
        return ppr.getRequestStatus().getRequestStatus().toString();
    }
    /**
     * Returns the status of the request.
     * @return RequestStatusType
     */
    public RequestStatusType getRequestStatusObject() {
        return ppr.getRequestStatus().getRequestStatus();
    }
    /**
     * Gets the reduction unit for this request.
     * @return ReductionUnitT
     */
    public ReductionUnitT getReductionUnit() {
        return ppr.getReductionUnit();
    }
    /**
     * Returns 'ObsUnitSet'
     *
     * @return String A string with the value ObsUnitSet.
     */
    public String getReductionUnitName() {
        return getReductionUnit().getEntityTypeName();
    }
    /**
     * This returns the uid of the reduction unit. This uid is
     * the uid of the ObsUnitSet.
     * @return String The uid of the ObsUnitSet.
     */
    public String getReductionUnitId() {
        return getReductionUnit().getEntityId();
    }
    /**
     * Returns the version of the reduction unit document.
     * @return String The version.
     */
    public String getReductionUnitDocumentVersion() {
        return getReductionUnit().getDocumentVersion();
    }

    //////////////////// Set Methods //////////////////////////


    /**
     * Set the comment.
     * @param s The comment to be set.
     */
    public void setComment(String s) {
        ppr.setComment(s);
    }
    
    /**
     * Set the imaging procedure name.
     * @param s The name of the imaging procedure.
     */
    public void setImagingProcedureName(String s) {
        ppr.setImagingProcedureName(s);
    }
    
    /**
     * Set the entity. This should be done whenever this object is created.
     * It needs a uid which can only be set externally and is set in its
     * entity. 
     * @param entity
     */
    public void setEntity(EntityT entity) {
        ppr.setPipelineProcessingRequestEntity(entity);
    }
    /**
     * Set the encrypted string for the entity id.
     *
     * @param s The encrypted id.
     */
    public void setEncryptedId(String s) throws NullPointerException {
        if(s.equals("") || s == null) {
            throw new NullPointerException("Can not set a null id!");
        }
        getEntity().setEntityIdEncrypted(s);
    }
    /**
     * Set the uid of the PipelineProcessingRequest.
     * @param id The uid.
     * @throws Exception Throws an exception if there is already a uid set.
     */
    public void setId(String id) throws Exception {
        if(getId() == null) {
            getEntity().setEntityId(id);
        } else {
            throw new Exception("Can not change the UID!");
        }
    }
    /**
     * Sets the document version
     * @param s The version.
     * @throws Exception Throws exception if null
     */
    public void setDocumentVersion(String s) throws Exception{
        if(s == null) {
            throw new Exception("Can not set a null version!");
        }
        getEntity().setDocumentVersion(s);
    }
    /**
     * Sets the schema version
     * @param s The version.
     * @throws Exception Throws exception if null
     */
    public void setSchemaVersion(String s) throws Exception {
        if(s == null) {
            //throw an error
            throw new Exception("Can not set a null version!");
        }
        getEntity().setSchemaVersion(s);
    }   
    
    /**
     * Sets the Project Reference object. This is where the uid of the project
     * is stored.
     * @param ppr_projRef The entityRefT object of the project.
     */
    public void setProjectReference(EntityRefT ppr_projRef) {
        ppr.setProjectReference(ppr_projRef);
    }
    /**
     * Sets the uid of the project into the Project's reference object.
     * @param s The uid.
     */
    public void setProjectReferenceId(String s) {
        ppr.getProjectReference().setEntityId(s);
    }
    /**
     * Sets the name of the project.
     * @param s The name.
     */
    public void setProjectReferenceTypeName(String s) {
        ppr.getProjectReference().setEntityTypeName(s);
    }
    /**
     * Sets the version of the project's document.
     * @param s The document version
     */
    public void setProjectReferenceDocumentVersion(String s) {
        ppr.getProjectReference().setDocumentVersion(s);
    }   

    //PPR ResultsReference functions
    /**
     * Sets the results reference object.
     * @param ppr_resultsRef The EntityRefT object 
     */
    public void setResultsReference(EntityRefT ppr_resultsRef) {
        ppr.setResultsReference(ppr_resultsRef);
    }
    /**
     * Sets the uid of the results entity
     * @param s The uid.
     */
    public void setResultsReferenceId(String s) {
        ppr.getResultsReference().setEntityId(s);
    }
    /**
     * Sets the name of the results reference.
     * @param s The results reference type name.
     */
    public void setResultsReferenceTypeName(String s) {
        ppr.getResultsReference().setEntityTypeName(s);
    }
    /**
     * Sets the results reference document version. 
     * @param s The version
     */
    public void setResultsReferenceDocumentVersion(String s) {
        ppr.getResultsReference().setDocumentVersion(s);
    }   

    /**
     * Sets the request status in the form of a string.
     * Must be set using all lowercase letters.
     * ie: <ul>
     *      <li>queued
     *      <li>running
     *      <li>complete
     *     </ul>
     * @param s The status.
     * @throws IllegalArgumentException 
     */
    public void setRequestStatus(String s) throws IllegalArgumentException {
        RequestStatusT tmp = new RequestStatusT();
        if(s.equals("queued")){
            tmp.setRequestStatus(RequestStatusType.QUEUED);
            ppr.setRequestStatus(tmp);
        } else if(s.equals("running")) {
            tmp.setRequestStatus(RequestStatusType.RUNNING);
            ppr.setRequestStatus(tmp);
        } else if(s.equals("completed")) {
            tmp.setRequestStatus(RequestStatusType.COMPLETED);
            ppr.setRequestStatus(tmp);
        } else {
            throw new IllegalArgumentException("Not a valid status type");
        }
        
    }
    /**
     * Sets the status of the request.
     * @param s The RequestStatusType object.
     * @throws NullPointerException
     */
    public void setRequestStatus(RequestStatusType s) throws NullPointerException {
        if(s == null) {
            //throw an error, can't be null
            throw new NullPointerException("RequestStatus must not be null!");
        }
        RequestStatusT tmp = new RequestStatusT();
        tmp.setRequestStatus(s);
        ppr.setRequestStatus(tmp);
    }
    /**
     * Sets the completion status in the form of a string.
     * Must be set using all lowercase letters.
     * ie:<ul>
     *      <li>submitted
     *      <li>incomplete
     *      <li>complete-failed
     *      <li>complete-succeeded.
     *    </ul>
     * @param s The string representing the status
     * @throws IllegalArgumentException
     */
    public void setCompletionStatus(String s) throws IllegalArgumentException {
        CompletionStatusT tmp = new CompletionStatusT();
        if(s.equals("submitted")) {
            tmp.setCompletionStatus(CompletionStatusType.SUBMITTED);
            ppr.setCompletionStatus(tmp);
        } else if(s.equals("incomplete")){
            tmp.setCompletionStatus(CompletionStatusType.INCOMPLETE);
            ppr.setCompletionStatus(tmp);
        } else if(s.equals("complete-failed")){
            tmp.setCompletionStatus(CompletionStatusType.COMPLETE_FAILED);
            ppr.setCompletionStatus(tmp);
        } else if(s.equals("complete-succeeded")) {
            tmp.setCompletionStatus(CompletionStatusType.COMPLETE_SUCCEEDED);
            ppr.setCompletionStatus(tmp);
        } else {
            //throw an error, not a valid status!
            throw new IllegalArgumentException("Not a valid status type");
        }
    }
    /**
     * Sets the completion status. 
     * @param s The CompletionStatusType object.
     * @throws NullPointerException
     */
    public void setCompletionStatus(CompletionStatusType s) throws NullPointerException {
        if(s == null) {
            //throw an error, can't be null
            throw new NullPointerException("CompletionStatus must not be null!");
        }
        CompletionStatusT tmp = new CompletionStatusT();
        tmp.setCompletionStatus(s);
        ppr.setCompletionStatus(tmp);
    }

    /**
     * Since the reduction unit is always going to be an ObsUnitSet 
     * it is easier to just give the PipelineProcessingRequest the id 
     * of this obs unit set and let this wrapper class take care of 
     * the creation on the ReductionUnitT object! The functionality
     * to set that object is still there tho.
     */

    /**
     * Creates a Reduction unit with the given id. The document and schema
     * versions are set to 1.0 and the type name is fixed as ObsUnitSet
     * @param id The uid of the obsunitset
     */
    public void setReductionUnit(String id) {
        ReductionUnitT ru = new ReductionUnitT();
        EntityT entity = new EntityT();
        entity.setEntityId(id);
        entity.setDocumentVersion("1.0"); // dont know if this should be different?
        entity.setSchemaVersion("1.0");
        entity.setEntityTypeName("ObsUnitSet");
    }
    /**
     * To make life more coherent this function was added but does the same thing 
     * as the one above!.
     * @param id The uid of the obsunitset
     */ 
    public void setObsUnitSet(String id) {
        setReductionUnit(id);
    }
    /**
     * Sets the reduction unit for this request.
     * @param ru The ReductionUnitT
     */
    public void setReductionUnit(ReductionUnitT ru) throws NullPointerException {
        if(ru == null) {
            throw new NullPointerException("Cannot set a null ReductionUnit!");
        }
        ppr.setReductionUnit(ru);
    }
    
    /** 
     * Sets the uid if the reduction unit. Technically this should be set before the
     * reduction unit is set.
     * @param s The uid of the reduction unit.
     */
    public void setReductionUnitId(String s) {
        getReductionUnit().setEntityId(s);
    }
    /**
     * Sets the document version of the reduction unit.
     * @param s The version.
     */
    public void setReductionUnitDocumentVersion(String s) {
        getReductionUnit().setDocumentVersion(s);
    }
    ///////////////////////////// Other functions ////////////////////////////////

    /**
     * Checks to see if the object is valid. It basically calls the validate 
     * function on the xmlbinding object.
     *
     * @return boolean True if its valid, false if its not.
     */
    public boolean isValid() {
        return ppr.isValid();
    }

    public static void main(String[] args) {
        //try {
            System.out.println("SCHEDULING: function PipelineProcessingRequest()");
            alma.scheduling.Define.PipelineProcessingRequest ppr1=null; 
            try {
                ppr1=new alma.scheduling.Define.PipelineProcessingRequest();
            } catch(Exception e) {
                System.out.println("SCHEDULING: error="+e.toString());
            }
            System.out.println("SCHEDULING: function createEntity()");
            System.out.println("SCHEDULING: function setEntity()");
            ppr1.setEntity(ppr1.createEntity());
            System.out.println("SCHEDULING: function setComment(string)");
            ppr1.setComment("This is a test");
            System.out.println("SCHEDULING: function getComment()");
            System.out.println("SCHEDULING: comment = "+ ppr1.getComment());
            System.out.println("SCHEDULING: function setImagingProcedureName(string)");
            ppr1.setImagingProcedureName("test");
            System.out.println("SCHEDULING: function getImagingProcedureName()");
            System.out.println("SCHEDULING: Image procedure name = "+ppr1.getImagingProcedureName());
            System.out.println("SCHEDULING: function setEncryptedId(string)");
            try {
                ppr1.setEncryptedId("Something encrypted here!");
            } catch(NullPointerException e) {
                System.out.println("SCHEDULING: error="+e.toString());
            }
            System.out.println("SCHEDULING: function setId() with 'ppr1 id'");
            try {
                ppr1.setId("ppr1 id");
            } catch (Exception ex) {
                System.out.println("SCHEDULING: error="+ex.toString());
            }
            System.out.println("SCHEDULING: function setId() 'another id'");
            System.out.println("SCHEDULING: should get error coz we've already set the id");
            try { 
                ppr1.setId("another id");
            } catch (Exception ex) {
                System.out.println("SCHEDULING: error="+ex.toString());
            }
            System.out.println("SCHEDULING: function getId()");
            System.out.println("SCHEDULING: ppr1's id ="+ppr1.getId());
            System.out.println("SCHEDULING: function setDocumentVersion(string)");
            try {
                ppr1.setDocumentVersion("1.0");
            } catch(Exception e) {
                System.out.println("SCHEDULING: error="+e.toString());
            }
            System.out.println("SCHEDULING: function getDocumentVersion()");
            System.out.println("SCHEDULING: version="+ppr1.getDocumentVersion());
            System.out.println("SCHEDULING: function setSchemaVersion(string)");
            try {
                ppr1.setSchemaVersion("1.0");
            } catch(Exception e) {
                System.out.println("SCHEDULING: error="+e.toString());
            }
            System.out.println("SCHEDULING: function getSchemaVersion()");
            System.out.println("SCHEDULING: version="+ppr1.getSchemaVersion());
            System.out.println("SCHEDULING: function setProjectReference(EntityT)");
            EntityRefT p_ent_ref = new EntityRefT();
            ppr1.setProjectReference(p_ent_ref);
            System.out.println("SCHEDULING: function setProjectReferenceId(string)");
            ppr1.setProjectReferenceId("proj id");
            System.out.println("SCHEDULING: function setProjectReferenceTypeName(string)");
            ppr1.setProjectReferenceTypeName("TestProjectName");
            System.out.println("SCHEDULING: function getProjectReferenceTypeName()");
            System.out.println("SCHEDULING: name="+ ppr1.getProjectReferenceTypeName());
            System.out.println("SCHEDULING: function setProjectReferenceDocumentVersion(string)");
            ppr1.setProjectReferenceDocumentVersion("1.0");
            System.out.println("SCHEDULING: function getProjectReferenceDocumentVersion()");
            System.out.println("SCHEDULING: name="+ ppr1.getProjectReferenceDocumentVersion());
            //System.out.println("SCHEDULING: function setProjectReferenceSchemaVersion(string)");
            //ppr1.setProjectReferenceSchemaVersion("1.0");
            //System.out.println("SCHEDULING: function getProjectReferenceSchemaVersion()");
            //System.out.println("SCHEDULING: name="+ ppr1.getProjectReferenceSchemaVersion());
           

            //ResultsReference stuff..

            System.out.println("SCHEDULING: function setRequestStatus(string) with 'queued'");
            ppr1.setRequestStatus("queued");
            System.out.println("SCHEDULING: getRequestStatusString()");
            System.out.println("SCHEDULING: status="+ppr1.getRequestStatusString());
            
            System.out.println("SCHEDULING: function setRequestStatus(string) with 'running'");
            ppr1.setRequestStatus("running");
            System.out.println("SCHEDULING: getRequestStatusString()");
            System.out.println("SCHEDULING: status="+ppr1.getRequestStatusString());

            System.out.println("SCHEDULING: function setRequestStatus(string) with 'completed'");
            ppr1.setRequestStatus("completed");
            System.out.println("SCHEDULING: getRequestStatusString()");
            System.out.println("SCHEDULING: status="+ppr1.getRequestStatusString());

            System.out.println("SCHEDULING: function setCompletionStatus(string) with 'submitted'");
            ppr1.setCompletionStatus("submitted");
            System.out.println("SCHEDULING: getCompletionStatusString()");
            System.out.println("SCHEDULING: status="+ppr1.getCompletionStatusString());

            System.out.println("SCHEDULING: function setCompletionStatus(string) with 'incomplete'");
            ppr1.setCompletionStatus("incomplete");
            System.out.println("SCHEDULING: getCompletionStatusString()");
            System.out.println("SCHEDULING: status="+ppr1.getCompletionStatusString());

            System.out.println("SCHEDULING: function setCompletionStatus(string) with 'complete-failed'");
            ppr1.setCompletionStatus("complete-failed");
            System.out.println("SCHEDULING: getCompletionStatusString()");
            System.out.println("SCHEDULING: status="+ppr1.getCompletionStatusString());

            System.out.println("SCHEDULING: function setCompletionStatus(string) with 'complete-succeeded'");
            ppr1.setCompletionStatus("complete-succeeded");
            System.out.println("SCHEDULING: getCompletionStatusString()");
            System.out.println("SCHEDULING: status="+ppr1.getCompletionStatusString());
    }
    
}
