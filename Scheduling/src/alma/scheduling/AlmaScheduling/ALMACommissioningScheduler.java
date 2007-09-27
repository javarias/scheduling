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
 * File ALMACommissioningScheduler.java
 */

package alma.scheduling.AlmaScheduling;

import alma.ACS.ComponentStates;
import alma.acs.container.ContainerServices;
import alma.acs.component.ComponentLifecycle;
import alma.acs.component.ComponentLifecycleException;

import alma.scheduling.*;
import alma.scheduling.MasterSchedulerIF;
import alma.scheduling.ProjectLite;
import alma.SchedulingExceptions.*;
import alma.SchedulingExceptions.wrappers.*;
/**
  * This class will eventully be the combination of the interactive and
  * queued schedulers. It will need a plugin that merges the 2 existing
  * plugins for the schedulers. (noted 9/27/07)
  */
public class ALMACommissioningScheduler 
    implements Commissioning_Operator_to_SchedulingOperations, ComponentLifecycle {
        
        private String instanceName;
        private ContainerServices container;
        private String schedulerId;
        private String arrayname;
        
        public void setSchedulerId(String id){
            schedulerId = id;
        }
        public String getSchedulerId(){
            return schedulerId;
        }

        /**
          * Set the array name of the scheduler
          */
        public void setCommissioningArray(String array)
            throws InvalidOperationEx, InvalidObjectEx{
                
            arrayname = array;
        }
        /**
          * Get the array name of the scheduler
          */
        public String getCommissioningArray() throws InvalidOperationEx {
            return arrayname;
        }

        /**
          * Add this SB to the scheduler's queue. Not the archive! It
          * should already be in the archive.           */
        public void addSCommissioningB(String sbid){
        }

        /**
          * tells the master scheduler to start a queued scheduler and
          * run the sbs in the list already collected in this comp.
          * if there have been no sbs added yet, an error is thrown
          */
        public void runCommissioningQueue() throws InvalidOperationEx {
        }
        /**
          * Remove this SB from the scheduler's queue. Not the archive!
          */
        public void removeCommissioningSBs(String[] sbid, int[] indices ) throws NoSuchSBEx{
        }

        /**
          * Tell the control system to stop executing this SB.
          */
        public void stopCommissioningSB(String sbid) throws InvalidOperationEx{
        }
        public void stopCommissioningSBNow(String sbid) throws InvalidOperationEx{
        }

        /**
          *Execute a single SB
          */
        public void executeCommissioningSB(String sbid)
            throws InvalidOperationEx, NoSuchSBEx, CannotRunCompleteSBEx {
        }

        /////////////
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
        public void initialize(ContainerServices cs) throws ComponentLifecycleException {
            container = cs;
        }
        public void execute() throws ComponentLifecycleException {
        }
        public void cleanUp(){
        }
        public void aboutToAbort() {
        }
        
}

