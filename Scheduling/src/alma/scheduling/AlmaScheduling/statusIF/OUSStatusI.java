package alma.scheduling.AlmaScheduling.statusIF;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.ContentHandler;

import alma.entity.xmlbinding.obsproject.ObsProjectRefT;
import alma.entity.xmlbinding.ousstatus.OUSStatusChoice;
import alma.entity.xmlbinding.ousstatus.OUSStatusEntityT;
import alma.entity.xmlbinding.ousstatus.OUSStatusRefT;
import alma.entity.xmlbinding.ousstatus.PipelineProcessingRequestT;
import alma.entity.xmlbinding.ousstatus.SessionT;
import alma.entity.xmlbinding.projectstatus.ProjectStatusRefT;
import alma.entity.xmlbinding.valuetypes.StatusT;
import alma.scheduling.Define.SchedulingException;

public interface OUSStatusI extends StatusBaseI {

	/*
	 * ================================================================
	 * Being a proxy
	 * ================================================================
	 */
	/**
	 * @return the PartId of the domain object for which the thing
	 *         for which we are a proxy is the status.
	 */
	public String getDomainPartId();

	/**
	 * @param index
	 * @param vSession
	 * @throws IndexOutOfBoundsException
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#addSession(int, alma.entity.xmlbinding.ousstatus.SessionT)
	 */
	public abstract void addSession(int index, SessionT vSession)
			throws IndexOutOfBoundsException;

	/**
	 * @param vSession
	 * @throws IndexOutOfBoundsException
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#addSession(alma.entity.xmlbinding.ousstatus.SessionT)
	 */
	public abstract void addSession(SessionT vSession)
			throws IndexOutOfBoundsException;

	/**
	 * 
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#clearSession()
	 */
	public abstract void clearSession();

	/**
	 * 
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#deleteNumberObsUnitSetsCompleted()
	 */
	public abstract void deleteNumberObsUnitSetsCompleted();

	/**
	 * 
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#deleteNumberObsUnitSetsFailed()
	 */
	public abstract void deleteNumberObsUnitSetsFailed();

	/**
	 * 
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#deleteNumberSBsCompleted()
	 */
	public abstract void deleteNumberSBsCompleted();

	/**
	 * 
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#deleteNumberSBsFailed()
	 */
	public abstract void deleteNumberSBsFailed();

	/**
	 * 
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#deleteTotalObsUnitSets()
	 */
	public abstract void deleteTotalObsUnitSets();

	/**
	 * 
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#deleteTotalRequiredTimeInSec()
	 */
	public abstract void deleteTotalRequiredTimeInSec();

	/**
	 * 
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#deleteTotalSBs()
	 */
	public abstract void deleteTotalSBs();

	/**
	 * 
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#deleteTotalUsedTimeInSec()
	 */
	public abstract void deleteTotalUsedTimeInSec();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#enumerateSession()
	 */
	public abstract Enumeration enumerateSession();

	/**
	 * @param o
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public abstract boolean equals(Object o);

	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getAlmatype()
	 */
	public abstract String getAlmatype();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#getContainingObsUnitSetRef()
	 */
	public abstract OUSStatusRefT getContainingObsUnitSetRef();

	/**
	 * @return the status object of the containing ObsUnitSet
	 * @throws SchedulingException 
	 */
	public abstract OUSStatusI getContainingObsUnitSet() throws SchedulingException;

	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getNumberObsUnitSetsCompleted()
	 */
	public abstract int getNumberObsUnitSetsCompleted();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getNumberObsUnitSetsFailed()
	 */
	public abstract int getNumberObsUnitSetsFailed();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getNumberSBsCompleted()
	 */
	public abstract int getNumberSBsCompleted();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getNumberSBsFailed()
	 */
	public abstract int getNumberSBsFailed();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getObsUnitSetRef()
	 */
	public abstract ObsProjectRefT getObsUnitSetRef();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getOUSStatusChoice()
	 */
	public abstract OUSStatusChoice getOUSStatusChoice();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getOUSStatusEntity()
	 */
	public abstract OUSStatusEntityT getOUSStatusEntity();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getPipelineProcessingRequest()
	 */
	public abstract PipelineProcessingRequestT getPipelineProcessingRequest();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#getProjectStatusRef()
	 */
	public abstract ProjectStatusRefT getProjectStatusRef();

	/**
	 * @return the status object of the containing ObsProject
	 * @throws SchedulingException 
	 */
	public abstract ProjectStatusI getProjectStatus() throws SchedulingException;

	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getRevision()
	 */
	public abstract String getRevision();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getSchemaVersion()
	 */
	public abstract String getSchemaVersion();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getSession()
	 */
	public abstract SessionT[] getSession();

