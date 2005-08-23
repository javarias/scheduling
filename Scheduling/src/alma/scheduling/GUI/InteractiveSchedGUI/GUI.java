/*
 * ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2002
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 * 
 * File GUI.java
 * 
 */
package alma.scheduling.GUI.InteractiveSchedGUI;

import alma.xmlentity.XmlEntityStruct;

import java.util.Map;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Collection;
import java.util.Enumeration;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;

import javax.swing.JFrame;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JViewport;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;

import alma.scheduling.Define.SB;
import alma.scheduling.Define.Project;
import alma.scheduling.Define.SchedulingException;

/**
 * A Gui that lets a PI interact with a scheduler to do interactive
 * scheduling.
 * 
 * @version 1.00 Dec 18, 2003
 * @author Sohaila Lucero
 */
public class GUI extends JFrame {
    private GUIController controller;
    private boolean loggedIn;
    private Object[][] sbRowInfo;
    private TableModel sbTableModel;
    private JScrollPane mainViewPane;
    private JScrollPane sbViewPane;
    private JScrollPane sbListPane;
    private JScrollPane selectedSBPane;
    private JPanel sbDisplayPanel;
    private JPanel projectDisplayPanel;
    private JTextArea  selectedSBView;
    //Toolbar & Buttons
    private JToolBar toolbar;
    private JButton sessionStateButton;
    private JButton addSBButton;
    private JButton updateSBButton;
    private JButton executeSBButton;
    private JButton deleteSBButton;
    private JButton stopSBButton;
    private JButton helpButton;
    /**
     *
     */
    public GUI(GUIController c) {
        this.controller = c;
        getContentPane().setLayout(new BorderLayout());
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_ALT);
        JMenuItem howto = new JMenuItem("HowTo");
        //fileMenu.add(howto);
        JMenuItem quit = new JMenuItem("Quit", KeyEvent.VK_Q);;
        quit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });
        fileMenu.add(quit);
        menuBar.add(fileMenu);
        JMenu projectMenu = new JMenu("Projects");
        String[] ids = controller.getProjectIds();
        JMenuItem pItem;
        for(int i=0; i<ids.length; i++){
            pItem = new JMenuItem(ids[i]);
            pItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JMenuItem item = (JMenuItem)e.getSource();
                    //item.getText() == project Id
                    displayProjectInfo(item.getText());
                    displaySBInfo(item.getText()); 
                }
            });
            projectMenu.add(pItem);
        }
        menuBar.add(projectMenu);
        setJMenuBar(menuBar);
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        //toolbar.setBackground(new Color(159,3,211));
        createButtons(toolbar);
        getContentPane().add(toolbar, BorderLayout.PAGE_START);

        int inset = 250;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset, screenSize.width - inset*2, 
            screenSize.height - inset*2);
        setTitle("Interactive Scheduling GUI");
        setSize(800, 600);

        JPanel p = new JPanel(new GridLayout(1,2));
        p.add(createOutputView());
        //p.add(createButtonView());
        getContentPane().add(p);
        addWindowListener(new WindowAdapter() 
        {
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });
        setVisible(true);
        loggedIn = false;
    }

    /**
     *
     */
    private JTabbedPane createOutputView() {
        JTabbedPane returnView = new JTabbedPane();
        returnView.setTabPlacement(JTabbedPane.TOP);
        ///////////////////////
        mainViewPane = new JScrollPane();
        mainViewPane.setViewportBorder(new BevelBorder(BevelBorder.RAISED));
        //mainViewPane.setBackground(new Color(159,3,211));
        ///////////////////////
        sbViewPane = new JScrollPane();
        sbViewPane.setViewportBorder(new BevelBorder(BevelBorder.RAISED));
        //sbViewPane.setBackground(new Color(159,3,211));
        ///////////////////////
        returnView.addTab("Project View", mainViewPane);
        returnView.addTab("SB View", sbViewPane);
        //returnView.setBackground(new Color(159,3,211));
        return returnView;
    }



    /**
     *
     */
    private void createButtons(JToolBar bar) {
        sessionStateButton = new JButton("Login");
        sessionStateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(!loggedIn) {
                    login();
                    loggedIn = true;
                } else {
                    logout();
                    loggedIn = false;
                }
            }
        });

        helpButton = new JButton("Help");
        
        toolbar.add(sessionStateButton);
        toolbar.add(helpButton);
        //toolbar.add(new JButton("woowoo"));
    }

    /**
     *
     */
    private void addSB(){
        //openObservingTool();
        //project id will only ever be shown in the sbOutputView..
        /*
        String projID = sbOutputView.getSelectedText();
        if(projID != null) {
            controller.openObservingTool(projID);
        } else {
            projID = JOptionPane.showInputDialog(this, "Enter Project ID","Add SB", 
                JOptionPane.PLAIN_MESSAGE);
            if(projID != null) {
                controller.openObservingTool(projID);
            }
        }
        */
    }
    /**
     *
     */
    private void executeSB() {
        /*
        String selectedSB = outputView.getSelectedText();
        if(selectedSB != null) {
            clear();
            controller.executeSB(selectedSB);
            //outputView.append("SB "+selectedSB+" is now executing.\n");
        } else {
            selectedSB = sbOutputView.getSelectedText();
            if(selectedSB != null) {
                controller.executeSB(selectedSB);
            } else {
                selectedSB = JOptionPane.showInputDialog(this,"Enter SB id" ,"Execute SB", 
                    JOptionPane.PLAIN_MESSAGE);
            }
        }
        */
    }
    /**
     *
     */
    private void deleteSB() {
        /*
        String selectedSB = outputView.getSelectedText();
        if(selectedSB != null) {
            controller.deleteSB(selectedSB);
            //clear();
            //outputView.append("SB "+selectedSB+" is now deleted.\n");
        } else {
            selectedSB = JOptionPane.showInputDialog(this, "Enter SB id", "Delete SB", 
                JOptionPane.PLAIN_MESSAGE);
            controller.deleteSB(selectedSB);
        }*/
    }

    /**
     *
     */
    private void updateSB(){
        //openObservingTool();
        /*
        String projID = sbOutputView.getSelectedText();
        if(projID != null) {
            controller.openObservingTool(projID);
        } else {
            projID = JOptionPane.showInputDialog(this, "Enter Project ID","Update SB", 
                JOptionPane.PLAIN_MESSAGE);
            if(projID != null) {
                controller.openObservingTool(projID);
            }
        }*/
    }
    
    /**
     * Creates a login window and returns the contents that the user 
     * entered. 
     */
    private void login(){
        try{
            String login = JOptionPane.showInputDialog(this,"Please log in.");
            controller.setLogin(login);
            displaySBInfo(controller.getDefaultProjectId());
            displayProjectInfo(controller.getDefaultProjectId());
            sessionStateButton.setText("Logout");
        } catch(SchedulingException e) {
            JOptionPane.showMessageDialog(this, e.toString(), "", 
                JOptionPane.WARNING_MESSAGE);
        }
    }

    private void displayProjectInfo(String id){
        Project proj = controller.getProject(id);
        projectDisplayPanel = new JPanel(new BorderLayout());
        JPanel projectTop = new JPanel(new GridLayout(4,1));
        //projectTop.add(new JSeparator()); 
        JPanel tmp = new JPanel();
        tmp.add(new JLabel("Project Name: ")); tmp.add(new JLabel(proj.getProjectName()));
        tmp.add(new JSeparator()); 
        projectTop.add(tmp);
        tmp = new JPanel();
        tmp.add(new JLabel("PI: ")); tmp.add(new JLabel(proj.getPI()));
        tmp.add(new JSeparator());
        projectTop.add(tmp);
        tmp = new JPanel();
        tmp.add(new JLabel("Status: ")); tmp.add(new JLabel(proj.getStatus().getStatus()));
        tmp.add(new JSeparator());
        projectTop.add(tmp);
        tmp = new JPanel();
        tmp.add(new JLabel("Time Created: ")); tmp.add(new JLabel(proj.getTimeOfCreation().toString()));
        tmp.add(new JSeparator()); 
        projectTop.add(tmp);
        
        /*
        JPanel projectCenter = new JPanel(new GridLayout(3,1));
        projectCenter.add(new JSeparator());
        projectCenter.add(new JLabel("Total Programs: "+proj.getProgram().getTotalPrograms()));
        projectCenter.add(new JLabel("Completed Programs: "+proj.getProgram().getNumberProgramsCompleted()));
        projectCenter.add(new JLabel("Failed Programs: "+proj.getProgram().getNumberProgramsFailed()));
        projectCenter.add(new JSeparator());
        projectCenter.add(new JSeparator());
        projectCenter.add(new JLabel("Total SBs: "+proj.getProgram().getTotalSBs()));
        projectCenter.add(new JLabel("Completed SBs: "+proj.getProgram().getNumberSBsCompleted()));
        projectCenter.add(new JLabel("Failed SBs: "+proj.getProgram().getNumberSBsFailed()));
        projectCenter.add(new JSeparator());
        
        projectCenter.add(new JSeparator());
        projectCenter.add(new JSeparator());
        projectCenter.add(new JSeparator());
        projectCenter.add(new JSeparator());
        projectCenter.add(new JSeparator());
        */
        projectDisplayPanel.add(projectTop, BorderLayout.NORTH);
        //projectDisplayPanel.add(projectCenter, BorderLayout.CENTER);
        mainViewPane.getViewport().add(projectDisplayPanel);
        mainViewPane.getViewport().repaint();
    }
    
    private void logout(){
        try{
           // controller.setLogin("");
            controller.endSession();
            sessionStateButton.setText("Login");
            mainViewPane.getViewport().removeAll();
            mainViewPane.getViewport().repaint();
            sbViewPane.getViewport().removeAll();
            sbViewPane.getViewport().repaint();
        } catch(Exception ex){}
        //do other things to insure user is logged out.
        //ie: close scheduler, etc
    }

    /**
     *
     */
    private void displaySBInfo(String projectId) {
        final String[] sbColumnInfo = {"SB Name", "Sci. Priority", "Center Freq."};
        Dimension d = new Dimension(400,100);
        sbDisplayPanel = new JPanel(new BorderLayout());
        SB[] allsbs = controller.getSBs();
        Vector s = new Vector();
        for(int i=0; i < allsbs.length;i++){
            if(allsbs[i].getProject().getId().equals(projectId)) {
                s.add(allsbs[i]);
            }
        }
        /**  */
        sbRowInfo = new Object[s.size()][4];
        for(int i=0; i < s.size(); i++){
            sbRowInfo[i][0] = ((SB)s.elementAt(i)).getSBName(); 
            sbRowInfo[i][1] = ((SB)s.elementAt(i)).getScientificPriority();
            sbRowInfo[i][2] = String.valueOf(((SB)s.elementAt(i)).getCenterFrequency());
            sbRowInfo[i][3] = ((SB)s.elementAt(i)).getSchedBlockId();
        }

        sbTableModel = new AbstractTableModel() { //Table which has 3 columns and as many rows as SBs
            public int getColumnCount() { return sbColumnInfo.length; }
            public String getColumnName(int column) { return sbColumnInfo[column]; }
            public int getRowCount() { return sbRowInfo.length;     }
            public Object getValueAt(int row, int col) { return sbRowInfo[row][col]; }
            public void setValueAt(Object val, int row, int col) { sbRowInfo[row][col]= val; }
        };
        JTable sbTable = new JTable(sbTableModel);
        sbTable.setPreferredScrollableViewportSize(d);
        sbTable.addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent fe) {
                    addSelectedSBView(fe);
                }
                public void focusLost(FocusEvent fe){
                    removeSelectedSBView();
                }
        });
            
        sbListPane = new JScrollPane(sbTable);
        JPanel sbList = new JPanel();
        sbList.add(new JSeparator());
        sbList.add(sbListPane); //Scroll pane which holds all the sbs
        sbList.add(new JSeparator());

        sbDisplayPanel.add(sbList,BorderLayout.NORTH);
        
            
        sbDisplayPanel.add(sbButtonView(), BorderLayout.SOUTH); //buttons for sbs (add, update, delete,...)

        sbViewPane.getViewport().add (sbDisplayPanel); //add everything to Sb tab view.
        sbViewPane.getViewport().repaint();
    }

    private void addSelectedSBView(FocusEvent e) {
        JPanel sbDisplayed = new JPanel();
        selectedSBView = new JTextArea();
        selectedSBView.setLineWrap(true);
        selectedSBView.setPreferredSize(new Dimension(400,300));
        JTable t = (JTable)e.getSource(); //e.getComponent(); 
        int row = t.getSelectedRow();
        if(row <0 ) {
            selectedSBView.setText("No row selected... nothing should have happened...");
        }
        SB sb = controller.getSB((String)sbRowInfo[row][3]);
        //String name, target, status, upri, spri, weather;

        selectedSBView.setText("SB Name:                "+ sbRowInfo[row][0] +"\n"+
                               "SB Target: \n"+
                               "   RA  (deg):           "+sb.getTarget().getCenter().getRaInDegrees()+"\n"+
                               "   Dec (deg):           "+sb.getTarget().getCenter().getDecInDegrees()+"\n\n"+
                               "SB Frequency: \n"+
                               "   Center Frequency:    "+sb.getCenterFrequency()+"\n"+
                               "   Frequency Band:      "+sb.getFrequencyBand().getName()+"\n"+       
                               "      Low:              "+sb.getFrequencyBand().getLowFrequency()+"\n"+
                               "      High:             "+sb.getFrequencyBand().getHighFrequency()+"\n\n"+
                               "SB Status:              "+sb.getStatus()+"\n"+
                               "SB User Priority:       "+sb.getUserPriority()+"\n"+
                               "SB Scientific Priority: "+sb.getScientificPriority()+"\n" +
                               " \n\n Eventually will have success, etc..");
        
                            //   "SB Weather Constraints: "+sb.getWeatherConstraint().toString()); //NULL right now..

        selectedSBPane = new JScrollPane(selectedSBView);
        sbDisplayed.add(new JSeparator());
        sbDisplayed.add(selectedSBPane); //scroll pane which holds the info on a specific sb
        sbDisplayed.add(new JSeparator());
        
        sbDisplayPanel.add(sbDisplayed, BorderLayout.CENTER);
        sbDisplayPanel.validate();
    }
    private void removeSelectedSBView() {
        selectedSBView.removeAll();
        /*
        JScrollPane tmp1 = (JScrollPane)selectedSBView.getParent();
        JViewport tmp2 = (JViewport)tmp1.getParent();
        JPanel tmp3 = (JPanel)tmp2.getParent();
        tmp3.removeAll();
        */
    }

    private JPanel sbButtonView(){
        JPanel panel = new JPanel();
        //addsb
        panel.add(new JSeparator());
        addSBButton = new JButton("Add SB");
        addSBButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(controller.getLogin() == "") {
                    mustLogin();
                    return;
                }
                addSB();
            }
        });
        panel.add(addSBButton);
        panel.add(new JSeparator());

        //update sb
        updateSBButton = new JButton("Update SB");
        updateSBButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(controller.getLogin() == "") {
                    mustLogin();
                    return;
                }
                updateSB();
            }
        });
        panel.add(updateSBButton);
        panel.add(new JSeparator());

        //delete sb
        deleteSBButton = new JButton("Delete SB");
        deleteSBButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(controller.getLogin() == "") {
                    mustLogin();
                    return;
                }
                deleteSB();
            }
        });
        panel.add(deleteSBButton);
        panel.add(new JSeparator());

        //execute sb
        executeSBButton = new JButton("Execute SB");
        executeSBButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(controller.getLogin() == "") {
                    mustLogin();
                    return;
                }
                executeSB();
            }
        });
        panel.add(executeSBButton);
        panel.add(new JSeparator());

        //stop sb
        stopSBButton = new JButton("Stop Current SchedBlock");
        stopSBButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(controller.getLogin() == "") {
                    mustLogin();
                    return;
                }
                stopSB();
            }
        });
        panel.add(stopSBButton);
        panel.add(new JSeparator());

        return panel;
    }

    /**
     *
     */
    private void mustLogin() {
        JOptionPane.showMessageDialog(this, "You Must Login First!", "", 
            JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Stops the current SB from executing.
     */ 
    public void stopSB() {
    }

    /**
     *
     */
    private void refreshSBQueue() {
        controller.refreshSBQueue();
    }

    /**
     * Exits the GUI in a clean way so it doesn't break anything else
     * running on the same JVM.
     */
    public void exit() {
        dispose();
    }

    /**
     *
     */
    public static void main(String[] args) {
    }
}
