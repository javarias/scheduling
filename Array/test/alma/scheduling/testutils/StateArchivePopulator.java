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
package alma.scheduling.testutils;

import java.util.Date;
import java.util.List;

import alma.entity.xmlbinding.obsproject.ObsProjectRefT;
import alma.entity.xmlbinding.obsproposal.ObsProposalRefT;
import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.entity.xmlbinding.ousstatus.OUSStatusEntityT;
import alma.entity.xmlbinding.ousstatus.OUSStatusRefT;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.projectstatus.ProjectStatusEntityT;
import alma.entity.xmlbinding.projectstatus.ProjectStatusRefT;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.entity.xmlbinding.sbstatus.SBStatusEntityT;
import alma.entity.xmlbinding.schedblock.SchedBlockRefT;
import alma.entity.xmlbinding.valuetypes.StatusT;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.obops.utils.DatetimeUtils;
import alma.scheduling.array.guis.ModelAccessor;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnit;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.SchedBlock;

public class StateArchivePopulator {

	private ModelAccessor modelAccessor;
	
	public StateArchivePopulator() throws Exception {
		modelAccessor = new ModelAccessor();
	}
	
	public static ProjectStatus createProjectStatus(String prjStatusUID, String obsUnitSetStatusUID, 
			String obsProjectUID, String obsProposalUID)
		throws Exception {
		ProjectStatus opStatus = new ProjectStatus();
		String now = DatetimeUtils.formatAsIso(new Date());
		// StatusBase parent class
		StatusT status = new StatusT();
		status.setState(StatusTStateType.PHASE2SUBMITTED);
		status.setStartTime(now);
		status.setEndTime(now);
		status.setReadyTime(now);
		opStatus.setStatus(status);
		opStatus.setTimeOfUpdate(now);
		// ObsProject reference
		ObsProjectRefT ref = new ObsProjectRefT();
		ref.setEntityId(obsProjectUID);
		ref.setEntityTypeName("ObsProject");
		ref.setDocumentVersion("1.0");
		ref.setPartId("X1");
		opStatus.setObsProjectRef(ref);
		// ObsProposal reference
		ObsProposalRefT obsPropRef = new ObsProposalRefT();
		obsPropRef.setEntityId(obsProposalUID);
		obsPropRef.setEntityTypeName("ObsProposal");
		obsPropRef.setDocumentVersion("1.0");
		obsPropRef.setPartId("X1");
		opStatus.setObsProposalRef(obsPropRef);
		// ObsUnitSetStatus reference
		OUSStatusRefT ousRef = new OUSStatusRefT();
		ousRef.setEntityId(obsUnitSetStatusUID);
		ousRef.setEntityTypeName("ObsProposal");
		ousRef.setDocumentVersion("1.0");
		ousRef.setPartId("X1");
		opStatus.setObsProgramStatusRef(ousRef);
		// Its own entity
		ProjectStatusEntityT psEnt = new ProjectStatusEntityT();
		psEnt.setEntityId(prjStatusUID);
		psEnt.setEntityIdEncrypted(prjStatusUID);
		psEnt.setEntityTypeName("ProjectStatus");
		psEnt.setDocumentVersion("1.0");
		psEnt.setDatamodelVersion("1.0");
		psEnt.setSchemaVersion("1.0");
		psEnt.setTimestamp(now);
		opStatus.setProjectStatusEntity(psEnt);
		// Its own attributes
		opStatus.setName("name");
		opStatus.setPI("pi");
		opStatus.setBreakpointTime(now);
		opStatus.setProjectWasTimedOut(now);
		return opStatus;		
	}

	public static OUSStatus createOUSStatus(String obsUnitSetStatusUID, String projectStatusUID,
			String obsUnitSetUID) {
		String now = DatetimeUtils.formatAsIso(new Date());
		OUSStatus ousStatus = new OUSStatus();
		// StatusBase parent class
		StatusT status = new StatusT();
		status.setState(StatusTStateType.PHASE2SUBMITTED);
		status.setStartTime(now);
		status.setEndTime(now);
		status.setReadyTime(now);
		ousStatus.setStatus(status);
		ousStatus.setTimeOfUpdate(now);
		// ObsUnitStatus parent class
		ousStatus.setObsUnitSetMemberType("");
		ousStatus.setTotalRequiredTimeInSec(1800);
		ousStatus.setTotalUsedTimeInSec(0);
		// ProjectStatus reference
		ProjectStatusRefT prjStatusRef = new ProjectStatusRefT();
		prjStatusRef.setEntityId(projectStatusUID);
		prjStatusRef.setEntityTypeName("ProjectStatus");
		prjStatusRef.setDocumentVersion("1.0");
		prjStatusRef.setPartId("X1");		
		ousStatus.setProjectStatusRef(prjStatusRef);
		// ObsUnitSet reference
		ObsProjectRefT obsPrjRef = new ObsProjectRefT();
		obsPrjRef.setEntityId(obsUnitSetUID);
		obsPrjRef.setEntityTypeName("ObsProject");
		obsPrjRef.setDocumentVersion("1.0");
		obsPrjRef.setPartId("X1");
		ousStatus.setObsUnitSetRef(obsPrjRef);
		// Its own entity
		OUSStatusEntityT ousEnt = new OUSStatusEntityT();
		ousEnt.setEntityId(obsUnitSetStatusUID);
		ousEnt.setEntityIdEncrypted(obsUnitSetStatusUID);
		ousEnt.setEntityTypeName("ProjectStatus");
		ousEnt.setDocumentVersion("1.0");
		ousEnt.setDatamodelVersion("1.0");
		ousEnt.setSchemaVersion("1.0");
		ousEnt.setTimestamp(now);		
		ousStatus.setOUSStatusEntity(ousEnt);
		// its own attributes
		ousStatus.setNumberSBsFailed(0);
		ousStatus.setTotalObsUnitSets(1);
		ousStatus.setNumberObsUnitSetsCompleted(0);
		ousStatus.setNumberObsUnitSetsFailed(0);
		ousStatus.setTotalSBs(0);
		ousStatus.setNumberSBsCompleted(0);
		return ousStatus;
	}
	
