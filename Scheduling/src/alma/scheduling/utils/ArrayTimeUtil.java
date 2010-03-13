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
 * File Util.java
 */
package alma.scheduling.utils;

import alma.hla.runtime.asdm.types.ArrayTime;

public class ArrayTimeUtil {
    
    private static final long ACS_TO_ARRAY_TIME_CONSTANT = 8712576000000000000L;
    private static final long SYSTEM_TIME_BASE = new ArrayTime(1970,1,1,0,0,0.0).get();

    /**
     * Convert ACS time to ArrayTime
     * 
     * ACS: hundreds of nanoseconds since 15 October 1582 00:00:00 UTC
     * ArrayTime: nanoseconds since 17 November 1858 00:00:00 UTC
     * base difference in days: JD(17 November 1858 00:00:00 UTC) minus
     *                          JD(15 October 1582 00:00:00 UTC)
     * ArrayTime = (ACS.time * 100) minus (baseDifference * 86400000000000)
     * ArrayTime = 100*ACS.time - (2400000.5 - 2299160.5) * 86400000000000
     * ArrayTime = 100*ACS.time - (100840) * 86400000000000
     * ArrayTime = 100*ACS.time - 8712576000000000000
     *  
     * @param acsTime The ACS time.
     * @return the ArrayTime as a long.
     */
    public static long acsTimeToArrayTime(long acsTime) {
        return 100L * acsTime - ACS_TO_ARRAY_TIME_CONSTANT;
    }
    
    /**
     * Convert ACS time to ArrayTime
     * 
     * @param arrayTime
     * @return
     */
    public static long arrayTimeToACSTime(long arrayTime) {
        return arrayTime / 100L + ACS_TO_ARRAY_TIME_CONSTANT;
    }
    
    /**
     * Converts an ACS Time to its string representation.
     * @param acsTime ACS Time
     * @return ACS Time string representation
     */
    public static String acsTimeToString(long acsTime) {
        return new ArrayTime(acsTimeToArrayTime(acsTime)).toFITS();
    }
    
    /**
     * Converts an Array Time to its string representation.
     * @param arrayTime Array Time
     * @return Array Time string representation
     */
    public static String arrayTimeToString(long arrayTime) {
        return new ArrayTime(arrayTime).toFITS();
    }
    
    /**
     * The getArrayTime method returns the ArrayTime 
     * using the current system time.
     * @return
     */
    public static ArrayTime getArrayTime() {
        // The difference, measured in milliseconds, between the 
        // current time and midnight, January 1, 1970 UTC.
        long currentSysTime = System.currentTimeMillis();        
        return new ArrayTime(currentSysTime * 1000000L + SYSTEM_TIME_BASE);
    }
}
