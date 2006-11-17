package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.awt.*;
import java.util.Vector;
import java.util.EventListener;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.logging.Logger;
import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.SBLite;
import alma.scheduling.ProjectLite;

public class ArchiveSearchFieldsPanel extends JPanel {
    private JButton searchB;
    private JButton clearB;
    private JCheckBox projectCB;
    private JCheckBox sbCB;
    private JTextField piNameTF;
    private JTextField projNameTF;
    private JComboBox projTypeChoices;
    private JComboBox sbModeNameChoices;
    private JComboBox sbModeTypeChoices;
    private JTextField expertQueryTF;
    private boolean connectedToALMA;
    private JPanel parent;
    private ArchiveSearchController controller;
    private boolean searchingOnProject;
    
    public ArchiveSearchFieldsPanel(){
        setLayout(new BorderLayout());
        createTextFields();
        createCheckBoxes();//need to do this 2nd coz other fields will be null when used
        controller = null;
        connectedToALMA= false;
        searchingOnProject=true;
    }

    public void setCS(PluginContainerServices cs) {
        controller = new ArchiveSearchController(cs);
        ((SearchArchiveOnlyTab)parent).setSearchMode(searchingOnProject);
    }
    public void connected(boolean x){
        connectedToALMA=x;
    }
    public void setOwner(JPanel p){
        parent = p;
    }

    public void setPanelEnabled(boolean b){
        projectCB.setEnabled(b);
        sbCB.setEnabled(b);
        sbModeNameChoices.setEnabled(b);
        sbModeTypeChoices.setEnabled(b);
        projTypeChoices.setEnabled(b);
        projNameTF.setEnabled(b);
        piNameTF.setEnabled(b);
        searchB.setEnabled(b);
        clearB.setEnabled(b);
    }

