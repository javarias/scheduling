/*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 * File Pipeline.java
 *
 */
package alma.scheduling.Define;

import alma.xmlentity.XmlEntityStruct;
import alma.entity.xmlbinding.pipelineprocessingrequest.*;
/**
 * This class is an interface to the pipeline subsystem.
 * @author Sohaila Lucero
 */
public interface Pipeline {
    /**
     * Creates a pipeline processing request to give to the pipeline
     * subsystem
     */
//    public XmlEntityStruct createPipelineProcessingRequest() 
  //      throws SchedulingException;

    
    /** 
     * Start the pipeline to process the given entity.
     */
    public String processRequest(XmlEntityStruct ppr) 
        throws SchedulingException;
}
