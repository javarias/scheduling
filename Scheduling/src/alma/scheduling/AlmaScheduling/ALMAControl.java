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
import java.util.Properties;

import alma.acs.container.ContainerServices;
import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;

import alma.asdmIDLTypes.IDLEntityRef;

import alma.scheduling.ArrayInfo;
import alma.scheduling.ArrayModeEnum;
import alma.scheduling.ArrayStateEnum;

import alma.scheduling.Define.Control;
import alma.scheduling.Define.BestSB;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.SchedulingException;

import alma.Control.ResourceId;
import alma.Control.ControlMaster;
import alma.Control.ArrayMonitor;
import alma.Control.AutomaticArrayCommand;
import alma.Control.ManualArrayMonitor;

import alma.ControlExceptions.*;
import alma.Control.InvalidRequest;
import alma.Control.InaccessibleException;
import alma.Control.AntennaMode;

import alma.alarmsystem.source.ACSAlarmSystemInterfaceFactory;
import alma.alarmsystem.source.ACSAlarmSystemInterface;
import alma.alarmsystem.source.ACSFaultState;
import java.sql.Timestamp;

/**
 * @author Sohaila Lucero
 * @version $Id: ALMAControl.java,v 1.56 2006/11/27 16:39:13 wlin Exp $
 */
public class ALMAControl implements Control {
    
    //container services
    private ContainerServices containerServices;
    // control system component
    private ControlMaster control_system;
    //list of current automatic array auto_controllers
    private Vector<ArrayModeInfo> auto_controllers;
    //list of current manual array monitors.
    private Vector manualArrays;
    //logger
    private Logger logger;
    //list of current observing sessions
    private Vector observedSessions;
    private ALMAProjectManager manager;
    private ALMAClock clock;

    public ALMAControl(ContainerServices cs, ALMAProjectManager m) {
        this.containerServices = cs;
        this.manager = m;
        this.logger = cs.getLogger();
            this.auto_controllers = new Vector<ArrayModeInfo>();
        manualArrays = new Vector();
        this.observedSessions = new Vector();
        this.clock = new ALMAClock();
        
        try {
            org.omg.CORBA.Object obj = containerServices.getComponent("CONTROL/MASTER");
            control_system = alma.Control.ControlMasterHelper.narrow(obj);
               // containerServices.getComponent("CONTROL_MASTER_COMP"));
            //control_system = (ControlMaster)alma.Control.ControlMasterHelper.narrow(
              //  containerServices.getComponent("CONTROL_MASTER_COMP"));
            logger.info("SCHEDULING: Got ControlMasterComponent");
            
        } catch (AcsJContainerServicesEx ce) {
            control_system=null;
            logger.severe("SCHEDULING: error getting ControlMaster Component.");
            logger.severe("SCHEDULING: "+ce.toString());
            sendAlarm("Scheduling","SchedControlConnAlarm",3,ACSFaultState.ACTIVE);
        }
    }
    /**
     *
     * @throws SchedulingException
     */
    
    public void sendAlarm(String ff, String fm, int fc, String fs) {
        try {
            ACSAlarmSystemInterface alarmSource = ACSAlarmSystemInterfaceFactory.createSource("ALMAControl");
            ACSFaultState state = ACSAlarmSystemInterfaceFactory.createFaultState(ff, fm, fc);
            state.setDescriptor(fs);
            state.setUserTimestamp(new Timestamp(clock.getDateTime().getMillisec()));
            Properties prop = new Properties();
            prop.setProperty(ACSFaultState.ASI_PREFIX_PROPERTY, "prefix");
			prop.setProperty(ACSFaultState.ASI_SUFFIX_PROPERTY, "suffix");
			prop.setProperty("ALMAMasterScheduling_PROPERTY", fm);
			state.setUserProperties(prop);
            alarmSource.push(state);
        } catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    
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
        IDLEntityRef sessionRef = manager.sendStartSessionEvent(sbId);
        logger.info("SCHEDULING: Sending BestSBs to Control!");
        logger.info("SCHEDULING: Array being used has name = "+arrayName);
        
        AutomaticArrayCommand ctrl = getAutomaticArray(arrayName);
        try{
            IDLEntityRef sbRef = new IDLEntityRef();
            sbRef.entityId = sbId;
            sbRef.partId = "";
            sbRef.entityTypeName = "SchedBlock";
            sbRef.instanceVersion = "1.0";
            //logger.info("SCHEDULING: session id "+sessionRef.entityId+":"+sessionRef.partId);
            ctrl.observe(sbRef, sessionRef, 0L); 
        } catch(InvalidRequest e1) {
            logger.severe("SCHEDULING: could not observe!");
            logger.severe("SCHEDULING: Problem was: "+e1.toString());
            sendAlarm("Scheduling","SchedControlConnAlarm",1,ACSFaultState.ACTIVE);
        } catch(InaccessibleException e2) {
            logger.severe("SCHEDULING: could not observe!");
            logger.severe("SCHEDULING: Problem was: "+e2.toString());
            sendAlarm("Scheduling","SchedControlConnAlarm",2,ACSFaultState.ACTIVE);
            throw new SchedulingException(e2);
        }
    }


