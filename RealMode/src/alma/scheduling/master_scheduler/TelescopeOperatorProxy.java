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
 * File TelescopeOperatorProxy.java
 * 
 */
package alma.scheduling.master_scheduler;

import alma.acs.container.ContainerServices;

/**
 * The interface to the telescope operator.  Methods in this class use
 * interfaces in the Executive Subsystem in their implementation.
 * 
 * @version 1.00 May 5, 2003
 * @author Allen Farris
 */
public class TelescopeOperatorProxy
	implements Scheduling_to_TelescopeOperator {

	public TelescopeOperatorProxy (boolean isSimulation, ContainerServices container) {
		System.out.println("The TelescopeOperatorProxy has been constructed.");
	}

	/**
	 * @see master_scheduler.Scheduling_to_TelescopeOperator#send(String)
	 */
	public void send(String message) {
	}

	/**
	 * @see master_scheduler.Scheduling_to_TelescopeOperator#SelectSB(String[], String)
	 */
	public void selectSB(String[] sbIdList, String messageId) {
        Thread timer = new Thread(new SelectSBTimer(5*60*1000)); //5 minutes in milliseconds
        //messageQueue.add(messageId, timer);
        timer.start();
        //executive.selectSB(sbIdList, messageId);
        try {
            timer.join();
        } catch(InterruptedException e) {
        }
        //return messageQueue.getMessage(messageId).getReply();

	}

	/**
	 * @see master_scheduler.Scheduling_to_TelescopeOperator#confirmAntennaActive(short, String)
	 */
	public void confirmAntennaActive(short antennaId, String messageId) {
	}

	/**
	 * @see master_scheduler.Scheduling_to_TelescopeOperator#comfirmSubarrayCreation(short[], String)
	 */
	public void comfirmSubarrayCreation(
		short[] antennaIdList,
		String messageId) {
	}

	public static void main(String[] args) {
	}

    class SelectSBTimer implements Runnable {
        private Thread thread;
        private long delay;
        public SelectSBTimer(long delay) {
            this.thread = new Thread("SB select Timer");
            this.delay = delay;
        }
        public void run() {
            try {
                thread.sleep(delay);
            }catch(InterruptedException e) {
            }
        }
    }
}

