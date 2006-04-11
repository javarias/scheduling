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
import alma.acs.container.ContainerException;

import alma.scheduling.Define.Operator;
import alma.scheduling.Define.BestSB;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.NothingCanBeScheduled;
import alma.scheduling.MasterScheduler.Message;
import alma.scheduling.MasterScheduler.MessageQueue;
import alma.entities.commonentity.EntityT;

import alma.exec.ReqType;
import alma.exec.SubSystem;

/**
 * @author Sohaila Lucero
 */
public class ALMAOperator implements Operator {
    // container services 
    private ContainerServices containerServices;
    // queue to hold all messages
    private MessageQueue messageQueue;
    //logger
    private Logger logger;
    //The Operator Component.
    private alma.exec.Scheduling_to_TelescopeOperator execSchedOperator;
    /**
      *
      */
    public ALMAOperator(ContainerServices cs, MessageQueue queue) {
        this.containerServices = cs;
        this.logger = cs.getLogger();
        this.messageQueue = queue;
    }

    /**
      *
      * @param int
      */
    public void setWaitTime(int seconds) {
    }

    /** 
      * Sends a message to the Telescope Operator
      * @param String
      * @return String The reply from the operator if there was one
      */
    public String send(String message, String arrayName) {
        /*
        String reply ="";
        try{
            execOperator = alma.exec.OperatorHelper.narrow(
                    containerServices.getComponent("EXEC_OPERATOR"));
            reply = execOperator.askOperator(message, arrayName ,
                    SubSystem.SCHEDULING_SUBSYSTEM, ReqType.NORMAL_REQUEST, true, 5);
            containerServices.releaseComponent("EXEC_OPERATOR");
            return reply;
        } catch(ContainerException e) {
            logger.info("SCHEDULING: Operator component not available, scheduling will pick.");
            return "TIMEOUT:Operator component not available";
        }*/
        return "not implemented yet";
    }

    /**
     * Given the list of all the possible best SBs the operator selects
     * which SB is best to schedule now! 
     * @param BestSB The selection of possible best SBs
     * @param Message
     * @return String The id of the selected SB.
     */
    public String selectSB(BestSB best, Message message, String arrayName) {
        // Temporary solution to giving the messages unique IDs!
        EntityT entity = new EntityT();
        try { 
            containerServices.assignUniqueEntityId(entity);
        } catch(Exception e) {}
        message.setMessageId(entity.getEntityId());
        Thread timer = containerServices.getThreadFactory().newThread(new SelectSBTimer(1000));
        timer.start();
        message.setTimer(timer);
        /////////////////////////////
        try {
            messageQueue.addMessage(message);
        } catch(Exception e) {
            logger.severe("SCHEDULING: error adding a message!");
            logger.severe(e.toString());
            e.printStackTrace(System.out);
        }
        String bestSBId= best.getBestSelection(); //used when Exec's operator times out
        if(bestSBId == null) {
            logger.info("SCHEDULING: best sb id == null. no visible targets");
            best = new BestSB(new NothingCanBeScheduled(
                        new DateTime(System.currentTimeMillis()),
                            NothingCanBeScheduled.NoVisibleTargets, ""));
        } else {
            //bestSBId is the reply
            //got best SB selection so now we respond via the MasterScheduler
            /*
            try {
                (alma.scheduling.MasterSchedulerIFHelper.narrow( 
                    containerServices.getDefaultComponent(
                        "IDL:alma/scheduling/MasterSchedulerIF:1.0"))).response(
                            message.getMessageId(), bestSBId);
            } catch(Exception e) {
                logger.severe("SCHEDULING: error getting MasterScheduler Component!");
                logger.severe(e.toString());
                e.printStackTrace(System.out);
            }
            */
            //ask operator now to select!
            try{
                execSchedOperator = alma.exec.Scheduling_to_TelescopeOperatorHelper.narrow(
                        containerServices.getComponent("EXEC_SCHEDULINGOPERATOR"));

                execSchedOperator.selectSB(message.getMessageId(), arrayName, best.getSBLites(), 5);
                        
                containerServices.releaseComponent("EXEC_SCHEDULINGOPERATOR");
            } catch(ContainerException e) {
                logger.info("SCHEDULING: Operator component not available, "+
                        "scheduling will pick.");
                //return "TIMEOUT:Operator component not available";
            }
        }
        try {
            timer.join();
            logger.info("SCHEDULING: timer joined!");
        } catch(InterruptedException e) {
            //logger.info("SCHEDULING: timer was interrupted!");
        }
        logger.info("SCHEDULING: best sb id = "+message.getReply());
        return message.getReply();
    }
    
    /**
      *
      * @param String
      * @return boolean 
      */
    public boolean confirmAntennaActive(String antennaId) {
        return true;
    }

    /**
      *
      * @param String[]
      * @return boolean
      */
    public boolean confirmSubarrayCreation(String[] antennaId) {
        return true;
    }

     /**
       * Internal nested class which runs a timer for how long it takes
       * for the operator to select the sb before its automatically selected
       */
     class SelectSBTimer implements Runnable {
        private long delay;
        
        /**
          * @param long
          */
        public SelectSBTimer(long delay) {
            this.delay = delay;
        }
        
        public void run() {
            try {
                Thread.sleep(delay);
            }catch(InterruptedException e) {
                //System.out.println("interrupted in selected SB timer");
                //e.printStackTrace(System.out);
            }
        }
    }
  
}