    public void stopAllSchedulingOnAllAutomaticArrays() throws SchedulingException{
        try {
            String[] all = getAllAutomaticArrays();
            for(int i=0; i < all.length; i++){

                //getAutomaticArray(all[i]).stop();
            }
        } catch(Exception e) {
            logger.severe("SCHEDULING: Error stopping all scheduling.");
            e.printStackTrace(System.out);
            throw new SchedulingException(e);
        }
    }
    /**
     *
     * @throws SchedulingException
     */
    public void stopSB(String name, String id) throws SchedulingException {
        AutomaticArrayCommand ctrl = getAutomaticArray(name);
        try{
            logger.info("SCHEDULING: Stopping scheduling on array "+name);
            ctrl.stop(); 
            removeAutomaticArray(false, name);
        } catch(InvalidRequest e1) {
            logger.severe("SCHEDULING: could not stop SB "+id+"!");
            logger.severe("SCHEDULING: Problem was: "+e1.toString());
            e1.printStackTrace(System.out);
            sendAlarm("Scheduling","SchedControlConnAlarm",1,ACSFaultState.ACTIVE);
            throw new SchedulingException(e1);
        } catch(InaccessibleException e2) {
            logger.severe("SCHEDULING: could not stop SB "+id+"!");
            logger.severe("SCHEDULING: Problem was: "+e2.toString());
            e2.printStackTrace(System.out);
            sendAlarm("Scheduling","SchedControlConnAlarm",1,ACSFaultState.ACTIVE);
            throw new SchedulingException(e2);
        }
    }

    public void stopAllScheduling() throws SchedulingException {
        try {
            for(int i=0; i < auto_controllers.size(); i++){
                ((AutomaticArrayCommand)auto_controllers.elementAt(i).getArrayComp()).stop();
            }
            removeAutomaticArray(true,"");
        } catch(Exception e) {
            e.printStackTrace(System.out);
            throw new SchedulingException (e);
        }
    }

    /**
     * Tells the control system to create a subarray with the given antennas.
     * @param antenna an array of the antennas which will make up the subarray
     * @return short the subarray id
     * @throws SchedulingException If antenna is null or contains nothing an 
     *                             exception is thrown.
     */
    public String createArray(String[] antenna, String mode)
        throws SchedulingException {

        if(antenna == null || antenna.length == 0) {
            throw new SchedulingException
                ("SCHEDULING: Cannot create an array with out any antennas!");
        }
        try {
            if(control_system == null) {
                logger.severe("SCHEDULING: control system == null..");
                throw new SchedulingException("SCHEDULING: Error with ControlMaster Component.");
            }
            if(auto_controllers == null) { 
                logger.severe("SCHEDULING: auto_controllers == null..");
                throw new SchedulingException("SCHEDULING: Something went very wrong when setting up ALMAControl");
            }
            for(int i=0;i < antenna.length; i++){
                logger.info("SCHEDULING: antenna name = "+antenna[i]);
            }
            String arrayName="";
            try {
            arrayName = control_system.createAutomaticArray(antenna);
            }catch(Exception e) {
                e.printStackTrace();
            }
            AutomaticArrayCommand ctrl;
            try {
                ctrl = alma.Control.AutomaticArrayCommandHelper.narrow(
                    containerServices.getComponent(arrayName));
            } catch(Exception e) {
                ctrl = null;
                logger.severe("SCHEDULING: automatic array command is null");
                throw new SchedulingException("SCHEDULING: Error with getting subarray & ArrayController!");
            }
            auto_controllers.add(new ArrayModeInfo(ctrl, mode));
            logger.info("SCHEDULING: Scheduling created array = "+ ctrl.getArrayComponentName());
            logger.info("SCHEDULING: "+ctrl.getArrayComponentName()+" has "+antenna.length+" antennas");
            return ctrl.getArrayComponentName();
            /*
        } catch(InvalidRequest e1) {
        	sendAlarm("Scheduling","SchedControlConnAlarm",1,ACSFaultState.ACTIVE);
            throw new SchedulingException
                ("SCHEDULING: Control error: "+ e1.toString());
        } catch(InaccessibleException e2) {
        	//sendAlarm("Scheduling","SchedControlConnAlarm",2,ACSFaultState.ACTIVE);
        	sendAlarm("Scheduling","SchedArrayConnAlarm",3,ACSFaultState.ACTIVE);
            throw new SchedulingException
                ("SCHEDULING: Control error: "+ e2.toString());
        } catch (AcsJContainerServicesEx e3) {
            throw new SchedulingException
                ("SCHEDULING: Error getting AutomaticArrayCommand component." +e3.toString());
            */
        } catch (Exception e4) {
            e4.printStackTrace();
            throw new SchedulingException
                ("SCHEDULING: Error" +e4.toString());

        }
    }

