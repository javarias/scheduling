package alma.scheduling.AlmaScheduling.GUI.SchedulingPanel;
//java stuff
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.util.logging.Logger;
//scheduling stuff
import alma.scheduling.*;

//acs stuff
//import alma.acs.container.*;
import alma.xmlentity.XmlEntityStruct;

import alma.exec.extension.subsystemplugin.PluginContainerServices;

public class DynamicSchedTab extends JScrollPane implements SchedulerTab {
    private final String[] sbColumnInfo = {"SB Name", "PI", "UID"};
    private Logger logger;
    private String schedulername;
    private String arrayname;
    private String type;
    private PluginContainerServices container;
    private MasterSchedulerIF masterScheduler;
    private JPanel mainPanel;
    private JPanel sbTablePanel;
    private JPanel sbDetailPanel;
    private JPanel schedulerDetails;
    private JPanel bottomView;
    private TableModel sbTableModel;
    private JTable sbTable;
    private Object[][] sbRowInfo;
    private boolean schedulerCreateArray;

//////////////////////////////////////
    public DynamicSchedTab(PluginContainerServices cs, String s, String an){
        container = cs;
        logger = cs.getLogger();
        schedulername = s;
        arrayname = an;
        if(arrayname.equals("")){
            schedulerCreateArray = true;
        } else{
            schedulerCreateArray = false;
        }
        type = "dynamic";
        getComponentRefs();
        mainPanel = new JPanel(new GridLayout (3,1));
        sbRowInfo= new Object[0][sbColumnInfo.length];
        createDynamicSchedulingView();
        getViewport().add(mainPanel);
    }

    private void createDynamicSchedulingView() {
        try{
            mainPanel.removeAll();
        } catch(Exception e) {/* don't care if it complains*/ }
        mainPanel.setBorder(new TitledBorder("Dynamic Scheduling"));
        //JPanel p = new JPanel(new GridLayout (3,1));
        //JPanel p = new JPanel(
        createSBTableView();
        mainPanel.add(sbTablePanel);
        createSBDetails();
        mainPanel.add(sbDetailPanel);
        //createBottomView();
        //mainPanel.add(bottomView);
        //mainPanel.add(p);

        validate();
        try {
            startScheduling();
    
        } catch (Exception e){
            e.printStackTrace();
            logger.severe("SCHEDULING_PANEL: dynamic scheduling did not start due to "+ e.toString());
                    
        }
    }

    private void createSBTableView(){
        createSBTable();
        JScrollPane pane = new JScrollPane(sbTable);
        sbTablePanel = new JPanel();
        sbTablePanel.setBorder(new TitledBorder("Scheduler's SBs"));
        sbTablePanel.add(pane);
    }

    private void createSBTable() {
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
        //sbTable.setToolTipText("Hold the ctrl key down to select multiple SBs");
    }

    private void createSBDetails(){
        sbDetailPanel = new JPanel();
        sbDetailPanel.setBorder(new TitledBorder("SB Display"));
    }

    private void createBottomView(){
        bottomView = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new GridLayout(2,1));
        JButton b;
        b = new JButton("Select");
        b.setToolTipText("Schedule the selected SB");
        buttonPanel.add(b);
        b = new JButton("Default");
        b.setToolTipText("Schedule the default SB");
        buttonPanel.add(b);
        
        bottomView.add(buttonPanel, BorderLayout.EAST);
    }

    private void getComponentRefs() {
        try {
            masterScheduler = alma.scheduling.MasterSchedulerIFHelper.narrow(
                    container.getDefaultComponent(
                        "IDL:alma/scheduling/MasterSchedulerIF:1.0"));
        }catch(Exception e){
            e.printStackTrace();
        }
        logger.info("Got master scheduler reference");
    }
    
//////////////////////////////////////

    private void startScheduling() throws Exception {
        XmlEntityStruct policy = new XmlEntityStruct();
        if(schedulerCreateArray){
            masterScheduler.startScheduling(policy);
        } else {
            masterScheduler.startScheduling1(policy, arrayname);
        }
    }
//////////////////////////////////////


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
            //logger.info("About to release "+scheduler.name());
//            container.releaseComponent(scheduler.name());
            container.releaseComponent(masterScheduler.name());
//            consumer.disconnect();
        } catch(Exception e){
            e.printStackTrace();
        }
        //System.out.println("Exiting dynamic Scheduler on array "+arrayname);
    }

///////////////////////////////////////     


}
