package alma.scheduling.psm.web;

import java.util.Map;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.http.SimpleSession;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zk.ui.util.Initiator;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.api.Button;
import org.zkoss.zul.Panel;
import org.zkoss.zul.api.Window;

import alma.scheduling.input.config.generated.Configuration;
import alma.scheduling.psm.util.PsmContext;

public class ConfigurationController extends GenericForwardComposer implements Initiator {

	private static final long serialVersionUID = -7010591114060503476L;
	private Button buttonCancel;
	private Button buttonSave;
	private Button buttonReset;
	private Textbox workDirectory;
	private Textbox projectDirectory;
	private Textbox weatherDirectory;
	private Textbox observatoryDirectory;
	private Textbox executiveDirectory;
	private Textbox outputDirectory;
	private Textbox reportDirectory;
	private Textbox contextFilePath;
	private Textbox arrayCenterLatitude;
	private Textbox arrayCenterLongitude;
	private Textbox maxWindSpeed;
	private Panel advancedConfiguration;

	public void onClick$buttonCancel(Event event) {
		System.out.println("Cancel button pressed");
		Window configurationWindow = (Window) Path
				.getComponent("//mainPage/mainWindow/configurationWindow");
		if (configurationWindow == null)
			System.out.println("configurationWindow is null");
		else {
			configurationWindow.detach();
		}

	}

	public void onClick$buttonAccept(Event event) {
		System.out.println("Accept button pressed");
		
		// Saving aprc-config.xml file to disk
		PsmContext psmCtx = new PsmContext( (String)Sessions.getCurrent().getAttribute("workDir") );
		System.out.println( ((Configuration)Sessions.getCurrent().getAttribute("configuration")).getProjectDirectory() );
		psmCtx.saveAprcConfig((Configuration) Sessions.getCurrent().getAttribute("configuration"));
		
		// Closing configuration window
		Window configurationWindow = (Window) Path
				.getComponent("//mainPage/mainWindow/configurationWindow");
		if (configurationWindow == null)
			System.out.println("configurationWindow is null");
		else {
			configurationWindow.detach();
		}
		
		// Opening simulation window.
		Window mainWindow = (Window) Path.getComponent("//");
    	if( mainWindow == null ){
    		System.out.println("mainWindow is null");
    	}else{
    		Window simulationWindow = (Window) Executions.createComponents("simulation.zul", mainWindow, null);
    		simulationWindow.doOverlapped();
    	}    	
	}

	public void onClick$buttonReset(Event event) {
		System.out.println("Reset button pressed");
		defaultValues();
	}
	
	public void onOpen$configurationWindow(Event event) {
		System.out.println("ConfigurationWindow loaded");
		defaultValues();
	}
	
	public void defaultValues(){
		workDirectory.setValue("tmp");
		projectDirectory.setValue("projects");
		weatherDirectory.setValue("weather");
		observatoryDirectory.setValue("observatory");
		executiveDirectory.setValue("executive");
		outputDirectory.setValue("output");
		reportDirectory.setValue("reports");
		contextFilePath.setValue("context.xml");
		arrayCenterLatitude.setValue("-23.022894444444443");
		arrayCenterLongitude.setValue("-67.75492777777778");
		maxWindSpeed.setValue("40.0");
	}

	@Override
	public void doAfterCompose(Page arg0) throws Exception {
		System.out.println("doAfterCompose(Page) called");		
	}

	@Override
	public void doInit(Page arg0, Map arg1) throws Exception {
		System.out.println("Configuration init() called");
		arg0.setVariable("configuration", Sessions.getCurrent().getAttribute("configuration"));
	}
}