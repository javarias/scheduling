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
import alma.ACS.SUBSYSSTATE_AVAILABLE;
import alma.ACS.SUBSYSSTATE_OPERATIONAL;
import alma.ACS.SUBSYSSTATE_ONLINE;

import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;
import alma.acs.component.ComponentLifecycleException;

import alma.scheduling.MasterSchedulerIF;
import alma.scheduling.SchedulingMasterComponentOperations;

public class SchedulingMasterComponentImpl extends MasterComponentImplBase 
    implements AlmaSubsystemActions, SchedulingMasterComponentOperations {
    

    private MasterSchedulerIF masterScheduler;
    private ContainerServices containerServices;

    /*public SchedulingMasterComponentImpl() {
        super();
    }*/

    ////////////////////////////////////////////////////////////////
    // Component Lifecycle Methods
    ////////////////////////////////////////////////////////////////
    public void initialize(ContainerServices containerServices) 
        throws ComponentLifecycleException {
    
        //super.initialize(containerServices);
        this.containerServices = containerServices;
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
                containerServices.getDefaultComponent(
                    "IDL:alma/scheduling/MasterSchedulerIF:1.0"));
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

    ////////////////////////////////////////////////////////////////
}
