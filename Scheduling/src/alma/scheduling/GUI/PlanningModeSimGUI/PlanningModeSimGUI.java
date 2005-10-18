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

import java.lang.Integer;

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
import javax.swing.ScrollPaneLayout;
import javax.swing.JFileChooser;
import java.awt.Container;

public class PlanningModeSimGUI extends JFrame {
    private PlanningModeSimGUIController controller; 
    private int totalSelected=0;
    private JFileChooser chooser;
    private Container contentpane;

    public PlanningModeSimGUI(PlanningModeSimGUIController c) {
        this.controller = c;

        guiSetup();

    }

    public String createFileChooser(String type) {
        String result = null;
        int returnVal=0;
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
        JMenuBar menuBar = new JMenuBar();
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);
        menuBar.add(file);

        JMenuItem newSim = new JMenuItem("New Simulation");
        newSim.setMnemonic(KeyEvent.VK_N);
        newSim.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getContentPane().add(createContentPanels(),BorderLayout.CENTER);
                
            }
        });
        file.add(newSim);
        
        JMenuItem save = new JMenuItem("Save As");
        save.setMnemonic(KeyEvent.VK_S);
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String s = createFileChooser("save");
                if (s != null) {
                    controller.saveToFile(chooser.getSelectedFile().getPath());
                } else {
                    System.out.println("Canceled Save, s == null");
                }
            }
        });
        file.add(save);

        JMenuItem load = new JMenuItem("Load");
        load.setMnemonic(KeyEvent.VK_L);
        load.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String s = createFileChooser("load");
                if(s != null) {
                    controller.loadFile(chooser.getSelectedFile().getPath(),
                                        chooser.getSelectedFile().getName());
                } else{
                    System.out.println("Canceled load, s == null!");
                }

            }
        });
        file.add(load);

        JMenuItem exit = new JMenuItem("Exit");
        exit.setMnemonic(KeyEvent.VK_X);
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });
        file.add(exit);

        JMenu simulation = new JMenu("Simulation");
        simulation.setMnemonic(KeyEvent.VK_S);
            
        JMenuItem runSim = new JMenuItem("Run Simulation");
        runSim.setMnemonic(KeyEvent.VK_R);
        runSim.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.runSimulation();
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
        outputPanes.addTab("Antennas", new GUIAntennaTab()); //<<<<<<<<<<<<<<<<AF,2005-10-18
        outputPanes.addTab("Frequency", new FrequencyBandTab());
        outputPanes.addTab("Weather", new WeatherDataTab());
        outputPanes.addTab("Weights", new AlgorithmWeightsTab());
        outputPanes.addTab("Projects", new ProjectsTab());
        return outputPanes;
    }

    public String getProjectFile() {
        String s = createFileChooser("load");
        if(s == null) {
            System.out.println("Canceled project load, s == null!");
        }
        return chooser.getSelectedFile().getPath();
    }

    public void exit() {
        dispose();
    }
}
