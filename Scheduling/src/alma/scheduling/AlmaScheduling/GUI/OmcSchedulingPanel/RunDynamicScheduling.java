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
 * file RunDynamicScheduling.java
 */
package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.util.logging.Logger;
import alma.acs.container.ContainerServices;
import alma.exec.extension.subsystemplugin.PluginContainerServices;

import alma.scheduling.AlmaScheduling.ALMASchedLogger;

import alma.scheduling.MasterSchedulerIF;
import alma.xmlentity.XmlEntityStruct;

public class RunDynamicScheduling implements Runnable {
    //private ContainerServices container;
    private PluginContainerServices container;
    private MasterSchedulerIF masterScheduler = null;
    private String[] sb_ids;
    private String arrayname;
    private ALMASchedLogger logger;
    
    public RunDynamicScheduling(PluginContainerServices cs ){
        container = cs;
        logger = new ALMASchedLogger(cs.getLogger());
        getMSRef();
    }
    private void getMSRef(){
        try {
            if(masterScheduler == null) {
                masterScheduler = alma.scheduling.MasterSchedulerIFHelper.narrow(
                    container.getComponentNonSticky("SCHEDULING_MASTERSCHEDULER"));
                        //"IDL:alma/scheduling/MasterSchedulerIF:1.0"));
            }
        } catch(Exception e){
            e.printStackTrace();
            logger.severe("RUN_DYNAMIC_SCHEDULING: Error in RunDynamicScheduling: "+e.toString());
        }
    }
    private void releaseMSRef(){
        try {
            if(masterScheduler != null){
                container.releaseComponent(masterScheduler.name());
            }
        } catch(Exception e){
            e.printStackTrace();
            logger.severe("RUN_DYNAMIC_SCHEDULING: Error in RunDynamicScheduling: "+e.toString());
        }
    }

    public void run() {
        if(masterScheduler == null) {
            logger.warning("RUN_DYNAMIC_SCHEDULING: NO Connection to MasterScheduler. Cannot schedule");
            return;
        }
        XmlEntityStruct policy = new XmlEntityStruct();
        try {
            masterScheduler.startScheduling(policy);
        } catch(Exception e) {
            releaseMSRef();
            //e.printStackTrace();
            logger.severe("RUN_DYNAMIC_SCHEDULING: Error in RunDynamicScheduling: "+e.toString());
        }
    }
    
    public void stop() {
        try {
            releaseMSRef();
        } catch (Exception e){
            e.printStackTrace();
            logger.severe("RUN_DYNAMIC_SCHEDULING: Error in RunDynamicScheduling: "+e.toString());
        }
    }
}
