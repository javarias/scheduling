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
 * File ArrayTable.java
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

// Imports for copy/paste
import java.io.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;

public class ArrayTable extends JTable {
    private final String[] arrayColumnInfo = {"Array Name","Array Type"};
    private Object[][] arrayRowInfo;
    private TableModel arrayTableModel;
    private Dimension size;
    private JPanel parent;
    private ArrayTableController controller;
    private JPopupMenu rtClickMenu = null;
    private String currentArray="";
    private Logger logger;
    
    public ArrayTable(Dimension tableSize) {
        super(); 
        size = tableSize;
        arrayRowInfo = new Object[0][2];
        createTableModel();
        setModel(arrayTableModel);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setPreferredScrollableViewportSize(size);
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ((DefaultTableCellRenderer)getTableHeader().getDefaultRenderer()).
            setHorizontalAlignment(SwingConstants.LEFT);
        manageColumnSizes();
        rtClickMenu = new JPopupMenu();
        addMouseListener(new MouseListener(){
            public void mouseClicked(MouseEvent e) { }
            public void mouseEntered(MouseEvent e){ }
            public void mouseExited(MouseEvent e){ }
            public void mousePressed(MouseEvent e){
                showPopup(e);
            }
            public void mouseReleased(MouseEvent e){
                showPopup(e);
            }
            private void showPopup(MouseEvent e) {
                updateRightClickMenu();
                if (e.isPopupTrigger()) {
                   rtClickMenu.show(e.getComponent(),
                       e.getX(), e.getY());
                } 
            }
        });
    }

    private void getSelectedArray(){
        try {
            int row = getSelectedRow();
            currentArray = (String)arrayRowInfo[row][0];
        } catch(Exception e){
            logger.severe("Crap problem getting currently selected array...");
        }
    }

    private void updateRightClickMenu() {
        rtClickMenu.removeAll();
       // getSelectedArray();
        //JMenuItem item1 = new JMenuItem("Open Scheduler");
        //item1.setToolTipText("Not Implemented yet");
       // item1.addActionListener(new ActionListener() {
         //   public void actionPerformed(ActionEvent event){
                //
        //    }
        //});
        //rtClickMenu.add(item1);
        JMenuItem item2 = new JMenuItem("Destroy Array");
        item2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event){
                DoDestroyArray foo = new DoDestroyArray();
                Thread t = new Thread(foo);
                t.start();
            }
        });
        rtClickMenu.add(item2);

    }
    
    public void setCS(PluginContainerServices cs){
        controller = new ArrayTableController(cs);
        logger = cs.getLogger();
    }
    
    public void setOwner(JPanel p){
        parent = p;
    }
    
    private void createTableModel() {
        arrayTableModel = new AbstractTableModel() {
            public int getColumnCount() { return arrayColumnInfo.length; }
            public String getColumnName(int column) { return arrayColumnInfo[column]; }
            public int getRowCount() { return arrayRowInfo.length; }
            public Object getValueAt(int row, int col) { return arrayRowInfo[row][col]; }
            public void setValueAt(Object val, int row, int col) { arrayRowInfo[row][col] = val; }
        };
    }

    public void setRowInfo(String[][] arrays) {
        clearSelection();
        arrayRowInfo = new Object[arrays.length][2];
        for(int i=0; i < arrays.length; i++) {
            arrayRowInfo[i][0]= arrays[i][0];
            arrayRowInfo[i][1]= arrays[i][1];
        }
        manageColumnSizes();
        repaint();
        revalidate();
        validate();
    }
    /**
      * Returns true if the name is in the existing arrays
      */
    private boolean isArrayInList(String a){
        if(arrayRowInfo.length == 0){
            return false; //nothing is in there!
        }
        for(int i=0; i < arrayRowInfo.length; i++){
            if(((String)arrayRowInfo[i][0]).equals(a)){
                return true;
            }
        }
        //should never happen coz all arrays shold be here!
        return false;
    }

    protected synchronized void removeArray(String name){
        if(name.equals("")){
            return;
        }
        if(!isArrayInList(name)){
            return;
        }
        //ok name is in the list so lets take it out
        Object[][] oldInfo = arrayRowInfo;
        arrayRowInfo = new Object[oldInfo.length - 1][2];
        int ctr =0;
        for(int i=0; i< oldInfo.length; i++){
            if(!((String)oldInfo[i][0]).equals(name)) {
                arrayRowInfo[ctr++] = oldInfo[i];
            }
        }
        repaint();
        revalidate();
        validate();
    }

    protected synchronized void addArray(String name, String type){
        if(arrayRowInfo.length == 0) {
            arrayRowInfo = new Object[1][2];
            arrayRowInfo[0][0] = name;
            arrayRowInfo[0][1] = type;
            repaint();
            revalidate();
            validate();
            return;
        }
        Object[][] oldInfo = arrayRowInfo;
        int size = oldInfo.length + 1;
        int ctr =0;
        arrayRowInfo = new Object[size][2];
        for(int i=0; i < size; i++){
            arrayRowInfo[i][0] = oldInfo[i][0];
            arrayRowInfo[i][1] = oldInfo[i][1];
            ctr++;
        }
        arrayRowInfo[ctr][0]=name;
        arrayRowInfo[ctr][1]=type;
        repaint();
        revalidate();
        validate();
    }
    
    public void clear(){
        arrayRowInfo = new Object[0][2];
        manageColumnSizes();
        repaint();
        revalidate();
        validate();
    }

    private void manageColumnSizes() {
        if(arrayRowInfo.length ==0 ){
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
        getSelectionModel().clearSelection();
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
    

    class DoDestroyArray implements Runnable {
        //private String name;
        public DoDestroyArray(){//String n){
          //  name = n;
        }
        public void run(){
            logger.fine("SP: about to destroy "+currentArray);
            getSelectedArray();
            if(currentArray.equals("")){
                return;
            }
            controller.destroyArray(currentArray);
        }
        
    }

}
