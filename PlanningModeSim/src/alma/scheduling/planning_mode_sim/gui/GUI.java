package alma.scheduling.planning_mode_sim.gui;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Dimension;
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
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JComboBox;
import javax.swing.ScrollPaneLayout;

public class GUI extends JFrame {
    private GUIController controller; 
    private int totalSelected=0;

    public GUI(GUIController c) {
        this.controller = c;

        guiSetup();

    }

    private void guiSetup() {
        JMenuBar menuBar = new JMenuBar();
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_ALT);
        menuBar.add(file);

        JMenuItem exit = new JMenuItem("Exit");
        exit.setMnemonic(KeyEvent.VK_X);
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });
        file.add(exit);
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
        outputPanes.addTab("Properties", panelOne());
        outputPanes.addTab("Frequency", panelTwo());
        outputPanes.addTab("Weather", panelThree());
        outputPanes.addTab("Weights", panelFour());
        outputPanes.addTab("Projects", panelFive());
        return outputPanes;
    }

    private JScrollPane panelOne() {
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
        label = new JLabel("Simulation Properties:");
        gridbag.setConstraints(label, c);
        gridPanel.add(label);
        p.add(gridPanel);
        ///////////////////
        gridPanel = new JPanel();
        gridPanel.setLayout(gridbag);
        label = new JLabel("Begin Time: ");
        c.gridwidth = 1;
        gridbag.setConstraints(label,c);
        gridPanel.add(label);
        JTextField tf = new JTextField("YYYY-MM-DDTHH:MM:SS");
        gridbag.setConstraints(tf,c);
        gridPanel.add(tf);
        label = new JLabel("");
        gridbag.setConstraints(label,c);
        gridPanel.add(label);
        label = new JLabel("Log Level");
        gridPanel.add(label);
        gridbag.setConstraints(label, c);
        JComboBox loglevel = new JComboBox();
        loglevel.addItem("CONFIG"); loglevel.addItem("FINE");
        loglevel.addItem("FINER"); loglevel.addItem("FINEST");
        gridbag.setConstraints(loglevel, c);
        gridPanel.add(loglevel);
        label = new JLabel("");
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(label,c);
        gridPanel.add(label);
        label = new JLabel("End Time: ");
        c.gridwidth = 1;
        gridbag.setConstraints(label,c);
        gridPanel.add(label);
        tf = new JTextField("YYYY-MM-DDTHH:MM:SS");
        gridbag.setConstraints(tf,c);
        gridPanel.add(tf);
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
        label = new JLabel("Setup Times:");
        gridbag.setConstraints(label, c);
        gridPanel.add(label);
        c.gridwidth = 1;
        label = new JLabel("Frequency Band");
        gridbag.setConstraints(label,c);
        gridPanel.add(label);
        tf = new JTextField();
        gridbag.setConstraints(tf,c);
        gridPanel.add(tf);
        JComboBox cb = new JComboBox();
        cb.addItem("sec"); cb.addItem("min"); cb.addItem("hour");
        gridbag.setConstraints(cb, c);
        gridPanel.add(cb);
        label = new JLabel("");
        gridbag.setConstraints(label, c);
        gridPanel.add(label);
        label = new JLabel("New Project");
        gridbag.setConstraints(label,c);
        gridPanel.add(label);
        tf = new JTextField();
        gridbag.setConstraints(tf,c);
        gridPanel.add(tf);
        cb = new JComboBox();
        cb.addItem("sec"); cb.addItem("min"); cb.addItem("hour");
        gridbag.setConstraints(cb, c);
        gridPanel.add(cb);
        c.gridwidth = GridBagConstraints.REMAINDER;
        label = new JLabel("");
        gridbag.setConstraints(label, c);
        gridPanel.add(label);
        p.add(gridPanel);
        ///////////////////
        //p.add(new JLabel());
        ///////////////////
        gridPanel = new JPanel();
        gridPanel.setLayout(gridbag);
        c.gridwidth = GridBagConstraints.REMAINDER;
        label = new JLabel("Site Characteristics:");
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
        tf = new JTextField("107.6177275");
        gridbag.setConstraints(tf,c);
        gridPanel.add(tf);
        label = new JLabel("");
        gridbag.setConstraints(label, c);
        gridPanel.add(label);
        label = new JLabel("Latitude");
        gridbag.setConstraints(label, c);
        gridPanel.add(label);
        tf = new JTextField("34.0787491666667");
        gridbag.setConstraints(tf,c);
        gridPanel.add(tf);
        label = new JLabel("");
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(label, c);
        gridPanel.add(label);
        c.gridwidth = 1;
        label = new JLabel("TimeZone");
        gridbag.setConstraints(label,c);
        gridPanel.add(label);
        cb = new GUITimezones();
        gridbag.setConstraints(cb,c);
        gridPanel.add(cb);
        label = new JLabel("");
        gridbag.setConstraints(label, c);
        gridPanel.add(label);
        label= new JLabel("Altitude (meters)");
        gridbag.setConstraints(label, c);
        gridPanel.add(label);
        tf = new JTextField("2124.0");
        gridbag.setConstraints(tf, c);
        gridPanel.add(tf);
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
        tf = new JTextField("8.0");
        gridbag.setConstraints(tf,c);
        gridPanel.add(tf);
        label = new JLabel("");
        gridbag.setConstraints(label,c);
        gridPanel.add(label);
        label = new JLabel("Total Antennas");
        gridbag.setConstraints(label,c);
        gridPanel.add(label);
        cb = new GUIAntennas();
        gridbag.setConstraints(cb,c);
        gridPanel.add(cb);
        c.gridwidth = GridBagConstraints.REMAINDER;
        label = new JLabel("");
        gridbag.setConstraints(label,c);
        gridPanel.add(label);

        p.add(gridPanel);
        ///////////////////
        JScrollPane pane = new JScrollPane(p);
        return pane;
    }
    
    private JComboBox panelTwo_cb;
    private JPanel panelTwo_gridPanel;
    private JPanel panelTwo_mainPanel;
    private JPanel freqFields;

    private JScrollPane panelTwo() { 
        JLabel label;
        panelTwo_mainPanel = new JPanel(new BorderLayout());

        panelTwo_gridPanel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0; c. weighty = 1.0;
        panelTwo_gridPanel.setLayout(gridbag);
        ///////////////////
        label = new JLabel("Frequency Bands");
        c.gridwidth = 1;
        gridbag.setConstraints(label, c);
        panelTwo_gridPanel.add(label);
        label = new JLabel("");
        c.gridwidth = 2;
        gridbag.setConstraints(label, c);
        panelTwo_gridPanel.add(label);
        label = new JLabel("Total Bands");
        c.gridwidth = 1;
        gridbag.setConstraints(label, c);
        panelTwo_gridPanel.add(label);
        panelTwo_cb = new JComboBox();
        panelTwo_cb.addItem("1"); panelTwo_cb.addItem("2"); panelTwo_cb.addItem("3"); 
        panelTwo_cb.addItem("4"); panelTwo_cb.addItem("5"); panelTwo_cb.addItem("6"); 
        panelTwo_cb.addItem("7"); panelTwo_cb.addItem("8"); panelTwo_cb.addItem("9"); 
        panelTwo_cb.addItem("10"); 
        panelTwo_cb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox x = (JComboBox)e.getSource();
                String s = (String)x.getSelectedItem();
                int i = Integer.parseInt(s);
                addFreqFields(i);
                panelTwo_mainPanel.repaint();
                panelTwo_mainPanel.validate();
            }
        });
        gridbag.setConstraints(panelTwo_cb,c);
        panelTwo_gridPanel.add(panelTwo_cb);
        c.gridwidth = GridBagConstraints.REMAINDER;
        label = new JLabel();
        gridbag.setConstraints(label, c);
        panelTwo_gridPanel.add(label);
        panelTwo_mainPanel.add(panelTwo_gridPanel, BorderLayout.NORTH);
        
        addFreqFields(1);
        JScrollPane pane = new JScrollPane(panelTwo_mainPanel);
        return pane;
    }

    private void addFreqFields(int total) {
        JPanel ff_p;
        JLabel ff_l;
        JTextField ff_tf;
        try {
            //System.out.println("Removing!");
            freqFields.removeAll();
        }catch(Exception e) {}
        freqFields = new JPanel(new GridLayout(10,1));
        for(int i=0; i < total; i++) {
            ff_p = new JPanel();
            ff_p.setLayout(new GridLayout(2,6));
            ff_l = new JLabel("Name");      ff_p.add(ff_l);
            ff_l.repaint();
            ff_l.validate();
            //System.out.println(ff_l.getText());
            ff_tf = new JTextField();       ff_p.add(ff_tf);
            ff_l = new JLabel("min");       ff_p.add(ff_l);
            ff_tf = new JTextField();       ff_p.add(ff_tf);
            ff_l = new JLabel("max");       ff_p.add(ff_l);
            ff_tf = new JTextField();       ff_p.add(ff_tf);
            ff_l = new JLabel("");          ff_p.add(ff_l);
            ff_l = new JLabel("");          ff_p.add(ff_l);
            ff_l = new JLabel("Center");    ff_p.add(ff_l);
            ff_tf = new JTextField();       ff_p.add(ff_tf);
            ff_l = new JLabel("Bandwidth"); ff_p.add(ff_l);
            ff_tf = new JTextField();       ff_p.add(ff_tf);
            ff_p.repaint();
            freqFields.add(ff_p);
        }
        panelTwo_mainPanel.add(freqFields, BorderLayout.CENTER);
    }

    /*
    private JPanel createFreqBandInput() {
        JLabel l;
        JTextField tf;
        JPanel p = new JPanel(new GridLayout(2,5));
        l = new JLabel("Band name");    p.add(l);
        tf = new JTextField();          p.add(tf);
        l = new JLabel();               p.add(l);
        l = new JLabel();               p.add(l);
        l = new JLabel();               p.add(l);
        ///////////////
        l = new JLabel();               p.add(l);
        l = new JLabel("Min");          p.add(l);
        tf = new JTextField();          p.add(tf);
        l = new JLabel("Max");          p.add(l);
        tf = new JTextField();          p.add(tf);
        ///////////////
        l = new JLabel();               p.add(l);
        l = new JLabel("Center");       p.add(l);
        tf = new JTextField();          p.add(tf);
        l = new JLabel("Bandwidth");    p.add(l);
        tf = new JTextField();          p.add(tf);
            
        return p;
    }
    */
    private JScrollPane panelThree() {
        JPanel main = new JPanel(new BorderLayout());
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0; c. weighty = 1.0;
        JPanel panelThree_gridPanel = new JPanel();
        panelThree_gridPanel.setLayout(gridbag);
        /////////////////
        JLabel label = new JLabel("Weather Model");
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(label, c);
        panelThree_gridPanel.add(label);
        label = new JLabel("");
        c.gridwidth = 1;
        gridbag.setConstraints(label, c);
        panelThree_gridPanel.add(label);
        label = new JLabel ("Functions");
        gridbag.setConstraints(label, c);
        panelThree_gridPanel.add(label);
        label = new JLabel("");
        c.gridwidth = 2;
        gridbag.setConstraints(label, c);
        panelThree_gridPanel.add(label);
        label = new JLabel("Total");
        c.gridwidth = 1;
        gridbag.setConstraints(label, c);
        panelThree_gridPanel.add(label);
        JComboBox panelThree_cb = new JComboBox();
        panelThree_cb.addItem("1");
        /* panelThree_cb.addItem("2"); panelThree_cb.addItem("3"); 
        panelThree_cb.addItem("4"); panelThree_cb.addItem("5"); panelThree_cb.addItem("6"); 
        panelThree_cb.addItem("7"); panelThree_cb.addItem("8"); panelThree_cb.addItem("9"); 
        panelThree_cb.addItem("10"); */
        panelThree_cb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        });

        gridbag.setConstraints(panelThree_cb,c);
        panelThree_gridPanel.add(panelThree_cb);
        c.gridwidth = GridBagConstraints.REMAINDER;
        label = new JLabel();
        gridbag.setConstraints(label, c);
        panelThree_gridPanel.add(label);
        /////////////////
        main.add(panelThree_gridPanel, BorderLayout.NORTH);
        JScrollPane pane = new JScrollPane(main);
        return pane;
    }
    private JScrollPane panelFour() {
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
        JTextField tf = new JTextField();
        gridbag.setConstraints(tf,c);
        main.add(tf);
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
        tf = new JTextField();
        gridbag.setConstraints(tf,c);
        main.add(tf);
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
        tf = new JTextField();
        gridbag.setConstraints(tf,c);
        main.add(tf);
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
        tf = new JTextField();
        gridbag.setConstraints(tf,c);
        main.add(tf);
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
        tf = new JTextField();
        gridbag.setConstraints(tf,c);
        main.add(tf);
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
        tf = new JTextField();
        gridbag.setConstraints(tf,c);
        main.add(tf);
        l = new JLabel();
        c.gridwidth= GridBagConstraints.REMAINDER;
        gridbag.setConstraints(l,c);
        main.add(l);
        l = new JLabel();
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        main.add(l);
        l = new JLabel("New Project");
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        main.add(l);
        tf = new JTextField();
        gridbag.setConstraints(tf,c);
        main.add(tf);
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
        tf = new JTextField();
        gridbag.setConstraints(tf,c);
        main.add(tf);
        l = new JLabel();
        c.gridwidth= GridBagConstraints.REMAINDER;
        gridbag.setConstraints(l,c);
        main.add(l);
        ///////////////////
        JScrollPane pane = new JScrollPane(main);
        return pane;
    }
    private JScrollPane panelFive() {
        JPanel p = new JPanel();
        JScrollPane pane = new JScrollPane(p);
        return pane;
    }
    

    public void exit() {
        dispose();
    }
}
