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
 *
 * File WeatherDataTab.java
 */

package alma.Scheduling.GUI.PlanningModeSimGUI;

import java.util.Vector;
import java.util.StringTokenizer;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;


public class WeatherDataTab extends JScrollPane {

    private Vector v;
    private JPanel weatherFields;
    //this will change when there are more than 1 weather functions.
    private JTextField name, units, p0,p1,p2,s0,s1,t0,t1;
    private JComboBox totalfuncs;
    private int totalWeatherFuncs;


    public WeatherDataTab() {
        super();
        setViewportView(createView());
    }
    
    public JPanel createView() {
        weatherFields = new JPanel();
        JPanel main = new JPanel(new BorderLayout());
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        //c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;// c. weighty = 1.0;
        JPanel panelThree_gridPanel = new JPanel();
        panelThree_gridPanel.setLayout(gridbag);
        /////////////////////////////////////////
        JLabel label = new JLabel("Weather Model");
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(label, c);
        panelThree_gridPanel.add(label);
        label = new JLabel("");
        c.gridwidth = 1;
        gridbag.setConstraints(label, c);
        panelThree_gridPanel.add(label);
        label = new JLabel ("Functions");
        gridbag.setConstraints(label, c);
        panelThree_gridPanel.add(label);
        label = new JLabel("");
        c.gridwidth = 2;
        gridbag.setConstraints(label, c);
        panelThree_gridPanel.add(label);
        label = new JLabel("Total");
        c.gridwidth = 1;
        gridbag.setConstraints(label, c);
        panelThree_gridPanel.add(label);
        totalfuncs = new JComboBox();
        totalfuncs.addItem("1");
        /* 
        panelThree_cb.addItem("2"); panelThree_cb.addItem("3"); 
        panelThree_cb.addItem("4"); panelThree_cb.addItem("5"); panelThree_cb.addItem("6"); 
        panelThree_cb.addItem("7"); panelThree_cb.addItem("8"); panelThree_cb.addItem("9"); 
        panelThree_cb.addItem("10"); 
        */
        totalfuncs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getTotalWeatherFuncs();
                //do nothing right now really...

                /*
                JComboBox x = (JComboBox)e.getSource();
                String s = (String)x.getSelectedItem();
                totalWeatherFuncs = Integer.parseInt(s);
                JPanel tmp1 = (JPanel)x.getParent();
                ((JPanel)tmp1.getParent()).add( 
                    addWeatherFunctions(totalWeatherFuncs));
                ((JPanel)tmp1.getParent()).validate();
                */
            }
        });

        gridbag.setConstraints(totalfuncs,c);
        panelThree_gridPanel.add(totalfuncs);
        c.gridwidth = GridBagConstraints.REMAINDER;
        label = new JLabel();
        gridbag.setConstraints(label, c);
        panelThree_gridPanel.add(label);
        ///////////////////////////////////////////
        main.add(panelThree_gridPanel, BorderLayout.NORTH);
        main.add(addWeatherFunctions(1), BorderLayout.CENTER);
        return main;
    }
    
    private JPanel addWeatherFunctions(int i) {
        // TODO: when more than one function, must clean before redo
        //weatherFields
        weatherFields.setLayout(new GridLayout(12,4));
        JLabel l; 
        /////////////////////////////////////////
        l = new JLabel();               weatherFields.add(l);
        l = new JLabel();               weatherFields.add(l);
        l = new JLabel();               weatherFields.add(l);
        l = new JLabel();               weatherFields.add(l);
        /////////////////////////////////////////
        l = new JLabel("Function Name");weatherFields.add(l);
        name = new JTextField("");        weatherFields.add(name);
        l = new JLabel("Units");        weatherFields.add(l);
        units = new JTextField("");        weatherFields.add(units);
        l = new JLabel();               weatherFields.add(l);
        l = new JLabel();               weatherFields.add(l);
        l = new JLabel("p0");           weatherFields.add(l);
        p0 = new JTextField("");        weatherFields.add(p0);
        l = new JLabel();               weatherFields.add(l);
        l = new JLabel();               weatherFields.add(l);
        l = new JLabel("p1");           weatherFields.add(l);
        p1 = new JTextField("");        weatherFields.add(p1);
        l = new JLabel();               weatherFields.add(l);
        l = new JLabel();               weatherFields.add(l);
        l = new JLabel("p2");           weatherFields.add(l);
        p2 = new JTextField("");        weatherFields.add(p2);
        l = new JLabel();               weatherFields.add(l);
        l = new JLabel();               weatherFields.add(l);
        l = new JLabel("s0");           weatherFields.add(l);
        s0 = new JTextField();          weatherFields.add(s0);
        l = new JLabel();               weatherFields.add(l);
        l = new JLabel();               weatherFields.add(l);
        l = new JLabel("s1");           weatherFields.add(l);
        s1 = new JTextField("");        weatherFields.add(s1);
        l = new JLabel();               weatherFields.add(l);
        l = new JLabel();               weatherFields.add(l);
        l = new JLabel("t0");           weatherFields.add(l);
        t0 = new JTextField("");        weatherFields.add(t0);
        l = new JLabel();               weatherFields.add(l);
        l = new JLabel();               weatherFields.add(l);
        l = new JLabel("t1");           weatherFields.add(l);
        t1 = new JTextField("");        weatherFields.add(t1);
        /////////////////////////////////////////
        l = new JLabel();               weatherFields.add(l);
        l = new JLabel();               weatherFields.add(l);
        l = new JLabel();               weatherFields.add(l);
        l = new JLabel();               weatherFields.add(l);
        /////////////////////////////////////////
        l = new JLabel();               weatherFields.add(l);
        l = new JLabel();               weatherFields.add(l);
        l = new JLabel();               weatherFields.add(l);
        l = new JLabel();               weatherFields.add(l);
        /////////////////////////////////////////
        return weatherFields;
    }

    ///////////////////////////////////////////////
    // Get Methods
    ///////////////////////////////////////////////
    
    public String getWeatherName(){ 
        return name.getText();
    }
    public String getUnits() {
        return units.getText();
    }
    public String getP0() {
        return p0.getText();
    }
    public String getP1() {
        return p1.getText();
    }
    public String getP2() {
        return p2.getText();
    }
    public String getS0() {
        return s0.getText();
    }
    public String getS1() {
        return s1.getText();
    }
    public String getT0() {
        return t0.getText();
    }
    public String getT1() {
        return t1.getText();
    }

    public int getTotalWeatherFuncs() {
        //return totalWeatherFuncs;
        return Integer.parseInt((String)totalfuncs.getSelectedItem());
    }
    
    ///////////////////////////////////////////////
    // Set Methods
    ///////////////////////////////////////////////
    public void setWeatherName(String s) {
        name.setText(s);
    }
    public void setUnits(String s) {
        units.setText(s);
    }
    public void setP0(String s) {
        p0.setText(s);
    }
    public void setP1(String s) {
        p1.setText(s);
    }
    public void setP2(String s) {
        p2.setText(s);
    }
    public void setS0(String s) {
        s0.setText(s);
    }
    public void setS1(String s) {
        s1.setText(s);
    }
    public void setT0(String s) {
        t0.setText(s);
    }
    public void setT1(String s) {
        t1.setText(s);
    }

    public void setTotalWeatherFuncs(String s){
        //totalfuncs.setSelectedItem(s);
        totalfuncs.setSelectedItem("1");
    }

    ///////////////////////////////////////////////

    public void loadValuesFromFile(Vector values) {
        v = values;
        setTotalWeatherFuncs((String)v.elementAt(0));
        StringTokenizer token = new StringTokenizer((String)v.elementAt(1), ";");
        setWeatherName(token.nextToken());
        setUnits(token.nextToken());
        setP0(token.nextToken());
        setP1(token.nextToken());
        setP2(token.nextToken());
        setS0(token.nextToken());
        setS1(token.nextToken());
        setT0(token.nextToken());
        setT1(token.nextToken());
        
    }

}
