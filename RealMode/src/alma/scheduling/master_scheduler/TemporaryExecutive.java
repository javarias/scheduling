/**
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
 * File TemporaryExecutive.java
 * 
 */

package alma.scheduling.master_scheduler;

import alma.entity.xmlbinding.schedblock.SchedBlock;
//import alma.bo.SchedBlock;
import alma.scheduling.UnidentifiedResponse;


/**
 *  This is a temporary class that will act as my executive.
 *  It will:        
 *      - select the top sb from the queue and send a reply
 *
 *  @author Sohaila Roberts
 */
public class TemporaryExecutive {
    private MasterScheduler ms;
    private MasterSBQueue queue;

    public TemporaryExecutive(MasterScheduler m, MasterSBQueue q) {
        this.ms = m;
        this.queue = q;
        System.out.println("Exec created");
    }

    /**
     * Picks a random SB (between 1 & 5) to be selected!
     */
    public void selectSB(String[] idList, String messageId) {
        int pos = Math.round((float)(Math.random()*5));
        //System.out.println("position="+pos);
        if(pos == 5) {
            pos = 4; //coz queue only filled from 0-4
        }
        //SchedBlock sb = queue.getSchedBlock(pos);
        SchedBlock sb = queue.getSB(pos);
        String reply = sb.getSchedBlockEntity().getEntityId();
        //String reply = sb.getId();
        System.out.println("Reply="+reply);
        try {
            ms.response(messageId, reply);
        } catch(UnidentifiedResponse e) {}
    }
}
