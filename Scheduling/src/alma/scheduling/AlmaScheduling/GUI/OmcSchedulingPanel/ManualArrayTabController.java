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
import alma.Control.DestroyedManualArrayEvent;
import alma.acs.nc.Consumer;

public class ManualArrayTabController extends SchedulingPanelController {
    private String arrayName="";
    private String arrayStatus="";
    private ManualArrayTab parent;
    private Consumer consumer = null;
    
    public ManualArrayTabController(PluginContainerServices cs, String a, ManualArrayTab p){
        super(cs);
        parent = p;
        arrayName = a;
        arrayStatus = "Active";
        try {
            consumer = new Consumer(alma.Control.CHANNELNAME_CONTROLSYSTEM.value, container);
            consumer.addSubscription(alma.Control.DestroyedManualArrayEvent.class, this);
            consumer.consumerReady();
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    protected void setArrayInUse(String arrayName){
        try {
            getMSRef();
            masterScheduler.setArrayInUse(arrayName);
            releaseMSRef();
        }catch(Exception e){
        }
    }

    protected String getArrayStatus() {
        return arrayStatus;
    }
    private void setArrayStatus(String s){
        arrayStatus = s;
        parent.updateArrayStatus();
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
            if(conumser != null){
                consumer.disconnect();
                consumer = null;
            }
        } catch(Exception e){}
    }
    
    public void receive(DestroyedManualArrayEvent e){
        System.out.println("Manual array destroyed event received for "+e.arrayName);
        if(e.arrayName.equals(arrayName)){
            setArrayStatus("Destroyed");
        }
    }
}
