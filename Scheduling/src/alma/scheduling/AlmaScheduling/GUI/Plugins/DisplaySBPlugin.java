
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

package alma.scheduling.AlmaScheduling.Plugins;

import java.util.logging.Logger;
import java.awt.Dimension;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;

import alma.exec.extension.subsystemplugin.*;

import alma.scheduling.SBLite;
import alma.scheduling.MasterSchedulerIF;

/**
  * Plugin GUI for Exec 
  *
  * @version $Id: DisplaySBPlugin.java,v 1.1 2006/08/28 20:52:08 sslucero Exp $
  */
public class DisplaySBPlugin extends JPanel implements SubsystemPlugin {

    private PluginContainerServices cs;
    private Logger logger;
    private MasterSchedulerIF ms;
    private Object[][] sbRowInfo;
    
    public void setServices(PluginContainerServices c) {
        this.cs = c;
        this.logger = cs.getLogger();
    }
    
    public void start () throws Exception {
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));
        startPlugin();
    }

    public boolean runRestricted (boolean b) throws Exception {
        return b;
    }
    public void stop() throws Exception{}

    //////////////////////////////////////////////////////

    protected void startPlugin() {
        try {
            getMS();
            displayStatus();
            displaySbLites();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    protected void getMS() {
        try{
            ms = (MasterSchedulerIF)cs.getComponent("SCHEDULING_MASTERSCHEDULER");
                //alma.scheduling.MasterSchedulerIFHelper.narrow(
                //m_containerServices.getDefaultComponent(
                //    "IDL:alma/scheduling/MasterSchedulerIF:1.0"));

        } catch(Exception e) {
            logger.severe("SCHED Plugin: Could not get master scheduler ref.");
            e.printStackTrace(System.out);
        }
            
    }
    protected void displayStatus() {
    }
    protected void displaySbLites(){
        final String[] sbColumnInfo = {"SB Name", "UID"};
        Dimension d = new Dimension(200,100);
        JPanel tablePanel = new JPanel();
        SBLite[] sblites = ms.getSBLites();  
        sbRowInfo = new Object[sblites.length][2];
        for(int i=0; i < sblites.length; i++){
            sbRowInfo[i][0] = sblites[i].sbName;
            sbRowInfo[i][1] = sblites[i].schedBlockRef;
        }
        TableModel sbTableModel = new AbstractTableModel() {
            public int getColumnCount() { return sbColumnInfo.length; }
            public String getColumnName(int col) { return sbColumnInfo[col]; }
            public int getRowCount() { return sbRowInfo.length; }
            public Object getValueAt(int row, int col) 
                { return sbRowInfo[row][col]; }
            public void setValueAt(Object val, int row, int col) 
                { sbRowInfo[row][col] = val; }
        };
        JTable sbTable= new JTable(sbTableModel);
        sbTable.setPreferredScrollableViewportSize(d);
        tablePanel.add(sbTable);
        this.add(tablePanel, BorderLayout.CENTER);
    }

}

