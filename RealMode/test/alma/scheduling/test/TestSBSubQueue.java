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
 * File TestSBSubQueue.java
 * 
 */

package alma.scheduling.test;

import alma.scheduling.scheduler.SBSubQueue;

import alma.entity.xmlbinding.schedblock.*;

/**
 * This class tests the PipelineStatus class
 * @author Sohaila Roberts
 */
public class TestSBSubQueue {
    public static void main(String[] args) {
        SchedBlock sb = new SchedBlock();
        SchedBlockEntityT sb_entity = new SchedBlockEntityT();
        sb_entity.setEntityId("sb1");
        sb.setSchedBlockEntity(sb_entity);
        System.out.println("SCHED_TEST: function new SBSubQueue()");
        SBSubQueue queue = new SBSubQueue();
        
        System.out.println("SCHED_TEST: function addSchedBlock(sb)");
        queue.addSchedBlock(sb);
        System.out.println("SCHED_TEST: function getSchedBlock()");
        sb = queue.getSchedBlock();
        System.out.println("SCHED_TEST: function getIds()");
        String[] ids = queue.getIds();
        System.out.println("SCHED_TEST: function getSchedBlock(int)");
        sb = queue.getSchedBlock(0);
        System.out.println("SCHED_TEST: function isInSubQueue(id) with id=sb2");
        System.out.println("SCHED_TEST: should be false!");
        boolean b = queue.isInSubQueue("sb2");
        System.out.println("SCHED_TEST: "+b);

        System.out.println("SCHED_TEST: function isInSubQueue(id) with id=sb1");
        System.out.println("SCHED_TEST: should be true!");
        b = queue.isInSubQueue("sb1");
        System.out.println("SCHED_TEST: "+b);
    
    }
}
