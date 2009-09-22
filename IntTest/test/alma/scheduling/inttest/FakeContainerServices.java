package alma.scheduling.inttest;

import java.util.concurrent.ThreadFactory;

import org.omg.CORBA.Object;
import org.omg.PortableServer.Servant;

import si.ijs.maci.ComponentSpec;
import alma.ACS.OffShoot;
import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.component.ComponentDescriptor;
import alma.acs.component.ComponentQueryDescriptor;
import alma.acs.component.ComponentStateManager;
import alma.acs.container.AdvancedContainerServices;
import alma.acs.container.ContainerServices;
import alma.acs.logging.AcsLogger;
import alma.entities.commonentity.EntityT;

import com.cosylab.CDB.DAL;

public class FakeContainerServices implements ContainerServices {

	private static int id = 1;
	
	public void assignUniqueEntityId(EntityT entity) throws AcsJContainerServicesEx {
		entity.setEntityId(String.format("%s %2H ", entity.getEntityTypeName(), id++));
	}


	public String[] findComponents(String curlWildcard, String typeWildcard)
			throws AcsJContainerServicesEx {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getCollocatedComponent(String compUrl, String targetCompUrl)
			throws AcsJContainerServicesEx {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getCollocatedComponent(ComponentQueryDescriptor compSpec,
			boolean markAsDefaul, String targetCompUrl)
			throws AcsJContainerServicesEx {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getComponent(String componentUrl)
			throws AcsJContainerServicesEx {
		// TODO Auto-generated method stub
		return null;
	}

	public ComponentDescriptor getComponentDescriptor(String componentUrl)
			throws AcsJContainerServicesEx {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getComponentNonSticky(String curl)
			throws AcsJContainerServicesEx {
		// TODO Auto-generated method stub
		return null;
	}

	public ComponentStateManager getComponentStateManager() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getDefaultComponent(String componentIDLType)
			throws AcsJContainerServicesEx {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getDynamicComponent(ComponentQueryDescriptor compSpec,
			boolean markAsDefault) throws AcsJContainerServicesEx {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getDynamicComponent(ComponentSpec compSpec,
			boolean markAsDefault) throws AcsJContainerServicesEx {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getReferenceWithCustomClientSideTimeout(
			Object originalCorbaRef, double timeoutSeconds)
			throws AcsJContainerServicesEx {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> T getTransparentXmlComponent(Class<T> transparentXmlIF,
			Object componentReference, Class flatXmlIF)
			throws AcsJContainerServicesEx {
		// TODO Auto-generated method stub
		return null;
	}

	public void registerComponentListener(ComponentListener listener) {
		// TODO Auto-generated method stub

	}

	public void releaseComponent(String componentUrl) {
		// TODO Auto-generated method stub

	}

	public String getName() {return null;};
	public AcsLogger getLogger() {return null;};
	public DAL getCDB() throws AcsJContainerServicesEx {return null;};
	public OffShoot activateOffShoot(Servant cbServant) 
		throws AcsJContainerServicesEx {return null;};
	public void deactivateOffShoot(Servant cbServant) 
		throws AcsJContainerServicesEx {}; 
    public AdvancedContainerServices getAdvancedContainerServices() {return null;};
    public ThreadFactory getThreadFactory() {return null;};


}
