/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 * File Container.java
 */
 
package ALMA.scheduling.simulator;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.XMLFormatter;
import java.util.logging.Level;
import org.omg.CORBA.Object;

import alma.acs.container.ContainerException;
import alma.acs.container.ContainerServices;
import alma.entities.commonentity.EntityT;
import ALMA.ACS.OffShoot;

import ALMA.scheduling.master_scheduler.MasterScheduler;

/**
 * Description 
 * 
 * @version 1.00  May 29, 2003
 * @author Allen Farris
 */
public class Container implements ContainerServices {
	static public final String ARCHIVE = "archive";
	static public final String CONTROL = "control";
	static public final String PIPELINE = "pipeline";
	static public final String CALIBRATION = "calibration";
	static public final String EXECUTIVE = "executive";
	static public final String MASTER_SCHEDULER = "master_scheduler";

	private Mode mode;
	
	private ArchiveSimulator archive;
	private ControlSimulator control;
	private PipelineSimulator pipeline;
	private CalibrationSimulator calibration;
	private ExecutiveSimulator executive;
	private MasterScheduler masterScheduler;
	
	private Logger logger;
	private FileHandler logfile;

	/**
	 * 
	 */
	public Container(Mode mode) {
		try {
			this.mode = mode;
			
			// Create the components.
			archive = new ArchiveSimulator (mode);
			executive = new ExecutiveSimulator (mode);
			pipeline = new PipelineSimulator (mode);
			calibration = new CalibrationSimulator (mode);
			control = new ControlSimulator (mode);
			masterScheduler = new MasterScheduler (true);
			
			// Create the logger. (We may have to make these names parms.)
			logger = Logger.getLogger("schedulingSimulator");
			try {
				logfile = new FileHandler("log.xml");
			} catch (IOException ioerr) {
				 ioerr.printStackTrace();
				 System.exit(0);
			}
			XMLFormatter formatter = new XMLFormatter();
			logfile.setFormatter(formatter);
			logger.setLevel(Level.FINEST);
			logger.addHandler(logfile);
			// All messages will be recorded and the logfile will be an XML file.
			
			// Set the component names.
			archive.setComponentName(ARCHIVE);
			executive.setComponentName(EXECUTIVE);
			pipeline.setComponentName(PIPELINE);
			calibration.setComponentName(CALIBRATION);
			control.setComponentName(CONTROL);
			masterScheduler.setComponentName(MASTER_SCHEDULER);
			
			// Set the component's container services.
			archive.setContainerServices(this);
			executive.setContainerServices(this);
			pipeline.setContainerServices(this);
			calibration.setContainerServices(this);
			control.setContainerServices(this);
			masterScheduler.setContainerServices(this);
			
		} catch (IllegalArgumentException err) {
			// This is basic stuff and if anything goes wrong, we stop.
			err.printStackTrace();
			System.exit(0);
		}
	}
	
	public void cleanUp() {
		logger.info("SCHEDULING: The logfile is being closed.");
		logfile.close();
	}

	/* (non-Javadoc)
	 * @see alma.acs.container.ContainerServices#getLogger()
	 */
	public Logger getLogger() {
		return logger;
	}

	/* (non-Javadoc)
	 * @see alma.acs.container.ContainerServices#assignUniqueEntityId(alma.entities.commonentity.EntityT)
	 */
	public void assignUniqueEntityId(EntityT entity)
		throws ContainerException {
			// Not sure what to do about this.
	}

	/* (non-Javadoc)
	 * @see alma.acs.container.ContainerServices#getComponent(java.lang.String)
	 */
	public Object getComponent(String componentUrl) throws ContainerException {
		if (componentUrl.equals(ARCHIVE))
			return archive;
		else if (componentUrl.equals(CONTROL))
			return control;
		else if (componentUrl.equals(PIPELINE))
			return pipeline;
		else if (componentUrl.equals(CALIBRATION))
			return calibration;
		else if (componentUrl.equals(EXECUTIVE))
			return executive;
		else if (componentUrl.equals(MASTER_SCHEDULER))
			return masterScheduler;
		throw new ContainerException("No such component as " + componentUrl);
	}

	/* (non-Javadoc)
	 * @see alma.acs.container.ContainerServices#releaseComponent(java.lang.String)
	 */
	public void releaseComponent(String componentUrl) {
	}

	public String[] findComponents(String curlWildcard, String typeWildcard)  throws ContainerException {
		return null;
	}

	public OffShoot activateOffShoot(org.omg.PortableServer.Servant cbServant) 
			throws ContainerException {
				return null;
	}
	

	/**
	 * Encapsulates {@link org.omg.CORBA.ORB#object_to_string(org.omg.CORBA.Object)}.
	 * @param objRef the corba stub 
	 * @return standardized string representation of <code>objRef</code>. 
	 */
	public String corbaObjectToString(org.omg.CORBA.Object objRef) {
		return null;
	}

	/**
	 * Encapsulates {@link org.omg.CORBA.ORB#string_to_object(String)}.
	 * @param strObjRef
	 * @return org.omg.CORBA.Object
	 */
	public org.omg.CORBA.Object corbaObjectFromString(String strObjRef) {
		return null;
	}

	/* (non-Javadoc)
	 * @see alma.acs.container.ContainerServices#createXmlBindingWrapper(java.lang.Class, org.omg.CORBA.Object, java.lang.Class)
	 */
	public java.lang.Object createXmlBindingWrapper(
		Class componentInterface,
		Object componentReference,
		Class corbaOperationsIF)
		throws ContainerException {
		return null;
	}

    public com.cosylab.CDB.DAL getCDB() {
        return null;
    }


	public static void main(String[] args) {
		System.out.println("Unit test of container.");
		Container container = new Container (Mode.FULL);
		System.out.println("End unit test of container.");
	}
}
