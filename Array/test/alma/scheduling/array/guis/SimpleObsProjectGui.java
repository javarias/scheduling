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
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.table.TableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleObsProjectGui extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory.getLogger(SimpleObsProjectGui.class);
		
	public SimpleObsProjectGui() {
		setLayout(new BorderLayout());
		ObsProjectModel model = new ObsProjectModel();
		TableModel tblmodel = new SimpleObsProjectTableModel(model);
		ObsProjectTable table = new ObsProjectTable(tblmodel);
		JTextArea area = new JTextArea();
		area.setPreferredSize(new Dimension(450, 150));
		area.setMinimumSize(new Dimension(450, 150));
		//ObsProjectGuiController ctrl = new ObsProjectGuiController(model, table, area);
		add(new JScrollPane(table), BorderLayout.NORTH);
		add(area, BorderLayout.SOUTH);
	}
	
	public static void main(String[] args) {
		
		SimpleObsProjectGui gui = new SimpleObsProjectGui();
		
		JFrame frame = new JFrame("ObsProject Simple Table");
		frame.getContentPane().add(gui);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500, 400);
		frame.setVisible(true);
	}
}
