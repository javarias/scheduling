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
 * File SimulationPropertiestTab.java
 */
package alma.Scheduling.GUI.PlanningModeSimGUI;

import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.util.Vector;

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JComboBox;

/** 
 * Extends JScrollPane to contain all the information required
 * to specify all the main information of the planning mode simulation.
 *
 *@author Sohaila Roberts
 */
public class SimulationPropertiesTab extends JScrollPane {
    private Vector v;    

    private JTextField beginTime, endTime, freqBandSetup, projSetup,
                       advanceClockBy, longitude, latitude, altitude,
                       minElevAngle;

    private JComboBox loglevel, freq_cb, proj_cb, clock_cb;

    private GUITimezones timezone;
    private GUIAntennas antennas;

    public SimulationPropertiesTab() {
        super(); 
        setViewportView(createView());
    }

    /** 
     * Creates the initial view for this pane.
     * @return JPanel
     */
    private JPanel createView() {
        JPanel p = new JPanel();
        JPanel gridPanel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        p.setLayout(new GridLayout(8,1));
        JLabel label;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0; c. weighty = 1.0;
        ///////////////////
        gridPanel.setLayout(gridbag);
        label = new JLabel("Simulation Properties");
        gridbag.setConstraints(label, c);
        gridPanel.add(label);
        p.add(gridPanel);
        ///////////////////
        gridPanel = new JPanel();
        gridPanel.setLayout(gridbag);
        label = new JLabel("Begin Time ");
        c.gridwidth = 1;
        gridbag.setConstraints(label,c);
        gridPanel.add(label);
        beginTime = new JTextField("YYYY-MM-DDTHH:MM:SS");
        gridbag.setConstraints(beginTime,c);
        gridPanel.add(beginTime);
        label = new JLabel("");
        gridbag.setConstraints(label,c);
        gridPanel.add(label);
        label = new JLabel("Log Level");
        gridPanel.add(label);
        gridbag.setConstraints(label, c);
        loglevel = new JComboBox();
        loglevel.addItem("CONFIG"); loglevel.addItem("FINE");
        loglevel.addItem("FINER"); loglevel.addItem("FINEST");
        gridbag.setConstraints(loglevel, c);
        gridPanel.add(loglevel);
        label = new JLabel("");
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(label,c);
        gridPanel.add(label);
        label = new JLabel("End Time ");
        c.gridwidth = 1;
        gridbag.setConstraints(label,c);
        gridPanel.add(label);
        endTime = new JTextField("YYYY-MM-DDTHH:MM:SS");
        gridbag.setConstraints(endTime,c);
        gridPanel.add(endTime);
        label = new JLabel("");
        gridbag.setConstraints(label,c);
        gridPanel.add(label);
        label = new JLabel("");
        gridbag.setConstraints(label,c);
        gridPanel.add(label);
        label = new JLabel("");
        gridbag.setConstraints(label,c);
        gridPanel.add(label);
        label = new JLabel("");
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(label,c);
        gridPanel.add(label);
        p.add(gridPanel);
        ///////////////////
        p.add(new JLabel());
        ///////////////////
        gridPanel = new JPanel();
        gridPanel.setLayout(gridbag);
        label = new JLabel("Setup Times");
        gridbag.setConstraints(label, c);
        gridPanel.add(label);
        c.gridwidth = 1;
        label = new JLabel("Frequency Band");
        gridbag.setConstraints(label,c);
        gridPanel.add(label);
        freqBandSetup = new JTextField("");
        gridbag.setConstraints(freqBandSetup,c);
        gridPanel.add(freqBandSetup);
        freq_cb = new JComboBox();
        freq_cb.addItem("sec"); freq_cb.addItem("min"); freq_cb.addItem("hour");
        gridbag.setConstraints(freq_cb, c);
        gridPanel.add(freq_cb);
        label = new JLabel("");
        gridbag.setConstraints(label, c);
        gridPanel.add(label);
        label = new JLabel("New Project");
        gridbag.setConstraints(label,c);
        gridPanel.add(label);
        projSetup = new JTextField("");
        gridbag.setConstraints(projSetup,c);
        gridPanel.add(projSetup);
        proj_cb = new JComboBox();
        proj_cb.addItem("sec"); proj_cb.addItem("min"); proj_cb.addItem("hour");
        gridbag.setConstraints(proj_cb, c);
        gridPanel.add(proj_cb);
        c.gridwidth = GridBagConstraints.REMAINDER;
        label = new JLabel("");
        gridbag.setConstraints(label, c);
        gridPanel.add(label);
        c.gridwidth = 1;
        label = new JLabel("Advance Clock by");
        gridbag.setConstraints(label, c);
        gridPanel.add(label);
        advanceClockBy = new JTextField("");
        gridbag.setConstraints(advanceClockBy,c);
        gridPanel.add(advanceClockBy);
        clock_cb = new JComboBox();
        clock_cb.addItem("sec"); clock_cb.addItem("min"); clock_cb.addItem("hour");
        gridbag.setConstraints(clock_cb, c);
        gridPanel.add(clock_cb);
        c.gridwidth = GridBagConstraints.REMAINDER;
        label = new JLabel("");
        gridbag.setConstraints(label, c);
        gridPanel.add(label);

        p.add(gridPanel);
        ///////////////////
        gridPanel = new JPanel();
        gridPanel.setLayout(gridbag);
        c.gridwidth = GridBagConstraints.REMAINDER;
        label = new JLabel("Site Characteristics");
        gridbag.setConstraints(label, c);
        gridPanel.add(label);
        p.add(gridPanel);
        ///////////////////
        gridPanel = new JPanel();
        gridPanel.setLayout(gridbag);
        c.gridwidth =1;
        label = new JLabel("Longitude");
        gridbag.setConstraints(label, c);
        gridPanel.add(label);
        longitude = new JTextField("107.6177275");
        gridbag.setConstraints(longitude,c);
        gridPanel.add(longitude);
        label = new JLabel("");
        gridbag.setConstraints(label, c);
        gridPanel.add(label);
        label = new JLabel("Latitude");
        gridbag.setConstraints(label, c);
        gridPanel.add(label);
        latitude = new JTextField("34.0787491666667");
        gridbag.setConstraints(latitude,c);
        gridPanel.add(latitude);
        label = new JLabel("");
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(label, c);
        gridPanel.add(label);
        c.gridwidth = 1;
        label = new JLabel("TimeZone");
        gridbag.setConstraints(label,c);
        gridPanel.add(label);
        timezone = new GUITimezones();
        gridbag.setConstraints(timezone,c);
        gridPanel.add(timezone);
        label = new JLabel("");
        gridbag.setConstraints(label, c);
        gridPanel.add(label);
        label= new JLabel("Altitude (meters)");
        gridbag.setConstraints(label, c);
        gridPanel.add(label);
        altitude = new JTextField("2124.0");
        gridbag.setConstraints(altitude, c);
        gridPanel.add(altitude);
        label = new JLabel("");
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(label, c);
        gridPanel.add(label);

        p.add(gridPanel);
        ///////////////////
        gridPanel = new JPanel();
        gridPanel.setLayout(gridbag);
        label = new JLabel("Min. Elevation Angle");
        c.gridwidth = 1;
        gridbag.setConstraints(label,c);
        gridPanel.add(label);
        minElevAngle = new JTextField("8.0");
        gridbag.setConstraints(minElevAngle,c);
        gridPanel.add(minElevAngle);
        label = new JLabel("");
        gridbag.setConstraints(label,c);
        gridPanel.add(label);
        label = new JLabel("Total Antennas");
        gridbag.setConstraints(label,c);
        gridPanel.add(label);
        antennas = new GUIAntennas();
        gridbag.setConstraints(antennas,c);
        gridPanel.add(antennas);
        c.gridwidth = GridBagConstraints.REMAINDER;
        label = new JLabel("");
        gridbag.setConstraints(label,c);
        gridPanel.add(label);

        p.add(gridPanel);
        ///////////////////
        return p;
    }

