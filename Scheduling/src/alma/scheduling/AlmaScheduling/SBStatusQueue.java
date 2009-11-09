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
 * File SBStatusQueue.java
 */

package alma.scheduling.AlmaScheduling;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import alma.acs.logging.AcsLogger;
import alma.entity.xmlbinding.sbstatus.SBStatusRefT;
import alma.scheduling.AlmaScheduling.statusIF.SBStatusI;
import alma.scheduling.Define.SchedulingException;

/**
 * The SBStatusQueue class is a queue of SB statuses, held in memory,
 * that can be accessed and updated by multiple threads, viz., the 
 * MasterScheduler and Scheduler objects.
 * 
 * @author David Clarke
 * @version $Id: SBStatusQueue.java,v 1.2 2009/11/09 22:58:45 rhiriart Exp $
 */
public class SBStatusQueue {

	private final AcsLogger logger;

	private final Map<String, SBStatusI> queue;

	/**
	 * Create an empty queue of SBStatusIs.
	 */
	public SBStatusQueue(AcsLogger logger) {
		this.logger = logger;
		
		// We use a LinkedHashMap to preserve the insertion order of
		// the elements. This may or may not prove to be important, but
		// somehow calling a class a something-Queue and not
		// maintaining order seems wrong.
		queue = new LinkedHashMap<String, SBStatusI>();
	}

	/**
	 * Create a queue of SBStatusI from the specified array.
	 */
	public SBStatusQueue(SBStatusI[] statuses, AcsLogger logger) {
		this(logger);
		for (SBStatusI sbs : statuses) {
			addUnsynchronised(sbs);
		}
	}

	/**
	 * Add an SBStatusI to this queue.
	 * NOTE: THIS METHOD IS NOT SYNCHRONISED
	 * @param sbs The SBStatusI to be added.
	 */
	private void addUnsynchronised(SBStatusI sbs) {
		if (sbs == null) {
			logger.warning("Trying to add a null SBStatusI to the SBStatusQueue - not added");
			return;
		} 
		if (sbs.getSBStatusEntity() == null) {
			logger.warning("Trying to add an SBStatusI with no Entity object to the SBStatusQueue - not added");
			return;
		} 
		final String key = sbs.getSBStatusEntity().getEntityId();
		if (key == null) {
			logger.warning("Trying to add an SBStatusI with no EntityId to the SBStatusQueue - not added");
			return;
		} 
		queue.put(key, sbs);
	}

	/**
	 * Add an SBStatusI to this queue.
	 * @param sbs The SBStatusI to be added.
	 */
	public synchronized void add(SBStatusI sbs) {
		addUnsynchronised(sbs);
	}

	/**
	 * Add an array of SBStatusI to this queue.
	 * @param statuses The array to be added.
	 */
	public synchronized void add(SBStatusI[] statuses) {
		for (SBStatusI sbs : statuses) {
			addUnsynchronised(sbs);
		}
	}

	/**
	 * Remove the SBStatusI with the specified entity-id from the list.
	 * This operation does not destroy the SBStatusI. 
	 * @param entityId The entity-id of the SBStatusI to be removed.
	 */
	public synchronized void remove(String entityId) {
		queue.remove(entityId);
	}

	/**
	 * Clear all SBStatusI from the queue.
	 */
	public synchronized void clear() {
		queue.clear();
	}

	/**
	 * Update the queue to match <code>that</code>.
	 */
	public synchronized void updateWith(SBStatusQueue that) {
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
	 * Get the SBStatusI with the specified entity-id.
	 * @param entityId The entity-id of the SBStatusI to be returned.
	 * @return The SBStatusI with the specified entity-id or null
	 * if there is no such entity.
	 */
	public synchronized SBStatusI get(String entityId) {
		//System.out.println("getting sbs "+entityId);
		//System.out.println("get in SBSQueue: queue size = "+queue.size());
		return queue.get(entityId);
	}

	/**
	 * @param ref A reference to the desired SBStatusI
	 * @return The SBStatusI referred to or null if there is no such entity.
	 */
	public synchronized SBStatusI get(SBStatusRefT ref) {
		return queue.get(ref.getEntityId());
	}
	
	/**
	 * @param refs References to the desired SBStatusIs
	 * @return The SBStatusIs referred to.
	 */
	public synchronized SBStatusI[] get(SBStatusRefT[] refs) {
		final SBStatusI[] result = new SBStatusI[refs.length];
		for (int i = 0; i < refs.length; i++) {
			result[i] = queue.get(refs[i].getEntityId());
		}
		return result;
	}
	
	/**
	 * Get all SBStatusI in the queue.
	 * @return All SBStatusI in the queue in the form of an array.
	 */
	public synchronized SBStatusI[] getAll() {
		//      System.out.println("getAll in SBSQueue: queue size = "+queue.size());
		SBStatusI[] x = new SBStatusI[queue.size()];
		return queue.values().toArray(x);
	}

	/**
	 * Get the entity-ids of all SBStatusI in the queue.
	 * @return All entity-ids in the queue in the form of a Set of strings.
	 */
	public synchronized Set<String> getAllIds() {
		return queue.keySet();
	}

	/**
	 * Return true if and only if there is a SBStatusI in the queue
	 * with the specified entity-id.
	 * @param entityId The entity-id of the SBStatusI to be found.
	 * @return True if and only if there is an SBStatusI in the queue
	 * with the specified entity-id.
	 */
	public synchronized boolean isExists(String entityId) {
		return queue.containsKey(entityId);
	}

	public synchronized SBStatusI getStatusFromSBId(String sbId) {
		for (SBStatusI sbs : queue.values()) {
			if (sbs.getSchedBlockRef().getEntityId().equals(sbId)) {
				return sbs;
			}
		}
		return null;
	}

	/**
	 * Get the number of items in the queue.
	 * @return The number of SBStatusI in this queue.
	 */
	public synchronized int size() {
		return queue.size();
	}

	/**
	 * Updates the SB status in the queue.
	 * @param sbs The SBStatusI to update.
	 */
	public synchronized void updateSBStatus(SBStatusI sbs)
			throws SchedulingException {
		String sbs_id = sbs.getSBStatusEntity().getEntityId();
		if (!isExists(sbs_id)) {
			throw new SchedulingException(
					"SCHEDULING: Cannot update SBStatusI because it doesn't exist in the queue. Try adding it.");
		}
		addUnsynchronised(sbs);
	}
	
	
    public synchronized void replace(SBStatusI sbs) {
    	addUnsynchronised(sbs);
    }


}
