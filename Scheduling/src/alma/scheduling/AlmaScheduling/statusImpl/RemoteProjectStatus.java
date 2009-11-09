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
 * File ALMAProjectStatus.java
 * $Id: RemoteProjectStatus.java,v 1.2 2009/11/09 22:58:45 rhiriart Exp $
 */
package alma.scheduling.AlmaScheduling.statusImpl;

import java.io.IOException;
import java.io.Writer;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.ContentHandler;

import alma.acs.entityutil.EntityException;
import alma.alarmsystem.source.ACSFaultState;
import alma.entity.xmlbinding.obsproject.ObsProjectRefT;
import alma.entity.xmlbinding.obsproposal.ObsProposalRefT;
import alma.entity.xmlbinding.ousstatus.OUSStatusEntityT;
import alma.entity.xmlbinding.ousstatus.OUSStatusRefT;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.projectstatus.ProjectStatusEntityT;
import alma.entity.xmlbinding.valuetypes.StatusT;
import alma.scheduling.AlmaScheduling.statusIF.OUSStatusI;
import alma.scheduling.AlmaScheduling.statusIF.ProjectStatusI;
import alma.scheduling.Define.SchedulingException;
import alma.statearchiveexceptions.InappropriateEntityTypeEx;
import alma.statearchiveexceptions.NoSuchEntityEx;
import alma.statearchiveexceptions.NullEntityIdEx;
import alma.xmlentity.XmlEntityStruct;

/**
 * A <code>Proxy</code> for an <code>ProjectStatus</code> which handles all
 * the connection to the State System.
 * @author dclarke
 */
