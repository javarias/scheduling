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
 * File ArchiveProxy.java
 */
 
package alma.scheduling.master_scheduler;

import alma.Control.ExecBlockEvent;
import alma.entity.xmlbinding.schedblock.SchedBlock;
import alma.entity.xmlbinding.obsproject.ObsProject;
import alma.entity.xmlbinding.schedulingpolicy.SchedulingPolicy;
import alma.entity.xmlbinding.pipelineprocessingrequest.PipelineProcessingRequest;

import alma.scheduling.define.STime;
/**
 * Description 
 * 
 * @version 1.00  Jun 3, 2003
 * @author Allen Farris
 */
public interface ArchiveProxy {
	SchedBlock[] getSchedBlock();
	SchedBlock[] getNewSchedBlock(STime time);
	SchedBlock getSchedBlock(String id);
//	void updateSchedBlock(SchedBlock sb);
	void updateSchedBlock(ExecBlockEvent execblockevent);

	ObsProject[] getProject();
	ObsProject getProject(String id);
	void updateProject(ObsProject p);

	SchedulingPolicy[] getSchedulingPolicy();

	PipelineProcessingRequest[] getPipelineProcessingRequest();
	PipelineProcessingRequest getPipelineProcessingRequest(String id);
	void addPipelineProcessingRequest(PipelineProcessingRequest ppr);
	void updatePipelineProcessingRequest(PipelineProcessingRequest ppr);

}
