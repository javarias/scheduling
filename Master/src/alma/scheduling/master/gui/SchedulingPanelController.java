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
package alma.scheduling.master.gui;

import java.util.logging.Logger;

import alma.Control.ControlMaster;
import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.nc.Consumer;
import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.Master;
import alma.scheduling.SchedulingState;
import alma.scheduling.SchedulingStateEvent;
import alma.scheduling.master.compimpl.Constants;

public class SchedulingPanelController {
    protected Master masterScheduler;
    protected PluginContainerServices container;
    protected Logger logger;
    protected Consumer consumer;
    protected boolean connected;
    protected boolean controlConnected;

    public SchedulingPanelController(){
        masterScheduler=null;
        container=null;
        logger=null;
        consumer =null;
        connected =false;
    }

    public SchedulingPanelController(PluginContainerServices cs) {
        this();
        container = cs;
        logger = cs.getLogger();
    }

    public void onlineSetup(PluginContainerServices cs) {
        container = cs;
        logger = cs.getLogger();
        logger.fine("SP: online setup of SchedulingPanelController");
        try {
            consumer = new Consumer(alma.scheduling.CHANNELNAME_SCHEDULING.value, cs);
            consumer.addSubscription(SchedulingStateEvent.class, this);
            consumer.consumerReady();
        }catch(Exception e){
            logger.severe("SCHEDULING_PANEL: problem getting NC to get state event");
            logger.severe("SCHEDULING_PANEL: setting state to connected anyways: COULD CAUSE PROBLEMS");
            //todo send alarm
            connected = true;
            e.printStackTrace();
        }
    }
    protected void getMSRef() {
        try {
            if(masterScheduler == null) {
                masterScheduler = alma.scheduling.MasterHelper.narrow(
                    container.getComponentNonSticky(Constants.SCHEDULING_MASTER_URL));
                logger.fine("SCHEDULING_PANEL: Got MS");
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
                logger.fine("SCHEDULING_PANEL: Released MS.");
            }
        } catch(Exception e){
            e.printStackTrace();
            logger.severe("SCHEDULING_PANCEL: Error releasing MS: "+e.toString());
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
            logger.warning("SCHEDULING_PANEL: Error destorying array "+arrayname+", see if it still exists.");
        }
    }
    public void receive(SchedulingStateEvent e){
        logger.fine("\n******************************");
        logger.fine("GOT SchedulingStateEvent: "+e.state);
        logger.fine("******************************\n");
        if(e.state == SchedulingState.ONLINE_PASS2){
            //setOperationalStartState();
            connected = true;
        }else if(e.state == SchedulingState.OFFLINE){
            connected = false;
            //parent.connectedToALMA(connected);
        } else {
            return;
        }
    }


	protected void checkOperationalState() {
		try {
			Master sched_mc = alma.scheduling.MasterHelper.narrow(getCS()
					.getComponentNonSticky(Constants.SCHEDULING_MASTER_URL));
			connected = true;
		} catch (Exception e) {
			logger
					.warning("SP: Problem checking master component state, check that SCHEDULING system is connected");
			e.printStackTrace();
		}
	}

    protected void checkControlConnection() {
        try {
			ControlMaster control_mc =
				alma.Control.ControlMasterHelper.narrow(getCS().getComponentNonSticky("CONTROL/MASTER"));
	        if (control_mc.getMasterState() == 
                alma.Control.SystemState.OPERATIONAL) {
	        	logger.info("CONTROL is OPERATIONAL");
            	controlConnected = true;
	        } else {
	        	logger.info("CONTROL is not OPERATIONAL");
	        	controlConnected = false;
	        }
		} catch (AcsJContainerServicesEx e) {
			controlConnected = false;
		}

    }
    
    protected boolean areWeConnected() {
        checkOperationalState();
        checkControlConnection();
        return connected && controlConnected;
    }
    

}
