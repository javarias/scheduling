/* ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2006 
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 */
package alma.scheduling.datamodel.obsproject.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.omg.CORBA.UserException;

import alma.ACSErrTypeCommon.IllegalArgumentEx;
import alma.acs.entityutil.EntityException;
import alma.entity.xmlbinding.obsproject.ObsProject;
import alma.entity.xmlbinding.obsproposal.ObsProposal;
import alma.entity.xmlbinding.obsreview.ObsReview;
import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.entity.xmlbinding.schedblock.SchedBlock;
import alma.scheduling.utils.SchedulingProperties.Phase1SBSourceValue;
import alma.xmlstore.ArchiveInternalError;

public interface ArchiveInterface {

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
	public abstract boolean hasObsProposal(String id);

	/**
	 * Remember the given ObsProposal.
	 * 
	 * @param op - the ObsProposal to cache
	 */
	public abstract void cache(ObsProposal op);

	/**
	 * Get the specified ObsProposal. Will go looking in the archive if
	 * necessary.
	 * 
	 * @param id - the String id of the ObsProposal we're after.
	 * @throws EntityException
	 * @throws UserException
	 * @return The indicated ObsProposal.
	 */
	public abstract ObsProposal getObsProposal(String id)
			throws EntityException, UserException;

	/**
	 * Get the specified ObsProposal. Doesn't look in the archive.
	 * 
	 * @param id - the String id of the ObsProposal we're after.
	 * @return The indicated ObsProposal, or <code>null</code> if it's
	 *         not in the cache.
	 */
	public abstract ObsProposal cachedObsProposal(String id);

	/**
	 * Clear the given ObsProposal from the cache. Does not check that
	 * the given id is actually that of an ObsProposal, or that the
	 * cache actually contains such an ObsProposal. 
	 * 
	 * @param id
	 */
	public abstract void forgetObsProposal(String id);

	/**
	 * Clear the given ObsProposal from the cache. Does not check that
	 * the given id is actually that of an ObsProposal, or that the
	 * cache actually contains such an ObsProposal.
	 * 
	 * @param op - the ObsProposal to forget about.
	 */
	public abstract void forgetObsProposal(ObsProposal op);

	/**
	 * Refresh any cache of the given ObsProposal - i.e. drop the
	 * current copy from the cache and fetch a new copy from the archive.
	 * 
	 * @param id
	 * @return the newly refreshed ObsProposal
	 * @throws UserException 
	 * @throws EntityException 
	 */
	public abstract ObsProposal refreshObsProposal(String id)
			throws EntityException, UserException;

	/**
	 * Refresh any cache of the given ObsProposal - i.e. drop the
	 * current copy from the cache and fetch a new copy from the archive.
	 * 
	 * @param op
	 * @return the newly refreshed ObsProposal
	 * @throws UserException 
	 * @throws EntityException 
	 */
	public abstract ObsProposal refreshObsProposal(ObsProposal op)
			throws EntityException, UserException;

	/**
	 * How many ObsProposals do we have cached?
	 * 
	 * @return int
	 */
	public abstract int numObsProposals();

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
	public abstract boolean hasObsReview(String id);

	/**
	 * Remember the given ObsReview.
	 * 
	 * @param op - the ObsReview to cache
	 */
	public abstract void cache(ObsReview or);

	/**
	 * Get the specified ObsReview. Will go looking in the archive if
	 * necessary.
	 * 
	 * @param id - the String id of the ObsReview we're after.
	 * @throws EntityException
	 * @throws UserException
	 * @return The indicated ObsReview.
	 */
	public abstract ObsReview getObsReview(String id) throws EntityException,
			UserException;

	/**
	 * Get the specified ObsReview. Doesn't look in the archive.
	 * 
	 * @param id - the String id of the ObsReview we're after.
	 * @return The indicated ObsReview, or <code>null</code> if it's
	 *         not in the cache.
	 */
	public abstract ObsReview cachedObsReview(String id);

