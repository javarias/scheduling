/* ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2006 
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 */

package alma.scheduling.archiveupd.compimpl;

import java.util.logging.Logger;

import alma.ACS.ComponentStates;
import alma.acs.component.ComponentLifecycle;
import alma.acs.container.ContainerServices;
import alma.acs.logging.AcsLogger;
import alma.acs.logging.domainspecific.OperatorLogger;
import alma.scheduling.ArchiveUpdaterCallback;
import alma.scheduling.ArchiveUpdaterOperations;
import alma.scheduling.archiveupd.external.SchedBlockStatusChecker;
import alma.scheduling.archiveupd.functionality.ArchivePoller;
import alma.scheduling.utils.ErrorHandling;

public class ArchiveUpdaterImpl implements ComponentLifecycle,
        ArchiveUpdaterOperations {

    private ContainerServices m_containerServices;

    private Logger m_logger;
    private OperatorLogger m_opLogger;
    
    private ArchivePoller poller = null;
    private Loop          loop = null;
    private SchedBlockStatusChecker sbStatusChecker = null;

	private ErrorHandling handler;

	private final static long initialMinutes = 1;
	private final static long initialMilliseconds = initialMinutes * 60 * 1000;
	private long pollInterval = initialMilliseconds;

    /////////////////////////////////////////////////////////////
    // Implementation of ComponentLifecycle
    /////////////////////////////////////////////////////////////

    public void initialize(ContainerServices containerServices) {
        m_containerServices = containerServices;
        final AcsLogger acsLogger = m_containerServices.getLogger();
        m_logger   = acsLogger;
        m_opLogger = new OperatorLogger(acsLogger);
        if (!ErrorHandling.isInitialized())
        	ErrorHandling.initialize(m_logger);
		handler = ErrorHandling.getInstance();
		getPoller(containerServices);
		getChecker(containerServices);
		
		m_logger.finest("initialize() called...");
    }

    public void execute() {
        m_logger.finest("execute() called...");
        loop = new Loop();
        loop.start();
    }

    public void cleanUp() {
        m_logger.finest("cleanUp() called");
        loop.interrupt();
    }

    public void aboutToAbort() {
        cleanUp();
        m_logger.finest("managed to abort...");
        System.out.println("DummyComponent component managed to abort...");
    }

    /////////////////////////////////////////////////////////////
    // Implementation of ACSComponent
    /////////////////////////////////////////////////////////////

    public ComponentStates componentState() {
        return m_containerServices.getComponentStateManager().getCurrentState();
    }

    public String name() {
        return m_containerServices.getName();
    }

    /////////////////////////////////////////////////////////////
    // Implementation of ArchiveUpdaterOperations
    /////////////////////////////////////////////////////////////
    
	@Override
	synchronized public void update() {
		getPoller(m_containerServices);
		if (poller != null) {
			m_logger.info("Polling archive");
			poller.pollArchive(false);
		}
	}
    
	@Override
	synchronized public void refresh() {
		getPoller(m_containerServices);
		if (poller != null) {
			m_logger.info("Refreshing SWDB");
			poller.pollArchive(true);
		}
	}

	@Override
	public void setPollInterval(int seconds) {
		pollInterval = seconds * 1000;
		loop.interrupt();
	}
	
	@Override
	public void refreshObsProject(String obsPrjUid) {
		getPoller(m_containerServices);
		if (poller != null) {
			m_logger.info("Refreshing ObsProject " + obsPrjUid);
			poller.pollObsProject(obsPrjUid);
		}
	}

//	@Override
//	public int getPollIntervalInSecs() {
//		return (int)pollInterval / 1000;
//	}
//
//	@Override
//	public int getNumSchedBlocks() {
//		return pollInterval / 1000;
//	}
//
//	@Override
//	public int getNumObsProject() {
//		return pollInterval / 1000;
//	}
//
//	@Override
//	public String getLastUpdate() {
//		return pollInterval / 1000;
//	}
    /////////////////////////////////////////////////////////////
    // Support methods
    /////////////////////////////////////////////////////////////

    public void getPoller(ContainerServices containerServices) {
    	if (poller == null) {
    		try {
				poller = new ArchivePoller(m_logger, containerServices);
			} catch (Exception e) {
				e.printStackTrace();
				handler.severe(String.format(
						"Error creating ArchivePoller - %s",
						e.getMessage()), e);
			}
    	}
    }
    
    public void getChecker(ContainerServices containerServices) {
    	if (sbStatusChecker == null) {
    		try {
    			sbStatusChecker = new SchedBlockStatusChecker(m_logger);
    		} catch (Exception ex) {
    			ex.printStackTrace();
				handler.severe(String.format(
						"Error creating SchedBlock Status Checker - %s",
						ex.getMessage()), ex);
    		}
    	}
    }

    private class Loop extends Thread {
    	public Loop() {
    		m_logger.info("Starting polling Thread");
    		setDaemon(true);
    	}
    	
    	public void run() {
    		while (!this.isInterrupted()) {
    			System.out.println("Interrupted: " + this.isInterrupted());
    			try {
    				update();
    			} catch (Exception e) {
    				final String message = String.format(
    						"Error updating Scheduler from ALMA Archive and State Archive - %s - some project updates might have been missed",
    						e.getMessage());
    				e.printStackTrace();
    				m_opLogger.severe(message);
    			}
    			try {
    				getChecker(m_containerServices);
    				sbStatusChecker.checkForStatus();
    			} catch (Exception e) {
    				final String message = String.format(
    						"Error updating SchedBlock Statuses in ALMA State Archive - %s ",
    						e.getMessage());
    				e.printStackTrace();
    				m_opLogger.severe(message);
    			}
    			try {
    				Thread.sleep(pollInterval);
    			} catch (InterruptedException e) {
    				break;
    			}
    		}
    	}
    }

	@Override
	public void deregisterCallback(String arg0) {
		poller.deregisterCallback(arg0);
		
	}

	@Override
	public void registerCallback(String arg0, ArchiveUpdaterCallback arg1) {
		poller.registerCallback(arg0, arg1);
	}

	@Override
	public int getPollInterval() {
		return (int) (pollInterval / 1000);
	}

}
