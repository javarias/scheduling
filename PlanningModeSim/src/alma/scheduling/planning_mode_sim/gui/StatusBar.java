package alma.scheduling.planning_mode_sim.gui;

import javax.swing.JPanel;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;

import alma.acs.gui.standards.StandardIcons;

public class StatusBar extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7315565234555246295L;
	/**
	 * 
	 */
	private JLabel onlineIconJLabel = null;
	private JLabel onlineTextJLabel = null;
	private JLabel simIconJLabel = null;
	private JLabel simTextJLabel = null;

	/**
	 * This method initializes 
	 * 
	 */
	public StatusBar() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		this.setBorder(BorderFactory.createEtchedBorder(
				EtchedBorder.RAISED));
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setSize(new Dimension(308, 19));
		this.add(getOnlineIconJLabel());
		this.add(getOnlineTextJLabel());
		Dimension minSize = new Dimension(5, 1);
		Dimension prefSize = new Dimension(5, 1);
		Dimension maxSize = new Dimension(Short.MAX_VALUE, 10);
		this.add(new Box.Filler(minSize, prefSize, maxSize));
		this.add(getSimIconJLabel());
		this.add(getSimTextJLabel());
		Dimension statusBarSize = getMinimumSize();
		statusBarSize.width = Short.MAX_VALUE;
		this.setMinimumSize(statusBarSize);
	}
	
	private JLabel getOnlineIconJLabel(){
		if( onlineIconJLabel == null ){
			onlineIconJLabel = new JLabel(StandardIcons.STATUS_UNKNOWN.icon);
		}
		return onlineIconJLabel;
	}
	
	private JLabel getOnlineTextJLabel(){
		if( onlineTextJLabel == null ){
			onlineTextJLabel = new JLabel("Offline");
		}
		return onlineTextJLabel;
	}
	
	private JLabel getSimIconJLabel(){
		if( simIconJLabel == null ){
			simIconJLabel = new JLabel(StandardIcons.ACTION_STOP.icon);
		}
		return simIconJLabel;
	}
	
	private JLabel getSimTextJLabel(){
		if( simTextJLabel == null ){
			simTextJLabel = new JLabel("Stand By");
		}
		return simTextJLabel;
	}
	
	public void notifySimStart(){
		simIconJLabel.setIcon(StandardIcons.ACTION_START.icon);
		simTextJLabel.setText("Simulating");
	}
	
	public void notifySimStop(){
		simIconJLabel.setIcon(StandardIcons.ACTION_STOP.icon);
		simTextJLabel.setText("Stand By");		
	}
	
	public void notifyOnline(){
		onlineIconJLabel.setIcon(StandardIcons.STATUS_OKAY.icon);
		onlineTextJLabel.setText("Online");
	}
	
	public void notifyOffline(){
		onlineIconJLabel.setIcon(StandardIcons.STATUS_UNKNOWN.icon);
		onlineTextJLabel.setText("Offline");
	}
}  //  @jve:decl-index=0:visual-constraint="65,166"
