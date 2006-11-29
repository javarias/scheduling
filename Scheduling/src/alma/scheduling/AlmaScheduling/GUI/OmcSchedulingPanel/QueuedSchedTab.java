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
    private JPanel middlePanel;
    private JPanel bottomPanel;
    private boolean searchingOnProject;
    private SBTable sbs;
    private SBTable queueSBs;
    private ProjectTable projects;
    private JTextArea executionInfo;
    private JButton addB;
    private JButton removeB;
    private JButton executeB;
    private JButton stopB;
    
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
        createMiddlePanel();
        createBottomPanel();
        Dimension d = getPreferredSize();
        add(archiveSearchPanel,BorderLayout.NORTH);
        add(middlePanel,BorderLayout.CENTER);
        add(bottomPanel,BorderLayout.SOUTH);
    }
    private void createTopPanel() {
        archiveSearchPanel = new ArchiveSearchFieldsPanel();
        archiveSearchPanel.setOwner(this);
        archiveSearchPanel.connected(true);
    }

    private void createMiddlePanel(){
        middlePanel = new JPanel(new GridLayout(1,2));//new BorderLayout());
        JPanel projectPanel = new JPanel();
        projectPanel.setBorder(new TitledBorder("Projects Found"));
        projects = new ProjectTable(new Dimension(175,100));
        projects.setOwner(this);
        JScrollPane pane1 = new JScrollPane(projects);
        projectPanel.add(pane1);
        middlePanel.add(projectPanel);//, BorderLayout.WEST);

        JPanel sbPanel = new JPanel(new BorderLayout());
        sbs = new SBTable(false, new Dimension(175,75));
        sbs.setOwner(this);
        sbPanel.setBorder(new TitledBorder("SBs Found"));
        JScrollPane pane2 = new JScrollPane(sbs);
        sbPanel.add(pane2,BorderLayout.CENTER);
        addB = new JButton("Add to Queue");
        addB.setToolTipText("Will add SB to queue.");
        addB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                addSBsToQueue();
            }
        });
        JPanel buttons = new JPanel(new GridLayout(1,2));
        buttons.add(addB);
        sbPanel.add(buttons, BorderLayout.SOUTH);
        middlePanel.add(sbPanel);//, BorderLayout.EAST);

    }

    private void createBottomPanel(){
        bottomPanel = new JPanel(new GridLayout(1,2));
        //bottomPanel = new JPanel(new BorderLayout());
        //have the following:
        //a sbtable for queue sbs, 
        JPanel p1 = new JPanel(new BorderLayout());
        queueSBs = new SBTable(true, new Dimension(175,75));
        queueSBs.setOwner(this);
        JScrollPane queueSbPane = new JScrollPane(queueSBs);
        p1.add(queueSbPane, BorderLayout.CENTER);
        //a button to remove selected ones
        JPanel buttonPanel = new JPanel();//new GridLayout(1,2));
        removeB = new JButton("Remove");
        removeB.setToolTipText("Will remove SB from queue.");
        removeB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                removeSBsFromQueue();
            }
        });
        buttonPanel.add(removeB);
        executeB = new JButton("Execute Queue");
        executeB.setToolTipText("Will execute the queue.");
        executeB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                executeSBs();
            }
        });
        buttonPanel.add(executeB);
        stopB = new JButton ("Stop SB");
        stopB.setToolTipText("Will stop the current SB and move to the next SB.");
        stopB.setEnabled(false);
        stopB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                stopSB();
            }
        });
        //buttonPanel.add(stopB);
        p1.add(buttonPanel, BorderLayout.SOUTH);
        
        bottomPanel.add(p1);//, BorderLayout.WEST);
        //a text area which displays process
        executionInfo = new JTextArea();
        executionInfo.setEditable(false);
        //Dimension d = new Dimension(75, 75);
        JScrollPane pane = new JScrollPane(executionInfo);
        //pane.setPreferredSize(d);
        
        bottomPanel.add(pane);//, BorderLayout.EAST);
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
        sbs.setRowInfo(sb);
    }
    
    public void updateExecutionInfo(String info){
        executionInfo.append(info);
    }
    private void executeSBs(){
        //get all ids from the queueSB table and send them to control
        controller.runQueuedScheduling(queueSBs.getAllSBIds());
    }
    
    public void setSBStatus(String sbid, String status){
        queueSBs.setSBExecStatus(sbid, status);
    }
    
    private void stopSB(){
    }
    
    private void addSBsToQueue(){
        //get selected SBs from sbTable
        String[] selectedSBs = sbs.getSelectedSBs();
        SBLite[] sbs = controller.getSBLites(selectedSBs);
        //pass these to queuedSBTable
        queueSBs.setRowInfo(sbs);
    }
    private void removeSBsFromQueue(){
        //remove selected sb from QueuedSbTable
        //and update view/scheduler/etc
    }
}
