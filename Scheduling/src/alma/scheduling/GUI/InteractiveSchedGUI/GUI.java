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

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Component;
import java.awt.Container;
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
import javax.swing.JDialog;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import alma.scheduling.Define.SB;
//import alma.entity.xmlbinding.schedblock.SchedBlock;
/*
*/
//import alma.obsprep.editors.ObservingTool;
/**
 * A Gui that lets a PI interact with a scheduler to do interactive
 * scheduling.
 * 
 * @version 1.00 Dec 18, 2003
 * @author Sohaila Lucero
 */
public class GUI extends JFrame {
    private GUIController controller;
    private JTextArea outputView;
    private JTextArea sbOutputView;
    private JDialog littleframe;
    private String projectID;
    
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

        getContentPane().setLayout(new BorderLayout());
        JPanel p = new JPanel(new GridLayout(1,2));
        p.add(createOutputView());
        p.add(createButtonView());
        getContentPane().add(p);
        addWindowListener(new WindowAdapter() 
        {
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });
        setVisible(true);
    }

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
        //JLabel pl = new JLabel("Image should be here");
        //pl.setBackground(Color.blue);
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
        openObservingTool();
    }
    private void executeSB() {
        String selectedSB = outputView.getSelectedText();
        if(selectedSB != null) {
            clear();
            controller.executeSB(selectedSB);
            //outputView.append("SB "+selectedSB+" is now executing.\n");
        } else {
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
                            //outputView.append("SB "+selectedSB+" is now executing.\n");
                            // Do something with the selected SB
                            controller.executeSB(selectedSB);
                        }
                    }
                    littleframe.hide();
                }
            });

            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    littleframe.hide();
                }
            });
            ok.setFocusable(true);
            makePopupFrame("Execute SB","Enter SB id:", tf, ok, cancel);
        }
    }
    private void deleteSB() {
        String selectedSB = outputView.getSelectedText();
        if(selectedSB != null) {
            clear();
            //outputView.append("SB "+selectedSB+" is now deleted.\n");
        } else {
            JTextField tf = new JTextField();
            JButton ok = new JButton("Ok");
            ok.setFocusable(true);
            ok.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JButton tmpB = (JButton)e.getSource();
                    JPanel tmpP = (JPanel)tmpB.getParent();
                    Component[] tmpC = tmpP.getComponents();
                    for(int i=0; i<tmpC.length; i++) {
                        if((tmpC[i].getClass().getName()).equals(
                            "javax.swing.JTextField")){
                            
                            String selectedSB = ((JTextField)tmpC[i]).getText();
                            //outputView.append("SB "+selectedSB+" is now deleted from session list.\n");
                            // deletes the SB from the scheduler's subqueue. 
                            // mark it as aborted
                            // update it in the archive.
                            controller.deleteSB(selectedSB);
                        }
                    }
                    littleframe.hide();
                }
            });
            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    littleframe.hide();
                }
            });
            makePopupFrame("Delete SB", "Enter SB id:", tf, ok, cancel);
        }
    }

    private void updateSB(){
        openObservingTool();
    }
    
    /**
     * Creates a login window and returns the contents that the user 
     * entered. 
     */
    private void login(){
        JTextField tf = new JTextField("");
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
                littleframe.hide();
            }
        });
        ok.setFocusable(true);
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                littleframe.hide();
            }   
        });
        makePopupFrame("Login","Enter username:",tf, ok, cancel);
    }

    private void getSBs() {
        SB[] s = controller.getSBs();
        outputView.append("SchedBlocks: \n");
        sbOutputView.append("SchedBlock Contents: \n");
        sbOutputView.append("/////////////////////////////////////// \n");
        for(int i=0; i < s.length; i++) {
            outputView.append(s[i].getSchedBlockId() +"\n");
            sbOutputView.append("SchedBlock ID: "+s[i].getSchedBlockId() +"\n");
            try {
                projectID = s[i].getProject().getId();
                sbOutputView.append("SB's project id: "+ projectID +"\n");
            } catch (Exception e) {
                sbOutputView.append("SB's project id: not set to a project! this is bad!\n");
            }
            //sbOutputView.append("SchedBlock Observing Procedure: "+
            //    s[i].getObsProcedure() +"\n");
            try {
                sbOutputView.append("SchedBlock Status: "+
                   s[i].getStatus().getStatus()+"\n");
            } catch (Exception e) {
                sbOutputView.append("SchedBlock Status: no status set. \n");
            }
            //sbOutputView.append("SchedBlock Performance Goal: "+
            //    s[i].getObsUnitControl().getPerformanceGoal() +"\n");
            sbOutputView.append("SchedBlock Weather Constraints: \n"); 
            try {
                //sbOutputView.append("Opacity: "+s[i].getPreconditions().getWeatherConstraints().getOpacity() +"\n");
            } catch(Exception e) {
                sbOutputView.append("Opacity: no opacity set.\n");
            }
            try {
                //sbOutputView.append("Phase Stability:"+s[i].getPreconditions().getWeatherConstraints().getPhaseStability() +"\n");
            }catch(Exception e) {
                sbOutputView.append("Phase Stability: no phase stability set.\n");
            }
            try {
                //sbOutputView.append("Seeing:"+s[i].getPreconditions().getWeatherConstraints().getSeeing() +"\n");
            } catch(Exception e) {
                sbOutputView.append("Seeing: no seeing set.\n");
            }
            /*
            project it comes from & name 
            sbOutputView.append(s[i].() +"\n");
            sbOutputView.append(s[i].() +"\n");
            */
            sbOutputView.append("/////////////////////////////////////// \n");
        }
    }

    private void mustLogin() {
        JButton ok = new JButton("Ok");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                littleframe.hide();
            }
        });
        ok.setFocusable(true);
        makePopupFrame("Login", "You must login!", null, ok, null);
    }

    /** 
     * Creates a popup window to display information to the user or get information
     * from the user.
     * @param title The title to be displayed on the popup window.
     * @param labeltext The text to show up in the label in the popup window.
     */
    private void makePopupFrame(String title, String labeltext, JTextField tf, JButton b1, JButton b2) {
    //private void makePopupFrame(String title, String labeltext, boolean addTF, boolean isLogin) {
      
        littleframe = new JDialog(this,title);
        Point p = this.getLocationOnScreen();
        int x = p.x + 350; int y = p.y + 250;
        littleframe.setBounds(x, y, 150, 100);
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        JPanel panel = new JPanel(gridbag);
        //panel.setBorder(new MatteBorder(3,3,3,3,Color.black));
        JLabel label = new JLabel(labeltext);
        c.weightx = 1.0; c.weighty = 1.0;
        c.anchor = GridBagConstraints.CENTER;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(label, c);
        panel.add(label);
        if(tf != null) {
            c.gridwidth = GridBagConstraints.REMAINDER;
            gridbag.setConstraints(tf, c);
            panel.add(tf);
        }
        if(b2 == null) {
            c.gridwidth = GridBagConstraints.REMAINDER;
            gridbag.setConstraints(b1, c);
            panel.add(b1);
        } else {
            c.gridwidth = GridBagConstraints.RELATIVE;
            gridbag.setConstraints(b1,c);
            panel.add(b1);
            c.gridwidth = GridBagConstraints.REMAINDER;
            gridbag.setConstraints(b2,c);
            panel.add(b2);
        }
        littleframe.getContentPane().add(panel);
        littleframe.toFront();
        littleframe.show();
        /*
        JButton ok = new JButton("Ok");
        ok.setFocusable(true);
        if(isLogin) {
        } else {
            ok.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    littleframe.hide();
                }
            });
        }
        */
    }

    /**
     * Stops the current SB from executing.
     */ 
    public void stopSB() {
    }

    public void openObservingTool(){ 
        OpenOT ot = new OpenOT(projectID);
        Thread t = new Thread(ot);
        t.start();
        //ObservingTool.main(new String[]{"-r",projectID});
    }

    private void clear() {
        outputView.setText("");
        sbOutputView.setText("");
    }

    /**
     * Exits the GUI in a clean way so it doesn't break anything else
     * running on the same JVM.
     */
    public void exit() {
        dispose();
    }

    public static void main(String[] args) {
    }
}
