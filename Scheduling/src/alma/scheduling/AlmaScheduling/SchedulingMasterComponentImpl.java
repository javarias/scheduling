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

import java.util.logging.Logger;

import alma.ACS.MasterComponentImpl.*;
import alma.ACS.MasterComponentImpl.statemachine.*;
import alma.acs.genfw.runtime.sm.*;
import alma.ACS.ComponentStates;
import alma.ACS.MasterComponentPackage.*;
import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;
import alma.acs.component.ComponentLifecycleException;

import alma.scheduling.MasterSchedulerIF;

import alma.acs.component.client.ComponentClient;
import alma.acs.commandcenter.meta.*;

/**
  *
  * @author Sohaila Lucero
  * @version $Id: SchedulingMasterComponentImpl.java,v 1.21 2006/05/01 18:10:42 sslucero Exp $
  */
public class SchedulingMasterComponentImpl extends MasterComponentImplBase 
    implements AlmaSubsystemActions {
    

    private MasterSchedulerIF masterScheduler;

    /**
      *
      */
    public SchedulingMasterComponentImpl() {
        super();
    }

    ////////////////////////////////////////////////////////////////
    
    /**
      *
      */
    protected AlmaSubsystemActions getActionHandler() {
        m_logger.info("SCHEDULING MC: getActionHandler() method called");
        return this;
    }

    /**
      * Try to initialize the MasterScheduler.
      * Throw a bunch of errors if it didn't work..
      */
    public void initSubsysPass1() throws AcsStateActionException {
        m_logger.info("SCHEDULING MC: initSubsysPass1() method called");
        try {
            masterScheduler = alma.scheduling.MasterSchedulerIFHelper.narrow(
                m_containerServices.getDefaultComponent("IDL:alma/scheduling/MasterSchedulerIF:1.0"));
        } catch (ContainerException e) {
            m_logger.severe("SCHEDULING MC: error getting MasterScheduler component in pass1.");
            //set the ms to null just to be safe..
            masterScheduler = null;
            e.printStackTrace(System.out);
            //set scheduling MC to error state. 
            try {
                doTransition(SubsystemStateEvent.SUBSYSEVENT_ERROR);
            } catch(alma.ACSErrTypeCommon.IllegalStateEventEx ex) {
                ex.printStackTrace(System.out);
                throw new AcsStateActionException(ex);
            }
            throw new AcsStateActionException(e);
        }
    }

    /**
      * If the first pass didnt initialize the masterScheduler
      */
    public void initSubsysPass2() throws AcsStateActionException {
        m_logger.info("SCHEDULING MC: initSubsysPass2() method called");
        try {
            if(masterScheduler == null) {
                masterScheduler = alma.scheduling.MasterSchedulerIFHelper.narrow(
                    m_containerServices.getDefaultComponent("IDL:alma/scheduling/MasterSchedulerIF:1.0"));
            }
        } catch (ContainerException e) {
            m_logger.severe("SCHEDULING MC: error getting MasterScheduler component in pass2.");
            //set the ms to null just to be safe..
            masterScheduler = null;
            e.printStackTrace(System.out);
            try {
                doTransition(SubsystemStateEvent.SUBSYSEVENT_ERROR);
            } catch(alma.ACSErrTypeCommon.IllegalStateEventEx ex) {
                ex.printStackTrace(System.out);
                throw new AcsStateActionException(ex);
            }
            throw new AcsStateActionException(e);
        }
    }

    /**
      *
      */
    public void reinitSubsystem() throws AcsStateActionException {
        m_logger.info("SCHEDULING MC: reinitSubsystem() method called.");
        try {
            if(masterScheduler == null) {
                masterScheduler = alma.scheduling.MasterSchedulerIFHelper.narrow(
                    m_containerServices.getDefaultComponent("IDL:alma/scheduling/MasterSchedulerIF:1.0"));
            } else {
                m_containerServices.releaseComponent(masterScheduler.name());
                masterScheduler = null;
                masterScheduler = alma.scheduling.MasterSchedulerIFHelper.narrow(
                    m_containerServices.getDefaultComponent("IDL:alma/scheduling/MasterSchedulerIF:1.0"));
            }
        } catch (ContainerException e) {
            m_logger.severe("SCHEDULING MC: error reinitializing Scheduling Subsystem...");
            //set the ms to null just to be safe..
            masterScheduler = null;
            e.printStackTrace(System.out);
            try {
                doTransition(SubsystemStateEvent.SUBSYSEVENT_ERROR);
            } catch(alma.ACSErrTypeCommon.IllegalStateEventEx ex) {
                ex.printStackTrace(System.out);
                throw new AcsStateActionException(ex);
            }
            throw new AcsStateActionException(e);
        }
    }

    /**
      * Attempt to release the master scheduler component.
      */
    public void shutDownSubsysPass1() throws AcsStateActionException {
        m_logger.info("SCHEDULING MC: shutDownSubsysPass1() method called");
        try {
            if(masterScheduler != null) {
                //m_containerServices.releaseComponent("SCHEDULING_MASTERSCHEDULER");
                m_containerServices.releaseComponent(masterScheduler.name());
                ReallyShutdownScheduling woo = new ReallyShutdownScheduling(m_logger);
                woo.forceMasterSchedulerShutdown();
                masterScheduler = null;
            }
        } catch(Exception e) {
            m_logger.severe("SCHEDULING MC: error releasing MasterScheduler component in Pass1.");
            e.printStackTrace(System.out);
            try {
                doTransition(SubsystemStateEvent.SUBSYSEVENT_ERROR);
            } catch(alma.ACSErrTypeCommon.IllegalStateEventEx ex) {
                ex.printStackTrace(System.out);
                throw new AcsStateActionException(ex);
            }
            throw new AcsStateActionException(e);
        }
    }

    /**
      * Try a second time just incase the first shutdown pass did not release the 
      * master scheduler.
      */
    public void shutDownSubsysPass2() throws AcsStateActionException {
        m_logger.info("SCHEDULING MC: shutDownSubsysPass2() method called");
        try {
            if(masterScheduler != null) {
                m_containerServices.releaseComponent("SCHEDULING_MASTERSCHEDULER");
                //m_containerServices.releaseComponent(masterScheduler.name());
                masterScheduler = null;
            }
        } catch(Exception e) {
            m_logger.severe("SCHEDULING MC: error releasing MasterScheduler component in Pass2.");
            e.printStackTrace(System.out);
            try {
                doTransition(SubsystemStateEvent.SUBSYSEVENT_ERROR);
            } catch(alma.ACSErrTypeCommon.IllegalStateEventEx ex) {
                ex.printStackTrace(System.out);
                throw new AcsStateActionException(ex);
            }
            throw new AcsStateActionException(e);
        }
    }

    ////////////////////////////////////////////////////////////////

    class ReallyShutdownScheduling extends ComponentClient {

        public ReallyShutdownScheduling(Logger l) throws Exception {
            super(l, System.getProperty("ACS.manager"), "SchedulingMC");
        }
        
        public void forceMasterSchedulerShutdown() throws Exception {
            m_logger.info("SCHEDULING MC = Get MaciSuperFactory");
            MaciSupervisorFactory factory = 
                new MaciSupervisorFactory(m_logger, acsCorba.getORB()); 
            
            m_logger.info("SCHEDULING MC = Get MaciSuper");
            IMaciSupervisor im = factory.giveMaciSupervisor(
                    System.getProperty("ACS.manager"), m_logger);
                    
            im.forceReleaseComponent("SCHEDULING_MASTERSCHEDULER");
        }
    }
}
