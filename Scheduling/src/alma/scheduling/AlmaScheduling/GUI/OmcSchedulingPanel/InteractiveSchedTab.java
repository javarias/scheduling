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
    //private String schedulerName;
    private String arrayName;
    private String type;
    
    private ArchiveSearchFieldsPanel archiveSearchPanel;
    private InteractiveSchedTabController controller;
    private JPanel middlePanel;
    private JPanel bottomPanel;
    private JButton execB;
    private JButton stopB;
    private SBTable sbs;
    private ProjectTable projects; 
  //  private boolean sessionStarted;
    private boolean searchingOnProject; 

    public InteractiveSchedTab(PluginContainerServices cs, String aName){
        super();
        super.onlineSetup(cs);
        searchingOnProject=true;
        arrayName = aName;
        controller = new InteractiveSchedTabController(cs, arrayName, this);
        controller.setArrayInUse(aName);
        controller.getISRef();
        type = "interactive"; 
        createLayout();
        archiveSearchPanel.setCS(cs);
        projects.setCS(cs);
        sbs.setCS(cs);
        setEnable(true);
        doInitialSearch();
    }
    private void doInitialSearch() {
        archiveSearchPanel.doSearch();
    }
    public void selectFirstResult(){
        projects.showFirstProject();
      
    }
    /////////// SchedulerTab stuff /////
    public String getSchedulerName(){
        return controller.getSchedulerName();
    }
    public String getArrayName(){
        return arrayName;
    }
    public String getSchedulerType(){
        return type;
    }
    public void exit(){
    //    controller.releaseISRef();
     //   controller.releaseArray(arrayName);
        controller.stopInteractiveScheduling();
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
        middlePanel = new JPanel(new GridLayout(1,2));// new BorderLayout());
        JPanel projectPanel = new JPanel();
        projectPanel.setBorder(new TitledBorder("Projects Found"));
        projects = new ProjectTable(new Dimension(175,100));
        projects.setOwner(this);
        JScrollPane pane1 = new JScrollPane(projects);
        projectPanel.add(pane1);
        middlePanel.add(projectPanel);//, BorderLayout.WEST);

        JPanel sbPanel = new JPanel(new BorderLayout());
        sbs = new SBTable(true, new Dimension(175,75));
        sbs.setOwner(this);
        sbPanel.setBorder(new TitledBorder("SBs Found"));
        JScrollPane pane2 = new JScrollPane(sbs);
        sbPanel.add(pane2,BorderLayout.CENTER);
        execB = new JButton("Execute");
        execB.setToolTipText("Will execute the selected SB.");
        execB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
            //disable searching while a SB is executing...
                executeSB();
            }
        });
        stopB = new JButton("Stop");
        stopB.setToolTipText("Will stop the currently running SB on this array");
        stopB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                stopSB();
            }
        });
        JPanel buttons = new JPanel(new GridLayout(1,2));
        buttons.add(execB);
        buttons.add(stopB);
        sbPanel.add(buttons, BorderLayout.SOUTH);
        middlePanel.add(sbPanel);//, BorderLayout.EAST);
        
    }
    private void createBottomPanel(){
        bottomPanel = new JPanel(new GridLayout(1,2));//new BorderLayout());
        JPanel p1=new JPanel();
        p1.setBorder(new TitledBorder("Project Details"));
        p1.add(projects.getProjectInfoView());
        bottomPanel.add(p1);//, BorderLayout.WEST);
        JPanel p2 = new JPanel();
        p2.setBorder(new TitledBorder("SB Details"));
        p2.add(sbs.getSBInfoView());
        bottomPanel.add(p2);//, BorderLayout.EAST);
        
    }

    /**
      * If true then search fields & execute button are enabled and stop disabled
      */
    public void setEnable(boolean b) {
        archiveSearchPanel.setPanelEnabled(b);
        execB.setEnabled(b);
        stopB.setEnabled(!b);
    }
    /**
      *
      */
    public void setSearchMode(boolean b) {
        searchingOnProject = b;
        projects.setSearchMode(b);
        sbs.setSearchMode(b);
    }
    
    public void updateSBView(SBLite[] sblites){
        sbs.setRowInfo(sblites);
        sbs.selectFirstSB();
    }

    public void updateProjectView(ProjectLite[] projectLites) {
        projects.setRowInfo(projectLites);
    }

    public void clearTables() {
        sbs.clear();
        projects.clear();
    }

    private void executeSB(){
        try {
            String sbId =sbs.returnSelectedSBId();
            if(!sbId.equals("")){
                controller.executeSB(sbId);
                setEnable(false);
                setSBStatus(sbId, "RUNNING");//eventually do this with exec block started event
            }
            //check if a sb has been selected.
        }catch(Exception e){
            e.printStackTrace();
            showErrorPopup(e.toString(), "executeSB");
        }
    }
    private void stopSB(){
        try {
            controller.stopSB();
            setEnable(true);
        } catch(Exception e) {
            e.printStackTrace();
            showErrorPopup(e.toString(), "stopSB");
        }
    }
    public void setSBStatus(String sb, String status){
        sbs.setSBExecStatus(sb, status);
    }

 /*   public void showErrorPopup(String error,String method) {
        JOptionPane.showMessageDialog(this, error, method, JOptionPane.ERROR_MESSAGE);
    }
    public void showWarningPopup(String warning, String method) {
        JOptionPane.showMessageDialog(this, warning, method, JOptionPane.WARNING_MESSAGE);
    }*/
}
