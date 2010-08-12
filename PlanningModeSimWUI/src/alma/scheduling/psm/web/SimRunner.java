package alma.scheduling.psm.web;

import java.util.Date;

import org.zkoss.lang.Threads;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.Clients;

import alma.scheduling.algorithm.VerboseLevel;
import alma.scheduling.psm.sim.SimulatorThread;


public class SimRunner extends Thread {
	private Desktop _desktop;

	public SimRunner(Desktop desktop) {
		_desktop = desktop;
	}

	public void run(){
		try {

			if (_desktop.isServerPushEnabled()) {
				
				SimulatorThread simThread = new SimulatorThread( 
						(String)_desktop.getSession().getAttribute("workDir"), 
						VerboseLevel.NONE );
				simThread.start();

				while( simThread.isAlive() ){
					try {
						Executions.activate(_desktop);
						Long totalTime = simThread.getStopDate().getTime() - simThread.getStartDate().getTime();
						Long currentTime = simThread.getCurrentDate().getTime() - simThread.getStartDate().getTime();
						Double progress = (currentTime.doubleValue() / totalTime.doubleValue()) * 100;
						Clients.showBusy(
								"Please wait, simulation is running. Progress: " + progress.intValue() + "%",  true);
						Executions.deactivate(_desktop);
						Threads.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				simThread.join();
				
				Executions.activate(_desktop);
				Clients.showBusy(
						"Please wait, simulation is running. Progress: " + 100 + "%",  true);
				Executions.deactivate(_desktop);
				Threads.sleep(1000);
				Executions.activate(_desktop);
				Clients.showBusy(null, false);
				Executions.deactivate(_desktop);
			}

		} catch (InterruptedException ex) {
		} finally {
				_desktop.enableServerPush(false);
			
		}

	}
	
}
