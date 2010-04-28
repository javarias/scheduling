/**
 * 
 */
package alma.scheduling.planning_mode_sim.controller;

import java.util.Date;

/**
 * Concrete implementation of TimeHandler that uses system time, and does not step forward time.
 * @author ahoffsta
 *
 */
public class TimeReal extends TimeHandler{
	
	protected TimeReal(){
	}
	
	public void setStartingDate(Date sd){
		logger.debug("Invocation of setStartingDate() method in a non-simulated environment");
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.planning_mode_sim.controller.TimeUtil#now()
	 */
	public Date getTime() {
		return new Date();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.planning_mode_sim.controller.TimeUtil#step()
	 */
	public void step() {
		logger.debug("Invocation of step() method in a non-simulated environment");
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.planning_mode_sim.controller.TimeUtil#step(double)
	 */
	public void step(int time) {
		logger.debug("Invocation of step() method in a non-simulated environment");
	}

	@Override
	public void step(Date date) {
		logger.debug("Invocation of step() method in a non-simulated environment");		
	}

}
