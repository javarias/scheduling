package alma.scheduling.planning_mode_sim.gui.simpreparation;

import javax.swing.JPanel;
import java.awt.Dimension;

import alma.acs.gui.standards.*;
import alma.scheduling.planning_mode_sim.controller.Controler;
import alma.scheduling.planning_mode_sim.gui.SimulationProgress;

import javax.swing.BoxLayout;
import javax.swing.JInternalFrame;
import java.awt.BorderLayout;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.JTextArea;
import javax.swing.JEditorPane;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;
import java.awt.FlowLayout;
import javax.swing.JButton;
import java.awt.GridLayout;
import java.awt.CardLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.JTextField;

import cl.utfsm.samplingSystemUI.DataPrinter;

public class SimulationProjectViewer extends JPanel{

	private JLabel simulationJLabel;
	private JComboBox simulationJComboBox;
	private JLabel simulationConf1JLabel;
	private JLabel observatoryJLabel;
	private JComboBox observatoryJComboBox;
	private JLabel observatoryConf1JLabel;
	private JLabel observatoryConf2JLabel;
	private JLabel observatoryConf3JLabel;
	private JLabel observatoryConf4JLabel;
	private JLabel dsaJLabel;
	private JComboBox dsaJComboBox;
	private JLabel dsaConf1JLabel;
	private JLabel weatherJLabel;
	private JComboBox weatherJComboBox;
	private JLabel weatherConf1JLabel;
	private JLabel obsprojectJLabel;
	private JComboBox obsprojectJComboBox;
	private JLabel obsprojectConf1JLabel;
	private JButton startSimulationJButton;

