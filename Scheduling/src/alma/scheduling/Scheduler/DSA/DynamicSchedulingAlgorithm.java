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
import alma.scheduling.Define.SchedLogger;
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
 * @version $Id: DynamicSchedulingAlgorithm.java,v 1.9 2007/09/20 16:08:25 sslucero Exp $
 * @author Allen Farris
 */
public class DynamicSchedulingAlgorithm {
	
	//private R2aPolicy dsa;
                    
    //private R1Policy dsa;
    private R5Policy dsa;

	
	public DynamicSchedulingAlgorithm(String arrayname, Policy policy, 
        SBQueue queue, Clock clock, Telescope telescope, 
        ProjectManager projectManager, SchedLogger log, int bestNumber ) 
        throws SchedulingException {
		
        if(policy.getName().equals("R1Policy")) {

            log.info("R1Policy: not available anymore, use R3Policy");
    	//	dsa = new R1Policy(
           //         arrayname, policy, queue, clock, telescope,
            //        projectManager, log, bestNumber);
    
        } else if(policy.getName().equals("R2aPolicy")) {
            log.info("R2Policy: not available anymore, use R3Policy");
    	//	dsa = new R2aPolicy(arrayname,policy, queue, clock, telescope,
          //      projectManager, log, bestNumber);
        
        } else if(policy.getName().equals("R3.0Policy")) {
        
            log.info("R3Policy: not available anymore, use R4Policy");
          //  dsa = new R3Policy(
            //        arrayname, policy, queue, clock, telescope,
              //      projectManager, log, bestNumber);
                    
        } else if(policy.getName().equals("R4.0Policy")){
            log.info("R4Policy: not available anymore, use R5Policy");
            /*
            dsa = new R4Policy(
                    arrayname, policy, queue, clock, telescope,
                    projectManager, log, bestNumber);
                    */
        } else if(policy.getName().equals("R5.0Policy")){
            dsa = new R5Policy(
                    arrayname, policy, queue, clock, telescope,
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
