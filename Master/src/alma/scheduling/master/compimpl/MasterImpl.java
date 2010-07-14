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

package alma.scheduling.master.compimpl;

import java.util.logging.Logger;

import alma.Control.CorrelatorType;
import alma.ACS.ComponentStates;
import alma.acs.component.ComponentLifecycle;
import alma.acs.container.ContainerServices;
import alma.scheduling.ArrayInfo;
import alma.scheduling.ArrayModeEnum;
import alma.scheduling.MasterOperations;

public class MasterImpl implements ComponentLifecycle,
        MasterOperations {

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
    // Implementation of MasterOperations
    /////////////////////////////////////////////////////////////
    
	@Override
	public ArrayInfo createArray(String[] arg0, String[] arg1,
			CorrelatorType arg2, ArrayModeEnum arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void destroyArray(String arg0) {
		// TODO Auto-generated method stub
		
	}
    
}
