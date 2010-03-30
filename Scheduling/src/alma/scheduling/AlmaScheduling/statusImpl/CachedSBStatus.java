/**
 * 
 */
package alma.scheduling.AlmaScheduling.statusImpl;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.ContentHandler;

import alma.acs.entityutil.EntityException;
import alma.entity.xmlbinding.ousstatus.OUSStatusEntityT;
import alma.entity.xmlbinding.ousstatus.OUSStatusRefT;
import alma.entity.xmlbinding.projectstatus.ProjectStatusEntityT;
import alma.entity.xmlbinding.projectstatus.ProjectStatusRefT;
import alma.entity.xmlbinding.sbstatus.ExecStatusT;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.entity.xmlbinding.sbstatus.SBStatusEntityT;
import alma.entity.xmlbinding.schedblock.SchedBlockRefT;
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
 * @author dclarke
 *
 */
public class CachedSBStatus extends CachedStatusBase implements SBStatusI {

	private SBStatus delegate;

	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	public CachedSBStatus(SBStatus delegate) {
		super(delegate.getSBStatusEntity().getEntityId());
		this.delegate = delegate;
	}

	public CachedSBStatus(String uid) throws SchedulingException {
		super(uid);
		checkEntity();
	}
	/*
	 * End of Construction
	 * ============================================================= */

	

