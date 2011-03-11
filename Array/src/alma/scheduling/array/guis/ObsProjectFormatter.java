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

package alma.scheduling.array.guis;

import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.entity.xmlbinding.ousstatus.OUSStatusChoice;
import alma.entity.xmlbinding.ousstatus.OUSStatusRefT;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.entity.xmlbinding.sbstatus.SBStatusRefT;
import alma.scheduling.array.util.StatusCollection;
import alma.scheduling.datamodel.obsproject.ObsProject;

/**
 *
 * @author dclarke
 * $Id: ObsProjectFormatter.java,v 1.1 2011/03/11 00:06:34 dclarke Exp $
 */
public class ObsProjectFormatter extends EntityFormatter {
	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	private ObsProjectFormatter() {
		super();
	}
	/* End Construction
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Utilities
	 * ================================================================
	 */
	private void formatNames(ObsProject op) {
		startTR();
		th("Name");
		td(op.getName(), 3);
		endTR();
		startTR();
		th("Version");
		td(op.getVersion(), 3);
		endTR();
		startTR();
		th("PI Name");
		td(op.getPrincipalInvestigator(), 3);
		endTR();
	}
	
	private void formatKnownStatus(ObsProject op, ProjectStatus ps) {
		startTR();
		th("Status");
		td(ps.getStatus().getState().toString(), 3);
		endTR();
		startTR();
		th("Entity ID");
		td(op.getUid(), 3);
		endTR();
		startTR();
		th("Status ID");
		td(op.getStatusEntity().getEntityId(), 3);
		endTR();
	}
	
	private void formatUnknownStatus(ObsProject op, String reason) {
		startTR();
		td("Unknown status", 2);
		td(String.format("(%s)", reason), 2);
		endTR();
		startTR();
		th("Entity ID");
		td(op.getUid(), 3);
		endTR();
	}
	
	private void formatTypes(ObsProject op) {
		startTR();
		th("CSV:");
		if (op.getCsv()) {
			tdItalic("True");
		} else {
			tdItalic("False");
		}
		th("Manual:");
		if (op.getManual()) {
			tdItalic("True");
		} else {
			tdItalic("False");
		}
		endTR();
	}
	
	private void sbStatus(SBStatus sbStatus) {
		startLI();
		buffer.append("SchedBlock ");
		buffer.append(sbStatus.getSchedBlockRef().getEntityId());
		buffer.append(" ");
		buffer.append(sbStatus.getStatus().getState().toString());
		endLI();
	}
	
	private void recursiveHierarchy(OUSStatus        ousStatus,
			                        StatusCollection statuses) {
		startLI();
		buffer.append("ObsUnitSet ");
		buffer.append(ousStatus.getObsUnitSetRef().getPartId());
		buffer.append(" ");
		buffer.append(ousStatus.getStatus().getState().toString());
		endLI();
		startUL();
		final OUSStatusChoice choice = ousStatus.getOUSStatusChoice();
		for (final OUSStatusRefT childRef : choice.getOUSStatusRef()) {
			final String ousId = childRef.getEntityId();
			final OUSStatus child = statuses.getOUSStatus(ousId);
			recursiveHierarchy(child, statuses);
		}
		for (final SBStatusRefT childRef : choice.getSBStatusRef()) {
			final String ousId = childRef.getEntityId();
			final SBStatus child = statuses.getSBStatus(ousId);
			sbStatus(child);
		}
		endUL();
	}
	
	private void formatHierarchy(StatusCollection statuses) {
		h4("Status Hierarchy");
		final ProjectStatus ps = statuses.getProjectStatus();
		final String ousId = ps.getObsProgramStatusRef().getEntityId();
		final OUSStatus ousStatus = statuses.getOUSStatus(ousId);
		
		startUL();
		recursiveHierarchy(ousStatus, statuses);
		endUL();
	}
	/* End Utilities
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Formatting
	 * ================================================================
	 */
	public static String formatted(ObsProject op, StatusCollection statuses) {
		final ObsProjectFormatter f = new ObsProjectFormatter();
		
		f.startHTML();
		
		f.startTable();
		
		f.formatNames(op);
		f.formatKnownStatus(op, statuses.getProjectStatus());
		f.formatTypes(op);
		
		f.endTable();

		f.formatHierarchy(statuses);
		
		f.endHTML();
		
		return f.toString();
	}
	
	public static String formatted(ObsProject op, String reason) {
		final ObsProjectFormatter f = new ObsProjectFormatter();
		
		f.startHTML();
		
		f.startTable();
		
		f.formatNames(op);
		f.formatUnknownStatus(op, reason);
		f.formatTypes(op);
		
		f.endTable();

		f.endHTML();
		
		return f.toString();
	}
	/* End Construction
	 * ============================================================= */
}
