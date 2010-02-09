package alma.scheduling.planning_mode_sim.gui;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import alma.acs.gui.standards.StandardIcons;

public class SimulationProgress extends JPanel{

	private static final long serialVersionUID = 503892596824428170L;
	private JButton stopJButton = null;
	private JLabel simulationRunningJLabel = null;
	private JLabel pleaseWaitJLabel = null;
	private JProgressBar progressJProgressBar = null;
	/**
	 * This method initializes 
	 * 
	 */
	public SimulationProgress() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		Dimension minSize = new Dimension(1, 5);
		Dimension prefSize = new Dimension(1, 5);
		Dimension maxSize = new Dimension(10, Short.MAX_VALUE);
		this.add(new Box.Filler(minSize, prefSize, maxSize));
		
		this.add(getSimulationRunningJLabel());
		
		minSize = new Dimension(1, 20);
		prefSize = new Dimension(1, 20);
		maxSize = new Dimension(1, 20);
		this.add(new Box.Filler(minSize, prefSize, maxSize));
		
		this.add(getPleaseWaitJLabel());
		minSize = new Dimension(1, 30);
		prefSize = new Dimension(1, 30);
		maxSize = new Dimension(1, 30);
		this.add(new Box.Filler(minSize, prefSize, maxSize));
		this.add(getProgressJProgressBar());
		minSize = new Dimension(1, 40);
		prefSize = new Dimension(1, 40);
		maxSize = new Dimension(1, 40);
		this.add(new Box.Filler(minSize, prefSize, maxSize));
		
		this.add(getStopJButton());
		
		minSize = new Dimension(1, 5);
		prefSize = new Dimension(1, 5);
		maxSize = new Dimension(10, Short.MAX_VALUE);
		this.add(new Box.Filler(minSize, prefSize, maxSize));
	}
	
	private JButton getStopJButton(){
		if( stopJButton == null ){
			stopJButton = new JButton("Stop", StandardIcons.ACTION_STOP.icon );
			stopJButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		}
		return stopJButton;
	}
	
	private JLabel getSimulationRunningJLabel(){
		if( simulationRunningJLabel == null ){
			simulationRunningJLabel = new JLabel("Simulation Running...");
			simulationRunningJLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			simulationRunningJLabel.setFont(simulationRunningJLabel.getFont().deriveFont((float) 24.0));
		}
		return simulationRunningJLabel;
	}
	
	private JLabel getPleaseWaitJLabel(){
		if( pleaseWaitJLabel == null ){
			pleaseWaitJLabel = new JLabel("Please Wait.");
			pleaseWaitJLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			pleaseWaitJLabel.setFont(pleaseWaitJLabel.getFont().deriveFont((float) 24.0));
		}
		return pleaseWaitJLabel;
	}
	
	private JProgressBar getProgressJProgressBar(){
		if( progressJProgressBar == null ){
			progressJProgressBar = new JProgressBar(0,100);
			progressJProgressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
		}
		return progressJProgressBar;
	}	

}  //  @jve:decl-index=0:visual-constraint="65,166"
