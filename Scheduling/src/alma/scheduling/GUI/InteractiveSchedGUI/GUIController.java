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
import java.util.logging.Logger;
import alma.acs.container.ContainerServices;
import alma.scheduling.Define.SB;
import alma.scheduling.Define.Project;
import alma.scheduling.Define.ProjectQueue;
import alma.scheduling.Define.TimeInterval;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.SchedulingException;
import alma.scheduling.Scheduler.InteractiveScheduler;
import alma.scheduling.Scheduler.SchedulerConfiguration;
//import alma.scheduling.AlmaScheduling.ALMAProjectManager;
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
    private Logger logger;

    public GUIController(InteractiveScheduler s, ContainerServices cs) {
        this.scheduler = s;
        this.config = scheduler.getConfiguration();
        this.containerServices = cs;
        this.logger = cs.getLogger();
        //this.scheduler = new InteractiveScheduler(config);
        try {
            String[] tmp = getProjectIds();
            defaultProjectId = tmp[0];
        } catch(Exception e) {
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
        config.getProjectManager().getProjectQueue().replace(p);
    }

    /*
    public void updateSBs(Project p) {
        config.getProjectManager().updateSBQueue(p);
    }*/

    public void getSBUpdates() {
        System.out.println("Getting SB updates");
        try {
            //tell the project manager to check for updates (pollArchive)
            config.getProjectManager().getUpdates(); //calls pollArchive
            //get those updates 
            SB[] sbs = config.getProjectManager().getSBsForProject(defaultProjectId);
            for(int i=0; i < sbs.length; i++){
                sbs[i].setType(SB.INTERACTIVE);
            }

            // update sbQueue
            for(int i=0; i < sbs.length;i++){
                //check if its in queue already
                if(config.getQueue().isExists(sbs[i].getId()) ){
                    //in queue, replace just in case
                    config.getQueue().replace(sbs[i]);
                } else {
                    //not in queue, add it
                    config.getQueue().add(sbs[i]);
                }
            }
        } catch(Exception e ) {
            e.printStackTrace(System.out);
        }
        
    }

    public String[] getProjectIds() {
        String[] tmp;
        try {
            tmp = config.getProjectManager().getProjectQueue().getAllIds();
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
        return config.getProjectManager().getProjectQueue().get(id);
    }

    /**
     *  Deletes the SB from the schedulers queue.
     */
     public void deleteSB(String sb_id) {
        config.getQueue().remove(sb_id);
        //TODO update listing
     }

     /**
       *
       */
     public void executeSB(String sb_id) throws SchedulingException {
        //first check if anything is already running
  //      if(config.getQueue().getRunning().length >0 ) {
  //         throw new SchedulingException("There is a SB Already running!");
  //      }
        try {
            //Set the start time
            SB selectedSB = config.getQueue().get(sb_id);
            //selectedSB.setStartTime(new DateTime(System.currentTimeMillis()));
            selectedSB.setStartTime(config.getClock().getDateTime());
            //config.getControl().execSB(config.getArrayName(), sb_id);
            scheduler.execute(sb_id);
        } catch(SchedulingException e) {
            System.out.println("SCHEDULING: error in executing the sb. "+e.toString());
            e.printStackTrace(System.out);
            throw new SchedulingException(e);
        }

    }
    

    public void stopSB() throws SchedulingException {
        SB[] runningSB ;
        try {
            //get the sb from the config's queue that is currently set to 'running'
            //should only get one here, but if by chance we do get more, stop them all!
            runningSB = config.getQueue().getRunning();
            if(runningSB.length > 1){
                logger.warning("SCHEDULING: There was more than one SB running interactively on the same array!");
            }
            String id;
            for(int i=0; i < runningSB.length; i++){ //stop them all, just in case might throw ctrl into error
                id = runningSB[i].getId();
                //config.getControl().stopSB(config.getArrayName(), id);
                scheduler.stop(id);
            }
        } catch(Exception e) {
            logger.severe("SCHEDULING: Error stopping SB(s). "+ e.toString());
            throw new SchedulingException (e);
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
                new ArchiveQueryWindowController(scheduler, containerServices);
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

    public void exit() {
        try {
            config.getControl().destroyArray(config.getArrayName());
        } catch (Exception e) {
            logger.severe("SCHEDULING: in IS-GC error = "+e.toString());
            logger.severe("SCHEDULING: Error destroying array "+config.getArrayName());
        }
    }

    public void close() {
    	logger.info("SCHEDULING: closing IS gui controller!");
        this.gui.dispose();
	    exit();
    }

    /**
      *
      */
    public static void main(String[] args) {
            GUIController c = new GUIController();
            c.run();
    }
}
