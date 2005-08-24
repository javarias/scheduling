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
/*
import alma.scheduling.Interactive_PI_to_Scheduling;
import alma.scheduling.SBExists;
import alma.scheduling.NoSuchSB;
import alma.scheduling.InvalidObject;
import alma.scheduling.InvalidOperation;
*/
import java.net.URL;
//import alma.entity.xmlbinding.schedblock.*;
//import alma.obsprep.bo.*;
//import alma.obsprep.bo.Target;
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

    public GUIController(SchedulerConfiguration s) {
        this.config = s;
        try {
            this.scheduler = new InteractiveScheduler(config);
            String[] tmp = getProjectIds();
            defaultProjectId = tmp[0];
        } catch(SchedulingException e) {
           // throw new SchedulingException("Problem starting interactive scheduling", e);
        }
    }

    public GUIController() {}

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

    public String[] getProjectIds() {
        return ((ALMAProjectManager)config.getProjectManager()).getProjectQueue().getAllIds();
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
            selectedSB.setStartTime(new DateTime(System.currentTimeMillis()));
            config.getControl().execSB(config.getArrayName(), sb_id);
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
        OpenOT ot = new OpenOT(projectID);
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
        userlogin = id;
        SB[] sbs = config.getQueue().getAll();
        //sbs[0].setRequiredStart(new TimeInterval(new DateTime(System.currentTimeMillis()), 3600));
        config.setCommandedEndTime(DateTime.add(new DateTime(System.currentTimeMillis()),3600 ));
        scheduler.login(userlogin, sbs[0]);
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
