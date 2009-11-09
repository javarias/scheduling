package alma.scheduling.AlmaScheduling.statusIF;

import java.io.IOException;
import java.io.Writer;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.ContentHandler;

import alma.entity.xmlbinding.obsproject.ObsProjectRefT;
import alma.entity.xmlbinding.obsproposal.ObsProposalRefT;
import alma.entity.xmlbinding.ousstatus.OUSStatusRefT;
import alma.entity.xmlbinding.projectstatus.ProjectStatusEntityT;
import alma.entity.xmlbinding.valuetypes.StatusT;
import alma.scheduling.Define.SchedulingException;

public interface ProjectStatusI extends StatusBaseI {

	/*
	 * ================================================================
	 * Being a proxy
	 * ================================================================
	 */
	/**
	 * @param o
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public abstract boolean equals(Object o);

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#getAlmatype()
	 */
	public abstract String getAlmatype();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#getBreakpointTime()
	 */
	public abstract String getBreakpointTime();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#getName()
	 */
	public abstract String getName();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#getObsProgramStatusRef()
	 */
	public abstract OUSStatusRefT getObsProgramStatusRef();

	/**
	 * @return the status for top level OUS (aka the obsProgram) 
	 * @throws SchedulingException 
	 */
	public abstract OUSStatusI getObsProgramStatus() throws SchedulingException;

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#getObsProjectRef()
	 */
	public abstract ObsProjectRefT getObsProjectRef();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#getObsProposalRef()
	 */
	public abstract ObsProposalRefT getObsProposalRef();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#getPI()
	 */
	public abstract String getPI();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#getProjectStatusEntity()
	 */
	public abstract ProjectStatusEntityT getProjectStatusEntity();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#getProjectWasTimedOut()
	 */
	public abstract String getProjectWasTimedOut();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#getRevision()
	 */
	public abstract String getRevision();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#getSchemaVersion()
	 */
	public abstract String getSchemaVersion();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.StatusBaseT#getStatus()
	 */
	public abstract StatusT getStatus();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.StatusBaseT#getTimeOfUpdate()
	 */
	public abstract String getTimeOfUpdate();

	/**
	 * @return
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#isValid()
	 */
	public abstract boolean isValid();

	/**
	 * @param handler
	 * @throws IOException
	 * @throws MarshalException
	 * @throws ValidationException
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#marshal(org.xml.sax.ContentHandler)
	 */
	public abstract void marshal(ContentHandler handler) throws IOException,
			MarshalException, ValidationException;

	/**
	 * @param out
	 * @throws MarshalException
	 * @throws ValidationException
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#marshal(java.io.Writer)
	 */
	public abstract void marshal(Writer out) throws MarshalException,
			ValidationException;

	/**
	 * @param almatype
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#setAlmatype(java.lang.String)
	 */
	public abstract void setAlmatype(String almatype);

	/**
	 * @param breakpointTime
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#setBreakpointTime(java.lang.String)
	 */
	public abstract void setBreakpointTime(String breakpointTime);

	/**
	 * @param name
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#setName(java.lang.String)
	 */
	public abstract void setName(String name);

	/**
	 * @param obsProgramStatusRef
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#setObsProgramStatusRef(alma.entity.xmlbinding.ousstatus.OUSStatusRefT)
	 */
	public abstract void setObsProgramStatusRef(
			OUSStatusRefT obsProgramStatusRef);

	/**
	 * @param obsProgramStatus
	 */
	public abstract void setObsProgramStatus(
			OUSStatusI obsProgramStatus);

	/**
	 * @param obsProjectRef
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#setObsProjectRef(alma.entity.xmlbinding.obsproject.ObsProjectRefT)
	 */
	public abstract void setObsProjectRef(ObsProjectRefT obsProjectRef);

	/**
	 * @param obsProposalRef
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#setObsProposalRef(alma.entity.xmlbinding.obsproposal.ObsProposalRefT)
	 */
	public abstract void setObsProposalRef(ObsProposalRefT obsProposalRef);

	/**
	 * @param PI
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#setPI(java.lang.String)
	 */
	public abstract void setPI(String PI);

	/**
	 * @param projectStatusEntity
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#setProjectStatusEntity(alma.entity.xmlbinding.projectstatus.ProjectStatusEntityT)
	 */
	public abstract void setProjectStatusEntity(
			ProjectStatusEntityT projectStatusEntity);

	/**
	 * @param projectWasTimedOut
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#setProjectWasTimedOut(java.lang.String)
	 */
	public abstract void setProjectWasTimedOut(String projectWasTimedOut);

	/**
	 * @param revision
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#setRevision(java.lang.String)
	 */
	public abstract void setRevision(String revision);

	/**
	 * @param schemaVersion
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#setSchemaVersion(java.lang.String)
	 */
	public abstract void setSchemaVersion(String schemaVersion);

	/**
	 * @param status
	 * @see alma.entity.xmlbinding.projectstatus.StatusBaseT#setStatus(alma.entity.xmlbinding.valuetypes.StatusT)
	 */
	public abstract void setStatus(StatusT status);

	/**
	 * @param timeOfUpdate
	 * @see alma.entity.xmlbinding.projectstatus.StatusBaseT#setTimeOfUpdate(java.lang.String)
	 */
	public abstract void setTimeOfUpdate(String timeOfUpdate);

	/**
	 * @throws ValidationException
	 * @see alma.entity.xmlbinding.projectstatus.ProjectStatus#validate()
	 */
	public abstract void validate() throws ValidationException;
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
	public ProjectStatusI asLocal();

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.statusIF.StatusBaseI#asRemote()
	 */
	public ProjectStatusI asRemote();
	/*
	 * End of Conversion between local cached and remote versions
	 * ============================================================= */
}