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
        Dimension d = getPreferredSize();
        add(archiveSearchPanel,BorderLayout.NORTH);
        add(middlePanel,BorderLayout.CENTER);
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
        middlePanel = new JPanel(new GridLayout(2,2));

        //first row: lefthand cell = project table
        projects = new ProjectTable(new Dimension(150,75));
        projects.setOwner(this);
        JScrollPane projectPane = new JScrollPane(projects,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        projectPane.setBorder(new TitledBorder("Projects Found"));
        middlePanel.add(projectPane);

        //first row: right hand side cell: sb table & buttons
        JPanel sbPanel = new JPanel(new BorderLayout());
        sbs = new SBTable(true, new Dimension(150,60));
        sbs.setOwner(this);
        JScrollPane sbPane = new JScrollPane(sbs,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        sbPane.setBorder(new TitledBorder("SBs Found"));
        sbPanel.add(sbPane,BorderLayout.CENTER);
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
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
        buttons.add(execB);
        buttons.add(stopB);
        sbPanel.add(buttons, BorderLayout.SOUTH);
        middlePanel.add(sbPanel);
        
       //second row: left hand cell = project info textarea
        middlePanel.add(projects.getProjectInfoView());

       //second row: right hand cell = sb info textarea
        middlePanel.add(sbs.getSBInfoView());
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
        sbs.setRowInfo(sblites, false);
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
        ExecuteSBThread exec = new ExecuteSBThread();
        Thread t = controller.getCS().getThreadFactory().newThread(exec);
        t.start();
    }
    private void stopSB(){
        StopSBThread stop = new StopSBThread();
        Thread t = controller.getCS().getThreadFactory().newThread(stop);
        t.start();
    }
    public void setSBStatus(String sb, String status){
        sbs.setSBExecStatus(sb, status);
    }

    /*
<<<<<<< InteractiveSchedTab.java

    class ExecuteSBThread implements Runnable {
        public ExecuteSBThread() {
        }
        public void run() {
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
    }

    class StopSBThread implements Runnable{
        public StopSBThread() {
        }
        public void run() {
            try {
                controller.stopSB();
                //so they can't press stop twice!
                stopB.setEnabled(false);
            } catch(Exception e) {
                e.printStackTrace();
                showErrorPopup(e.toString(), "stopSB");
            }
        }
=======
    */

    class ExecuteSBThread implements Runnable {
        public ExecuteSBThread() {
        }
        public void run() {
            try {
                String sbId =sbs.returnSelectedSBId();
                if(!sbId.equals("")){
                    controller.executeSB(sbId);
                    //setEnable(false);
                    //setSBStatus(sbId, "RUNNING");//eventually do this with exec block started event
                }
                //check if a sb has been selected.
            }catch(Exception e){
                e.printStackTrace();
                showErrorPopup(e.toString(), "executeSB");
            }
        }
    }

    class StopSBThread implements Runnable{
        public StopSBThread() {
        }
        public void run() {
            try {
                controller.stopSB();
                //so they can't press stop twice!
                stopB.setEnabled(false);
            } catch(Exception e) {
                e.printStackTrace();
                showErrorPopup(e.toString(), "stopSB");
            }
        }
    }
}
