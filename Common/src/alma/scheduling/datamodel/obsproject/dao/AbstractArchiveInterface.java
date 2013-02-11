/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
/**
 * 
 */
package alma.scheduling.datamodel.obsproject.dao;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.omg.CORBA.UserException;

import alma.ACSErrTypeCommon.IllegalArgumentEx;
import alma.ACSErrTypeCommon.wrappers.AcsJIllegalArgumentEx;
import alma.acs.entityutil.EntityDeserializer;
import alma.acs.entityutil.EntityException;
import alma.acs.entityutil.EntitySerializer;
import alma.entity.xmlbinding.obsproject.ObsProject;
import alma.entity.xmlbinding.obsproject.ObsProjectEntityT;
import alma.entity.xmlbinding.obsproposal.ObsProposal;
import alma.entity.xmlbinding.obsproposal.ObsProposalEntityT;
import alma.entity.xmlbinding.obsreview.ObsReview;
import alma.entity.xmlbinding.obsreview.ObsReviewEntityT;
import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.entity.xmlbinding.ousstatus.OUSStatusEntityT;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.projectstatus.ProjectStatusEntityT;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.entity.xmlbinding.sbstatus.SBStatusEntityT;
import alma.entity.xmlbinding.schedblock.SchedBlock;
import alma.entity.xmlbinding.schedblock.SchedBlockEntityT;
import alma.lifecycle.persistence.StateArchive;
import alma.lifecycle.stateengine.StateEngine;
import alma.lifecycle.stateengine.constants.Subsystem;
import alma.projectlifecycle.StateSystemOperations;
import alma.scheduling.utils.SchedulingProperties.Phase1SBSourceValue;
import alma.statearchiveexceptions.InappropriateEntityTypeEx;
import alma.statearchiveexceptions.NoSuchEntityEx;
import alma.statearchiveexceptions.NullEntityIdEx;
import alma.statearchiveexceptions.wrappers.AcsJInappropriateEntityTypeEx;
import alma.statearchiveexceptions.wrappers.AcsJNoSuchEntityEx;
import alma.statearchiveexceptions.wrappers.AcsJNullEntityIdEx;
import alma.statearchiveexceptions.wrappers.AcsJStateIOFailedEx;
import alma.statearchiveexceptions.wrappers.AcsJstatearchiveexceptionsEx;
import alma.xmlentity.XmlEntityStruct;
import alma.xmlstore.OperationalOperations;

/**
 * @author dclarke
 *
 */
public abstract class AbstractArchiveInterface implements ArchiveInterface  {
    
	/*
	 * ================================================================
	 * Constants
	 * ================================================================
	 */
    @SuppressWarnings("unused") // field is here for the future
	protected static String projectQuery  = "/prj:ObsProject";
    protected static String projectSchema = "ObsProject";
    @SuppressWarnings("unused") // field is here for the future
	protected static String sbQuery       = "/sbl:SchedBlock";
    protected static String sbSchema      = "SchedBlock";
	/* End of Constants
	 * ============================================================= */

    
    
	/*
	 * ================================================================
	 * Caches for the various Entities
	 * ================================================================
	 */
	protected Map<String, ObsProposal>   obsProposals;
	protected Map<String, ObsReview>     obsReviews;
	protected Map<String, ObsProject>    obsProjects;
	protected Map<String, SchedBlock>    schedBlocks;
	protected Map<String, ProjectStatus> projectStatuses;
	protected Map<String, OUSStatus>     ousStatuses;
	protected Map<String, SBStatus>      sbStatuses;
	/* End of caches for the various Entities
	 * ============================================================= */

    
    
	/*
	 * ================================================================
	 * Where to look for Phase 1 SBs
	 * ================================================================
	 */
	private Map<String, Phase1SBSourceValue> phase1SBSources;
	/* End of Where to look for Phase 1 SBs
	 * ============================================================= */


	
	/*
	 * ================================================================
	 * Other fields
	 * ================================================================
	 */
	/** The connection to the archive */
	protected OperationalOperations archive;
	
    /** Something to deserialize objects */
	protected EntityDeserializer entityDeserializer;
    
    /** Something to serialize objects */
	protected EntitySerializer entitySerializer;

	/** The connection to the state system */
//	protected StateSystemOperations stateSystem;
	protected StateArchive stateArchive;
	