	/**
	 * This method initializes 
	 * 
	 */
	public SimulationProjectViewer() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5,5,5,5);
        c.fill = GridBagConstraints.NONE;
        c.weightx = 1; c.weighty = 1;
        
        // Simulation configs
        c.gridwidth = 1; c.gridheight = 1;
        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 0; c.gridy = 0;
        this.add(getSimulationJLabel(), c);
		
		c.anchor = GridBagConstraints.LINE_START;
		c.gridwidth = 1;
		c.gridx = 1; c.gridy = 0;
		this.add(getSimulationJComboBox(), c);
		
		c.anchor = GridBagConstraints.LINE_END;
		c.gridwidth = 2;
		c.gridx = 0; c.gridy = 1;
		this.add(getSimulationConf1JLabel(), c);
		
		// Observatory Configs
		c.anchor = GridBagConstraints.LINE_END;
		c.gridwidth = 1;
		c.gridx = 0; c.gridy = 2;
		this.add(getObservatoryJLabel(), c);
		
		c.anchor = GridBagConstraints.LINE_START;
		c.gridwidth = 1;
		c.gridx = 1; c.gridy = 2;
		this.add(getObservatoryJComboBox(), c);
		
		c.anchor = GridBagConstraints.LINE_END;
		c.gridwidth = 2;
		c.gridx = 0; c.gridy = 3;
		this.add(getObservatoryConf1JLabel(), c);
		c.gridx = 0; c.gridy = 4;
		this.add(getObservatoryConf2JLabel(), c);
		c.gridx = 0; c.gridy = 5;
		this.add(getObservatoryConf3JLabel(), c);
		c.gridx = 0; c.gridy = 6;
		this.add(getObservatoryConf4JLabel(), c);
		
		// Dsa Configs
		c.anchor = GridBagConstraints.LINE_END;
		c.gridwidth = 1;
		c.gridx = 0; c.gridy = 7;
		this.add(getDsaJLabel(), c);
		
		c.anchor = GridBagConstraints.LINE_START;
		c.gridwidth = 1;
		c.gridx = 1; c.gridy = 7;
		this.add(getDsaJComboBox(), c);
		
		c.anchor = GridBagConstraints.LINE_END;
		c.gridwidth = 2;
		c.gridx = 0; c.gridy = 8;
		this.add(getDsaConf1JLabel(), c);
		
		// Weather Configs
		c.anchor = GridBagConstraints.LINE_END;
		c.gridwidth = 1;
		c.gridx = 0; c.gridy = 9;
		this.add(getWeatherJLabel(), c);
		
		c.anchor = GridBagConstraints.LINE_START;
		c.gridwidth = 1;
		c.gridx = 1; c.gridy = 9;
		this.add(getWeatherJComboBox(), c);
		
		c.anchor = GridBagConstraints.LINE_END;
		c.gridwidth = 2;
		c.gridx = 0; c.gridy = 10;
		this.add(getWeatherConf1JLabel(), c);
		
		// Obsproject Configs
		c.anchor = GridBagConstraints.LINE_END;
		c.gridwidth = 1;
		c.gridx = 0; c.gridy = 11;
		this.add(getObsprojectJLabel(), c);
		
		c.anchor = GridBagConstraints.LINE_START;
		c.gridwidth = 1;
		c.gridx = 1; c.gridy = 11;
		this.add(getObsprojectJComboBox(), c);
		
		c.anchor = GridBagConstraints.LINE_END;
		c.gridwidth = 2;
		c.gridx = 0; c.gridy = 12;
		this.add(getObsprojectConf1JLabel(), c);
		
		c.anchor = GridBagConstraints.LINE_END;
		c.gridwidth = 1;
		c.gridx = 1; c.gridy = 13;
		this.add(getStartSimulationJButton(), c);
	}
	
	public JLabel getSimulationJLabel(){
		if( simulationJLabel == null){
			simulationJLabel = new JLabel("Select simulation mode: ");
			simulationJLabel.setHorizontalAlignment(JLabel.RIGHT);
		}
		return simulationJLabel;
	}
	
	public JComboBox getSimulationJComboBox(){
		if( simulationJComboBox == null){
			simulationJComboBox = new JComboBox();
			simulationJComboBox.addItem("Offline Simulation");
			simulationJComboBox.addItem("Online Simulation");
		}
		return simulationJComboBox;
	}
	
	public JLabel getSimulationConf1JLabel(){
		if( simulationConf1JLabel == null){
			simulationConf1JLabel = new JLabel("Basic configuration...");
			simulationConf1JLabel.setHorizontalAlignment(JLabel.RIGHT);
		}
		return simulationConf1JLabel;
		
	}
	
	public JLabel getObservatoryJLabel(){
		if( observatoryJLabel == null){
			observatoryJLabel = new JLabel("Select observatory charateristics source: ");
			observatoryJLabel.setHorizontalAlignment(JLabel.RIGHT);
		}
		return observatoryJLabel;
	}
	
	public JComboBox getObservatoryJComboBox(){
		if( observatoryJComboBox == null){
			observatoryJComboBox = new JComboBox();
			observatoryJComboBox.addItem("ATF");
			observatoryJComboBox.addItem("ALMA");
			observatoryJComboBox.addItem("ALMA+ACA");
		}
		return observatoryJComboBox;
	}
	
	public JLabel getObservatoryConf1JLabel(){
		if( observatoryConf1JLabel == null){
			observatoryConf1JLabel = new JLabel("Antennas capabilities...");
			observatoryConf1JLabel.setHorizontalAlignment(JLabel.RIGHT);
		}
		return observatoryConf1JLabel;
	}
	
	public JLabel getObservatoryConf2JLabel(){
		if( observatoryConf2JLabel == null){
			observatoryConf2JLabel = new JLabel("Array configurations...");
			observatoryConf2JLabel.setHorizontalAlignment(JLabel.RIGHT);
		}
		return observatoryConf2JLabel;
	}
	
	public JLabel getObservatoryConf3JLabel(){
		if( observatoryConf3JLabel == null){
			observatoryConf3JLabel = new JLabel("Antenna to pad mapping...");
			observatoryConf3JLabel.setHorizontalAlignment(JLabel.RIGHT);
		}
		return observatoryConf3JLabel;
	}
	
	public JLabel getObservatoryConf4JLabel(){
		if( observatoryConf4JLabel == null){
			observatoryConf4JLabel = new JLabel("Basic configuration...");
			observatoryConf4JLabel.setHorizontalAlignment(JLabel.RIGHT);
		}
		return observatoryConf4JLabel;
	}
	
	public JLabel getDsaJLabel(){
		if( dsaJLabel == null){
			dsaJLabel = new JLabel("Select DSA weights source: ");
			dsaJLabel.setHorizontalAlignment(JLabel.RIGHT);
		}
		return dsaJLabel;
	}
	
	public JComboBox getDsaJComboBox(){
		if( dsaJComboBox == null){
			dsaJComboBox = new JComboBox();
			dsaJComboBox.addItem("Executive Ranker only");
			dsaJComboBox.addItem("Executive+Weather Ranker");
		}
		return dsaJComboBox;
	}
	
	public JLabel getDsaConf1JLabel(){
		if( dsaConf1JLabel == null){
			dsaConf1JLabel = new JLabel("Configure DSA...");
			dsaConf1JLabel.setHorizontalAlignment(JLabel.RIGHT);
		}
		return dsaConf1JLabel;
	}
	
	public JLabel getWeatherJLabel(){
		if( weatherJLabel == null){
			weatherJLabel = new JLabel("Select weather data source: ");
			weatherJLabel.setHorizontalAlignment(JLabel.RIGHT);
		}
		return weatherJLabel;
	}
	
	public JComboBox getWeatherJComboBox(){
		if( weatherJComboBox == null){
			weatherJComboBox = new JComboBox();
			weatherJComboBox.addItem("Good weather");
			weatherJComboBox.addItem("Normal weather");
			weatherJComboBox.addItem("Bad weather");
		}
		return weatherJComboBox;
	}
	
	public JLabel getWeatherConf1JLabel(){
		if( weatherConf1JLabel == null){
			weatherConf1JLabel = new JLabel("Configure simulation model and randomization...");
			weatherConf1JLabel.setHorizontalAlignment(JLabel.RIGHT);
		}
		return weatherConf1JLabel;
	}
	
	public JLabel getObsprojectJLabel(){
		if( obsprojectJLabel == null){
			obsprojectJLabel = new JLabel("Select observation projects data source: ");
			obsprojectJLabel.setHorizontalAlignment(JLabel.RIGHT);
		}
		return obsprojectJLabel;
	}
	
	public JComboBox getObsprojectJComboBox(){
		if( obsprojectJComboBox == null){
			obsprojectJComboBox = new JComboBox();
			obsprojectJComboBox.addItem("Salsacia/Conservia");
			obsprojectJComboBox.addItem("EEUU/UE/Japan/Chile");
		}
		return obsprojectJComboBox;
	}
	
	public JLabel getObsprojectConf1JLabel(){
		if( obsprojectConf1JLabel == null){
			obsprojectConf1JLabel = new JLabel("Change science rating...");
			obsprojectConf1JLabel.setHorizontalAlignment(JLabel.RIGHT);
		}
		return obsprojectConf1JLabel;
	}

	public JButton getStartSimulationJButton(){
		if( startSimulationJButton == null){
			startSimulationJButton = new JButton("Start Simulation", StandardIcons.ACTION_START.icon);
			startSimulationJButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					Controler.getControler().getParentWindow().startSimulation();
				}
			});
		}
		return startSimulationJButton;
	}


}  //  @jve:decl-index=0:visual-constraint="65,166"
