
/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 */

package alma.scheduling.AlmaScheduling.GUI.Plugins;

import java.util.logging.Logger;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.border.TitledBorder;

import alma.exec.extension.subsystemplugin.*;

import alma.scheduling.SBLite;
import alma.scheduling.MasterSchedulerIF;

/**
  * Plugin GUI for Exec 
  *
  * @version $Id: DisplaySBPlugin.java,v 1.3 2006/08/29 22:55:33 sslucero Exp $
  */
public class DisplaySBPlugin extends JPanel implements SubsystemPlugin {

    private final String[] sbColumnInfo = {"SB Name","PI", "Project Name", "UID"};
    private TableModel sbTableModel;
    private JTable sbTable;
    private PluginContainerServices cs;
    private Logger logger;
    private MasterSchedulerIF ms = null;
    private SBLite[] sblites=null;
    private Object[][] sbRowInfo;
    private JPanel eastPanel;
    private JPanel westPanel;
    private JPanel detailsPanel;
    private JTextArea detailsTA;
    
    public DisplaySBPlugin (){}

    public void setServices(PluginContainerServices c) {
        this.cs = c;
        this.logger = cs.getLogger();
    }
    
    public void start () throws Exception {
        setLayout(new GridLayout(1,2));
        startPlugin();
        setVisible(true);
    }

    public boolean runRestricted (boolean b) throws Exception {
        return b;
    }
    public void stop() throws Exception{
        releaseMS();
    }

    //////////////////////////////////////////////////////

    private void startPlugin() {
        try {
            if (cs != null){
                getMS();
            } 
            setup();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    private void setup() {
        eastPanel = new JPanel();
        eastPanel.setBorder(new TitledBorder("All SBs"));
        westPanel = new JPanel(new BorderLayout());
        
        displaySbLites();
        displayDetails();
        displayRefreshButton();
        add(eastPanel);
        add(westPanel);
    }

    private void displaySbLites(){
        Dimension d = new Dimension(200,100);
        JPanel tablePanel = new JPanel();
        //populateSbRowInfo();
        sblites= null;
        if (ms != null){
            sblites = ms.getSBLites();  
        } else {
            sblites = new SBLite[1];
            sblites[0] = new SBLite();
            sblites[0].sbName = "foo";
            sblites[0].PI = "foo's PI";
            sblites[0].projectName = "foo's project";
            sblites[0].schedBlockRef = "n/a";
        }
        sbRowInfo = new Object[sblites.length][sbColumnInfo.length];
        for(int i=0; i < sblites.length; i++){
            sbRowInfo[i][0] = sblites[i].sbName;
            sbRowInfo[i][1] = sblites[i].PI;
            sbRowInfo[i][2] = sblites[i].projectName;
            sbRowInfo[i][3] = sblites[i].schedBlockRef;
        }
        createSBTableModel();
        sbTable = new JTable(sbTableModel);
        sbTable.addMouseListener(new MouseListener(){
            public void mouseClicked(MouseEvent e) {
                showSBInfo();
            }
            public void mouseEntered(MouseEvent e){ }
            public void mouseExited(MouseEvent e){ }
            public void mousePressed(MouseEvent e){ }
            public void mouseReleased(MouseEvent e){}
        });
        sbTable.setPreferredScrollableViewportSize(d);
        sbTable.doLayout();
        JScrollPane pane = new JScrollPane(sbTable);
        tablePanel.add(pane);
        eastPanel.add(tablePanel);
    }
    private void createSBTableModel() {
        sbTableModel= new AbstractTableModel() {
            public int getColumnCount() { return sbColumnInfo.length; }
            public String getColumnName(int col) { return sbColumnInfo[col]; }
            public int getRowCount() { return sbRowInfo.length; }
            public Object getValueAt(int row, int col) 
                { return sbRowInfo[row][col]; }
            public void setValueAt(Object val, int row, int col) 
                { sbRowInfo[row][col] = val; }
        };
    }

    private void displayDetails() {
        detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBorder(new TitledBorder("SB Details"));
        westPanel.add(detailsPanel, BorderLayout.CENTER);
    }

    private void displayRefreshButton() {
        JPanel p = new JPanel(new GridLayout(1,3));
        JButton b = new JButton("Refresh");
        b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updateSBTable();
                }
        });
        p.add(new JLabel());
        p.add(b);
        p.add(new JLabel());
        westPanel.add(p,BorderLayout.SOUTH);
    }

    private void getMS() {
        try{
            if(ms == null) {
                ms = alma.scheduling.MasterSchedulerIFHelper.narrow(
                        cs.getDefaultComponent(
                            "IDL:alma/scheduling/MasterSchedulerIF:1.0"));
            }
        } catch(Exception e) {
            ms = null;
            logger.severe("SCHED Plugin: Could not get master scheduler ref.");
            e.printStackTrace(System.out);
        }
    }

    private void releaseMS() {
        try {
            if(ms != null){
                cs.releaseComponent(ms.name());
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private void populateSbRowInfo() {
        sblites= null;
        if(ms == null) {
            sblites = new SBLite[2];
            sblites[0] = new SBLite();
            sblites[0].sbName = "foo";
            sblites[0].PI = "foo's PI";
            sblites[0].projectName = "foo's project";
            sblites[0].schedBlockRef ="n/a";

            sblites[1] = new SBLite();
            sblites[1].sbName = "foo2";
            sblites[1].PI = "foo2's PI";
            sblites[1].projectName = "foo2's project";
            sblites[1].schedBlockRef ="n/a";
        } else {
            try {
                sblites = ms.getSBLites();  
            }catch(Exception e){
                e.printStackTrace();
             }
        }
        sbRowInfo = new Object[sblites.length][sbColumnInfo.length];
        for(int i=0; i < sblites.length; i++){
            sbRowInfo[i][0] = sblites[i].sbName;
            sbRowInfo[i][1] = sblites[i].PI;
            sbRowInfo[i][2] = sblites[i].projectName;
            sbRowInfo[i][3] = sblites[i].schedBlockRef;
        }
    }

    private void updateSBTable() {
        populateSbRowInfo();
        System.out.println(sbRowInfo.length);
        sbTable.revalidate();
        validate();
        System.out.println("should be updated now");
    }

    private void showSBInfo() {
        try {//remove anything previously viewed in here
            detailsPanel.removeAll();
            detailsPanel.revalidate();
            validate();
        } catch(Exception e) { /* don't care if it complains! */ }
        if(!checkOneSelected()) {
            //show nothing
            System.out.println("more than one");
            return;
        }
        int row = sbTable.getSelectedRow();
        SBLite sb=null;
        for(int i=0; i < sblites.length; i++){
            if(sblites[i].schedBlockRef.equals(sbRowInfo[row][3])){
                sb = sblites[i];
                break;
            }
        }
        detailsTA = new JTextArea("Details:\n");
        detailsTA.append("SB Name: "+ sb.sbName +"\n");
        detailsTA.append("PI Name: "+sb.PI +"\n");
        detailsPanel.add(detailsTA, BorderLayout.CENTER);
        detailsPanel.revalidate();
        validate();
    }

    private boolean checkOneSelected() {
        int[] rows = sbTable.getSelectedRows();
        if(rows.length > 1 ){
            return false;
        } else {
            return true;
        }
    }

    public static void main(String[] args){
        try {
            JFrame f = new JFrame();
            DisplaySBPlugin foo = new DisplaySBPlugin();
            foo.start();
            f.add(foo);
            f.pack();
            f.setVisible(true);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

