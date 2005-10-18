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
 * File PlanningModeSimGUIController.java
 */

package alma.scheduling.GUI.PlanningModeSimGUI;

import java.io.FileNotFoundException;
import java.net.URL;
import java.awt.Component;
import java.lang.Integer;
import java.util.Vector;
import java.util.Properties;
import java.util.StringTokenizer;
import java.io.File;
import java.io.PrintStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JLayeredPane;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import alma.scheduling.PlanningModeSim.Tag;
import alma.scheduling.PlanningModeSim.Simulator;

/**
 * This class has all the functionality that is required by the PlanningModeSimGUI. It
 * is the class that interacts with the rest of the planning mode simulation
 * classes.
 */
public class PlanningModeSimGUIController implements Runnable {
    private PlanningModeSimGUI gui;
    //Tab objects
    private ProjectsTab             projTab;
    private WeatherDataTab          weatherTab;
    private FrequencyBandTab        frequencyTab;
    private AlgorithmWeightsTab     algorithmTab;
    private AntennaTab           antennaTab;
    private SimulationPropertiesTab simPropTab;
    
    //Stuff gotten from first tab!
    private String startTime, endTime, logLevel, freqSetupTime, projSetupTime,
                    advanceClock, latitude, longitude, timezone, altitude,
                    minAngle, totalAntennas;
    //Stuff gotten from second tab!
    private int totalfreqbands;
    private Vector freqbands;
    //Stuff gotten from third tab!
    private int totalweatherfunc;
    private Vector weatherfuncvalues;
    //Stuff gotten from forth tab!
    private String posElevation_w, posMax_w, weather_w, spsb_w, spdb_w, dpdb_w,
                    dpsb_w, newproj_w, lastsb_w, priority_w;
    //Stuff gotten from fifth tab!
    private int totalprojects;
    private Vector projects;

    private Simulator simulator;
    private String data_filename;

    public PlanningModeSimGUIController() {
        weatherfuncvalues = new Vector();
        freqbands = new Vector();
    }
    
    public void run() {
        this.gui = new PlanningModeSimGUI(this);
        getTabs();
    }
    
    protected URL getImage(String name) {
        return this.getClass().getClassLoader().getResource(
            "alma/scheduling/Image/"+name);
    }

    private void getTabs() {
        Component[] comp1 = gui.getComponents();
        JRootPane rp = (JRootPane)comp1[0];
        Component[] comp2 = rp.getComponents();
        JLayeredPane lp = (JLayeredPane)comp2[1];
        Component[] comp3 = lp.getComponents();
        JPanel p = (JPanel)comp3[0];
        Component[] comp4 = p.getComponents();
        JTabbedPane tp = (JTabbedPane)comp4[1];
        Component[] comp5 = tp.getComponents();
        simPropTab = (SimulationPropertiesTab)comp5[0];
        antennaTab = (AntennaTab)comp5[1];
        frequencyTab = (FrequencyBandTab)comp5[2];
        weatherTab = (WeatherDataTab)comp5[3];
        algorithmTab = (AlgorithmWeightsTab)comp5[4];
        projTab = (ProjectsTab)comp5[5];
    }

    protected void saveToFile(String filename) {
        //Simulation Properties tab
        stuffFromSimulationPropertiesTab();
        
        //Frequency band tab
        stuffFromFrequencyTab();
       
        //Weather model tab
        stuffFromWeatherTab();
       
        //Weights tab
        stuffFromAlgorithmWeightsTab();
              
        //Projects tab
        stuffFromProjectsTab();
       
        //save all gathered info to a file
        data_filename = filename;

    }

    private void stuffFromSimulationPropertiesTab(){
        startTime = simPropTab.getBeginTime();
        endTime = simPropTab.getEndTime();
        logLevel = simPropTab.getLoglevel();
        freqSetupTime = simPropTab.getFreqBandSetup();
        projSetupTime = simPropTab.getProjSetup();
        advanceClock = simPropTab.getClockAdvance();
        longitude = simPropTab.getLongitude();
        latitude = simPropTab.getLatitude();
        timezone = simPropTab.getTimezone();
        altitude = simPropTab.getAltitude();
        minAngle = simPropTab.getMinElevationAngle();
        totalAntennas = simPropTab.getAntennas();
    }

