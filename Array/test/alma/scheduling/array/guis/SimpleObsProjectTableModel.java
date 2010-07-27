package alma.scheduling.array.guis;

import javax.swing.table.AbstractTableModel;

import alma.scheduling.datamodel.obsproject.ObsProject;

public class SimpleObsProjectTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	private String[] headings = {"ID", "Principal Investigator"};
	private ObsProjectModel model;

	public SimpleObsProjectTableModel(ObsProjectModel model) {
		this.model = model;
	}
	
	@Override
	public int getRowCount() {
		return model.getProjects().size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		ObsProject prj = model.getProjects().get(rowIndex);
		if (columnIndex == 0)
			return prj.getId();
		else if (columnIndex == 1)
			return prj.getPrincipalInvestigator();
		return null;
	}
	
	@Override
	public String getColumnName(int columnIndex) {
		return headings[columnIndex];
	}

}
