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
 * File ALMAClock.java
 */
 
package alma.scheduling.master_scheduler;

import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Logger;
import java.util.StringTokenizer;

import alma.acs.container.ContainerServices;
import alma.scheduling.define.STime;
import alma.scheduling.define.ArrayTime;
import alma.scheduling.define.ClockBase;
import alma.scheduling.define.ClockAlarmListener;

import alma.scheduling.define.DateTime;

/**
 * The Clock class gives access to the array time, as implemented by
 * the Control System, in the real mode.  In the simulation mode, this
 * class accesses the ClockSimulator in the simulation class.
 * 
 * @version 1.00 May 5, 2003
 * @author Allen Farris
 */
public class ALMAClock extends ClockBase {
	int delta;
    private Logger logger;
    private ContainerServices container;
    private String fits_str;

	/**
	 * Create a clock object.
	 * @param isSimulation	True, if this a simulation.
	 * @param container The object that implements the container services.
	 */
	public ALMAClock (boolean isSimulation, ContainerServices container) {
		System.out.println("SCHEDULING: The Clock has been constructed.");
		synchronize();
        this.container = container;
        this.logger = container.getLogger();
        try {
            org.omg.CORBA.Object obj = container.getDefaultComponent("IDL:alma/acstime/Clock:1.0");
            alma.acstime.Clock acsclock = alma.acstime.ClockHelper.narrow(obj);

            alma.ACS.ROuLongLong now = acsclock.now();
            alma.ACS.CompletionHolder something = new alma.ACS.CompletionHolder();
            long now1 = now.get_sync(something);
            fits_str = acsclock.toISO8601(alma.acstime.TimeSystem.TSUTC,
                                              new alma.acstime.Epoch(now1));
            logger.fine("SCHEDULING: time now is "+ fits_str);
            //DateTime dt = new DateTime(now1);
            //logger.fine("SCHEDULING: DateTime is now "+ dt.toString());
            //break it into date and time
            /*
            StringTokenizer st = new StringTokenizer(fits_str, "T");
            if(st.countTokens() != 2) {
                //fix this..
                logger.severe("SCHEDULING: error.. more than 2!");
            }
            String date = st.nextToken();
            logger.fine("SCHEDULING: date now is "+ date);
            String time = st.nextToken();
            logger.fine("SCHEDULING: time now is "+ time);
            
            st = new StringTokenizer(date, "-");
            String year = st.nextToken();
            logger.fine("SCHEDULING: year now is "+ year);
            String month = st.nextToken();
            logger.fine("SCHEDULING: month now is "+ month);
            String day = st.nextToken();
            logger.fine("SCHEDULING: day now is "+ day);

            st = new StringTokenizer(time, ":");
            String hour = st.nextToken();
            logger.fine("SCHEDULING: hour now is "+ hour);
            String min = st.nextToken();
            logger.fine("SCHEDULING: min now is "+ min);
            String sec = st.nextToken();
            logger.fine("SCHEDULING: sec now is "+ sec);
            */
            DateTime dt = new DateTime(fits_str);
            logger.fine("SCHEDULING: date time = "  + dt.toString());
        }catch(Exception e) {
            logger.severe("SCHEDULING: "+e.toString());
            e.printStackTrace();
        }
	}
	
	
	/**
	 * Return the current time as an STime object.  This time is
	 * an approximation to the array time.  It uses the current system
	 * time and adds a delta to it to synchronize it to the array time.
	 * @return The current time as an STime object.
	 */
	public STime getSTime() {
		return null;
	}
	
	/**
	 * Return the array time.  This time is from the control system,
	 * if this is real; if it is a simulation, then it is from the
	 * clock simulator.
	 * @return The current time as an ArrayTime object.
	 */
	public ArrayTime getArrayTime() {
		return null;
	}

	/**
	 * Synchronize the currrent system time and the array time.
	 * This class gets the array time and then saves a delta to 
	 * convert from the system time to the array time.
	 */
	public void synchronize() {
		delta = 0;
	}

	/**
	 * Set an alarm, to go off at a particular time.
	 * @param time		The time at which the alarm goes off.
	 * @param listener	The listener method to call to process the alarm.
	 */
	public void setAlarm(STime time, ClockAlarmListener listener) {
		// Spawn a thread that sleeps until now is equal to time.
		// Execute the listener method.
		// interface ClockAlarmListener
		//		wakeUp(STime time)
	}

	/**
	 * Set an alarm, to go off at N seconds from now.
	 * @param seconds	The number of seconds to wait for the alarm to go off.
	 * @param listener	The listener method to call to process the alarm.
	 */
	public void setAlarm(int seconds, ClockAlarmListener listener) {
		// Spawn a thread that sleeps for seconds.
		// Execute the listener method.
	}

	public static void main(String[] args) {
	}
}

