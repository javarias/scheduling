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
 * File ALMASBStatus.java
 * $Id: RemoteSBStatus.java,v 1.4 2010/08/05 15:27:29 dclarke Exp $
 */
package alma.scheduling.AlmaScheduling.statusImpl;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.ContentHandler;

import alma.acs.entityutil.EntityException;
import alma.alarmsystem.source.ACSFaultState;
import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.entity.xmlbinding.ousstatus.OUSStatusEntityT;
import alma.entity.xmlbinding.ousstatus.OUSStatusRefT;
import alma.entity.xmlbinding.projectstatus.ProjectStatusEntityT;
import alma.entity.xmlbinding.projectstatus.ProjectStatusRefT;
import alma.entity.xmlbinding.sbstatus.ExecStatusT;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.entity.xmlbinding.sbstatus.SBStatusEntityT;
import alma.entity.xmlbinding.schedblock.SchedBlockRefT;
import alma.entity.xmlbinding.valuetypes.StatusT;
import alma.scheduling.AlmaScheduling.ALMAArchivePoller;
import alma.scheduling.AlmaScheduling.statusIF.OUSStatusI;
import alma.scheduling.AlmaScheduling.statusIF.ProjectStatusI;
import alma.scheduling.AlmaScheduling.statusIF.SBStatusI;
import alma.scheduling.Define.SchedulingException;
import alma.statearchiveexceptions.InappropriateEntityTypeEx;
import alma.statearchiveexceptions.NoSuchEntityEx;
import alma.statearchiveexceptions.NullEntityIdEx;
import alma.xmlentity.XmlEntityStruct;

/**
 * A <code>Proxy</code> for an <code>SBStatus</code> which handles all
 * the connection to the State System.
 * @author dclarke
 */
