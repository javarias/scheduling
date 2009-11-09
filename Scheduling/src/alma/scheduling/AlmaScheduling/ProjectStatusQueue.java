/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 * File ProjectStatusQueue.java
 */

package alma.scheduling.AlmaScheduling;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import alma.acs.logging.AcsLogger;
import alma.entity.xmlbinding.projectstatus.ProjectStatusRefT;
import alma.scheduling.AlmaScheduling.statusIF.ProjectStatusI;
import alma.scheduling.Define.SchedulingException;

/**
 * The ProjectStatusQueue class is a queue of Project statuses, held in memory,
 * that can be accessed and updated by multiple threads, viz., the 
 * MasterScheduler and Scheduler objects.
 * 
 * @author David Clarke
 * @version $Id: ProjectStatusQueue.java,v 1.12 2009/11/09 22:58:45 rhiriart Exp $
 */
public class ProjectStatusQueue {

	private final AcsLogger logger;

	/** A map from the status */
	private final Map<String, ProjectStatusI> statusQueue;

	/**
	 * Create an empty queue of ProjectStatuses.
	 */
	public ProjectStatusQueue(AcsLogger logger) {
		this.logger = logger;
		
		// We use a LinkedHashMap to preserve the insertion order of
		// the elements. This may or may not prove to be important, but
		// somehow calling a class a something-Queue and not
		// maintaining order seems wrong.
		statusQueue = new LinkedHashMap<String, ProjectStatusI>();
	}

	/**
	 * Create a queue of ProjectStatusI from the specified array.
	 */
	public ProjectStatusQueue(ProjectStatusI[] statuses, AcsLogger logger) {
		this(logger);
		for (ProjectStatusI ps : statuses) {
			addUnsynchronised(ps);
		}
	}

	/**
	 * Add an ProjectStatusI to this queue.
	 * NOTE: THIS METHOD IS NOT SYNCHRONISED
	 * @param ps The ProjectStatusI to be added.
	 */
	private void addUnsynchronised(ProjectStatusI ps) {
		if (ps == null) {
			logger.warning("Trying to add a null ProjectStatusI to the ProjectStatusQueue - not added");
			return;
		} 
		if (ps.getProjectStatusEntity() == null) {
			logger.warning("Trying to add an ProjectStatusI with no Entity object to the ProjectStatusQueue - not added");
			return;
		} 
		final String key = ps.getProjectStatusEntity().getEntityId();
		if (key == null) {
			logger.warning("Trying to add an ProjectStatusI with no EntityId to the ProjectStatusQueue - not added");
			return;
		} 
		statusQueue.put(key, ps);
	}

	/**
	 * Add an ProjectStatusI to this queue.
	 * @param ps The ProjectStatusI to be added.
	 */
	public synchronized void add(ProjectStatusI ps) {
		addUnsynchronised(ps);
	}

	/**
	 * Add an array of ProjectStatusI to this queue.
	 * @param statuses The array to be added.
	 */
	public synchronized void add(ProjectStatusI[] statuses) {
		for (ProjectStatusI ps : statuses) {
			addUnsynchronised(ps);
		}
	}

	/**
	 * Remove the ProjectStatusI with the specified entity-id from the list.
	 * This operation does not destroy the ProjectStatusI. 
	 * @param entityId The entity-id of the ProjectStatusI to be removed.
	 */
	public synchronized void remove(String entityId) {
		statusQueue.remove(entityId);
	}

	/**
	 * Clear all ProjectStatusI from the queue.
	 */
	public synchronized void clear() {
		statusQueue.clear();
	}

	/**
	 * Update the queue to match <code>that</code>.
	 */
	public synchronized void updateWith(ProjectStatusQueue that) {
		// 1. Remove things from this which aren't in that
		final Collection<String> idsToRemove = new HashSet<String>();
		for (final String thisId : this.getAllIds()) {
			if (!that.isExists(thisId)) {
				// thisId is not in that, so mark for removal from this
				idsToRemove.add(thisId);
			}
		}
		for (final String thisId : idsToRemove) {
			this.remove(thisId);
		}
		
		// 2. Add things from that which aren't already in this
		for (final String thatId : that.getAllIds()) {
			if (!this.isExists(thatId)) {
				// thatId is not in this, so add it
				this.add(that.get(thatId));
			}
		}
	}

	/**
	 * Get the ProjectStatusI with the specified entity-id.
	 * @param entityId The entity-id of the ProjectStatusI to be returned.
	 * @return The ProjectStatusI with the specified entity-id or null
	 * if there is no such entity.
	 */
	public synchronized ProjectStatusI get(String entityId) {
		//System.out.println("getting ps "+entityId);
		//System.out.println("get in ProjectSQueue: queue size = "+queue.size());
		return statusQueue.get(entityId);
	}

	/**
	 * @param ref A reference to the desired ProjectStatusI
	 * @return The ProjectStatusI referred to or null if there is no such entity.
	 */
	public synchronized ProjectStatusI get(ProjectStatusRefT ref) {
		return statusQueue.get(ref.getEntityId());
	}
	
	/**
	 * @param refs References to the desired ProjectStatusIs
	 * @return The ProjectStatusIs referred to.
	 */
	public synchronized ProjectStatusI[] get(ProjectStatusRefT[] refs) {
		final ProjectStatusI[] result = new ProjectStatusI[refs.length];
		for (int i = 0; i < refs.length; i++) {
			result[i] = statusQueue.get(refs[i].getEntityId());
		}
		return result;
	}
	
	/**
	 * Get all ProjectStatusI in the queue.
	 * @return All ProjectStatusI in the queue in the form of an array.
	 */
	public synchronized ProjectStatusI[] getAll() {
		//      System.out.println("getAll in ProjectSQueue: queue size = "+queue.size());
		ProjectStatusI[] x = new ProjectStatusI[statusQueue.size()];
		return statusQueue.values().toArray(x);
	}

	/**
	 * Get the entity-ids of all ProjectStatusI in the queue.
	 * @return All entity-ids in the queue in the form of a Set of strings.
	 */
	public synchronized Set<String> getAllIds() {
		return statusQueue.keySet();
	}

	/**
	 * Return true if and only if there is a ProjectStatusI in the queue
	 * with the specified entity-id.
	 * @param entityId The entity-id of the ProjectStatusI to be found.
	 * @return True if and only if there is an ProjectStatusI in the queue
	 * with the specified entity-id.
	 */
	public synchronized boolean isExists(String entityId) {
		return statusQueue.containsKey(entityId);
	}

	public synchronized ProjectStatusI getStatusFromProjectId(String opId) {
		for (ProjectStatusI ps : statusQueue.values()) {
			if (ps.getObsProjectRef().getEntityId().equals(opId)) {
				return ps;
			}
		}
		return null;
	}

	/**
	 * Get the number of items in the queue.
	 * @return The number of ProjectStatusI in this queue.
	 */
	public synchronized int size() {
		return statusQueue.size();
	}

	/**
	 * Updates the Project status in the queue.
	 * @param ps The ProjectStatusI to update.
	 */
	public synchronized void updateProjectStatus(ProjectStatusI ps)
			throws SchedulingException {
		String ps_id = ps.getProjectStatusEntity().getEntityId();
		if (!isExists(ps_id)) {
			throw new SchedulingException(
					"SCHEDULING: Cannot update ProjectStatusI because it doesn't exist in the queue. Try adding it.");
		}
		addUnsynchronised(ps);
	}
	
    public synchronized void replace (ProjectStatusI ps) {
    	addUnsynchronised(ps);
    }

}
