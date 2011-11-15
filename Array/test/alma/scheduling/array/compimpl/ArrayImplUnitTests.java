package alma.scheduling.array.compimpl;

import java.util.logging.Logger;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.jmock.lib.legacy.ClassImposteriser;

import alma.Control.AutomaticArray;
import alma.Control.CorrelatorType;
import alma.Control.ManualArrayCommand;
import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.container.ContainerServices;
import alma.acs.logging.AcsLogger;
import alma.lifecycle.config.StateSystemContextFactory;
import alma.lifecycle.persistence.StateArchive;
import alma.lifecycle.stateengine.StateEngine;
import alma.pipelineql.QlDisplayManager;
import alma.scheduling.ArrayDescriptor;
import alma.scheduling.ArrayModeEnum;
import alma.scheduling.ArraySchedulerLifecycleType;
import alma.scheduling.ArraySchedulerMode;
import alma.scheduling.SchedBlockQueueItem;
import alma.scheduling.array.executor.Executor;
import alma.scheduling.array.executor.services.AcsNotificationChannel;
import alma.scheduling.array.executor.services.AcsProvider;
import alma.scheduling.array.sbQueue.SchedBlockItem;
import alma.scheduling.array.sbSelection.DSASelector;
import alma.scheduling.datamodel.obsproject.dao.ModelAccessor;

public class ArrayImplUnitTests extends MockObjectTestCase  {
	{
		setImposteriser(ClassImposteriser.INSTANCE);
	}
	private ContainerServices contServices = mock(ContainerServices.class);
	final private Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	final private AcsLogger acsLogger = mock(AcsLogger.class);
	private ArraySchedulerMode[] simpleAutoArrayMode = {ArraySchedulerMode.INTERACTIVE_I};
	private ArraySchedulerMode[] dynamicPassiveMode = {ArraySchedulerMode.DYNAMIC_PASSIVE_I};
	private ArraySchedulerMode[] simpleManualArrayMode = {ArraySchedulerMode.MANUAL_I};
	private ArrayImpl array;
	final private AutomaticArray retAutoArray = mock(AutomaticArray.class);
	final private ManualArrayCommand retManualArray = mock(ManualArrayCommand.class);
	final private QlDisplayManager retQlDispMan = mock(QlDisplayManager.class);
	final private StateArchive stateArchive = mock(StateArchive.class);
	final private StateEngine stateEngine = mock(StateEngine.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		checking(new Expectations() { {
			oneOf(contServices).getLogger(); will(returnValue(null));
			allowing(contServices).getLogger(); will(returnValue(acsLogger));
			allowing(acsLogger);
		}});
		array = new ArrayImpl();
		array.setLogger(logger);
		array.initialize(contServices);
	}




	public void testAutomaticArrayConfiguration() throws AcsJContainerServicesEx, InterruptedException {
		final AcsProvider provider = mock(AcsProvider.class);
		final ModelAccessor model = mock(ModelAccessor.class);
		final AcsNotificationChannel controlEventReceiver = mock(AcsNotificationChannel.class);
		checking(new Expectations() {{ 
			//oneOf(contServices).getComponent("CONTROL/Array001"); will(returnValue(retAutoArray));
			oneOf(contServices).getDefaultComponent("IDL:alma/pipelineql/QlDisplayManager:1.0"); will(returnValue(retQlDispMan));
			atLeast(1).of(provider).getModel(); will(returnValue(model));
			atLeast(1).of(model).getStateArchive(); will(returnValue(stateArchive));
			atLeast(1).of(model).getStateEngine(); will(returnValue(stateEngine));
			atLeast(1).of(provider).getControlEventReceiver(); will(returnValue(controlEventReceiver));
			atLeast(1).of(model).getObsProjectDao();
			atLeast(1).of(model).getSchedBlockDao();
			atLeast(1).of(provider).getControlArray();
			oneOf(controlEventReceiver).attach(with(equal("alma.Control.ExecBlockStartedEvent")), with(any(Executor.class)));
			oneOf(controlEventReceiver).attach(with(equal("alma.Control.ExecBlockEndedEvent")), with(any(Executor.class)));
			oneOf(controlEventReceiver).attach(with(equal("alma.offline.ASDMArchivedEvent")), with(any(Executor.class)));
			oneOf(controlEventReceiver).begin();
			oneOf(model).getSchedBlockFromEntityId("uid://X001/X002/X003");
			allowing(stateArchive);
			allowing(stateEngine);
		}});
		ArrayDescriptor descriptor = new ArrayDescriptor();
		descriptor.antennaIdList = new String[2];
		descriptor.antennaIdList[0] = "DV01"; descriptor.antennaIdList[1] = "DA41";
		descriptor.corrType = CorrelatorType.BL;
		descriptor.lifecycleType = ArraySchedulerLifecycleType.NORMAL;
		descriptor.policyName = "";
		descriptor.schedulingMode = ArrayModeEnum.INTERACTIVE;
		array.setServiceProvider(provider);
		array.configure("Array001", simpleAutoArrayMode, descriptor);
		SchedBlockQueueItem item = new SchedBlockQueueItem(System.currentTimeMillis(), "uid://X001/X002/X003");
		array.push(item);
		array.start("Scheduling", "MASTER_OF_THE_UNIVERSE");
		Thread.sleep(1000);
		array.stop("Scheduling", "MASTER_OF_THE_UNIVERSE");
	}
	
