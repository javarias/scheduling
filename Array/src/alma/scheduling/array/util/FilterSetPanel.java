/* ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2010
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 */

package alma.scheduling.array.util;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.PatternSyntaxException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import alma.scheduling.array.guis.ObsProjectTableModel;

/**
 *
 * @author dclarke
 * $Id: FilterSetPanel.java,v 1.2 2010/07/29 15:55:39 dclarke Exp $
 */
@SuppressWarnings("serial")
public class FilterSetPanel extends JPanel {
	/*
	 * ================================================================
	 * Constants
	 * ================================================================
	 */
	final static Color okColour = Color.black;
	final static Color errorColour = Color.red;
	final static Color workingColour = Color.white;
	final static Color idleColour = Color.lightGray;
	/* End Constants
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Fields for widgets &c
	 * ================================================================
	 */
	/** The FilterSet we're editing */
	private FilterSet filterSet;
	
	/** A backup in case the user cancels the edit */
	private FilterSet backup;
	
	/** The labels for the filters we're editing */
	private JLabel[] labels;
	
	/** Checkboxes to enable/disable the filters we're editing */
	private JCheckBox[] enabled;
	
	/** The text fields for the filters we're editing */
	private JTextField[] regexps;
	
	/** OK button */
	private JButton okButton;
	
	/** Revert button */
	private JButton revertButton;
	
	/** Reset button */
	private JButton resetButton;
	/* End Fields for widgets &c
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Constructors and GUI building
	 * ================================================================
	 */
	/**
	 * Basic constructor.
	 */
	public FilterSetPanel(FilterSet filterSet) {
		super();
		this.filterSet = filterSet;
		checkPoint();
		createWidgets();
		createLayoutManager();
		addWidgets();
	}
	
