package alma.scheduling.AlmaScheduling.statusIF;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.ContentHandler;

import alma.entity.xmlbinding.ousstatus.OUSStatusRefT;
import alma.entity.xmlbinding.projectstatus.ProjectStatusRefT;
import alma.entity.xmlbinding.sbstatus.ExecStatusT;
import alma.entity.xmlbinding.sbstatus.SBStatusEntityT;
import alma.entity.xmlbinding.schedblock.SchedBlockRefT;
import alma.entity.xmlbinding.valuetypes.StatusT;
import alma.scheduling.Define.SchedulingException;

public interface SBStatusI extends StatusBaseI {

	/*
	 * ================================================================
	 * Being a proxy
	 * ================================================================
	 */
	/**
	 * @param vExecStatus
	 * @throws IndexOutOfBoundsException
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#addExecStatus(alma.entity.xmlbinding.sbstatus.ExecStatusT)
	 */
	public abstract void addExecStatus(ExecStatusT vExecStatus)
			throws IndexOutOfBoundsException;

	/**
	 * @param index
	 * @param vExecStatus
	 * @throws IndexOutOfBoundsException
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#addExecStatus(int, alma.entity.xmlbinding.sbstatus.ExecStatusT)
	 */
	public abstract void addExecStatus(int index, ExecStatusT vExecStatus)
			throws IndexOutOfBoundsException;

	/**
	 * 
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#clearExecStatus()
	 */
	public abstract void clearExecStatus();

	/**
	 * 
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#deleteExecutionsRemaining()
	 */
	public abstract void deleteExecutionsRemaining();

	/**
	 * 
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#deleteTotalRequiredTimeInSec()
	 */
	public abstract void deleteTotalRequiredTimeInSec();

	/**
	 * 
	 * @see alma.entity.xmlbinding.projectstatus.ObsUnitStatusT#deleteTotalUsedTimeInSec()
	 */
	public abstract void deleteTotalUsedTimeInSec();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#enumerateExecStatus()
	 */
	public abstract Enumeration enumerateExecStatus();

	/**
	 * @param o
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public abstract boolean equals(Object o);

	/**
	 * @return
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#getAlmatype()
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
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#getExecStatus()
	 */
	public abstract ExecStatusT[] getExecStatus();

	/**
	 * @param index
	 * @return
	 * @throws IndexOutOfBoundsException
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#getExecStatus(int)
	 */
	public abstract ExecStatusT getExecStatus(int index)
			throws IndexOutOfBoundsException;

	/**
	 * @return
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#getExecStatusCount()
	 */
	public abstract int getExecStatusCount();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#getExecutionsRemaining()
	 */
	public abstract int getExecutionsRemaining();

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
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#getRevision()
	 */
	public abstract String getRevision();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#getSBStatusEntity()
	 */
	public abstract SBStatusEntityT getSBStatusEntity();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#getSchedBlockRef()
	 */
	public abstract SchedBlockRefT getSchedBlockRef();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#getSchemaVersion()
	 */
	public abstract String getSchemaVersion();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#hasExecutionsRemaining()
	 */
	public abstract boolean hasExecutionsRemaining();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#isValid()
	 */
	public abstract boolean isValid();

	/**
	 * @param handler
	 * @throws IOException
	 * @throws MarshalException
	 * @throws ValidationException
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#marshal(org.xml.sax.ContentHandler)
	 */
	public abstract void marshal(ContentHandler handler) throws IOException,
			MarshalException, ValidationException;

	/**
	 * @param out
	 * @throws MarshalException
	 * @throws ValidationException
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#marshal(java.io.Writer)
	 */
	public abstract void marshal(Writer out) throws MarshalException,
			ValidationException;

	/**
	 * @param vExecStatus
	 * @return
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#removeExecStatus(alma.entity.xmlbinding.sbstatus.ExecStatusT)
	 */
	public abstract boolean removeExecStatus(ExecStatusT vExecStatus);

	/**
	 * @param almatype
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#setAlmatype(java.lang.String)
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
	 * @param execStatusArray
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#setExecStatus(alma.entity.xmlbinding.sbstatus.ExecStatusT[])
	 */
	public abstract void setExecStatus(ExecStatusT[] execStatusArray);

	/**
	 * @param index
	 * @param vExecStatus
	 * @throws IndexOutOfBoundsException
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#setExecStatus(int, alma.entity.xmlbinding.sbstatus.ExecStatusT)
	 */
	public abstract void setExecStatus(int index, ExecStatusT vExecStatus)
			throws IndexOutOfBoundsException;

	/**
	 * @param executionsRemaining
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#setExecutionsRemaining(int)
	 */
	public abstract void setExecutionsRemaining(int executionsRemaining);

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
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#setRevision(java.lang.String)
	 */
	public abstract void setRevision(String revision);

	/**
	 * @param SBStatusEntity
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#setSBStatusEntity(alma.entity.xmlbinding.sbstatus.SBStatusEntityT)
	 */
	public abstract void setSBStatusEntity(SBStatusEntityT SBStatusEntity);

	/**
	 * @param schedBlockRef
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#setSchedBlockRef(alma.entity.xmlbinding.schedblock.SchedBlockRefT)
	 */
	public abstract void setSchedBlockRef(SchedBlockRefT schedBlockRef);

	/**
	 * @param schemaVersion
	 * @see alma.entity.xmlbinding.sbstatus.SBStatus#setSchemaVersion(java.lang.String)
	 */
	public abstract void setSchemaVersion(String schemaVersion);

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
	public SBStatusI asLocal();

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.statusIF.StatusBaseI#asRemote()
	 */
	public SBStatusI asRemote();
	/*
	 * End of Conversion between local cached and remote versions
	 * ============================================================= */
}