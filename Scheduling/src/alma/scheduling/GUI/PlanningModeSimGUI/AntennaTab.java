/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
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
 * File ProjectsTab.java
 */
package alma.scheduling.GUI.PlanningModeSimGUI;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.lang.Integer;
import java.util.Vector;
import java.util.StringTokenizer;

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

/**
 * This class extends the JScrollPane and includes everything required for the
 * PlanningModeSim gui antenna tab. The Tab in the gui where all the information
 * specific to the antennas and their configuration info.
 *
 * @author Sohaila Roberts
 */
public class AntennaTab extends JScrollPane {

    private Vector v;
    private int totalAntennas = 0;
    private JPanel mainPanel;
    private JTabbedPane antennaPane;
    private JPanel antennaPanel;
    private GUIAntennas ants;

    public AntennaTab() {
        super();
        setViewportView(createView());
    }

    /**
     * Creates in initial display.
     *
     * @return JPanel
     */
    public JPanel createView() {

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        
        mainPanel.add(createTopPanel(), BorderLayout.NORTH);
        
        ants.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JComboBox b = (JComboBox)e.getSource();
                    int val = Integer.parseInt((String)b.getSelectedItem());
                    createAntennaConfigTabs(val);
                }
        });
        return mainPanel;
    }

    private JPanel createTopPanel() {
        JPanel p = new JPanel();
        JLabel label = new JLabel("Select the number of antennas");
        p.add(new JSeparator());
        p.add(label);
        ants = new GUIAntennas();
        p.add(ants);
        p.add(new JSeparator());
        return p;
    }
    
    private void createAntennaConfigTabs(int n){
        
        JPanel p = new JPanel(new BorderLayout());
        JTabbedPane antennasPane = new JTabbedPane(JTabbedPane.TOP);
        for(int i=0; i < n; i++) {
            antennasPane.addTab("Antenna "+(i+1), antennaConfig());
        }
        p.add(antennasPane, BorderLayout.CENTER);
        JScrollPane pane = new JScrollPane(p);
        mainPanel.add(pane, BorderLayout.CENTER);
        mainPanel.validate();
        mainPanel.getParent().validate();
    }

    private JScrollPane antennaConfig() {
        JPanel p = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.fill =GridBagConstraints.HORIZONTAL;
        c.weightx= 1.0;
        p.setLayout(gridbag);
        JLabel l;
        JTextField tf;
        // name
        p.add(new JSeparator());
        l = new JLabel("Antenna Name: ");
        p.add(l);
        tf = new JTextField();
        p.add(tf);
        p.add(new JSeparator());
        // location
        p.add(new JSeparator());
        l = new JLabel("Antenna Location: ");
        p.add(l);
        tf = new JTextField();
        p.add(tf);
        p.add(new JSeparator());
        // pad name
        p.add(new JSeparator());
        l = new JLabel("Antenna Pad Name: ");
        p.add(l);
        tf = new JTextField();
        p.add(tf);
        p.add(new JSeparator());
        // has nutator??
        p.add(new JSeparator());
        l = new JLabel("Does this antenna have a nutator?");
        p.add(l);
        p.add(new JSeparator());
        p.add(new JSeparator());
        JScrollPane ant = new JScrollPane(p);
        return ant;
    }

    //////////////////////////////////////////////////////
    // Get Methods
    //////////////////////////////////////////////////////
    

}
