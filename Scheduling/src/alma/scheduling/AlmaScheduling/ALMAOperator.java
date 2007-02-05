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

import alma.scheduling.SBLite;
import alma.acs.container.ContainerServices;

import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.scheduling.Define.Operator;
import alma.scheduling.Define.LiteSB;
import alma.scheduling.Define.BestSB;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.NothingCanBeScheduled;
import alma.scheduling.MasterScheduler.Message;
import alma.scheduling.MasterScheduler.MessageQueue;
import alma.entities.commonentity.EntityT;

import alma.exec.ReqType;
import alma.exec.SubSystem;
import alma.scheduling.Dynamic_Operator_to_Scheduling;
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
        } catch(AcsJContainerServicesEx e) {
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
    public String selectSB(BestSB best, Message message, String arrayName, String schedulerId) {
        // Temporary solution to giving the messages unique IDs!
        EntityT entity = new EntityT();
        try { 
            containerServices.assignUniqueEntityId(entity);
        } catch(Exception e) {}
        message.setMessageId(entity.getEntityId());
        //5 minute timer
        Thread timer = containerServices.getThreadFactory().newThread(new SelectSBTimer((60 * 5)* 1000));
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
            logger.finest("SCHEDULING: best sb id == null. no visible targets");
            best = new BestSB(new NothingCanBeScheduled(
                        new DateTime(System.currentTimeMillis()),
                            NothingCanBeScheduled.NoVisibleTargets, ""));
        } else {
            //try to ask operator now to select!
            try{
                //execSchedOperator = alma.exec.Scheduling_to_TelescopeOperatorHelper.narrow(
                        //containerServices.getComponent("EXEC_SCHEDULINGOPERATOR"));

                LiteSB[] liteSB = best.getLiteSBs();
                SBLite[] sbLites = new SBLite[liteSB.length];
                for(int i=0;i< liteSB.length; i++) {
                    sbLites[i] = convertToSBLite(liteSB[i]);
                }
                //TODO Send list to Dynamic_Operator Component
                //get DSComp
                //dsComp.setAllSBs(??)
                //dsComp.setTopSBs(sbLites);
                Dynamic_Operator_to_Scheduling dsComp = 
                    alma.scheduling.Dynamic_Operator_to_SchedulingHelper.
                        narrow(containerServices.getComponent("DS_"+arrayName));
                dsComp.setTopSbs(sbLites, message.getMessageId());
                containerServices.releaseComponent(dsComp.name());
                //execSchedOperator.selectSB(message.getMessageId(), arrayName, sbLites, 5);
                        
                //containerServices.releaseComponent("EXEC_SCHEDULINGOPERATOR");
            } catch(Exception ce) {
            //} catch(AcsJContainerServicesEx ce) {
                logger.warning("SCHEDULING: Operator component not available, "+
                        "scheduling will pick.");
                //return "TIMEOUT:Operator component not available";
        
                try {
                    (alma.scheduling.MasterSchedulerIFHelper.narrow( 
                        containerServices.getDefaultComponent(
                            "IDL:alma/scheduling/MasterSchedulerIF:1.0"))).response(
                                message.getMessageId(), bestSBId);
                } catch(Exception e) {
                    logger.severe("SCHEDULING: error getting MasterScheduler Component!");
                    logger.severe(e.toString());
                    e.printStackTrace(System.out);
                //TODO: lets throw an alarm here because operator didn't pick and scheduling
                // couldn't pick...
                }
            }
        }
        try {
            timer.join();
            logger.info("SCHEDULING: timer joined! timeout reached?");
            message.setReply(bestSBId);
        } catch(InterruptedException e) {
            logger.info("SCHEDULING: timer was interrupted!");
        }
        logger.info("SCHEDULING: best sb id = "+message.getReply());
        return message.getReply();
    }

    private SBLite convertToSBLite(LiteSB l){
        SBLite sb= new SBLite();
        sb.schedBlockRef = l.getSBRef();
        sb.projectRef = l.getProjRef();
        sb.obsUnitsetRef = l.getOUSRef();
        sb.sbName = l.getSBName();
        sb.projectName = l.getProjName();
        sb.PI = l.getPI();
        sb.priority = l.getPri();
        sb.ra = l.getRA();
        sb.dec = l.getDEC();
        sb.freq = l.getFreq();
        sb.maxTime =l.getMaxTime();
        sb.score =l.getScore();
        sb.success = l.getSuccess();
        sb.rank = l.getRank();
        return sb;
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
