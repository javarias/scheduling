package alma.scheduling.master.compimpl;

import java.util.logging.Logger;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;

import si.ijs.maci.ComponentInfo;
import alma.Control.ArrayIdentifier;
import alma.Control.ControlMaster;
import alma.Control.CorrelatorType;
import alma.Control.InaccessibleException;
import alma.Control.InvalidRequest;
import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.SchedulingMasterExceptions.ACSInternalExceptionEx;
import alma.SchedulingMasterExceptions.ControlInternalExceptionEx;
import alma.SchedulingMasterExceptions.SchedulingInternalExceptionEx;
import alma.acs.component.ComponentDescriptor;
import alma.acs.component.ComponentQueryDescriptor;
import alma.acs.container.ContainerServices;
import alma.scheduling.Array;
import alma.scheduling.ArrayModeEnum;
import alma.scheduling.ArraySchedulerLifecycleType;
import alma.scheduling.ArraySchedulerMode;
import alma.scheduling.ArrayStatusCallback;

public class MasterSchedulerUnitTests extends MockObjectTestCase {
	private ControlMaster controlMaster = mock(ControlMaster.class);
	private ContainerServices contServices = mock(ContainerServices.class);
	String[] antennas = {"DV01", "DA41"};
	String[] photonics = {};
	final Array retArray = mock(Array.class);
	ArraySchedulerMode[] simpleAutoArrayMode = {ArraySchedulerMode.DYNAMIC_ACTIVE_I};
	ArraySchedulerMode[] simpleManualArrayMode = {ArraySchedulerMode.MANUAL_I};
	MasterImpl schedMaster;
	Logger logger;
	
	protected void setUp() throws Exception {
		super.setUp();
//		setImposteriser(ClassImposteriser.INSTANCE);
		final ArrayStatusCallback callback = mock(ArrayStatusCallback.class);
		logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		schedMaster = new MasterImpl();
		schedMaster.setControlMaster(controlMaster);
		schedMaster.setLogger(logger);
		schedMaster.addMonitorMaster("MyTestMonitor", callback);
		checking(new Expectations(){ {
			atLeast(1).of(controlMaster).getMasterState(); will(returnValue(alma.Control.SystemState.OPERATIONAL));
			oneOf(contServices).getCollocatedComponent(with(any(ComponentQueryDescriptor.class)), with(any(boolean.class)), with(any(String.class))); will(returnValue(retArray));
			oneOf(contServices).getComponentDescriptor(with(any(String.class))); will(returnValue(new ComponentDescriptor(new ComponentInfo())));
			allowing(contServices).getComponent(with(any(String.class))); will(returnValue(retArray));
			allowing(callback);
			ignoring(contServices);
		}});
	}
	
	public void testCreateAutomaticArray() throws ControlInternalExceptionEx, ACSInternalExceptionEx, SchedulingInternalExceptionEx, InaccessibleException, InvalidRequest, AcsJContainerServicesEx {
		checking(new Expectations() { {
			oneOf(controlMaster).createAutomaticArray(antennas, photonics,
					CorrelatorType.NONE); will(returnValue(new ArrayIdentifier("Array001", "CONTROL/Array001")));
			oneOf(retArray).configure("Array001", simpleAutoArrayMode , ArraySchedulerLifecycleType.NORMAL);
			oneOf(contServices).getDefaultComponent("IDL:alma/Control/CurrentWeather:1.0");
		} } );
		schedMaster.initialize(contServices);
		schedMaster.setLogger(logger);
		schedMaster.setControlMaster(controlMaster);
		schedMaster.createArray(antennas, photonics, CorrelatorType.NONE, ArrayModeEnum.DYNAMIC, ArraySchedulerLifecycleType.NORMAL, null);
	}
	
	public void testCreateCSVAutomaticArray() throws ControlInternalExceptionEx, ACSInternalExceptionEx, SchedulingInternalExceptionEx, InaccessibleException, InvalidRequest {
		checking(new Expectations() { {
			oneOf(controlMaster).createAutomaticArray(antennas, photonics,
					CorrelatorType.NONE); will(returnValue(new ArrayIdentifier("Array001", "CONTROL/Array001")));
			oneOf(retArray).configure("Array001", simpleAutoArrayMode , ArraySchedulerLifecycleType.COMMISSIONING);
		} } );
		schedMaster.initialize(contServices);
		schedMaster.setLogger(logger);
		schedMaster.setControlMaster(controlMaster);
		schedMaster.createArray(antennas, photonics, CorrelatorType.NONE, ArrayModeEnum.DYNAMIC, ArraySchedulerLifecycleType.COMMISSIONING, null);
	}
	
	public void testCreateManualArray() throws ControlInternalExceptionEx, ACSInternalExceptionEx, SchedulingInternalExceptionEx, InaccessibleException, InvalidRequest {
		checking( new Expectations() { { 
			oneOf(controlMaster).createManualArray(antennas, photonics,
					CorrelatorType.NONE); will(returnValue(new ArrayIdentifier("Array001", "CONTROL/Array001")));
			oneOf(retArray).configure("Array001", simpleManualArrayMode , ArraySchedulerLifecycleType.NORMAL);
		} });
		schedMaster.initialize(contServices);
		schedMaster.setLogger(logger);
		schedMaster.setControlMaster(controlMaster);
		schedMaster.createArray(antennas, photonics, CorrelatorType.NONE, ArrayModeEnum.MANUAL, ArraySchedulerLifecycleType.NORMAL, null);
	}
	
	public void testCreateManualCSVArray() throws ControlInternalExceptionEx, ACSInternalExceptionEx, SchedulingInternalExceptionEx, InaccessibleException, InvalidRequest {
		checking( new Expectations() { { 
			oneOf(controlMaster).createManualArray(antennas, photonics,
					CorrelatorType.NONE); will(returnValue(new ArrayIdentifier("Array001", "CONTROL/Array001")));
			oneOf(retArray).configure("Array001", simpleManualArrayMode , ArraySchedulerLifecycleType.COMMISSIONING);
		} });
		schedMaster.initialize(contServices);
		schedMaster.setLogger(logger);
		schedMaster.setControlMaster(controlMaster);
		schedMaster.createArray(antennas, photonics, CorrelatorType.NONE, ArrayModeEnum.MANUAL, ArraySchedulerLifecycleType.COMMISSIONING, null);
	}
	
	public void testDestroyArray() throws InaccessibleException, InvalidRequest, ControlInternalExceptionEx, ACSInternalExceptionEx, SchedulingInternalExceptionEx, AcsJContainerServicesEx {
		checking(new Expectations() { {
			oneOf(controlMaster).createAutomaticArray(antennas, photonics,
					CorrelatorType.NONE); will(returnValue(new ArrayIdentifier("Array001", "CONTROL/Array001")));
			oneOf(retArray).configure("Array001", simpleAutoArrayMode , ArraySchedulerLifecycleType.NORMAL);
			oneOf(retArray).stop(with(any(String.class)), with(any(String.class)));
			oneOf(retArray).stopRunningSchedBlock(with(any(String.class)), with(any(String.class)));
			oneOf(retArray).destroyArray("Array001", "Scheduling");
			oneOf(controlMaster).destroyArray("Array001");
		} } );
		schedMaster.initialize(contServices);
		schedMaster.setLogger(logger);
		schedMaster.setControlMaster(controlMaster);
		schedMaster.createArray(antennas, photonics, CorrelatorType.NONE, ArrayModeEnum.DYNAMIC, ArraySchedulerLifecycleType.NORMAL, null);
		schedMaster.destroyArray("Array001", "Array001", "Scheduling");
	}
}
