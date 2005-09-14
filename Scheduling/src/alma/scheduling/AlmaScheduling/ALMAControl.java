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

import alma.Control.ControlMaster;
//import alma.Control.ArrayController;
import alma.Control.AutomaticArrayCommand;

import alma.ControlExceptions.*;
import alma.Control.InvalidRequest;
import alma.Control.InaccessibleException;

/**
 * @author Sohaila Lucero
 * @version $Id: ALMAControl.java,v 1.29 2005/09/14 22:25:55 sslucero Exp $
 */
public class ALMAControl implements Control {
    
    //container services
    private ContainerServices containerServices;
    // control system component
    private ControlMaster control_system;
    //list of current array controllers
    private Vector controllers;
    //logger
    private Logger logger;
    //list of current observing sessions
    private Vector observedSessions;
    private ALMAProjectManager manager;

    public ALMAControl(ContainerServices cs, ALMAProjectManager m) {
        this.containerServices = cs;
        this.manager = m;
        this.logger = cs.getLogger();
        this.controllers = new Vector();
        this.observedSessions = new Vector();
        try {
            org.omg.CORBA.Object obj = containerServices.getComponent("CONTROL/MASTER");
            logger.info("got CONTROL_MASTER stub of type " + obj.getClass().getName());
            control_system = alma.Control.ControlMasterHelper.narrow(obj);
               // containerServices.getComponent("CONTROL_MASTER_COMP"));
            //control_system = (ControlMaster)alma.Control.ControlMasterHelper.narrow(
              //  containerServices.getComponent("CONTROL_MASTER_COMP"));
            logger.info("SCHEDULING: Got ControlMasterComponent");
            
        } catch (alma.acs.container.ContainerException ce) {
            logger.severe("SCHEDULING: error getting ControlMaster Component.");
            logger.severe("SCHEDULING: "+ce.toString());
        }
    }
    /**
     *
     * @throws SchedulingException
     */
    public void execSB(String arrayName, BestSB best, DateTime time) 
        throws SchedulingException {

        execSB(arrayName, best);
    }

    /**
     * Executes the sb that was selected by the telescope operator
     * @param subarrayId
     * @param bestSBId
     * @throws SchedulingException
     */
    public void execSB(String arrayName, BestSB best) 
        throws SchedulingException {
        
        execSB(arrayName, best.getBestSelection());
    }
    
    /**
     * Executes the sb that was selected by the telescope operator
     * @param subarrayId
     * @param sbId
     * @throws SchedulingException
     */
    public void execSB(String arrayName, String sbId) 
        throws SchedulingException {

        //send out start of session
        String sessionId = manager.sendStartSessionEvent(sbId);
        logger.info("SCHEDULING: Sending BestSBs to Control!");
        logger.info("SCHEDULING: Array being used has name = "+arrayName);
        
        AutomaticArrayCommand ctrl = getAutomaticArray(arrayName);
        try{
            ctrl.observeNow(sbId, sessionId); 
        } catch(InvalidRequest e1) {
            logger.severe("SCHEDULING: could not observe!");
            e1.printStackTrace();
        } catch(InaccessibleException e2) {
            logger.severe("SCHEDULING: could not observe!");
            e2.printStackTrace();
        }
    }


    /**
     *
     * @throws SchedulingException
     */
    public void stopSB(String name, String id) throws SchedulingException {
    }

    /**
     * Tells the control system to create a subarray with the given antennas.
     * @param antenna an array of the antennas which will make up the subarray
     * @return short the subarray id
     * @throws SchedulingException If antenna is null or contains nothing an 
     *                             exception is thrown.
     */
    public String createArray(String[] antenna) throws SchedulingException {
        if(antenna == null || antenna.length == 0) {
            throw new SchedulingException
                ("SCHEDULING: Cannot create a subarray with out any antennas!");
        }
        try {
            if(control_system == null) {
                logger.severe("SCHEDULING: control system == null..");
                throw new SchedulingException("SCHEDULING: Error with ControlMaster Component.");
            }
            if(controllers == null) { 
                logger.severe("SCHEDULING: controllers == null..");
                throw new SchedulingException("SCHEDULING: Something went very wrong when setting up ALMAControl");
            }
            String arrayName = control_system.createAutomaticArray(antenna);
            AutomaticArrayCommand ctrl = alma.Control.AutomaticArrayCommandHelper.narrow(
                    containerServices.getComponent(arrayName));
            if(ctrl == null) {
                logger.severe("SCHEDULING: ctrl is null");
                throw new SchedulingException("SCHEDULING: Error with getting subarray & ArrayController!");
            }
            controllers.add(ctrl);
            logger.info("SCHEDULING: array controller id = "+ ctrl.getName());
            return ctrl.getName();
        } catch(InvalidRequest e1) {
            throw new SchedulingException
                ("SCHEDULING: Control error: "+ e1.toString());
        } catch(InaccessibleException e2) {
            throw new SchedulingException
                ("SCHEDULING: Control error: "+ e2.toString());
        } catch (alma.acs.container.ContainerException e3) {
            throw new SchedulingException
                ("SCHEDULING: Error getting AutomaticArrayCommand component." +e3.toString());
        }
    }

    /**
     *
     * @throws SchedulingException
     */
    public void destroyArray(String name) throws SchedulingException {
        try {
            control_system.destroyArray(name);
        } catch(InvalidRequest e1) {
        } catch(InaccessibleException e2){
        }
    }

    /**
     * @return String[]
     * @throws SchedulingException
     */
    public String[] getActiveArray() throws SchedulingException {
        return null;
    }
    
    /** 
     * @return String[]
     * @throws SchedulingException
     */
    public String[] getIdleAntennas() throws SchedulingException {
        try{
            String[] antennas = control_system.getAvailableAntennas();
            logger.info("SCHEDULING: Got "+ antennas.length +" antennas");
            return antennas;
        } catch(InaccessibleException e1) {
            throw new SchedulingException
                ("SCHEDULING: Couldn't get available antennas. "+e1.toString()); 
        }
    }

    /**
     * @return String[]
     */
    public String[] getArrayAntennas(String name) throws SchedulingException {
            return getAutomaticArray(name).getAntennas();
    }
    
    /**
      * @return ArrayController
      * @throws SchedulingException
      */
    private AutomaticArrayCommand getAutomaticArray(String name) throws SchedulingException {
        logger.info("SCHEDULING: looking for subarray with id = "+ name);
        for(int i=0; i < controllers.size(); i++){
            if( ((AutomaticArrayCommand)controllers.elementAt(i)).getName().equals(name)) {
                logger.info("SCHEDULING: found subarray with id = "+ name);
                return (AutomaticArrayCommand)controllers.elementAt(i);
            }
        }
        return null;
    }

    /**
      * release control comp
      */
    public void releaseControlComp() {
        try {
            containerServices.releaseComponent("CONTROL_MASTER");
        }catch(Exception e) {
            logger.severe("SCHEDULING: Error releasing control comp.");
            e.printStackTrace();
        }
    }
}
