/*
 *    ALMA - Atacama Large Millimiter Array
 *    (c) European Southern Observatory, 2002
 *    Copyright by ESO (in the framework of the ALMA collaboration)
 *    and Cosylab 2002, All rights reserved
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *    MA 02111-1307  USA
 */
package alma.scheduling.plugintest;

import java.util.Properties;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

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
import alma.exec.extension.subsystemplugin.PluginContainerException;
import alma.exec.extension.subsystemplugin.SessionProperties;
import alma.exec.extension.subsystemplugin.SubsystemPlugin;

import com.cosylab.CDB.DAL;

public class PluginContainerServices implements
		alma.exec.extension.subsystemplugin.PluginContainerServices {
	
	private ContainerServices acsCS;
	private SessionProperties properties;
	
	public PluginContainerServices(ContainerServices CS, Properties props) {
		if (CS==null || props==null) {
			throw new IllegalArgumentException("null ContainerServices or properties!!");
		}
		acsCS=CS;

		properties=new SessionProperties();
		for (String n : props.stringPropertyNames())
			properties.setProperty(n, props.getProperty(n));
	}
		
	public Logger getExecLogger () {
		return getLogger();
	}
	
	public String getProperty (String name) {
		String prop= properties.getProperty(name);
		if (prop==null) {
			return System.getProperty(name);
		} else {
			return prop;
		}
	}

	
	public void startChildPlugin(String childName, SubsystemPlugin child) throws PluginContainerException {
		try {
    			PluginStarter pluginStarter = new PluginStarter(childName,child, new Properties());
		} catch(Exception ex) {
			throw new PluginContainerException("Exception caught trying to start child plugin!");
		}
	}

	public void stopPlugin(SubsystemPlugin child) throws PluginContainerException {
		throw new PluginContainerException("Unsupported stopPlugin");
	}
	
	public SessionProperties getSessionProperties() {
		return properties;
	}
	
	 public String getName() {
		 return acsCS.getName();
	 }
	 
	 public ComponentStateManager getComponentStateManager() {
		 return acsCS.getComponentStateManager();
	 }

	 public AcsLogger getLogger() {
		 return acsCS.getLogger();
	 }
	 
	 public org.omg.CORBA.Object getComponent(String componentUrl)
     throws AcsJContainerServicesEx {
		return acsCS.getComponent(componentUrl);
	 }
	 
	 public org.omg.CORBA.Object getCollocatedComponent(String compUrl, String targetCompUrl) 
	 throws AcsJContainerServicesEx {
		 return acsCS.getCollocatedComponent(compUrl, targetCompUrl);
	 }
	 
	 public org.omg.CORBA.Object getCollocatedComponent(ComponentQueryDescriptor spec, boolean markAsDefaul, String targetCompUrl) 
	 throws AcsJContainerServicesEx {
		 return acsCS.getCollocatedComponent(spec,markAsDefaul,targetCompUrl);
	 }
	 
	 public org.omg.CORBA.Object getDynamicComponent(ComponentQueryDescriptor compSpec, boolean markAsDefault)
     throws AcsJContainerServicesEx {
		 return acsCS.getDynamicComponent(compSpec, markAsDefault);
	 }
	 
	 @SuppressWarnings("deprecation")
	public org.omg.CORBA.Object getDynamicComponent(ComponentSpec compSpec, boolean markAsDefault)
     throws AcsJContainerServicesEx {
		 return acsCS.getDynamicComponent(compSpec, markAsDefault);
	 }
	 
	 public DAL getCDB() throws AcsJContainerServicesEx {
		 return acsCS.getCDB();
	 }
	 
	 public String[] findComponents(String curlWildcard, String typeWildcard)
     throws AcsJContainerServicesEx {
		 return acsCS.findComponents(curlWildcard, typeWildcard);
	 }
	 
	 public ComponentDescriptor getComponentDescriptor(String componentUrl)
     throws AcsJContainerServicesEx {
		 return acsCS.getComponentDescriptor(componentUrl);
	 }
	 
	 public void releaseComponent(String componentUrl) {
		 acsCS.releaseComponent(componentUrl);
	 }
	 
	 public void registerComponentListener(ComponentListener listener) {
		 acsCS.registerComponentListener(listener);
	 }
	 
	 public OffShoot activateOffShoot(Servant cbServant)
     throws AcsJContainerServicesEx {
		 return acsCS.activateOffShoot(cbServant);
	 }
	 
	 public void deactivateOffShoot(Servant cbServant)
     throws AcsJContainerServicesEx {
		 acsCS.deactivateOffShoot(cbServant);
	 }
	 
	 public AdvancedContainerServices getAdvancedContainerServices() {
		 return acsCS.getAdvancedContainerServices();
	 }
	 
	 public ThreadFactory getThreadFactory() {
		 return acsCS.getThreadFactory();
	 }
	 
	 public <T> T getTransparentXmlComponent(
             Class<T> transparentXmlIF,
             org.omg.CORBA.Object componentReference,
             Class flatXmlIF)
             throws AcsJContainerServicesEx {
		 return acsCS.getTransparentXmlComponent(transparentXmlIF, componentReference, flatXmlIF);
	 }
	 
	 public void assignUniqueEntityId(EntityT entity) throws AcsJContainerServicesEx {
		 acsCS.assignUniqueEntityId(entity);
	 }
	 
	 public org.omg.CORBA.Object getDefaultComponent(String componentIDLType)
     throws AcsJContainerServicesEx {
		 return acsCS.getDefaultComponent(componentIDLType);
	 }
	 
	 public org.omg.CORBA.Object getComponentNonSticky(String curl)
     throws AcsJContainerServicesEx {
		 return acsCS.getComponentNonSticky(curl);
	 }
	 
		public org.omg.CORBA.Object getReferenceWithCustomClientSideTimeout(org.omg.CORBA.Object originalCorbaRef, double timeoutSeconds) throws AcsJContainerServicesEx {
			// once we are on Acs7.0.2 we can implement this by forwarding to acsCS   
			throw new AcsJContainerServicesEx(new UnsupportedOperationException("getReferenceWithCustomClientSideTimeout() not implemented at this time, please contact EXEC"));
		}	
	 
}
