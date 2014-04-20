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
 */
package alma.scheduling.algorithm.sbselection;

import java.util.Collection;
import java.util.Date;

import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;

/**
 * This interface generalizes the selection of a set of SchedBlocks, an
 * operation needed by ModelUpdaters and SchedBlockRankers.
 *
 */
public interface SchedBlockSelector {

    /**
     * Selects a set of SchedBlocks for a given point in time for a given array.
     * If the queries underlying the selection don't require the point in time
     * or the array, these parameters
     * are ignored.
     * 
     * @param ut Time (UTC), it can be null if the implementation doesn't use this parameter.
     * @param arrConf the array it can be null if the implementation doesn't use this parameter.
     * @return the selected SchedBlocks
     * @throws NoSbSelectedException if no selection is possible
     */
    public Collection<SchedBlock> select(Date ut, ArrayConfiguration arrConf) throws NoSbSelectedException;
     
     /**
      * Determine if the SchedBlock passed as parameter can be selected by the class
      * implementing this interface.
      * 
      * @param sb The SchedBlock to be evaluated.
      * @param date the date to evaluate if the SchedBlock whether can be selected or not.
      * @param arrConf the array configuration to evaluate whether can the SchedBlock can be selected or not
      * @return True if the SchedBlock can be selected or False otherwise
      */
     public boolean canBeSelected(SchedBlock sb, Date date, ArrayConfiguration arrConf);
     
}