    public String createManualArray(String[] antenna) throws SchedulingException {
        if(antenna == null || antenna.length==0){
            throw new SchedulingException
                ("SCHEDULING: Cannot create an array with out any antennas!");
        }
        try {
            if(control_system == null){
                logger.severe("SCHEDULING: control system == null..");
                throw new SchedulingException("SCHEDULING: Error with ControlMaster Component.");
            }
            if(manualArrays == null) {
                logger.severe("SCHEDULING: manualArrays == null..");
                throw new SchedulingException("SCHEDULING: Something went very wrong when setting up ALMAControl");
            }
            String arrayName = control_system.createManualArray(antenna);
            manualArrays.add(arrayName);
            //ManualArrayMonitor mon = alma.Control.
            logger.info("SCHEDULING: Array "+arrayName+" created with "+antenna.length+" antennas.");
            return arrayName;
        } catch(InvalidRequest e1) {
        	sendAlarm("Scheduling","SchedControlConnAlarm",1,ACSFaultState.ACTIVE);
            throw new SchedulingException
                ("SCHEDULING: Control error: "+ e1.toString());
        } catch(InaccessibleException e2) {
        	//sendAlarm("Scheduling","SchedControlConnAlarm",2,ACSFaultState.ACTIVE);
        	sendAlarm("Scheduling","SchedArrayConnAlarm",3,ACSFaultState.ACTIVE);
            throw new SchedulingException
                ("SCHEDULING: Control error: "+ e2.toString());
        }
    }
    

    /**
     *
     * @throws SchedulingException
     */
    public void destroyArray(String name) throws SchedulingException {
        try {
    	    logger.info("SCHEDULING about to destroy array "+name);
            for(int i=0; i < auto_controllers.size(); i++){
	            if( ((AutomaticArrayCommand)auto_controllers.elementAt(i).getArrayComp()).getArrayComponentName().equals(name)) {
	          	    auto_controllers.removeElementAt(i);
                }
	        }
            
            containerServices.releaseComponent(name);
            control_system.destroyArray(name);
        } catch(InvalidRequest e1) {
        	sendAlarm("Scheduling","SchedControlConnAlarm",1,ACSFaultState.ACTIVE);
            throw new SchedulingException(e1); 
        } catch(InaccessibleException e2){
        	//sendAlarm("Scheduling","SchedControlConnAlarm",2,ACSFaultState.ACTIVE);
        	sendAlarm("Scheduling","SchedArrayConnAlarm",3,ACSFaultState.ACTIVE);
            throw new SchedulingException(e2);
        } catch(Exception e3){
            throw new SchedulingException(e3);
        }
    }

    /**
     * @return String[]
     * @throws SchedulingException
     */
    public String[] getActiveArray() throws SchedulingException {
        try {
            ResourceId[] automaticArrays = control_system.getAutomaticArrayComponents();
            ResourceId[] manualArrays = control_system.getManualArrayComponents();
            int all = automaticArrays.length + manualArrays.length;
            String[] allArrays = new String[all];
            int x=0;
            for(int i=0; i < automaticArrays.length; i++){
                allArrays[x++] = automaticArrays[i].ComponentName;
            }
            for(int i=0; i < manualArrays.length; i++){
                allArrays[x++] = manualArrays[i].ComponentName;
            }
            if(allArrays.length != all) {
                throw new SchedulingException(
                        "SCHEDULING: Filling allArrays isn't equal to the all size");
            }
            return allArrays;
        } catch(InaccessibleException e) {
        	//sendAlarm("Scheduling","SchedControlConnAlarm",2,ACSFaultState.ACTIVE);
        	sendAlarm("Scheduling","SchedArrayConnAlarm",3,ACSFaultState.ACTIVE);
            throw new SchedulingException (e);
        }
    }