public class RemoteProjectStatus extends RemoteStatusBase implements ProjectStatusI {
	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	public RemoteProjectStatus(String uid) throws SchedulingException {
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
	private ProjectStatus cache;
	
	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ALMAStatusEntity#checkEntity()
	 */
	@Override
	protected void checkEntity() throws SchedulingException {
		XmlEntityStruct xml = null;
		cache = null;

		try {
			xml = stateSystem.getProjectStatus(uid);
			cache = (ProjectStatus)entityDeserializer.deserializeEntity(xml, ProjectStatus.class); 
		} catch (InappropriateEntityTypeEx e) {
			throw new SchedulingException(String.format(
					"Error retrieving ProjectStatus entity %s - entity is not an ProjectStatus", getUID()),
					e);
		} catch (NullEntityIdEx e) {
			throw new SchedulingException(String.format(
					"Error retrieving ProjectStatus entity %s - entity id is null", getUID()),
					e);
		} catch (NoSuchEntityEx e) {
			throw new SchedulingException(String.format(
					"Error retrieving ProjectStatus entity %s - no such entity found", getUID()),
					e);
		} catch (EntityException e) {
			if (xml == null) {
				throw new SchedulingException(String.format(
						"Error retrieving ProjectStatus entity %s - returned XML is null",
						getUID()),
						e);
			} else if (xml.xmlString == null) {
				throw new SchedulingException(String.format(
						"Error retrieving ProjectStatus entity %s - returned XML has null string",
						getUID()),
						e);
			} else {
				final int end = (xml.xmlString.length() > 255)?
																255:
																xml.xmlString.length();
				throw new SchedulingException(String.format(
						"Error retrieving ProjectStatus entity %s - cannot deserialise the returned XML%n\t%s",
						getUID(), xml.xmlString.substring(0, end)),
						e);
			}
		} catch (Exception e) {
			throw new SchedulingException(String.format(
					"Error retrieving ProjectStatus entity %s", getUID()),
					e);
		}

	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ALMAStatusEntity#getEntity()
	 */
	@Override
	protected ProjectStatus getEntity() {
        cache = null;
        try {
            XmlEntityStruct xml = stateSystem.getProjectStatus(uid);

            cache = (ProjectStatus)entityDeserializer.deserializeEntity(xml, ProjectStatus.class); 
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
    		XmlEntityStruct xml = entitySerializer.serializeEntity(cache, cache.getProjectStatusEntity());
    		stateSystem.updateProjectStatus(xml);
    	} catch(Exception e){
    		logger.severe("SCHEDULING: error updating ProjectStatus in archive, "+e.toString());
    		e.printStackTrace(System.out);
    		//throw new SchedulingException (e);
    	}
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.statusIF.StatusBaseI#getDomainEntityId()
	 */
	public String getDomainEntityId() {
		return getObsProjectRef().getEntityId();
	}

    /* (non-Javadoc)
     * @see alma.scheduling.AlmaScheduling.status.ALMAStatusEntity#asLocal()
     */
    public ProjectStatusI asLocal() {
    	return new CachedProjectStatus(getEntity());
    }
    
    /* (non-Javadoc)
     * @see alma.scheduling.AlmaScheduling.status.ALMAStatusEntity#asRemote()
     */
    public RemoteProjectStatus asRemote() {
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
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		getEntity();
		return cache.equals(o);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#getAlmatype()
	 */
	public String getAlmatype() {
		getEntity();
		return cache.getAlmatype();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#getBreakpointTime()
	 */
	public String getBreakpointTime() {
		getEntity();
		return cache.getBreakpointTime();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#getName()
	 */
	public String getName() {
		getEntity();
		return cache.getName();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#getObsProgramStatusRef()
	 */
	public OUSStatusRefT getObsProgramStatusRef() {
		getEntity();
		return cache.getObsProgramStatusRef();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#getObsProjectRef()
	 */
	public ObsProjectRefT getObsProjectRef() {
		getEntity();
		return cache.getObsProjectRef();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#getObsProposalRef()
	 */
	public ObsProposalRefT getObsProposalRef() {
		getEntity();
		return cache.getObsProposalRef();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#getPI()
	 */
	public String getPI() {
		getEntity();
		return cache.getPI();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#getProjectStatusEntity()
	 */
	public ProjectStatusEntityT getProjectStatusEntity() {
		getEntity();
		return cache.getProjectStatusEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#getProjectWasTimedOut()
	 */
	public String getProjectWasTimedOut() {
		getEntity();
		return cache.getProjectWasTimedOut();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#getRevision()
	 */
	public String getRevision() {
		getEntity();
		return cache.getRevision();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#getSchemaVersion()
	 */
	public String getSchemaVersion() {
		getEntity();
		return cache.getSchemaVersion();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#getStatus()
	 */
	public StatusT getStatus() {
		getEntity();
		return cache.getStatus();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#getTimeOfUpdate()
	 */
	public String getTimeOfUpdate() {
		getEntity();
		return cache.getTimeOfUpdate();
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
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#isValid()
	 */
	public boolean isValid() {
		getEntity();
		return cache.isValid();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#marshal(org.xml.sax.ContentHandler)
	 */
	public void marshal(ContentHandler handler) throws IOException, MarshalException, ValidationException {
		getEntity();
		cache.marshal(handler);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#marshal(java.io.Writer)
	 */
	public void marshal(Writer out) throws MarshalException, ValidationException {
		getEntity();
		cache.marshal(out);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#setAlmatype(java.lang.String)
	 */
	public void setAlmatype(String almatype) {
		getEntity();
		cache.setAlmatype(almatype);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#setBreakpointTime(java.lang.String)
	 */
	public void setBreakpointTime(String breakpointTime) {
		getEntity();
		cache.setBreakpointTime(breakpointTime);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#setName(java.lang.String)
	 */
	public void setName(String name) {
		getEntity();
		cache.setName(name);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#setObsProgramStatusRef(alma.entity.xmlbinding.ousstatus.OUSStatusRefT)
	 */
	public void setObsProgramStatusRef(OUSStatusRefT obsProgramStatusRef) {
		getEntity();
		cache.setObsProgramStatusRef(obsProgramStatusRef);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#setObsProjectRef(alma.entity.xmlbinding.obsproject.ObsProjectRefT)
	 */
	public void setObsProjectRef(ObsProjectRefT obsProjectRef) {
		getEntity();
		cache.setObsProjectRef(obsProjectRef);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#setObsProposalRef(alma.entity.xmlbinding.obsproposal.ObsProposalRefT)
	 */
	public void setObsProposalRef(ObsProposalRefT obsProposalRef) {
		getEntity();
		cache.setObsProposalRef(obsProposalRef);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#setPI(java.lang.String)
	 */
	public void setPI(String PI) {
		getEntity();
		cache.setPI(PI);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#setProjectStatusEntity(alma.entity.xmlbinding.projectstatus.ProjectStatusEntityT)
	 */
	public void setProjectStatusEntity(ProjectStatusEntityT projectStatusEntity) {
		getEntity();
		cache.setProjectStatusEntity(projectStatusEntity);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#setProjectWasTimedOut(java.lang.String)
	 */
	public void setProjectWasTimedOut(String projectWasTimedOut) {
		getEntity();
		cache.setProjectWasTimedOut(projectWasTimedOut);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#setRevision(java.lang.String)
	 */
	public void setRevision(String revision) {
		getEntity();
		cache.setRevision(revision);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#setSchemaVersion(java.lang.String)
	 */
	public void setSchemaVersion(String schemaVersion) {
		getEntity();
		cache.setSchemaVersion(schemaVersion);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#setStatus(alma.entity.xmlbinding.valuetypes.StatusT)
	 */
	public void setStatus(StatusT status) {
		getEntity();
		cache.setStatus(status);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#setTimeOfUpdate(java.lang.String)
	 */
	public void setTimeOfUpdate(String timeOfUpdate) {
		getEntity();
		cache.setTimeOfUpdate(timeOfUpdate);
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
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#validate()
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
	public OUSStatusI getObsProgramStatus() throws SchedulingException {
		final OUSStatusRefT ref = getObsProgramStatusRef();
		OUSStatusI result = null;
		if (ref != null) {
			result = getFactory().createOUSStatus(ref.getEntityId());
		}
		return result;
	}

	public void setObsProgramStatus(OUSStatusI containingObsUnitSet) {
		if (containingObsUnitSet != null) {
			final OUSStatusRefT    ref = new OUSStatusRefT();
			final OUSStatusEntityT ent = containingObsUnitSet.getOUSStatusEntity();
			ref.setDocumentVersion(ent.getDocumentVersion());
			ref.setEntityId(ent.getEntityId());
			setObsProgramStatusRef(ref);
		} else {
			setObsProgramStatusRef(null);
		}
	}
	/*
	 * End of Methods with entity resolution
	 * ============================================================= */
}
