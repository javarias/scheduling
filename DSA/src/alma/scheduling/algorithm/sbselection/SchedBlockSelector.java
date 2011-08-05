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
 * "@(#) $Id: SchedBlockSelector.java,v 1.10 2011/08/05 18:17:10 javarias Exp $"
 */
package alma.scheduling.algorithm.sbselection;

import java.util.Collection;
import java.util.Date;

import org.hibernate.criterion.Criterion;

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
     * If the query is done over SchedBlocks, who implement this method must use the alias
     * sb for schedblocks: For Example: </br>
     * Do a query for SchecBlock.manual: "sb.manual"
     * 
     * Aliases also must be used for:
     * "schedulingConstraints.representativeTarget","rt" </br>
     * "schedulingConstraints.representativeTarget.source","s" </br>
     * "executive.executivePercentage", "ep" </br>
     * 
     * If you want to use, for example: </br>
     * "schedulingConstraints.representativeTarget.source.coordinates.RA" you
     * must replace it by "s.coordinates.RA"
     * 
     * All this alias are defined as part of the criteria created by the {@link MasterSelector#select()}
     * 
     * @return the criterion for the SchedBlocks ('sb') to be used in conjunction
     * with the others selectors 
     */
     public Criterion getCriterion(Date ut, ArrayConfiguration arrConf);
     
     /**
      * Determine if the SchedBlock passed as parameter can be selected by the class
      * implementing this interface.
      * 
      * @param sb The SchedBlock to be evaluated
      * @return True if the SchedBlock can be selected or False otherwise
      */
     public boolean canBeSelected(SchedBlock sb);
}