	/** How to lay out dates */
	protected DateFormat dateFormat;
	/* End of other fields
	 * ============================================================= */


	
	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	public AbstractArchiveInterface(OperationalOperations archive,
            				StateArchive stateArchive,
            				EntityDeserializer    entityDeserializer,
            				EntitySerializer      entitySerializer) {
		this.archive     = archive;
		this.stateArchive = stateArchive;
		this.entityDeserializer = entityDeserializer;
		this.entitySerializer   = entitySerializer;
		this.dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

		obsProposals    = new HashMap<String, ObsProposal>();
		obsReviews      = new HashMap<String, ObsReview>();
		obsProjects     = new HashMap<String, ObsProject>();
		schedBlocks     = new HashMap<String, SchedBlock>();
		projectStatuses = new HashMap<String, ProjectStatus>();
		ousStatuses     = new HashMap<String, OUSStatus>();
		sbStatuses      = new HashMap<String, SBStatus>();
		phase1SBSources = null;
	}

	protected AbstractArchiveInterface(AbstractArchiveInterface that) {
		this.archive            = that.archive;
		this.stateArchive       = that.stateArchive;
		this.entityDeserializer = that.entityDeserializer;
		this.dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

		obsProposals    = new HashMap<String, ObsProposal>();
		obsReviews      = new HashMap<String, ObsReview>();
		obsProjects     = new HashMap<String, ObsProject>();
		schedBlocks     = new HashMap<String, SchedBlock>();
		projectStatuses = new HashMap<String, ProjectStatus>();
		ousStatuses     = new HashMap<String, OUSStatus>();
		sbStatuses      = new HashMap<String, SBStatus>();
		phase1SBSources = null;
	}
	/* End of construction
	 * ============================================================= */


	
	/*
	 * ================================================================
	 * ObsProposals
	 * ================================================================
	 */
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#hasObsProposal(java.lang.String)
	 */
	@Override
	public boolean hasObsProposal(String id) {
		return obsProposals.containsKey(id);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#cache(alma.entity.xmlbinding.obsproposal.ObsProposal)
	 */
	@Override
	public void cache(ObsProposal op) {
		final ObsProposalEntityT ent = op.getObsProposalEntity();
		obsProposals.put(ent.getEntityId(), op);
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#cachedObsProposal(java.lang.String)
	 */
	@Override
	public ObsProposal cachedObsProposal(String id) {
		return obsProposals.get(id);
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#forgetObsProposal(java.lang.String)
	 */
	@Override
	public void forgetObsProposal(String id) {
		obsProposals.remove(id);
	}
	
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#forgetObsProposal(alma.entity.xmlbinding.obsproposal.ObsProposal)
	 */
	@Override
	public void forgetObsProposal(ObsProposal op) {
		final ObsProposalEntityT ent = op.getObsProposalEntity();
		forgetObsProposal(ent.getEntityId());
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#refreshObsProposal(java.lang.String)
	 */
	@Override
	public ObsProposal refreshObsProposal(String id)
								throws EntityException, UserException {
		forgetObsProposal(id);
		return getObsProposal(id);
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#refreshObsProposal(alma.entity.xmlbinding.obsproposal.ObsProposal)
	 */
	@Override
	public ObsProposal refreshObsProposal(ObsProposal op)
								throws EntityException, UserException {
		final ObsProposalEntityT ent = op.getObsProposalEntity();
		return refreshObsProposal(ent.getEntityId());
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#numObsProposals()
	 */
	@Override
	public int numObsProposals() {
		return obsProposals.size();
	}
	/* End of ObsProposals
	 * ============================================================= */


	
	/*
	 * ================================================================
	 * ObsReviews
	 * ================================================================
	 */
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#hasObsReview(java.lang.String)
	 */
	@Override
	public boolean hasObsReview(String id) {
		return obsReviews.containsKey(id);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#cache(alma.entity.xmlbinding.obsreview.ObsReview)
	 */
	@Override
	public void cache(ObsReview or) {
		final ObsReviewEntityT ent = or.getObsReviewEntity();
		obsReviews.put(ent.getEntityId(), or);
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#cachedObsReview(java.lang.String)
	 */
	@Override
	public ObsReview cachedObsReview(String id) {
		return obsReviews.get(id);
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#forgetObsReview(java.lang.String)
	 */
	@Override
	public void forgetObsReview(String id) {
		obsReviews.remove(id);
	}
	
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#forgetObsReview(alma.entity.xmlbinding.obsreview.ObsReview)
	 */
	@Override
	public void forgetObsReview(ObsReview or) {
		final ObsReviewEntityT ent = or.getObsReviewEntity();
		forgetObsReview(ent.getEntityId());
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#refreshObsReview(java.lang.String)
	 */
	@Override
	public ObsReview refreshObsReview(String id)
								throws EntityException, UserException {
		forgetObsReview(id);
		return getObsReview(id);
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#refreshObsReview(alma.entity.xmlbinding.obsreview.ObsReview)
	 */
	@Override
	public ObsReview refreshObsReview(ObsReview or)
								throws EntityException, UserException {
		final ObsReviewEntityT ent = or.getObsReviewEntity();
		return refreshObsReview(ent.getEntityId());
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#numObsReviews()
	 */
	@Override
	public int numObsReviews() {
		return obsReviews.size();
	}
	/* End of ObsReviews
	 * ============================================================= */


	
	/*
	 * ================================================================
	 * ObsProjects
	 * ================================================================
	 */
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#hasObsProject(java.lang.String)
	 */
	@Override
	public boolean hasObsProject(String id) {
		return obsProjects.containsKey(id);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#cache(alma.entity.xmlbinding.obsproject.ObsProject)
	 */
	@Override
	public void cache(ObsProject op) {
		final ObsProjectEntityT ent = op.getObsProjectEntity();
		obsProjects.put(ent.getEntityId(), op);
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#cachedObsProject(java.lang.String)
	 */
	@Override
	public ObsProject cachedObsProject(String id) {
		return obsProjects.get(id);
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#forgetObsProject(java.lang.String)
	 */
	@Override
	public void forgetObsProject(String id) {
		obsProjects.remove(id);
	}
	
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#forgetObsProject(alma.entity.xmlbinding.obsproject.ObsProject)
	 */
	@Override
	public void forgetObsProject(ObsProject op) {
		final ObsProjectEntityT ent = op.getObsProjectEntity();
		forgetObsProject(ent.getEntityId());
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#refreshObsProject(java.lang.String)
	 */
	@Override
	public ObsProject refreshObsProject(String id)
								throws EntityException, UserException {
		forgetObsProject(id);
		return getObsProject(id);
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#refreshObsProject(alma.entity.xmlbinding.obsproject.ObsProject)
	 */
	@Override
	public ObsProject refreshObsProject(ObsProject op)
								throws EntityException, UserException {
		final ObsProjectEntityT ent = op.getObsProjectEntity();
		return refreshObsProject(ent.getEntityId());
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#numObsProjects()
	 */
	@Override
	public int numObsProjects() {
		return obsProjects.size();
	}
	/* End of ObsProjects
	 * ============================================================= */


	
	/*
	 * ================================================================
	 * SchedBlocks
	 * ================================================================
	 */
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#hasSchedBlock(java.lang.String)
	 */
	@Override
	public boolean hasSchedBlock(String id) {
		return schedBlocks.containsKey(id);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#cache(alma.entity.xmlbinding.schedblock.SchedBlock)
	 */
	@Override
	public void cache(SchedBlock sb) {
		final SchedBlockEntityT ent = sb.getSchedBlockEntity();
		schedBlocks.put(ent.getEntityId(), sb);
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#cachedSchedBlock(java.lang.String)
	 */
	@Override
	public SchedBlock cachedSchedBlock(String id) {
		return schedBlocks.get(id);
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#forgetSchedBlock(java.lang.String)
	 */
	@Override
	public void forgetSchedBlock(String id) {
		schedBlocks.remove(id);
	}
	
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#forgetSchedBlock(alma.entity.xmlbinding.schedblock.SchedBlock)
	 */
	@Override
	public void forgetSchedBlock(SchedBlock op) {
		final SchedBlockEntityT ent = op.getSchedBlockEntity();
		forgetSchedBlock(ent.getEntityId());
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#refreshSchedBlock(java.lang.String)
	 */
	@Override
	public SchedBlock refreshSchedBlock(String id)
								throws EntityException, UserException {
		forgetSchedBlock(id);
		return getSchedBlock(id);
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#refreshSchedBlock(alma.entity.xmlbinding.schedblock.SchedBlock)
	 */
	@Override
	public SchedBlock refreshSchedBlock(SchedBlock op)
								throws EntityException, UserException {
		final SchedBlockEntityT ent = op.getSchedBlockEntity();
		return refreshSchedBlock(ent.getEntityId());
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#numSchedBlocks()
	 */
	@Override
	public int numSchedBlocks() {
		return schedBlocks.size();
	}
	/* End of SchedBlocks
	 * ============================================================= */


	
	/*
	 * ================================================================
	 * ProjectStatuses
	 * ================================================================
	 */
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#getProjectStatus(java.lang.String)
	 */
	@Override
	public ProjectStatus getProjectStatus(String id)
								throws EntityException, UserException {
		ProjectStatus result = null;
		if (hasProjectStatus(id)) {
			result = cachedProjectStatus(id);
		} else {
			ProjectStatusEntityT idT = new ProjectStatusEntityT();  idT.setEntityId(id);
			try {
				result = stateArchive.getProjectStatus(idT);
			} catch (AcsJstatearchiveexceptionsEx ex) {
				throw new EntityException(ex);
			}
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#hasProjectStatus(java.lang.String)
	 */
	@Override
	public boolean hasProjectStatus(String id) {
		return projectStatuses.containsKey(id);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#cache(alma.entity.xmlbinding.projectstatus.ProjectStatus)
	 */
	@Override
	public void cache(ProjectStatus ps) {
		final ProjectStatusEntityT ent = ps.getProjectStatusEntity();
		projectStatuses.put(ent.getEntityId(), ps);
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#cachedProjectStatus(java.lang.String)
	 */
	@Override
	public ProjectStatus cachedProjectStatus(String id) {
		return projectStatuses.get(id);
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#forgetProjectStatus(java.lang.String)
	 */
	@Override
	public void forgetProjectStatus(String id) {
		projectStatuses.remove(id);
	}
	
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#forgetProjectStatus(alma.entity.xmlbinding.projectstatus.ProjectStatus)
	 */
	@Override
	public void forgetProjectStatus(ProjectStatus op) {
		final ProjectStatusEntityT ent = op.getProjectStatusEntity();
		forgetProjectStatus(ent.getEntityId());
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#refreshProjectStatus(java.lang.String)
	 */
	@Override
	public ProjectStatus refreshProjectStatus(String id)
								throws EntityException, UserException {
		forgetProjectStatus(id);
		return getProjectStatus(id);
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#refreshProjectStatus(alma.entity.xmlbinding.projectstatus.ProjectStatus)
	 */
	@Override
	public ProjectStatus refreshProjectStatus(ProjectStatus op)
								throws EntityException, UserException {
		final ProjectStatusEntityT ent = op.getProjectStatusEntity();
		return refreshProjectStatus(ent.getEntityId());
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#numProjectStatuses()
	 */
	@Override
	public int numProjectStatuses() {
		return projectStatuses.size();
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#write(alma.entity.xmlbinding.projectstatus.ProjectStatus)
	 */
	@Override
	public void write(ProjectStatus status)
			throws EntityException, UserException {
		try {
			stateArchive.insertOrUpdate(status, Subsystem.SCHEDULING);
			refreshProjectStatus(status);	// Forces a refresh when asked
		} catch (AcsJstatearchiveexceptionsEx ex) {
			throw new EntityException(ex);
		} 
	}
	/* End of ProjectStatuses
	 * ============================================================= */


	
	/*
	 * ================================================================
	 * OUSStatuses
	 * ================================================================
	 */
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#getOUSStatus(java.lang.String)
	 */
	@Override
	public OUSStatus getOUSStatus(String id)
								throws EntityException, UserException {
		OUSStatus result = null;
		if (hasOUSStatus(id)) {
			result = cachedOUSStatus(id);
		} else {
			OUSStatusEntityT idT = new OUSStatusEntityT(); idT.setEntityId(id);
			try {
				result = stateArchive.getOUSStatus(idT);
				ousStatuses.put(id, result);
			} catch (AcsJstatearchiveexceptionsEx ex) {
				throw new EntityException(ex);
			}
			
		}
		return result;
	}
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#hasOUSStatus(java.lang.String)
	 */
	@Override
	public boolean hasOUSStatus(String id) {
		return ousStatuses.containsKey(id);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#cache(alma.entity.xmlbinding.ousstatus.OUSStatus)
	 */
	@Override
	public void cache(OUSStatus os) {
		final OUSStatusEntityT ent = os.getOUSStatusEntity();
		ousStatuses.put(ent.getEntityId(), os);
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#cachedOUSStatus(java.lang.String)
	 */
	@Override
	public OUSStatus cachedOUSStatus(String id) {
		return ousStatuses.get(id);
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#forgetOUSStatus(java.lang.String)
	 */
	@Override
	public void forgetOUSStatus(String id) {
		ousStatuses.remove(id);
	}
	
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#forgetOUSStatus(alma.entity.xmlbinding.ousstatus.OUSStatus)
	 */
	@Override
	public void forgetOUSStatus(OUSStatus op) {
		final OUSStatusEntityT ent = op.getOUSStatusEntity();
		forgetOUSStatus(ent.getEntityId());
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#refreshOUSStatus(java.lang.String)
	 */
	@Override
	public OUSStatus refreshOUSStatus(String id)
								throws EntityException, UserException {
		forgetOUSStatus(id);
		return getOUSStatus(id);
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#refreshOUSStatus(alma.entity.xmlbinding.ousstatus.OUSStatus)
	 */
	@Override
	public OUSStatus refreshOUSStatus(OUSStatus op)
								throws EntityException, UserException {
		final OUSStatusEntityT ent = op.getOUSStatusEntity();
		return refreshOUSStatus(ent.getEntityId());
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#numOUSStatuses()
	 */
	@Override
	public int numOUSStatuses() {
		return ousStatuses.size();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#write(alma.entity.xmlbinding.ousstatus.OUSStatus)
	 */
	@Override
	public void write(OUSStatus status)
			throws EntityException, UserException {
		try {
			stateArchive.insertOrUpdate(status, Subsystem.SCHEDULING);
			refreshOUSStatus(status);	// Forces a refresh when asked
		} catch (AcsJstatearchiveexceptionsEx ex) {
			throw new EntityException(ex);
		} 
	}
	/* End of OUSStatuses
	 * ============================================================= */


	
	/*
	 * ================================================================
	 * SBStatuses
	 * ================================================================
	 */
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#getSBStatus(java.lang.String)
	 */
	@Override
	public SBStatus getSBStatus(String id)
								throws EntityException, UserException {
		SBStatus result = null;
		if (hasSBStatus(id)) {
			result = cachedSBStatus(id);
		} else {
			SBStatusEntityT idT = new SBStatusEntityT(); idT.setEntityId(id);
			try {
				result = stateArchive.getSBStatus(idT);
				sbStatuses.put(id, result);
			} catch (AcsJstatearchiveexceptionsEx ex ) {
				throw new EntityException(ex);
			} 
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#hasSBStatus(java.lang.String)
	 */
	@Override
	public boolean hasSBStatus(String id) {
		return sbStatuses.containsKey(id);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#cache(alma.entity.xmlbinding.sbstatus.SBStatus)
	 */
	@Override
	public void cache(SBStatus sb) {
		final SBStatusEntityT ent = sb.getSBStatusEntity();
		sbStatuses.put(ent.getEntityId(), sb);
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#cachedSBStatus(java.lang.String)
	 */
	@Override
	public SBStatus cachedSBStatus(String id) {
		return sbStatuses.get(id);
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#forgetSBStatus(java.lang.String)
	 */
	@Override
	public void forgetSBStatus(String id) {
		sbStatuses.remove(id);
	}
	
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#forgetSBStatus(alma.entity.xmlbinding.sbstatus.SBStatus)
	 */
	@Override
	public void forgetSBStatus(SBStatus op) {
		final SBStatusEntityT ent = op.getSBStatusEntity();
		forgetSBStatus(ent.getEntityId());
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#refreshSBStatus(java.lang.String)
	 */
	@Override
	public SBStatus refreshSBStatus(String id)
								throws EntityException, UserException {
		forgetSBStatus(id);
		return getSBStatus(id);
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#refreshSBStatus(alma.entity.xmlbinding.sbstatus.SBStatus)
	 */
	@Override
	public SBStatus refreshSBStatus(SBStatus op)
								throws EntityException, UserException {
		final SBStatusEntityT ent = op.getSBStatusEntity();
		return refreshSBStatus(ent.getEntityId());
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#numSBStatuses()
	 */
	@Override
	public int numSBStatuses() {
		return sbStatuses.size();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#write(alma.entity.xmlbinding.sbstatus.SBStatus)
	 */
	@Override
	public void write(SBStatus status)
			throws EntityException, UserException {
		try {
			stateArchive.insertOrUpdate(status, Subsystem.SCHEDULING);
			refreshSBStatus(status);	// Forces a refresh when asked
		} catch (AcsJstatearchiveexceptionsEx ex) {
			throw new EntityException(ex);
		} 
	}
	/* End of SBStatuses
	 * ============================================================= */


	
	/*
	 * ================================================================
	 * Compound operations
	 * ================================================================
	 */
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#getProjectStatusIdsByState(java.lang.String[])
	 */
    @Override
	public Collection<String> getProjectStatusIdsByState(String[] states)
    		throws InappropriateEntityTypeEx, IllegalArgumentEx {
        final Collection<String> result = new ArrayList<String>();
        
		ProjectStatus[] xml = null;
		try {
			xml = stateArchive.findProjectStatusByState(states);
			
			for (final ProjectStatus xes : xml) {
				result.add(xes.getProjectStatusEntity().getEntityId());
			}
			return result;
		} catch (AcsJIllegalArgumentEx ex) {
			throw ex.toIllegalArgumentEx();
		} catch (AcsJInappropriateEntityTypeEx ex) {
			throw ex.toInappropriateEntityTypeEx();
		}
	}
    
	/*
	 * (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#getSBStatusesForProjectStatus(java.lang.String)
	 */
    @Override
    public Collection<SBStatus> getSBStatusesForProjectStatus(String projectStatusId)
    		throws InappropriateEntityTypeEx,
    			   NullEntityIdEx,
    			   NoSuchEntityEx,
    			   EntityException {
        final Collection<SBStatus> result = new ArrayList<SBStatus>();
        ProjectStatusEntityT idT = new ProjectStatusEntityT(); idT.setEntityId(projectStatusId);
        SBStatus[] xmlList;
		try {
			xmlList = stateArchive.getSBStatusList(idT);
	        
	        for (final SBStatus sbs : xmlList) {
				sbStatuses.put(sbs.getSBStatusEntity().getEntityId(), sbs);
				result.add(sbs);
	        }
	        
			return result;
		} catch (AcsJNullEntityIdEx ex) {
			throw ex.toNullEntityIdEx();
		} catch (AcsJNoSuchEntityEx ex) {
			throw ex.toNoSuchEntityEx();
		} catch (AcsJInappropriateEntityTypeEx ex) {
			throw ex.toInappropriateEntityTypeEx();
		}
    }

	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#getIdsOfChangedProjects(java.util.Date)
	 */
	@Override
	public List<String> getIdsOfChangedProjects(Date since)
												 throws UserException {
		String when = dateFormat.format(since);
    	String[] ids = archive.queryRecent(projectSchema, when);
    	
 		return Arrays.asList(ids);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#getIdsOfChangedSBs(java.util.Date)
	 */
	@Override
	public List<String> getIdsOfChangedSBs(Date since)
												 throws UserException {
		String when = dateFormat.format(since);
    	String[] ids = archive.queryRecent(sbSchema, when);
 		return Arrays.asList(ids);
	}
	/* End of Compound operations
	 * ============================================================= */


	
	/*
	 * ================================================================
	 * Iteration
	 * ================================================================
	 */
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#obsProposals()
	 */
	@Override
	public Iterable<ObsProposal> obsProposals() {
		return obsProposals.values();
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#obsReviews()
	 */
	@Override
	public Iterable<ObsReview> obsReviews() {
		return obsReviews.values();
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#obsProjects()
	 */
	@Override
	public Iterable<ObsProject> obsProjects() {
		return obsProjects.values();
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#schedBlocks()
	 */
	@Override
	public Iterable<SchedBlock> schedBlocks() {
		return schedBlocks.values();
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#projectStatuses()
	 */
	@Override
	public Collection<ProjectStatus> projectStatuses() {
		return projectStatuses.values();
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#ousStatuses()
	 */
	@Override
	public Iterable<OUSStatus> ousStatuses() {
		return ousStatuses.values();
	}
	
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#sbStatuses()
	 */
	@Override
	public Iterable<SBStatus> sbStatuses() {
		return sbStatuses.values();
	}
	/* End of iteration
	 * ============================================================= */


	
	/*
	 * ================================================================
	 * Location of Phase 1 SBs
	 * ================================================================
	 */
	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#rememberPhase1Location(java.lang.String, alma.scheduling.utils.SchedulingProperties.Phase1SBSourceValue)
	 */
	@Override
	public void rememberPhase1Location(String              projectId,
			                           Phase1SBSourceValue location) {
		if (phase1SBSources == null) {
			phase1SBSources = new HashMap<String, Phase1SBSourceValue>();
		}
		phase1SBSources.put(projectId, location);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.ArchiveInterface#getPhase1Location(java.lang.String)
	 */
	@Override
	public Phase1SBSourceValue getPhase1Location(String projectId) {
		return phase1SBSources.get(projectId);
	}
	/* End of Location of Phase 1 SBs
	 * ============================================================= */
}