    //////////////////////////////////////////////////
    //  Get Methods
    //////////////////////////////////////////////////

    /**
     * Gets the begin time of this simulation
     * @return String The string representation of the simulation start time
     */
    public String getBeginTime() { 
        return beginTime.getText();
    }
    /**
     * Gets the end time of this simulation
     * @return String The string representation of the simulation end time
     */
    public String getEndTime() { 
        return endTime.getText();
    }

    /**
     * Gets the frequency band.
     * @return String The string representation of the frequency band.
     */
    public String getFreqBandSetup() { 
        return freqBandSetup.getText();
    }
    /**
     * Gets the Project setup time
     * @return String The string representation of the Project setup time
     */
    public String getProjSetup() { 
        return projSetup.getText();
    }
    /**
     * Gets the amount of time to advance the clock
     * @return String The string representation of the amount of time to advance the clock
     */
    public String getClockAdvance() {
        return advanceClockBy.getText();
    }
    /**
     * Gets the longitude
     * @return String The string representation of the longitude
     */
    public String getLongitude() { 
        return longitude.getText();
    }
    /**
     * Gets the latitude
     * @return String The string representation of the latitude
     */
    public String getLatitude() { 
        return latitude.getText();
    }
    /**
     * Gets the altitude
     * @return String The string representation of the altitude
     */
    public String getAltitude() { 
        return altitude.getText();
    }
    /**
     * Gets the minimum elevation angle
     * @return String The string representation of the minimum elevation angle
     */
    public String getMinElevationAngle() { 
        return minElevAngle.getText();
    }

