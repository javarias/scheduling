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
 * File ALMAClock.java
 */
 
package alma.scheduling.planning_mode_sim.define;

/**
 * The ALMA clock has not been completely implemented. 
 * 
 * @version 1.00  Oct 6, 2003
 * @author Allen Farris
 */
public class ALMAClock extends ClockBase {

	public ALMAClock() {
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.planning_mode_sim.define.Clock#setAlarm(alma.scheduling.planning_mode_sim.define.DateTime, alma.scheduling.planning_mode_sim.define.ClockAlarmListener)
	 */
	public void setAlarm(DateTime time, ClockAlarmListener listener) {
		long duration = (long)(time.getJD() - getJD()) * 86400000L;
		if (duration <= 0)
			return;
		Timer t = new Timer(this,listener,duration);
		Thread x = new Thread(t);
		x.start();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.planning_mode_sim.define.Clock#setAlarm(int, alma.scheduling.planning_mode_sim.define.ClockAlarmListener)
	 */
	public void setAlarm(int seconds, ClockAlarmListener listener) {
		if (seconds <= 0)
			return;
		Timer t = new Timer(this,listener,seconds*1000L);
		Thread x = new Thread(t);
		x.start();
	}
	/* (non-Javadoc)
	 * @see alma.scheduling.planning_mode_sim.define.Clock#getArrayTime()
	 */
	public ArrayTime getArrayTime() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.planning_mode_sim.define.Clock#getDateTime()
	 */
	public DateTime getDateTime() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.planning_mode_sim.define.Clock#getTimeOfDay()
	 */
	public double getTimeOfDay() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.planning_mode_sim.define.Clock#synchronize()
	 */
	public void synchronize() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see alma.scheduling.planning_mode_sim.define.Clock#getJD()
	 */
	public double getJD() {
		// TODO Auto-generated method stub
		return 0;
	}

}

class Timer implements Runnable {
	private Clock clock;
	private long milliseconds;
	private ClockAlarmListener alarm;
	Timer(Clock clock, ClockAlarmListener alarm, long milliseconds) {
		this.clock = clock;
		this.alarm = alarm;
		this.milliseconds = milliseconds;
	}
	public void run() {
		/*if (milliseconds <= 0)
			return;
		long startTime = clock.currentTimeMillis();
		long newStartTime = 0;
		long timeInterval = milliseconds;
		do {
			try {
				Thread.sleep(timeInterval);
			} catch (InterruptedException ex) {
				newStartTime = clock.currentTimeMillis();
				timeInterval -= newStartTime - startTime;
				startTime = newStartTime;
			}
		} while (timeInterval >= 0);
		alarm.wakeUp(clock.getDateTime());*/
	}
}
