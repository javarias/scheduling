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

    public PlanningModeSimGUI(PlanningModeSimGUIController c) {
        this.controller = c;

        guiSetup();

    }

    public String createFileChooser(String type, String name) {
        String result = null;
        int returnVal=0;
        File f = chooser.getCurrentDirectory();
        //System.out.println(System.getProperty("user.dir"));
        chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        chooser.setDialogTitle(name);
        if(type.equals("save") ) {
            returnVal = chooser.showSaveDialog(this);
        } else if(type.equals("load") ) {
            returnVal = chooser.showOpenDialog(this);
        }
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            result = chooser.getSelectedFile().getName();
        }
        return result;
    }

    private void guiSetup() {
        chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.setFileFilter(new TxtFileFilter());
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
        String s = createFileChooser("save", "Save Simulation Input file");
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
        String s = createFileChooser("load", "Load input file");
        if(s != null) {
            controller.loadFile(chooser.getSelectedFile().getPath(),
                                chooser.getSelectedFile().getName());
        } else{
            System.out.println("Canceled load");
        }
    }
    private void doNewSimulationAction(){
        runSim.setEnabled(true);
        controller.startingNewSimulation();
        getContentPane().remove(1);
        getContentPane().add(createContentPanels(),BorderLayout.CENTER);
        getContentPane().requestFocus();
        getContentPane().validate();
    }
    private void doRunSimulationAction(){
        RunSimulationThread sim = new RunSimulationThread();
        Thread t = new Thread(sim);
        t.run();
    }

    private JPanel createGUIHeader() {
        ImageIcon almaImage = new ImageIcon(controller.getImage("alma_logo.jpg"));
        JPanel panel = new JPanel(new BorderLayout());
        JLabel logo = new JLabel(almaImage);
        panel.add(logo,BorderLayout.EAST);
        JLabel title1 = new JLabel("Scheduling");
        title1.setFont(new Font("", Font.ITALIC, 24));
        JLabel title2 = new JLabel("Planning Mode Simulator");
        title2.setFont(new Font("", Font.ITALIC, 24));
        JPanel titlepanel = new JPanel(new GridLayout(2,1));
        titlepanel.add(title1);
        titlepanel.add(title2);
        panel.add(titlepanel,BorderLayout.CENTER);

        return panel;
    }

    private JTabbedPane createContentPanels() {
        JTabbedPane outputPanes = new JTabbedPane();
        outputPanes.setTabPlacement(JTabbedPane.TOP);
        outputPanes.addTab("Properties", new SimulationPropertiesTab());
        outputPanes.addTab("Antennas", new AntennaTab());
        outputPanes.addTab("Frequency", new FrequencyBandTab());
        outputPanes.addTab("Weather", new WeatherDataTab());
        outputPanes.addTab("Weights", new AlgorithmWeightsTab());
        outputPanes.addTab("Projects", new ProjectsTab());
        return outputPanes;
    }

    //private JTabbedPane clearContentPanels(){
    //}

    public String getProjectFile() {
        String s = createFileChooser("load","Load Project File");
        if(s == null) {
            System.out.println("Canceled project load");
            return "";
        }
        return chooser.getSelectedFile().getPath();
    }

    public void exit() {
        dispose();
    }

   
    class TxtFileFilter extends FileFilter {
        private Hashtable filters = null;
        public TxtFileFilter(){
            filters = new Hashtable();
            addExtension("txt");
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

    class RunSimulationThread implements Runnable {
        public RunSimulationThread(){}
        
        public void run(){
            doSaveAction();
            controller.runSimulation();
            runSim.setEnabled(false);
        }
    }
}
