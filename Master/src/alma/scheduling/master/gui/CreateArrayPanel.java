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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import alma.Control.CorrelatorType;
import alma.common.gui.chessboard.ChessboardEntry;
import alma.common.gui.chessboard.ChessboardPanel;
import alma.common.gui.chessboard.ChessboardStatusEvent;
import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.ArrayModeEnum;
import alma.scheduling.array.guis.ArrayPanel;
import alma.scheduling.utils.ErrorHandling;

public class CreateArrayPanel extends SchedulingPanelGeneralPanel {

    private String[] availableAntennas;
    private Vector<String> allArrays;
    private int columnIndex = 0;
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

    public void setOwner(JTabbedPane p){
        parent = p;
    }

    public void connectedSetup(PluginContainerServices cs) {
        super.onlineSetup(cs);
        logger = cs.getLogger();
        logger.fine("SECOND SETUP FOR ANTENNA PANEL");
        controller.secondSetup(cs);
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
        gridBagConstraints.gridwidth=gridBagConstraints.REMAINDER;
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
        cbPanel.add(createCorrelatorTypeComponent(), gridBagConstraints);
        
        chessboardPanel.add(cbPanel,BorderLayout.CENTER);
        chessboardPanel.add(createSouthPanel(),BorderLayout.SOUTH);
        validate();
    }

    private JPanel createTwelveMeterChessboard(ChessboardEntry[] foo) {
        ChessboardEntry[][] entries = new ChessboardEntry[5][10];
        int ctr=0;
        for(int i=0; i < 5;i++){
            for(int j=0; j < 10; j++){
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
        for(int i=0; i < 2;i++){
            for(int j=0; j < 6; j++){
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
         for(int j=0; j < 4; j++){
            entries[0][j] = foo[ctr++];
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

    private boolean isIn(int is, int[] in){
        for(int i=0; i < in.length; i++){
            if(in[i] == is) {
                return true;
            }
        }
        return false;
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
            event = new ChessboardStatusEvent(antNames[i], SPAntennaStatus.ONLINE);//, null);
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
            for(int i=0; i < all[0].length; i++){
                event = new ChessboardStatusEvent(all[0][i].getDisplayName(), SPAntennaStatus.OFFLINE);//, null);
                twelveMeterChessboard.processStatusChange(event);
            }
            
            for(int i=0; i < all[2].length; i++){
                event = new ChessboardStatusEvent(all[2][i].getDisplayName(), SPAntennaStatus.OFFLINE);//, null);
                tpChessboard.processStatusChange(event);
            }
        }
    }
    

    public void prepareCreateArray(ArrayModeEnum mode){
        arrayMode = mode;
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
    	//get selected antenns from chessboard
    	ChessboardEntry[] ACSselected = twelveMeterChessboard.getSelectedEntries();
    	int NumberOfantennaSelected=0 ;
    	//check the ones selected have online status
    	//temporary: change so that can't select ones not online
    	if(ACSselected!=null) {
    		for(int i=0; i <ACSselected.length; i++){
    			if(ACSselected[i].getCurrentStatus() != SPAntennaStatus.ONLINE){
    				JOptionPane.showMessageDialog(this, 
    						"Selected Antenna not available",
    						"Antenna "+ACSselected[i].getDisplayName()+" is not available "+
    						"to be included in an array", JOptionPane.ERROR_MESSAGE);
    				return false;
    			}
    		}
    		NumberOfantennaSelected = ACSselected.length;
    	}

    	ChessboardEntry[] TPSelected = tpChessboard.getSelectedEntries();
    	if(TPSelected!=null) {
    		for(int i=0; i <TPSelected.length; i++){
    			if(TPSelected[i].getCurrentStatus() != SPAntennaStatus.ONLINE){
    				JOptionPane.showMessageDialog(this, 
    						"Selected Antenna not available",
    						"Antenna "+TPSelected[i].getDisplayName()+" is not available "+
    						"to be included in an array", JOptionPane.ERROR_MESSAGE);
    				return false;
    			}
    		}
    		NumberOfantennaSelected = NumberOfantennaSelected+TPSelected.length;
    	}

    	ChessboardEntry[] selected = new ChessboardEntry[NumberOfantennaSelected];
    	if(ACSselected!=null && TPSelected==null){
    		System.arraycopy(ACSselected, 0, selected, 0, ACSselected.length);
    	} else if(ACSselected==null && TPSelected!=null) {
    		System.arraycopy(TPSelected, 0, selected, 0, TPSelected.length);
    	} else if(ACSselected!=null && TPSelected!=null) {
    		System.arraycopy(ACSselected, 0, selected, 0, ACSselected.length);
    		System.arraycopy(TPSelected, 0, selected, ACSselected.length, TPSelected.length);
    	}  else if(ACSselected==null && TPSelected==null) {
    		JOptionPane.showMessageDialog(this, 
    				"None Antenna Selected",
					"Please select at least one antenna to create array",JOptionPane.ERROR_MESSAGE);
    	}
    	
    	//for(int i=0;i<selected.length;i++) {
    	//	logger.info("index "+i+" : "+selected[i].getDisplayName());
    	//}
    	//get select LO photonic
    	//String selectPhotonic= selectRadioButton.getText();
    	String[] photonicsChoice = getSelectedLOPhotonics();
    	if(photonicsChoice.length>0){
    		logger.info("the selected photonics is:"+photonicsChoice[0]);
    	}
    	else {
    		logger.info("None of the photonics is selected in CreateArray stage");
    	}
    	CorrelatorType correlator = getCorrelatorType();
    	logger.info("The selected correlator is:" + correlator);

    	String arrayName;
    	disableCreateArrayPanel();
    	try {
    		arrayName = controller.createArray(arrayMode,
    				selected,
    				photonicsChoice,
    				correlator);
    		allArrays.add(arrayName);
    	} catch(Exception e) {
    		e.printStackTrace();
    		JOptionPane.showMessageDialog(this, e.toString()+
    				"\nMake sure these antennas are really available to "+
    				"create this array. Also check state of Control "+
    				"System and its logs.\n\n" +
    				ErrorHandling.printedStackTrace(e), 
    				"Error creating array", JOptionPane.ERROR_MESSAGE);
    		return false;
    	}
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
    	logger.fine(String.format(
    			"Correlator Type is %s (index = %d)",
    			result, result.value()
    	));
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
          String choice = photonicsGroup.getSelection().getActionCommand();
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

}

