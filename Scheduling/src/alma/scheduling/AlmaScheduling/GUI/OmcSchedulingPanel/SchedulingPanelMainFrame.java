package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

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
import javax.swing.JScrollPane;
import java.util.logging.Logger;
//exec plugin stuff
import alma.exec.extension.subsystemplugin.*;

public class SchedulingPanelMainFrame extends JPanel implements SubsystemPlugin {
    private PluginContainerServices cs;
    private JPanel main;
    private JScrollPane pane;
    private MainSchedTabPane mainSchedPanel;
    private Logger logger;
    
    public SchedulingPanelMainFrame(){
        createMainSchedPanel();
        main = new JPanel();
        Dimension d = getPreferredSize();
        main.setSize(d.width + 5, d.height + 5);
        /*
        pane = new JScrollPane(mainSchedPanel);
        main.add(pane);
        */
        main.add(mainSchedPanel);
        add(main);
        setVisible(true);
    }

    public void setServices(PluginContainerServices ctrl) {
        cs = ctrl;
        logger = ctrl.getLogger();
        mainSchedPanel.secondSetup(cs);
    }

    public void start() throws Exception {
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


    private void createMainSchedPanel() {
        mainSchedPanel = new MainSchedTabPane();
    }

}
