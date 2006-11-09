package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import alma.scheduling.SBLite;

public class SBTable extends JTable {
    private final String[] sbColumnInfo = {"SB Name"};
    private final String[] sbColumnInfoWithStatus = {"SB Name","Exec Status"};
    private Object[][] sbRowInfo;
    private TableModel sbTableModel;
    private JTextArea sbInfo;
    private boolean withExec; 
    private Dimension size;
    private JPanel parent;

    public SBTable(boolean b, Dimension tableSize) {
        super();
        size = tableSize;
        sbInfo = new JTextArea();
        withExec = b;
        if(withExec) {
            sbRowInfo = new Object[0][sbColumnInfoWithStatus.length];
        } else {
            sbRowInfo = new Object[0][sbColumnInfo.length];
        }
        createTableModel();
        setModel(sbTableModel);
        setPreferredScrollableViewportSize(size);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ((DefaultTableCellRenderer)getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        addMouseListener(new MouseListener(){
            public void mouseClicked(MouseEvent e) {
                showSBInfo();
                showSBProject();
            }
            public void mouseEntered(MouseEvent e){ }
            public void mouseExited(MouseEvent e){ }
            public void mousePressed(MouseEvent e){ }
            public void mouseReleased(MouseEvent e){}
        });
    }
    
    public void setOwner(JPanel p){
        parent = p;
    }
    private void createTableModel() {
        sbTableModel = new AbstractTableModel() {
            public int getColumnCount() { 
                if(withExec) {
                    return sbColumnInfoWithStatus.length;
                } else {
                    return sbColumnInfo.length;
                }
            }
            public String getColumnName(int column) { 
                if(withExec) {
                    return sbColumnInfoWithStatus[column]; 
                }else {
                    return sbColumnInfo[column]; 
                }
            }
            public int getRowCount() { return sbRowInfo.length; }
            public Object getValueAt(int row, int col) { return sbRowInfo[row][col]; }
            public void setValueAt(Object val, int row, int col) { sbRowInfo[row][col] = val; }
        };
    }

    public void setRowInfo(SBLite[] sblites){
        int size = sblites.length;
        if(withExec) {
            sbRowInfo = new Object[size][sbColumnInfoWithStatus.length];
        } else {
            sbRowInfo = new Object[size][sbColumnInfo.length];
        }
        for(int i=0; i<size; i++){
            sbRowInfo[i][0] = sblites[i].sbName;
        }
        repaint();
        revalidate();
        validate();
    }
    
    public Object[][] getRowInfo() {
        return sbRowInfo;
    }

    public JScrollPane getSBInfoView(){
        JScrollPane p = new JScrollPane (sbInfo);
        p.setPreferredSize(size);
        return p;
    }

    private void showSBInfo(){
    }

    private void showSBProject(){
    }
}
