
package alma.scheduling.planning_mode_sim.gui;

import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JComboBox;

public class SimulationPropertiesTab extends JScrollPane {
    
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

    public String getBeginTime() { 
        return beginTime.getText();
    }
    public String getEndTime() { 
        return endTime.getText();
    }
    public String getFreqBandSetup() { 
        return freqBandSetup.getText();
    }
    public String getProjSetup() { 
        return projSetup.getText();
    }
    public String getClockAdvance() {
        return advanceClockBy.getText();
    }
    public String getLongitude() { 
        return longitude.getText();
    }
    public String getLatitude() { 
        return latitude.getText();
    }
    public String getAltitude() { 
        return altitude.getText();
    }
    public String getMinElevationAngle() { 
        return minElevAngle.getText();
    }

    public String getLoglevel() {
        return (String)loglevel.getSelectedItem();
    }
    public String getFrequencySetupUnits() {
        return (String)freq_cb.getSelectedItem();
    }
    public String getProjectSetupUnits() {
        return (String)proj_cb.getSelectedItem();
    }
    public String getClockAdvanceUnits() {
        return (String)clock_cb.getSelectedItem();
    }

    public String getTimezone() {
        return timezone.getValue();
    }

    public String getAntennas() {
        return antennas.getValue();
    }
    

    //////////////////////////////////////////////////
    //  Set Methods
    //////////////////////////////////////////////////
    
    public void setBeginTime(String s) { 
        beginTime.setText(s);
    }
    public void setEndTime(String s) { 
        endTime.setText(s);
    }
    public void setFreqBandSetup(String s) { 
        freqBandSetup.setText(s);
        setFrequencySetupUnits("sec");
    }
    public void setProjSetup(String s) { 
        projSetup.setText(s);
        setProjectSetupUnits("sec");
    }
    public void setClockAdvance(String s) {
        advanceClockBy.setText(s);
        setClockAdvanceUnits("sec");
    }
    public void setLongitude(String s) { 
        longitude.setText(s);
    }
    public void setLatitude(String s) { 
        latitude.setText(s);
    }
    public void setAltitude(String s) { 
        altitude.setText(s);
    }
    public void setMinElevationAngle(String s) { 
        minElevAngle.setText(s);
    }

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


    public void setTimezone(String s) {
        timezone.setValue(s);
    }

    public void setAntennas(String s) {
        antennas.setValue(s);
    }
}
