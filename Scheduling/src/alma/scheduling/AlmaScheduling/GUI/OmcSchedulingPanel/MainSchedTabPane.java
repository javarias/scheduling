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
 * File MainSchedTabPane.java
 */
package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.awt.*;
import java.util.EventListener;
import java.util.Vector;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.util.logging.Logger;

import alma.scheduling.MasterSchedulerIF;
import alma.scheduling.SBLite;
import alma.exec.extension.subsystemplugin.PluginContainerServices;

import alma.acs.container.ContainerServices;

public class MainSchedTabPane extends JTabbedPane {
    private PluginContainerServices container;
    //private MainSchedTabPaneController controller;
    //private JPopupMenu rightClickMenu;
    private boolean createArrayEnabled = false;
    private Logger logger;
    private int overTabIndex;
    private JPanel mainPanel;
    private JPanel topPanel;
    private SearchArchiveOnlyTab archiveTab;
    private CreateArrayPanel middlePanel;
    private JPanel showAntennaPanel;
    private JPanel middleButtonPanel;
    private Vector<SchedulerTab> allSchedulers;
    private JButton interactiveB;
    private JButton queuedB;
    private JButton dynamicB;
    private MainSchedTabPaneController controller;
    private Color origButtonColor;
    private Color selectedButtonColor;
    private Dimension maxSize;

    /**
      * Constructor
      */
    public MainSchedTabPane(){
        super(JTabbedPane.TOP);
        setup();
        Dimension d = getPreferredSize();
        setMaximumSize(d);
        controller = new MainSchedTabPaneController (this);
        selectedButtonColor = Color.green;
    }
        
    public void setup() {
        allSchedulers = new Vector<SchedulerTab>();
        createMainTab();
        maxSize = getSize();
        //System.out.println("Main tab size = "+maxSize.toString());
        setMaximumSize(maxSize);
        createSearchArchiveOnlyTab();
        addTab("Main",mainPanel);
        addTab("Search", archiveTab);
        //addTab("Queued", new QueuedSchedTab("foo","foo"));
      //  addTab("Dynamic", new DynamicSchedTab("foo","foo"));
        super.setUI(new SchedTabUI());
        addCloseTabListener(new CloseTabListener(){
            public void closeOperation(MouseEvent e) {
                logger.info("in close operation");
                closeTab(overTabIndex);
            }
        });
    }
    
    public void secondSetup(PluginContainerServices cs){
        container = cs;
        controller.setup(cs);
        logger = cs.getLogger();
        archiveTab.connectedSetup(cs);
        middlePanel.connectedSetup(cs);
        logger.info("SCHEDULING_PANEL: Second setup, connected to manager");
        logger.finest("SCHEDULING_PANEL: Finest log");
    }

    public void setDefaults(){
        doInteractiveButton();
        //select antenna 1 already..
        middlePanel.selectDefaultAntenna();
    }
    public void connectedToALMA(boolean b){
        archiveTab.connectToALMA(b);
    }

