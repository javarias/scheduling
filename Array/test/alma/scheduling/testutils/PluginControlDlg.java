/*
 * ALMA - Atacama Large Millimiter Array (c) European Southern Observatory, 2007
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

/** 
 * @author  caproni   
 * @version $Id: PluginControlDlg.java,v 1.1 2010/07/27 16:50:28 rhiriart Exp $
 * @since    
 */

package alma.scheduling.testutils;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import alma.exec.extension.subsystemplugin.SubsystemPlugin;
import alma.exec.extension.subsystemplugin.IPauseResume;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import alma.acs.logging.AcsLogLevel;

/**
 * A dialog that allows controlling the plugin
 *
 */
public class PluginControlDlg extends JDialog implements ActionListener{
	// The plugin to control
	private SubsystemPlugin plugin;
	private IPauseResume pausablePlugin=null;
	
	private JButton startB =new JButton("Start");
	private JButton stopB = new JButton("Stop");
	private JButton pauseB= new JButton("Pause");
	private JButton resumeB=new JButton("Resume");
	
	private Logger logger;
	
	/**
	 * Constructor 
	 * 
	 * @param thePlugin The plugin to send commands to
	 * @param log The logger
	 * @param p The position of the plugin window
	 */
	public PluginControlDlg(SubsystemPlugin thePlugin,Logger log,Rectangle p) {
		super();
		if (thePlugin==null) {
			throw new IllegalArgumentException("Invalid null SubsystemPlugin in constructor");
		}
		if (log==null) {
			throw new IllegalArgumentException("Invalid null Logger in constructor");
		}
		if (p==null) {
			throw new IllegalArgumentException("Invalid null Point in constructor");
		}
		
		plugin=thePlugin;
		logger=log;
		initialize(p);
	}
	
	/**
	 * Init the GUI
	 * 
	 * @param p The position of the main GUI
	 */
	private void initialize(Rectangle p) {
		setTitle("Plugin control");
		setModal(false);
		GridLayout layout = new GridLayout(4,1);
		layout.setVgap(5);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setLayout(layout);
		getContentPane().add(startB);
		getContentPane().add(stopB);
		getContentPane().add(pauseB);
		getContentPane().add(resumeB);
		
		startB.addActionListener(this);
		stopB.addActionListener(this);
		pauseB.addActionListener(this);
		resumeB.addActionListener(this);
		
		chekButtons();
		pack();
		
		Rectangle bounds = getBounds();
		int x= p.x-bounds.width-10;
		if (x<0) {
			x=25;
		}
		int y=p.y;
		setLocation(x,y);
		
		setVisible(true);
	}
	
	/**
	 * Enable/disable pause and resume if the plugin does not
	 * implements the interface
	 *
	 */
	private void chekButtons() {
		//Class[] interfaces = plugin.getClass().getInterfaces();
		if (plugin instanceof IPauseResume) {
			pausablePlugin=(IPauseResume)plugin;
		}
		pauseB.setEnabled(pausablePlugin!=null);
		resumeB.setEnabled(pausablePlugin!=null);
	}
	
	/**
	 * Show an exception
	 *
	 */
	private void reportException(Throwable t) {
		String msg = "<HTML>Exception caught: <EM>"+t.getMessage()+"</EM></HTML";
		t.printStackTrace(System.err);
		JOptionPane.showMessageDialog(this,msg,"Exception",JOptionPane.ERROR_MESSAGE);
		logger.log(AcsLogLevel.ERROR,"Exception from plugin: "+t.getMessage());
	}
	
	public void actionPerformed(ActionEvent e) {
		try {
		if (e.getSource()==pauseB) {
			logger.log(AcsLogLevel.INFO,"Pausing the plugin");
			pausablePlugin.pause();
			logger.log(AcsLogLevel.INFO,"Plugin paused");
		} else if (e.getSource()==resumeB) {
			logger.log(AcsLogLevel.INFO,"Resuminging the plugin");
			pausablePlugin.resume();
			logger.log(AcsLogLevel.INFO,"Plugin resumed");
		} else if (e.getSource()==startB) {
			logger.log(AcsLogLevel.INFO,"Starting the plugin");
			plugin.start();
			logger.log(AcsLogLevel.INFO,"Plugin started");
		} else if (e.getSource()==stopB) {
			logger.log(AcsLogLevel.INFO,"Stopping the plugin");
			plugin.stop();
			logger.log(AcsLogLevel.INFO,"Plugin stopped");
		} else {
			System.err.println("Unknow source of event: "+e);
		}
		} catch (Throwable t) {
			reportException(t);
			return;
		}
		JOptionPane.showMessageDialog(this,"Operation completed OK","Plugin report",JOptionPane.INFORMATION_MESSAGE);
	}
}