	/**
	 * @param index
	 * @return
	 * @throws IndexOutOfBoundsException
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getSession(int)
	 */
	public abstract SessionT getSession(int index)
			throws IndexOutOfBoundsException;

	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getSessionCount()
	 */
	public abstract int getSessionCount();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getTotalObsUnitSets()
	 */
	public abstract int getTotalObsUnitSets();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#getTotalSBs()
	 */
	public abstract int getTotalSBs();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#hasNumberObsUnitSetsCompleted()
	 */
	public abstract boolean hasNumberObsUnitSetsCompleted();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#hasNumberObsUnitSetsFailed()
	 */
	public abstract boolean hasNumberObsUnitSetsFailed();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#hasNumberSBsCompleted()
	 */
	public abstract boolean hasNumberSBsCompleted();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#hasNumberSBsFailed()
	 */
	public abstract boolean hasNumberSBsFailed();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#hasTotalObsUnitSets()
	 */
	public abstract boolean hasTotalObsUnitSets();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#hasTotalSBs()
	 */
	public abstract boolean hasTotalSBs();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#isValid()
	 */
	public abstract boolean isValid();

	/**
	 * @param handler
	 * @throws IOException
	 * @throws MarshalException
	 * @throws ValidationException
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#marshal(org.xml.sax.ContentHandler)
	 */
	public abstract void marshal(ContentHandler handler) throws IOException,
			MarshalException, ValidationException;

	/**
	 * @param out
	 * @throws MarshalException
	 * @throws ValidationException
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#marshal(java.io.Writer)
	 */
	public abstract void marshal(Writer out) throws MarshalException,
			ValidationException;

	/**
	 * @param vSession
	 * @return
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#removeSession(alma.entity.xmlbinding.ousstatus.SessionT)
	 */
	public abstract boolean removeSession(SessionT vSession);

	/**
	 * @param almatype
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setAlmatype(java.lang.String)
	 */
	public abstract void setAlmatype(String almatype);

	/**
	 * @param containingObsUnitSetRef
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#setContainingObsUnitSetRef(alma.entity.xmlbinding.ousstatus.OUSStatusRefT)
	 */
	public abstract void setContainingObsUnitSetRef(
			OUSStatusRefT containingObsUnitSetRef);

	/**
	 * @param containingObsUnitSet
	 */
	public abstract void setContainingObsUnitSet(
			OUSStatusI containingObsUnitSet);

	/**
	 * @param numberObsUnitSetsCompleted
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setNumberObsUnitSetsCompleted(int)
	 */
	public abstract void setNumberObsUnitSetsCompleted(
			int numberObsUnitSetsCompleted);

	/**
	 * @param numberObsUnitSetsFailed
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setNumberObsUnitSetsFailed(int)
	 */
	public abstract void setNumberObsUnitSetsFailed(int numberObsUnitSetsFailed);

	/**
	 * @param numberSBsCompleted
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setNumberSBsCompleted(int)
	 */
	public abstract void setNumberSBsCompleted(int numberSBsCompleted);

	/**
	 * @param numberSBsFailed
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setNumberSBsFailed(int)
	 */
	public abstract void setNumberSBsFailed(int numberSBsFailed);

	/**
	 * @param obsUnitSetRef
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setObsUnitSetRef(alma.entity.xmlbinding.obsproject.ObsProjectRefT)
	 */
	public abstract void setObsUnitSetRef(ObsProjectRefT obsUnitSetRef);

	/**
	 * @param OUSStatusChoice
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setOUSStatusChoice(alma.entity.xmlbinding.ousstatus.OUSStatusChoice)
	 */
	public abstract void setOUSStatusChoice(OUSStatusChoice OUSStatusChoice);

	/**
	 * @param OUSStatusEntity
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setOUSStatusEntity(alma.entity.xmlbinding.ousstatus.OUSStatusEntityT)
	 */
	public abstract void setOUSStatusEntity(OUSStatusEntityT OUSStatusEntity);

	/**
	 * @param pipelineProcessingRequest
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setPipelineProcessingRequest(alma.entity.xmlbinding.ousstatus.PipelineProcessingRequestT)
	 */
	public abstract void setPipelineProcessingRequest(
			PipelineProcessingRequestT pipelineProcessingRequest);

	/**
	 * @param projectStatusRef
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#setProjectStatusRef(alma.entity.xmlbinding.projectstatus.ProjectStatusRefT)
	 */
	public abstract void setProjectStatusRef(ProjectStatusRefT projectStatusRef);

	/**
	 * @param projectStatusI
	 */
	public abstract void setProjectStatus(ProjectStatusI projectStatus);

	/**
	 * @param revision
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setRevision(java.lang.String)
	 */
	public abstract void setRevision(String revision);

	/**
	 * @param schemaVersion
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setSchemaVersion(java.lang.String)
	 */
	public abstract void setSchemaVersion(String schemaVersion);

	/**
	 * @param index
	 * @param vSession
	 * @throws IndexOutOfBoundsException
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setSession(int, alma.entity.xmlbinding.ousstatus.SessionT)
	 */
	public abstract void setSession(int index, SessionT vSession)
			throws IndexOutOfBoundsException;

