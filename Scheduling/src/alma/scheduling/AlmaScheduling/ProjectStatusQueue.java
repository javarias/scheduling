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

import java.util.ArrayList;
import alma.entity.xmlbinding.projectstatus.*;

/**
 * The ProjectStatusQueue class is a queue of project status', held in memory,
 * that can be accessed and updated by multiple threads, viz., the 
 * MasterScheduler and Scheduler objects.
 * 
 * @author Sohaila Lucero
 */
public class ProjectStatusQueue {

	private ArrayList queue;

	/**
	 * Create an enpty queue of ProjectStatus.
	 */
	public ProjectStatusQueue() {
		queue = new ArrayList ();
	}

	/**
	 * Create an queue of ProjectStatus from the specified array.
	 */
	public ProjectStatusQueue(ProjectStatus[] item) {
		queue = new ArrayList (item.length);
		for (int i = 0; i < item.length; ++i)
			queue.add(item[i]);
	}

	/**
	 * Add a ProjectStatus to this queue.
	 * @param item The ProjectStatus to be added.
	 */
	public synchronized void add(ProjectStatus item) {
		queue.add(item);
	}

	/**
	 * Add an array of ProjectStatus to this queue.
	 * @param item The array to be added.
	 */
	public synchronized void add(ProjectStatus[] item) {
		for (int i = 0; i < item.length; ++i){
            //if(!isExists(item[i].getId())){
            if(!isExists(item[i].getProjectStatusEntity().getEntityId())){
			    queue.add(item[i]);
            }
        }
	}
	
	/**
	 * Remove the ProjectStatus with the specified entity-id from the list.
	 * This operation does not destroy the ProjectStatus. 
	 * @param entityId The entity-id of the ProjectStatus to be removed.
	 */
	public synchronized void remove(String entityId) {
		ProjectStatus x = null;
		for (int i = 0; i < queue.size(); ++i) {
			x = (ProjectStatus)queue.get(i);
			//if (x.getId().equals(entityId)) {
			if (x.getProjectStatusEntity().getEntityId().equals(entityId)) {
				queue.remove(i);
				break;
			}
		}
	}

	/**
	 * Clear all ProjectStatus from the queue.
	 */
	public synchronized void clear() {
		queue.clear();
	}

	/**
	 * Get the ProjectStatus with the specified entity-id.
	 * @param entityId The entity-id of the ProjectStatus to be returned.
	 * @return The ProjectStatus with the specified entity-id or null
	 * if there is no such entity.
	 */
	public synchronized ProjectStatus get(String entityId) {
		ProjectStatus x = null;
		for (int i = 0; i < queue.size(); ++i) {
			x = (ProjectStatus)queue.get(i);
			//if (x.getId().equals(entityId)) {
			if (x.getProjectStatusEntity().getEntityId().equals(entityId)) {
				return x;
			}
		}
		return null;
	}

	/**
	 * Get all ProjectStatus in the queue.
	 * @return All ProjectStatus in the queue in the form of an array.
	 */
	public synchronized ProjectStatus[] getAll() {
        System.out.println("queue size = "+queue.size());
		ProjectStatus[] x = new ProjectStatus [queue.size()];
        for(int i=0; i<queue.size(); i++){
            x[i] = (ProjectStatus)queue.get(i);
        }
		//x = (ProjectStatus[])queue.toArray(x);
		return x;
	}

	/**
	 * Get the entity-ids of all ProjectStatus in the queue.
	 * @return All entity-ids in the queue in the form of an array of strings.
	 */
	public synchronized String[] getAllIds() {
		String[] x = new String [queue.size()];
		for (int i = 0; i < x.length; ++i)
			//x[i] = ((ProjectStatus)queue.get(i)).getId();
			x[i] = ((ProjectStatus)queue.get(i)).getProjectStatusEntity().getEntityId();
		return x;
	}

	/**
	 * Get all ProjectStatus in the queue whose status is READY.
	 * @return All ProjectStatus in the queue whose status is READY as an array.
	public synchronized ProjectStatus[] getReady() {
		ProjectStatus x = null;
		ArrayList y = new ArrayList ();
		for (int i = 0; i < queue.size(); ++i) {
			x = (ProjectStatus)queue.get(i);
			if (x.getStatus().isReady()) {
				y.add(x);
			}
		}
		ProjectStatus[] z = new ProjectStatus [y.size()];
		z = (ProjectStatus[])y.toArray(z);
		return z;
	}
	 */

