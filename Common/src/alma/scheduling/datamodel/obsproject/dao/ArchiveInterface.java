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
import alma.projectlifecycle.StateSystemOperations;
import alma.scheduling.utils.SchedulingProperties.Phase1SBSourceValue;
import alma.statearchiveexceptions.InappropriateEntityTypeEx;
import alma.statearchiveexceptions.NoSuchEntityEx;
import alma.statearchiveexceptions.NullEntityIdEx;
import alma.xmlentity.XmlEntityStruct;
import alma.xmlstore.ArchiveInternalError;
import alma.xmlstore.OperationalOperations;

/**
 * @author dclarke
 *
 */
public class ArchiveInterface  {
    
	/*
	 * ================================================================
	 * Constants
	 * ================================================================
	 */
    @SuppressWarnings("unused") // field is here for the future
	private static String projectQuery  = "/prj:ObsProject";
    private static String projectSchema = "ObsProject";
    @SuppressWarnings("unused") // field is here for the future
	private static String sbQuery       = "/sbl:SchedBlock";
    private static String sbSchema      = "SchedBlock";
	/* End of Constants
	 * ============================================================= */

    
    
	/*
	 * ================================================================
	 * Caches for the various Entities
	 * ================================================================
	 */
	private Map<String, ObsProposal>   obsProposals;
	private Map<String, ObsReview>     obsReviews;
	private Map<String, ObsProject>    obsProjects;
	private Map<String, SchedBlock>    schedBlocks;
	private Map<String, ProjectStatus> projectStatuses;
	private Map<String, OUSStatus>     ousStatuses;
	private Map<String, SBStatus>      sbStatuses;
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
	private OperationalOperations archive;
	
    /** Something to deserialize objects */
    private EntityDeserializer entityDeserializer;
    
    /** Something to serialize objects */
    private EntitySerializer entitySerializer;

	/** The connection to the state system */
	protected StateSystemOperations stateSystem;
	