	/**
	 * @param sessionArray
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setSession(alma.entity.xmlbinding.ousstatus.SessionT[])
	 */
	public abstract void setSession(SessionT[] sessionArray);

	/**
	 * @param totalObsUnitSets
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setTotalObsUnitSets(int)
	 */
	public abstract void setTotalObsUnitSets(int totalObsUnitSets);

	/**
	 * @param totalSBs
	 * @see alma.entity.xmlbinding.ousstatus.OUSStatus#setTotalSBs(int)
	 */
	public abstract void setTotalSBs(int totalSBs);

	/**
	 * @throws ValidationException
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#validate()
	 */
	public abstract void validate() throws ValidationException;

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#getObsUnitSetMemberType()
	 */
	public abstract String getObsUnitSetMemberType();

	/**
	 * @param obsUnitSetMemberType
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#setObsUnitSetMemberType(java.lang.String)
	 */
	public abstract void setObsUnitSetMemberType(String obsUnitSetMemberType);

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.StatusBaseT#getTimeOfUpdate()
	 */
	public abstract String getTimeOfUpdate();

	/**
	 * @param timeOfUpdate
	 * @see alma.entity.xmlbinding.projectstatus.StatusBaseT#setTimeOfUpdate(java.lang.String)
	 */
	public abstract void setTimeOfUpdate(String timeOfUpdate);

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.StatusBaseT#getStatus()
	 */
	public abstract StatusT getStatus();

	/**
	 * @param status
	 * @see alma.entity.xmlbinding.projectstatus.StatusBaseT#setStatus(alma.entity.xmlbinding.valuetypes.StatusT)
	 */
	public abstract void setStatus(StatusT status);

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#getTotalRequiredTimeInSec()
	 */
	public abstract int getTotalRequiredTimeInSec();

	/**
	 * @param totalRequiredTimeInSec
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#setTotalRequiredTimeInSec(int)
	 */
	public abstract void setTotalRequiredTimeInSec(int totalRequiredTimeInSec);

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#hasTotalRequiredTimeInSec()
	 */
	public abstract boolean hasTotalRequiredTimeInSec();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#getTotalUsedTimeInSec()
	 */
	public abstract int getTotalUsedTimeInSec();

	/**
	 * @param totalUsedTimeInSec
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#setTotalUsedTimeInSec(int)
	 */
	public abstract void setTotalUsedTimeInSec(int totalUsedTimeInSec);

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#hasTotalUsedTimeInSec()
	 */
	public abstract boolean hasTotalUsedTimeInSec();
	public abstract void addOUSStatus(int index, OUSStatusI vOUSStatus)
	throws IndexOutOfBoundsException;

	public abstract void addOUSStatus(OUSStatusI vOUSStatus)
	throws IndexOutOfBoundsException;

	public abstract void addSBStatus(int index, SBStatusI vSBStatus)
	throws IndexOutOfBoundsException;

	public abstract void addSBStatus(SBStatusI vSBStatus)
	throws IndexOutOfBoundsException;

	public abstract void clearOUSStatus();

	public abstract void clearSBStatus();

	public abstract Enumeration enumerateOUSStatus() throws IndexOutOfBoundsException, SchedulingException;

	public abstract Enumeration enumerateSBStatus() throws IndexOutOfBoundsException, SchedulingException;

	public abstract OUSStatusI[] getOUSStatus() throws IndexOutOfBoundsException, SchedulingException;

	public abstract OUSStatusI getOUSStatus(int index)
	throws IndexOutOfBoundsException, SchedulingException;

	public abstract int getOUSStatusCount();

	public abstract SBStatusI[] getSBStatus() throws IndexOutOfBoundsException, SchedulingException;

	public abstract SBStatusI getSBStatus(int index)
	throws IndexOutOfBoundsException, SchedulingException;

	public abstract int getSBStatusCount();

	public abstract boolean removeOUSStatus(OUSStatusI vOUSStatus);

	public abstract boolean removeSBStatus(SBStatusI vSBStatus);

	public abstract void setOUSStatus(int index, OUSStatusI vOUSStatus)
	throws IndexOutOfBoundsException;

	public abstract void setOUSStatus(OUSStatusI[] OUSStatusArray);

	public abstract void setSBStatus(int index, SBStatusI vSBStatus)
	throws IndexOutOfBoundsException;

	public abstract void setSBStatus(SBStatusI[] SBStatusArray);

    /*
	 * End of Being a proxy
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Conversion between local cached and remote versions
	 * ================================================================
	 */
	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.statusIF.StatusBaseI#asLocal()
	 */
	public OUSStatusI asLocal();

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.statusIF.StatusBaseI#asRemote()
	 */
	public OUSStatusI asRemote();
	/*
	 * End of Conversion between local cached and remote versions
	 * ============================================================= */
}