package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class ProjectTable extends JTable {
    private final String[] projColumnInfo = {"Project Name", "PI Name" };//, "Version"};
    private Object[][] projRowInfo;
    private TableModel projTableModel;
    private JTextArea projectInfo;
    private Dimension size;
    
    public ProjectTable(Dimension tableSize) {
        super(); 
        size = tableSize;
        projectInfo = new JTextArea();
        projRowInfo = new Object[0][projColumnInfo.length];
        createTableModel();
        setModel(projTableModel);
        setPreferredScrollableViewportSize(size);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ((DefaultTableCellRenderer)getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
    }
    
    private void createTableModel() {
        projTableModel = new AbstractTableModel() {
            public int getColumnCount() { return projColumnInfo.length; }
            public String getColumnName(int column) { return projColumnInfo[column]; }
            public int getRowCount() { return projRowInfo.length; }
            public Object getValueAt(int row, int col) { return projRowInfo[row][col]; }
            public void setValueAt(Object val, int row, int col) { projRowInfo[row][col] = val; }
        };
    }

    public void setRowInfo(Object[][] info){
        projRowInfo = info;
    }
    
    /*
    public void addRow(String[] info){
    }
    */

    public Object[][] getRowInfo() {
        return projRowInfo;
    }

    public JScrollPane getProjectInfoView(){
        JScrollPane p = new JScrollPane(projectInfo);
        p.setPreferredSize(size);
        return p;
    }
}
