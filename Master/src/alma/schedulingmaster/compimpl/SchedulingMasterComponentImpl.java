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
package alma.schedulingmaster.compimpl;

import alma.ACS.MasterComponentImpl.MasterComponentImplBase;
import alma.ACS.MasterComponentImpl.statemachine.AlmaSubsystemActions;
import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.genfw.runtime.sm.AcsStateActionException;
import alma.acs.nc.SimpleSupplier;
import alma.scheduling.ArchiveUpdater;
import alma.scheduling.Master;
import alma.scheduling.SchedulingState;
import alma.scheduling.SchedulingStateEvent;

/**
  *
  * @author Jorge Avarias <javarias[at]nrao.edu>
  * @version $Id: SchedulingMasterComponentImpl.java,v 1.3 2011/08/31 19:50:07 javarias Exp $
  */
//TODO: Start archive poller
public class SchedulingMasterComponentImpl extends MasterComponentImplBase 
    implements AlmaSubsystemActions {
    

    private Master masterScheduler;
//    private ArchiveUpdater archiveUpdater;
    
    private SimpleSupplier nc;
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
        m_logger.fine("SCHEDULING MC: getActionHandler() method called");
        return this;
    }

    /**
      * Try to initialize the MasterScheduler.
      * Throw a bunch of errors if it didn't work..
      */
    public void initSubsysPass1() throws AcsStateActionException {
        getNC();
        m_logger.fine("SCHEDULING MC: initSubsysPass1() method called");
        publishSchedulingStateEvent(SchedulingState.ONLINE_PASS1);
    }

	/**
	 * If the first pass didn't initialize the masterScheduler
	 */
	public void initSubsysPass2() throws AcsStateActionException {
		m_logger.fine("SCHEDULING MC: initSubsysPass2() method called");
		try {
			masterScheduler = alma.scheduling.MasterHelper
					.narrow(m_containerServices
							.getDefaultComponent("IDL:alma/scheduling/Master:1.0"));
			monitorComponent(masterScheduler);
			publishSchedulingStateEvent(SchedulingState.ONLINE_PASS2);
		} catch (AcsJContainerServicesEx e) {
			m_logger.severe("SCHEDULING MC: error getting MasterScheduler component in pass2.");
			// set the ms to null just to be safe..
			masterScheduler = null;
			publishSchedulingStateEvent(SchedulingState.ERROR);
			throw new AcsStateActionException(e);
		}
	}

    /**
      *
      */
    public void reinitSubsystem() throws AcsStateActionException {
        m_logger.fine("SCHEDULING MC: reinitSubsystem() method called.");
        if(masterScheduler != null){
        	try {
        		m_containerServices.releaseComponent(masterScheduler.name());
        		masterScheduler = null;
        	} catch(Exception ex){
        		m_logger.severe("SCHEDULING MC: Unable to shutdown master scheduler component.");
        		throw new AcsStateActionException(ex);
        	}
        }
        
        try {
            if(masterScheduler == null) {
                masterScheduler = alma.scheduling.MasterHelper.narrow(
                    m_containerServices.getDefaultComponent("IDL:alma/scheduling/Master:1.0"));
            } else {
                m_containerServices.releaseComponent(masterScheduler.name());
                masterScheduler = null;
                masterScheduler = alma.scheduling.MasterHelper.narrow(
                    m_containerServices.getDefaultComponent("IDL:alma/scheduling/Master:1.0"));
            }
            monitorComponent(masterScheduler);
        } catch (AcsJContainerServicesEx e) {
            m_logger.severe("SCHEDULING MC: error reinitializing Scheduling Subsystem...");
            //set the ms to null just to be safe..
            masterScheduler = null;
            publishSchedulingStateEvent(SchedulingState.ERROR);
            throw new AcsStateActionException(e);
        }
    }

    /**
      * Attempt to release the master scheduler component.
      */
	public void shutDownSubsysPass1() throws AcsStateActionException {
		m_logger.fine("SCHEDULING MC: shutDownSubsysPass1() method called");
		stopMonitoringAllResources();
		try {
			if (masterScheduler != null) {
				m_containerServices.releaseComponent(masterScheduler.name());
				masterScheduler = null;
			}
			if (nc != null) {
				nc.disconnect();
				nc = null;
			}
		} catch (Exception e) {
			m_logger.severe("SCHEDULING MC: error releasing MasterScheduler component in Pass1.");
			publishSchedulingStateEvent(SchedulingState.ERROR);
			throw new AcsStateActionException(e);
		}
	}
    
    /**
      * Try a second time just incase the first shutdown pass did not release the 
      * master scheduler.
      */
    public void shutDownSubsysPass2() throws AcsStateActionException {
        m_logger.fine("SCHEDULING MC: shutDownSubsysPass2() method called");
        stopMonitoringAllResources();
        
        try {
            if(masterScheduler != null) {
                m_containerServices.releaseComponent("SCHEDULING_MASTERSCHEDULER");
                masterScheduler = null;
            }
            
            publishSchedulingStateEvent(SchedulingState.OFFLINE);
        } catch(Exception e) {
            m_logger.severe("SCHEDULING MC: error releasing MasterScheduler component in Pass2.");
            publishSchedulingStateEvent(SchedulingState.ERROR);
            throw new AcsStateActionException(e);
        }
    }
    
    private void getNC(){
        try {
            nc = new SimpleSupplier(
                alma.scheduling.CHANNELNAME_SCHEDULING.value, m_containerServices);
            m_logger.fine("SchedulingStateEvent sent");
        }catch(Exception e){
        	m_logger.severe("can not get the Scheduling notification");
            e.printStackTrace();
        }
    }

    private void publishSchedulingStateEvent(SchedulingState x) {        
        SchedulingStateEvent e = new SchedulingStateEvent();
        e.state = x;
        try {
            nc.publishEvent(e);
        }catch(Exception ex){
            m_logger.severe("MasterScheduler can not publish the SchedulingStateEvent");
        }
    }


}