public class RemoteSBStatus extends RemoteStatusBase implements SBStatusI {
	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	public RemoteSBStatus(String uid) throws SchedulingException {
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
	private SBStatus cache;
	
	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ALMAStatusEntity#checkEntity()
	 */
	@Override
	protected void checkEntity() throws SchedulingException {
		XmlEntityStruct xml = null;
		cache = null;

		try {
			xml = stateSystem.getSBStatus(uid);
			cache = (SBStatus)entityDeserializer.deserializeEntity(xml, SBStatus.class); 
		} catch (InappropriateEntityTypeEx e) {
			throw new SchedulingException(String.format(
					"Error retrieving SBStatus entity %s - entity is not an SBStatus", getUID()),
					e);
		} catch (NullEntityIdEx e) {
			throw new SchedulingException(String.format(
					"Error retrieving SBStatus entity %s - entity id is null", getUID()),
					e);
		} catch (NoSuchEntityEx e) {
			throw new SchedulingException(String.format(
					"Error retrieving SBStatus entity %s - no such entity found", getUID()),
					e);
		} catch (EntityException e) {
			if (xml == null) {
				throw new SchedulingException(String.format(
						"Error retrieving SBStatus entity %s - returned XML is null",
						getUID()),
						e);
			} else if (xml.xmlString == null) {
				throw new SchedulingException(String.format(
						"Error retrieving SBStatus entity %s - returned XML has null string",
						getUID()),
						e);
			} else {
				final int end = (xml.xmlString.length() > 255)?
																255:
																xml.xmlString.length();
				throw new SchedulingException(String.format(
						"Error retrieving SBStatus entity %s - cannot deserialise the returned XML%n\t%s",
						getUID(), xml.xmlString.substring(0, end)),
						e);
			}
		} catch (Exception e) {
			throw new SchedulingException(String.format(
					"Error retrieving SBStatus entity %s", getUID()),
					e);
		}

	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ALMAStatusEntity#getEntity()
	 */
	@Override
	protected SBStatus getEntity() {
        cache = null;
        try {
            XmlEntityStruct xml = stateSystem.getSBStatus(uid);

            cache = (SBStatus)entityDeserializer.deserializeEntity(xml, SBStatus.class); 
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
    		XmlEntityStruct xml = entitySerializer.serializeEntity(cache, cache.getSBStatusEntity());
    		stateSystem.updateSBStatus(xml);
    	} catch(Exception e){
    		logger.severe("SCHEDULING: error updating SBStatus in archive, "+e.toString());
    		e.printStackTrace(System.out);
    		//throw new SchedulingException (e);
    	}
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.statusIF.StatusBaseI#getDomainEntityId()
	 */
	public String getDomainEntityId() {
		return getSchedBlockRef().getEntityId();
	}

    /* (non-Javadoc)
     * @see alma.scheduling.AlmaScheduling.status.ALMAStatusEntity#asLocal()
     */
    public SBStatusI asLocal() {
    	return new CachedSBStatus(getEntity());
    }
    
    /* (non-Javadoc)
     * @see alma.scheduling.AlmaScheduling.status.ALMAStatusEntity#asRemote()
     */
    public SBStatusI asRemote() {
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
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#addExecStatus(alma.entity.xmlbinding.sbstatus.ExecStatusT)
	 */
	public void addExecStatus(ExecStatusT vExecStatus) throws IndexOutOfBoundsException {
		getEntity();
		cache.addExecStatus(vExecStatus);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#addExecStatus(int, alma.entity.xmlbinding.sbstatus.ExecStatusT)
	 */
	public void addExecStatus(int index, ExecStatusT vExecStatus) throws IndexOutOfBoundsException {
		getEntity();
		cache.addExecStatus(index, vExecStatus);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#clearExecStatus()
	 */
	public void clearExecStatus() {
		getEntity();
		cache.clearExecStatus();
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#deleteExecutionsRemaining()
	 */
	public void deleteExecutionsRemaining() {
		getEntity();
		cache.deleteExecutionsRemaining();
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#deleteTotalRequiredTimeInSec()
	 */
	public void deleteTotalRequiredTimeInSec() {
		getEntity();
		cache.deleteTotalRequiredTimeInSec();
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#deleteTotalUsedTimeInSec()
	 */
	public void deleteTotalUsedTimeInSec() {
		getEntity();
		cache.deleteTotalUsedTimeInSec();
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#enumerateExecStatus()
	 */
	public Enumeration enumerateExecStatus() {
		getEntity();
		return cache.enumerateExecStatus();
	}

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
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#getContainingObsUnitSetRef()
	 */
	public OUSStatusRefT getContainingObsUnitSetRef() {
		getEntity();
		return cache.getContainingObsUnitSetRef();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#getExecStatus()
	 */
	public ExecStatusT[] getExecStatus() {
		getEntity();
		return cache.getExecStatus();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#getExecStatus(int)
	 */
	public ExecStatusT getExecStatus(int index) throws IndexOutOfBoundsException {
		getEntity();
		return cache.getExecStatus(index);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#getExecStatusCount()
	 */
	public int getExecStatusCount() {
		getEntity();
		return cache.getExecStatusCount();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#getExecutionsRemaining()
	 */
	public int getExecutionsRemaining() {
		getEntity();
		return cache.getExecutionsRemaining();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#getObsUnitSetMemberType()
	 */
	public String getObsUnitSetMemberType() {
		getEntity();
		return cache.getObsUnitSetMemberType();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#getProjectStatusRef()
	 */
	public ProjectStatusRefT getProjectStatusRef() {
		getEntity();
		return cache.getProjectStatusRef();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#getRevision()
	 */
	public String getRevision() {
		getEntity();
		return cache.getRevision();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#getSBStatusEntity()
	 */
	public SBStatusEntityT getSBStatusEntity() {
		getEntity();
		return cache.getSBStatusEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#getSchedBlockRef()
	 */
	public SchedBlockRefT getSchedBlockRef() {
		getEntity();
		return cache.getSchedBlockRef();
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

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#getTotalRequiredTimeInSec()
	 */
	public int getTotalRequiredTimeInSec() {
		getEntity();
		return cache.getTotalRequiredTimeInSec();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#getTotalUsedTimeInSec()
	 */
	public int getTotalUsedTimeInSec() {
		getEntity();
		return cache.getTotalUsedTimeInSec();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#hasExecutionsRemaining()
	 */
	public boolean hasExecutionsRemaining() {
		getEntity();
		return cache.hasExecutionsRemaining();
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
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#hasTotalRequiredTimeInSec()
	 */
	public boolean hasTotalRequiredTimeInSec() {
		getEntity();
		return cache.hasTotalRequiredTimeInSec();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#hasTotalUsedTimeInSec()
	 */
	public boolean hasTotalUsedTimeInSec() {
		getEntity();
		return cache.hasTotalUsedTimeInSec();
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
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#removeExecStatus(alma.entity.xmlbinding.sbstatus.ExecStatusT)
	 */
	public boolean removeExecStatus(ExecStatusT vExecStatus) {
		getEntity();
		final boolean result = cache.removeExecStatus(vExecStatus);
		putEntity();
		return result;
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
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#setContainingObsUnitSetRef(alma.entity.xmlbinding.ousstatus.OUSStatusRefT)
	 */
	public void setContainingObsUnitSetRef(OUSStatusRefT containingObsUnitSetRef) {
		getEntity();
		cache.setContainingObsUnitSetRef(containingObsUnitSetRef);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#setExecStatus(alma.entity.xmlbinding.sbstatus.ExecStatusT[])
	 */
	public void setExecStatus(ExecStatusT[] execStatusArray) {
		getEntity();
		cache.setExecStatus(execStatusArray);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#setExecStatus(int, alma.entity.xmlbinding.sbstatus.ExecStatusT)
	 */
	public void setExecStatus(int index, ExecStatusT vExecStatus) throws IndexOutOfBoundsException {
		getEntity();
		cache.setExecStatus(index, vExecStatus);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#setExecutionsRemaining(int)
	 */
	public void setExecutionsRemaining(int executionsRemaining) {
		getEntity();
		cache.setExecutionsRemaining(executionsRemaining);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#setObsUnitSetMemberType(java.lang.String)
	 */
	public void setObsUnitSetMemberType(String obsUnitSetMemberType) {
		getEntity();
		cache.setObsUnitSetMemberType(obsUnitSetMemberType);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#setProjectStatusRef(alma.entity.xmlbinding.projectstatus.ProjectStatusRefT)
	 */
	public void setProjectStatusRef(ProjectStatusRefT projectStatusRef) {
		getEntity();
		cache.setProjectStatusRef(projectStatusRef);
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
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#setSBStatusEntity(alma.entity.xmlbinding.sbstatus.SBStatusEntityT)
	 */
	public void setSBStatusEntity(SBStatusEntityT SBStatusEntity) {
		getEntity();
		cache.setSBStatusEntity(SBStatusEntity);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#setSchedBlockRef(alma.entity.xmlbinding.schedblock.SchedBlockRefT)
	 */
	public void setSchedBlockRef(SchedBlockRefT schedBlockRef) {
		getEntity();
		cache.setSchedBlockRef(schedBlockRef);
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

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#setTotalRequiredTimeInSec(int)
	 */
	public void setTotalRequiredTimeInSec(int totalRequiredTimeInSec) {
		getEntity();
		cache.setTotalRequiredTimeInSec(totalRequiredTimeInSec);
		putEntity();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ProjectStatusI#setTotalUsedTimeInSec(int)
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
	/*
	 * End of Methods with entity resolution
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Utilities
	 * ================================================================
	 */
	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.statusIF.SBStatusI#isRunnable()
	 */
	public boolean isRunnable() {
		final String myState = getStatus().getState().toString();
		for (final String runnableState : ALMAArchivePoller.sbRunnableStates) {
			if (myState.equals(runnableState)) {
				return true;
			}
		}
		return false;
	}
	/*
	 * End of Utilities
	 * ============================================================= */
}
