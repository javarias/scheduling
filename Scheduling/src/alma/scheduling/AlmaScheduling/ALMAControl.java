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
 * File ALMAControl.java
 * 
 */
package alma.scheduling.AlmaScheduling;

import java.util.logging.Logger;
import java.util.Vector;

import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;

import alma.scheduling.Define.Control;
import alma.scheduling.Define.BestSB;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.SchedulingException;

import alma.Control.ControlSystem;
import alma.Control.ArrayController;
import alma.Control.CSInactiveException;
import alma.Control.InvalidAntennaIDException;
import alma.Control.InvalidSubArrayIDException;
import alma.Control.TooLateException;
import alma.Control.ArrayNotIdleException;


/**
 * @author Sohaila Lucero
 */
public class ALMAControl implements Control {
    
    private ContainerServices containerServices;
    private ControlSystem control_system;
    private Vector controllers;
    private Logger logger;

    public ALMAControl(ContainerServices cs) {
        this.containerServices = cs;
        this.logger = cs.getLogger();
        this.controllers = new Vector();
        try {
        //alma/Control/ControlSystemHelper
            control_system = alma.Control.ControlSystemHelper.narrow(
                containerServices.getDefaultComponent(
                    "IDL:alma/Control/ControlSystem:1.0"));
            logger.info("SCHEDULING: Got ControlSystem Component");
        } catch (ContainerException ce) {
            logger.severe("SCHEDULING: error getting ControlSystem Component.");
            logger.severe("SCHEDULING: "+ce.toString());
        }
    }
    /**
     *
     */
    public void execSB(short subarrayId, BestSB best, DateTime time) 
        throws SchedulingException {
    }

    /**
     * Executes the sb that was selected by the telescope operator
     * @param subarrayId
     * @param bestSBId
     */
    public void execSB(short subarrayId, BestSB best) 
        throws SchedulingException {
        
        execSB(subarrayId, best.getBestSelection());
    }
    
    /**
     * Executes the sb that was selected by the telescope operator
     * @param subarrayId
     * @param sbId
     */
    public void execSB(short subarrayId, String sbId) 
        throws SchedulingException {
    
        logger.info("SCHEDULING: Sending BestSBs to Control!");
        logger.info("SCHEDULING: Subarray being used has id = "+subarrayId);
        
        ArrayController ctrl = getArrayController(subarrayId);
        try{
            ctrl.observeNow(sbId);
        } catch(ArrayNotIdleException e1) {
            logger.severe("SCHEDULING: could not observe!");
            e1.printStackTrace();
        }
    
    }


    /**
     *
     */
    public void stopSB(short subarrayId, String id) throws SchedulingException {
    }

    /**
     * Tells the control system to create a subarray with the given antennas.
     * @param antenna an array of the antennas which will make up the subarray
     * @return short the subarray id
     * @throws SchedulingException If antenna is null or contains nothing an 
     *                             exception is thrown.
     */
    public short createSubarray(short[] antenna) throws SchedulingException {
        if(antenna == null || antenna.length == 0) {
            throw new SchedulingException
                ("SCHEDULING: Cannot create a subarray with out any antennas!");
        }
        try {
            ArrayController ctrl = control_system.createSubArray(antenna);
            if(control_system == null) {
                System.out.println("control system == null..");
            }
            if(controllers == null) { 
                System.out.println("controllers == null..");
            }
            if(ctrl == null) {
                System.out.println("ctrl is null");
            }
            controllers.add(ctrl);
            System.out.println("SCHEDULING: array controller id = "+ ctrl.id());
            return ctrl.id();
        } catch(CSInactiveException e1) {
            throw new SchedulingException
                ("SCHEDULING: Control error: "+ e1.toString());
        } catch(InvalidAntennaIDException e2) {
            throw new SchedulingException
                ("SCHEDULING: Control error: "+ e2.toString());
        }
    }

    /**
     *
     */
    public void destroySubarray(short subarrayId) throws SchedulingException {
        try {
            //ArrayController ctrl = getArrayController(subarrayId);
            control_system.destroySubArray(subarrayId);
        } catch(CSInactiveException e1) {
        } catch(InvalidSubArrayIDException e2){
        }
    }

    /**
     *
     */
    public short[] getActiveSubarray() throws SchedulingException {
        return null;
    }
    
    /** 
     *
     */
    public short[] getIdleAntennas() throws SchedulingException {
        /*
        try {
            short[] antennas = control_system.availableAntennas();
            logger.info("SCHEDULING: Got "+ antennas.length +" antennas");
            return antennas;
        } catch(CSInactiveException e) {
            logger.severe("SCHEDULING: Control System == INACTIVE!");
            e.printStackTrace();
            return null;
        }
        */
        
        short[] tmp_antennas = new short[64];
        for (int i=0; i < 64; i++) {
            tmp_antennas[i] = (short)i;
        }
        return tmp_antennas;
        
    }

    /**
     *
     */
    public short[] getSubarrayAntennas(short subarrayId) {
        return null;
    }
    
    private ArrayController getArrayController(short subarrayId) throws SchedulingException {
        logger.info("SCHEDULING: looking for subarray with id = "+ subarrayId);
        for(int i=0; i < controllers.size(); i++){
            if( ((ArrayController)controllers.elementAt(i)).id() == subarrayId) {
                logger.info("SCHEDULING: found subarray with id = "+ subarrayId);
                return (ArrayController)controllers.elementAt(i);
            }
        }
        return null;
    }
}
