/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
package alma.scheduling.archiveupd.mastercomp;

import org.omg.CORBA.Object;

import alma.ACS.MasterComponentImpl.MasterComponentImplBase;
import alma.ACS.MasterComponentImpl.statemachine.AlmaSubsystemActions;
import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.genfw.runtime.sm.AcsStateActionException;
import alma.scheduling.ArchiveUpdater;

public class WorkingDBUpdaterMasterCompImpl extends MasterComponentImplBase
		implements AlmaSubsystemActions {

    private ArchiveUpdater archiveUpdater;
	
	@Override
	public void initSubsysPass1() throws AcsStateActionException {
		//Nothing to do

	}

	@Override
	public void initSubsysPass2() throws AcsStateActionException {
		try {
			Object obj = m_containerServices.getDefaultComponent("IDL:alma/scheduling/ArchiveUpdater");
			archiveUpdater = alma.scheduling.ArchiveUpdaterHelper.narrow(obj);
		} catch (AcsJContainerServicesEx e) {
			e.printStackTrace();
			//TODO: throw/publish new event
		}

	}

	@Override
	public void reinitSubsystem() throws AcsStateActionException {
		if (archiveUpdater != null) {
			m_containerServices.releaseComponent(archiveUpdater.name());
			archiveUpdater = null;
		}
		if (archiveUpdater == null) {
			try {
				Object obj = m_containerServices
						.getDefaultComponent("IDL:alma/scheduling/ArchiveUpdater");
				archiveUpdater = alma.scheduling.ArchiveUpdaterHelper
						.narrow(obj);
			} catch (AcsJContainerServicesEx e) {
				e.printStackTrace();
				// TODO: throw/publish new event
			}
		}
	}

	@Override
	public void shutDownSubsysPass1() throws AcsStateActionException {
		if (archiveUpdater != null) {
			m_containerServices.releaseComponent(archiveUpdater.name());
			archiveUpdater = null;
		}
	}

	@Override
	public void shutDownSubsysPass2() throws AcsStateActionException {
		// Nothing to do

	}

	@Override
	protected AlmaSubsystemActions getActionHandler() {
		return this;
	}

}