    /**
     * Gets the log level
     * @return String The string representation of the log level
     */
    public String getLoglevel() {
        return (String)loglevel.getSelectedItem();
    }
    /**
     * Gets the frequency band setup units
     * @return String The string representation of the  frequency band setup units
     */
    public String getFrequencySetupUnits() {
        return (String)freq_cb.getSelectedItem();
    }
    /**
     * Gets the project setup time units
     * @return String The string representation of the project setup time units
     */
    public String getProjectSetupUnits() {
        return (String)proj_cb.getSelectedItem();
    }
    /**
     * Gets the clock advance time units
     * @return String The string representation of the clock advance time units
     */
    public String getClockAdvanceUnits() {
        return (String)clock_cb.getSelectedItem();
    }

    /**
     * Gets the timezone
     * @return String The string representation of the timezone
     */
    public String getTimezone() {
        return timezone.getValue();
    }

    /**
     * Gets the number of antennas
     * @return String The string representation of the number of antennas
     */
    public String getAntennas() {
        return antennas.getValue();
    }
    

    //////////////////////////////////////////////////
    //  Set Methods
    //////////////////////////////////////////////////
    
    /** 
     * Sets the begin time of the simulation.
     * @param s The string representation of the begin time
     *          It must be in the following format.
     *          xxxx-xx-xxTxx:xx:xx where x = numbers
     */
    public void setBeginTime(String s) { 
        if(s.charAt(4) != '-') {
            System.out.println("ERROR at 4");
            System.out.println("Wrong Format: xxxx-xx-xxTxx:xx:xx");
        } else if(s.charAt(7) != '-') {
            System.out.println("ERROR at 7");
            System.out.println("Wrong Format: xxxx-xx-xxTxx:xx:xx");
        } else if(s.charAt(10) != 'T') {
            System.out.println("ERROR at 10");
            System.out.println("Wrong Format: xxxx-xx-xxTxx:xx:xx");
        } else if(s.charAt(13) != ':') {
            System.out.println("ERROR at 13");
            System.out.println("Wrong Format: xxxx-xx-xxTxx:xx:xx");
        } else if(s.charAt(16) != ':') {
            System.out.println("ERROR at 16");
            System.out.println("Wrong Format: xxxx-xx-xxTxx:xx:xx");
        } else if(s.length() != 19) {
            System.out.println("ERROR: wrong size ");
            System.out.println("xxxx-xx-xxTxx:xx:xx");
        } else {
           beginTime.setText(s);
        }
    }
    /** 
     * Sets the end time of the simulation.
     * @param s The string representation of the end time
     *          It must be in the following format.
     *          xxxx-xx-xxTxx:xx:xx where x = numbers
     */
    public void setEndTime(String s) { 
        if(s.charAt(4) != '-') {
            System.out.println("ERROR at 4");
            System.out.println("Wrong Format: xxxx-xx-xxTxx:xx:xx");
        } else if(s.charAt(7) != '-') {
            System.out.println("ERROR at 7");
            System.out.println("Wrong Format: xxxx-xx-xxTxx:xx:xx");
        } else if(s.charAt(10) != 'T') {
            System.out.println("ERROR at 10");
            System.out.println("Wrong Format: xxxx-xx-xxTxx:xx:xx");
        } else if(s.charAt(13) != ':') {
            System.out.println("ERROR at 13");
            System.out.println("Wrong Format: xxxx-xx-xxTxx:xx:xx");
        } else if(s.charAt(16) != ':') {
            System.out.println("ERROR at 16");
            System.out.println("Wrong Format: xxxx-xx-xxTxx:xx:xx");
        } else if(s.length() != 19) {
            System.out.println("ERROR: wrong size ");
            System.out.println("xxxx-xx-xxTxx:xx:xx");
        } else {
           beginTime.setText(s);
        }
    }
    /**
     * Sets the frequency band setup time. Its units will always be in seconds.
     * @param s The string representation of the frequency band setup time.
     */
    public void setFreqBandSetup(String s) { 
        freqBandSetup.setText(s);
        setFrequencySetupUnits("sec");
    }
    /**
     * Sets the project setup time. Its units will always be in seconds.
     * @param s The string representation of the project setup time.
     */
    public void setProjSetup(String s) { 
        projSetup.setText(s);
        setProjectSetupUnits("sec");
    }
    /**
     * Sets the clock advance time. Its units will always be in seconds.
     * @param s The string representation of the clock advance time.
     */
    public void setClockAdvance(String s) {
        advanceClockBy.setText(s);
        setClockAdvanceUnits("sec");
    }
    /**
     * Sets the longitude
     * @param s The string representation of the longitude
     */
    public void setLongitude(String s) { 
        longitude.setText(s);
    }
    /**
     * Sets the latitude
     * @param s The string representation of the latitude
     */
    public void setLatitude(String s) { 
        latitude.setText(s);
    }
    /**
     * Sets the Altitude
     * @param s The string representation of the Altitude
     */
    public void setAltitude(String s) { 
        altitude.setText(s);
    }
    /**
     * Sets the minimum elevation angle
     * @param s The string representation of the minimum elevation angle
     */
    public void setMinElevationAngle(String s) { 
        minElevAngle.setText(s);
    }

