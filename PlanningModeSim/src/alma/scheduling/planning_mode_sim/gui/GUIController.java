package alma.scheduling.planning_mode_sim.gui;

import java.net.URL;
import java.awt.Component;
import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JLayeredPane;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.JViewport;


public class GUIController implements Runnable {
    private GUI gui;
    //Strings gotten from first tab!
    private String startTime, endTime, logLevel, freqSetupTime, projSetupTime,
                    advanceClock, latitude, longitude, timezone, altitude,
                    minAngle, totalAntennas;
    //Strings gotten from second tab!

    public GUIController() {
    }
    public void run() {
        this.gui = new GUI(this);
    }
    protected URL getImage(String name) {
        return this.getClass().getClassLoader().getResource(
            "alma/scheduling/image/"+name);
    }
    protected void saveToFile() {
        Component[] comp1 = gui.getComponents();
        JRootPane rp = (JRootPane)comp1[0];
        Component[] comp2 = rp.getComponents();
        JLayeredPane lp = (JLayeredPane)comp2[1];
        Component[] comp3 = lp.getComponents();
        JPanel p = (JPanel)comp3[0];
        Component[] comp4 = p.getComponents();
        JTabbedPane tp = (JTabbedPane)comp4[1];
        Component[] comp5 = tp.getComponents();
        JScrollPane pane1 = (JScrollPane)comp5[0];
        Component[] comp5a = pane1.getComponents();
        Component[] comp5aa= ((JViewport)comp5a[0]).getComponents();
        
        JPanel p1 = (JPanel)comp5aa[0];
        stuffFromTab1(p1);
        
        JScrollPane pane4 = (JScrollPane)comp5[3];
        Component[] comp5d = pane4.getComponents();
        Component[] comp5da= ((JViewport)comp5d[0]).getComponents();
        JPanel p4 = (JPanel)comp5da[0];
        stuffFromTab4(p4);

        //createFile();

        /*
        Component[] comp5aaa= ((JPanel)comp5aa[0]).getComponents();
        Component[] pane1Components = ((JPanel)comp5aaa[0]).getComponents();
        */

        JScrollPane pane2 = (JScrollPane)comp5[1];
        JScrollPane pane3 = (JScrollPane)comp5[2];
        JScrollPane pane5 = (JScrollPane)comp5[4];
    }

    private void stuffFromTab1(JPanel p) {
        // 7 JPanels on 1st tab
        Component[] p1Comps = p.getComponents(); 
        //0=Simulation Properties
        JPanel p1a = (JPanel)p1Comps[0];
        //1 start-end time and log level
        JPanel p1b = (JPanel)p1Comps[1];
        Component[] p1bComps = p1b.getComponents();
        startTime = ((JTextField)p1bComps[1]).getText();
        logLevel = (String)((JComboBox)p1bComps[4]).getSelectedItem();
        endTime = ((JTextField)p1bComps[7]).getText();
        //2 empty label
        //3 setup times 
        // make it so if they enter hours or min it is changed to sec
        JPanel p1c = (JPanel)p1Comps[3];
        Component[] p1cComps = p1c.getComponents();
        freqSetupTime = ((JTextField)p1cComps[2]).getText();// + " " +
                        //((JComboBox)p1cComps[3]).getSelectedItem();

        projSetupTime = ((JTextField)p1cComps[6]).getText();// + " " +
                        //((JComboBox)p1cComps[7]).getSelectedItem(); 

        advanceClock = ((JTextField)p1cComps[10]).getText();//+ " " +
                        //((JComboBox)p1cComps[11]).getSelectedItem();
        //4 Site characteristics label
        //5 longitude, latitude, timezone, altitude
        JPanel p1d = (JPanel)p1Comps[5];
        Component[] p1dComps = p1d.getComponents();
        longitude = ((JTextField)p1dComps[1]).getText();
        latitude = ((JTextField)p1dComps[4]).getText();
        //FIX SO JUST #
        timezone = (String)((JComboBox)p1dComps[7]).getSelectedItem(); 
        altitude = ((JTextField)p1dComps[10]).getText();
        //6 angle & antennas
        JPanel p1e = (JPanel)p1Comps[6];
        Component[] p1eComps = p1e.getComponents();
        minAngle = ((JTextField)p1eComps[1]).getText();
        totalAntennas = (String)((JComboBox)p1eComps[4]).getSelectedItem();
    }

    private void stuffFromTab4(JPanel p) {
        Component[] p4Comps = p.getComponents();

        System.out.println(p.getComponentCount());
        System.out.println(p4Comps[0].getClass().getName());
        System.out.println(p4Comps[1].getClass().getName());
        System.out.println(p4Comps[2].getClass().getName());
        System.out.println(p4Comps[3].getClass().getName());
        System.out.println(p4Comps[4].getClass().getName());
        System.out.println(p4Comps[5].getClass().getName());
        System.out.println(p4Comps[6].getClass().getName());
            
    }
    
    private void createFile() {
        try {
            File f = new File("output.txt");
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
            out.println("Site.longitude = " + longitude);
            out.println("Site.latitude = " + latitude);
            out.println("Site.timezone = " + timezone);
            out.println("Site.altitude = " + altitude);
            out.println("Site.minimumElevationAngle = " +minAngle);
            out.println("Site.numberAntennas = " + totalAntennas);
            out.println();
            out.println("FrequencyBand.numberofBands = ");
        } catch(Exception ex) {
            System.out.println("ERROR!");
            ex.printStackTrace();
        }
    }

    public static void main(String args[]) {
        GUIController controller = new GUIController();
        Thread t = new Thread(controller);
        t.start();
    }
}
