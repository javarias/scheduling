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
 * File CreateArrayPanel.java
 *
 */
package alma.scheduling.master.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import alma.Control.CorrelatorType;
import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.SchedulingMasterExceptions.ACSInternalExceptionEx;
import alma.SchedulingMasterExceptions.ControlInternalExceptionEx;
import alma.SchedulingMasterExceptions.SchedulingInternalExceptionEx;
import alma.common.gui.chessboard.ChessboardEntry;
import alma.common.gui.chessboard.ChessboardPanel;
import alma.common.gui.chessboard.ChessboardStatusEvent;
import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.ArrayModeEnum;
import alma.scheduling.Master;
import alma.scheduling.SchedulingPolicyFile;
import alma.scheduling.array.guis.ArrayPanel;
import alma.scheduling.master.util.SchedulingPolicyWrapper;
import alma.scheduling.policy.gui.PolicyChangeListener;
import alma.scheduling.policy.gui.PolicyManagementPanel;
import alma.scheduling.policy.gui.PolicySelectionListener;
import alma.scheduling.utils.ErrorHandling;
import alma.scheduling.utils.SchedulingProperties;

@SuppressWarnings("serial")
public class CreateArrayPanel extends SchedulingPanelGeneralPanel implements PolicyChangeListener{

    private Vector<String> allArrays;
    private JButton createArrayB;
    private JButton cancelB;
    private ArrayModeEnum arrayMode;
    private CreateArrayController controller;
    private JTabbedPane parent;
    private ChessboardPanel twelveMeterChessboard;
    private ChessboardPanel sevenMeterChessboard;
    private ChessboardPanel tpChessboard;
    private JRadioButton[] availablePhotonics;
    private JPanel chessboardPanel;
    private ButtonGroup photonicsGroup;
    private JComboBox correlatorType;
    private JComboBox schedulingPolicy;
    private JButton   setPolicyB;
    private Master master;
	private PolicyManagementPanel policyPanel = null;


    public CreateArrayPanel() {
        super();
        super.setBorder(new TitledBorder("Create Array"));
        allArrays = new Vector<String>();
        //setSize(600,300);
        controller= new CreateArrayController();
        createGenericAntennaChessboards();
        this.setLayout(new GridLayout(1,1));
        add(chessboardPanel);
    }

    /**
	 * @return the controller
	 */
	public CreateArrayController getController() {
		return controller;
	}

	public void setOwner(JTabbedPane p){
        parent = p;
    }

    public void connectedSetup(PluginContainerServices cs) {
        super.onlineSetup(cs);
        logger = cs.getLogger();
        logger.fine("SECOND SETUP FOR ANTENNA PANEL");
        controller.secondSetup(cs);
        try {
			master = alma.scheduling.MasterHelper.narrow(
					cs.getComponentNonSticky("SCHEDULING_MASTERSCHEDULER"));
		} catch (AcsJContainerServicesEx ex) {
			ex.printStackTrace();
			master = null;
		}
        //the controlMaster must be ready before we do the initializing
        CheckControlReady controlComponent = new CheckControlReady();
        Thread t = controller.getCS().getThreadFactory().newThread(controlComponent);
        t.setDaemon(true);
        t.start();
        try {
            t.join();
        } 
        catch(InterruptedException e) { 
            e.printStackTrace(); 
        } 
        //when process running this. the controlMaster and tmcdb must be ready.
        initializeChessboards();
        remove(chessboardPanel);
        createALMAAntennaChessboards();
        add(chessboardPanel, BorderLayout.CENTER);
        setEnabled(false);
        
        PolicyChangeCallbackImpl callback = new PolicyChangeCallbackImpl(this);
        try {
			cs.activateOffShoot(callback);
			master.addMonitorPolicy(InetAddress.getLocalHost().getHostName() + "_"
					+ this.toString() + "_"
					+ System.currentTimeMillis(), callback._this());
		} catch (AcsJContainerServicesEx e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			master.addMonitorPolicy(this.toString() + "_"
					+ System.currentTimeMillis(), callback._this());
		}
    }

