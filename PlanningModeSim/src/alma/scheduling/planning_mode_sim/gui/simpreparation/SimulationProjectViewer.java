package alma.scheduling.planning_mode_sim.gui.simpreparation;

import alma.scheduling.planning_mode_sim.controller.Controler;

import alma.acs.gui.standards.*;

import javax.swing.JPanel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JButton;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;

import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.Box.Filler;
import javax.swing.border.TitledBorder;
import javax.swing.SwingConstants;


public class SimulationProjectViewer extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3958217885611268216L;
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
	private JPanel jPanel = null;
	private JPanel jPanel1 = null;
	private JPanel jPanel2 = null;
	private JPanel jPanel3 = null;
	private JPanel jPanel4 = null;

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
        this.add(getJPanel(), null);
        Dimension minSize = new Dimension(10, 10);
		Dimension prefSize = new Dimension(10, 10);
		Dimension maxSize = new Dimension(10, 10);
		this.add(new Box.Filler(minSize, prefSize, maxSize));
        this.add(getJPanel1(), null);
        this.add(new Box.Filler(minSize, prefSize, maxSize));
        this.add(getJPanel2(), null);
        this.add(new Box.Filler(minSize, prefSize, maxSize));
        this.add(getJPanel3(), null);
        this.add(new Box.Filler(minSize, prefSize, maxSize));
        this.add(getJPanel4(), null);
        this.add(new Box.Filler(minSize, prefSize, maxSize));
        this.add(getStartSimulationJButton(), null);
        this.add(getJPanel4(), null);

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
			simulationConf1JLabel = new JLabel("<html><font color='blue'><u>Basic configuration...</u></font></html></html>");
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
			observatoryConf1JLabel = new JLabel("<html><font color='blue'><u>Antennas capabilities...</u></font></html>");
			observatoryConf1JLabel.setHorizontalAlignment(JLabel.RIGHT);
		}
		return observatoryConf1JLabel;
	}
	
	public JLabel getObservatoryConf2JLabel(){
		if( observatoryConf2JLabel == null){
			observatoryConf2JLabel = new JLabel("<html><font color='blue'><u>Array configurations...</u></font></html>");
			observatoryConf2JLabel.setHorizontalAlignment(JLabel.RIGHT);
		}
		return observatoryConf2JLabel;
	}
	
	public JLabel getObservatoryConf3JLabel(){
		if( observatoryConf3JLabel == null){
			observatoryConf3JLabel = new JLabel("<html><font color='blue'><u>Antenna to pad mapping...</u></font></html>");
			observatoryConf3JLabel.setHorizontalAlignment(JLabel.RIGHT);
		}
		return observatoryConf3JLabel;
	}
	
	public JLabel getObservatoryConf4JLabel(){
		if( observatoryConf4JLabel == null){
			observatoryConf4JLabel = new JLabel("<html><font color='blue'><u>Basic configuration...</u></font></html>");
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
			dsaConf1JLabel = new JLabel("<html><font color='blue'><u>Configure DSA...</u></font></html>");
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
			weatherConf1JLabel = new JLabel("<html><font color='blue'><u>Configure simulation model and randomization...</u></font></html>");
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
			obsprojectConf1JLabel = new JLabel("<html><font color='blue'><u>Change science rating...</u></font></html>");
			obsprojectConf1JLabel.setHorizontalAlignment(JLabel.RIGHT);
		}
		return obsprojectConf1JLabel;
	}

	public JButton getStartSimulationJButton(){
		if( startSimulationJButton == null){
			startSimulationJButton = new JButton("Start Simulation", StandardIcons.ACTION_START.icon);
			startSimulationJButton.setHorizontalAlignment(SwingConstants.RIGHT);
			startSimulationJButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					Controler.getControler().getParentWindow().startSimulation();
				}
			});
		}
		return startSimulationJButton;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(4, 4, 4, 4);
			jPanel.setLayout(new GridBagLayout());
			jPanel.setBorder(BorderFactory.createTitledBorder(null, "Simulation configurations", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
			c.gridx = 0; c.gridy = 0;
			c.anchor = GridBagConstraints.LINE_END;
			jPanel.add(getSimulationJLabel(), c);
			c.gridx = 1; c.gridy = 0;
			c.anchor = GridBagConstraints.LINE_START;
			c.fill = GridBagConstraints.HORIZONTAL;
			jPanel.add(getSimulationJComboBox(), c);
			c.gridwidth = 2;
			c.gridx = 0; c.gridy = 1;
			c.anchor = GridBagConstraints.LINE_END;
			jPanel.add(getSimulationConf1JLabel(), c);
		}
		return jPanel;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(4, 4, 4, 4);
			jPanel1.setLayout(new GridBagLayout());
			jPanel1.setBorder(BorderFactory.createTitledBorder(null, "Observatory characteristics configurations", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
			c.gridx = 0; c.gridy = 0;
			c.anchor = GridBagConstraints.LINE_END;
			jPanel1.add(getObservatoryJLabel(), c);
			c.gridx = 1; c.gridy = 0;
			c.anchor = GridBagConstraints.LINE_START;
			c.fill = GridBagConstraints.HORIZONTAL;
			jPanel1.add(getObservatoryJComboBox(), c);
			c.gridwidth = 2;
			c.gridx = 0; c.gridy = 1;
			c.anchor = GridBagConstraints.LINE_END;
			jPanel1.add(getObservatoryConf1JLabel(), c);
			c.gridx = 0; c.gridy = 2;
			jPanel1.add(getObservatoryConf2JLabel(), c);
			c.gridx = 0; c.gridy = 3;
			jPanel1.add(getObservatoryConf3JLabel(), c);
			c.gridx = 0; c.gridy = 4;
			jPanel1.add(getObservatoryConf4JLabel(), c);
		}
		return jPanel1;
	}

	/**
	 * This method initializes jPanel2	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(4, 4, 4, 4);
			jPanel2 = new JPanel();
			jPanel2.setLayout(new GridBagLayout());
			jPanel2.setBorder(BorderFactory.createTitledBorder(null, "DSA configurations", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
			c.gridx = 0; c.gridy = 0;
			c.anchor = GridBagConstraints.LINE_END;
			jPanel2.add(getDsaJLabel(), c);
			c.gridx = 1; c.gridy = 0;
			c.anchor = GridBagConstraints.LINE_START;
			c.fill = GridBagConstraints.HORIZONTAL;
			jPanel2.add(getDsaJComboBox(), c);
			c.gridx = 0; c.gridy = 1;
			c.gridwidth = 2;
			c.anchor = GridBagConstraints.LINE_END;
			jPanel2.add(getDsaConf1JLabel(), c);
		}
		return jPanel2;
	}

	/**
	 * This method initializes jPanel3	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel3() {
		if (jPanel3 == null) {
			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(4, 4, 4, 4);
			jPanel3 = new JPanel();
			jPanel3.setLayout(new GridBagLayout());
			jPanel3.setBorder(BorderFactory.createTitledBorder(null, "Weather configurations", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
			c.gridx = 0; c.gridy = 0;
			c.anchor = GridBagConstraints.LINE_END;
			jPanel3.add(getWeatherJLabel(), c);
			c.gridx = 1; c.gridy = 0;
			c.anchor = GridBagConstraints.LINE_START;
			c.fill = GridBagConstraints.HORIZONTAL;
			jPanel3.add(getWeatherJComboBox(), c);
			c.gridx = 0; c.gridy = 1;
			c.gridwidth = 2;
			c.anchor = GridBagConstraints.LINE_END;
			jPanel3.add(getWeatherConf1JLabel(), c);
		}
		return jPanel3;
	}

	/**
	 * This method initializes jPanel4	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel4() {
		if (jPanel4 == null) {
			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(4, 4, 4, 4);
			jPanel4 = new JPanel();
			jPanel4.setLayout(new GridBagLayout());
			jPanel4.setBorder(BorderFactory.createTitledBorder(null, "Observation projects configurations", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
			c.gridx = 0; c.gridy = 0;
			c.anchor = GridBagConstraints.LINE_END;
			jPanel4.add(getObsprojectJLabel(), c) ;
			c.gridx = 1; c.gridy = 0;
			c.anchor = GridBagConstraints.LINE_START;
			c.fill = GridBagConstraints.HORIZONTAL;
			jPanel4.add(getObsprojectJComboBox(), c);
			c.gridx = 0; c.gridy = 1;
			c.gridwidth = 2;
			c.anchor = GridBagConstraints.LINE_END;
			jPanel4.add(getObsprojectConf1JLabel(), c);
		}
		return jPanel4;
	}


}  //  @jve:decl-index=0:visual-constraint="65,166"
