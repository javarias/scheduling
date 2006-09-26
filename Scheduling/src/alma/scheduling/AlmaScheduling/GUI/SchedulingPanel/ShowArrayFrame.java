package alma.scheduling.AlmaScheduling.GUI.SchedulingPanel;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.Vector;
import javax.swing.table.*;
import java.util.logging.Logger;
import alma.acs.container.ContainerServices;
import alma.Control.ControlMaster;
import alma.scheduling.ArrayModeEnum;
import alma.scheduling.ArrayStateEnum;
import alma.scheduling.ArrayInfo;
import alma.scheduling.SchedulingInfo;
import alma.scheduling.MasterSchedulerIF;
import alma.exec.extension.subsystemplugin.PluginContainerServices;

public class ShowArrayFrame extends JDialog {

    private MasterSchedulerIF masterScheduler;
    private ControlMaster control;
    private Logger logger;
    //private ContainerServices container;
    private PluginContainerServices container;
    private TableModel arrayTableModel;
    private JTable arrayTable;
    private Object[][] arrayRowInfo;
    private String[] autoArrays;
    private String[] manArrays;
    private static String selectedArray;//="";
    private JPanel centerDisplayPanel;
    private JPanel arrayDisplayPanel;
    private JScrollPane selectedArrayPane;
    private JTextArea selectedArrayView;
    private int columnIndex = 0;
    private boolean canSelect;
    
