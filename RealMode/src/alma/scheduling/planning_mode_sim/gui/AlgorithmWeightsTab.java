package alma.scheduling.planning_mode_sim.gui;

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


public class AlgorithmWeightsTab extends JScrollPane {

    private Vector v;
    private JTextField posElev, posMax, weather, spsb, spdb, dpsb, dpdb,
                       newProj, lastSB, pri;

    public AlgorithmWeightsTab(){
        super();
        setViewportView(createView());
    }

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

    public String getPositionElevationWeight() {
        return posElev.getText();
    }
    public String getPositionMaxWeight() {
        return posMax.getText();
    }
    public String getWeatherWeight() {
        return weather.getText();
    }
    public String getSPSBWeight() {
        return spsb.getText();
    }
    public String getSPDBWeight() {
        return spdb.getText();
    }
    public String getDPSBWeight() {
        return dpsb.getText();
    }
    public String getDPDBWeight() {
        return dpdb.getText();
    }
    public String getNewProjectWeight() {
        return newProj.getText();
    }
    public String getLastSBWeight(){
        return lastSB.getText();
    }
    public String getPriorityWeight() {
        return pri.getText();
    }

    
    ////////////////////////////////////////////////////
    // Set Methods
    ////////////////////////////////////////////////////


    public void setPositionElevationWeight(String s){ 
        posElev.setText(s);
    }
    public void setPositionMaxWeight(String s) {
        posMax.setText(s);
    }
    public void setWeatherWeight(String s) {
        weather.setText(s);
    }
    public void setSPSBWeight(String s) {
        spsb.setText(s);
    }
    public void setSPDBWeight(String s) {
        spdb.setText(s);
    }
    public void setDPSBWeight(String s) {
        dpsb.setText(s);
    }
    public void setDPDBWeight(String s) {
        dpdb.setText(s);
    }
    public void setNewProjectWeight(String s) {
        newProj.setText(s);
    }
    public void setLastSBWeight(String s) {
        lastSB.setText(s);
    }
    
    public void setPriorityWeight(String s) {
        pri.setText(s);
    }

    ////////////////////////////////////////////////////
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

