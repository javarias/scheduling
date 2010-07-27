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
		ObsProjectGuiController ctrl = new ObsProjectGuiController(model, table, area);
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
