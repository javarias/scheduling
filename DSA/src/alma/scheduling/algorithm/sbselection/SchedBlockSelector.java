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
 * "@(#) $Id: SchedBlockSelector.java,v 1.8 2010/07/09 17:17:31 javarias Exp $"
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
     * Selects a set of SchedBlocks, in a manner that is independent of
     * the time. If the underlying queries require the time, the current
     * time is used.
     * @return the selected SchedBlocks
     * @throws NoSbSelectedException if no selection is possible
     */
    public Collection<SchedBlock> select() throws NoSbSelectedException;

    /**
     * Selects a set of SchedBlocks for a given point in time. If the queries
     * underlying the selection don't require the point in time, this parameter
     * is ignored.
     * 
     * @param ut Time (UTC)
     * @return the selected SchedBlocks
     * @throws NoSbSelectedException if no selection is possible
     */
    public Collection<SchedBlock> select(Date ut) throws NoSbSelectedException;
    
    /**
     * Selects a set of SchedBlocks for a given array.
     * If the queries underlying the selection don't require
     * the array, this parameter is ignored.
     * 
     * @param arrConf the array
     * @return the selected SchedBlocks
     * @throws NoSbSelectedException 
     * @throws NoSbSelectedException if no selection is possible
     */
    public Collection<SchedBlock> select(ArrayConfiguration arrConf) throws NoSbSelectedException;
    /**
     * Selects a set of SchedBlocks for a given point in time for a given array.
     * If the queries underlying the selection don't require the point in time
     * or the array, these parameters
     * are ignored.
     * 
     * @param ut Time (UTC)
     * @param arrConf the array
     * @return the selected SchedBlocks
     * @throws NoSbSelectedException if no selection is possible
     */
    public Collection<SchedBlock> select(Date ut, ArrayConfiguration arrConf) throws NoSbSelectedException;
    
   
    /**
     * The name of the property used for the restrictions must be 'sb'. 
     * 
     * SchedBlock properties alias that are used:
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
     
     public boolean canBeSelected(SchedBlock sb);
}
