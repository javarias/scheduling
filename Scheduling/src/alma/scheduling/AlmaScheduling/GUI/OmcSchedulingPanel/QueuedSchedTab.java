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

public class QueuedSchedTab extends SchedulingPanelGeneralPanel implements SchedulerTab {

    private String schedulerName;
    private String arrayName;
    private String type;
    private QueuedSchedTabController controller;
    private ArchiveSearchFieldsPanel archiveSearchPanel;
    //private JPanel middlePanel;
    //private JPanel bottomPanel;
    private JPanel centerPanel;
    private boolean searchingOnProject;
    private SBTable sbs;
    private SBTable queueSBs;
    private ProjectTable projects;
    private JTextArea executionInfo;
    private JButton addB;
    private JButton removeB;
    private JButton executeB;
    private JButton stopB;
    private int currentExecutionRow;
    
    
    public QueuedSchedTab(String title, String aName){
        type = "queued";
        arrayName = aName;
        searchingOnProject = true;
        schedulerName = title;
        createLayout();
    }
    public QueuedSchedTab(PluginContainerServices cs, String title, String aName){
        super();
        super.onlineSetup(cs);
        type = "queued";
        arrayName = aName;
        searchingOnProject = true;
        schedulerName = title;
        controller = new QueuedSchedTabController(cs, this, aName);
        createLayout();
        archiveSearchPanel.setCS(cs);
        projects.setCS(cs);
        sbs.setCS(cs);
        queueSBs.setCS(cs);
        doInitialSearch();
    }
    private void doInitialSearch(){
        archiveSearchPanel.doSearch();
    }
    public void selectFirstResult(){
        projects.showFirstProject();
    }

///////////////////////////////
    public void exit(){
        controller.stopQueuedScheduling();
    }
    public String getSchedulerType(){
        return type;
    }
    public String getArrayName() {
        return arrayName;
    }
    public String getSchedulerName() {
        return schedulerName;
    }
///////////////////////////////
    private void createLayout(){
        setBorder(new TitledBorder("Queued Scheduling"));
        setLayout(new BorderLayout());
        //setLayout(new GridLayout(3,1));
        createTopPanel();
        //createMiddlePanel();
        //createBottomPanel();
        Dimension d = getPreferredSize();
        add(archiveSearchPanel,BorderLayout.NORTH);
        add(createCenterPanel(),BorderLayout.CENTER);
        //JPanel tablePanel = new JPanel(new GridLayout(2,1));
        //tablePanel.add(middlePanel);
        //tablePanel.add(bottomPanel);
        //add(tablePanel, BorderLayout.CENTER);
        //add(middlePanel,BorderLayout.CENTER);
        //add(bottomPanel,BorderLayout.SOUTH);
    }
    private void createTopPanel() {
        archiveSearchPanel = new ArchiveSearchFieldsPanel();
        archiveSearchPanel.setOwner(this);
        archiveSearchPanel.connected(true);
    }

    public JPanel createCenterPanel() {
        centerPanel = new JPanel(new GridLayout(2,2));
        //first row: left hand cell == Project Table
        projects = new ProjectTable(new Dimension(150,75));
        projects.setOwner(this);
        JScrollPane projectPane = new JScrollPane(projects,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        projectPane.setBorder(new TitledBorder("Projects Found"));

        centerPanel.add(projectPane);

        //first row: right hand cell == sbTable + button
        JPanel sbPanel = new JPanel(new BorderLayout());
        sbs = new SBTable(false, new Dimension(150,60));
        sbs.setOwner(this);
        JScrollPane sbPane = new JScrollPane(sbs,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        sbPane.setBorder(new TitledBorder("SBs Found"));
        sbPanel.add(sbPane,BorderLayout.CENTER);
        addB = new JButton("Add to Queue");
        addB.setToolTipText("Will add SB to queue.");
        addB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                addSBsToQueue();
            }
        });
        JPanel button1 = new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
        button1.add(addB);
        sbPanel.add(button1, BorderLayout.SOUTH);

        centerPanel.add(sbPanel);
        //second row: left hand cell == sbTable + buttons (PROBLEMATIC ONE!)
        JPanel queuePanel = new JPanel(new BorderLayout());
        queueSBs = new SBTable(true, new Dimension(150,60));
        queueSBs.setOwner(this);
        JScrollPane queueSbPane = new JScrollPane(queueSBs,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        //queueSbPane.setMaximumSize( new Dimension(130,60));
        queueSbPane.setBorder(new TitledBorder("SB Queue"));
        

        removeB = new JButton("Remove");
        removeB.setToolTipText("Will remove SB from queue.");
        removeB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                removeSBsFromQueue();
            }
        });

        executeB = new JButton("Run");
        executeB.setToolTipText("Will execute the queue.");
        executeB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                executeSBs();
            }
        });
        stopB = new JButton ("Stop");
        stopB.setToolTipText("Will stop the current SB and move to the next SB.");
        stopB.setEnabled(false);
        stopB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                stopSB();
            }
        });
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0,0));
        buttonPanel.add(removeB);
        buttonPanel.add(executeB);
        buttonPanel.add(stopB);

        queuePanel.add(queueSbPane, BorderLayout.CENTER);
        queuePanel.add(buttonPanel, BorderLayout.SOUTH);

        centerPanel.add(queuePanel);
        
        //second row: right hand cell == execution info text area
        executionInfo = new JTextArea();
        executionInfo.setEditable(false);
        JScrollPane taPane = new JScrollPane(executionInfo,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        taPane.setBorder(new TitledBorder("Execution Info"));

        centerPanel.add(taPane);

        return centerPanel;
    }
    

    public void setEnable(boolean b){
    }

    public void setSearchMode(boolean b) {
        searchingOnProject =b;
        projects.setSearchMode(b);
        sbs.setSearchMode(b);
    }

    public void clearTables() {
        sbs.clear();
        //queueSBs.clear();
        projects.clear();
    }
    
    public void updateProjectView(ProjectLite[] p){
        projects.setRowInfo(p);
    }
    
    public void updateSBView(SBLite[] sb){
        sbs.setRowInfo(sb, false);
    }
    
    public void updateExecutionInfo(String info){
        executionInfo.append(info);
    }
    private void executeSBs(){
        //get all ids from the queueSB table and send them to control
        currentExecutionRow =0;
        controller.runQueuedScheduling(queueSBs.getAllSBIds());
    }
    public void updateExecutionRow(){
        currentExecutionRow++;
    }
    
    public void setSBStatus(String sbid, String status){
        queueSBs.setSBExecStatusForRow(currentExecutionRow, sbid, status);
    }
    
    private void stopSB(){
    }
    
    private void addSBsToQueue(){
        //get selected SBs from sbTable
        String[] selectedSBs = sbs.getSelectedSBs();
        SBLite[] sbs = controller.getSBLites(selectedSBs);
        //pass these to queuedSBTable
        queueSBs.setRowInfo(sbs, true);
    }
    private void removeSBsFromQueue(){
        //remove selected sb from QueuedSbTable
        queueSBs.removeRowsFromQueue();
        //and update view/scheduler/etc
    }
}