    protected void initializeChessboards() {
        controller.updateChessboardWithALMANames();
        createALMAAntennaChessboards();
        add(chessboardPanel, BorderLayout.CENTER);
    }

    public void setEnabled(boolean enabled){
        createArrayB.setEnabled(enabled);
        cancelB.setEnabled(enabled);
        disableChessboards(enabled);
        repaint();
        validate();
    }

    private void disableCreateArrayPanel() {
        ((MainSchedTabPane)parent).disableSchedulerButtons();
        setEnabled(false);
    }
    private void enableCreateArrayPanel() {
        ((MainSchedTabPane)parent).enableSchedulerButtons();
    }
    
    private void createGenericAntennaChessboards(){
        chessboardPanel = new JPanel(new BorderLayout());
        JPanel cbPanel = new JPanel(new GridLayout(4,1));
        cbPanel.setLayout(new BoxLayout(cbPanel, BoxLayout.Y_AXIS));
        ChessboardEntry[][] all = controller.getGenericAntennaMapping();
        //all[0] is antennas for TwelveMeterChessboard
        cbPanel.add(createTwelveMeterChessboard(all[0]));
        //all[1] is antennas for SevenMeterChessboard
        cbPanel.add(createSevenMeterChessboard(all[1]));
        //all[2] is antennas for TP Chessboard
        cbPanel.add(createTPChessboard(all[2]));
        cbPanel.add(createCentralLOComponent());
        cbPanel.add(createCorrelatorTypeComponent());
        chessboardPanel.add(cbPanel, BorderLayout.CENTER);
        chessboardPanel.add(createSouthPanel(),BorderLayout.SOUTH);
    }

