package alma.scheduling.planning_mode_sim.gui;

import javax.swing.JFrame;
import java.awt.Dimension;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFileChooser;
import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JWindow;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import alma.acs.gui.standards.*;
import alma.scheduling.planning_mode_sim.controller.Controler;
import alma.scheduling.planning_mode_sim.gui.simpreparation.SimulationProjectViewer;

public class MainWindow extends JFrame implements WindowListener{

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
	
	public JFileChooser jFileChooser = null;
	
	//TODO: Missing "Close Window" listener to handle exit.
	

	
	/**
	 * This method initializes 
	 * 
	 */
	public MainWindow() {
		super();
		//GuiStandards.enforce();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setUIFont(new FontUIResource("Sans", 0, 10));
        this.setMinimumSize(new Dimension(350, 400));
        this.setJMenuBar(getJJMenuBar());
        this.setTitle("Planning Mode Simulator");
        this.pack();			
	}
	
	public static void setUIFont(javax.swing.plaf.FontUIResource f) {
		//
		// sets the default font for all Swing components.
		// ex.
		// setUIFont (new javax.swing.plaf.FontUIResource
		// ("Serif",Font.ITALIC,12));
		//
		java.util.Enumeration keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource)
				UIManager.put(key, f);
		}
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
			jMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					//TODO: Implement "Are you sure..."
					Controler.getControler().createNew();
					Controler.getControler().getParentWindow().setContentPane(new SimulationProjectViewer());
					Controler.getControler().getParentWindow().pack();	
				}
			});
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
			jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// Obtain the FileChooser dialog
					// TODO: Implement "Are you sure you want to loss changes"
					JFileChooser fc = Controler.getControler().getParentWindow().getFileChooser();
					int returnVal = fc.showOpenDialog(
											Controler.getControler().getParentWindow()
											);
			        if (returnVal == JFileChooser.APPROVE_OPTION) {
			        	//Tell the controller to lead the selected file
			            Controler.getControler().load(fc.getSelectedFile());
			            //TODO: This is where a real application would open the file.
			            //log.append("Opening: " + file.getName() + "." + newline);
			        } else {
			            //TODO: log.append("Open command cancelled by user." + newline);
			        }

				}
			});
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
			jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("actionPerformed()"); // TODO Auto-generated Event stub actionPerformed()
					if( Controler.getControler().getFilename() != ""){
						Controler.getControler().save();
					}else{
						JFileChooser fc = Controler.getControler().getParentWindow().getFileChooser();
						int returnVal = fc.showSaveDialog(
												Controler.getControler().getParentWindow()
												);

				        if (returnVal == JFileChooser.APPROVE_OPTION) {
				        	Controler.getControler().saveAs(fc.getSelectedFile().toString());
				            //This is where a real application would open the file.
				            //log.append("Opening: " + file.getName() + "." + newline);
				        } else {
				            //log.append("Open command cancelled by user." + newline);
				        }
					}
						
				}
			});
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
			jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("actionPerformed()"); // TODO Auto-generated Event stub actionPerformed()
					JFileChooser fc = Controler.getControler().getParentWindow().getFileChooser();
					int returnVal = fc.showSaveDialog(
											Controler.getControler().getParentWindow()
											);

			        if (returnVal == JFileChooser.APPROVE_OPTION) {
			        	Controler.getControler().saveAs(fc.getSelectedFile().toString());
			            //This is where a real application would open the file.
			            //log.append("Opening: " + file.getName() + "." + newline);
			        } else {
			            //log.append("Open command cancelled by user." + newline);
			        }
				}
			});
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
			jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Controler.getControler().exit();
				}
			});
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
	
	public JFileChooser getFileChooser(){
		if ( jFileChooser == null)
			this.jFileChooser = new JFileChooser();
		return this.jFileChooser;
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		// TODO Auto-generated method stub
		Controler.getControler().exit();
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	

}  //  @jve:decl-index=0:visual-constraint="97,119"
