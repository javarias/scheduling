package alma.scheduling.AlmaScheduling.GUI.SchedulingPanel;

//java stuff
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.util.Vector;
import java.util.logging.Logger;
//acs stuff
import alma.acs.container.ContainerServices;
import alma.acs.nc.Consumer;
import alma.acs.util.UTCUtility;
//scheduling stuff
import alma.scheduling.MasterSchedulerIF;
import alma.scheduling.SBLite;
//import alma.scheduling.Define.ArrayTime;
import alma.scheduling.Define.DateTime;
//Control stuff
import alma.Control.ExecBlockStartedEvent;
import alma.Control.ExecBlockEndedEvent;

public class QueuedSchedTab extends JScrollPane implements SchedulerTab {
    private final String[] sbColumnInfo = {"SB Name", "PI", "UID"};
    private ContainerServices container;
    private Logger logger;
    //private JPopupMenu rightClickMenu;
    private MasterSchedulerIF masterScheduler = null;
    private SBLite[] sblites=null;
    private TableModel sbTableModel;
    private TableModel queueTableModel;
    private JTable sbTable;
    private JTable queueTable;
    private JTextField projectTF, piTF;
    private JScrollPane sbliteDisplayPane;
    private JScrollPane queueScrollPane;
    private Object[][] sbRowInfo;
    private Object[][] queueRowInfo;
    private String arrayname;
    private String schedulername;
    private String type;
    private Thread thread;
    private RunQueuedScheduling run;
    private Consumer consumer=null;
    private String[] sb_ids;
    private JPanel mainPanel;
    private JPanel searchPanel;
    private JPanel sbTablePanel;
    private JPanel infoPanel;
    private JPanel queueListPanel;
    private JPanel statusDisplayPanel;

