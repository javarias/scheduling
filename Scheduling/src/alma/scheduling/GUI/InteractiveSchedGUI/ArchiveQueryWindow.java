

package alma.scheduling.GUI.InteractiveSchedGUI;

import javax.swing.JFrame;
import javax.swing.JSeparator;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JScrollPane;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;

import javax.swing.JTable;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;

import alma.entity.xmlbinding.obsproject.*;

public class ArchiveQueryWindow extends JFrame {

    private ArchiveQueryWindowController controller;
    private JPanel main;
    private JTextField pn, pi;
    private Object[][] projRowInfo;
    private JTable projTable;
    private TableModel projTableModel;

    /**
      *
      */
    public ArchiveQueryWindow() {

        super();
        getContentPane().setLayout(new BorderLayout());
        addWindowListener(new WindowAdapter() 
        {
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });

        main = new JPanel(new BorderLayout());

        int inset = 250;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset, screenSize.width - inset*2, 
            screenSize.height - inset*2);
        setSize(600, 200);
        setTitle("Interactive Scheduling GUI - Project Search");

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(createMainView());
        setVisible(true);

    }

    
    /**
      *
      */
    public ArchiveQueryWindow(ArchiveQueryWindowController c) {
        this();
        this.controller = c;

    }

    /**
      *
      */
    public JPanel createMainView() {
        try {
            main.removeAll();
        }catch(Exception e) { /* don't care if it complains */ }
       // JLabel l = new JLabel("Query on the following parameters.");
        JLabel l = new JLabel("Search using the following parameters.");
        main.add(l, BorderLayout.NORTH);
        main.add(makeQueryPanel(), BorderLayout.CENTER);
        main.add(makeQueryButtonPanel(), BorderLayout.SOUTH);
        
        return main;
    
    }
    
    /**
      *
      */
    private JPanel makeQueryPanel(){
        JPanel p = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill =GridBagConstraints.HORIZONTAL;
        c.weightx= 1.0;
        c.gridwidth = 1;
        p.setLayout(gridbag);
        
        JLabel l = new JLabel("ProjectName:");
        gridbag.setConstraints(l,c);
        p.add(l);
        pn = new JTextField("");
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx= 2.0;
        gridbag.setConstraints(pn,c);
        p.add(pn);

        l = new JLabel("PI Name:");
        c.gridwidth = 1;
        c.weightx= 1.0;
        gridbag.setConstraints(l,c);
        p.add(l);
        pi = new JTextField("");
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx= 2.0;
        gridbag.setConstraints(pi,c);
        p.add(pi);

        return p;
    }
    
    /**
      *
      */
    private JPanel makeQueryButtonPanel(){
        JPanel p = new JPanel(new FlowLayout());

        JButton b = new JButton("Search");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendQuery(pn.getText(), pi.getText());
            }
        });
        p.add(b);
        b = new JButton("Clear");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearTFs();
            }
        });
        p.add(b);
        b = new JButton("Exit");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });
        p.add(b);
        return p;
    }

    /**
      *
      */
    private void sendQuery(String n, String pi) {
        //there should usually only be one result, but you never know!
        String[] results = controller.queryProjectAndPi(n,pi);
        //show results
        try {
            main.removeAll();
        }catch(Exception e) { /* don't care if it complains */ }
        main.add(makeDisplayPanel(results), BorderLayout.CENTER);
        main.add(makeDisplayButtonPanel(), BorderLayout.SOUTH);
        main.validate();
    }

    /**
      *
      */
    private JPanel makeDisplayPanel(String[] r) {
        //change the size so we see all the info!
        //get projects so we can extract info to display.
        ObsProject[] projects = new ObsProject[r.length];
        projRowInfo = new Object[r.length][4];
        for(int i=0; i < r.length; i++){
            projects[i] = controller.retrieveProject(r[i]);
            projRowInfo[i][0] = projects[i].getProjectName();
            projRowInfo[i][1] = projects[i].getPI();
            projRowInfo[i][2] = projects[i].getVersion();
            projRowInfo[i][3] = projects[i].getObsProjectEntity().getEntityId();
        }
        final String[] projColumnInfo = {"Project Name", "PI", "Version", "UID" };
        projTableModel = new AbstractTableModel() {
            public int getColumnCount() { return projColumnInfo.length; }
            public String getColumnName(int column) { return projColumnInfo[column]; }
            public int getRowCount() { return projRowInfo.length; }
            public Object getValueAt(int row, int col) { return projRowInfo[row][col]; }
            public void setValueAt(Object val, int row, int col) { projRowInfo[row][col] = val; }
        };
        projTable = new JTable(projTableModel);
        projTable.setPreferredScrollableViewportSize(new Dimension(550,100));
        projTable.addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent fe) {
                }
                public void focusLost(FocusEvent fe) {
                }
        });
        JScrollPane projListPane = new JScrollPane(projTable);
        
        JPanel p = new JPanel();
        p.add(projListPane);

        return p;
    }

    /**
      *
      */
    private JPanel makeDisplayButtonPanel(){
        JPanel p = new JPanel();
        JButton b = new JButton("Select");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loginToProject();
            }
        });
        p.add(b);
        b = new JButton("Search Again");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    main.removeAll();
                }catch(Exception ex) { /* don't care if it complains */ }
                JLabel l = new JLabel("Search using the following parameters.");
                main.add(l, BorderLayout.NORTH);
                main.add(makeQueryPanel(), BorderLayout.CENTER);
                main.add(makeQueryButtonPanel(), BorderLayout.SOUTH);
                main.validate();
            }
        });
        p.add(b);
        b = new JButton("Exit");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });
        p.add(b);
        return p;
    }

    private void loginToProject() {
        int row = projTable.getSelectedRow();
        String id = (String)projRowInfo[row][3]; //uid of selected project
        String pi = (String)projRowInfo[row][1];
        controller.loginToInteractiveProject(id, pi);
        System.out.println("Logging into project with ID = "+id);
        //exit();
        dispose();
    }

    /**
      *
      */
    private void clearTFs() {
        pn.setText("");
        pi.setText("");
    }
    /**
      *
      */
    public void exit() {
        controller.exit();
        dispose();
    }
}
