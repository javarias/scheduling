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
import javax.swing.JLayeredPane;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.JViewport;


public class GUIController implements Runnable {
    private GUI gui;
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


    public GUIController() {
        weatherfuncvalues = new Vector();
        freqbands = new Vector();
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
        
        //Simulation Properties tab
        JScrollPane pane1 = (JScrollPane)comp5[0];
        Component[] comp5a = pane1.getComponents();
        Component[] comp5aa= ((JViewport)comp5a[0]).getComponents();
        JPanel p1 = (JPanel)comp5aa[0];
        stuffFromTab1(p1);

        //Frequency band tab
        JScrollPane pane2 = (JScrollPane)comp5[1];
        Component[] comp5b = pane2.getComponents();
        Component[] comp5ba= ((JViewport)comp5b[0]).getComponents();
        JPanel p2 =(JPanel)comp5ba[0];
        stuffFromTab2(p2);
        
        //Weather model tab
        JScrollPane pane3 = (JScrollPane)comp5[2];
        Component[] comp5c = pane3.getComponents();
        Component[] comp5ca= ((JViewport)comp5c[0]).getComponents();
        JPanel p3 =(JPanel)comp5ca[0];
        stuffFromTab3(p3);
        
        //Weights tab
        JScrollPane pane4 = (JScrollPane)comp5[3];
        Component[] comp5d = pane4.getComponents();
        Component[] comp5da= ((JViewport)comp5d[0]).getComponents();
        JPanel p4 = (JPanel)comp5da[0];
        stuffFromTab4(p4);

        //Projects tab
        JScrollPane pane5 = (JScrollPane)comp5[4];
        Component[] comp5e = pane5.getComponents();
        Component[] comp5ea= ((JViewport)comp5e[0]).getComponents();
        JPanel p5 =(JPanel)comp5ea[0];
        stuffFromTab5(p5);

        //save all gathered info to a file
        createFile();

    }

    private void stuffFromTab1(JPanel p) {
        // 7 JPanels on 1st tab
        Component[] p1Comps = p.getComponents(); 
        //0=Simulation Properties
        JPanel p1a = (JPanel)p1Comps[0];
        //1. start-end time and log level
        JPanel p1b = (JPanel)p1Comps[1];
        Component[] p1bComps = p1b.getComponents();
        startTime = ((JTextField)p1bComps[1]).getText();
        logLevel = (String)((JComboBox)p1bComps[4]).getSelectedItem();
        endTime = ((JTextField)p1bComps[7]).getText();
        //2. empty label
        //3. setup times 
        // make it so if they enter hours or min it is changed to sec
        JPanel p1c = (JPanel)p1Comps[3];
        Component[] p1cComps = p1c.getComponents();
        freqSetupTime = ((JTextField)p1cComps[2]).getText();
                        //((JComboBox)p1cComps[3]).getSelectedItem();

        projSetupTime = ((JTextField)p1cComps[6]).getText();
                        //((JComboBox)p1cComps[7]).getSelectedItem(); 

        advanceClock = ((JTextField)p1cComps[10]).getText();
                        //((JComboBox)p1cComps[11]).getSelectedItem();
        //4. Site characteristics label
        //5. longitude, latitude, timezone, altitude
        JPanel p1d = (JPanel)p1Comps[5];
        Component[] p1dComps = p1d.getComponents();
        longitude = ((JTextField)p1dComps[1]).getText();
        latitude = ((JTextField)p1dComps[4]).getText();
        //FIX SO JUST a #
        timezone = (String)((JComboBox)p1dComps[7]).getSelectedItem(); 
        altitude = ((JTextField)p1dComps[10]).getText();
        //6. angle & antennas
        JPanel p1e = (JPanel)p1Comps[6];
        Component[] p1eComps = p1e.getComponents();
        minAngle = ((JTextField)p1eComps[1]).getText();
        totalAntennas = (String)((JComboBox)p1eComps[4]).getSelectedItem();
    }

    private void stuffFromTab2(JPanel p) {
        Component[] pComps = p.getComponents();
        JPanel p1 = (JPanel)pComps[0];
        Component[] p1Comps = p1.getComponents();
        totalfreqbands = Integer.parseInt(
                        (String) ((JComboBox)p1Comps[3]).getSelectedItem());
        JPanel p2 = (JPanel)pComps[1];
        Component[] p2Comps = p2.getComponents();
        for(int i=0; i < totalfreqbands; i++) {
            Vector tmp1 = new Vector();
            JPanel tmp2 = (JPanel) p2Comps[i];
            Component[] tmp3 = tmp2.getComponents();
            tmp1.add(((JTextField)tmp3[1]).getText());
            tmp1.add(((JTextField)tmp3[3]).getText());
            tmp1.add(((JTextField)tmp3[5]).getText());
            tmp1.add(((JTextField)tmp3[9]).getText());
            tmp1.add(((JTextField)tmp3[11]).getText());
            freqbands.add(tmp1);
        }
        
    }

