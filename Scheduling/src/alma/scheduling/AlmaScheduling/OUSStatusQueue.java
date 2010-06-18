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
 * File OUSStatusQueue.java
 */

package alma.scheduling.AlmaScheduling;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import alma.acs.logging.AcsLogger;
import alma.entity.xmlbinding.obsproject.ObsProjectRefT;
import alma.entity.xmlbinding.ousstatus.OUSStatusRefT;
import alma.scheduling.AlmaScheduling.statusIF.OUSStatusI;
import alma.scheduling.Define.SchedulingException;

/**
 * The OUSStatusQueue class is a queue of OUS statuses, held in memory,
 * that can be accessed and updated by multiple threads, viz., the 
 * MasterScheduler and Scheduler objects.
 * 
 * @author David Clarke
 * @version $Id: OUSStatusQueue.java,v 1.4 2010/06/18 15:09:45 dclarke Exp $
 */
public class OUSStatusQueue {

	private final AcsLogger logger;

	private final Map<String, OUSStatusI> queue;

	/**
	 * Create an empty queue of OUSStatusIs.
	 */
	public OUSStatusQueue(AcsLogger logger) {
		this.logger = logger;
		
		// We use a LinkedHashMap to preserve the insertion order of
		// the elements. This may or may not prove to be important, but
		// somehow calling a class a something-Queue and not
		// maintaining order seems wrong.
		queue = new LinkedHashMap<String, OUSStatusI>();
	}

	/**
	 * Create a queue of OUSStatusI from the specified array.
	 */
	public OUSStatusQueue(OUSStatusI[] statuses, AcsLogger logger) {
		this(logger);
		for (OUSStatusI ouss : statuses) {
			addUnsynchronised(ouss);
		}
	}

	/**
	 * Add an OUSStatusI to this queue.
	 * NOTE: THIS METHOD IS NOT SYNCHRONISED
	 * @param ouss The OUSStatusI to be added.
	 */
	private void addUnsynchronised(OUSStatusI ouss) {
		if (ouss == null) {
			logger.warning("Trying to add a null OUSStatusI to the OUSStatusQueue - not added");
			return;
		} 
		if (ouss.getOUSStatusEntity() == null) {
			logger.warning("Trying to add an OUSStatusI with no Entity object to the OUSStatusQueue - not added");
			return;
		} 
		final String key = ouss.getOUSStatusEntity().getEntityId();
		if (key == null) {
			logger.warning("Trying to add an OUSStatusI with no EntityId to the OUSStatusQueue - not added");
			return;
		} 
		queue.put(key, ouss);
	}

	/**
	 * Add an OUSStatusI to this queue.
	 * @param ouss The OUSStatusI to be added.
	 */
	public synchronized void add(OUSStatusI ouss) {
		addUnsynchronised(ouss);
	}

	/**
	 * Add an array of OUSStatusI to this queue.
	 * @param statuses The array to be added.
	 */
	public synchronized void add(OUSStatusI[] statuses) {
		for (OUSStatusI ouss : statuses) {
			addUnsynchronised(ouss);
		}
	}

	/**
	 * Add a Collection of OUSStatusI to this queue.
	 * @param statuses The Collection to be added.
	 */
	private synchronized void add(Collection<OUSStatusI> statuses) {
		for (OUSStatusI ouss : statuses) {
			addUnsynchronised(ouss);
		}
	}

	/**
	 * Remove the OUSStatusI with the specified entity-id from the list.
	 * This operation does not destroy the OUSStatusI. 
	 * @param entityId The entity-id of the OUSStatusI to be removed.
	 */
	public synchronized void remove(String entityId) {
		queue.remove(entityId);
	}

	/**
	 * Clear all OUSStatusI from the queue.
	 */
	public synchronized void clear() {
		queue.clear();
	}

	/**
	 * Update the queue to match <code>that</code>.
	 */
	public synchronized void updateWith(OUSStatusQueue that) {
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
		
		// 2. update with the things from that
		this.add(that.getValues());
	}