    public String[] getAllAutomaticArrays() throws SchedulingException{
        try {
            ResourceId[] auto_arrays = control_system.getAutomaticArrayComponents();
            String[] tmp = new String[auto_arrays.length];
            for(int i=0;i< auto_arrays.length; i++){
                tmp[i] = auto_arrays[i].ComponentName;
            }
            return tmp;
        } catch(InaccessibleException e) {
        	sendAlarm("Scheduling","SchedControlConnAlarm",2,ACSFaultState.ACTIVE);
            throw new SchedulingException (e);
        }
    }
    public String[] getAllManualArrays() throws SchedulingException{
        try {
            ResourceId[] man_arrays = control_system.getManualArrayComponents();
            String[] tmp = new String[man_arrays.length];
            for(int i=0;i< man_arrays.length; i++){
                tmp[i] = man_arrays[i].ComponentName;
            }
            return tmp;
        } catch(InaccessibleException e) {
        	//sendAlarm("Scheduling","SchedControlConnAlarm",2,ACSFaultState.ACTIVE);
        	sendAlarm("Scheduling","SchedArrayConnAlarm",3,ACSFaultState.ACTIVE);
            throw new SchedulingException (e);
        }
    }
    
    /**
      * Returns information about ALL arrays which are active (manual and automatic).
      * @return ArrayInfo[]
      */
    public ArrayInfo[] getAllArraysInfo() {
        try {
            ResourceId[] automaticArrays = control_system.getAutomaticArrayComponents();
            for(int i=0;i < automaticArrays.length; i++){
                logger.info("SCHEDULING: auto-array name = "+automaticArrays[i].ComponentName);
            }
            ResourceId[] manualArrays = control_system.getManualArrayComponents();
            int all = automaticArrays.length + manualArrays.length;
            ArrayInfo[] allInfo = new ArrayInfo[all];
            int x=0; //counter for adding to 'allInfo'
            for(int i=0; i < automaticArrays.length; i++){
                allInfo[x] = new ArrayInfo();
                allInfo[x].arrayName = getAutomaticArray(automaticArrays[i].ComponentName).getArrayComponentName();
                //TODO need a way to see if its dynamic/interactive
                allInfo[x].mode =  getArrayMode(allInfo[x].arrayName);//ArrayModeEnum.DYNAMIC;
                if(getAutomaticArray(automaticArrays[i].ComponentName).isBusy()){
                    allInfo[x].state= ArrayStateEnum.BUSY; 
                } else {
                    allInfo[x].state= ArrayStateEnum.IDLE; 
                }
                allInfo[x].projectName ="";
                allInfo[x].SBName ="";
                allInfo[x].completionTime = "";
                allInfo[x].comment="";

                x++;
            }
            for(int i=0; i < manualArrays.length; i++){
                x++;
            }
        
            return allInfo;
        }catch(InaccessibleException e){
            //TODO do something better here eventually
        	//sendAlarm("Scheduling","SchedControlConnAlarm",2,ACSFaultState.ACTIVE);
        	sendAlarm("Scheduling","SchedArrayConnAlarm",3,ACSFaultState.ACTIVE);
            e.printStackTrace(System.out);
            return null;
        } catch (Exception ex){
            ex.printStackTrace(System.out);
            return null;
        }
    }


