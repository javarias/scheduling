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
 * File MasterSBQueue.java
 * 
 */
package ALMA.scheduling.master_scheduler;

import java.util.Vector;
import alma.entity.xmlbinding.schedblock.SchedBlock;
//import alma.bo.SchedBlock;

/**
 * The MasterSBQueue class is the collection of scheduling blocks
 * currently under consideration by the scheduling system.
 * 
 * @version 1.00 May 5, 2003
 * @author Allen Farris
 */
public class MasterSBQueue {
	private Vector queue;

	public MasterSBQueue () {
        queue = new Vector();
	}

    public void addSB(SchedBlock sb) {
    System.out.println("Adding sbs to queue");
        queue.add(sb);
    }

    //public void addNonCompleteSBsToQueue(Vector sbs) {
    public void addSB(SchedBlock[] sbs) {
        //int len = sbs.size();
        for(int i=0; i < sbs.length; i++) {
            queue.add(sbs[i]);
        }
    }

    /**
     *  Returns the SchedBlock at location i
     */
    public SchedBlock getSB(int i) {
        return (SchedBlock)queue.elementAt(i);
    }
    /**
     *  Returns the first SB in the queue
     */
    public SchedBlock getSB() {
        return (SchedBlock) queue.firstElement();
    }


    public Vector getAllUid() {
        int size = queue.size();
        //String[] uid = new String[size];
        Vector uid = new Vector();
        for(int i = 0; i< size; i++) {
        
        //    System.out.println( ((SchedBlock)queue.elementAt(i)).getSchedBlockEntity().getEntityId() );
            uid.add( (String)((SchedBlock)queue.elementAt(i)).getSchedBlockEntity().getEntityId() );
            //uid.add( (String)((SchedBlock)queue.elementAt(i)).getId());
        }
        return uid;
    }

    /**
     * Checks to see if any new Fixed Events have been entered.
     * Returns true if there are new fixed events, else false.
     * @return boolean
     */
    public boolean checkNewFixedEvents() {
        boolean result;
        // true if theres a new event
        result = false;
        return result;
    }
    

	public static void main(String[] args) {
	}
}

