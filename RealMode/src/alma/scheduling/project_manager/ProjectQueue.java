/*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * All rights reserved
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 * 
 * File ProjectQueue.java
 * 
 */
package ALMA.scheduling.project_manager;

import java.util.Vector;
import alma.entity.xmlbinding.obsproject.ObsProject;

import ALMA.scheduling.define.*;
/**
 * The ProjectQueue class is the collection of Projects
 * currently under consideration by the scheduling system.
 * 
 * @author Sohaila Roberts
 */
public class ProjectQueue {
	private Vector queue;

	public ProjectQueue () {
        queue = new Vector();
	}

    //public synchronized void addProject(ObsProject proj) {
    public synchronized void addProject(SProject proj) {
        queue.add(proj);
    }

    public synchronized void addProject(ObsProject[] projs) {
    //public synchronized void addProject(SProject[] projs) {
        for(int i=0; i < projs.length; i++) {
            queue.add(new SProject(projs[i]));
        }
    }

    /**
     *  Returns the SchedBlock at location i
     */
    public synchronized SProject getProject(int i) {
        return (SProject)queue.elementAt(i);
    }

    /**
     *  Returns the first SB in the queue
     */
    public synchronized SProject getProject() {
        return (SProject) queue.firstElement();
    }


    public synchronized Vector queueToVector() {
        return queue;
    }

    public boolean isProjectComplete() {
        boolean res = false;
        for(int i=0; i< queue.size(); i++) {
            
        }
        return res;
    }
    public String[] getCompletedProjects() {
        return null;
    }
    public int getQueueSize() {
        return queue.size();
    }
	public static void main(String[] args) {
	}
}

