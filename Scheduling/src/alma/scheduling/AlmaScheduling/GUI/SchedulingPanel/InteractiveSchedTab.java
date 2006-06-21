package alma.scheduling.AlmaScheduling.GUI.SchedulingPanel;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class InteractiveSchedTab extends JScrollPane {
    
    
    public InteractiveSchedTab(){
        getViewport().add(createSearchView());
    }

    private JPanel createSearchView() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(searchSection1(), BorderLayout.NORTH);
        //p.add(projectInfo(), BorderLayout.CENTER);
        //p.add(buttonSection(), BorderLayout.SOUTH);
        return p;
    }
    private JPanel searchSection1() {
        JPanel search = new JPanel(new GridLayout(2,1));
        JPanel p1 = new JPanel(new GridLayout(2,2));
      //  p1.add(new JLabel());
        p1.add(new JLabel("PI Name: ")); 
        p1.add(new JTextField());
      //  p1.add(new JLabel());
      //  p1.add(new JLabel());
        p1.add(new JLabel("Project Name: ")); 
        p1.add(new JTextField());
      //  p1.add(new JLabel());
        ////
        JPanel p2 = new JPanel(new FlowLayout());
        JButton b = new JButton("Search");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
          //      sendQuery(pn.getText(), pi.getText());
            }
        });
        p2.add(b);
        b = new JButton("Clear");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
          //      clearTFs();
            }
        });
        p2.add(b);
        ////
        search.add(p1,BorderLayout.CENTER);
        search.add(p2,BorderLayout.SOUTH);
        return search;
    }

    private JPanel projectInfo() {
        //contains brief project info and list of all sbs
        JPanel p = new JPanel();
        return p;
    }
    private void detailedSbSection() {
    }

    private JPanel buttonSection() {
        JPanel p = new JPanel(new GridLayout(1,4));
        p.add(new JButton("execute"));
        p.add(new JButton("modify"));
        p.add(new JButton("remove"));
        p.add(new JButton("execute"));
        return p;
    }
    
        
}