    public QueuedSchedTab(ContainerServices cs, String an){
        container = cs;
        arrayname = an;
        logger = cs.getLogger();
        logger.info("SCHEDULING_PANEL: QueuedScheduling Panel created");
        sbRowInfo = new Object[0][sbColumnInfo.length];
        queueRowInfo = new Object[0][sbColumnInfo.length];
        createStartupView();        
        getViewport().add(mainPanel);
        getMasterSchedulerRef();
        //createRightClickMenu();
        schedulername = "QS_"+arrayname;
        type = "queued";
        try {
            consumer = new Consumer(alma.Control.CHANNELNAME_CONTROLSYSTEM.value, container);
            consumer.addSubscription(alma.Control.ExecBlockStartedEvent.class, this);
            consumer.addSubscription(alma.Control.ExecBlockEndedEvent.class, this);
            consumer.consumerReady();
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("SCHEDULING: Could not get consumer in Queued Scheduling panel.");
        }
    }

        
    private void getMasterSchedulerRef() {
        try {
            masterScheduler = alma.scheduling.MasterSchedulerIFHelper.narrow(
                    container.getDefaultComponent(
                        "IDL:alma/scheduling/MasterSchedulerIF:1.0"));
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createStartupView() {
        mainPanel = new JPanel(new GridLayout(4, 1));
        mainPanel.setBorder(new TitledBorder("Queued Scheduling"));
        mainPanel.add(createQueryView());
        mainPanel.add(createDisplayAllSB());
        mainPanel.add(createDetailDisplayView());
        mainPanel.add(createQueueListView());
        manageTableColumnSize();
   //     return mainPanel;
    }
       
    private JPanel createQueryView(){
        JPanel p = new JPanel(new GridLayout(3,1));
        p.setBorder(new TitledBorder("Search"));
        JPanel top = new JPanel(new FlowLayout());
        top.add(new JLabel("Project Name:"));
        projectTF = new JTextField();
        projectTF.setPreferredSize(new Dimension(50,20));
        top.add(projectTF);
        top.add(new JLabel("PI Name:"));
        piTF = new JTextField();
        piTF.setPreferredSize(new Dimension(50,20));
        top.add(piTF);
        JPanel buttons1 = new JPanel();
        JButton search = new JButton("Search");
        search.setToolTipText("Not Implemented Yet!");
        JButton clear = new JButton("Clear");
        clear.setToolTipText("Clears text in search areas");
        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                projectTF.setText("");
                piTF.setText("");
            }
        });
        JButton allSBs = new JButton("Get All SBs");
        allSBs.setToolTipText("Gets all SBs from archive.");
        allSBs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //get all sbLites
                if(masterScheduler != null){
                    sblites = masterScheduler.getSBLites();
                    updateAllSBList();
                    validate();
                }
            }
        });
        buttons1.add(search);
        buttons1.add(clear);
        buttons1.add(allSBs);
        p.add(top);
        p.add(buttons1);
        return p;
    }

    private JPanel createDisplayAllSB() {
        sbTablePanel = new JPanel(new BorderLayout());
        sbTablePanel.setBorder(new TitledBorder("All SBs"));
        createSBTableModel();
        sbliteDisplayPane= new JScrollPane(sbTable);
        JPanel p = new JPanel();
        p.add(sbliteDisplayPane);
        sbTablePanel.add(p, BorderLayout.CENTER);
        JButton addToQueue = new JButton("Add to Queue");
        addToQueue.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addSBsToQueue();
                    validate();
                }
        });
        addToQueue.setToolTipText("Add selected SBs to scheduler queue");
        JPanel p2 = new JPanel(new GridLayout(1,2));
        p2.add(new JLabel());
        p2.add(addToQueue);
        
        sbTablePanel.add(p2, BorderLayout.SOUTH);
        return sbTablePanel;
    }


    private JPanel createDetailDisplayView(){
        JPanel p = new JPanel();
        p.setBorder(new TitledBorder("SB Display"));
        return p;
    }

    private JPanel createQueueListView(){
        queueListPanel = new JPanel(new BorderLayout());
        queueListPanel.setBorder(new TitledBorder("Queue for Scheduler"));

        createQueueTableModel();
        queueScrollPane = new JScrollPane(queueTable);
        JPanel p = new JPanel();
        p.add(queueScrollPane);
        queueListPanel.add(p, BorderLayout.CENTER);
        
        JButton start = new JButton("Schedule");
        start.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createQueuedSchedulingView();

            } 
        }); 
        start.setToolTipText("Schedule selected SBs");
        JButton clearQueue = new JButton("Clear");
        clearQueue.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    clearQueueTable();
                }
        });
        p = new JPanel(new GridLayout(1,2));
        p.add(start);
        p.add(clearQueue);
        queueListPanel.add(p, BorderLayout.SOUTH);
        return queueListPanel;
    }
    private void clearQueueTable(){
        queueRowInfo = new Object[0][sbColumnInfo.length];
        queueTable.repaint();
        validate();
    }

    private void createSBTableModel(){
        sbTableModel = new AbstractTableModel() {
            public int getColumnCount() { return sbColumnInfo.length; }
            public String getColumnName(int column) { return sbColumnInfo[column]; }
            public int getRowCount() { return sbRowInfo.length;     }
            public Object getValueAt(int row, int col) { return sbRowInfo[row][col]; }
            public void setValueAt(Object val, int row, int col) { sbRowInfo[row][col]= val; }
        };
        sbTable = new JTable(sbTableModel);
        sbTable.doLayout();
        sbTable.setPreferredScrollableViewportSize(new Dimension(200,75));
        sbTable.setToolTipText("Hold the ctrl key down to select multiple SBs");
        sbTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        ((DefaultTableCellRenderer)sbTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        //manageTableColumnSize();//sbTable, sbRowInfo.length);
    }

    private void createQueueTableModel(){
        queueTableModel = new AbstractTableModel() {
            public int getColumnCount() { return sbColumnInfo.length; }
            public String getColumnName(int column) { return sbColumnInfo[column]; }
            public int getRowCount() { return queueRowInfo.length;     }
            public Object getValueAt(int row, int col) { return queueRowInfo[row][col]; }
            public void setValueAt(Object val, int row, int col) { queueRowInfo[row][col]= val; }
        };
        queueTable = new JTable(queueTableModel);
        queueTable.doLayout();
        queueTable.setPreferredScrollableViewportSize(new Dimension(200,75));
        //queueTable.setToolTipText("Hold the ctrl key down to select multiple SBs");
        queueTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        ((DefaultTableCellRenderer)queueTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
    }



    private void manageTableColumnSize(){//JTable t, int rows) {
        TableColumn column;
        int prev=0;
        for(int j=0; j < sbColumnInfo.length; j++){
            column = sbTable.getColumnModel().getColumn(j);
            int w = column.getWidth();
            int n = sbTable.getRowCount();
            for (int i = 0; i < n; i ++) {
                TableCellRenderer r = sbTable.getCellRenderer(i, j);
                Component c = r.getTableCellRendererComponent(
                        sbTable, 
                        sbTable.getValueAt(i, j),
                        false,
                        false,
                        i,
                        j);
                w = Math.max(w, c.getPreferredSize().width);
            }
            //if(prev < 62 || w < 62){
            if( w < 62){
                w = 62;
            }
            column.setPreferredWidth(w+5);
        }
        for(int j=0; j < sbColumnInfo.length; j++){
            column = queueTable.getColumnModel().getColumn(j);
            int w = column.getWidth();
            int n = queueTable.getRowCount();
            for (int i = 0; i < n; i ++) {
                TableCellRenderer r = queueTable.getCellRenderer(i, j);
                Component c = r.getTableCellRendererComponent(
                        queueTable, 
                        queueTable.getValueAt(i, j),
                        false,
                        false,
                        i,
                        j);
                w = Math.max(w, c.getPreferredSize().width);
            }
            //if(prev < 62 || w < 62){
            if( w < 62){
                w = 62;
            }
            column.setPreferredWidth(w+5);
        }
    }
    
    private void updateAllSBList() {
        if(sblites != null){
            sbRowInfo = new Object[sblites.length][sbColumnInfo.length];
            for(int i=0; i < sblites.length; i++){
                sbRowInfo[i][0] = sblites[i].sbName;
                sbRowInfo[i][1] = sblites[i].PI;
                sbRowInfo[i][2] = sblites[i].schedBlockRef;
            }
            sbTable.repaint();
            System.out.println("got "+ sblites.length+" sb lites ");
            sbTablePanel.setBorder(new TitledBorder("All SBs"));
            sbTablePanel.repaint();
            manageTableColumnSize();
            sbTable.revalidate();
            validate();
        }
    }

    private void addSBsToQueue() {
        //get list of sbs to add,
        int[] rows = sbTable.getSelectedRows();
        if(rows.length == 0 || rows == null){
            JOptionPane.showMessageDialog(this, 
                    "You need to select at least one SB",
                    "Nothing Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if(queueRowInfo.length ==0) {
            queueRowInfo = new Object[rows.length][sbColumnInfo.length];
            System.out.println("row len = "+rows.length);
            for(int i=0;i< rows.length;i++){
                queueRowInfo[i][0] = sbRowInfo[rows[i]][0];
                queueRowInfo[i][1] = sbRowInfo[rows[i]][1];
                queueRowInfo[i][2] = sbRowInfo[rows[i]][2];
            }
        } else {
            //check their uid to make sure they're not in queue already
            Vector<Object[]> newQueue = new Vector<Object[]>();
            for(int i=0; i < queueRowInfo.length; i++){
                newQueue.add(queueRowInfo[i]);
            }
            Object[][] possibleSBsToAdd = new Object[rows.length][sbColumnInfo.length] ;
            for(int i=0; i < rows.length; i++){
                possibleSBsToAdd[i] = sbRowInfo[rows[i]];
            }   
            boolean add = true;
            int ctr =0;
            for(int i=0; i< possibleSBsToAdd.length; i++){
                for(int j=0; j < queueRowInfo.length; j++){
                    if(possibleSBsToAdd[i][2] == queueRowInfo[j][2]){
                        System.out.println("already there not adding");
                        add = false;
                        break;
                    }
                }
                if(add){
                    newQueue.add(possibleSBsToAdd[i]);
                }
                add= true;
            }
            queueRowInfo = new Object[newQueue.size()][sbColumnInfo.length];
            for(int i=0; i< newQueue.size();i++){
                queueRowInfo[i] = newQueue.elementAt(i);
                System.out.println("queue row info = "+ queueRowInfo[i][2]);
            }
        }

        System.out.println(queueRowInfo.length);
        queueTable.repaint();
        queueScrollPane.repaint();
        queueListPanel.repaint();
        manageTableColumnSize();
        queueTable.revalidate();
        validate();
    }

    private void createQueuedSchedulingView() {
        if(queueRowInfo.length == 0){
            JOptionPane.showMessageDialog(this, 
                    "You need to add at least one SB to queue",
                    "Nothing added", JOptionPane.WARNING_MESSAGE);
            return;
        }
        displayQueuedSchedulerView();
        startQueuedScheduler();
        manageTableColumnSize();
        validate();
    }
    private void displayQueuedSchedulerView() {
        try {
            mainPanel.removeAll();
        } catch(Exception e){}
        mainPanel.setLayout(new GridLayout(2, 1));
        mainPanel.setBorder(new TitledBorder("Queued Scheduling"));
        queueListPanel = new JPanel(new BorderLayout());
        queueListPanel.setBorder(new TitledBorder("Scheduler's SBs"));
        createQueueTableModel();
        queueScrollPane = new JScrollPane(queueTable);
        System.out.println(queueRowInfo.length);
        JPanel p = new JPanel();
        p.add(queueScrollPane);
        queueListPanel.add(p, BorderLayout.CENTER);
        JPanel p2 = new JPanel();
        JButton stop = new JButton("Stop Current SB");
        stop.setToolTipText("Not yet Implemented!");
        p2.add(new JLabel());
        p2.add(stop);
        p2.add(new JLabel());
        queueListPanel.add(p2, BorderLayout.SOUTH);
        queueListPanel.repaint();
        mainPanel.add(queueListPanel);
        mainPanel.validate();
    }

    private void startQueuedScheduler() {
        //get uids of sbs to send to MS to make queued scheduler
        sb_ids = new String[queueRowInfo.length];
        for(int i=0; i < queueRowInfo.length; i++){
            sb_ids[i] = (String)queueRowInfo[i][2];
            System.out.println(sb_ids[i]);
        }
        try {
            run = new RunQueuedScheduling(
                    container,sb_ids, arrayname);
            thread = new Thread(run);
            thread.start();
        } catch(Exception e){
            e.printStackTrace();
        }
        
    }
    // Receive methods for NC
    public void receive(ExecBlockStartedEvent e){
        System.out.println("Got ExecBlockStartedEvent");
        String exec_id = e.execId.entityId;
        String sbid = e.sbId.entityId;
        boolean belongs = ebForThisScheduler(sbid);
        DateTime start_time = new DateTime(UTCUtility.utcOmgToJava(e.startTime));
    }
    public void receive(ExecBlockEndedEvent e){
        System.out.println("Got ExecBlockEndedEvent");
        String exec_id = e.execId.entityId;
        String sbid = e.sbId.entityId;
        DateTime end_time = new DateTime(UTCUtility.utcOmgToJava(e.endTime));
    }

    /**
      * Takes the sb id that comes with the exec block event and compares it to the 
      * sbs being run by this scheduler.
      * @return true if there is a match
      */
    private boolean ebForThisScheduler(String sb){
        for(int i=0; i < sb_ids.length; i++){
            if(sb_ids[i].equals(sb)){
                return true;
            }
        }
        return false;
    }

//SchedulerTab interface implementation
    public String getSchedulerName(){
        return schedulername;
    }
    
    public String getArrayName(){
        return arrayname;
    }
    
    public String getSchedulerType(){
        return type;
    }

    public void exit() {
        try{
            container.releaseComponent(masterScheduler.name());
            try {
                run.stop();
            }catch(Exception e){}
            run = null;
            thread = null;
            consumer.disconnect();
        } catch(Exception e){
            e.printStackTrace();
        }
        System.out.println("Exiting Queued Scheduler on array "+arrayname);
    }


///////////////////////////////////////    

///////////////////////////////////////    
    class PopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e){
            maybeShowPopup(e);
        }
        public void mouseReleased(MouseEvent e){
            maybeShowPopup(e);
        }
        private void maybeShowPopup(MouseEvent e){
            if (e.isPopupTrigger()) {
                //rightClickMenu.show(e.getComponent(),
                 //      e.getX(), e.getY());
            }
        }
    }    
}