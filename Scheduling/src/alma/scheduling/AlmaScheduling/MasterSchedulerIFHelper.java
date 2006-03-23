/*
 *    ALMA - Atacama Large Millimiter Array
 *    (c) European Southern Observatory, 2002
 *    (c) Associated Universities Inc., 2002
 *    Copyright by AUI (in the framework of the ALMA collaboration),
 *    All rights reserved
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *    MA 02111-1307  USA
 */

package alma.scheduling.AlmaScheduling;

import java.util.logging.Logger;

import alma.scheduling.MasterSchedulerIFPOATie;
import alma.scheduling.MasterSchedulerIFOperations;
import alma.scheduling.AlmaScheduling.ALMAMasterScheduler;

import alma.acs.component.ComponentLifecycle;
import alma.acs.container.ComponentHelper;

/**
 * @author alma-component-helper-generator-tool
 * @version $Id: MasterSchedulerIFHelper.java,v 1.4 2006/03/23 17:52:54 sslucero Exp $
 */
public class MasterSchedulerIFHelper extends ComponentHelper
{
	/**
	 * Constructor
	 * @param containerLogger logger used only by the parent class.
	 */
	public MasterSchedulerIFHelper(Logger containerLogger)
	{
		super(containerLogger);
	}

	/**
	* @see alma.acs.container.ComponentHelper#_createComponentImpl()
	*/
	protected ComponentLifecycle _createComponentImpl()
	{
		return new ALMAMasterScheduler();
	}

	/**
	* @see alma.acs.container.ComponentHelper#_getPOATieClass()
	*/
	protected Class _getPOATieClass()
	{
		return MasterSchedulerIFPOATie.class;
		//return MasterSchedulerPOATie.class;
	}

	/**
	* @see alma.acs.container.ComponentHelper#getOperationsInterface()
	*/
	protected Class _getOperationsInterface()
	{
		return MasterSchedulerIFOperations.class;
	}

    protected String[] _getComponentMethodsExcludedFromInvocationLogging() {
        return new String[] {"OFFSHOOT::alma.ACS.ROstringSeq#get_sync"};
    }

}
