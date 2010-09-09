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
 * "@(#) $Id: SchedBlockExecutor.java,v 1.4 2010/09/09 18:07:11 javarias Exp $"
 */
package alma.scheduling.algorithm;

import java.util.Date;

import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;

public interface SchedBlockExecutor {

    public static double FUDGE_FACTOR = 0.8;
    
    /**
     * Executes the SchedBlock at the given time.
     * 
     * @param sb Scheduling Block
     * @param ut Point in time when the SchedBlock is executed
     * @return Point in time after the SchedBlock has been executed.
     */
    Date execute(SchedBlock sb, ArrayConfiguration arrCnf, Date ut);
    
    /**
     * 
     * @param sb Scheduling Block
     * @param ut Point in time when the SchedBlock is executed
     * @return Point in time after the SchedBlock has been executed.
     */
    void finishSbExecution(SchedBlock sb, ArrayConfiguration arrCnf, Date ut);
}
