package alma.scheduling.master.compimpl;

import org.omg.CORBA.Object;

import alma.Control.CorrelatorType;
import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.SchedulingMasterExceptions.ACSInternalExceptionEx;
import alma.SchedulingMasterExceptions.ControlInternalExceptionEx;
import alma.SchedulingMasterExceptions.SchedulingInternalExceptionEx;
import alma.acs.component.client.ComponentClientTestCase;
import alma.scheduling.ArrayCreationInfo;
import alma.scheduling.ArrayModeEnum;
import alma.scheduling.ArraySchedulerLifecycleType;
import alma.scheduling.Master;
import alma.scheduling.MasterHelper;

public class MasterTest extends ComponentClientTestCase {
	
	public MasterTest() throws Exception {
		super("Scheduling Master Test");
	}
	
//	public void testMasterCreation() throws AcsJContainerServicesEx{
//		Master master = null;
//		Object obj = getContainerServices().getComponent(Constants.SCHEDULING_MASTER_URL);
//		assertNotNull(obj);
//		master = MasterHelper.narrow(obj);
//		assertNotNull(master);
//		getContainerServices().releaseComponent(Constants.SCHEDULING_MASTER_URL);
//	}
	
	public void testArrayCreation() throws AcsJContainerServicesEx, ControlInternalExceptionEx, SchedulingInternalExceptionEx, ACSInternalExceptionEx{
		Object obj = getContainerServices().getComponent(Constants.SCHEDULING_MASTER_URL);
		Master master = MasterHelper.narrow(obj);
		String[] antennas = {"DV01", "DA41"};
		String[] photonics = {};
		ArrayCreationInfo arrayInfo = master.createArray(antennas, photonics, CorrelatorType.NONE, 
				ArrayModeEnum.DYNAMIC, ArraySchedulerLifecycleType.NORMAL);
		assertEquals("Array001", arrayInfo.arrayId);
		assertEquals("SCHEDULING/Array001", arrayInfo.arrayComponentName);
		master.destroyArray(arrayInfo.arrayId);
		getContainerServices().releaseComponent(Constants.SCHEDULING_MASTER_URL);
	}
	
	public void testArrayDestruction() throws AcsJContainerServicesEx, ControlInternalExceptionEx, SchedulingInternalExceptionEx, ACSInternalExceptionEx{
		Object obj = getContainerServices().getComponent(Constants.SCHEDULING_MASTER_URL);
		Master master = MasterHelper.narrow(obj);
		String[] antennas = {"DV01", "DA41"};
		String[] photonics = {};
		ArrayCreationInfo arrayInfo = master.createArray(antennas, photonics, CorrelatorType.NONE, 
				ArrayModeEnum.DYNAMIC, ArraySchedulerLifecycleType.NORMAL);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) { }
		finally {
			m_logger.fine("Destroying Array");
			master.destroyArray(arrayInfo.arrayId);
			getContainerServices().releaseComponent(Constants.SCHEDULING_MASTER_URL);
		}
	}

}