    private String getArrayProjectName(String arrayName){
        return "";
    }
    /** 
     * @return String[]
     * @throws SchedulingException
     */
    public String[] getIdleAntennas() throws SchedulingException {
        try{
            /*
            ResourceId[] antennas = control_system.getAvailableAntennaComponents();
            logger.info("SCHEDULING: Got "+ antennas.length +" idle antennas");
            String[] tmp=new String[antennas.length];
            for(int i=0; i < antennas.length; i++){
                tmp[i] = antennas[i].ComponentName;
            }
            return tmp;
            */
            return control_system.getAvailableAntennas();
        } catch(InaccessibleException e1) {
        	sendAlarm("Scheduling","SchedControlConnAlarm",2,ACSFaultState.ACTIVE);
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
      * @return AutomaticArrayCommand
      * @throws SchedulingException
      */
    private AutomaticArrayCommand getAutomaticArray(String name) throws SchedulingException {
        logger.info("SCHEDULING: looking for array with id = "+ name);
        for(int i=0; i < auto_controllers.size(); i++){
            if( ((AutomaticArrayCommand)auto_controllers.elementAt(i).getArrayComp()).getArrayComponentName().equals(name)) {
                logger.info("SCHEDULING: found array with id = "+ ((AutomaticArrayCommand)auto_controllers.elementAt(i).getArrayComp()).getArrayComponentName());
                
                return (AutomaticArrayCommand)auto_controllers.elementAt(i).getArrayComp();
            }
        }
        return null;
    }

    private ArrayModeEnum getArrayMode(String arrayname){
        String mode;
        for(int i=0 ; i< auto_controllers.size(); i++){
            if(((AutomaticArrayCommand)auto_controllers.elementAt(i).getArrayComp()).getArrayComponentName().equals(arrayname)){
                mode = auto_controllers.elementAt(i).getMode();
                if(mode.equals("dynamic")){
                    return ArrayModeEnum.DYNAMIC;
                } else if(mode.equals("interactive")){
                    return ArrayModeEnum.INTERACTIVE;
                } else if(mode.equals("queued")){
                    return ArrayModeEnum.QUEUED;
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    /**
      * This will remove the automatic arrays from the controller vector if the 'all' boolean 
      * is true. If it is false it will look for the array with the 'name' and remove just that 
      * one.
      * TODO: check if there is more than one array with the given name.. could be problomatic
      */
    private void removeAutomaticArray(boolean all, String name) throws SchedulingException {
        if(all) {
            logger.finest("SCHEDULING: Removing AutomaticArray objects from queue.");
            auto_controllers.removeAllElements();
            return;
        }
        for(int i=0; i < auto_controllers.size() ;i++){
            if( ((AutomaticArrayCommand)auto_controllers.elementAt(i).getArrayComp()).getArrayComponentName().equals(name)) {
                auto_controllers.removeElementAt(i);
                return;
            }
        }
        throw new SchedulingException("Array ("+name+") does not exist.. cannot remove");
        
    }
    
    private ManualArrayMonitor getManualArray(String name) throws SchedulingException {
        return null;
    }
    /** 
      * If you want to get an array with a given name and you don't know if its
      * automatic or manual use this command. Then check the isManual/isAutomatic to 
      * cast it to the right type.
      *
      * @param name Name of the array.
      * @return ArrayMonitor
      */
    private ArrayMonitor getArray(String name) throws SchedulingException {
        return null;
    }
    
    public void getWeatherStations() throws SchedulingException {
        try { 
            String[] weather = control_system.getWeatherStations();
            logger.info("SCHEDULING: Current weather stations ");
            for(int i=0; i < weather.length; i++) {
                logger.info("\tStation id ="+ weather[i]);
            }
        }catch(InaccessibleException e) {
        	sendAlarm("Scheduling","SchedControlConnAlarm",1,ACSFaultState.ACTIVE);
            throw new SchedulingException(e);
        }
    }

    public void setAntennaOfflineNow(String antennaId) throws SchedulingException {
        try {
            control_system.setAntennaMode(antennaId, AntennaMode.OFFLINE, true);
        } catch(InvalidRequest e1) {
        	sendAlarm("Scheduling","SchedControlConnAlarm",1,ACSFaultState.ACTIVE);
            e1.printStackTrace(System.out);
            throw new SchedulingException(e1);
        } catch(InaccessibleException e2){
        	sendAlarm("Scheduling","SchedControlConnAlarm",2,ACSFaultState.ACTIVE);
            e2.printStackTrace(System.out);
            throw new SchedulingException(e2);
        }
    }
    public void setAntennaOnlineNow(String antennaId) throws SchedulingException {
        try {
            control_system.setAntennaMode(antennaId, AntennaMode.ONLINE, true);
        } catch(InvalidRequest e1) {
        	sendAlarm("Scheduling","SchedControlConnAlarm",1,ACSFaultState.ACTIVE);
            e1.printStackTrace(System.out);
            throw new SchedulingException(e1);
        } catch(InaccessibleException e2){
        	sendAlarm("Scheduling","SchedControlConnAlarm",2,ACSFaultState.ACTIVE);
            e2.printStackTrace(System.out);
            throw new SchedulingException(e2);
        }
    }

    /**
      * release control comp
      */
    public void releaseControlComp() {
        try {
            containerServices.releaseComponent(control_system.name());
        }catch(Exception e) {
            logger.severe("SCHEDULING: Error releasing control comp.");
            sendAlarm("Scheduling","SchedControlConnAlarm",3,ACSFaultState.ACTIVE);
            e.printStackTrace(System.out);
        }
    }

    class ArrayModeInfo {
        private AutomaticArrayCommand arrayComp;
        private String mode;

        public ArrayModeInfo(AutomaticArrayCommand a, String m){
            arrayComp = a;
            mode = m ;
        }
        public String getMode() {
            return mode;
        }
        public AutomaticArrayCommand getArrayComp() {
            return arrayComp;
        }

    }
}