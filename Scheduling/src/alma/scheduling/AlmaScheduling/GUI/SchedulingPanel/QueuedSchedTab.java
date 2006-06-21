package alma.scheduling.AlmaScheduling.GUI.SchedulingPanel;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.logging.Logger;
import alma.acs.container.ContainerServices;
import alma.scheduling.QueuedScheduling;

public class QueuedSchedTab extends JScrollPane {
    private ContainerServices container;
    private Logger logger;
    private QueuedScheduling schedulerComp;
    
    public QueuedSchedTab(ContainerServices cs){
        container = cs;
        logger = cs.getLogger();
        logger.info("SCHEDULING_PANEL: QueuedScheduling Panel created");
        getViewport().add(createMainView());        
        System.out.println(getClass().toString());
    }

    private JPanel createMainView() {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setBorder(new TitledBorder("QueuedScheduling"));
        p.add(createQueryView());
        p.add(createDisplayView());
        return p;
    }
       
    private JPanel createQueryView(){
        JPanel p = new JPanel();
        p.setBorder(new TitledBorder("Search"));
        p.add(new JLabel("Project Name:"));
        p.add(new JTextField());
        p.add(new JLabel("PI Name:"));
        p.add(new JTextField());
        JButton search = new JButton("Search");
        JButton clear = new JButton("Clear");
        JButton allSBs = new JButton("Get All SBs");

        return p;
    }
    private JPanel createDisplayView(){
        JPanel p = new JPanel();
        p.setBorder(new TitledBorder("SB Display"));
        return p;
    }

    
    private void createRightClickMenu() {
    }
}
