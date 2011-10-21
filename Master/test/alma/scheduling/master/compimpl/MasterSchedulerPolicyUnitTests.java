package alma.scheduling.master.compimpl;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.jmock.lib.legacy.ClassImposteriser;

import alma.Control.ControlMaster;
import alma.SchedulingMasterExceptions.SchedulingInternalExceptionEx;
import alma.acs.container.ContainerServices;
import alma.acs.logging.AcsLogger;
import alma.scheduling.Array;
import alma.scheduling.ArrayStatusCallback;

public class MasterSchedulerPolicyUnitTests extends MockObjectTestCase {
	private ControlMaster controlMaster = mock(ControlMaster.class);
	private ContainerServices contServices = mock(ContainerServices.class);
	final Array retArray = mock(Array.class);
	MasterImpl schedMaster;
	AcsLogger logger;
	
	protected void setUp() throws Exception {
		super.setUp();
		setImposteriser(ClassImposteriser.INSTANCE);
		schedMaster = new MasterImpl();
		logger = mock(AcsLogger.class);
		final ArrayStatusCallback callback = mock(ArrayStatusCallback.class);
		checking(new Expectations() { {
			ignoring(contServices);
			ignoring(logger);
		} });
		schedMaster.initialize(contServices);
		schedMaster.setLogger(logger);
		schedMaster.setControlMaster(controlMaster);
	}

	public void testAddNewSchedulingPolicies() throws SchedulingInternalExceptionEx {
		final String xmlToLoad = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<Policies><SchedulingPolicy name=\"TestPolicy\">\n<SelectionCriteria></SelectionCriteria>" +
				"<Scorers></Scorers></SchedulingPolicy></Policies>";
		checking(new Expectations() { {
		} });
		schedMaster.addSchedulingPolicies("localhost", "lala.xml", xmlToLoad);
	}
	
	public void testAddNewSchedulingPoliciesFailure() {
		final String xmlToLoad = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<Policies><SchedulingPolicy name=\"TestPolicy\">\n<SelectionCriteria></SelectionCriteria>" +
				"<Scorers></Scorers><SchedulingPolicy></Policies>";
		checking(new Expectations() { {
		} });
		try {
			schedMaster.addSchedulingPolicies("localhost", "lala.xml", xmlToLoad);
		} catch (SchedulingInternalExceptionEx ex) {
			//System.out.println("This failure is expected: " + ex.errorTrace.previousError[0].shortDescription);
		}
	}
}