    public void createSearchArchiveOnlyTab() {
        archiveTab = new SearchArchiveOnlyTab();
        archiveTab.setMaxSize(maxSize);
    }
    public void createMainTab(){ 
        mainPanel = new JPanel(new BorderLayout());
        try {
            createTopPanel(); //buttons
            createMiddlePanel(); //createArrayPanel
            mainPanel.add(topPanel, BorderLayout.NORTH);
            mainPanel.add(middlePanel, BorderLayout.CENTER);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void doInteractiveButton() {
        resetMainViewButtons();
        origButtonColor = interactiveB.getBackground();
        interactiveB.setBackground(selectedButtonColor);
        createArrayEnabled = true;
        middlePanel.setEnabled(true);
        middlePanel.prepareCreateArray("interactive");
    }

    private void doQueuedButton() {
        resetMainViewButtons();
        origButtonColor = queuedB.getBackground();
        queuedB.setBackground(selectedButtonColor);
        createArrayEnabled = true;
        middlePanel.setEnabled(true);
       // middlePanel.setArrayMode("queued");
        middlePanel.prepareCreateArray("queued");
        //createArray with mode 'queued'
    }

    private void doDynamicButton(){ 
        resetMainViewButtons();
    //    JOptionPane.showMessageDialog(this,"Dynamic scheduling not available yet.",
      //          "Not Available", JOptionPane.INFORMATION_MESSAGE);
        
        origButtonColor = dynamicB.getBackground();
        dynamicB.setBackground(selectedButtonColor);
        createArrayEnabled = true;
        middlePanel.setEnabled(true);
        middlePanel.prepareCreateArray("dynamic");
        //createArray with mode 'dynamic'
    
    }

    public void resetMainViewButtons(){
        interactiveB.setBackground(origButtonColor);
        queuedB.setBackground(origButtonColor);
        dynamicB.setBackground(origButtonColor);
    }
    
    public void createTopPanel(){
        topPanel = new JPanel(new GridLayout(1,2));
        topPanel.setBorder(new TitledBorder("Start New Scheduler"));
        JPanel buttons = new JPanel(new GridLayout(1,3));
        interactiveB = new JButton("Interactive");
        queuedB = new JButton("Queued");
        dynamicB = new JButton("Dynamic");

        interactiveB.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(areWeConnected()){
                        doInteractiveButton();
                        //createArray with mode 'interactive'
                        //if array created open interactive tab
                    } else {
                        showConnectMessage();
                        return;
                    }
                }
        });               
        queuedB.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(areWeConnected()){
                        doQueuedButton();
                    } else {
                        showConnectMessage();
                        return;
                    }
                }
        });               
        dynamicB.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(areWeConnected()){
                        doDynamicButton();
                    } else {
                        showConnectMessage();
                        return;
                    }
                }
        });               

        buttons.add(interactiveB);
        buttons.add(queuedB);
        buttons.add(dynamicB);
        topPanel.add(buttons);
       // topPanel.add(new JPanel()); //spacer
    }
    
    public void createMiddlePanel() {
        middlePanel = new CreateArrayPanel();
        middlePanel.setOwner(this);
        middlePanel.setEnabled(false);
    }

    private void updateAntennaView(){
    }

    public void openSchedulerTab(String mode, String array) {
        SchedulerTab tab;
        String title="";
        if(mode.equals("interactive")){
            tab = new InteractiveSchedTab(container, array);
            allSchedulers.add(tab);
            title = array +" (Interactive)";
            ((InteractiveSchedTab)tab).setMaxSize(maxSize);
            addTab(title, (JPanel)tab);
        } else if (mode.equals("queued")){
            title = array +" (Queued)";
            tab = new QueuedSchedTab(container, title, array);
            ((QueuedSchedTab)tab).setMaxSize(maxSize);
            allSchedulers.add(tab);
            addTab(title, (JPanel)tab);
        } else if (mode.equals("dynamic")){
            title = array +" (Dynamic)";
            tab = new DynamicSchedTab(container, array);
            allSchedulers.add(tab);
            addTab(title, (JPanel)tab);
        }
        int i = indexOfTab(title);
        setSelectedIndex(i);
    }

    private void showConnectMessage(){
        JOptionPane.showMessageDialog(this,"System not operational yet.",
                "Not Connected", JOptionPane.ERROR_MESSAGE);
    }

    public synchronized void addCloseTabListener(CloseTabListener l){
        listenerList.add(CloseTabListener.class, l);
    }

    public void closeTabEvent(MouseEvent e, int tabIndex) {
        logger.info("in close tab event");
        EventListener close[] = getListeners(CloseTabListener.class);
        overTabIndex = tabIndex;
        for(int i=0; i< close.length; i++){
            ((CloseTabListener)close[i]).closeOperation(e);
        }
    }
    private void closeTab(int i) {
        SchedulerTab tab = (SchedulerTab)getComponentAt(i);
        tab.exit();
        remove(i);
        //remove it from all schedulers list
        int x = getSchedulerPosition(tab);
        if(x!=-1){
            allSchedulers.removeElementAt(x);
        }
        setSelectedIndex(0); //default to main tab when something is closed
    }

    private int getSchedulerPosition(SchedulerTab tab){
        logger.info("SchedTab Info: "+tab.getArrayName() +"; "+tab.getSchedulerName() +";"+tab.getSchedulerType());
        for(int i=0; i< allSchedulers.size(); i++){
            logger.info("SchedTab "+i+" Info: "+allSchedulers.elementAt(i).getArrayName() +"; "+
               allSchedulers.elementAt(i).getSchedulerName() +"; "+ allSchedulers.elementAt(i).getSchedulerType());

            if(allSchedulers.elementAt(i).getArrayName().equals(tab.getArrayName()) &&
               allSchedulers.elementAt(i).getSchedulerName().equals(tab.getSchedulerName()) &&
               allSchedulers.elementAt(i).getSchedulerType().equals(tab.getSchedulerType())){
                  return i;
            }
        }
        return -1;
    }

    private boolean areWeConnected(){
        return controller.areWeConnected();
    }
    public void exit(){
    }

}
