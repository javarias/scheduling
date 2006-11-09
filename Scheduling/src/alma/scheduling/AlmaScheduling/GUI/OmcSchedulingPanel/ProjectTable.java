package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import alma.scheduling.ProjectLite;

public class ProjectTable extends JTable {
    private final String[] projColumnInfo = {"Project Name", "PI Name" };//, "Version"};
    private Object[][] projRowInfo;
    private TableModel projTableModel;
    private JTextArea projectInfo;
    private Dimension size;
    private JPanel parent;
    
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

        addMouseListener(new MouseListener(){
            public void mouseClicked(MouseEvent e) {
                showProjectInfo();
                showProjectSBs();
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
        projTableModel = new AbstractTableModel() {
            public int getColumnCount() { return projColumnInfo.length; }
            public String getColumnName(int column) { return projColumnInfo[column]; }
            public int getRowCount() { return projRowInfo.length; }
            public Object getValueAt(int row, int col) { return projRowInfo[row][col]; }
            public void setValueAt(Object val, int row, int col) { projRowInfo[row][col] = val; }
        };
    }

    public void setRowInfo(ProjectLite[] projects) {
       int size = projects.length;
       projRowInfo = new Object[size][projColumnInfo.length];
       for(int i=0; i < size; i++) {
           projRowInfo[i][0]= projects[i].projectName;
           projRowInfo[i][1]= projects[i].piName;
       }
       repaint();
       revalidate();
       validate();
    }
    
    public Object[][] getRowInfo() {
        return projRowInfo;
    }

    public JScrollPane getProjectInfoView(){
        JScrollPane p = new JScrollPane(projectInfo);
        p.setPreferredSize(size);
        return p;
    }

    private void showProjectInfo(){

    }

    private void showProjectSBs() {
    }
}
