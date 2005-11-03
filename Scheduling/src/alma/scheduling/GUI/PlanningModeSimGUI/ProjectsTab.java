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

import java.util.Vector;
import java.util.StringTokenizer;

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

/**
 * This class extends the JScrollPane and includes everything required for the
 * PlanningModeSim gui project tab. The Tab in the gui where all the information
 * specific to the projects/sets/targets are displayed. 
 *
 * @author Sohaila Roberts
 */
public class ProjectsTab extends JScrollPane {

    private Vector v;
    private int totalProjects = 0;
    private JPanel panelFive_main;
    private JTabbedPane projectPane;
    private JTextField totalproj_tf;

    public ProjectsTab() {
        super();
        setViewportView(createView());
    }

    /**
     * Creates in initial display.
     *
     * @return JPanel
     */
    public JPanel createView() {

        projectPane = new JTabbedPane();
        /*
        projectPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JTabbedPane pane = (JTabbedPane)e.getSource();
                int i = pane.getSelectedIndex();
                JScrollPane scroll = (JScrollPane)pane.getComponent(i);
                pane.setSelectedComponent(scroll);
            }
        });*/
        panelFive_main = new JPanel(new BorderLayout());
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0; c. weighty = 1.0;
        ///////////////////
        JPanel header = new JPanel(gridbag);
        JLabel l = new JLabel();
        l = new JLabel("Project Specifications");
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(l,c);
        header.add(l);
        l = new JLabel();
        c.gridwidth =1;
        gridbag.setConstraints(l,c);
        header.add(l);
        l = new JLabel("Total projects");
        gridbag.setConstraints(l,c);
        header.add(l);
        totalproj_tf = new JTextField("Enter # of projects");
        totalproj_tf.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                JTextField tf = (JTextField)e.getSource();
                String s = tf.getText();
                int i;
                if(s == null || s.equals("") 
                  || s.equals("Enter # of projects") 
                  || s.equals("Enter a number!")) {

                    tf.setText("Enter a number!");
                }else {
                    i = Integer.parseInt(s);
                    try {
                        projectPane.removeAll();
                    }catch(Exception ex) {}
                    for(int x=0; x< i; x++) {
                        projectPane.add("Project "+(x+1),addProjectTab());
                    }
                    panelFive_main.add(projectPane, BorderLayout.CENTER);
                    panelFive_main.validate();
                    panelFive_main.getParent().validate();
                }
            }
        });
        gridbag.setConstraints(totalproj_tf,c);
        //tf.selectAll();
        header.add(totalproj_tf);
        l = new JLabel();
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(l,c);
        header.add(l);
        ///////////////////
        panelFive_main.add(header, BorderLayout.NORTH);
        return panelFive_main;
    }
    
    /**
     * A new Tab is created for each project the user wants to create.
     * This function creates that tab.
     *
     * @return JScrollPane
     */
    private JScrollPane addProjectTab() {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0; c.weighty = 1.0;
        JPanel main = new JPanel(new BorderLayout());
        JPanel p = new JPanel(gridbag);
        /////////////////////
        JLabel l; JTextField tf;
        c.gridwidth=1;
        l = new JLabel("Project Name");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField("");
        gridbag.setConstraints(tf,c);
        p.add(tf);
        l = new JLabel("PI name");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField("");
        gridbag.setConstraints(tf,c);
        p.add(tf);
        l = new JLabel("Priority");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField("");
        gridbag.setConstraints(tf,c);
        p.add(tf);
        l = new JLabel("# of Sets");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField("");
        tf.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JTextField tf = (JTextField)e.getSource();
                String s = tf.getText();
                int sets = Integer.parseInt(s);
                JPanel p1 = (JPanel)tf.getParent();
                JPanel projPanel = (JPanel)p1.getParent();
                try {
                    projPanel.remove(1);
                } catch(Exception ex) {}
                JTabbedPane setsPane = new JTabbedPane();
                for(int i=0; i < sets; i++) {
                    setsPane.addTab("Set "+(i+1), addSetPanel());
                }
                projPanel.add(setsPane);
                projPanel.validate();
                
            }
        });
        c.gridwidth= GridBagConstraints.REMAINDER;
        gridbag.setConstraints(tf,c);
        p.add(tf);
        main.add(p, BorderLayout.NORTH);
        /////////////////////
        JScrollPane pane = new JScrollPane(main);
        return pane;
    }

    /**
     * A new tab is created for each set in each project. This function 
     * creates that display.
     *
     * @return JScrollPane
     */
    public JScrollPane addSetPanel() {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0; c.weighty = 1.0;
        JPanel main = new JPanel(new BorderLayout());
        JPanel p = new JPanel(gridbag);
        ///////////////////
        JLabel l; JTextField tf;
        c.gridwidth = 1;
        l = new JLabel("Name of Set");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField();
        gridbag.setConstraints(tf,c);
        p.add(tf);
        l = new JLabel();
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(l,c);
        p.add(l);
        l= new JLabel("Frequency Band");
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField();
        gridbag.setConstraints(tf,c);
        p.add(tf);
        l= new JLabel("Frequency");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField();
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(tf,c);
        p.add(tf);  
        l= new JLabel("Weather Condition");
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        p.add(l);
        JComboBox cb = new JComboBox();
        cb.addItem("Exceptional");
        cb.addItem("Excellent");
        cb.addItem("Good");
        cb.addItem("Average");
        cb.addItem("Below Ave.");
        cb.addItem("Poor");
        cb.addItem("Dismal");
        cb.addItem("Any");
        cb.setSelectedItem("Any");
        gridbag.setConstraints(cb,c);
        p.add(cb);
        l= new JLabel("# of Targets");
        c.gridwidth = GridBagConstraints.RELATIVE;
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField();
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(tf,c);
        tf.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JTextField tf = (JTextField)e.getSource();
                String s = tf.getText();
                int targets = Integer.parseInt(s);
                JPanel p1 = (JPanel)tf.getParent(); //main panel
                JPanel tmp1 = (JPanel)p1.getParent();
                try {
                    tmp1.remove(1);
                } catch(Exception ex) {}
                JPanel p2 = new JPanel(new GridLayout(targets,1));
                JPanel tmp;
                for(int i=0; i < targets; i++) {
                    tmp = addTargetsField();
                    p2.add(tmp);
                }
                tmp1.add(p2);
                tmp1.getParent().validate();
            }
        });
        p.add(tf);  
        l = new JLabel("-----");
        gridbag.setConstraints(l,c);
        p.add(l);
        
        main.add(p, BorderLayout.NORTH);
        ///////////////////
        JScrollPane pane = new JScrollPane(main);
        return pane;
        
    }

    /**
     * The targets of each set are displayed in the fields created in this 
     * function.
     * @return JPanel
     */
    private JPanel addTargetsField() {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0; //c.weighty = 1.0;
        JPanel p = new JPanel(gridbag);
        JLabel l = new JLabel();
        /*
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(l,c);
        p.add(l);
        */
        //////////////////
        c.gridwidth = 1;
        l =new JLabel("-- Target Name");
        gridbag.setConstraints(l,c);
        p.add(l);
        JTextField tf = new JTextField("");
        gridbag.setConstraints(tf,c);
        p.add(tf);
        l = new JLabel("RA");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField("");
        gridbag.setConstraints(tf,c);
        p.add(tf);
        l = new JLabel("DEC");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField("");
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(tf,c);
        p.add(tf);
        ////////////////////////
        l = new JLabel();
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        p.add(l);
        l = new JLabel();
        gridbag.setConstraints(l,c);
        p.add(l);
        l = new JLabel("Freq.");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField("");
        gridbag.setConstraints(tf,c);
        p.add(tf);
        l = new JLabel("Total Time");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField("");
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(tf,c);
        p.add(tf);
        ////////////////////////
        l = new JLabel();
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        p.add(l);
        l = new JLabel();
        gridbag.setConstraints(l,c);
        p.add(l);
        l = new JLabel("Weather");
        gridbag.setConstraints(l,c);
        p.add(l);
        JComboBox cb = new JComboBox();
        cb.addItem("Exceptional");
        cb.addItem("Excellent");
        cb.addItem("Good");
        cb.addItem("Average");
        cb.addItem("Below Ave.");
        cb.addItem("Poor");
        cb.addItem("Dismal");
        cb.addItem("Any");
        cb.setSelectedItem("Any");
        gridbag.setConstraints(cb,c);
        p.add(cb);
        l = new JLabel("Repeat Count");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField("");
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(tf,c);
        p.add(tf);
        return p;
    }

    //////////////////////////////////////////////////////
    // Get Methods
    //////////////////////////////////////////////////////
    
    
    /**
     * Collects all the information out of the textfields and panels that 
     * display all the information entered about the project. It is stored 
     * in a vector in the order that it was collected.
     * @return Vector
     */
    public Vector getProjects() {
        Vector allProjects = new Vector(); //projects vector
        try {
            totalProjects = projectPane.getTabCount();
            //System.out.println("Total projects = "+totalProjects);
            for(int i=0; i < totalProjects; i++) {
                Vector v = new Vector();
                Component scrollPane = projectPane.getComponentAt(i);
                Component[] tabPane = ((JScrollPane)scrollPane).getComponents();
                Component[] tabPaneComps = ((JViewport)tabPane[0]).getComponents();
                JPanel mainPanel = (JPanel)tabPaneComps[0];
                Component[] mainPanelComps = mainPanel.getComponents();
                //System.out.println(mainPanelComps.length);
                //System.out.println(mainPanelComps[0].getClass().getName());
                //System.out.println(mainPanelComps[1].getClass().getName());
                JPanel projectInfoPanel = (JPanel)mainPanelComps[0];
                //JPanel targetsInfoPanel = (JPanel)mainPanelComps[1];
                JTabbedPane setsTabs = (JTabbedPane) mainPanelComps[1];

                Component[] projectInfo = projectInfoPanel.getComponents();
                v.add( ((JTextField)projectInfo[1]).getText() );//project name
                v.add( ((JTextField)projectInfo[3]).getText() ); //PI
                v.add( ((JTextField)projectInfo[5]).getText() ); //priority
                v.add( ((JTextField)projectInfo[7]).getText() ); //number of sets
                
                Component[] setsPaneComps = setsTabs.getComponents();
                //System.out.println(setsPaneComps.length);// The number of sets equals this.
                //System.out.println(setsPaneComps[0].getClass().getName());//JScrollPane
                //for each scroll pane in setsPaneComps get the internals.
                int numberOfSets = setsPaneComps.length;// The number of sets equals this.
                Vector sets = new Vector();
                for (int j=0; j < numberOfSets;j++){
                    sets.add( getSetsInfo( ((JScrollPane)setsPaneComps[j]).getComponents() ) );
                }
                v.add(sets);
                     
                allProjects.add(v);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return allProjects;
    }

    /**
      *
      */
    public Vector getSetsInfo(Component[] comp) throws Exception {
        int len = comp.length;
        Vector setStuff = new Vector();//First 5 = set info stuff, next = target stuff
        /*System.out.println(len);
        for(int i=0; i < len; i++){
            System.out.println(comp[i].getClass().getName());
        }
        */
        Component[] setsContents = ((JViewport)comp[0]).getComponents();
        Component[] setsPanel = ((JPanel)setsContents[0]).getComponents();
        
        //System.out.println(setsPanel.length);
        //System.out.println(setsPanel[0].getClass().getName());
        //System.out.println(setsPanel[1].getClass().getName());
        Component[] setsInfo = ((JPanel)setsPanel[0]).getComponents(); //12 items
        setStuff.add(((JTextField)setsInfo[1]).getText());//sets name
        setStuff.add(((JTextField)setsInfo[4]).getText());//freq band
        setStuff.add((String)((JComboBox)setsInfo[8]).getSelectedItem());//weather cond.
        setStuff.add(((JTextField)setsInfo[6]).getText());//freq
        setStuff.add(((JTextField)setsInfo[10]).getText());//# of targets
        /*
        for(int i=0; i < setStuff.size();i++){
            System.out.println(setStuff.elementAt(i));
        }
        */
        //System.out.println(setsInfo.length);
        Component[] targets = ((JPanel)setsPanel[1]).getComponents();
        Vector targetsInfo = new Vector(targets.length);
        for(int i=0; i < targets.length;i++){
            targetsInfo.add(getTargetsInfo(((JPanel)targets[i]).getComponents()));
        }
        setStuff.add(targetsInfo);
        //There will be a JPanel for each target.
        int targetLen = targets.length;
        return setStuff;
    }

    /** 
     * Collects all the information about the targets and stores it in a vector.
     *
     * @return Vector
     */
    public Vector getTargetsInfo(Component[] comp) throws Exception {
        int totaltargets = comp.length;
        Vector target = new Vector(); //holds all the targets info.
        target.add(((JTextField)comp[1]).getText()); //targetName
        target.add(((JTextField)comp[3]).getText()); //RA
        target.add(((JTextField)comp[5]).getText()); //DEC
        target.add(((JTextField)comp[9]).getText()); //FREQ
        target.add(((JTextField)comp[11]).getText()); //total time
        target.add((String)((JComboBox)comp[15]).getSelectedItem()); //weather
        target.add(((JTextField)comp[17]).getText()); //repeat count
           
        return target;
    }

    /**
     * Returns the total number of projects.
     *
     * @return int
     */
    public int getTotalProjectCount() {
        return totalProjects;
    }

    //////////////////////////////////////////////////////
    // Set Methods
    //////////////////////////////////////////////////////

    /**
     * Sets the number of projects.
     * @param s The string representation of the number of projects.
     */
    public void setTotalProjects(String s) {
        totalProjects = Integer.parseInt(s.trim());
        totalproj_tf.setText(s);
    }

    
    //////////////////////////////////////////////////////

    /**
     * A vector containing all information regarding the projects, its sets
     * and its targets are submitted in a vector. The vector is parsed and the 
     * values are then displayed
     *
     * @param values The vector containing all info.
     */
    public void loadValuesFromFile(Vector values) {
        v = values;
        setTotalProjects((String)v.elementAt(0));
        v.removeElementAt(0);
        try {
            projectPane.removeAll();
        }catch(Exception ex) {}
        
        String s1="", s2="", s3="", tmp="";
        StringTokenizer token;
        int projects=1, sets=0, targets=0;
        Vector sets_v = new Vector(), targets_v = new Vector();
        while( !v.isEmpty() ) {
            s1 = (String)v.elementAt(0); //project info line
            v.removeElementAt(0);
            token = new StringTokenizer(s1,";");
            //get the last token
            while(token.hasMoreTokens()){
                tmp = token.nextToken().trim();
            }
            sets = Integer.parseInt(tmp.trim());
            if(sets > 0) {
                sets_v = new Vector();
            }
            for(int i=0; i < sets; i++) {
                s2 = (String) v.elementAt(0); //set info line
                sets_v.add(s2);
                v.removeElementAt(0);
                token = new StringTokenizer(s2, ";");
                while(token.hasMoreTokens()){
                    tmp = token.nextToken().trim();
                }
                targets = Integer.parseInt(tmp.trim());
                if(targets > 0) {
                    targets_v = new Vector();
                }
                for(int j=0; j < targets; j++) {
                    s3 = (String) v.elementAt(0); //target info
                    targets_v.add(s3);
                    v.removeElementAt(0);
                }
            }
            projectPane.add("Project "+projects, updateProjectTab(s1, sets_v, targets_v));
            projects++;
        }

        panelFive_main.add(projectPane, BorderLayout.CENTER);
        panelFive_main.validate();
    }

    /**
     * Creates a JScrollPane with all the updated project information.
     * 
     * @param projInfo A vector containing the main project information
     * @param setsInfo A vector containing the main info about the sets
     * @param targetsInfo A vector containing the information about the targets.
     *
     * @return JScrollPane
     */
    public JScrollPane updateProjectTab(String projInfo, Vector setsInfo, Vector targetsInfo) {
        for(int i=0; i< targetsInfo.size(); i++){
            System.out.println(targetsInfo.elementAt(i));
        }
        StringTokenizer token = new StringTokenizer(projInfo,";");
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0; c.weighty = 1.0;
        JPanel main = new JPanel(new BorderLayout());
        JPanel p = new JPanel(gridbag);
        /////////////////////
        JLabel l; JTextField tf;
        c.gridwidth=1;
        l = new JLabel("Project Name");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField((token.nextToken()).trim());
        gridbag.setConstraints(tf,c);
        p.add(tf);
        l = new JLabel("PI name");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField((token.nextToken()).trim());
        gridbag.setConstraints(tf,c);
        p.add(tf);
        l = new JLabel("Priority");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField((token.nextToken()).trim());
        gridbag.setConstraints(tf,c);
        p.add(tf);
        l = new JLabel("# of Sets");
        gridbag.setConstraints(l,c);
        p.add(l);
        String sets = token.nextToken();
        int setscount = Integer.parseInt(sets.trim());
        tf = new JTextField(sets.trim());
        c.gridwidth= GridBagConstraints.REMAINDER;
        gridbag.setConstraints(tf,c);
        p.add(tf);

        main.add(p, BorderLayout.NORTH);
        
        JTabbedPane setsPane = new JTabbedPane();
        for(int i=0; i < setsInfo.size(); i++) {
            //System.out.println("Target vector size = "+ targetsInfo.size());
            setsPane.addTab("Set "+(i+1), updateSetPanel((String)setsInfo.elementAt(i), targetsInfo));
        }
        main.add(setsPane, BorderLayout.CENTER);
        main.validate();

        JScrollPane pane = new JScrollPane(main);
        return pane;
    }

    /**
     *
     * @param setString A String that has the set information.
     * @param targets A Vector containing the information about all the targets in this set.
     * @return JScrollPane
     */
    public JScrollPane updateSetPanel(String setString, Vector targets) {
        StringTokenizer st = new StringTokenizer(setString,";");
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0; c.weighty = 1.0;
        JPanel main = new JPanel(new BorderLayout());
        JPanel p = new JPanel(gridbag);
        ///////////////////
        JLabel l; JTextField tf;
        c.gridwidth = 1;
        l = new JLabel("Name of Set");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField(st.nextToken().trim());
        gridbag.setConstraints(tf,c);
        p.add(tf);
        l = new JLabel();
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(l,c);
        p.add(l);
        l= new JLabel("Frequency Band");
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField(st.nextToken().trim());
        gridbag.setConstraints(tf,c);
        p.add(tf);
        l= new JLabel("Frequency");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField(st.nextToken().trim());
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(tf,c);
        p.add(tf);  
        l= new JLabel("Weather Condition");
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        p.add(l);
        JComboBox cb = new JComboBox();
        cb.addItem("Exceptional");
        cb.addItem("Excellent");
        cb.addItem("Good");
        cb.addItem("Average");
        cb.addItem("Below Ave.");
        cb.addItem("Poor");
        cb.addItem("Dismal");
        cb.addItem("Any");
        String weather = st.nextToken().trim();
        if(weather.equals("exceptional")) {
            cb.setSelectedItem("Exceptional");
        } else if(weather.equals("excellent")) { 
            cb.setSelectedItem("Excellent");
        } else if(weather.equals("good")) {
            cb.setSelectedItem("Good");
        } else if(weather.equals("average")) {
            cb.setSelectedItem("Average");
        } else if(weather.equals("belowAverage")) {
            cb.setSelectedItem("Below Ave.");
        } else if(weather.equals("poor")) {
            cb.setSelectedItem("Poor");
        } else if(weather.equals("dismal")) {
            cb.setSelectedItem("Dismal");
        } else if(weather.equals("any")) {
            cb.setSelectedItem("Any");
        }
        gridbag.setConstraints(cb,c);
        p.add(cb);
        l= new JLabel("# of Targets");
        c.gridwidth = GridBagConstraints.RELATIVE;
        gridbag.setConstraints(l,c);
        p.add(l);
        int t = Integer.parseInt( ((String)st.nextToken()).trim() );
        tf = new JTextField(""+t+"");
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(tf,c);
        p.add(tf);
        main.add(p, BorderLayout.NORTH);
        Vector setTargets = new Vector(t);
        for (int i =0 ; i<t; i++){
            //System.out.println(targets.elementAt(i));
            setTargets.add(targets.elementAt(i));
            //targets.removeElementAt(i);
        }
        main.add(updateTargetsField(t,setTargets));
        JScrollPane pane = new JScrollPane(main);
        return pane;
    }


    /**
     * @param targetSize The number of targets.
     * @param targets A vector containing all the targets specifics
     * @return JPanel
     */
    public JPanel updateTargetsField(int targetSize, Vector targets) {
        if(targetSize != targets.size()) {
            System.out.println("target sizes don't match!");
        }
        JPanel targetPanel = new JPanel( new GridLayout(targetSize, 1));
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        StringTokenizer token;
        // Do this for each target/line in vector.
        for(int i=0; i < targetSize; i++) {
            
            token = new StringTokenizer((String)targets.elementAt(i), ";");
            
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0; //c.weighty = 1.0;
            JPanel p = new JPanel(gridbag);
            JLabel l = new JLabel();
            //////////////////
            c.gridwidth = 1;
            l =new JLabel("-- Target Name");
            gridbag.setConstraints(l,c);
            p.add(l);
            JTextField tf = new JTextField((token.nextToken()).trim());
            gridbag.setConstraints(tf,c);
            p.add(tf);
            l = new JLabel("RA");
            gridbag.setConstraints(l,c);
            p.add(l);
            tf = new JTextField((token.nextToken()).trim());
            gridbag.setConstraints(tf,c);
            p.add(tf);
            l = new JLabel("DEC");
            gridbag.setConstraints(l,c);
            p.add(l);
            tf = new JTextField((token.nextToken()).trim()); 
            c.gridwidth = GridBagConstraints.REMAINDER; 
            gridbag.setConstraints(tf,c); 
            p.add(tf);
            ////////////////////////
            l = new JLabel();
            c.gridwidth = 1;
            gridbag.setConstraints(l,c);
            p.add(l);
            l = new JLabel();
            gridbag.setConstraints(l,c);
            p.add(l);
            l = new JLabel("Freq.");
            gridbag.setConstraints(l,c);
            p.add(l);
            tf = new JTextField((token.nextToken()).trim());
            gridbag.setConstraints(tf,c);
            p.add(tf);
            l = new JLabel("Total Time");
            gridbag.setConstraints(l,c);
            p.add(l);
            tf = new JTextField((token.nextToken()).trim());
            c.gridwidth = GridBagConstraints.REMAINDER;
            gridbag.setConstraints(tf,c);
            p.add(tf);
            ////////////////////////
            l = new JLabel();
            c.gridwidth = 1;
            gridbag.setConstraints(l,c);
            p.add(l);
            l = new JLabel();
            gridbag.setConstraints(l,c);
            p.add(l);
            l = new JLabel("Weather");
            gridbag.setConstraints(l,c);
            p.add(l);
            JComboBox cb = new JComboBox();
            cb.addItem("Exceptional");
            cb.addItem("Excellent");
            cb.addItem("Good");
            cb.addItem("Average");
            cb.addItem("Below Ave.");
            cb.addItem("Poor");
            cb.addItem("Dismal");
            cb.addItem("Any");
            cb.setSelectedItem(getWeatherCondition((token.nextToken()).trim()));
            gridbag.setConstraints(cb,c);
            p.add(cb);
            l = new JLabel("Repeat Count");
            gridbag.setConstraints(l,c);
            p.add(l);
            try {
                tf = new JTextField((token.nextToken()).trim());
            } catch(Exception e) {
                tf = new JTextField("");
            }
            c.gridwidth = GridBagConstraints.REMAINDER;
            gridbag.setConstraints(tf,c);
            p.add(tf);
            targetPanel.add(p);
        }
        return targetPanel;
    }

    /** 
     * I don't know why I have this function...
     * @param s The string that has lowercase info about the weather condition
     * @return String The weather condition starting with a capatial letter..
     */
    public String getWeatherCondition(String s) {

        if(s.equals("exceptional") ){
            return "Exceptional";
        } else if(s.equals("excellent")) {
            return "Excellent";
        } else if(s.equals("good")) {
            return "Good";
        } else if(s.equals("average")) {
            return "Average";
        } else if(s.equals("below average")) {
            return "Below Ave.";
        } else if(s.equals("poor")) {
            return "Poor";
        } else if(s.equals("dismal")) {
            return "Dismal";
        } else if(s.equals("any")) {
            return "Any";   
        } else {
            return "Any";   
        }
    }
    
}
