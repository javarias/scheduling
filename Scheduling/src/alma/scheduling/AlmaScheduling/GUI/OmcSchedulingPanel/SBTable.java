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
    private final String[] sbColumnInfo = {"SB Name","Project"};
    private final String[] sbColumnInfoWithStatus = {"SB Name","Project","Exec Status"};
    private Object[][] sbRowInfo;
    private int infoSize;
    private int uidLoc;
    private int execLoc;
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
        execLoc = 2;
        createTableModel();
        setModel(sbTableModel);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setPreferredScrollableViewportSize(size);
        //setMaximumSize(size);
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ((DefaultTableCellRenderer)getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        projectSearchMode = true;
        addMouseListener(new MouseListener(){
            public void mouseClicked(MouseEvent e) {
                showSelectedSBDetails(getSelectedSBId() );
            }
            public void mouseEntered(MouseEvent e){ }
            public void mouseExited(MouseEvent e){ }
            public void mousePressed(MouseEvent e){ }
            public void mouseReleased(MouseEvent e){}
        });
        addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e){
                showSelectedSBDetails(getSelectedSBId() );
            }
            public void keyReleased(KeyEvent e) {
                showSelectedSBDetails(getSelectedSBId() );
            }
            public void keyTyped(KeyEvent e){
            }                
        });
        manageColumnSizes();
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

    private void showSelectedSBDetails(String id){
        SBLite sb = controller.getSBLite( id);
        showSBInfo(sb);
        if(!projectSearchMode){
            showSBProject(sb);
        }
    }

    public void selectFirstSB() {
        if(sbRowInfo.length > 0){
            ListSelectionModel selectionModel = getSelectionModel();
            selectionModel.setSelectionInterval(0,0);
            showSelectedSBDetails(getSelectedSBId());
        }
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

    public void setRowInfo(SBLite[] sblites, boolean queuedList){
        if(queuedList){
            //do a special thing
            updateRowsForQueue(sblites);
            return;
        }
        int size = sblites.length;
        sbRowInfo = new Object[size][infoSize];
        for(int i=0; i<size; i++){
            sbRowInfo[i][0] = sblites[i].sbName;
            sbRowInfo[i][1] = sblites[i].projectName;
            sbRowInfo[i][uidLoc] = sblites[i].schedBlockRef;
            if(withExec){
                setSBExecStatus((String)sbRowInfo[i][uidLoc], "N/A");
            }
        }
        manageColumnSizes();
        System.out.println("SB Table size "+getSize().toString());
        repaint();
        revalidate();
        validate();
    }
    
    private void updateRowsForQueue(SBLite[] sbs){
        //get existing row info
        int newSize = sbRowInfo.length+sbs.length;
        Object[][] newRowInfo = new Object[newSize][infoSize];
        //add new ones to newRowInfo
        int tmpCtr=0;
        for(int i=0; i < sbs.length; i++){
            newRowInfo[i][0] = sbs[i].sbName;
            newRowInfo[i][1] = sbs[i].projectName;
            newRowInfo[i][uidLoc] = sbs[i].schedBlockRef;
            if(withExec){
                newRowInfo[i][execLoc] = "N/A";
            }
            tmpCtr = i+1;
        }
        if(sbRowInfo.length > 0){
            for(int i=0; i < sbRowInfo.length; i++){
                newRowInfo[tmpCtr][0] = (String)sbRowInfo[i][0];
                newRowInfo[tmpCtr][1] = (String)sbRowInfo[i][1];
                newRowInfo[tmpCtr][uidLoc] = (String)sbRowInfo[i][uidLoc];
                if(withExec){
                    newRowInfo[tmpCtr][execLoc]= sbRowInfo[i][execLoc]; //retain exec status
                }
                tmpCtr++;
            }
        }
        sbRowInfo = newRowInfo;
        manageColumnSizes();
        repaint();
        revalidate();
        validate();
    }

    public void removeRowsFromQueue(){
        if(sbRowInfo.length == 0){
            return;
        }
        int[] rows = getSelectedRows();
        if(rows.length ==0){
            //do nothing
            return;
        }
        //get existing row info
        //remove the rows that have correspond to the given row numbers
        int newSize = sbRowInfo.length - rows.length;
        Object[][] newRowInfo = new Object[newSize][infoSize];
        int ctr=0;
        for(int i=0; i < sbRowInfo.length; i++){
            if(!isRowToBeRemoved(rows, i)){
                newRowInfo[ctr][0] = sbRowInfo[i][0];
                newRowInfo[ctr][1] = sbRowInfo[i][1];
                newRowInfo[ctr][uidLoc] = sbRowInfo[i][uidLoc];
                if(withExec){
                    newRowInfo[ctr][execLoc] = sbRowInfo[i][execLoc];
                }
                ctr++;
            }
        }
        sbRowInfo = newRowInfo;
        manageColumnSizes();
        repaint();
        revalidate();
        validate();
    }
    
    /**
      * Used in queued scheduling
      */
    public void setSBExecStatusForRow(int row, String id, String status){
        if(withExec){
            if( ((String)sbRowInfo[row][uidLoc]).equals(id)){ //good
                sbRowInfo[row][execLoc] = status;
            }
            manageColumnSizes();
            repaint();
            revalidate();
            validate();
        }
    }
    
    private boolean isRowToBeRemoved(int[] rows, int r){
        for (int i=0; i< rows.length; i++){
            if(rows[i] == r) {
                return true;
            }
        }
        //not found in rows
        return false;
    }
    
    public void setSBExecStatus(String sbid, String status){
        if(withExec){
            int i= getRowPosForSB(sbid);
            if(i != -1) {
                setValueAt(status, i, 1);
            } //else sb not found in table.
        } //else ignore
        manageColumnSizes();
        repaint();
        revalidate();
        validate();
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
        Dimension actualSize = getSize();
        TableColumnModel columns = getColumnModel();
        if((sbRowInfo == null) || (sbRowInfo.length ==0)){
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
            }
            column.setPreferredWidth(w);  
        }
    }

    public JScrollPane getSBInfoView(){
        JScrollPane p = new JScrollPane (sbInfo,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        p.setBorder(new TitledBorder("SB Details"));
        if(withExec){
            Dimension d = new Dimension(size.width, size.height + 25);
            p.setPreferredSize(d);
            //p.setMaximumSize(d);
        }else{
            //p.setMaximumSize(size);
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
        System.out.println("SB Info size "+sbInfo.getSize().toString());
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
        manageColumnSizes();
        repaint();
        revalidate();
        validate();
    }

    public String returnSelectedSBId() {
        return getSelectedSBId();
    }

    public String[] getSelectedSBs(){
        
        int[] rows = getSelectedRows();
        String[] selected = new String[rows.length];
        for(int i=0;i < rows.length; i++){
            //last one == uid
            selected[i] = (String)sbRowInfo[rows[i]][uidLoc];
        }
        return selected;
    }

    public String[] getAllSBIds() {
        String[] allIds = new String[sbRowInfo.length];
        for(int i=0; i < sbRowInfo.length; i++){
            allIds[i] =(String) sbRowInfo[i][uidLoc];
        }
        return allIds;
    }

    
}