	/**
	 * Get all ProjectStatus in the queue whose status is WAITING.
	 * @return All ProjectStatus in the queue whose status is WAITING as an array.
	public synchronized ProjectStatus[] getWaiting() {
		ProjectStatus x = null;
		ArrayList y = new ArrayList ();
		for (int i = 0; i < queue.size(); ++i) {
			x = (ProjectStatus)queue.get(i);
			if (x.getStatus().isWaiting()) {
				y.add(x);
			}
		}
		ProjectStatus[] z = new ProjectStatus [y.size()];
		z = (ProjectStatus[])y.toArray(z);
		return z;
	}
	 */

	/**
	 * Get all ProjectStatus in the queue whose status is RUNNING.
	 * @return All ProjectStatus in the queue whose status is RUNNING as an array.
	public synchronized ProjectStatus[] getRunning() {
		ProjectStatus x = null;
		ArrayList y = new ArrayList ();
		for (int i = 0; i < queue.size(); ++i) {
			x = (ProjectStatus)queue.get(i);
			if (x.getStatus().isRunning()) {
				y.add(x);
			}
		}
		ProjectStatus[] z = new ProjectStatus [y.size()];
		z = (ProjectStatus[])y.toArray(z);
		return z;
	}
	 */

	/**
	 * Get all ProjectStatus in the queue whose status is COMPLETE.
	 * @return All ProjectStatus in the queue whose status is COMPLETE as an array.
	public synchronized ProjectStatus[] getComplete() {
		ProjectStatus x = null;
		ArrayList y = new ArrayList ();
		for (int i = 0; i < queue.size(); ++i) {
			x = (ProjectStatus)queue.get(i);
			if (x.getStatus().isComplete()) {
				y.add(x);
			}
		}
		ProjectStatus[] z = new ProjectStatus [y.size()];
		z = (ProjectStatus[])y.toArray(z);
		return z;
	}
	 */

	/**
	 * Get all ProjectStatus in the queue whose status is ABORTED.
	 * @return All ProjectStatus in the queue whose status is ABORTED as an array.
	public synchronized ProjectStatus[] getAborted() {
		ProjectStatus x = null;
		ArrayList y = new ArrayList ();
		for (int i = 0; i < queue.size(); ++i) {
			x = (ProjectStatus)queue.get(i);
			if (x.getStatus().isAborted()) {
				y.add(x);
			}
		}
		ProjectStatus[] z = new ProjectStatus [y.size()];
		z = (ProjectStatus[])y.toArray(z);
		return z;
	}
	 */

	/**
	 * Get all ProjectStatus in the queue whose status is not defined.
	 * @return All ProjectStatus in the queue whose status is not defined as an array.
	public synchronized ProjectStatus[] getNotDefined() {
		ProjectStatus x = null;
		ArrayList y = new ArrayList ();
		for (int i = 0; i < queue.size(); ++i) {
			x = (ProjectStatus)queue.get(i);
			if (!x.getStatus().isDefined()) {
				y.add(x);
			}
		}
		ProjectStatus[] z = new ProjectStatus [y.size()];
		z = (ProjectStatus[])y.toArray(z);
		return z;
	}
	 */

	/**
	 * Return true if and only if there is a ProjectStatus in the queue
	 * with the specified entity-id.
	 * @param entityId The entity-id of the ProjectStatus to be found.
	 * @return True if and only if there is an ProjectStatus in the queue
	 * with the specified entity-id.
	 */
	public synchronized boolean isExists(String entityId) {
		ProjectStatus x = null;
		for (int i = 0; i < queue.size(); ++i) {
			x = (ProjectStatus)queue.get(i);
			//if (x.getId().equals(entityId)) {
			if (x.getProjectStatusEntity().getEntityId().equals(entityId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the number of items in the queue.
	 * @return The number of ProjectStatus in this queue.
	 */
	public int size() {
		return queue.size();
	}
}