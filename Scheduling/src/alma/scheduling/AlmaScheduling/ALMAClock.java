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
 * File ALMACLock.java
 */
package alma.scheduling.AlmaScheduling;

import alma.scheduling.Define.Clock;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.ArrayTime;
import alma.scheduling.Define.ClockAlarmListener;

/**
 * @author Sohaila Lucero
 */
public class ALMAClock implements Clock {
    private DateTime time;
    public ALMAClock() {
        this.time = new DateTime(System.currentTimeMillis());
    }

    /**
     * Will eventually print out the time/date in a
     * readable format.
     * @return String
     */ 
    public String toString() {
        return "No clock defined yet.";
    }

    /**
     * 
     */
    public void setClockCoordinates(double lng, double lat, int zone) {
    }

    /**
     * @return DateTime
     */
    public DateTime getDateTime() {
        if(time == null) {
            return new DateTime();
        } else {
            return time;
        }
    }
    
    /**
     * @return double
     */
    public double getTimeOfDay() {
        return 0.0;
    }

    /**
     * @return double
     */
    public double getJD() {
        return 0.0;
    }

    /**
     * @return ArrayTime
     */
    public ArrayTime getArrayTime() {
        return null;
    }
    /**
     * @return double
     */
    public double getLatitude() {
        return 0.0;
    }
    /**
     *
     */
    public double getLatitudeInDegrees() {
        return 0.0;
    }
    /**
     *
     */
    public double getLongitude() {
        return 0.0;
    }

    /**
     *
     */
    public double getLongitudeInDegrees() {
        return 0.0;
    }
    /**
     *
     */
    public double getLongitudeInHours() {
        return 0.0;
    }

    /**
     *
     */
    public int getTimeZone() {
        return 0;
    }

    /**
     *
     */
    public double getConvertToUT() {
        return 0.0;
    }
    /**
     *
     */
    public void synchronize() {
    }

    /**
     *
     */
    public void setAlarm(DateTime time, ClockAlarmListener listener) {
    }

    /**
     *
     */
    public void setAlarm(int seconds, ClockAlarmListener listener) {
    }
}
