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
package alma.scheduling.master.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Logger;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import alma.exec.extension.subsystemplugin.PluginContainerServices;

public class ArrayTable extends JTable {
    private final String[] arrayColumnInfo = {"Array Name","Array Type"};
    private Object[][] arrayRowInfo;
    private TableModel arrayTableModel;
    private Dimension size;
    private JPanel parent;
    private ArrayTableController controller;
    private JPopupMenu rtClickMenu = null;
    private String currentArray = "";
    private String currentType  = "";
    //private ALMASchedLogger logger;
    private Logger logger;
    
	public ArrayTable(Dimension tableSize) {
		super();
		size = tableSize;
		arrayRowInfo = new Object[0][2];
		createTableModel();
		setModel(arrayTableModel);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setPreferredScrollableViewportSize(size);
		getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		((DefaultTableCellRenderer) getTableHeader().getDefaultRenderer())
				.setHorizontalAlignment(SwingConstants.LEFT);
		manageColumnSizes();
		createRightClickMenu();
		addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
				showPopup(e);
			}

			public void mouseReleased(MouseEvent e) {
				showPopup(e);
			}

			private void showPopup(MouseEvent e) {
				// updateRightClickMenu();
				int row = rowAtPoint(new Point(e.getX(), e.getY()));
				clearSelection();
				setRowSelectionInterval(row, row);
				if (e.isPopupTrigger()) {
					rtClickMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
	}

    private void getSelectedArray(){
    	try {

    		int row = getSelectedRow();
    		if(row>=0) {
    			currentArray = (String)arrayRowInfo[row][0];
    			currentType  = (String)arrayRowInfo[row][1];
    		}

    	} catch(Exception e){
    		logger.severe("Crap problem getting currently selected array...");
    	}
    }

    private void createRightClickMenu() {
        rtClickMenu = new JPopupMenu();
       // rtClickMenu.removeAll();
       // getSelectedArray();
        JMenuItem item1;
        item1 = new JMenuItem("Destroy Array");
        item1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event){
                DoDestroyArray foo = new DoDestroyArray();
                Thread t = controller.getCS().getThreadFactory().newThread(foo);
                t.start();
            }
        });
        JMenuItem item2 = new JMenuItem("Show Array Panel");
        item2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DoShowArrayPanel foo = new DoShowArrayPanel();
				Thread t = controller.getCS().getThreadFactory().newThread(foo);
				t.start();
			}
		});
        rtClickMenu.add(item2);
        rtClickMenu.add(item1);
	clearSelection();
    }
    
    public void setCS(PluginContainerServices cs){
        controller = new ArrayTableController(cs);
        //logger = new ALMASchedLogger(cs.getLogger());
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
        	logger.fine(String.format(
        			"ArrayTable.removeArray(%s) - existing %d, failed as name is empty",
        			name, arrayRowInfo.length));
            return;
        }
        if(!isArrayInList(name)){
        	logger.fine(String.format(
        			"ArrayTable.removeArray(%s) - existing %d, failed as name not in list",
        			name, arrayRowInfo.length));
            return;
        }
    	logger.fine(String.format(
    			"ArrayTable.removeArray(%s) - existing %d",
    			name, arrayRowInfo.length));
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
    	logger.fine(String.format(
    			"ArrayTable.addArray(%s, %s) - existing %d",
    			name, type, arrayRowInfo.length));
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
        for(int i=0; i < (size-1); i++){
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
    	logger.fine(String.format(
    			"ArrayTable.clear() - existing %d",
    			arrayRowInfo.length));
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
		// private String name;
		public DoDestroyArray() {
			// name = n;
		}
		
		@Override
		public void run() {
			getSelectedArray();
			clearSelection();
			if (currentArray.equals("")) {
				return;
			}
			logger.fine("SP: about to destroy " + currentArray);
			controller.destroyArray(currentArray);
		}

	}
	
	class DoShowArrayPanel implements Runnable {

		@Override
		public void run() {
			getSelectedArray();
			clearSelection();
			if(currentArray.equals("")) {
				return;
			}
			logger.fine("SP: about to create ArrayPanel for Array " + currentArray);
			controller.showArrayPanel(currentArray);
		}
		
	}

}
