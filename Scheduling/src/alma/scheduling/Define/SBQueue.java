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
 * File SBQueue.java
 */
 
package alma.scheduling.Define;

import java.util.ArrayList;

/**
 * The SBQueue class is a queue of scheduling units, held in memory,
 * that can be accessed and updated by multiple threads, viz., the 
 * MasterScheduler and Scheduler objects.
 * 
 * @version $Id: SBQueue.java,v 1.3 2004/11/23 20:41:21 sslucero Exp $
 * @author Allen Farris
 */
public class SBQueue {

	private ArrayList queue;

	/**
	 * Create an enpty queue of SBs.
	 */
	public SBQueue() {
		queue = new ArrayList ();
	}

	/**
	 * Create an queue of SBs from the specified array.
	 */
	public SBQueue(SB[] item) {
		queue = new ArrayList (item.length);
		for (int i = 0; i < item.length; ++i)
			queue.add(item[i]);
	}

	/**
	 * Add a SB to this queue.
	 * @param item The SB to be added.
	 */
	public synchronized void add(SB item) {
		queue.add(item);
	}

	/**
	 * Add an array of SBs to this queue.
	 * @param item The array to be added.
	 */
	public synchronized void add(SB[] item) {
		for (int i = 0; i < item.length; ++i)
			queue.add(item[i]);
	}
	
	/**
	 * Remove the SB with the specified entity-id from the list.
	 * This operation does not destroy the SB. 
	 * @param entityId The entity-id of the SB to be removed.
	 */
	public synchronized void remove(String entityId) {
		SB x = null;
		for (int i = 0; i < queue.size(); ++i) {
			x = (SB)queue.get(i);
			if (x.getId().equals(entityId)) {
				queue.remove(i);
				break;
			}
		}
	}

	/**
	 * Clear all SBs from the queue.
	 */
	public synchronized void clear() {
		queue.clear();
	}

	/**
	 * Get the SB with the specified entity-id.
	 * @param entityId The entity-id of the SB to be returned.
	 * @return The SB with the specified entity-id or null
	 * if there is no such entity.
	 */
	public synchronized SB get(String entityId) {
		SB x = null;
		for (int i = 0; i < queue.size(); ++i) {
			x = (SB)queue.get(i);
			if (x.getId().equals(entityId)) {
				return x;
			}
		}
		return null;
	}

	/**
	 * Get all SBs in the queue.
	 * @return All SBs in the queue in the form of an array.
	 */
	public synchronized SB[] getAll() {
        System.out.println("queue size = "+queue.size());
		SB[] x = new SB [queue.size()];
        for(int i=0; i<queue.size(); i++){
            x[i] = (SB)queue.get(i);
        }
		//x = (SB[])queue.toArray(x);
		return x;
	}

	/**
	 * Get the entity-ids of all SBs in the queue.
	 * @return All entity-ids in the queue in the form of an array of strings.
	 */
	public synchronized String[] getAllIds() {
		String[] x = new String [queue.size()];
		for (int i = 0; i < x.length; ++i)
			x[i] = ((SB)queue.get(i)).getId();
		return x;
	}

	/**
	 * Get all SBs in the queue whose status is READY.
	 * @return All SBs in the queue whose status is READY as an array.
	 */
	public synchronized SB[] getReady() {
		SB x = null;
		ArrayList y = new ArrayList ();
		for (int i = 0; i < queue.size(); ++i) {
			x = (SB)queue.get(i);
			if (x.getStatus().isReady()) {
				y.add(x);
			}
		}
		SB[] z = new SB [y.size()];
		z = (SB[])y.toArray(z);
		return z;
	}

	/**
	 * Get all SBs in the queue whose status is WAITING.
	 * @return All SBs in the queue whose status is WAITING as an array.
	 */
	public synchronized SB[] getWaiting() {
		SB x = null;
		ArrayList y = new ArrayList ();
		for (int i = 0; i < queue.size(); ++i) {
			x = (SB)queue.get(i);
			if (x.getStatus().isWaiting()) {
				y.add(x);
			}
		}
		SB[] z = new SB [y.size()];
		z = (SB[])y.toArray(z);
		return z;
	}

	/**
	 * Get all SBs in the queue whose status is RUNNING.
	 * @return All SBs in the queue whose status is RUNNING as an array.
	 */
	public synchronized SB[] getRunning() {
		SB x = null;
		ArrayList y = new ArrayList ();
		for (int i = 0; i < queue.size(); ++i) {
			x = (SB)queue.get(i);
			if (x.getStatus().isRunning()) {
				y.add(x);
			}
		}
		SB[] z = new SB [y.size()];
		z = (SB[])y.toArray(z);
		return z;
	}

	/**
	 * Get all SBs in the queue whose status is COMPLETE.
	 * @return All SBs in the queue whose status is COMPLETE as an array.
	 */
	public synchronized SB[] getComplete() {
		SB x = null;
		ArrayList y = new ArrayList ();
		for (int i = 0; i < queue.size(); ++i) {
			x = (SB)queue.get(i);
			if (x.getStatus().isComplete()) {
				y.add(x);
			}
		}
		SB[] z = new SB [y.size()];
		z = (SB[])y.toArray(z);
		return z;
	}

	/**
	 * Get all SBs in the queue whose status is ABORTED.
	 * @return All SBs in the queue whose status is ABORTED as an array.
	 */
	public synchronized SB[] getAborted() {
		SB x = null;
		ArrayList y = new ArrayList ();
		for (int i = 0; i < queue.size(); ++i) {
			x = (SB)queue.get(i);
			if (x.getStatus().isAborted()) {
				y.add(x);
			}
		}
		SB[] z = new SB [y.size()];
		z = (SB[])y.toArray(z);
		return z;
	}

	/**
	 * Get all SBs in the queue whose status is not defined.
	 * @return All SBs in the queue whose status is not defined as an array.
	 */
	public synchronized SB[] getNotDefined() {
		SB x = null;
		ArrayList y = new ArrayList ();
		for (int i = 0; i < queue.size(); ++i) {
			x = (SB)queue.get(i);
			if (!x.getStatus().isDefined()) {
				y.add(x);
			}
		}
		SB[] z = new SB [y.size()];
		z = (SB[])y.toArray(z);
		return z;
	}

	/**
	 * Return true if and only if there is a SB in the queue
	 * with the specified entity-id.
	 * @param entityId The entity-id of the SB to be found.
	 * @return True if and only if there is an SB in the queue
	 * with the specified entity-id.
	 */
	public synchronized boolean isExists(String entityId) {
		SB x = null;
		for (int i = 0; i < queue.size(); ++i) {
			x = (SB)queue.get(i);
			if (x.getId().equals(entityId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the number of items in the queue.
	 * @return The number of SBs in this queue.
	 */
	public int size() {
		return queue.size();
	}
}
