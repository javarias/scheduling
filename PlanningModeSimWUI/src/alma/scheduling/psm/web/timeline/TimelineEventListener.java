package alma.scheduling.psm.web.timeline;

import java.util.Observable;
import java.util.Observer;

import alma.scheduling.psm.sim.SimulationProgressEvent;
import alma.scheduling.psm.sim.TimeEvent;

public class TimelineEventListener implements Observer {

	private final TimelineCollector collector = TimelineCollector.getInstance();
	
	@Override
	public void update(Observable o, Object arg) {
		if (arg != null && arg instanceof SimulationProgressEvent){
			TimeEvent ev = ((SimulationProgressEvent) arg).getTimeEvent();
			collector.addEvent(ev);
		}
	}
	
	public TimelineCollector getCollector() {
		return collector;
	}
}