    private void stuffFromFrequencyTab() {
        freqbands = frequencyTab.getFrequencyValues();
        totalfreqbands = freqbands.size();
    }

    private void stuffFromWeatherTab() {
        totalweatherfunc = weatherTab.getTotalWeatherFuncs();
        //for (int i=0; i < totalweatherfunc; i++) {
        //### This might need some debugging when more weather funcs are added 
            weatherfuncvalues.add( weatherTab.getWeatherName() ); //name 
            weatherfuncvalues.add( weatherTab.getUnits() ); //units 
            weatherfuncvalues.add( weatherTab.getP0() ); //p0 
            weatherfuncvalues.add( weatherTab.getP1() ); //p1 
            weatherfuncvalues.add( weatherTab.getP2() ); //p2 
            weatherfuncvalues.add( weatherTab.getS0() ); //s0 
            weatherfuncvalues.add( weatherTab.getS1() ); //s1 
            weatherfuncvalues.add( weatherTab.getT0() ); //t0 
            weatherfuncvalues.add( weatherTab.getT1() ); //t1 
            //i += 35;
        //}
        
    }

    private void stuffFromAlgorithmWeightsTab() {
        posElevation_w = algorithmTab.getPositionElevationWeight();
        posMax_w = algorithmTab.getPositionMaxWeight();
        weather_w = algorithmTab.getWeatherWeight();
        spsb_w = algorithmTab.getSPSBWeight();
        spdb_w = algorithmTab.getSPDBWeight();
        dpsb_w = algorithmTab.getDPSBWeight();
        dpdb_w =algorithmTab.getDPDBWeight();
        newproj_w = algorithmTab.getNewProjectWeight();
        lastsb_w = algorithmTab.getLastSBWeight();
        priority_w = algorithmTab.getPriorityWeight();
    }
    
    private void stuffFromProjectsTab() {
        projects = projTab.getProjects();
        totalprojects = projTab.getTotalProjectCount();
    }


