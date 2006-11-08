package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.awt.*;
import java.util.EventListener;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.util.logging.Logger;

import alma.exec.extension.subsystemplugin.PluginContainerServices;

public class SearchArchiveOnlyTab extends JPanel {
    private PluginContainerServices container;
    private Logger logger;
    private ArchiveSearchFieldsPanel topPanel;
    private JPanel middlePanel;
    private JPanel bottomPanel;
    private SBTable sbs;
    private ProjectTable projects;

    public SearchArchiveOnlyTab(){
        setBorder(new TitledBorder("Search Archive"));
        setLayout(new BorderLayout());
        createTopPanel();
        createMiddlePanel();
        createBottomPanel();
        Dimension d = getPreferredSize();
        add(topPanel,BorderLayout.NORTH);
        add(middlePanel,BorderLayout.CENTER);
        add(bottomPanel,BorderLayout.SOUTH);
        
    }

    public void connectedSetup(PluginContainerServices cs){
        container = cs;
        logger = cs.getLogger();
        topPanel.setCS(cs);
    }

    /**
      * Top panel contains check boxes for determining if we
      * search by project or by sb.
      */
    public void createTopPanel() {
        topPanel = new ArchiveSearchFieldsPanel();
    }

    /**
      * Middle panel contains he search text boxes and the buttons.
      */
    public void createMiddlePanel() {
        middlePanel = new JPanel( new BorderLayout());
        JPanel projectPanel = new JPanel();
        projectPanel.setBorder(new TitledBorder("Projects Found"));
        projects = new ProjectTable(new Dimension(175,100));
        JScrollPane pane1 = new JScrollPane(projects);
        projectPanel.add(pane1);
        middlePanel.add(projectPanel, BorderLayout.WEST);

        JPanel sbPanel = new JPanel();
        sbs = new SBTable(false, new Dimension(175,100));
        sbPanel.setBorder(new TitledBorder("SBs Found"));
        JScrollPane pane2 = new JScrollPane(sbs);
                                   
        sbPanel.add(pane2);
        middlePanel.add(sbPanel, BorderLayout.EAST);
        
    }
    private void createBottomPanel(){
        bottomPanel = new JPanel(new BorderLayout());
        JPanel p1=new JPanel();
        p1.setBorder(new TitledBorder("Project Details"));
        p1.add(projects.getProjectInfoView());
        bottomPanel.add(p1, BorderLayout.WEST);
        JPanel p2 = new JPanel();
        p2.setBorder(new TitledBorder("Project Details"));
        p2.add(sbs.getSBInfoView());
        bottomPanel.add(p2, BorderLayout.EAST);
        
    }

}
