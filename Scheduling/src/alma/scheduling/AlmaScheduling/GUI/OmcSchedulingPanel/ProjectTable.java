package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.SBLite;
import alma.scheduling.ProjectLite;

public class ProjectTable extends JTable {
    private final String[] projColumnInfo = {"Project Name", "PI Name" };//, "Version"};
    private Object[][] projRowInfo;
    private int infoSize;
    private int uidLoc; //used to find uid local in projRowInfo
    private TableModel projTableModel;
    private JTextArea projectInfo;
    private Dimension size;
    private JPanel parent;
    private ProjectTableController controller;
    
    public ProjectTable(Dimension tableSize) {
        super(); 
        size = tableSize;
        projectInfo = new JTextArea();
        projRowInfo = new Object[0][infoSize];
        infoSize = projColumnInfo.length +1;
        uidLoc = infoSize-1; 
        createTableModel();
        setModel(projTableModel);
        setPreferredScrollableViewportSize(size);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ((DefaultTableCellRenderer)getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        addMouseListener(new MouseListener(){
            public void mouseClicked(MouseEvent e) {
                //make sure only one selected
                int[] rows = getSelectedRows();
                if(rows.length > 1) {
                    //not good!
                }
                //get row number, 
                int row = getSelectedRow();
                //corresponds to rowInfo index, 
                //last one == uid
                String uid = (String)projRowInfo[row][uidLoc];
                //get the particular info and display it
                ProjectLite p = controller.getProjectLite(uid);
                showProjectInfo(p);
                //get project sbs
                showProjectSBs(p);
            }
            public void mouseEntered(MouseEvent e){ }
            public void mouseExited(MouseEvent e){ }
            public void mousePressed(MouseEvent e){ }
            public void mouseReleased(MouseEvent e){}
        });
    }

    public void setCS(PluginContainerServices cs){
        controller = new ProjectTableController(cs);
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
      // projectLites = projects;
       projRowInfo = new Object[size][infoSize];
       for(int i=0; i < size; i++) {
           projRowInfo[i][0]= projects[i].projectName;
           projRowInfo[i][1]= projects[i].piName;
           projRowInfo[i][2]= projects[i].uid;
       }
       repaint();
       revalidate();
       validate();
    }
    
    public JScrollPane getProjectInfoView(){
        JScrollPane p = new JScrollPane(projectInfo);
        p.setPreferredSize(size);
        return p;
    }

    private void showProjectInfo(ProjectLite p){
        projectInfo.append("Project Name = "+p.projectName +"\n");
        projectInfo.append("PI Name = "+p.piName+"\n");
        projectInfo.append("Status = "+p.status +"\n");  
        projectInfo.append("Total number of SBs = "+p.totalSBs +"\n");  
        projectInfo.append("Total number of SBs completed = "+p.completeSBs +"\n");  
        projectInfo.append("Total number of SBs failed = "+p.failedSBs +"\n");  
        projectInfo.repaint();
        projectInfo.validate();
    }

    private void showProjectSBs(ProjectLite p) {
        String[] ids = p.allSBIds;
        SBLite[] sbs = controller.getSBLites(ids);
        ((SearchArchiveOnlyTab)parent).updateSBView(sbs);
    }

    public void clear(){
       projRowInfo = new Object[0][infoSize];
       repaint();
       revalidate();
       validate();
    }
}
