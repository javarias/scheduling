package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import alma.scheduling.SBLite;
import alma.scheduling.ProjectLite;
import alma.exec.extension.subsystemplugin.PluginContainerServices;

public class InteractiveSchedTab extends SchedulingPanelGeneralPanel implements SchedulerTab {
    private String schedulerName;
    private String arrayName;
    private String type;
    
    private ArchiveSearchFieldsPanel archiveSearchPanel;
    private JPanel middlePanel;
    private JPanel bottomPanel;
    private SBTable sbs;
    private ProjectTable projects; 
    
    public InteractiveSchedTab(PluginContainerServices cs, String schedName, String aName){
        super();
        super.onlineSetup(cs);
        schedulerName = schedName;
        arrayName = aName;
        type = "interactive"; 
        createLayout();
    }
    /////////// SchedulerTab stuff /////
    public String getSchedulerName(){
        return schedulerName;
    }
    public String getArrayName(){
        return arrayName;
    }
    public String getSchedulerType(){
        return type;
    }
    public void exit(){
    }
    ////////////////////////////////////
    
    private void createLayout(){
        setBorder(new TitledBorder("Interactive Scheduling"));
        setLayout(new BorderLayout());
        createTopPanel();
        createMiddlePanel();
        createBottomPanel();
        Dimension d = getPreferredSize();
        add(archiveSearchPanel,BorderLayout.NORTH);
        add(middlePanel,BorderLayout.CENTER);
        add(bottomPanel,BorderLayout.SOUTH);
    }
    /**
      * Top panel contains check boxes for determining if we
      * search by project or by sb.
      */
    public void createTopPanel() {
        archiveSearchPanel = new ArchiveSearchFieldsPanel();
        archiveSearchPanel.setOwner(this);
        archiveSearchPanel.connected(true);
    }

    /**
      * Middle panel contains he search text boxes and the buttons.
      */
    public void createMiddlePanel() {
        middlePanel = new JPanel( new BorderLayout());
        JPanel projectPanel = new JPanel();
        projectPanel.setBorder(new TitledBorder("Projects Found"));
        projects = new ProjectTable(new Dimension(175,100));
        projects.setOwner(this);
        JScrollPane pane1 = new JScrollPane(projects);
        projectPanel.add(pane1);
        middlePanel.add(projectPanel, BorderLayout.WEST);

        JPanel sbPanel = new JPanel(new BorderLayout());
        sbs = new SBTable(true, new Dimension(175,75));
        sbs.setOwner(this);
        sbPanel.setBorder(new TitledBorder("SBs Found"));
        JScrollPane pane2 = new JScrollPane(sbs);
        sbPanel.add(pane2,BorderLayout.CENTER);
        //add execute button and stop button
        JPanel buttons = new JPanel(new GridLayout(1,2));
        buttons.add(new JButton("Execute"));
        buttons.add(new JButton("Stop"));
        sbPanel.add(buttons, BorderLayout.SOUTH);
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
    
    public void updateSBView(SBLite[] sblites){
        sbs.setRowInfo(sblites);
    }

    public void updateProjectView(ProjectLite[] projectLites) {
        projects.setRowInfo(projectLites);
    }

    public void clearTables() {
        sbs.clear();
        projects.clear();
    }


}
