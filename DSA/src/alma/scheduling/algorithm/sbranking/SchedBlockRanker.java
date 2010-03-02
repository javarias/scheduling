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
 * "@(#) $Id: SchedBlockRanker.java,v 1.4 2010/03/02 23:19:15 javarias Exp $"
 */
package alma.scheduling.algorithm.sbranking;

import java.util.List;

import alma.scheduling.datamodel.obsproject.SchedBlock;

public interface SchedBlockRanker {

    /**
     * This method should clean the current ranks and calculate the
     * rank of each SchedBlock
     * 
     * @param the list of SchedBlock to be ranked
     * @return the rank of each SchedBlock. 
     */
    public List<SBRank> rank(List<SchedBlock> sbs);

    /**
     * 
     * @param ranks the ranks of the SchedBlocks
     * @return the best ranked SckedBlock
     */
    public SchedBlock getBestSB(List<SBRank> ranks);
}
