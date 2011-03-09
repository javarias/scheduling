package alma.scheduling.inttest;

import java.util.logging.Logger;
import java.util.concurrent.ThreadFactory;

import org.omg.CORBA.Object;
import org.omg.PortableServer.Servant;

import si.ijs.maci.ComponentSpec;
import alma.ACS.OffShoot;
import alma.ACS.OffShootOperations;
import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.component.ComponentDescriptor;
import alma.acs.component.ComponentQueryDescriptor;
import alma.acs.component.ComponentStateManager;
import alma.acs.container.AdvancedContainerServices;
import alma.acs.container.ContainerServices;
import alma.acs.logging.AcsLogger;
import alma.entities.commonentity.EntityT;

import com.cosylab.CDB.DAL;

public class FakeContainerServices extends alma.acs.container.testsupport.DummyContainerServices {

	private static int id = 1;

	FakeContainerServices(String name, Logger logger) {
	    super(name, logger);
	}

	public void assignUniqueEntityId(EntityT entity)
			throws AcsJContainerServicesEx {
		entity.setEntityId(String.format("%s %2H ", entity.getEntityTypeName(),
				id++));
	}

}
