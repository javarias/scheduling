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
import alma.ACS.OffShootOperations;
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

public class PluginContainerServices extends alma.acs.container.ContainerServicesProxy implements
		alma.exec.extension.subsystemplugin.PluginContainerServices {
	
	private ContainerServices acsCS;
	private SessionProperties properties;
	
	public PluginContainerServices(ContainerServices CS, Properties props) {
	    super(CS);
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
		for (Object prop : System.getProperties().keySet()) {
			try {
				String value = System.getProperty((String)prop);
				System.out.format("%s -> %s%n", prop, value);
			} catch (ClassCastException c) {
			}
		}
		try {
			Properties props = new Properties();
			//props.setProperty("array.name", childName);
			//props.setProperty("user.name", System.getProperty("username"));
			//props.setProperty("user.role", childName);
			PluginStarter pluginStarter = new PluginStarter(childName, child, props);
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new PluginContainerException("Exception caught trying to start child plugin!");
		}
	}

	public void stopPlugin(SubsystemPlugin child) throws PluginContainerException {
		throw new PluginContainerException("Unsupported stopPlugin");
	}
	
	public SessionProperties getSessionProperties() {
		return properties;
	}	
	 
}
