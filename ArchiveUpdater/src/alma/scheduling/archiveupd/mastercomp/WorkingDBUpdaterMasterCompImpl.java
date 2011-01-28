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
