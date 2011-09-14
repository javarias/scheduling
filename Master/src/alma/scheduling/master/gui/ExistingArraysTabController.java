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
 * File ExistingArraysTabController.java
 */

package alma.scheduling.master.gui;

import java.net.InetAddress;
import java.net.UnknownHostException;

import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.common.gui.chessboard.ChessboardEntry;
import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.ArrayModeEnum;

public class ExistingArraysTabController extends SchedulingPanelController 
	implements ArrayStatusListener{
    private ExistingArraysTab parent;
    private ArrayStatusCallbackImpl callback;
    
  //  private PluginContainerServices container; 
//    private Consumer consumer;

	public ExistingArraysTabController(PluginContainerServices cs,
									   ExistingArraysTab       p) {
		super(cs);
		// container = cs;
		parent = p;
		callback = new ArrayStatusCallbackImpl(this);
		try {
			cs.activateOffShoot(callback);
			getMSRef();
			masterScheduler.addMonitorMaster(
					InetAddress.getLocalHost().getHostName() + "_"
							+ this.toString() + "_"
							+ System.currentTimeMillis(), callback._this());
		} catch (AcsJContainerServicesEx e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			cs.getLogger().info(e.getMessage());
			masterScheduler.addMonitorMaster(
					this.toString() + "_" + System.currentTimeMillis(),
					callback._this());
		} finally {
		}

	}

    public void secondSetup(PluginContainerServices cs){
        super.onlineSetup(cs);
        if(cs == null) {
            //logger = Logger.getLogger("OFFLINE SP");
            //logger.warning("SchedulingPanel: problem getting CS");
        }
    }
/*
    public void receive(CreatedAutomaticArrayEvent event) {
        String name = event.arrayName;
        logger.fine("SP: Received created array event for "+name);
        parent.addArray(name, "automatic");
    }
    public void receive(DestroyedAutomaticArrayEvent event) {
        String name = event.arrayName;
        logger.fine("SP: Received destroy array event for "+name+" in existing array tab");
        parent.removeArray(name);
    }
    public void receive(CreatedManualArrayEvent event) {
        String name = event.arrayName;
        logger.fine("SP: Received created array event for "+name);
        parent.addArray(name, "manual");
    }
    public void receive(DestroyedManualArrayEvent event) {
        String name = event.arrayName;
        logger.fine("SP: Received destroy array event for "+name+" in existing array tab");
        parent.removeArray(name);
    }
*/
    
    protected String[] getCurrentAutomaticArrays() {
        try {
            getMSRef();
            String[] a = masterScheduler.getActiveAutomaticArrays();
            return a;
        }catch(Exception e){
            return new String[0];
        }
    }
    protected String[] getCurrentManualArrays() {
        try {
            getMSRef();
            String[] a = masterScheduler.getActiveManualArrays();
            return a;
        }catch(Exception e){
            return new String[0];
        }
    }

	@Override
	public void notifyArrayCreation(String name, ArrayModeEnum arrayMode) {
		parent.addArray(name, arrayMode.toString());
	}

	@Override
	public void notifyArrayDestruction(String name) {
		parent.removeArray(name);
		
	}
}
