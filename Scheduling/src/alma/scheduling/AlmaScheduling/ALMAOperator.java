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
 * File ALMAOperator.java
 */
package alma.scheduling.AlmaScheduling;

import java.util.logging.Logger;

import alma.acs.container.ContainerServices;

import alma.scheduling.Define.Operator;
import alma.scheduling.Define.BestSB;
import alma.scheduling.MasterScheduler.Message;
import alma.scheduling.MasterScheduler.MessageQueue;

import alma.entities.commonentity.EntityT;
/**
 * @author Sohaila Roberts
 */
public class ALMAOperator implements Operator {
    private ContainerServices containerServices;
    private MessageQueue messageQueue;
    private Logger logger;
    
    public ALMAOperator(ContainerServices cs, MessageQueue queue) {
        this.containerServices = cs;
        this.logger = cs.getLogger();
        this.messageQueue = queue;
    }

    /**
     *
     * @author Sohaila Roberts
     */
    public void setWaitTime(int seconds) {
    }

    /** 
     *
     */
    public void send(String message) {
    }

    /**
     * Given the list of all the possible best SBs the operator selects
     * which SB is best to schedule now! 
     * @param best The selection of possible best SBs
     * @return String The id of the selected SB.
     */
    public String selectSB(BestSB best, Message message) {
        // Temporary solution to giving the messages unique IDs!
        EntityT entity = new EntityT();
        try { 
            containerServices.assignUniqueEntityId(entity);
        } catch(Exception e) {}
        message.setMessageId(entity.getEntityId());
        Thread timer = new Thread(new SelectSBTimer(1000));
        timer.start();
        message.setTimer(timer);
        /////////////////////////////
        try {
            messageQueue.addMessage(message);
        } catch(Exception e) {
            logger.severe("SCHEDULING: error adding a message!");
            logger.severe(e.toString());
            e.printStackTrace();
        }
        String bestSBId= best.getBestSelection();
        //bestSBId is the reply
        //got best SB selection so now we respond via the MasterScheduler
        try {
            (alma.scheduling.MasterSchedulerIFHelper.narrow( 
                containerServices.getDefaultComponent(
                    "IDL:alma/scheduling/MasterSchedulerIF:1.0"))).response(
                        message.getMessageId(), bestSBId);
        } catch(Exception e) {
            logger.severe("SCHEDULING: error getting MasterScheduler Component!");
            logger.severe(e.toString());
            e.printStackTrace();
        }
        try {
            timer.join();
            logger.info("SCHEDULING: timer joined!");
        } catch(InterruptedException e) {
            logger.info("SCHEDULING: timer was interrupted!");
        }
        System.out.println("best sb id = "+bestSBId);
        return bestSBId;
    }
    
    /**
     *
     */
    public boolean confirmAntennaActive(int antennaId) {
        return true;
    }

    public boolean confirmSubarrayCreation(int[] antennaId) {
        return true;
    }
}
