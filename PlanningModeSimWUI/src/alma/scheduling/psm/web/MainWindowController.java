package alma.scheduling.psm.web;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
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
 
}