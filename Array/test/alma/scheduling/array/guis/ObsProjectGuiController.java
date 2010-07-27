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
