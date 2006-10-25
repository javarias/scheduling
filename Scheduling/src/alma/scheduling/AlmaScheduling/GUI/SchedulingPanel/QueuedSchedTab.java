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

import alma.exec.extension.subsystemplugin.PluginContainerServices;


public class QueuedSchedTab extends JScrollPane implements SchedulerTab {
    private final String[] sbColumnInfo = {"SB Name", "PI","Exec Status", "UID"};
    //private ContainerServices container;
    private PluginContainerServices container;
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
    private JTextArea statusDisplayTA;

    //public QueuedSchedTab(ContainerServices cs, String an){
    public QueuedSchedTab(PluginContainerServices cs, String an){
        container = cs;
        arrayname = an;
        logger = cs.getLogger();
        logger.info("SCHEDULING_PANEL: QueuedScheduling Panel created");
        sbRowInfo = new Object[0][sbColumnInfo.length];
        queueRowInfo = new Object[0][sbColumnInfo.length];
        createStartupView();        
        getViewport().add(mainPanel);
        getMSRef();
        //createRightClickMenu();
        schedulername = "QS_"+arrayname;
        masterScheduler.setArrayInUse(arrayname);
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

        
    private void getMSRef() {
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
        infoPanel = createDetailDisplayView();
        mainPanel.add(infoPanel);
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
        sbTable.addMouseListener(new MouseListener(){
            public void mouseClicked(MouseEvent e) {
                showSBInfo();
            }
            public void mouseEntered(MouseEvent e){ }
            public void mouseExited(MouseEvent e){ }
            public void mousePressed(MouseEvent e){ }
            public void mouseReleased(MouseEvent e){}
        });

        sbTable.doLayout();
        sbTable.setPreferredScrollableViewportSize(new Dimension(200,75));
        sbTable.setToolTipText("Hold the ctrl key down to select multiple SBs");
        sbTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        ((DefaultTableCellRenderer)sbTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        if( sbRowInfo.length == 1){
            sbTable.getSelectionModel().setSelectionInterval(0,0);
            showSBInfo();
        }        
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
        //if( queueRowInfo.length == 1){
        //    queueTable.getSelectionModel().setSelectionInterval(0,0);
        //}
    }

    private void showSBInfo(){
        try {
            infoPanel.removeAll();
        } catch(Exception e) { /*dont' care if it complains */ }
        
        if(!checkSomethingSelected()){
            return;
        }
        if(!checkOneSelected()){
            return;
        }
         
        int row = sbTable.getSelectedRow();
        String id =(String)sbRowInfo[row][3];
        JTextArea info = new JTextArea();
        info.setEditable(false);
        SBLite sb = getSBLiteForId(id);
        try{
            info.append("SB Name: "+sb.sbName+"\n");
            info.append("Priority: "+sb.priority+"\n");
            info.append("RA: "+sb.ra+"\n");
            info.append("DEC: "+sb.dec+"\n");
            info.append("Frequency: "+sb.freq+"\n");
            info.append("Score: "+sb.score+"\n");
            info.append("Success: "+sb.success+"\n");
            info.append("Rank: "+sb.rank+"\n");
        } catch(Exception e){
            info.append("Problem getting SB's info.\n");
            info.append(e.toString()+"\n");
        }
        JScrollPane pane = new JScrollPane(info);
        infoPanel.add(pane);
        infoPanel.repaint();
        validate();
    }

    private SBLite getSBLiteForId(String id) {
        for(int i=0; i< sblites.length; i++){
            if(sblites[i].schedBlockRef.equals(id) ){
                return sblites[i];
            }
        }
        return null;
    }

    private boolean hasSearchBeenDone(){
        if(sbTable == null) {
            return false;
        }
        return true;
    }
    private boolean checkSomethingSelected() {
        if(!hasSearchBeenDone()){
            JOptionPane.showMessageDialog(this,
                "Do a search first!",
                "Nothing Searched", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        int[] rows = sbTable.getSelectedRows();
        if(rows.length ==0 ) {
            JOptionPane.showMessageDialog(this,
                "Select one!",
                "Nothing Selected", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
    private boolean checkOneSelected(){
        if(!hasSearchBeenDone()){
            JOptionPane.showMessageDialog(this,
                "Do a search first!",
                "Nothing Searched", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        int[] rows = sbTable.getSelectedRows();
        if(rows.length > 1) {
            JOptionPane.showMessageDialog(this,
                "Select only one.",
                "Too Many Selected", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
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
                sbRowInfo[i][2] = "Waiting";
                sbRowInfo[i][3] = sblites[i].schedBlockRef;
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
                queueRowInfo[i][3] = sbRowInfo[rows[i]][3];
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
                    if(possibleSBsToAdd[i][3] == queueRowInfo[j][3]){ //comparing UIDs which are in last column.
                        System.out.println("already there, will be executed twice.");
                        //check repeat count, add if not maxed out repeat count.
                        //add = false; TODO add this back in once its possible to check the repeat count.
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
                System.out.println("queue row info = "+ queueRowInfo[i][3]); //uid
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
        mainPanel.setLayout(new GridLayout(4, 1));
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
        //execution log window.
        statusDisplayPanel = new JPanel(new GridLayout(1,1));
        statusDisplayTA = new JTextArea(40,40);
        statusDisplayTA.setLineWrap(true);
        statusDisplayTA.setEditable(false); //read only log!
        JScrollPane sp = new JScrollPane(statusDisplayTA);
        statusDisplayPanel.add(sp);
        mainPanel.add(statusDisplayPanel);
        mainPanel.validate();
    }

    private void startQueuedScheduler() {
        //get uids of sbs to send to MS to make queued scheduler
        sb_ids = new String[queueRowInfo.length];
        for(int i=0; i < queueRowInfo.length; i++){
            sb_ids[i] = (String)queueRowInfo[i][3]; //uid
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
        //System.out.println("Got ExecBlockStartedEvent");
        String exec_id = e.execId.entityId;
        String sbid = e.sbId.entityId;
        boolean belongs = ebForThisScheduler(sbid);
        DateTime start_time = new DateTime(UTCUtility.utcOmgToJava(e.startTime));
        if(belongs) {
            String sbname = getSBNameFromID(sbid);
            if(sbname.startsWith("Something odd happened")){
                statusDisplayTA.append(sbname+"\n");
            } else {
                System.out.println("Got ExecBlockStartedEvent and sb belongs");
                statusDisplayTA.append("Execution started for SB: "+sbname+"\n");
                updateSBStatusInfoInTable(sbid, "RUNNING");
            }
        }
    }
    public void receive(ExecBlockEndedEvent e){
        String exec_id = e.execId.entityId;
        String sbid = e.sbId.entityId;
        DateTime end_time = new DateTime(UTCUtility.utcOmgToJava(e.endTime));
        boolean belongs = ebForThisScheduler(sbid);
        if(belongs) {
            String sbname = getSBNameFromID(sbid);
            if(sbname.startsWith("Something odd happened")){
                statusDisplayTA.append(sbname+"\n");
            } else {
                System.out.println("Got ExecBlockEndedEvent and sb belongs");
                statusDisplayTA.append("Execution ended for SB: "+sbname+"\n");
                String completion;
                switch(e.status.value()) {
                    case 0:
                        completion ="FAILED";
                        break;
                    case 1:
                        completion ="SUCCESS";
                        break;
                    case 2:
                        completion ="PARTIAL";
                        break;
                    case 3:
                        completion ="TIMEOUT";
                        break;
                    default:
                        completion ="ERROR";
                        break;
                }
                statusDisplayTA.append("Execution completion status = "+completion+"\n");
                updateSBStatusInfoInTable(sbid, completion);
            }
        }
        statusDisplayPanel.validate();
    }
    private String getSBNameFromID(String id) {
        for(int i=0; i < sblites.length; i++){
            if(sblites[i].schedBlockRef.equals(id)){
                return sblites[i].sbName;
            }
        }
        return "Something odd happened.. sb ("+id+") somehow thought it was part of this session.";
    }

    private void updateSBStatusInfoInTable(String id, String status) {
        try {
        for(int i=0; i < queueRowInfo.length; i++) {
            if(queueRowInfo[i][3] == id){ //comparing uids
                queueRowInfo[i][2] = status; //updating status if uids match
                queueTable.validate();
                queueTable.repaint();
                queueListPanel.validate();
                validate();
                logger.info("Table should be updated now!");
                return;
            }
        }
        } catch(Exception e){
        e.printStackTrace();
        }
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
        logger.info("EXIT IN QS");
        try {
            masterScheduler.destroyArray(arrayname);
        }catch(Exception e){
            //e.printStackTrace();
            logger.warning("QUEUED_SP: Problem destroying array, could already have been destroyed");

        }
        try{
            container.releaseComponent(masterScheduler.name());
            if(thread != null) {
                try {
                    thread.interrupt();
                    run.stop();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
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
