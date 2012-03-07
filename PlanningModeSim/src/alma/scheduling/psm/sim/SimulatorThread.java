package alma.scheduling.psm.sim;

import java.util.Date;

import alma.scheduling.algorithm.VerboseLevel;
import alma.scheduling.psm.sim.Simulator;

public class SimulatorThread extends Thread{
	
	private Date currentDate;
	private Date startDate;
	private Date stopDate;

	private VerboseLevel verboseLvl;
	private String workDir;
	
	public SimulatorThread( String workDir, VerboseLevel verboseLevel ){
		this.workDir = workDir;
		this.verboseLvl = verboseLevel;
	}

	synchronized public Date getCurrentDate() {
		return currentDate;
	}

	synchronized public void setCurrentDate(Date currentDate) {
		this.currentDate = currentDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public Date getStopDate() {
		return stopDate;
	}

	public void setStopDate(Date stopDate) {
		this.stopDate = stopDate;
	}

	public void run(String DSAPolicyName){
		Simulator simulator = new Simulator( workDir, this );
    	simulator.setVerboseLvl(verboseLvl);
    	simulator.run(DSAPolicyName);
	}

}