    private void createALMAAntennaChessboards(){
        chessboardPanel.removeAll();
        remove(chessboardPanel);
        chessboardPanel = new JPanel(new BorderLayout());
        
        //JPanel cbPanel = new JPanel(new GridLayout(4,1));
        JPanel cbPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gridBagConstraints= new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.6;
        gridBagConstraints.gridheight = 6;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.gridwidth=GridBagConstraints.REMAINDER;
        //cbPanel.setLayout(new BoxLayout(cbPanel, BoxLayout.Y_AXIS));
        ChessboardEntry[][] all = controller.getAntennasForOfflineChessboards();
        //all[0] is antennas for TwelveMeterChessboard
        cbPanel.add(createTwelveMeterChessboard(all[0]),gridBagConstraints);
        //all[1] is antennas for SevenMeterChessboard
        //gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.weighty = 0.2;
        cbPanel.add(createSevenMeterChessboard(all[1]),gridBagConstraints);
        //all[2] is antennas for TP Chessboard
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.weighty = 0.1;
        cbPanel.add(createTPChessboard(all[2]),gridBagConstraints);
        //String[] availablePhotonics = controller.getAvailableCLOPhotonics();
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.weighty = 0.1;
        cbPanel.add(createCentralLOComponent(), gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 1;
        cbPanel.add(createCorrelatorTypeComponent(), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        cbPanel.add(createPolicyComponent(), gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        
        chessboardPanel.add(new JScrollPane(cbPanel),BorderLayout.CENTER);
        chessboardPanel.add(createSouthPanel(),BorderLayout.SOUTH);
        validate();
    }

    private JPanel createTwelveMeterChessboard(ChessboardEntry[] foo) {
        ChessboardEntry[][] entries = new ChessboardEntry[5][10];
        int ctr=0;
        for(int i=0; i < ACS_ANTENNAS_ROWS;i++){
            for(int j=0; j < ACS_ANTENNAS_COLS; j++){
                entries[i][j] = foo[ctr++];
            }
        }
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gridBagConstraints= new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        p.setBorder(new TitledBorder("Twelve Meter Antennas"));
        twelveMeterChessboard = new ChessboardPanel(entries, true, null, null);
        twelveMeterChessboard.setDetailsDisplayEnabled(false);
        p.setName("TwelveMeterAntennas");
        p.add(twelveMeterChessboard,gridBagConstraints);
        return p;
    }
    
    private JPanel createSevenMeterChessboard(ChessboardEntry[] foo) {
        ChessboardEntry[][] entries = new ChessboardEntry[2][6];
        int ctr=0;
        for(int i=0; i < ACA_ANTENNAS_ROWS;i++){
            for(int j=0; j < ACA_ANTENNAS_COLS; j++){
                entries[i][j] = foo[ctr++];
            }
        }
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gridBagConstraints= new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        //gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.6;
        gridBagConstraints.weighty = 1.0;
        p.setBorder(new TitledBorder("Seven Meter Antennas"));
        sevenMeterChessboard = new ChessboardPanel(entries, true, null, null);
        sevenMeterChessboard.setDetailsDisplayEnabled(false);
        p.add(sevenMeterChessboard,gridBagConstraints);
        return p;
    }

    private JPanel createTPChessboard(ChessboardEntry[] foo) {
        ChessboardEntry[][] entries = new ChessboardEntry[1][4];
        int ctr=0;
        for (int i=0; i < TP_ANTENNAS_ROWS; i++) {
        	for(int j=0; j < TP_ANTENNAS_COLS; j++){
        		entries[0][j] = foo[ctr++];
        	}
        }
         JPanel p = new JPanel(new GridBagLayout());
         GridBagConstraints gridBagConstraints= new GridBagConstraints();
         gridBagConstraints.fill = GridBagConstraints.BOTH;
         //gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
         gridBagConstraints.weightx = 0.4;
         gridBagConstraints.weighty = 1.0;
        p.setBorder(new TitledBorder("Total Power Antennas"));
        tpChessboard = new ChessboardPanel(entries, true, null, null);
        tpChessboard.setDetailsDisplayEnabled(false);
        p.add(tpChessboard,gridBagConstraints);
        return p;
    }
    
    private JPanel createCentralLOComponent(){
    	JPanel p= new JPanel();
    	p.setBorder(new TitledBorder("Central Local Oscillator Photonics"));
    	p.setLayout(new GridLayout(4,2));
    	photonicsGroup = new ButtonGroup();
    	JButton resetButton = new JButton("Deselect");
    	
    	availablePhotonics = new JRadioButton[6];
    	LOActionListener radioButtonEvent = new LOActionListener();
    	for(int i=0;i<availablePhotonics.length;i++){
    		availablePhotonics[i] = new JRadioButton("PhotonicReference"+(i+1));
    		availablePhotonics[i].setActionCommand("PhotonicReference"+(i+1));
    		availablePhotonics[i].addActionListener(radioButtonEvent);
    		availablePhotonics[i].setEnabled(false);
    		p.add(availablePhotonics[i]);
        	photonicsGroup.add(availablePhotonics[i]);
    	}
    	
    	resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
            		photonicsGroup.clearSelection();
            }
        });
    	
    	p.add(resetButton);
    	
    	return p;
    }
    
    private JPanel createCorrelatorTypeComponent() {
//    	final ArrayList<String> labels = new ArrayList<String>();
    	final ArrayList<CorrelatorType> correlatorTypes = new ArrayList<CorrelatorType>();
    	
    	// Start by getting all the correlator types. Do this by first
    	// getting the one with index 0, then the one with index 1 and
    	// so on until we run out of them (at which point from_int()
    	// will throw an exception.
    	int index = 0;
    	boolean finished = false;
    	while (!finished) {
    		try {
    			final CorrelatorType c = CorrelatorType.from_int(index);
    			correlatorTypes.add(c);
//    			labels.add(c.toString());
    			index ++;
    		} catch (org.omg.CORBA.BAD_PARAM e) {
    			finished = true;
    		}
    	}

    	final CorrelatorType options[] = correlatorTypes.toArray(
    			new CorrelatorType[correlatorTypes.size()]);
    	
    	correlatorType = new JComboBox(options);
    	final JLabel    label = new JLabel("Correlator");
    	
    	JPanel result = new JPanel();
    	result.add(label);
    	result.add(correlatorType);
    	
    	return result;
    }
    
