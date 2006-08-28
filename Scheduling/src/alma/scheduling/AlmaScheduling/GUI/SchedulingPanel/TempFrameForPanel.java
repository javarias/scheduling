package alma.scheduling.AlmaScheduling.GUI.SchedulingPanel;

import java.awt.event.*;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JFrame;

import alma.acs.container.ContainerServices;
import alma.acs.component.client.ComponentClient;

public class TempFrameForPanel extends JFrame{
    private ContainerServices cs;
    private MainSchedTabPane mainSchedPanel;
    public TempFrameForPanel(ContainerServices cs){
        super("SchedulingPanel");
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

        createMainSchedPanel(cs);
        add(mainSchedPanel);
        setVisible(true);
    }

    public void exit(){
        System.out.println("Calling exit in TempFrameForSchedulingPanel");
        mainSchedPanel.exit();
        dispose();
    }

    private void createMainSchedPanel(ContainerServices cs) {
        //mainSchedPanel = new MainSchedTabPane(cs);
    }

    public static void main(String[] args) {
        try {
            ComponentClient client = new ComponentClient(null, System.getProperty("ACS.manager"),"SP");
            TempFrameForPanel p = new TempFrameForPanel(client.getContainerServices());
        } catch( Exception e){
            TempFrameForPanel p = new TempFrameForPanel(null);
        }
    }
}
