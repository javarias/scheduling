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
 * File AlgorithmWeightsTab.java
 */

package alma.Scheduling.GUI.PlanningModeSimGUI;

import java.util.Vector;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * This class creates a JScrollPane which contains all the required gui fields to
 * display and collect information about the weights given to the Dynamic scheduling
 * algorithm.
 *
 * @author Sohaila Roberts
 */
public class AlgorithmWeightsTab extends JScrollPane {

    private Vector v;
    private JTextField posElev, posMax, weather, spsb, spdb, dpsb, dpdb,
                       newProj, lastSB, pri;

    public AlgorithmWeightsTab(){
        super();
        setViewportView(createView());
    }

    /**
     * Creates the main view.
     *
     * @return JPanel
     */
    private JPanel createView() {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0; c. weighty = 1.0;
        JPanel main = new JPanel(gridbag);
        ///////////////////
        JLabel l = new JLabel("Dynamic Algorithm Weights");
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(l, c);
        main.add(l);
        c.gridwidth = 1;
        l = new JLabel();
        gridbag.setConstraints(l,c);
        main.add(l);
        l = new JLabel("Position Elevation");
        gridbag.setConstraints(l,c);
        main.add(l);
        posElev = new JTextField("");
        gridbag.setConstraints(posElev,c);
        main.add(posElev);
        l = new JLabel();
        c.gridwidth= GridBagConstraints.REMAINDER;
        gridbag.setConstraints(l,c);
        main.add(l);
        l = new JLabel();
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        main.add(l);
        l = new JLabel("Position Maximum");
        gridbag.setConstraints(l,c);
        main.add(l);
        posMax = new JTextField("");
        gridbag.setConstraints(posMax,c);
        main.add(posMax);
        l = new JLabel();
        c.gridwidth= GridBagConstraints.REMAINDER;
        gridbag.setConstraints(l,c);
        main.add(l);
        l = new JLabel();
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        main.add(l);
        l = new JLabel("Weather");
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        main.add(l);
        weather = new JTextField("");
        gridbag.setConstraints(weather,c);
        main.add(weather);
        l = new JLabel();
        c.gridwidth= GridBagConstraints.REMAINDER;
        gridbag.setConstraints(l,c);
        main.add(l);
        l = new JLabel();
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        main.add(l);
        l = new JLabel("Same Proj. Same Band");
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        main.add(l);
        spsb = new JTextField("");
        gridbag.setConstraints(spsb,c);
        main.add(spsb);
        l = new JLabel();
        c.gridwidth= GridBagConstraints.REMAINDER;
        gridbag.setConstraints(l,c);
        main.add(l);
        l = new JLabel();
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        main.add(l);
        l = new JLabel("Same Proj. Diff. Band");
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        main.add(l);
        spdb = new JTextField("");
        gridbag.setConstraints(spdb,c);
        main.add(spdb);
        l = new JLabel();
        c.gridwidth= GridBagConstraints.REMAINDER;
        gridbag.setConstraints(l,c);
        main.add(l);
        ///
        l = new JLabel();
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        main.add(l);
        l = new JLabel("Diff. Proj. Same. Band");
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        main.add(l);
        dpsb = new JTextField("");
        gridbag.setConstraints(dpsb,c);
        main.add(dpsb);
        l = new JLabel();
        c.gridwidth= GridBagConstraints.REMAINDER;
        gridbag.setConstraints(l,c);
        main.add(l);
        l = new JLabel();
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        main.add(l);
        l = new JLabel("Diff. Proj. Diff. Band");
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        main.add(l);
        dpdb = new JTextField("");
        gridbag.setConstraints(dpdb,c);
        main.add(dpdb);
        l = new JLabel();
        c.gridwidth= GridBagConstraints.REMAINDER;
        gridbag.setConstraints(l,c);
        main.add(l);
        l = new JLabel();
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        main.add(l);
        ///
        l = new JLabel("New Project");
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        main.add(l);
        newProj = new JTextField("");
        gridbag.setConstraints(newProj,c);
        main.add(newProj);
        l = new JLabel();
        c.gridwidth= GridBagConstraints.REMAINDER;
        gridbag.setConstraints(l,c);
        main.add(l);
        l = new JLabel();
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        main.add(l);
        l = new JLabel("Last SB");
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        main.add(l);
        lastSB = new JTextField("");
        gridbag.setConstraints(lastSB,c);
        main.add(lastSB);
        l = new JLabel();
        c.gridwidth= GridBagConstraints.REMAINDER;
        gridbag.setConstraints(l,c);
        main.add(l);
        l = new JLabel();
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        main.add(l);
        l = new JLabel("Priority");
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        main.add(l);
        pri = new JTextField("");
        gridbag.setConstraints(pri,c);
        main.add(pri);
        l = new JLabel();
        c.gridwidth= GridBagConstraints.REMAINDER;
        gridbag.setConstraints(l,c);
        main.add(l);
        ///////////////////
        return main;
    }
    

