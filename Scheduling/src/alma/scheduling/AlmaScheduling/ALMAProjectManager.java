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

import java.util.Vector;
import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;

import alma.scheduling.Define.SB;
import alma.scheduling.Define.SBQueue;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.Project;
import alma.scheduling.Define.Program;
import alma.scheduling.Define.ProjectQueue;
import alma.scheduling.ObsProjectManager.ProjectManager;
import alma.scheduling.ObsProjectManager.ProjectManagerTaskControl;

/**
 *
 * @author Sohaila Lucero
 */
public class ALMAProjectManager extends ProjectManager {
    //The container services
    private ContainerServices containerServices;
    private ALMAArchive archive;
    private SBQueue sbQueue;
    private ProjectQueue pQueue;

    public ALMAProjectManager(ContainerServices cs, ALMAArchive a, SBQueue q) {
        super();
        this.containerServices = cs;
        this.logger = cs.getLogger();
        this.archive = a;
        this.sbQueue = q;
        this.pQueue = new ProjectQueue(pollArchive());
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
            if(!stopCommand){
                pollArchive();
            }
        }
    }

    /** 
     * Check the archive periodically to see if there have been any additional
     * scheduling blocks added. This function will eventually poll for new 
     * projects.
     *
     */
    private Project[] pollArchive() {
        logger.info("SCHEDULING: Polling ARCHIVE");
        boolean sb_present = false;
        Project[] projs = null;
        try {
            String[] ids;
            projs = archive.getAllProject();
            logger.info("SCHEDULING: projects = "+projs.length);
            SB[] sbs = getSBsFromProjects(projs);
            logger.info("SCHEDULING: total SBs = "+sbs.length);
            for(int i=0; i< sbs.length; i++) {
                ids = sbQueue.getAllIds();
                logger.info("SCHEDULING: sb # in queue = "+ids.length);
                String tmp_id = sbs[i].getId();
                if(ids.length == 0) { //nothing in the SBQueue
                    System.out.println("SCHEDULING: id's length == 0");
                    sb_present = false;
                }
                for(int j=0; j< ids.length; j++) {
                    logger.info("SCHEDULING: DOES "+tmp_id+" == "+ids[j]);
                    if(tmp_id.equals(ids[j])) { // SB already in queue
                        sb_present = true;
                        break;
                    } else {
                        sb_present = false; //not in queue
                    }
                }
                if(!sb_present) { //not already in queue so add it to queue.
                    System.out.println("SCHEDULING: sbpresent false; adding "+tmp_id +" to queue");
                    sbQueue.add(sbs[i]);
                }
            }
        }catch(Exception e) {
            e.toString();
        }
        return projs;
    }


    /** 
     * Get the SB ids from all the projects and retrieve the SB objects from 
     * the archive.
     *
     * @param Project[]
     * @return SB[]
     */
    private SB[] getSBsFromProjects(Project[] projects) {
        logger.info("SCHEDULING: Getting SBs from projects");
        Vector tmpsbs = new Vector();
        try {
            for(int i=0; i< projects.length; i++) {
                String[] ids = ((ALMAProject)projects[i]).getSBIds();
                System.out.println("Project = "+projects[i].getId());
                System.out.println("SB length... "+ids.length);
                for (int j=0;j < ids.length; j++) {
                    //Program prog = new Program("not implemented yet");
                    //prog.setProject(projects[i]);
                    SB sb = archive.getSB(ids[j]);
                    sb.getParent().setProject(projects[i]);
                    sb.setTimeOfCreation(new DateTime(System.currentTimeMillis()));
                    sb.setProject(projects[i]);
                    sb.getParent().addMember(sb);
                    projects[i].setProgram(sb.getParent());
                    tmpsbs.add(sb);
                }
            }
        } catch (Exception e) {
            logger.severe("SCHEDULING: "+e.toString());
            e.printStackTrace();
        }
        System.out.println("SB total from all projects = "+tmpsbs.size());
        SB[] schedblocks = new SB[tmpsbs.size()];
        for(int i=0; i < tmpsbs.size();i++){
            schedblocks[i] = (SB)tmpsbs.elementAt(i);
        }
        System.out.println("schedblock[] size == "+schedblocks.length);
        return schedblocks;
    }

    public void sessionStart(String sessionId, String sb_id) {
        String proj_id = (sbQueue.get(sb_id)).getProject().getId();
        logger.info("SCHEDULING:(session info) Session ("+sessionId+") has started.");
        logger.info("SCHEDULING:(session info) Project id = "+proj_id+".");
        logger.info("SCHEDULING:(session info) SB id = "+sb_id+".");
    }

    public void sessionEnd(String sb_id) {
        String proj_id = (sbQueue.get(sb_id)).getProject().getId();
        logger.info("SCHEDULING:(session info) Session has ended.");
        logger.info("SCHEDULING:(session info) Project id = "+proj_id+".");
        logger.info("SCHEDULING:(session info) SB id = "+sb_id+".");
    }

}