    public ShowArrayFrame(PluginContainerServices cs, boolean b) {
    //public ShowArrayFrame(ContainerServices cs, boolean b) {
        super();
        this.container = cs;
        this.logger = cs.getLogger();
        int inset = 260;
        //allArrays = new Vector<String>();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset, screenSize.width- inset*2,
                screenSize.height - inset *2);
        setSize(350,300);
        setResizable(false);
        setTitle("Array Selector");
        setModal(true);
        getComponentRefs();
        canSelect = b;
        getContentPane().add(createArrayTable(), BorderLayout.CENTER);
        getContentPane().add(actionButtons(), BorderLayout.SOUTH);
        getArrays();
        selectedArray="";
        addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e){
                addArrayDetails();
            }
            public void focusLost(FocusEvent e){}
        });

    }

    private JPanel createArrayTable(){
        centerDisplayPanel = new JPanel();
        final String[] arrayColumnInfo= {"Arrays","Mode"};
        arrayRowInfo = new Object[0][arrayColumnInfo.length];
        arrayTableModel = new AbstractTableModel(){
            public int getColumnCount() { return arrayColumnInfo.length; }
            public String getColumnName(int column) { return arrayColumnInfo[column]; }
            public int getRowCount() { return arrayRowInfo.length;     }
            public Object getValueAt(int row, int col) { return arrayRowInfo[row][col]; }
            public void setValueAt(Object val, int row, int col) { arrayRowInfo[row][col]= val; }
        };
        arrayTable = new JTable(arrayTableModel);
        arrayTable.setPreferredScrollableViewportSize(new Dimension(275,100));
        //arrayTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        arrayTable.addFocusListener(new FocusListener(){
            public void focusGained(FocusEvent e){
                addArrayDetails();
            }
            public void focusLost(FocusEvent e){}
        });
        arrayTable.addMouseListener(new MouseListener(){
            public void mouseClicked(MouseEvent e) {
                addArrayDetails();
            }
            public void mouseEntered(MouseEvent e){ }
            public void mouseExited(MouseEvent e){ }
            public void mousePressed(MouseEvent e){ }
            public void mouseReleased(MouseEvent e){}
        });
        //manageTableColumnSize();
        //((DefaultTableCellRenderer)arrayTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        JScrollPane pane = new JScrollPane(arrayTable);
        centerDisplayPanel.add(pane, BorderLayout.NORTH);
        return centerDisplayPanel;
    }

    private void manageTableColumnSize(){
        TableColumn column = arrayTable.getColumnModel().getColumn(columnIndex);
        int w = column.getWidth();
        int n = arrayTable.getRowCount();
        for (int i = 0; i < n; i ++) {
            TableCellRenderer r = arrayTable.getCellRenderer(i, this.columnIndex);
            Component c = r.getTableCellRendererComponent(
                    arrayTable,
                    arrayTable.getValueAt(i, columnIndex),
                    false,
                    false,
                    i,
                    columnIndex);
            w = Math.max(w, c.getPreferredSize().width);
        }
        System.out.println(w);
        if(w < 270) {
            w = 270;
        }
        column.setPreferredWidth(w+5);
    }

    private void addArrayDetails(){
        getArrays();
        int row = arrayTable.getSelectedRow();
        arrayDisplayPanel = new JPanel();
        if(row<0){
            Component[] c = centerDisplayPanel.getComponents();
            try {
                if(c.length > 1) {
                    centerDisplayPanel.remove(1);
                }
            } catch(Exception e){}
            centerDisplayPanel.validate();
            centerDisplayPanel.repaint();

            centerDisplayPanel.getParent().repaint();
            validate();
            return;
        }
        try {
            centerDisplayPanel.remove(1);
        } catch(Exception e){}
        selectedArrayView = new JTextArea();
        selectedArrayView.setEditable(false);
        selectedArrayPane = new JScrollPane(selectedArrayView);
        
        selectedArrayView.setLineWrap(true);
        selectedArrayView.setText(getArrayInfo((String)arrayRowInfo[row][0]));
        selectedArrayPane.setPreferredSize(new Dimension(275, 100));

        arrayDisplayPanel .add(selectedArrayPane);
        //centerDisplayPanel.add(selectedArrayPane , BorderLayout.CENTER);
        centerDisplayPanel.add(arrayDisplayPanel , BorderLayout.SOUTH);
        centerDisplayPanel.validate();
    }

    private void getArrays() {
        try {
            autoArrays = control.getAutomaticArrays();
            manArrays = control.getManualArrays();
            arrayRowInfo = new Object[autoArrays.length + manArrays.length][2];
            int allArrays = 0;
            for (int i=0; i < autoArrays.length; i++){
                arrayRowInfo[i][0] = autoArrays[i];
                arrayRowInfo[i][1] = "Automatic";
                allArrays = i;
            }
            for(int i=0; i < manArrays.length; i++){
                arrayRowInfo[allArrays][0] = manArrays[i];
                arrayRowInfo[allArrays][1] = "Manual";
            }
            //manageTableColumnSize();
            arrayTable.repaint();
            arrayTable.revalidate();
            validate();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private String getArrayInfo(String name) {
        String s="";
        try {
            SchedulingInfo info = masterScheduler.getArrayInfo();
            ArrayInfo a_info;
            for(int i=0; i <  info.array.length; i++){
                a_info = info.array[i];
                if(a_info.arrayName.equals(name)){
                    s = "Array Name: "+a_info.arrayName+"\n";
                    s = s+ "Mode: "+getArrayModeString(a_info.mode)+"\n";
                    s = s+ "State: "+getArrayStateString(a_info.state)+"\n";
                    s = s+ "Running Project: "+a_info.projectName+"\n";
                    s = s+ "Current SB: "+a_info.SBName+"\n";
                    s = s+ "Completion Time: "+a_info.completionTime+"\n";
                    s = s+ "Comment: "+a_info.comment+"\n";
                }
            }
        } catch (Exception e){
            s = "Problem getting array info. \n";
            s = s+ e.toString() +"\n";
            s = s+ "Best action is to destroy the array and re-create it. \n";
        }
        return s;
    }

    private String getArrayModeString(ArrayModeEnum e){
        if(e.value() == 0){
            return "Dynamic";
        } else if (e.value() ==1){
            return "Interactive";
        }else if(e.value() == 2){
            return "Queued";
        } else if(e.value()==3){
            return "Manual";
        } else {
            return "Invalid Array Mode: "+e.value();
        }
         
    }
    private String getArrayStateString(ArrayStateEnum e){
        if(e.value() == 0){
            return "Busy";
        } else if(e.value() == 1){
            return "Idle";
        } else {
            return "Invalide Array State: "+e.value(); 
        }
    }

    private JPanel actionButtons(){
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton createArrayB = new JButton("Select");
        //TODO this need return the name of the array selected here.
        createArrayB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                selectArray();
            }
        });
        if(!canSelect){
            createArrayB.setEnabled(false);
        }
        JButton cancelB = new JButton("Cancel");
        cancelB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                exit();
            }
        });
        JButton antFrameB = new JButton("Create");
        antFrameB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                CreateArrayFrame f = new CreateArrayFrame(container);
                f.setVisible(true);
            }
        });
        antFrameB.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e){
                addArrayDetails();
            }
            public void focusLost(FocusEvent e){}
        });
        JButton destroyArrayB = new JButton("Destroy");
        destroyArrayB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                destroyArray();
            }
        });
        p.add(createArrayB);
        p.add(cancelB);
        p.add(antFrameB);
        p.add(destroyArrayB);
        return p;
    }
    private void selectArray(){
        int[] rows = arrayTable.getSelectedRows();
        if(rows.length != 1) {
            JOptionPane.showMessageDialog(this,
                "Select one array!","",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        selectedArray = (String)arrayRowInfo[rows[0]][0];
        exit();
    }

    private void destroyArray() {
        int[] rows = arrayTable.getSelectedRows();
        if (rows.length == 0 ){
            JOptionPane.showMessageDialog(this,
                "Selected at least one array to destroy","",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        for(int i=0; i < rows.length; i++){
            try {
                masterScheduler.destroyArray((String)arrayRowInfo[rows[i]][0]);
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        getArrays();
        try {
            centerDisplayPanel.remove(1);
            centerDisplayPanel.repaint();
        } catch(Exception e){}
        //try {
          //  arrayDisplayPanel.removeAll();
        //} catch(Exception e){}
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
        dispose();
    }

    
    //public static String showArraySelectFrame(ContainerServices cs, boolean x) {
    public static String showArraySelectFrame(PluginContainerServices cs, boolean x) {
        try {
            ShowArrayFrame f = new ShowArrayFrame(cs, x);
            f.setVisible(true);
        }catch(Exception e) {
            e.printStackTrace();
        }
        return selectedArray;
    }
}
