package alma.scheduling.AlmaScheduling.GUI.SchedulingPanel;

//java stuff
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
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
    private JTable sbTable;
    private JTextField projectTF, piTF;
    private JScrollPane sbliteDisplayPane;
    private Object[][] sbRowInfo;
    private String arrayname;
    private String schedulername;
    private String type;
    private Thread thread;
    private RunQueuedScheduling run;
    private Consumer consumer=null;
    private String[] sb_ids;

    public QueuedSchedTab(ContainerServices cs, String an){
        container = cs;
        arrayname = an;
        logger = cs.getLogger();
        logger.info("SCHEDULING_PANEL: QueuedScheduling Panel created");
        sbRowInfo = new Object[0][sbColumnInfo.length];
        getViewport().add(createStartupView());        
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

    private JPanel createStartupView() {
        JPanel p = new JPanel(new GridLayout(4, 1));
        p.setBorder(new TitledBorder("QueuedScheduling"));
        p.add(createQueryView());
        p.add(createDisplayAllSB());
        p.add(createDetailDisplayView());
        p.add(createStatusInfoView());
        return p;
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
        JPanel buttons2 = new JPanel();//new GridLayout(1,3,15,15));
        JButton start = new JButton("Schedule");
        start.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            //pass the selected SBs to the master scheduler
            //and then the MS will create a queued scheduler.
            // the tab's view will then change to show only the 
            //sbs which were passed to the queued scheduler.
                //showSchedulerSBList();
                createQueuedSchedulingView();

            } 
        }); 
        start.setToolTipText("Schedule selected SBs");
        buttons2.add(new JLabel());
        buttons2.add(start);
        buttons2.add(new JLabel());
        p.add(top);
        p.add(buttons1);
        p.add(buttons2);
        return p;
    }

    private JPanel createDisplayAllSB() {
        JPanel p = new JPanel();
        p.setBorder(new TitledBorder("All SBs"));
        createSBTableModel();
        sbliteDisplayPane= new JScrollPane(sbTable);
        p.add(sbliteDisplayPane);
        return p;
    }
    
    private JPanel createDetailDisplayView(){
        JPanel p = new JPanel();
        p.setBorder(new TitledBorder("SB Display"));
        return p;
    }

    private JPanel createStatusInfoView(){
        JPanel p = new JPanel();
        p.setBorder(new TitledBorder("Scheduler Status Display"));
        return p;
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
        Dimension d = new Dimension(200,75);
        sbTable.setPreferredScrollableViewportSize(d);
        sbTable.setToolTipText("Hold the ctrl key down to select multiple SBs");
    }
    
    private void updateAllSBList() {
        if(sblites != null){
            sbRowInfo = new Object[sblites.length][sbColumnInfo.length];
            for(int i=0; i < sblites.length; i++){
                sbRowInfo[i][0] = sblites[i].sbName;
                sbRowInfo[i][1] = sblites[i].PI;
                sbRowInfo[i][2] = sblites[i].schedBlockRef;
            }
            JPanel p =(JPanel) sbTable.getParent().getParent().getParent();
            p.setBorder(new TitledBorder("All SBs"));
            sbTable.repaint();
            sbTable.revalidate();
            validate();
        }
    }

    private void createRightClickMenu() {
        ////rightClickMenu = new JPopupMenu();
        JMenuItem menuItem;
        menuItem = new JMenuItem("Start Queued Scheduling");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            //pass the selected SBs to the master scheduler
            //and then the MS will create a queued scheduler.
            // the tab's view will then change to show only the 
            //sbs which were passed to the queued scheduler.
                //showSchedulerSBList();
                createQueuedSchedulingView();

            } 
        });
        //rightClickMenu.add(menuItem);
        addMouseListener(new PopupListener());
    }

    private void createQueuedSchedulingView() {
        int[] rows = sbTable.getSelectedRows();
        if(rows.length == 0 || rows == null){
            JOptionPane.showMessageDialog(this, 
                    "You need to select at least one SB",
                    "Nothing Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        getViewport().removeAll();
        JPanel p = new JPanel(new GridLayout(4, 1));
        p.setBorder(new TitledBorder("QueuedScheduling"));
        p.add(createDisplayAllSB());
        showSchedulerSBList(rows);
        getViewport().add(p);
        startQueuedScheduler();
    }

    private void showSchedulerSBList(int[] rows) {
        //System.out.println(rows.length);
        //System.out.println(sbRowInfo.length);
        Object[][] sbsForScheduler = new Object[rows.length][3];
        for(int i=0; i <  rows.length; i++){
            sbsForScheduler[i] = sbRowInfo[rows[i]];
        }
        sbRowInfo = new Object[sbsForScheduler.length][3];
        for(int i=0; i < sbsForScheduler.length; i++){
            sbRowInfo[i][0] = sbsForScheduler[i][0];
            sbRowInfo[i][1] = sbsForScheduler[i][1];
            sbRowInfo[i][2] = sbsForScheduler[i][2];
        }
        JPanel p =(JPanel) sbTable.getParent().getParent().getParent();
        p.setBorder(new TitledBorder("Scheduler's SBs"));
        sbTable.repaint();
        sbTable.revalidate();
        validate();
    }

    private void startQueuedScheduler() {
        //get uids of sbs to send to MS to make queued scheduler
        sb_ids = new String[sbRowInfo.length];
        for(int i=0; i < sbRowInfo.length; i++){
            sb_ids[i] = (String)sbRowInfo[i][2];
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