    private void createCheckBoxes(){ 
        JPanel p1 = new JPanel();
        JLabel l = new JLabel("Projects:");
        projectCB = new JCheckBox("",true);
        searchingOnProject = true;
        projectCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                projectCB.setSelected(true);
                searchingOnProject = true;
                ((SearchArchiveOnlyTab)parent).setSearchMode(searchingOnProject);
                if(sbCB.isSelected()){
                    sbCB.setSelected(false);
                } 
            }
        });
        p1.add(l); 
        p1.add(projectCB);
        JPanel p2 = new JPanel();
        l = new JLabel("SBs:");
        sbCB = new JCheckBox("",false);
        sbModeNameChoices.setEnabled(true);
        sbModeTypeChoices.setEnabled(true);
        sbCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sbCB.setSelected(true);
                searchingOnProject = false;
                ((SearchArchiveOnlyTab)parent).setSearchMode(searchingOnProject);
                if(projectCB.isSelected()){
                    projectCB.setSelected(false);
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
        piNameTF = new JTextField("*",2);
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
        sbModeNameChoices = new JComboBox(foo);
        sbModeNameChoices.setSelectedIndex(0);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(sbModeNameChoices,c);
        p.add(sbModeNameChoices);
        ////
        l = new JLabel("Project Name");
        c.gridwidth =1;
        gridbag.setConstraints(l,c);
        p.add(l);
        projNameTF = new JTextField("*",3);
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
        String[] modeTypeChoices = {"All","Observer","Observatory","Expert"}; 
        sbModeTypeChoices = new JComboBox(modeTypeChoices);
        sbModeTypeChoices.setSelectedIndex(0);
        c.gridwidth =GridBagConstraints.REMAINDER;
        gridbag.setConstraints(sbModeTypeChoices,c);
        p.add(sbModeTypeChoices);
        ////
        l = new JLabel("Project Type");
        c.gridwidth =1;
        gridbag.setConstraints(l,c);
        p.add(l);
        String[] projectTypes = {"All","Continuum","Polarization","Other"};
        projTypeChoices = new JComboBox(projectTypes);
        projTypeChoices.setSelectedIndex(0);
        projTypeChoices.setEnabled(true); 
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
                if(!connectedToALMA){
                    return;
                }
                if(controller == null) {
                    return;     
                }
                String name =parent.getClass().getName();
                if(name.contains("SearchArchiveOnlyTab")){
                    ((SearchArchiveOnlyTab)parent).clearTables();
                }
                SPSearchArchiveThread foo = new SPSearchArchiveThread();
                Thread t = new Thread(foo);
                t.start();
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
                sbModeNameChoices.setSelectedItem(0);
                sbModeTypeChoices.setSelectedItem(0);
                //when there is expert do clear there
                //expertQueryTF.setText("");
                String name =parent.getClass().getName();
                if(name.contains("SearchArchiveOnlyTab")){
                    ((SearchArchiveOnlyTab)parent).clearTables();
                }
            }
        });
        clearB.setToolTipText("Click here to clear text fields");
        bp.add(clearB);
        
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(bp,c);
        p.add(bp);
        add(p, BorderLayout.CENTER);
    }

    //////////////////////////////////////
    /// SB Stuff Below
    //////////////////////////////////////
        
    public String makeSBQuery(){
        //access to pi name, sb type, sb mode
        String sbModeType =(String) sbModeTypeChoices.getSelectedItem();
        String sbModeName = (String)sbModeNameChoices.getSelectedItem(); //correspondstypename of mode
        System.out.println(sbModeType +" = type");
        System.out.println(sbModeName +" = name");
        String query;
        if(sbModeName.equals("All") && sbModeType.equals("All") )  {
            query="/*";
        } else if(sbModeType.equals("All") ) {
            //mode == all so serching by type only
            query="/sbl:SchedBlock[sbl:modeName=\""+sbModeName+"\"]";
        } else if(sbModeName.equals("All")) {
            //type == all so serching by mode only
            query="/sbl:SchedBlock[sbl:modeType=\""+sbModeType+"\"]";
        } else {
            //searching with both!
            query = "/sbl:SchedBlock[sbl:modeType=\""+
                        sbModeType+"\" and sbl:modeName=\""+sbModeName+"\"]";
        }
        System.out.println("sb query = "+query);
        return query;
    }

    public void displaySBResults(SBLite[] results){
        String name =parent.getClass().getName();
        //System.out.println("Parent class = "+name);
        if(name.contains("SearchArchiveOnlyTab")){
            ((SearchArchiveOnlyTab)parent).updateSBView(results);
        } else if(name.contains("InteractiveSchedTab")){
            ((InteractiveSchedTab)parent).updateSBView(results);
        } else if(name.contains("QueuedSchedTab")){
            ((InteractiveSchedTab)parent).updateSBView(results);
        }
    }
    
    //////////////////////////////////////
    /// Project Stuff Below
    //////////////////////////////////////
    
    public void displayProjectResults(ProjectLite[] results){
        String name =parent.getClass().getName();
        //System.out.println("Parent class = "+name);
        if(name.contains("SearchArchiveOnlyTab")){
            ((SearchArchiveOnlyTab)parent).updateProjectView(results);
        } else if(name.contains("InteractiveSchedTab")){
            ((InteractiveSchedTab)parent).updateProjectView(results);
        } else if(name.contains("QueuedSchedTab")){
            ((InteractiveSchedTab)parent).updateProjectView(results);
        }
    }

    //////////////////////////////////////
    // Search Thread
    //////////////////////////////////////
    class SPSearchArchiveThread implements Runnable {
        public SPSearchArchiveThread (){
        }
        public void run(){
            String pi = piNameTF.getText();
            String pName = projNameTF.getText();
            String type = (String)projTypeChoices.getSelectedItem();
            //if we know its for all SBs ignore it
            String sbquery = makeSBQuery();
            //returns a vector, first item will be matching projects
            //second item will be matching sbs.
            Vector res = controller.doQuery(sbquery, pName, pi, type);
            if(projectCB.isSelected() ){
                displayProjectResults((ProjectLite[])res.elementAt(0));
            } else if(sbCB.isSelected()){
                displaySBResults((SBLite[])res.elementAt(1));
            } else {
            //shouldnt have happened
                System.out.println("both of the CBs aren't selected!");
            }
        }
    }
}
