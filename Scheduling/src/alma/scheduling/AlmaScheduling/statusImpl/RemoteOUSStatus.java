/*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2009
 * (c) Associated Universities Inc., 2009
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
 * File ALMAOUSStatus.java
 * $Id: RemoteOUSStatus.java,v 1.2 2009/11/09 22:58:45 rhiriart Exp $
 */
package alma.scheduling.AlmaScheduling.statusImpl;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Vector;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.ContentHandler;

import alma.acs.entityutil.EntityException;
import alma.alarmsystem.source.ACSFaultState;
import alma.entity.xmlbinding.obsproject.ObsProjectRefT;
import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.entity.xmlbinding.ousstatus.OUSStatusChoice;
import alma.entity.xmlbinding.ousstatus.OUSStatusEntityT;
import alma.entity.xmlbinding.ousstatus.OUSStatusRefT;
import alma.entity.xmlbinding.ousstatus.PipelineProcessingRequestT;
import alma.entity.xmlbinding.ousstatus.SessionT;
import alma.entity.xmlbinding.projectstatus.ProjectStatusEntityT;
import alma.entity.xmlbinding.projectstatus.ProjectStatusRefT;
import alma.entity.xmlbinding.sbstatus.SBStatusEntityT;
import alma.entity.xmlbinding.sbstatus.SBStatusRefT;
import alma.entity.xmlbinding.valuetypes.StatusT;
import alma.scheduling.AlmaScheduling.statusIF.OUSStatusI;
import alma.scheduling.AlmaScheduling.statusIF.ProjectStatusI;
import alma.scheduling.AlmaScheduling.statusIF.SBStatusI;
import alma.scheduling.Define.SchedulingException;
import alma.statearchiveexceptions.InappropriateEntityTypeEx;
import alma.statearchiveexceptions.NoSuchEntityEx;
import alma.statearchiveexceptions.NullEntityIdEx;
import alma.xmlentity.XmlEntityStruct;

/**
 * A <code>Proxy</code> for an <code>OUSStatus</code> which handles all
 * the connection to the State System.
 * @author dclarke
 */
