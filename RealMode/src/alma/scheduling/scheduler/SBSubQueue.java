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
 * File SBSubQueue.java
 * 
 */
package ALMA.scheduling.scheduler;

import java.util.Vector;
import alma.entity.xmlbinding.schedblock.SchedBlock;
//import alma.bo.SchedBlock;

/**
 * The SBSubQueue class is a sub-collection of scheduling blocks
 * currently under consideration by the scheduling system. This 
 * queue is sent to a particular scheduler for execution.
 * 
 * @author Sohaila Roberts
 */
public class SBSubQueue {
	private Vector queue;

	public SBSubQueue () {
        queue = new Vector();
	}
    public SBSubQueue(Vector q) {
        queue = q;
    }

    public void addSchedBlock(SchedBlock sb) {
        System.out.println("Adding sbs to queue");
        queue.add(sb);
    }

    public void addSchedBlock(SchedBlock[] sbs) {
        //int len = sbs.size();
        for(int i=0; i < sbs.length; i++) {
            queue.add(sbs[i]);
        }
    }
    
    /////////////////////////////////////////////////////
    /* GetMethods */
    
    /**
     *  Returns the SchedBlock at location i
     */
    public SchedBlock getSchedBlock(int i) {
        return (SchedBlock)queue.elementAt(i);
    }
    /**
     *  Returns the first SB in the queue
     */
    public SchedBlock getSchedBlock() {
        return (SchedBlock) queue.firstElement();
    }

    /**
     *  Return a list of all the ids
     */
    public String[] getIds() {
        String[] tmp = new String[queue.size()];
        for(int i =0; i < queue.size(); i++) {
            tmp[i] = (String)((SchedBlock)queue.elementAt(i)).getSchedBlockEntity().getEntityId();
        }
        return tmp;
    }

    /////////////////////////////////////////////////////
	public static void main(String[] args) {
	}
}

