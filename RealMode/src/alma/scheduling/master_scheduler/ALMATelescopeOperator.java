/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
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
 * File ALMATelescopeOperator.java
 */
 
package alma.scheduling.master_scheduler;

import java.util.logging.Logger;
import java.util.logging.Level;

import alma.scheduling.MS;
import alma.scheduling.UnidentifiedResponse;
import alma.scheduling.master_scheduler.MSHelper;
import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;
import alma.scheduling.simulator.*;
/**
 * Description 
 *  The interface to the telescope operator.  Methods in this class use
 *  interfaces in the Executive Subsystem in their implementation.
 * 
 * @version 1.00  Jun 3, 2003
 * @author Allen Farris
 */
public class ALMATelescopeOperator implements Scheduling_to_TelescopeOperator {
    private Logger logger;
    private boolean isSimulation;
    private ContainerServices container;
    private MessageQueue messageQueue;
    private ALMAArchive archive;
    private MS masterSchedulerComp;

	/**
	 * 
	 */
	public ALMATelescopeOperator(boolean isSimulation, 
        ContainerServices container, ALMAArchive a) {
		super();
        this.container = container;
        this.logger = container.getLogger();
        this.isSimulation = isSimulation;
        this.archive = a;
        this.logger = container.getLogger();
        logger.info("SCHEDULING: The ALMATelescopeOperator has been constructed.");
	}


	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.Scheduling_to_TelescopeOperator#send(java.lang.String)
	 */
	public void send(String message) {
		// TODO Auto-generated method stub

	}

	/* 
     *
	 */
	public String selectSB(String[] sbIdList, String messageId) {
        if(sbIdList == null || sbIdList.length == 0) {
            logger.info("SCHEDULING: There are no SBs to select from!");
            logger.info("SCHEDULING: Put some in the archive and try again.");
            return null;
        }
        Thread timer = new Thread(new SelectSBTimer(1000)); 
        //5 minutes in milliseconds
        timer.start();
        Message m = new Message(messageId,timer);
        try {
            messageQueue.addMessage(m);
        } catch(Exception e) {
        }
        //search sb ids to get the first non-complete sb
        int pos = 0; //First one.
        for(int i=0; i < sbIdList.length; i++) {
            if(!isCompletedSb(sbIdList[i])) { //it is non-complete
                pos = i;
                break;
            } //else just loop again
        }
        if(pos ==0 && isCompletedSb(sbIdList[0])) {
            //if all are complete return.
            return null;
        }   
        String reply = sbIdList[pos];
        logger.info("SCHEDULING: in TO. reply ="+ reply);
        if(isSimulation) {
            try {
                FullModeSimulatorImpl.getMasterScheduler().response(
                    messageId, reply);
            } catch(Exception e) {}
        } else {
            try {
                this.masterSchedulerComp = alma.scheduling.MSHelper.narrow(
                    container.getComponent("MASTER_SCHEDULER"));
                logger.info("SCHEDULING: in TO. ms response about to be called");
                masterSchedulerComp.response(messageId, reply);
                logger.info("SCHEDULING: in TO. ms response called");
            } catch(Exception e) {
            }
        }
        try {
            timer.join();
            logger.info("SCHEDULING: in TO. timer.join()");
        } catch(InterruptedException e) {
            logger.fine("SCHEDULING: in TO. timer interrupted");
        }

        if(messageQueue == null) {
            logger.info("SCHEDULING: in TO. messageQueue is null");
        }
        if(messageId == null) {
            logger.info("SCHEDULING: in TO. messageId is null");
        }
        if(messageQueue.getMessage(messageId) == null) {
            logger.info("SCHEDULING: in TO. mesage is null");
            return "";
        }
        if(messageQueue.getMessage(messageId).getReply() == null){
            logger.info("SCHEDULING: in TO. message reply is null");
        }

        return messageQueue.getMessage(messageId).getReply();
	}

    public boolean isCompletedSb(String id) {
        String completed = "completed";
        boolean result = false;
        try {
            String status = archive.getSchedBlock(id).getObsUnitControl().getSchedStatus().toString();
            logger.info("SCHEDULING: checking for non-complete SB");
        
            if((status != null) && (!status.equals(""))){
                if(status.equals("completed")) {
                    result = true;
                }
            } 
        } catch(NullPointerException e) {}
        return result;
    }

	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.Scheduling_to_TelescopeOperator#confirmAntennaActive(short, java.lang.String)
	 */
	public void confirmAntennaActive(short antennaId, String messageId) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.Scheduling_to_TelescopeOperator#comfirmSubarrayCreation(short[], java.lang.String)
	 */
	public void confirmSubarrayCreation(short[] antennaIdList, String messageId) {
		// TODO Auto-generated method stub

	}
    
    public void setMessageQueue(MessageQueue mq) {
        this.messageQueue = mq;
        logger.info("SCHEDULING: Message queue set in operator");
    }

    ////////////////////////////////////////////////////////////////
    // for interactive scheduling
    ////////////////////////////////////////////////////////////////

	public static void main(String[] args) {
	}

    class SelectSBTimer implements Runnable {
        private long delay;
        
        public SelectSBTimer(long delay) {
            this.delay = delay;
        }
        
        public void run() {
            try {
                Thread.sleep(delay);
            }catch(InterruptedException e) {
            }
        }
    }

}
