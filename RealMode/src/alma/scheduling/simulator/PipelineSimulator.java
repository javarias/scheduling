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
 * File PipelineSimulator.java
 */
 
package alma.scheduling.simulator;

import alma.entity.xmlbinding.pipelineprocessingrequest.PipelineProcessingRequest;
import alma.scheduling.master_scheduler.SchedulingException;
import alma.scheduling.project_manager.PipelineProxy;
import alma.scheduling.project_manager.PipelineStatus;

/**
 * Description 
 * 
 * @version 1.00  Jul 18, 2003
 * @author Allen Farris
 */
public class PipelineSimulator
	extends BasicComponent
	implements PipelineProxy {

	/**
	 * @param mode
	 */
	public PipelineSimulator(Mode mode) {
		super(mode);
	}

	/**
	 * 
	 */
	public PipelineSimulator() {
		super(Mode.FULL);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.project_manager.PipelineProxy#processRequest(alma.entity.xmlbinding.pipelineprocessingrequest.PipelineProcessingRequest)
	 */
	public String processRequest(PipelineProcessingRequest request)
		throws SchedulingException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.project_manager.PipelineProxy#getStatus(java.lang.String)
	 */
	public PipelineStatus getStatus(String pipelineProcessingId) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) {
		System.out.println("Unit test of pipeline simulator.");
		PipelineSimulator pipeline = new PipelineSimulator(Mode.FULL);
		System.out.println("End unit test of pipeline simulator.");
	}
}
