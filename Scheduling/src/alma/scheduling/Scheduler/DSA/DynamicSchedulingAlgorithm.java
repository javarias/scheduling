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
 * File DynamicSchedulingAlgorithm.java
 */
package alma.scheduling.Scheduler.DSA;

import alma.scheduling.Define.SB;
import alma.scheduling.Define.SBQueue;
import alma.scheduling.Define.Clock;
import alma.scheduling.Define.Telescope;
import alma.scheduling.Define.ProjectManager;
import alma.scheduling.Define.SiteCharacteristics;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.Policy;
import alma.scheduling.Define.PolicyFactor;
import alma.scheduling.Define.Subarray;
import alma.scheduling.Define.FrequencyBand;
import alma.scheduling.Define.WeatherCondition;
import alma.scheduling.Define.SchedulingException;
import alma.scheduling.Define.BestSB;
import alma.scheduling.Define.NothingCanBeScheduled;

import java.util.logging.Logger;

/**
 * This is the dynamic scheduling algorithm for R1.
 * <p>
 * Implementation Notes
 * <p>
 * The way this will be implemented when we have more than one scheduling
 * policy concept is the following.  All such classes will be called
 * DynamicSchedulingAlgorithm and placed in distinct packages.  Each will
 * have a static name that identifies the algorithm.  Then we will use the
 * Java classloader to load the appropriate algorithm based on the 
 * scheduling policy.
 * <p>
 * An alternative to the above approach is to define a "generic" interface
 * to the scheduling algorithm and have the scheduler rely on it.  This may
 * be a simpler apporach.
 * <p>
 * There are only a limited number of public method in the algorithm:
 * <ul>
 * <li> the constructor (which does a lot of computations)
 * <li> the getBest method
 * <li> log information (formatToString and visibleToString)
 * </ul> 
 * <p>
 * We will eventually need two different types of getBest methods:
 * <ul>
 * <li>	BestSB getBest() <i>Get the best list now.</i>
 * <li>	SB getBest(DateTime t) <i>Get the best list at time t.</i>
 * </ul>
 * It is clear that the second method, getting the best list at a 
 * particular time, is much more difficult and requires not only
 * rethinking how expressions are evaluated but also how to compute and
 * store state information (previous project and frequency, etc.).
 * So, there is a good deal of thinking to be done on how to do this.
 * For now, the second method is not implemented.
 * 
 * @version 1.00  Sep 26, 2003
 * @author Allen Farris
 */
public class DynamicSchedulingAlgorithm {
	
	private R2aPolicy dsa;

	
	public DynamicSchedulingAlgorithm(int subarrayId, Policy policy, 
        SBQueue queue, Clock clock, Telescope telescope, 
        ProjectManager projectManager, Logger log, int bestNumber ) 
        throws SchedulingException {
		
        if(policy.getName().equals("R1Policy")) {
    		//dsa = new R1Policy(subarrayId,policy, queue, clock, telescope,
              //  projectManager, log, bestNumber);
        } else if(policy.getName().equals("R2aPolicy")) {
    		dsa = new R2aPolicy(subarrayId,policy, queue, clock, telescope,
                projectManager, log, bestNumber);
        }
	}

	/**
	 * Get the best scheduling blocks to run at the specified time.
	 */
	public BestSB getBest() throws SchedulingException {
		return dsa.getBest();
        //return null;
	}


}