    /**
     * Sets the log level
     * @param s The string representation of the log level
     */
    public void setLoglevel(String s) {
        if(s.equals("CONFIG")) {
           loglevel.setSelectedItem("CONFIG");
        } else if(s.equals("FINE")) {
           loglevel.setSelectedItem("FINE");
        } else if(s.equals("FINER")) {
           loglevel.setSelectedItem("FINER");
        } else if(s.equals("FINEST")) {
           loglevel.setSelectedItem("FINEST");
        } else {
            System.out.println("Log level must be either:");
            System.out.println("\tCONFIG");
            System.out.println("\tFINE");
            System.out.println("\tFINER");
            System.out.println("\tFINEST");
        }
        
    }
    /**
     * Sets the frequency band units
     * @param s The string representation of the frequency band units
     */
    public void setFrequencySetupUnits(String s) {
        if(s.equals("sec")){
            freq_cb.setSelectedItem("sec");
        } else if(s.equals("min")){
            freq_cb.setSelectedItem("min");
        } else if(s.equals("hour")){
            freq_cb.setSelectedItem("hour");
        } else {
            System.out.println("Frequency setup units must be either:");
            System.out.println("\tsec");
            System.out.println("\tmin");
            System.out.println("\thour");
        }
    }
    /**
     * Sets the project setup units
     * @param s The string representation of the project setup units
     */
    public void setProjectSetupUnits(String s) {
        if(s.equals("sec")){
            proj_cb.setSelectedItem("sec");
        } else if(s.equals("min")){
            proj_cb.setSelectedItem("min");
        } else if(s.equals("hour")){
            proj_cb.setSelectedItem("hour");
        } else {
            System.out.println("Frequency setup units must be either:");
            System.out.println("\tsec");
            System.out.println("\tmin");
            System.out.println("\thour");
        }
    }
    /**
     * Sets the clock advance units
     * @param s The string representation of the clock advance units
     */
    public void setClockAdvanceUnits(String s) {
        if(s.equals("sec")){
            clock_cb.setSelectedItem("sec");
        } else if(s.equals("min")){
            clock_cb.setSelectedItem("min");
        } else if(s.equals("hour")){
            clock_cb.setSelectedItem("hour");
        } else {
            System.out.println("Units for advancing the clock must be either:");
            System.out.println("\tsec");
            System.out.println("\tmin");
            System.out.println("\thour");
        }
    }


    /**
     * Sets the timezone
     * @param s The string representation of the timezone
     */
    public void setTimezone(String s) {
        timezone.setValue(s);
    }

    /**
     * Sets the number of antennas
     * @param s The string representation of the number of antennas
     */
    public void setAntennas(String s) {
        antennas.setValue(s);
    }

    /**
     * Gets the values from the vector and creates the tab with all the 
     * simulation's properties
     * @param Vector All the properties in this vector.
     */
    public void loadValuesFromFile(Vector values) {
        v = values;
        setBeginTime((String)v.elementAt(0));
        setEndTime((String)v.elementAt(1));
        setLoglevel((String)v.elementAt(2));
        setFreqBandSetup((String)v.elementAt(3));
        setProjSetup((String)v.elementAt(4));
        setClockAdvance((String)v.elementAt(5));
        setLongitude((String)v.elementAt(6));
        setLatitude((String)v.elementAt(7));
        setTimezone((String)v.elementAt(8));
        setAltitude((String)v.elementAt(9));
        setMinElevationAngle((String)v.elementAt(10));
        setAntennas((String)v.elementAt(11));
    
    }
}
