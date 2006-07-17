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
import alma.scheduling.MasterSchedulerIF;

public class ShowArrayFrame extends JDialog {

    private MasterSchedulerIF masterScheduler;
    private ControlMaster control;
    private Logger logger;
    private ContainerServices container;
    private TableModel arrayTableModel;
    private JTable arrayTable;
    private Object[][] arrayRowInfo;
    private String[] autoArrays;
    private String[] manArrays;
    private static String selectedArray="";
    private JPanel centerDisplayPanel;
    private JPanel arrayDisplayPanel;
    private JScrollPane selectedArrayPane;
    private JTextArea selectedArrayView;
    
    public ShowArrayFrame(ContainerServices cs) {
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
        getContentPane().add(createArrayTable(), BorderLayout.CENTER);
        getContentPane().add(actionButtons(), BorderLayout.SOUTH);
        getArrays();
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
        arrayTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
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
        JScrollPane pane = new JScrollPane(arrayTable);
        centerDisplayPanel.add(pane, BorderLayout.CENTER);
        return centerDisplayPanel;
    }

    private void addArrayDetails(){
        getArrays();
        int row = arrayTable.getSelectedRow();
        arrayDisplayPanel = new JPanel();
        if(row<0){
            Component[] c = centerDisplayPanel.getComponents();
            if(c.length > 1) {
                centerDisplayPanel.remove(1);
            }
            centerDisplayPanel.validate();
            centerDisplayPanel.repaint();

            centerDisplayPanel.getParent().repaint();
            validate();
            return;
        }
        selectedArrayView = new JTextArea();
        selectedArrayView.setLineWrap(true);
        selectedArrayView.setPreferredSize(new Dimension(275, 100));
        selectedArrayView.setText("Array: "+((String)arrayRowInfo[row][0]));
        selectedArrayPane = new JScrollPane(selectedArrayView);
        arrayDisplayPanel .add(selectedArrayPane);
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
            arrayTable.repaint();
            arrayTable.revalidate();
            validate();
        } catch(Exception e){
            e.printStackTrace();
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

    
    public static String showArraySelectFrame(ContainerServices cs) {
        try {
            ShowArrayFrame f = new ShowArrayFrame(cs);
            f.setVisible(true);
        }catch(Exception e) {
            e.printStackTrace();
        }
        return selectedArray;
    }
}
