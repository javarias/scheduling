/*
 * ALMA - Atacama Large Millimiter Array
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
 * File GUIController.java
 * 
 */
package alma.scheduling.GUI.InteractiveSchedGUI;

import java.net.URL;
import alma.acs.container.ContainerServices;
import alma.scheduling.Define.SB;
import alma.scheduling.Define.Project;
import alma.scheduling.Define.ProjectQueue;
import alma.scheduling.Define.TimeInterval;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.SchedulingException;
import alma.scheduling.Scheduler.InteractiveScheduler;
import alma.scheduling.Scheduler.SchedulerConfiguration;
import alma.scheduling.AlmaScheduling.ALMAProjectManager;
/**
 * A controller for the Interactive Scheduling GUI. 
 * All the functionality that is required from the the GUI
 * is implemented here.
 *
 *  @version 1.00 Dec 18, 2003
 *  @author Sohaila Lucero 
 */
public class GUIController implements Runnable {
    private SchedulerConfiguration config;
    private String userlogin="";
    private String defaultProjectId;
    private GUI gui;
    private InteractiveScheduler scheduler;
    private ContainerServices containerServices;

    public GUIController(SchedulerConfiguration s, ContainerServices cs) {
        this.config = s;
        this.containerServices = cs;
        try {
            this.scheduler = new InteractiveScheduler(config);
            try {
                String[] tmp = getProjectIds();
                defaultProjectId = tmp[0];
            } catch(Exception e) {
            }
        } catch(SchedulingException e) {
           // throw new SchedulingException("Problem starting interactive scheduling", e);
        }
    }

    public GUIController() {
    }

    /**
      *
      */
    protected URL getImage(String name) {
        return this.getClass().getClassLoader().getResource(
            "alma/scheduling/Image/"+name);
    }

    ////////////////////////////////////////////////////

    /**
      *
      */
    public String getLogin() {
        return userlogin;
    }

    /**
     * Returns a list of the schedblock ids.
     * 
     */
    public SB[] getSBs() {
        return config.getQueue().getAll();
    }
    
    public SB getSB(String uid) {
        return config.getQueue().get(uid);
    }

    public void addSB(SB sb){
        config.getQueue().add(sb);
    }
    
    public void updateProject(Project p) {
        ((ALMAProjectManager)config.getProjectManager()).getProjectQueue().replace(p);
    }

    public void getSBUpdates() {
        // get project from archive and compare it with the project in the project queue 
        try {

            Project newProj = ((ALMAProjectManager)config.getProjectManager())
                .getProject(defaultProjectId);
            Project oldProj = ((ALMAProjectManager)config.getProjectManager())
                .getProjectQueue().get(defaultProjectId);
            
            System.out.println("New project " +newProj.getProgram().getTotalSBs());
            System.out.println("Old project " +oldProj.getProgram().getTotalSBs());
            
            //update new project in project queue
            updateProject(newProj);
            
            if(newProj.getProgram().getTotalSBs() == oldProj.getProgram().getTotalSBs()) {
                // same number, check that they're all the same
                // compareSBs will return false if there is a sb in either project that isn't in the other
                if( !((ALMAProjectManager)config.getProjectManager()).compareSBs(newProj, oldProj) ) {
                    System.out.println("SCHEDULING: There was a problem comparing.");
                }
                System.out.println("Comparing identical sized total sbs");
                
            } else if(newProj.getProgram().getTotalSBs() > oldProj.getProgram().getTotalSBs()) {
                System.out.println("SCHEDULING: There are new SBs.");
                SB[] sbs = ((ALMAProjectManager)config.getProjectManager()).getNewSBs(newProj, oldProj);
                for(int i=0; i < sbs.length; i++){
                    //System.out.println("adding sb "+ i);
                    //System.out.println("Stupid sb has id = "+ sbs[i].getId());
                    addSB(sbs[i]);
                }
                System.out.println("size of sb queue = "+ config.getQueue().size());
            } else if(newProj.getProgram().getTotalSBs() < oldProj.getProgram().getTotalSBs()) {
                System.out.println("SCHEDULING: There were some SBs deleted.");
                SB[] sbs = ((ALMAProjectManager)config.getProjectManager()).getNewSBs(newProj, oldProj);
                //there are less sbs

            }
                
        } catch(Exception e ) {
            e.printStackTrace();
        }
        
    }

    public String[] getProjectIds() {
        String[] tmp;
        try {
            tmp = ((ALMAProjectManager)config.getProjectManager()).getProjectQueue().getAllIds();
        } catch(Exception e) {
            tmp = new String[1];
            tmp[0] = "No projects loaded.";
        }
        return tmp;
    }
    
    public String getDefaultProjectId() {
        return defaultProjectId;
    }

    public void setDefaultProjectId(String id) {
        defaultProjectId = id;
    }

    public Project getProject(String id) {
        return ((ALMAProjectManager)config.getProjectManager()).getProjectQueue().get(id);
    }

    /**
     *  Deletes the SB from the schedulers queue.
     */
     public void deleteSB(String sb_id) {
        config.getQueue().remove(sb_id);
     }

     /**
       *
       */
     public void executeSB(String sb_id) {
        try {
            //Set the start time
            SB selectedSB = config.getQueue().get(sb_id);
            //selectedSB.setStartTime(new DateTime(System.currentTimeMillis()));
            selectedSB.setStartTime(config.getClock().getDateTime());
            //config.getControl().execSB(config.getArrayName(), sb_id);
            scheduler.execute(sb_id);
        } catch(SchedulingException e) {
            System.out.println("SCHEDULING: error in executing the sb.");
            e.printStackTrace();
        }

    }

    /**
      *
      */
    public void openObservingTool(String projectID) {
        //SB sb = config.getQueue().get(sbid);
        //String projectID = (sb.getProject()).getId();
        OpenOT ot = new OpenOT(projectID, containerServices);
        Thread t = new Thread(ot);
        t.start();
    }

     //public short[] selectAntennasForSubarray() {
     
     //}

    ////////////////////////////////////////////////////
    
    /**
      *
      */
    public void setLogin(String id) throws SchedulingException {
        SB[] sbs = config.getQueue().getAll();
        userlogin = id;
        scheduler.login(userlogin, defaultProjectId, sbs[0]);
        config.setCommandedEndTime(DateTime.add(new DateTime(System.currentTimeMillis()),3600 ));
    }

    /**
      *
      */
    public void endSession() throws SchedulingException {
        userlogin = "";
        scheduler.logout();
    }

    /**
      *
      */
    public void refreshSBQueue() {
        config.getProjectManager().getProjectManagerTaskControl().interruptTask();
    }
    
    /**
      *
      */
    public void openArchiveQueryWindow() {
        ArchiveQueryWindowController interactiveGUI =
                new ArchiveQueryWindowController(config, containerServices);
        Thread scheduler_thread = containerServices.getThreadFactory().newThread(interactiveGUI);
        scheduler_thread.start();

    }
    
    ////////////////////////////////////////////////////

    /**
      *
      */
    public void run() {
        this.gui = new GUI(this);
    }

    /**
      *
      */
    public static void main(String[] args) {
            GUIController c = new GUIController();
            c.run();
    }
}
