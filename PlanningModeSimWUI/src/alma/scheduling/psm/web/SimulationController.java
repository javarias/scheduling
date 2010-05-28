package alma.scheduling.psm.web;

import java.util.Map;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zk.ui.util.Initiator;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.api.Button;
import org.zkoss.zul.api.Window;

public class SimulationController extends GenericForwardComposer implements Initiator {

	private static final long serialVersionUID = 8804377866471220025L;
	
	private Button buttonRun;
	private Button buttonReports;
	
	public void onClick$buttonLoad(Event event) {
		System.out.println("Load button pressed");
		
		buttonRun.setDisabled(false);
		buttonReports.setDisabled(false);
	}
	
	public void onClick$buttonRun(Event event) {
		System.out.println("Run button pressed");
		Window simulationWindow = (Window) Path
				.getComponent("//mainPage/mainWindow/simulationWindow");
		if (simulationWindow == null)
			System.out.println("configurationWindow is null");
		else {
			simulationWindow.detach();
			Window mainWindow = (Window) Path
					.getComponent("//mainPage/mainWindow");
			Window runningWindow = (Window) Executions.createComponents(
					"running.zul", mainWindow, null);
			runningWindow.doOverlapped();
		}
	}

	@Override
	public void doAfterCompose(Page arg0) throws Exception {
		
	}

	@Override
	public void doInit(Page arg0, Map arg1) throws Exception {
		
	}
	
	

}