	/**
	 * Build the LayoutManager we plan to use.
	 */
	private void createLayoutManager() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}
	
	/**
	 * Create a button with the given label and tooltip.
	 * 
	 * @param label
	 * @param tooltip
	 * @return
	 */
	private JButton newButton(String label, String tooltip) {
		final JButton result = new JButton(label);
		result.setToolTipText(tooltip);
		return result;
	}
	
	/**
	 * Create those widgets which we want to keep track of.
	 * Don't sweat that the model of the FilterSet is that it has
	 * columns but we map them to rows on the panel.
	 */
	private void createWidgets() {
		final int rows = filterSet.size();
		labels = new JLabel[rows];
		enabled = new JCheckBox[rows];
		regexps = new JTextField[rows];
		
		for (int i = 0; i < rows; i++) {
			final String label = filterSet.getName(i);

			labels[i] = new JLabel(filterSet.getName(i));
			enabled[i] = new JCheckBox();
			enabled[i].setSelected(filterSet.isActive(i));
			regexps[i] = new JTextField(filterSet.getFilter(i), 30);
			
			enabled[i].addActionListener(enableListener(i));
			enabled[i].setToolTipText("If checked, this filter will be used");
			regexps[i].addKeyListener(regexpsListener(i));
			regexps[i].setToolTipText(String.format(
					"The regular expression to use to select projects by %s.",
					label));
		}
		
		okButton     = newButton("OK",     "Apply the filters and exit this dialogue");
		okButton.addActionListener(okButtonListener());
		revertButton = newButton("Revert", "Undo all changes since the dialogue was opened");
		revertButton.addActionListener(revertButtonListener());
		resetButton  = newButton("Reset",  "Reset the filters to their defaults");
		resetButton.addActionListener(resetButtonListener());
		
		enableWidgets();
	}

	/**
	 * Creates and returns an ActionListener which will deal with
	 * events on the checkbox in the indicated row.
	 * @param i
	 * @return
	 */
	private ActionListener enableListener(final int i) {
		final ActionListener result = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final boolean b = enabled[i].isSelected();
//				regexps[i].setEnabled(b);
				if (b) {
					filterSet.activate(i);
				} else {
					filterSet.deactivate(i);
				}
				enableWidgets(i);
			}};
		return result;
	}

	/**
	 * Creates and returns a KeyListener which will deal with
	 * events on the text field in the indicated row.
	 * @param i
	 * @return
	 */
	private KeyListener regexpsListener(final int i) {
		final KeyListener result = new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				enabled[i].setSelected(true);
				filterSet.activate(i);
				try {
					filterSet.setFilter(i, regexps[i].getText());
					regexps[i].setForeground(okColour);
				} catch (PatternSyntaxException pse) {
					regexps[i].setForeground(errorColour);
				}
				enableWidgets(i);
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}};
		return result;
	}

	/**
	 * Add the widgets to the display.
	 */
	private void addWidgets() {
		final JPanel             rowPanel = new JPanel();
		final GridBagLayout      l        = new GridBagLayout();
		final GridBagConstraints c        = new GridBagConstraints();
		
		c.gridheight = 1;
		c.weighty = 1.0;
		c.ipadx = 2;
		c.ipady = 2;
		c.fill = GridBagConstraints.BOTH;
		rowPanel.setLayout(l);

		for (int i = 0; i < filterSet.size(); i++) {
			c.gridy = i;

			c.gridx = 0;
			c.anchor = GridBagConstraints.EAST;
			c.weightx = 0.0;
			l.setConstraints(labels[i], c);
			rowPanel.add(labels[i]);
			
			c.gridx++;
			c.anchor = GridBagConstraints.CENTER;
			c.weightx = 1.0;
			l.setConstraints(regexps[i], c);
			rowPanel.add(regexps[i]);
			
			c.gridx++;
			c.anchor = GridBagConstraints.WEST;
			c.weightx = 0.0;
			l.setConstraints(enabled[i], c);
			rowPanel.add(enabled[i]);
		}
		
		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(okButton);
		buttonPanel.add(resetButton);
		buttonPanel.add(revertButton);

		this.add(new JScrollPane(rowPanel));
		this.add(buttonPanel);
	}

	/**
	 * Create the actions listener for the OK button
	 * 
	 * @return
	 */
	private ActionListener okButtonListener() {
		final ActionListener result = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				checkPoint();
				setVisible(false);
			}};
		return result;
	}
	
	/**
	 * Create the actions listener for the Reset button
	 * 
	 * @return
	 */
	public void reset() {
		filterSet.reset();
		filterSet.deactivate();
		refreshWidgets();
	}
	
	/**
	 * Create the actions listener for the Reset button
	 * 
	 * @return
	 */
	private ActionListener resetButtonListener() {
		final ActionListener result = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				reset();
			}};
		return result;
	}
	
	/**
	 * Create the actions listener for the Revert button
	 * 
	 * @return
	 */
	private ActionListener revertButtonListener() {
		final ActionListener result = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				filterSet.restore(backup);
				refreshWidgets();
			}};
		return result;
	}
	/* End Constructors and GUI building
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * GUI Management
	 * ================================================================
	 */
	/**
	 * Remember the current filter set so we can, if necessary, back up
	 * to it.
	 */
	private void checkPoint() {
		this.backup = new FilterSet(this.filterSet);
	}
	
	/**
	 * Enable and disable widgets appropriately
	 */
	private void enableWidgets(int i) {
		if (enabled[i].isSelected()) {
			regexps[i].setBackground(workingColour);
		} else {
			regexps[i].setBackground(idleColour);
		}
	}
	
	/**
	 * Enable and disable widgets appropriately
	 */
	private void enableWidgets() {
		for (int i = 0; i < labels.length; i++) {
			enableWidgets(i);
		}
	}
	
	/**
	 * Reset the contents of the widgets from the filterSet
	 */
	private void refreshWidgets() {
		for (int i = 0; i < labels.length; i++) {
			enabled[i].setSelected(filterSet.isActive(i));
			regexps[i].setText(filterSet.getFilter(i));
		}
		enableWidgets();
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#setVisible(boolean)
	 */
	public void setVisible(boolean b) {
		Frame frame = JOptionPane.getFrameForComponent(this);
		if (b) {
			refreshWidgets();
		}
		frame.setVisible(b);
	}
	/* End GUI Management
	 * ============================================================= */

	

	/*
	 * ================================================================
	 * Running stand-alone
	 * ================================================================
	 */
    public static FilterSetPanel createGUI(String title, FilterSet filters) {
        //Create and set up the window.
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        //Add the stuff.
        FilterSetPanel panel = new FilterSetPanel(filters);
        frame.getContentPane().add(panel);

        frame.pack();
        panel.setVisible(false);
        
        return panel;
    }

	/**
	 * @param args
	 */
   public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	final FilterSetPanel panel = createGUI("Filter Panel", 
                		new FilterSet(
                				new ObsProjectTableModel()));
            	panel.setVisible(true);
            }
        });
    }
	/*
	 * End Running stand-alone
	 * ============================================================= */
}
