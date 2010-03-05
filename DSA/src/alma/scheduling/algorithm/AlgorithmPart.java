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
 * "@(#) $Id: AlgorithmPart.java,v 1.3 2010/03/05 18:28:06 rhiriart Exp $"
 */
package alma.scheduling.algorithm;

import java.util.Date;
import java.util.List;

/**
 * An AlgorithPart represents a participant in the DSA algorithm. It can
 * be either a ModelUpdater or a SchedBlockRanker. This interface allows to
 * follow the dependencies and run all the required ModelUpdaters for a given
 * iteration of the DSA. At this point the database is ready to perform the
 * final selection and ranking.
 */
public interface AlgorithmPart {

    /**
     * Get all the AlgorithmParts that need to be executed before the current
     * Algorithm part can be executed. 
     */
    List<AlgorithmPart> getAlgorithmDependencies();

    /**
     * Execute this AlgorithmPart. For a ModelUpdater this means to update the
     * model, for a SchedBlockRanker it means to execute its dependent AlgorithmParts.
     * @param ut Current time
     */
    void execute(Date ut);
}
