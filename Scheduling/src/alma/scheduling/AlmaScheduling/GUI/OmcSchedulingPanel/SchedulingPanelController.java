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
 * File SchedulingPanelController.java
 */
package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.SBLite;
import alma.scheduling.ProjectLite;
import alma.scheduling.MasterSchedulerIF;
import java.util.logging.Logger;

public class SchedulingPanelController {
    protected MasterSchedulerIF masterScheduler;
    protected PluginContainerServices container;
    protected Logger logger;

    public SchedulingPanelController(){
        masterScheduler=null;
        container=null;
        logger=null;
    }

    public SchedulingPanelController(PluginContainerServices cs) {
        this();
        container = cs;
        logger = cs.getLogger();
    }
    public void onlineSetup(PluginContainerServices cs) {
        container = cs;
        logger = cs.getLogger();
    }
    protected void getMSRef() {
        try {
            if(masterScheduler == null) {
                masterScheduler = alma.scheduling.MasterSchedulerIFHelper.narrow(
                    container.getDefaultComponent(
                        "IDL:alma/scheduling/MasterSchedulerIF:1.0"));
                logger.info("SCHEDULING_PANEL: Got MS");
            }
        } catch(Exception e){
            e.printStackTrace();
            logger.severe("SCHEDULING_PANEL: Error getting MS: "+e.toString()); 
        }
    }
    
    protected void releaseMSRef(){
        try {
            if(masterScheduler != null){
                container.releaseComponent(masterScheduler.name());
                logger.info("SCHEDULING_PANEL: Released MS.");
            }
        } catch(Exception e){
            e.printStackTrace();
            logger.severe("SCHEDULING_PANEL: Error releasing MS: "+e.toString());
        }
    }

    public PluginContainerServices getCS() {
        return container;
    }
    
    protected void destroyArray(String arrayname){
        try {
            getMSRef();
            masterScheduler.destroyArray(arrayname);
            releaseMSRef();
        }catch(Exception e){
            logger.severe("SCHEDULING_PANEL: Error destorying array "+arrayname);
            e.printStackTrace();
        }
    }

}

