/*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2002
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
package alma.scheduling.scheduler;

import alma.xmlentity.XmlEntityStruct;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
import javax.swing.JTextArea;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import java.lang.Process;
import java.lang.Runtime;
import java.io.IOException;
/**
 * A Gui that lets a PI interact with a scheduler to do interactive
 * scheduling.
 * 
 * @version 1.00 Dec 18, 2003
 * @author sroberts
 */
public class GUI extends JFrame {
    private GUIController controller;
    private JTextArea outputView;
    private JTextArea sbOutputView;
    private Popup errorPopup;
    private Popup loginPopup;
    private Popup addSBpopup;
    private Popup executePopup;
    private Popup deletePopup;
    private Popup updatePopup;

    public GUI(GUIController c) {
        this.controller = c;
        JMenuBar menuBar = new JMenuBar();
        JMenu menu1 = new JMenu("File");
        menu1.setMnemonic(KeyEvent.VK_ALT);
        JMenuItem howto = new JMenuItem("HowTo");
        //menu1.add(howto);
        JMenuItem quit = new JMenuItem("Quit", KeyEvent.VK_Q);;
        quit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exit();
                //System.exit(0);
        //        controller.exit();
            }
        });
        menu1.add(quit);
        menuBar.add(menu1);
        setJMenuBar(menuBar);

        int inset = 250;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset, screenSize.width - inset*2, 
            screenSize.height - inset*2);
        setTitle("Interactive Scheduling GUI");
        setSize(800, 600);

        getContentPane().setLayout(new GridLayout(1,2));
        //JScrollPane pane = createOutputView();
        getContentPane().add(createOutputView());
        getContentPane().add(createButtonView());
        
        addWindowListener(new WindowAdapter() 
        {
            public void windowClosing(WindowEvent e) {
                //System.exit(0);
                exit();
            }
        });
        setVisible(true);
    }

