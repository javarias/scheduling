/*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
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
 * File ALMADispatcher.java
 * 
 */
package ALMA.scheduling.master_scheduler;

import java.util.Vector;
import java.util.logging.Logger;
import java.util.logging.Level;

import alma.acs.container.ContainerServices;
import ALMA.scheduling.define.STime;
import ALMA.scheduling.project_manager.ProjectManagerTaskControl;

import alma.entity.xmlbinding.schedblock.*;

import ALMA.Control.ArrayController;
import ALMA.Control.TooLateException;
import ALMA.Control.ArrayNotIdleException;
/**
 * The ALMADispatcher class presents a list of scheduling blocks for execution
 * to the telescope operator, in priority order, and waits for a selection.
 * If no selection is made, then the first one is selected.  It then 
 * initiates the execution of the selected scheduling block.
 * 
 * @version 1.00 May 2, 2003
 * @author Allen Farris
 */
public class ALMADispatcher implements ControlProxy {
	private boolean isSimulation;
	private ContainerServices containerServices;
    private ALMAArchive archive;
    private Vector idleAntennas;
    private Vector subArrays;
    private Vector antennaMonitor;
    private ArrayController arrayControllerComp;
    private Logger logger;
    //private ControlReceiverEvent c_event;
    private ProjectManagerTaskControl pmtc;

	//private Vector schedBlocks;
	//private ArrayController ac;
    
	public ALMADispatcher (boolean isSimulation, ContainerServices container,
                            ALMAArchive a) {
		this.isSimulation = isSimulation;
		this.containerServices = container;
        this.archive = a;
        //this.projectManager = pm;
        idleAntennas = new Vector();
        subArrays = new Vector();
        antennaMonitor = new Vector();
        this.logger = containerServices.getLogger();
		logger.log(Level.INFO,"SCHEDULING: The Dispatcher has been constructed.");
	}

	/**
	 *  Tells the control system to execute the SB with 'id' and its start
	 *  time is 'time'.
	 *  @param id
	 *  @param time
	 */
	public void sendToControl(String id, STime time) {
		logger.log(Level.INFO,"SCHEDULING: Sending SB with id = "+id+" to controller.");
        try {
            //connect to control's component
            arrayControllerComp = ALMA.Control.ArrayControllerHelper.narrow(
                containerServices.getComponent("ArrayController1"));
            logger.log(Level.INFO, "SCHEDULING: Got array controller");
            //tell control to process the schedblock
            //arrayControllerComp.processSchedBlock(id, time.getTime());
            try {
                arrayControllerComp.observeNow(id);
            } catch(ArrayNotIdleException e){
                logger.severe("SCHEDULING: "+e.toString());
            } catch(TooLateException e) {
                logger.severe("SCHEDULING: "+e.toString());
            }
        } catch (Exception e) {
            logger.severe("SCHEDULING: error getting array controller");
            logger.severe("SCHEDULING: "+e.toString());
        }
	}

    /**
     *  Creates a subarray of antennas and returns the id of that subarray
     *  @return String
     */
    public String createSubArray() {
        String subArrayId = "";
        /*
        Vector idleAntennas = controlStatusComp.idleAntennas();
        for(int i = 0; i < idleAntennas.size(); i++) {
            antennaMonitor.add( arrayMonitorComp.antennaMonitor(
                idleAntennas.elementAt(i) ) ); //probably will need to be casted
        }

        Vector selectedAntennas = selectAntennas(antennaMonitor);
        arrayControllerComp = controlSystemComp.createSubArray(selectedAntennas);
        //get schedblocks to send to subarray
        //arrayControllerComp.observe(sb,starttime);
        */
        return subArrayId;
    }

    /**
     *  Will select the 'best' antennas to create a subarray.
     *  @param am A list of the antenna monitors for the currently idle antennas
     *  @return Vector A list of the best antennas ids to make the subarray
     */
    public Vector selectAntennas(Vector am) {
        Vector results = new Vector();
        //will have conditions to select antennas
        /*
        for(int i = 0 ; i < am.size(); i++) {
            if( ((AntennaMonitor)am.elementAt(i)).status() == ControlStatus.IDLE ) {
                results.add( ((AntennaMonitor)am.elementAt(i)).id() );
            }
        }
        */
        return results;
    }

    ///////////////////////////////////////
    /* Get Methods */
    /* Set Methods */
    public void setProjectManagerTaskControl(ProjectManagerTaskControl p) {
        this.pmtc = p;
    }
    ///////////////////////////////////////


	public static void main(String[] args) {
	}
}