    private void stuffFromTab3(JPanel p) {
        Component[] p3Comps = p.getComponents();
        JPanel p1 = (JPanel)p3Comps[0];
        Component[] p1Comps = p1.getComponents();
        totalweatherfunc = Integer.parseInt(
                            (String)((JComboBox)p1Comps[5]).getSelectedItem());
        JPanel p2 = (JPanel)p3Comps[1];
        Component[] p2Comps = p2.getComponents();
        for (int i=0; i < totalweatherfunc; i++) {
        //### This might need some debugging when more weather funcs are added 
            weatherfuncvalues.add( ((JTextField)p2Comps[i+5]).getText() ); //name 
            weatherfuncvalues.add( ((JTextField)p2Comps[i+7]).getText() ); //units 
            weatherfuncvalues.add( ((JTextField)p2Comps[i+11]).getText() ); //p0 
            weatherfuncvalues.add( ((JTextField)p2Comps[i+15]).getText() ); //p1 
            weatherfuncvalues.add( ((JTextField)p2Comps[i+19]).getText() ); //p2 
            weatherfuncvalues.add( ((JTextField)p2Comps[i+23]).getText() ); //s0 
            weatherfuncvalues.add( ((JTextField)p2Comps[i+27]).getText() ); //s1 
            weatherfuncvalues.add( ((JTextField)p2Comps[i+31]).getText() ); //t0 
            weatherfuncvalues.add( ((JTextField)p2Comps[i+35]).getText() ); //t1 
            i += 35;
        }
        
    }

    private void stuffFromTab4(JPanel p) {
        Component[] p4Comps = p.getComponents();
        posElevation_w = ((JTextField)p4Comps[3]).getText();
        posMax_w = ((JTextField)p4Comps[7]).getText();
        weather_w = ((JTextField)p4Comps[11]).getText();
        spsb_w = ((JTextField)p4Comps[15]).getText();
        spdb_w = ((JTextField)p4Comps[19]).getText();
        dpsb_w = ((JTextField)p4Comps[23]).getText();
        dpdb_w = ((JTextField)p4Comps[27]).getText();
        newproj_w = ((JTextField)p4Comps[31]).getText();
        lastsb_w = ((JTextField)p4Comps[35]).getText();
        priority_w = ((JTextField)p4Comps[39]).getText();
    }
    
    private void stuffFromTab5(JPanel p) {
        Component[] p5Comps = p.getComponents();
        JPanel p1 = (JPanel)p5Comps[0];
        Component[] p1Comps = p1.getComponents();
        String tmpstr = "";
        try {
            tmpstr = ((JTextField)p1Comps[3]).getText();
            if(tmpstr.equals("Enter # of projects") || tmpstr.equals("")) {
                totalprojects = 0;
            } else {
                totalprojects = Integer.parseInt(tmpstr);
            }
            //System.out.println(totalprojects);
            if(totalprojects > 0 ){
                projects = new Vector();
                Component[] projectPanes = ((JTabbedPane)p5Comps[1]).getComponents();
                Component[] tabPanes, tmp, comps;
                JPanel panel1, panel2, targetpanel;
                for(int i=0; i < totalprojects; i++) {
                    tabPanes = ((JScrollPane)projectPanes[i]).getComponents();
                    tmp = ((JViewport)tabPanes[0]).getComponents();
                    panel1 =(JPanel)tmp[0]; //mainpanel
                    tmp = panel1.getComponents();
                    panel2 = (JPanel)tmp[0];
                    comps = panel2.getComponents();
                    projects.add(((JTextField)comps[1]).getText());
                    projects.add(((JTextField)comps[3]).getText());
                    projects.add(((JTextField)comps[5]).getText());
                    projects.add(((JTextField)comps[7]).getText());
                    //add targets now!
                    Component[] targets = ((JPanel)tmp[1]).getComponents();
                    projects.add(getTargetsInfo(targets));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }
    private Vector getTargetsInfo(Component[] comp) throws Exception {
        int totaltargets = comp.length;
        Vector targets = new Vector(totaltargets);
        JPanel p;
        Component[] c;
        String tmp;
        for(int i=0; i < totaltargets; i++ ) {
            p = (JPanel)comp[i];
            c = p.getComponents();
            //System.out.println(c.length);
            tmp = ((JTextField)c[1]).getText();
            if(tmp == "") throw new Exception("Target must have a name.");
            targets.add(tmp);
            tmp = ((JTextField)c[3]).getText();
            if(tmp == "") throw new Exception("Target must have a RA.");
            targets.add(tmp);
            tmp = ((JTextField)c[5]).getText();
            if(tmp == "") throw new Exception("Target must have a DEC.");
            targets.add(tmp);
            tmp = ((JTextField)c[9]).getText();
            if(tmp == "") throw new Exception("Target must have a frequency.");
            targets.add(tmp);
            tmp = ((JTextField)c[11]).getText();
            if(tmp == "") throw new Exception("Target must have a total time.");
            targets.add(tmp);
            tmp =  (String)((JComboBox)c[15]).getSelectedItem();
            targets.add(tmp);
            tmp = ((JTextField)c[17]).getText();
            if ((tmp == "") || (tmp == null)) tmp = "0";
            targets.add(tmp);
            
        }

        return targets;
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
                System.out.println("total targets = "+(targets.size()/7));
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

    public static void main(String args[]) {
        GUIController controller = new GUIController();
        Thread t = new Thread(controller);
        t.start();
    }
}