	public static SBStatus createSBStatus(String sbStatusUID, String parentOUSStatusUID,
			String projectStatusUID, String containingObsUnitSetUID,
			String schedBlockUID) {
		String now = DatetimeUtils.formatAsIso(new Date());
		SBStatus sbStatus = new SBStatus();
		// StatusBase parent class
		StatusT status = new StatusT();
		status.setState(StatusTStateType.PHASE2SUBMITTED);
		status.setStartTime(now);
		status.setEndTime(now);
		status.setReadyTime(now);
		sbStatus.setStatus(status);
		sbStatus.setTimeOfUpdate(now);
		// ObsUnitStatus parent class
		sbStatus.setObsUnitSetMemberType("");
		sbStatus.setTotalRequiredTimeInSec(1800);
		sbStatus.setTotalUsedTimeInSec(0);
		// ProjectStatus reference
		ProjectStatusRefT prjStatusRef = new ProjectStatusRefT();
		prjStatusRef.setEntityId(projectStatusUID);
		prjStatusRef.setEntityTypeName("ProjectStatus");
		prjStatusRef.setDocumentVersion("1.0");
		prjStatusRef.setPartId("X1");		
		sbStatus.setProjectStatusRef(prjStatusRef);
		// Containing ObsUnitSet reference
		OUSStatusRefT containingObsUnitSetRef = new OUSStatusRefT();
		containingObsUnitSetRef.setEntityId(containingObsUnitSetUID);
		containingObsUnitSetRef.setEntityTypeName("OUSStatus");
		containingObsUnitSetRef.setDocumentVersion("1.0");
		containingObsUnitSetRef.setPartId("X1");		
		sbStatus.setContainingObsUnitSetRef(containingObsUnitSetRef);
		// SchedBlock reference
		SchedBlockRefT schedBlockRef = new SchedBlockRefT();
		schedBlockRef.setEntityId(schedBlockUID);
		schedBlockRef.setEntityTypeName("ObsProject");
		schedBlockRef.setDocumentVersion("1.0");
		schedBlockRef.setPartId("X1");
		sbStatus.setSchedBlockRef(schedBlockRef);
		// Its own entity
		SBStatusEntityT sbEnt = new SBStatusEntityT();
		sbEnt.setEntityId(sbStatusUID);
		sbEnt.setEntityIdEncrypted(sbStatusUID);
		sbEnt.setEntityTypeName("SBStatus");
		sbEnt.setDocumentVersion("1.0");
		sbEnt.setDatamodelVersion("1.0");
		sbEnt.setSchemaVersion("1.0");
		sbEnt.setTimestamp(now);		
		sbStatus.setSBStatusEntity(sbEnt);
		// own attributes
		sbStatus.setExecutionsRemaining(1);
		return sbStatus;
	}
	
	public void createStatuses(String obsProjectUID, String schedBlockUID)
		throws Exception {
		String prjStatusUID = addToLastUIDPart(obsProjectUID, 10);
		String ousStatusUID = addToLastUIDPart(obsProjectUID, 1);
		String sbStatusUID = addToLastUIDPart(schedBlockUID, 10);
		ProjectStatus opStatus = createProjectStatus(prjStatusUID, ousStatusUID, obsProjectUID,	
				obsProjectUID); // using project UID as proposal UID
		OUSStatus[] ousStatuses = { createOUSStatus(ousStatusUID, prjStatusUID,	obsProjectUID)};
		SBStatus[] sbStatuses = { createSBStatus(sbStatusUID, ousStatusUID, prjStatusUID,
				obsProjectUID, schedBlockUID) };
		modelAccessor.getStateArchive().insert(opStatus, ousStatuses, sbStatuses);
	}
	
	
	public void traverseProjects() {
		List<ObsProject> projects = modelAccessor.getObsProjectDao().findAll(ObsProject.class);
		for (ObsProject project : projects) {
			ObsUnit obsUnit = modelAccessor.getObsProjectDao().getObsUnitForProject(project);
			traverseObsUnit(obsUnit, project);
		}
	}
	
	public void traverseObsUnit(ObsUnit obsUnit, ObsProject obsProject) {
		if (obsUnit instanceof ObsUnitSet) {
			ObsUnitSet ous = (ObsUnitSet) obsUnit;
			for (ObsUnit ou : ous.getObsUnits()) {
				traverseObsUnit(ou, obsProject);
			}
		} else if (obsUnit instanceof SchedBlock) {
			SchedBlock sb = (SchedBlock) obsUnit;
			try {
				createStatuses(obsProject.getUid(), sb.getUid());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public static String addToLastUIDPart(String baseUID, int addum) {
		int idx = baseUID.lastIndexOf("X");
		String base = baseUID.substring(0, idx+1);
		String ext = baseUID.substring(idx+1);
		String newExt = String.valueOf(Integer.parseInt(ext)+addum);
		return base + newExt;
	}
	
	public static void main(String[] args) {
		try {
			StateArchivePopulator populator = new StateArchivePopulator();
			populator.traverseProjects();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