    private JPanel createPolicyComponent() {
    	final Vector<SchedulingPolicyWrapper> policies = new Vector<SchedulingPolicyWrapper>();
    	for (SchedulingPolicyFile policiesFile: master.getSchedulingPolicies()) {
    		for (String policy: policiesFile.schedulingPolicies) {
    			policies.add(new SchedulingPolicyWrapper(policiesFile, policy));
    		}
    	}
//    	final Vector<String> policies = new Vector<String>();
    	
    	schedulingPolicy = new JComboBox(policies);
    	schedulingPolicy.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				logger.fine(String.format("Action performed on scheduling policy combo box, %s at %d selected",
						schedulingPolicy.getSelectedItem(), schedulingPolicy.getSelectedIndex()));
			}});
    	
    	final JLabel label = new JLabel("Scheduling Policy");
   		setPolicyB = new JButton("Setup Policy");
   		
    	final PolicySelectionListener polly = new PolicySelectionListener(){

 			@Override
			public void policySelected(String beanName) {
				boolean foundIt = false;
				logger.fine(
						String.format(
								"CreateArrayPanel, policySelected: %s",
								beanName));
				for (int i = 0; i < schedulingPolicy.getItemCount(); i++) {
					SchedulingPolicyWrapper spw = (SchedulingPolicyWrapper) schedulingPolicy.getItemAt(i);
					try {
						if (spw.getSpringBeanName().equals(beanName)) {
							schedulingPolicy.setSelectedIndex(i);
							foundIt = true;
						}
					} catch (Exception e) {
						ErrorHandling.warning(logger, String.format(
								"Exception at index %d (itemCount = %d)",
								i, schedulingPolicy.getItemCount()), e);
					}
				}
				if (!foundIt) {
					logger.warning("Cannot find policy called " + beanName + ", policies on widget are:");
					for (int i = 0; i < schedulingPolicy.getItemCount(); i++) {
						SchedulingPolicyWrapper spw = (SchedulingPolicyWrapper) schedulingPolicy.getItemAt(i);
						logger.warning("\t" + spw.getSpringBeanName());
					}
				} else {
					logger.fine("Found policy " + beanName);
				}
			}
		};
		setPolicyB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setPolicyB.setEnabled(false);
				JFrame frame = new JFrame("Policy Selection for new Array");
				policyPanel = new PolicyManagementPanel(master);
				policyPanel.addListener(polly);
				frame.addWindowListener(new WindowListener(){

					@Override
					public void windowClosed(WindowEvent e) {
						policyPanel.removeListener(polly);
						policyPanel = null;
						setPolicyB.setEnabled(true);
					}

					@Override public void windowOpened(WindowEvent e) {} // ignore
					@Override public void windowClosing(WindowEvent e) {} // ignore
					@Override public void windowIconified(WindowEvent e) {} // ignore
					@Override public void windowDeiconified(WindowEvent e) {} // ignore
					@Override public void windowActivated(WindowEvent e) {} // ignore
					@Override public void windowDeactivated(WindowEvent e) {} // ignore
				});
				frame.getContentPane().add(policyPanel);
				frame.pack();
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.setVisible(true);
			}
		});
    	JPanel result = new JPanel();
    	result.add(label);
    	result.add(schedulingPolicy);
    	result.add(setPolicyB);
    	setPolicyEnabled(false);
    	
    	return result;
    }
    
    protected void setPolicyEnabled(boolean b) {
    	schedulingPolicy.setEnabled(b);
    	setPolicyB.setEnabled(b);
    }
    
    
    private JPanel createSouthPanel() {
        JPanel p = new JPanel();
        p.add(actionButtons(), BorderLayout.SOUTH);
        return p;
    }

    private JPanel actionButtons(){
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        createArrayB = new JButton("Create");
        createArrayB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                CreateArrayThread at = new CreateArrayThread();
                Thread t = controller.getCS().getThreadFactory().newThread(at);
                t.start();
            }
        });
        cancelB = new JButton("Cancel");
        cancelB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                //exit();
                setEnabled(false);
                ((MainSchedTabPane)parent).resetMainViewButtons();
            }
        });
        p.add(createArrayB);
        p.add(cancelB);
        return p;
    }

    private void updateChessboard(String[] antNames) {
    //show the available antennas as online
        //for each available antenna
        //call chessboard.processStatusChange(..) with new ChessboardStatusEvent
        //will need cb entry's name
        //String cbeName;
    	//logger.info("updateChessboard: Number of active's antennas:"+antNames.length);
        ChessboardStatusEvent event=null;
        for (int i=0; i < antNames.length; i++){
        	//logger.info("updateChessboard active antenna name:"+antNames[i]);
            event = new ChessboardStatusEvent(antNames[i],
            		SPAntennaStatus.ONLINE,
            		controller.toolTipForAntenna(antNames[i]));
            if(antNames[i].contains("PM"))
            	tpChessboard.processStatusChange(event);
            else if(antNames[i].contains("CM")) 
                sevenMeterChessboard.processStatusChange(event);
            else
                twelveMeterChessboard.processStatusChange(event);
        }
    }
    
    private void updateAvailablePhotonics(String[] Photonics) {
    	for(int i=0;i<availablePhotonics.length;i++){
    		availablePhotonics[i].setEnabled(false);
    	}
    	photonicsGroup.clearSelection();
    	
    	for (int i=0;i<Photonics.length;i++){
    		for(int j=0;j<availablePhotonics.length;j++){
    			if(Photonics[i].equalsIgnoreCase(availablePhotonics[j].getText())) {
    				availablePhotonics[j].setEnabled(true);
    				//availablePhotonics[j].setSelected(true);
    			}
    		}
    	}
    }

    private void disableChessboards(boolean enabled) {
        ChessboardStatusEvent event=null;
        twelveMeterChessboard.clearSelection();
        sevenMeterChessboard.clearSelection();
        tpChessboard.clearSelection();
        twelveMeterChessboard.setEnabled(enabled);
        sevenMeterChessboard.setEnabled(enabled);
        tpChessboard.setEnabled(enabled);
        if( !enabled) {
            ChessboardEntry[][] all = controller.getAntennasForOfflineChessboards();
            String name;
            for(int i=0; i < all[0].length; i++){
            	name = all[0][i].getDisplayName();
                event = new ChessboardStatusEvent(name,
                		SPAntennaStatus.OFFLINE,
                		controller.toolTipForAntenna(name));
                twelveMeterChessboard.processStatusChange(event);
            }
            
            for(int i=0; i < all[1].length; i++){
            	name = all[1][i].getDisplayName();
                event = new ChessboardStatusEvent(name,
                		SPAntennaStatus.OFFLINE,
                		controller.toolTipForAntenna(name));
               sevenMeterChessboard.processStatusChange(event);
            }
            
            for(int i=0; i < all[2].length; i++){
            	name = all[2][i].getDisplayName();
                event = new ChessboardStatusEvent(name,
                		SPAntennaStatus.OFFLINE,
                		controller.toolTipForAntenna(name));
                tpChessboard.processStatusChange(event);
            }
        }
    }
    

    public void prepareCreateArray(ArrayModeEnum mode){
        arrayMode = mode;
        disableChessboards(false);
        disableChessboards(true);
        GetAntennaThread ant = new GetAntennaThread();
        if(controller == null){
            logger.fine("crappy controller == null");
        }
        if(controller.getCS() == null){
            logger.fine("crappy CS== null");
        }
        Thread t = controller.getCS().getThreadFactory().newThread(ant);
        t.start();
    }
    // add this method call by Create/Destory array event but can not get the mode
    public void updateOnlineAntenna() {
    	GetAntennaThread ant = new GetAntennaThread();
        if(controller == null){
            logger.fine("crappy controller == null");
        }
        if(controller.getCS() == null){
            logger.fine("crappy CS== null");
        }
        Thread t = controller.getCS().getThreadFactory().newThread(ant);
        t.start();
    }
    
    
       

    private boolean createArray() {
    	// Get selected antennas from each chessboard and check that
    	// they are online (really, we should prevent selection of
    	// offline ones).
    	Collection<ChessboardEntry> antennas = new Vector<ChessboardEntry>();

    	ChessboardEntry[] ACSselected = twelveMeterChessboard.getSelectedEntries();
    	if (ACSselected != null) {
    		for (final ChessboardEntry antenna : ACSselected) {
    			if (antenna.getCurrentStatus() == SPAntennaStatus.ONLINE) {
    				antennas.add(antenna);
    			} else {
    				JOptionPane.showMessageDialog(this, 
    						"Selected Antenna not available",
    						"Antenna "+antenna.getDisplayName()+" is not available "+
    						"to be included in an array",
    						JOptionPane.ERROR_MESSAGE);
    				return false;
    			}
    		}
    	}

    	ChessboardEntry[] TPSelected = tpChessboard.getSelectedEntries();
    	if (TPSelected != null) {
    		for (final ChessboardEntry antenna : TPSelected) {
    			if (antenna.getCurrentStatus() == SPAntennaStatus.ONLINE) {
    				antennas.add(antenna);
    			} else {
    				JOptionPane.showMessageDialog(this, 
    						"Selected Antenna not available",
    						"Antenna "+antenna.getDisplayName()+" is not available "+
    						"to be included in an array",
    						JOptionPane.ERROR_MESSAGE);
    				return false;
    			}
    		}
    	}

    	ChessboardEntry[] ACASelected = sevenMeterChessboard.getSelectedEntries();
    	if (ACASelected != null) {
    		for (final ChessboardEntry antenna : ACASelected) {
    			if (antenna.getCurrentStatus() == SPAntennaStatus.ONLINE) {
    				antennas.add(antenna);
    			} else {
    				JOptionPane.showMessageDialog(this, 
    						"Selected Antenna not available",
    						"Antenna "+antenna.getDisplayName()+" is not available "+
    						"to be included in an array",
    						JOptionPane.ERROR_MESSAGE);
    				return false;
    			}
    		}
    	}

    	if (ACSselected != null) {
    		logger.info("Number of 12m antennas for new array: " + ACSselected.length );
    	}
    	if (TPSelected != null) {
    		logger.info("Number of TP antennas for new array: " + TPSelected.length );
    	}
    	if (ACASelected != null) {
    		logger.info("Number of 7m antennas for new array: " + ACASelected.length );
    	}
    	if (antennas.size() == 0){
    		JOptionPane.showMessageDialog(this, 
    				"No Antennas Selected",
    				"Please select at least one antenna to create array",
    				JOptionPane.ERROR_MESSAGE);
    		return false;
    	}
    	
    	// Get the optional photonic reference
    	String[] photonicsChoice = getSelectedLOPhotonics();
    	if (photonicsChoice.length > 0) {
        	logger.info(String.format("The selected photonic reference is: %s",
        			photonicsChoice[0]));
    	} else {
    		logger.info("None of the photonics is selected in CreateArray stage");
    	}
    	
    	
    	// Get the optional correlator
    	CorrelatorType correlator = getCorrelatorType();
    	logger.info(String.format("The selected correlator is: %s",
    			correlator));
    	
    	// Get the optional scheduling policy
    	SchedulingPolicyWrapper spw = getPolicy();
    	String policy = "";
    	if (spw == null) {
    		// No policy selected
        	logger.info("No scheduling policy is selected");
    	} else {
    		// There is a policy, so get its name
    		policy = spw.getSpringBeanName();
        	logger.info(String.format("The selected scheduling policy is: %s",
        			policy));
    	}

    	String arrayName;
    	disableCreateArrayPanel();
    	try {
    		arrayName = controller.createArray(arrayMode,
    				antennas,
    				photonicsChoice,
    				correlator,
    				policy);
    		allArrays.add(arrayName);
    	} catch (ControlInternalExceptionEx e) {
    		String acsErrorTrace = ErrorHandling.getNiceErrorTraceString(e.errorTrace);
    		CreateArrayErrorDialog dialog = new CreateArrayErrorDialog(this,
    				"Error creating array", "Make sure these antennas are really available to "+
    				"create this array. \n Also check state of Control "+
    				"System and its logs.\n\n", acsErrorTrace);
    		dialog.setVisible(true);
    		return false;
    	} catch (SchedulingInternalExceptionEx e) {
    		String acsErrorTrace = ErrorHandling.getNiceErrorTraceString(e.errorTrace);
    		CreateArrayErrorDialog dialog = new CreateArrayErrorDialog(this,
    				"Error creating array", "Make sure these antennas are really available to "+
    				"create this array. \n Also check state of Control "+
    				"System and its logs.\n\n", acsErrorTrace);
    		dialog.setVisible(true);
    		return false;
    	} catch (ACSInternalExceptionEx e) {
    		String acsErrorTrace = ErrorHandling.getNiceErrorTraceString(e.errorTrace);
    		CreateArrayErrorDialog dialog = new CreateArrayErrorDialog(this,
    				"Error creating array", "Make sure these antennas are really available to "+
    				"create this array. \n Also check state of Control "+
    				"System and its logs.\n\n", acsErrorTrace);
    		dialog.setVisible(true);
    		return false;
    	} catch(Exception e) {
    		CreateArrayErrorDialog dialog = new CreateArrayErrorDialog(this,
    				"Error creating array", "Make sure these antennas are really available to "+
    				"create this array. \n Also check state of Control "+
    				"System and its logs.\n\n", ErrorHandling.printedStackTrace(e));
    		dialog.setVisible(true);
    		return false;
    	}
    	
    	// We can automagically pop-up the array panel if the magic
    	// option is set. This is for testing, not the deployed
    	// environment.
    	if (SchedulingProperties.isAutoPopupArrayPlugin()) {
    		try {
    			ArrayPanel arrayPanel = new ArrayPanel(arrayName);
    			container.startChildPlugin(arrayName, arrayPanel);
    		} catch(Exception e) {
    			e.printStackTrace();
    			JOptionPane.showMessageDialog(this, e.toString() +
    					"\nProblem creating or displaying the array panel for " +
    					arrayName + "\n\n" +
    					ErrorHandling.printedStackTrace(e), 
    					"Error creating array panel", JOptionPane.ERROR_MESSAGE);
    			return false;
    		}
    	}
    	return true;
    }
    
    private String[] getSelectedLOPhotonics() {
    	 
    	 String[] selectCentralLO = {""};
    	 
    	 for (Enumeration<AbstractButton> selectedLO = photonicsGroup.getElements(); selectedLO.hasMoreElements();) {
    		 JRadioButton radiobutton= (JRadioButton)selectedLO.nextElement();
    		 if(radiobutton.isSelected()){
    			 selectCentralLO[0] = radiobutton.getText();
    		 }	 
    	 }
    	 
    	 if(selectCentralLO[0].length()<1){
    		 selectCentralLO=new String[0];
    	 }
    	 return selectCentralLO;
    }
    
    private CorrelatorType getCorrelatorType() {
    	final CorrelatorType result =
    		(CorrelatorType)correlatorType.getSelectedItem();
    	return result;
    }
    
    private SchedulingPolicyWrapper getPolicy() {
    	SchedulingPolicyWrapper result;
    	if (schedulingPolicy.isEnabled()) {
        	result = (SchedulingPolicyWrapper)schedulingPolicy.getSelectedItem();
        	if (result == null) {
        		result = null;
        	}
    	} else {
    		result = null;
    	}
    	return result;
    }

    class UpdateCB implements Runnable {
        private String[] vals;
        public UpdateCB(String[] v){
            vals = v;
        }
        public void run() {
        	//logger.info("method updateCB: available antennea"+vals.length);
            updateChessboard(vals);
        }
    }
    
    class UpdateLOPhotonics implements Runnable {
    	private String[] Photonics;
    	public UpdateLOPhotonics(String[] photonics) {
    		Photonics = photonics;
    	}
    	
    	public void run() {
    		updateAvailablePhotonics(Photonics);
    	}
    }
    
    class GetAntennaThread implements Runnable {
        public GetAntennaThread(){}
        public void run(){ 
            String[] onlineAntennasForChessboard = controller.getAntennasForActiveChessboards();
            //logger.info("GetAntennaThread:available antennas"+onlineAntennasForChessboard.length);
            javax.swing.SwingUtilities.invokeLater(new UpdateCB(onlineAntennasForChessboard));
            String[] available = controller.getAvailableCLOPhotonics();
            javax.swing.SwingUtilities.invokeLater(new UpdateLOPhotonics(available));
        }
    }

    class CreateArrayThread implements Runnable{
        public CreateArrayThread(){ }
        public void run(){
            if(createArray()) {
              //  exit();
                setEnabled(false);
                ((MainSchedTabPane)parent).resetMainViewButtons();
            }
            //reset buttons
            ((MainSchedTabPane)parent).resetMainViewButtons();
            //re-enable panel
            enableCreateArrayPanel();
        }
    }
    
    class LOActionListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
