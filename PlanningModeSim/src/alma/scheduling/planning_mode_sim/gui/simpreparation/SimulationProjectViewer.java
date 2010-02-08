package alma.scheduling.planning_mode_sim.gui.simpreparation;

import javax.swing.JPanel;
import java.awt.Dimension;

import alma.acs.gui.standards.*;
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

import javax.swing.JTextField;

public class SimulationProjectViewer extends JPanel{

	private JPanel simulationJPanel;
	private JLabel simulationJLabel;
	private JComboBox simulationJComboBox;
	private JLabel simulationConf1JLabel;
	private JPanel observatoryJPanel;
	private JPanel dsaJPanel;
	private JPanel weatherPanel;
	private JPanel obsprojectsJPanel;
	private JLabel configurationJLabel;
	private JLabel observatoryJLabel;
	private JLabel observatoryConf1JLabel;
	private JLabel observatoryConf2JLabel;
	private JLabel observatoryConf3JLabel;
	private JLabel observatoryConf4JLabel;
	private JLabel dsaJLabel;
	private JLabel dsaConf1JLabel;
	private JLabel weatherJLabel;
	private JLabel weatherConfigJLabel;
	private JLabel obsprojectJLabel;
	private JLabel obsprojectConf1JLabel;

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
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setMinimumSize(new Dimension(350, 400));
        this.add(getSimulationJPanel());
        //this.add(observatoryJPanel);
        //this.add(dsaJPanel);
        //this.add(weatherPanel);
        //this.add(obsprojectsJPanel);
			
	}
	
	public JLabel getSimulationJLabel(){
		if( simulationJLabel == null){
			simulationJLabel = new JLabel("Select Simulation: ");
			simulationJLabel.setHorizontalAlignment(JLabel.RIGHT);
		}
		return simulationJLabel;
		
	}
	
	public JComboBox getSimulationComboBox(){
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
			simulationJLabel.setHorizontalAlignment(JLabel.RIGHT);
		}
		return simulationConf1JLabel;
		
	}
	
	public JPanel getSimulationJPanel(){
		if( simulationJPanel == null){
			simulationJPanel = new JPanel();
			simulationJPanel.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(0,10,0,10);
            c.fill = GridBagConstraints.NONE;
            c.weighty = 0;
            c.weightx = 0;
            c.gridheight = 1;
            c.gridwidth = 1;
            c.anchor = GridBagConstraints.LINE_END;
            c.gridx = 0; c.gridy = 0;
			this.add(getSimulationJLabel(), c);
			c.anchor = GridBagConstraints.LINE_START;
            c.gridx = 1;
			this.add(getSimulationComboBox(), c);
			c.anchor = GridBagConstraints.LINE_END;
			c.gridy = 1;
			this.add(getSimulationConf1JLabel(), c);
		}
		return simulationJPanel;		
		
	}




}  //  @jve:decl-index=0:visual-constraint="65,166"
