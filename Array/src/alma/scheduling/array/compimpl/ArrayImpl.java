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

package alma.scheduling.array.compimpl;

import java.util.logging.Logger;

import alma.ACS.ComponentStates;
import alma.acs.component.ComponentLifecycle;
import alma.acs.container.ContainerServices;
import alma.scheduling.ArrayOperations;
import alma.scheduling.SchedBlockExecutionCallback;
import alma.scheduling.SchedBlockScore;

public class ArrayImpl implements ComponentLifecycle,
        ArrayOperations {

    private ContainerServices m_containerServices;

    private Logger m_logger;

    /////////////////////////////////////////////////////////////
    // Implementation of ComponentLifecycle
    /////////////////////////////////////////////////////////////

    public void initialize(ContainerServices containerServices) {
        m_containerServices = containerServices;
        m_logger = m_containerServices.getLogger();

        m_logger.finest("initialize() called...");
    }

    public void execute() {
        m_logger.finest("execute() called...");
    }

    public void cleanUp() {
        m_logger.finest("cleanUp() called");
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
    // Implementation of ArrayOperations
    /////////////////////////////////////////////////////////////    
    
	@Override
	public void moveDown(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveUp(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String pull() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void push(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void abortRunningtSchedBlock() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start(SchedBlockExecutionCallback arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopRunningSchedBlock() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SchedBlockScore[] run() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRunningSchedBlock() {
		// TODO Auto-generated method stub
		return null;
	}
}
