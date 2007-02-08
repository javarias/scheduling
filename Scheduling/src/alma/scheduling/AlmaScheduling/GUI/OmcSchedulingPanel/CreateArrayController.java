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
 * File CreateArrayController.java
 *
 */
package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.util.logging.Logger;
import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.SBLite;
import alma.scheduling.ProjectLite;
import alma.scheduling.MasterSchedulerIF;
import alma.Control.ControlMaster;
import alma.Control.AntennaStateEvent;
import alma.scheduling.Define.*;
import alma.scheduling.ArrayModeEnum;


public class CreateArrayController extends SchedulingPanelController {

    private ControlMaster control;

    public CreateArrayController(PluginContainerServices cs){
        super(cs);
    }

    private void getControlRef() {
        try {
            control = alma.Control.ControlMasterHelper.narrow(
                    container.getComponent("CONTROL/MASTER"));
            logger.info("SCHEDULING_PANEL: Got control system in array creator");
        } catch(Exception e){
            e.printStackTrace();
            logger.severe("SCHEDULING_PANEL: Error getting components from array creator");
        }
    }
    private void releaseControlRef() {
        try {
            container.releaseComponent(control.name());
        } catch(Exception e) {
            e.printStackTrace();
            logger.severe("SCHEDULING_PANEL: Error releasing components from array creator");
        }
    }

    public String[] getAntennas() {
        String[] antennas=null;
        try{
            getControlRef();
            antennas= control.getAvailableAntennas();
            releaseControlRef();
        } catch(Exception e){
            logger.severe("SCHEDULING_PANEL: Control not accessible yet. Try again when its opeational");
            e.printStackTrace();
        }
        return antennas;
    }

    public String createArray(String arrayMode, String[] antennas) throws SchedulingException {
        String arrayName = null;
        getMSRef();
        try {
            if(arrayMode.toLowerCase().equals("dynamic")){
                arrayName = masterScheduler.createArray(
                        antennas,ArrayModeEnum.DYNAMIC);
            } else if(arrayMode.toLowerCase().equals("interactive")){
                arrayName = masterScheduler.createArray(
                        antennas,ArrayModeEnum.INTERACTIVE);
            } else if(arrayMode.toLowerCase().equals("queued")) {
                arrayName = masterScheduler.createArray(
                        antennas,ArrayModeEnum.QUEUED);
            } else if(arrayMode.toLowerCase().equals("manual")){
                arrayName = masterScheduler.createArray(
                        antennas,ArrayModeEnum.MANUAL);
            }
        } catch(Exception e) {
            releaseMSRef();
            e.printStackTrace();
            throw new SchedulingException (e);
        }
        releaseMSRef();
        return arrayName;
    }

}