    private void createFile(String fileName) {
        try {
            File f = new File(fileName);
            PrintStream out = new PrintStream(new FileOutputStream(f));
            out.println("Simulation.beginTime = " + startTime);
            out.println("Simulation.endTime = " + endTime);
            //this will never change for this particular gui
            out.println("Simulation.mode = PLANNING");
            out.println("Simulation.logLevel = " + logLevel);
            out.println("Simulation.setUpTime = " + freqSetupTime);
            out.println("Simulation.changeProjectTime = " + projSetupTime);
            out.println("Simulation.advanceClock = " + advanceClock);
            out.println(); out.println();
            out.println("Site.longitude = " + longitude);
            out.println("Site.latitude = " + latitude); 
            out.println("Site.timeZone = " + timezone); 
            out.println("Site.altitude = " + altitude); 
            out.println("Site.minimumElevationAngle = " +minAngle); 
            out.println("Site.numberAntennas = " + totalAntennas); 
            out.println(); out.println(); 
            out.println("FrequencyBand.numberOfBands = " + totalfreqbands);
            for(int i=0; i< totalfreqbands; i++) {
                out.println("FrequencyBand."+i+" = " + 
                    (String)((Vector)freqbands.elementAt(i)).elementAt(0) +"; "+
                    (String)((Vector)freqbands.elementAt(i)).elementAt(1) +"; "+
                    (String)((Vector)freqbands.elementAt(i)).elementAt(2) +"; "+
                    (String)((Vector)freqbands.elementAt(i)).elementAt(3) +"; "+
                    (String)((Vector)freqbands.elementAt(i)).elementAt(4) +"; ");
            }
            out.println(); out.println();
            out.println("Weather.numberFunctions = " + totalweatherfunc);
            for(int i=0; i < totalweatherfunc; i = i+9){
                out.println("Weather."+ i + " = " +
                    (String)weatherfuncvalues.elementAt(i+0) + "; "+
                    (String)weatherfuncvalues.elementAt(i+1) + "; "+
                    (String)weatherfuncvalues.elementAt(i+2) + "; "+
                    (String)weatherfuncvalues.elementAt(i+3) + "; "+
                    (String)weatherfuncvalues.elementAt(i+4) + "; "+
                    (String)weatherfuncvalues.elementAt(i+5) + "; "+
                    (String)weatherfuncvalues.elementAt(i+6) + "; "+
                    (String)weatherfuncvalues.elementAt(i+7) + "; "+
                    (String)weatherfuncvalues.elementAt(i+8) );
            }
            out.println(); out.println();
            out.println("Weight.positionElevation =  " + posElevation_w);
            out.println("Weight.positionMaximum =  " + posMax_w);
            out.println("Weight.weather =  " + weather_w);
            out.println("Weight.priority = " + priority_w);
            out.println("Weight.sameProjectSameBand = " + spsb_w);
            out.println("Weight.sameProjectDifferentBand = " + spdb_w);
            out.println("Weight.differentProjectSameBand = " + dpsb_w);
            out.println("Weight.differentProjectDifferentBand = " + dpdb_w);
            out.println("Weight.newProject = " + newproj_w);
            out.println("Weight.oneSBRemaining = " + lastsb_w);
            out.println(); out.println();
            /**
              * Indicate the source of the project data.
              * The projectSourceType tells what kind of input the source data is.
              * At present, only JavaPoperties is supported.  In the future,
              * there will be other possibilities, such as an archive.
              * The projectSource is the name of the project source file.
              * This name is relative to the same directory that is specified
              * in the simulation input.
              */
            out.println("projectSourceType = JavaProperties");
            out.println("projectSource = project.txt");
            /** 
              * Create a new file for all the project stuff. Right now this is
              * set to always be called project.txt
              * TODO make it so user can change name of file the projects are stored in.
              */
            f = new File("project.txt");
            out = new PrintStream(new FileOutputStream(f));
            out.println("numberProjects = " + totalprojects);
            out.println();
            for(int i=0; i < totalprojects; i++){
                //project name; pi; priority; # of sets
                Vector proj = (Vector)projects.elementAt(i);
                out.println("project."+i+" = "+
                    (String)(proj.elementAt(0)) + "; " +
                    (String)(proj.elementAt(1)) + "; " +
                    (String)(proj.elementAt(2)) + "; " +
                    (String)(proj.elementAt(3)) );
                //get # of sets
                Vector sets = (Vector)proj.elementAt(4);
                int numOfSets = sets.size();
                for(int j=0; j< numOfSets; j++){
                    Vector set = (Vector)sets.elementAt(j);
                    out.println("set."+i+"."+j+" = "+
                           ((String)set.elementAt(0)) +"; "+ 
                           ((String)set.elementAt(1)) +"; "+ 
                           ((String)set.elementAt(2)) +"; "+ 
                           ((String)set.elementAt(3)) +"; "+ 
                           ((String)set.elementAt(4)) );
                    Vector targets = (Vector)set.elementAt(5);

                    //get # of targets.
                    int numOfTargets= targets.size();
                    for(int k=0; k < numOfTargets;k++){
                        Vector target = (Vector)targets.elementAt(k);
                        out.println("target."+i+"."+j+"."+k+" = "+
                                ((String)target.elementAt(0)) +"; "+ 
                                ((String)target.elementAt(1)) +"; "+ 
                                ((String)target.elementAt(2)) +"; "+ 
                                ((String)target.elementAt(3)) +"; "+ 
                                ((String)target.elementAt(4)) +"; "+ 
                                ((String)target.elementAt(5)) +"; "+ 
                                ((String)target.elementAt(6)) );
                    }
                }
                out.println();
                
                        
            }
            out.println();

        } catch(Exception ex) {
            System.out.println("ERROR!");
            ex.printStackTrace();
        }
    }

    
    //////////////////////////////////////////////
    //  After file is created/Saved here's what
    //  is used to run the simulation and display
    //  the results.
    //////////////////////////////////////////////
    