public class RemoteOUSStatus extends RemoteStatusBase implements OUSStatusI {
	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	public RemoteOUSStatus(String uid) throws SchedulingException {
		super(uid);
		checkEntity(); // Force a check that it exists
	}
	/*
	 * End of Construction
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Behaviour required by abstract superclass
	 * ================================================================
	 */
	/** Somewhere to hold our status entity until we write it */
	private OUSStatus cache;
	
	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ALMAStatusEntity#checkEntity()
	 */
	@Override
	protected void checkEntity() throws SchedulingException {
		XmlEntityStruct xml = null;
		cache = null;

		try {
			xml = stateSystem.getOUSStatus(uid);
			cache = (OUSStatus)entityDeserializer.deserializeEntity(xml, OUSStatus.class); 
		} catch (InappropriateEntityTypeEx e) {
			throw new SchedulingException(String.format(
					"Error retrieving OUSStatus entity %s - entity is not an OUSStatus", getUID()),
					e);
		} catch (NullEntityIdEx e) {
			throw new SchedulingException(String.format(
					"Error retrieving OUSStatus entity %s - entity id is null", getUID()),
					e);
		} catch (NoSuchEntityEx e) {
			throw new SchedulingException(String.format(
					"Error retrieving OUSStatus entity %s - no such entity found", getUID()),
					e);
		} catch (EntityException e) {
			if (xml == null) {
				throw new SchedulingException(String.format(
						"Error retrieving OUSStatus entity %s - returned XML is null",
						getUID()),
						e);
			} else if (xml.xmlString == null) {
				throw new SchedulingException(String.format(
						"Error retrieving OUSStatus entity %s - returned XML has null string",
						getUID()),
						e);
			} else {
				final int end = (xml.xmlString.length() > 255)?
																255:
																xml.xmlString.length();
				throw new SchedulingException(String.format(
						"Error retrieving OUSStatus entity %s - cannot deserialise the returned XML%n\t%s",
						getUID(), xml.xmlString.substring(0, end)),
						e);
			}
		} catch (Exception e) {
			throw new SchedulingException(String.format(
					"Error retrieving OUSStatus entity %s", getUID()),
					e);
		}

	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ALMAStatusEntity#getEntity()
	 */
	@Override
	protected OUSStatus getEntity() {
        cache = null;
        try {
            XmlEntityStruct xml = stateSystem.getOUSStatus(uid);

            cache = (OUSStatus)entityDeserializer.deserializeEntity(xml, OUSStatus.class); 
        } catch(Exception e) {
//            e.printStackTrace(System.out);
            sendArchiveAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
            //send to alarm system take some time, throw any Exception immediately will result in 
            //send Alarm to alarm system failure. so we do a delay for one second to wait alarm is send.
            try {
            	Thread.sleep(1000);
            } catch (InterruptedException e1) {
            	e1.printStackTrace(System.out);
            }
        }
        return cache;
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ALMAStatusEntity#putEntity()
	 */
	@Override
	protected void putEntity() {
    	try {
    		XmlEntityStruct xml = entitySerializer.serializeEntity(cache, cache.getOUSStatusEntity());
    		stateSystem.updateOUSStatus(xml);
    	} catch(Exception e){
    		logger.severe("SCHEDULING: error updating OUSStatus in archive, "+e.toString());
    		e.printStackTrace(System.out);
    		//throw new SchedulingException (e);
    	}
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.statusIF.StatusBaseI#getDomainEntityId()
	 */
	public String getDomainEntityId() {
		return getObsUnitSetRef().getEntityId();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.statusIF.StatusBaseI#getDomainEntityId()
	 */
	public String getDomainPartId() {
		return getObsUnitSetRef().getPartId();
	}

	/* (non-Javadoc)
     * @see alma.scheduling.AlmaScheduling.status.ALMAStatusEntity#asLocal()
     */
    public OUSStatusI asLocal() {
    	return new CachedOUSStatus(getEntity());
    }
    
    /* (non-Javadoc)
     * @see alma.scheduling.AlmaScheduling.status.ALMAStatusEntity#asRemote()
     */
    public OUSStatusI asRemote() {
    	return this;
    }
	/*
	 * End of Behaviour required by abstract superclass
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Being a proxy
	 * ================================================================
	 */
	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#addSession(int, alma.entity.xmlbinding.ousstatus.SessionT)
	 */
	public void addSession(int index, SessionT vSession) throws IndexOutOfBoundsException {
		getEntity();
		cache.addSession(index, vSession);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#addSession(alma.entity.xmlbinding.ousstatus.SessionT)
	 */
	public void addSession(SessionT vSession) throws IndexOutOfBoundsException {
		getEntity();
		cache.addSession(vSession);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#clearSession()
	 */
	public void clearSession() {
		getEntity();
		cache.clearSession();
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#deleteNumberObsUnitSetsCompleted()
	 */
	public void deleteNumberObsUnitSetsCompleted() {
		getEntity();
		cache.deleteNumberObsUnitSetsCompleted();
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#deleteNumberObsUnitSetsFailed()
	 */
	public void deleteNumberObsUnitSetsFailed() {
		getEntity();
		cache.deleteNumberObsUnitSetsFailed();
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#deleteNumberSBsCompleted()
	 */
	public void deleteNumberSBsCompleted() {
		getEntity();
		cache.deleteNumberSBsCompleted();
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#deleteNumberSBsFailed()
	 */
	public void deleteNumberSBsFailed() {
		getEntity();
		cache.deleteNumberSBsFailed();
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#deleteTotalObsUnitSets()
	 */
	public void deleteTotalObsUnitSets() {
		getEntity();
		cache.deleteTotalObsUnitSets();
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#deleteTotalRequiredTimeInSec()
	 */
	public void deleteTotalRequiredTimeInSec() {
		getEntity();
		cache.deleteTotalRequiredTimeInSec();
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#deleteTotalSBs()
	 */
	public void deleteTotalSBs() {
		getEntity();
		cache.deleteTotalSBs();
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#deleteTotalUsedTimeInSec()
	 */
	public void deleteTotalUsedTimeInSec() {
		getEntity();
		cache.deleteTotalUsedTimeInSec();
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#enumerateSession()
	 */
	public Enumeration enumerateSession() {
		getEntity();
		return cache.enumerateSession();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		getEntity();
		return cache.equals(o);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#getAlmatype()
	 */
	public String getAlmatype() {
		getEntity();
		return cache.getAlmatype();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#getContainingObsUnitSetRef()
	 */
	public OUSStatusRefT getContainingObsUnitSetRef() {
		getEntity();
		return cache.getContainingObsUnitSetRef();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#getNumberObsUnitSetsCompleted()
	 */
	public int getNumberObsUnitSetsCompleted() {
		getEntity();
		return cache.getNumberObsUnitSetsCompleted();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#getNumberObsUnitSetsFailed()
	 */
	public int getNumberObsUnitSetsFailed() {
		getEntity();
		return cache.getNumberObsUnitSetsFailed();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#getNumberSBsCompleted()
	 */
	public int getNumberSBsCompleted() {
		getEntity();
		return cache.getNumberSBsCompleted();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#getNumberSBsFailed()
	 */
	public int getNumberSBsFailed() {
		getEntity();
		return cache.getNumberSBsFailed();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#getObsUnitSetMemberType()
	 */
	public String getObsUnitSetMemberType() {
		getEntity();
		return cache.getObsUnitSetMemberType();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#getObsUnitSetRef()
	 */
	public ObsProjectRefT getObsUnitSetRef() {
		getEntity();
		return cache.getObsUnitSetRef();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#getOUSStatusChoice()
	 */
	public OUSStatusChoice getOUSStatusChoice() {
		getEntity();
		return cache.getOUSStatusChoice();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#getOUSStatusEntity()
	 */
	public OUSStatusEntityT getOUSStatusEntity() {
		getEntity();
		return cache.getOUSStatusEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#getPipelineProcessingRequest()
	 */
	public PipelineProcessingRequestT getPipelineProcessingRequest() {
		getEntity();
		return cache.getPipelineProcessingRequest();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#getProjectStatusRef()
	 */
	public ProjectStatusRefT getProjectStatusRef() {
		getEntity();
		return cache.getProjectStatusRef();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#getRevision()
	 */
	public String getRevision() {
		getEntity();
		return cache.getRevision();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#getSchemaVersion()
	 */
	public String getSchemaVersion() {
		getEntity();
		return cache.getSchemaVersion();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#getSession()
	 */
	public SessionT[] getSession() {
		getEntity();
		return cache.getSession();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#getSession(int)
	 */
	public SessionT getSession(int index) throws IndexOutOfBoundsException {
		getEntity();
		return cache.getSession(index);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#getSessionCount()
	 */
	public int getSessionCount() {
		getEntity();
		return cache.getSessionCount();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#getStatus()
	 */
	public StatusT getStatus() {
		getEntity();
		return cache.getStatus();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#getTimeOfUpdate()
	 */
	public String getTimeOfUpdate() {
		getEntity();
		return cache.getTimeOfUpdate();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#getTotalObsUnitSets()
	 */
	public int getTotalObsUnitSets() {
		getEntity();
		return cache.getTotalObsUnitSets();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#getTotalRequiredTimeInSec()
	 */
	public int getTotalRequiredTimeInSec() {
		getEntity();
		return cache.getTotalRequiredTimeInSec();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#getTotalSBs()
	 */
	public int getTotalSBs() {
		getEntity();
		return cache.getTotalSBs();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#getTotalUsedTimeInSec()
	 */
	public int getTotalUsedTimeInSec() {
		getEntity();
		return cache.getTotalUsedTimeInSec();
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		getEntity();
		return cache.hashCode();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#hasNumberObsUnitSetsCompleted()
	 */
	public boolean hasNumberObsUnitSetsCompleted() {
		getEntity();
		return cache.hasNumberObsUnitSetsCompleted();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#hasNumberObsUnitSetsFailed()
	 */
	public boolean hasNumberObsUnitSetsFailed() {
		getEntity();
		return cache.hasNumberObsUnitSetsFailed();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#hasNumberSBsCompleted()
	 */
	public boolean hasNumberSBsCompleted() {
		getEntity();
		return cache.hasNumberSBsCompleted();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#hasNumberSBsFailed()
	 */
	public boolean hasNumberSBsFailed() {
		getEntity();
		return cache.hasNumberSBsFailed();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#hasTotalObsUnitSets()
	 */
	public boolean hasTotalObsUnitSets() {
		getEntity();
		return cache.hasTotalObsUnitSets();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#hasTotalRequiredTimeInSec()
	 */
	public boolean hasTotalRequiredTimeInSec() {
		getEntity();
		return cache.hasTotalRequiredTimeInSec();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#hasTotalSBs()
	 */
	public boolean hasTotalSBs() {
		getEntity();
		return cache.hasTotalSBs();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#hasTotalUsedTimeInSec()
	 */
	public boolean hasTotalUsedTimeInSec() {
		getEntity();
		return cache.hasTotalUsedTimeInSec();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#isValid()
	 */
	public boolean isValid() {
		getEntity();
		return cache.isValid();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#marshal(org.xml.sax.ContentHandler)
	 */
	public void marshal(ContentHandler handler) throws IOException, MarshalException, ValidationException {
		getEntity();
		cache.marshal(handler);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#marshal(java.io.Writer)
	 */
	public void marshal(Writer out) throws MarshalException, ValidationException {
		getEntity();
		cache.marshal(out);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#removeSession(alma.entity.xmlbinding.ousstatus.SessionT)
	 */
	public boolean removeSession(SessionT vSession) {
		getEntity();
		final boolean result = cache.removeSession(vSession);
		putEntity();
		return result;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#setAlmatype(java.lang.String)
	 */
	public void setAlmatype(String almatype) {
		getEntity();
		cache.setAlmatype(almatype);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#setContainingObsUnitSetRef(alma.entity.xmlbinding.ousstatus.OUSStatusRefT)
	 */
	public void setContainingObsUnitSetRef(OUSStatusRefT containingObsUnitSetRef) {
		getEntity();
		cache.setContainingObsUnitSetRef(containingObsUnitSetRef);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#setNumberObsUnitSetsCompleted(int)
	 */
	public void setNumberObsUnitSetsCompleted(int numberObsUnitSetsCompleted) {
		getEntity();
		cache.setNumberObsUnitSetsCompleted(numberObsUnitSetsCompleted);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#setNumberObsUnitSetsFailed(int)
	 */
	public void setNumberObsUnitSetsFailed(int numberObsUnitSetsFailed) {
		getEntity();
		cache.setNumberObsUnitSetsFailed(numberObsUnitSetsFailed);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#setNumberSBsCompleted(int)
	 */
	public void setNumberSBsCompleted(int numberSBsCompleted) {
		getEntity();
		cache.setNumberSBsCompleted(numberSBsCompleted);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#setNumberSBsFailed(int)
	 */
	public void setNumberSBsFailed(int numberSBsFailed) {
		getEntity();
		cache.setNumberSBsFailed(numberSBsFailed);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#setObsUnitSetMemberType(java.lang.String)
	 */
	public void setObsUnitSetMemberType(String obsUnitSetMemberType) {
		getEntity();
		cache.setObsUnitSetMemberType(obsUnitSetMemberType);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#setObsUnitSetRef(alma.entity.xmlbinding.obsproject.ObsProjectRefT)
	 */
	public void setObsUnitSetRef(ObsProjectRefT obsUnitSetRef) {
		getEntity();
		cache.setObsUnitSetRef(obsUnitSetRef);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#setOUSStatusChoice(alma.entity.xmlbinding.ousstatus.OUSStatusChoice)
	 */
	public void setOUSStatusChoice(OUSStatusChoice OUSStatusChoice) {
		getEntity();
		cache.setOUSStatusChoice(OUSStatusChoice);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#setOUSStatusEntity(alma.entity.xmlbinding.ousstatus.OUSStatusEntityT)
	 */
	public void setOUSStatusEntity(OUSStatusEntityT OUSStatusEntity) {
		getEntity();
		cache.setOUSStatusEntity(OUSStatusEntity);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#setPipelineProcessingRequest(alma.entity.xmlbinding.ousstatus.PipelineProcessingRequestT)
	 */
	public void setPipelineProcessingRequest(PipelineProcessingRequestT pipelineProcessingRequest) {
		getEntity();
		cache.setPipelineProcessingRequest(pipelineProcessingRequest);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#setProjectStatusRef(alma.entity.xmlbinding.projectstatus.ProjectStatusRefT)
	 */
	public void setProjectStatusRef(ProjectStatusRefT projectStatusRef) {
		getEntity();
		cache.setProjectStatusRef(projectStatusRef);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#setRevision(java.lang.String)
	 */
	public void setRevision(String revision) {
		getEntity();
		cache.setRevision(revision);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#setSchemaVersion(java.lang.String)
	 */
	public void setSchemaVersion(String schemaVersion) {
		getEntity();
		cache.setSchemaVersion(schemaVersion);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#setSession(int, alma.entity.xmlbinding.ousstatus.SessionT)
	 */
	public void setSession(int index, SessionT vSession) throws IndexOutOfBoundsException {
		getEntity();
		cache.setSession(index, vSession);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#setSession(alma.entity.xmlbinding.ousstatus.SessionT[])
	 */
	public void setSession(SessionT[] sessionArray) {
		getEntity();
		cache.setSession(sessionArray);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#setStatus(alma.entity.xmlbinding.valuetypes.StatusT)
	 */
	public void setStatus(StatusT status) {
		getEntity();
		cache.setStatus(status);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#setTimeOfUpdate(java.lang.String)
	 */
	public void setTimeOfUpdate(String timeOfUpdate) {
		getEntity();
		cache.setTimeOfUpdate(timeOfUpdate);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#setTotalObsUnitSets(int)
	 */
	public void setTotalObsUnitSets(int totalObsUnitSets) {
		getEntity();
		cache.setTotalObsUnitSets(totalObsUnitSets);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#setTotalRequiredTimeInSec(int)
	 */
	public void setTotalRequiredTimeInSec(int totalRequiredTimeInSec) {
		getEntity();
		cache.setTotalRequiredTimeInSec(totalRequiredTimeInSec);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#setTotalSBs(int)
	 */
	public void setTotalSBs(int totalSBs) {
		getEntity();
		cache.setTotalSBs(totalSBs);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#setTotalUsedTimeInSec(int)
	 */
	public void setTotalUsedTimeInSec(int totalUsedTimeInSec) {
		getEntity();
		cache.setTotalUsedTimeInSec(totalUsedTimeInSec);
		putEntity();
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		getEntity();
		return cache.toString();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.OUSStatusI#validate()
	 */
	public void validate() throws ValidationException {
		getEntity();
		cache.validate();
	}
	/*
	 * End of Being a proxy
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Methods with entity resolution
	 * ================================================================
	 */
	public OUSStatusI getContainingObsUnitSet() throws SchedulingException {
		final OUSStatusRefT ref = getContainingObsUnitSetRef();
		OUSStatusI result = null;
		if (ref != null) {
			result = getFactory().createOUSStatus(ref.getEntityId());
		}
		return result;
	}

	public ProjectStatusI getProjectStatus() throws SchedulingException {
		final ProjectStatusRefT ref = getProjectStatusRef();
		ProjectStatusI result = null;
		if (ref != null) {
			result = getFactory().createProjectStatus(ref.getEntityId());
		}
		return result;
	}

	public void setContainingObsUnitSet(OUSStatusI containingObsUnitSet) {
		if (containingObsUnitSet != null) {
			final OUSStatusRefT    ref = new OUSStatusRefT();
			final OUSStatusEntityT ent = containingObsUnitSet.getOUSStatusEntity();
			ref.setDocumentVersion(ent.getDocumentVersion());
			ref.setEntityId(ent.getEntityId());
			setContainingObsUnitSetRef(ref);
		} else {
			setContainingObsUnitSetRef(null);
		}
	}

	public void setProjectStatus(ProjectStatusI projectStatus) {
		if (projectStatus != null) {
			final ProjectStatusRefT    ref = new ProjectStatusRefT();
			final ProjectStatusEntityT ent = projectStatus.getProjectStatusEntity();
			ref.setDocumentVersion(ent.getDocumentVersion());
			ref.setEntityId(ent.getEntityId());
			setProjectStatusRef(ref);
		} else {
			setProjectStatusRef(null);
		}
	}

	
	public void addOUSStatus(int index, OUSStatusI vOUSStatus) throws IndexOutOfBoundsException {
		if (vOUSStatus != null) {
			final OUSStatusRefT    ref = new OUSStatusRefT();
			final OUSStatusEntityT ent = vOUSStatus.getOUSStatusEntity();
			ref.setDocumentVersion(ent.getDocumentVersion());
			ref.setEntityId(ent.getEntityId());
			getOUSStatusChoice().addOUSStatusRef(index, ref);
		} else {
			getOUSStatusChoice().addOUSStatusRef(index, null);
		}
	}


	public void addOUSStatus(OUSStatusI vOUSStatus) throws IndexOutOfBoundsException {
		if (vOUSStatus != null) {
			final OUSStatusRefT    ref = new OUSStatusRefT();
			final OUSStatusEntityT ent = vOUSStatus.getOUSStatusEntity();
			ref.setDocumentVersion(ent.getDocumentVersion());
			ref.setEntityId(ent.getEntityId());
			getOUSStatusChoice().addOUSStatusRef(ref);
		} else {
			getOUSStatusChoice().addOUSStatusRef(null);
		}
	}


	public void addSBStatus(int index, SBStatusI vSBStatus) throws IndexOutOfBoundsException {
		if (vSBStatus != null) {
			final SBStatusRefT    ref = new SBStatusRefT();
			final SBStatusEntityT ent = vSBStatus.getSBStatusEntity();
			ref.setDocumentVersion(ent.getDocumentVersion());
			ref.setEntityId(ent.getEntityId());
			getOUSStatusChoice().addSBStatusRef(index, ref);
		} else {
			getOUSStatusChoice().addSBStatusRef(index, null);
		}
	}


	public void addSBStatus(SBStatusI vSBStatus) throws IndexOutOfBoundsException {
		if (vSBStatus != null) {
			final SBStatusRefT    ref = new SBStatusRefT();
			final SBStatusEntityT ent = vSBStatus.getSBStatusEntity();
			ref.setDocumentVersion(ent.getDocumentVersion());
			ref.setEntityId(ent.getEntityId());
			getOUSStatusChoice().addSBStatusRef(ref);
		} else {
			getOUSStatusChoice().addSBStatusRef(null);
		}
	}


	public void clearOUSStatus() {
		getOUSStatusChoice().clearOUSStatusRef();
	}


	public void clearSBStatus() {
		getOUSStatusChoice().clearSBStatusRef();
	}


	public Enumeration<OUSStatusI> enumerateOUSStatus() throws IndexOutOfBoundsException, SchedulingException {
		final Vector<OUSStatusI> v = new Vector<OUSStatusI>();
		for (final OUSStatusI status : getOUSStatus()) {
			v.add(status);
		}
		return v.elements();
	}


	public Enumeration<SBStatusI> enumerateSBStatus() throws IndexOutOfBoundsException, SchedulingException {
		final Vector<SBStatusI> v = new Vector<SBStatusI>();
		for (final SBStatusI status : getSBStatus()) {
			v.add(status);
		}
		return v.elements();
	}


	public OUSStatusI[] getOUSStatus() throws IndexOutOfBoundsException, SchedulingException {
		final OUSStatusI[] result = new OUSStatusI[getOUSStatusChoice().getOUSStatusRefCount()];
		
		for (int i = 0; i < result.length; i++) {
			result[i] = getOUSStatus(i);
		}
		return result;
	}


	public OUSStatusI getOUSStatus(int index) throws IndexOutOfBoundsException, SchedulingException {
		final OUSStatusRefT ref = getOUSStatusChoice().getOUSStatusRef(index);
		OUSStatusI result = null;
		if (ref != null) {
			result = getFactory().createOUSStatus(ref.getEntityId());
		}
		return result;
	}


	public int getOUSStatusCount() {
		return getOUSStatusChoice().getOUSStatusRefCount();
	}


	public SBStatusI[] getSBStatus() throws IndexOutOfBoundsException, SchedulingException {
		final SBStatusI[] result = new SBStatusI[getOUSStatusChoice().getSBStatusRefCount()];
		
		for (int i = 0; i < result.length; i++) {
			result[i] = getSBStatus(i);
		}
		return result;
	}


	public SBStatusI getSBStatus(int index) throws IndexOutOfBoundsException, SchedulingException {
		final SBStatusRefT ref = getOUSStatusChoice().getSBStatusRef(index);
		SBStatusI result = null;
		if (ref != null) {
			result = getFactory().createSBStatus(ref.getEntityId());
		}
		return result;
	}


	public int getSBStatusCount() {
		return getOUSStatusChoice().getSBStatusRefCount();
	}


	public boolean removeOUSStatus(OUSStatusI vOUSStatus) {
		final OUSStatusRefT[] statuses = getOUSStatusChoice().getOUSStatusRef();
		OUSStatusRefT toRemove = null;
		
		for (int i = 0; i < statuses.length; i++) {
			if (vOUSStatus.getUID().equals(statuses[i].getEntityId())) {
				toRemove = statuses[i];
			}
		}
		
		if (toRemove != null) {
			return getOUSStatusChoice().removeOUSStatusRef(toRemove);
		}
		
		return false;
	}


	public boolean removeSBStatus(SBStatusI vSBStatus) {
		final SBStatusRefT[] statuses = getOUSStatusChoice().getSBStatusRef();
		SBStatusRefT toRemove = null;
		
		for (int i = 0; i < statuses.length; i++) {
			if (vSBStatus.getUID().equals(statuses[i].getEntityId())) {
				toRemove = statuses[i];
			}
		}
		
		if (toRemove != null) {
			return getOUSStatusChoice().removeSBStatusRef(toRemove);
		}
		
		return false;
	}


	public void setOUSStatus(int index, OUSStatusI vOUSStatus) throws IndexOutOfBoundsException {
		if (vOUSStatus != null) {
			final OUSStatusRefT    ref = new OUSStatusRefT();
			final OUSStatusEntityT ent = vOUSStatus.getOUSStatusEntity();
			ref.setDocumentVersion(ent.getDocumentVersion());
			ref.setEntityId(ent.getEntityId());
			getOUSStatusChoice().setOUSStatusRef(index, ref);
		} else {
			getOUSStatusChoice().setOUSStatusRef(index, null);
		}
	}


	public void setOUSStatus(OUSStatusI[] OUSStatusArray) {
		clearOUSStatus();
		for (final OUSStatusI s : OUSStatusArray) {
			addOUSStatus(s);
		}
	}


	public void setSBStatus(int index, SBStatusI vSBStatus) throws IndexOutOfBoundsException {
		if (vSBStatus != null) {
			final SBStatusRefT    ref = new SBStatusRefT();
			final SBStatusEntityT ent = vSBStatus.getSBStatusEntity();
			ref.setDocumentVersion(ent.getDocumentVersion());
			ref.setEntityId(ent.getEntityId());
			getOUSStatusChoice().setSBStatusRef(index, ref);
		} else {
			getOUSStatusChoice().setSBStatusRef(index, null);
		}
	}


	public void setSBStatus(SBStatusI[] SBStatusArray) {
		clearSBStatus();
		for (final SBStatusI s : SBStatusArray) {
			addSBStatus(s);
		}
	}
	/*
	 * End of Methods with entity resolution
	 * ============================================================= */
}
