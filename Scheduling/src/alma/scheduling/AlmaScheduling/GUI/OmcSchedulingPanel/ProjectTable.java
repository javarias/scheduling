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
    private boolean projectSearchMode;
    
    public ProjectTable(Dimension tableSize) {
        super(); 
        size = tableSize;
        projectInfo = new JTextArea();

        projRowInfo = new Object[0][infoSize];
        infoSize = projColumnInfo.length +1;
        uidLoc = infoSize-1; 
        createTableModel();
        setModel(projTableModel);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        //setMaximumSize(size);
        setPreferredScrollableViewportSize(size);
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ((DefaultTableCellRenderer)getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);


        addMouseListener(new MouseListener(){
            public void mouseClicked(MouseEvent e) {
                displaySelectedRow();
            }
            public void mouseEntered(MouseEvent e){ }
            public void mouseExited(MouseEvent e){ }
            public void mousePressed(MouseEvent e){ }
            public void mouseReleased(MouseEvent e){}
        });
        addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e){
                displaySelectedRow();
            }
            public void keyReleased(KeyEvent e) {
                displaySelectedRow();
            }
            public void keyTyped(KeyEvent e){
            }
        });
        manageColumnSizes();
    }

    private void displaySelectedRow() {
        //make sure only one selected
        int[] rows = getSelectedRows();
        if(rows.length > 1) {
            //not good! we only want to show one
            return;
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
        if(projectSearchMode){
            showProjectSBs(p);
        }
    }

    public void showFirstProject() {
        //if at least one project found
        if(projRowInfo.length > 0) {
            //select it
            ListSelectionModel selectionModel = getSelectionModel();
            selectionModel.setSelectionInterval(0, 0);
            String uid = (String)projRowInfo[0][uidLoc];
            //get the particular info and display it
            ProjectLite p = controller.getProjectLite(uid);
            showProjectInfo(p);
            //get project sbs
            if(projectSearchMode){
                showProjectSBs(p);
            }
            //and show its sbs
        }
        //else do nothing
    }
    public void setCS(PluginContainerServices cs){
        controller = new ProjectTableController(cs);
    }
    public void setSearchMode(boolean b){
        projectSearchMode = b;
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
        manageColumnSizes();
        System.out.println("Project Table size "+getSize().toString());
        repaint();
        revalidate();
        validate();
    }
    
    public JScrollPane getProjectInfoView(){
        JScrollPane p = new JScrollPane(projectInfo,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        p.setBorder(new TitledBorder("Project Info"));
        p.setPreferredSize(size);
        return p;
    }

    private void showProjectInfo(ProjectLite p){
        projectInfo.setText("");
        projectInfo.append("Project Name = "+p.projectName +"\n");
        projectInfo.append("PI Name = "+p.piName+"\n");
        projectInfo.append("Status = "+p.status +"\n");  
        projectInfo.append("Total number of SBs = "+p.totalSBs +"\n");  
        projectInfo.append("Total number of SBs completed = "+p.completeSBs +"\n"); 
        projectInfo.append("Total number of SBs failed = "+p.failedSBs +"\n");  
        System.out.println("Project Info size "+projectInfo.getSize().toString());
        projectInfo.repaint();
        projectInfo.validate();
    }

    private void showProjectSBs(ProjectLite p) {
        String[] ids = p.allSBIds;
        SBLite[] sbs = controller.getSBLites(ids);
        String par = parent.getClass().getName();
        if(par.contains("SearchArchiveOnlyTab")){
            ((SearchArchiveOnlyTab)parent).updateSBView(sbs);
        } else if(par.contains("InteractiveSchedTab")){
            ((InteractiveSchedTab)parent).updateSBView(sbs);
        } else if(par.contains("QueuedSchedTab")){
            ((QueuedSchedTab)parent).updateSBView(sbs);
        }
    }

    public void clear(){
        projRowInfo = new Object[0][infoSize];
        projectInfo.setText("");
        manageColumnSizes();
        repaint();
        revalidate();
        validate();
    }

    private void manageColumnSizes() {
        Dimension actualSize = getSize();
        TableColumnModel columns = getColumnModel();
        //TableColumn column = getColumnModel().getColumn(x);
        if((projRowInfo == null) || (projRowInfo.length ==0)){
            setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            return;
        }
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for(int x=0; x < columns.getColumnCount(); x++){
            TableColumn column = getColumnModel().getColumn(x);
            int w = column.getWidth();
            int n = getRowCount();
            for(int i = 0; i < n; i ++) {
                TableCellRenderer r = getCellRenderer(i, x);
                Component c = r.getTableCellRendererComponent(
                        this, getValueAt(i, x),
                        false,
                        false,
                        i,
                        x);
                w = Math.max(w, c.getPreferredSize().width);
                ((DefaultTableCellRenderer)r).
                     setHorizontalAlignment(SwingConstants.LEFT);
            }
            column.setPreferredWidth(w);  
        }
    }

}
