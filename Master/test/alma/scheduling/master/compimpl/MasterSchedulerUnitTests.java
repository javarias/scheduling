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
package alma.scheduling.master.compimpl;

import java.util.logging.Logger;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.jmock.lib.legacy.ClassImposteriser;

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
import alma.acs.logging.AcsLogger;
import alma.scheduling.Array;
import alma.scheduling.ArrayDescriptor;
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
	ArraySchedulerMode[] simpleAutoArrayMode = {ArraySchedulerMode.INTERACTIVE_I};
	ArraySchedulerMode[] dynamicPassiveMode = {ArraySchedulerMode.DYNAMIC_PASSIVE_I};
	ArraySchedulerMode[] simpleManualArrayMode = {ArraySchedulerMode.MANUAL_I};
	MasterImpl schedMaster;
	AcsLogger logger;
	
	protected void setUp() throws Exception {
		super.setUp();
		setImposteriser(ClassImposteriser.INSTANCE);
		final ArrayStatusCallback callback = mock(ArrayStatusCallback.class);
		logger = mock(AcsLogger.class);
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
			ignoring(logger);
//			oneOf(contServices).getDefaultComponent("IDL:alma/Control/CurrentWeather:1.0");
		}});
	}
	
	public void testCreateAutomaticArray() throws ControlInternalExceptionEx, ACSInternalExceptionEx, SchedulingInternalExceptionEx, InaccessibleException, InvalidRequest, AcsJContainerServicesEx {
		final ArrayDescriptor details =  new ArrayDescriptor(antennas, photonics, CorrelatorType.NONE, ArrayModeEnum.INTERACTIVE, ArraySchedulerLifecycleType.NORMAL, null);
		checking(new Expectations() { {
			oneOf(controlMaster).createAutomaticArray(antennas, photonics,
					CorrelatorType.NONE); will(returnValue(new ArrayIdentifier("Array001", "CONTROL/Array001")));
			oneOf(retArray).configure("Array001", simpleAutoArrayMode , details);
//			oneOf(contServices).getDefaultComponent("IDL:alma/Control/CurrentWeather:1.0");
		} } );
		schedMaster.initialize(contServices);
		schedMaster.setLogger(logger);
		schedMaster.setControlMaster(controlMaster);
		schedMaster.createArray(details);
	}
	
	public void testCreateCSVAutomaticArray() throws ControlInternalExceptionEx, ACSInternalExceptionEx, SchedulingInternalExceptionEx, InaccessibleException, InvalidRequest {
		final ArrayDescriptor details =  new ArrayDescriptor(antennas, photonics, CorrelatorType.NONE, ArrayModeEnum.INTERACTIVE, ArraySchedulerLifecycleType.COMMISSIONING, null);
		checking(new Expectations() { {
			oneOf(controlMaster).createAutomaticArray(antennas, photonics,
					CorrelatorType.NONE); will(returnValue(new ArrayIdentifier("Array001", "CONTROL/Array001")));
			oneOf(retArray).configure("Array001", simpleAutoArrayMode , details);
		} } );
		schedMaster.initialize(contServices);
		schedMaster.setLogger(logger);
		schedMaster.setControlMaster(controlMaster);
		schedMaster.createArray(details);
	}
	
	public void testCreateManualArray() throws ControlInternalExceptionEx, ACSInternalExceptionEx, SchedulingInternalExceptionEx, InaccessibleException, InvalidRequest {
		final ArrayDescriptor details =  new ArrayDescriptor(antennas, photonics, CorrelatorType.NONE, ArrayModeEnum.MANUAL, ArraySchedulerLifecycleType.NORMAL, null);
		checking( new Expectations() { { 
			oneOf(controlMaster).createManualArray(antennas, photonics,
					CorrelatorType.NONE); will(returnValue(new ArrayIdentifier("Array001", "CONTROL/Array001")));
			oneOf(retArray).configure("Array001", simpleManualArrayMode , details);
		} });
		schedMaster.initialize(contServices);
		schedMaster.setLogger(logger);
		schedMaster.setControlMaster(controlMaster);
		schedMaster.createArray(details);
	}
	
	public void testCreateDynamicArray() throws ControlInternalExceptionEx, ACSInternalExceptionEx, SchedulingInternalExceptionEx, InaccessibleException, InvalidRequest, AcsJContainerServicesEx {
		final ArrayDescriptor details =  new ArrayDescriptor(antennas, photonics, CorrelatorType.NONE, ArrayModeEnum.DYNAMIC, ArraySchedulerLifecycleType.NORMAL, "DefaultPolicy");
		checking(new Expectations() { {
			oneOf(controlMaster).createAutomaticArray(antennas, photonics,
					CorrelatorType.NONE); will(returnValue(new ArrayIdentifier("Array001", "CONTROL/Array001")));
			oneOf(retArray).configure("Array001", dynamicPassiveMode , details);
			oneOf(retArray).configureDynamicScheduler("DefaultPolicy");
		} } );
		schedMaster.initialize(contServices);
		schedMaster.setLogger(logger);
		schedMaster.setControlMaster(controlMaster);
		schedMaster.createArray(details);
	}
	
	public void testCreateManualCSVArray() throws ControlInternalExceptionEx, ACSInternalExceptionEx, SchedulingInternalExceptionEx, InaccessibleException, InvalidRequest {
		final ArrayDescriptor details =  new ArrayDescriptor(antennas, photonics, CorrelatorType.NONE, ArrayModeEnum.MANUAL, ArraySchedulerLifecycleType.COMMISSIONING, null);
		checking( new Expectations() { { 
			oneOf(controlMaster).createManualArray(antennas, photonics,
					CorrelatorType.NONE); will(returnValue(new ArrayIdentifier("Array001", "CONTROL/Array001")));
			oneOf(retArray).configure("Array001", simpleManualArrayMode, details);
		} });
		schedMaster.initialize(contServices);
		schedMaster.setLogger(logger);
		schedMaster.setControlMaster(controlMaster);
		schedMaster.createArray(details);
	}
	
	public void testDestroyArray() throws InaccessibleException, InvalidRequest, ControlInternalExceptionEx, ACSInternalExceptionEx, SchedulingInternalExceptionEx, AcsJContainerServicesEx {
		final ArrayDescriptor details =  new ArrayDescriptor(antennas, photonics, CorrelatorType.NONE, ArrayModeEnum.INTERACTIVE, ArraySchedulerLifecycleType.NORMAL, null);
		checking(new Expectations() { {
			oneOf(controlMaster).createAutomaticArray(antennas, photonics,
					CorrelatorType.NONE); will(returnValue(new ArrayIdentifier("Array001", "CONTROL/Array001")));
			oneOf(retArray).configure("Array001", simpleAutoArrayMode, details);
			oneOf(retArray).stop(with(any(String.class)), with(any(String.class)));
			oneOf(retArray).stopRunningSchedBlock(with(any(String.class)), with(any(String.class)));
			oneOf(retArray).destroyArray("Array001", "Scheduling");
			oneOf(controlMaster).destroyArray("Array001");
			oneOf(retArray).hasRunningSchedBlock(); will(returnValue(false));
		} } );
		schedMaster.initialize(contServices);
		schedMaster.setLogger(logger);
		schedMaster.setControlMaster(controlMaster);
		schedMaster.createArray(details);
		schedMaster.destroyArray("Array001", "Array001", "Scheduling");
	}
}
