/*
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
 * File ProjectTable.java
 */
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

// Imports for copy/paste
import java.io.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;

public class ProjectTable extends JTable {
    private final String[] projColumnInfo = {"Project Name", "PI Name" , "Version"};
    private Object[][] projRowInfo;
    private int infoSize;
    private int uidLoc; //used to find uid local in projRowInfo
    private TableModel projTableModel;
    private JTextArea projectInfo;
    private Dimension size;
    private JPanel parent;
    private ProjectTableController controller;
    private boolean projectSearchMode;
    private JPopupMenu rtClickMenu = null;
    private CopyProjUID curUIDinCB = null;
    
    public ProjectTable(Dimension tableSize) {
        super(); 
        size = tableSize;
        projectInfo = new JTextArea();
        projectInfo.setEditable(false);
        infoSize = projColumnInfo.length +1;
        uidLoc = infoSize-1; 
        projRowInfo = new Object[0][infoSize];
        createTableModel();
        setModel(projTableModel);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        //setMaximumSize(size);
        setPreferredScrollableViewportSize(size);
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ((DefaultTableCellRenderer)getTableHeader().getDefaultRenderer()).
            setHorizontalAlignment(SwingConstants.LEFT);
        rtClickMenu = new JPopupMenu();
        addMouseListener(new MouseListener(){
            public void mouseClicked(MouseEvent e) {
                displaySelectedRow();
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
        //set uid in right lcick menu
        updateRightClickMenu(uid);
    }

    private void updateRightClickMenu(String uid){
        rtClickMenu.removeAll();
        JMenuItem menuItem = new JMenuItem("Copy "+uid+" to System clipboard");
        final String curUID = uid;
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event){
                curUIDinCB = new CopyProjUID();
                curUIDinCB.setClipboardContents("ObsProject: "+curUID);
            }
        });
        rtClickMenu.add(menuItem);
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
        javax.swing.SwingUtilities.invokeLater(new UpdateProjectRows(projects));
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
        if(p.isComplete == true) {
            projectInfo.append("Project's status = complete\n");  
        } else {
            projectInfo.append("Project's status = not complete\n");  
        }
        projectInfo.append("PI Name = "+p.piName+"\n");
        projectInfo.append("Status = "+p.status +"\n");  
        projectInfo.append("Total SBs = "+p.totalSBs +"\n");  
        projectInfo.append("Total SBs complete = "+p.completeSBs +"\n"); 
        projectInfo.append("Total SBs failed = "+p.failedSBs +"\n");  
        projectInfo.repaint();
        projectInfo.validate();
    }

    private void showProjectSBs(ProjectLite p) {
        String[] ids = p.allSBIds;
        SBLite[] sbs = controller.getSBLites(ids);
        String par = parent.getClass().getName();
        if(par.contains("SearchArchiveOnlyPlugin")){
            ((SearchArchiveOnlyPlugin)parent).updateSBView(sbs);
        } else if(par.contains("InteractiveSchedTab")){
            ((InteractiveSchedTab)parent).updateSBView(sbs);
        } else if(par.contains("QueuedSchedTab")){
            ((QueuedSchedTab)parent).updateSBView(sbs);
        }  else if(par.contains("ManualArrayTab")){
            ((ManualArrayTab)parent).updateSBView(sbs);
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
        if(projRowInfo.length ==0 ){
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
        if(allColumnWidth < getPreferredScrollableViewportSize().width) {
            int difference = getPreferredScrollableViewportSize().width - allColumnWidth;
            int currentSize;
            int totalColumns = columns.getColumnCount();
            for(int i=0;i< totalColumns; i++) {
                column = getColumnModel().getColumn(i);
                currentSize = column.getPreferredWidth();
                column.setPreferredWidth(currentSize +
                        (difference/totalColumns));
            }
        }
        validate();
    }

    public void clearSelectedItems(){
        projectInfo.setText("");
        clearSelection();
        getSelectionModel().clearSelection();
        validate();
    }

    //copy/paste class
    class CopyProjUID implements ClipboardOwner {
        public CopyProjUID(){
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

    class UpdateProjectRows implements Runnable {
        private ProjectLite[] projects;
        public UpdateProjectRows(ProjectLite[] p){
            projects = p;
        }
        public void run() {
            clearSelectedItems();
            int size = projects.length;
            // projectLites = projects;
            projRowInfo = new Object[size][infoSize];
            for(int i=0; i < size; i++) {
                projRowInfo[i][0]= projects[i].projectName;
                projRowInfo[i][1]= projects[i].piName;
                projRowInfo[i][2]= projects[i].version;
                projRowInfo[i][uidLoc]= projects[i].uid;
            }
            manageColumnSizes();
            repaint();
            revalidate();
            validate();
        }
    }
    
}
