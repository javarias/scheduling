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
package alma.scheduling.scheduler;
/*
import alma.scheduling.Interactive_PI_to_Scheduling;
import alma.scheduling.SBExists;
import alma.scheduling.NoSuchSB;
import alma.scheduling.InvalidObject;
import alma.scheduling.InvalidOperation;
*/
import java.net.URL;
import alma.entity.xmlbinding.schedblock.*;
//import alma.obsprep.bo.*;
//import alma.obsprep.bo.Target;

/**
 * A controller for the Interactive Scheduling GUI. 
 * All the functionality that is required from the the GUI
 * is implemented here.
 *
 *  @version 1.00 Dec 18, 2003
 *  @author sroberts 
 */
public class GUIController implements Runnable {
    private Scheduler scheduler;
    private String userlogin="";
    private GUI gui;

    public GUIController(Scheduler s) {
        this.scheduler = s;
    }

    protected URL getImage(String name) {
        return this.getClass().getClassLoader().getResource(
            "alma/scheduling/image/"+name);
    }

    ////////////////////////////////////////////////////

    public String getLogin() {
        return userlogin;
    }

    /**
     * Returns a list of the schedblock ids.
     * 
     */
    public SchedBlock[] getSBs() {
        return scheduler.getSBs();
    }
/*
    public String[] getSBContents(String uid) {
        String[] contents = new String[10];
        SchedBlock sb = scheduler.getSB(uid);
        contents[0] = "SchedBlock - " + uid + "\n";
        contents[1] = "Weather Constraints = " +
                sb.getPreconditions().getWeatherConstraints().toString() +
                "\n";
        contents[2] = "Performance Goal = " + 
                sb.getObsUnitControl().getPerformanceGoal()+ "\n";
        contents[3] = "The max time this SchedBlock has is "+
                sb.getSchedBlockControl().getSBMaximumTime().toString() + "\n";
        contents[4] = "SchedBlock has been executed " +
                sb.getSchedBlockControl().getRepeatCount() + " times \n";
        
        return contents;
    }
    */

    /**
     *  Deletes the SB from the schedulers subqueue.
     */
     public void deleteSB(String sb_id) {
        scheduler.removeSBfromQueue(sb_id, "aborted");
     }

     public void executeSB(String sb_id) {
        scheduler.dispatchSB(sb_id);
     }

    ////////////////////////////////////////////////////
    
    public void setLogin(String id) {
        userlogin = id;
    }
    
    ////////////////////////////////////////////////////

    public void run() {
        this.gui = new GUI(this);
    }

    public static void main(String[] args) {
    }
}
