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
 * File ALMAQueuedScheduler.java
 */
package alma.scheduling.AlmaScheduling;

import alma.scheduling.*;
import alma.scheduling.MasterSchedulerIF;
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
import java.util.Vector;

public class ALMAQueuedScheduler
    implements Queued_Operator_to_SchedulingOperations, ComponentLifecycle  {

    private String instanceName;
    private String schedulerId;
    private String currentSB;
    private String currentEB;
    private Vector<String> sbQueue;
    private ContainerServices container;
    private String arrayname;
    private Logger logger;
    private MasterSchedulerIF masterScheduler;
    private boolean execStarted;
    
    public ALMAQueuedScheduler() {
        sbQueue = new Vector<String>();
        arrayname = null;
        schedulerId = null;
        currentSB = null;
        currentEB=null;
        masterScheduler = null;
        execStarted = false;
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
        logger.fine("SCHEDULING: Queued Scheduler execute() ");
        try {
            this.masterScheduler =alma.scheduling.MasterSchedulerIFHelper.narrow(
                    container.getComponentNonSticky("SCHEDULING_MASTERSCHEDULER"));
                        
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
    public void setSchedulerId(String id){
         schedulerId = id;
    }
    public String getSchedulerId(){
         return schedulerId ;
    }

    public void setArray(String array){
        arrayname=array;
    }

    public String getArray() throws InvalidOperationEx {
        if(arrayname == null) {
            AcsJInvalidOperationEx e = new AcsJInvalidOperationEx(
                    new InvalidOperation("runQueue","No array defined!"));
            throw e.toInvalidOperationEx();
        }
        return arrayname;
    }

    public void addSB(String sbid){
        try {
            sbQueue.add(sbid);
            logger.fine("QS_SCHEDULER: adding sb to queue");
            if(execStarted){
                masterScheduler.addSBToQueue(sbid, schedulerId);
            }
        } catch(Exception e){}
    
    }

    public void runQueue() throws InvalidOperationEx {
        if(sbQueue.size() < 1) {
            AcsJInvalidOperationEx e = new AcsJInvalidOperationEx(
                    new InvalidOperation("runQueue","No SBs to run!"));
            throw e.toInvalidOperationEx();
        }
        if(arrayname == null) {
            AcsJInvalidOperationEx e = new AcsJInvalidOperationEx(
                    new InvalidOperation("runQueue","No array defined!"));
            throw e.toInvalidOperationEx();
        }
        if(schedulerId == null){
            AcsJInvalidOperationEx e = new AcsJInvalidOperationEx(
                    new InvalidOperation("runQueue","No scheduler id defined!"));
            throw e.toInvalidOperationEx();
        }
        if(masterScheduler == null){
            AcsJInvalidOperationEx e = new AcsJInvalidOperationEx(
                    new InvalidOperation("runQueue","No master scheduler connection!"));
            throw e.toInvalidOperationEx();
        }
        String[] sbs = new String[sbQueue.size()];
        for(int i=0; i < sbQueue.size(); i++){
            sbs[i]=(String)sbQueue.elementAt(i);
        }
        RunQueuedScheduling run = new RunQueuedScheduling(sbs);
        Thread t = container.getThreadFactory().newThread(run);
        t.start();
        execStarted = true;
    }
    
    public void removeSBs(String[] sbid, int[] i){
        //hmm do i need the sbids?
        for(int x=0; x < i.length; x++){
            if(sbid[x].equals(sbQueue.elementAt(i[x])) ) {
                logger.fine("QS: removing "+sbQueue.elementAt(i[x]));
                sbQueue.removeElementAt(i[x]);
            } else {
                logger.severe("no match when removing sb");
            }
        }
        //tell MS to tell scheduler to take these out of its queue.
        if(execStarted){
            logger.fine("QS: exec started, removing sbs from scheduler");
            try {
                masterScheduler.removeQueuedSBs(sbid, i, schedulerId);
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    
    public void stopSB(String sbid){
        if(execStarted){
        }
    }

    private void releaseMSRef() {
        try {
            if(masterScheduler != null){
                container.releaseComponent(masterScheduler.name());
                
            }
        } catch(Exception e){
            e.printStackTrace();
            logger.severe("RUN_QUEUED_SCHEDULING: Error in RunQueuedScheduling: "+e.toString());
        }

    }

    class RunQueuedScheduling implements Runnable {
        private String[] ids;
    
        public RunQueuedScheduling(String[] i){
            this.ids=i;
        }

        public void run() {
            if(masterScheduler == null) {
                logger.warning("RUN_QUEUED_SCHEDULING: NO Connection to MasterScheduler. Cannot schedule");
                return;
            }
            try {
                masterScheduler.startQueuedScheduling(ids, arrayname);
            } catch(Exception e) {
                releaseMSRef();
                //e.printStackTrace();
                logger.severe("RUN_QUEUED_SCHEDULING: Error in RunQueuedScheduling: "+e.toString());
            }
        }

    }  //end RunQueuedScheduling 

}