	public void testManualArrayConfiguration() throws AcsJContainerServicesEx, InterruptedException {
		final AcsProvider provider = mock(AcsProvider.class);
		final ModelAccessor model = mock(ModelAccessor.class);
		final AcsNotificationChannel controlEventReceiver = mock(AcsNotificationChannel.class);
		checking(new Expectations() {{ 
			//oneOf(contServices).getComponent("CONTROL/Array001"); will(returnValue(retAutoArray));
			oneOf(contServices).getDefaultComponent("IDL:alma/pipelineql/QlDisplayManager:1.0"); will(returnValue(retQlDispMan));
			atLeast(1).of(provider).getModel(); will(returnValue(model));
			atLeast(1).of(model).getStateArchive(); will(returnValue(stateArchive));
			atLeast(1).of(model).getStateEngine(); will(returnValue(stateEngine));
			atLeast(1).of(model).getObsProjectDao();
			atLeast(1).of(model).getSchedBlockDao();
			atLeast(1).of(provider).getControlArray();
			atLeast(1).of(provider).getControlEventReceiver(); will(returnValue(controlEventReceiver));
//			atLeast(1).of(provider).getControlEventReceiver(); will(returnValue(controlEventReceiver));
			oneOf(controlEventReceiver).attach(with(equal("alma.Control.ExecBlockStartedEvent")), with(any(Executor.class)));
			oneOf(controlEventReceiver).attach(with(equal("alma.Control.ExecBlockEndedEvent")), with(any(Executor.class)));
			oneOf(controlEventReceiver).attach(with(equal("alma.offline.ASDMArchivedEvent")), with(any(Executor.class)));
			oneOf(controlEventReceiver).begin();
			oneOf(model).getSchedBlockFromEntityId("uid://X001/X002/X003");
			allowing(stateArchive);
			allowing(stateEngine);
		}});
		ArrayDescriptor descriptor = new ArrayDescriptor();
		descriptor.antennaIdList = new String[2];
		descriptor.antennaIdList[0] = "DV01"; descriptor.antennaIdList[1] = "DA41";
		descriptor.corrType = CorrelatorType.BL;
		descriptor.lifecycleType = ArraySchedulerLifecycleType.NORMAL;
		descriptor.policyName = "";
		descriptor.schedulingMode = ArrayModeEnum.MANUAL;
		array.setServiceProvider(provider);
		array.configure("Array001", simpleManualArrayMode, descriptor);
		SchedBlockQueueItem item = new SchedBlockQueueItem(System.currentTimeMillis(), "uid://X001/X002/X003");
		array.push(item);
		array.start("Scheduling", "MASTER_OF_THE_UNIVERSE");
		Thread.sleep(1000);
		array.stop("Scheduling", "MASTER_OF_THE_UNIVERSE");
	}
	
	public void testDynamicArrayConfiguration() throws AcsJContainerServicesEx {
		final AcsProvider provider = mock(AcsProvider.class);
		final ModelAccessor model = mock(ModelAccessor.class);
		final AcsNotificationChannel controlEventReceiver = mock(AcsNotificationChannel.class);
		final DSASelector selector = mock(DSASelector.class);
		checking(new Expectations() {{ 
			//oneOf(contServices).getComponent("CONTROL/Array001"); will(returnValue(retAutoArray));
			oneOf(contServices).getDefaultComponent("IDL:alma/pipelineql/QlDisplayManager:1.0"); will(returnValue(retQlDispMan));
			atLeast(1).of(provider).getModel(); will(returnValue(model));
			atLeast(1).of(provider).getControlEventReceiver(); will(returnValue(controlEventReceiver));
			oneOf(controlEventReceiver).attach(with(equal("alma.Control.ExecBlockStartedEvent")), with(any(Executor.class)));
			oneOf(controlEventReceiver).attach(with(equal("alma.Control.ExecBlockEndedEvent")), with(any(Executor.class)));
			oneOf(controlEventReceiver).attach(with(equal("alma.offline.ASDMArchivedEvent")), with(any(Executor.class)));
			oneOf(controlEventReceiver).begin();
		}});
		ArrayDescriptor descriptor = new ArrayDescriptor();
		descriptor.antennaIdList = new String[2];
		descriptor.antennaIdList[0] = "DV01"; descriptor.antennaIdList[1] = "DA41";
		descriptor.corrType = CorrelatorType.BL;
		descriptor.lifecycleType = ArraySchedulerLifecycleType.NORMAL;
		descriptor.policyName = "TestPolicy";
		descriptor.schedulingMode = ArrayModeEnum.MANUAL;
		array.setServiceProvider(provider);
		array.setSelector(selector);
		array.configure("Array001", dynamicPassiveMode, descriptor);
		array.configureDynamicScheduler(descriptor.policyName);
	}
}
