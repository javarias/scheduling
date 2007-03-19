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
 * File ManualArrayTabController.java
 */
package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.util.logging.Logger;
import alma.scheduling.MasterSchedulerIF;
import alma.exec.extension.subsystemplugin.*;
import alma.Control.console.gui.CCLConsolePlugin;

public class ManualArrayTabController extends SchedulingPanelController {
    private String arrayName="";
    private ManualArrayTab parent;
    
    public ManualArrayTabController(PluginContainerServices cs, String a, ManualArrayTab p){
        super(cs);
        parent = p;
        arrayName = a;
    }

    protected void setArrayInUse(String arrayName){
        try {
            getMSRef();
            masterScheduler.setArrayInUse(arrayName);
            releaseMSRef();
        }catch(Exception e){
        }
    }

    protected boolean createConsolePlugin() {
        if(arrayName.equals("")){
            return false;
        }
        try {
            CCLConsolePlugin ctrl = new CCLConsolePlugin(arrayName);
            getCS().startChildPlugin("CCL Console", (SubsystemPlugin)ctrl);
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
    

    protected void destroyArray(){
        try{
            getMSRef();
            masterScheduler.destroyArray(arrayName);
            releaseMSRef();
        } catch(Exception e){}
    }

}
