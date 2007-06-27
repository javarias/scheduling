/*
 * Gets a list of projects for the sbs ids that are passed in.
 * This function is for the start queue scheduling method./*
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
 * File ALMAInteractiveScheduler.java
 */

package alma.scheduling.AlmaScheduling;

import alma.scheduling.*;
import alma.scheduling.MasterSchedulerIF;
import alma.scheduling.Interactive_PI_to_SchedulingOperations;
import alma.scheduling.ProjectLite;
import alma.SchedulingExceptions.InvalidOperationEx;
import alma.SchedulingExceptions.InvalidObjectEx;
import alma.SchedulingExceptions.UnidentifiedResponseEx;
import alma.SchedulingExceptions.SBExistsEx;
import alma.SchedulingExceptions.NoSuchSBEx;
import alma.SchedulingExceptions.wrappers.AcsJInvalidOperationEx;
import alma.SchedulingExceptions.wrappers.AcsJInvalidObjectEx;
import alma.SchedulingExceptions.wrappers.AcsJUnidentifiedResponseEx;
import alma.SchedulingExceptions.wrappers.AcsJNoSuchSBEx;
import alma.SchedulingExceptions.wrappers.AcsJSBExistsEx;

import alma.scheduling.Define.*;
import alma.scheduling.Scheduler.*;

import alma.ACS.ComponentStates;
import alma.acs.container.ContainerServices;
import alma.acs.component.ComponentLifecycle;
import alma.acs.component.ComponentLifecycleException;
import alma.xmlentity.XmlEntityStruct;

import java.util.logging.Logger;

public class ALMAInteractiveScheduler extends InteractiveScheduler 
    implements Interactive_PI_to_SchedulingOperations, ComponentLifecycle  {

    private String instanceName;
    private String schedulerId;
    private String currentSB;
    private String currentEB;
    private ContainerServices container;
    private String arrayname;
    private Logger logger;
    private MasterSchedulerIF masterScheduler;
    
    public ALMAInteractiveScheduler() {
        currentSB = "";
        schedulerId = "";
        arrayName = "";
    }

    /////////////////////////////////////////////////////////////////////

    /**
     * Needed from ACSComponentOperations (MasterSchedulerIFOperations)
     * @return ComponentStates
     */
    public ComponentStates componentState() {
        ComponentStates state = container.getComponentStateManager().getCurrentState();
        return state;
    }
    /**
     * Needed from ACSComponentOperations (MasterSchedulerIFOperations)
     * @return String
     */
    public String name() {
        return instanceName;
    }

    public void initialize(ContainerServices cs)
        throws ComponentLifecycleException {

        container = cs;
        logger = cs.getLogger();
        this.instanceName = container.getName();
    }
    public void execute() throws ComponentLifecycleException{
        logger.fine("SCHEDULING: Interactive Scheduler execute() ");
        try {
            this.masterScheduler =alma.scheduling.MasterSchedulerIFHelper.narrow(
                    container.getDefaultComponent(
                        "IDL:alma/scheduling/MasterSchedulerIF:1.0"));
        } catch(Exception e){
            e.printStackTrace();
            masterScheduler=null;
        }
    }

    public void cleanUp(){
        aboutToAbort();
    }

    public void aboutToAbort() {
        try {
            container.releaseComponent(masterScheduler.name());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    /////////////////////////////////////////////////////////////////////

    public void startSession(String piId, String projectId) 
        throws InvalidOperationEx, InvalidObjectEx {
            
        try {
            masterScheduler.startInteractiveSession(piId, projectId, schedulerId);
        }catch(Exception e){
            e.printStackTrace();
            InvalidOperation e1 = new InvalidOperation("startSession",e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
    }

    public void setArray(String an) throws InvalidOperationEx, InvalidObjectEx {
        //TODO check to make sure the array exists eventually
        arrayname = an;
    }
    public void setSchedulerId(String id) {
        schedulerId = id;
    }
    public String getSchedulerId() {
        return schedulerId ;
    }

    public String getArray() {
        return arrayname;
    }

    public void setCurrentSB(String sbid){
        currentSB = sbid;
    }

    public String getCurrentSB(){
        return currentSB;
    }
    
    public void setCurrentEB(String ebid){
        currentEB = ebid;
    }

    public String getCurrentEB(){
        return currentEB;
    }
    /*public void addSB(String sbId) throws 
        InvalidOperationEx, InvalidObjectEx, SBExistsEx {
        try {
            masterScheduler.addInteractiveSB(sbId, schedulerId);
        }catch(Exception e){
            InvalidOperation e1 = new InvalidOperation("addSB",e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
    }
    
    public void deleteSB(String sbId) throws InvalidOperationEx, NoSuchSBEx{
        try {
            masterScheduler.deleteInteractiveSB(sbId, schedulerId);
        }catch(Exception e){
            InvalidOperation e1 = new InvalidOperation("deleteSB",e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
    }

    public void updateSB(String sbId) throws
        InvalidOperationEx, InvalidObjectEx, NoSuchSBEx {

        try {
            masterScheduler.updateInteractiveSB(sbId, schedulerId);
        }catch(Exception e){
            InvalidOperation e1 = new InvalidOperation("updateSB",e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }

    }*/

    public void executeSB(String sbId) 
        throws InvalidOperationEx, NoSuchSBEx {

        try {
            logger.fine("sb id: "+sbId);
            logger.fine("scheduler id: "+schedulerId);
            masterScheduler.executeInteractiveSB(sbId, schedulerId);
        } catch(Exception e){
            logger.severe("SCHEDULING: executeSB in IS_COMP: error = "+e.toString());
            e.printStackTrace();
            InvalidOperation e1 = new InvalidOperation("executeSB",e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
    }

    public void stopSB() throws InvalidOperationEx {
        try {
            masterScheduler.stopInteractiveSB(schedulerId);
        } catch(Exception e) {
            e.printStackTrace();
            InvalidOperation e1 = new InvalidOperation("stopSB",e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
    }
    
    public void endSession() throws InvalidOperationEx{
        try {
            masterScheduler.endInteractiveSession(schedulerId);
        } catch (Exception e){
            e.printStackTrace();
            InvalidOperation e1 = new InvalidOperation("endSession",e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
    }
}
