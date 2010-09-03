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
import alma.scheduling.ArchiveUpdaterOperations;
import alma.scheduling.Define.SchedulingException;
import alma.scheduling.archiveupd.functionality.ArchivePoller;
import alma.scheduling.utils.ErrorHandling;

public class ArchiveUpdaterImpl implements ComponentLifecycle,
        ArchiveUpdaterOperations {

    private ContainerServices m_containerServices;

    private Logger m_logger;
    
    private ArchivePoller poller = null;
    private Loop          loop = null;

	private ErrorHandling handler;

	final public long pollInterval = 5 * 60 * 1000; // 5 minutes

    /////////////////////////////////////////////////////////////
    // Implementation of ComponentLifecycle
    /////////////////////////////////////////////////////////////

    public void initialize(ContainerServices containerServices) {
        m_containerServices = containerServices;
        m_logger = m_containerServices.getLogger();
		handler = new ErrorHandling(m_logger);
		getPoller();
		
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
		getPoller();
		if (poller != null) {
			try {
				m_logger.info("Polling archive");
				poller.pollArchive();
			} catch (SchedulingException e) {
				handler.warning(String.format(
						"Error polling archive - %s",
						e.getMessage()), e);
			}
		}
	}

    /////////////////////////////////////////////////////////////
    // Support methods
    /////////////////////////////////////////////////////////////

    public void getPoller() {
    	if (poller == null) {
    		try {
				poller = new ArchivePoller(m_logger);
			} catch (Exception e) {
				handler.severe(String.format(
						"Error creating ArchivePoller - %s",
						e.getMessage()), e);
			}
    	}
    }

    private class Loop extends Thread {
    	public Loop() {
    		setDaemon(true);
    	}
    	
    	public void run() {
    		while (!this.isInterrupted()) {
    			update();
    			try {
    				Thread.sleep(pollInterval);
    			} catch (InterruptedException e) {
    				break;
    			}
    		}
    	}
    }
}
