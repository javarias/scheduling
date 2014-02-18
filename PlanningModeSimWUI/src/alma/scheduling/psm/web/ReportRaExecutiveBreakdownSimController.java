package alma.scheduling.psm.web;

import org.zkoss.zk.ui.event.Event;

public class ReportRaExecutiveBreakdownSimController extends ReportsController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public void onCreate$reportWindowAfterSimRaExec(Event event) {
		showReport();
	}
}
