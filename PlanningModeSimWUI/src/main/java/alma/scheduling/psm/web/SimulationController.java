package alma.scheduling.psm.web;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zk.ui.util.Initiator;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.api.Button;
import org.zkoss.zul.api.Panelchildren;
import org.zkoss.zul.api.Window;

import alma.scheduling.algorithm.VerboseLevel;
import alma.scheduling.input.config.generated.Configuration;
import alma.scheduling.psm.sim.InputActions;
import alma.scheduling.psm.sim.ReportGenerator;
import alma.scheduling.psm.sim.Simulator;
import alma.scheduling.psm.sim.SimulatorThread;

public class SimulationController extends GenericForwardComposer implements Initiator {

	
    private static Logger logger = LoggerFactory.getLogger(SimulationController.class);


	private static final long serialVersionUID = 8804377866471220025L;
	
	private Button buttonBasicConfiguration;
	private Button buttonDataSources;
	private Button buttonLoad;
	private Button buttonUnload;
	private Button buttonClean;
	private Button buttonRun;
	private Button buttonReports;
	
	private Panelchildren panelChildrenStatus;

	
	public void onClick$buttonBasicConfiguration(Event event) {
		System.out.println("Basic Configuration button pressed");
		Window mainWindow = (Window) Path.getComponent("//");
    	if( mainWindow == null ){
    		System.out.println("mainWindow is null");
    	}
    	Window configurationWindow = (Window)Executions.createComponents("configuration.zul", mainWindow, null);
    	try {
    		configurationWindow.doModal();
//		} catch (SuspendNotAllowedException e) {
//			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}
	
	public void onClick$buttonFullLoad(Event event){
		System.out.println("Load button pressed, starting fullload");
		System.out.println( (String)Sessions.getCurrent().getAttribute("workDir") );
		
		InputActions inputActions = InputActions.getInstance(( (String)Sessions.getCurrent().getAttribute("workDir") ));
		try{
			inputActions.remoteFullLoad();
		}catch(NoSuchBeanDefinitionException e){
			logger.warn("No remote operations available, fallback to local");
			inputActions.fullLoad();
		}
		System.out.println( panelChildrenStatus.getChildren().get(0).getClass() );
		System.out.println("Fullload finished");
	}
	
	public void onClick$buttonLoad(Event event){
		System.out.println("Load button pressed, starting fullload");
		System.out.println( (String)Sessions.getCurrent().getAttribute("workDir") );
		
		InputActions inputActions = InputActions.getInstance(( (String)Sessions.getCurrent().getAttribute("workDir") ));
		try{
			inputActions.remoteLoad();
		}catch(NoSuchBeanDefinitionException e){
			logger.warn("No remote operations available, fallback to local");
			inputActions.load();
		}
		System.out.println("Fullload finished");
	}
	
	public void onClick$buttonUnload(Event event){
		System.out.println("Unload button pressed, starting unload");
		System.out.println( (String)Sessions.getCurrent().getAttribute("workDir") );
		
		InputActions inputActions = InputActions.getInstance(( (String)Sessions.getCurrent().getAttribute("workDir") ));
		inputActions.unload();
		System.out.println("Fullload finished");
	}
	
	public void onClick$buttonClean(Event event){
		System.out.println("Clean button pressed, starting clean");
		System.out.println( (String)Sessions.getCurrent().getAttribute("workDir") );
		
		InputActions inputActions = InputActions.getInstance(( (String)Sessions.getCurrent().getAttribute("workDir") ));
		try{
			inputActions.remoteClean();
		}catch(NoSuchBeanDefinitionException e){
			logger.warn("No remote operations available, fallback to local");
			inputActions.clean();
		}
		
		System.out.println("Clean finished");
	}
	

	public void onClick$buttonReports(Event event){
		System.out.println("Reports button pressed, creating reports");
		System.out.println( (String)Sessions.getCurrent().getAttribute("workDir") );
		ReportGenerator reportGenerator = new ReportGenerator( (String)Sessions.getCurrent().getAttribute("workDir") );
		reportGenerator.createLstRangesBeforeSimReport();
		reportGenerator.finalreport();
		reportGenerator.printLSTRangesReport();
		
		System.out.println("Generation finished");
	}
	
	public void onClick$buttonRun(Event event) {
		System.out.println("Run button pressed");
//		Window simulationWindow = (Window) Path.getComponent("//mainPage/mainWindow/simulationWindow");
//		if (simulationWindow == null)
//			System.out.println("configurationWindow is null");
//		else {
//			simulationWindow.detach();
//			Window mainWindow = (Window) Path.getComponent("//mainPage/mainWindow");
//			Window runningWindow = (Window) Executions.createComponents("running.zul", mainWindow, null);
//			runningWindow.doOverlapped();
//		}



//		Simulator simulator = new Simulator((String)Sessions.getCurrent().getAttribute("workDir"));
//		simulator.run();

                System.out.println("Run button pressed, starting run");
                System.out.println( (String)Sessions.getCurrent().getAttribute("workDir") );

                InputActions inputActions = InputActions.getInstance(( (String)Sessions.getCurrent().getAttribute("workDir") ));
                try{
                        inputActions.remoteRun();
                }catch(NoSuchBeanDefinitionException e){
                        logger.warn("No remote operations available, fallback to local");
                }
                System.out.println("Run finished");

	}

	public void onClick$buttonPh1mSynch(Event event){
		Window mainWindow = (Window) Path.getComponent("//");
    	if( mainWindow == null ){
    		System.out.println("mainWindow is null");
    	}
    	Window ph1mSychToolWindow = (Window)Executions.createComponents("Phase1MSynch.zul", mainWindow, null);
    	try {
    		ph1mSychToolWindow.doModal();
		} catch (SuspendNotAllowedException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void doAfterCompose(Page arg0) throws Exception {
		System.out.println("SimulatorController.doAfterCompose()");
	}

	@Override
	public void doInit(Page arg0, Map arg1) throws Exception {
		System.out.println("SimulatorController.doInit()");
		System.out.println( (String)Sessions.getCurrent().getAttribute("workDir") );
	}
	
	

}