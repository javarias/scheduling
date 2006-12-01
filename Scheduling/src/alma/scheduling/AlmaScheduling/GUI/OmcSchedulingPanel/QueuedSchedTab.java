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
    private int currentExecutionRow;
    
    /*
    public QueuedSchedTab(String title, String aName){
        type = "queued";
        arrayName = aName;
        searchingOnProject = true;
        schedulerName = title;
        createLayout();
    }*/
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
        JPanel projectPanel = new JPanel();//new BorderLayout());
        projectPanel.setBorder(new TitledBorder("Projects Found"));
        projects = new ProjectTable(new Dimension(170,100));
        projects.setOwner(this);
        JScrollPane pane1 = new JScrollPane(projects);
        projectPanel.add(pane1);//, BorderLayout.CENTER);
        middlePanel.add(projectPanel);//, BorderLayout.WEST);

        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0; c.weighty = 1.0;
        
        JPanel sbPanel = new JPanel(gridbag);//new BorderLayout());
        sbs = new SBTable(false, new Dimension(170,75));
        sbs.setOwner(this);
        sbPanel.setBorder(new TitledBorder("SBs Found"));
        JScrollPane pane2 = new JScrollPane(sbs);
        
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(pane2,c);
        sbPanel.add(pane2);//,BorderLayout.CENTER);
        
        addB = new JButton("Add to Queue");
        addB.setToolTipText("Will add SB to queue.");
        addB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                addSBsToQueue();
            }
        });
        JPanel buttons = new JPanel(new BorderLayout());
        //JPanel buttons = new JPanel(new GridLayout(1,1));
        buttons.add(addB, BorderLayout.CENTER);
        sbPanel.add(buttons);//, BorderLayout.SOUTH);
        middlePanel.add(sbPanel);//, BorderLayout.EAST);

    }

    private void createBottomPanel(){
        bottomPanel = new JPanel(new GridLayout(1,2));
        //bottomPanel = new JPanel(new BorderLayout());
        //have the following:
        //a sbtable for queue sbs, 
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0; c.weighty = 1.0;
        
        JPanel p1 = new JPanel(gridbag);//new BorderLayout());
        p1.setBorder(new TitledBorder("SB Queue"));
        queueSBs = new SBTable(true, new Dimension(170,75));
        queueSBs.setOwner(this);
        JScrollPane queueSbPane = new JScrollPane(queueSBs);
        
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(queueSbPane,c);
        
        p1.add(queueSbPane);//, BorderLayout.CENTER);
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
        c.gridwidth = 1;
        gridbag.setConstraints(buttonPanel,c);
        p1.add(buttonPanel);//, BorderLayout.SOUTH);
        
        bottomPanel.add(p1);//, BorderLayout.WEST);
        //a text area which displays process
        executionInfo = new JTextArea();
        executionInfo.setEditable(false);
        //executionInfo.setMaximumSize(new Dimension(170,50));
        //Dimension d = new Dimension(75, 75);
        JScrollPane pane = new JScrollPane(executionInfo);
        //pane.setMaximumSize(new Dimension(170,50));
        pane.setPreferredSize(new Dimension(170,50));
        JPanel taPanel = new JPanel(new GridLayout(1,1));
        taPanel.setBorder(new TitledBorder("Execution Info"));
        taPanel.add(pane);
        bottomPanel.add(taPanel);//, BorderLayout.EAST);
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