//    private JScrollPane createOutputView() {
    private JTabbedPane createOutputView() {
        JTabbedPane returnView = new JTabbedPane();
        returnView.setTabPlacement(JTabbedPane.TOP);
        ///////////////////////
        outputView = new JTextArea();
        outputView.setEditable(false);
        JScrollPane pane = new JScrollPane(outputView);
        pane.setViewportBorder(new BevelBorder(BevelBorder.RAISED));
        returnView.addTab("Main View", pane);
        ///////////////////////
        sbOutputView = new JTextArea();
        sbOutputView.setEditable(false);
        pane = new JScrollPane(sbOutputView);
        returnView.addTab("SB View", pane);
        ///////////////////////
        return returnView;
    }
    private JPanel createButtonView() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        /////////////////////////////
        JPanel picturePanel = new JPanel(new BorderLayout());
        picturePanel.setBackground(new Color(159,3,211));
        picturePanel.add(new JLabel());
        ImageIcon almaImage = new ImageIcon(controller.getImage("alma_logo.jpg"));
        JLabel pl = new JLabel(almaImage);
        pl.setBackground(Color.blue);
        picturePanel.add(pl, BorderLayout.EAST);
        ////////////////////////////
        mainPanel.add(picturePanel, BorderLayout.NORTH);
        ////////////////////////////
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        JPanel panel = new JPanel();
        panel.setBackground(new Color(159,3,211));
        panel.setLayout(gridbag);
        JButton startSession = new JButton("Start Interactive Session");
        startSession.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });
        JButton getSBs = new JButton("Get all SchedBlocks");
        getSBs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(controller.getLogin() == "") {
                    mustLogin();
                    return;
                }
                getSBs();
            }
        });
        JButton addSB = new JButton("Add SB");
        addSB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(controller.getLogin() == "") {
                    mustLogin();
                    return;
                }
                addSB();
            }
        });
        JButton deleteSB = new JButton("Delete SB");
        deleteSB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(controller.getLogin() == "") {
                    mustLogin();
                    return;
                }
                deleteSB();
            }
        });
        JButton updateSB = new JButton("Update SB");
        updateSB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(controller.getLogin() == "") {
                    mustLogin();
                    return;
                }
                updateSB();
            }
        });
        JButton executeSB = new JButton("Execute SB");
        executeSB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(controller.getLogin() == "") {
                    mustLogin();
                    return;
                }
                executeSB();
            }
        });
        JButton stopSB = new JButton("Stop Current SchedBlock");
        stopSB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(controller.getLogin() == "") {
                    mustLogin();
                    return;
                }
                stopSB();
            }
        });
        JButton endSession = new JButton("End Interactive Session");
        endSession.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(controller.getLogin() == "") {
                    mustLogin();
                    return;
                }
                controller.setLogin("");
                //do other things to insure user is logged out.
                //ie: close scheduler, etc
            }
        });
        JButton clearView = new JButton("Clear View");
        clearView.setFont(new Font("Ariel",Font.PLAIN, 10));
        clearView.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clear();
            }
        });

        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0; c.weighty = 1.0;

        JLabel blank = new JLabel("");
        gridbag.setConstraints(blank, c);
        panel.add(blank);
        
        c.gridwidth = 2;
        gridbag.setConstraints(startSession, c);
        panel.add(startSession);

        c.gridwidth = GridBagConstraints.REMAINDER;
        blank = new JLabel("");
        gridbag.setConstraints(blank, c);
        panel.add(blank);
        ///////////////////////////////////////
        c.gridwidth = 1;
        blank = new JLabel("");
        gridbag.setConstraints(blank, c);
        panel.add(blank);

        c.gridwidth = 2;
        gridbag.setConstraints(getSBs, c);
        panel.add(getSBs);

        c.gridwidth = GridBagConstraints.REMAINDER;
        blank = new JLabel("");
        gridbag.setConstraints(blank, c);
        panel.add(blank);
        ///////////////////////////////////////
        c.gridwidth = 1;
        blank = new JLabel("");
        gridbag.setConstraints(blank, c);
        panel.add(blank);

        gridbag.setConstraints(addSB, c);
        panel.add(addSB);

        gridbag.setConstraints(deleteSB, c);
        panel.add(deleteSB);

        c.gridwidth = GridBagConstraints.REMAINDER;
        blank = new JLabel("");
        gridbag.setConstraints(blank, c);
        panel.add(blank);
        ///////////////////////////////////////
        c.gridwidth = 1;
        blank = new JLabel("");
        gridbag.setConstraints(blank, c);
        panel.add(blank);

        gridbag.setConstraints(updateSB, c);
        panel.add(updateSB);

        gridbag.setConstraints(executeSB, c);
        panel.add(executeSB);

        c.gridwidth = GridBagConstraints.REMAINDER;
        blank = new JLabel("");
        gridbag.setConstraints(blank, c);
        panel.add(blank);
        ///////////////////////////////////////
        //c.fill = GridBagConstraints.NONE;
        //c.fill = GridBagConstraints.HORIZONTAL;
        ///////////////////////////////////////
        c.gridwidth = 1;
        blank = new JLabel("");
        gridbag.setConstraints(blank, c);
        panel.add(blank);

        c.gridwidth = 2;
        gridbag.setConstraints(stopSB, c);
        panel.add(stopSB);

        c.gridwidth = GridBagConstraints.REMAINDER;
        blank = new JLabel("");
        gridbag.setConstraints(blank, c);
        panel.add(blank);
        ///////////////////////////////////////
        c.gridwidth = 1;
        blank = new JLabel("");
        gridbag.setConstraints(blank, c);
        panel.add(blank);

        c.gridwidth = 2;
        gridbag.setConstraints(endSession, c);
        panel.add(endSession);

        c.gridwidth = GridBagConstraints.REMAINDER;
        blank = new JLabel("");
        gridbag.setConstraints(blank, c);
        panel.add(blank);
        ///////////////////////////////////////
        c.gridwidth = 2;
        blank = new JLabel("");
        gridbag.setConstraints(blank, c);
        panel.add(blank);

        c.gridwidth = 1;
        gridbag.setConstraints(clearView, c);
        panel.add(clearView);

        c.gridwidth = GridBagConstraints.REMAINDER;
        blank = new JLabel("");
        gridbag.setConstraints(blank, c);
        panel.add(blank);
        ///////////////////////////////////////
        c.gridwidth = GridBagConstraints.REMAINDER;
        blank = new JLabel("");
        gridbag.setConstraints(blank, c);
        panel.add(blank);

        ////////////////////////////
        mainPanel.add(panel, BorderLayout.CENTER);
        ////////////////////////////
        return mainPanel;
    }

    private void addSB(){
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        JPanel panel = new JPanel(gridbag);
        panel.setBorder(new MatteBorder(3,3,3,3,Color.black));
        JLabel label = new JLabel("  ObservingTool will popup....");
        JTextField tf = new JTextField();
        JButton ok = new JButton("Ok");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addSBpopup.hide();
            }
        });
        c.weightx = 1.0; c.weighty = 1.0;
        c.anchor = GridBagConstraints.CENTER;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(label, c);
        panel.add(label);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(ok, c);
        panel.add(ok);
        PopupFactory pf = PopupFactory.getSharedInstance();
        Point p = this.getLocationOnScreen();
        int x = p.x + 350; int y = p.y + 250;
        addSBpopup = pf.getPopup(this,panel,x,y);
        addSBpopup.show();
        
        openObservingTool();
    }
    private void executeSB() {
        String selectedSB = outputView.getSelectedText();
        if(selectedSB != null) {
            clear();
            outputView.append("SB "+selectedSB+" is now executing.\n");
        } else {
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            JPanel panel = new JPanel(gridbag);
            panel.setBorder(new MatteBorder(3,3,3,3,Color.black));
            JLabel label = new JLabel("  Enter SB id or cancel and select it from list  ");
            JTextField tf = new JTextField();
            JButton ok = new JButton("Ok");
            ok.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JButton tmpB = (JButton)e.getSource();
                    JPanel tmpP = (JPanel)tmpB.getParent();
                    Component[] tmpC = tmpP.getComponents();
                    for(int i=0; i<tmpC.length; i++) {
                        if((tmpC[i].getClass().getName()).equals(
                            "javax.swing.JTextField")){
                            
                            String selectedSB = ((JTextField)tmpC[i]).getText();
                            outputView.append("SB "+selectedSB+" is now executing.\n");
                            // Do something with the selected SB
                        }
                    }
                    executePopup.hide();
                }
            });
            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    executePopup.hide();
                }
            });
            c.weightx = 1.0; c.weighty = 1.0;
                c.anchor = GridBagConstraints.CENTER;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(label, c);
            panel.add(label);
            gridbag.setConstraints(tf, c);
            panel.add(tf);
            c.gridwidth = 1;
            gridbag.setConstraints(ok, c);
            panel.add(ok);
            c.gridwidth = GridBagConstraints.REMAINDER;
            gridbag.setConstraints(cancel, c);
            panel.add(cancel);
            PopupFactory pf = PopupFactory.getSharedInstance();
            Point p = this.getLocationOnScreen();
            int x = p.x + 350; int y = p.y + 250;
            executePopup = pf.getPopup(this,panel,x,y);
            executePopup.show();
        }
    }
    private void deleteSB() {
        String selectedSB = outputView.getSelectedText();
        if(selectedSB != null) {
            clear();
            outputView.append("SB "+selectedSB+" is now executing.\n");
        } else {
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            JPanel panel = new JPanel(gridbag);
            panel.setBorder(new MatteBorder(3,3,3,3,Color.black));
            JLabel label = new JLabel("  Enter SB id or cancel and select it from list  ");
            JTextField tf = new JTextField();
            JButton ok = new JButton("Ok");
            ok.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JButton tmpB = (JButton)e.getSource();
                    JPanel tmpP = (JPanel)tmpB.getParent();
                    Component[] tmpC = tmpP.getComponents();
                    for(int i=0; i<tmpC.length; i++) {
                        if((tmpC[i].getClass().getName()).equals(
                            "javax.swing.JTextField")){
                            
                            String selectedSB = ((JTextField)tmpC[i]).getText();
                            outputView.append("SB "+selectedSB+" is now deleted from session list.\n");
                            // Do something with the selected SB
                        }
                    }
                    deletePopup.hide();
                }
            });
            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    deletePopup.hide();
                }
            });
            c.weightx = 1.0; c.weighty = 1.0;
                c.anchor = GridBagConstraints.CENTER;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(label, c);
            panel.add(label);
            gridbag.setConstraints(tf, c);
            panel.add(tf);
            c.gridwidth = 1;
            gridbag.setConstraints(ok, c);
            panel.add(ok);
            c.gridwidth = GridBagConstraints.REMAINDER;
            gridbag.setConstraints(cancel, c);
            panel.add(cancel);
            PopupFactory pf = PopupFactory.getSharedInstance();
            Point p = this.getLocationOnScreen();
            int x = p.x + 350; int y = p.y + 250;
            deletePopup = pf.getPopup(this,panel,x,y);
            deletePopup.show();
        }
    }
    private void updateSB(){
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        JPanel panel = new JPanel(gridbag);
        panel.setBorder(new MatteBorder(3,3,3,3,Color.black));
        JLabel label = new JLabel("  ObservingTool will popup....");
        JTextField tf = new JTextField();
        JButton ok = new JButton("Ok");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updatePopup.hide();
            }
        });
        c.weightx = 1.0; c.weighty = 1.0;
        c.anchor = GridBagConstraints.CENTER;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(label, c);
        panel.add(label);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(ok, c);
        panel.add(ok);
        PopupFactory pf = PopupFactory.getSharedInstance();
        Point p = this.getLocationOnScreen();
        int x = p.x + 350; int y = p.y + 250;
        updatePopup = pf.getPopup(this,panel,x,y);
        updatePopup.show();
    }
    
    /**
     * Creates a login window and returns the contents that the user 
     * entered. 
     */
    private void login(){
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        JPanel panel = new JPanel(gridbag);
        panel.setBorder(new MatteBorder(3,3,3,3,Color.black));
        JLabel label = new JLabel("  Login ID: ");
        JTextField tf = new JTextField();
        JButton ok = new JButton("Ok");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JButton tmpB = (JButton)e.getSource();
                JPanel tmpP = (JPanel)tmpB.getParent();
                Component[] tmpC = tmpP.getComponents();
                for(int i=0; i<tmpC.length; i++) {
                    if((tmpC[i].getClass().getName()).equals(
                        "javax.swing.JTextField")){
                        
                        controller.setLogin(((JTextField)tmpC[i]).getText());
                        outputView.append("Welcome " +controller.getLogin() +"\n");
                    }
                }
                loginPopup.hide();
            }
        });

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loginPopup.hide();
            }
        });
        c.weightx = 1.0; c.weighty = 1.0;
        c.anchor = GridBagConstraints.CENTER;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(label, c);
        panel.add(label);
        gridbag.setConstraints(tf, c);
        panel.add(tf);
        c.gridwidth = 1;
        gridbag.setConstraints(ok, c);
        panel.add(ok);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(cancel, c);
        panel.add(cancel);
        PopupFactory pf = PopupFactory.getSharedInstance();
        Point p = this.getLocationOnScreen();
        int x = p.x + 350; int y = p.y + 250;
        loginPopup = pf.getPopup(this,panel,x,y);
        loginPopup.show();
    }

    private void getSBs() {
        String[] s = controller.getSBs();
        outputView.append("SchedBlocks: \n");
        for(int i=0; i < s.length; i++) {
            outputView.append(s[i] +"\n");
        }
    }

    private void mustLogin() {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        JPanel panel = new JPanel(gridbag);
        panel.setBorder(new MatteBorder(3,3,3,3,Color.black));
        JLabel label = new JLabel("  You must log in first!  ");
        JTextField tf = new JTextField();
        JButton ok = new JButton("Ok");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                errorPopup.hide();
            }
        });
        c.weightx = 1.0; c.weighty = 1.0;
        c.anchor = GridBagConstraints.CENTER;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(label, c);
        panel.add(label);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(ok, c);
        panel.add(ok);
        PopupFactory pf = PopupFactory.getSharedInstance();
        Point p = this.getLocationOnScreen();
        int x = p.x + 350; int y = p.y + 250;
        errorPopup = pf.getPopup(this,panel,x,y);
        errorPopup.show();
    }

    public void stopSB() {
    }

    public void openObservingTool(){ 
        try {
            Process process = Runtime.getRuntime().exec("otproto &");
        } catch(IOException e) {
            System.out.println("Observing tool didn't pop up..");
            System.out.println(e.toString());
        }
    }

    private void clear() {
        outputView.setText("");
    }
    public void exit() {
        dispose();
    }
    public static void main(String[] args) {
        //GUI gui = new GUI(new GUIController());
    }
}
