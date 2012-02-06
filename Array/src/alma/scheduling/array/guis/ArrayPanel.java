/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
package alma.scheduling.array.guis;

import java.awt.BorderLayout;
import java.io.Serializable;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.ArraySchedulerMode;
import alma.scheduling.utils.ErrorHandling;

/**
 * This class is the main panel shown by the OMC. It contains a
 * {@code CurrentActivityPanel} and a {@code InteractivePanel}
 * 
 * @author Jorge Avarias <javarias[at]nrao.edu>
 *
 */
public class ArrayPanel extends AbstractArrayPanel {

	private static final long serialVersionUID = -7832483714456316810L;
	private CurrentActivityPanel cup = null;
	private InteractivePanel ip = null;
	private JSplitPane splitPane = null;
	private String arrayMode = null;
	private JLabel title;
	
	
	public ArrayPanel(String arrayName) {
		super(arrayName);
		this.arrayMode = "";
		this.setLayout(new BorderLayout());
		initialize();
	}
	
	public ArrayPanel() {
		super();
		this.arrayMode = "";
		this.setLayout(new BorderLayout());
		initialize();
	}
	
	private void initialize(){
		cup = new CurrentActivityPanel();
		ip = new InteractivePanel();
		
		JPanel left = new JPanel(new BorderLayout());
		JPanel right = new JPanel(new BorderLayout());
		right.add(cup, BorderLayout.CENTER);
		left.add(ip, BorderLayout.CENTER);
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(450);
		cup.setSize(400, 800);
		ip.setSize(400, 800);
		this.add(splitPane, BorderLayout.CENTER);
		
		title = new JLabel();
		setTitle("", "Initializing");
		this.add(title, BorderLayout.NORTH);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.array.guis.AbstractArrayPanel#stop()
	 */
	@Override
	public void stop() throws Exception {
		if (cup != null) {
			cup.stop();
		}
		if (ip != null) {
			ip.stop();
		}
	}
	
	private void setTitle(String arrayName, String arrayMode) {
		title.setText(String.format(
				"<html><font size=\"5\" color=%s>%s %s</font></html>",
				ArrayPanel.TitleColour,
				arrayMode,
				arrayName
		));
	}


	/* (non-Javadoc)
	 * @see alma.exec.extension.subsystemplugin.SubsystemPlugin#setServices(alma.exec.extension.subsystemplugin.PluginContainerServices)
	 */
	@Override
	public void setServices(PluginContainerServices services) {
		System.out.format("%s.setServices, arrayName = %s%n",
				this.getClass().getSimpleName(),
				services.getSessionProperties().getArrayName());
		super.setServices(services);
		cup.setServices(services);
		ip.setServices(services);
	}

	private void setModesInTitle(final ArraySchedulerMode[] modes) {
		final String suffix = "I";
		final StringBuilder b = new StringBuilder();
		String sep = "";
		for (final ArraySchedulerMode mode : modes) {
			String[] parts = mode.toString().split("_");
			for (final String part : parts) {
				if (!part.equals(suffix)) {
					b.append(sep);
					b.append(part.charAt(0));
					b.append(part.substring(1).toLowerCase());
					sep = ", ";
				}
			}
		}
		this.arrayMode = b.toString();
		setTitle(this.arrayName, this.arrayMode);
	}
	
	@Override
	protected void arrayAvailable() {
		setModesInTitle(array.getModes());
		cup.setArray(array);
		cup.arrayAvailable();
		ip.setArray(array);
		ip.arrayAvailable();
	}

	@Override
	protected void modelsAvailable() {
		cup.setModelAccessor(models);
		cup.modelsAvailable();
		ip.setModelAccessor(models);
		ip.modelsAvailable();
	}
	
	private static ArrayPanel createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Array Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add the ubiquitous "Hello World" label.
        ArrayPanel panel = new ArrayPanel("Array");
        frame.getContentPane().add(panel, BorderLayout.CENTER);

        //Display the window.
        frame.pack();
        frame.setSize(800, 600);
        frame.setVisible(true);
        return panel;
    }
	
	public static void main(String[] args) {
    	javax.swing.SwingUtilities.invokeLater(new Runnable() {
    		public void run() {
    			ArrayPanel p = createAndShowGUI();
    			try {
    				p.runRestricted(false);
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    		}
    	});
    }

	
	
	/*
	 * ================================================================
	 * IStateKeeping implementation
	 * ================================================================
	 */
	@Override
	public Serializable getState() {
		final ArrayPanelState state = new ArrayPanelState();

		state.setCurrentActivityPanelState(cup.getState());
		state.setInteractivePanelState(ip.getState());
		
    	state.setSplitPaneDividerLocation(calculateSplitPaneDividerLocation(splitPane));
    	
		return state;
	}

	@Override
	public void setState(Serializable inState) throws Exception {
		logger.info(String.format("%s.setState(Serializable)",
				this.getClass().getSimpleName()));
		if (inState == null) {
			logger.info("\tinState is null, returning");
			return;
		}
		final ArrayPanelState state = (ArrayPanelState) inState;

		splitPane.setDividerLocation(state.getSplitPaneDividerLocation());
	}
	/*
	 * End IStateKeeping implementation
	 * ============================================================= */
}
