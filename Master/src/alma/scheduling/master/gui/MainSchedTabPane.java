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
package alma.scheduling.master.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EventListener;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import org.omg.CORBA.portable.IDLEntity;

import alma.ACSErrTypeCommon.wrappers.AcsJCouldntPerformActionEx;
import alma.ACSErrTypeCommon.wrappers.AcsJIllegalStateEventEx;
import alma.Control.CreatedAutomaticArrayEvent;
import alma.Control.CreatedManualArrayEvent;
import alma.Control.DestroyedAutomaticArrayEvent;
import alma.Control.DestroyedManualArrayEvent;
import alma.acs.nc.AcsEventSubscriber;
import alma.acs.nc.AcsEventSubscriberImplBase;
import alma.acs.nc.NCSubscriber;
import alma.acsnc.EventDescription;
import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.ArrayModeEnum;


public class MainSchedTabPane extends JTabbedPane {
    private PluginContainerServices container;
    private boolean createArrayEnabled = false;
    //private ALMASchedLogger logger;
    private Logger logger;
    private int overTabIndex;
    private JPanel mainPanel;
    private JPanel topPanel;
    private ExistingArraysTab arraysTab;
    //private SearchArchiveOnlyTab archiveTab;
    private CreateArrayPanel middlePanel;
    private JPanel showAntennaPanel;
    private JPanel middleButtonPanel;
    private Vector<SchedulerTab> allSchedulers;
    private JButton interactiveB;
    private JButton queuedB;
    private JButton dynamicB;
    private JButton manualB;
    private MainSchedTabPaneController controller;
    private Color origButtonColor;
    private Color selectedButtonColor;
    private Dimension maxSize;
    private JPanel parent;
    private AcsEventSubscriber<IDLEntity> subscriber;
    

    /**
      * Constructor
      */
    public MainSchedTabPane(JPanel p){
        super(JTabbedPane.TOP);
        setup();
        parent = p;
        controller = new MainSchedTabPaneController (this);
        selectedButtonColor = Color.green;
       
    }
        
    public void setup() {
        allSchedulers = new Vector<SchedulerTab>();
        createMainTab();
        //maxSize = getSize();
        //setMaximumSize(maxSize);
  //      createSearchArchiveOnlyTab();
        createExistingArrayTab();

        addTab("Main",mainPanel);
        addTab("Existing Arrays", arraysTab);
        this.revalidate();
        this.repaint();
        super.setUI(new SchedTabUI());
        addCloseTabListener(new CloseTabListener(){
            public void closeOperation(MouseEvent e) {
                logger.fine("in close operation");
                closeTab(overTabIndex);
            }
        });
    }
    
    public Boolean secondSetup(PluginContainerServices cs){
        container = cs;
        controller.setup(cs);
        logger = cs.getLogger();
        //archiveTab.connectedSetup(cs);
        middlePanel.connectedSetup(cs);
        arraysTab.connectedSetup(cs);
        controller.checkOperationalState();
        logger.fine("SCHEDULING_PANEL: Second setup, connected to manager");
        try {
            subscriber = container.createNotificationChannelSubscriber(alma.Control.CHANNELNAME_CONTROLSYSTEM.value, IDLEntity.class);
            subscriber.addSubscription(new MainSchedTabPane.CreatedAutomaticArrayEvCallback());
            subscriber.addSubscription(new MainSchedTabPane.CreatedManualArrayEvCallback());
            subscriber.addSubscription(new MainSchedTabPane.DestroyedAutomaticArrayEvCallback());
            subscriber.addSubscription(new MainSchedTabPane.DestroyedManualArrayEvCallback());
            subscriber.startReceivingEvents();
        } catch(Exception e){
            e.printStackTrace();
            logger.warning("SCHEDULING_PANEL: Problem getting NC consumer for control system channel, won't see existing arrays");
        }
        
        return true;
    }
    
