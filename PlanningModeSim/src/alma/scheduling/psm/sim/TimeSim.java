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
 */
package alma.scheduling.psm.sim;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Concrete implementation of TimeHandler that keeps its own track of time, and allows step methods to work.
 * @author ahoffsta
 *
 */
public class TimeSim extends TimeHandler{
	
	private Date date;
		
	protected TimeSim(){
        date = new Date();
	}
	
	public void setStartingDate(Date sd){
		date = sd;
		TimeHandler.getLogger().debug("Setting starting date to" + sd.toString() );
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.planning_mode_sim.controller.TimeUtil#now()
	 */
	public Date getTime() {
		return date;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.planning_mode_sim.controller.TimeUtil#step()
	 */
	public void step() {
		// 1 hr and 20 mins
		this.step(60 * 60 * 1000 + 20 * 60 * 1000);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.planning_mode_sim.controller.TimeUtil#step(int)
	 */
	public void step(int time) {
		Calendar cal = Calendar.getInstance( TimeZone.getTimeZone("UT") );
        cal.setTime(date);
        cal.add(Calendar.MILLISECOND, time * 1000);
        date = cal.getTime();
        logger.debug("Stepping forward into:" + date.toString() );
	}
	
	
	@Override
	public void step(Date date) {
		this.date = date;
		logger.debug("Stepping forward into:" + date.toString() );
	}

}
