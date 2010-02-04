package alma.scheduling.planning_mode_sim.gui.simpreparation;

import javax.swing.JFrame;
import java.awt.Dimension;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JWindow;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JMenu;

import alma.acs.gui.standards.*;

public class MainWindow extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6277902498253584548L;
	private JMenuBar jJMenuBar = null;
	private JMenu jMenu = null;
	private JMenu jMenu1 = null;
	private JMenu jMenu2 = null;
	private JMenu jMenu3 = null;
	private JMenuItem jMenuItem = null;
	private JMenuItem jMenuItem1 = null;
	private JMenuItem jMenuItem2 = null;
	private JMenuItem jMenuItem3 = null;
	private JMenuItem jMenuItem4 = null;
	private JMenu jMenu4 = null;
	/**
	 * This method initializes 
	 * 
	 */
	public MainWindow() {
		super();
		GuiStandards.enforce();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setSize(new Dimension(344, 227));
        this.setJMenuBar(getJJMenuBar());
        this.setTitle("Planning Mode Simulator");
			
	}

	/**
	 * This method initializes jJMenuBar	
	 * 	
	 * @return javax.swing.JMenuBar	
	 */
	private JMenuBar getJJMenuBar() {
		if (jJMenuBar == null) {
			jJMenuBar = new JMenuBar();
			jJMenuBar.add(getJMenu());
			jJMenuBar.add(getJMenu1());
			jJMenuBar.add(getJMenu2());
			jJMenuBar.add(getJMenu3());
			jJMenuBar.add(getJMenu4());
		}
		return jJMenuBar;
	}

	/**
	 * This method initializes jMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getJMenu() {
		if (jMenu == null) {
			jMenu = new JMenu();
			jMenu.setText("File");
			jMenu.add(getJMenuItem());
			jMenu.add(getJMenuItem1());
			jMenu.add(getJMenuItem2());
			jMenu.add(getJMenuItem3());
			jMenu.add(getJMenuItem4());
		}
		return jMenu;
	}

	/**
	 * This method initializes jMenu1	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getJMenu1() {
		if (jMenu1 == null) {
			jMenu1 = new JMenu();
			jMenu1.setText("Edit");
		}
		return jMenu1;
	}

	/**
	 * This method initializes jMenu2	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getJMenu2() {
		if (jMenu2 == null) {
			jMenu2 = new JMenu();
			jMenu2.setText("View");
		}
		return jMenu2;
	}

	/**
	 * This method initializes jMenu3	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getJMenu3() {
		if (jMenu3 == null) {
			jMenu3 = new JMenu();
			jMenu3.setText("Simulation");
		}
		return jMenu3;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getJMenuItem() {
		if (jMenuItem == null) {
			jMenuItem = new JMenuItem();
			jMenuItem.setText("New");
		}
		return jMenuItem;
	}

	/**
	 * This method initializes jMenuItem1	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getJMenuItem1() {
		if (jMenuItem1 == null) {
			jMenuItem1 = new JMenuItem();
			jMenuItem1.setText("Open");
		}
		return jMenuItem1;
	}

	/**
	 * This method initializes jMenuItem2	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getJMenuItem2() {
		if (jMenuItem2 == null) {
			jMenuItem2 = new JMenuItem();
			jMenuItem2.setText("Save");
			jMenuItem2.setIcon(StandardIcons.ACTION_SAVE.icon);
		}
		return jMenuItem2;
	}

	/**
	 * This method initializes jMenuItem3	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getJMenuItem3() {
		if (jMenuItem3 == null) {
			jMenuItem3 = new JMenuItem();
			jMenuItem3.setText("Save as...");
			jMenuItem3.setIcon(StandardIcons.ACTION_SAVE.icon);
		}
		return jMenuItem3;
	}

	/**
	 * This method initializes jMenuItem4	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getJMenuItem4() {
		if (jMenuItem4 == null) {
			jMenuItem4 = new JMenuItem();
			jMenuItem4.setText("Exit");
		}
		return jMenuItem4;
	}

	/**
	 * This method initializes jMenu4	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getJMenu4() {
		if (jMenu4 == null) {
			jMenu4 = new JMenu();
			jMenu4.setText("Help");
		}
		return jMenu4;
	}
	
	

}  //  @jve:decl-index=0:visual-constraint="202,145"
