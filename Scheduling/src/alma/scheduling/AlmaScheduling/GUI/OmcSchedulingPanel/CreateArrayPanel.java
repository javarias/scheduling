package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;
import java.util.Vector;
import javax.swing.table.*;
import java.util.logging.Logger;
import alma.acs.container.ContainerServices;
import alma.exec.extension.subsystemplugin.PluginContainerServices;

import alma.Control.ControlMaster;
import alma.scheduling.ArrayModeEnum;
import alma.scheduling.MasterSchedulerIF;

public class CreateArrayPanel extends JPanel {

    private MasterSchedulerIF masterScheduler;
    private ControlMaster control;
    private Logger logger;
    private PluginContainerServices container;
    private TableModel antennaTableModelA;
    private TableModel antennaTableModelB;
    private JTable antennaTableA;
    private JTable antennaTableB;
    private Object[][] antennaRowInfoA;
    private Object[][] antennaRowInfoB;
    private String[] availableAntennas;
    private Vector<String> allArrays;
    private int columnIndex = 0;
    private JButton addToArray;
    private JButton removeFromArray;
    private JButton createArrayB;
    private JButton cancelB;
    private String arrayMode;
    
    public CreateArrayPanel() {
        super();
        super.setBorder(new TitledBorder("Create Array"));
        allArrays = new Vector<String>();
        setSize(400,300);
        add(createAntennaColumns(), BorderLayout.CENTER);
    }

    public void connectedSetup(PluginContainerServices cs) {
        this.container = cs;
        this.logger = cs.getLogger();
    }

    public void setEnabled(boolean enabled){
        antennaTableA.setEnabled(enabled);
        antennaTableB.setEnabled(enabled);
        createArrayB.setEnabled(enabled);
        cancelB.setEnabled(enabled);
        removeFromArray.setEnabled(enabled);
        addToArray.setEnabled(enabled);
        if(enabled){
        }
        repaint();
        validate();
    }
    
    private JPanel createAntennaColumns() {
        JPanel p = new JPanel(new BorderLayout());
        availableAntennas = new String[0];
        p.add(createAntennaListA(), BorderLayout.WEST);
        p.add(transferButtons(),BorderLayout.CENTER);
        p.add(createAntennaListB(), BorderLayout.EAST);
        p.add(createSouthPanel(),BorderLayout.SOUTH);
        manageColumnSizesInA();
        manageColumnSizesInB();
        return p;
    }