	/**
	 * Clear the given ObsReview from the cache. Does not check that
	 * the given id is actually that of an ObsReview, or that the
	 * cache actually contains such an ObsReview. 
	 * 
	 * @param id
	 */
	public abstract void forgetObsReview(String id);

	/**
	 * Clear the given ObsReview from the cache. Does not check that
	 * the given id is actually that of an ObsReview, or that the
	 * cache actually contains such an ObsReview.
	 * 
	 * @param op - the ObsReview to forget about.
	 */
	public abstract void forgetObsReview(ObsReview or);

	/**
	 * Refresh any cache of the given ObsReview - i.e. drop the
	 * current copy from the cache and fetch a new copy from the archive.
	 * 
	 * @param id
	 * @return the newly refreshed ObsReview
	 * @throws UserException 
	 * @throws EntityException 
	 */
	public abstract ObsReview refreshObsReview(String id)
			throws EntityException, UserException;

	/**
	 * Refresh any cache of the given ObsReview - i.e. drop the
	 * current copy from the cache and fetch a new copy from the archive.
	 * 
	 * @param op
	 * @return the newly refreshed ObsReview
	 * @throws UserException 
	 * @throws EntityException 
	 */
	public abstract ObsReview refreshObsReview(ObsReview or)
			throws EntityException, UserException;

	/**
	 * How many ObsReviews do we have cached?
	 * 
	 * @return int
	 */
	public abstract int numObsReviews();

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
	public abstract boolean hasObsProject(String id);

	/**
	 * Remember the given ObsProject.
	 * 
	 * @param op - the ObsProject to cache
	 */
	public abstract void cache(ObsProject op);

	/**
	 * Get the specified ObsProject. Will go looking in the archive if
	 * necessary.
	 * 
	 * @param id - the String id of the ObsProject we're after.
	 * @throws EntityException
	 * @throws UserException
	 * @return The indicated ObsProject.
	 */
	public abstract ObsProject getObsProject(String id) throws EntityException,
			UserException;

	/**
	 * Get the specified ObsProject. Doesn't look in the archive.
	 * 
	 * @param id - the String id of the ObsProject we're after.
	 * @return The indicated ObsProject, or <code>null</code> if it's
	 *         not in the cache.
	 */
	public abstract ObsProject cachedObsProject(String id);

	/**
	 * Clear the given ObsProject from the cache. Does not check that
	 * the given id is actually that of an ObsProject, or that the
	 * cache actually contains such an ObsProject. 
	 * 
	 * @param id
	 */
	public abstract void forgetObsProject(String id);

	/**
	 * Clear the given ObsProject from the cache. Does not check that
	 * the given id is actually that of an ObsProject, or that the
	 * cache actually contains such an ObsProject.
	 * 
	 * @param op - the ObsProject to forget about.
	 */
	public abstract void forgetObsProject(ObsProject op);

	/**
	 * Refresh any cache of the given ObsProject - i.e. drop the
	 * current copy from the cache and fetch a new copy from the archive.
	 * 
	 * @param id
	 * @return the newly refreshed ObsProject
	 * @throws UserException 
	 * @throws EntityException 
	 */
	public abstract ObsProject refreshObsProject(String id)
			throws EntityException, UserException;

	/**
	 * Refresh any cache of the given ObsProject - i.e. drop the
	 * current copy from the cache and fetch a new copy from the archive.
	 * 
	 * @param op
	 * @return the newly refreshed ObsProject
	 * @throws UserException 
	 * @throws EntityException 
	 */
	public abstract ObsProject refreshObsProject(ObsProject op)
			throws EntityException, UserException;

	/**
	 * How many ObsProjects do we have cached?
	 * 
	 * @return int
	 */
	public abstract int numObsProjects();

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
	public abstract boolean hasSchedBlock(String id);

	/**
	 * Remember the given SchedBlock.
	 * 
	 * @param op - the SchedBlock to cache
	 */
	public abstract void cache(SchedBlock sb);

