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
 * File PlanningModeSimGUI.java
 */

package alma.scheduling.GUI.PlanningModeSimGUI;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagLayout; 
import java.awt.GridBagConstraints; 

import java.util.Hashtable;
import java.lang.Integer;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.ScrollPaneLayout;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.JProgressBar;

public class PlanningModeSimGUI extends JFrame {
    private PlanningModeSimGUIController controller; 
    private int totalSelected=0;
    private JFileChooser chooser;
    private Container contentpane;
    private JMenuItem newSim;
    private JMenuItem save;
    private JMenuItem load;
    private JMenuItem exit;
    private final JMenuItem runSim = new JMenuItem("Run Simulation");
    private JMenuItem saveAntenna;
    private JMenuItem saveSchedule;
    private JProgressBar progressBar;
    private JPanel headerPanel;

    public PlanningModeSimGUI(PlanningModeSimGUIController c) {
        this.controller = c;

        guiSetup();

    }

    public String createFileChooser(String type, String name, String action) {
        String result = null;
        int returnVal=0;
        chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        chooser.setDialogTitle(name);
        if(type.equals("save") && action.equals("txtfile") ) {
            chooser.setFileFilter(new TxtFileFilter());
            returnVal = chooser.showSaveDialog(this);
        } else if(type.equals("save") && action.equals("giffile") ) {
            chooser.setFileFilter(new JPGFileFilter());
            returnVal = chooser.showSaveDialog(this);
        } else if(type.equals("load") && action.equals("txtfile") ) {
            chooser.setFileFilter(new TxtFileFilter());
            returnVal = chooser.showOpenDialog(this);
        }
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                result = chooser.getSelectedFile().getCanonicalPath();
            } catch(Exception e){
                result = chooser.getSelectedFile().getName();
            }
            //result = chooser.getSelectedFile().getName();
        }
        return result;
    }

    private void guiSetup() {
        chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(true);
        JMenuBar menuBar = new JMenuBar();
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);
        menuBar.add(file);

        newSim = new JMenuItem("New Simulation");
        save = new JMenuItem("Save As");
        load = new JMenuItem("Load");
        exit = new JMenuItem("Exit");
        
        newSim.setMnemonic(KeyEvent.VK_N);
        newSim.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doNewSimulationAction();
            }
        });
        file.add(newSim);
        
        save.setMnemonic(KeyEvent.VK_S);
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doSaveAction();
            }
        });
        file.add(save);

        load.setMnemonic(KeyEvent.VK_L);
        load.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doLoadAction();
            }
        });
        file.add(load);

        exit.setMnemonic(KeyEvent.VK_X);
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });
        file.add(exit);

        JMenu simulation = new JMenu("Simulation");
        simulation.setMnemonic(KeyEvent.VK_S);
            
        runSim.setMnemonic(KeyEvent.VK_R);
        runSim.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doRunSimulationAction();
            }
        });

        simulation.add(runSim);

        /*
        JMenuItem runScripts = new JMenuItem("Run Scripts");
        runScripts.setMnemonic(KeyEvent.VK_C);
        runScripts.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runAnalysisScripts();
            }
        });
        simulation.add(runScripts); 
        */
        saveSchedule = new JMenuItem("Save Schedule GIF");
        saveSchedule.setMnemonic(KeyEvent.VK_S);
        saveSchedule.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doSaveScheduleJPG();
            }
        });
        
        saveAntenna = new JMenuItem("Save Antenna Location GIF");
        saveAntenna.setMnemonic(KeyEvent.VK_A);
        saveAntenna.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doSaveAntennaJPG();
            }
        });

        saveSchedule.setEnabled(false);
        saveAntenna.setEnabled(false);
        simulation.add(saveSchedule); 
        simulation.add(saveAntenna); 
        menuBar.add(simulation);

        setJMenuBar(menuBar);

        int inset = 250;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset, screenSize.width - inset*2, 
                screenSize.height - inset*2);
        setTitle("Planning Mode Simulator");
        setSize(600, 700);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(createGUIHeader(),BorderLayout.NORTH);
        getContentPane().add(createContentPanels(),BorderLayout.CENTER);
        setVisible(true);
    }

    private void doSaveAction(){
        String s = createFileChooser("save", "Save Simulation Input file","txtfile");
        if (s != null) {
            File f = chooser.getCurrentDirectory();
            //String name = f.toString() + File.separator + chooser.getSelectedFile().getName();
            //System.out.println(name);
            controller.saveToFile(
                    chooser.getCurrentDirectory().toString(), 
                    chooser.getSelectedFile().getName());
        } else {
            System.out.println("Canceled Save");
        }
    }
    private void doLoadAction(){
        String s = createFileChooser("load", "Load input file","txtfile");
        if(s != null) {
            controller.loadFile(chooser.getSelectedFile().getPath(),
                                chooser.getSelectedFile().getName());
        } else{
            System.out.println("Canceled load");
        }
    }
    private void doNewSimulationAction(){
        runSim.setEnabled(true);
        saveSchedule.setEnabled(false);
        saveAntenna.setEnabled(false);
        controller.startingNewSimulation();
        getContentPane().remove(1);
        getContentPane().add(createContentPanels(),BorderLayout.CENTER);
        getContentPane().requestFocus();
        getContentPane().validate();
    }
    private void doRunSimulationAction(){
        saveSchedule.setEnabled(true);
        saveAntenna.setEnabled(true);
        RunSimulationThread sim = new RunSimulationThread();
        Thread t = new Thread(sim);
        t.run();
        //start a progress bar 
        progressBar = new JProgressBar(0,100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setIndeterminate(true);
        progressBar.setString("Running Simulation");
        JPanel p = new JPanel(new GridLayout(4,1));
        p.add(progressBar);
        p.add(new JLabel());
        p.add(new JLabel());
        p.add(new JLabel());
        headerPanel.add(p, BorderLayout.CENTER);
        headerPanel.revalidate();
    }

    protected void simulationComplete() {
        progressBar.setBorderPainted(false);
        progressBar.setStringPainted(false);
        progressBar.setIndeterminate(false);
        headerPanel.remove(progressBar);
        headerPanel.validate();
        runSim.setEnabled(true);
    }

    private void doSaveScheduleJPG(){
        String s = createFileChooser("save", "Save Schedule Image","giffile");
        if(s !=null){
            controller.saveScheduleJPG(s);
        }
    }
    private void doSaveAntennaJPG(){
        String s = createFileChooser("save", "Save Antenna Location Image","giffile");
        if(s!=null){
            controller.saveAntennaJPG(s);
        }
    }

    private JPanel createGUIHeader() {
        ImageIcon almaImage = new ImageIcon(controller.getImage("alma_logo.jpg"));
        headerPanel = new JPanel(new BorderLayout());
        JLabel logo = new JLabel(almaImage);
        headerPanel.add(logo,BorderLayout.EAST);
        JLabel title1 = new JLabel("Dynamic Scheduling");
        title1.setFont(new Font("", Font.ITALIC, 24));
        JLabel title2 = new JLabel("Planning Mode Simulator");
        title2.setFont(new Font("", Font.ITALIC, 24));
        JPanel titlepanel = new JPanel(new GridLayout(2,1));
        titlepanel.add(title1);
        titlepanel.add(title2);
        headerPanel.add(titlepanel,BorderLayout.WEST);

        return headerPanel;
    }

    private JTabbedPane createContentPanels() {
        JTabbedPane outputPanes = new JTabbedPane();
        outputPanes.setTabPlacement(JTabbedPane.TOP);
        outputPanes.addTab("Projects", new ProjectsTab());
        outputPanes.addTab("Properties", new SimulationPropertiesTab());
        outputPanes.addTab("Antennas", new AntennaTab());
        outputPanes.addTab("Frequency", new FrequencyBandTab());
        outputPanes.addTab("Weather", new WeatherDataTab(this));
        outputPanes.addTab("Weights", new AlgorithmWeightsTab());
        return outputPanes;
    }

    public String getProjectFile() {
        String s = createFileChooser("load","Load Project File","txtfile");
        if(s == null) {
            System.out.println("Canceled project load");
            return "";
        }
        return chooser.getSelectedFile().getPath();
    }

    public void exit() {
        saveOutput();
        cleanUpFiles();
        dispose();
    }

    private void cleanUpFiles(){
        controller.cleanUpFiles();
    }

    private void saveOutput() {
        int o = JOptionPane.showConfirmDialog(this, 
                "Do you wish to save simulation input and output?",
                "Save Information?",
                JOptionPane.YES_NO_OPTION);

        if(o == JOptionPane.YES_OPTION) {
            doSaveAction();
            //check if simulation has been run & if its has do..
            doSaveAntennaJPG();
            doSaveScheduleJPG();
        } else if(o == JOptionPane.NO_OPTION){
            //just quit
        }
    }

    /////////////////////
    private void runAnalysisScripts(){
        String cmd1 = "ALMASched_lst_vs_day";
        String cmd2 = "ALMASchedSim_antennaLocation";
        String stats = controller.getStatFile(); //input.getStatsFile().getAbsolutePath();
        String inputfilename = controller.getInputFile(); //input.getInputFile().getAbsolutePath();
        String outputfilename = inputfilename.substring(0, (inputfilename.length() -4))+"_graph";
        System.out.println("Stupid output file name = "+outputfilename);
        String scheduleCmdString = cmd1 +" "+ stats +" "+ outputfilename +" "+ inputfilename;
        String antennaPlotCmdString = cmd2 +" "+ inputfilename;
        //System.out.println(scheduleCmdString);
        //System.out.println(inputfilename);
        try{
            CreateScheduleFile foo1 = new CreateScheduleFile(scheduleCmdString);   
            CreateAntennaLocationFile foo2 = new CreateAntennaLocationFile(antennaPlotCmdString);
            Thread t1 = new Thread(foo1);
            Thread t2 = new Thread(foo2);
            t1.start();
            t2.start();
        } catch(Exception e){
            e.printStackTrace();
          //  logger.warning("Error writing analysis files");
        }
    }
     
    class CreateScheduleFile implements Runnable {
        private String command;
        public CreateScheduleFile (String cmd){ 
            command = cmd;
        }
        public void run(){
            try {
                Process p = Runtime.getRuntime().exec(command);
            } catch(Exception e){
                //logger.warning("Error writing schedule file using command \n\t"+command);
            }
        }
    }

    class CreateAntennaLocationFile implements Runnable{
        private String command;
        public CreateAntennaLocationFile (String cmd){
            command = cmd;
        }
        public void run(){
            try {
                Process p = Runtime.getRuntime().exec(command);
            } catch(Exception e){
                //logger.warning("Error writing antenna location file using command \n\t"+command);
            }
        }
    }
   
    class TxtFileFilter extends FileFilter {
        private Hashtable filters = null;
        public TxtFileFilter(){
            filters = new Hashtable();
            addExtension("txt");
            addExtension("TXT");
        }

        public boolean accept(File f){
            if(f!=null){
                if(f.isDirectory()){
                    return true;
                }
                String extension = getExtension(f);
                if(extension !=null && filters.get(getExtension(f)) != null) {
                    return true;
                }
            }
            return false;
        }
        public void addExtension(String extension){
            if(filters == null){
                filters = new Hashtable(3);
            }
            filters.put(extension.toLowerCase(), this);
        }
        public String getDescription(){
            return "TextFile";
        }
        public String getExtension (File f){
            if(f !=null){
                String filename=f.getName();
                int i=filename.lastIndexOf(".");
                if(i>0 && i<filename.length()-1) {
                    return filename.substring(i+1).toLowerCase();
                }
            }
            return null;
        }

    } //end of TxtFileFilter class

  class JPGFileFilter extends FileFilter {
        private Hashtable filters = null;
        public JPGFileFilter(){
            filters = new Hashtable();
            addExtension("jpg");
            addExtension("JPG");
        }

        public boolean accept(File f){
            if(f!=null){
                if(f.isDirectory()){
                    return true;
                }
                String extension = getExtension(f);
                if(extension !=null && filters.get(getExtension(f)) != null) {
                    return true;
                }
            }
            return false;
        }
        public void addExtension(String extension){
            if(filters == null){
                filters = new Hashtable(3);
            }
            filters.put(extension.toLowerCase(), this);
        }
        public String getDescription(){
            return "JPG Image File";
        }
        public String getExtension (File f){
            if(f !=null){
                String filename=f.getName();
                int i=filename.lastIndexOf(".");
                if(i>0 && i<filename.length()-1) {
                    return filename.substring(i+1).toLowerCase();
                }
            }
            return null;
        }

    } //end of GIF FileFilter class


    class RunSimulationThread implements Runnable {
        public RunSimulationThread(){}
        
        public void run(){
            //doSaveAction();
            controller.runSimulation();
            runSim.setEnabled(false);
        }
    }

}
