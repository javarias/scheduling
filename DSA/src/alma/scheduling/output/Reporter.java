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
 * "@(#) $Id: Reporter.java,v 1.1 2010/03/02 16:19:55 rhiriart Exp $"
 */
package alma.scheduling.output;

import alma.scheduling.datamodel.obsproject.SchedBlock;

/**
 * A Reporter represents the final step in the DSA Algorithm. Once the 
 * candidate SchedBlocks have been analyzed, and a selection has been made,
 * the selected SchedBlock is passed to this class for the final accounting
 * and reporting.
 * 
 */
public interface Reporter {

    /**
     * Reports that a SchedBlock has been selected to run.
     * This function updates the output tables in the database.
     * 
     * @param schedBlock Selected SchedBlock
     */
    public void report(SchedBlock schedBlock);
    
    /**
     * Generates an XML report.
     */
    public void generateXMLOutput();
}
