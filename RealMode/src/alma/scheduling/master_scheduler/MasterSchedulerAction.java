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
 * File MasterSchedulerAction.java
 * 
 */
package ALMA.scheduling.master_scheduler;

import alma.acs.nc.SimpleSupplier;

import ALMA.scheduling.NothingCanBeScheduledEvent;

/**
 * The MasterSchedulerAction class encapsulates periodic actions that
 * must be executed by the MasterScheduler.  these include, getting new 
 * scheduling blocks and project definitions from the archive, analyzing the
 * master queue for fixed events and possible changes in sub-array 
 * configuratiions.
 * 
 * @version 1.00 May 5, 2003
 * @author Allen Farris
 */
public class MasterSchedulerAction implements Runnable {
    private ALMAArchive archive;
    private MasterSBQueue queue;
    private SimpleSupplier simpleSupplier;
	/**
	 * Constructor for MasterSchedulerAction.
	 */
	public MasterSchedulerAction() {
		super();
        String[] names = new String[3];
        names[SimpleSupplier.CHANNELPOS] = ALMA.scheduling.CHANNELNAME.value;
        names[SimpleSupplier.TYPEPOS] = ALMA.acsnc.DEFAULTTYPE.value;
        names[SimpleSupplier.HELPERPOS] = new
            String("ALMA.scheduling.NothingCanBeScheduledEventHelper");
        simpleSupplier = new SimpleSupplier(names);
	}
    public void setArchive(ALMAArchive a) {
        this.archive = a;
    }
    public void setMasterSBQueue(MasterSBQueue q) {
        this.queue = q;
    }

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
        while(true) {
            try {
                this.wait(30000); //wait 5 minutes
                // Check for new SBs from archive
                if(archive.checkNewSB()) { //if true do something with the new sbs
                }
                // Check for new project defs from archive
                if(archive.checkNewProjectDefs()) { //if true do something with new project defs
                }
                // Check master queue for fixed events
                if(queue.checkNewFixedEvents()) {
                }
                // Check for sub-array config changes
                // dont know where this info comes from..
            } catch (InterruptedException e) {
            }
            System.out.println("MB Action running");
        }
	}

    public void start() {
        System.out.println("MB Action started");
    }
    public void stop() {
        System.out.println("MB Action stopped");
    }
    public void sendNothingCanBeScheduledEvent(NothingCanBeScheduledEvent ev) {
        try {
            simpleSupplier.publishEvent(ev); 
        } catch (Exception e) {
        }
    }

	public static void main(String[] args) {
	}
}