	/**
	 * Get the specified SchedBlock. Will go looking in the archive if
	 * necessary.
	 * 
	 * @param id - the String id of the SchedBlock we're after.
	 * @throws EntityException
	 * @throws UserException
	 * @return The indicated SchedBlock.
	 */
	public abstract SchedBlock getSchedBlock(String id) throws EntityException,
			UserException;

	/**
	 * Get the specified SchedBlock. Doesn't look in the archive.
	 * 
	 * @param id - the String id of the SchedBlock we're after.
	 * @return The indicated SchedBlock, or <code>null</code> if it's
	 *         not in the cache.
	 */
	public abstract SchedBlock cachedSchedBlock(String id);

	/**
	 * Clear the given SchedBlock from the cache. Does not check that
	 * the given id is actually that of an SchedBlock, or that the
	 * cache actually contains such an SchedBlock. 
	 * 
	 * @param id
	 */
	public abstract void forgetSchedBlock(String id);

	/**
	 * Clear the given SchedBlock from the cache. Does not check that
	 * the given id is actually that of an SchedBlock, or that the
	 * cache actually contains such an SchedBlock.
	 * 
	 * @param op - the SchedBlock to forget about.
	 */
	public abstract void forgetSchedBlock(SchedBlock op);

	/**
	 * Refresh any cache of the given SchedBlock - i.e. drop the
	 * current copy from the cache and fetch a new copy from the archive.
	 * 
	 * @param id
	 * @return the newly refreshed SchedBlock
	 * @throws UserException 
	 * @throws EntityException 
	 */
	public abstract SchedBlock refreshSchedBlock(String id)
			throws EntityException, UserException;

	/**
	 * Refresh any cache of the given SchedBlock - i.e. drop the
	 * current copy from the cache and fetch a new copy from the archive.
	 * 
	 * @param op
	 * @return the newly refreshed SchedBlock
	 * @throws UserException 
	 * @throws EntityException 
	 */
	public abstract SchedBlock refreshSchedBlock(SchedBlock op)
			throws EntityException, UserException;

	/**
	 * How many SchedBlocks do we have cached?
	 * 
	 * @return int
	 */
	public abstract int numSchedBlocks();

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
	public abstract boolean hasProjectStatus(String id);

	/**
	 * Remember the given ProjectStatus.
	 * 
	 * @param op - the ProjectStatus to cache
	 */
	public abstract void cache(ProjectStatus ps);

	/**
	 * Get the specified ProjectStatus. Will go looking in the state
	 * archive if necessary.
	 * 
	 * @param id - the String id of the ProjectStatus we're after.
	 * @throws EntityException
	 * @throws UserException
	 * @return The indicated ProjectStatus.
	 */
	public abstract ProjectStatus getProjectStatus(String id)
			throws EntityException, UserException;

	/**
	 * Get the specified ProjectStatus. Doesn't look in the state
	 * archive.
	 * 
	 * @param id - the String id of the ProjectStatus we're after.
	 * @return The indicated ProjectStatus, or <code>null</code> if it's
	 *         not in the cache.
	 */
	public abstract ProjectStatus cachedProjectStatus(String id);

	/**
	 * Clear the given ProjectStatus from the cache. Does not check that
	 * the given id is actually that of an ProjectStatus, or that the
	 * cache actually contains such an ProjectStatus. 
	 * 
	 * @param id
	 */
	public abstract void forgetProjectStatus(String id);

	/**
	 * Clear the given ProjectStatus from the cache. Does not check that
	 * the given id is actually that of an ProjectStatus, or that the
	 * cache actually contains such an ProjectStatus.
	 * 
	 * @param op - the ProjectStatus to forget about.
	 */
	public abstract void forgetProjectStatus(ProjectStatus op);

	/**
	 * Refresh any cache of the given ProjectStatus - i.e. drop the
	 * current copy from the cache and fetch a new copy from the archive.
	 * 
	 * @param id
	 * @return the newly refreshed ProjectStatus
	 * @throws UserException 
	 * @throws EntityException 
	 */
	public abstract ProjectStatus refreshProjectStatus(String id)
			throws EntityException, UserException;

