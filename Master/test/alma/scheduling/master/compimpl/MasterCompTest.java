package alma.scheduling.master.compimpl;

import org.omg.CORBA.Object;

import alma.ACS.MasterComponent;
import alma.ACS.MasterComponentHelper;
import alma.ACS.MasterComponentImpl.statemachine.AlmaSubsystemActions;
import alma.ACS.MasterComponentPackage.SubsystemStateEvent;
import alma.ACS.MasterComponentPackage.SubsystemStateEventHolder;
import alma.ACSErrTypeCommon.IllegalStateEventEx;
import alma.acs.component.client.ComponentClientTestCase;
import alma.acs.genfw.runtime.sm.AcsStateActionException;

public class MasterCompTest extends ComponentClientTestCase {

	MasterComponent comp;
	
	public MasterCompTest() throws Exception {
		super("Master Component Test");
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Object obj = getContainerServices().getComponent(Constants.MASTER_SCHEDULING_COMP_URL);
		comp = MasterComponentHelper.narrow(obj);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		getContainerServices().releaseComponent(Constants.MASTER_SCHEDULING_COMP_URL);
		comp=null;
	}
	
	public void testInitialization() throws AcsStateActionException, IllegalStateEventEx, InterruptedException {
		comp.doTransition(SubsystemStateEvent.SUBSYSEVENT_INITPASS1);
		Thread.sleep(10000);
		comp.doTransition(SubsystemStateEvent.SUBSYSEVENT_INITPASS2);
		Thread.sleep(10000);
		comp.doTransition(SubsystemStateEvent.SUBSYSEVENT_START);
		Thread.sleep(10000);
	}

}