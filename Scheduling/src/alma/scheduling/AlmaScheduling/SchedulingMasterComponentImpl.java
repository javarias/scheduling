/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 * File SchedulingMasterComponentImpl.java
 */
package alma.scheduling.AlmaScheduling;

import alma.ACS.MasterComponentImpl.*;
import alma.ACS.MasterComponentImpl.statemachine.*;
import alma.ACS.ComponentStates;
import alma.ACS.SUBSYSSTATE_AVAILABLE;
import alma.ACS.SUBSYSSTATE_OPERATIONAL;
import alma.ACS.SUBSYSSTATE_ONLINE;

import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;
import alma.acs.component.ComponentLifecycleException;

import alma.scheduling.MasterSchedulerIF;
//import alma.scheduling.SchedulingMasterComponentOperations;

public class SchedulingMasterComponentImpl extends MasterComponentImplBase 
    implements AlmaSubsystemActions {
    

    private MasterSchedulerIF masterScheduler;
    private ContainerServices cs;

    public SchedulingMasterComponentImpl() {
        super();
    }

    ////////////////////////////////////////////////////////////////
    // Component Lifecycle Methods
    ////////////////////////////////////////////////////////////////
    
    
    public void initialize(ContainerServices containerServices) 
        throws ComponentLifecycleException {
    
        super.initialize(containerServices);
        this.cs = containerServices;
        m_logger = containerServices.getLogger();
        m_logger.info("SCHEDULING MC: master component initialized.");
    }

    public void execute() {
        m_logger.info("SCHEDULING MC: master component executing.");
    }

    public void cleanUp() {
        m_logger.info("SCHEDULING MC: master component clean up.");
    }

    public void aboutToAbort() {
        cleanUp();
        m_logger.info("SCHEDULING MC: master component about to abort.");
    }
    
    ////////////////////////////////////////////////////////////////
    
    protected AlmaSubsystemActions getActionHandler() {
        m_logger.info("SCHEDULING MC: getActionHandler() method called");
        return this;
    }

    public void initSubsysPass1() {
        m_logger.info("SCHEDULING MC: initSubsysPass1() method called");
        try {
            masterScheduler = alma.scheduling.MasterSchedulerIFHelper.narrow(
                cs.getComponent("MasterScheduler"));
        } catch (ContainerException e) {
            m_logger.severe("SCHEDULING MC: error getting MasterScheduler component.");
            e.printStackTrace();
        }
    }

    public void initSubsysPass2() {
        m_logger.info("SCHEDULING MC: initSubsysPass2() method called");
    }

    public void reinitSubsystem() {
        m_logger.info("SCHEDULING MC: reinitSubsystem() method called");
    }

    public void shutDownSubsysPass1() {
        m_logger.info("SCHEDULING MC: shutDownSubsysPass1() method called");
    }

    public void shutDownSubsysPass2() {
        m_logger.info("SCHEDULING MC: shutDownSubsysPass2() method called");
    }

    
    public void stateChangedNotify (AcsState[] oldStateHierarchy,
        AcsState[] newStateHierarchy) {
        //super.stateChangedNotify(oldStateHierarchy, newStateHierarchy);

                //
        // Note that an assumption that the substate separator is "/" is being
        // made here. I cannot find an easy method to create an AcsState
        // quantity from a list of substate names. This is probably easy
        // but not obvious.
        //
        /*
        String startHi = SUBSYSSTATE_AVAILABLE.value + "/" +
            SUBSYSSTATE_OPERATIONAL.value;
        String stopHi = SUBSYSSTATE_AVAILABLE.value + "/" +
            SUBSYSSTATE_ONLINE.value;

        //
        // This uses the builtin in string conversion routine which currently
        // uses the separator "/" internally
        //

        String oldHi = AcsStateUtil.stateHierarchyToString (oldStateHierarchy);
        String newHi = AcsStateUtil.stateHierarchyToString (newStateHierarchy);

        //
        // Change the state of the master component. Trap the start and stop
        // events which are defined by the appropriate before and after state
        // hieararchies.
        //

        try {

            //
            // Check for a null change of state.
            //

            if (oldHi.equals(newHi)) {
                m_logger.info ("schedtest: subsystem states are the same " +  newHi);

            //
            // Deal with changes of state by updating the state hierarchy. Do
            // something special with start and stop events.
            //

            } else {
                if (oldHi.equals(stopHi) && newHi.equals(startHi)) {
                    m_logger.info ("schedtest: subsystem start method executed"
);
                } else if (oldHi.equals(startHi) && newHi.equals(stopHi)) {
                    m_logger.info ("schedtest: subsystem stop method executed")
;
                }
                updateStateHierarchy();
                m_logger.info ("schedtest: subsystem state has changed from " + oldHi + " to " + newHi);
            }
        } catch (Exception e) {
            m_logger.warning ("schedtest: failed to update state hierarchy");
        }
        */

    }
    ////////////////////////////////////////////////////////////////

}
