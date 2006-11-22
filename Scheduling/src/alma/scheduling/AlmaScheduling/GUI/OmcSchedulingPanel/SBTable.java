package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import alma.scheduling.ProjectLite;
import alma.scheduling.SBLite;
import alma.exec.extension.subsystemplugin.PluginContainerServices;

public class SBTable extends JTable {
    private final String[] sbColumnInfo = {"SB Name"};
    private final String[] sbColumnInfoWithStatus = {"SB Name","Exec Status"};
    private Object[][] sbRowInfo;
    private int infoSize;
    private int uidLoc;
    private TableModel sbTableModel;
    private JTextArea sbInfo;
    private boolean withExec; 
    private Dimension size;
    private JPanel parent;
    private SBTableController controller;
    private boolean projectSearchMode;

    public SBTable(boolean b, Dimension tableSize) {
        super();
        size = tableSize;
        sbInfo = new JTextArea();
        withExec = b;
        if(withExec) {
            infoSize = sbColumnInfoWithStatus.length +1;
            sbRowInfo = new Object[0][infoSize];
        } else {
            infoSize = sbColumnInfo.length+1;
            sbRowInfo = new Object[0][infoSize];
        }
        uidLoc = infoSize-1;
        createTableModel();
        setModel(sbTableModel);
        setPreferredScrollableViewportSize(size);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ((DefaultTableCellRenderer)getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        projectSearchMode = true;
        addMouseListener(new MouseListener(){
            public void mouseClicked(MouseEvent e) {
                SBLite sb = controller.getSBLite( getSelectedSBId() );
                showSBInfo(sb);
                if(!projectSearchMode){
                    showSBProject(sb);
                }
            }
            public void mouseEntered(MouseEvent e){ }
            public void mouseExited(MouseEvent e){ }
            public void mousePressed(MouseEvent e){ }
            public void mouseReleased(MouseEvent e){}
        });
    }
    private String getSelectedSBId() {
        int[] rows = getSelectedRows();
        if(rows.length > 1) {
            //not good!
            return "";
        }
        //get row number,
        int row = getSelectedRow();
        //corresponds to rowInfo index,
        //last one == uid
        String uid = (String)sbRowInfo[row][uidLoc];
        return uid;
    }
    
    public void setCS(PluginContainerServices cs) {
        controller = new SBTableController(cs);
    }
    public void setSearchMode(boolean b){
        projectSearchMode = b;
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
            sbRowInfo = new Object[size][infoSize];
        } else {
            sbRowInfo = new Object[size][infoSize];
        }
        for(int i=0; i<size; i++){
            sbRowInfo[i][0] = sblites[i].sbName;
            sbRowInfo[i][uidLoc] = sblites[i].schedBlockRef;
            if(withExec){
                setSBExecStatus((String)sbRowInfo[i][uidLoc], "N/A");
            }
        }
        repaint();
        revalidate();
        validate();
    }
    
    public void setSBExecStatus(String sbid, String status){
        if(withExec){
            int i= getRowPosForSB(sbid);
            if(i != -1) {
                setValueAt(status, i, 1);
            } //else sb not found in table.
        } //else ignore
    }
   
    public int getRowPosForSB(String sbId){
        for(int i=0; i< sbRowInfo.length; i++){
            if(sbId.equals((String)sbRowInfo[i][uidLoc])){
                return i;
            }
        }
        return -1;
    }
    private void manageColumnSizes() {
    }

    public JScrollPane getSBInfoView(){
        JScrollPane p = new JScrollPane (sbInfo);
        if(withExec){
            Dimension d = new Dimension(size.width, size.height + 25);
            p.setPreferredSize(d);
        }else{
            p.setPreferredSize(size);
        }
        return p;
    }

    private void showSBInfo(SBLite sb){
        sbInfo.setText("");
        sbInfo.append("SB Name: "+sb.sbName+"\n");
        sbInfo.append("Priority: "+sb.priority+"\n");
        sbInfo.append("RA: "+sb.ra+"\n");
        sbInfo.append("DEC: "+sb.dec+"\n");
        sbInfo.append("Frequency: "+sb.freq+"\n");
        sbInfo.append("Score: "+sb.score+"\n");
        sbInfo.append("Success: "+sb.success+"\n");
        sbInfo.append("Rank: "+sb.rank+"\n");
        sbInfo.repaint();
        sbInfo.validate();
    }

    private void showSBProject(SBLite sb){
        ProjectLite[] p = controller.getProjectLite(sb.projectRef);
        String par = parent.getClass().getName();
        if(par.contains("SearchArchiveOnlyTab")){
            ((SearchArchiveOnlyTab)parent).updateProjectView(p);
        } else if(par.contains("InteractiveSchedTab")){
            ((InteractiveSchedTab)parent).updateProjectView(p);
        } else if(par.contains("QueuedSchedTab")){
            ((QueuedSchedTab)parent).updateProjectView(p);
        }
    }

    public void clear() {
        if(withExec) {
            sbRowInfo = new Object[0][infoSize];
        } else {
            sbRowInfo = new Object[0][infoSize];
        }
        sbInfo.setText("");
        repaint();
        revalidate();
        validate();
    }

    public String returnSelectedSBId() {
        return getSelectedSBId();
    }

}
