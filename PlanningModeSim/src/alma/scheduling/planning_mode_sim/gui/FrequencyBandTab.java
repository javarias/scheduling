package alma.scheduling.planning_mode_sim.gui;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JComboBox;


public class FrequencyBandTab extends JScrollPane {

    private JComboBox totalFrequencies;
    private JPanel panelTwo_gridPanel;
    private JPanel panelTwo_mainPanel;
    private JPanel freqFields;


    public FrequencyBandTab() {
        super();
        setViewportView(createView());
    }

    public JPanel createView() { 
        freqFields = new JPanel(new GridLayout(10,1));
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
        totalFrequencies = new JComboBox();
        totalFrequencies.addItem("1"); totalFrequencies.addItem("2"); totalFrequencies.addItem("3"); 
        totalFrequencies.addItem("4"); totalFrequencies.addItem("5"); totalFrequencies.addItem("6"); 
        totalFrequencies.addItem("7"); totalFrequencies.addItem("8"); totalFrequencies.addItem("9"); 
        totalFrequencies.addItem("10"); 
        totalFrequencies.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox x = (JComboBox)e.getSource();
                String s = (String)x.getSelectedItem();
                int i = Integer.parseInt(s);
                addFreqFields(i);
                panelTwo_mainPanel.repaint();
                panelTwo_mainPanel.validate();
                panelTwo_mainPanel.getParent().validate();
            }
        });
        gridbag.setConstraints(totalFrequencies,c);
        panelTwo_gridPanel.add(totalFrequencies);
        c.gridwidth = GridBagConstraints.REMAINDER;
        label = new JLabel();
        gridbag.setConstraints(label, c);
        panelTwo_gridPanel.add(label);
        panelTwo_mainPanel.add(panelTwo_gridPanel, BorderLayout.NORTH);
        
        addFreqFields(1);
        return panelTwo_mainPanel;
    }


   private void addFreqFields(int total) {
        JPanel ff_p;
        JLabel ff_l;
        JTextField ff_tf;
        try {
            freqFields.removeAll();
        }catch(Exception e) {}
        for(int i=0; i < total; i++) {
            ff_p = new JPanel();
            ff_p.setLayout(new GridLayout(2,6));
            ff_l = new JLabel("Name");      ff_p.add(ff_l);
            ff_tf = new JTextField("");     ff_p.add(ff_tf);
            ff_l = new JLabel("min");       ff_p.add(ff_l);
            ff_tf = new JTextField("");     ff_p.add(ff_tf);
            ff_l = new JLabel("max");       ff_p.add(ff_l);
            ff_tf = new JTextField("");     ff_p.add(ff_tf);
            ff_l = new JLabel("");          ff_p.add(ff_l);
            ff_l = new JLabel("");          ff_p.add(ff_l);
            ff_l = new JLabel("Center");    ff_p.add(ff_l);
            ff_tf = new JTextField("");     ff_p.add(ff_tf);
            ff_l = new JLabel("Bandwidth"); ff_p.add(ff_l);
            ff_tf = new JTextField("");     ff_p.add(ff_tf);
            ff_p.repaint();
            freqFields.add(ff_p);
        }
        panelTwo_mainPanel.add(freqFields, BorderLayout.CENTER);
    }

    /////////////////////////////
    // Get Methods
    /////////////////////////////

    public String getTotalFreq() {
        return (String)totalFrequencies.getSelectedItem();
    }

    /** 
     * Will return a vector full of vectors. Each entry in the 
     * main vector will represent one frequency band. The vector 
     * which is contained in that entry is the values associated
     * with that particular frequency band.
     */
    public Vector getFrequencyValues() {
        Vector v = new Vector(); 
        Component[] ff_comps = freqFields.getComponents();
        int total = Integer.parseInt(getTotalFreq());
        for (int i = 0; i < total; i++) {
            Vector tmp = new Vector();
            JPanel ffp = (JPanel)ff_comps[i];
            Component[] ffp_comps = ffp.getComponents();
            tmp.add(((JTextField)ffp_comps[1]).getText());
            tmp.add(((JTextField)ffp_comps[3]).getText());
            tmp.add(((JTextField)ffp_comps[5]).getText());
            tmp.add(((JTextField)ffp_comps[9]).getText());
            tmp.add(((JTextField)ffp_comps[11]).getText());
            v.add(tmp);
        }
        return v;
        
    }
    
    /////////////////////////////
    // Set Methods
    /////////////////////////////
    

    public void setTotalFreq(String s) {
        int total = Integer.parseInt(s);
        if ( total > 10 || total < 1 ) {
            System.out.println("Frequency band limit between 1 - 10. ");
        } else {
            totalFrequencies.setSelectedItem(s);
            //addFreqFields(total);
        }
    }
    
    public void setFrequencyValues(int total, Vector v) {
        JPanel ff_p;
        JLabel ff_l;
        JTextField ff_tf;
        int index=0;
        String min, max;
        try {
            freqFields.removeAll();
        }catch(Exception e) {}
        for(int i=0; i < total; i++) {
            ff_p = new JPanel();
            ff_p.setLayout(new GridLayout(2,6));
            ff_l = new JLabel("Name");      ff_p.add(ff_l);
            ff_tf = new JTextField("");     ff_p.add(ff_tf);
            ff_tf.setText((String)v.elementAt(index++));
            ff_l = new JLabel("min");       ff_p.add(ff_l);
            ff_tf = new JTextField("");     ff_p.add(ff_tf);
            min = (String)v.elementAt(index++);
            ff_tf.setText(min);
            ff_l = new JLabel("max");       ff_p.add(ff_l);
            ff_tf = new JTextField("");     ff_p.add(ff_tf);
            max =(String)v.elementAt(index++);
            ff_tf.setText(max);
            ff_l = new JLabel("");          ff_p.add(ff_l);
            ff_l = new JLabel("");          ff_p.add(ff_l);
            ff_l = new JLabel("Center");    ff_p.add(ff_l);
            ff_tf = new JTextField("");     ff_p.add(ff_tf);
            ff_tf.setText( calcCenter(min, max) );
            ff_l = new JLabel("Bandwidth"); ff_p.add(ff_l);
            ff_tf = new JTextField("");     ff_p.add(ff_tf);
            ff_tf.setText( calcBandwidth(min, max) );
            ff_p.repaint();
            freqFields.add(ff_p);
        }
        panelTwo_mainPanel.add(freqFields, BorderLayout.CENTER);
    }

    /////////////////////////////////////////////////////////

    private String calcCenter(String min, String max) {
        double min_d = Double.parseDouble(min);
        double max_d = Double.parseDouble(max);
        double center = (min_d + max_d) / 2;
        return ""+center+"";   
    }
    private String calcBandwidth(String min, String max) {
        double min_d = Double.parseDouble(min);
        double max_d = Double.parseDouble(max);
        double bandwidth = max_d - min_d;
        return ""+bandwidth+"";
    }
}
