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
 * File ArchiveSimulator.java
 */
 
package alma.scheduling.simulator;

import alma.entity.xmlbinding.obsproject.ObsProject;
import alma.entity.xmlbinding.pipelineprocessingrequest.PipelineProcessingRequest;
import alma.entity.xmlbinding.schedblock.SchedBlock;
import alma.entity.xmlbinding.schedulingpolicy.SchedulingPolicy;
import alma.scheduling.define.STime;
import alma.scheduling.master_scheduler.ArchiveProxy;

import alma.Control.ExecBlockEvent;

/**
 * Description 
 * 
 * @version 1.00  Jul 18, 2003
 * @author Allen Farris
 */
public class ArchiveSimulator extends BasicComponent implements ArchiveProxy {

	/**
	 * @param mode
	 */
	public ArchiveSimulator(Mode mode) {
		super(mode);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	public ArchiveSimulator() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.ArchiveProxy#getSchedBlock()
	 */
	public SchedBlock[] getSchedBlock() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.ArchiveProxy#getNewSchedBlock(alma.scheduling.define.STime)
	 */
	public SchedBlock[] getNewSchedBlock(STime time) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.ArchiveProxy#getSchedBlock(java.lang.String)
	 */
	public SchedBlock getSchedBlock(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.ArchiveProxy#updateSchedBlock(alma.entity.xmlbinding.schedblock.SchedBlock)
	 */
	public void updateSchedBlock(ExecBlockEvent execblockevent) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.ArchiveProxy#getProject()
	 */
	public ObsProject[] getProject() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.ArchiveProxy#getProject(java.lang.String)
	 */
	public ObsProject getProject(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.ArchiveProxy#updateProject(alma.entity.xmlbinding.obsproject.ObsProject)
	 */
	public void updateProject(ObsProject p) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.ArchiveProxy#getSchedulingPolicy()
	 */
	public SchedulingPolicy[] getSchedulingPolicy() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.ArchiveProxy#getPipelineProcessingRequest()
	 */
	public PipelineProcessingRequest[] getPipelineProcessingRequest() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.ArchiveProxy#getPipelineProcessingRequest(java.lang.String)
	 */
	public PipelineProcessingRequest getPipelineProcessingRequest(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.ArchiveProxy#addPipelineProcessingRequest(alma.entity.xmlbinding.pipelineprocessingrequest.PipelineProcessingRequest)
	 */
	public void addPipelineProcessingRequest(PipelineProcessingRequest ppr) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.ArchiveProxy#updatePipelineProcessingRequest(alma.entity.xmlbinding.pipelineprocessingrequest.PipelineProcessingRequest)
	 */
	public void updatePipelineProcessingRequest(PipelineProcessingRequest ppr) {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) {
		System.out.println("Unit test of archive simulator.");
		ArchiveSimulator archive = new ArchiveSimulator(Mode.FULL);
		System.out.println("End unit test of archive simulator.");
	}
}
