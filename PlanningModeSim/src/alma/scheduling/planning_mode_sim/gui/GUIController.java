package alma.scheduling.planning_mode_sim.gui;

import java.net.URL;
import java.awt.Component;
import java.lang.Integer;
import java.util.Vector;
import java.io.File;
import java.io.PrintStream;
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

import alma.scheduling.planning_mode_sim.simulator.Simulator;

/**
 * This class has all the functionality that is required by the GUI. It
 * is the class that interacts with the rest of the planning mode simulation
 * classes.
 */
public class GUIController implements Runnable {
    private GUI gui;
    //Tab objects
    private ProjectsTab             projTab;
    private WeatherDataTab          weatherTab;
    private FrequencyBandTab        frequencyTab;
    private AlgorithmWeightsTab     algorithmTab;
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

    public GUIController() {
        weatherfuncvalues = new Vector();
        freqbands = new Vector();
    }
    
    public void run() {
        this.gui = new GUI(this);
    }
    
    protected URL getImage(String name) {
        return this.getClass().getClassLoader().getResource(
            "alma/scheduling/planning_mode_sim/image/"+name);
    }

    protected void saveToFile(String filename) {
        Component[] comp1 = gui.getComponents();
        JRootPane rp = (JRootPane)comp1[0];
        Component[] comp2 = rp.getComponents();
        JLayeredPane lp = (JLayeredPane)comp2[1];
        Component[] comp3 = lp.getComponents();
        JPanel p = (JPanel)comp3[0];
        Component[] comp4 = p.getComponents();
        JTabbedPane tp = (JTabbedPane)comp4[1];
        Component[] comp5 = tp.getComponents();
        
        //Simulation Properties tab
        simPropTab = (SimulationPropertiesTab)comp5[0];
        stuffFromSimulationPropertiesTab();
        
        //Frequency band tab
        frequencyTab = (FrequencyBandTab)comp5[1];
        stuffFromFrequencyTab();
       
        //Weather model tab
        weatherTab = (WeatherDataTab)comp5[2];
        stuffFromWeatherTab();
       
        //Weights tab
        algorithmTab = (AlgorithmWeightsTab)comp5[3];
        stuffFromAlgorithmWeightsTab();
              
        //Projects tab
        projTab = (ProjectsTab)comp5[4];
        stuffFromProjectsTab();
       
        //save all gathered info to a file
        data_filename = filename;
        createFile(data_filename);

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
            out.println();
            out.println();
            out.println("Site.longitude = " + longitude);
            out.println("Site.latitude = " + latitude);
            out.println("Site.timezone = " + timezone);
            out.println("Site.altitude = " + altitude);
            out.println("Site.minimumElevationAngle = " +minAngle);
            out.println("Site.numberAntennas = " + totalAntennas);
            out.println();
            out.println();
            out.println("FrequencyBand.numberOfBands = " + totalfreqbands);
            for(int i=0; i< totalfreqbands; i++) {
                out.println("FrequencyBand."+i+" = " + 
                    (String)((Vector)freqbands.elementAt(i)).elementAt(0) +"; "+
                    (String)((Vector)freqbands.elementAt(i)).elementAt(1) +"; "+
                    (String)((Vector)freqbands.elementAt(i)).elementAt(2) +"; "+
                    (String)((Vector)freqbands.elementAt(i)).elementAt(3) +"; "+
                    (String)((Vector)freqbands.elementAt(i)).elementAt(4) +"; ");
            }
            out.println();
            out.println();
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
            out.println();
            out.println();
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
            out.println();
            out.println();
            out.println("numberProjects = " + totalprojects);
            out.println();
            int x=0;
            for(int i=0; i < totalprojects*5; i=i+5) {
                out.println("project."+ x +" = " +
                    ((String)(projects.elementAt(i+0))) + "; " +
                    ((String)(projects.elementAt(i+1))) + "; " +
                    ((String)(projects.elementAt(i+2))) + "; " +
                    ((String)(projects.elementAt(i+3))) );
                Vector targets = (Vector)projects.elementAt(i+4);
                int targetSize = targets.size()/7;
                //System.out.println("total targets = "+(targets.size()/7));
                for(int j=0; j < targetSize; j++) {
                    int z = 0;
                    out.println("target."+x+"."+j+" = " +
                        ((String)(targets.elementAt(z))) + "; " +
                        ((String)(targets.elementAt(z+1))) + "; " +
                        ((String)(targets.elementAt(z+2))) + "; " +
                        ((String)(targets.elementAt(z+3))) + "; " +
                        ((String)(targets.elementAt(z+4))) + "; " +
                        ((String)(targets.elementAt(z+5))) + "; " +
                        ((String)(targets.elementAt(z+6))) );
                }
                x++;
            }
            out.println();
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
        File f = new File("data.txt");
        if(!f.exists()){
            System.out.println("A data file must be properly created " 
                + "before a simulation can be run.");
        } else {
            clearInputFields();
            simulator = new Simulator(".", "data.txt", "output.txt", "log.txt");
            Thread t = new Thread(simulator);
            t.start();
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
    public void loadFile(String filename) {
        System.out.println("Opening "+filename);
    }
    
    //////////////////////////////////////////////

    public static void main(String args[]) {
        GUIController controller = new GUIController();
        Thread t = new Thread(controller);
        t.start();
    }
}
