/*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by AUI (in the framework of the ALMA collaboration),
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
package alma.scheduling.master_scheduler;

import java.util.Vector;
import alma.entity.xmlbinding.schedblock.*;
import alma.entity.xmlbinding.obsproject.*;
import alma.entity.xmlbinding.obsproject.types.*;
import alma.scheduling.define.SUnit;
import alma.Control.ExecBlockEvent;
//import alma.bo.SchedBlock;

/**
 * The MasterSBQueue class is the collection of scheduling blocks
 * currently under consideration by the scheduling system.
 * 
 * @version 1.00 May 5, 2003
 * @author Allen Farris
 */
public class MasterSBQueue {
	private Vector sbQueue;
    private Vector suQueue;

	public MasterSBQueue () {
        sbQueue = new Vector();
        suQueue = new Vector();
	}

    /** 
     * Checks the queue to see if the given schedblock is already in
     * the queue.
     * @param sb The schedblock to check.
     * @return boolean True if the sb is in the queue. False if its not.
     */
    public boolean isSBinQueue(SchedBlock sb) {
        boolean res = false;
        String id = sb.getSchedBlockEntity().getEntityId();
        SchedBlock tmpSB;
        for(int i=0; i < sbQueue.size(); i++) {
            tmpSB = (SchedBlock)sbQueue.elementAt(i);
            if (tmpSB.getSchedBlockEntity().getEntityId().equals(id)) {
                //sb already in queue!
                res = true;
            }
        }
        return res;
    }

    public synchronized void addSchedBlock(SchedBlock sb) {
        //System.out.println("Adding sbs to queue");
        sbQueue.add(sb);
        suQueue.add(new SUnit(sb));
    }

    public synchronized void addSchedBlock(SchedBlock[] sbs) {
        //int len = sbs.size();
        for(int i=0; i < sbs.length; i++) {
            sbQueue.add(sbs[i]);
            suQueue.add(new SUnit(sbs[i]));
        }
    }

    /**
     *  Returns the SchedBlock at location i
     */
    public synchronized SchedBlock getSchedBlock(int i) {
        return (SchedBlock)sbQueue.elementAt(i);
    }
    /**
     *  Returns the SUnit at location i
     */
    public synchronized SUnit getSUnit(int i) {
        return (SUnit)suQueue.elementAt(i);
    }

    /**
     *  Returns the first SB in the queue
     */
    public synchronized SchedBlock getSchedBlock() {
        return (SchedBlock) sbQueue.firstElement();
    }
    /**
     *  Returns the first SUnit in the queue
     */
    public synchronized SUnit getSUnit() {
        return (SUnit) suQueue.firstElement();
    }


    public synchronized Vector getAllUid() {
        int size = sbQueue.size();
        //String[] uid = new String[size];
        Vector uid = new Vector();
        for(int i = 0; i< size; i++) {
        
            uid.add( (String)((SchedBlock)sbQueue.elementAt(i)).getSchedBlockEntity().getEntityId() );
            //uid.add( (String)((SchedBlock)queue.elementAt(i)).getId());
        }
        return uid;
    }

    /**
     * Checks to see if any new Fixed Events have been entered.
     * Returns true if there are new fixed events, else false.
     * @return boolean
     */
    public synchronized boolean checkNewFixedEvents() {
        boolean result;
        // true if theres a new event
        result = false;
        return result;
    }

    public void updateSUnit(ExecBlockEvent e) {
        SchedBlock tmpsb;
        String id;
        for(int i =0; i < sbQueue.size(); i++) {
            tmpsb =(SchedBlock)sbQueue.elementAt(i) ;
            id = tmpsb.getSchedBlockEntity().getEntityId();
            if(e.sbId.equals(id)) {
                ObsUnitControl ouc = tmpsb.getObsUnitControl(); 
                if(ouc == null) {
                    ouc = new ObsUnitControl();
                }
                switch(e.status.value()) {
                    case 0://exec block status = processing
                        ouc.setSchedStatus(SchedStatusT.RUNNING);
                        break;
                    case 1: //exec block status = ok
                        ouc.setSchedStatus(SchedStatusT.COMPLETED);
                        break;
                    case 2://exec block status = failed
                        ouc.setSchedStatus(SchedStatusT.ABORTED);
                        break;
                    case 3://exec block status = timeout
                        ouc.setSchedStatus(SchedStatusT.ABORTED);
                        break;
                    default://exec block status kooky.. 
                        break;
                }
                tmpsb.setObsUnitControl(ouc);
                SUnit tmpsu = matchSUnit(id);
                tmpsu.updateSB(tmpsb);
            }
        }
    }
    private SchedBlock matchSB(String id) {
        SchedBlock tmp=null;
        for(int i=0; i<sbQueue.size();i++){
            tmp = (SchedBlock)sbQueue.elementAt(i);
            if(id.equals(tmp.getSchedBlockEntity().getEntityId()) ){
                return tmp;
            }
        }
        return tmp;
    }
    private SUnit matchSUnit(String id) {
        SUnit tmp=null;
        for(int i=0; i<suQueue.size(); i++) {
            tmp = (SUnit)suQueue.elementAt(i);
            if(id.equals(tmp.getId()) ){
                return tmp;
            }
        }
        return tmp;
    }
    public int getSUnitSize(){
        return suQueue.size();
    }
    public int getSBSize() {
        return sbQueue.size();
    }
    
    public synchronized Vector queueToVector() {
        return sbQueue;
    }
	public static void main(String[] args) {
	}
}