    public void receive(CreatedAutomaticArrayEvent event) {
        
        // first reflash the offine antenna and reflash the online antenna
    	middlePanel.setEnabled(java.lang.Boolean.FALSE);
    	if(interactiveB.getBackground().equals(selectedButtonColor) ||
    			queuedB.getBackground().equals(selectedButtonColor) ||
    			dynamicB.getBackground().equals(selectedButtonColor) ||
    			manualB.getBackground().equals(selectedButtonColor)) {
//    	middlePanel.updateOnlineAntenna();
    	}
    	
        
        }
    
    public void receive(CreatedManualArrayEvent event) {
        
        // first reflash the offine antenna and reflash the online antenna
    	middlePanel.setEnabled(java.lang.Boolean.FALSE);
    	if(interactiveB.getBackground().equals(selectedButtonColor) ||
    			queuedB.getBackground().equals(selectedButtonColor) ||
    			dynamicB.getBackground().equals(selectedButtonColor) ||
    			manualB.getBackground().equals(selectedButtonColor)) {
//    	middlePanel.updateOnlineAntenna();
    	}
        }
    
    public void receive(DestroyedAutomaticArrayEvent event) {
        
        // first reflash the offine antenna and reflash the online antenna
    	middlePanel.setEnabled(java.lang.Boolean.FALSE);
    	if(interactiveB.getBackground().equals(selectedButtonColor) ||
    			queuedB.getBackground().equals(selectedButtonColor) ||
    			dynamicB.getBackground().equals(selectedButtonColor) ||
    			manualB.getBackground().equals(selectedButtonColor)) {
//    	middlePanel.updateOnlineAntenna();
    	}
        }

    public void receive(DestroyedManualArrayEvent event) {
    
    // first reflash the offine antenna and reflash the online antenna
	middlePanel.setEnabled(java.lang.Boolean.FALSE);
	if(interactiveB.getBackground().equals(selectedButtonColor) ||
			queuedB.getBackground().equals(selectedButtonColor) ||
			dynamicB.getBackground().equals(selectedButtonColor) ||
			manualB.getBackground().equals(selectedButtonColor)) {
//	middlePanel.updateOnlineAntenna();
	}
    
    	}


    private void initializeChessboardsWithALMA(){
        middlePanel.initializeChessboards();
        validate();
    }

    public void setDefaults(){
        initializeChessboardsWithALMA();
        doInteractiveButton();
        //select antenna 1 already..
    }
    public void connectedToALMA(boolean b){
        //tell chessboards to getTMCDB if its null
        if(!b){
            resetMainViewButtons();
        }
        middlePanel.setEnabled(b);
    }
    
    public void setOfflineDisplay(){
        resetMainViewButtons();
        createArrayEnabled = false;
        middlePanel.setEnabled(false);
    }
    
    private void createExistingArrayTab() {
        arraysTab = new ExistingArraysTab();
        //arraysTab.setMaxSize(maxSize);
    }
    
