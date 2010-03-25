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
 * "@(#) $Id: DynamicSchedulingAlgorithm.java,v 1.10 2010/03/25 16:43:16 javarias Exp $"
 */
package alma.scheduling.algorithm;

import java.util.Date;

import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;

public interface DynamicSchedulingAlgorithm {

    
    public void rankSchedBlocks();

    /**
     * Clean the current candidate SBs and run again the selectors
     * 
     * @throws NoSbSelectedException if a selector cannot get SBs or if this method
     * cannot intersect a common group between al SBs returned by the selectors used
     */
    public void selectCandidateSB() throws NoSbSelectedException;
    
    /**
     * Clean the current candidate SBs and run again the selectors
     * 
     * @param The time simulated
     * 
     * @throws NoSbSelectedException if a selector cannot get SBs or if this method
     * cannot intersect a common group between al SBs returned by the selectors used
     */
    public void selectCandidateSB(Date ut) throws NoSbSelectedException;

    public void updateModel();
    
    public SchedBlock getSelectedSchedBlock();
    
    /**
     * Set the Array to be schedulued by this DSA instance
     * @param arrConf The Array
     */
    public void setArray(ArrayConfiguration arrConf);

    /**
     * @return The scheduled array
     */
    public ArrayConfiguration getArray();

}