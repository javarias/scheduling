package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.awt.*;
import java.util.EventListener;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.logging.Logger;
import alma.exec.extension.subsystemplugin.PluginContainerServices;

public class ArchiveSearchFieldsPanel extends JPanel {
    private JButton searchB;
    private JButton clearB;
    private JCheckBox projectCB;
    private JCheckBox sbCB;
    private JTextField piNameTF;
    private JTextField projNameTF;
    private JComboBox projTypeChoices;
    private JComboBox sbTypeChoices;
    private JComboBox sbModeChoices;
    private JTextField expertQueryTF;

    private ArchiveSearchController controller;
    
    public ArchiveSearchFieldsPanel(){
        setLayout(new BorderLayout());
        createCheckBoxes();
        createTextFields();
        controller = null;
    }

    public void setCS(PluginContainerServices cs) {
        controller = new ArchiveSearchController(cs);
    }

    private void createCheckBoxes(){ 
        JPanel p1 = new JPanel();
        JLabel l = new JLabel("Projects:");
        projectCB = new JCheckBox("",true);
        projectCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                projectCB.setSelected(true);
                if(sbCB.isSelected()){
                    sbCB.setSelected(false);
                    sbTypeChoices.setEnabled(false);
                    sbModeChoices.setEnabled(false);
                } 
            }
        });
        p1.add(l); 
        p1.add(projectCB);
        JPanel p2 = new JPanel();
        l = new JLabel("SBs:");
        sbCB = new JCheckBox("",false);
        sbCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sbCB.setSelected(true);
                if(projectCB.isSelected()){
                    projectCB.setSelected(false);
                    projTypeChoices.setEnabled(false);
                    projNameTF.setEnabled(false);
                } 
            }
        });
        p2.add(l);
        p2.add(sbCB);
        JPanel p = new JPanel();
        p.add(p1);
        p.add(p2);
        add(p,BorderLayout.NORTH);
    }

    private void createTextFields() {
        JLabel sep;
        JLabel l;
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0; c.weighty = 1.0;

        JPanel p = new JPanel(gridbag);
        l = new JLabel("PI Name");
        c.gridwidth =1;
        gridbag.setConstraints(l,c);
        p.add(l);
        piNameTF = new JTextField(2);
        c.gridwidth =1;
        gridbag.setConstraints(piNameTF,c);
        p.add(piNameTF);
        ////
        sep = new JLabel();
        c.gridwidth =1;
        gridbag.setConstraints(sep,c);
        p.add(sep);
        ////
        l = new JLabel("SB Type");
        c.gridwidth =1;
        gridbag.setConstraints(l,c);
        p.add(l);
        String[] foo= {"All","SingleFieldInterferometry","OpticalPointing","TowerHolography"};
        sbTypeChoices = new JComboBox(foo);
        sbTypeChoices.setSelectedIndex(0);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(sbTypeChoices,c);
        p.add(sbTypeChoices);
        ////
        l = new JLabel("Project Name");
        c.gridwidth =1;
        gridbag.setConstraints(l,c);
        p.add(l);
        projNameTF = new JTextField(3);
        c.gridwidth =1;
        gridbag.setConstraints(projNameTF,c);
        p.add(projNameTF);
        ////
        sep = new JLabel();
        c.gridwidth =1;
        gridbag.setConstraints(sep,c);
        p.add(sep);
        ////
        l = new JLabel("SB Mode");
        c.gridwidth =1;
        gridbag.setConstraints(l,c);
        p.add(l);
        //TODO check if OT has this as something i can import
        String[] modeChoices = {"All","Observer","Observatory","Expert"}; 
        sbModeChoices = new JComboBox(modeChoices);
        sbModeChoices.setSelectedIndex(0);
        c.gridwidth =GridBagConstraints.REMAINDER;
        gridbag.setConstraints(sbModeChoices,c);
        p.add(sbModeChoices);
        ////
        l = new JLabel("Project Type");
        c.gridwidth =1;
        gridbag.setConstraints(l,c);
        p.add(l);
        String[] projectTypes = {"All","Continuum","Polarization","Other"};
        projTypeChoices = new JComboBox(projectTypes);
        projTypeChoices.setSelectedIndex(0);
        c.gridwidth =1;
        gridbag.setConstraints(projTypeChoices,c);
        p.add(projTypeChoices);
        ////
        sep = new JLabel();
        c.gridwidth =1;
        gridbag.setConstraints(sep,c);
        p.add(sep);
        ////
        // TODO Expert search mode later
        ///
        /*
        l = new JLabel("Expert Search");
        c.gridwidth =1;
        gridbag.setConstraints(l,c);
        p.add(l);
        expertQueryTF = new JTextField();
        c.gridwidth =1;
        gridbag.setConstraints(expertQueryTF,c);
        p.add(expertQueryTF);
        */
        ////
        JPanel bp = new JPanel(new GridLayout(1,2));
        searchB = new JButton("Search");
        searchB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                if(controller == null) {
                    return;     
                }
                if(projectCB.isSelected() ){
                    makeProjectQuery();
                } else if(sbCB.isSelected()){
                    makeSBQuery();
                } else {
                //shouldnt have happened
                    System.out.println("both of the CBs aren't selected!");
                }
            }
        });
        searchB.setToolTipText("Click here to search archive.");
        bp.add(searchB);
        clearB = new JButton("Clear");
        clearB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                piNameTF.setText("");
                projNameTF.setText("");
                projTypeChoices.setSelectedItem(0);
                sbTypeChoices.setSelectedItem(0);
                sbModeChoices.setSelectedItem(0);
                //when there is expert do clear there
                //expertQueryTF.setText("");
            }
        });
        clearB.setToolTipText("Click here to clear text fields");
        bp.add(clearB);
        
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(bp,c);
        p.add(bp);
        add(p, BorderLayout.CENTER);
    }

        
    public String makeSBQuery(){
        //access to pi name, sb type, sb mode
        String query="";
        return query;
    }

    public String makeProjectQuery() {
        //access to pi name, project name, proj type
        String query="";
        return query;
    }

    
}
