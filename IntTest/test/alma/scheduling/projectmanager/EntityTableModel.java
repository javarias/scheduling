/**
 * 
 */
package alma.scheduling.projectmanager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.sbstatus.SBStatus;

/**
 * @author dclarke
 *
 */
@SuppressWarnings("serial")
public abstract class EntityTableModel extends AbstractTableModel {
	
	protected StateArchiveDAO sa;
	
	public EntityTableModel(StateArchiveDAO sa) {
		this.sa = sa;
		refresh();
	}

	public abstract void refresh();
	public abstract void setStatus(int row, String to);
	public abstract boolean canTransition(int row, String to);
	public abstract String getEntityType();
}
