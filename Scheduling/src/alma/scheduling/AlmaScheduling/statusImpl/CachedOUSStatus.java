/**
 * 
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
 * @author dclarke
 *
 */
public class CachedOUSStatus extends CachedStatusBase implements OUSStatusI {

	private OUSStatus delegate;

	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	public CachedOUSStatus(OUSStatus delegate) {
		super(delegate.getOUSStatusEntity().getEntityId());
		this.delegate = delegate;
	}

	public CachedOUSStatus(String uid) throws SchedulingException {
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
			xml = stateSystem.getOUSStatus(uid);
			delegate = (OUSStatus)entityDeserializer.deserializeEntity(xml, OUSStatus.class); 
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
    	return this;
    }
    
    /* (non-Javadoc)
     * @see alma.scheduling.AlmaScheduling.status.ALMAStatusEntity#asRemote()
     */
    public OUSStatusI asRemote() {
    	try {
			final OUSStatusI s = new RemoteOUSStatus(getUID());
			return s;
		} catch (SchedulingException e) {
			getLogger().warning(String.format(
					"Error linking OUSStatus entity %s to archive - %s",
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
	 * @param index
	 * @param vSession
	 * @throws IndexOutOfBoundsException
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#addSession(int, alma.entity.xmlbinding.ousstatus.SessionT)
	 */
	public void addSession(int index, SessionT vSession) throws IndexOutOfBoundsException {
		delegate.addSession(index, vSession);
	}



	/**
	 * @param vSession
	 * @throws IndexOutOfBoundsException
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#addSession(alma.entity.xmlbinding.ousstatus.SessionT)
	 */
	public void addSession(SessionT vSession) throws IndexOutOfBoundsException {
		delegate.addSession(vSession);
	}



	/**
	 * 
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#clearSession()
	 */
	public void clearSession() {
		delegate.clearSession();
	}



	/**
	 * 
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#deleteNumberObsUnitSetsCompleted()
	 */
	public void deleteNumberObsUnitSetsCompleted() {
		delegate.deleteNumberObsUnitSetsCompleted();
	}



	/**
	 * 
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#deleteNumberObsUnitSetsFailed()
	 */
	public void deleteNumberObsUnitSetsFailed() {
		delegate.deleteNumberObsUnitSetsFailed();
	}



	/**
	 * 
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#deleteNumberSBsCompleted()
	 */
	public void deleteNumberSBsCompleted() {
		delegate.deleteNumberSBsCompleted();
	}



	/**
	 * 
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#deleteNumberSBsFailed()
	 */
	public void deleteNumberSBsFailed() {
		delegate.deleteNumberSBsFailed();
	}



	/**
	 * 
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#deleteTotalObsUnitSets()
	 */
	public void deleteTotalObsUnitSets() {
		delegate.deleteTotalObsUnitSets();
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
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#deleteTotalSBs()
	 */
	public void deleteTotalSBs() {
		delegate.deleteTotalSBs();
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
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#enumerateSession()
	 */
	public Enumeration enumerateSession() {
		return delegate.enumerateSession();
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
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getAlmatype()
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
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getNumberObsUnitSetsCompleted()
	 */
	public int getNumberObsUnitSetsCompleted() {
		return delegate.getNumberObsUnitSetsCompleted();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getNumberObsUnitSetsFailed()
	 */
	public int getNumberObsUnitSetsFailed() {
		return delegate.getNumberObsUnitSetsFailed();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getNumberSBsCompleted()
	 */
	public int getNumberSBsCompleted() {
		return delegate.getNumberSBsCompleted();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getNumberSBsFailed()
	 */
	public int getNumberSBsFailed() {
		return delegate.getNumberSBsFailed();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#getObsUnitSetMemberType()
	 */
	public String getObsUnitSetMemberType() {
		return delegate.getObsUnitSetMemberType();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getObsUnitSetRef()
	 */
	public ObsProjectRefT getObsUnitSetRef() {
		return delegate.getObsUnitSetRef();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getOUSStatusChoice()
	 */
	public OUSStatusChoice getOUSStatusChoice() {
		return delegate.getOUSStatusChoice();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getOUSStatusEntity()
	 */
	public OUSStatusEntityT getOUSStatusEntity() {
		return delegate.getOUSStatusEntity();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getPipelineProcessingRequest()
	 */
	public PipelineProcessingRequestT getPipelineProcessingRequest() {
		return delegate.getPipelineProcessingRequest();
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
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getRevision()
	 */
	public String getRevision() {
		return delegate.getRevision();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getSchemaVersion()
	 */
	public String getSchemaVersion() {
		return delegate.getSchemaVersion();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getSession()
	 */
	public SessionT[] getSession() {
		return delegate.getSession();
	}



	/**
	 * @param index
	 * @return
	 * @throws IndexOutOfBoundsException
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getSession(int)
	 */
	public SessionT getSession(int index) throws IndexOutOfBoundsException {
		return delegate.getSession(index);
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getSessionCount()
	 */
	public int getSessionCount() {
		return delegate.getSessionCount();
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
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getTotalObsUnitSets()
	 */
	public int getTotalObsUnitSets() {
		return delegate.getTotalObsUnitSets();
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
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getTotalSBs()
	 */
	public int getTotalSBs() {
		return delegate.getTotalSBs();
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
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#hasNumberObsUnitSetsCompleted()
	 */
	public boolean hasNumberObsUnitSetsCompleted() {
		return delegate.hasNumberObsUnitSetsCompleted();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#hasNumberObsUnitSetsFailed()
	 */
	public boolean hasNumberObsUnitSetsFailed() {
		return delegate.hasNumberObsUnitSetsFailed();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#hasNumberSBsCompleted()
	 */
	public boolean hasNumberSBsCompleted() {
		return delegate.hasNumberSBsCompleted();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#hasNumberSBsFailed()
	 */
	public boolean hasNumberSBsFailed() {
		return delegate.hasNumberSBsFailed();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#hasTotalObsUnitSets()
	 */
	public boolean hasTotalObsUnitSets() {
		return delegate.hasTotalObsUnitSets();
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
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#hasTotalSBs()
	 */
	public boolean hasTotalSBs() {
		return delegate.hasTotalSBs();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#hasTotalUsedTimeInSec()
	 */
	public boolean hasTotalUsedTimeInSec() {
		return delegate.hasTotalUsedTimeInSec();
	}



	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#isValid()
	 */
	public boolean isValid() {
		return delegate.isValid();
	}



	/**
	 * @param handler
	 * @throws IOException
	 * @throws MarshalException
	 * @throws ValidationException
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#marshal(org.xml.sax.ContentHandler)
	 */
	public void marshal(ContentHandler handler) throws IOException, MarshalException, ValidationException {
		delegate.marshal(handler);
	}



	/**
	 * @param out
	 * @throws MarshalException
	 * @throws ValidationException
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#marshal(java.io.Writer)
	 */
	public void marshal(Writer out) throws MarshalException, ValidationException {
		delegate.marshal(out);
	}



	/**
	 * @param vSession
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#removeSession(alma.entity.xmlbinding.ousstatus.SessionT)
	 */
	public boolean removeSession(SessionT vSession) {
		return delegate.removeSession(vSession);
	}



	/**
	 * @param almatype
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setAlmatype(java.lang.String)
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
	 * @param numberObsUnitSetsCompleted
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setNumberObsUnitSetsCompleted(int)
	 */
	public void setNumberObsUnitSetsCompleted(int numberObsUnitSetsCompleted) {
		delegate.setNumberObsUnitSetsCompleted(numberObsUnitSetsCompleted);
	}



	/**
	 * @param numberObsUnitSetsFailed
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setNumberObsUnitSetsFailed(int)
	 */
	public void setNumberObsUnitSetsFailed(int numberObsUnitSetsFailed) {
		delegate.setNumberObsUnitSetsFailed(numberObsUnitSetsFailed);
	}



	/**
	 * @param numberSBsCompleted
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setNumberSBsCompleted(int)
	 */
	public void setNumberSBsCompleted(int numberSBsCompleted) {
		delegate.setNumberSBsCompleted(numberSBsCompleted);
	}



	/**
	 * @param numberSBsFailed
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setNumberSBsFailed(int)
	 */
	public void setNumberSBsFailed(int numberSBsFailed) {
		delegate.setNumberSBsFailed(numberSBsFailed);
	}



	/**
	 * @param obsUnitSetMemberType
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#setObsUnitSetMemberType(java.lang.String)
	 */
	public void setObsUnitSetMemberType(String obsUnitSetMemberType) {
		delegate.setObsUnitSetMemberType(obsUnitSetMemberType);
	}



	/**
	 * @param obsUnitSetRef
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setObsUnitSetRef(alma.entity.xmlbinding.obsproject.ObsProjectRefT)
	 */
	public void setObsUnitSetRef(ObsProjectRefT obsUnitSetRef) {
		delegate.setObsUnitSetRef(obsUnitSetRef);
	}



	/**
	 * @param OUSStatusChoice
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setOUSStatusChoice(alma.entity.xmlbinding.ousstatus.OUSStatusChoice)
	 */
	public void setOUSStatusChoice(OUSStatusChoice OUSStatusChoice) {
		delegate.setOUSStatusChoice(OUSStatusChoice);
	}



	/**
	 * @param OUSStatusEntity
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setOUSStatusEntity(alma.entity.xmlbinding.ousstatus.OUSStatusEntityT)
	 */
	public void setOUSStatusEntity(OUSStatusEntityT OUSStatusEntity) {
		delegate.setOUSStatusEntity(OUSStatusEntity);
	}



	/**
	 * @param pipelineProcessingRequest
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setPipelineProcessingRequest(alma.entity.xmlbinding.ousstatus.PipelineProcessingRequestT)
	 */
	public void setPipelineProcessingRequest(PipelineProcessingRequestT pipelineProcessingRequest) {
		delegate.setPipelineProcessingRequest(pipelineProcessingRequest);
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
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setRevision(java.lang.String)
	 */
	public void setRevision(String revision) {
		delegate.setRevision(revision);
	}



	/**
	 * @param schemaVersion
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setSchemaVersion(java.lang.String)
	 */
	public void setSchemaVersion(String schemaVersion) {
		delegate.setSchemaVersion(schemaVersion);
	}



	/**
	 * @param index
	 * @param vSession
	 * @throws IndexOutOfBoundsException
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setSession(int, alma.entity.xmlbinding.ousstatus.SessionT)
	 */
	public void setSession(int index, SessionT vSession) throws IndexOutOfBoundsException {
		delegate.setSession(index, vSession);
	}



	/**
	 * @param sessionArray
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setSession(alma.entity.xmlbinding.ousstatus.SessionT[])
	 */
	public void setSession(SessionT[] sessionArray) {
		delegate.setSession(sessionArray);
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
	 * @param totalObsUnitSets
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setTotalObsUnitSets(int)
	 */
	public void setTotalObsUnitSets(int totalObsUnitSets) {
		delegate.setTotalObsUnitSets(totalObsUnitSets);
	}



	/**
	 * @param totalRequiredTimeInSec
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#setTotalRequiredTimeInSec(int)
	 */
	public void setTotalRequiredTimeInSec(int totalRequiredTimeInSec) {
		delegate.setTotalRequiredTimeInSec(totalRequiredTimeInSec);
	}



	/**
	 * @param totalSBs
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setTotalSBs(int)
	 */
	public void setTotalSBs(int totalSBs) {
		delegate.setTotalSBs(totalSBs);
	}



	/**
	 * @param totalUsedTimeInSec
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#setTotalUsedTimeInSec(int)
	 */
	public void setTotalUsedTimeInSec(int totalUsedTimeInSec) {
		delegate.setTotalUsedTimeInSec(totalUsedTimeInSec);
	}



	/**
	 * @throws ValidationException
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#validate()
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
	    OUSStatusI[] result = null;
	    try {
	        result = new OUSStatusI[getOUSStatusChoice().getOUSStatusRefCount()];
	    } catch (NullPointerException ex) {
	        result = new OUSStatusI[0];
	    }
		
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