//          String choice = photonicsGroup.getSelection().getActionCommand();
          photonicsGroup.getSelection().setSelected(false);
          //System.out.println("ACTION Choice Selected: " + choice);
        }
      }
    
    class CheckControlReady implements Runnable {
    	
    	public CheckControlReady() {		
    	}
    	
    	public void run() {
    		boolean isControlReady = false;
    		while(!isControlReady) {
    			try {
    				Thread.sleep(3000);
    			}
    			catch (InterruptedException e) {
    				e.printStackTrace();
    			}
    			isControlReady= controller.getControlRef();
    			boolean isTMCDBReady = controller.getTMCDBComponent();
    			if(isControlReady && isTMCDBReady) {
    				isControlReady = true;
    			} else {
    				isControlReady = false;
    			}
    			
    			//logger.severe("check control interface...");
    		}
    	}
    	
    }

	@Override
	public void refreshPolicyList() {
		refreshComboBoxPolicies();
		if (policyPanel != null) {
			// Decided to pass on the notification from this parent GUI
			// rather than have the policyPanel listen for itself - it
			// seemed a bit of a hack to have policyPanel need to know
			// about the environment in which it operated (e.g. the
			// ContainerServices which it would need in order to set up
			// an offshoot callback thingy.
			policyPanel.refreshPolicyList();
		}
	}
	
	private synchronized void refreshComboBoxPolicies() {
    	final Vector<SchedulingPolicyWrapper> policies = new Vector<SchedulingPolicyWrapper>();
    	for (SchedulingPolicyFile policiesFile: master.getSchedulingPolicies()) {
    		for (String policy: policiesFile.schedulingPolicies) {
    			policies.add(new SchedulingPolicyWrapper(policiesFile, policy));
    		}
    	}
    	if (schedulingPolicy != null ) {
    		schedulingPolicy.removeAllItems();
    		for (SchedulingPolicyWrapper policy: policies)
    			schedulingPolicy.addItem(policy);
    	}
	}

}