    /**
     * Creates a simulator object and runs a simulation using the data.txt file.
     * If no file is found with that name the simulation does not run.
     */
    public void runSimulation() {
        File f = new File(data_filename);
        if(!f.exists()){
            System.out.println("A data file must be properly created " 
                + "before a simulation can be run.");
        } else {
            clearInputFields();
            simulator = new Simulator();
            try {
                simulator.initialize(".", data_filename, "output.txt", "log.txt");
                Thread t = new Thread(simulator);
                t.start();
            } catch(Exception e) {
                System.out.println("Error initializing simulator!");
                e.printStackTrace();
            }
        }
    }

    /**
     * Clears the gui of the layout used for creating the data file.
     */
    private void clearInputFields() {
        Component[] comp1 = gui.getComponents();
        JRootPane rp = (JRootPane)comp1[0];
        Component[] comp2 = rp.getComponents();
        JLayeredPane lp = (JLayeredPane)comp2[1];
        Component[] comp3 = lp.getComponents();
        JPanel p = (JPanel)comp3[0];
        Component[] comp4 = p.getComponents();
        JTabbedPane tp = (JTabbedPane)comp4[1];
        tp.removeAll();
        createSimulationOutputView(tp);
        
    }

    /**
     *  Creates the output views for the results of the simulation.
     */
    private void createSimulationOutputView(JTabbedPane pane) {
        pane.addTab("SchedBlock Info", 
                new JScrollPane(new JTextArea()));
        pane.addTab("Sources - Visibility and Execution", 
                new JScrollPane(new JTextArea()));
    }

    
    /**
     * Pop up a file chooser so that the user can pick which file to 
     * load. The file is then loaded and the contents of the file are
     * displayed in their appropriate fields.
     */
    public void loadFile(String filepathname, String filename) {
        System.out.println("Opening "+filepathname);
        data_filename = filename;
        Vector tab1 = new Vector();
        Vector tab2 = new Vector();
        Vector tab3 = new Vector();
        Vector tab4 = new Vector();
        Vector tab5 = new Vector();
        Properties fileproperties = new Properties();
        FileInputStream file;
        try {
            file = new FileInputStream(filepathname);
            fileproperties.load(file);
            ////////////////////////////////////////////////////////////
            tab1.add(fileproperties.getProperty(Tag.beginTime));
            tab1.add(fileproperties.getProperty(Tag.endTime));
            tab1.add(fileproperties.getProperty(Tag.logLevel));
            tab1.add(fileproperties.getProperty(Tag.setUpTime));
            tab1.add(fileproperties.getProperty(Tag.changeProjectTime));
            tab1.add(fileproperties.getProperty(Tag.advanceClock));
            tab1.add(fileproperties.getProperty(Tag.longitude));
            tab1.add(fileproperties.getProperty(Tag.latitude));
            tab1.add(fileproperties.getProperty(Tag.timeZone));
            tab1.add(fileproperties.getProperty(Tag.altitude));
            tab1.add(fileproperties.getProperty(Tag.minimumElevationAngle));
            tab1.add(fileproperties.getProperty(Tag.numberAntennas));
            simPropTab.loadValuesFromFile(tab1);
            ////////////////////////////////////////////////////////////
            int totalbands = Integer.parseInt(
                (String)fileproperties.getProperty(Tag.numberOfBands));
            tab2.add(fileproperties.getProperty(Tag.numberOfBands));
            for(int i = 0; i < totalbands; i++) {
                tab2.add(fileproperties.getProperty(Tag.band+"."+i));
            }
            frequencyTab.loadValuesFromFile(tab2);
            ////////////////////////////////////////////////////////////
            tab3.add(fileproperties.getProperty(Tag.numberWeatherFunctions));
            int totalWeatherFuncs = Integer.parseInt(
                (String)fileproperties.getProperty(Tag.numberWeatherFunctions));
            for(int i=0; i<totalWeatherFuncs; i++) {
                tab3.add(fileproperties.getProperty(Tag.weather+"."+i));
            }
            weatherTab.loadValuesFromFile(tab3);
            ////////////////////////////////////////////////////////////
            tab4.add(fileproperties.getProperty(Tag.weightPositionElevation));
            tab4.add(fileproperties.getProperty(Tag.weightPositionMaximum));
            tab4.add(fileproperties.getProperty(Tag.weightWeather));
            tab4.add(fileproperties.getProperty(Tag.weightSameProjectSameBand));
            tab4.add(fileproperties.getProperty(Tag.weightSameProjectDifferentBand));
            tab4.add(fileproperties.getProperty(Tag.weightDifferentProjectSameBand));
            tab4.add(fileproperties.getProperty(Tag.weightDifferentProjectDifferentBand));
            tab4.add(fileproperties.getProperty(Tag.weightNewProject));
            tab4.add(fileproperties.getProperty(Tag.weightOneSBRemaining));
            tab4.add(fileproperties.getProperty(Tag.weightPriority));
            algorithmTab.loadValuesFromFile(tab4);
            ////////////////////////////////////////////////////////////
            try {
                String projectfile = fileproperties.getProperty(Tag.projectSource);
                System.out.println(projectfile);
                file = new FileInputStream(projectfile);
                fileproperties.load(file);
                System.out.println("Project file loaded with project.txt");
            } catch (Exception e1) {
                String projFile = promptForProjectFile();
                System.out.println(projFile);
                try {
                    file = new FileInputStream(projFile);
                    fileproperties.load(file);
                } catch(Exception e2) {
                    file = new FileInputStream(filepathname);
                    fileproperties.load(file);
                }
                System.out.println("Project file loaded with "+projFile);
            }
            
            tab5.add(fileproperties.getProperty(Tag.numberProjects));
            int totalprojects = Integer.parseInt(
                (String)fileproperties.getProperty(Tag.numberProjects));
            System.out.println(totalprojects);
            for(int i=0; i<totalprojects; i++) {
                String s = fileproperties.getProperty(Tag.project+"."+i);
                System.out.println(s);
                StringTokenizer token = new StringTokenizer(s,";");
                int tokencount = token.countTokens();
                if(tokencount != 4) {
                    throw new Exception("Incorrect input data file.");
                } else if (tokencount == 4) {
                    while(token.hasMoreElements()) {
                        s = token.nextToken();
                    }
                }
                s = s.trim();
                int totalsets = Integer.parseInt(s);
                //add proj info line
                tab5.add(fileproperties.getProperty(Tag.project+"."+i)); 
                //add target stuff
                for(int j=0; j<totalsets; j++) {
                    s = fileproperties.getProperty(Tag.set+"."+i+"."+j);
                    tab5.add(s);
                    token = new StringTokenizer(s,";");
                    if(token.countTokens() !=5 ) {
                        throw new Exception("Incorrect input data file.");
                    } else if (token.countTokens() == 5 ) {
                        while(token.hasMoreElements()) {
                            s = token.nextToken();
                        }
                    }
                    s = s.trim();
                    int totaltargets = Integer.parseInt(s);
                    for(int k=0; k < totaltargets; k++) {
                        s = fileproperties.getProperty(Tag.target+"."+i+"."+j+"."+k);
                        tab5.add(s);
                    }
                }
            }
            //for(int i=0; i < tab5.size(); i++) {
            //    System.out.println(tab5.elementAt(i));
            //}
            projTab.loadValuesFromFile(tab5);
        
        ////////////////////////////////////////////////////////////
        
        } catch(Exception e) {
            System.out.println("Crap");
            e.printStackTrace();
        }
    }

    public String promptForProjectFile() {
        return gui.getProjectFile();
    }

    //////////////////////////////////////////////

    public static void main(String args[]) {
        PlanningModeSimGUIController controller = new PlanningModeSimGUIController();
        Thread t = new Thread(controller);
        t.start();
    }
}