    public synchronized void updateIncrWith(OUSStatusQueue that) {
		this.add(that.getValues());
    }	
	
	/**
	 * Get the OUSStatusI with the specified entity-id.
	 * @param entityId The entity-id of the OUSStatusI to be returned.
	 * @return The OUSStatusI with the specified entity-id or null
	 * if there is no such entity.
	 */
	public synchronized OUSStatusI get(String entityId) {
		//System.out.println("getting ouss "+entityId);
		//System.out.println("get in OUSSQueue: queue size = "+queue.size());
		return queue.get(entityId);
	}

	/**
	 * @param ref A reference to the desired OUSStatusI
	 * @return The OUSStatusI referred to or null if there is no such entity.
	 */
	public synchronized OUSStatusI get(OUSStatusRefT ref) {
		return queue.get(ref.getEntityId());
	}

	/**
	 * @param refs References to the desired OUSStatusIs
	 * @return The OUSStatusIs referred to.
	 */
	public synchronized OUSStatusI[] get(OUSStatusRefT[] refs) {
		final OUSStatusI[] result = new OUSStatusI[refs.length];
		for (int i = 0; i < refs.length; i++) {
			result[i] = queue.get(refs[i].getEntityId());
		}
		return result;
	}

	/**
	 * Get all OUSStatusI in the queue.
	 * @return All OUSStatusI in the queue in the form of a Collection.
	 */
	private synchronized Collection<OUSStatusI> getValues() {
		return queue.values();
	}

	/**
	 * Get all OUSStatusI in the queue.
	 * @return All OUSStatusI in the queue in the form of an array.
	 */
	public synchronized OUSStatusI[] getAll() {
		//      System.out.println("getAll in OUSSQueue: queue size = "+queue.size());
		OUSStatusI[] x = new OUSStatusI[queue.size()];
		return queue.values().toArray(x);
	}

	/**
	 * Get the entity-ids of all OUSStatusI in the queue.
	 * @return All entity-ids in the queue in the form of a Set of strings.
	 */
	public synchronized Set<String> getAllIds() {
		return queue.keySet();
	}

	/**
	 * Return true if and only if there is a OUSStatusI in the queue
	 * with the specified entity-id.
	 * @param entityId The entity-id of the OUSStatusI to be found.
	 * @return True if and only if there is an OUSStatusI in the queue
	 * with the specified entity-id.
	 */
	public synchronized boolean isExists(String entityId) {
		return queue.containsKey(entityId);
	}

	public synchronized OUSStatusI getStatusFromOUSId(String projectId, String partId) {
		for (OUSStatusI ouss : queue.values()) {
			final ObsProjectRefT projectRef = ouss.getObsUnitSetRef();
			if (projectRef.getEntityId().equals(projectId) &&
					projectRef.getPartId().equals(partId)) {
				return ouss;
			}
		}
		return null;
	}

	/**
	 * Get the number of items in the queue.
	 * @return The number of OUSStatusI in this queue.
	 */
	public synchronized int size() {
		return queue.size();
	}

	/**
	 * Updates the OUS status in the queue.
	 * @param ouss The OUSStatusI to update.
	 */
	public synchronized void updateOUSStatus(OUSStatusI ouss)
			throws SchedulingException {
		String ouss_id = ouss.getOUSStatusEntity().getEntityId();
		if (!isExists(ouss_id)) {
			throw new SchedulingException(
					"SCHEDULING: Cannot update OUSStatusI because it doesn't exist in the queue. Try adding it.");
		}
		addUnsynchronised(ouss);
	}
	
    public synchronized void replace (OUSStatusI ouss) {
    	addUnsynchronised(ouss);
    }

	/**
	 * Remove all the things in <code>that</code> from this queue
	 * @param that
	 */
	public synchronized void remove(OUSStatusQueue that) {
		for (final String id : that.getAllIds()) {
			remove(id);
		}
	}
}
