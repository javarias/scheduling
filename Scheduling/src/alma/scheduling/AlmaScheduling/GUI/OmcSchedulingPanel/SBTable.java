/*
   String [] asdmIds = controller.getASDMsForSB(sb);
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 * File SBTable.java
 */
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
// Imports for copy/paste
import java.io.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;

public class SBTable extends JTable {
    private final String[] sbColumnInfo = {"SB Name","Project"};
    //private final String[] sbColumnInfoWithStatus = {"SB Name","Project","Exec Status"};
    //S stands for status and possible status' are N, R, {C, Ab, F}, AR
    private final String[] sbColumnInfoWithStatus = {"S", "SB Name","Project"};
    private Object[][] sbRowInfo;
    private int infoSize;
    private int uidLoc;
    private int sbLoc;
    private int pnLoc;
    private int execLoc;
    private TableModel sbTableModel;
    private JTextArea sbInfo;
    private boolean withExec; 
    private Dimension size;
    private JPanel parent;
    private SBTableController controller;
    private boolean projectSearchMode;
    private JPopupMenu rtClickMenu = null;
    private CopySBUID curUIDinCB = null;

    /**
      * @param b True if you want a column for execution status
      * @param tableSize The dimensions for the table
      */
    public SBTable(boolean b, Dimension tableSize) {
        super();
        size = tableSize;
        sbInfo = new JTextArea();
        sbInfo.setEditable(false);
        withExec = b;
        if(withExec) {
            execLoc = 0;
            sbLoc = 1;
            pnLoc =2;
            infoSize = sbColumnInfoWithStatus.length +1;
            sbRowInfo = new Object[0][infoSize];
                
        } else {
            sbLoc = 0;
            pnLoc =1;
            infoSize = sbColumnInfo.length+1;
            sbRowInfo = new Object[0][infoSize];
        }
        uidLoc = infoSize-1;
        createTableModel();
        setModel(sbTableModel);
        //setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        //setPreferredSize(size);
        setPreferredScrollableViewportSize(size);
        //System.out.println("Size: "+size.width);
        //setMaximumSize(size);
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ((DefaultTableCellRenderer)getTableHeader().getDefaultRenderer()).
            setHorizontalAlignment(SwingConstants.LEFT);
        projectSearchMode = true;
        rtClickMenu = new JPopupMenu();
        addMouseListener(new MouseListener(){
            public void mouseClicked(MouseEvent e) {
                showSelectedSBDetails(getSelectedSBId() );
            }
            public void mouseEntered(MouseEvent e){ }
            public void mouseExited(MouseEvent e){ }
            public void mousePressed(MouseEvent e){ 
                showPopup(e);
            }
            public void mouseReleased(MouseEvent e){
                showPopup(e);
            }
            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                   rtClickMenu.show(e.getComponent(),
                       e.getX(), e.getY());
                } 
            }
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
            return "You can only execute one at a time";
        } else if (rows.length < 1){
            return "You must selected one SB!";
        }
        //get row number,
        int row = getSelectedRow();
        //corresponds to rowInfo index,
        //last one == uid
        String uid = (String)sbRowInfo[row][uidLoc];
        return uid;
    }

    private void showSelectedSBDetails(String id){
        //System.out.println("SB ID = "+id);
        SBLite sb = controller.getSBLite(id);
        showSBInfo(sb);
        if(!projectSearchMode){
            showSBProject(sb);
        }
        updateRightClickMenu(id);
    }

    private void updateRightClickMenu(String uid){
        rtClickMenu.removeAll();
        JMenuItem menuItem = new JMenuItem("Copy "+uid+" to System clipboard");
        final String curUID = uid;
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event){
                curUIDinCB = new CopySBUID();
                curUIDinCB.setClipboardContents("SchedBlock: "+curUID);
            }
        });
        rtClickMenu.add(menuItem);
    }

    //Check to see if the highlighted sb has any ExecBlock's archived.
    //if yes Add them to the RT click menu
    private void getASDMsForSB(String sb) {
        //String [] asdmIds = controller.getASDMsForSB(sb);
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
            public void setValueAt(Object val, int row, int col) {
                sbRowInfo[row][col] = val;
                fireTableCellUpdated(row, col);
            }
        };
    }

    public void setRowInfo(SBLite[] sblites, boolean queuedList){
        clearSelection();
        if(queuedList){
            //do a special thing
            updateRowsForQueue(sblites);
            return;
        }
        int size = sblites.length;
        sbRowInfo = new Object[size][infoSize];
        for(int i=0; i<size; i++){
            sbRowInfo[i][sbLoc] = sblites[i].sbName;
            sbRowInfo[i][pnLoc] = sblites[i].projectName;
            sbRowInfo[i][uidLoc] = sblites[i].schedBlockRef;
            if(withExec){
                setSBExecStatus((String)sbRowInfo[i][uidLoc], "N/A");
            }
        }
        manageColumnSizes();
        //System.out.println("SB Table size "+getSize().toString());
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
        if(sbRowInfo.length > 0){
            for(int i=0; i < sbRowInfo.length; i++){
                newRowInfo[i][sbLoc] = (String)sbRowInfo[i][sbLoc];
                newRowInfo[i][pnLoc] = (String)sbRowInfo[i][pnLoc];
                newRowInfo[i][uidLoc] = (String)sbRowInfo[i][uidLoc];
                if(withExec){
                    newRowInfo[i][execLoc]= sbRowInfo[i][execLoc]; //retain exec status
                }
                tmpCtr++;
            }
        }
        for(int i=0; i < sbs.length; i++){
            newRowInfo[tmpCtr][sbLoc] = sbs[i].sbName;
            newRowInfo[tmpCtr][pnLoc] = sbs[i].projectName;
            newRowInfo[tmpCtr][uidLoc] = sbs[i].schedBlockRef;
            if(withExec){
                newRowInfo[tmpCtr][execLoc] = "N/A";
                //System.out.println("Exec location = "+execLoc);
            }
            tmpCtr++;
        }
        sbRowInfo = newRowInfo;
        manageColumnSizes();
        repaint();
        revalidate();
        validate();
    }

    /**
      * The following two methods; getIndicesOfSBsToRemove and removeRowsFromQueue, 
      * are used to remove sbs from the Queued Scheduling queue.
      * The ids (using getSelectedSBs) and indexes are needed to update the  queued component's list.
      */
    
    public int[] getIndicesOfSBsToRemove() {
        return getSelectedRows();
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
                newRowInfo[ctr][sbLoc] = sbRowInfo[i][sbLoc];
                newRowInfo[ctr][pnLoc] = sbRowInfo[i][pnLoc];
                //newRowInfo[ctr][0] = sbRowInfo[i][0];
                //newRowInfo[ctr][1] = sbRowInfo[i][1];
                newRowInfo[ctr][uidLoc] = sbRowInfo[i][uidLoc];
                if(withExec){
                    newRowInfo[ctr][execLoc] = sbRowInfo[i][execLoc];
                }
                ctr++;
            }
        }
        sbRowInfo = newRowInfo;
        manageColumnSizes();
        clearSelection();
        repaint();
        revalidate();
        validate();
    }
    
    /**
      * Used in queued scheduling
      */
    public void setSBExecStatusForRow(int row, String id, String status){
        if(withExec){
            String displayStatus = getDisplayStatus(status);            
            if( ((String)sbRowInfo[row][uidLoc]).equals(id)){ //good
                sbRowInfo[row][execLoc] = displayStatus;
                        
                changeBackgroundColor(row, displayStatus);
            }
            manageColumnSizes();
            repaint();
            revalidate();
            validate();
        }
    }
    
    private void changeBackgroundColor(int row, String s) {
        ListSelectionModel selectionModel = getSelectionModel();
        selectionModel.setSelectionInterval(row, execLoc);
        if(s.equals("F") || s.equals("AB") || s.equals("FAIL") || 
                s.equals("FAILED") || s.equals("ABORTED") ){
            setBackground(Color.RED);
        } else {
            setBackground(Color.WHITE);
        }
        selectionModel.clearSelection();
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
   
    private String getDisplayStatus(String status){
        String displayStatus="";
        //System.out.println("Execution Status = "+status);
        if(status.equals("ARCHIVED")){
            displayStatus = "AR";
        } else if(status.equals("COMPLETE")){
            displayStatus= "C";
        } else if(status.equals("FAILED")){
            displayStatus= "F";
        } else if(status.equals("RUNNING")){
            displayStatus= "R";
        } else if(status.equals("ABORTED")){
            displayStatus= "AB";
        } else if(status.equals("SUCCESS")){
            displayStatus= "S";
        } else {
            displayStatus = status;
        }
        return displayStatus;
    }
    public void setSBExecStatus(String sbid, String status){
        if(withExec){
            int i= getRowPosForSB(sbid);
            if(i != -1) {
                setValueAt(getDisplayStatus(status), i, execLoc);
                changeBackgroundColor(i, getDisplayStatus(status));
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
        if(sbRowInfo.length ==0 ){
            setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
            ((DefaultTableCellRenderer)getTableHeader().getDefaultRenderer()).
                setHorizontalAlignment(SwingConstants.CENTER);
            return;
        }
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnModel columns = getColumnModel();
        TableColumn column = null;
        TableCellRenderer r= null;
        Component c = null;
        Component header = null;
        int rows = getRowCount();
        int width, headerWidth;
        int allColumnWidth=0;
        for(int i=0;i< columns.getColumnCount(); i++){
            column = getColumnModel().getColumn(i);
            width =0;
            header = getTableHeader().getDefaultRenderer().
                getTableCellRendererComponent (null,
                        column.getHeaderValue(), false,
                        false, 0,0 );
            ((DefaultTableCellRenderer)getTableHeader().getDefaultRenderer()).
                setHorizontalAlignment(SwingConstants.CENTER);
            headerWidth = header.getPreferredSize().width;
            for(int j=0; j < rows; j++){
                r = getCellRenderer(j,i);
                c = r.getTableCellRendererComponent(
                        this, getValueAt(j, i),
                        false,
                        false,
                        j,
                        i);

                width = Math.max(width, c.getPreferredSize().width);
                ((DefaultTableCellRenderer)r).
                     setHorizontalAlignment(SwingConstants.LEFT);
            }
            column.setPreferredWidth(Math.max(headerWidth,width)+5);
            allColumnWidth += Math.max(headerWidth,width)+5;
        }
        //now make sure we fill the whole column if there's extra
        //System.out.println("sb all col wid: "+ allColumnWidth);
        //System.out.println("sb total preferred size: "+ 
          //      getPreferredScrollableViewportSize().width);
        if(allColumnWidth < getPreferredScrollableViewportSize().width) {
       // System.out.println("******************************************");
            int difference = getPreferredScrollableViewportSize().width - allColumnWidth;
           // System.out.println("sb difference: "+ difference);
            int currentSize;
            int totalColumns = columns.getColumnCount();
            for(int i=0;i< totalColumns; i++) {
                column = getColumnModel().getColumn(i);
                currentSize = column.getPreferredWidth();
              //  System.out.println("sb Current size: "+currentSize);
                column.setPreferredWidth(currentSize +
                        (difference/totalColumns));
            //    System.out.println("New SB Col width: "+column.getWidth()+" total columns = "+totalColumns+"; current col # = "+i);
          //      System.out.println("New SB Col width: "+column.getPreferredWidth()+" total columns = "+totalColumns+"; current col # = "+i);
            }
        }
        validate();
        //System.out.println("******************************************");
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
        //System.out.println("SB Info size "+sbInfo.getSize().toString());
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

    public void clearSelectedItems(){
        getSelectionModel().clearSelection();
    }
    
    //copy/paste class
    class CopySBUID implements ClipboardOwner {
        public CopySBUID(){
            super();
        }
        public void lostOwnership(Clipboard c, Transferable t){}
        
        public void setClipboardContents(String s) {
            StringSelection sel = new StringSelection(s);
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            cb.setContents(sel, this);
        }
        public String getClipboardContents(){
            String result = "";
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable contents = cb.getContents(null);
            boolean hasTransferableText = (contents != null) && 
                contents.isDataFlavorSupported(DataFlavor.stringFlavor);
            if(hasTransferableText) {
                try {
                    result = (String)contents.getTransferData(DataFlavor.stringFlavor);
                }catch(UnsupportedFlavorException e1){
                    e1.printStackTrace();
                } catch (IOException e2){
                    e2.printStackTrace();
                }
            }
            return result;
        }
    }

    class PopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e){
            maybeShowPopup(e);
        }
        public void mouseReleased(MouseEvent e){
            maybeShowPopup(e);
        }
        private void maybeShowPopup(MouseEvent e){
            if (e.isPopupTrigger()) {
                rtClickMenu.show(e.getComponent(),
                       e.getX(), e.getY());
            }
        }
    }
}