	/**
	 * Refresh any cache of the given ProjectStatus - i.e. drop the
	 * current copy from the cache and fetch a new copy from the archive.
	 * 
	 * @param op
	 * @return the newly refreshed ProjectStatus
	 * @throws UserException 
	 * @throws EntityException 
	 */
	public abstract ProjectStatus refreshProjectStatus(ProjectStatus op)
			throws EntityException, UserException;

	/**
	 * How many ProjectStatuses do we have cached?
	 * 
	 * @return int
	 */
	public abstract int numProjectStatuses();

	/**
	 * Write the given ProjectStatus back to the StateArchive.
	 * 
	 * @param status - the ProjectStatus to cache
	 * @throws EntityException 
	 * @throws UserException 
	 */
	public abstract void write(ProjectStatus status) throws EntityException,
			UserException;

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
	public abstract boolean hasOUSStatus(String id);

	/**
	 * Remember the given OUSStatus.
	 * 
	 * @param op - the OUSStatus to cache
	 */
	public abstract void cache(OUSStatus os);

	/**
	 * Get the specified OUSStatus. Will go looking in the state
	 * archive if necessary.
	 * 
	 * @param id - the String id of the OUSStatus we're after.
	 * @throws EntityException
	 * @throws UserException
	 * @return The indicated OUSStatus.
	 */
	public abstract OUSStatus getOUSStatus(String id) throws EntityException,
			UserException;

	/**
	 * Get the specified OUSStatus. Doesn't look in the state
	 * archive.
	 * 
	 * @param id - the String id of the OUSStatus we're after.
	 * @return The indicated OUSStatus, or <code>null</code> if it's
	 *         not in the cache.
	 */
	public abstract OUSStatus cachedOUSStatus(String id);

	/**
	 * Clear the given OUSStatus from the cache. Does not check that
	 * the given id is actually that of an OUSStatus, or that the
	 * cache actually contains such an OUSStatus. 
	 * 
	 * @param id
	 */
	public abstract void forgetOUSStatus(String id);

	/**
	 * Clear the given OUSStatus from the cache. Does not check that
	 * the given id is actually that of an OUSStatus, or that the
	 * cache actually contains such an OUSStatus.
	 * 
	 * @param op - the OUSStatus to forget about.
	 */
	public abstract void forgetOUSStatus(OUSStatus op);

	/**
	 * Refresh any cache of the given OUSStatus - i.e. drop the
	 * current copy from the cache and fetch a new copy from the archive.
	 * 
	 * @param id
	 * @return the newly refreshed OUSStatus
	 * @throws UserException 
	 * @throws EntityException 
	 */
	public abstract OUSStatus refreshOUSStatus(String id)
			throws EntityException, UserException;

	/**
	 * Refresh any cache of the given OUSStatus - i.e. drop the
	 * current copy from the cache and fetch a new copy from the archive.
	 * 
	 * @param op
	 * @return the newly refreshed OUSStatus
	 * @throws UserException 
	 * @throws EntityException 
	 */
	public abstract OUSStatus refreshOUSStatus(OUSStatus op)
			throws EntityException, UserException;

	/**
	 * How many OUSStatuses do we have cached?
	 * 
	 * @return int
	 */
	public abstract int numOUSStatuses();

	/**
	 * Write the given OUSStatus back to the StateArchive.
	 * 
	 * @param status - the OUSStatus to cache
	 * @throws EntityException 
	 * @throws UserException 
	 */
	public abstract void write(OUSStatus status) throws EntityException,
			UserException;

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
	public abstract boolean hasSBStatus(String id);

	/**
	 * Remember the given SBStatus.
	 * 
	 * @param sb - the SBStatus to cache
	 */
	public abstract void cache(SBStatus sb);

	/**
	 * Get the specified SBStatus. Will go looking in the state
	 * archive if necessary.
	 * 
	 * @param id - the String id of the SBStatus we're after.
	 * @throws EntityException
	 * @throws UserException
	 * @return The indicated SBStatus.
	 */
	public abstract SBStatus getSBStatus(String id) throws EntityException,
			UserException;

