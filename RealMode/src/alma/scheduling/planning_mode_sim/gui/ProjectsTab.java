package alma.scheduling.planning_mode_sim.gui;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.util.Vector;
import java.util.StringTokenizer;

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;


public class ProjectsTab extends JScrollPane {

    private Vector v;
    private int totalProjects = 0;
    private JPanel panelFive_main;
    private JTabbedPane projectPane;
    private JTextField totalproj_tf;

    public ProjectsTab() {
        super();
        setViewportView(createView());
    }

    public JPanel createView() {

        projectPane = new JTabbedPane();
        /*
        projectPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JTabbedPane pane = (JTabbedPane)e.getSource();
                int i = pane.getSelectedIndex();
                JScrollPane scroll = (JScrollPane)pane.getComponent(i);
                pane.setSelectedComponent(scroll);
            }
        });*/
        panelFive_main = new JPanel(new BorderLayout());
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0; c. weighty = 1.0;
        ///////////////////
        JPanel header = new JPanel(gridbag);
        JLabel l = new JLabel();
        l = new JLabel("Project Specifications");
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(l,c);
        header.add(l);
        l = new JLabel();
        c.gridwidth =1;
        gridbag.setConstraints(l,c);
        header.add(l);
        l = new JLabel("Total projects");
        gridbag.setConstraints(l,c);
        header.add(l);
        totalproj_tf = new JTextField("Enter # of projects");
        totalproj_tf.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                JTextField tf = (JTextField)e.getSource();
                String s = tf.getText();
                int i;
                if(s == null || s.equals("") 
                  || s.equals("Enter # of projects") 
                  || s.equals("Enter a number!")) {

                    tf.setText("Enter a number!");
                }else {
                    i = Integer.parseInt(s);
                    try {
                        projectPane.removeAll();
                    }catch(Exception ex) {}
                    for(int x=0; x< i; x++) {
                        projectPane.add("Project "+(x+1),addProjectTab());
                    }
                    panelFive_main.add(projectPane, BorderLayout.CENTER);
                    panelFive_main.validate();
                    panelFive_main.getParent().validate();
                }
            }
        });
        gridbag.setConstraints(totalproj_tf,c);
        //tf.selectAll();
        header.add(totalproj_tf);
        l = new JLabel();
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(l,c);
        header.add(l);
        ///////////////////
        panelFive_main.add(header, BorderLayout.NORTH);
        return panelFive_main;
    }
    
    private JScrollPane addProjectTab() {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0; c.weighty = 1.0;
        JPanel main = new JPanel(new BorderLayout());
        JPanel p = new JPanel(gridbag);
        /////////////////////
        JLabel l; JTextField tf;
        c.gridwidth=1;
        l = new JLabel("Project Name");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField("");
        gridbag.setConstraints(tf,c);
        p.add(tf);
        l = new JLabel("PI name");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField("");
        gridbag.setConstraints(tf,c);
        p.add(tf);
        l = new JLabel("Priority");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField("");
        gridbag.setConstraints(tf,c);
        p.add(tf);
        l = new JLabel("# of Sets");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField("");
        tf.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JTextField tf = (JTextField)e.getSource();
                String s = tf.getText();
                int sets = Integer.parseInt(s);
                JPanel p1 = (JPanel)tf.getParent();
                JPanel projPanel = (JPanel)p1.getParent();
                try {
                    projPanel.remove(1);
                } catch(Exception ex) {}
                JTabbedPane setsPane = new JTabbedPane();
                for(int i=0; i < sets; i++) {
                    setsPane.addTab("Set "+(i+1), addSetPanel());
                }
                projPanel.add(setsPane);
                projPanel.validate();
                
            }
        });
        c.gridwidth= GridBagConstraints.REMAINDER;
        gridbag.setConstraints(tf,c);
        p.add(tf);
        main.add(p, BorderLayout.NORTH);
        /////////////////////
        JScrollPane pane = new JScrollPane(main);
        return pane;
    }

    public JScrollPane addSetPanel() {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0; c.weighty = 1.0;
        JPanel main = new JPanel(new BorderLayout());
        JPanel p = new JPanel(gridbag);
        ///////////////////
        JLabel l; JTextField tf;
        c.gridwidth = 1;
        l = new JLabel("Name of Set");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField();
        gridbag.setConstraints(tf,c);
        p.add(tf);
        l = new JLabel();
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(l,c);
        p.add(l);
        l= new JLabel("Frequency Band");
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField();
        gridbag.setConstraints(tf,c);
        p.add(tf);
        l= new JLabel("Frequency");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField();
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(tf,c);
        p.add(tf);  
        l= new JLabel("Weather Condition");
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        p.add(l);
        JComboBox cb = new JComboBox();
        cb.addItem("Exceptional");
        cb.addItem("Excellent");
        cb.addItem("Good");
        cb.addItem("Average");
        cb.addItem("Below Ave.");
        cb.addItem("Poor");
        cb.addItem("Dismal");
        cb.addItem("Any");
        cb.setSelectedItem("Any");
        gridbag.setConstraints(cb,c);
        p.add(cb);
        l= new JLabel("# of Targets");
        c.gridwidth = GridBagConstraints.RELATIVE;
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField();
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(tf,c);
        tf.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JTextField tf = (JTextField)e.getSource();
                String s = tf.getText();
                int targets = Integer.parseInt(s);
                JPanel p1 = (JPanel)tf.getParent(); //main panel
                JPanel tmp1 = (JPanel)p1.getParent();
                try {
                    tmp1.remove(1);
                } catch(Exception ex) {}
                JPanel p2 = new JPanel(new GridLayout(targets,1));
                JPanel tmp;
                for(int i=0; i < targets; i++) {
                    tmp = addTargetsField();
                    p2.add(tmp);
                }
                tmp1.add(p2);
                tmp1.getParent().validate();
            }
        });
        p.add(tf);  
        l = new JLabel("-----");
        gridbag.setConstraints(l,c);
        p.add(l);
        
        main.add(p, BorderLayout.NORTH);
        ///////////////////
        JScrollPane pane = new JScrollPane(main);
        return pane;
        
    }

    private JPanel addTargetsField() {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0; //c.weighty = 1.0;
        JPanel p = new JPanel(gridbag);
        JLabel l = new JLabel();
        /*
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(l,c);
        p.add(l);
        */
        //////////////////
        c.gridwidth = 1;
        l =new JLabel("-- Target Name");
        gridbag.setConstraints(l,c);
        p.add(l);
        JTextField tf = new JTextField("");
        gridbag.setConstraints(tf,c);
        p.add(tf);
        l = new JLabel("RA");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField("");
        gridbag.setConstraints(tf,c);
        p.add(tf);
        l = new JLabel("DEC");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField("");
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(tf,c);
        p.add(tf);
        ////////////////////////
        l = new JLabel();
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        p.add(l);
        l = new JLabel();
        gridbag.setConstraints(l,c);
        p.add(l);
        l = new JLabel("Freq.");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField("");
        gridbag.setConstraints(tf,c);
        p.add(tf);
        l = new JLabel("Total Time");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField("");
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(tf,c);
        p.add(tf);
        ////////////////////////
        l = new JLabel();
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        p.add(l);
        l = new JLabel();
        gridbag.setConstraints(l,c);
        p.add(l);
        l = new JLabel("Weather");
        gridbag.setConstraints(l,c);
        p.add(l);
        JComboBox cb = new JComboBox();
        cb.addItem("Exceptional");
        cb.addItem("Excellent");
        cb.addItem("Good");
        cb.addItem("Average");
        cb.addItem("Below Ave.");
        cb.addItem("Poor");
        cb.addItem("Dismal");
        cb.addItem("Any");
        cb.setSelectedItem("Any");
        gridbag.setConstraints(cb,c);
        p.add(cb);
        l = new JLabel("Repeat Count");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField("");
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(tf,c);
        p.add(tf);
        return p;
    }

    //////////////////////////////////////////////////////
    // Get Methods
    //////////////////////////////////////////////////////
    
    
    public Vector getProjects() {
        Vector v = new Vector(); //projects vector
        try {
            totalProjects = projectPane.getTabCount();
            for(int i=0; i < totalProjects; i++) {
                Component scrollPane = projectPane.getComponentAt(i);
                Component[] tabPane = ((JScrollPane)scrollPane).getComponents();
                Component[] tabPaneComps = ((JViewport)tabPane[0]).getComponents();
                JPanel mainPanel = (JPanel)tabPaneComps[0];
                Component[] mainPanelComps = mainPanel.getComponents();
                JPanel projectInfoPanel = (JPanel)mainPanelComps[0];
                JPanel targetsInfoPanel = (JPanel)mainPanelComps[1];

                Component[] projectInfo = projectInfoPanel.getComponents();
                v.add( ((JTextField)projectInfo[1]).getText() );//project name
                v.add( ((JTextField)projectInfo[3]).getText() ); //PI
                v.add( ((JTextField)projectInfo[5]).getText() ); //priority
                v.add( ((JTextField)projectInfo[7]).getText() ); //number of targets
                
                Component[] targetsInfo = targetsInfoPanel.getComponents();
                v.add(getTargetsInfo(targetsInfo));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return v;
    }

    public Vector getTargetsInfo(Component[] comp) throws Exception {
        int totaltargets = comp.length;
        Vector targets = new Vector(totaltargets);
        JPanel p;
        Component[] c;
        String tmp;
        for(int i=0; i < totaltargets; i++ ) {
            p = (JPanel)comp[i];
            c = p.getComponents();
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

    public int getTotalProjectCount() {
        return totalProjects;
    }




    
    //////////////////////////////////////////////////////
    // Set Methods
    //////////////////////////////////////////////////////

    public void setTotalProjects(String s) {
        totalProjects = Integer.parseInt(s.trim());
        totalproj_tf.setText(s);
    }

    
    //////////////////////////////////////////////////////

    public void loadValuesFromFile(Vector values) {
        v = values;
        setTotalProjects((String)v.elementAt(0));
        v.removeElementAt(0);
        try {
            projectPane.removeAll();
        }catch(Exception ex) {}
        
        int counter = v.size();
        String s, sets="", targets="";
        StringTokenizer token, tmp, tmp2;
        int totalproj=1,totalSets=0, totaltargets=0, i=0;
        Vector projTargets, projSets;
        while(counter > 0) {
            s = (String)v.elementAt(i++);
            counter--;
            //System.out.println(s);
            token = new StringTokenizer(s,";");
            tmp = new StringTokenizer(s,";");
            
            projectPane.add("Project "+totalproj, updateProjectTab(token));//, projTargets));
            totalproj++;
            
            while(tmp.hasMoreTokens()){
                sets = tmp.nextToken();
            }

            totalSets = Integer.parseInt(sets.trim());
            projSets = new Vector(totalSets);
            for(int t=0; t < totalSets; t++) {
                s = (String)v.elementAt(i++);
                counter--;
                JScrollPane projectTab = (JScrollPane)projectPane.getComponentAt(totalproj-2); //coz totalproj starts at 1 and has already been incremented & getComponent starts at 0.
                System.out.println(projectTab.getClass().getName());
                projSets.add(s);
                updateSetsTab(s, (t+1), projectTab);
                tmp2 = new StringTokenizer(s,";");
                while(tmp2.hasMoreElements()) {
                    targets = tmp2.nextToken();
                }
                totaltargets = Integer.parseInt(targets.trim());
                for(int k=0; k< totaltargets; k++) {
                    //get target
                    s = (String)v.elementAt(i++);
                    counter--;
                }
            }
            
        }
        panelFive_main.add(projectPane, BorderLayout.CENTER);
    }

    public void updateSetsTab(String setString, int setNum, JScrollPane projectTab) {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0; c.weighty = 1.0;
        JPanel main = new JPanel(new BorderLayout());
        JPanel p = new JPanel(gridbag);
        ///////////////////
        JLabel l; JTextField tf;
        c.gridwidth = 1;
        l = new JLabel("Name of Set");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField();
        gridbag.setConstraints(tf,c);
        p.add(tf);
        l = new JLabel();
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(l,c);
        p.add(l);
        l= new JLabel("Frequency Band");
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField();
        gridbag.setConstraints(tf,c);
        p.add(tf);
        l= new JLabel("Frequency");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField();
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(tf,c);
        p.add(tf);  
        l= new JLabel("Weather Condition");
        c.gridwidth = 1;
        gridbag.setConstraints(l,c);
        p.add(l);
        JComboBox cb = new JComboBox();
        cb.addItem("Exceptional");
        cb.addItem("Excellent");
        cb.addItem("Good");
        cb.addItem("Average");
        cb.addItem("Below Ave.");
        cb.addItem("Poor");
        cb.addItem("Dismal");
        cb.addItem("Any");
        cb.setSelectedItem("Any");
        gridbag.setConstraints(cb,c);
        p.add(cb);
        l= new JLabel("# of Targets");
        c.gridwidth = GridBagConstraints.RELATIVE;
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField();
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(tf,c);
        p.add(tf);
        main.add(p);
        projectTab.add(main);
        projectTab.getParent().validate();
    }


    public JScrollPane updateProjectTab(StringTokenizer token) {//, Vector t) {
        
        //StringTokenizer token = new StringTokenizer(s,";");
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0; c.weighty = 1.0;
        JPanel main = new JPanel(new BorderLayout());
        JPanel p = new JPanel(gridbag);
        /////////////////////
        JLabel l; JTextField tf;
        c.gridwidth=1;
        l = new JLabel("Project Name");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField((token.nextToken()).trim());
        gridbag.setConstraints(tf,c);
        p.add(tf);
        l = new JLabel("PI name");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField((token.nextToken()).trim());
        gridbag.setConstraints(tf,c);
        p.add(tf);
        l = new JLabel("Priority");
        gridbag.setConstraints(l,c);
        p.add(l);
        tf = new JTextField((token.nextToken()).trim());
        gridbag.setConstraints(tf,c);
        p.add(tf);
        l = new JLabel("# of Sets");
        gridbag.setConstraints(l,c);
        p.add(l);
        String sets = token.nextToken();
        int setscount = Integer.parseInt(sets.trim());
        tf = new JTextField(sets.trim());
        c.gridwidth= GridBagConstraints.REMAINDER;
        gridbag.setConstraints(tf,c);
        p.add(tf);

        main.add(p, BorderLayout.NORTH);
        
        //main.add(updateTargetsField(targetcount, t));

        JScrollPane pane = new JScrollPane(main);
        return pane;
    }

    public JPanel updateTargetsField(int targetSize, Vector targets) {
        if(targetSize != targets.size()) {
            System.out.println("target sizes don't match!");
        }
        JPanel targetPanel = new JPanel( new GridLayout(targetSize, 1));
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        StringTokenizer token;
        // Do this for each target/line in vector.
        for(int i=0; i < targetSize; i++) {
            
            token = new StringTokenizer((String)targets.elementAt(i), ";");
            
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0; //c.weighty = 1.0;
            JPanel p = new JPanel(gridbag);
            JLabel l = new JLabel();
            //////////////////
            c.gridwidth = 1;
            l =new JLabel("-- Target Name");
            gridbag.setConstraints(l,c);
            p.add(l);
            JTextField tf = new JTextField((token.nextToken()).trim());
            gridbag.setConstraints(tf,c);
            p.add(tf);
            l = new JLabel("RA");
            gridbag.setConstraints(l,c);
            p.add(l);
            tf = new JTextField((token.nextToken()).trim());
            gridbag.setConstraints(tf,c);
            p.add(tf);
            l = new JLabel("DEC");
            gridbag.setConstraints(l,c);
            p.add(l);
            tf = new JTextField((token.nextToken()).trim()); 
            c.gridwidth = GridBagConstraints.REMAINDER; 
            gridbag.setConstraints(tf,c); 
            p.add(tf);
            ////////////////////////
            l = new JLabel();
            c.gridwidth = 1;
            gridbag.setConstraints(l,c);
            p.add(l);
            l = new JLabel();
            gridbag.setConstraints(l,c);
            p.add(l);
            l = new JLabel("Freq.");
            gridbag.setConstraints(l,c);
            p.add(l);
            tf = new JTextField((token.nextToken()).trim());
            gridbag.setConstraints(tf,c);
            p.add(tf);
            l = new JLabel("Total Time");
            gridbag.setConstraints(l,c);
            p.add(l);
            tf = new JTextField((token.nextToken()).trim());
            c.gridwidth = GridBagConstraints.REMAINDER;
            gridbag.setConstraints(tf,c);
            p.add(tf);
            ////////////////////////
            l = new JLabel();
            c.gridwidth = 1;
            gridbag.setConstraints(l,c);
            p.add(l);
            l = new JLabel();
            gridbag.setConstraints(l,c);
            p.add(l);
            l = new JLabel("Weather");
            gridbag.setConstraints(l,c);
            p.add(l);
            JComboBox cb = new JComboBox();
            cb.addItem("Exceptional");
            cb.addItem("Excellent");
            cb.addItem("Good");
            cb.addItem("Average");
            cb.addItem("Below Ave.");
            cb.addItem("Poor");
            cb.addItem("Dismal");
            cb.addItem("Any");
            cb.setSelectedItem(getWeatherCondition((token.nextToken()).trim()));
            gridbag.setConstraints(cb,c);
            p.add(cb);
            l = new JLabel("Repeat Count");
            gridbag.setConstraints(l,c);
            p.add(l);
            try {
                tf = new JTextField((token.nextToken()).trim());
            } catch(Exception e) {
                tf = new JTextField("");
            }
            c.gridwidth = GridBagConstraints.REMAINDER;
            gridbag.setConstraints(tf,c);
            p.add(tf);
            targetPanel.add(p);
        }
        //
        return targetPanel;

    }

    public String getWeatherCondition(String s) {

        if(s.equals("exceptional") ){
            return "Exceptional";
        } else if(s.equals("excellent")) {
            return "Excellent";
        } else if(s.equals("good")) {
            return "Good";
        } else if(s.equals("average")) {
            return "Average";
        } else if(s.equals("below average")) {
            return "Below Ave.";
        } else if(s.equals("poor")) {
            return "Poor";
        } else if(s.equals("dismal")) {
            return "Dismal";
        } else if(s.equals("any")) {
            return "Any";   
        } else {
            return "Any";   
        }
    }
    
}
