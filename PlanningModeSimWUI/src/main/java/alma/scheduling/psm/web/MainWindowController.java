package alma.scheduling.psm.web;

import java.util.HashMap;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.api.Button;
import org.zkoss.zul.api.Menuitem;
import org.zkoss.zul.api.Toolbarbutton;
import org.zkoss.zul.api.Window;

public class MainWindowController extends GenericForwardComposer {
	
	private static final long serialVersionUID = 8408410035368708515L;
	private Menuitem menuItemNew;
    private Menuitem menuItemOpen;
    private Menuitem menuItemSave;
    private Menuitem menuItemSaveAs;
    private Menuitem menuItemExit;
    private Button buttonClick;
    private Toolbarbutton toolbarButtonOpen;
 
    public void onClick$menuItemNew(Event event) {
       	System.out.println("menuItemNew pressed");
    	Window mainWindow = (Window) Path.getComponent("//");
    	if( mainWindow == null ){
    		System.out.println("mainWindow is null");
    	}
    	Window configurationWindow = (Window)Executions.createComponents("configuration.zul", mainWindow, null);
    	try {
    		configurationWindow.doModal();
		} catch (SuspendNotAllowedException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
    }
    
    public void onClick$menuItemOpen(Event event) { 
    	System.out.println("menuItemOpen pressed");
    	Window mainWindow = (Window) Path.getComponent("//");
    	if( mainWindow == null ){
    		System.out.println("mainWindow is null");
    	}
    	Window simulationWindow = (Window) Executions.createComponents("simulation.zul", mainWindow, null);
		simulationWindow.doOverlapped();
    } 
 
    public void onClick$menuItemBeforeSimBand(Event event) {
    	Window mainWindow = (Window) Path.getComponent("//");
    	if( mainWindow == null ){
    		System.out.println("mainWindow is null");
    	}
    	HashMap<String, String> param = new HashMap<String, String>();
    	param.put("id", "reportWindowBeforeSimBand");
    	param.put("title", "Frequency Bands Usage (Before simulation)");
    	param.put("controller", ReportBandBeforeSimController.class.getCanonicalName());
    	Window reportsWindow = (Window) Executions.createComponents("reports.zul", mainWindow , param);
    	reportsWindow.doOverlapped();
    }
    
    public void onClick$menuItemBeforeSimLST(Event event) {
    	Window mainWindow = (Window) Path.getComponent("//");
    	if( mainWindow == null ){
    		System.out.println("mainWindow is null");
    	}
    	HashMap<String, String> param = new HashMap<String, String>();
    	param.put("id", "reportWindowBeforeSimLST");
    	param.put("title", "Right Ascension Distribution (Before simulation)");
    	param.put("controller", ReportLstBeforeSimController.class.getCanonicalName());
    	Window reportsWindow = (Window) Executions.createComponents("reports.zul", mainWindow , param);
    	reportsWindow.doOverlapped();
    }
    
    public void onClick$menuItemAfterSimExec(Event event) {
    	Window mainWindow = (Window) Path.getComponent("//");
    	if( mainWindow == null ){
    		System.out.println("mainWindow is null");
    	}
    	HashMap<String, String> param = new HashMap<String, String>();
    	param.put("id", "reportWindowAfterSimExec");
    	param.put("title", "Executive Percentage Balance");
    	param.put("controller", ReportExecAfterSimController.class.getCanonicalName());
    	Window reportsWindow = (Window) Executions.createComponents("reports.zul", mainWindow , param);
    	reportsWindow.doOverlapped();
    }
    
    public void onClick$menuItemAfterSimLST(Event event) {
    	Window mainWindow = (Window) Path.getComponent("//");
    	if( mainWindow == null ){
    		System.out.println("mainWindow is null");
    	}
    	HashMap<String, String> param = new HashMap<String, String>();
    	param.put("id", "reportWindowAfterSimLST");
    	param.put("title", "Right Ascension Distribution (After simulation)");
    	param.put("controller",  ReportLstAfterSimController.class.getCanonicalName());
    	Window reportsWindow = (Window) Executions.createComponents("reports.zul", mainWindow , param);
    	reportsWindow.doOverlapped();
    }
    
    public void onClick$menuItemAfterSimBand(Event event) {
    	Window mainWindow = (Window) Path.getComponent("//");
    	if( mainWindow == null ){
    		System.out.println("mainWindow is null");
    	}
    	HashMap<String, String> param = new HashMap<String, String>();
    	param.put("id", "reportWindowAfterSimBand");
    	param.put("title", "Frequency Bands Usage (After simulation)");
    	param.put("controller", ReportBandAfterSimController.class.getCanonicalName());
    	Window reportsWindow = (Window) Executions.createComponents("reports.zul", mainWindow , param);
    	reportsWindow.doOverlapped();
    }
    
    public void onClick$menuItemObsProjectCompletion(Event event) {
    	Window mainWindow = (Window) Path.getComponent("//");
    	if( mainWindow == null ){
    		System.out.println("mainWindow is null");
    	}
    	HashMap<String, String> param = new HashMap<String, String>();
    	param.put("id", "reportWindowAfterSimCompletion");
    	param.put("title", "Observation Projects Completion");
    	param.put("controller", ReportObsProjectCompletionController.class.getCanonicalName());
    	Window reportsWindow = (Window) Executions.createComponents("reports.zul", mainWindow , param);
    	reportsWindow.doOverlapped();
    }
    
    
    public void onClick$menuItemHelp(Event event) {
    	Window helpWindow = (Window) Path.getComponent("/mainWindow/helpWindow");
    	if( helpWindow == null ){
    		System.out.println("helpWindow is null");
    		Window mainWindow = (Window) Path.getComponent("//");
        	if( mainWindow == null ){
        		System.out.println("mainWindow is null");
        	}
    		helpWindow = (Window) Executions.createComponents("help.zul", mainWindow , null);
    	}    	
    	helpWindow.doOverlapped();
    }
    
    public void onClick$menuItemDistribution(Event event) {
    	Window distributionWindow = (Window) Path.getComponent("/mainWindow/distributionWindow");
    	if( distributionWindow == null ){
    		System.out.println("distributionWindow is null");
    		Window mainWindow = (Window) Path.getComponent("//");
        	if( mainWindow == null ){
        		System.out.println("mainWindow is null");
        	}
        	distributionWindow = (Window) Executions.createComponents("distribution.zul", mainWindow , null);
    	}    	
    	distributionWindow.doOverlapped();
    }
    
    public void onClick$menuItemAbout(Event event) {
    	Window aboutWindow = (Window) Path.getComponent("/mainWindow/aboutWindow");
    	if( aboutWindow == null ){
    		System.out.println("aboutWindow is null");
    		Window mainWindow = (Window) Path.getComponent("//");
        	if( mainWindow == null ){
        		System.out.println("mainWindow is null");
        	}
        	aboutWindow = (Window) Executions.createComponents("about.zul", mainWindow , null);
    	}    	
    	aboutWindow.doOverlapped();
    }

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
    	Window mainWindow = (Window) Path.getComponent("//");
    	if( mainWindow == null ){
    		System.out.println("mainWindow is null");
    	}
    	Window simulationWindow = (Window) Executions.createComponents("simulation.zul", mainWindow, null);
		simulationWindow.doOverlapped();
	}
    
    
}