	/*
	 * ================================================================
	 * Requirements from abstract superclass
	 * ================================================================
	 */
	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.ALMAStatusEntity#checkEntity()
	 */
	@Override
	protected void checkEntity() throws SchedulingException {
		XmlEntityStruct xml = null;
		delegate = null;

		try {
			xml = stateSystem.getSBStatus(uid);
			delegate = (SBStatus)entityDeserializer.deserializeEntity(xml, SBStatus.class); 
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
	 * @see alma.scheduling.AlmaScheduling.statusIF.StatusBaseI#getDomainEntityId()
	 */
	public String getDomainEntityId() {
		return getSchedBlockRef().getEntityId();
	}

    /* (non-Javadoc)
     * @see alma.scheduling.AlmaScheduling.status.ALMAStatusEntity#asLocal()
     */
    public SBStatusI asLocal() {
    	return this;
    }
    
    /* (non-Javadoc)
     * @see alma.scheduling.AlmaScheduling.status.ALMAStatusEntity#asRemote()
     */
    public SBStatusI asRemote() {
    	try {
			final SBStatusI s = new RemoteSBStatus(getUID());
			return s;
		} catch (SchedulingException e) {
			getLogger().warning(String.format(
					"Error linking SBStatus entity %s to archive - %s",
					getUID(), e.getLocalizedMessage()));
			return null;
		}
    }
	/*
	 * End of Requirements from abstract superclass
	 * ============================================================= */

	

	/*
	 * ================================================================
	 * Delegation
	 * ================================================================
	 */
	/**
	 * @param vExecStatus
	 * @throws IndexOutOfBoundsException
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#addExecStatus(alma.entity.xmlbinding.sbstatus.ExecStatusT)
	 */
	public void addExecStatus(ExecStatusT vExecStatus) throws IndexOutOfBoundsException {
		delegate.addExecStatus(vExecStatus);
	}



	/**
	 * @param index
	 * @param vExecStatus
	 * @throws IndexOutOfBoundsException
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#addExecStatus(int, alma.entity.xmlbinding.sbstatus.ExecStatusT)
	 */
	public void addExecStatus(int index, ExecStatusT vExecStatus) throws IndexOutOfBoundsException {
		delegate.addExecStatus(index, vExecStatus);
	}



	/**
	 * 
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#clearExecStatus()
	 */
	public void clearExecStatus() {
		delegate.clearExecStatus();
	}



	/**
	 * 
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#deleteExecutionsRemaining()
	 */
	public void deleteExecutionsRemaining() {
		delegate.deleteExecutionsRemaining();
	}



	/**
	 * 
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#deleteTotalRequiredTimeInSec()
	 */
	public void deleteTotalRequiredTimeInSec() {
		delegate.deleteTotalRequiredTimeInSec();
	}



	/**
	 * 
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#deleteTotalUsedTimeInSec()
	 */
	public void deleteTotalUsedTimeInSec() {
		delegate.deleteTotalUsedTimeInSec();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#enumerateExecStatus()
	 */
	public Enumeration enumerateExecStatus() {
		return delegate.enumerateExecStatus();
	}



	/**
	 * @param o
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		return delegate.equals(o);
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#getAlmatype()
	 */
	public String getAlmatype() {
		return delegate.getAlmatype();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#getContainingObsUnitSetRef()
	 */
	public OUSStatusRefT getContainingObsUnitSetRef() {
		return delegate.getContainingObsUnitSetRef();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#getExecStatus()
	 */
	public ExecStatusT[] getExecStatus() {
		return delegate.getExecStatus();
	}



	/**
	 * @param index
	 * @return
	 * @throws IndexOutOfBoundsException
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#getExecStatus(int)
	 */
	public ExecStatusT getExecStatus(int index) throws IndexOutOfBoundsException {
		return delegate.getExecStatus(index);
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#getExecStatusCount()
	 */
	public int getExecStatusCount() {
		return delegate.getExecStatusCount();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#getExecutionsRemaining()
	 */
	public int getExecutionsRemaining() {
		return delegate.getExecutionsRemaining();
	}

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#getProjectStatusRef()
	 */
	public ProjectStatusRefT getProjectStatusRef() {
		return delegate.getProjectStatusRef();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#getRevision()
	 */
	public String getRevision() {
		return delegate.getRevision();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#getSBStatusEntity()
	 */
	public SBStatusEntityT getSBStatusEntity() {
		return delegate.getSBStatusEntity();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#getSchedBlockRef()
	 */
	public SchedBlockRefT getSchedBlockRef() {
		return delegate.getSchedBlockRef();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#getSchemaVersion()
	 */
	public String getSchemaVersion() {
		return delegate.getSchemaVersion();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#hasExecutionsRemaining()
	 */
	public boolean hasExecutionsRemaining() {
		return delegate.hasExecutionsRemaining();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#isValid()
	 */
	public boolean isValid() {
		return delegate.isValid();
	}



	/**
	 * @param handler
	 * @throws IOException
	 * @throws MarshalException
	 * @throws ValidationException
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#marshal(org.xml.sax.ContentHandler)
	 */
	public void marshal(ContentHandler handler) throws IOException, MarshalException, ValidationException {
		delegate.marshal(handler);
	}



	/**
	 * @param out
	 * @throws MarshalException
	 * @throws ValidationException
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#marshal(java.io.Writer)
	 */
	public void marshal(Writer out) throws MarshalException, ValidationException {
		delegate.marshal(out);
	}



	/**
	 * @param vExecStatus
	 * @return
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#removeExecStatus(alma.entity.xmlbinding.sbstatus.ExecStatusT)
	 */
	public boolean removeExecStatus(ExecStatusT vExecStatus) {
		return delegate.removeExecStatus(vExecStatus);
	}



	/**
	 * @param almatype
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#setAlmatype(java.lang.String)
	 */
	public void setAlmatype(String almatype) {
		delegate.setAlmatype(almatype);
	}



	/**
	 * @param containingObsUnitSetRef
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#setContainingObsUnitSetRef(alma.entity.xmlbinding.ousstatus.OUSStatusRefT)
	 */
	public void setContainingObsUnitSetRef(OUSStatusRefT containingObsUnitSetRef) {
		delegate.setContainingObsUnitSetRef(containingObsUnitSetRef);
	}



	/**
	 * @param execStatusArray
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#setExecStatus(alma.entity.xmlbinding.sbstatus.ExecStatusT[])
	 */
	public void setExecStatus(ExecStatusT[] execStatusArray) {
		delegate.setExecStatus(execStatusArray);
	}



	/**
	 * @param index
	 * @param vExecStatus
	 * @throws IndexOutOfBoundsException
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#setExecStatus(int, alma.entity.xmlbinding.sbstatus.ExecStatusT)
	 */
	public void setExecStatus(int index, ExecStatusT vExecStatus) throws IndexOutOfBoundsException {
		delegate.setExecStatus(index, vExecStatus);
	}



	/**
	 * @param executionsRemaining
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#setExecutionsRemaining(int)
	 */
	public void setExecutionsRemaining(int executionsRemaining) {
		delegate.setExecutionsRemaining(executionsRemaining);
	}



	/**
	 * @param projectStatusRef
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#setProjectStatusRef(alma.entity.xmlbinding.projectstatus.ProjectStatusRefT)
	 */
	public void setProjectStatusRef(ProjectStatusRefT projectStatusRef) {
		delegate.setProjectStatusRef(projectStatusRef);
	}



	/**
	 * @param revision
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#setRevision(java.lang.String)
	 */
	public void setRevision(String revision) {
		delegate.setRevision(revision);
	}



	/**
	 * @param SBStatusEntity
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#setSBStatusEntity(alma.entity.xmlbinding.sbstatus.SBStatusEntityT)
	 */
	public void setSBStatusEntity(SBStatusEntityT SBStatusEntity) {
		delegate.setSBStatusEntity(SBStatusEntity);
	}



	/**
	 * @param schedBlockRef
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#setSchedBlockRef(alma.entity.xmlbinding.schedblock.SchedBlockRefT)
	 */
	public void setSchedBlockRef(SchedBlockRefT schedBlockRef) {
		delegate.setSchedBlockRef(schedBlockRef);
	}



	/**
	 * @param schemaVersion
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#setSchemaVersion(java.lang.String)
	 */
	public void setSchemaVersion(String schemaVersion) {
		delegate.setSchemaVersion(schemaVersion);
	}



	/**
	 * @throws ValidationException
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#validate()
	 */
	public void validate() throws ValidationException {
		delegate.validate();
	}

	/* ====================== ObsUnitStatusI ====================== */
	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#getObsUnitSetMemberType()
	 */
	public String getObsUnitSetMemberType() {
		return delegate.getObsUnitSetMemberType();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.StatusBaseT#getStatus()
	 */
	public StatusT getStatus() {
		return delegate.getStatus();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.StatusBaseT#getTimeOfUpdate()
	 */
	public String getTimeOfUpdate() {
		return delegate.getTimeOfUpdate();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#getTotalRequiredTimeInSec()
	 */
	public int getTotalRequiredTimeInSec() {
		return delegate.getTotalRequiredTimeInSec();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#getTotalUsedTimeInSec()
	 */
	public int getTotalUsedTimeInSec() {
		return delegate.getTotalUsedTimeInSec();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#hasTotalRequiredTimeInSec()
	 */
	public boolean hasTotalRequiredTimeInSec() {
		return delegate.hasTotalRequiredTimeInSec();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#hasTotalUsedTimeInSec()
	 */
	public boolean hasTotalUsedTimeInSec() {
		return delegate.hasTotalUsedTimeInSec();
	}



	/**
	 * @param obsUnitSetMemberType
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#setObsUnitSetMemberType(java.lang.String)
	 */
	public void setObsUnitSetMemberType(String obsUnitSetMemberType) {
		delegate.setObsUnitSetMemberType(obsUnitSetMemberType);
	}



	/**
	 * @param status
	 * @see alma.entity.xmlbinding.projectstatus.StatusBaseT#setStatus(alma.entity.xmlbinding.valuetypes.StatusT)
	 */
	public void setStatus(StatusT status) {
		delegate.setStatus(status);
	}



	/**
	 * @param timeOfUpdate
	 * @see alma.entity.xmlbinding.projectstatus.StatusBaseT#setTimeOfUpdate(java.lang.String)
	 */
	public void setTimeOfUpdate(String timeOfUpdate) {
		delegate.setTimeOfUpdate(timeOfUpdate);
	}



	/**
	 * @param totalRequiredTimeInSec
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#setTotalRequiredTimeInSec(int)
	 */
	public void setTotalRequiredTimeInSec(int totalRequiredTimeInSec) {
		delegate.setTotalRequiredTimeInSec(totalRequiredTimeInSec);
	}



	/**
	 * @param totalUsedTimeInSec
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#setTotalUsedTimeInSec(int)
	 */
	public void setTotalUsedTimeInSec(int totalUsedTimeInSec) {
		delegate.setTotalUsedTimeInSec(totalUsedTimeInSec);
	}

	/*
	 * End of Delegation
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
}