	/** How to lay out dates */
	protected DateFormat dateFormat;
	/* End of other fields
	 * ============================================================= */


	
	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	public ArchiveInterface(OperationalOperations archive,
            				StateSystemOperations stateSystem,
            				EntityDeserializer    entityDeserializer,
            				EntitySerializer      entitySerializer) {
		this.archive     = archive;
		this.stateSystem = stateSystem;
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

	protected ArchiveInterface(ArchiveInterface that) {
		this.archive            = that.archive;
		this.stateSystem        = that.stateSystem;
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
	/**
	 * Do we have an ObsProposal with the specified id in the cache?
	 * Does not check that the given id is actually that of an
	 * ObsProposal.
	 * 
	 * @param id - the String id of the ObsProposal we're after.
	 * @return <code>true</code> if the indicated ObsProposal is in the
	 *         cache, <code>false</code> otherwise.
	 */
	public boolean hasObsProposal(String id) {
		return obsProposals.containsKey(id);
	}

	/**
	 * Remember the given ObsProposal.
	 * 
	 * @param op - the ObsProposal to cache
	 */
	public void cache(ObsProposal op) {
		final ObsProposalEntityT ent = op.getObsProposalEntity();
		obsProposals.put(ent.getEntityId(), op);
	}

	/**
	 * Get the specified ObsProposal. Will go looking in the archive if
	 * necessary.
	 * 
	 * @param id - the String id of the ObsProposal we're after.
	 * @throws EntityException
	 * @throws UserException
	 * @return The indicated ObsProposal.
	 */
	public ObsProposal getObsProposal(String id)
								throws EntityException, UserException {
		ObsProposal result = null;
		if (hasObsProposal(id)) {
			result = cachedObsProposal(id);
		} else {
			final XmlEntityStruct xml = archive.retrieve(id);
			result = (ObsProposal) entityDeserializer.
				deserializeEntity(xml, ObsProposal.class);
			obsProposals.put(id, result);
		}
		return result;
	}
	
	/**
	 * Get the specified ObsProposal. Doesn't look in the archive.
	 * 
	 * @param id - the String id of the ObsProposal we're after.
	 * @return The indicated ObsProposal, or <code>null</code> if it's
	 *         not in the cache.
	 */
	public ObsProposal cachedObsProposal(String id) {
		return obsProposals.get(id);
	}
	
	/**
	 * Clear the given ObsProposal from the cache. Does not check that
	 * the given id is actually that of an ObsProposal, or that the
	 * cache actually contains such an ObsProposal. 
	 * 
	 * @param id
	 */
	public void forgetObsProposal(String id) {
		obsProposals.remove(id);
	}
	
	
	/**
	 * Clear the given ObsProposal from the cache. Does not check that
	 * the given id is actually that of an ObsProposal, or that the
	 * cache actually contains such an ObsProposal.
	 * 
	 * @param op - the ObsProposal to forget about.
	 */
	public void forgetObsProposal(ObsProposal op) {
		final ObsProposalEntityT ent = op.getObsProposalEntity();
		forgetObsProposal(ent.getEntityId());
	}
	
	/**
	 * Refresh any cache of the given ObsProposal - i.e. drop the
	 * current copy from the cache and fetch a new copy from the archive.
	 * 
	 * @param id
	 * @return the newly refreshed ObsProposal
	 * @throws UserException 
	 * @throws EntityException 
	 */
	public ObsProposal refreshObsProposal(String id)
								throws EntityException, UserException {
		forgetObsProposal(id);
		return getObsProposal(id);
	}
	
	/**
	 * Refresh any cache of the given ObsProposal - i.e. drop the
	 * current copy from the cache and fetch a new copy from the archive.
	 * 
	 * @param op
	 * @return the newly refreshed ObsProposal
	 * @throws UserException 
	 * @throws EntityException 
	 */
	public ObsProposal refreshObsProposal(ObsProposal op)
								throws EntityException, UserException {
		final ObsProposalEntityT ent = op.getObsProposalEntity();
		return refreshObsProposal(ent.getEntityId());
	}
	
	/**
	 * How many ObsProposals do we have cached?
	 * 
	 * @return int
	 */
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
	/**
	 * Do we have an ObsReview with the specified id in the cache?
	 * Does not check that the given id is actually that of an
	 * ObsReview.
	 * 
	 * @param id - the String id of the ObsReview we're after.
	 * @return <code>true</code> if the indicated ObsReview is in the
	 *         cache, <code>false</code> otherwise.
	 */
	public boolean hasObsReview(String id) {
		return obsReviews.containsKey(id);
	}

	/**
	 * Remember the given ObsReview.
	 * 
	 * @param op - the ObsReview to cache
	 */
	public void cache(ObsReview or) {
		final ObsReviewEntityT ent = or.getObsReviewEntity();
		obsReviews.put(ent.getEntityId(), or);
	}

	/**
	 * Get the specified ObsReview. Will go looking in the archive if
	 * necessary.
	 * 
	 * @param id - the String id of the ObsReview we're after.
	 * @throws EntityException
	 * @throws UserException
	 * @return The indicated ObsReview.
	 */
	public ObsReview getObsReview(String id)
								throws EntityException, UserException {
		ObsReview result = null;
		if (hasObsReview(id)) {
			result = cachedObsReview(id);
		} else {
			final XmlEntityStruct xml = archive.retrieve(id);
			result = (ObsReview) entityDeserializer.
				deserializeEntity(xml, ObsReview.class);
			obsReviews.put(id, result);
		}
		return result;
	}
	
	/**
	 * Get the specified ObsReview. Doesn't look in the archive.
	 * 
	 * @param id - the String id of the ObsReview we're after.
	 * @return The indicated ObsReview, or <code>null</code> if it's
	 *         not in the cache.
	 */
	public ObsReview cachedObsReview(String id) {
		return obsReviews.get(id);
	}
	
	/**
	 * Clear the given ObsReview from the cache. Does not check that
	 * the given id is actually that of an ObsReview, or that the
	 * cache actually contains such an ObsReview. 
	 * 
	 * @param id
	 */
	public void forgetObsReview(String id) {
		obsReviews.remove(id);
	}
	
	
	/**
	 * Clear the given ObsReview from the cache. Does not check that
	 * the given id is actually that of an ObsReview, or that the
	 * cache actually contains such an ObsReview.
	 * 
	 * @param op - the ObsReview to forget about.
	 */
	public void forgetObsReview(ObsReview or) {
		final ObsReviewEntityT ent = or.getObsReviewEntity();
		forgetObsReview(ent.getEntityId());
	}
	
	/**
	 * Refresh any cache of the given ObsReview - i.e. drop the
	 * current copy from the cache and fetch a new copy from the archive.
	 * 
	 * @param id
	 * @return the newly refreshed ObsReview
	 * @throws UserException 
	 * @throws EntityException 
	 */
	public ObsReview refreshObsReview(String id)
								throws EntityException, UserException {
		forgetObsReview(id);
		return getObsReview(id);
	}
	
	/**
	 * Refresh any cache of the given ObsReview - i.e. drop the
	 * current copy from the cache and fetch a new copy from the archive.
	 * 
	 * @param op
	 * @return the newly refreshed ObsReview
	 * @throws UserException 
	 * @throws EntityException 
	 */
	public ObsReview refreshObsReview(ObsReview or)
								throws EntityException, UserException {
		final ObsReviewEntityT ent = or.getObsReviewEntity();
		return refreshObsReview(ent.getEntityId());
	}
	
	/**
	 * How many ObsReviews do we have cached?
	 * 
	 * @return int
	 */
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
	/**
	 * Do we have an ObsProject with the specified id in the cache?
	 * Does not check that the given id is actually that of an
	 * ObsProject.
	 * 
	 * @param id - the String id of the ObsProject we're after.
	 * @return <code>true</code> if the indicated ObsProject is in the
	 *         cache, <code>false</code> otherwise.
	 */
	public boolean hasObsProject(String id) {
		return obsProjects.containsKey(id);
	}

	/**
	 * Remember the given ObsProject.
	 * 
	 * @param op - the ObsProject to cache
	 */
	public void cache(ObsProject op) {
		final ObsProjectEntityT ent = op.getObsProjectEntity();
		obsProjects.put(ent.getEntityId(), op);
	}

	/**
	 * Get the specified ObsProject. Will go looking in the archive if
	 * necessary.
	 * 
	 * @param id - the String id of the ObsProject we're after.
	 * @throws EntityException
	 * @throws UserException
	 * @return The indicated ObsProject.
	 */
	public ObsProject getObsProject(String id)
								throws EntityException, UserException {
		ObsProject result = null;
		if (hasObsProject(id)) {
			result = cachedObsProject(id);
		} else {
			final XmlEntityStruct xml = archive.retrieve(id);
			result = (ObsProject) entityDeserializer.
				deserializeEntity(xml, ObsProject.class);
			obsProjects.put(id, result);
		}
		return result;
	}
	
	/**
	 * Get the specified ObsProject. Doesn't look in the archive.
	 * 
	 * @param id - the String id of the ObsProject we're after.
	 * @return The indicated ObsProject, or <code>null</code> if it's
	 *         not in the cache.
	 */
	public ObsProject cachedObsProject(String id) {
		return obsProjects.get(id);
	}
	
	/**
	 * Clear the given ObsProject from the cache. Does not check that
	 * the given id is actually that of an ObsProject, or that the
	 * cache actually contains such an ObsProject. 
	 * 
	 * @param id
	 */
	public void forgetObsProject(String id) {
		obsProjects.remove(id);
	}
	
	
	/**
	 * Clear the given ObsProject from the cache. Does not check that
	 * the given id is actually that of an ObsProject, or that the
	 * cache actually contains such an ObsProject.
	 * 
	 * @param op - the ObsProject to forget about.
	 */
	public void forgetObsProject(ObsProject op) {
		final ObsProjectEntityT ent = op.getObsProjectEntity();
		forgetObsProject(ent.getEntityId());
	}
	
	/**
	 * Refresh any cache of the given ObsProject - i.e. drop the
	 * current copy from the cache and fetch a new copy from the archive.
	 * 
	 * @param id
	 * @return the newly refreshed ObsProject
	 * @throws UserException 
	 * @throws EntityException 
	 */
	public ObsProject refreshObsProject(String id)
								throws EntityException, UserException {
		forgetObsProject(id);
		return getObsProject(id);
	}
	
	/**
	 * Refresh any cache of the given ObsProject - i.e. drop the
	 * current copy from the cache and fetch a new copy from the archive.
	 * 
	 * @param op
	 * @return the newly refreshed ObsProject
	 * @throws UserException 
	 * @throws EntityException 
	 */
	public ObsProject refreshObsProject(ObsProject op)
								throws EntityException, UserException {
		final ObsProjectEntityT ent = op.getObsProjectEntity();
		return refreshObsProject(ent.getEntityId());
	}
	
	/**
	 * How many ObsProjects do we have cached?
	 * 
	 * @return int
	 */
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
	/**
	 * Do we have a SchedBlock with the specified id in the cache?
	 * Does not check that the given id is actually that of a
	 * SchedBlock.
	 * 
	 * @param id - the String id of the SchedBlock we're after.
	 * @return <code>true</code> if the indicated SchedBlock is in the
	 *         cache, <code>false</code> otherwise.
	 */
	public boolean hasSchedBlock(String id) {
		return schedBlocks.containsKey(id);
	}

	/**
	 * Remember the given SchedBlock.
	 * 
	 * @param op - the SchedBlock to cache
	 */
	public void cache(SchedBlock sb) {
		final SchedBlockEntityT ent = sb.getSchedBlockEntity();
		schedBlocks.put(ent.getEntityId(), sb);
	}

	/**
	 * Get the specified SchedBlock. Will go looking in the archive if
	 * necessary.
	 * 
	 * @param id - the String id of the SchedBlock we're after.
	 * @throws EntityException
	 * @throws UserException
	 * @return The indicated SchedBlock.
	 */
	public SchedBlock getSchedBlock(String id)
								throws EntityException, UserException {
		SchedBlock result = null;
		if (hasSchedBlock(id)) {
			result = cachedSchedBlock(id);
		} else {
			final XmlEntityStruct xml = archive.retrieve(id);
			result = (SchedBlock) entityDeserializer.
				deserializeEntity(xml, SchedBlock.class);
			schedBlocks.put(id, result);
		}
		return result;
	}
	
	/**
	 * Get the specified SchedBlock. Doesn't look in the archive.
	 * 
	 * @param id - the String id of the SchedBlock we're after.
	 * @return The indicated SchedBlock, or <code>null</code> if it's
	 *         not in the cache.
	 */
	public SchedBlock cachedSchedBlock(String id) {
		return schedBlocks.get(id);
	}
	
	/**
	 * Clear the given SchedBlock from the cache. Does not check that
	 * the given id is actually that of an SchedBlock, or that the
	 * cache actually contains such an SchedBlock. 
	 * 
	 * @param id
	 */
	public void forgetSchedBlock(String id) {
		schedBlocks.remove(id);
	}
	
	
	/**
	 * Clear the given SchedBlock from the cache. Does not check that
	 * the given id is actually that of an SchedBlock, or that the
	 * cache actually contains such an SchedBlock.
	 * 
	 * @param op - the SchedBlock to forget about.
	 */
	public void forgetSchedBlock(SchedBlock op) {
		final SchedBlockEntityT ent = op.getSchedBlockEntity();
		forgetSchedBlock(ent.getEntityId());
	}
	
	/**
	 * Refresh any cache of the given SchedBlock - i.e. drop the
	 * current copy from the cache and fetch a new copy from the archive.
	 * 
	 * @param id
	 * @return the newly refreshed SchedBlock
	 * @throws UserException 
	 * @throws EntityException 
	 */
	public SchedBlock refreshSchedBlock(String id)
								throws EntityException, UserException {
		forgetSchedBlock(id);
		return getSchedBlock(id);
	}
	
	/**
	 * Refresh any cache of the given SchedBlock - i.e. drop the
	 * current copy from the cache and fetch a new copy from the archive.
	 * 
	 * @param op
	 * @return the newly refreshed SchedBlock
	 * @throws UserException 
	 * @throws EntityException 
	 */
	public SchedBlock refreshSchedBlock(SchedBlock op)
								throws EntityException, UserException {
		final SchedBlockEntityT ent = op.getSchedBlockEntity();
		return refreshSchedBlock(ent.getEntityId());
	}
	
	/**
	 * How many SchedBlocks do we have cached?
	 * 
	 * @return int
	 */
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
	/**
	 * Do we have a ProjectStatus with the specified id in the cache?
	 * Does not check that the given id is actually that of a
	 * ProjectStatus.
	 * 
	 * @param id - the String id of the ProjectStatus we're after.
	 * @return <code>true</code> if the indicated ProjectStatus is in the
	 *         cache, <code>false</code> otherwise.
	 */
	public boolean hasProjectStatus(String id) {
		return projectStatuses.containsKey(id);
	}

	/**
	 * Remember the given ProjectStatus.
	 * 
	 * @param op - the ProjectStatus to cache
	 */
	public void cache(ProjectStatus ps) {
		final ProjectStatusEntityT ent = ps.getProjectStatusEntity();
		projectStatuses.put(ent.getEntityId(), ps);
	}

	/**
	 * Get the specified ProjectStatus. Will go looking in the state
	 * archive if necessary.
	 * 
	 * @param id - the String id of the ProjectStatus we're after.
	 * @throws EntityException
	 * @throws UserException
	 * @return The indicated ProjectStatus.
	 */
	public ProjectStatus getProjectStatus(String id)
								throws EntityException, UserException {
		ProjectStatus result = null;
		if (hasProjectStatus(id)) {
			result = cachedProjectStatus(id);
		} else {
			final XmlEntityStruct xml = stateSystem.getProjectStatus(id);
			result = (ProjectStatus) entityDeserializer.
				deserializeEntity(xml, ProjectStatus.class);
			projectStatuses.put(id, result);
		}
		return result;
	}
	
	/**
	 * Get the specified ProjectStatus. Doesn't look in the state
	 * archive.
	 * 
	 * @param id - the String id of the ProjectStatus we're after.
	 * @return The indicated ProjectStatus, or <code>null</code> if it's
	 *         not in the cache.
	 */
	public ProjectStatus cachedProjectStatus(String id) {
		return projectStatuses.get(id);
	}
	
	/**
	 * Clear the given ProjectStatus from the cache. Does not check that
	 * the given id is actually that of an ProjectStatus, or that the
	 * cache actually contains such an ProjectStatus. 
	 * 
	 * @param id
	 */
	public void forgetProjectStatus(String id) {
		projectStatuses.remove(id);
	}
	
	
	/**
	 * Clear the given ProjectStatus from the cache. Does not check that
	 * the given id is actually that of an ProjectStatus, or that the
	 * cache actually contains such an ProjectStatus.
	 * 
	 * @param op - the ProjectStatus to forget about.
	 */
	public void forgetProjectStatus(ProjectStatus op) {
		final ProjectStatusEntityT ent = op.getProjectStatusEntity();
		forgetProjectStatus(ent.getEntityId());
	}
	
	/**
	 * Refresh any cache of the given ProjectStatus - i.e. drop the
	 * current copy from the cache and fetch a new copy from the archive.
	 * 
	 * @param id
	 * @return the newly refreshed ProjectStatus
	 * @throws UserException 
	 * @throws EntityException 
	 */
	public ProjectStatus refreshProjectStatus(String id)
								throws EntityException, UserException {
		forgetProjectStatus(id);
		return getProjectStatus(id);
	}
	
	/**
	 * Refresh any cache of the given ProjectStatus - i.e. drop the
	 * current copy from the cache and fetch a new copy from the archive.
	 * 
	 * @param op
	 * @return the newly refreshed ProjectStatus
	 * @throws UserException 
	 * @throws EntityException 
	 */
	public ProjectStatus refreshProjectStatus(ProjectStatus op)
								throws EntityException, UserException {
		final ProjectStatusEntityT ent = op.getProjectStatusEntity();
		return refreshProjectStatus(ent.getEntityId());
	}
	
	/**
	 * How many ProjectStatuses do we have cached?
	 * 
	 * @return int
	 */
	public int numProjectStatuses() {
		return projectStatuses.size();
	}
	
	/**
	 * Write the given ProjectStatus back to the StateArchive.
	 * 
	 * @param status - the ProjectStatus to cache
	 * @throws EntityException 
	 * @throws UserException 
	 */
	public void write(ProjectStatus status)
			throws EntityException, UserException {
		XmlEntityStruct e = entitySerializer.serializeEntity(
				status, status.getProjectStatusEntity());
		stateSystem.updateProjectStatus(e);
		refreshProjectStatus(status);	// Forces a refresh when asked
	}
	/* End of ProjectStatuses
	 * ============================================================= */


	
	/*
	 * ================================================================
	 * OUSStatuses
	 * ================================================================
	 */
	/**
	 * Do we have an OUSStatus with the specified id in the cache?
	 * Does not check that the given id is actually that of an
	 * OUSStatus.
	 * 
	 * @param id - the String id of the OUSStatus we're after.
	 * @return <code>true</code> if the indicated OUSStatus is in the
	 *         cache, <code>false</code> otherwise.
	 */
	public boolean hasOUSStatus(String id) {
		return ousStatuses.containsKey(id);
	}

	/**
	 * Remember the given OUSStatus.
	 * 
	 * @param op - the OUSStatus to cache
	 */
	public void cache(OUSStatus os) {
		final OUSStatusEntityT ent = os.getOUSStatusEntity();
		ousStatuses.put(ent.getEntityId(), os);
	}

	/**
	 * Get the specified OUSStatus. Will go looking in the state
	 * archive if necessary.
	 * 
	 * @param id - the String id of the OUSStatus we're after.
	 * @throws EntityException
	 * @throws UserException
	 * @return The indicated OUSStatus.
	 */
	public OUSStatus getOUSStatus(String id)
								throws EntityException, UserException {
		OUSStatus result = null;
		if (hasOUSStatus(id)) {
			result = cachedOUSStatus(id);
		} else {
			final XmlEntityStruct xml = stateSystem.getOUSStatus(id);
			result = (OUSStatus) entityDeserializer.
				deserializeEntity(xml, OUSStatus.class);
			ousStatuses.put(id, result);
		}
		return result;
	}
	
	/**
	 * Get the specified OUSStatus. Doesn't look in the state
	 * archive.
	 * 
	 * @param id - the String id of the OUSStatus we're after.
	 * @return The indicated OUSStatus, or <code>null</code> if it's
	 *         not in the cache.
	 */
	public OUSStatus cachedOUSStatus(String id) {
		return ousStatuses.get(id);
	}
	
	/**
	 * Clear the given OUSStatus from the cache. Does not check that
	 * the given id is actually that of an OUSStatus, or that the
	 * cache actually contains such an OUSStatus. 
	 * 
	 * @param id
	 */
	public void forgetOUSStatus(String id) {
		ousStatuses.remove(id);
	}
	
	
	/**
	 * Clear the given OUSStatus from the cache. Does not check that
	 * the given id is actually that of an OUSStatus, or that the
	 * cache actually contains such an OUSStatus.
	 * 
	 * @param op - the OUSStatus to forget about.
	 */
	public void forgetOUSStatus(OUSStatus op) {
		final OUSStatusEntityT ent = op.getOUSStatusEntity();
		forgetOUSStatus(ent.getEntityId());
	}
	
	/**
	 * Refresh any cache of the given OUSStatus - i.e. drop the
	 * current copy from the cache and fetch a new copy from the archive.
	 * 
	 * @param id
	 * @return the newly refreshed OUSStatus
	 * @throws UserException 
	 * @throws EntityException 
	 */
	public OUSStatus refreshOUSStatus(String id)
								throws EntityException, UserException {
		forgetOUSStatus(id);
		return getOUSStatus(id);
	}
	
	/**
	 * Refresh any cache of the given OUSStatus - i.e. drop the
	 * current copy from the cache and fetch a new copy from the archive.
	 * 
	 * @param op
	 * @return the newly refreshed OUSStatus
	 * @throws UserException 
	 * @throws EntityException 
	 */
	public OUSStatus refreshOUSStatus(OUSStatus op)
								throws EntityException, UserException {
		final OUSStatusEntityT ent = op.getOUSStatusEntity();
		return refreshOUSStatus(ent.getEntityId());
	}
	
	/**
	 * How many OUSStatuses do we have cached?
	 * 
	 * @return int
	 */
	public int numOUSStatuses() {
		return ousStatuses.size();
	}

	/**
	 * Write the given OUSStatus back to the StateArchive.
	 * 
	 * @param status - the OUSStatus to cache
	 * @throws EntityException 
	 * @throws UserException 
	 */
	public void write(OUSStatus status)
			throws EntityException, UserException {
		XmlEntityStruct e = entitySerializer.serializeEntity(
				status, status.getOUSStatusEntity());
		stateSystem.updateOUSStatus(e);
		refreshOUSStatus(status);	// Forces a refresh when asked
	}
	/* End of OUSStatuses
	 * ============================================================= */


	
	/*
	 * ================================================================
	 * SBStatuses
	 * ================================================================
	 */
	/**
	 * Do we have an SBStatus with the specified id in the cache?
	 * Does not check that the given id is actually that of an
	 * SBStatus.
	 * 
	 * @param id - the String id of the SBStatus we're after.
	 * @return <code>true</code> if the indicated SBStatus is in the
	 *         cache, <code>false</code> otherwise.
	 */
	public boolean hasSBStatus(String id) {
		return sbStatuses.containsKey(id);
	}

	/**
	 * Remember the given SBStatus.
	 * 
	 * @param sb - the SBStatus to cache
	 */
	public void cache(SBStatus sb) {
		final SBStatusEntityT ent = sb.getSBStatusEntity();
		sbStatuses.put(ent.getEntityId(), sb);
	}

	/**
	 * Get the specified SBStatus. Will go looking in the state
	 * archive if necessary.
	 * 
	 * @param id - the String id of the SBStatus we're after.
	 * @throws EntityException
	 * @throws UserException
	 * @return The indicated SBStatus.
	 */
	public SBStatus getSBStatus(String id)
								throws EntityException, UserException {
		SBStatus result = null;
		if (hasSBStatus(id)) {
			result = cachedSBStatus(id);
		} else {
			final XmlEntityStruct xml = stateSystem.getSBStatus(id);
			result = (SBStatus) entityDeserializer.
				deserializeEntity(xml, SBStatus.class);
			sbStatuses.put(id, result);
		}
		return result;
	}
	
	/**
	 * Get the specified SBStatus. Doesn't look in the state
	 * archive.
	 * 
	 * @param id - the String id of the SBStatus we're after.
	 * @return The indicated SBStatus, or <code>null</code> if it's
	 *         not in the cache.
	 */
	public SBStatus cachedSBStatus(String id) {
		return sbStatuses.get(id);
	}
	
	/**
	 * Clear the given SBStatus from the cache. Does not check that
	 * the given id is actually that of an SBStatus, or that the
	 * cache actually contains such an SBStatus. 
	 * 
	 * @param id
	 */
	public void forgetSBStatus(String id) {
		sbStatuses.remove(id);
	}
	
	
	/**
	 * Clear the given SBStatus from the cache. Does not check that
	 * the given id is actually that of an SBStatus, or that the
	 * cache actually contains such an SBStatus.
	 * 
	 * @param op - the SBStatus to forget about.
	 */
	public void forgetSBStatus(SBStatus op) {
		final SBStatusEntityT ent = op.getSBStatusEntity();
		forgetSBStatus(ent.getEntityId());
	}
	
	/**
	 * Refresh any cache of the given SBStatus - i.e. drop the
	 * current copy from the cache and fetch a new copy from the archive.
	 * 
	 * @param id
	 * @return the newly refreshed SBStatus
	 * @throws UserException 
	 * @throws EntityException 
	 */
	public SBStatus refreshSBStatus(String id)
								throws EntityException, UserException {
		forgetSBStatus(id);
		return getSBStatus(id);
	}
	
	/**
	 * Refresh any cache of the given SBStatus - i.e. drop the
	 * current copy from the cache and fetch a new copy from the archive.
	 * 
	 * @param op
	 * @return the newly refreshed SBStatus
	 * @throws UserException 
	 * @throws EntityException 
	 */
	public SBStatus refreshSBStatus(SBStatus op)
								throws EntityException, UserException {
		final SBStatusEntityT ent = op.getSBStatusEntity();
		return refreshSBStatus(ent.getEntityId());
	}
	
	/**
	 * How many SBStatuses do we have cached?
	 * 
	 * @return int
	 */
	public int numSBStatuses() {
		return sbStatuses.size();
	}

	/**
	 * Write the given SBStatus back to the StateArchive.
	 * 
	 * @param status - the SBStatus to cache
	 * @throws EntityException 
	 * @throws UserException 
	 */
	public void write(SBStatus status)
			throws EntityException, UserException {
		XmlEntityStruct e = entitySerializer.serializeEntity(
				status, status.getSBStatusEntity());
		stateSystem.updateSBStatus(e);
		refreshSBStatus(status);	// Forces a refresh when asked
	}
	/* End of SBStatuses
	 * ============================================================= */


	
	/*
	 * ================================================================
	 * Compound operations
	 * ================================================================
	 */
	/**
	 * Get the ids of all the project statuses that are in the state
	 * archive in a given set of states.
	 * 
	 * @param states - we are interested in ProjectStatuses in any of
	 *                these states.
	 * @return a Collection<String> containing the ids of all the
	 *         ProjectStatus entities found
	 * @throws IllegalArgumentEx 
	 * @throws InappropriateEntityTypeEx 
	 */
    public Collection<String> getProjectStatusIdsByState(String[] states)
    		throws InappropriateEntityTypeEx, IllegalArgumentEx {
        final Collection<String> result = new ArrayList<String>();
        
		XmlEntityStruct xml[] = null;
		xml = stateSystem.findProjectStatusByState(states);
		
		for (final XmlEntityStruct xes : xml) {
			result.add(xes.entityId);
		}
		return result;
	}
	
    /**
     * Get all of the SBStatuses associated with the given ProjectStatus.
     * 
     * @param projectStatusId - the id of the ProjectStatus under which
     *                          to search.
     * @return - the collected SBStatuses.
     * 
     * @throws InappropriateEntityTypeEx
     * @throws NullEntityIdEx
     * @throws NoSuchEntityEx
     * @throws EntityException
     */
    Collection<SBStatus> getSBStatusesForProjectStatus(String projectStatusId)
    		throws InappropriateEntityTypeEx,
    			   NullEntityIdEx,
    			   NoSuchEntityEx,
    			   EntityException {
        final Collection<SBStatus> result = new ArrayList<SBStatus>();
        final XmlEntityStruct[] xmlList =
        	stateSystem.getSBStatusListForProjectStatus(projectStatusId);
        
        for (final XmlEntityStruct xml : xmlList) {
			final SBStatus sbs = (SBStatus) entityDeserializer.
				deserializeEntity(xml, SBStatus.class);
			sbStatuses.put(sbs.getSBStatusEntity().getEntityId(), sbs);
			result.add(sbs);
        }
        
		return result;
    }

	/**
	 * Return the ids of all the ObsProjects which have changed since
	 * the given time.
	 * 
	 * @param since
	 * @return
	 * @throws ArchiveInternalError 
	 */
	public List<String> getIdsOfChangedProjects(Date since)
												 throws UserException {
		String when = dateFormat.format(since);
    	String[] ids = archive.queryRecent(projectSchema, when);
    	
 		return Arrays.asList(ids);
	}

	/**
	 * Return the ids of all the SchedBlocks which have changed since
	 * the given time.
	 * 
	 * @param since
	 * @return
	 */
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
	public Iterable<ObsProposal> obsProposals() {
		return obsProposals.values();
	}
	
	public Iterable<ObsReview> obsReviews() {
		return obsReviews.values();
	}
	
	public Iterable<ObsProject> obsProjects() {
		return obsProjects.values();
	}
	
	public Iterable<SchedBlock> schedBlocks() {
		return schedBlocks.values();
	}
	
	public Collection<ProjectStatus> projectStatuses() {
		return projectStatuses.values();
	}
	
	public Iterable<OUSStatus> ousStatuses() {
		return ousStatuses.values();
	}
	
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
	public void rememberPhase1Location(String              projectId,
			                           Phase1SBSourceValue location) {
		if (phase1SBSources == null) {
			phase1SBSources = new HashMap<String, Phase1SBSourceValue>();
		}
		phase1SBSources.put(projectId, location);
	}

	public Phase1SBSourceValue getPhase1Location(String projectId) {
		return phase1SBSources.get(projectId);
	}
	/* End of Location of Phase 1 SBs
	 * ============================================================= */
}
