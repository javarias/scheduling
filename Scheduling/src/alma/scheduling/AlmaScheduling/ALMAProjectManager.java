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
 * File ALMAProjectManager.java
 * 
 */
package alma.scheduling.AlmaScheduling;

//import java.util.logging.Logger;

import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;

import alma.scheduling.Define.SB;
import alma.scheduling.Define.SBQueue;
import alma.scheduling.Define.Project;
import alma.scheduling.ObsProjectManager.ProjectManager;

/**
 *
 * @author Sohaila Lucero
 */
public class ALMAProjectManager extends ProjectManager {
    //The container services
    private ContainerServices containerServices;
    private ALMAArchive archive;
    private SBQueue sbQueue;

    public ALMAProjectManager(ContainerServices cs, ALMAArchive a, SBQueue q) {
        super();
        this.containerServices = cs;
        this.logger = cs.getLogger();
        this.archive = a;
        this.sbQueue = q;
        pollArchive();
        //temporary!
        try {
            System.out.println("Getting all projects in PM");
            Project[] projs = archive.getAllProject();
            System.out.println("projs length = "+projs.length);
        }catch(Exception e) {
            e.toString();
        }
    }

    /**
     * Calls the run from ProjectManager and then does 
     * some other stuff.
     */
    public void run() {
        super.run();
        while(!stopCommand) {
            try {
                //Thread.sleep(5*60*1000);
                Thread.sleep(60*1000);
            }catch(InterruptedException e) {
                logger.info("SCHEDULING: ProjectManager Interrupted!");
            }
            pollArchive();
        }
    }

    /** 
     * Check the archive periodically to see if there have been any additional
     * scheduling blocks added. This function will eventually poll for new 
     * projects.
     *
     */
    private void pollArchive() {
        
        try {
            SB[] sbs = archive.getAllSB();
            System.out.println("sbs length = "+ sbs.length);
            String[] ids = sbQueue.getAllIds();
            boolean sb_present = false;
            for(int i=0; i< sbs.length; i++){
                String tmp_id = sbs[i].getId();
                if(ids.length == 0) {
                    sb_present = false;
                }
                for(int j=0; j< ids.length; j++) {
                    if(tmp_id.equals(ids[j])) {
                        sb_present = true;
                    } else {
                        sb_present = false;
                    }
                }
                if(!sb_present) {
                    sbQueue.add(sbs[i]);
                }
            }
        } catch (Exception e) {
            logger.severe("SCHEDULING: Error polling the archvie.");
            logger.severe("SCHEDULING: "+e.toString());
            e.printStackTrace();
        }
        
    }
}
