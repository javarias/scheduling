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

package alma.scheduling.GUI.PlanningModeSimGUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;


public class WeatherDataTab extends JScrollPane {

    private Vector v;
    private JPanel initialPanel, realModelPanel;
    //this will change when there are more than 1 weather functions.
    private JTextField name, units, p0,p1,p2,s0,s1,t0,t1;
    private JTextField windFile, rmsFile, opacityFile;
    private JComboBox modelType, totalfuncs;
    private int totalWeatherFuncs, totalWeatherFiles;
    private boolean isRealMode;
    private JFrame parent;


    public WeatherDataTab(JFrame p) {
        super();
        parent = p;
        setViewportView(createSelectModelView());
    }
    
    private JPanel createSelectModelView(){
        initialPanel = new JPanel();
        initialPanel.add(new JLabel("Weather Model type: "));
        modelType = new JComboBox();
        modelType.addItem("Select Type");
        modelType.addItem("Real Weather Model");
        modelType.addItem("Diurnal Weather Model");
        modelType.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                displayWeatherModel();
            }
        });
        initialPanel.add(modelType);
        return initialPanel;
    }

    private void displayWeatherModel() {
        String type = (String)modelType.getSelectedItem();
        if(type.equals("Real Weather Model")){
            isRealMode = true;
            setViewportView(createRealModelView());
        } else if(type.equals("Diurnal Weather Model")){
            isRealMode = false;
            setViewportView(createDiurnalView());
        }
    }

    private JPanel createRealModelView(){
        JPanel main= new JPanel (new BorderLayout());
        realModelPanel = new JPanel();
        Dimension tfSize = new Dimension(200,20);
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
        
        JPanel files = new JPanel();
        JPanel labels = new JPanel();
        JPanel buttons = new JPanel();
        
        files.setLayout(new BoxLayout(files, BoxLayout.Y_AXIS));
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        
        JLabel opacityL = new JLabel("Opacity Datafile: ");
        labels.add(opacityL);
        opacityFile = new JTextField();
        opacityFile.setPreferredSize(tfSize);
        files.add(opacityFile);
        JButton opacityB = new JButton("Browse");
        opacityB.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e){
                    opacityFile.setText(((PlanningModeSimGUI)parent).createFileChooser("load","Load Opacity Data File","txtfile"));
                }
        });
        buttons.add(opacityB);
        
        JLabel rmsL = new JLabel("RMS Datafile: ");
        labels.add(rmsL);
        rmsFile = new JTextField();
        rmsFile.setPreferredSize(tfSize);
        files.add(rmsFile);
        JButton rmsB = new JButton("Browse");
        rmsB.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e){
                    rmsFile.setText(((PlanningModeSimGUI)parent).createFileChooser("load","Load RMS Data File","txtfile"));
                }
        });
        buttons.add(rmsB);
        JLabel windL = new JLabel("Wind Velocity Datafile: ");
        labels.add(windL);
        windFile = new JTextField();
        windFile.setPreferredSize(tfSize);
        files.add(windFile);
        JButton windB = new JButton("Browse");
        windB.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e){
                    windFile.setText(((PlanningModeSimGUI)parent).createFileChooser("load","Load Wind Data File","txtfile"));
                }
        });
        buttons.add(windB);
        
        centerPanel.add(labels);
        centerPanel.add(files);
        centerPanel.add(buttons);
        realModelPanel.add(centerPanel);
        main.add(realModelPanel, BorderLayout.CENTER);
        main.add(createSelectModelView(), BorderLayout.SOUTH);
        return main;
    }

    private JPanel createDiurnalView() {
        JPanel main = new JPanel(new BorderLayout());
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
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
        totalfuncs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getTotalWeatherFuncs();
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
        main.add(createSelectModelView(), BorderLayout.SOUTH);
        return main;
    }
    
    private JPanel addWeatherFunctions(int i) {
        // TODO: when more than one function, must clean before redo
        //weatherFields
        JPanel weatherFields = new JPanel();
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

    public boolean getIsRealMode(){
        return isRealMode;
    }
    public String getOpacityFile(){
        return opacityFile.getText();
    }
    public String getRMSFile(){
        return rmsFile.getText();
    }
    public String getWindFile(){
        return windFile.getText();
    }
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

        if(isRealMode) {
            return totalWeatherFiles;
        } else {
            return Integer.parseInt((String)totalfuncs.getSelectedItem());
        }
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
        if(isRealMode) {
            totalWeatherFiles = Integer.parseInt(s);
        } else {
            totalfuncs.setSelectedItem(Integer.parseInt(s));
        }
    }

    ///////////////////////////////////////////////

    public void loadValuesFromFile(Vector values) {
        v = values;
        StringTokenizer token;
        String name;
        if(((String)v.elementAt(0)).equals("real")) {
            isRealMode = true;
            setTotalWeatherFuncs((String)v.elementAt(1));
            setViewportView(createRealModelView());
            for(int i=2; i < v.size(); i++){
                token = new StringTokenizer((String)v.elementAt(i), ";");
                name = token.nextToken();
                if(name.equals("wind")){
                    windFile.setText( token.nextToken().trim());
                } else if(name.equals("rms")){
                    rmsFile.setText( token.nextToken().trim());
                } else if(name.equals("opacity")){
                    opacityFile.setText( token.nextToken().trim());
                } else {
                    System.out.println("Software needs to be updated to take "+
                    "new weather parameters into account");
                }
            }
        } else if( ((String)v.elementAt(0)).equals("diurnal")){
            isRealMode = false;
            setViewportView(createDiurnalView());
            setTotalWeatherFuncs((String)v.elementAt(1));
            token = new StringTokenizer((String)v.elementAt(2), ";");
            setWeatherName(token.nextToken());
            setUnits(token.nextToken());
            setP0(token.nextToken());
            setP1(token.nextToken());
            setP2(token.nextToken());
            setS0(token.nextToken());
            setS1(token.nextToken());
            setT0(token.nextToken());
            setT1(token.nextToken());
        } else {
            System.out.println("Invalid Weather Mode");
            return;
        }
        
        
    }

}
