/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
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
 * File ArrayTime.java
 */
 
package alma.scheduling.Define;

/**
 * ArrayTime is based on the definition of time from the 
 * OMG Time Service Specification. 
 * (See ftp://ftp.omg.org/pub/docs/formal/97-12-21.pdf)
 * It is the number of 100 ns since 1582-10-15 00:00:00.
 * 
 * @version $Id: ArrayTime.java,v 1.7 2006/07/17 20:53:49 sslucero Exp $
 * @author Allen Farris
 */
public class ArrayTime {
    //ACSTime in seconds when unix time is at 0.
    private final long conversion = 12219292800L; //in seconds
    //The array time
	private long acsTime;

	/**
	 * Create an ArrayTime from a long. The long representing ACSTime
	 */
	public ArrayTime(long t) {
		this.acsTime = t; //in 100ns units ... acs time
        System.out.println("ACS Time = "+acsTime);
	}

    /**
     * @return
     */
    public DateTime arrayTimeToDateTime() {
        //long unixTime = (time - 122192928000000000L) / 10000L; 
        double acs_jd = 2299160.5; //acs base time as JD
        double jd = acs_jd + (acsTime / 864000000000.0);
        //System.out.println("JD = "+jd);
        return  new DateTime(jd);
    }
     
    /*
     * Another method for converting
     * To convert from ACSTime to MJD:
     *
     *  1. Convert the base time to JD. Call it jd1582
     *      1582-10-15 00:00:00 UT = 2299160.5 JD
     *      jd1582 = 2299160.5
     *
     *  2. convert ACSTime to days and fractions --> call it acsdays
     *      acsdays = asctime / 864000000000.0
     *
     *  3. compute the current time in JD
     *      currentjd = jd1582 + acsdays
     *
     *  4. compute the mjd
     *      mjd = currentjd - 2400000.5
     *
     */


        
    /**
     * Get the value of the array time as a long.
     * @return
     */
    public long getValue() {
        return acsTime;
    }



    
}