 /*   private void createSearchArchiveOnlyTab() {
        archiveTab = new SearchArchiveOnlyTab();
        archiveTab.setMaxSize(maxSize);
    }
    */
    private void createMainTab(){ 
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

    private void doSchedulerTypeButton(JButton button, ArrayModeEnum mode) {
        resetMainViewButtons();
        origButtonColor = button.getBackground();
        button.setBackground(selectedButtonColor);
        createArrayEnabled = true;
        middlePanel.setEnabled(true);
        middlePanel.prepareCreateArray(mode);
    }

    private void doInteractiveButton() {
    	doSchedulerTypeButton(interactiveB, ArrayModeEnum.INTERACTIVE);
    }

    private void doQueuedButton() {
    	doSchedulerTypeButton(queuedB, ArrayModeEnum.QUEUED);
    }

    private void doDynamicButton(){ 
    	doSchedulerTypeButton(dynamicB, ArrayModeEnum.DYNAMIC);
        middlePanel.setPolicyEnabled(true);
    }

    private void doManualButton(){
    	doSchedulerTypeButton(manualB, ArrayModeEnum.MANUAL);
    }

    public void resetMainViewButtons(){
        interactiveB.setBackground(origButtonColor);
        queuedB.setBackground(origButtonColor);
        dynamicB.setBackground(origButtonColor);
        manualB.setBackground(origButtonColor);
        middlePanel.setPolicyEnabled(false);
    }
    
    protected void disableSchedulerButtons() {
        interactiveB.setEnabled(false);
        queuedB.setEnabled(false);
        dynamicB.setEnabled(false);
        manualB.setEnabled(false);
        middlePanel.setPolicyEnabled(false);
    }
    protected void enableSchedulerButtons() {
        interactiveB.setEnabled(true);
//      TODO: Activate queued scheduling button after adding queued scheduling
        queuedB.setEnabled(false);
        dynamicB.setEnabled(true);
        manualB.setEnabled(true);
    }

    public void createTopPanel(){
        topPanel = new JPanel(new GridLayout(1,2));
        topPanel.setBorder(new TitledBorder("Start New Observing Scheduler"));
        JPanel buttons = new JPanel(new GridLayout(1,4));
        interactiveB = new JButton("Interactive");
        queuedB = new JButton("Queued");
        dynamicB = new JButton("Dynamic");
        manualB = new JButton("Manual");
        
        enableSchedulerButtons();

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
        manualB.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(areWeConnected()){
                        doManualButton();
                    } else {
                        showConnectMessage();
                        return;
                    }
                }
        });               

        buttons.add(interactiveB);
        buttons.add(queuedB);
        buttons.add(dynamicB);
        buttons.add(manualB);
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

    private void showConnectMessage(){
        JOptionPane.showMessageDialog(this,"System not operational yet.",
                "Not Connected", JOptionPane.ERROR_MESSAGE);
    }

    public synchronized void addCloseTabListener(CloseTabListener l){
        listenerList.add(CloseTabListener.class, l);
    }

    public void closeTabEvent(MouseEvent e, int tabIndex) {
        logger.fine("in close tab event");
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
        logger.fine("SchedTab Info: "+tab.getArrayName() +"; "+tab.getSchedulerName() +";"+tab.getSchedulerType());
        for(int i=0; i< allSchedulers.size(); i++){
            logger.fine("SchedTab "+i+" Info: "+allSchedulers.elementAt(i).getArrayName() +"; "+
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
    
	public void exit() {
		try {
			subscriber.disconnect();
		} catch (AcsJIllegalStateEventEx e) {
			e.printStackTrace();
		} catch (AcsJCouldntPerformActionEx e) {
			e.printStackTrace();
		}
	}
    
    private class CreatedManualArrayEvCallback implements alma.acs.nc.AcsEventSubscriber.Callback<CreatedManualArrayEvent> {
		@Override
		public void receive(CreatedManualArrayEvent eventData, EventDescription eventDescrip) {
			MainSchedTabPane.this.receive(eventData);
		}

		@Override
		public Class<CreatedManualArrayEvent> getEventType() {
			return CreatedManualArrayEvent.class;
		}
    }
    
    private class CreatedAutomaticArrayEvCallback implements alma.acs.nc.AcsEventSubscriber.Callback<CreatedAutomaticArrayEvent> {
		@Override
		public void receive(CreatedAutomaticArrayEvent eventData, EventDescription eventDescrip) {
			MainSchedTabPane.this.receive(eventData);
		}

		@Override
		public Class<CreatedAutomaticArrayEvent> getEventType() {
			return CreatedAutomaticArrayEvent.class;
		}
    }
    
    private class DestroyedManualArrayEvCallback implements alma.acs.nc.AcsEventSubscriber.Callback<DestroyedManualArrayEvent> {

		@Override
		public void receive(DestroyedManualArrayEvent eventData,
				EventDescription eventDescrip) {
			MainSchedTabPane.this.receive(eventData);
		}

		@Override
		public Class<DestroyedManualArrayEvent> getEventType() {
			return DestroyedManualArrayEvent.class;
		}
    }
    
    private class DestroyedAutomaticArrayEvCallback implements alma.acs.nc.AcsEventSubscriber.Callback<DestroyedAutomaticArrayEvent> {

		@Override
		public void receive(DestroyedAutomaticArrayEvent eventData,
				EventDescription eventDescrip) {
			MainSchedTabPane.this.receive(eventData);
		}

		@Override
		public Class<DestroyedAutomaticArrayEvent> getEventType() {
			return DestroyedAutomaticArrayEvent.class;
		}
    	
    }
}
