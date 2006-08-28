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

public class ExecFrameForPanel extends JPanel implements SubsystemPlugin {
//    private ContainerServices cs;
    private PluginContainerServices cs;
    private MainSchedTabPane mainSchedPanel;
    private Logger logger;
    
    public ExecFrameForPanel(){
        /*
        int inset = 250;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset, screenSize.width - inset*2, 
            screenSize.height - inset*2);
        setSize(325, 600);
        
        JMenuBar menubar = new JMenuBar();
        JMenu filemenu = new JMenu("File");
        filemenu.setMnemonic(KeyEvent.VK_F);
        JMenuItem exit = new JMenuItem("Exit", KeyEvent.VK_X);
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                exit();
            }
        });
        filemenu.add(exit);
        menubar.add(filemenu);
        setJMenuBar(menubar);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });

        */
        setLayout(new BorderLayout());
        createMainSchedPanel(cs);
        add(mainSchedPanel,BorderLayout.CENTER);
        setVisible(true);
    }

    public void setServices(PluginContainerServices ctrl) {
        cs = ctrl;
        logger = ctrl.getLogger();
    }

    public void start() throws Exception {
        createMainSchedPanel(cs);
        add(mainSchedPanel);
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
