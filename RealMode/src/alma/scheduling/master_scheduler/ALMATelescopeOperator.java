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
 * File ALMATelescopeOperator.java
 */
 
package ALMA.scheduling.master_scheduler;

import java.util.logging.Logger;
import java.util.logging.Level;

import ALMA.scheduling.MS;
import ALMA.scheduling.UnidentifiedResponse;
import ALMA.scheduling.master_scheduler.MSHelper;
import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;

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
    //private TemporaryExecutive executive;
    private MS masterSchedulerComp;

	/**
	 * 
	 */
	public ALMATelescopeOperator(boolean isSimulation, ContainerServices container) {
		super();
        this.container = container;
        this.logger = container.getLogger();
        this.isSimulation = isSimulation;
        System.out.println("The ALMATelescopeOperator has been constructed.");
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
        
        //System.out.println(toString());
        
        Thread timer = new Thread(new SelectSBTimer(1000)); 
        //5 minutes in milliseconds
        timer.start();
        Message m = new Message(messageId,timer);
//        Message m = new Message(messageId);
        messageQueue.addMessage(m);
//        Message m = messageQueue.getMessage(messageId);
//        m.setThread(timer);
        
        logger.log(Level.INFO,"in TO, messageQueue size ="+messageQueue.size());
        //System.out.println(messageQueue.toString());
        logger.log(Level.INFO,"in TO. sbidlist len = "+ sbIdList.length);

        int pos = 0; //First one.
        String reply = sbIdList[pos];
        logger.log(Level.INFO,"in TO. reply ="+ reply);
        try {
            this.masterSchedulerComp = ALMA.scheduling.MSHelper.narrow(
                container.getComponent("MASTER_SCHEDULER"));
            logger.log(Level.INFO,"in TO. ms response about to be called");
            masterSchedulerComp.response(messageId, reply);
            logger.log(Level.INFO,"in TO. ms response called");
            //TODO= container.release("MASTER_SCHEDULER");
//            if(timer.isAlive()){
//                timer.interrupt();
//            }
        } catch(ContainerException e) {
        } catch(UnidentifiedResponse e) {
        }

        try {
            timer.join();
            logger.log(Level.INFO,"in TO. timer.join()");
        } catch(InterruptedException e) {
            logger.log(Level.INFO,"in TO. timer interrupted");
        }

        if(messageQueue == null) {
            logger.log(Level.INFO,"in TO. messageQueue is null");
        }
        if(messageId == null) {
            logger.log(Level.INFO,"in TO. messageId is null");
        }
        if(messageQueue.getMessage(messageId) == null) {
            logger.log(Level.INFO,"in TO. mesage is null");
            return "";
        }
        if(messageQueue.getMessage(messageId).getReply() == null){
            logger.log(Level.INFO,"in TO. message reply is null");
        }

        return messageQueue.getMessage(messageId).getReply();
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
        System.out.println("Message queue set in operator");
    }

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
