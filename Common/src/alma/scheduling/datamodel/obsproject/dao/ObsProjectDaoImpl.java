package alma.scheduling.datamodel.obsproject.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnit;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.ObservationStatus;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.ScienceGrade;

public class ObsProjectDaoImpl implements ObsProjectDao {

	private HashMap<String, ObsProject> obsProjects;
	
	private SchedBlockDao schedBlockDao;
	
	public ObsProjectDaoImpl() {
		obsProjects = new HashMap<>();
	}
	
	@Override
	public int countAll() {
		return obsProjects.size();
	}

	@Override
	public void hydrateSchedBlocks(ObsProject prj) {
		//NO-OP
	}

	@Override
	public ObsUnit getObsUnitForProject(ObsProject prj) {
		prj.getObsUnit();
		return null;
	}

	@Override
	public List<ObsProject> getObsProjectsOrderBySciRank() {
		TreeMap<Integer, ObsProject> sortedMap = new TreeMap<>();
		for (ObsProject p: obsProjects.values()) {
			sortedMap.put(p.getScienceRank(), p);
		}
		return new ArrayList<ObsProject>(sortedMap.values());
	}

	@Override
	public void saveOrUpdate(ObsProject prj) {
		obsProjects.put(prj.getUid(), prj);
		findAndSaveSchedBlocks(prj.getObsUnit(), prj);
	}

	@Override
	public void saveOrUpdate(Collection<ObsProject> prj) {
		for (ObsProject p: prj) {
			obsProjects.put(p.getUid(), p);
			findAndSaveSchedBlocks(p.getObsUnit(), p);
		}
	}
	
	private void findAndSaveSchedBlocks(ObsUnit ou, ObsProject p) {
		ou.setProject(p);
		if (ou instanceof ObsUnitSet)
			for (ObsUnit ouc: ((ObsUnitSet) ou).getObsUnits())
			findAndSaveSchedBlocks(ouc, p);
		else if (ou instanceof SchedBlock)
			schedBlockDao.saveOrUpdate((SchedBlock) ou);
	}

	@Override
	public ObsProject getObsProject(ObsUnit ou) {
		return ou.getProject();
	}

	@Override
	public ObsProject findByEntityId(String entityId) {
		return obsProjects.get(entityId);
	}

	@Override
	public void deleteAll() {
		obsProjects = new HashMap<>();
		schedBlockDao.deleteAll();
	}

	@Override
	public void refreshProject(ObsProject prj) {
		//NO-OP
	}

	@Override
	public void refreshProjects(List<ObsProject> list) {
		//NO-OP
	}

	@Override
	public List<String> getObsProjectsUidsByCode(String code) {
		//Not used
		return null;
	}

	@Override
	public List<String> getObsProjectsUidsbySciGrade(List<ScienceGrade> grades) {
		// Not used
		return null;
	}

	@Override
	public void setObsProjectStatusAsReady() {
		for (ObsProject p: obsProjects.values()) {
			p.setStatus(ObservationStatus.NOT_STARTED);
			if (p.getObsUnit() != null)
				setObsUnitStatusAsReady(p.getObsUnit());
		}
	}
	
	private void setObsUnitStatusAsReady(ObsUnit ou) {
		if (ou instanceof ObsUnitSet) {
			ou.setStatus(ObservationStatus.NOT_STARTED);
			for (ObsUnit ouc: ((ObsUnitSet) ou).getObsUnits())
				setObsUnitStatusAsReady(ouc);
		}
		else if (ou instanceof SchedBlock) {
			SchedBlock sb = (SchedBlock) ou;
			sb.setStatus(ObservationStatus.READY);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Collection<T> findAll(Class<T> clazz) {
		if (!clazz.isInstance(new ObsProject()))
			return null;
		return (Collection<T>) obsProjects.values();
	}

	public void setSchedBlockDao(SchedBlockDao schedBlockDao) {
		this.schedBlockDao = schedBlockDao;
	}
	
}
