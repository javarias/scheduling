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
 * File ProjectQueue.java
 */
 
package alma.scheduling.Define;

import java.util.ArrayList;

/**
 * The ProjectQueue class is a queue of projects, held in memory,
 * that can be accessed and updated by multiple threads,
 *
 * @version $Id: ProjectQueue.java,v 1.4 2005/09/28 22:41:07 sslucero Exp $
 * @author Sohaila Lucero
 */
public class ProjectQueue {

	private ArrayList queue;

	/**
	 * Create an enpty queue of Project.
	 */
	public ProjectQueue() {
		queue = new ArrayList ();
	}

	/**
	 * Create an queue of Project from the specified array.
	 */
	public ProjectQueue(Project[] item) {
		queue = new ArrayList (item.length);
		for (int i = 0; i < item.length; ++i)
			queue.add(item[i]);
	}

	/**
	 * Add a Project to this queue.
	 * @param item The Project to be added.
	 */
	public synchronized void add(Project item) {
		queue.add(item);
	}

	/**
	 * Add an array of Project to this queue.
	 * @param item The array to be added.
	 */
	public synchronized void add(Project[] item) {
		for (int i = 0; i < item.length; ++i){
            if(!isExists(item[i].getId())){
    			queue.add(item[i]);
	        } else {
                System.out.println("Project already exists! not adding to queue");
            }
        }
    }

    /**
      * Replace the given project with its older version in the queue.
      * @param p The new version of an existing project
      */
    public synchronized void replace(Project p) {
        for(int i=0; i < queue.size(); i++) {
            if( p.getId().equals( ((Project)queue.get(i)).getId()) ){
                queue.set(i, p);
            }
        }

    }
	
	/**
	 * Remove the Project with the specified entity-id from the list.
	 * This operation does not destroy the Project. 
	 * @param entityId The entity-id of the Project to be removed.
	 */
	public synchronized void remove(String entityId) {
		Project x = null;
		for (int i = 0; i < queue.size(); ++i) {
			x = (Project)queue.get(i);
			if (x.getId().equals(entityId)) {
				queue.remove(i);
				break;
			}
		}
	}

	/**
	 * Clear all Project from the queue.
	 */
	public synchronized void clear() {
		queue.clear();
	}

	/**
	 * Get the Project with the specified entity-id.
	 * @param entityId The entity-id of the Project to be returned.
	 * @return The Project with the specified entity-id or null
	 *         if there is no such entity.
	 */
	public synchronized Project get(String entityId) {
		Project x = null;
		for (int i = 0; i < queue.size(); ++i) {
			x = (Project)queue.get(i);
			if (x.getId().equals(entityId)) {
				return x;
			}
		}
		return null;
	}

	/**
	 * Get all Projects in the queue.
	 * @return All Projects in the queue in the form of an array.
	 */
	public synchronized Project[] getAll() {
        //System.out.println("queue size = "+queue.size());
		Project[] x = new Project [queue.size()];
        for(int i=0; i<queue.size(); i++){
            x[i] = (Project)queue.get(i);
        }
		return x;
	}

	/**
	 * Get the entity-ids of all Projects in the queue.
	 * @return All entity-ids in the queue in the form of an array of strings.
	 */
	public synchronized String[] getAllIds() {
		String[] x = new String [queue.size()];
		for (int i = 0; i < x.length; ++i)
			x[i] = ((Project)queue.get(i)).getId();
		return x;
	}

	/**
	 * Get all Projects in the queue whose status is READY.
	 * @return All Projects in the queue whose status is READY as an array.
	 */
	public synchronized Project[] getReady() {
		Project x = null;
		ArrayList y = new ArrayList ();
		for (int i = 0; i < queue.size(); ++i) {
			x = (Project)queue.get(i);
			if (x.getStatus().isReady()) {
				y.add(x);
			}
		}
		Project[] z = new Project [y.size()];
		z = (Project[])y.toArray(z);
		return z;
	}

	/**
	 * Get all Projects in the queue whose status is WAITING.
	 * @return All Projects in the queue whose status is WAITING as an array.
	 */
	public synchronized Project[] getWaiting() {
		Project x = null;
		ArrayList y = new ArrayList ();
		for (int i = 0; i < queue.size(); ++i) {
			x = (Project)queue.get(i);
			if (x.getStatus().isWaiting()) {
				y.add(x);
			}
		}
		Project[] z = new Project [y.size()];
		z = (Project[])y.toArray(z);
		return z;
	}

	/**
	 * Get all Projects in the queue whose status is RUNNING.
	 * @return All Projects in the queue whose status is RUNNING as an array.
	 */
	public synchronized Project[] getRunning() {
		Project x = null;
		ArrayList y = new ArrayList ();
		for (int i = 0; i < queue.size(); ++i) {
			x = (Project)queue.get(i);
			if (x.getStatus().isRunning()) {
				y.add(x);
			}
		}
		Project[] z = new Project [y.size()];
		z = (Project[])y.toArray(z);
		return z;
	}

	/**
	 * Get all Projects in the queue whose status is COMPLETE.
	 * @return All Projects in the queue whose status is COMPLETE as an array.
	 */
	public synchronized Project[] getComplete() {
		Project x = null;
		ArrayList y = new ArrayList ();
		for (int i = 0; i < queue.size(); ++i) {
			x = (Project)queue.get(i);
			if (x.getStatus().isComplete()) {
				y.add(x);
			}
		}
		Project[] z = new Project [y.size()];
		z = (Project[])y.toArray(z);
		return z;
	}

	/**
	 * Get all Projects in the queue whose status is ABORTED.
	 * @return All Projects in the queue whose status is ABORTED as an array.
	 */
	public synchronized Project[] getAborted() {
		Project x = null;
		ArrayList y = new ArrayList ();
		for (int i = 0; i < queue.size(); ++i) {
			x = (Project)queue.get(i);
			if (x.getStatus().isAborted()) {
				y.add(x);
			}
		}
		Project[] z = new Project [y.size()];
		z = (Project[])y.toArray(z);
		return z;
	}

	/**
	 * Get all Projects in the queue whose status is not defined.
	 * @return All Projects in the queue whose status is not defined, as an array.
	 */
	public synchronized Project[] getNotDefined() {
		Project x = null;
		ArrayList y = new ArrayList ();
		for (int i = 0; i < queue.size(); ++i) {
			x = (Project)queue.get(i);
			if (!x.getStatus().isDefined()) {
				y.add(x);
			}
		}
		Project[] z = new Project [y.size()];
		z = (Project[])y.toArray(z);
		return z;
	}

	/**
	 * Return true if and only if there is a Project in the queue
	 * with the specified entity-id.
	 * @param entityId The entity-id of the Project to be found.
	 * @return True if and only if there is an Project in the queue
	 * with the specified entity-id.
	 */
	public synchronized boolean isExists(String entityId) {
		Project x = null;
		for (int i = 0; i < queue.size(); ++i) {
			x = (Project)queue.get(i);
			if (x.getId().equals(entityId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the number of items in the queue.
	 * @return The number of Projects in this queue.
	 */
	public int size() {
		return queue.size();
	}
}