	/**
	 * Get the specified SBStatus. Doesn't look in the state
	 * archive.
	 * 
	 * @param id - the String id of the SBStatus we're after.
	 * @return The indicated SBStatus, or <code>null</code> if it's
	 *         not in the cache.
	 */
	public abstract SBStatus cachedSBStatus(String id);

	/**
	 * Clear the given SBStatus from the cache. Does not check that
	 * the given id is actually that of an SBStatus, or that the
	 * cache actually contains such an SBStatus. 
	 * 
	 * @param id
	 */
	public abstract void forgetSBStatus(String id);

	/**
	 * Clear the given SBStatus from the cache. Does not check that
	 * the given id is actually that of an SBStatus, or that the
	 * cache actually contains such an SBStatus.
	 * 
	 * @param op - the SBStatus to forget about.
	 */
	public abstract void forgetSBStatus(SBStatus op);

	/**
	 * Refresh any cache of the given SBStatus - i.e. drop the
	 * current copy from the cache and fetch a new copy from the archive.
	 * 
	 * @param id
	 * @return the newly refreshed SBStatus
	 * @throws UserException 
	 * @throws EntityException 
	 */
	public abstract SBStatus refreshSBStatus(String id) throws EntityException,
			UserException;

	/**
	 * Refresh any cache of the given SBStatus - i.e. drop the
	 * current copy from the cache and fetch a new copy from the archive.
	 * 
	 * @param op
	 * @return the newly refreshed SBStatus
	 * @throws UserException 
	 * @throws EntityException 
	 */
	public abstract SBStatus refreshSBStatus(SBStatus op)
			throws EntityException, UserException;

	/**
	 * How many SBStatuses do we have cached?
	 * 
	 * @return int
	 */
	public abstract int numSBStatuses();

	/**
	 * Write the given SBStatus back to the StateArchive.
	 * 
	 * @param status - the SBStatus to cache
	 * @throws EntityException 
	 * @throws UserException 
	 */
	public abstract void write(SBStatus status) throws EntityException,
			UserException;

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
	public abstract Collection<String> getProjectStatusIdsByState(
			String[] states) throws IllegalArgumentEx;

	/**
	 * Return the ids of all the ObsProjects which have changed since
	 * the given time.
	 * 
	 * @param since
	 * @return
	 * @throws ArchiveInternalError 
	 */
	public abstract List<String> getIdsOfChangedProjects(Date since)
			throws UserException;

	/**
	 * Return the ids of all the SchedBlocks which have changed since
	 * the given time.
	 * 
	 * @param since
	 * @return
	 */
	public abstract List<String> getIdsOfChangedSBs(Date since)
			throws UserException;

	/* End of Compound operations
	 * ============================================================= */

	/*
	 * ================================================================
	 * Iteration
	 * ================================================================
	 */
	public abstract Iterable<ObsProposal> obsProposals();

	public abstract Iterable<ObsReview> obsReviews();

	public abstract Iterable<ObsProject> obsProjects();

	public abstract Iterable<SchedBlock> schedBlocks();

	public abstract Collection<ProjectStatus> projectStatuses();

	public abstract Iterable<OUSStatus> ousStatuses();

	public abstract Iterable<SBStatus> sbStatuses();

	/* End of iteration
	 * ============================================================= */

	/*
	 * ================================================================
	 * Location of Phase 1 SBs
	 * ================================================================
	 */
	public abstract void rememberPhase1Location(String projectId,
			Phase1SBSourceValue location);

	public abstract Phase1SBSourceValue getPhase1Location(String projectId);
	/* End of Location of Phase 1 SBs
	 * ============================================================= */

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
			throws EntityException;
	
	/**
	 * Clean up the resources used the this archive interface. <br/>
	 * After calling this method, the archive interface could be not usable anymore
	 */
	public void tidyUp();

}