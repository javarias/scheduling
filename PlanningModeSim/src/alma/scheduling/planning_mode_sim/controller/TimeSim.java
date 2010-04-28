/**
 * 
 */
package alma.scheduling.planning_mode_sim.controller;

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
		this.step(30 * 60 * 1000);
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
