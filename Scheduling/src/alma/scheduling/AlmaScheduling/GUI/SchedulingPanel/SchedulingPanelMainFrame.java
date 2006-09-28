package alma.scheduling.AlmaScheduling.GUI.SchedulingPanel;

//java stuff
import java.awt.event.*;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.BorderLayout;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import java.util.logging.Logger;
//acs stuff
//import alma.acs.container.ContainerServices;
//import alma.acs.component.client.ComponentClient;

//exec plugin stuff
import alma.exec.extension.subsystemplugin.*;

public class SchedulingPanelMainFrame extends JPanel implements SubsystemPlugin {
//    private ContainerServices cs;
    private PluginContainerServices cs;
    private MainSchedTabPane mainSchedPanel;
    private Logger logger;
    
    public SchedulingPanelMainFrame(){
        setLayout(new BorderLayout());
        createMainSchedPanel(cs);
        add(mainSchedPanel,BorderLayout.CENTER);
    }

    public void setServices(PluginContainerServices ctrl) {
        cs = ctrl;
        logger = ctrl.getLogger();
    }

    public void start() throws Exception {
        createMainSchedPanel(cs);
        add(mainSchedPanel, BorderLayout.CENTER);
        setVisible(true);
    }
    public void stop() throws Exception{
        exit();
    }
    public boolean runRestricted(boolean b) throws Exception {
        return b;
    }

    public void exit(){
        logger.info("Calling exit in ExecFrameForSchedulingPanel");
        mainSchedPanel.exit();
    }


    private void createMainSchedPanel(PluginContainerServices cs) {
        mainSchedPanel = new MainSchedTabPane(cs);
    }

}
