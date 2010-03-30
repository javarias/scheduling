/**
 * 
 */
package alma.scheduling.AlmaScheduling.statusImpl;

import java.io.IOException;
import java.io.Writer;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.ContentHandler;

import alma.acs.entityutil.EntityException;
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
 * @author dclarke
 *
 */
public class CachedProjectStatus extends CachedStatusBase implements ProjectStatusI {

	private ProjectStatus delegate;

	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	public CachedProjectStatus(ProjectStatus delegate) {
		super(delegate.getProjectStatusEntity().getEntityId());
		this.delegate = delegate;
	}

	public CachedProjectStatus(String uid) throws SchedulingException {
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
			xml = stateSystem.getProjectStatus(uid);
			delegate = (ProjectStatus)entityDeserializer.deserializeEntity(xml, ProjectStatus.class); 
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
	 * @see alma.scheduling.AlmaScheduling.statusIF.StatusBaseI#getDomainEntityId()
	 */
	public String getDomainEntityId() {
		return getObsProjectRef().getEntityId();
	}

    /* (non-Javadoc)
     * @see alma.scheduling.AlmaScheduling.status.ALMAStatusEntity#asLocal()
     */
    public ProjectStatusI asLocal() {
    	return this;
    }
    
    /* (non-Javadoc)
     * @see alma.scheduling.AlmaScheduling.status.ALMAStatusEntity#asRemote()
     */
    public ProjectStatusI asRemote() {
    	try {
			final ProjectStatusI s = new RemoteProjectStatus(getUID());
			return s;
		} catch (SchedulingException e) {
			getLogger().warning(String.format(
					"Error linking ProjectStatus entity %s to archive - %s",
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
	 * @param o
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		return delegate.equals(o);
	}

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#getAlmatype()
	 */
	public String getAlmatype() {
		return delegate.getAlmatype();
	}

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#getBreakpointTime()
	 */
	public String getBreakpointTime() {
		return delegate.getBreakpointTime();
	}

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#getName()
	 */
	public String getName() {
		return delegate.getName();
	}

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#getObsProgramStatusRef()
	 */
	public OUSStatusRefT getObsProgramStatusRef() {
		return delegate.getObsProgramStatusRef();
	}

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#getObsProjectRef()
	 */
	public ObsProjectRefT getObsProjectRef() {
		return delegate.getObsProjectRef();
	}

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#getObsProposalRef()
	 */
	public ObsProposalRefT getObsProposalRef() {
		return delegate.getObsProposalRef();
	}

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#getPI()
	 */
	public String getPI() {
		return delegate.getPI();
	}

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#getProjectStatusEntity()
	 */
	public ProjectStatusEntityT getProjectStatusEntity() {
		return delegate.getProjectStatusEntity();
	}

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#getProjectWasTimedOut()
	 */
	public String getProjectWasTimedOut() {
		return delegate.getProjectWasTimedOut();
	}

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#getRevision()
	 */
	public String getRevision() {
		return delegate.getRevision();
	}

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#getSchemaVersion()
	 */
	public String getSchemaVersion() {
		return delegate.getSchemaVersion();
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
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#isValid()
	 */
	public boolean isValid() {
		return delegate.isValid();
	}

	/**
	 * @param handler
	 * @throws IOException
	 * @throws MarshalException
	 * @throws ValidationException
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#marshal(org.xml.sax.ContentHandler)
	 */
	public void marshal(ContentHandler handler) throws IOException, MarshalException, ValidationException {
		delegate.marshal(handler);
	}

	/**
	 * @param out
	 * @throws MarshalException
	 * @throws ValidationException
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#marshal(java.io.Writer)
	 */
	public void marshal(Writer out) throws MarshalException, ValidationException {
		delegate.marshal(out);
	}

	/**
	 * @param almatype
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#setAlmatype(java.lang.String)
	 */
	public void setAlmatype(String almatype) {
		delegate.setAlmatype(almatype);
	}

	/**
	 * @param breakpointTime
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#setBreakpointTime(java.lang.String)
	 */
	public void setBreakpointTime(String breakpointTime) {
		delegate.setBreakpointTime(breakpointTime);
	}

	/**
	 * @param name
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#setName(java.lang.String)
	 */
	public void setName(String name) {
		delegate.setName(name);
	}

	/**
	 * @param obsProgramStatusRef
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#setObsProgramStatusRef(alma.entity.xmlbinding.ousstatus.OUSStatusRefT)
	 */
	public void setObsProgramStatusRef(OUSStatusRefT obsProgramStatusRef) {
		delegate.setObsProgramStatusRef(obsProgramStatusRef);
	}

	/**
	 * @param obsProjectRef
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#setObsProjectRef(alma.entity.xmlbinding.obsproject.ObsProjectRefT)
	 */
	public void setObsProjectRef(ObsProjectRefT obsProjectRef) {
		delegate.setObsProjectRef(obsProjectRef);
	}

	/**
	 * @param obsProposalRef
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#setObsProposalRef(alma.entity.xmlbinding.obsproposal.ObsProposalRefT)
	 */
	public void setObsProposalRef(ObsProposalRefT obsProposalRef) {
		delegate.setObsProposalRef(obsProposalRef);
	}

	/**
	 * @param PI
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#setPI(java.lang.String)
	 */
	public void setPI(String PI) {
		delegate.setPI(PI);
	}

	/**
	 * @param projectStatusEntity
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#setProjectStatusEntity(alma.entity.xmlbinding.projectstatus.ProjectStatusEntityT)
	 */
	public void setProjectStatusEntity(ProjectStatusEntityT projectStatusEntity) {
		delegate.setProjectStatusEntity(projectStatusEntity);
	}

	/**
	 * @param projectWasTimedOut
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#setProjectWasTimedOut(java.lang.String)
	 */
	public void setProjectWasTimedOut(String projectWasTimedOut) {
		delegate.setProjectWasTimedOut(projectWasTimedOut);
	}

	/**
	 * @param revision
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#setRevision(java.lang.String)
	 */
	public void setRevision(String revision) {
		delegate.setRevision(revision);
	}

	/**
	 * @param schemaVersion
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#setSchemaVersion(java.lang.String)
	 */
	public void setSchemaVersion(String schemaVersion) {
		delegate.setSchemaVersion(schemaVersion);
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
	 * @throws ValidationException
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#validate()
	 */
	public void validate() throws ValidationException {
		delegate.validate();
	}
	/*
	 * End of Delegation
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
