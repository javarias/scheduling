/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
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
 * File ExecutiveSimulator.java
 */
 
package alma.scheduling.simulator;

import alma.acs.component.ComponentLifecycleException;
import alma.acs.container.ContainerException;
import alma.scheduling.master_scheduler.MasterScheduler;

/**
 * Description 
 * 
 * @version 1.00  May 29, 2003
 * @author Allen Farris
 */
public class ExecutiveSimulator extends BasicComponent {
	
	// Components
	private ArchiveSimulator archive;
	private ControlSimulator control;
	private PipelineSimulator pipeline;
	private CalibrationSimulator calibration;
	private MasterScheduler masterScheduler;
	
	
	/**
	 * 
	 */
	public ExecutiveSimulator(Mode mode) {
		super(mode);
	}

	/* (non-Javadoc)
	 * @see alma.acs.component.ComponentLifecycle#execute()
	 */
	public void execute() {
		try {
			// Activate the archive.
			archive = (ArchiveSimulator)m_containerServices.getComponent(Container.ARCHIVE);
			archive.initialize();
			archive.execute();
			// Activate control.
			control = (ControlSimulator)m_containerServices.getComponent(Container.CONTROL);
			control.initialize();
			control.execute();
			// Active the pipeline.
			pipeline = (PipelineSimulator)m_containerServices.getComponent(Container.PIPELINE);
			pipeline.initialize();
			pipeline.execute();
			// Activate the MasterScheduler;
			masterScheduler = (MasterScheduler)m_containerServices.getComponent(Container.MASTER_SCHEDULER);
			//masterScheduler.initialize();
			//masterScheduler.execute();
		} catch (ComponentLifecycleException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (ContainerException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		// Set up listeners.
		
		// OK, we're ready to start scheduling.
		//try {
		//	masterScheduler.startScheduling(null);
		//} catch (InvalidOperation e) {
		//	e.printStackTrace();
		//	System.exit(0);
		//}
		m_logger.info("SCHEDULING: executive.executing");
	}

	public static void main(String[] args) {
		System.out.println("Unit test of executive simulator.");
		ExecutiveSimulator executive = new ExecutiveSimulator(Mode.FULL);
		System.out.println("End unit test of executive simulator.");
	}
}
