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
import java.awt.event.KeyEvent;

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
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
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

        antennaPane = new JTabbedPane(JTabbedPane.TOP);
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        
        mainPanel.add(createTopPanel(), BorderLayout.NORTH);
        
        ants.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox b = (JComboBox)e.getSource();
                int val = Integer.parseInt((String)b.getSelectedItem());
                JPanel o1 = (JPanel)b.getParent();
                JPanel o2 = (JPanel)o1.getParent();
                try { 
                    o2.remove(1);
                }catch(Exception ex){}
                totalAntennas = val;
                createAntennaConfigTabs(val);
                o2.getParent().validate();
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
        for(int i=0; i < n; i++) {
            antennaPane.addTab("Antenna "+(i+1), antennaConfig());
        }
        p.add(antennaPane, BorderLayout.CENTER);
        //JScrollPane pane = new JScrollPane(p);
        mainPanel.add(antennaPane, BorderLayout.CENTER);
        mainPanel.validate();
        mainPanel.getParent().validate();
    }

    private JPanel antennaConfig() {
        JPanel p = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill =GridBagConstraints.HORIZONTAL;
        c.weightx= 1.0;
        c.gridwidth = 1;
        p.setLayout(gridbag);
        JLabel l;
        JTextField tf;
        JSeparator sep; 
        // name
        sep = new JSeparator();
        gridbag.setConstraints(sep, c);
        p.add(sep);
        l = new JLabel("Antenna Name: ");
        gridbag.setConstraints(l, c);
        p.add(l);
        tf = new JTextField();
        c.gridwidth = 4;
        c.weightx= 4.0;
        gridbag.setConstraints(tf, c);
        p.add(tf);
        sep = new JSeparator();
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(sep, c);
        p.add(sep);
        // location
        c.gridwidth = 1;
        c.weightx= 1.0;
        sep = new JSeparator();
        gridbag.setConstraints(sep, c);
        p.add(sep);
        l = new JLabel("Antenna Location: ");
        gridbag.setConstraints(l, c);
        p.add(l);
        tf = new JTextField();
        c.gridwidth = 4;
        c.weightx= 4.0;
        gridbag.setConstraints(tf, c);
        p.add(tf);
        sep = new JSeparator();
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(sep, c);
        p.add(sep);
        // pad name
        c.gridwidth = 1;
        c.weightx= 1.0;
        sep = new JSeparator();
        gridbag.setConstraints(sep, c);
        p.add(sep);
        l = new JLabel("Antenna Pad Name: ");
        gridbag.setConstraints(l, c);
        p.add(l);
        tf = new JTextField();
        c.gridwidth = 4;
        c.weightx= 4.0;
        gridbag.setConstraints(tf, c);
        p.add(tf);
        sep = new JSeparator();
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(sep, c);
        p.add(sep);
        // has nutator??
        c.gridwidth = 1;
        c.weightx= 1.0;
        sep = new JSeparator();
        gridbag.setConstraints(sep, c);
        p.add(sep);
        l = new JLabel("Nutator present");
        c.weightx= 2.0;
        gridbag.setConstraints(l, c);
        p.add(l);
        YesNoNutator nut = new YesNoNutator();
        c.gridwidth = 4;
        c.weightx= 4.0;
        gridbag.setConstraints(nut, c);
        p.add(nut);
        sep = new JSeparator();
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(sep, c);
        p.add(sep);
        return p;
    }

    //////////////////////////////////////////////////////
    // Get Methods
    //////////////////////////////////////////////////////


    public Vector getAllConfigInfo() {
        Vector allAntennas = new Vector();
        for(int i =0; i < totalAntennas; i++){
            allAntennas.add(getAntennaConfigInfo(i));
        }
        return allAntennas;
    }

    public Vector getAntennaConfigInfo(int n) {
        Vector antennaConfig = new Vector();
        JPanel p1 = (JPanel)antennaPane.getComponentAt(n);
        Component[] c = p1.getComponents();
        for(int i=0; i < c.length;i++){
            if(c[i] instanceof JTextField){
                antennaConfig.add( ((JTextField)c[i]).getText() );
            } else if(c[i] instanceof YesNoNutator) {
                System.out.println("nut result: "+((YesNoNutator)c[i]).getResult() );
                antennaConfig.add( ((YesNoNutator)c[i]).getResult() );
            }
        }
        return antennaConfig;
    }

    public int getAntennaTotal() {
        return totalAntennas;
    }

    //////////////////////////////////////////////////////
    // Set Methods
    //////////////////////////////////////////////////////
    
    public void setAntennaTotal(int i){
        totalAntennas = i;
        System.out.println("AntennaTotal = "+i);
    }

    //////////////////////////////////////////////////////
    // Methods for loading values from file
    //////////////////////////////////////////////////////

    public void loadValuesFromFile(Vector val){
        setAntennaTotal(Integer.parseInt((String)val.elementAt(0)));
        ants.setValue((String)val.elementAt(0));
        val.removeElementAt(0);
        try {
            antennaPane.removeAll();
        }catch(Exception ex){}
        String s1="";
        StringTokenizer token;
        int antennaNum=1;
        String[] s2= new String[4];
        System.out.println(val.size());
        for(int i=0; i < val.size(); i++) {
            s1 = (String)val.elementAt(i);
            token = new StringTokenizer(s1,"; ");
            s2[0] = token.nextToken().trim();
            s2[1] = token.nextToken().trim();
            s2[2] = token.nextToken().trim();
            s2[3] = token.nextToken();
            antennaPane.add("Antenna"+(antennaNum++), updateAntennaConfigTab(s2));
        }
        mainPanel.add(antennaPane, BorderLayout.CENTER);
        mainPanel.validate();
        mainPanel.getParent().validate();

    }

    public JPanel updateAntennaConfigTab(String[] s){
        JPanel p = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill =GridBagConstraints.HORIZONTAL;
        c.weightx= 1.0;
        c.gridwidth = 1;
        p.setLayout(gridbag);
        JLabel l;
        JTextField tf;
        JSeparator sep; 
        // name
        sep = new JSeparator();
        gridbag.setConstraints(sep, c);
        p.add(sep);
        l = new JLabel("Antenna Name: ");
        gridbag.setConstraints(l, c);
        p.add(l);
        //System.out.println(s[0]);
        tf = new JTextField(s[0]);
        c.gridwidth = 4;
        c.weightx= 4.0;
        gridbag.setConstraints(tf, c);
        p.add(tf);
        sep = new JSeparator();
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(sep, c);
        p.add(sep);
        // location
        c.gridwidth = 1;
        c.weightx= 1.0;
        sep = new JSeparator();
        gridbag.setConstraints(sep, c);
        p.add(sep);
        l = new JLabel("Antenna Location: ");
        gridbag.setConstraints(l, c);
        p.add(l);
        //System.out.println(s[1]);
        tf = new JTextField(s[1]);
        c.gridwidth = 4;
        c.weightx= 4.0;
        gridbag.setConstraints(tf, c);
        p.add(tf);
        sep = new JSeparator();
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(sep, c);
        p.add(sep);
        // pad name
        c.gridwidth = 1;
        c.weightx= 1.0;
        sep = new JSeparator();
        gridbag.setConstraints(sep, c);
        p.add(sep);
        l = new JLabel("Antenna Pad Name: ");
        gridbag.setConstraints(l, c);
        p.add(l);
        //System.out.println(s[2]);
        tf = new JTextField(s[2]);
        c.gridwidth = 4;
        c.weightx= 4.0;
        gridbag.setConstraints(tf, c);
        p.add(tf);
        sep = new JSeparator();
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(sep, c);
        p.add(sep);
        // has nutator??
        c.gridwidth = 1;
        c.weightx= 1.0;
        sep = new JSeparator();
        gridbag.setConstraints(sep, c);
        p.add(sep);
        l = new JLabel("Nutator present");
        c.weightx= 2.0;
        gridbag.setConstraints(l, c);
        p.add(l);
        //System.out.println(s[3]);
        YesNoNutator nut = new YesNoNutator(s[3]);
        c.gridwidth = 4;
        c.weightx= 4.0;
        gridbag.setConstraints(nut, c);
        p.add(nut);
        sep = new JSeparator();
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(sep, c);
        p.add(sep);
        return p;
    }

    //////////////////////////////////////////////////////
    // Nested class 
    //////////////////////////////////////////////////////

    class YesNoNutator extends JPanel implements ActionListener {
        JRadioButton yes;
        JRadioButton no;
        String value;

        public YesNoNutator() {
            yes = new JRadioButton("yes");
            yes.setActionCommand("yes");
            yes.addActionListener(this);
            no = new JRadioButton("no");
            no.setActionCommand("no");
            no.addActionListener(this);

            add(yes);
            add(no);
            value="no";
            ButtonGroup group = new ButtonGroup();
            group.add(yes);
            group.add(no);
        }

        public YesNoNutator(String val) {
            this();
            setResult(val);
        }

        public String getResult() {
            return value;
        }

        private void setResult(String v) {
            value = v;
        }

        public void actionPerformed(ActionEvent e) {
            setResult(e.getActionCommand());
        }
        
    }
}
