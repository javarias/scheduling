/**
 * 
 */
package alma.scheduling.projectmanager;

import java.util.List;
import java.util.Vector;

import alma.entity.xmlbinding.projectstatus.ProjectStatus;

/**
 * @author dclarke
 *
 */
@SuppressWarnings("serial")
public class PSTableModel extends EntityTableModel {
	
	private List<ProjectStatus> statuses;
	
	public PSTableModel(StateArchiveDAO sa) {
		super(sa);
	}

	public void refresh() {
		this.statuses = new Vector<ProjectStatus>(sa.getAllProjectStatuses());
	}
	
	@Override
	public int getColumnCount() {
		return 6;
	}

	@Override
	public int getRowCount() {
		return statuses.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		final ProjectStatus ps = statuses.get(rowIndex);
		switch(columnIndex) {
			case 0:
				return ps.getName();
			case 1:
				return ps.getPI();
			case 2:
				return ps.getObsProjectRef().getEntityId();
			case 3:
				return ps.getProjectStatusEntity().getEntityId();
			case 4:
				return ps.getStatus().getState().toString();
			case 5:
				return ps.getTimeOfUpdate();
			default:
				return "UNKNOWN";
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public String getColumnName(int column) {
		// TODO Auto-generated method stub
		switch(column) {
			case 0:
				return "Name";
			case 1:
				return "P.I.";
			case 2:
				return "Project Id";
			case 3:
				return "Status Id";
			case 4:
				return "State";
			case 5:
				return "Updated";
			default:
				return "UNKNOWN";
		}
	}

	@Override
	public void setStatus(int row, String to) {
		final ProjectStatus ps = statuses.get(row);
		sa.setStatus(ps, to);
		refresh();
	}

	@Override
	public boolean canTransition(int row, String to) {
		final ProjectStatus status = statuses.get(row);
		final String from = status.getStatus().getState().toString();
		return sa.subsystemForProject(from, to) != null;
	}

	@Override
	public String getEntityType() {
		return "Project Status";
	}
}
