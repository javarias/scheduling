package alma.scheduling.array.guis;

import java.awt.Dimension;

import javax.swing.JTable;
import javax.swing.table.TableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObsProjectTable extends JTable {

	private static Logger logger = LoggerFactory.getLogger(ObsProjectTable.class);
	private static final long serialVersionUID = 1L;
		
	public ObsProjectTable(TableModel model) {
		super(model);
		setPreferredScrollableViewportSize(new Dimension(450, 200));
	}
	
}