    ////////////////////////////////////////////////////
    // Get Methods
    ////////////////////////////////////////////////////

    /**
     *  Get the weight for the position elevation
     * @return String The string representation of the weight for the position elevation
     */
    public String getPositionElevationWeight() {
        return posElev.getText();
    }
    /**
     * Get the weight for the maximum position
     * @return String the string representation for the weight for the maximum position
     */
    public String getPositionMaxWeight() {
        return posMax.getText();
    }
    /**
     * Get the weight for the weather.
     * @return String The string representation for the weather weight
     */
    public String getWeatherWeight() {
        return weather.getText();
    }
    /**
     * Get the weight for the Same Project / Same Band 
     * @return String The string representation for the same project / same band weight
     */
    public String getSPSBWeight() {
        return spsb.getText();
    }
    /**
     * Get the weight for the Same Project / different Band 
     * @return String The string representation for the same project / different band weight
     */
    public String getSPDBWeight() {
        return spdb.getText();
    }
    /**
     * Get the weight for the different Project / Same Band 
     * @return String The string representation for the different project / same band weight
     */
    public String getDPSBWeight() {
        return dpsb.getText();
    }
    /**
     * Get the weight for the different Project / different Band 
     * @return String The string representation for the different project / different band weight
     */
    public String getDPDBWeight() {
        return dpdb.getText();
    }
    /**
     * Get the weight for a new project
     * @return String The string representation of the weight for a  new project
     */
    public String getNewProjectWeight() {
        return newProj.getText();
    }
    /**
     * Get the weight for if its the last SB in a project
     * @return String The string representation of the weight for if its the last SB in the project
     */
    public String getLastSBWeight(){
        return lastSB.getText();
    }
    /**
     * Get the weight for priority.
     * @return String The string representation of the weight put on priority
     */
    public String getPriorityWeight() {
        return pri.getText();
    }

    
    ////////////////////////////////////////////////////
    // Set Methods
    ////////////////////////////////////////////////////


    /**
     * Set the weight for position elevation
     */
    public void setPositionElevationWeight(String s){ 
        posElev.setText(s);
    }
    /**
     * Set the weight for maximum position
     */
    public void setPositionMaxWeight(String s) {
        posMax.setText(s);
    }
    /**
     * Set the weight for weather
     */
    public void setWeatherWeight(String s) {
        weather.setText(s);
    }
    /**
     * Set the weight for same project same band
     */
    public void setSPSBWeight(String s) {
        spsb.setText(s);
    }
    /**
     * Set the weight for same project different band
     */
    public void setSPDBWeight(String s) {
        spdb.setText(s);
    }
    /**
     * Set the weight for different project same band
     */
    public void setDPSBWeight(String s) {
        dpsb.setText(s);
    }
    /**
     * Set the weight for different project different band
     */
    public void setDPDBWeight(String s) {
        dpdb.setText(s);
    }
    /**
     * Set the weight for it being a new project
     */
    public void setNewProjectWeight(String s) {
        newProj.setText(s);
    }
    /**
     * Set the weight for it being the last SB in the project
     */
    public void setLastSBWeight(String s) {
        lastSB.setText(s);
    }
    
    /**
     * Set the weight for its priority
     */
    public void setPriorityWeight(String s) {
        pri.setText(s);
    }

    ////////////////////////////////////////////////////
    /**
     *  Get the values in the form of a vector from the controller
     * to load them into the fields and display them.
     * @param values A vector containing all the values specific to this tab
     */
    public void loadValuesFromFile(Vector values) {
        v = values;
        setPositionElevationWeight((String)v.elementAt(0));
        setPositionMaxWeight((String)v.elementAt(1));
        setWeatherWeight((String)v.elementAt(2));
        setSPSBWeight((String)v.elementAt(3));
        setSPDBWeight((String)v.elementAt(4));
        setDPSBWeight((String)v.elementAt(5));
        setDPDBWeight((String)v.elementAt(6));
        setNewProjectWeight((String)v.elementAt(7));
        setLastSBWeight((String)v.elementAt(8));
        setPriorityWeight((String)v.elementAt(9));
    }
    
}

