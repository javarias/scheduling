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
 * File FullModeSimulatorImp.java
 */

package alma.scheduling.simulator;

import java.io.File;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JFileChooser;

import alma.xmlentity.XmlEntityStruct;
import alma.acs.component.ComponentImplBase;
import alma.acs.component.client.ComponentClient;
import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;

//import alma.scheduling.FullModeSimulatorOperations;
//import alma.scheduling.InvalidOperation;
import alma.scheduling.*;
import alma.scheduling.master_scheduler.MasterScheduler;

import alma.scheduling.planning_mode_sim.simulator.SimulationInput;
import alma.entity.xmlbinding.schedulinginfo.*;
import alma.entity.xmlbinding.schedulingpolicy.*;

/**
 * @author Sohaila Roberts
 */
public class FullModeSimulatorImpl extends ComponentImplBase 
    implements FullModeSimulatorOperations, Runnable 
{
    /**
     * Is the simulation running?
     */
    private static final String log_tag = "SCHEDULING SIMULATOR:";
    private boolean isRunning;
    private Logger logger;
    private ContainerServices container;
    private SimulationInput data;
    private OpenFile of;
    private NotificationChannelSimulator nc_simulator;
    // make simualtors static?
    private static MasterScheduler masterScheduler;
    private static ControlSimulator control;
    
    public FullModeSimulatorImpl() {
        isRunning = false;
    }
    
    public void initialize(ContainerServices containerServices) {
        if(containerServices == null) {
            throw new IllegalArgumentException (
                    "ContainerServices object cannot be null.");
        }
        this.container = containerServices;
        this.logger = container.getLogger();
        createNotificationChannels();
        createControlSimulator();
        masterScheduler = new MasterScheduler(true);
    }

    public void execute() {
    }

    public void cleanUp() {
    }

    public void aboutToAbort() {
        cleanUp();
    }

    //////////////////////////////////
    //FullModeSimulator Interface ////
    //////////////////////////////////
    public void startSimulation(){
        isRunning = true;
        //start the masterscheduler in simulation mode
        masterScheduler.initialize(container);
        masterScheduler.execute();
    }

    public void stopSimulation() {
        isRunning = false;
        masterScheduler.aboutToAbort();
    }

    public void selectInputFile() {
        of = new OpenFile();
        Thread t = new Thread(of);
        t.start();
    }
    public void loadInputFile() {
        File f = of.returnFile();
        if(f == null) {
            logger.severe("SCHEDULING SIMULATOR: no file loaded!");
            return;
        }
        logger.fine("SCHEDULING SIMULATOR: file has been loaded!");
        try {
            data = new SimulationInput(f.getAbsolutePath(), logger);
        } catch(Exception e) {
            logger.severe("SCHEDULING SIMULATOR: could not create "+
                "SimulationInput object!");
        }
        logger.fine("SCHEDULING SIMULATOR: Simulation Input created.");
    }
    /////////////////////////////////////
    //Executive_to_Scheduling Interface//
    /////////////////////////////////////
    public void startScheduling(XmlEntityStruct sp) 
      throws InvalidOperation {
        masterScheduler.startScheduling(sp);
    }
    public void startInteractiveScheduling() {
        try {
            masterScheduler.startInteractiveScheduling();
        } catch(Exception e) {}
    }
    public void stopScheduling() throws InvalidOperation {
        masterScheduler.stopScheduling();
    }
    public boolean getStatus() {
        return masterScheduler.getStatus();
    }
    public XmlEntityStruct getSchedulingInfo() {
        return masterScheduler.getSchedulingInfo();
    }
    /////////////////////////////////////////////
    //TelescopeOperator_to_Scheduling Interface//
    /////////////////////////////////////////////
    public void response(String messageId, String reply) 
        throws UnidentifiedResponse {
    }
    public XmlEntityStruct getSubarrayInfo() {
        return masterScheduler.getSubarrayInfo();
    }
    public short createSubarray(short[] antennaIdList, String schedulingMode)
		throws InvalidOperation {
		return 0;
	}
    public void destroySubarray(short subarrayId) throws InvalidOperation {
	}
    public void executeProject(String projectId, short subarrayId)
		throws InvalidOperation {
	}
    public void executeSB(String sbId, short subarrayId, String when) {
	}
    public void stopSB(String sbId) throws InvalidOperation, NoSuchSB {
	}
    public void pauseScheduling(short subarrayId) {
	}
    public void resumeScheduling(short subarrayId) {
	}
    public void manualMode(short antennaId) throws InvalidOperation {
	}
    public void activeMode(short antennaId) throws InvalidOperation {
	}


    //////////////////////////////////
    // Runnable interface
    //////////////////////////////////
    public void run() {
        while(isRunning) {
        }
    }

    private void createNotificationChannels() {
        nc_simulator = new NotificationChannelSimulator(container); 
        logger.fine("SCHEDULING SIMULATOR: notification channels created.");
    }

    private void createControlSimulator() {
        control = new ControlSimulator(container);
    }   
    public static ControlSimulator getControlSimulator(){
        return control;
    }
    public static MasterScheduler getMasterScheduler() {
        return masterScheduler;
    }
    

    public static void main(String[] args) {
    }

    class OpenFile implements Runnable {
        private int opened; 
        private JFileChooser fileChooser;
        private JFrame frame;
        
        public OpenFile() {
            frame = new JFrame();
        }

        public void run() {
            frame.setVisible(true);
            fileChooser = new JFileChooser();
            opened = fileChooser.showOpenDialog(frame);
        }

        public File returnFile() {
            if(opened == JFileChooser.APPROVE_OPTION) {
                return fileChooser.getSelectedFile();
            } else {
                return null;
            }
        }
        
    }

}