    private void updateAntennaRowInfoA() {
        antennaRowInfoA = new Object[availableAntennas.length][1];
        for(int i=0; i < availableAntennas.length; i++){
            antennaRowInfoA[i][0] = availableAntennas[i];
        }
    }
    private JPanel createAntennaListA(){
        JPanel p = new JPanel();
        final String[] antennaColumnInfoA= {"Antenna Name"};
        updateAntennaRowInfoA();
        antennaTableModelA = new AbstractTableModel(){
            public int getColumnCount() { return antennaColumnInfoA.length; }
            public String getColumnName(int column) { return antennaColumnInfoA[column]; }
            public int getRowCount() { return antennaRowInfoA.length;     }
            public Object getValueAt(int row, int col) { return antennaRowInfoA[row][col]; }
            public void setValueAt(Object val, int row, int col) { antennaRowInfoA[row][col]= val; }
        };

        antennaTableA = new JTable(antennaTableModelA);
        //antennaTableA.setDragEnabled(true);
        //antennaTableA.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        antennaTableA.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        antennaTableA.setPreferredScrollableViewportSize(new Dimension(128,100));
        JScrollPane pane = new JScrollPane(antennaTableA);
        pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        p.add(pane);
        return p;
    }
    private void manageColumnSizesInA(){
        TableColumn column = antennaTableA.getColumnModel().getColumn(columnIndex);
        int w = column.getWidth();
        int n = antennaTableA.getRowCount();
        for (int i = 0; i < n; i ++) {
            TableCellRenderer r = antennaTableA.getCellRenderer(i, this.columnIndex);
            Component c = r.getTableCellRendererComponent(
                    antennaTableA,
                    antennaTableA.getValueAt(i, columnIndex),
                    false,
                    false,
                    i,
                    columnIndex);
            w = Math.max(w, c.getPreferredSize().width);
        }
        if(w< 95){
            w = 95;
        }
        column.setPreferredWidth(128);   
        //column.setPreferredWidth(w+5);   
        ((DefaultTableCellRenderer)antennaTableA.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
    }

    private JPanel createAntennaListB(){
        JPanel p = new JPanel();
        final String[] antennaColumnInfoB= {"Antenna Name"};
        antennaRowInfoB = new Object[0][1];
        antennaTableModelB = new AbstractTableModel(){
            public int getColumnCount() { return antennaColumnInfoB.length; }
            public String getColumnName(int column) { return antennaColumnInfoB[column]; }
            public int getRowCount() { return antennaRowInfoB.length;     }
            public Object getValueAt(int row, int col) { return antennaRowInfoB[row][col]; }
            public void setValueAt(Object val, int row, int col) { antennaRowInfoB[row][col]= val; }
        };
        antennaTableB = new JTable(antennaTableModelB);
        antennaTableB.setPreferredScrollableViewportSize(new Dimension(128,100));
        //antennaTableB.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        antennaTableB.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane pane = new JScrollPane(antennaTableB);
        p.add(pane);
        return p;
    }

    private void manageColumnSizesInB(){
        TableColumn column = antennaTableB.getColumnModel().getColumn(columnIndex);
        int w = column.getWidth();
        int n = antennaTableB.getRowCount();
        for (int i = 0; i < n; i ++) {
            TableCellRenderer r = antennaTableB.getCellRenderer(i, this.columnIndex);
            Component c = r.getTableCellRendererComponent(
                    antennaTableB,
                    antennaTableB.getValueAt(i, columnIndex),
                    false,
                    false,
                    i,
                    columnIndex);
            w = Math.max(w, c.getPreferredSize().width);
        }
        if(w< 95){
            w = 95;
        }
        //column.setPreferredWidth(w+5);    
        column.setPreferredWidth(128);   
        ((DefaultTableCellRenderer)antennaTableB.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
    }

    private JPanel transferButtons() {
        JPanel p =new JPanel(new GridLayout(4,1));
        p.add(new JLabel());
        addToArray = new JButton ("->");
        addToArray.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                moveAntennasToArrayColumn();
                manageColumnSizesInA();
                manageColumnSizesInB();
            }
        });
        p.add(addToArray);
        removeFromArray = new JButton ("<-");
        removeFromArray.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                moveAntennasOutOfArrayColumn();
                manageColumnSizesInA();
                manageColumnSizesInB();
            }
        });
        p.add(removeFromArray);
        p.add(new JLabel());
        return p;
    }

    private JPanel createSouthPanel() {
        JPanel p = new JPanel();
        //p.add(selectModeView(), BorderLayout.NORTH);
        p.add(actionButtons(), BorderLayout.SOUTH);
        return p;
    }

    private JPanel actionButtons(){
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        createArrayB = new JButton("Create");
        createArrayB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                if(createArray()) {
                    exit();
                    setEnabled(false);
                }
            }
        });
        cancelB = new JButton("Cancel");
        cancelB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                exit();
                setEnabled(false);
            }
        });
        p.add(createArrayB);
        p.add(cancelB);
        return p;
    }

    private boolean isIn(int is, int[] in){
        for(int i=0; i < in.length; i++){
            if(in[i] == is) {
                return true;
            }
        }
        return false;
    }

    private void moveAntennasToArrayColumn() {
        int[] rows = antennaTableA.getSelectedRows();
        //System.out.println("number of selected rows in A = "+rows.length);
        if(rows.length == 0 || rows == null){
            JOptionPane.showMessageDialog(this, "You need to select an antenna",
                    "Nothing Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int newLengthA = antennaRowInfoA.length - rows.length;
        int newLengthB = antennaRowInfoB.length + rows.length;
        Object[][] tmpObjectsA =new Object[newLengthA][1];
        Object[][] tmpObjectsB =new Object[newLengthB][1];
        int tmpCtrA=0;
        if(rows.length != antennaRowInfoA.length){
            for(int i=0; i < antennaRowInfoA.length; i++){
                if(!isIn(i, rows)){
                    tmpObjectsA[tmpCtrA++][0] = antennaRowInfoA[i][0];
                }
            }
        }
        if(antennaRowInfoB.length < 1){
            for(int i=0; i < rows.length;i++){
                tmpObjectsB[i][0] =  antennaRowInfoA[rows[i]][0];
            }
        } else {
            int tmpCtrB = 0;
            for(int i=0; i < antennaRowInfoB.length; i++){
                tmpObjectsB[i][0]=antennaRowInfoB[i][0];
                tmpCtrB=i;
            }
            for (int i=0; i < rows.length; i++){
                tmpCtrB++;
                //move rows from A into B
                tmpObjectsB[tmpCtrB][0] = antennaRowInfoA[rows[i]][0];
            }
        }
        
        antennaRowInfoB = tmpObjectsB;
        antennaRowInfoA = tmpObjectsA;
        antennaTableA.repaint();
        antennaTableA.clearSelection();
        antennaTableA.revalidate();
        antennaTableB.repaint();
        antennaTableB.revalidate();
        validate();
    }

    private void moveAntennasOutOfArrayColumn() {
        int[] rows = antennaTableB.getSelectedRows();
        //System.out.println("number of selected rows in b = "+rows.length);
        if(rows.length == 0 || rows == null){
            JOptionPane.showMessageDialog(this, "You need to select an antenna",
                    "Nothing Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int newLengthB= antennaRowInfoB.length - rows.length;
        int newLengthA= antennaRowInfoA.length + rows.length;
        Object[][] tmpObjectsB = new Object[newLengthB][1];
        Object[][] tmpObjectsA = new Object[newLengthA][1];
        int tmpCtrB=0;
        if(antennaRowInfoA.length < 1){
            for(int i=0; i < rows.length;i++){
                tmpObjectsA[i][0] =  antennaRowInfoB[rows[i]][0];
            }
        } else {
            int tmpCtrA=0;
            for(int i=0; i< antennaRowInfoA.length; i++){
                //put all of existing A's in tmp object
                tmpObjectsA[i][0]=antennaRowInfoA[i][0];
                tmpCtrA =i;
            }
            for(int i=0; i < rows.length; i++){
                //put rows from B, which are moving to A, into A
                tmpCtrA++;
                tmpObjectsA[tmpCtrA][0] = antennaRowInfoB[rows[i]][0];
            }
        }
        if(rows.length != antennaRowInfoB.length){
            for(int i=0; i < antennaRowInfoB.length; i++){
                if(!isIn(i, rows)){
                    tmpObjectsB[tmpCtrB++][0] = antennaRowInfoB[i][0];
                }
            }
        }
        //////
        antennaRowInfoA = tmpObjectsA;
        antennaRowInfoB = tmpObjectsB;
        antennaTableA.repaint();
        antennaTableA.revalidate();
        antennaTableB.clearSelection();
        antennaTableB.repaint();
        antennaTableB.revalidate();
        validate();
    }
    
    private void getComponentRefs(){
        //will need
        // - master scheduler
        // - control master
        try {
            masterScheduler = alma.scheduling.MasterSchedulerIFHelper.narrow(
                    container.getDefaultComponent(
                    "IDL:alma/scheduling/MasterSchedulerIF:1.0"));
            logger.info("SCHEDULING_PANEL: Got MS in array creator");
            control = alma.Control.ControlMasterHelper.narrow(
                    container.getComponent("CONTROL/MASTER"));
            logger.info("SCHEDULING_PANEL: Got control system in array creator");
                        
        } catch (Exception e){
            e.printStackTrace();
            logger.severe("SCHEDULING_PANEL: Could not get components to create array");
        }
    }
    private void getAntennas() {
        try{
            availableAntennas = control.getAvailableAntennas();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void updateAntennaTableA(){
        updateAntennaRowInfoA();
        antennaTableA.repaint();
        antennaTableA.revalidate();
        validate();
    }

    public void prepareCreateArray(String mode){
        getComponentRefs();
        arrayMode = mode;
        getAntennas();
        updateAntennaTableA();
    }
       

    private boolean createArray() {
        //make sure there are antennas in the B column
        if(antennaRowInfoB.length < 1) {
            JOptionPane.showMessageDialog(this, 
                    "You need to select at least one antenna",
                    "Nothing Selected", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        //create list of antennas picked
        String[] antennas= new String[antennaRowInfoB.length];
        for(int i=0; i < antennaRowInfoB.length; i++){
            antennas[i] = (String)antennaRowInfoB[i][0];
            System.out.println(antennas[i]);
        }
        String arrayName;
        try {
            if(arrayMode.toLowerCase().equals("dynamic")){
                arrayName = masterScheduler.createArray(
                        antennas,ArrayModeEnum.DYNAMIC);
            } else if(arrayMode.toLowerCase().equals("interactive")){
                arrayName = masterScheduler.createArray(
                        antennas,ArrayModeEnum.INTERACTIVE);
            } else if(arrayMode.toLowerCase().equals("queued")) {
                arrayName = masterScheduler.createArray(
                        antennas,ArrayModeEnum.QUEUED);
            } else if(arrayMode.toLowerCase().equals("manual")){
                arrayName = masterScheduler.createArray(
                        antennas,ArrayModeEnum.MANUAL);
            } else {
                //this should never happen!
                logger.severe("SCHEDULING_PANEL: No array created. InvalidMode");
                JOptionPane.showMessageDialog(this,"Invalid array mode. No array created", "Invalid Mode", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            allArrays.add(arrayName);
        } catch(Exception e) {
            JOptionPane.showMessageDialog(this, e.toString()+
                    "\nMake sure these antennas are really available to "+
                    "create this array. Also check state of Control System.", 
                    "Error creating array", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;

    }

    private void releaseComponentRefs() {
        try {
            container.releaseComponent(control.name());
            container.releaseComponent(masterScheduler.name());
        } catch(Exception e) {
            e.printStackTrace();
            logger.severe("SCHEDULING_PANEL: Error releasing components from array creator");
        }
    }

    public void exit() {
        releaseComponentRefs();
        clearAntennaTables();
    }

    private void clearAntennaTables(){
        antennaRowInfoA= new String[0][1];
        antennaTableA.repaint();
        antennaTableA.revalidate();
        antennaRowInfoB= new Object[0][1];
        antennaTableB.repaint();
        antennaTableB.revalidate();
    }
    
}
