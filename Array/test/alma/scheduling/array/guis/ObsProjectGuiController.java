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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.datamodel.obsproject.ObsUnit;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.SchedBlock;

public class ObsProjectGuiController {

	private static Logger logger = LoggerFactory.getLogger(ObsProjectGuiController.class);

	private ObsProjectModel model;
	private ObsProjectTable table;
	private JTextArea area;
	
	public ObsProjectGuiController(ObsProjectModel model, ObsProjectTable table, JTextArea area) {
		this.model = model;
		this.table = table;
		this.area = area;
		table.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {}
			
			@Override
			public void mousePressed(MouseEvent e) {}
			
			@Override
			public void mouseExited(MouseEvent e) {}
			
			@Override
			public void mouseEntered(MouseEvent e) {}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				displaySelectedObsProject();
			}
		});
	}
	
	private void displaySelectedObsProject() {
		int row = table.getSelectedRow();
		logger.info("selected row: " + row);
		ObsUnit ou = model.getObsUnitForProject(model.getObsProjectAt(row));
		if (ou instanceof ObsUnitSet) {
			ObsUnitSet ous = (ObsUnitSet) ou;
			logger.info("ObsUnit is a ObsUnitSet");
			String txt = "ObsUnit is a ObsUnitSet\n" +
				"# of SchedBlocks " + ous.getObsUnits().size() + "\n";
			txt += "SchedBlocks:\n";
			int i = 1;
			for (ObsUnit sou : ous.getObsUnits()) {
				SchedBlock sb = (SchedBlock) sou;
				model.hydrateSchedBlock(sb);
				txt += "# " + i++ + "\n";
				txt += "rank: " + sb.getScienceRank() +  "\n";
				txt += "repr source: " + sb.getSchedulingConstraints().getRepresentativeTarget().getSource().getName();
			}
			area.setText(txt);
		} else if (ou instanceof SchedBlock) {			
			logger.info("ObsUnit is a SchedBlock");
			area.setText("ObsUnit is a SchedBlock\n");			
		}
	}
}
