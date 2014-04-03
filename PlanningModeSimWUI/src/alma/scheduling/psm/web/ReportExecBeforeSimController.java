package alma.scheduling.psm.web;

import org.zkoss.zk.ui.event.Event;

public class ReportExecBeforeSimController extends ReportsController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 134568934573459L;

	public void onCreate$reportWindowBeforeSimExec(Event event) {
		showReport();
	}
}
