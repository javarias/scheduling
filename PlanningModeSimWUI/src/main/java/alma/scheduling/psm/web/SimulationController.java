package alma.scheduling.psm.web;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.http.WebManager;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zk.ui.util.Initiator;
import org.zkoss.zul.Label;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.api.Button;
import org.zkoss.zul.api.Combobox;
import org.zkoss.zul.api.Datebox;
import org.zkoss.zul.api.Panelchildren;
import org.zkoss.zul.api.Window;

import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.psm.sim.InputActions;
import alma.scheduling.psm.sim.ReportGenerator;
import alma.scheduling.psm.sim.SimulationProgressEvent;
import alma.scheduling.psm.sim.Simulator;
import alma.scheduling.psm.util.SchedulingPolicyWrapper;
import alma.scheduling.psm.web.timeline.TimelineCollector;
import alma.scheduling.psm.web.timeline.TimelineEventListener;
import alma.scheduling.psm.web.util.DSAPoliciesLoaderListener;
import alma.scheduling.utils.DSAContextFactory;

public class SimulationController extends GenericForwardComposer implements
		Initiator, Observer {

	private static Logger logger = LoggerFactory
			.getLogger(SimulationController.class);
	
	private static final long serialVersionUID = 8804377866471220025L;

	private Button buttonBasicConfiguration;
	private Button buttonDataSources;
	private Button buttonLoad;
	private Button buttonUnload;
	private Button buttonClean;
	private Button buttonRun;
	private Button buttonReports;
	private Combobox DSAPoliciesComboBox;
	private Progressmeter simulationProgress;
	private Label simulationPercentageLabel;
	private Datebox dateboxStartDate;
	private Datebox dateboxEndDate;
	
	private Panelchildren panelChildrenStatus;

	private WebApp webapp;

	public static final ExecutiveDAO execDao = (ExecutiveDAO) alma.scheduling.utils.DSAContextFactory
			.getContext().getBean("execDao");
	
	public static final String PROGRESS_QUEUE = "simulationRun";

	public void onClick$buttonBasicConfiguration(Event event) {
		System.out.println("Basic Configuration button pressed");
		Window mainWindow = (Window) Path.getComponent("//");
		if (mainWindow == null) {
			System.out.println("mainWindow is null");
		}
		Window configurationWindow = (Window) Executions.createComponents(
				"configuration.zul", mainWindow, null);
		try {
			configurationWindow.doModal();
			// } catch (SuspendNotAllowedException e) {
			// e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void onClick$buttonFullLoad(Event event) {
		System.out.println("Load button pressed, starting fullload");
		System.out.println((String) Sessions.getCurrent().getAttribute(
				"workDir"));

		InputActions inputActions = InputActions.getInstance(((String) Sessions
				.getCurrent().getAttribute("workDir")));
		try {
			inputActions.fullLoad();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(panelChildrenStatus.getChildren().get(0).getClass());
		System.out.println("Fullload finished");
	}

	public void onClick$buttonLoad(Event event) {
		System.out.println("Load button pressed, starting fullload");
		System.out.println((String) Sessions.getCurrent().getAttribute(
				"workDir"));

		InputActions inputActions = InputActions.getInstance(((String) Sessions
				.getCurrent().getAttribute("workDir")));
		try {
			inputActions.load();
		} catch (NoSuchBeanDefinitionException e) {
			logger.warn("No remote operations available, fallback to local");
			inputActions.load();
		}
		System.out.println("Fullload finished");
	}

	public void onClick$buttonUnload(Event event) {
		System.out.println("Unload button pressed, starting unload");
		System.out.println((String) Sessions.getCurrent().getAttribute(
				"workDir"));

		InputActions inputActions = InputActions.getInstance(((String) Sessions
				.getCurrent().getAttribute("workDir")));
		inputActions.unload();
		System.out.println("Fullload finished");
	}

	public void onClick$buttonClean(Event event) {
		System.out.println("Clean button pressed, starting clean");
		System.out.println((String) Sessions.getCurrent().getAttribute(
				"workDir"));

		InputActions inputActions = InputActions.getInstance(((String) Sessions
				.getCurrent().getAttribute("workDir")));
		try {
			inputActions.clean();
		} catch (NoSuchBeanDefinitionException e) {
			logger.warn("No remote operations available, fallback to local");
			inputActions.clean();
		}

		System.out.println("Clean finished");
	}

	public void onClick$buttonReports(Event event) {
		System.out.println("Reports button pressed, creating reports");
		System.out.println((String) Sessions.getCurrent().getAttribute(
				"workDir"));
		ReportGenerator reportGenerator = new ReportGenerator((String) Sessions
				.getCurrent().getAttribute("workDir"));
		reportGenerator.createLstRangesBeforeSimReport();
		reportGenerator.finalreport();
		reportGenerator.printLSTRangesReport();

		System.out.println("Generation finished");
	}

	public void onClick$buttonRun(Event event) {
		final String workDir = (String) Sessions.getCurrent().getAttribute("workDir");
		final Observer o =  this;
		final Observer tlo = new TimelineEventListener();
		logger.info("Using APRC_WORK_DIR: " + workDir);
		final EventQueue eq = EventQueues.lookup(PROGRESS_QUEUE, EventQueues.APPLICATION, true);
		eq.subscribe(new EventListener() {
			@Override
			public void onEvent(Event event) throws Exception {
				if ("startSimulation".compareTo(event.getName()) == 0) {
					Simulator.setApplicationContext(DSAContextFactory.getContext());
					TimelineCollector.getInstance().reset();
					Simulator simt = new Simulator(workDir);
					simt.addObserver(o);
					simt.addObserver(tlo);
					simt.run(((SchedulingPolicyWrapper) DSAPoliciesComboBox
							.getSelectedItemApi().getValue()).getSpringBeanName());
					System.out.println("Run finished");
					simt.deleteObserver(o);
					simt.deleteObserver(tlo);
					eq.publish(new Event("endSimulation"));
				}
			}
		}, true);
		eq.publish(new Event("startSimulation"));
	}

	public void onClick$buttonPh1mSynch(Event event) {
		Window mainWindow = (Window) Path.getComponent("//");
		if (mainWindow == null) {
			System.out.println("mainWindow is null");
		}
		Window ph1mSychToolWindow = (Window) Executions.createComponents(
				"Phase1MSynch.zul", mainWindow, null);
		try {
			ph1mSychToolWindow.doModal();
		} catch (SuspendNotAllowedException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void doAfterCompose(Page arg0) throws Exception {
		System.out.println("SimulatorController.doAfterCompose()");
		webapp = Sessions.getCurrent().getWebApp();
	}

	public void doInit(Page arg0, Map arg1) throws Exception {
		System.out.println("SimulatorController.doInit()");
		System.out.println((String) Sessions.getCurrent().getAttribute(
				"workDir"));
	}
	
	@Override
	public void update(Observable o, Object arg) {
		SimulationProgressEvent e = (SimulationProgressEvent) arg;
		final EventQueue eq = EventQueues.lookup(PROGRESS_QUEUE, 
				WebManager.getWebApp(DSAPoliciesLoaderListener.servletContext),
				true);
		Event event = new Event("progressUpdate", null, e);
		eq.publish((event));
	}
	
	private final EventListener simMonitoringListener = new EventListener() {
		@Override
		public void onEvent(Event event) throws Exception {
			System.out.println(event.getName());
			if ("endSimulation".compareTo(event.getName()) == 0) {
				EventQueues.remove(PROGRESS_QUEUE, EventQueues.APPLICATION);
				simulationPercentageLabel.setValue("Simulation Completed");
				simulationProgress.setValue(100);
			} else if ("progressUpdate".compareTo(event.getName()) == 0) {
				SimulationProgressEvent e = (SimulationProgressEvent) event
						.getData();
				if (simulationProgress != null) 
					simulationProgress.setValue((int) e.getProgressPercentage());
				if (simulationPercentageLabel != null)
					simulationPercentageLabel.setValue("Running: "
							+ e.getFormattedProgressPercentage() + " % ");
			} else if ("startSimulation".compareTo(event.getName()) == 0) {
				simulationPercentageLabel.setValue("Starting simulation 0 % ");
			}
		}
	};
	
}